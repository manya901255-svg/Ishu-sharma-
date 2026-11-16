const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const serverTimestamp = () => new Date(Date.now() - 500);
const Timestamp = { now: () => new Date(Date.now() - 1000) };

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("1. Unauthenticated callers cannot read users, public_profiles, voice_clubs, or match_rooms", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("public_profiles").where("level", ">=", 1).get());
  await assertFails(unauthDb.collection("voice_clubs").where("isPrivate", "==", false).get());
  await assertFails(unauthDb.collection("match_rooms").where("isPrivate", "==", false).get());
});

test("2. Cross-user profile & wallet isolation", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      goldCoins: 5000,
      cashNotes: 20,
      xp: 100,
      level: 1,
      referralCode: "ALICE99",
      createdAt: Timestamp.now(),
      updatedAt: Timestamp.now(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ goldCoins: 999999, updatedAt: serverTimestamp() }));

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("3. Incomplete task cannot be claimed (server rule enforcement)", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("tasks").doc("daily_play_3").set({
      taskId: "daily_play_3",
      userId: ALICE_UID,
      category: "DAILY",
      title: "Play 3 Ludo matches",
      progress: 1,
      target: 3,
      rewardCoins: 100,
      rewardCash: 0,
      status: "IN_PROGRESS",
      periodKey: "2026-11-15",
      updatedAt: Timestamp.now(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const batch = aliceDb.batch();
  batch.set(
    aliceDb.collection("users").doc(ALICE_UID).collection("reward_history").doc("tx_fake_1"),
    {
      txId: "tx_fake_1",
      userId: ALICE_UID,
      sourceType: "DAILY_TASK",
      sourceId: "daily_play_3",
      periodKey: "2026-11-15",
      coinsDelta: 100,
      cashDelta: 0,
      description: "Fake claim",
      createdAt: serverTimestamp(),
    }
  );
  batch.update(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("daily_play_3"),
    {
      status: "CLAIMED",
      rewardTxId: "tx_fake_1",
      claimedAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    }
  );
  await assertFails(batch.commit());
});

test("4 & 5. Completed task claims once with atomic ledger, then duplicate claim is rejected", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("tasks").doc("daily_play_3").set({
      taskId: "daily_play_3",
      userId: ALICE_UID,
      category: "DAILY",
      title: "Play 3 Ludo matches",
      progress: 3,
      target: 3,
      rewardCoins: 100,
      rewardCash: 0,
      status: "COMPLETED",
      periodKey: "2026-11-15",
      updatedAt: Timestamp.now(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Claim without ledger entry must FAIL
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("daily_play_3").update({
      status: "CLAIMED",
      rewardTxId: "missing_tx",
      claimedAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Atomic claim WITH ledger entry must SUCCEED
  const batch1 = aliceDb.batch();
  batch1.set(
    aliceDb.collection("users").doc(ALICE_UID).collection("reward_history").doc("tx_valid_1"),
    {
      txId: "tx_valid_1",
      userId: ALICE_UID,
      sourceType: "DAILY_TASK",
      sourceId: "daily_play_3",
      periodKey: "2026-11-15",
      coinsDelta: 100,
      cashDelta: 0,
      description: "Play 3 Ludo matches completed",
      createdAt: serverTimestamp(),
    }
  );
  batch1.update(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("daily_play_3"),
    {
      status: "CLAIMED",
      rewardTxId: "tx_valid_1",
      claimedAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    }
  );
  await assertSucceeds(batch1.commit());

  // Duplicate claim on already CLAIMED task must FAIL
  const batch2 = aliceDb.batch();
  batch2.set(
    aliceDb.collection("users").doc(ALICE_UID).collection("reward_history").doc("tx_valid_2"),
    {
      txId: "tx_valid_2",
      userId: ALICE_UID,
      sourceType: "DAILY_TASK",
      sourceId: "daily_play_3",
      periodKey: "2026-11-15",
      coinsDelta: 100,
      cashDelta: 0,
      description: "Duplicate claim",
      createdAt: serverTimestamp(),
    }
  );
  batch2.update(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("daily_play_3"),
    {
      status: "CLAIMED",
      rewardTxId: "tx_valid_2",
      claimedAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    }
  );
  await assertFails(batch2.commit());
});

test("6. Private Ludo room rejects unauthorized/non-invited outsider and admits invited user", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("match_rooms").doc("priv_room_1").set({
      roomId: "priv_room_1",
      roomCode: "ROYAL7",
      ownerId: ALICE_UID,
      isPrivate: true,
      allowedUids: [ALICE_UID],
      invitedUids: [BOB_UID],
      playerUids: [ALICE_UID],
      maxPlayers: 4,
      gameMode: "PRIVATE_ROOM",
      wagerCoins: 500,
      status: "WAITING",
      currentTurnUid: ALICE_UID,
      moveSequence: 0,
      boardState: { diceValue: 1 },
      createdAt: Timestamp.now(),
      updatedAt: Timestamp.now(),
    });
  });

  // Charlie is NOT invited and does NOT know code
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(charlieDb.collection("match_rooms").doc("priv_room_1").get());
  await assertFails(
    charlieDb.collection("match_rooms").doc("priv_room_1").update({
      playerUids: [ALICE_UID, CHARLIE_UID],
      allowedUids: [ALICE_UID, CHARLIE_UID],
      attemptedRoomCode: "WRONG1",
      updatedAt: serverTimestamp(),
    })
  );

  // Bob IS invited and can join
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("match_rooms").doc("priv_room_1").update({
      playerUids: [ALICE_UID, BOB_UID],
      allowedUids: [ALICE_UID, BOB_UID],
      updatedAt: serverTimestamp(),
    })
  );
});

test("7. Voice Club bans & moderation role enforcement", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("voice_clubs").doc("club_royal").set({
      clubId: "club_royal",
      name: "Royal Warriors",
      description: "Voice Ludo Club",
      avatarEmoji: "👑",
      ownerId: ALICE_UID,
      ownerName: "Alice",
      isPrivate: false,
      memberCount: 1,
      clubXp: 100,
      memberUids: [ALICE_UID],
      createdAt: Timestamp.now(),
      updatedAt: Timestamp.now(),
    });
    await context.firestore().collection("voice_clubs").doc("club_royal").collection("members").doc(ALICE_UID).set({
      uid: ALICE_UID,
      clubId: "club_royal",
      displayName: "Alice",
      role: "OWNER",
      isOnline: true,
      isMutedByMod: false,
      joinedAt: Timestamp.now(),
      updatedAt: Timestamp.now(),
    });
    await context.firestore().collection("voice_clubs").doc("club_royal").collection("banned").doc(CHARLIE_UID).set({
      uid: CHARLIE_UID,
      clubId: "club_royal",
      bannedByUid: ALICE_UID,
      reason: "Abusive voice behavior",
      createdAt: Timestamp.now(),
    });
  });

  // Banned Charlie cannot join member subcollection
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(
    charlieDb.collection("voice_clubs").doc("club_royal").collection("members").doc(CHARLIE_UID).set({
      uid: CHARLIE_UID,
      clubId: "club_royal",
      displayName: "Charlie",
      role: "MEMBER",
      isOnline: true,
      isMutedByMod: false,
      joinedAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Regular member Bob cannot post announcement
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("voice_clubs").doc("club_royal").collection("members").doc(BOB_UID).set({
      uid: BOB_UID,
      clubId: "club_royal",
      displayName: "Bob",
      role: "MEMBER",
      isOnline: true,
      isMutedByMod: false,
      joinedAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
  await assertFails(
    bobDb.collection("voice_clubs").doc("club_royal").collection("messages").doc("msg_1").set({
      messageId: "msg_1",
      clubId: "club_royal",
      senderUid: BOB_UID,
      senderName: "Bob",
      text: "Fake announcement",
      isAnnouncement: true,
      isPublicClub: true,
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: serverTimestamp(),
    })
  );
  await assertSucceeds(
    bobDb.collection("voice_clubs").doc("club_royal").collection("messages").doc("msg_2").set({
      messageId: "msg_2",
      clubId: "club_royal",
      senderUid: BOB_UID,
      senderName: "Bob",
      text: "Hello club!",
      isAnnouncement: false,
      isPublicClub: true,
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: serverTimestamp(),
    })
  );
});

test("8. Shadow update with ghost field & unverified admin email spoofing rejected", async () => {
  const spoofAdminDb = testEnv.authenticatedContext("spoof_1", {
    email: "manya901255@gmail.com",
    email_verified: false,
  }).firestore();

  await assertFails(spoofAdminDb.collection("users").doc(ALICE_UID).get());

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      goldCoins: 5000,
      cashNotes: 20,
      xp: 0,
      level: 1,
      referralCode: "ALICE1",
      isAdmin: true,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
});
