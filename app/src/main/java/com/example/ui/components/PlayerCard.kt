package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight

/**
 * Reusable Player Status Card for matches and lobbies.
 * Displays player avatar, crown, name, coins, level, and active turn indicator.
 */
@Composable
fun PlayerCard(
    player: Player,
    isActiveTurn: Boolean,
    isHuman: Boolean,
    modifier: Modifier = Modifier,
    coins: Long? = null,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "player_turn_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActiveTurn) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .scale(pulseScale)
            .shadow(if (isActiveTurn) 8.dp else 3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    if (isActiveTurn) listOf(Color(0xFF0F2653), Color(0xFF1E3A70))
                    else listOf(Color(0xFF0A152E), Color(0xFF0F1E3D))
                )
            )
            .border(
                width = if (isActiveTurn) 2.dp else 1.dp,
                color = if (isActiveTurn) RoyalGold else player.color.primary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("player_card_${player.id}"),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Avatar with Crown
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(player.color.light, player.color.primary)
                        )
                    )
                    .border(1.5.dp, if (isActiveTurn) RoyalGoldLight else Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isHuman) "👑" else "🤖",
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isHuman) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF00E676))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("YOU", color = Color(0xFF003300), fontSize = 7.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                if (coins != null) {
                    Text(
                        text = "🪙 ${formatCompactCoins(coins)}",
                        color = RoyalGoldLight,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "${player.color.title} • ${player.title}",
                        color = Color(0xFFB0BEC5),
                        fontSize = 8.5.sp
                    )
                }
            }

            // Pawns Finished counter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x33000000))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${player.finishedPawnsCount}/4 🏁",
                    color = if (player.finishedPawnsCount > 0) Color(0xFF00E676) else Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
