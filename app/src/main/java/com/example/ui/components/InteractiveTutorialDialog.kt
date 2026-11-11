package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class TutorialPage(
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val points: List<String>,
    val proTip: String
)

val TUTORIAL_PAGES = listOf(
    TutorialPage(
        title = "Board & Safe Star Zones",
        subtitle = "Master the 52-tile royal battlefield",
        iconEmoji = "⭐",
        points = listOf(
            "The Ludo board consists of 52 track tiles plus 4 colored home stretches.",
            "Star squares (tiles 1, 9, 14, 22, 27, 35, 40, 48) are Royal Safe Zones.",
            "Pawns on safe star squares can NEVER be captured by opponents.",
            "Your pawns can safely stack and wait on star tiles for tactical strikes."
        ),
        proTip = "Pro Tip: Always position your tokens on star tiles when enemy pawns are trailing 1 to 6 steps behind!"
    ),
    TutorialPage(
        title = "Rolling & Launching Pawns",
        subtitle = "Rules of the Royal Jaipur Dice",
        iconEmoji = "🎲",
        points = listOf(
            "Roll a 6 to deploy a locked pawn from your base corner onto the track.",
            "Rolling a 6 always awards a BONUS EXTRA ROLL immediately!",
            "Caution: Rolling three consecutive 6s forfeits your turn as a royal penalty.",
            "You can choose which eligible pawn to move after rolling your dice."
        ),
        proTip = "Pro Tip: If you have active pawns on the board, consider whether deploying a new pawn or advancing is safer."
    ),
    TutorialPage(
        title = "Combat & Royal Captures",
        subtitle = "Knockout enemies and conquer the realm",
        iconEmoji = "⚔️",
        points = listOf(
            "Landing directly on an enemy pawn on an unshaded tile CAPTURES it!",
            "Captured pawns are sent back to the enemy's starting base corner.",
            "Every successful capture grants you an immediate BONUS ROLL!",
            "In Team 2 vs 2 mode, you never capture teammates—you protect each other!"
        ),
        proTip = "Pro Tip: Chasing enemy pawns for captures is key to gaining momentum and bonus rolls."
    ),
    TutorialPage(
        title = "Palace Finish & Game Modes",
        subtitle = "Nine unique ways to play and win",
        iconEmoji = "👑",
        points = listOf(
            "Classic Mode: First player to home all 4 pawns wins the match.",
            "Quick Rush: Accelerated blitz mode! 1 or 2 pawns home to win immediately.",
            "Team 2 vs 2: Pair up with a partner! Team with 4 combined home pawns wins.",
            "Practice Mode: Solo academy with tactical hints & unlimited Undo moves.",
            "Online & Private Room: Play with friends anywhere with private room codes."
        ),
        proTip = "Pro Tip: Try Quick Rush when you want an instant 3-minute game, or Team 2vs2 for strategic teamwork!"
    ),
    TutorialPage(
        title = "Customization & Indian Music",
        subtitle = "Personalize every corner of MG Ludo",
        iconEmoji = "🎨",
        points = listOf(
            "Custom Wallpapers: Set custom backgrounds for Home, Board, Lobby, and Profile.",
            "Upload from Gallery: Use your own photos with crop, zoom, and live preview.",
            "Dice & Token Skins: Equip Imperial Gold, Ruby Crystal, and Lion Shields.",
            "44 Indian Melodies: Enjoy authentic classical, sitar, and Bollywood playlists."
        ),
        proTip = "Pro Tip: Open Settings > Customize Wallpaper anytime to tailor your board!"
    )
)

@Composable
fun InteractiveTutorialDialog(
    onDismiss: () -> Unit
) {
    var currentPageIndex by remember { mutableIntStateOf(0) }
    val page = TUTORIAL_PAGES[currentPageIndex]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("interactive_tutorial_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalNavyDark),
            border = androidx.compose.foundation.BorderStroke(
                2.5.dp,
                Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📖", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "ROYAL LUDO ACADEMY",
                                color = RoyalGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "How to Play & Master MG Ludo",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TUTORIAL_PAGES.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (index == currentPageIndex) 22.dp else 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentPageIndex) RoyalGold else Color.White.copy(alpha = 0.25f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Badge
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(Color(0xFF2A4B8D), Color(0xFF102754)))
                            )
                            .border(2.dp, RoyalGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(page.iconEmoji, fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = page.title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = page.subtitle,
                        color = RoyalGold,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rules checklist cards
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        page.points.forEach { point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF142952))
                                    .border(1.dp, Color(0xFF254682), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("⚜️", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = point,
                                    color = Color(0xFFECEFF1),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pro tip banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFB78103).copy(alpha = 0.25f), Color(0xFFE5A93C).copy(alpha = 0.25f))
                                )
                            )
                            .border(1.2.dp, RoyalGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = page.proTip,
                            color = RoyalGoldLight,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPageIndex > 0) {
                        OutlinedButton(
                            onClick = { currentPageIndex-- },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalGold)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentPageIndex < TUTORIAL_PAGES.lastIndex) {
                        Button(
                            onClick = { currentPageIndex++ },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGold)
                        ) {
                            Text("Next Lesson", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGold)
                        ) {
                            Text("Enter Battlefield! ⚔️", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
