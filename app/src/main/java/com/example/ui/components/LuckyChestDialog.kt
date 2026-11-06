package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * LuckyChestDialog renders the exact authentic "Lucky Chest" modal shown in Screenshot 2:
 * - Header: "Lucky Chest" in an orange rounded pill with top-right (X) close button
 * - Subtitle: "Unlock the chest and win rewards!"
 * - Bubble tag: "Win up to 500,000 🪙 + 5,000 💵"
 * - Graphic: Chained gold-and-red treasure chest with padlocks
 * - "To Open: [key icon] 0/3" blue progress bar
 * - 3 cards with dashed borders: "Get Keys", "Get Keys", "Get Keys"
 * - "0 keys left"
 * - (i) Info button + large green button: "[key icon] Send & Get Keys"
 * - When keys == 3: opens chest and claims the 500,000 coins + 5,000 cash/gems!
 */
@Composable
fun LuckyChestDialog(
    keysReceivedCount: Int,
    keysSentCount: Int,
    onSendAndGetKeys: () -> Unit,
    onReceiveKeyFromFriend: (String) -> Unit = {},
    onClaimLuckyChestReward: () -> Unit,
    onDismiss: () -> Unit
) {
    val isUnlocked = keysReceivedCount >= 3
    var showInfoExplanation by remember { mutableStateOf(false) }
    var lockedFeedback by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "finger_bounce")
    val fingerBounceOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 10f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "finger_bounce_offset"
    )

    val friendNames = listOf("Prince Aryan", "Maharani Ananya", "Sultan Kabir")

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("lucky_chest_dialog"),
            contentAlignment = Alignment.TopCenter
        ) {
            // Main White / Cream Container Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .shadow(24.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F3E9)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE2D6C0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(18.dp))

                    // Subtitle
                    Text(
                        text = "Unlock the chest and win rewards!",
                        color = Color(0xFF333333),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Speech bubble: "Win up to 500,000 [Coins] + 5,000 [Cash]"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFFB0BEC5), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Win up to",
                                color = Color(0xFF455A64),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪙", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "500,000",
                                    color = Color(0xFF212121),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+", color = Color(0xFF757575), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("💵", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "5,000",
                                    color = Color(0xFF212121),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Graphic: Chained Chest with Padlocks (Clickable to open with finger tap!)
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clickable {
                                if (isUnlocked) {
                                    onClaimLuckyChestReward()
                                } else {
                                    lockedFeedback = "Need 3 keys from friends to open! ($keysReceivedCount/3 collected)"
                                }
                            }
                            .testTag("lucky_chest_box_tap_target"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isUnlocked) "🎁" else "📦",
                            fontSize = 76.sp
                        )
                        if (!isUnlocked) {
                            Text(
                                text = "⛓️🔒⛓️",
                                fontSize = 34.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            Text(
                                text = "✨💎✨",
                                fontSize = 28.sp,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                            // Animated Finger Tap indicator
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = fingerBounceOffset.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("👆", fontSize = 36.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFF9800))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("TAP TO OPEN!", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    if (lockedFeedback != null && !isUnlocked) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lockedFeedback ?: "",
                            color = Color(0xFFD32F2F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar: "To Open: [Key icon] 0/3"
                    Row(
                        modifier = Modifier.fillMaxWidth(0.65f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "To Open:",
                            color = Color(0xFF455A64),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF1976D2), Color(0xFF0D47A1))
                                    )
                                )
                                .border(1.5.dp, Color(0xFF64B5F6), RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔑", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$keysReceivedCount/3",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Cards with dashed border: "Get Keys from Friend"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in 1..3) {
                            val isCollected = i <= keysReceivedCount
                            val friendName = friendNames.getOrElse(i - 1) { "Friend $i" }
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(110.dp)
                                    .clickable {
                                        if (!isCollected) {
                                            onReceiveKeyFromFriend(friendName)
                                        }
                                    }
                                    .testTag("lucky_chest_slot_$i"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCollected) Color(0xFFE8F5E9) else Color(0xFFF0EAE1)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isCollected) Color(0xFF00C853) else Color(0xFFBCAAA4)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (isCollected) {
                                        Text("🔑", fontSize = 28.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Key $i ✓",
                                            color = Color(0xFF2E7D32),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = friendName,
                                            color = Color(0xFF388E3C),
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    } else {
                                        Text("🔑", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Get Key",
                                            color = Color(0xFF8D6E63),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = friendName,
                                            color = Color(0xFF1976D2),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Keys left status
                    val keysLeft = (3 - keysReceivedCount).coerceAtLeast(0)
                    Text(
                        text = if (isUnlocked) "🎉 3/3 Keys collected from friends! Tap chest to open!" else "$keysLeft keys left from friends to unlock",
                        color = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFF5D4037),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Row: (i) Info icon button + large green button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Info Button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1976D2))
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { showInfoExplanation = !showInfoExplanation }
                                .testTag("lucky_chest_info_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("i", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }

                        // Big Action Button
                        if (isUnlocked) {
                            Button(
                                onClick = onClaimLuckyChestReward,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .shadow(8.dp, RoundedCornerShape(16.dp))
                                    .testTag("lucky_chest_open_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                            ) {
                                Text(
                                    text = "🎉 OPEN CHEST REWARDS 🔓",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        } else {
                            Button(
                                onClick = onSendAndGetKeys,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .shadow(8.dp, RoundedCornerShape(16.dp))
                                    .testTag("lucky_chest_send_get_keys_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00C853))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🔑", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Send & Get Keys",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    if (showInfoExplanation) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Exchange keys with friends! When you have 3 keys collected, the Lucky Chest unchains and grants up to 500,000 Coins + 5,000 Cash!",
                            color = Color(0xFF455A64),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                    }
                }
            }

            // Top Header Pill: "Lucky Chest" (Orange / Coral Red pill)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
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
                    text = "Lucky Chest",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Top-right (X) Close Button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 6.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onDismiss() }
                    .testTag("lucky_chest_close_button"),
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
