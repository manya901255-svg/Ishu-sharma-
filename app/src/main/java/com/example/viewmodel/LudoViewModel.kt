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
    PROFILE,
    TECH_BLUEPRINT
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
    val activeCelebration: CelebrationType? = null,
    val playerNickname: String = "Ishu",
    val playerCrownAvatar: String = "crown_imperial_gold",
    val customProfilePhotoUri: String? = null,
    val playerTitle: String = "Maharaja",
    val playerLevel: Int = 18,
    // Player Career Statistics
    val totalWins: Int = 28,
    val totalGamesPlayed: Int = 42,
    val totalCaptures: Int = 89,
    val winStreak: Int = 4,
    val equippedPawnCrownAvatar: String = "crown_imperial_gold",
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
    val board3DAngle: Float = 28f,
    val showMusicPlayerDialog: Boolean = false,
    val isMusicPlaying: Boolean = false,
    val musicRepeatMode: com.example.model.MusicRepeatMode = com.example.model.MusicRepeatMode.ALL,
    val isMusicShuffleEnabled: Boolean = false,
    val favoriteSongIds: Set<String> = emptySet(),
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
    val activeInAppProduct: com.example.ui.components.InAppPurchaseProduct? = null
)

class LudoViewModel(application: Application) : AndroidViewModel(application) {

    val soundAndHaptics = SoundAndHaptics(application)
    val voiceChatManager = com.example.util.LudoVoiceChatManager(application)
    private val profilePreferences = com.example.util.ProfilePreferences(application)
    private val repository = com.example.data.LudoRepository(application)

    private val _uiState = MutableStateFlow(LudoUiState())
    val uiState: StateFlow<LudoUiState> = _uiState.asStateFlow()

    private var turnTimerJob: Job? = null
    private var aiTurnJob: Job? = null

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
                equippedBoardTheme = savedBoardTheme,
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

        // Initialize Room Database persistence for Match History and Daily Rewards
        viewModelScope.launch {
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
        }
        soundAndHaptics.soundManager.setOnPlaybackStateListener { isPlaying ->
            _uiState.update { it.copy(isMusicPlaying = isPlaying) }
        }
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

        // Prepare initial board state while keeping lobby as starting destination
        startNewGame(mode = GameMode.VS_AI, playerCount = 4, isInitialSetup = true)
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

    fun toggle3DBoard(enabled: Boolean) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(is3DBoardEnabled = enabled) }
    }

    fun cycle3DAngle() {
        soundAndHaptics.vibrateClick()
        _uiState.update { state ->
            val nextAngle = when (state.board3DAngle) {
                28f -> 36f
                36f -> 18f
                18f -> 28f
                else -> 28f
            }
            state.copy(board3DAngle = nextAngle, is3DBoardEnabled = true)
        }
    }

    fun set3DAngle(angle: Float) {
        soundAndHaptics.vibrateClick()
        _uiState.update { it.copy(board3DAngle = angle, is3DBoardEnabled = angle > 0f) }
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
                if (m.id == missionId && !m.isCompleted) {
                    val newProgress = (m.currentProgress + increment).coerceAtMost(m.targetProgress)
                    m.copy(currentProgress = newProgress)
                } else m
            }
            s.copy(dailyMissions = updated)
        }
    }

    fun claimDailyMission(mission: com.example.model.DailyMission) {
        soundAndHaptics.soundManager.playVictoryFanfare()
        triggerCelebration(CelebrationType.MATCH_WON)
        val newCoins = _uiState.value.userCoins + mission.rewardCoins
        val newGems = _uiState.value.userGems + mission.rewardGems
        profilePreferences.setUserCoins(newCoins)
        profilePreferences.setUserGems(newGems)

        val newAvatar = mission.rewardCosmeticId ?: _uiState.value.playerCrownAvatar
        if (mission.rewardCosmeticId != null) {
            profilePreferences.setCrownAvatar(newAvatar)
        }

        _uiState.update { s ->
            val updatedMissions = s.dailyMissions.map {
                if (it.id == mission.id) it.copy(isClaimed = true) else it
            }
            val updatedPlayers = s.players.map { p ->
                if ((p.name == s.playerNickname || p.id == "p_0") && mission.rewardCosmeticId != null) {
                    p.copy(avatar = newAvatar)
                } else p
            }
            s.copy(
                userCoins = newCoins,
                userGems = newGems,
                playerCrownAvatar = newAvatar,
                players = updatedPlayers,
                dailyMissions = updatedMissions,
                bannerNotice = "🎉 Quest Completed: Claimed +🪙 ${mission.rewardCoins} & +💎 ${mission.rewardGems}!"
            )
        }
        showPopUpNotice(
            title = "📜 Royal Mission Claimed! 👑",
            message = "Completed: ${mission.title}\nClaimed +🪙 ${mission.rewardCoins} Coins & +💎 ${mission.rewardGems} Gems" +
                    if (mission.rewardCosmeticName != null) "\nUnlocked: ${mission.rewardCosmeticName}" else "",
            icon = "📜",
            type = "REWARD"
        )
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

    fun rewardUserForAd(coins: Long = 1000L, gems: Int = 0) {
        soundAndHaptics.vibrateVictory()
        soundAndHaptics.playCoinReward()
        _uiState.update {
            val newCoins = it.userCoins + coins
            val newGems = it.userGems + gems
            profilePreferences.setUserCoins(newCoins)
            profilePreferences.setUserGems(newGems)
            it.copy(
                userCoins = newCoins,
                userGems = newGems,
                bannerNotice = "🎬 Ad Completed: +$coins Royal Coins added to your vault!"
            )
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

        val colors = if (playerCount == 2) {
            val opposite = when (chosenColor) {
                LudoColor.RED -> LudoColor.BLUE
                LudoColor.GREEN -> LudoColor.YELLOW
                LudoColor.YELLOW -> LudoColor.GREEN
                LudoColor.BLUE -> LudoColor.RED
            }
            listOf(chosenColor, opposite)
        } else {
            val remaining = listOf(LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW, LudoColor.BLUE).filter { it != chosenColor }
            listOf(chosenColor) + remaining.take(playerCount - 1)
        }

        val players = colors.mapIndexed { index, color ->
            val isHuman = when (mode) {
                GameMode.PASS_AND_PLAY -> true
                else -> index == 0
            }
            val name = when {
                index == 0 -> if (_uiState.value.playerNickname.isNotBlank() && _uiState.value.playerNickname != "You") "You (${_uiState.value.playerNickname})" else "You"
                mode == GameMode.PASS_AND_PLAY -> "Player ${index + 1}"
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
                isOnline = mode == GameMode.ONLINE_MULTIPLAYER || mode == GameMode.PRIVATE_ROOM,
                avatar = avatar,
                customPhotoUri = if (index == 0) _uiState.value.customProfilePhotoUri else null,
                level = playerLevel,
                pawns = initialPawns,
                coins = if (index == 0) _uiState.value.userCoins else 15000L + Random.nextInt(5000)
            )
        }

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
                currentMatchReplayMoves = emptyList(),
                bannerNotice = "Royal ${mode.title} begins! Wager: 🪙 ${com.example.ui.components.formatCompactCoins(wager)} • 2X Double Prize Active!",
                chatMessages = listOf(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        senderName = "Royal Court",
                        senderColor = LudoColor.YELLOW,
                        text = "Match started! 2X Double Prize active: Winner claims double coins and a Mystery Chest Box!"
                    )
                )
            )
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
        } else if (state.turnPhase == TurnPhase.SELECT_PAWN) {
            // Auto select best pawn
            val movable = currentPlayer.pawns.filter { it.isMovable }
            if (movable.isNotEmpty()) {
                val best = LudoEngine.pickBestAiMove(currentPlayer, movable, state.currentDiceValue, state.players)
                    ?: movable.first()
                onPawnClicked(best)
            } else {
                passToNextPlayer()
            }
        }
    }

    fun rollDice() {
        val state = _uiState.value
        if (state.isRolling || state.turnPhase != TurnPhase.ROLL_DICE || state.winner != null) return

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
            soundAndHaptics.playDiceRoll()

            // Intermediate bounce 1 impact haptics
            delay(400)
            soundAndHaptics.playButtonClick()

            // Settle trajectory
            delay(450)

            val currentPlayer = state.players[state.currentPlayerIndex]
            val newConsecutive = if (rollValue == 6) state.consecutiveSixes + 1 else 0

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
                delay(1200)
                passToNextPlayer()
                return@launch
            }

            if (rollValue == 6) {
                soundAndHaptics.playSixRolled()
                triggerCelebration(CelebrationType.SIX_ROLLED)
                updateMissionProgress("quest_roll_sixes", 1)
            }

            val updatedPawns = currentPlayer.pawns.map { pawn ->
                pawn.copy(isMovable = LudoEngine.canPawnMove(pawn, rollValue))
            }
            val movableCount = updatedPawns.count { it.isMovable }

            _uiState.update { s ->
                val newPlayers = s.players.toMutableList()
                newPlayers[s.currentPlayerIndex] = currentPlayer.copy(pawns = updatedPawns, lastDiceValue = rollValue)
                s.copy(
                    players = newPlayers,
                    currentDiceValue = rollValue,
                    isRolling = false,
                    fairDiceProof = proof,
                    consecutiveSixes = newConsecutive,
                    turnPhase = if (movableCount > 0) TurnPhase.SELECT_PAWN else TurnPhase.ROLL_DICE,
                    bannerNotice = if (movableCount == 0) "${currentPlayer.name} rolled $rollValue with no valid moves." else "${currentPlayer.name} rolled $rollValue! Select pawn to move."
                )
            }

            if (movableCount == 0) {
                delay(900)
                passToNextPlayer()
            } else if (movableCount == 1 && !currentPlayer.isAi) {
                // Auto-advance if only 1 move is possible
                delay(400)
                val onlyPawn = updatedPawns.first { it.isMovable }
                onPawnClicked(onlyPawn)
            } else if (currentPlayer.isAi) {
                delay(600)
                val aiBestPawn = LudoEngine.pickBestAiMove(
                    player = currentPlayer,
                    movablePawns = updatedPawns.filter { it.isMovable },
                    dice = rollValue,
                    allPlayers = _uiState.value.players
                )
                if (aiBestPawn != null) {
                    onPawnClicked(aiBestPawn)
                } else {
                    passToNextPlayer()
                }
            }
        }
    }

    fun onPawnClicked(pawn: Pawn) {
        val state = _uiState.value
        if (state.turnPhase != TurnPhase.SELECT_PAWN || state.winner != null) return

        val currentPlayer = state.players[state.currentPlayerIndex]
        if (pawn.color != currentPlayer.color || !pawn.isMovable) return

        viewModelScope.launch {
            _uiState.update { it.copy(turnPhase = TurnPhase.ANIMATING_MOVE) }

            val movedPawn = LudoEngine.calculateMoveResult(pawn, state.currentDiceValue)
            val isHeadingHome = movedPawn.state == PawnState.IN_HOME_STRETCH

            // Trigger wooden pawn step movement sound with ascending acoustic step pitch
            soundAndHaptics.playPawnMove(
                stepIndex = state.currentDiceValue,
                totalSteps = state.currentDiceValue,
                isHomeStretch = isHeadingHome
            )

            var gaveExtraTurn = false
            var bannerMsg: String? = null

            // Check if captured enemy pawn(s)
            val captures = LudoEngine.checkForCaptures(movedPawn, state.players)
            val updatedPlayers = state.players.map { player ->
                if (player.color == currentPlayer.color) {
                    val newPawns = player.pawns.map { if (it.id == pawn.id) movedPawn else it.copy(isMovable = false) }
                    player.copy(pawns = newPawns)
                } else {
                    // Check if any pawn of this player was captured
                    val capturedOfPlayer = captures.filter { it.first.color == player.color }.map { it.second.id }
                    if (capturedOfPlayer.isNotEmpty()) {
                        val resetPawns = player.pawns.map { p ->
                            if (p.id in capturedOfPlayer) p.copy(state = PawnState.IN_BASE, trackPosition = -1, homeStep = 0) else p
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
            }

            // Check if pawn reached home
            if (movedPawn.state == PawnState.FINISHED && pawn.state != PawnState.FINISHED) {
                soundAndHaptics.playPawnHome()
                triggerCelebration(CelebrationType.PAWN_FINISHED)
                bannerMsg = "👑 Royal Home! A pawn entered the center triangle! Bonus Turn!"
                gaveExtraTurn = true
                updateMissionProgress("quest_home_run", 1)
            }

            // Check if rolled 6
            if (state.currentDiceValue == 6) {
                gaveExtraTurn = true
                bannerMsg = bannerMsg ?: "Rolled 6! ${currentPlayer.name} earns an extra roll!"
            }

            // Check Win Condition based on active game mode (Classic, Rush, Bolt, Dash)
            val updatedCurrentPlayer = updatedPlayers[state.currentPlayerIndex]
            val isWinner = com.example.engine.LudoEngine.hasPlayerWon(updatedCurrentPlayer, state.gameMode)

            if (isWinner) {
                soundAndHaptics.vibrateVictory()
                triggerCelebration(CelebrationType.MATCH_WON)
                val isHumanWin = updatedCurrentPlayer.id == "p_0"
                profilePreferences.recordGameFinished(isWin = isHumanWin, captures = captures.size)
                
                if (updatedCurrentPlayer.color == com.example.model.LudoColor.YELLOW) {
                    updateMissionProgress("quest_gold_pawns", 1)
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

                _uiState.update { s ->
                    val newCoins = if (isHumanWin) s.userCoins + coinWinnings else s.userCoins
                    if (isHumanWin) {
                        profilePreferences.setUserCoins(newCoins)
                    }
                    s.copy(
                        players = updatedPlayers,
                        winner = updatedCurrentPlayer,
                        showResultsDialog = true,
                        showSpecialOfferDialog = triggerSpecialOffer,
                        onlineGamesPlayedCount = newOnlineCount,
                        wonChestBoxName = wonChest,
                        totalWins = profilePreferences.getTotalWins(),
                        totalGamesPlayed = profilePreferences.getGamesPlayed(),
                        winStreak = profilePreferences.getWinStreak(),
                        totalCaptures = profilePreferences.getTotalCaptures(),
                        bannerNotice = if (isHumanWin) "🏆 2X DOUBLE PRIZE! ${updatedCurrentPlayer.name} won +🪙 ${com.example.ui.components.formatCompactCoins(coinWinnings)} and received $wonChest!" else "Match concluded.",
                        userCoins = newCoins,
                        savedReplays = listOf(completedReplaySession) + s.savedReplays,
                        currentMatchReplayMoves = emptyList()
                    )
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

            _uiState.update { s ->
                s.copy(
                    players = updatedPlayers,
                    currentMatchReplayMoves = updatedReplayMoves,
                    bannerNotice = bannerMsg ?: s.bannerNotice
                )
            }

            delay(400)

            if (gaveExtraTurn) {
                _uiState.update {
                    it.copy(
                        turnPhase = TurnPhase.ROLL_DICE
                    )
                }
                startTurnTimer()
                checkAiTurn()
            } else {
                passToNextPlayer()
            }
        }
    }

    private fun passToNextPlayer() {
        val state = _uiState.value
        if (state.winner != null) return

        val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
        val nextPlayer = state.players[nextIndex]

        // Reset movable flags
        val clearedPlayers = state.players.map { player ->
            player.copy(pawns = player.pawns.map { it.copy(isMovable = false) })
        }

        _uiState.update {
            it.copy(
                players = clearedPlayers,
                currentPlayerIndex = nextIndex,
                turnPhase = TurnPhase.ROLL_DICE,
                consecutiveSixes = 0,
                bannerNotice = "Turn: ${nextPlayer.name} (${nextPlayer.color.title})"
            )
        }

        startTurnTimer()
        voiceChatManager.onPlayerTurnStart(nextPlayer.id, nextPlayer.isAi)
        checkAiTurn()
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

    override fun onCleared() {
        super.onCleared()
        soundAndHaptics.release()
    }
}

