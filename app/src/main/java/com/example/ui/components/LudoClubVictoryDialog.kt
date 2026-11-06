package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Player
import com.example.ui.theme.*

/**
 * Ludo Club Match Victory & Celebration Dialog.
 * Shows tournament champion, 2x coins pot payout, winner spotlight, and rematch options.
 */
@Composable
fun LudoClubVictoryDialog(
    winner: Player,
    players: List<Player>,
    userPlayer: Player?,
    wager: Long,
    totalPot: Long,
    onPlayAgain: () -> Unit,
    onReturnHome: () -> Unit
) {
    val isHumanWin = userPlayer != null && winner.id == userPlayer.id

    val infiniteTransition = rememberInfiniteTransition(label = "trophy_bounce")
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF2050E22)),
            border = androidx.compose.foundation.BorderStroke(
                3.dp,
                Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("ludo_club_victory_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (isHumanWin) listOf(Color(0xFFFF8F00), Color(0xFFFFB300), Color(0xFFFF8F00))
                                else listOf(Color(0xFFB71C1C), Color(0xFFE53935), Color(0xFFB71C1C))
                            )
                        )
                        .border(1.5.dp, RoyalGoldLight, RoundedCornerShape(16.dp))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isHumanWin) "🎉 MATCH CHAMPION! 🎉" else "⚔️ MATCH CONCLUDED ⚔️",
                        fontFamily = CinzelFontFamily,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Trophy Icon with scaling animation
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(trophyScale)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(RoyalGoldLight, RoyalGoldDark)))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isHumanWin) "🏆" else "🎖️", fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isHumanWin) "You Won The Royal Match!" else "${winner.name} won the match!",
                    fontFamily = CinzelFontFamily,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Prize Payout Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F2653))
                        .border(1.dp, Color(0xFF28549C), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isHumanWin) "2X WINNING POT AWARDED" else "MATCH WAGER",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isHumanWin) "+🪙 ${formatCompactCoins(totalPot)}" else "-🪙 ${formatCompactCoins(wager)}",
                            color = if (isHumanWin) Color(0xFF00E676) else RubyRed,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: Play Again & Return Home
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReturnHome,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("victory_home_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF64B5F6))
                    ) {
                        Text("Home", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("victory_play_again_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Play Again", color = Color(0xFF040A18), fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
