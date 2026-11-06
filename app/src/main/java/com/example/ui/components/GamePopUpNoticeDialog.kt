package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.example.model.GamePopUpNotice
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * GamePopUpNoticeDialog: Royal modal pop-up displayed for all game notifications
 * (friend coming online with green tick, key section updates, daily chest rewards, etc.)
 */
@Composable
fun GamePopUpNoticeDialog(
    notice: GamePopUpNotice?,
    onActionClick: () -> Unit = {},
    onDismiss: () -> Unit
) {
    if (notice == null) return

    // Auto-dismiss after 4.5 seconds if user hasn't clicked
    LaunchedEffect(notice.id) {
        delay(4500)
        onDismiss()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "popUpGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .shadow(28.dp, RoundedCornerShape(24.dp))
                .scale(pulseScale)
                .testTag("game_popup_notice_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF007132B)),
            border = androidx.compose.foundation.BorderStroke(
                2.5.dp,
                Brush.linearGradient(
                    if (notice.type == "FRIEND_ONLINE") {
                        listOf(Color(0xFF00E676), RoyalGold, Color(0xFF00C853))
                    } else {
                        listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark)
                    }
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top header bar with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (notice.type == "FRIEND_ONLINE") Color(0xFF1B5E20)
                                else Color(0xFF1E3A6E)
                            )
                            .border(
                                1.dp,
                                if (notice.type == "FRIEND_ONLINE") Color(0xFF00E676)
                                else RoyalGold,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (notice.type == "FRIEND_ONLINE") {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E676))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ONLINE ✓", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            } else {
                                Text("ROYAL NOTICE", color = RoyalGoldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .testTag("dismiss_popup_notice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Big Centered Animated Icon with Green Tick Badge if Friend Online
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    if (notice.type == "FRIEND_ONLINE") {
                                        listOf(Color(0xFF1B5E20), Color(0xFF0A2E10))
                                    } else {
                                        listOf(RoyalGoldLight, RoyalGoldDark)
                                    }
                                )
                            )
                            .border(
                                2.dp,
                                if (notice.type == "FRIEND_ONLINE") Color(0xFF00E676) else Color.White,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = notice.icon, fontSize = 32.sp)
                    }

                    // Green tick emblem if friend online
                    if (notice.type == "FRIEND_ONLINE") {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C853))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Online tick",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = notice.title,
                    color = RoyalGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Message Text
                Text(
                    text = notice.message,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                if (notice.type == "FRIEND_ONLINE") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("ignore_online_friend_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text(
                                text = "Ignore",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = {
                                onActionClick()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("invite_friend_to_game_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Invite to Game ⚔️",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            onActionClick()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("confirm_popup_notice_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when (notice.type) {
                                "KEY" -> "CHECK VAULT 🔑"
                                "CHEST" -> "OPEN REWARDS 🎁"
                                "SEASON_PASS" -> "VIEW PASS 👑"
                                else -> "OK ✓"
                            },
                            color = RoyalNavyDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
