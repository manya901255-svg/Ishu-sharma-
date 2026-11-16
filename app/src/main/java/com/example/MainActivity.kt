package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.model.GameMode
import com.example.ui.components.NotificationsCenterDialog
import com.example.ui.components.TasksAndRewardsDialog
import com.example.ui.components.MainMenuOverlay
import com.example.ui.components.WelcomeOverlayScreen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LudoViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LudoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            MyApplicationTheme(darkTheme = uiState.isDarkTheme) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    if (uiState.isFirebaseConfigured && !uiState.isSignedIn) {
                        SignInScreen(
                            onSignedIn = { firebaseUser ->
                                viewModel.onGoogleUserSignedIn(firebaseUser)
                            }
                        )
                    } else {
                        // BackHandler for overlays and secondary screens
                        BackHandler(enabled = uiState.showWelcomeOverlay) {
                            viewModel.dismissWelcomeOverlay()
                        }

                        BackHandler(enabled = uiState.showVerifiedTasksDialog) {
                            viewModel.toggleVerifiedTasksDialog(false)
                        }

                        BackHandler(enabled = uiState.showNotificationsDialog) {
                            viewModel.toggleNotificationsDialog(false)
                        }

                        BackHandler(enabled = !uiState.showWelcomeOverlay && uiState.showMainMenuOverlay) {
                            viewModel.toggleMainMenuOverlay(false)
                        }

                        BackHandler(enabled = uiState.showFriendsDialog) {
                            viewModel.toggleFriendsDialog(false)
                        }

                        BackHandler(enabled = uiState.showDailySpin) {
                            viewModel.toggleDailySpin(false)
                        }

                        BackHandler(enabled = uiState.showWallpaperCustomizerDialog) {
                            viewModel.toggleWallpaperCustomizer(false)
                        }

                        BackHandler(
                            enabled = !uiState.showWelcomeOverlay &&
                                !uiState.showMainMenuOverlay &&
                                uiState.currentScreen != AppScreen.HOME &&
                                uiState.currentScreen != AppScreen.GAME
                        ) {
                            viewModel.navigateTo(AppScreen.HOME)
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            when (uiState.currentScreen) {
                                AppScreen.HOME -> {
                                    HomeScreen(
                                        uiState = uiState,
                                        viewModel = viewModel
                                    )
                                }
                                AppScreen.GAME -> {
                                    GameScreen(
                                        uiState = uiState,
                                        viewModel = viewModel
                                    )
                                }
                                AppScreen.LEADERBOARD -> {
                                    LeaderboardScreen(
                                        uiState = uiState,
                                        onSendFriendRequest = { profile -> viewModel.sendFriendRequest(profile) },
                                        onToggleFollow = { uid, name -> viewModel.toggleFollowPlayer(uid, name) },
                                        onOpenClub = { club ->
                                            viewModel.selectActiveClub(club)
                                            viewModel.navigateTo(AppScreen.VOICE_CLUB)
                                        },
                                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                                    )
                                }
                                AppScreen.SHOP -> {
                                    RoyalShopScreen(
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                                    )
                                }
                                AppScreen.PREMIUM_DICE_STORE -> {
                                    PremiumDiceStoreScreen(
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateTo(AppScreen.SHOP) }
                                    )
                                }
                                AppScreen.INVENTORY -> {
                                    InventoryScreen(
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                                    )
                                }
                                AppScreen.PROFILE -> {
                                    ProfileScreen(
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                                    )
                                }
                                AppScreen.VOICE_CLUB -> {
                                    VoiceClubScreen(
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                                    )
                                }
                                AppScreen.TECH_BLUEPRINT -> {
                                    TechBlueprintScreen(
                                        onBack = { viewModel.navigateTo(AppScreen.HOME) }
                                    )
                                }
                            }

                            // Welcome Overlay screen on initial launch
                            AnimatedVisibility(
                                visible = uiState.showWelcomeOverlay,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                WelcomeOverlayScreen(
                                    onFinished = { viewModel.dismissWelcomeOverlay() }
                                )
                            }

                            // Main Menu Overlay appearing after welcome screen
                            AnimatedVisibility(
                                visible = !uiState.showWelcomeOverlay && uiState.showMainMenuOverlay,
                                enter = fadeIn() + scaleIn(initialScale = 0.94f),
                                exit = fadeOut() + scaleOut(targetScale = 0.94f)
                            ) {
                                MainMenuOverlay(
                                    onPlayOnline = {
                                        viewModel.toggleMainMenuOverlay(false)
                                        viewModel.startMatchmaking(GameMode.ONLINE_MULTIPLAYER, 4)
                                        viewModel.navigateTo(AppScreen.GAME)
                                    },
                                    onPlayAi = {
                                        viewModel.toggleMainMenuOverlay(false)
                                        viewModel.startNewGame(GameMode.VS_AI, 4)
                                        viewModel.navigateTo(AppScreen.GAME)
                                    },
                                    onPassAndPlay = {
                                        viewModel.toggleMainMenuOverlay(false)
                                        viewModel.startNewGame(GameMode.PASS_AND_PLAY, 4)
                                        viewModel.navigateTo(AppScreen.GAME)
                                    },
                                    onOpenSettings = {
                                        viewModel.toggleMainMenuOverlay(false)
                                        viewModel.toggleSoundSettings(true)
                                    },
                                    onOpenWallpapers = {
                                        viewModel.toggleMainMenuOverlay(false)
                                        viewModel.toggleWallpaperCustomizer(true)
                                    },
                                    onEnterLobby = {
                                        viewModel.toggleMainMenuOverlay(false)
                                        viewModel.navigateTo(AppScreen.HOME)
                                    },
                                    onOpenBlueprint = {
                                        viewModel.toggleMainMenuOverlay(false)
                                        viewModel.navigateTo(AppScreen.TECH_BLUEPRINT)
                                    }
                                )
                            }

                            // Verified Tasks & Rewards Dialog (Globally Accessible)
                            if (uiState.showVerifiedTasksDialog) {
                                TasksAndRewardsDialog(
                                    tasks = uiState.verifiedTasks,
                                    achievements = uiState.verifiedAchievements,
                                    rewardHistory = uiState.rewardHistory,
                                    loginStreak = uiState.loginStreak,
                                    onClaimTask = { taskId -> viewModel.claimVerifiedTaskReward(taskId) },
                                    onDismiss = { viewModel.toggleVerifiedTasksDialog(false) }
                                )
                            }

                            // Notifications Center Dialog (Globally Accessible)
                            if (uiState.showNotificationsDialog) {
                                NotificationsCenterDialog(
                                    notifications = uiState.notifications,
                                    preferences = uiState.notificationPrefs,
                                    onMarkRead = { notifId -> viewModel.markNotificationRead(notifId) },
                                    onUpdatePreferences = { prefs -> viewModel.updateNotificationPrefs(prefs) },
                                    onDismiss = { viewModel.toggleNotificationsDialog(false) }
                                )
                            }

                            // Root Custom Wallpaper Studio Dialog (Globally Accessible)
                            if (uiState.showWallpaperCustomizerDialog) {
                                com.example.ui.components.CustomWallpaperDialog(
                                    initialTarget = uiState.activeWallpaperTarget,
                                    wallpapersPerTarget = uiState.activeWallpapers,
                                    customUploadedWallpapers = uiState.customUploadedWallpapers,
                                    favoriteWallpaperIds = uiState.favoriteWallpaperIds,
                                    recentWallpaperIds = uiState.recentWallpaperIds,
                                    onApplyWallpaper = { target, idOrPath ->
                                        viewModel.applyWallpaper(target, idOrPath)
                                    },
                                    onUploadImageUri = { uri, target ->
                                        viewModel.uploadWallpaperFromUri(uri, target)
                                    },
                                    onDeleteUploadedWallpaper = { path ->
                                        viewModel.deleteCustomWallpaper(path)
                                    },
                                    onToggleFavorite = { idOrPath ->
                                        viewModel.toggleWallpaperFavorite(idOrPath)
                                    },
                                    onResetTarget = { target ->
                                        viewModel.resetWallpaperForTarget(target)
                                    },
                                    onResetAll = {
                                        viewModel.resetAllWallpapers()
                                    },
                                    onDismiss = {
                                        viewModel.toggleWallpaperCustomizer(false)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.soundAndHaptics.soundManager.resumeAmbientBgm()
    }

    override fun onPause() {
        super.onPause()
        viewModel.soundAndHaptics.soundManager.pauseAmbientBgm()
    }
}


