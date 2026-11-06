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

    val achievements = remember {
        listOf(
            ProfileAchievement(
                id = "ach_defender",
                title = "The Defender",
                description = "Capture 500 pawns",
                currentProgress = 230,
                targetProgress = 500,
                xpReward = 50,
                coinReward = 10000L,
                medalsEarned = 1
            ),
            ProfileAchievement(
                id = "ach_ludo_love",
                title = "Ludo Love",
                description = "Complete 500 online games",
                currentProgress = 66,
                targetProgress = 500,
                xpReward = 200,
                coinReward = 50000L,
                medalsEarned = 2
            ),
            ProfileAchievement(
                id = "ach_six_cliques",
                title = "Six Cliques",
                description = "Get 1000 6s in dice rolls",
                currentProgress = 1000,
                targetProgress = 1000,
                xpReward = 100,
                coinReward = 25000L,
                isCompleted = true,
                medalsEarned = 3
            ),
            ProfileAchievement(
                id = "ach_home_runner",
                title = "Home Runner",
                description = "Take Home 300 Pawns in online",
                currentProgress = 224,
                targetProgress = 300,
                xpReward = 50,
                coinReward = 10000L,
                medalsEarned = 1
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("player_profile_screen")
    ) {
        // Authentic Purple Woodgrain Texture Background
        Image(
            painter = painterResource(id = R.drawable.bg_purple_wood),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000))
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
                                            // Avatar DP with Level 19 Star Badge
                                            Box(
                                                modifier = Modifier.size(76.dp),
                                                contentAlignment = Alignment.BottomEnd
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(72.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(Color(0xFF381559))
                                                        .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                                                        .clickable { viewModel.toggleDynamicProfileDialog(true) },
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

                                                // Referral Code: GNFR200 [copy]
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.clickable {
                                                        clipboardManager.setText(AnnotatedString("GNFR200"))
                                                        Toast.makeText(context, "Referral Code Copied! 📋", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Text(
                                                        text = "Referral Code: ",
                                                        color = Color(0xFF5D4037),
                                                        fontSize = 11.sp
                                                    )
                                                    Text(
                                                        text = "GNFR200",
                                                        color = Color(0xFF1565C0),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Copy",
                                                        tint = Color(0xFF757575),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Level: 19  68 of 1570 Progress Bar
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
                                                            .size(18.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFFFA000)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text("19", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
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
                                                                .fillMaxWidth(68f / 1570f)
                                                                .align(Alignment.CenterStart)
                                                                .background(Color(0xFF00E676))
                                                        )
                                                        Text(
                                                            text = "68 of 1570",
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

                                        // Authentic Stats List (matching Screenshot 5)
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5D0)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD7CCC8))
                                        ) {
                                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                listOf(
                                                    Pair("Total Earnings", "2.36 M"),
                                                    Pair("Games Won", "53 of 113"),
                                                    Pair("Win ratio", "46%"),
                                                    Pair("Current win streak", "6"),
                                                    Pair("Max win streak", "9"),
                                                    Pair("2 Player Wins", "42"),
                                                    Pair("4 Player Wins", "0"),
                                                    Pair("Pawns Captured", "376")
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
                                    }
                                }
                            } else {
                                // Achievements Tab Content (Screenshot 12)
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
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
                                                    // Medal / Icon
                                                    Text(ach.iconEmoji, fontSize = 24.sp)

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

                                                        // Progress Bar
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
                                                                    .background(
                                                                        Brush.horizontalGradient(
                                                                            listOf(Color(0xFF29B6F6), Color(0xFF0288D1))
                                                                        )
                                                                    )
                                                            )
                                                            Text(
                                                                text = "${ach.currentProgress} / ${ach.targetProgress}",
                                                                color = Color.White,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                // Rewards & Completed / Claim action
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
                                                            modifier = Modifier.height(26.dp)
                                                        ) {
                                                            Text("Claim 🎁", color = Color(0xFF003300), fontSize = 10.sp, fontWeight = FontWeight.Black)
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
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 6.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { onBack() }
                        .testTag("profile_close_button"),
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
    }
}
