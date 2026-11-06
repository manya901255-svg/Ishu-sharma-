package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.MatchHistoryEntity
import com.example.model.LudoColor
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MatchHistoryDialog: Displays summaries of the last 10 matches played with final leaderboard rankings.
 */
@Composable
fun MatchHistoryDialog(
    matches: List<MatchHistoryEntity>,
    onDismiss: () -> Unit
) {
    val totalMatches = matches.size
    val totalWins = matches.count { it.isWin }
    val winRate = if (totalMatches > 0) (totalWins * 100 / totalMatches) else 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .shadow(24.dp, RoundedCornerShape(22.dp))
                .testTag("match_history_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalNavyDark),
            border = androidx.compose.foundation.BorderStroke(
                2.5.dp,
                Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📜", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ROYAL MATCH HISTORY",
                                color = RoyalGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Summaries of your last 10 court matches",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .testTag("match_history_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats Summary Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0F265C), Color(0xFF1B3D8A), Color(0xFF0F265C))
                            )
                        )
                        .border(1.dp, RoyalGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Logged", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Text("$totalMatches Matches", color = RoyalGoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Victories", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Text("$totalWins Wins 🏆", color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Win Rate", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                        Text("$winRate%", color = RoyalGold, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Match List
                if (matches.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F1E40).copy(alpha = 0.6f))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎲", fontSize = 38.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Match History Yet",
                                color = RoyalGoldLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Complete a match in vs AI or Multiplayer to record your first game!",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(matches, key = { it.id }) { match ->
                            MatchSummaryCard(match = match)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchSummaryCard(match: MatchHistoryEntity) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(match.timestamp) { dateFormat.format(Date(match.timestamp)) }
    val positions = remember(match.leaderboardSummary) { match.parseLeaderboardPositions() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .testTag("match_card_${match.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102554)),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (match.isWin) RoyalGold.copy(alpha = 0.8f) else Color(0xFF264484)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Card Top Row: Mode badge, Date, and Outcome/Coins badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF193B7D))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = match.gameMode,
                            color = RoyalGoldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formattedDate,
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 10.sp
                    )
                }

                // Outcome Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (match.userRank) {
                                    1 -> RoyalGold.copy(alpha = 0.2f)
                                    2 -> Color(0xFF90A4AE).copy(alpha = 0.2f)
                                    3 -> Color(0xFFB08D57).copy(alpha = 0.2f)
                                    else -> RubyRed.copy(alpha = 0.2f)
                                }
                            )
                            .border(
                                1.dp,
                                when (match.userRank) {
                                    1 -> RoyalGold
                                    2 -> Color(0xFFB0BEC5)
                                    3 -> Color(0xFFCD7F32)
                                    else -> RubyRed
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (match.userRank) {
                                1 -> "🏆 1ST PLACE"
                                2 -> "🥈 2ND PLACE"
                                3 -> "🥉 3RD PLACE"
                                else -> "❌ DEFEAT"
                            },
                            color = when (match.userRank) {
                                1 -> RoyalGold
                                2 -> Color(0xFFECEFF1)
                                3 -> Color(0xFFFFCC80)
                                else -> RubyRed
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Coins won / lost
                    Text(
                        text = if (match.coinsWon >= 0) "+${match.coinsWon} 🪙" else "${match.coinsWon} 🪙",
                        color = if (match.coinsWon >= 0) EmeraldGreen else Color(0xFFEF5350),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Final Leaderboard Positions Section
            Text(
                text = "FINAL LEADERBOARD POSITIONS",
                color = RoyalGoldLight,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF091736))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (positions.isNotEmpty()) {
                    positions.forEach { pos ->
                        LeaderboardRow(pos = pos)
                    }
                } else {
                    // Fallback winner view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👑 Winner: ${match.winnerName} (${match.winnerColor})", color = RoyalGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Match Duration & Wager footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⏱️ Duration: ${match.durationSeconds / 60}m ${match.durationSeconds % 60}s",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 9.sp
                )
                Text(
                    text = "Wager: ${match.wager} Coins",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(pos: com.example.data.LeaderboardPositionItem) {
    val playerColor = when (pos.colorName.lowercase()) {
        "red" -> Color(0xFFE53935)
        "green" -> Color(0xFF43A047)
        "blue" -> Color(0xFF1E88E5)
        "yellow" -> Color(0xFFFFC107)
        else -> RoyalGold
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when (pos.rank) {
                    1 -> "👑 1st"
                    2 -> "🥈 2nd"
                    3 -> "🥉 3rd"
                    else -> "🎖️ 4th"
                },
                color = when (pos.rank) {
                    1 -> RoyalGold
                    2 -> Color(0xFFECEFF1)
                    3 -> Color(0xFFFFCC80)
                    else -> Color.White.copy(alpha = 0.6f)
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))

            // Color circle dot
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(playerColor)
            )
            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = pos.playerName,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = if (pos.rank == 1) FontWeight.Bold else FontWeight.Normal
            )
        }

        Text(
            text = "${pos.pawnsHome}/4 Home",
            color = if (pos.pawnsHome == 4) EmeraldGreen else Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
