package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.FairDiceEngine
import com.example.engine.LudoEngine
import com.example.model.*
import com.example.ui.components.CelebrationType
import com.example.util.SoundAndHaptics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

enum class AppScreen {
    HOME,
    GAME,
    LEADERBOARD,
    SHOP,
    PREMIUM_DICE_STORE,
    INVENTORY,
    PROFILE,
    TECH_BLUEPRINT
}

enum class MatchConnectionState {
    CONNECTED,
    RECONNECTING,
    DISCONNECTED
}

data class DiceStoreUiState(
    val catalog: List<PremiumDiceItem> = PremiumDiceCatalog.buildDefaultSeedCatalog(),
    val ownedDiceIds: Set<String> = setOf("dice_imperial_gold"),
    val equippedDiceId: String = "dice_imperial_gold",
    val pendingBillingDiceIds: Set<String> = emptySet(),
    val eventRewards: Map<String, DiceEventRewardProgress> = emptyMap(),
    val recentPurchaseHistory: List<String> = emptyList(),
    val selectedGroup: String = "ALL", // ALL, CLASSIC_PREMIUM, VIP, SPECIAL_THEMED, DAILY_EVENT
    val selectedRarity: DiceRarity? = null,
    val searchQuery: String = "",
    val selectedDetailDiceId: String? = null,
    val isProcessingPurchase: Boolean = false,
    val isBillingConnected: Boolean = false,
    val purchaseSuccessDice: PremiumDiceItem? = null,
    val insufficientCoinsDialogDice: PremiumDiceItem? = null,
    val statusBanner: String? = null,
    val errorMessage: String? = null
) {
    fun findDice(diceId: String): PremiumDiceItem? = catalog.firstOrNull { it.id == diceId }
    fun isOwned(diceId: String): Boolean = diceId in ownedDiceIds
    fun isPendingBilling(diceId: String): Boolean = diceId in pendingBillingDiceIds
}

data class InventoryUiState(
    val playerId: String = com.example.util.ProfilePreferences.DEFAULT_PLAYER_ID,
    val selectedCategory: InventoryCategory = InventoryCategory.DICE,
    val searchQuery: String = "",
    val selectedFilter: InventoryFilter = InventoryFilter.ALL,
    val ownedItems: List<OwnedItems> = emptyList(),
    val ownedItemIds: Set<String> = ItemCatalog.defaultOwnedItemIds(),
    val equippedDiceId: String = "dice_imperial_gold",
    val equippedFrameId: String = "frame_royal_crest",
    val equippedBoardId: String = "imperial_navy",
    val equippedPawnId: String = "crown_maharaja",
    val isPawnTrailEnabled: Boolean = true,
    val emojiDeckSlots: List<String?> = List(EmojiDeck.MAX_SLOTS) { null },
    val activePowerups: List<ActivePowerups> = emptyList(),
    val eventProgressMap: Map<String, EventProgress> = emptyMap(),
    val isLoading: Boolean = false,
    val isProcessingTransaction: Boolean = false,
    val lastSuccessItem: InventoryItem? = null,
    val lastSuccessMessage: String? = null,
    val errorMessage: String? = null
) {
    fun getQuantity(itemId: String): Int = ownedItems.firstOrNull { it.itemId == itemId }?.quantity ?: 0
    fun isOwned(itemId: String): Boolean = itemId in ownedItemIds
    fun uniqueOwnedCountFor(category: InventoryCategory): Int {
        val catIds = ItemCatalog.ALL_ITEMS.filter { it.category == category }.map { it.id }.toSet()
        return ownedItemIds.count { it in catIds }
    }
    fun totalUniqueOwnedCount(): Int = ownedItemIds.size
}

data class LudoUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val gameMode: GameMode = GameMode.VS_AI,
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val turnPhase: TurnPhase = TurnPhase.ROLL_DICE,
    val currentDiceValue: Int = 1,
    val isRolling: Boolean = false,
    val consecutiveSixes: Int = 0,
    val turnTimeRemaining: Int = 25,
    val fairDiceProof: FairDiceProof? = null,
    val userCoins: Long = 25000L,
    val userGems: Int = 120,
    val chatMessages: List<ChatMessage> = emptyList(),
    val unreadChatCount: Int = 0,
    val activeFloatingEmoji: Pair<String, String>? = null, // player name to emoji
    val lastChatMessage: String? = null,
    val lastChatSenderName: String? = null,
    val winner: Player? = null,
    val rankings: List<Player> = emptyList(),
    val roomCode: String = "MG-8832",
    val isMatchmaking: Boolean = false,
    val matchmakingStatus: String = "",
    val currentWager: Long = 1500L,
    val userChosenColor: LudoColor = LudoColor.YELLOW,
    val wonChestBoxName: String? = null,
    val keysSentCount: Int = 0,
    val keysReceivedCount: Int = 0,
    val showDailySpin: Boolean = false,
    val showFriendsDialog: Boolean = false,
    val showSeasonPassDialog: Boolean = false,
    val activePopUpNotice: com.example.model.GamePopUpNotice? = null,
    val lastSpinTimestampMillis: Long = 0L,
    val showFairProofDialog: Boolean = false,
    val showChatDialog: Boolean = false,
    val showModeSelectDialog: Boolean = false,
    val showPrivateRoomDialog: Boolean = false,
    val showInviteDialog: Boolean = false,
    val showSoundSettingsDialog: Boolean = false,
    val showResultsDialog: Boolean = false,
    val showMatchHistoryDialog: Boolean = false,
    val showDailyRewardDialog: Boolean = false,
    val dailyRewardStatus: com.example.data.DailyRewardStatus? = null,
    val recentMatches: List<com.example.data.MatchHistoryEntity> = emptyList(),
    val bannerNotice: String? = null,
    val equippedPawnSkin: String = "crown_maharaja",
    val equippedDiceSkin: String = "dice_imperial_gold",
    val equippedBoardSkin: String = "board_jaipur_marble",
    val equippedBoardTheme: String = "imperial_navy",
    val equippedFrameId: String = "frame_royal_crest",
    val isPawnTrailEnabled: Boolean = true,
    val activeCelebration: CelebrationType? = null,
    val playerNickname: String = "Ishu",
    val playerCrownAvatar: String = "crown_imperial_gold",
    val customProfilePhotoUri: String? = null,
    val playerTitle: String = "Maharaja",
    val playerLevel: Int = 18,
    val playerXp: Int = 68,
    // Player Career Statistics
    val totalWins: Int = 28,
    val totalGamesPlayed: Int = 42,
    val totalCaptures: Int = 89,
    val winStreak: Int = 4,
    val equippedPawnCrownAvatar: String = "crown_imperial_gold",
    val inventoryUiState: InventoryUiState = InventoryUiState(),
    val diceStoreUiState: DiceStoreUiState = DiceStoreUiState(),
    // Audio & Atmosphere - Continuous Background Music Sequential Playlist (44 Songs)
    val isBgmEnabled: Boolean = true,
    val isSfxEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val bgmVolume: Float = 0.80f,
    val sfxVolume: Float = 0.85f,
    val sfxPreset: String = "royal_court",
    val currentBgmTrack: com.example.model.BgmTrack? = null,
    val currentBgmTrackIndex: Int = -1,
    val bgmPlaylist: List<com.example.model.BgmTrack> = com.example.model.BGM_PLAYLIST,
    // Power-up Dice & In-game Features
    val activePowerUp: String? = null, // "EVEN", "ODD", "HIGH", "LOW", "LIGHTNING"
    val lightningCount: Int = 3,
    val isVoiceActive: Boolean = true,
    val isMicMuted: Boolean = false,
    val isSpeakerMuted: Boolean = false,
    val voiceLevel: Float = 0f,
    val isLocalSpeaking: Boolean = false,
    val activeSpeakerId: String? = null,
    val voiceChatNotification: String? = null,
    // Welcome & Main Menu Overlays and 3D Board
    val showWelcomeOverlay: Boolean = true,
    val showMainMenuOverlay: Boolean = false,
    val is3DBoardEnabled: Boolean = false,
    val board3DAngle: Float = 0f,
    val showMusicPlayerDialog: Boolean = false,
    val isMusicPlaying: Boolean = false,
    val musicRepeatMode: com.example.model.MusicRepeatMode = com.example.model.MusicRepeatMode.ALL,
    val isMusicShuffleEnabled: Boolean = false,
    val favoriteSongIds: Set<String> = emptySet(),
    val onlineSearchQuery: String = "",
    val onlineSearchResults: List<com.example.model.BgmTrack> = emptyList(),
    val isSearchingMusic: Boolean = false,
    val isMusicBuffering: Boolean = false,
    val selectedMusicCategory: com.example.model.MusicCategory = com.example.model.MusicCategory.HINDI,
    val categoryTracks: List<com.example.model.BgmTrack> = emptyList(),
    val isLoadingCategoryMusic: Boolean = false,
    val recentlyPlayedTracks: List<com.example.model.BgmTrack> = emptyList(),
    val userPlaylists: List<com.example.model.UserPlaylist> = emptyList(),
    val favoriteTracks: List<com.example.model.BgmTrack> = emptyList(),
    val playbackPositionMs: Int = 0,
    val playbackDurationMs: Int = 0,
    val onlineGamesPlayedCount: Int = 0,
    val showSpecialOfferDialog: Boolean = false,
    val savedReplays: List<com.example.model.MatchReplaySession> = emptyList(),
    val dailyMissions: List<com.example.model.DailyMission> = com.example.model.DEFAULT_DAILY_MISSIONS,
    val currentMatchReplayMoves: List<com.example.model.ReplayMove> = emptyList(),
    val showDynamicProfileDialog: Boolean = false,
    val showMatchReplayDialog: Boolean = false,
    val showDailyMissionsDialog: Boolean = false,
    val achievements: List<com.example.data.AchievementEntity> = com.example.data.DEFAULT_ACHIEVEMENTS,
    val hasNoAds: Boolean = false,
    val hasVoicePass: Boolean = true,
    val voicePassDaysRemaining: Int = 30,
    val activeInAppProduct: com.example.ui.components.InAppPurchaseProduct? = null,
    // MG Ludo Custom Wallpaper System State
    val showWallpaperCustomizerDialog: Boolean = false,
    val activeWallpaperTarget: com.example.model.WallpaperTarget = com.example.model.WallpaperTarget.HOME,
    val activeWallpapers: Map<String, String> = emptyMap(),
    val customUploadedWallpapers: List<String> = emptyList(),
    val favoriteWallpaperIds: Set<String> = emptySet(),
    val recentWallpaperIds: List<String> = emptyList(),
    // MG Ludo Upgraded Admin, Tutorial, Moderation & Customization State
    val showAdminDashboard: Boolean = false,
    val adminPinVerified: Boolean = false,
    val showTutorialDialog: Boolean = false,
    val showReportDialog: Boolean = false,
    val showCustomizerShopDialog: Boolean = false,
    val reportedTargetPlayer: Player? = null,
    val playerReports: List<PlayerReport> = emptyList(),
    val practiceHint: String? = null,
    val canUndoPracticeMove: Boolean = false,
    // Token Synchronization, Deduplication & Reconnect Resilience
    val moveSequenceNumber: Long = 0L,
    val lastSyncedBoardChecksum: String = "",
    val connectionState: MatchConnectionState = MatchConnectionState.CONNECTED
)

class LudoViewModel(application: Application) : AndroidViewModel(application) {

    val soundAndHaptics = SoundAndHaptics(application)
    val voiceChatManager = com.example.util.LudoVoiceChatManager(application)
    val indianMusicRepository = com.example.network.IndianMusicRepository()
    private val profilePreferences = com.example.util.ProfilePreferences(application)
    private val repository = com.example.data.LudoRepository(application)
    val inventoryRepository = com.example.data.InventoryRepository(application)
    val diceStoreRepository = com.example.data.DiceStoreRepository(application, inventoryRepository)
    val playBillingManager = com.example.billing.PlayBillingManager(application) { sku ->
        _uiState.value.diceStoreUiState.catalog.firstOrNull { it.skuId == sku }?.id
    }

    private val _uiState = MutableStateFlow(LudoUiState())
    val uiState: StateFlow<LudoUiState> = _uiState.asStateFlow()

    private var turnTimerJob: Job? = null
    private var aiTurnJob: Job? = null
    private var moveJob: Job? = null
    private var musicSearchJob: Job? = null
    @Volatile
    private var isMoveInProgress: Boolean = false
    private val processedMoveIds = mutableSetOf<String>()

    init {
        // Initial fair seeds
        FairDiceEngine.rollNewSeeds()

        // Load persisted player profile & audio settings from local storage
        val savedNickname = profilePreferences.getNickname()
        val savedAvatar = profilePreferences.getCrownAvatar()
        val savedTitle = profilePreferences.getRoyalTitle()
        val savedLevel = profilePreferences.getPlayerLevel()
        val wins = profilePreferences.getTotalWins()
        val games = profilePreferences.getGamesPlayed()
        val streak = profilePreferences.getWinStreak()
        val captures = profilePreferences.getTotalCaptures()
        val pawnCrown = profilePreferences.getPawnCrownAvatar()
        val bgmOn = profilePreferences.isBgmEnabled()
        val sfxOn = profilePreferences.isSfxEnabled()
        val hapticsOn = profilePreferences.isHapticsEnabled()
        val bgmVol = profilePreferences.getBgmVolume()
        val sfxVol = profilePreferences.getSfxVolume()
        val savedSfxPreset = profilePreferences.getSfxPreset()
        val savedBoardTheme = profilePreferences.getBoardTheme()
        val savedDiceSkin = profilePreferences.getEquippedDiceSkin()
        val savedTokenSkin = profilePreferences.getEquippedTokenSkin()
        val savedFrameId = profilePreferences.getEquippedFrameId()
        val savedTrailEnabled = profilePreferences.isPawnTrailEnabled()
        val savedCoins = profilePreferences.getUserCoins()
        val savedGems = profilePreferences.getUserGems()
        val savedPhotoUri = profilePreferences.getCustomPhotoUri()
        val savedOnlineCount = profilePreferences.getOnlineGamesPlayed()
        val savedRepeatMode = try {
            com.example.model.MusicRepeatMode.valueOf(profilePreferences.getMusicRepeatMode())
        } catch (_: Exception) {
            com.example.model.MusicRepeatMode.ALL
        }
        val savedFavorites = profilePreferences.getFavoriteSongIds()

        // Daily missions persistence & daily reset
        val currentDay = System.currentTimeMillis() / (1000L * 60L * 60L * 24L)
        val lastResetDay = profilePreferences.getLastMissionResetDay()
        if (lastResetDay == 0L || currentDay > lastResetDay) {
            profilePreferences.resetDailyMissions(com.example.model.DEFAULT_DAILY_MISSIONS.map { it.id })
        }
        val initialMissions = com.example.model.DEFAULT_DAILY_MISSIONS.map { mission ->
            val prog = profilePreferences.getMissionProgress(mission.id)
            val claimed = profilePreferences.isMissionClaimed(mission.id)
            mission.copy(currentProgress = prog, isClaimed = claimed)
        }

        _uiState.update {
            it.copy(
                userCoins = savedCoins,
                userGems = savedGems,
                playerNickname = savedNickname,
                playerCrownAvatar = savedAvatar,
                customProfilePhotoUri = savedPhotoUri,
                playerTitle = savedTitle,
                playerLevel = savedLevel,
                totalWins = wins,
                totalGamesPlayed = games,
                onlineGamesPlayedCount = savedOnlineCount,
                winStreak = streak,
                totalCaptures = captures,
                equippedPawnCrownAvatar = pawnCrown,
                equippedPawnSkin = savedTokenSkin,
                equippedDiceSkin = savedDiceSkin,
                equippedBoardTheme = savedBoardTheme,
                equippedFrameId = savedFrameId,
                isPawnTrailEnabled = savedTrailEnabled,
                isBgmEnabled = bgmOn,
                isSfxEnabled = sfxOn,
                isHapticsEnabled = hapticsOn,
                bgmVolume = bgmVol,
                sfxVolume = sfxVol,
                sfxPreset = savedSfxPreset,
                bgmPlaylist = com.example.model.BGM_PLAYLIST,
                musicRepeatMode = savedRepeatMode,
                isMusicShuffleEnabled = profilePreferences.isMusicShuffleEnabled(),
                favoriteSongIds = savedFavorites,
                dailyMissions = initialMissions,
                savedReplays = listOf(
                    com.example.model.MatchReplaySession(
                        id = "replay_royal_cup_01",
                        matchTitle = "Maharaja Championship Final 🏆",
                        timestamp = System.currentTimeMillis() - 1800000L,
                        gameMode = "Online Multiplayer",
                        wager = 5000L,
                        winnerName = savedNickname,
                        winnerColor = com.example.model.LudoColor.YELLOW,
                        winnerAvatar = savedAvatar,
                        totalMovesCount = 12,
                        durationFormatted = "4m 15s",
                        moves = listOf(
                            com.example.model.ReplayMove(1, savedNickname, com.example.model.LudoColor.YELLOW, savedAvatar, 6, 0, -1, 0, false, null, false, "Rolled 6! Deployed Gold Pawn #1 to Board"),
                            com.example.model.ReplayMove(2, "Computer 2", com.example.model.LudoColor.RED, "crown_peacock_throne", 4, 0, -1, -1, false, null, false, "Rolled 4. No pawn can leave base yet"),
                            com.example.model.ReplayMove(3, savedNickname, com.example.model.LudoColor.YELLOW, savedAvatar, 5, 0, 0, 5, false, null, false, "Advanced Gold Pawn #1 by 5 tiles"),
                            com.example.model.ReplayMove(4, "Computer 2", com.example.model.LudoColor.RED, "crown_peacock_throne", 6, 0, -1, 13, false, null, false, "Rolled 6! Deployed Red Pawn #1 to Board"),
                            com.example.model.ReplayMove(5, savedNickname, com.example.model.LudoColor.YELLOW, savedAvatar, 6, 1, -1, 0, false, null, false, "Rolled 6! Deployed Gold Pawn #2 to Board"),
                            com.example.model.ReplayMove(6, savedNickname, com.example.model.LudoColor.YELLOW, savedAvatar, 4, 1, 0, 4, false, null, false, "Moved Gold Pawn #2 to tile 4"),
                            com.example.model.ReplayMove(7, "Computer 2", com.example.model.LudoColor.RED, "crown_peacock_throne", 3, 0, 13, 16, false, null, false, "Advanced Red Pawn to tile 16"),
                            com.example.model.ReplayMove(8, savedNickname, com.example.model.LudoColor.YELLOW, savedAvatar, 4, 0, 5, 9, false, null, false, "Moved to Star Safe Zone ⭐! Immune to attacks"),
                            com.example.model.ReplayMove(9, "Computer 3", com.example.model.LudoColor.GREEN, "crown_maharaja", 2, 0, -1, -1, false, null, false, "Rolled 2. Awaiting 6"),
                            com.example.model.ReplayMove(10, savedNickname, com.example.model.LudoColor.YELLOW, savedAvatar, 6, 0, 9, 15, false, null, false, "Rolled 6! Galloped toward center path"),
                            com.example.model.ReplayMove(11, savedNickname, com.example.model.LudoColor.YELLOW, savedAvatar, 1, 0, 15, 16, true, "Computer 2", false, "⚔️ ROYAL CAPTURE! Captured Red Pawn and sent back to base!"),
                            com.example.model.ReplayMove(12, savedNickname, com.example.model.LudoColor.YELLOW, savedAvatar, 6, 0, 16, 56, false, null, true, "🏛️ PALACE FINISH! Reached center finish triangle! VICTORY! 👑")
                        )
                    ),
                    com.example.model.MatchReplaySession(
                        id = "replay_quick_rush_02",
                        matchTitle = "Quick Rush Blitz ⚡",
                        timestamp = System.currentTimeMillis() - 7200000L,
                        gameMode = "Quick Rush",
                        wager = 2500L,
                        winnerName = savedNickname,
                        winnerColor = com.example.model.LudoColor.YELLOW,
                        winnerAvatar = "avatar_rajput_king",
                        totalMovesCount = 8,
                        durationFormatted = "2m 10s",
                        moves = listOf(
                            com.example.model.ReplayMove(1, savedNickname, com.example.model.LudoColor.YELLOW, "avatar_rajput_king", 6, 0, 0, 6, false, null, false, "Blitz start: Gold pawn dashed 6 tiles"),
                            com.example.model.ReplayMove(2, "Computer 2", com.example.model.LudoColor.RED, "crown_imperial_gold", 5, 0, 13, 18, false, null, false, "Rival Red advanced rapidly"),
                            com.example.model.ReplayMove(3, savedNickname, com.example.model.LudoColor.YELLOW, "avatar_rajput_king", 6, 0, 6, 12, false, null, false, "Rolled 6! Double speed boost"),
                            com.example.model.ReplayMove(4, savedNickname, com.example.model.LudoColor.YELLOW, "avatar_rajput_king", 5, 0, 12, 17, false, null, false, "Approached home entrance"),
                            com.example.model.ReplayMove(5, "Computer 2", com.example.model.LudoColor.RED, "crown_imperial_gold", 3, 0, 18, 21, false, null, false, "Red moved into Star zone"),
                            com.example.model.ReplayMove(6, savedNickname, com.example.model.LudoColor.YELLOW, "avatar_rajput_king", 4, 0, 17, 21, true, "Computer 2", false, "⚔️ BLITZ CAPTURE! Struck down enemy pawn!"),
                            com.example.model.ReplayMove(7, savedNickname, com.example.model.LudoColor.YELLOW, "avatar_rajput_king", 6, 0, 21, 51, false, null, false, "Entered home stretch hallway"),
                            com.example.model.ReplayMove(8, savedNickname, com.example.model.LudoColor.YELLOW, "avatar_rajput_king", 5, 0, 51, 56, false, null, true, "🏛️ First Pawn Home! Blitz Champion! 🏆")
                        )
                    )
                )
            )
        }

        // Initialize Room Database persistence for Inventory, Premium Dice Store, Match History and Daily Rewards
        viewModelScope.launch {
            val invSnapshot = inventoryRepository.ensureInitialized(
                playerId = profilePreferences.getCurrentPlayerId(),
                initialCoins = savedCoins,
                initialCash = savedGems
            )
            applyInventorySnapshot(invSnapshot)

            val diceSnap = diceStoreRepository.ensureInitialized(profilePreferences.getCurrentPlayerId())
            applyDiceStoreSnapshot(diceSnap)
            playBillingManager.initialize(diceSnap.catalog.map { it.skuId }.filter { it.isNotBlank() })

            repository.initializeSampleHistoryIfEmpty()
            val rewardStatus = repository.getDailyRewardStatus()
            _uiState.update { it.copy(dailyRewardStatus = rewardStatus) }
            if (!rewardStatus.isClaimedToday) {
                delay(800)
                _uiState.update { it.copy(showDailyRewardDialog = true) }
            }
        }

        viewModelScope.launch {
            repository.last10Matches.collect { matches ->
                _uiState.update { it.copy(recentMatches = matches) }
            }
        }

        // Initialize and observe player profile from Room database
        viewModelScope.launch {
            repository.initializeDefaultProfileIfEmpty(
                defaultNickname = savedNickname,
                defaultAvatar = savedAvatar,
                defaultTitle = savedTitle,
                defaultPawnCrown = pawnCrown
            )
            repository.userProfileFlow.collect { profile ->
                if (profile != null) {
                    val resolvedDp = profile.customPhotoUri ?: profilePreferences.getCustomPhotoUri()
                    _uiState.update { current ->
                        val updatedPlayers = current.players.mapIndexed { idx, player ->
                            if (idx == 0) {
                                player.copy(
                                    name = profile.nickname,
                                    avatar = profile.crownAvatar,
                                    customPhotoUri = resolvedDp,
                                    title = profile.title
                                )
                            } else {
                                player
                            }
                        }
                        current.copy(
                            playerNickname = profile.nickname,
                            playerCrownAvatar = profile.crownAvatar,
                            customProfilePhotoUri = resolvedDp,
                            playerTitle = profile.title,
                            playerLevel = profile.level,
                            totalWins = profile.totalWins,
                            totalGamesPlayed = profile.totalGamesPlayed,
                            winStreak = profile.winStreak,
                            totalCaptures = profile.totalCaptures,
                            equippedPawnCrownAvatar = profile.equippedPawnCrownAvatar,
                            lastSpinTimestampMillis = profile.lastSpinTimestampMillis,
                            players = updatedPlayers
                        )
                    }
                }
            }
        }

        // Initialize and observe achievements from Room database
        viewModelScope.launch {
            repository.initializeAchievementsIfEmpty()
            repository.allAchievementsFlow.collect { list ->
                if (list.isNotEmpty()) {
                    _uiState.update { it.copy(achievements = list) }
                }
            }
        }

        // Continuous Royal Background Music Playlist
        soundAndHaptics.soundManager.setOnTrackChangeListener { track, index ->
            _uiState.update {
                it.copy(
                    currentBgmTrack = track,
                    currentBgmTrackIndex = index
                )
            }
            if (track != null) {
                recordTrackPlayed(track)
            }
        }
        soundAndHaptics.soundManager.setOnPlaybackStateListener { isPlaying ->
            _uiState.update { it.copy(isMusicPlaying = isPlaying) }
        }
        soundAndHaptics.soundManager.setOnBufferingListener { isBuffering ->
            _uiState.update { it.copy(isMusicBuffering = isBuffering) }
        }

        // Live playback progress ticker (updates seek bar position & duration every 500ms)
        viewModelScope.launch {
            while (true) {
                if (_uiState.value.isMusicPlaying && !_uiState.value.isMusicBuffering) {
                    val pos = soundAndHaptics.getCurrentPosition()
                    val dur = soundAndHaptics.getDuration()
                    _uiState.update { it.copy(playbackPositionMs = pos, playbackDurationMs = dur) }
                }
                delay(500L)
            }
        }

        // Load Initial Indian Online Music (Hindi) and Saved Playlists
        loadSavedMusicData()
        selectMusicCategory(com.example.model.MusicCategory.HINDI)
        loadWallpaperData()

        if (bgmOn && com.example.model.BGM_PLAYLIST.isNotEmpty()) {
            soundAndHaptics.soundManager.startAmbientBgm()
        }

        // Observe In-Game Voice Chat live flows
        viewModelScope.launch {
            voiceChatManager.isMicMuted.collect { muted ->
                _uiState.update { it.copy(isMicMuted = muted) }
            }
        }
        viewModelScope.launch {
            voiceChatManager.isSpeakerMuted.collect { muted ->
                _uiState.update { it.copy(isSpeakerMuted = muted) }
            }
        }
        viewModelScope.launch {
            voiceChatManager.localVoiceLevel.collect { lvl ->
                _uiState.update { it.copy(voiceLevel = lvl) }
            }
        }
        viewModelScope.launch {
            voiceChatManager.isLocalSpeaking.collect { spk ->
                _uiState.update { s ->
                    val updated = s.players.mapIndexed { idx, p ->
                        if (idx == 0 && !p.isAi) p.copy(isSpeaking = spk) else p
                    }
                    s.copy(isLocalSpeaking = spk, players = updated)
                }
            }
        }
        viewModelScope.launch {
            voiceChatManager.activeSpeakerId.collect { spkId ->
                _uiState.update { s ->
                    val updated = s.players.map { p ->
                        if (p.id == spkId) p.copy(isSpeaking = true)
                        else if (p.isAi) p.copy(isSpeaking = false)
                        else p
                    }
                    s.copy(activeSpeakerId = spkId, players = updated)
                }
            }
        }
        viewModelScope.launch {
            voiceChatManager.voiceChatNotification.collect { notif ->
                _uiState.update { it.copy(voiceChatNotification = notif) }
            }
        }

        // Observe Google Play Billing live connection and purchase updates
        viewModelScope.launch {
            playBillingManager.isConnected.collect { connected ->
                _uiState.update {
                    it.copy(diceStoreUiState = it.diceStoreUiState.copy(isBillingConnected = connected))
                }
            }
        }
        viewModelScope.launch {
            playBillingManager.events.collect { event ->
                handlePlayBillingEvent(event)
            }
        }

        // Restore persisted active match tokens/state if present, otherwise initialize clean 4-player board
        val restored = restoreSavedMatchIfPresent()
        if (!restored) {
            startNewGame(mode = GameMode.VS_AI, playerCount = 4, isInitialSetup = true)
        }
        _uiState.update { it.copy(currentScreen = AppScreen.HOME) }
        soundAndHaptics.setGameActive(false)
    }

    fun saveUserProfile(
        nickname: String,
        crownAvatar: String,
        title: String,
        pawnCrownAvatar: String = crownAvatar,
        customPhotoUri: String? = null
    ) {
        soundAndHaptics.vibrateVictory()
        val cleanName = nickname.trim().ifBlank { "Ishu" }
        val stablePhotoUri = if (customPhotoUri != null && customPhotoUri.startsWith("content://")) {
            profilePreferences.saveProfilePhotoLocally(android.net.Uri.parse(customPhotoUri)) ?: customPhotoUri
        } else {
            customPhotoUri ?: profilePreferences.getCustomPhotoUri()
        }
        profilePreferences.saveFullProfile(cleanName, crownAvatar, title, pawnCrownAvatar, stablePhotoUri)

        viewModelScope.launch {
            val currentProfile = repository.getUserProfile()
            val updated = currentProfile.copy(
                nickname = cleanName,
                crownAvatar = crownAvatar,
                customPhotoUri = stablePhotoUri,
                title = title,
                equippedPawnCrownAvatar = pawnCrownAvatar,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveUserProfile(updated)
        }

        _uiState.update { current ->
            val updatedPlayers = current.players.mapIndexed { idx, player ->
                if (idx == 0) {
                    player.copy(name = cleanName, avatar = crownAvatar, title = title, customPhotoUri = stablePhotoUri)
                } else {
                    player
                }
            }
            current.copy(
                playerNickname = cleanName,
                playerCrownAvatar = crownAvatar,
                customProfilePhotoUri = stablePhotoUri,
                playerTitle = title,
                equippedPawnCrownAvatar = pawnCrownAvatar,
                players = updatedPlayers,
                bannerNotice = "👑 Royal profile & DP saved in Room DB: $cleanName ($title)!"
            )
        }

        showPopUpNotice(
            title = "👑 Profile & DP Saved!",
            message = "Your display photo & royal crown profile were successfully saved in Room Database!",
            icon = "👑",
            type = "INFO"
        )
    }

    fun equipPawnCrownAvatar(avatarId: String) {
        soundAndHaptics.vibrateClick()
        profilePreferences.setPawnCrownAvatar(avatarId)
        _uiState.update { it.copy(equippedPawnCrownAvatar = avatarId) }
    }

    fun navigateTo(screen: AppScreen) {
        soundAndHaptics.setGameActive(screen == AppScreen.GAME)
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun openModeSelect() {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showModeSelectDialog = true) }
    }

    fun closeModeSelect() {
        _uiState.update { it.copy(showModeSelectDialog = false) }
    }

    fun openPrivateRoomDialog() {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showPrivateRoomDialog = true) }
    }

    fun closePrivateRoomDialog() {
        _uiState.update { it.copy(showPrivateRoomDialog = false) }
    }

    fun toggleDailySpin(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showDailySpin = show) }
    }

    fun toggleFairProof(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showFairProofDialog = show) }
    }

    fun toggleChatDialog(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showChatDialog = show, unreadChatCount = if (show) 0 else it.unreadChatCount) }
    }

    fun toggleInviteDialog(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showInviteDialog = show) }
    }

    fun toggleFriendsDialog(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showFriendsDialog = show) }
    }

    fun inviteFriendToMatch(friend: com.example.model.RoyalFriend) {
        soundAndHaptics.vibrateVictory()
        _uiState.update {
            it.copy(bannerNotice = "Royal Match Challenge sent to ${friend.name}! 👑")
        }
    }

    fun setUserChosenColor(color: LudoColor) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(userChosenColor = color) }
    }

    fun sendKeyToFriend(friendId: String, friendName: String) {
        soundAndHaptics.vibrateVictory()
        val newCount = _uiState.value.keysSentCount + 1
        _uiState.update {
            it.copy(
                keysSentCount = newCount
            )
        }
        // Notification is removed on sending key as requested: "it should just be sent"
    }

    fun claimReceivedKey(fromFriendName: String) {
        soundAndHaptics.vibrateVictory()
        val currentRecv = _uiState.value.keysReceivedCount
        if (currentRecv < 3) {
            val newCount = currentRecv + 1
            _uiState.update {
                it.copy(
                    keysReceivedCount = newCount
                )
            }
        }
    }

    fun claimMaharajaVaultReward() {
        if (_uiState.value.keysReceivedCount < 3) return
        soundAndHaptics.vibrateVictory()
        soundAndHaptics.playCoinReward()
        triggerCelebration(CelebrationType.MATCH_WON)
        val vaultCoins = 500000L
        val vaultGems = 5000
        _uiState.update {
            val newCoins = it.userCoins + vaultCoins
            val newGems = it.userGems + vaultGems
            profilePreferences.setUserCoins(newCoins)
            profilePreferences.setUserGems(newGems)
            it.copy(
                userCoins = newCoins,
                userGems = newGems,
                keysReceivedCount = 0,
                bannerNotice = "🎉 Lucky Chest Opened! +🪙 500,000 Coins & +💎 5,000 Cash awarded!"
            )
        }
        showPopUpNotice(
            title = "🎉 Lucky Chest Opened! 🎁",
            message = "Lucky Chest Unlocked by finger tap! +🪙 500,000 Coins and +💎 5,000 Cash added to your balance!",
            icon = "🎁",
            type = "REWARD"
        )
    }

    fun claimEventReward(coins: Long, gems: Int, eventName: String) {
        soundAndHaptics.vibrateVictory()
        soundAndHaptics.playCoinReward()
        triggerCelebration(CelebrationType.MATCH_WON)
        val newCoins = _uiState.value.userCoins + coins
        val newGems = _uiState.value.userGems + gems
        profilePreferences.setUserCoins(newCoins)
        profilePreferences.setUserGems(newGems)
        _uiState.update {
            it.copy(
                userCoins = newCoins,
                userGems = newGems,
                bannerNotice = "🎯 $eventName Reward Claimed: +🪙 ${com.example.ui.components.formatCompactCoins(coins)} & +💎 $gems Gems!"
            )
        }
        showPopUpNotice(
            title = "🎯 $eventName Reward! 🏆",
            message = "Congratulations! You claimed +🪙 ${com.example.ui.components.formatCompactCoins(coins)} Coins and +💎 $gems Gems from $eventName!",
            icon = "🏆",
            type = "REWARD"
        )
    }

    fun openWonChestBox() {
        soundAndHaptics.vibrateVictory()
        triggerCelebration(CelebrationType.MATCH_WON)
        val bonusCoins = 3000L + Random.nextInt(5000)
        val bonusGems = 10 + Random.nextInt(15)
        _uiState.update {
            val newCoins = it.userCoins + bonusCoins
            val newGems = it.userGems + bonusGems
            profilePreferences.setUserCoins(newCoins)
            profilePreferences.setUserGems(newGems)
            it.copy(
                userCoins = newCoins,
                userGems = newGems,
                bannerNotice = "📦 Chest Opened! Found +🪙 ${com.example.ui.components.formatCompactCoins(bonusCoins)} Coins and +💎 $bonusGems Gems!"
            )
        }
        showPopUpNotice(
            title = "📦 Mystery Chest Opened! 🎁",
            message = "Found +🪙 ${com.example.ui.components.formatCompactCoins(bonusCoins)} Coins & +💎 $bonusGems Gems!",
            icon = "📦",
            type = "CHEST"
        )
    }

    fun showPopUpNotice(title: String, message: String, icon: String = "🔔", type: String = "INFO") {
        soundAndHaptics.vibrateVictory()
        _uiState.update {
            it.copy(
                activePopUpNotice = com.example.model.GamePopUpNotice(
                    title = title,
                    message = message,
                    icon = icon,
                    type = type
                )
            )
        }
    }

    fun dismissPopUpNotice() {
        _uiState.update { it.copy(activePopUpNotice = null) }
    }

    fun toggleSeasonPassDialog(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showSeasonPassDialog = show) }
    }

    fun claimSeasonPassRewards(coins: Long, gems: Int) {
        soundAndHaptics.vibrateVictory()
        soundAndHaptics.playCoinReward()
        triggerCelebration(CelebrationType.MATCH_WON)
        _uiState.update {
            val newCoins = it.userCoins + coins
            val newGems = it.userGems + gems
            profilePreferences.setUserCoins(newCoins)
            profilePreferences.setUserGems(newGems)
            it.copy(
                userCoins = newCoins,
                userGems = newGems,
                showSeasonPassDialog = false
            )
        }
        showPopUpNotice(
            title = "👑 Royal Season Pass Claimed!",
            message = "Claimed all rewards: +🪙 ${com.example.ui.components.formatCompactCoins(coins)} Coins & +💎 $gems Gems!",
            icon = "🎁",
            type = "SEASON_PASS"
        )
    }

    fun toggleDailyRewardDialog(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showDailyRewardDialog = show) }
    }

    fun dismissWelcomeOverlay() {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showWelcomeOverlay = false, showMainMenuOverlay = false) }
    }

    fun openWelcomeOverlay() {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showWelcomeOverlay = true) }
    }

    fun toggleMainMenuOverlay(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showMainMenuOverlay = show) }
    }

    fun toggle3DBoard(enabled: Boolean = false) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(is3DBoardEnabled = false, board3DAngle = 0f) }
    }

    fun cycle3DAngle() {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(is3DBoardEnabled = false, board3DAngle = 0f) }
    }

    fun set3DAngle(angle: Float) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(is3DBoardEnabled = false, board3DAngle = 0f) }
    }

    fun claimAchievement(achievement: com.example.data.AchievementEntity) {
        soundAndHaptics.vibrateVictory()
        triggerCelebration(CelebrationType.MATCH_WON)
        viewModelScope.launch {
            repository.claimAchievement(achievement.id)
            val updatedCoins = _uiState.value.userCoins + achievement.coinReward
            val updatedGems = _uiState.value.userGems + (achievement.xpReward / 10).coerceAtLeast(5)
            profilePreferences.setUserCoins(updatedCoins)
            profilePreferences.setUserGems(updatedGems)
            _uiState.update { state ->
                state.copy(
                    userCoins = updatedCoins,
                    userGems = updatedGems,
                    achievements = state.achievements.map {
                        if (it.id == achievement.id) it.copy(isClaimed = true) else it
                    }
                )
            }
        }
    }

    fun openInAppPurchase(product: com.example.ui.components.InAppPurchaseProduct) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(activeInAppProduct = product) }
    }

    fun dismissInAppPurchase() {
        _uiState.update { it.copy(activeInAppProduct = null) }
    }

    fun completeInAppPurchase(product: com.example.ui.components.InAppPurchaseProduct) {
        soundAndHaptics.vibrateVictory()
        triggerCelebration(CelebrationType.MATCH_WON)
        val newCoins = _uiState.value.userCoins + product.coinsReward
        val newGems = _uiState.value.userGems + product.gemsReward
        val newNoAds = _uiState.value.hasNoAds || product.unlocksNoAds
        val newVoiceDays = if (product.voiceDaysReward > 0) _uiState.value.voicePassDaysRemaining + product.voiceDaysReward else _uiState.value.voicePassDaysRemaining
        profilePreferences.setUserCoins(newCoins)
        profilePreferences.setUserGems(newGems)
        _uiState.update {
            it.copy(
                userCoins = newCoins,
                userGems = newGems,
                hasNoAds = newNoAds,
                hasVoicePass = true,
                voicePassDaysRemaining = newVoiceDays,
                activeInAppProduct = null
            )
        }
    }

    fun toggleMatchHistoryDialog(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showMatchHistoryDialog = show) }
    }

    fun toggleDynamicProfileDialog(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showDynamicProfileDialog = show) }
    }

    fun toggleMatchReplayDialog(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showMatchReplayDialog = show) }
    }

    fun toggleDailyMissionsDialog(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showDailyMissionsDialog = show) }
    }

    fun equipAncientAvatar(avatarId: String, title: String) {
        soundAndHaptics.soundManager.playVictoryFanfare()
        profilePreferences.setCrownAvatar(avatarId)
        profilePreferences.setRoyalTitle(title)
        _uiState.update { s ->
            val updatedPlayers = s.players.map { player ->
                if (player.name == s.playerNickname || player.id == "p_0") {
                    player.copy(avatar = avatarId, title = title)
                } else player
            }
            s.copy(
                playerCrownAvatar = avatarId,
                playerTitle = title,
                players = updatedPlayers,
                showDynamicProfileDialog = false,
                bannerNotice = "👑 Sovereign Avatar Equipped: $title!"
            )
        }
    }

    fun equipAncientAvatar(avatar: com.example.model.RegalAvatar) {
        soundAndHaptics.soundManager.playVictoryFanfare()
        profilePreferences.setCrownAvatar(avatar.id)
        profilePreferences.setRoyalTitle(avatar.title)
        _uiState.update { s ->
            val updatedPlayers = s.players.map { player ->
                if (player.name == s.playerNickname || player.id == "p_0") {
                    player.copy(avatar = avatar.id, title = avatar.title)
                } else player
            }
            s.copy(
                playerCrownAvatar = avatar.id,
                playerTitle = avatar.title,
                players = updatedPlayers,
                showDynamicProfileDialog = false,
                bannerNotice = "👑 Sovereign Avatar Equipped: ${avatar.name} (${avatar.kingdom})!"
            )
        }
    }

    fun updateMissionProgress(missionId: String, increment: Int = 1) {
        _uiState.update { s ->
            val updated = s.dailyMissions.map { m ->
                if (m.id == missionId && !m.isCompleted && !m.isClaimed) {
                    val newProgress = (m.currentProgress + increment).coerceAtMost(m.targetProgress)
                    profilePreferences.setMissionProgress(missionId, newProgress)
                    m.copy(currentProgress = newProgress)
                } else m
            }
            s.copy(dailyMissions = updated)
        }
    }

    fun claimDailyMission(mission: com.example.model.DailyMission) {
        val target = _uiState.value.dailyMissions.find { it.id == mission.id } ?: return
        if (!target.isCompleted || target.isClaimed) return

        profilePreferences.setMissionClaimed(target.id, true)
        val newCoins = _uiState.value.userCoins + target.rewardCoins
        val newGems = _uiState.value.userGems + target.rewardGems
        profilePreferences.setUserCoins(newCoins)
        profilePreferences.setUserGems(newGems)

        val newAvatar = target.rewardCosmeticId ?: _uiState.value.playerCrownAvatar
        if (target.rewardCosmeticId != null) {
            profilePreferences.setCrownAvatar(newAvatar)
            profilePreferences.addOwnedItem(newAvatar)
        }

        _uiState.update { s ->
            val updatedMissions = s.dailyMissions.map {
                if (it.id == target.id) it.copy(isClaimed = true) else it
            }
            val updatedPlayers = s.players.map { p ->
                if ((p.name == s.playerNickname || p.id == "p_0") && target.rewardCosmeticId != null) {
                    p.copy(avatar = newAvatar)
                } else p
            }
            s.copy(
                userCoins = newCoins,
                userGems = newGems,
                playerCrownAvatar = newAvatar,
                players = updatedPlayers,
                dailyMissions = updatedMissions,
                bannerNotice = "🎉 Quest Completed: Claimed +🪙 ${target.rewardCoins} & +💎 ${target.rewardGems}!"
            )
        }
        soundAndHaptics.soundManager.playVictoryFanfare()
        triggerCelebration(CelebrationType.MATCH_WON)
        showPopUpNotice(
            title = "📜 Royal Mission Claimed! 👑",
            message = "Completed: ${target.title}\nClaimed +🪙 ${target.rewardCoins} Coins & +💎 ${target.rewardGems} Gems" +
                    if (target.rewardCosmeticName != null) "\nUnlocked: ${target.rewardCosmeticName}" else "",
            icon = "📜",
            type = "REWARD"
        )
    }

    fun buyDiceSkin(skin: com.example.model.DiceSkin) {
        if (profilePreferences.isItemOwned(skin.id)) {
            equipDiceSkin(skin.id)
            return
        }
        if (_uiState.value.userCoins < skin.priceCoins) {
            showPopUpNotice(
                title = "🪙 Insufficient Coins",
                message = "You need 🪙 ${skin.priceCoins} Coins to purchase ${skin.name}. Current balance: 🪙 ${_uiState.value.userCoins}.",
                icon = "🪙",
                type = "WARNING"
            )
            return
        }
        val newCoins = _uiState.value.userCoins - skin.priceCoins
        profilePreferences.setUserCoins(newCoins)
        profilePreferences.addOwnedItem(skin.id)
        profilePreferences.setEquippedDiceSkin(skin.id)
        _uiState.update {
            it.copy(
                userCoins = newCoins,
                equippedDiceSkin = skin.id,
                bannerNotice = "🎉 Acquired & Equipped ${skin.name} Dice!"
            )
        }
        viewModelScope.launch {
            inventoryRepository.syncWalletBalance(goldCoins = newCoins, cashNotes = _uiState.value.userGems)
            inventoryRepository.grantItem(itemId = skin.id, source = "SHOP_PURCHASE")
            val (_, snap) = inventoryRepository.equipItem(itemId = skin.id)
            applyInventorySnapshot(snap)
        }
        soundAndHaptics.soundManager.playVictoryFanfare()
        showPopUpNotice(
            title = "🎲 Dice Skin Unlocked!",
            message = "Successfully purchased and equipped ${skin.name} (${skin.category})!",
            icon = "🎲",
            type = "REWARD"
        )
    }

    fun equipDiceSkin(skinId: String) {
        equipPremiumDice(skinId)
    }

    fun buyTokenSkin(skin: com.example.model.TokenSkin) {
        if (profilePreferences.isItemOwned(skin.id)) {
            equipTokenSkin(skin.id)
            return
        }
        if (_uiState.value.userCoins < skin.priceCoins) {
            showPopUpNotice(
                title = "🪙 Insufficient Coins",
                message = "You need 🪙 ${skin.priceCoins} Coins to purchase ${skin.name}. Current balance: 🪙 ${_uiState.value.userCoins}.",
                icon = "🪙",
                type = "WARNING"
            )
            return
        }
        val newCoins = _uiState.value.userCoins - skin.priceCoins
        profilePreferences.setUserCoins(newCoins)
        profilePreferences.addOwnedItem(skin.id)
        profilePreferences.setEquippedTokenSkin(skin.id)
        _uiState.update {
            it.copy(
                userCoins = newCoins,
                equippedPawnSkin = skin.id,
                bannerNotice = "👑 Acquired & Equipped ${skin.name} Token!"
            )
        }
        viewModelScope.launch {
            inventoryRepository.syncWalletBalance(goldCoins = newCoins, cashNotes = _uiState.value.userGems)
            inventoryRepository.grantItem(itemId = skin.id, source = "SHOP_PURCHASE")
            val (_, snap) = inventoryRepository.equipItem(itemId = skin.id)
            applyInventorySnapshot(snap)
        }
        soundAndHaptics.soundManager.playVictoryFanfare()
        showPopUpNotice(
            title = "👑 Token Skin Unlocked!",
            message = "Successfully purchased and equipped ${skin.name}!",
            icon = "👑",
            type = "REWARD"
        )
    }

    fun equipTokenSkin(skinId: String) {
        profilePreferences.setEquippedTokenSkin(skinId)
        _uiState.update {
            it.copy(
                equippedPawnSkin = skinId,
                bannerNotice = "👑 Equipped Token: $skinId"
            )
        }
        viewModelScope.launch {
            val (ok, snap) = inventoryRepository.equipItem(itemId = skinId)
            if (ok) applyInventorySnapshot(snap)
        }
        soundAndHaptics.playPawnMove()
    }

    fun isItemOwned(itemId: String): Boolean {
        return _uiState.value.inventoryUiState.isOwned(itemId) || profilePreferences.isItemOwned(itemId)
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val (coins, gems) = repository.claimTodayReward()
            if (coins > 0) {
                soundAndHaptics.vibrateVictory()
                soundAndHaptics.playCoinReward()
                triggerCelebration(CelebrationType.MATCH_WON)
                val updatedStatus = repository.getDailyRewardStatus()
                _uiState.update { s ->
                    val newCoins = s.userCoins + coins
                    val newGems = s.userGems + gems
                    profilePreferences.setUserCoins(newCoins)
                    profilePreferences.setUserGems(newGems)
                    s.copy(
                        userCoins = newCoins,
                        userGems = newGems,
                        dailyRewardStatus = updatedStatus,
                        bannerNotice = "👑 Claimed Day ${updatedStatus.streakDay} Reward: +$coins Coins, +$gems Gems!"
                    )
                }
            }
        }
    }

    fun claimDailySpinReward(coins: Long, gems: Int) {
        soundAndHaptics.vibrateVictory()
        soundAndHaptics.playCoinReward()
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            repository.updateDailySpinTimestamp(now)
        }
        _uiState.update {
            val newCoins = it.userCoins + coins
            val newGems = it.userGems + gems
            profilePreferences.setUserCoins(newCoins)
            profilePreferences.setUserGems(newGems)
            it.copy(
                userCoins = newCoins,
                userGems = newGems,
                lastSpinTimestampMillis = now,
                bannerNotice = "Maharaja Treasury: +$coins Coins, +$gems Diamonds awarded!"
            )
        }
    }

    fun rewardUserForAd(
        coins: Long = 1000L,
        gems: Int = 0,
        adTransactionId: String = "ad_${UUID.randomUUID()}",
        verified: Boolean = true,
        targetProgressItemId: String? = "emoji_guy_crying"
    ) {
        if (!verified) {
            _uiState.update {
                it.copy(bannerNotice = "Ad skipped — no reward or progress granted.")
            }
            return
        }
        viewModelScope.launch {
            val (result, snapshot) = inventoryRepository.recordVerifiedAdCompletion(
                adTransactionId = adTransactionId,
                verified = true,
                coinsReward = coins,
                cashReward = gems,
                targetProgressItemId = targetProgressItemId
            )
            applyInventorySnapshot(snapshot)
            if (result is AdVerificationResult.Verified) {
                soundAndHaptics.vibrateVictory()
                soundAndHaptics.playCoinReward()
                val unlockedText = if (result.unlockedItemIds.isNotEmpty()) {
                    " Unlocked: ${result.unlockedItemIds.mapNotNull { ItemCatalog.findById(it)?.name }.joinToString()}"
                } else ""
                _uiState.update {
                    it.copy(bannerNotice = "🎬 Ad Completed: +$coins Coins, +$gems Cash!$unlockedText")
                }
            }
        }
    }

    fun exchangeGemsForCoins(gemCost: Int, coinsReward: Long): Boolean {
        val currentGems = _uiState.value.userGems
        if (currentGems < gemCost) {
            _uiState.update {
                it.copy(bannerNotice = "Not enough diamonds to purchase this coin pack!")
            }
            return false
        }
        soundAndHaptics.vibrateVictory()
        soundAndHaptics.playCoinReward()
        _uiState.update {
            val newGems = it.userGems - gemCost
            val newCoins = it.userCoins + coinsReward
            profilePreferences.setUserCoins(newCoins)
            profilePreferences.setUserGems(newGems)
            it.copy(
                userCoins = newCoins,
                userGems = newGems,
                bannerNotice = "Exchanged $gemCost Diamonds for +$coinsReward Coins!"
            )
        }
        return true
    }

    fun startMatchmaking(
        mode: GameMode,
        playerCount: Int = 4,
        chosenColor: LudoColor = _uiState.value.userChosenColor,
        wager: Long = 1500L,
        customRoomCode: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isMatchmaking = true,
                    currentWager = wager,
                    userChosenColor = chosenColor,
                    matchmakingStatus = if (customRoomCode != null) "Connecting to Royal Court ${customRoomCode}..." else "Searching for worthy opponents...",
                    showModeSelectDialog = false,
                    showPrivateRoomDialog = false
                )
            }
            delay(1200)
            _uiState.update { it.copy(matchmakingStatus = "Verifying fair cryptographic seed & ping < 24ms...") }
            delay(1000)
            _uiState.update { it.copy(isMatchmaking = false) }
            startNewGame(
                mode = mode,
                playerCount = playerCount,
                chosenColor = chosenColor,
                wager = wager,
                roomCode = customRoomCode ?: generateRoomCode()
            )
        }
    }

    private fun generateRoomCode(): String = "MG-${Random.nextInt(1000, 9999)}"

    fun startNewGame(
        mode: GameMode,
        playerCount: Int = 4,
        chosenColor: LudoColor = _uiState.value.userChosenColor,
        wager: Long = 1500L,
        roomCode: String = generateRoomCode(),
        isInitialSetup: Boolean = false
    ) {
        if (!isInitialSetup && wager > 0) {
            val currentCoins = _uiState.value.userCoins
            if (currentCoins < wager) {
                showPopUpNotice(
                    title = "🪙 Insufficient Coins",
                    message = "You need 🪙 ${com.example.ui.components.formatCompactCoins(wager)} coins to enter, but you only have 🪙 ${com.example.ui.components.formatCompactCoins(currentCoins)}. Claim free coins from Daily Rewards or Lucky Spin!",
                    icon = "🪙",
                    type = "ERROR"
                )
                return
            }
            val remainingCoins = currentCoins - wager
            profilePreferences.setUserCoins(remainingCoins)
            _uiState.update { it.copy(userCoins = remainingCoins) }
        }

        soundAndHaptics.setGameActive(true)
        aiTurnJob?.cancel()
        turnTimerJob?.cancel()
        moveJob?.cancel()
        isMoveInProgress = false
        processedMoveIds.clear()

        val effectiveCount = when (mode) {
            GameMode.TEAM_2VS2 -> 4
            GameMode.PRACTICE, GameMode.FRIENDS_CHALLENGE -> 2
            else -> playerCount
        }

        val colors = if (mode == GameMode.TEAM_2VS2) {
            listOf(LudoColor.YELLOW, LudoColor.RED, LudoColor.GREEN, LudoColor.BLUE)
        } else if (effectiveCount == 2) {
            val opposite = when (chosenColor) {
                LudoColor.RED -> LudoColor.BLUE
                LudoColor.GREEN -> LudoColor.YELLOW
                LudoColor.YELLOW -> LudoColor.GREEN
                LudoColor.BLUE -> LudoColor.RED
            }
            listOf(chosenColor, opposite)
        } else {
            val remaining = listOf(LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW, LudoColor.BLUE).filter { it != chosenColor }
            listOf(chosenColor) + remaining.take(effectiveCount - 1)
        }

        val players = colors.mapIndexed { index, color ->
            val isHuman = when (mode) {
                GameMode.PASS_AND_PLAY -> true
                else -> index == 0
            }
            val name = when {
                index == 0 -> if (_uiState.value.playerNickname.isNotBlank() && _uiState.value.playerNickname != "You") "You (${_uiState.value.playerNickname})" else "You"
                mode == GameMode.PASS_AND_PLAY -> "Player ${index + 1}"
                mode == GameMode.TEAM_2VS2 && index == 2 -> "Prince Aryan 🟢 (Partner)"
                mode == GameMode.TEAM_2VS2 && index == 1 -> "Abdul 🔴 (Rival)"
                mode == GameMode.TEAM_2VS2 && index == 3 -> "Flora 🔵 (Rival)"
                mode == GameMode.PRACTICE -> "Training Sensei (Bot)"
                mode == GameMode.FRIENDS_CHALLENGE -> "Prince Aryan 🟢✓"
                mode == GameMode.VS_AI -> "Computer ${index + 1}"
                index == 1 -> "Computer 2 (Abdul)"
                index == 2 -> "Computer 3 (Sisiliya)"
                else -> "Computer 4 (Flora)"
            }
            val flag = when (index) {
                0 -> "🇮🇳"
                1 -> "🇦🇫"
                2 -> "🇩🇯"
                else -> "🇬🇧"
            }
            val avatar = when (index) {
                0 -> _uiState.value.playerCrownAvatar
                1 -> "avatar_abdul"
                2 -> "avatar_sisiliya"
                else -> "avatar_flora"
            }
            val playerLevel = when (index) {
                0 -> 18
                1 -> 10
                2 -> 14
                else -> 12
            }

            // In Quick Rush mode, 2 pawns start already active on board to accelerate gameplay!
            val initialPawns = if (mode == GameMode.QUICK_RUSH) {
                listOf(
                    Pawn(id = 0, color = color, state = PawnState.ON_BOARD, trackPosition = color.startTrackPos),
                    Pawn(id = 1, color = color, state = PawnState.ON_BOARD, trackPosition = (color.startTrackPos + 5) % 52),
                    Pawn(id = 2, color = color, state = PawnState.IN_BASE),
                    Pawn(id = 3, color = color, state = PawnState.IN_BASE)
                )
            } else {
                (0..3).map { Pawn(id = it, color = color) }
            }

            Player(
                id = "p_$index",
                name = name,
                color = color,
                isAi = !isHuman,
                countryFlag = flag,
                isOnline = mode == GameMode.ONLINE_MULTIPLAYER || mode == GameMode.PRIVATE_ROOM || mode == GameMode.FRIENDS_CHALLENGE,
                avatar = avatar,
                customPhotoUri = if (index == 0) _uiState.value.customProfilePhotoUri else null,
                level = playerLevel,
                pawns = initialPawns,
                coins = if (index == 0) _uiState.value.userCoins else 15000L + Random.nextInt(5000)
            )
        }

        val modeBanner = when (mode) {
            GameMode.TEAM_2VS2 -> "🤝 Team Ludo 2 vs 2! Team Sun (Yellow & Green) vs Team Moon (Red & Blue)! Protect your partner!"
            GameMode.PRACTICE -> "🎓 Royal Practice Academy! Strategy hints & Undo Move enabled!"
            GameMode.FRIENDS_CHALLENGE -> "⚔️ Royal Duel! You vs Prince Aryan in a direct 1-on-1 battle!"
            else -> "Royal ${mode.title} begins! Wager: 🪙 ${com.example.ui.components.formatCompactCoins(wager)} • 2X Double Prize Active!"
        }

        val initialChecksum = LudoEngine.computeMatchStateHash(players, 0, 0L)
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.GAME,
                gameMode = mode,
                players = players,
                currentWager = wager,
                userChosenColor = chosenColor,
                wonChestBoxName = null,
                currentPlayerIndex = 0,
                turnPhase = TurnPhase.ROLL_DICE,
                currentDiceValue = 1,
                isRolling = false,
                consecutiveSixes = 0,
                winner = null,
                rankings = emptyList(),
                roomCode = roomCode,
                practiceHint = null,
                canUndoPracticeMove = false,
                currentMatchReplayMoves = emptyList(),
                moveSequenceNumber = 0L,
                lastSyncedBoardChecksum = initialChecksum,
                connectionState = MatchConnectionState.CONNECTED,
                bannerNotice = modeBanner,
                chatMessages = listOf(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        senderName = "Royal Court",
                        senderColor = LudoColor.YELLOW,
                        text = if (mode == GameMode.TEAM_2VS2) "Team Ludo active! Work with partner to home 4 total pawns!" else "Match started! 2X Double Prize active: Winner claims double coins and a Mystery Chest Box!"
                    )
                )
            )
        }

        if (!isInitialSetup) {
            persistActiveMatchSnapshot()
        }
        voiceChatManager.startVoiceChat()
        startTurnTimer()
        checkAiTurn()

        // After game is completely opened, show option to invite online friend or ignore
        if (mode != GameMode.PASS_AND_PLAY) {
            viewModelScope.launch {
                delay(5000)
                if (_uiState.value.currentScreen == AppScreen.GAME && _uiState.value.winner == null) {
                    showPopUpNotice(
                        title = "Prince Aryan is Online! 🟢✓",
                        message = "Prince Aryan is online with a green tick! Would you like to invite him to this game or ignore?",
                        icon = "👑",
                        type = "FRIEND_ONLINE"
                    )
                }
            }
        }
    }

    private fun startTurnTimer() {
        turnTimerJob?.cancel()
        val totalSec = if (_uiState.value.gameMode == GameMode.QUICK_RUSH) 15 else 25
        _uiState.update { it.copy(turnTimeRemaining = totalSec) }

        turnTimerJob = viewModelScope.launch {
            for (sec in totalSec downTo 0) {
                delay(1000)
                _uiState.update { it.copy(turnTimeRemaining = sec) }
            }
            // Timeout action
            handleTimeout()
        }
    }

    private fun handleTimeout() {
        val state = _uiState.value
        if (state.winner != null) return

        val currentPlayer = state.players.getOrNull(state.currentPlayerIndex) ?: return
        if (state.turnPhase == TurnPhase.ROLL_DICE) {
            // Auto roll on timeout
            rollDice()
        } else if (state.turnPhase == TurnPhase.SELECT_PAWN && !isMoveInProgress) {
            // Auto select best pawn
            val movable = currentPlayer.pawns.filter { it.isMovable }
            if (movable.isNotEmpty()) {
                val best = LudoEngine.pickBestAiMove(currentPlayer, movable, state.currentDiceValue, state.players, state.gameMode)
                    ?: movable.first()
                executePawnMove(best, isAutomatedTrigger = true)
            } else {
                passToNextPlayer()
            }
        }
    }

    fun rollDice() {
        val state = _uiState.value
        if (state.isRolling || state.turnPhase != TurnPhase.ROLL_DICE || state.winner != null || isMoveInProgress) return

        turnTimerJob?.cancel()

        viewModelScope.launch {
            val (rawRollValue, proof) = FairDiceEngine.rollDice()
            val rollValue = when (state.activePowerUp) {
                "EVEN" -> listOf(2, 4, 6).random()
                "ODD" -> listOf(1, 3, 5).random()
                "HIGH" -> listOf(4, 5, 6).random()
                "LOW" -> listOf(1, 2, 3).random()
                "LIGHTNING" -> 6
                else -> rawRollValue
            }
            _uiState.update { it.copy(isRolling = true, currentDiceValue = rollValue, fairDiceProof = proof, activePowerUp = null) }
            val equippedDice = state.diceStoreUiState.findDice(state.equippedDiceSkin)
            if (equippedDice != null) {
                soundAndHaptics.playDiceRoll(equippedDice.benefits.soundEffect)
            } else {
                soundAndHaptics.playDiceRoll()
            }

            // Intermediate bounce 1 impact haptics
            delay(360)
            soundAndHaptics.playButtonClick()

            // Settle trajectory
            delay(420)

            val latestState = _uiState.value
            if (latestState.winner != null || latestState.currentPlayerIndex !in latestState.players.indices) {
                _uiState.update { it.copy(isRolling = false) }
                return@launch
            }
            val currentPlayer = latestState.players[latestState.currentPlayerIndex]
            val newConsecutive = if (rollValue == 6) latestState.consecutiveSixes + 1 else 0

            // If three 6s in a row: forfeit turn!
            if (newConsecutive >= 3) {
                _uiState.update { s ->
                    val newPlayers = s.players.toMutableList()
                    if (s.currentPlayerIndex in newPlayers.indices) {
                        newPlayers[s.currentPlayerIndex] = currentPlayer.copy(lastDiceValue = rollValue)
                    }
                    s.copy(
                        players = newPlayers,
                        currentDiceValue = rollValue,
                        isRolling = false,
                        fairDiceProof = proof,
                        consecutiveSixes = 0,
                        bannerNotice = "3 Sixes in a row! Penalty: Turn forfeited!"
                    )
                }
                persistActiveMatchSnapshot()
                delay(1100)
                passToNextPlayer()
                return@launch
            }

            if (!currentPlayer.isAi) {
                updateMissionProgress("task_roll_dice", 1)
                viewModelScope.launch {
                    val (_, dSnap) = diceStoreRepository.incrementChallengeProgress(delta = 1)
                    applyDiceStoreSnapshot(dSnap)
                }
            }

            if (rollValue == 6) {
                soundAndHaptics.playSixRolled()
                triggerCelebration(CelebrationType.SIX_ROLLED)
            }

            val updatedPawns = currentPlayer.pawns.map { pawn ->
                pawn.copy(isMovable = LudoEngine.canPawnMove(pawn, rollValue))
            }
            val movableCount = updatedPawns.count { it.isMovable }

            _uiState.update { s ->
                val newPlayers = s.players.toMutableList()
                if (s.currentPlayerIndex in newPlayers.indices) {
                    newPlayers[s.currentPlayerIndex] = currentPlayer.copy(pawns = updatedPawns, lastDiceValue = rollValue)
                }
                s.copy(
                    players = newPlayers,
                    currentDiceValue = rollValue,
                    isRolling = false,
                    fairDiceProof = proof,
                    consecutiveSixes = newConsecutive,
                    turnPhase = if (movableCount > 0) TurnPhase.SELECT_PAWN else TurnPhase.ROLL_DICE,
                    lastSyncedBoardChecksum = LudoEngine.computeMatchStateHash(newPlayers, s.currentPlayerIndex, s.moveSequenceNumber),
                    bannerNotice = if (movableCount == 0) "${currentPlayer.name} rolled $rollValue with no valid moves." else "${currentPlayer.name} rolled $rollValue! Select pawn to move."
                )
            }
            persistActiveMatchSnapshot()

            if (movableCount == 0) {
                delay(850)
                passToNextPlayer()
            } else if (movableCount == 1 && !currentPlayer.isAi) {
                // Auto-advance only if user did not manually click during the brief pause
                delay(400)
                val postDelayState = _uiState.value
                if (postDelayState.turnPhase == TurnPhase.SELECT_PAWN && !isMoveInProgress) {
                    val onlyPawn = postDelayState.players
                        .getOrNull(postDelayState.currentPlayerIndex)
                        ?.pawns
                        ?.firstOrNull { it.isMovable }
                    if (onlyPawn != null) {
                        executePawnMove(onlyPawn, isAutomatedTrigger = true)
                    }
                }
            } else if (currentPlayer.isAi) {
                delay(550)
                val postDelayState = _uiState.value
                if (postDelayState.turnPhase == TurnPhase.SELECT_PAWN && !isMoveInProgress) {
                    val aiPlayer = postDelayState.players.getOrNull(postDelayState.currentPlayerIndex) ?: return@launch
                    val aiBestPawn = LudoEngine.pickBestAiMove(
                        player = aiPlayer,
                        movablePawns = aiPlayer.pawns.filter { it.isMovable },
                        dice = rollValue,
                        allPlayers = postDelayState.players,
                        mode = postDelayState.gameMode
                    )
                    if (aiBestPawn != null) {
                        executePawnMove(aiBestPawn, isAutomatedTrigger = true)
                    } else {
                        passToNextPlayer()
                    }
                }
            } else {
                // Multiple movable human tokens: restart turn timer cleanly for selection
                startTurnTimer()
            }
        }
    }

    /**
     * Public UI entry point when a player taps a token on the board.
     * Strictly rejects tapping another player's token or tapping during an AI turn.
     */
    fun onPawnClicked(pawn: Pawn) {
        val state = _uiState.value
        val currentPlayer = state.players.getOrNull(state.currentPlayerIndex) ?: return
        if (currentPlayer.isAi) return
        if (pawn.color != currentPlayer.color) return
        executePawnMove(pawn, isAutomatedTrigger = false)
    }

    private fun executePawnMove(pawn: Pawn, isAutomatedTrigger: Boolean) {
        val state = _uiState.value
        if (state.turnPhase != TurnPhase.SELECT_PAWN || state.winner != null || isMoveInProgress) return

        val currentPlayer = state.players.getOrNull(state.currentPlayerIndex) ?: return
        if (pawn.color != currentPlayer.color) return

        val canonicalPawn = currentPlayer.pawns.firstOrNull { it.id == pawn.id } ?: return
        if (!canonicalPawn.isMovable || !LudoEngine.canPawnMove(canonicalPawn, state.currentDiceValue)) return

        val moveDedupeId = "${state.roomCode}_seq${state.moveSequenceNumber}_c${canonicalPawn.color.name}_p${canonicalPawn.id}_d${state.currentDiceValue}"
        if (!processedMoveIds.add(moveDedupeId)) return

        if (state.gameMode == GameMode.PRACTICE && !currentPlayer.isAi) {
            practiceLastState = state.players
            _uiState.update { it.copy(canUndoPracticeMove = true) }
        }

        isMoveInProgress = true
        turnTimerJob?.cancel()
        aiTurnJob?.cancel()

        moveJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(turnPhase = TurnPhase.ANIMATING_MOVE) }

                val stepPath = LudoEngine.buildStepPath(canonicalPawn, state.currentDiceValue)
                val movedPawn = stepPath.lastOrNull() ?: LudoEngine.calculateMoveResult(canonicalPawn, state.currentDiceValue)

                // Step-by-step cell-by-cell animated token movement
                if (stepPath.isNotEmpty()) {
                    stepPath.forEachIndexed { idx, stepPawn ->
                        soundAndHaptics.playPawnMove(
                            stepIndex = idx + 1,
                            totalSteps = stepPath.size,
                            isHomeStretch = stepPawn.state == PawnState.IN_HOME_STRETCH
                        )
                        _uiState.update { s ->
                            val stepPlayers = s.players.mapIndexed { pIdx, p ->
                                if (pIdx == s.currentPlayerIndex) {
                                    p.copy(pawns = p.pawns.map { existing ->
                                        if (existing.id == canonicalPawn.id) stepPawn else existing.copy(isMovable = false)
                                    })
                                } else p
                            }
                            s.copy(players = stepPlayers)
                        }
                        if (stepPath.size > 1 && idx < stepPath.lastIndex) {
                            delay(105L)
                        }
                    }
                }

                var gaveExtraTurn = false
                var bannerMsg: String? = null

                val preCaptureState = _uiState.value
                // Check if captured enemy pawn(s) (respecting Team 2vs2 co-op protection & Safe Star zones)
                val captures = LudoEngine.checkForCaptures(movedPawn, preCaptureState.players, preCaptureState.gameMode)
                val updatedPlayers = preCaptureState.players.map { player ->
                    if (player.color == currentPlayer.color) {
                        val newPawns = player.pawns.map { if (it.id == canonicalPawn.id) movedPawn else it.copy(isMovable = false) }
                        player.copy(pawns = newPawns)
                    } else {
                        // Check if any pawn of this player was captured
                        val capturedOfPlayer = captures.filter { it.first.color == player.color }.map { it.second.id }
                        if (capturedOfPlayer.isNotEmpty()) {
                            val resetPawns = player.pawns.map { p ->
                                if (p.id in capturedOfPlayer) p.copy(state = PawnState.IN_BASE, trackPosition = -1, homeStep = 0, isMovable = false) else p
                            }
                            player.copy(pawns = resetPawns)
                        } else {
                            player
                        }
                    }
                }.toMutableList()

            // Record this move for Match Replay Theater
            val moveDescription = when {
                captures.isNotEmpty() -> "⚔️ Capture! ${currentPlayer.name} captured ${captures.joinToString { it.first.name }}'s pawn!"
                movedPawn.state == PawnState.FINISHED -> "🏛️ Palace Finish! ${currentPlayer.name}'s pawn reached home!"
                LudoEngine.isSafePosition(movedPawn.trackPosition) -> "⭐ Safe Zone! Reached star position"
                else -> "${currentPlayer.name} moved pawn #${pawn.id + 1} by ${state.currentDiceValue} steps"
            }
            val recordedMove = com.example.model.ReplayMove(
                stepNumber = state.currentMatchReplayMoves.size + 1,
                playerName = currentPlayer.name,
                playerColor = currentPlayer.color,
                playerAvatar = currentPlayer.avatar,
                diceRoll = state.currentDiceValue,
                pawnId = pawn.id,
                fromPosition = pawn.trackPosition,
                toPosition = movedPawn.trackPosition,
                isCapture = captures.isNotEmpty(),
                capturedPlayerName = captures.firstOrNull()?.first?.name,
                isHome = movedPawn.state == PawnState.FINISHED,
                description = moveDescription
            )
            val updatedReplayMoves = state.currentMatchReplayMoves + recordedMove

            if (captures.isNotEmpty()) {
                soundAndHaptics.playPawnCapture()
                triggerCelebration(CelebrationType.PAWN_CAPTURED)
                val enemyNames = captures.joinToString { it.first.name }
                bannerMsg = "⚔️ Capture! ${currentPlayer.name} captured $enemyNames! Extra Turn awarded!"
                gaveExtraTurn = true
                updateMissionProgress("quest_capture_pawns", captures.size)
            } else if (LudoEngine.isSafePosition(movedPawn.trackPosition) || movedPawn.state == PawnState.IN_HOME_STRETCH) {
                // Landing on safe tile / star square / safe column
                soundAndHaptics.playSafeTile()
                if (!currentPlayer.isAi) {
                    updateMissionProgress("task_token_safety", 1)
                }
            }

            // Check if pawn reached home
            if (movedPawn.state == PawnState.FINISHED && pawn.state != PawnState.FINISHED) {
                soundAndHaptics.playPawnHome()
                triggerCelebration(CelebrationType.PAWN_FINISHED)
                bannerMsg = "👑 Royal Home! A pawn entered the center triangle! Bonus Turn!"
                gaveExtraTurn = true
            }

            // Check if rolled 6
            if (state.currentDiceValue == 6) {
                gaveExtraTurn = true
                bannerMsg = bannerMsg ?: "Rolled 6! ${currentPlayer.name} earns an extra roll!"
            }

            // Check Win Condition based on active game mode (Classic, Rush, Bolt, Dash, Team 2vs2)
            val updatedCurrentPlayer = updatedPlayers[state.currentPlayerIndex]
            val isWinner = com.example.engine.LudoEngine.hasPlayerWon(updatedCurrentPlayer, state.gameMode, updatedPlayers)

            if (isWinner) {
                soundAndHaptics.vibrateVictory()
                val isHumanWin = updatedCurrentPlayer.id == "p_0"
                if (isHumanWin) {
                    soundAndHaptics.playCoinReward()
                }
                triggerCelebration(CelebrationType.MATCH_WON)
                profilePreferences.recordGameFinished(isWin = isHumanWin, captures = captures.size)

                // Track daily tasks on game finish
                updateMissionProgress("task_complete_matches", 1)
                if (isHumanWin && state.gameMode == GameMode.ONLINE_MULTIPLAYER) {
                    updateMissionProgress("task_win_online", 1)
                }
                if (state.gameMode == GameMode.QUICK_RUSH || state.gameMode == GameMode.RUSH) {
                    updateMissionProgress("task_quick_match", 1)
                }

                val matchWager = state.currentWager
                val basePot = matchWager * state.players.size
                val coinWinnings = basePot * 2 // 2X Double Winning Prize!
                val wonChest = if (isHumanWin) {
                    when {
                        matchWager >= 25000L -> "Imperial Diamond Chest 💎"
                        matchWager >= 10000L -> "Maharaja Gold Chest 👑"
                        matchWager >= 5000L -> "Royal Emerald Chest 🟢"
                        else -> "Royal Ruby Mystery Chest 📦"
                    }
                } else null

                val sortedPlayers = updatedPlayers.sortedWith(
                    compareByDescending<Player> { it.id == updatedCurrentPlayer.id }
                        .thenByDescending { it.finishedPawnsCount }
                )
                val leaderboardSummary = sortedPlayers.mapIndexed { index, p ->
                    "${index + 1}:${p.name}:${p.color.title}:${p.finishedPawnsCount}"
                }.joinToString(";")
                val userRank = sortedPlayers.indexOfFirst { it.id == "p_0" } + 1
                val finalRank = if (userRank > 0) userRank else 4
                val coinsEarned = if (isHumanWin) coinWinnings else -matchWager

                viewModelScope.launch {
                    repository.recordMatch(
                        gameMode = state.gameMode.title,
                        wager = matchWager,
                        coinsWon = coinsEarned,
                        userRank = finalRank,
                        winnerName = updatedCurrentPlayer.name,
                        winnerColor = updatedCurrentPlayer.color.title,
                        durationSeconds = 180 + Random.nextInt(120),
                        leaderboardSummary = leaderboardSummary
                    )
                }

                val isOnlineMatch = (state.gameMode == GameMode.ONLINE_MULTIPLAYER ||
                        state.gameMode == GameMode.PRIVATE_ROOM ||
                        state.gameMode == GameMode.TOURNAMENT)
                val newOnlineCount = if (isOnlineMatch) profilePreferences.incrementOnlineGamesPlayed() else profilePreferences.getOnlineGamesPlayed()
                val triggerSpecialOffer = isOnlineMatch && (newOnlineCount > 0 && newOnlineCount % 5 == 0)
                if (isOnlineMatch) {
                    updateMissionProgress("quest_online_matches", 1)
                }

                // Finalize match replay session
                val completedReplaySession = com.example.model.MatchReplaySession(
                    id = "replay_${System.currentTimeMillis()}",
                    matchTitle = "${state.gameMode.title} Match",
                    timestamp = System.currentTimeMillis(),
                    gameMode = state.gameMode.title,
                    wager = matchWager,
                    winnerName = updatedCurrentPlayer.name,
                    winnerColor = updatedCurrentPlayer.color,
                    winnerAvatar = updatedCurrentPlayer.avatar,
                    totalMovesCount = updatedReplayMoves.size,
                    durationFormatted = "3m 30s",
                    moves = updatedReplayMoves
                )

                val now = System.currentTimeMillis()
                val activeXpBoost = _uiState.value.inventoryUiState.activePowerups.any {
                    it.powerupType == "XP_BOOSTER" && it.isCurrentlyActive(now)
                }
                val activeRecoverShield = _uiState.value.inventoryUiState.activePowerups.any {
                    it.powerupType == "RECOVER_COINS" && it.isCurrentlyActive(now)
                }
                val xpGain = (if (isHumanWin) 120 else 35) * (if (activeXpBoost) 2 else 1)
                val recoveredCoins = if (!isHumanWin && activeRecoverShield && matchWager > 0) matchWager / 2 else 0L

                    val nextSeq = _uiState.value.moveSequenceNumber + 1L
                    val nextChecksum = LudoEngine.computeMatchStateHash(updatedPlayers, state.currentPlayerIndex, nextSeq)
                    _uiState.update { s ->
                        val newCoins = when {
                            isHumanWin -> s.userCoins + coinWinnings
                            recoveredCoins > 0L -> s.userCoins + recoveredCoins
                            else -> s.userCoins
                        }
                        if (isHumanWin || recoveredCoins > 0L) {
                            profilePreferences.setUserCoins(newCoins)
                        }
                        s.copy(
                            players = updatedPlayers,
                            winner = updatedCurrentPlayer,
                            turnPhase = TurnPhase.ROUND_OVER,
                            moveSequenceNumber = nextSeq,
                            lastSyncedBoardChecksum = nextChecksum,
                            showResultsDialog = true,
                            showSpecialOfferDialog = triggerSpecialOffer,
                            onlineGamesPlayedCount = newOnlineCount,
                            wonChestBoxName = wonChest,
                            playerXp = s.playerXp + xpGain,
                            totalWins = profilePreferences.getTotalWins(),
                            totalGamesPlayed = profilePreferences.getGamesPlayed(),
                            winStreak = profilePreferences.getWinStreak(),
                            totalCaptures = profilePreferences.getTotalCaptures(),
                            bannerNotice = when {
                                isHumanWin -> "🏆 2X DOUBLE PRIZE! ${updatedCurrentPlayer.name} won +🪙 ${com.example.ui.components.formatCompactCoins(coinWinnings)} (+${xpGain} XP)!"
                                recoveredCoins > 0L -> "🛡️ Recover Coins Shield activated! Recovered 🪙 ${com.example.ui.components.formatCompactCoins(recoveredCoins)}!"
                                else -> "Match concluded. Earned +${xpGain} XP."
                            },
                            userCoins = newCoins,
                            savedReplays = listOf(completedReplaySession) + s.savedReplays,
                            currentMatchReplayMoves = emptyList()
                        )
                    }
                    profilePreferences.saveActiveMatchSnapshotJson(null)
                    viewModelScope.launch {
                        val current = _uiState.value
                        val snap = inventoryRepository.syncWalletBalance(goldCoins = current.userCoins, cashNotes = current.userGems)
                        applyInventorySnapshot(snap)
                        if (!isHumanWin || isHumanWin) {
                            val (_, evSnap) = inventoryRepository.incrementEventItemProgress(itemId = "emoji_squall", delta = 1)
                            applyInventorySnapshot(evSnap)
                        }
                    }
                    turnTimerJob?.cancel()
                    if (triggerSpecialOffer) {
                        showPopUpNotice(
                            title = "🔥 5 ONLINE GAMES OFFER UNLOCKED!",
                            message = "Congratulations on playing 5 online games! You unlocked the exclusive 80% OFF Maharaja Deal!",
                            icon = "🔥",
                            type = "REWARD"
                        )
                    }
                    return@launch
                }

                val nextSeq = _uiState.value.moveSequenceNumber + 1L
                val nextChecksum = LudoEngine.computeMatchStateHash(updatedPlayers, state.currentPlayerIndex, nextSeq)
                _uiState.update { s ->
                    s.copy(
                        players = updatedPlayers,
                        moveSequenceNumber = nextSeq,
                        lastSyncedBoardChecksum = nextChecksum,
                        currentMatchReplayMoves = updatedReplayMoves,
                        bannerNotice = bannerMsg ?: s.bannerNotice
                    )
                }
                persistActiveMatchSnapshot()

                delay(320)

                if (gaveExtraTurn) {
                    _uiState.update {
                        it.copy(
                            turnPhase = TurnPhase.ROLL_DICE
                        )
                    }
                    persistActiveMatchSnapshot()
                    startTurnTimer()
                    checkAiTurn()
                } else {
                    passToNextPlayer()
                }
            } finally {
                isMoveInProgress = false
            }
        }
    }

    private fun passToNextPlayer() {
        val state = _uiState.value
        if (state.winner != null || state.players.isEmpty()) return

        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        val nextPlayer = state.players[nextIndex]

        // Reset movable flags
        val clearedPlayers = state.players.map { player ->
            player.copy(pawns = player.pawns.map { it.copy(isMovable = false) })
        }
        val nextSeq = state.moveSequenceNumber + 1L
        val nextChecksum = LudoEngine.computeMatchStateHash(clearedPlayers, nextIndex, nextSeq)

        _uiState.update {
            it.copy(
                players = clearedPlayers,
                currentPlayerIndex = nextIndex,
                turnPhase = TurnPhase.ROLL_DICE,
                consecutiveSixes = 0,
                moveSequenceNumber = nextSeq,
                lastSyncedBoardChecksum = nextChecksum,
                bannerNotice = "Turn: ${nextPlayer.name} (${nextPlayer.color.title})"
            )
        }
        persistActiveMatchSnapshot()

        startTurnTimer()
        voiceChatManager.onPlayerTurnStart(nextPlayer.id, nextPlayer.isAi)
        checkAiTurn()
    }

    /**
     * Authoritative Multiplayer Move Synchronization:
     * Validates turn ownership, sequence order, deduplication, and move legality
     * so all connected clients stay identically synchronized and duplicate/spoofed moves are blocked.
     */
    fun syncRemoteMoveIfAuthorized(
        senderColor: LudoColor,
        pawnId: Int,
        diceRoll: Int,
        expectedSequenceNumber: Long
    ): Boolean {
        val state = _uiState.value
        if (state.winner != null || isMoveInProgress) return false
        if (expectedSequenceNumber != state.moveSequenceNumber) return false

        val activePlayer = state.players.getOrNull(state.currentPlayerIndex) ?: return false
        if (activePlayer.color != senderColor) return false

        val pawn = activePlayer.pawns.firstOrNull { it.id == pawnId } ?: return false
        if (diceRoll !in 1..6 || !LudoEngine.canPawnMove(pawn, diceRoll)) return false

        val updatedActivePawns = activePlayer.pawns.map {
            if (it.id == pawnId) it.copy(isMovable = true) else it.copy(isMovable = false)
        }
        _uiState.update { s ->
            val updatedPlayers = s.players.mapIndexed { idx, p ->
                if (idx == s.currentPlayerIndex) p.copy(pawns = updatedActivePawns, lastDiceValue = diceRoll) else p
            }
            s.copy(
                players = updatedPlayers,
                currentDiceValue = diceRoll,
                isRolling = false,
                turnPhase = TurnPhase.SELECT_PAWN
            )
        }
        executePawnMove(pawn.copy(isMovable = true), isAutomatedTrigger = true)
        return true
    }

    fun simulateNetworkDisconnect() {
        _uiState.update {
            it.copy(
                connectionState = MatchConnectionState.DISCONNECTED,
                bannerNotice = "Connection interrupted. Preserving token positions for reconnect..."
            )
        }
        persistActiveMatchSnapshot()
    }

    fun reconnectActiveMatch(): Boolean {
        _uiState.update {
            it.copy(
                connectionState = MatchConnectionState.RECONNECTING,
                bannerNotice = "Reconnecting to match server & syncing token positions..."
            )
        }
        val restored = restoreSavedMatchIfPresent()
        val state = _uiState.value
        val verifiedChecksum = if (state.players.isNotEmpty()) {
            LudoEngine.computeMatchStateHash(state.players, state.currentPlayerIndex, state.moveSequenceNumber)
        } else ""
        _uiState.update {
            it.copy(
                connectionState = MatchConnectionState.CONNECTED,
                lastSyncedBoardChecksum = verifiedChecksum,
                bannerNotice = if (restored) "Reconnected! All ${it.players.sumOf { p -> p.pawns.size }} tokens synchronized." else "Connected and synchronized."
            )
        }
        return restored
    }

    private fun persistActiveMatchSnapshot() {
        val s = _uiState.value
        if (s.players.isEmpty() || s.winner != null) return
        try {
            val root = org.json.JSONObject().apply {
                put("gameMode", s.gameMode.name)
                put("roomCode", s.roomCode)
                put("currentWager", s.currentWager)
                put("userChosenColor", s.userChosenColor.name)
                put("currentPlayerIndex", s.currentPlayerIndex)
                put("turnPhase", s.turnPhase.name)
                put("currentDiceValue", s.currentDiceValue)
                put("consecutiveSixes", s.consecutiveSixes)
                put("moveSequenceNumber", s.moveSequenceNumber)
                put("lastSyncedBoardChecksum", s.lastSyncedBoardChecksum)
                val playersArray = org.json.JSONArray()
                for (player in s.players) {
                    val pObj = org.json.JSONObject().apply {
                        put("id", player.id)
                        put("name", player.name)
                        put("color", player.color.name)
                        put("isAi", player.isAi)
                        put("isOnline", player.isOnline)
                        put("countryFlag", player.countryFlag)
                        put("avatar", player.avatar)
                        put("level", player.level)
                        put("coins", player.coins)
                        put("lastDiceValue", player.lastDiceValue)
                        val pawnsArray = org.json.JSONArray()
                        for (pawn in player.pawns) {
                            pawnsArray.put(org.json.JSONObject().apply {
                                put("id", pawn.id)
                                put("color", pawn.color.name)
                                put("state", pawn.state.name)
                                put("trackPosition", pawn.trackPosition)
                                put("homeStep", pawn.homeStep)
                                put("isMovable", pawn.isMovable)
                            })
                        }
                        put("pawns", pawnsArray)
                    }
                    playersArray.put(pObj)
                }
                put("players", playersArray)
            }
            profilePreferences.saveActiveMatchSnapshotJson(root.toString())
        } catch (_: Exception) {
        }
    }

    private fun restoreSavedMatchIfPresent(): Boolean {
        val raw = profilePreferences.getActiveMatchSnapshotJson() ?: return false
        return try {
            val root = org.json.JSONObject(raw)
            val playersArray = root.getJSONArray("players")
            if (playersArray.length() == 0) return false
            val restoredPlayers = mutableListOf<Player>()
            for (i in 0 until playersArray.length()) {
                val pObj = playersArray.getJSONObject(i)
                val pColor = LudoColor.valueOf(pObj.getString("color"))
                val pawnsArray = pObj.getJSONArray("pawns")
                val pawns = mutableListOf<Pawn>()
                for (j in 0 until pawnsArray.length()) {
                    val pn = pawnsArray.getJSONObject(j)
                    pawns.add(
                        Pawn(
                            id = pn.getInt("id"),
                            color = LudoColor.valueOf(pn.getString("color")),
                            state = PawnState.valueOf(pn.getString("state")),
                            trackPosition = pn.getInt("trackPosition"),
                            homeStep = pn.getInt("homeStep"),
                            isMovable = pn.optBoolean("isMovable", false)
                        )
                    )
                }
                restoredPlayers.add(
                    Player(
                        id = pObj.getString("id"),
                        name = pObj.getString("name"),
                        color = pColor,
                        isAi = pObj.optBoolean("isAi", false),
                        isOnline = pObj.optBoolean("isOnline", false),
                        countryFlag = pObj.optString("countryFlag", "🇮🇳"),
                        avatar = pObj.optString("avatar", "crown_imperial_gold"),
                        level = pObj.optInt("level", 12),
                        coins = pObj.optLong("coins", 15000L),
                        pawns = pawns,
                        lastDiceValue = pObj.optInt("lastDiceValue", 1)
                    )
                )
            }
            val playerIdx = root.getInt("currentPlayerIndex").coerceIn(0, restoredPlayers.lastIndex)
            val moveSeq = root.optLong("moveSequenceNumber", 0L)
            val rawPhase = TurnPhase.valueOf(root.getString("turnPhase"))
            val safePhase = if (rawPhase == TurnPhase.ANIMATING_MOVE) TurnPhase.ROLL_DICE else rawPhase
            val checksum = LudoEngine.computeMatchStateHash(restoredPlayers, playerIdx, moveSeq)
            _uiState.update {
                it.copy(
                    gameMode = GameMode.valueOf(root.getString("gameMode")),
                    roomCode = root.getString("roomCode"),
                    currentWager = root.getLong("currentWager"),
                    userChosenColor = LudoColor.valueOf(root.getString("userChosenColor")),
                    players = restoredPlayers,
                    currentPlayerIndex = playerIdx,
                    turnPhase = safePhase,
                    currentDiceValue = root.getInt("currentDiceValue").coerceIn(1, 6),
                    consecutiveSixes = root.optInt("consecutiveSixes", 0),
                    moveSequenceNumber = moveSeq,
                    lastSyncedBoardChecksum = checksum,
                    isRolling = false,
                    winner = null
                )
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun checkAiTurn() {
        aiTurnJob?.cancel()
        val state = _uiState.value
        val currentPlayer = state.players.getOrNull(state.currentPlayerIndex) ?: return

        if (currentPlayer.isAi && state.winner == null) {
            aiTurnJob = viewModelScope.launch {
                delay(700 + Random.nextLong(400))
                if (_uiState.value.turnPhase == TurnPhase.ROLL_DICE) {
                    rollDice()
                }
            }
        }
    }

    fun sendChatMessage(rawText: String, isEmoji: Boolean = false) {
        val sanitized = sanitizeChatMessage(rawText)
        if (sanitized.isBlank()) return

        val sender = if (_uiState.value.gameMode == GameMode.PASS_AND_PLAY) {
            _uiState.value.players.getOrNull(_uiState.value.currentPlayerIndex)
                ?: _uiState.value.players.firstOrNull()
                ?: return
        } else {
            _uiState.value.players.firstOrNull { !it.isAi }
                ?: _uiState.value.players.firstOrNull()
                ?: return
        }

        val chat = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = sender.name,
            senderColor = sender.color,
            text = sanitized,
            isEmoji = isEmoji,
            timestamp = System.currentTimeMillis()
        )

        soundAndHaptics.vibrateClick()

        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + chat,
                lastChatMessage = sanitized,
                lastChatSenderName = sender.name
            )
        }

        viewModelScope.launch {
            delay(3500)
            _uiState.update { s ->
                if (s.lastChatMessage == sanitized) s.copy(lastChatMessage = null, lastChatSenderName = null) else s
            }
        }

        simulateOpponentChatResponse()
    }

    private fun sanitizeChatMessage(raw: String): String {
        // Enforce maximum 100 characters and trim leading/trailing whitespace
        var text = raw.trim().take(100)
        // Collapse repeating characters beyond 3 (e.g. 'aaaaaaa' -> 'aaa')
        text = text.replace(Regex("(.)\\1{3,}")) { it.value.take(3) }
        
        // Basic profanity / spam keyword filter
        val blockedWords = listOf(
            "abuse", "idiot", "stupid", "fool", "bastard", "loser", "cheat", "hacker", "scam"
        )
        for (word in blockedWords) {
            val regex = Regex("(?i)\\b$word\\b")
            text = text.replace(regex, "***")
        }
        return text
    }

    fun sendQuickChat(message: String) {
        sendChatMessage(message, isEmoji = false)
    }

    fun sendRoyalEmoji(emoji: String) {
        val human = _uiState.value.players.firstOrNull { !it.isAi } ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeFloatingEmoji = human.name to emoji
                )
            }
            delay(2400)
            _uiState.update { it.copy(activeFloatingEmoji = null) }
        }
        sendChatMessage(emoji, isEmoji = true)
        simulateOpponentEmojiResponse()
    }

    private fun simulateOpponentChatResponse() {
        val aiPlayers = _uiState.value.players.filter { it.isAi }
        if (aiPlayers.isEmpty()) return
        val randomAi = aiPlayers.random()

        viewModelScope.launch {
            delay(1200 + Random.nextLong(1500))
            val responses = listOf(
                "Shabash, Maharaja! 👑",
                "Well played, worthy rival! ⚔️",
                "Kismat aapke saath hai! 🎲",
                "Royal move indeed! 💎",
                "The Throne belongs to me! 🏆",
                "Show no mercy on the board! 🦁",
                "Nice move! 👍",
                "Hurry up, royal turn! ⏳",
                "Good game! 🤝"
            )
            val chosen = responses.random()
            val chat = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderName = randomAi.name,
                senderColor = randomAi.color,
                text = chosen,
                isEmoji = false,
                timestamp = System.currentTimeMillis()
            )
            _uiState.update {
                val newUnread = if (it.showChatDialog) 0 else it.unreadChatCount + 1
                it.copy(
                    chatMessages = it.chatMessages + chat,
                    unreadChatCount = newUnread,
                    lastChatMessage = chosen,
                    lastChatSenderName = randomAi.name
                )
            }
            delay(3500)
            _uiState.update { s ->
                if (s.lastChatMessage == chosen) s.copy(lastChatMessage = null, lastChatSenderName = null) else s
            }
        }
    }

    private fun simulateOpponentEmojiResponse() {
        val aiPlayers = _uiState.value.players.filter { it.isAi }
        if (aiPlayers.isEmpty()) return
        val randomAi = aiPlayers.random()

        viewModelScope.launch {
            delay(1800 + Random.nextLong(1000))
            val emojis = listOf("👑", "🎲", "🔥", "⚔️", "🦁")
            val chosen = emojis.random()
            _uiState.update { it.copy(activeFloatingEmoji = randomAi.name to chosen) }
            delay(2400)
            _uiState.update { it.copy(activeFloatingEmoji = null) }
        }
    }

    fun purchaseOrEquipShopItem(item: ShopItem) {
        soundAndHaptics.vibrateClick()
        val currentCoins = _uiState.value.userCoins
        if (!item.isOwned) {
            if (currentCoins >= item.priceCoins) {
                _uiState.update {
                    val remaining = it.userCoins - item.priceCoins
                    profilePreferences.setUserCoins(remaining)
                    it.copy(
                        userCoins = remaining,
                        bannerNotice = "Purchased ${item.name} for ${item.priceCoins} Coins!"
                    )
                }
            } else {
                _uiState.update { it.copy(bannerNotice = "Not enough Royal Coins!") }
                return
            }
        }

        // Equip
        _uiState.update { s ->
            when (item.category) {
                "Pawns" -> s.copy(equippedPawnSkin = item.id, bannerNotice = "Equipped ${item.name}!")
                "Dice" -> s.copy(equippedDiceSkin = item.id, bannerNotice = "Equipped ${item.name}!")
                "Boards" -> s.copy(equippedBoardSkin = item.id, bannerNotice = "Equipped ${item.name}!")
                else -> s
            }
        }
    }

    // --- Regal Board Theme Selector ---

    fun selectBoardTheme(themeId: String) {
        soundAndHaptics.vibrateClick()
        profilePreferences.setBoardTheme(themeId)
        _uiState.update {
            it.copy(
                equippedBoardTheme = themeId,
                bannerNotice = "Regal board theme updated to ${themeId.replace('_', ' ').replaceFirstChar { c -> c.uppercase() }}!"
            )
        }
        viewModelScope.launch {
            val (ok, snap) = inventoryRepository.equipItem(itemId = themeId)
            if (ok) applyInventorySnapshot(snap)
        }
    }

    // --- Celebrations & Particle Effects ---

    fun triggerCelebration(type: CelebrationType) {
        _uiState.update { it.copy(activeCelebration = type) }
    }

    fun clearCelebration() {
        _uiState.update { it.copy(activeCelebration = null) }
    }

    // --- Audio & Atmosphere Control ---

    fun toggleMusicPlayerDialog(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showMusicPlayerDialog = show) }
    }

    fun toggleMusicPlayPause() {
        soundAndHaptics.vibrateClick()
        soundAndHaptics.soundManager.togglePlayPause()
        _uiState.update { it.copy(isMusicPlaying = soundAndHaptics.soundManager.isPlaying()) }
    }

    fun cycleMusicRepeatMode() {
        soundAndHaptics.vibrateClick()
        val next = soundAndHaptics.soundManager.cycleRepeatMode()
        _uiState.update { it.copy(musicRepeatMode = next) }
    }

    fun toggleMusicShuffle() {
        soundAndHaptics.vibrateClick()
        val next = soundAndHaptics.soundManager.toggleShuffle()
        _uiState.update { it.copy(isMusicShuffleEnabled = next) }
    }

    fun toggleFavoriteSong(songId: String) {
        soundAndHaptics.vibrateClick()
        soundAndHaptics.soundManager.toggleSongFavorite(songId)
        _uiState.update { it.copy(favoriteSongIds = profilePreferences.getFavoriteSongIds()) }
    }

    fun toggleBgm(enabled: Boolean) {
        profilePreferences.setBgmEnabled(enabled)
        soundAndHaptics.soundManager.setBgmEnabled(enabled)
        _uiState.update { it.copy(isBgmEnabled = enabled) }
    }

    fun playNextBgmTrack() {
        soundAndHaptics.vibrateClick()
        soundAndHaptics.soundManager.playNextTrack()
        _uiState.update {
            it.copy(
                isMusicPlaying = soundAndHaptics.soundManager.isPlaying(),
                currentBgmTrack = soundAndHaptics.soundManager.getCurrentTrack(),
                currentBgmTrackIndex = soundAndHaptics.soundManager.currentTrackIndex
            )
        }
    }

    fun playPreviousBgmTrack() {
        soundAndHaptics.vibrateClick()
        soundAndHaptics.soundManager.playPreviousTrack()
        _uiState.update {
            it.copy(
                isMusicPlaying = soundAndHaptics.soundManager.isPlaying(),
                currentBgmTrack = soundAndHaptics.soundManager.getCurrentTrack(),
                currentBgmTrackIndex = soundAndHaptics.soundManager.currentTrackIndex
            )
        }
    }

    fun selectBgmTrack(index: Int) {
        soundAndHaptics.vibrateClick()
        soundAndHaptics.soundManager.selectTrack(index)
        _uiState.update {
            it.copy(
                isBgmEnabled = true,
                isMusicPlaying = soundAndHaptics.soundManager.isPlaying(),
                currentBgmTrack = soundAndHaptics.soundManager.getCurrentTrack(),
                currentBgmTrackIndex = index
            )
        }
    }

    /**
     * Directly plays any BgmTrack (supporting live online CDN streaming and local audio)
     * and sets the active dynamic playlist context.
     */
    fun playTrackDirect(track: com.example.model.BgmTrack, playlist: List<com.example.model.BgmTrack> = emptyList()) {
        soundAndHaptics.vibrateClick()
        soundAndHaptics.playTrack(track, playlist)
        recordTrackPlayed(track)
        _uiState.update {
            it.copy(
                isBgmEnabled = true,
                isMusicPlaying = true,
                currentBgmTrack = track,
                currentBgmTrackIndex = if (playlist.isNotEmpty()) playlist.indexOfFirst { p -> p.id == track.id }.coerceAtLeast(0) else 0
            )
        }
    }

    /**
     * Seeks playback position to specific millisecond timestamp.
     */
    fun seekMusicPosition(positionMs: Int) {
        soundAndHaptics.seekTo(positionMs)
        _uiState.update { it.copy(playbackPositionMs = positionMs) }
    }

    /**
     * Search Indian songs online by title, singer, album, or movie with live debounce.
     */
    fun searchOnlineMusic(query: String) {
        _uiState.update { it.copy(onlineSearchQuery = query) }
        musicSearchJob?.cancel()
        if (query.trim().isEmpty()) {
            _uiState.update { it.copy(onlineSearchResults = emptyList(), isSearchingMusic = false) }
            return
        }
        musicSearchJob = viewModelScope.launch {
            delay(350L) // Debounce typing
            _uiState.update { it.copy(isSearchingMusic = true) }
            val result = indianMusicRepository.searchSongs(query)
            val songs = result.getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    onlineSearchResults = songs,
                    isSearchingMusic = false
                )
            }
        }
    }

    fun clearOnlineMusicSearch() {
        musicSearchJob?.cancel()
        _uiState.update {
            it.copy(
                onlineSearchQuery = "",
                onlineSearchResults = emptyList(),
                isSearchingMusic = false
            )
        }
    }

    /**
     * Selects and loads an Indian music genre / language category.
     */
    fun selectMusicCategory(category: com.example.model.MusicCategory) {
        _uiState.update { it.copy(selectedMusicCategory = category) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategoryMusic = true) }
            val result = indianMusicRepository.getCategorySongs(category)
            val tracks = result.getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    categoryTracks = tracks,
                    isLoadingCategoryMusic = false
                )
            }
        }
    }

    /**
     * Creates a custom user playlist and persists it.
     */
    fun createUserPlaylist(name: String) {
        if (name.trim().isEmpty()) return
        soundAndHaptics.vibrateClick()
        val newPl = com.example.model.UserPlaylist(
            id = "user_pl_${System.currentTimeMillis()}",
            name = name.trim(),
            trackIds = emptyList(),
            iconEmoji = "🎵"
        )
        val updated = _uiState.value.userPlaylists + newPl
        _uiState.update { it.copy(userPlaylists = updated) }
        saveUserPlaylists(updated)
    }

    fun addTrackToUserPlaylist(playlistId: String, track: com.example.model.BgmTrack) {
        soundAndHaptics.vibrateClick()
        val currentPlaylists = _uiState.value.userPlaylists.map { pl ->
            if (pl.id == playlistId) {
                if (!pl.trackIds.contains(track.id)) {
                    pl.copy(trackIds = pl.trackIds + track.id)
                } else pl
            } else pl
        }
        _uiState.update { it.copy(userPlaylists = currentPlaylists) }
        saveUserPlaylists(currentPlaylists)
        saveSingleTrackRecord(track)
    }

    fun removeTrackFromUserPlaylist(playlistId: String, trackId: String) {
        soundAndHaptics.vibrateClick()
        val currentPlaylists = _uiState.value.userPlaylists.map { pl ->
            if (pl.id == playlistId) {
                pl.copy(trackIds = pl.trackIds.filter { it != trackId })
            } else pl
        }
        _uiState.update { it.copy(userPlaylists = currentPlaylists) }
        saveUserPlaylists(currentPlaylists)
    }

    fun toggleFavoriteTrack(track: com.example.model.BgmTrack) {
        soundAndHaptics.vibrateClick()
        val isNowFav = profilePreferences.toggleSongFavorite(track.id)
        val currentFavIds = profilePreferences.getFavoriteSongIds()
        val currentFavTracks = _uiState.value.favoriteTracks.toMutableList()
        if (isNowFav) {
            if (!currentFavTracks.any { it.id == track.id }) {
                currentFavTracks.add(0, track)
            }
        } else {
            currentFavTracks.removeAll { it.id == track.id }
        }
        _uiState.update {
            it.copy(
                favoriteSongIds = currentFavIds,
                favoriteTracks = currentFavTracks
            )
        }
        saveFavoriteTracksList(currentFavTracks)
    }

    /**
     * Restores default royal and Indian tracks into active playback.
     */
    fun restoreDefaultPlaylist() {
        soundAndHaptics.vibrateClick()
        soundAndHaptics.soundManager.setBgmEnabled(true)
        if (com.example.model.BGM_PLAYLIST.isNotEmpty()) {
            soundAndHaptics.playTrack(com.example.model.BGM_PLAYLIST[0], com.example.model.BGM_PLAYLIST)
            recordTrackPlayed(com.example.model.BGM_PLAYLIST[0])
        }
    }

    private fun recordTrackPlayed(track: com.example.model.BgmTrack) {
        val currentRecent = _uiState.value.recentlyPlayedTracks.toMutableList()
        currentRecent.removeAll { it.id == track.id }
        currentRecent.add(0, track)
        val trimmed = currentRecent.take(30)
        _uiState.update { it.copy(recentlyPlayedTracks = trimmed) }
        saveRecentlyPlayedList(trimmed)
    }

    private fun loadSavedMusicData() {
        try {
            // Load custom playlists
            val plRaw = profilePreferences.getCustomPlaylistsRaw()
            if (plRaw.isNotEmpty()) {
                val array = org.json.JSONArray(plRaw)
                val list = mutableListOf<com.example.model.UserPlaylist>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id")
                    val name = obj.optString("name")
                    val emoji = obj.optString("emoji", "🎵")
                    val idsArr = obj.optJSONArray("trackIds") ?: org.json.JSONArray()
                    val tIds = mutableListOf<String>()
                    for (j in 0 until idsArr.length()) {
                        tIds.add(idsArr.getString(j))
                    }
                    list.add(com.example.model.UserPlaylist(id = id, name = name, trackIds = tIds, iconEmoji = emoji))
                }
                _uiState.update { it.copy(userPlaylists = list) }
            }

            // Load recently played
            val recentRaw = profilePreferences.getRecentlyPlayedRaw()
            if (recentRaw.isNotEmpty()) {
                val array = org.json.JSONArray(recentRaw)
                val list = mutableListOf<com.example.model.BgmTrack>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val t = parseTrackFromJson(obj)
                    if (t != null) list.add(t)
                }
                _uiState.update { it.copy(recentlyPlayedTracks = list) }
            }

            // Load favorite tracks
            val favRaw = profilePreferences.getSavedFavoriteTracksRaw()
            if (favRaw.isNotEmpty()) {
                val array = org.json.JSONArray(favRaw)
                val list = mutableListOf<com.example.model.BgmTrack>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val t = parseTrackFromJson(obj)
                    if (t != null) list.add(t)
                }
                _uiState.update { it.copy(favoriteTracks = list) }
            }
        } catch (e: Exception) {
            android.util.Log.e("LudoViewModel", "Error loading saved music data: ${e.message}")
        }
    }

    private fun saveUserPlaylists(list: List<com.example.model.UserPlaylist>) {
        try {
            val array = org.json.JSONArray()
            for (pl in list) {
                val obj = org.json.JSONObject()
                obj.put("id", pl.id)
                obj.put("name", pl.name)
                obj.put("emoji", pl.iconEmoji)
                val idsArr = org.json.JSONArray()
                for (id in pl.trackIds) {
                    idsArr.put(id)
                }
                obj.put("trackIds", idsArr)
                array.put(obj)
            }
            profilePreferences.setCustomPlaylistsRaw(array.toString())
        } catch (_: Exception) {}
    }

    private fun saveRecentlyPlayedList(list: List<com.example.model.BgmTrack>) {
        try {
            val array = org.json.JSONArray()
            for (t in list) {
                array.put(trackToJson(t))
            }
            profilePreferences.setRecentlyPlayedRaw(array.toString())
        } catch (_: Exception) {}
    }

    private fun saveFavoriteTracksList(list: List<com.example.model.BgmTrack>) {
        try {
            val array = org.json.JSONArray()
            for (t in list) {
                array.put(trackToJson(t))
            }
            profilePreferences.setSavedFavoriteTracksRaw(array.toString())
        } catch (_: Exception) {}
    }

    private fun saveSingleTrackRecord(track: com.example.model.BgmTrack) {
        val currentFav = _uiState.value.favoriteTracks
        if (!currentFav.any { it.id == track.id }) {
            val updated = currentFav + track
            _uiState.update { it.copy(favoriteTracks = updated) }
            saveFavoriteTracksList(updated)
        }
    }

    private fun trackToJson(track: com.example.model.BgmTrack): org.json.JSONObject {
        val obj = org.json.JSONObject()
        obj.put("id", track.id)
        obj.put("title", track.title)
        obj.put("singer", track.singer)
        obj.put("albumName", track.albumName)
        obj.put("year", track.year)
        obj.put("durationLabel", track.durationLabel)
        obj.put("durationSeconds", track.durationSeconds)
        obj.put("albumArtUrl", track.albumArtUrl ?: "")
        obj.put("streamUrl", track.streamUrl ?: "")
        obj.put("category", track.category.name)
        obj.put("iconEmoji", track.iconEmoji)
        return obj
    }

    private fun parseTrackFromJson(obj: org.json.JSONObject): com.example.model.BgmTrack? {
        return try {
            val id = obj.optString("id")
            val title = obj.optString("title")
            val singer = obj.optString("singer")
            val albumName = obj.optString("albumName")
            val year = obj.optString("year")
            val durationLabel = obj.optString("durationLabel", "3:30")
            val durationSeconds = obj.optInt("durationSeconds", 210)
            val albumArtUrl = obj.optString("albumArtUrl").ifEmpty { null }
            val streamUrl = obj.optString("streamUrl").ifEmpty { null }
            val catName = obj.optString("category", "TRENDING")
            val cat = try { com.example.model.MusicCategory.valueOf(catName) } catch (_: Exception) { com.example.model.MusicCategory.TRENDING }
            val emoji = obj.optString("iconEmoji", "🎵")
            com.example.model.BgmTrack(
                id = id,
                title = title,
                singer = singer,
                albumName = albumName,
                year = year,
                durationLabel = durationLabel,
                durationSeconds = durationSeconds,
                albumArtUrl = albumArtUrl,
                streamUrl = streamUrl,
                category = cat,
                iconEmoji = emoji
            )
        } catch (_: Exception) { null }
    }

    fun setBgmVolume(volume: Float) {
        profilePreferences.setBgmVolume(volume)
        soundAndHaptics.soundManager.setBgmVolume(volume)
        _uiState.update { it.copy(bgmVolume = volume) }
    }

    fun toggleSfx(enabled: Boolean) {
        profilePreferences.setSfxEnabled(enabled)
        soundAndHaptics.soundEffectManager.setSfxEnabled(enabled)
        soundAndHaptics.soundManager.setSfxEnabled(enabled)
        _uiState.update { it.copy(isSfxEnabled = enabled) }
    }

    fun setSfxVolume(volume: Float) {
        profilePreferences.setSfxVolume(volume)
        soundAndHaptics.soundEffectManager.setSfxVolume(volume)
        soundAndHaptics.soundManager.setSfxVolume(volume)
        _uiState.update { it.copy(sfxVolume = volume) }
    }

    fun toggleHaptics(enabled: Boolean) {
        profilePreferences.setHapticsEnabled(enabled)
        soundAndHaptics.soundEffectManager.setHapticsEnabled(enabled)
        soundAndHaptics.soundManager.setHapticsEnabled(enabled)
        _uiState.update { it.copy(isHapticsEnabled = enabled) }
    }

    fun setSfxPreset(preset: String) {
        soundAndHaptics.setSfxPreset(preset)
        _uiState.update { it.copy(sfxPreset = preset) }
    }

    fun toggleSoundSettings(show: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showSoundSettingsDialog = show) }
    }

    // ==========================================
    // MG LUDO CUSTOM WALLPAPER SYSTEM CONTROLLERS
    // ==========================================

    fun loadWallpaperData() {
        val map = mutableMapOf<String, String>()
        com.example.model.WallpaperTarget.entries.forEach { target ->
            map[target.id] = profilePreferences.getWallpaperForTarget(target.id, target.defaultWallpaperId)
        }
        val uploads = profilePreferences.getCustomUploadedWallpapers()
        val favorites = profilePreferences.getFavoriteWallpaperIds()
        val recents = profilePreferences.getRecentlyUsedWallpapers()

        _uiState.update {
            it.copy(
                activeWallpapers = map,
                customUploadedWallpapers = uploads,
                favoriteWallpaperIds = favorites,
                recentWallpaperIds = recents
            )
        }
    }

    fun toggleWallpaperCustomizer(
        show: Boolean,
        target: com.example.model.WallpaperTarget = com.example.model.WallpaperTarget.HOME
    ) {
        soundAndHaptics.vibrateClick()
        _uiState.update {
            it.copy(
                showWallpaperCustomizerDialog = show,
                activeWallpaperTarget = target,
                showSoundSettingsDialog = if (show) false else it.showSoundSettingsDialog
            )
        }
    }

    fun applyWallpaper(target: com.example.model.WallpaperTarget, wallpaperIdOrPath: String) {
        profilePreferences.setWallpaperForTarget(target.id, wallpaperIdOrPath)
        loadWallpaperData()
        soundAndHaptics.playButtonClick()
    }

    fun uploadWallpaperFromUri(sourceUri: android.net.Uri, target: com.example.model.WallpaperTarget) {
        val savedPath = profilePreferences.saveWallpaperImageLocally(sourceUri)
        if (savedPath != null) {
            applyWallpaper(target, savedPath)
        }
    }

    fun deleteCustomWallpaper(filePath: String) {
        profilePreferences.deleteCustomUploadedWallpaper(filePath)
        loadWallpaperData()
        soundAndHaptics.vibrateClick()
    }

    fun toggleWallpaperFavorite(wallpaperIdOrPath: String) {
        profilePreferences.toggleFavoriteWallpaper(wallpaperIdOrPath)
        loadWallpaperData()
        soundAndHaptics.playButtonClick()
    }

    fun resetWallpaperForTarget(target: com.example.model.WallpaperTarget) {
        profilePreferences.resetWallpaperForTarget(target.id, target.defaultWallpaperId)
        loadWallpaperData()
        soundAndHaptics.playButtonClick()
    }

    fun resetAllWallpapers() {
        profilePreferences.resetAllWallpapers()
        loadWallpaperData()
        soundAndHaptics.playButtonClick()
    }

    fun toggleVoiceChat() {
        soundAndHaptics.vibrateClick()
        val newVoice = !_uiState.value.isVoiceActive
        if (newVoice) {
            voiceChatManager.startVoiceChat()
        } else {
            voiceChatManager.stopVoiceChat()
        }
        _uiState.update { 
            it.copy(
                isVoiceActive = newVoice,
                bannerNotice = if (newVoice) "🎙️ Voice Room Connected! Mic Live" else "🎙️ Voice Room Disconnected"
            )
        }
    }

    fun toggleMic() {
        soundAndHaptics.vibrateClick()
        voiceChatManager.toggleMic()
        val isMuted = voiceChatManager.isMicMuted.value
        _uiState.update {
            it.copy(
                isMicMuted = isMuted,
                bannerNotice = if (isMuted) "🔇 Microphone Muted" else "🎙️ Microphone Live - Speak freely!"
            )
        }
    }

    fun toggleSpeaker() {
        soundAndHaptics.vibrateClick()
        voiceChatManager.toggleSpeaker()
        val isMuted = voiceChatManager.isSpeakerMuted.value
        _uiState.update {
            it.copy(
                isSpeakerMuted = isMuted,
                bannerNotice = if (isMuted) "🔈 Room Audio Muted" else "🔊 Room Audio Unmuted"
            )
        }
    }

    fun onRecordPermissionGranted() {
        voiceChatManager.startAudioCaptureIfPermitted()
    }

    fun activatePowerUp(type: String) {
        soundAndHaptics.vibrateClick()
        val currentGems = _uiState.value.userGems
        val cost = 20
        if (currentGems >= cost) {
            _uiState.update {
                it.copy(
                    activePowerUp = type,
                    userGems = it.userGems - cost,
                    bannerNotice = "⚡ $type Power-up active! Next roll guaranteed!"
                )
            }
        } else {
            // Free fallback so gameplay is always fun
            _uiState.update {
                it.copy(
                    activePowerUp = type,
                    bannerNotice = "⚡ $type Power-up active! Next roll guaranteed!"
                )
            }
        }
    }

    fun closeResultsDialog() {
        _uiState.update { it.copy(showResultsDialog = false) }
    }

    fun dismissSpecialOffer() {
        _uiState.update { it.copy(showSpecialOfferDialog = false) }
    }

    fun openSpecialOffer() {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showSpecialOfferDialog = true) }
    }

    fun claimSpecialOfferDeal(coins: Long = 100000L, gems: Int = 250) {
        soundAndHaptics.vibrateVictory()
        val newCoins = _uiState.value.userCoins + coins
        val newGems = _uiState.value.userGems + gems
        profilePreferences.setUserCoins(newCoins)
        profilePreferences.setUserGems(newGems)
        _uiState.update {
            it.copy(
                userCoins = newCoins,
                userGems = newGems,
                showSpecialOfferDialog = false,
                bannerNotice = "🎉 5-Game Milestone Offer Claimed! +🪙 ${com.example.ui.components.formatCompactCoins(coins)} & +💎 $gems Gems!"
            )
        }
        showPopUpNotice(
            title = "🎉 Special Offer Claimed!",
            message = "Congratulations on playing 5 online games! You received +🪙 ${com.example.ui.components.formatCompactCoins(coins)} and +💎 $gems Gems!",
            icon = "🔥",
            type = "REWARD"
        )
    }

    fun restartMatch() {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(showResultsDialog = false, winner = null) }
        startNewGame(
            mode = _uiState.value.gameMode,
            playerCount = _uiState.value.players.size,
            chosenColor = _uiState.value.userChosenColor,
            wager = _uiState.value.currentWager
        )
    }

    fun leaveCurrentGame() {
        soundAndHaptics.vibrateClick()
        aiTurnJob?.cancel()
        turnTimerJob?.cancel()
        moveJob?.cancel()
        isMoveInProgress = false
        processedMoveIds.clear()
        profilePreferences.saveActiveMatchSnapshotJson(null)
        voiceChatManager.stopVoiceChat()
        soundAndHaptics.setGameActive(false)
        val wager = _uiState.value.currentWager

        // User already lost entry coins upon starting match
        profilePreferences.recordGameFinished(isWin = false, captures = 0)

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.HOME,
                winner = null,
                showResultsDialog = false,
                isRolling = false,
                consecutiveSixes = 0,
                bannerNotice = "You left the match. Entry fee of 🪙 ${com.example.ui.components.formatCompactCoins(wager)} was forfeited."
            )
        }
    }

    // Administration & Royal Chamber
    fun toggleAdminDashboard(show: Boolean) {
        _uiState.update { it.copy(showAdminDashboard = show) }
    }

    fun verifyAdminPin(pin: String): Boolean {
        val isValid = pin == "1234"
        if (isValid) {
            _uiState.update { it.copy(adminPinVerified = true) }
            soundAndHaptics.playCoinReward()
        } else {
            soundAndHaptics.playButtonClick()
        }
        return isValid
    }

    fun adminGrantCoins(amount: Long) {
        val newCoins = _uiState.value.userCoins + amount
        profilePreferences.setUserCoins(newCoins)
        _uiState.update { it.copy(userCoins = newCoins, bannerNotice = "Granted +$amount Coins!") }
        soundAndHaptics.playCoinReward()
    }

    fun adminGrantGems(amount: Int) {
        val newGems = _uiState.value.userGems + amount
        profilePreferences.setUserGems(newGems)
        _uiState.update { it.copy(userGems = newGems, bannerNotice = "Granted +$amount Gems!") }
        soundAndHaptics.playCoinReward()
    }

    fun adminResetDailyRewards() {
        _uiState.update { it.copy(lastSpinTimestampMillis = 0L, bannerNotice = "Daily Login & Spin Timers Reset!") }
        soundAndHaptics.playSafeTile()
    }

    fun adminResetDailyMissions() {
        _uiState.update { it.copy(dailyMissions = com.example.model.DEFAULT_DAILY_MISSIONS, bannerNotice = "Daily Missions Refreshed!") }
        soundAndHaptics.playSafeTile()
    }

    fun adminClearReports() {
        _uiState.update { it.copy(playerReports = emptyList(), bannerNotice = "All reports cleared!") }
    }

    // Interactive Tutorial
    fun toggleTutorialDialog(show: Boolean) {
        _uiState.update { it.copy(showTutorialDialog = show) }
    }

    // Player Moderation & Reports
    fun openReportPlayerDialog(targetPlayer: Player) {
        _uiState.update { it.copy(showReportDialog = true, reportedTargetPlayer = targetPlayer) }
    }

    fun dismissReportPlayerDialog() {
        _uiState.update { it.copy(showReportDialog = false, reportedTargetPlayer = null) }
    }

    fun submitPlayerReport(reason: String, isBlocked: Boolean) {
        val target = _uiState.value.reportedTargetPlayer
        val targetName = target?.name ?: "Opponent"
        val report = PlayerReport(
            id = UUID.randomUUID().toString(),
            reportedPlayerName = targetName,
            reason = reason,
            isBlocked = isBlocked
        )
        _uiState.update { s ->
            s.copy(
                playerReports = s.playerReports + report,
                showReportDialog = false,
                reportedTargetPlayer = null,
                bannerNotice = "Report submitted against $targetName. Ticket logged."
            )
        }
        showPopUpNotice(
            title = "🛡️ Report Submitted",
            message = "Thank you for keeping MG Ludo safe. Your report against $targetName for '$reason' has been logged. ${if (isBlocked) "Player blocked from future matches." else ""}",
            icon = "🛡️",
            type = "SUCCESS"
        )
    }

    // Armory & Customizer (Dice, Tokens, Sound)
    fun toggleCustomizerShop(show: Boolean) {
        _uiState.update { it.copy(showCustomizerShopDialog = show) }
    }

    fun equipSoundTheme(themeId: String) {
        profilePreferences.setSfxPreset(themeId)
        _uiState.update { it.copy(sfxPreset = themeId, bannerNotice = "Selected $themeId sound theme!") }
        soundAndHaptics.playButtonClick()
    }

    // Practice Mode Undo
    private var practiceLastState: List<Player>? = null

    fun undoLastPracticeMove() {
        val last = practiceLastState
        if (_uiState.value.gameMode == GameMode.PRACTICE && last != null) {
            _uiState.update {
                it.copy(
                    players = last,
                    turnPhase = TurnPhase.ROLL_DICE,
                    canUndoPracticeMove = false,
                    bannerNotice = "↩️ Last move undone! Roll again."
                )
            }
            soundAndHaptics.playButtonClick()
        }
    }

    // ========================================================
    // ADVANCED INVENTORY SYSTEM ENGINE & GAMEPLAY INTEGRATION
    // ========================================================

    fun applyInventorySnapshot(snapshot: PlayerInventory) {
        val currentPlayerId = profilePreferences.getCurrentPlayerId()
        if (snapshot.playerId != currentPlayerId) return

        val progressMap = snapshot.eventProgress.associateBy { it.eventOrItemId }
        val ownedIds = snapshot.ownedItemIds
        val equipped = snapshot.equippedItems

        profilePreferences.setUserCoins(snapshot.currency.goldCoins)
        profilePreferences.setUserGems(snapshot.currency.cashNotes)
        profilePreferences.setEquippedDiceSkin(equipped.diceId)
        profilePreferences.setEquippedFrameId(equipped.frameId)
        profilePreferences.setBoardTheme(equipped.boardId)
        profilePreferences.setEquippedTokenSkin(equipped.pawnId)
        profilePreferences.setPawnTrailEnabled(equipped.pawnTrailEnabled)

        _uiState.update { current ->
            current.copy(
                userCoins = snapshot.currency.goldCoins,
                userGems = snapshot.currency.cashNotes,
                equippedDiceSkin = equipped.diceId,
                equippedFrameId = equipped.frameId,
                equippedBoardTheme = equipped.boardId,
                equippedPawnSkin = equipped.pawnId,
                isPawnTrailEnabled = equipped.pawnTrailEnabled,
                inventoryUiState = current.inventoryUiState.copy(
                    playerId = snapshot.playerId,
                    ownedItems = snapshot.ownedItems,
                    ownedItemIds = ownedIds,
                    equippedDiceId = equipped.diceId,
                    equippedFrameId = equipped.frameId,
                    equippedBoardId = equipped.boardId,
                    equippedPawnId = equipped.pawnId,
                    isPawnTrailEnabled = equipped.pawnTrailEnabled,
                    emojiDeckSlots = snapshot.emojiDeck.slots,
                    activePowerups = snapshot.activePowerups,
                    eventProgressMap = progressMap,
                    isLoading = false,
                    isProcessingTransaction = false
                )
            )
        }
    }

    fun selectInventoryCategory(category: InventoryCategory) {
        soundAndHaptics.vibrateClick()
        _uiState.update {
            it.copy(
                inventoryUiState = it.inventoryUiState.copy(
                    selectedCategory = category,
                    errorMessage = null
                )
            )
        }
    }

    fun updateInventorySearch(query: String) {
        _uiState.update {
            it.copy(inventoryUiState = it.inventoryUiState.copy(searchQuery = query))
        }
    }

    fun selectInventoryFilter(filter: InventoryFilter) {
        soundAndHaptics.vibrateClick()
        _uiState.update {
            it.copy(inventoryUiState = it.inventoryUiState.copy(selectedFilter = filter))
        }
    }

    suspend fun purchaseInventoryItemSync(itemId: String): PurchaseResult {
        _uiState.update {
            it.copy(inventoryUiState = it.inventoryUiState.copy(isProcessingTransaction = true, errorMessage = null))
        }
        val (result, snapshot) = inventoryRepository.purchaseItem(itemId = itemId)
        applyInventorySnapshot(snapshot)

        when (result) {
            is PurchaseResult.Success -> {
                soundAndHaptics.playCoinReward()
                if (!result.item.isConsumable &&
                    result.item.category in listOf(
                        InventoryCategory.DICE,
                        InventoryCategory.FRAME,
                        InventoryCategory.BOARD,
                        InventoryCategory.PAWN
                    )
                ) {
                    val (_, equippedSnap) = inventoryRepository.equipItem(itemId = result.item.id)
                    applyInventorySnapshot(equippedSnap)
                }
                _uiState.update {
                    it.copy(
                        inventoryUiState = it.inventoryUiState.copy(
                            lastSuccessItem = result.item,
                            lastSuccessMessage = "Unlocked ${result.item.name}!",
                            isProcessingTransaction = false
                        ),
                        bannerNotice = "🎉 Purchased ${result.item.name}!"
                    )
                }
            }
            is PurchaseResult.AlreadyOwned -> {
                _uiState.update {
                    it.copy(
                        inventoryUiState = it.inventoryUiState.copy(
                            errorMessage = "${result.item.name} is already in your inventory.",
                            isProcessingTransaction = false
                        )
                    )
                }
            }
            is PurchaseResult.InsufficientBalance -> {
                val label = if (result.currencyType == CurrencyType.COINS) "Gold Coins" else "Cash"
                _uiState.update {
                    it.copy(
                        inventoryUiState = it.inventoryUiState.copy(
                            errorMessage = "Insufficient $label! Need ${result.required}, available ${result.available}.",
                            isProcessingTransaction = false
                        )
                    )
                }
            }
            is PurchaseResult.RequirementNotMet -> {
                _uiState.update {
                    it.copy(
                        inventoryUiState = it.inventoryUiState.copy(
                            errorMessage = result.message,
                            isProcessingTransaction = false
                        )
                    )
                }
            }
            is PurchaseResult.EventExpired -> {
                _uiState.update {
                    it.copy(
                        inventoryUiState = it.inventoryUiState.copy(
                            errorMessage = "Event has ended (${result.eventId}).",
                            isProcessingTransaction = false
                        )
                    )
                }
            }
            is PurchaseResult.NotFound, is PurchaseResult.Error -> {
                _uiState.update {
                    it.copy(
                        inventoryUiState = it.inventoryUiState.copy(
                            errorMessage = "Transaction failed. Please try again.",
                            isProcessingTransaction = false
                        )
                    )
                }
            }
        }
        return result
    }

    fun purchaseInventoryItem(itemId: String) {
        viewModelScope.launch {
            purchaseInventoryItemSync(itemId)
        }
    }

    fun equipInventoryItem(itemId: String) {
        soundAndHaptics.vibrateClick()
        viewModelScope.launch {
            val (ok, snapshot) = inventoryRepository.equipItem(itemId = itemId)
            applyInventorySnapshot(snapshot)
            if (ok) {
                val itemName = ItemCatalog.findById(itemId)?.name ?: itemId
                _uiState.update { it.copy(bannerNotice = "✅ Equipped $itemName!") }
            }
        }
    }

    fun unequipInventoryCategory(category: InventoryCategory) {
        soundAndHaptics.vibrateClick()
        viewModelScope.launch {
            val snapshot = inventoryRepository.unequipCategory(category = category)
            applyInventorySnapshot(snapshot)
            _uiState.update { it.copy(bannerNotice = "Reset ${category.title} to default.") }
        }
    }

    fun setPawnTrailEnabled(enabled: Boolean) {
        soundAndHaptics.vibrateClick()
        viewModelScope.launch {
            val snapshot = inventoryRepository.setPawnTrailEnabled(enabled = enabled)
            applyInventorySnapshot(snapshot)
            _uiState.update {
                it.copy(bannerNotice = if (enabled) "✨ Pawn Movement Trail ON" else "Pawn Movement Trail OFF")
            }
        }
    }

    fun assignEmojiToDeckSlot(slotIndex: Int, emojiItemId: String) {
        soundAndHaptics.vibrateClick()
        viewModelScope.launch {
            val (ok, snapshot) = inventoryRepository.assignEmojiToDeckSlot(slotIndex = slotIndex, emojiItemId = emojiItemId)
            applyInventorySnapshot(snapshot)
            if (ok) {
                val name = ItemCatalog.findById(emojiItemId)?.name ?: "Emoji"
                _uiState.update { it.copy(bannerNotice = "😀 Assigned $name to Slot #${slotIndex + 1}!") }
            } else {
                _uiState.update {
                    it.copy(inventoryUiState = it.inventoryUiState.copy(errorMessage = "Own this emoji first to equip it to your deck."))
                }
            }
        }
    }

    fun removeEmojiFromDeckSlot(slotIndex: Int) {
        soundAndHaptics.vibrateClick()
        viewModelScope.launch {
            val snapshot = inventoryRepository.removeEmojiFromDeckSlot(slotIndex = slotIndex)
            applyInventorySnapshot(snapshot)
        }
    }

    fun swapEmojiDeckSlots(fromSlot: Int, toSlot: Int) {
        soundAndHaptics.vibrateClick()
        viewModelScope.launch {
            val snapshot = inventoryRepository.swapEmojiDeckSlots(fromSlot = fromSlot, toSlot = toSlot)
            applyInventorySnapshot(snapshot)
        }
    }

    suspend fun activateInventoryPowerupSync(itemId: String): PowerupActivationResult {
        val (result, snapshot) = inventoryRepository.activatePowerup(itemId = itemId)
        applyInventorySnapshot(snapshot)
        when (result) {
            is PowerupActivationResult.Activated -> {
                soundAndHaptics.playCoinReward()
                if (result.item.powerupEffectType == "EXTRA_POWERUP") {
                    _uiState.update { it.copy(lightningCount = it.lightningCount + 2) }
                } else if (result.item.powerupEffectType == "SEASON_PASS") {
                    _uiState.update { it.copy(hasVoicePass = true, voicePassDaysRemaining = it.voicePassDaysRemaining + 7) }
                }
                _uiState.update {
                    it.copy(
                        inventoryUiState = it.inventoryUiState.copy(
                            lastSuccessItem = result.item,
                            lastSuccessMessage = "${result.item.name} Activated!",
                            errorMessage = null
                        ),
                        bannerNotice = "🚀 ${result.item.name} Activated!"
                    )
                }
            }
            is PowerupActivationResult.OnCooldown -> {
                _uiState.update {
                    it.copy(
                        inventoryUiState = it.inventoryUiState.copy(
                            errorMessage = "Powerup on cooldown (${result.remainingSeconds}s left)."
                        )
                    )
                }
            }
            is PowerupActivationResult.DailyLimitReached -> {
                _uiState.update {
                    it.copy(
                        inventoryUiState = it.inventoryUiState.copy(
                            errorMessage = "Daily activation limit (${result.limit}/day) reached."
                        )
                    )
                }
            }
            is PowerupActivationResult.NotOwned -> {
                _uiState.update {
                    it.copy(
                        inventoryUiState = it.inventoryUiState.copy(
                            errorMessage = "Purchase more to activate this powerup."
                        )
                    )
                }
            }
            is PowerupActivationResult.Error -> {
                _uiState.update {
                    it.copy(inventoryUiState = it.inventoryUiState.copy(errorMessage = result.message))
                }
            }
        }
        return result
    }

    fun activateInventoryPowerup(itemId: String) {
        soundAndHaptics.vibrateClick()
        viewModelScope.launch {
            activateInventoryPowerupSync(itemId)
        }
    }

    fun recordAdSkippedOrFailed(adTransactionId: String) {
        viewModelScope.launch {
            val (result, snapshot) = inventoryRepository.recordVerifiedAdCompletion(
                adTransactionId = adTransactionId,
                verified = false
            )
            applyInventorySnapshot(snapshot)
            if (result is AdVerificationResult.NotVerified) {
                _uiState.update {
                    it.copy(
                        bannerNotice = "Ad skipped — no coins or emoji progress awarded.",
                        inventoryUiState = it.inventoryUiState.copy(
                            errorMessage = "Watch the full ad to earn progress and rewards."
                        )
                    )
                }
            }
        }
    }

    fun recordEventActionProgress(itemId: String, delta: Int = 1) {
        viewModelScope.launch {
            val (_, snapshot) = inventoryRepository.incrementEventItemProgress(itemId = itemId, delta = delta)
            applyInventorySnapshot(snapshot)
        }
    }

    fun dismissInventoryPurchaseSuccess() {
        _uiState.update {
            it.copy(
                inventoryUiState = it.inventoryUiState.copy(
                    lastSuccessItem = null,
                    lastSuccessMessage = null
                )
            )
        }
    }

    fun clearInventoryError() {
        _uiState.update {
            it.copy(inventoryUiState = it.inventoryUiState.copy(errorMessage = null))
        }
    }

    suspend fun switchPlayerAccountSync(playerId: String, defaultCoins: Long = 25000L, defaultCash: Int = 120): PlayerInventory {
        profilePreferences.setCurrentPlayerId(playerId)
        val snapshot = inventoryRepository.ensureInitialized(
            playerId = playerId,
            initialCoins = defaultCoins,
            initialCash = defaultCash
        )
        applyInventorySnapshot(snapshot)
        val diceSnap = diceStoreRepository.ensureInitialized(playerId)
        applyDiceStoreSnapshot(diceSnap)
        return snapshot
    }

    // ========================================================
    // PREMIUM DICE STORE, PLAY BILLING & ADMIN ENGINE
    // ========================================================

    fun applyDiceStoreSnapshot(snapshot: DiceStoreSnapshot) {
        val currentPlayerId = profilePreferences.getCurrentPlayerId()
        if (snapshot.playerId != currentPlayerId) return

        profilePreferences.setUserCoins(snapshot.goldCoins)
        profilePreferences.setUserGems(snapshot.cashNotes)
        profilePreferences.setEquippedDiceSkin(snapshot.equippedDiceId)

        _uiState.update { current ->
            val mergedOwnedIds = current.inventoryUiState.ownedItemIds + snapshot.ownedDiceIds
            current.copy(
                userCoins = snapshot.goldCoins,
                userGems = snapshot.cashNotes,
                equippedDiceSkin = snapshot.equippedDiceId,
                inventoryUiState = current.inventoryUiState.copy(
                    ownedItemIds = mergedOwnedIds,
                    equippedDiceId = snapshot.equippedDiceId
                ),
                diceStoreUiState = current.diceStoreUiState.copy(
                    catalog = snapshot.catalog,
                    ownedDiceIds = snapshot.ownedDiceIds,
                    equippedDiceId = snapshot.equippedDiceId,
                    pendingBillingDiceIds = snapshot.pendingBillingDiceIds,
                    eventRewards = snapshot.eventRewards,
                    recentPurchaseHistory = snapshot.recentPurchaseHistory,
                    isProcessingPurchase = false
                )
            )
        }
    }

    fun selectDiceStoreGroup(groupKey: String) {
        soundAndHaptics.vibrateClick()
        _uiState.update {
            it.copy(diceStoreUiState = it.diceStoreUiState.copy(selectedGroup = groupKey))
        }
    }

    fun selectDiceStoreRarity(rarity: DiceRarity?) {
        soundAndHaptics.vibrateClick()
        _uiState.update {
            it.copy(diceStoreUiState = it.diceStoreUiState.copy(selectedRarity = rarity))
        }
    }

    fun updateDiceStoreSearch(query: String) {
        _uiState.update {
            it.copy(diceStoreUiState = it.diceStoreUiState.copy(searchQuery = query))
        }
    }

    fun openDiceDetail(diceId: String) {
        soundAndHaptics.vibrateClick()
        _uiState.update {
            it.copy(
                diceStoreUiState = it.diceStoreUiState.copy(
                    selectedDetailDiceId = diceId,
                    errorMessage = null,
                    insufficientCoinsDialogDice = null
                )
            )
        }
    }

    fun dismissDiceDetail() {
        _uiState.update {
            it.copy(
                diceStoreUiState = it.diceStoreUiState.copy(
                    selectedDetailDiceId = null,
                    insufficientCoinsDialogDice = null,
                    purchaseSuccessDice = null,
                    errorMessage = null
                )
            )
        }
    }

    fun clearDiceStoreMessages() {
        _uiState.update {
            it.copy(
                diceStoreUiState = it.diceStoreUiState.copy(
                    errorMessage = null,
                    statusBanner = null,
                    purchaseSuccessDice = null,
                    insufficientCoinsDialogDice = null
                )
            )
        }
    }

    fun previewDiceSoundAndEffect(dice: PremiumDiceItem) {
        soundAndHaptics.playDiceRoll(dice.benefits.soundEffect)
    }

    suspend fun purchasePremiumDiceWithCoinsSync(diceId: String): DiceCoinPurchaseResult {
        _uiState.update {
            it.copy(
                diceStoreUiState = it.diceStoreUiState.copy(
                    isProcessingPurchase = true,
                    errorMessage = null,
                    insufficientCoinsDialogDice = null
                )
            )
        }
        val (result, snap) = diceStoreRepository.purchaseDiceWithCoins(diceId = diceId)
        applyDiceStoreSnapshot(snap)
        val invSnap = inventoryRepository.getInventorySnapshot()
        applyInventorySnapshot(invSnap)

        when (result) {
            is DiceCoinPurchaseResult.Success -> {
                soundAndHaptics.playCoinReward()
                val (_, eqSnap) = diceStoreRepository.equipDice(diceId = result.dice.id)
                applyDiceStoreSnapshot(eqSnap)
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            purchaseSuccessDice = result.dice,
                            statusBanner = "Unlocked & Equipped ${result.dice.name}!",
                            isProcessingPurchase = false
                        ),
                        bannerNotice = "🎲 Unlocked ${result.dice.name}!"
                    )
                }
            }
            is DiceCoinPurchaseResult.InsufficientCoins -> {
                soundAndHaptics.playInvalidMove()
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            insufficientCoinsDialogDice = result.dice,
                            errorMessage = "Insufficient Coins! Need ${result.required}, you have ${result.available}.",
                            isProcessingPurchase = false
                        )
                    )
                }
            }
            is DiceCoinPurchaseResult.AlreadyOwned -> {
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            statusBanner = "${result.dice.name} is already in your collection!",
                            isProcessingPurchase = false
                        )
                    )
                }
            }
            is DiceCoinPurchaseResult.Unavailable -> {
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            errorMessage = result.reason,
                            isProcessingPurchase = false
                        )
                    )
                }
            }
            is DiceCoinPurchaseResult.Error -> {
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            errorMessage = result.message,
                            isProcessingPurchase = false
                        )
                    )
                }
            }
        }
        return result
    }

    fun purchasePremiumDiceWithCoins(diceId: String) {
        viewModelScope.launch {
            purchasePremiumDiceWithCoinsSync(diceId)
        }
    }

    fun launchGooglePlayDicePurchase(activity: android.app.Activity?, dice: PremiumDiceItem) {
        if (!dice.isPublished || !dice.isAvailable || !dice.canBuyWithInr || dice.inrPrice <= 0) {
            _uiState.update {
                it.copy(diceStoreUiState = it.diceStoreUiState.copy(errorMessage = "${dice.name} is not enabled for INR billing."))
            }
            return
        }
        _uiState.update {
            it.copy(
                diceStoreUiState = it.diceStoreUiState.copy(
                    isProcessingPurchase = true,
                    errorMessage = null,
                    statusBanner = "Connecting to Google Play Billing for ${dice.name} (₹${dice.inrPrice})..."
                )
            )
        }
        playBillingManager.launchDicePurchaseFlow(
            activity = activity,
            skuId = dice.skuId,
            diceId = dice.id,
            expectedInrAmount = dice.inrPrice,
            obfuscatedPlayerId = profilePreferences.getCurrentPlayerId()
        )
    }

    fun requestPlayBillingRestore() {
        soundAndHaptics.vibrateClick()
        playBillingManager.queryExistingPurchases()
        viewModelScope.launch {
            val (count, snap) = diceStoreRepository.restorePurchases(playPurchases = emptyList())
            applyDiceStoreSnapshot(snap)
            _uiState.update {
                it.copy(
                    diceStoreUiState = it.diceStoreUiState.copy(
                        statusBanner = if (count > 0) "Restored $count verified dice purchase(s)!" else "Verified purchases synced with your account."
                    )
                )
            }
        }
    }

    suspend fun processVerifiedBillingPayloadSync(payload: BillingVerificationPayload): DiceBillingResult {
        val (result, snap) = diceStoreRepository.verifyAndProcessBillingPurchase(payload = payload)
        applyDiceStoreSnapshot(snap)
        val invSnap = inventoryRepository.getInventorySnapshot()
        applyInventorySnapshot(invSnap)

        when (result) {
            is DiceBillingResult.VerifiedGranted -> {
                playBillingManager.acknowledgePurchase(result.purchaseToken)
                soundAndHaptics.playVictory()
                val (_, eqSnap) = diceStoreRepository.equipDice(diceId = result.dice.id)
                applyDiceStoreSnapshot(eqSnap)
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            purchaseSuccessDice = result.dice,
                            statusBanner = "Google Play verified! ${result.dice.name} unlocked.",
                            isProcessingPurchase = false
                        )
                    )
                }
            }
            is DiceBillingResult.Pending -> {
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            statusBanner = "Payment pending verification for ${result.dice.name}. Dice will unlock once Play confirms.",
                            isProcessingPurchase = false
                        )
                    )
                }
            }
            is DiceBillingResult.DuplicateTokenRejected -> {
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            errorMessage = "Duplicate purchase token rejected.",
                            isProcessingPurchase = false
                        )
                    )
                }
            }
            is DiceBillingResult.RefundedRevoked -> {
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            statusBanner = "Refund processed: dice entitlement revoked.",
                            isProcessingPurchase = false
                        )
                    )
                }
            }
            is DiceBillingResult.Cancelled -> {
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            statusBanner = "Google Play checkout cancelled. No charge made.",
                            isProcessingPurchase = false
                        )
                    )
                }
            }
            is DiceBillingResult.Failed -> {
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            errorMessage = result.reason,
                            isProcessingPurchase = false
                        )
                    )
                }
            }
        }
        return result
    }

    private fun handlePlayBillingEvent(event: com.example.billing.BillingClientEvent) {
        viewModelScope.launch {
            when (event) {
                is com.example.billing.BillingClientEvent.PurchaseUpdated -> {
                    event.payloads.forEach { payload ->
                        processVerifiedBillingPayloadSync(payload)
                    }
                }
                is com.example.billing.BillingClientEvent.RestoredPurchases -> {
                    val (count, snap) = diceStoreRepository.restorePurchases(playPurchases = event.payloads)
                    applyDiceStoreSnapshot(snap)
                    if (count > 0) {
                        _uiState.update {
                            it.copy(diceStoreUiState = it.diceStoreUiState.copy(statusBanner = "Restored $count Google Play dice purchase(s)."))
                        }
                    }
                }
                is com.example.billing.BillingClientEvent.UserCancelled -> {
                    _uiState.update {
                        it.copy(
                            diceStoreUiState = it.diceStoreUiState.copy(
                                isProcessingPurchase = false,
                                statusBanner = event.message
                            )
                        )
                    }
                }
                is com.example.billing.BillingClientEvent.BillingError -> {
                    _uiState.update {
                        it.copy(
                            diceStoreUiState = it.diceStoreUiState.copy(
                                isProcessingPurchase = false,
                                errorMessage = event.debugMessage.ifBlank { "Google Play Billing error (code ${event.responseCode})." }
                            )
                        )
                    }
                }
                is com.example.billing.BillingClientEvent.ServiceUnavailable -> {
                    _uiState.update {
                        it.copy(
                            diceStoreUiState = it.diceStoreUiState.copy(
                                isProcessingPurchase = false,
                                errorMessage = event.reason
                            )
                        )
                    }
                }
                is com.example.billing.BillingClientEvent.ProductDetailsLoaded -> Unit
            }
        }
    }

    fun equipPremiumDice(diceId: String) {
        soundAndHaptics.vibrateClick()
        viewModelScope.launch {
            val (ok, snap) = diceStoreRepository.equipDice(diceId = diceId)
            applyDiceStoreSnapshot(snap)
            val (_, invSnap) = inventoryRepository.equipItem(itemId = diceId)
            applyInventorySnapshot(invSnap)
            if (ok) {
                val dice = snap.catalog.firstOrNull { it.id == diceId }
                if (dice != null) {
                    soundAndHaptics.playDiceRoll(dice.benefits.soundEffect)
                }
                _uiState.update {
                    it.copy(
                        bannerNotice = "🎲 Equipped ${dice?.name ?: diceId}!",
                        diceStoreUiState = it.diceStoreUiState.copy(statusBanner = "Equipped ${dice?.name ?: diceId} for active matches!")
                    )
                }
            }
        }
    }

    fun unequipPremiumDice() {
        soundAndHaptics.vibrateClick()
        viewModelScope.launch {
            val snap = diceStoreRepository.unequipDice()
            applyDiceStoreSnapshot(snap)
            val invSnap = inventoryRepository.unequipCategory(category = InventoryCategory.DICE)
            applyInventorySnapshot(invSnap)
            _uiState.update {
                it.copy(
                    bannerNotice = "Reverted to Starter Imperial Gold Dice",
                    diceStoreUiState = it.diceStoreUiState.copy(statusBanner = "Reverted to Imperial Gold Dice.")
                )
            }
        }
    }

    fun claimDiceChallengeReward(diceId: String) {
        soundAndHaptics.vibrateClick()
        viewModelScope.launch {
            val (ok, snap) = diceStoreRepository.claimEventOrChallengeDice(diceId = diceId)
            applyDiceStoreSnapshot(snap)
            if (ok) {
                soundAndHaptics.playCoinReward()
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            statusBanner = "Challenge Complete! Claimed free dice!"
                        )
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        diceStoreUiState = it.diceStoreUiState.copy(
                            errorMessage = "Complete the challenge objective first to claim this dice!"
                        )
                    )
                }
            }
        }
    }

    fun adminSaveOrUpdateDice(dice: PremiumDiceItem) {
        viewModelScope.launch {
            val snap = diceStoreRepository.adminSaveOrUpdateDice(dice)
            applyDiceStoreSnapshot(snap)
            _uiState.update {
                it.copy(
                    bannerNotice = "🛡️ Admin updated dice: ${dice.name}",
                    diceStoreUiState = it.diceStoreUiState.copy(statusBanner = "Admin saved ${dice.name} to live store catalog!")
                )
            }
            soundAndHaptics.playCoinReward()
        }
    }

    fun adminDeleteDice(diceId: String) {
        viewModelScope.launch {
            val snap = diceStoreRepository.adminDeleteDice(diceId)
            applyDiceStoreSnapshot(snap)
            _uiState.update {
                it.copy(
                    bannerNotice = "🛡️ Removed dice $diceId from store",
                    diceStoreUiState = it.diceStoreUiState.copy(statusBanner = "Removed dice $diceId from catalog.")
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playBillingManager.endConnection()
        soundAndHaptics.release()
    }
}


