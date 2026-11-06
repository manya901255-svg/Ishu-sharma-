package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.SocialInviteUtils
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LudoUiState
import com.example.viewmodel.LudoViewModel

@Composable
fun HomeScreen(
    uiState: LudoUiState,
    viewModel: LudoViewModel
) {
    var selectedModeForSetup by remember { mutableStateOf<GameMode?>(null) }
    var showEventsDialog by remember { mutableStateOf(false) }
    var showGiftDialog by remember { mutableStateOf(false) }
    var showTasksDialog by remember { mutableStateOf(false) }
    var showMailDialog by remember { mutableStateOf(false) }
    var showOfferDialog by remember { mutableStateOf(false) }
    var showChestDialog by remember { mutableStateOf<Int?>(null) }
    var showRewardedAdDialog by remember { mutableStateOf(false) }
    var adRewardTitle by remember { mutableStateOf("1,000 Coins") }
    var showVaultKeyDialog by remember { mutableStateOf(false) }
    var showKeyExchangeDialog by remember { mutableStateOf(false) }
    var showChallengesDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        // 1. Authentic Purple Woodgrain Texture Background (Ludo Club Theme)
        Image(
            painter = painterResource(id = R.drawable.bg_purple_wood),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle Ambient Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33000000),
                            Color.Transparent,
                            Color(0x55000000)
                        )
                    )
                )
        )

        // 2. Main Scrollable Lobby Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // A. Top Header Bar (Player Profile, Rocket, Coins, Cash, Settings)
            LudoClubTopHeader(
                playerName = uiState.playerNickname,
                crownAvatarId = uiState.playerCrownAvatar,
                customPhotoUri = uiState.customProfilePhotoUri,
                level = uiState.playerLevel,
                coins = uiState.userCoins,
                gems = uiState.userGems,
                isMusicPlaying = uiState.isMusicPlaying,
                onProfileClick = { viewModel.navigateTo(AppScreen.PROFILE) },
                onMusicClick = { viewModel.toggleMusicPlayerDialog(true) },
                onSettingsClick = { viewModel.toggleSoundSettings(true) },
                onAddCoins = {
                    adRewardTitle = "2,500 Bonus Coins"
                    showRewardedAdDialog = true
                },
                onAddGems = {
                    adRewardTitle = "15 Royal Gems"
                    showRewardedAdDialog = true
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // B. Top Event Pills Row (Ranking, Leaderboard, Tournaments, Golden Key)
            LudoClubTopEventPills(
                onRankingClick = { viewModel.navigateTo(AppScreen.LEADERBOARD) },
                onLeaderboardClick = { viewModel.navigateTo(AppScreen.LEADERBOARD) },
                onTournamentsClick = { showEventsDialog = true },
                onVaultKeyClick = { showVaultKeyDialog = true }
            )


            Spacer(modifier = Modifier.height(6.dp))

            // C. Center Court Carousel & Side Action Buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
            ) {
                // Left Column Buttons (Get 1K ad, Key Quest 3 friends, Trials, Like, Invite, Offer)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LudoClubActionBadge(
                        icon = "🎬",
                        label = "Get 1K",
                        badge = "AD",
                        badgeColor = Color(0xFF00C853)
                    ) {
                        adRewardTitle = "1,000 Gold Coins"
                        showRewardedAdDialog = true
                    }

                    LudoClubActionBadge(
                        icon = "🔑",
                        label = "Keys",
                        badge = "${uiState.keysReceivedCount}/3",
                        badgeColor = if (uiState.keysReceivedCount >= 3) Color(0xFF00E676) else RoyalGold
                    ) {
                        showVaultKeyDialog = true
                    }

                    LudoClubActionBadge(
                        icon = "🎯",
                        label = "Trials",
                        badge = "BOUNTY",
                        badgeColor = Color(0xFFFF5252)
                    ) {
                        showChallengesDialog = true
                    }

                    LudoClubActionBadge(
                        icon = "👍",
                        label = "Like",
                        badge = null
                    ) {
                        viewModel.claimDailySpinReward(500, 2)
                    }

                    LudoClubActionBadge(
                        icon = getCrownAvatarEmoji(uiState.playerCrownAvatar),
                        label = "Avatar",
                        badge = "REGAL",
                        badgeColor = RoyalGold
                    ) {
                        viewModel.toggleDynamicProfileDialog(true)
                    }

                    LudoClubActionBadge(
                        icon = "📜",
                        label = "Quests",
                        badge = "${uiState.dailyMissions.count { it.isCompleted }}",
                        badgeColor = Color(0xFF00E676)
                    ) {
                        viewModel.toggleDailyMissionsDialog(true)
                    }

                    LudoClubActionBadge(
                        icon = "👥",
                        label = "Friends",
                        badge = "ONLINE",
                        badgeColor = Color(0xFF00C853)
                    ) {
                        viewModel.toggleFriendsDialog(true)
                    }

                    LudoClubActionBadge(
                        icon = "🔨",
                        label = "OFFER",
                        badge = "HOT",
                        badgeColor = Color(0xFFFF9100)
                    ) {
                        showOfferDialog = true
                    }
                }

                // Center Game Mode Cards (Large cards with authentic illustrations, vines, pumpkins)
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 56.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Card 1: Play Online
                    LudoClubImageCard(
                        imageRes = R.drawable.card_play_online,
                        title = "Play Online",
                        onClick = { selectedModeForSetup = GameMode.ONLINE_MULTIPLAYER }
                    )

                    // Card 2: 2 vs 2 Team Up
                    LudoClubImageCard(
                        imageRes = R.drawable.card_two_vs_two,
                        title = "2 vs 2",
                        onClick = { selectedModeForSetup = GameMode.PASS_AND_PLAY }
                    )

                    // Card 3: Quick Rush Blitz
                    LudoClubProceduralCard(
                        title = "Quick Rush",
                        subtitle = "1 Pawn Home to Win!",
                        bgGradient = listOf(Color(0xFFFF9100), Color(0xFFFF3D00)),
                        icon = "⚡",
                        decorEmoji = "🔥🎲",
                        badge = "BLITZ",
                        onClick = { selectedModeForSetup = GameMode.QUICK_RUSH }
                    )

                    // Card 4: vs AI Court
                    LudoClubProceduralCard(
                        title = "vs AI Mode",
                        subtitle = "Offline Royal Court",
                        bgGradient = listOf(Color(0xFF7C4DFF), Color(0xFF4527A0)),
                        icon = "🤖",
                        decorEmoji = "👑✨",
                        badge = "OFFLINE",
                        onClick = { selectedModeForSetup = GameMode.VS_AI }
                    )

                    // Card 5: Private Room
                    LudoClubProceduralCard(
                        title = "Private Room",
                        subtitle = "Play with Friends",
                        bgGradient = listOf(Color(0xFFFF4081), Color(0xFFC2185B)),
                        icon = "🔑",
                        decorEmoji = "🏰💎",
                        badge = "CODE",
                        onClick = { viewModel.openPrivateRoomDialog() }
                    )
                }

                // Right Column Buttons (Daily Reward, Match History, Tasks checklist, Mail envelope)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LudoClubActionBadge(
                        icon = "🎁",
                        label = "Daily",
                        badge = if (uiState.dailyRewardStatus?.isClaimedToday == false) "FREE" else null,
                        badgeColor = LudoClubRedBadge
                    ) {
                        viewModel.toggleDailyRewardDialog(true)
                    }

                    LudoClubActionBadge(
                        icon = "📜",
                        label = "History",
                        badge = if (uiState.recentMatches.isNotEmpty()) "${minOf(uiState.recentMatches.size, 10)}" else null,
                        badgeColor = Color(0xFF1976D2)
                    ) {
                        viewModel.toggleMatchHistoryDialog(true)
                    }

                    LudoClubActionBadge(
                        icon = "📋",
                        label = "Tasks",
                        badge = null
                    ) {
                        showTasksDialog = true
                    }

                    LudoClubActionBadge(
                        icon = "✉️",
                        label = "Mail",
                        badge = "1",
                        badgeColor = LudoClubRedBadge
                    ) {
                        showMailDialog = true
                    }

                    LudoClubActionBadge(
                        icon = "📐",
                        label = "Blueprint",
                        badge = "DOCS",
                        badgeColor = Color(0xFF00E5FF)
                    ) {
                        viewModel.navigateTo(AppScreen.TECH_BLUEPRINT)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // D. Lower Left Events Hub Target + Fortune Spin Wheel trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // EVENTS HUB Button with Red-White Target & Coin Sacks
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFA000), Color(0xFFFF6F00))
                            )
                        )
                        .border(2.dp, RoyalGoldLight, RoundedCornerShape(16.dp))
                        .clickable { showEventsDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("events_hub_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_events_hub),
                        contentDescription = "Events Hub",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "EVENTS HUB",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Live Tournaments",
                            color = RoyalGoldLight,
                            fontSize = 9.sp
                        )
                    }
                }

                // Fortune Daily Spin Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF7C4DFF), Color(0xFF512DA8))
                            )
                        )
                        .border(1.5.dp, RoyalGold, RoundedCornerShape(16.dp))
                        .clickable { viewModel.toggleDailySpin(true) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("daily_spin_trigger"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎡", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "DAILY SPIN",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // E. Bottom 5 Chest Slots Tray (4 Standard Slots + 1 Season Pass Locked Slot)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 4 Standard Chest Slots
                repeat(4) { slotIndex ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(88.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF381A63).copy(alpha = 0.5f))
                            .border(1.2.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .clickable { showChestDialog = slotIndex }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (slotIndex == 0) "📦" else "📭", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (slotIndex == 0) "Ready!" else "Chest\nSlot",
                                color = if (slotIndex == 0) RoyalGold else TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }

                // 5th Season Pass Locked Slot
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF280E4B))
                        .border(2.dp, RoyalGoldDark, RoundedCornerShape(12.dp))
                        .clickable { viewModel.toggleSeasonPassDialog(true) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔒", fontSize = 22.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF7B1FA2))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Season Pass",
                                color = RoyalGoldLight,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Chest Slot",
                            color = TextMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. Iconic Ludo Club 5-Tab Glossy Blue Bottom Dock
        LudoClubBottomDock(
            currentScreen = uiState.currentScreen,
            onScreenSelected = { viewModel.navigateTo(it) },
            onInventoryClick = { viewModel.navigateTo(AppScreen.SHOP) },
            onClubClick = { showEventsDialog = true },
            onFriendsClick = { viewModel.toggleFriendsDialog(true) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )


        // Regal Settings & Atmosphere Dialog (BGM, SFX, Haptics, Regal Board Themes)
        if (uiState.showSoundSettingsDialog) {
            RegalSettingsDialog(
                isBgmEnabled = uiState.isBgmEnabled,
                bgmVolume = uiState.bgmVolume,
                isSfxEnabled = uiState.isSfxEnabled,
                sfxVolume = uiState.sfxVolume,
                isHapticsEnabled = uiState.isHapticsEnabled,
                selectedBoardThemeId = uiState.equippedBoardTheme,
                selectedSfxPreset = uiState.sfxPreset,
                currentBgmTrack = uiState.currentBgmTrack,
                currentBgmTrackIndex = uiState.currentBgmTrackIndex,
                bgmPlaylist = uiState.bgmPlaylist,
                onToggleBgm = { viewModel.toggleBgm(it) },
                onBgmVolumeChange = { viewModel.setBgmVolume(it) },
                onNextBgmTrack = { viewModel.playNextBgmTrack() },
                onPrevBgmTrack = { viewModel.playPreviousBgmTrack() },
                onSelectBgmTrack = { viewModel.selectBgmTrack(it) },
                onToggleSfx = { viewModel.toggleSfx(it) },
                onSfxVolumeChange = { viewModel.setSfxVolume(it) },
                onToggleHaptics = { viewModel.toggleHaptics(it) },
                onSelectBoardTheme = { viewModel.selectBoardTheme(it) },
                onSelectSfxPreset = { viewModel.setSfxPreset(it) },
                onTestDice = { viewModel.soundAndHaptics.playDiceRoll() },
                onTestPawn = { viewModel.soundAndHaptics.playPawnMove() },
                onTestFanfare = { viewModel.soundAndHaptics.soundManager.playVictoryFanfare() },
                onTestCapture = { viewModel.soundAndHaptics.soundManager.playPawnCapture() },
                onTestStarSafe = { viewModel.soundAndHaptics.playStarSafe() },
                onTestSixRolled = { viewModel.soundAndHaptics.playSixRolled() },
                onOpenMusicPlayer = { viewModel.toggleMusicPlayerDialog(true) },
                onDismiss = { viewModel.toggleSoundSettings(false) }
            )
        }

        // Royal Music Player & Jukebox Dialog (44 Songs & Tracks)
        if (uiState.showMusicPlayerDialog) {
            LudoMusicPlayerDialog(
                currentTrack = uiState.currentBgmTrack,
                isPlaying = uiState.isMusicPlaying,
                repeatMode = uiState.musicRepeatMode,
                volume = uiState.bgmVolume,
                favoriteSongIds = uiState.favoriteSongIds,
                onPlayPauseToggle = { viewModel.toggleMusicPlayPause() },
                onNextTrack = { viewModel.playNextBgmTrack() },
                onPreviousTrack = { viewModel.playPreviousBgmTrack() },
                onSelectTrack = { viewModel.selectBgmTrack(it) },
                onRepeatModeCycle = { viewModel.cycleMusicRepeatMode() },
                onVolumeChange = { viewModel.setBgmVolume(it) },
                onToggleFavorite = { viewModel.toggleFavoriteSong(it) },
                onDismiss = { viewModel.toggleMusicPlayerDialog(false) }
            )
        }

        // Daily Login Reward Dialog (7-Day Streak in Room Database)
        if (uiState.showDailyRewardDialog) {
            DailyRewardDialog(
                status = uiState.dailyRewardStatus,
                onClaimReward = { viewModel.claimDailyReward() },
                onDismiss = { viewModel.toggleDailyRewardDialog(false) }
            )
        }

        // Royal Match History Dialog (Last 10 Matches with Leaderboard in Room Database)
        if (uiState.showMatchHistoryDialog) {
            MatchHistoryDialog(
                matches = uiState.recentMatches,
                onDismiss = { viewModel.toggleMatchHistoryDialog(false) }
            )
        }

        // Invite Friends via WhatsApp / Instagram / Facebook Dialog
        if (uiState.showInviteDialog) {
            InviteFriendsDialog(
                roomCode = uiState.roomCode,
                onRewardEarned = { coins, gems -> viewModel.claimDailySpinReward(coins, gems) },
                onDismiss = { viewModel.toggleInviteDialog(false) }
            )
        }

        // 3D Entry Theme Dialog for all Selected Game Modes
        if (selectedModeForSetup != null) {
            val mode = selectedModeForSetup!!
            val modeTitle = when (mode) {
                GameMode.ONLINE_MULTIPLAYER -> "Online"
                GameMode.VS_AI -> "Play AI"
                GameMode.PASS_AND_PLAY -> "Pass & Play"
                GameMode.QUICK_RUSH -> "Rush"
                GameMode.PRIVATE_ROOM -> "Room"
                else -> "Online"
            }
            OnlineModeLobbyDialog(
                userCoins = uiState.userCoins,
                initialColor = uiState.userChosenColor,
                initialMode = mode,
                titleText = modeTitle,
                onDismiss = { selectedModeForSetup = null },
                onStartGame = { playerCount, wager, chosenColor, gameMode ->
                    selectedModeForSetup = null
                    if (gameMode == GameMode.ONLINE_MULTIPLAYER || gameMode == GameMode.QUICK_RUSH) {
                        viewModel.startMatchmaking(gameMode, playerCount, chosenColor, wager)
                    } else {
                        viewModel.startNewGame(gameMode, playerCount, chosenColor, wager)
                    }
                }
            )
        }

        // Private Room Dialog
        if (uiState.showPrivateRoomDialog) {
            PrivateRoomDialog(
                roomCode = uiState.roomCode,
                onDismiss = { viewModel.closePrivateRoomDialog() },
                onCreateRoom = { count ->
                    viewModel.closePrivateRoomDialog()
                    viewModel.startNewGame(GameMode.PRIVATE_ROOM, count)
                },
                onJoinRoom = { code ->
                    viewModel.closePrivateRoomDialog()
                    viewModel.startMatchmaking(GameMode.PRIVATE_ROOM, 4, customRoomCode = code)
                }
            )
        }

        // Matchmaking Overlay
        if (uiState.isMatchmaking) {
            MatchmakingOverlay(status = uiState.matchmakingStatus)
        }

        // Daily Spin Wheel Modal
        if (uiState.showDailySpin) {
            DailySpinDialog(
                lastSpinTimestampMillis = uiState.lastSpinTimestampMillis,
                onClaimReward = { coins, gems -> viewModel.claimDailySpinReward(coins, gems) },
                onWatchAdForSpin = {
                    adRewardTitle = "Maharaja Daily Fortune Spin"
                    showRewardedAdDialog = true
                },
                onDismiss = { viewModel.toggleDailySpin(false) }
            )
        }

        // Royal Friend List Dialog (Online Players & Avatars & Key Exchange)
        if (uiState.showFriendsDialog) {
            FriendListDialog(
                userNickname = uiState.playerNickname,
                userCrownAvatar = uiState.playerCrownAvatar,
                userCustomPhotoUri = uiState.customProfilePhotoUri,
                userTitle = uiState.playerTitle,
                keysSentCount = uiState.keysSentCount,
                keysReceivedCount = uiState.keysReceivedCount,
                onInviteToMatch = { friend -> viewModel.inviteFriendToMatch(friend) },
                onSendKey = { friend -> viewModel.sendKeyToFriend(friend.id, friend.name) },
                onClaimReceivedKey = { fromFriend -> viewModel.claimReceivedKey(fromFriend) },
                onOpenVault = {
                    viewModel.toggleFriendsDialog(false)
                    showVaultKeyDialog = true
                },
                onShareInviteLink = { viewModel.toggleInviteDialog(true) },
                onDismiss = { viewModel.toggleFriendsDialog(false) }
            )
        }

        // Events Hub Dialog
        if (showEventsDialog) {
            com.example.ui.components.EventsHubDialog(
                uiState = uiState,
                onClaimEventReward = { coins, gems, eventName ->
                    viewModel.claimEventReward(coins, gems, eventName)
                },
                onJoinTournament = { mode, players ->
                    showEventsDialog = false
                    viewModel.startNewGame(mode, players)
                    viewModel.navigateTo(AppScreen.GAME)
                },
                onClaimMission = { mission ->
                    viewModel.claimDailyMission(mission)
                },
                onDismiss = { showEventsDialog = false }
            )
        }

        // Dynamic Profile Selection Dialog (Regal ancient kingdom avatars)
        if (uiState.showDynamicProfileDialog) {
            DynamicProfileDialog(
                currentAvatarId = uiState.playerCrownAvatar,
                currentTitle = uiState.playerTitle,
                playerName = uiState.playerNickname,
                onAvatarSelected = { avatarId, title ->
                    viewModel.equipAncientAvatar(avatarId, title)
                },
                onDismiss = { viewModel.toggleDynamicProfileDialog(false) }
            )
        }

        // Daily Regal Missions Dialog
        if (uiState.showDailyMissionsDialog) {
            DailyMissionsDialog(
                missions = uiState.dailyMissions,
                onClaimMission = { mission ->
                    viewModel.claimDailyMission(mission)
                },
                onDismiss = { viewModel.toggleDailyMissionsDialog(false) }
            )
        }

        // Match Replay Theater Dialog
        if (uiState.showMatchReplayDialog) {
            MatchReplayViewerDialog(
                replays = uiState.savedReplays,
                onDismiss = { viewModel.toggleMatchReplayDialog(false) }
            )
        }

        // Rewarded Video Ad Simulator Dialog
        if (showRewardedAdDialog) {
            RewardedAdDialog(
                rewardTitle = adRewardTitle,
                onRewardEarned = {
                    if (adRewardTitle.contains("Gems")) {
                        viewModel.rewardUserForAd(coins = 0, gems = 15)
                    } else if (adRewardTitle.contains("2,500")) {
                        viewModel.rewardUserForAd(coins = 2500L, gems = 0)
                    } else {
                        viewModel.rewardUserForAd(coins = 1000L, gems = 0)
                    }
                },
                onDismiss = { showRewardedAdDialog = false }
            )
        }

        // Lucky Chest Dialog (Screenshot 2: Chained chest, 0/3 keys, 500K Coins + 5K Cash)
        if (showVaultKeyDialog) {
            LuckyChestDialog(
                keysReceivedCount = uiState.keysReceivedCount,
                keysSentCount = uiState.keysSentCount,
                onSendAndGetKeys = {
                    showKeyExchangeDialog = true
                },
                onReceiveKeyFromFriend = { fromFriend ->
                    viewModel.claimReceivedKey(fromFriend)
                },
                onClaimLuckyChestReward = {
                    viewModel.claimMaharajaVaultReward()
                    showVaultKeyDialog = false
                },
                onDismiss = { showVaultKeyDialog = false }
            )
        }

        // Golden Vault Key Dialog (Friends exchange keys, rewards open only at 3 keys)
        if (showKeyExchangeDialog) {
            GoldenVaultKeyDialog(
                keysReceivedCount = uiState.keysReceivedCount,
                keysSentCount = uiState.keysSentCount,
                onSendKeyToFriend = { friendName ->
                    viewModel.sendKeyToFriend("f_$friendName", friendName)
                },
                onReceiveKeyFromFriend = { fromFriend ->
                    viewModel.claimReceivedKey(fromFriend)
                },
                onClaimVaultReward = {
                    viewModel.claimMaharajaVaultReward()
                    showKeyExchangeDialog = false
                    showVaultKeyDialog = false
                },
                onDismiss = { showKeyExchangeDialog = false }
            )
        }

        // Royal Challenges Dialog (Screenshot 3: Tier 1 Rewards, Star 10/75, Daily Challenges)
        if (showChallengesDialog) {
            RoyalChallengesDialog(
                userCoins = uiState.userCoins,
                userCash = uiState.userGems,
                onClaimChallenge = { coins, gems ->
                    viewModel.claimDailySpinReward(coins, gems)
                },
                onPlayChallenge = {
                    showChallengesDialog = false
                    viewModel.startNewGame(GameMode.ONLINE_MULTIPLAYER, 2)
                    viewModel.navigateTo(AppScreen.GAME)
                },
                onDismiss = { showChallengesDialog = false }
            )
        }

        // Free Gift Dialog
        if (showGiftDialog) {
            FreeGiftDialog(
                onClaim = { coins, gems ->
                    viewModel.claimDailySpinReward(coins, gems)
                    showGiftDialog = false
                },
                onDismiss = { showGiftDialog = false }
            )
        }

        // Daily Tasks Dialog
        if (showTasksDialog) {
            DailyTasksDialog(
                onClaimTask = { rewardCoins ->
                    viewModel.claimDailySpinReward(rewardCoins, 0)
                },
                onDismiss = { showTasksDialog = false }
            )
        }

        // Mailbox Dialog
        if (showMailDialog) {
            MailboxDialog(
                onClaimMail = { coins, gems ->
                    viewModel.claimDailySpinReward(coins, gems)
                },
                onDismiss = { showMailDialog = false }
            )
        }

        // Special VIP Offer Dialog (Tapped via button or automatically triggered after playing 5 online games)
        if (showOfferDialog || uiState.showSpecialOfferDialog) {
            SpecialOfferDialog(
                onClaim = { coins, gems ->
                    viewModel.claimSpecialOfferDeal(coins, gems)
                    showOfferDialog = false
                },
                onDismiss = {
                    showOfferDialog = false
                    viewModel.dismissSpecialOffer()
                }
            )
        }

        // Chest Slot Opening Dialog
        if (showChestDialog != null) {
            ChestSlotDialog(
                slotIndex = showChestDialog!!,
                onOpen = { coins, gems ->
                    viewModel.claimDailySpinReward(coins, gems)
                    showChestDialog = null
                },
                onDismiss = { showChestDialog = null }
            )
        }

        // Royal Season Pass Dialog
        if (uiState.showSeasonPassDialog) {
            RoyalSeasonPassDialog(
                playerLevel = uiState.playerLevel,
                onClaimAll = { coins, gems ->
                    viewModel.claimSeasonPassRewards(coins, gems)
                },
                onDismiss = { viewModel.toggleSeasonPassDialog(false) }
            )
        }

        // Royal Interactive Pop-Up Notice Dialog (Modal notification for all game events)
        GamePopUpNoticeDialog(
            notice = uiState.activePopUpNotice,
            onDismiss = { viewModel.dismissPopUpNotice() }
        )
    }
}

/**
 * Top Status Bar matching Ludo Club screenshot:
 * Profile picture on left, Level 18 star badge, Coins wallet with green +,
 * Notes wallet with green +, Golden Settings cog on top-right.
 */
@Composable
private fun LudoClubTopHeader(
    playerName: String,
    crownAvatarId: String,
    customPhotoUri: String? = null,
    level: Int,
    coins: Long,
    gems: Int,
    isMusicPlaying: Boolean = false,
    onProfileClick: () -> Unit,
    onMenuClick: () -> Unit = {},
    onMusicClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    onAddCoins: () -> Unit,
    onAddGems: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(8.dp),
        color = LudoClubHeaderBlue
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player Profile on Left (Square Avatar + Level Badge + Progress bar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onProfileClick() }
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RoyalGold)
                        .border(1.5.dp, Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!customPhotoUri.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = customPhotoUri,
                            contentDescription = "Custom Profile Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(7.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(getCrownAvatarEmoji(crownAvatarId), fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = playerName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Level badge with yellow star
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0D47A1))
                                .border(1.dp, RoyalGold, RoundedCornerShape(10.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐", fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(level.toString(), color = RoyalGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // XP Progress Bar
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF0D47A1))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.68f)
                                    .background(Brush.horizontalGradient(listOf(RoyalGoldLight, RoyalGold)))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))
                Text("🚀", fontSize = 18.sp)
            }

            // Center / Right: Currency Wallets + Settings Cog
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Coin Balance Pill with Green +
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0D3CA8))
                        .border(1.dp, Color(0xFF42A5F5), RoundedCornerShape(16.dp))
                        .clickable { onAddCoins() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = formatCompactCoins(coins),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        // Green +
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(LudoClubPlusGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                // Cash/Gem Balance Pill with Green +
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0D3CA8))
                        .border(1.dp, Color(0xFF42A5F5), RoundedCornerShape(16.dp))
                        .clickable { onAddGems() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💵", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = gems.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(LudoClubPlusGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }


                // Royal Music Jukebox Button (44 Bollywood & Royal tracks)
                IconButton(
                    onClick = onMusicClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(
                        text = if (isMusicPlaying) "🎶" else "🎵",
                        fontSize = 20.sp
                    )
                }

                // Golden Settings Cog
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = RoyalGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Top Event Pills matching the screenshot:
 * Ranking (medal 38, 5d 20h), Leaderboard (podium 1-2-3, 6d 7h), Tournaments (trophy), Golden Key with badge 1.
 */
@Composable
private fun LudoClubTopEventPills(
    onRankingClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onTournamentsClick: () -> Unit,
    onVaultKeyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Ranking Pill
        EventPillButton(
            title = "Ranking",
            timer = "5d 20h",
            badgeIcon = "🏅",
            badgeNumber = "38",
            modifier = Modifier.weight(1f),
            onClick = onRankingClick
        )

        // 2. Leaderboard Pill
        EventPillButton(
            title = "Leaderboard",
            timer = "6d 7h",
            badgeIcon = "📊",
            badgeNumber = null,
            modifier = Modifier.weight(1f),
            onClick = onLeaderboardClick
        )

        // 3. Tournaments Pill
        EventPillButton(
            title = "Tournaments",
            timer = "Get In!",
            badgeIcon = "🏆",
            badgeNumber = null,
            modifier = Modifier.weight(1f),
            onClick = onTournamentsClick
        )

        // 4. Golden Key / Vault Icon with notification badge 1
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(LudoClubCyanPill, LudoClubCyanPillDark)
                    )
                )
                .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                .clickable { onVaultKeyClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("🗝️", fontSize = 22.sp)

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(LudoClubRedBadge),
                contentAlignment = Alignment.Center
            ) {
                Text("1", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EventPillButton(
    title: String,
    timer: String,
    badgeIcon: String,
    badgeNumber: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(LudoClubCyanPill, LudoClubCyanPillDark)
                )
            )
            .border(1.5.dp, Color(0xFFE1F5FE), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(badgeIcon, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF003D73).copy(alpha = 0.7f))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = timer,
                    color = Color(0xFFE1F5FE),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Side action widget buttons matching left and right columns of Ludo Club
 */
@Composable
private fun LudoClubActionBadge(
    icon: String,
    label: String,
    badge: String?,
    badgeColor: Color = Color(0xFFFF9800),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF42A5F5), Color(0xFF1565C0))
                )
            )
            .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 20.sp)
            Text(
                text = label,
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }

        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Large tactile Game Mode Cards with dedicated high-res illustrations (Play Online, 2 vs 2)
 */
@Composable
private fun LudoClubImageCard(
    imageRes: Int,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(265.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .testTag("mode_card_${title.replace(" ", "_")}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(3.dp, Color.White.copy(alpha = 0.9f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Procedural fallback cards for other modes (Quick Rush, AI, Private Room)
 */
@Composable
private fun LudoClubProceduralCard(
    title: String,
    subtitle: String,
    bgGradient: List<Color>,
    icon: String,
    decorEmoji: String,
    badge: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(265.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .testTag("mode_card_${title.replace(" ", "_")}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(3.dp, Color.White.copy(alpha = 0.9f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(bgGradient))
                .padding(16.dp)
        ) {
            Text(text = icon, fontSize = 28.sp, modifier = Modifier.align(Alignment.TopStart))

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = badge, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = decorEmoji, fontSize = 52.sp)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Ludo Club Iconic 5-Tab Glossy Blue Bottom Dock:
 * STORE, INVENTORY, HOME (raised central tab), FRIENDS, CLUB.
 */
@Composable
private fun LudoClubBottomDock(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    onInventoryClick: () -> Unit,
    onClubClick: () -> Unit,
    onFriendsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(16.dp),
        color = LudoClubDockBlue,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, LudoClubDockBlueLight)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: STORE
            DockTabButton(
                icon = "🏪",
                label = "STORE",
                isSelected = currentScreen == AppScreen.SHOP,
                hasBadge = true
            ) {
                onScreenSelected(AppScreen.SHOP)
            }

            // Tab 2: INVENTORY
            DockTabButton(
                icon = "🎲",
                label = "INVENTORY",
                isSelected = false,
                hasBadge = true
            ) {
                onInventoryClick()
            }

            // Tab 3: HOME (Raised central tab)
            Box(
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .size(62.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF42A5F5), LudoClubDockActive)
                        )
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                    .clickable { onScreenSelected(AppScreen.HOME) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏠", fontSize = 24.sp)
                    Text("HOME", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // Tab 4: FRIENDS (Invite Friends via WhatsApp, Instagram, Facebook)
            DockTabButton(
                icon = "👥",
                label = "FRIENDS",
                isSelected = false,
                hasBadge = true
            ) {
                onFriendsClick()
            }

            // Tab 5: CLUB
            DockTabButton(
                icon = "🎶",
                label = "CLUB",
                isSelected = false,
                hasBadge = true
            ) {
                onClubClick()
            }
        }
    }
}

@Composable
private fun DockTabButton(
    icon: String,
    label: String,
    isSelected: Boolean,
    hasBadge: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 22.sp)
            Text(
                text = label,
                color = if (isSelected) RoyalGold else Color.White,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold
            )
        }

        if (hasBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(LudoClubRedBadge)
            )
        }
    }
}

/**
 * Daily Tasks / Quests Dialog
 */
@Composable
private fun DailyTasksDialog(
    onClaimTask: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val tasks = listOf(
        "Win 1 Match in Play Online" to 2000L,
        "Capture 3 Opponent Pawns" to 1500L,
        "Roll three 6s in any game" to 3000L,
        "Play 1 Quick Rush match" to 1000L
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LudoClubPurplePlank),
            border = androidx.compose.foundation.BorderStroke(2.5.dp, RoyalGold)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📋 DAILY TASKS", color = RoyalGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                tasks.forEach { (task, reward) ->
                    var isClaimed by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF240E44))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Reward: 🪙 +$reward", color = RoyalGoldLight, fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                if (!isClaimed) {
                                    isClaimed = true
                                    onClaimTask(reward)
                                }
                            },
                            enabled = !isClaimed,
                            colors = ButtonDefaults.buttonColors(containerColor = if (isClaimed) Color.Gray else LudoClubPlusGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isClaimed) "DONE" else "CLAIM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Court Mailbox Dialog
 */
@Composable
private fun MailboxDialog(
    onClaimMail: (Long, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var claimed by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LudoClubPurplePlank),
            border = androidx.compose.foundation.BorderStroke(2.dp, RoyalGold)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("✉️ MG LUDO INBOX", color = RoyalGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF240E44))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("Welcome Maharaja! 👑", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Thank you for playing MG Ludo! Here is your royal welcome reward of 5,000 Coins and 15 Diamonds.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (!claimed) {
                                    claimed = true
                                    onClaimMail(5000, 15)
                                }
                            },
                            enabled = !claimed,
                            colors = ButtonDefaults.buttonColors(containerColor = LudoClubPlusGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (claimed) "COLLECTED" else "COLLECT ALL", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D5DE8))) {
                    Text("CLOSE", color = Color.White)
                }
            }
        }
    }
}

/**
 * VIP Special Deal Offer Dialog
 */
@Composable
private fun SpecialOfferDialog(
    onClaim: (Long, Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0D4A)),
            border = androidx.compose.foundation.BorderStroke(3.dp, RoyalGold)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🔥 SPECIAL MAHARAJA OFFER", color = RoyalGold, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        Text("80% OFF • 5 ONLINE GAMES MILESTONE DEAL", color = Color(0xFFFF5722), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text("👑 100,000 Gold Coins", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("💎 250 Kohinoor Diamonds", color = SapphireBlueLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("🎲 Ruby Crystal Dice Skin", color = RoyalAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = { onClaim(100000, 250) },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CLAIM 5-GAME DEAL NOW 🎉", color = Color(0xFF1A1A1A), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/**
 * Free Gift Dialog
 */
@Composable
private fun FreeGiftDialog(onClaim: (Long, Int) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LudoClubPurplePlank),
            border = androidx.compose.foundation.BorderStroke(2.dp, RoyalGold)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎁 FREE DAILY BOUNTY", color = RoyalGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text("You received +2,000 Coins and +5 Diamonds!", color = Color.White, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onClaim(2000, 5) },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold)
                ) {
                    Text("CLAIM GIFT", color = Color(0xFF1A1A1A), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Chest Slot Dialog
 */
@Composable
private fun ChestSlotDialog(
    slotIndex: Int,
    onOpen: (Long, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var chestState by remember { mutableStateOf("CLOSED") } // "CLOSED", "OPENING", "OPENED"
    val rewardCoins = 1500L + (slotIndex * 500L)
    val rewardGems = 3 + slotIndex

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("chest_slot_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14244D)),
            border = androidx.compose.foundation.BorderStroke(2.dp, RoyalGold)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📦 TREASURE CHEST #${slotIndex + 1}",
                    color = RoyalGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                when (chestState) {
                    "CLOSED" -> {
                        Text(
                            text = "First open the chest box to reveal your hidden treasure!",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F1E40))
                                .border(2.dp, RoyalGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🧰", fontSize = 38.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { chestState = "OPENING" },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("open_chest_slot_button")
                        ) {
                            Text("OPEN CHEST BOX 🔓", color = RoyalNavyDark, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                    "OPENING" -> {
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(850)
                            chestState = "OPENED"
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("✨ 📦 ✨", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Unlocking Treasure Chest...",
                            color = RoyalGoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    "OPENED" -> {
                        Text(
                            text = "Chest Box Opened! Treasures Unlocked:",
                            color = Color(0xFF00E676),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪙 +$rewardCoins Coins", color = RoyalGoldLight, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            Text("💎 +$rewardGems Gems", color = SapphireBlueLight, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onOpen(rewardCoins, rewardGems) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("collect_chest_reward_button")
                        ) {
                            Text("COLLECT REWARD ✓", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSetupDialog(
    mode: GameMode,
    userCoins: Long,
    initialColor: LudoColor = LudoColor.BLUE,
    onDismiss: () -> Unit,
    onStart: (playerCount: Int, wager: Long, chosenColor: LudoColor) -> Unit
) {
    var playerCount by remember { mutableStateOf(4) }
    var chosenColor by remember { mutableStateOf(initialColor) }
    val wagers = listOf(1500L, 3000L, 6000L, 12000L, 25000L, 50000L, 100000L)
    var selectedWager by remember { mutableStateOf(1500L) }

    val basePot = selectedWager * playerCount
    val doublePrize = basePot * 2
    val awardedChest = when {
        selectedWager >= 25000L -> "Imperial Diamond Chest 💎"
        selectedWager >= 10000L -> "Maharaja Gold Chest 👑"
        selectedWager >= 5000L -> "Royal Emerald Chest 🟢"
        else -> "Royal Ruby Chest 📦"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .shadow(20.dp, RoundedCornerShape(22.dp))
                .testTag("mode_setup_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = LudoClubPurplePlank),
            border = androidx.compose.foundation.BorderStroke(2.5.dp, RoyalGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = mode.title,
                            color = RoyalGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = mode.subtitle,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Choose Your Token Color
                Text(
                    text = "🎨 Choose Your Token Color:",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple(LudoColor.BLUE, "Blue", Color(0xFF1E88E5)),
                        Triple(LudoColor.RED, "Red", Color(0xFFE53935)),
                        Triple(LudoColor.GREEN, "Green", Color(0xFF43A047)),
                        Triple(LudoColor.YELLOW, "Yellow", Color(0xFFFFC107))
                    ).forEach { (colorOption, label, chipColor) ->
                        val isSelected = chosenColor == colorOption
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) chipColor else Color(0xFF0F1C3B))
                                .border(
                                    2.dp,
                                    if (isSelected) Color.White else chipColor.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { chosenColor = colorOption }
                                .padding(vertical = 8.dp)
                                .testTag("select_color_${colorOption.title.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(chipColor)
                                        .border(1.5.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Text("👑", fontSize = 12.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Player Count Selector
                Text(
                    text = "👥 Number of Players:",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(2, 4).forEach { count ->
                        val isSelected = playerCount == count
                        OutlinedButton(
                            onClick = { playerCount = count },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("select_players_$count"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) LudoClubCyanPill else Color(0xFF0F1C3B),
                                contentColor = if (isSelected) Color.White else TextMuted
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            Text("$count Players", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                if (mode != GameMode.PASS_AND_PLAY) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Entry Price & Scalable Wager
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🪙 Starting Entry Price: Rs 1,500",
                            color = RoyalGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Increase as desired",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        wagers.forEach { w ->
                            val isSelected = selectedWager == w
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) RoyalGold else Color(0xFF143063))
                                    .border(1.dp, if (isSelected) Color.White else Color(0xFF28549C), RoundedCornerShape(10.dp))
                                    .clickable { selectedWager = w }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                    .testTag("wager_chip_${w}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Rs ${formatCompactCoins(w)}",
                                    color = if (isSelected) Color(0xFF1A1A1A) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Double Winning Prize & Chest Box Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1938)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E676))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "2X DOUBLE WINNING PRIZE: 🪙 ${formatCompactCoins(doublePrize)}",
                                    color = Color(0xFF00E676),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🎁 Winner also unlocks: $awardedChest",
                                color = RoyalGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onStart(playerCount, selectedWager, chosenColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_game_modal_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = LudoClubPlusGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "START MATCH (${formatCompactCoins(selectedWager)} Coins)",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivateRoomDialog(
    roomCode: String,
    onDismiss: () -> Unit,
    onCreateRoom: (Int) -> Unit,
    onJoinRoom: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var roomCodeInput by remember { mutableStateOf("") }
    var selectedPlayerCount by remember { mutableIntStateOf(4) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LudoClubPurplePlank),
            border = androidx.compose.foundation.BorderStroke(2.dp, RoyalGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "👑 Private Royal Room",
                    color = RoyalGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Play with friends in a private chamber",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SELECT PLAYERS:",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 3, 4).forEach { count ->
                        val isSelected = selectedPlayerCount == count
                        Button(
                            onClick = { selectedPlayerCount = count },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) RoyalGold else Color(0xFF1E3A70)
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "$count Players",
                                color = if (isSelected) Color(0xFF3E2723) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onCreateRoom(selectedPlayerCount) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LudoClubHeaderBlue)
                ) {
                    Text("CREATE NEW ROOM ($selectedPlayerCount PLAYERS)", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Social Invite Section (WhatsApp, Instagram, Facebook)
                Text(
                    text = "INVITE FRIENDS VIA SOCIAL APPS:",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // WhatsApp
                    Button(
                        onClick = { SocialInviteUtils.shareToWhatsApp(context, roomCode) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        WhatsAppIcon(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Instagram
                    Button(
                        onClick = { SocialInviteUtils.shareToInstagram(context, roomCode) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC13584)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        InstagramIcon(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Insta", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Facebook
                    Button(
                        onClick = { SocialInviteUtils.shareToFacebook(context, roomCode) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        FacebookIcon(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Facebook", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("OR JOIN EXISTING ROOM:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = roomCodeInput,
                    onValueChange = { roomCodeInput = it.uppercase() },
                    placeholder = { Text("e.g. MG-8832", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { if (roomCodeInput.isNotBlank()) onJoinRoom(roomCodeInput) },
                    enabled = roomCodeInput.length >= 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LudoClubPlusGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("JOIN ROOM", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MatchmakingOverlay(status: String) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LudoClubPurplePlank),
            border = androidx.compose.foundation.BorderStroke(2.dp, RoyalGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = RoyalGold, modifier = Modifier.size(52.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "MG Ludo Matchmaker",
                    color = RoyalGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = status,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}
