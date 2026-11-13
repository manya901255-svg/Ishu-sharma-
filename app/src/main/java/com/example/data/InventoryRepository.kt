package com.example.data

import android.content.Context
import com.example.model.*
import com.example.util.ProfilePreferences
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Authoritative Inventory & Economy Transaction Engine.
 * Enforces:
 * - Concurrency-safe atomic mutations via Mutex (no double-deduction / race conditions)
 * - Strict balance validation (never goes negative)
 * - Duplicate ownership prevention for non-consumables
 * - Server-style ad & event progress verification with transaction ID deduplication
 * - Powerup cooldowns, daily usage caps, and active timers
 * - Per-playerId scoping for multi-account / logout-login persistence
 */
class InventoryRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.inventoryDao()
    private val prefs = ProfilePreferences(context)
    private val txMutex = Mutex()

    private fun key(playerId: String, id: String) = "${playerId}_$id"
    private fun slotKey(playerId: String, index: Int) = "${playerId}_slot_$index"
    private fun currentDayIndex(nowMillis: Long = System.currentTimeMillis()): Long =
        nowMillis / (1000L * 60L * 60L * 24L)

    /**
     * Initializes player inventory tables if missing, seeding only starter defaults
     * and migrating any pre-existing ProfilePreferences owned items/balances.
     */
    suspend fun ensureInitialized(
        playerId: String = prefs.getCurrentPlayerId(),
        initialCoins: Long = prefs.getUserCoins(),
        initialCash: Int = prefs.getUserGems()
    ): PlayerInventory = txMutex.withLock {
        ensureInitializedLocked(playerId, initialCoins, initialCash)
    }

    private suspend fun ensureInitializedLocked(
        playerId: String,
        initialCoins: Long,
        initialCash: Int
    ): PlayerInventory {
        var wallet = dao.getWallet(playerId)
        if (wallet == null) {
            wallet = PlayerWalletEntity(
                playerId = playerId,
                goldCoins = initialCoins.coerceAtLeast(0L),
                cashNotes = initialCash.coerceAtLeast(0)
            )
            dao.upsertWallet(wallet)
        }

        // Seed starter items ONLY (never auto-unlock rewarded/paid items)
        val existingOwned = dao.getOwnedItems(playerId).map { it.itemId }.toSet()
        for (defaultItem in ItemCatalog.ALL_ITEMS.filter { it.defaultOwned }) {
            if (defaultItem.id !in existingOwned) {
                dao.upsertOwnedItem(
                    OwnedItemEntity(
                        compositeId = key(playerId, defaultItem.id),
                        playerId = playerId,
                        itemId = defaultItem.id,
                        quantity = 1,
                        source = "STARTER"
                    )
                )
            }
        }

        // Migrate any items already unlocked via RoyalShopScreen in ProfilePreferences
        if (playerId == ProfilePreferences.DEFAULT_PLAYER_ID) {
            val legacyOwned = prefs.getOwnedItems()
            for (legacyId in legacyOwned) {
                val catalogItem = ItemCatalog.findById(legacyId)
                if (catalogItem != null && legacyId !in existingOwned) {
                    dao.upsertOwnedItem(
                        OwnedItemEntity(
                            compositeId = key(playerId, legacyId),
                            playerId = playerId,
                            itemId = legacyId,
                            quantity = 1,
                            source = "LEGACY_MIGRATION"
                        )
                    )
                }
            }
        }

        // Seed equipped loadout if missing
        if (dao.getEquippedLoadout(playerId) == null) {
            dao.upsertEquippedLoadout(
                EquippedLoadoutEntity(
                    playerId = playerId,
                    diceId = prefs.getEquippedDiceSkin().takeIf { ItemCatalog.findById(it) != null } ?: "dice_imperial_gold",
                    frameId = prefs.getEquippedFrameId().takeIf { ItemCatalog.findById(it) != null } ?: "frame_royal_crest",
                    boardId = prefs.getBoardTheme().takeIf { ItemCatalog.findById(it) != null } ?: "imperial_navy",
                    pawnId = prefs.getEquippedTokenSkin().takeIf { ItemCatalog.findById(it) != null } ?: "crown_maharaja",
                    pawnTrailEnabled = prefs.isPawnTrailEnabled()
                )
            )
        }

        // Seed 18 Emoji Deck slots if missing (putting starter emojis in slots 0..1)
        val existingSlots = dao.getEmojiDeckSlots(playerId)
        if (existingSlots.isEmpty()) {
            val starterEmojiIds = listOf("emoji_royal_crown", "emoji_fire_streak")
            for (i in 0 until EmojiDeck.MAX_SLOTS) {
                dao.upsertEmojiDeckSlot(
                    EmojiDeckSlotEntity(
                        compositeId = slotKey(playerId, i),
                        playerId = playerId,
                        slotIndex = i,
                        emojiItemId = starterEmojiIds.getOrNull(i)
                    )
                )
            }
        }

        // Initialize Progress records (0/50) for progress-gated items if not created
        for (item in ItemCatalog.ALL_ITEMS.filter { it.progressTarget > 0 }) {
            val progressComposite = key(playerId, item.id)
            if (dao.getEventProgress(progressComposite) == null) {
                dao.upsertEventProgress(
                    EventProgressEntity(
                        compositeId = progressComposite,
                        playerId = playerId,
                        eventOrItemId = item.id,
                        currentProgress = 0,
                        targetProgress = item.progressTarget,
                        isCompleted = false,
                        isClaimed = false,
                        expiresAtMillis = Long.MAX_VALUE
                    )
                )
            }
        }

        return loadInventorySnapshotLocked(playerId)
    }

    suspend fun getInventorySnapshot(playerId: String = prefs.getCurrentPlayerId()): PlayerInventory =
        txMutex.withLock {
            ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        }

    private suspend fun loadInventorySnapshotLocked(playerId: String): PlayerInventory {
        val wallet = dao.getWallet(playerId) ?: PlayerWalletEntity(playerId, prefs.getUserCoins(), prefs.getUserGems())
        val owned = dao.getOwnedItems(playerId).map {
            OwnedItems(it.playerId, it.itemId, it.quantity, it.unlockedAt, it.source)
        }
        val loadoutEntity = dao.getEquippedLoadout(playerId) ?: EquippedLoadoutEntity(playerId)
        val loadout = EquippedItems(
            playerId = loadoutEntity.playerId,
            diceId = loadoutEntity.diceId,
            frameId = loadoutEntity.frameId,
            boardId = loadoutEntity.boardId,
            pawnId = loadoutEntity.pawnId,
            pawnTrailEnabled = loadoutEntity.pawnTrailEnabled
        )
        val slotEntities = dao.getEmojiDeckSlots(playerId).sortedBy { it.slotIndex }
        val slots = MutableList<String?>(EmojiDeck.MAX_SLOTS) { null }
        for (s in slotEntities) {
            if (s.slotIndex in 0 until EmojiDeck.MAX_SLOTS) {
                slots[s.slotIndex] = s.emojiItemId
            }
        }
        val powerups = dao.getActivePowerups(playerId).map {
            ActivePowerups(
                playerId = it.playerId,
                itemId = it.itemId,
                powerupType = it.powerupType,
                quantity = it.quantity,
                activeUntilMillis = it.activeUntilMillis,
                lastActivatedMillis = it.lastActivatedMillis,
                dailyUsedCount = it.dailyUsedCount,
                dailyResetDay = it.dailyResetDay
            )
        }
        val events = dao.getEventProgressList(playerId).map {
            EventProgress(
                playerId = it.playerId,
                eventOrItemId = it.eventOrItemId,
                currentProgress = it.currentProgress,
                targetProgress = it.targetProgress,
                isCompleted = it.isCompleted,
                isClaimed = it.isClaimed,
                expiresAtMillis = it.expiresAtMillis
            )
        }
        return PlayerInventory(
            playerId = playerId,
            currency = PlayerCurrency(playerId, wallet.goldCoins, wallet.cashNotes),
            ownedItems = owned,
            equippedItems = loadout,
            emojiDeck = EmojiDeck(playerId, slots),
            activePowerups = powerups,
            eventProgress = events
        )
    }

    /**
     * Synchronizes external balance mutations (e.g., match winnings, daily rewards, spin wheel)
     * into the authoritative Room wallet while keeping positive bounds.
     */
    suspend fun syncWalletBalance(
        playerId: String = prefs.getCurrentPlayerId(),
        goldCoins: Long,
        cashNotes: Int
    ): PlayerInventory = txMutex.withLock {
        ensureInitializedLocked(playerId, goldCoins, cashNotes)
        dao.upsertWallet(
            PlayerWalletEntity(
                playerId = playerId,
                goldCoins = goldCoins.coerceAtLeast(0L),
                cashNotes = cashNotes.coerceAtLeast(0),
                updatedAt = System.currentTimeMillis()
            )
        )
        loadInventorySnapshotLocked(playerId)
    }

    /**
     * Adjusts currency atomically by delta (never allows negative balance).
     */
    suspend fun adjustCurrency(
        playerId: String = prefs.getCurrentPlayerId(),
        coinsDelta: Long = 0L,
        cashDelta: Int = 0
    ): PlayerCurrency = txMutex.withLock {
        ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        val wallet = dao.getWallet(playerId)!!
        val nextCoins = (wallet.goldCoins + coinsDelta).coerceAtLeast(0L)
        val nextCash = (wallet.cashNotes + cashDelta).coerceAtLeast(0)
        dao.upsertWallet(
            wallet.copy(goldCoins = nextCoins, cashNotes = nextCash, updatedAt = System.currentTimeMillis())
        )
        if (playerId == prefs.getCurrentPlayerId()) {
            prefs.setUserCoins(nextCoins)
            prefs.setUserGems(nextCash)
        }
        PlayerCurrency(playerId, nextCoins, nextCash)
    }

    /**
     * Atomically validates and executes an inventory item purchase.
     * Protected by Mutex so simultaneous calls cannot double-spend or double-grant.
     */
    suspend fun purchaseItem(
        playerId: String = prefs.getCurrentPlayerId(),
        itemId: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<PurchaseResult, PlayerInventory> = txMutex.withLock {
        ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        val item = ItemCatalog.findById(itemId)
        if (item == null) {
            return@withLock PurchaseResult.NotFound(itemId) to loadInventorySnapshotLocked(playerId)
        }

        val existingOwned = dao.getOwnedItem(key(playerId, itemId))
        if (!item.isConsumable && existingOwned != null && existingOwned.quantity > 0) {
            return@withLock PurchaseResult.AlreadyOwned(item) to loadInventorySnapshotLocked(playerId)
        }

        // Event expiry & progress validation
        if (item.isEventOnly && item.eventId != null) {
            val eventProg = dao.getEventProgress(key(playerId, item.id))
            if (eventProg != null && eventProg.expiresAtMillis < nowMillis) {
                return@withLock PurchaseResult.EventExpired(item.eventId) to loadInventorySnapshotLocked(playerId)
            }
            if (item.progressTarget > 0) {
                val cur = eventProg?.currentProgress ?: 0
                if (cur < item.progressTarget) {
                    return@withLock PurchaseResult.RequirementNotMet(
                        "Event progress incomplete ($cur/${item.progressTarget})"
                    ) to loadInventorySnapshotLocked(playerId)
                }
            }
        }

        if (item.currencyType == CurrencyType.AD_REWARD && item.progressTarget > 0) {
            val prog = dao.getEventProgress(key(playerId, item.id))
            val cur = prog?.currentProgress ?: 0
            if (cur < item.progressTarget) {
                return@withLock PurchaseResult.RequirementNotMet(
                    "Watch ${item.progressTarget - cur} more verified ads ($cur/${item.progressTarget})"
                ) to loadInventorySnapshotLocked(playerId)
            }
        }

        val wallet = dao.getWallet(playerId)!!
        val availableBalance: Long = when (item.currencyType) {
            CurrencyType.COINS -> wallet.goldCoins
            CurrencyType.CASH -> wallet.cashNotes.toLong()
            CurrencyType.FREE, CurrencyType.AD_REWARD, CurrencyType.EVENT, CurrencyType.CHEST -> 0L
        }

        if ((item.currencyType == CurrencyType.COINS || item.currencyType == CurrencyType.CASH) && availableBalance < item.price) {
            return@withLock PurchaseResult.InsufficientBalance(
                required = item.price,
                available = availableBalance,
                currencyType = item.currencyType
            ) to loadInventorySnapshotLocked(playerId)
        }

        // Deduct currency safely
        val newCoins = if (item.currencyType == CurrencyType.COINS) wallet.goldCoins - item.price else wallet.goldCoins
        val newCash = if (item.currencyType == CurrencyType.CASH) wallet.cashNotes - item.price.toInt() else wallet.cashNotes

        dao.upsertWallet(
            wallet.copy(
                goldCoins = newCoins.coerceAtLeast(0L),
                cashNotes = newCash.coerceAtLeast(0),
                updatedAt = nowMillis
            )
        )
        if (playerId == prefs.getCurrentPlayerId()) {
            prefs.setUserCoins(newCoins)
            prefs.setUserGems(newCash)
            prefs.addOwnedItem(item.id)
        }

        // Grant ownership (or increment consumable stack)
        val newQty = if (item.isConsumable) (existingOwned?.quantity ?: 0) + 1 else 1
        dao.upsertOwnedItem(
            OwnedItemEntity(
                compositeId = key(playerId, item.id),
                playerId = playerId,
                itemId = item.id,
                quantity = newQty,
                unlockedAt = existingOwned?.unlockedAt ?: nowMillis,
                source = if (item.price == 0L) item.currencyType.name else "PURCHASE"
            )
        )

        // If powerup, also sync ActivePowerupEntity stack
        if (item.category == InventoryCategory.POWERUP) {
            val existingPowerup = dao.getActivePowerup(key(playerId, item.id))
            dao.upsertActivePowerup(
                ActivePowerupEntity(
                    compositeId = key(playerId, item.id),
                    playerId = playerId,
                    itemId = item.id,
                    powerupType = item.powerupEffectType ?: item.id,
                    quantity = newQty,
                    activeUntilMillis = existingPowerup?.activeUntilMillis ?: 0L,
                    lastActivatedMillis = existingPowerup?.lastActivatedMillis ?: 0L,
                    dailyUsedCount = existingPowerup?.dailyUsedCount ?: 0,
                    dailyResetDay = existingPowerup?.dailyResetDay ?: currentDayIndex(nowMillis)
                )
            )
        }

        val txId = "tx_${UUID.randomUUID()}"
        val txEntity = PurchaseTransactionEntity(
            transactionId = txId,
            playerId = playerId,
            itemId = item.id,
            amount = item.price,
            currencyType = item.currencyType.name,
            status = "COMPLETED",
            timestamp = nowMillis
        )
        dao.insertPurchaseTransaction(txEntity)

        val resultTx = PurchaseTransactions(
            transactionId = txId,
            playerId = playerId,
            itemId = item.id,
            amount = item.price,
            currencyType = item.currencyType,
            status = "COMPLETED",
            timestamp = nowMillis
        )
        return@withLock PurchaseResult.Success(item, resultTx) to loadInventorySnapshotLocked(playerId)
    }

    /**
     * Grants an item directly from a verified Chest drop or Admin grant.
     */
    suspend fun grantItem(
        playerId: String = prefs.getCurrentPlayerId(),
        itemId: String,
        quantityToAdd: Int = 1,
        source: String = "CHEST"
    ): PlayerInventory = txMutex.withLock {
        ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        val item = ItemCatalog.findById(itemId) ?: return@withLock loadInventorySnapshotLocked(playerId)
        val existing = dao.getOwnedItem(key(playerId, itemId))
        val nextQty = if (item.isConsumable) (existing?.quantity ?: 0) + quantityToAdd else 1
        dao.upsertOwnedItem(
            OwnedItemEntity(
                compositeId = key(playerId, itemId),
                playerId = playerId,
                itemId = itemId,
                quantity = nextQty,
                unlockedAt = existing?.unlockedAt ?: System.currentTimeMillis(),
                source = source
            )
        )
        if (item.category == InventoryCategory.POWERUP) {
            val existingPowerup = dao.getActivePowerup(key(playerId, itemId))
            dao.upsertActivePowerup(
                ActivePowerupEntity(
                    compositeId = key(playerId, itemId),
                    playerId = playerId,
                    itemId = itemId,
                    powerupType = item.powerupEffectType ?: itemId,
                    quantity = nextQty,
                    activeUntilMillis = existingPowerup?.activeUntilMillis ?: 0L,
                    lastActivatedMillis = existingPowerup?.lastActivatedMillis ?: 0L,
                    dailyUsedCount = existingPowerup?.dailyUsedCount ?: 0,
                    dailyResetDay = existingPowerup?.dailyResetDay ?: currentDayIndex()
                )
            )
        }
        if (playerId == prefs.getCurrentPlayerId()) {
            prefs.addOwnedItem(itemId)
        }
        loadInventorySnapshotLocked(playerId)
    }

    /**
     * Server-verified rewarded ad callback.
     * - If verified == false (skipped/closed/failed), NO currency and NO progress is granted.
     * - Deduplicates by adTransactionId so replay attacks cannot credit twice.
     * - Increments progress for AD_REWARD items (e.g. Guy Crying 0/50) and unlocks at target.
     */
    suspend fun recordVerifiedAdCompletion(
        playerId: String = prefs.getCurrentPlayerId(),
        adTransactionId: String,
        verified: Boolean,
        coinsReward: Long = 0L,
        cashReward: Int = 0,
        targetProgressItemId: String? = "emoji_guy_crying",
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<AdVerificationResult, PlayerInventory> = txMutex.withLock {
        ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        if (!verified) {
            return@withLock AdVerificationResult.NotVerified("Ad skipped or failed verification") to loadInventorySnapshotLocked(playerId)
        }
        if (dao.getRewardTransaction(adTransactionId) != null) {
            return@withLock AdVerificationResult.DuplicateTransaction(adTransactionId) to loadInventorySnapshotLocked(playerId)
        }

        val wallet = dao.getWallet(playerId)!!
        val newCoins = wallet.goldCoins + coinsReward.coerceAtLeast(0L)
        val newCash = wallet.cashNotes + cashReward.coerceAtLeast(0)
        dao.upsertWallet(wallet.copy(goldCoins = newCoins, cashNotes = newCash, updatedAt = nowMillis))

        if (playerId == prefs.getCurrentPlayerId()) {
            prefs.setUserCoins(newCoins)
            prefs.setUserGems(newCash)
        }

        val unlockedIds = mutableListOf<String>()
        val progressTargets = if (targetProgressItemId != null) {
            listOfNotNull(ItemCatalog.findById(targetProgressItemId))
        } else {
            ItemCatalog.ALL_ITEMS.filter { it.currencyType == CurrencyType.AD_REWARD && it.progressTarget > 0 }
        }

        for (item in progressTargets) {
            val progKey = key(playerId, item.id)
            val current = dao.getEventProgress(progKey)
            val prevCount = current?.currentProgress ?: 0
            val nextCount = (prevCount + 1).coerceAtMost(item.progressTarget)
            val completed = nextCount >= item.progressTarget
            dao.upsertEventProgress(
                EventProgressEntity(
                    compositeId = progKey,
                    playerId = playerId,
                    eventOrItemId = item.id,
                    currentProgress = nextCount,
                    targetProgress = item.progressTarget,
                    isCompleted = completed,
                    isClaimed = completed || (current?.isClaimed == true),
                    expiresAtMillis = current?.expiresAtMillis ?: Long.MAX_VALUE
                )
            )
            if (completed) {
                val owned = dao.getOwnedItem(key(playerId, item.id))
                if (owned == null || owned.quantity == 0) {
                    dao.upsertOwnedItem(
                        OwnedItemEntity(
                            compositeId = key(playerId, item.id),
                            playerId = playerId,
                            itemId = item.id,
                            quantity = 1,
                            unlockedAt = nowMillis,
                            source = "AD_MILESTONE"
                        )
                    )
                    if (playerId == prefs.getCurrentPlayerId()) prefs.addOwnedItem(item.id)
                    unlockedIds.add(item.id)
                }
            }
        }

        dao.insertRewardTransaction(
            RewardTransactionEntity(
                rewardId = adTransactionId,
                playerId = playerId,
                adOrEventId = "AD_VERIFIED",
                itemId = targetProgressItemId,
                coinsGranted = coinsReward,
                cashGranted = cashReward,
                verified = true,
                timestamp = nowMillis
            )
        )

        return@withLock AdVerificationResult.Verified(
            transactionId = adTransactionId,
            coinsGranted = coinsReward,
            cashGranted = cashReward,
            unlockedItemIds = unlockedIds
        ) to loadInventorySnapshotLocked(playerId)
    }

    /**
     * Increments verified event progress (e.g., Squall 0/50) and unlocks once completed.
     * Rejects updates if event window has expired.
     */
    suspend fun incrementEventItemProgress(
        playerId: String = prefs.getCurrentPlayerId(),
        itemId: String,
        delta: Int = 1,
        expiresAtMillis: Long = Long.MAX_VALUE,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<Boolean, PlayerInventory> = txMutex.withLock {
        ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        val item = ItemCatalog.findById(itemId) ?: return@withLock false to loadInventorySnapshotLocked(playerId)
        val progKey = key(playerId, itemId)
        val existing = dao.getEventProgress(progKey)
        val effectiveExpiry = existing?.expiresAtMillis ?: expiresAtMillis
        if (effectiveExpiry < nowMillis) {
            return@withLock false to loadInventorySnapshotLocked(playerId)
        }
        val target = item.progressTarget.coerceAtLeast(1)
        val nextCount = ((existing?.currentProgress ?: 0) + delta).coerceIn(0, target)
        val completed = nextCount >= target
        dao.upsertEventProgress(
            EventProgressEntity(
                compositeId = progKey,
                playerId = playerId,
                eventOrItemId = itemId,
                currentProgress = nextCount,
                targetProgress = target,
                isCompleted = completed,
                isClaimed = completed || (existing?.isClaimed == true),
                expiresAtMillis = effectiveExpiry
            )
        )
        if (completed) {
            val owned = dao.getOwnedItem(key(playerId, itemId))
            if (owned == null || owned.quantity == 0) {
                dao.upsertOwnedItem(
                    OwnedItemEntity(
                        compositeId = key(playerId, itemId),
                        playerId = playerId,
                        itemId = itemId,
                        quantity = 1,
                        unlockedAt = nowMillis,
                        source = "EVENT"
                    )
                )
                if (playerId == prefs.getCurrentPlayerId()) prefs.addOwnedItem(itemId)
            }
        }
        return@withLock completed to loadInventorySnapshotLocked(playerId)
    }

    /**
     * Equips an owned item (Dice, Frame, Board, or Pawn) and syncs ProfilePreferences.
     */
    suspend fun equipItem(
        playerId: String = prefs.getCurrentPlayerId(),
        itemId: String
    ): Pair<Boolean, PlayerInventory> = txMutex.withLock {
        ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        val item = ItemCatalog.findById(itemId) ?: return@withLock false to loadInventorySnapshotLocked(playerId)
        val owned = dao.getOwnedItem(key(playerId, itemId))
        if (owned == null || owned.quantity <= 0) {
            return@withLock false to loadInventorySnapshotLocked(playerId)
        }

        val currentLoadout = dao.getEquippedLoadout(playerId) ?: EquippedLoadoutEntity(playerId)
        val updated = when (item.category) {
            InventoryCategory.DICE -> currentLoadout.copy(diceId = item.id, updatedAt = System.currentTimeMillis())
            InventoryCategory.FRAME -> currentLoadout.copy(frameId = item.id, updatedAt = System.currentTimeMillis())
            InventoryCategory.BOARD -> currentLoadout.copy(boardId = item.id, updatedAt = System.currentTimeMillis())
            InventoryCategory.PAWN -> currentLoadout.copy(pawnId = item.id, updatedAt = System.currentTimeMillis())
            else -> return@withLock false to loadInventorySnapshotLocked(playerId)
        }
        dao.upsertEquippedLoadout(updated)

        if (playerId == prefs.getCurrentPlayerId()) {
            when (item.category) {
                InventoryCategory.DICE -> prefs.setEquippedDiceSkin(item.id)
                InventoryCategory.FRAME -> prefs.setEquippedFrameId(item.id)
                InventoryCategory.BOARD -> prefs.setBoardTheme(item.id)
                InventoryCategory.PAWN -> prefs.setEquippedTokenSkin(item.id)
                else -> {}
            }
        }
        return@withLock true to loadInventorySnapshotLocked(playerId)
    }

    /**
     * Unequips a slot back to its default starter item.
     */
    suspend fun unequipCategory(
        playerId: String = prefs.getCurrentPlayerId(),
        category: InventoryCategory
    ): PlayerInventory = txMutex.withLock {
        ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        val currentLoadout = dao.getEquippedLoadout(playerId) ?: EquippedLoadoutEntity(playerId)
        val updated = when (category) {
            InventoryCategory.DICE -> currentLoadout.copy(diceId = ProfilePreferences.DEFAULT_DICE_SKIN)
            InventoryCategory.FRAME -> currentLoadout.copy(frameId = ProfilePreferences.DEFAULT_EQUIPPED_FRAME)
            InventoryCategory.BOARD -> currentLoadout.copy(boardId = ProfilePreferences.DEFAULT_BOARD_THEME)
            InventoryCategory.PAWN -> currentLoadout.copy(pawnId = ProfilePreferences.DEFAULT_TOKEN_SKIN)
            else -> currentLoadout
        }
        dao.upsertEquippedLoadout(updated)
        if (playerId == prefs.getCurrentPlayerId()) {
            prefs.setEquippedDiceSkin(updated.diceId)
            prefs.setEquippedFrameId(updated.frameId)
            prefs.setBoardTheme(updated.boardId)
            prefs.setEquippedTokenSkin(updated.pawnId)
        }
        loadInventorySnapshotLocked(playerId)
    }

    suspend fun setPawnTrailEnabled(
        playerId: String = prefs.getCurrentPlayerId(),
        enabled: Boolean
    ): PlayerInventory = txMutex.withLock {
        ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        val currentLoadout = dao.getEquippedLoadout(playerId) ?: EquippedLoadoutEntity(playerId)
        dao.upsertEquippedLoadout(currentLoadout.copy(pawnTrailEnabled = enabled, updatedAt = System.currentTimeMillis()))
        if (playerId == prefs.getCurrentPlayerId()) {
            prefs.setPawnTrailEnabled(enabled)
        }
        loadInventorySnapshotLocked(playerId)
    }

    // ======================================================
    // 18-SLOT EMOJI DECK MANAGEMENT
    // ======================================================

    suspend fun assignEmojiToDeckSlot(
        playerId: String = prefs.getCurrentPlayerId(),
        slotIndex: Int,
        emojiItemId: String
    ): Pair<Boolean, PlayerInventory> = txMutex.withLock {
        ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        if (slotIndex !in 0 until EmojiDeck.MAX_SLOTS) {
            return@withLock false to loadInventorySnapshotLocked(playerId)
        }
        val item = ItemCatalog.findById(emojiItemId)
        val owned = dao.getOwnedItem(key(playerId, emojiItemId))
        if (item == null || item.category != InventoryCategory.EMOJI || owned == null || owned.quantity <= 0) {
            return@withLock false to loadInventorySnapshotLocked(playerId)
        }

        // If already present in another slot, remove old slot so each slot holds clean assignment (rearrange/replace)
        val allSlots = dao.getEmojiDeckSlots(playerId)
        for (slot in allSlots) {
            if (slot.slotIndex != slotIndex && slot.emojiItemId == emojiItemId) {
                dao.upsertEmojiDeckSlot(slot.copy(emojiItemId = null))
            }
        }
        dao.upsertEmojiDeckSlot(
            EmojiDeckSlotEntity(
                compositeId = slotKey(playerId, slotIndex),
                playerId = playerId,
                slotIndex = slotIndex,
                emojiItemId = emojiItemId
            )
        )
        return@withLock true to loadInventorySnapshotLocked(playerId)
    }

    suspend fun removeEmojiFromDeckSlot(
        playerId: String = prefs.getCurrentPlayerId(),
        slotIndex: Int
    ): PlayerInventory = txMutex.withLock {
        ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        if (slotIndex in 0 until EmojiDeck.MAX_SLOTS) {
            dao.upsertEmojiDeckSlot(
                EmojiDeckSlotEntity(
                    compositeId = slotKey(playerId, slotIndex),
                    playerId = playerId,
                    slotIndex = slotIndex,
                    emojiItemId = null
                )
            )
        }
        loadInventorySnapshotLocked(playerId)
    }

    suspend fun swapEmojiDeckSlots(
        playerId: String = prefs.getCurrentPlayerId(),
        fromSlot: Int,
        toSlot: Int
    ): PlayerInventory = txMutex.withLock {
        ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        if (fromSlot in 0 until EmojiDeck.MAX_SLOTS && toSlot in 0 until EmojiDeck.MAX_SLOTS && fromSlot != toSlot) {
            val allSlots = dao.getEmojiDeckSlots(playerId).associateBy { it.slotIndex }
            val fromId = allSlots[fromSlot]?.emojiItemId
            val toId = allSlots[toSlot]?.emojiItemId
            dao.upsertEmojiDeckSlot(EmojiDeckSlotEntity(slotKey(playerId, fromSlot), playerId, fromSlot, toId))
            dao.upsertEmojiDeckSlot(EmojiDeckSlotEntity(slotKey(playerId, toSlot), playerId, toSlot, fromId))
        }
        loadInventorySnapshotLocked(playerId)
    }

    // ======================================================
    // POWERUP ACTIVATION WITH COOLDOWN & DAILY LIMIT
    // ======================================================

    suspend fun activatePowerup(
        playerId: String = prefs.getCurrentPlayerId(),
        itemId: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<PowerupActivationResult, PlayerInventory> = txMutex.withLock {
        ensureInitializedLocked(playerId, prefs.getUserCoins(), prefs.getUserGems())
        val item = ItemCatalog.findById(itemId)
        if (item == null || item.category != InventoryCategory.POWERUP) {
            return@withLock PowerupActivationResult.Error("Invalid powerup") to loadInventorySnapshotLocked(playerId)
        }

        val owned = dao.getOwnedItem(key(playerId, itemId))
        val qty = owned?.quantity ?: 0
        if (qty <= 0) {
            return@withLock PowerupActivationResult.NotOwned(itemId) to loadInventorySnapshotLocked(playerId)
        }

        val existing = dao.getActivePowerup(key(playerId, itemId))
        val today = currentDayIndex(nowMillis)
        val usedToday = if (existing != null && existing.dailyResetDay == today) existing.dailyUsedCount else 0

        if (item.dailyLimit > 0 && usedToday >= item.dailyLimit) {
            return@withLock PowerupActivationResult.DailyLimitReached(item.dailyLimit) to loadInventorySnapshotLocked(playerId)
        }

        if (existing != null && existing.lastActivatedMillis > 0L && item.cooldownSeconds > 0L) {
            val elapsedSec = (nowMillis - existing.lastActivatedMillis) / 1000L
            if (elapsedSec < item.cooldownSeconds) {
                return@withLock PowerupActivationResult.OnCooldown(item.cooldownSeconds - elapsedSec) to loadInventorySnapshotLocked(playerId)
            }
        }

        val remainingQty = (qty - 1).coerceAtLeast(0)
        dao.upsertOwnedItem(owned!!.copy(quantity = remainingQty))

        val activeUntil = nowMillis + item.durationMillis.coerceAtLeast(60_000L)
        val updatedEntity = ActivePowerupEntity(
            compositeId = key(playerId, itemId),
            playerId = playerId,
            itemId = itemId,
            powerupType = item.powerupEffectType ?: itemId,
            quantity = remainingQty,
            activeUntilMillis = activeUntil,
            lastActivatedMillis = nowMillis,
            dailyUsedCount = usedToday + 1,
            dailyResetDay = today
        )
        dao.upsertActivePowerup(updatedEntity)

        val resultModel = ActivePowerups(
            playerId = updatedEntity.playerId,
            itemId = updatedEntity.itemId,
            powerupType = updatedEntity.powerupType,
            quantity = updatedEntity.quantity,
            activeUntilMillis = updatedEntity.activeUntilMillis,
            lastActivatedMillis = updatedEntity.lastActivatedMillis,
            dailyUsedCount = updatedEntity.dailyUsedCount,
            dailyResetDay = updatedEntity.dailyResetDay
        )
        return@withLock PowerupActivationResult.Activated(item, resultModel) to loadInventorySnapshotLocked(playerId)
    }

    suspend fun getPurchaseHistory(playerId: String = prefs.getCurrentPlayerId()): List<PurchaseTransactionEntity> =
        dao.getPurchaseTransactions(playerId)
}
