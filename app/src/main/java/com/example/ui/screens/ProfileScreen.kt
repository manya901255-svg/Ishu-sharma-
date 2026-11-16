package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.ui.components.MgLudoRoyalBackground
import com.example.ui.components.WallpaperBackgroundView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.*
import com.example.ui.components.formatCompactCoins
import com.example.ui.theme.*
import com.example.viewmodel.LudoUiState
import com.example.viewmodel.LudoViewModel

data class ProfileAchievement(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val xpReward: Int,
    val coinReward: Long,
    val isCompleted: Boolean = false,
    val medalsEarned: Int = 1
)

/**
 * ProfileScreen renders the authentic Ludo Club "Player Profile" dialog from Screenshots 5 & 12:
 * - Two Tabs: [Profile] | [Achievements]
 * - Profile Tab (Screenshot 5):
 *   * Avatar DP with Level 19 star badge
 *   * Indian Flag + Player Name ("Ishu")
 *   * Referral Code: "GNFR200 [copy]"
 *   * Level 19, "68 of 1570" XP bar
 *   * [Edit] button to edit display name & pick custom photo DP from gallery
 *   * Full Player Stats card:
 *     - Total Earnings: 2.36 M
 *     - Games Won: 53 of 113
 *     - Win ratio: 46%
 *     - Current win streak: 6
 *     - Max win streak: 9
 *     - 2 Player Wins: 42
 *     - 4 Player Wins: 0
 *     - Pawns Captured: 376
 * - Achievements Tab (Screenshot 12):
 *   * "The Defender": Capture 500 pawns (230/500), 50 XP, 10K Coins
 *   * "Ludo Love": Complete 500 online games (66/500), 200 XP, 50K Coins
 *   * "Six Cliques": Get 1000 6s in dice rolls (Completed ✓)
 *   * "Home Runner": Take Home 300 Pawns in online (224/300), 50 XP, 10K Coins
 */
@Composable
fun ProfileScreen(
    uiState: LudoUiState,
    viewModel: LudoViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Profile, 1: Achievements
    var showEditProfileDialog by remember { mutableStateOf(false) }

    var nicknameInput by remember { mutableStateOf(uiState.playerNickname) }
    var customPhotoUriInput by remember { mutableStateOf(uiState.customProfilePhotoUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val prefs = com.example.util.ProfilePreferences(context)
            val stableUri = prefs.saveProfilePhotoLocally(uri) ?: uri.toString()
            customPhotoUriInput = stableUri
            viewModel.saveUserProfile(
                nickname = nicknameInput,
                crownAvatar = uiState.playerCrownAvatar,
                title = uiState.playerTitle,
                pawnCrownAvatar = uiState.equippedPawnCrownAvatar,
                customPhotoUri = stableUri
            )
            Toast.makeText(context, "Profile Photo Saved Permanently! 📸", Toast.LENGTH_SHORT).show()
        }
    }

    val displayReferralCode = uiState.referralCode.ifBlank { "MGL-000000" }
    val xpIntoLevel = uiState.playerXp % 250
    val xpTarget = 250
    val xpProgress = (xpIntoLevel.toFloat() / xpTarget.toFloat()).coerceIn(0f, 1f)
    val winRatioPct = if (uiState.totalGamesPlayed > 0) {
        (uiState.totalWins * 100) / uiState.totalGamesPlayed
    } else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("player_profile_screen")
    ) {
        // Dynamic Profile Background (Customizable)
        WallpaperBackgroundView(
            wallpaperIdOrPath = uiState.activeWallpapers[com.example.model.WallpaperTarget.PROFILE.id],
            target = com.example.model.WallpaperTarget.PROFILE,
            modifier = Modifier.fillMaxSize(),
            dimOverlayAlpha = 0.25f
        )

        // Centered Player Profile Card (matching Screenshot 5 & 12)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Profile Dialog Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                // Card Body
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp)
                        .shadow(20.dp, RoundedCornerShape(26.dp)),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7ECD8)), // Cream parchment
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE2D6C0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Spacer(modifier = Modifier.height(18.dp))

                        // Dual Tabs: [Profile] | [Achievements]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            // Profile Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    .background(if (selectedTab == 0) Color(0xFFF0DCB8) else Color(0xFFDFC69C))
                                    .border(
                                        1.5.dp,
                                        if (selectedTab == 0) Color(0xFFC7A574) else Color.Transparent,
                                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                    )
                                    .clickable { selectedTab = 0 }
                                    .testTag("profile_tab_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Profile",
                                    color = if (selectedTab == 0) Color(0xFF3E2723) else Color(0xFF795548),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Achievements Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    .background(if (selectedTab == 1) Color(0xFFF0DCB8) else Color(0xFFDFC69C))
                                    .border(
                                        1.5.dp,
                                        if (selectedTab == 1) Color(0xFFC7A574) else Color.Transparent,
                                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                    )
                                    .clickable { selectedTab = 1 }
                                    .testTag("achievements_tab_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Achievements",
                                    color = if (selectedTab == 1) Color(0xFF3E2723) else Color(0xFF795548),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Inner Parchment Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp, topEnd = if (selectedTab == 0) 18.dp else 0.dp, topStart = if (selectedTab == 1) 18.dp else 0.dp))
                                .background(Color(0xFFFBF4E8))
                                .border(1.5.dp, Color(0xFFD7CCC8), RoundedCornerShape(18.dp))
                                .padding(12.dp)
                        ) {
                            if (selectedTab == 0) {
                                // Profile Tab Content (Screenshot 5)
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    item {
                                        // Header Row: Avatar + Name + Referral + Level + Edit Button
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Avatar DP with Equipped Frame & Star Badge
                                            Box(
                                                modifier = Modifier
                                                    .size(78.dp)
                                                    .clickable { viewModel.toggleDynamicProfileDialog(true) }
                                                    .testTag("profile_framed_avatar"),
                                                contentAlignment = Alignment.BottomEnd
                                            ) {
                                                com.example.ui.components.FramedAvatarBox(
                                                    frameId = uiState.equippedFrameId,
                                                    size = 76.dp,
                                                    shape = RoundedCornerShape(16.dp),
                                                    showCornerBadge = true,
                                                    modifier = Modifier.align(Alignment.Center)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color(0xFF381559)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (uiState.customProfilePhotoUri != null) {
                                                            AsyncImage(
                                                                model = uiState.customProfilePhotoUri,
                                                                contentDescription = "DP Photo",
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier.fillMaxSize()
                                                            )
                                                        } else {
                                                            Text(getCrownAvatarEmoji(uiState.playerCrownAvatar), fontSize = 36.sp)
                                                        }
                                                    }
                                                }

                                                // Star Level Badge
                                                Box(
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFFFB300))
                                                        .border(1.5.dp, Color.White, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("★", color = Color(0xFF3E2723), fontSize = 14.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                // Indian Flag + Name + Title
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("🇮🇳", fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = uiState.playerNickname.ifBlank { "Ishu" },
                                                        color = Color(0xFF263238),
                                                        fontSize = 17.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "(${uiState.playerTitle})",
                                                        color = Color(0xFF6A1B9A),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                // Live Referral Code [copy]
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .minimumInteractiveComponentSize()
                                                        .clickable {
                                                            clipboardManager.setText(AnnotatedString(displayReferralCode))
                                                            Toast.makeText(context, "Referral Code Copied! 📋", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .testTag("copy_referral_code_button")
                                                ) {
                                                    Text(
                                                        text = "Referral Code: ",
                                                        color = Color(0xFF5D4037),
                                                        fontSize = 11.sp
                                                    )
                                                    Text(
                                                        text = displayReferralCode,
                                                        color = Color(0xFF1565C0),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy Referral Code",
                                                        tint = Color(0xFF757575),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Dynamic Level & XP Progress Bar
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "Level:",
                                                        color = Color(0xFF1565C0),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFFFA000)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("${uiState.playerLevel}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(14.dp)
                                                            .clip(RoundedCornerShape(7.dp))
                                                            .background(Color(0xFF1A237E)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxHeight()
                                                                .fillMaxWidth(xpProgress)
                                                                .align(Alignment.CenterStart)
                                                                .background(Color(0xFF00E676))
                                                        )
                                                        Text(
                                                            text = "$xpIntoLevel of $xpTarget XP",
                                                            color = Color.White,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                // [Edit] Button and [Sovereign Avatars] Button
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Button(
                                                        onClick = { showEditProfileDialog = true },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                                                        modifier = Modifier
                                                            .height(30.dp)
                                                            .testTag("edit_profile_dialog_button")
                                                    ) {
                                                        Text("Edit", color = Color(0xFF3E2723), fontSize = 12.sp, fontWeight = FontWeight.Black)
                                                    }

                                                    Button(
                                                        onClick = { viewModel.toggleDynamicProfileDialog(true) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(30.dp)
                                                    ) {
                                                        Text("👑 Sovereign Avatars", color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = FontWeight.Black)
                                                    }

                                                    Button(
                                                        onClick = { viewModel.toggleWallpaperCustomizer(true, com.example.model.WallpaperTarget.PROFILE) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                                        shape = RoundedCornerShape(12.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(30.dp)
                                                    ) {
                                                        Text("🎨 Wallpaper", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Stats Header
                                        Text(
                                            text = "Stats",
                                            color = Color(0xFF3E2723),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Dynamic Stats List from real player gameplay
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5D0)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD7CCC8))
                                        ) {
                                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                listOf(
                                                    Pair("Total Earnings", formatCompactCoins(uiState.totalEarnings)),
                                                    Pair("Games Won", "${uiState.totalWins} of ${uiState.totalGamesPlayed}"),
                                                    Pair("Win ratio", "$winRatioPct%"),
                                                    Pair("Current win streak", "${uiState.winStreak}"),
                                                    Pair("Max win streak", "${uiState.bestWinStreak}"),
                                                    Pair("Total Dice Rolls", "${uiState.totalRolls}"),
                                                    Pair("Pawns Captured", "${uiState.totalCaptures}"),
                                                    Pair("Today's Matches", "${uiState.todayActivity.matchesPlayed}"),
                                                    Pair("Verified Invites", "${uiState.verifiedReferralsCount}")
                                                ).forEachIndexed { idx, (label, value) ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(if (idx % 2 == 0) Color.Transparent else Color(0x22FFFFFF))
                                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = label,
                                                            color = Color(0xFF4E342E),
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = value,
                                                            color = Color(0xFF263238),
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Black
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Theme, Notifications & Account Controls
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5D0)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD7CCC8))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text(
                                                    text = "Preferences & Account",
                                                    color = Color(0xFF3E2723),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = if (uiState.isDarkTheme) "Dark Royal Theme" else "Light Royal Theme",
                                                            color = Color(0xFF3E2723),
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "Switch between Dark & Light appearance",
                                                            color = Color(0xFF5D4037),
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                    Switch(
                                                        checked = uiState.isDarkTheme,
                                                        onCheckedChange = { viewModel.toggleDarkTheme(it) },
                                                        modifier = Modifier.testTag("theme_mode_switch")
                                                    )
                                                }

                                                val unreadNotif = uiState.notifications.count { !it.isRead }
                                                OutlinedButton(
                                                    onClick = { viewModel.toggleNotificationsDialog(true) },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .minimumInteractiveComponentSize()
                                                        .testTag("open_notifications_button"),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        text = if (unreadNotif > 0) "🔔 Notifications ($unreadNotif Unread)" else "🔔 Notifications & Alerts",
                                                        color = Color(0xFF1565C0),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                if (uiState.isFirebaseConfigured && uiState.isSignedIn) {
                                                    if (!uiState.authUserEmail.isNullOrBlank()) {
                                                        Text(
                                                            text = "Signed in: ${uiState.authUserEmail}",
                                                            color = Color(0xFF5D4037),
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                    Button(
                                                        onClick = { viewModel.signOutGoogleUser() },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .minimumInteractiveComponentSize()
                                                            .testTag("google_sign_out_button")
                                                    ) {
                                                        Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Verified Achievements Tab Content
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (uiState.verifiedAchievements.isNotEmpty()) {
                                        items(uiState.verifiedAchievements, key = { it.taskId }) { ach ->
                                            val target = ach.target.coerceAtLeast(1)
                                            val prog = (ach.progress.toFloat() / target.toFloat()).coerceIn(0f, 1f)
                                            val badgeEmoji = when (ach.taskId) {
                                                "ach_first_blood" -> "🥇"
                                                "ach_ten_wins" -> "👑"
                                                "ach_fifty_captures" -> "⚔️"
                                                "ach_hundred_rolls" -> "🎲"
                                                else -> "🏆"
                                            }
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("verified_achievement_${ach.taskId}"),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5D0)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD7CCC8))
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(badgeEmoji, fontSize = 24.sp)
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = ach.title,
                                                                color = Color(0xFF3E2723),
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                            Text(
                                                                text = ach.description,
                                                                color = Color(0xFF5D4037),
                                                                fontSize = 10.sp
                                                            )
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(14.dp)
                                                                    .clip(RoundedCornerShape(7.dp))
                                                                    .background(Color(0xFF6A1B9A)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxHeight()
                                                                        .fillMaxWidth(prog)
                                                                        .align(Alignment.CenterStart)
                                                                        .background(
                                                                            Brush.horizontalGradient(
                                                                                listOf(Color(0xFF29B6F6), Color(0xFF0288D1))
                                                                            )
                                                                        )
                                                                )
                                                                Text(
                                                                    text = "${ach.progress} / ${ach.target}",
                                                                    color = Color.White,
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text("Rewards:", color = Color(0xFF5D4037), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            if (ach.rewardXp > 0) {
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("⭐ ${ach.rewardXp} XP", color = Color(0xFFF57F17), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                            }
                                                            if (ach.rewardCoins > 0) {
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text("🪙 ${formatCompactCoins(ach.rewardCoins.toLong())}", color = Color(0xFFE65100), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                            }
                                                        }
                                                        when (ach.status) {
                                                            TaskStatus.CLAIMED -> {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(8.dp))
                                                                        .background(Color(0xFFC8E6C9))
                                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text("✓ Claimed", color = Color(0xFF1B5E20), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                            TaskStatus.COMPLETED -> {
                                                                Button(
                                                                    onClick = { viewModel.claimVerifiedTaskReward(ach.taskId) },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                                    modifier = Modifier.minimumInteractiveComponentSize()
                                                                ) {
                                                                    Text("Claim Reward", color = Color(0xFF003300), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                                }
                                                            }
                                                            TaskStatus.IN_PROGRESS -> {
                                                                Text("In Progress", color = Color(0xFF8D6E63), fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        items(uiState.achievements) { ach ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5D0)),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD7CCC8))
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(ach.iconEmoji, fontSize = 24.sp)
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(ach.title, color = Color(0xFF3E2723), fontSize = 13.sp, fontWeight = FontWeight.Black)
                                                            Text(ach.description, color = Color(0xFF5D4037), fontSize = 10.sp)
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(14.dp)
                                                                    .clip(RoundedCornerShape(7.dp))
                                                                    .background(Color(0xFF6A1B9A)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxHeight()
                                                                        .fillMaxWidth((ach.currentProgress.toFloat() / ach.targetProgress).coerceIn(0f, 1f))
                                                                        .align(Alignment.CenterStart)
                                                                        .background(Brush.horizontalGradient(listOf(Color(0xFF29B6F6), Color(0xFF0288D1))))
                                                                )
                                                                Text("${ach.currentProgress} / ${ach.targetProgress}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text("Rewards:", color = Color(0xFF5D4037), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("⭐ ${ach.xpReward} XP", color = Color(0xFFF57F17), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("🪙 ${formatCompactCoins(ach.coinReward)}", color = Color(0xFFE65100), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                        }
                                                        if (ach.isClaimed) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(Color(0xFFC8E6C9))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text("✓ Claimed", color = Color(0xFF1B5E20), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        } else if (ach.currentProgress >= ach.targetProgress || ach.isCompleted) {
                                                            Button(
                                                                onClick = { viewModel.claimAchievement(ach) },
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                                                shape = RoundedCornerShape(8.dp),
                                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                                modifier = Modifier.minimumInteractiveComponentSize()
                                                            ) {
                                                                Text("Claim Reward", color = Color(0xFF003300), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                            }
                                                        } else {
                                                            Text("In Progress", color = Color(0xFF8D6E63), fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Header Pill: "Player Profile"
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFF7043), Color(0xFFF4511E))
                            )
                        )
                        .border(2.dp, Color.White, RoundedCornerShape(22.dp))
                        .shadow(10.dp, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Player Profile",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Top-right Close Button (X)
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 2.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("profile_close_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Edit Profile Dialog (Edit nickname & pick photo DP)
        if (showEditProfileDialog) {
            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                title = { Text("Edit Player Profile ✏️", fontWeight = FontWeight.Black) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Display Name:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = nicknameInput,
                            onValueChange = { nicknameInput = it },
                            modifier = Modifier.fillMaxWidth().testTag("edit_nickname_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Custom Profile Photo (DP):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            modifier = Modifier.fillMaxWidth().testTag("pick_dp_photo_button")
                        ) {
                            Text("📸 Select Photo From Gallery", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.saveUserProfile(
                                nickname = nicknameInput.ifBlank { "Ishu" },
                                crownAvatar = uiState.playerCrownAvatar,
                                title = uiState.playerTitle,
                                pawnCrownAvatar = uiState.equippedPawnCrownAvatar,
                                customPhotoUri = customPhotoUriInput
                            )
                            showEditProfileDialog = false
                            Toast.makeText(context, "Profile Saved! ✨", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (uiState.showDynamicProfileDialog) {
            com.example.ui.components.DynamicProfileDialog(
                currentAvatarId = uiState.playerCrownAvatar,
                currentTitle = uiState.playerTitle,
                playerName = uiState.playerNickname,
                onAvatarSelected = { avatarId, title ->
                    viewModel.equipAncientAvatar(avatarId, title)
                },
                onDismiss = { viewModel.toggleDynamicProfileDialog(false) }
            )
        }

        // MG Ludo Custom Wallpaper Studio Dialog
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
