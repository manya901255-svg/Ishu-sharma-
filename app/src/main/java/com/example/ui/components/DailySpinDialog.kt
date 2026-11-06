package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.random.Random

data class SpinReward(
    val label: String,
    val coins: Long,
    val gems: Int,
    val color: Color
)

val WHEEL_REWARDS = listOf(
    SpinReward("1,000 🪙", 1000, 0, RubyRed),
    SpinReward("5 💎", 0, 5, SapphireBlue),
    SpinReward("2,500 🪙", 2500, 0, EmeraldGreen),
    SpinReward("10 💎", 0, 10, RoyalAmber),
    SpinReward("500 🪙", 500, 0, TopazYellow),
    SpinReward("5,000 💰", 5000, 0, RubyRedDark),
    SpinReward("25 💎", 0, 25, SapphireBlueDark),
    SpinReward("10K 👑", 10000, 50, RoyalGoldDark)
)

@Composable
fun DailySpinDialog(
    lastSpinTimestampMillis: Long = 0L,
    onClaimReward: (Long, Int) -> Unit,
    onWatchAdForSpin: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var isSpinning by remember { mutableStateOf(false) }
    var selectedReward by remember { mutableStateOf<SpinReward?>(null) }
    val rotationAngle = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val cooldownDuration = 24 * 60 * 60 * 1000L
    val now = System.currentTimeMillis()
    val isFreeSpinAvailable = (now - lastSpinTimestampMillis) >= cooldownDuration || lastSpinTimestampMillis == 0L
    val remainingMillis = if (isFreeSpinAvailable) 0L else (cooldownDuration - (now - lastSpinTimestampMillis))
    val hoursLeft = remainingMillis / (1000 * 60 * 60)
    val minutesLeft = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60)

    Dialog(onDismissRequest = { if (!isSpinning) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(20.dp, RoundedCornerShape(24.dp))
                .testTag("daily_spin_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalNavyDark),
            border = androidx.compose.foundation.BorderStroke(
                3.dp,
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
                    Text(
                        text = "🎡 Maharaja Fortune Wheel",
                        color = RoyalGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isSpinning) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Text(
                    text = "Daily Royal Bounty • Spin to claim treasury gold & gems!",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Wheel Container with Pointer
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Rotating Wheel
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotationAngle.value)
                            .clip(CircleShape)
                            .border(4.dp, RoyalGold, CircleShape)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val sweepAngle = 360f / WHEEL_REWARDS.size
                            WHEEL_REWARDS.forEachIndexed { i, reward ->
                                drawArc(
                                    color = reward.color,
                                    startAngle = i * sweepAngle - 90f,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    size = size
                                )
                                // Divider line
                                val angleRad = Math.toRadians((i * sweepAngle - 90).toDouble())
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val ex = cx + (cx * Math.cos(angleRad)).toFloat()
                                val ey = cy + (cy * Math.sin(angleRad)).toFloat()
                                drawLine(
                                    color = RoyalGoldLight,
                                    start = Offset(cx, cy),
                                    end = Offset(ex, ey),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }

                        // Wheel Sector Labels (Shows rewards clearly on the wheel slices)
                        val sectorAngle = 360f / WHEEL_REWARDS.size
                        WHEEL_REWARDS.forEachIndexed { i, reward ->
                            val midAngle = i * sectorAngle + sectorAngle / 2f
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(midAngle),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Text(
                                    text = reward.label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    style = androidx.compose.ui.text.TextStyle(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.Black,
                                            blurRadius = 6f
                                        )
                                    ),
                                    modifier = Modifier.padding(top = 18.dp)
                                )
                            }
                        }

                        // Center Golden Crest
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(RoyalGoldLight, RoyalGoldDark)))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👑", fontSize = 24.sp)
                        }
                    }

                    // Golden Pointer Needle at Top
                    Canvas(
                        modifier = Modifier
                            .size(28.dp, 36.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (-6).dp)
                    ) {
                        val path = Path().apply {
                            moveTo(size.width / 2f, size.height) // tip pointing down
                            lineTo(0f, 0f)
                            lineTo(size.width, 0f)
                            close()
                        }
                        drawPath(path, Brush.verticalGradient(listOf(RoyalGoldLight, RoyalGoldDark)), style = Fill)
                        drawPath(path, Color.White, style = Stroke(width = 1.5.dp.toPx()))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedReward != null) {
                    Text(
                        text = "🎉 You Won: ${selectedReward!!.label}!",
                        color = RoyalGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onClaimReward(selectedReward!!.coins, selectedReward!!.gems)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("claim_spin_reward_button")
                    ) {
                        Text("Claim & Add to Treasury", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else if (isFreeSpinAvailable) {
                    Button(
                        onClick = {
                            if (!isSpinning) {
                                isSpinning = true
                                coroutineScope.launch {
                                    val winningIndex = Random.nextInt(WHEEL_REWARDS.size)
                                    val baseSpins = 5 * 360f
                                    val sectorAngle = 360f / WHEEL_REWARDS.size
                                    val targetAngle = baseSpins + (360f - (winningIndex * sectorAngle)) + (sectorAngle / 2f)
                                    
                                    rotationAngle.animateTo(
                                        targetValue = targetAngle,
                                        animationSpec = tween(
                                            durationMillis = 3500,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                    selectedReward = WHEEL_REWARDS[winningIndex]
                                    isSpinning = false
                                }
                            }
                        },
                        enabled = !isSpinning,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("spin_wheel_button")
                    ) {
                        Text(
                            text = if (isSpinning) "Spinning Royal Wheel..." else "SPIN FOR FREE",
                            color = RoyalNavyDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    // Cooldown active
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⏳ Daily Bonus Claimed! Next Free Spin in ${hoursLeft}h ${minutesLeft}m",
                            color = RoyalGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onWatchAdForSpin()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("ad_extra_spin_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎬", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Watch Ad for Instant Spin!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
