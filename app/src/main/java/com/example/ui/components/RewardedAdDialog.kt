package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Rewarded Video Ad Simulator Dialog.
 * Plays a realistic 5-second sponsored rewarded ad before granting the requested reward (e.g. 1,000 Coins).
 */
@Composable
fun RewardedAdDialog(
    rewardTitle: String = "1,000 Coins",
    onRewardEarned: () -> Unit,
    onDismiss: () -> Unit,
    onAdSkippedOrFailed: () -> Unit = {}
) {
    var secondsRemaining by remember { mutableIntStateOf(5) }
    var isAdCompleted by remember { mutableStateOf(false) }
    var showPrematureCloseWarning by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "ad_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ad_wave"
    )

    // Countdown ticker
    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
        isAdCompleted = true
    }

    Dialog(
        onDismissRequest = {
            if (isAdCompleted) {
                onDismiss()
            } else {
                showPrematureCloseWarning = true
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .shadow(24.dp, RoundedCornerShape(22.dp))
                .testTag("rewarded_ad_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D1A)),
            border = androidx.compose.foundation.BorderStroke(
                2.5.dp,
                Brush.linearGradient(listOf(RoyalGold, Color(0xFFFFA000), RoyalGoldDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Ad Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RoyalGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("AD", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Royal Game Network",
                            color = Color(0xFF90CAF9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Countdown / Close
                    if (!isAdCompleted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x55000000))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Reward in ${secondsRemaining}s",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                onRewardEarned()
                                onDismiss()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Ad", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Simulated Video Player Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0D2248), Color(0xFF061430), Color(0xFF030A17))
                            )
                        )
                        .border(1.5.dp, Color(0xFF1E88E5), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Simulated animated sound/video waves
                        val waveCount = 12
                        val barWidth = width / (waveCount * 2f)
                        for (i in 0 until waveCount) {
                            val barHeight = (height * 0.35f) * (0.3f + 0.7f * ((i % 4 + 1) / 4f) * pulseAlpha)
                            val x = (i * 2 + 1) * barWidth
                            drawRect(
                                color = Color(0xFFFFD54F).copy(alpha = 0.7f),
                                topLeft = Offset(x, height / 2f - barHeight / 2f),
                                size = androidx.compose.ui.geometry.Size(barWidth * 0.8f, barHeight)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎬", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isAdCompleted) "✓ Video Finished!" else "Playing Video Ad...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Watch till end to receive $rewardTitle",
                            color = RoyalGoldLight,
                            fontSize = 11.sp
                        )
                    }

                    // Progress line at bottom of video
                    val progressFraction = (5 - secondsRemaining) / 5f
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(progressFraction)
                            .height(4.dp)
                            .background(Color(0xFF00E676))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Area
                if (isAdCompleted) {
                    Text(
                        text = "🎉 Ad Completed Successfully!",
                        color = Color(0xFF00E676),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onRewardEarned()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("claim_ad_reward_button")
                    ) {
                        Text(
                            text = "CLAIM $rewardTitle ✓",
                            color = Color(0xFF040A18),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Text(
                        text = "Please watch the video (${secondsRemaining}s remaining)",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                // Premature Close Warning Dialog
                if (showPrematureCloseWarning) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF261010)),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⚠️ Close without reward?",
                                color = Color(0xFFFF8A80),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "If you close now, you will not receive $rewardTitle.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { showPrematureCloseWarning = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Text("KEEP WATCHING", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        onAdSkippedOrFailed()
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f).height(34.dp)
                                ) {
                                    Text("DISCARD", fontSize = 10.sp, color = Color(0xFFFF8A80))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
