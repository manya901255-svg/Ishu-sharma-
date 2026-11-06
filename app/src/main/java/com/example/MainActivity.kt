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
import com.example.ui.components.MainMenuOverlay
import com.example.ui.components.WelcomeOverlayScreen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoyalNavyDark
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LudoViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LudoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = RoyalNavyDark
                ) {
                    val uiState by viewModel.uiState.collectAsState()

                    // BackHandler for overlays and secondary screens
                    BackHandler(enabled = uiState.showWelcomeOverlay) {
                        viewModel.dismissWelcomeOverlay()
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

                    BackHandler(enabled = !uiState.showWelcomeOverlay && !uiState.showMainMenuOverlay && uiState.currentScreen != AppScreen.HOME && uiState.currentScreen != AppScreen.GAME) {
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
                            AppScreen.PROFILE -> {
                                ProfileScreen(
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


