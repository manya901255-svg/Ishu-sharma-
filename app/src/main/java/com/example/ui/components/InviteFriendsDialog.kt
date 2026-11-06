package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.util.SocialInviteUtils

@Composable
fun InviteFriendsDialog(
    roomCode: String,
    onRewardEarned: (coins: Long, gems: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var copiedFeedback by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .shadow(20.dp, RoundedCornerShape(24.dp))
                .testTag("invite_friends_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalNavyDark),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎁", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Invite Friends",
                                color = RoyalGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Play & Earn Royal Rewards",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reward Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF6A1B9A), Color(0xFF283593))
                            )
                        )
                        .border(1.dp, RoyalGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INVITE REWARD",
                                color = RoyalGoldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "+5,000 Coins & +20 Gems",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "for each friend who joins your match!",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Text("🪙💎", fontSize = 26.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Room / Invite Code Box
                Text(
                    text = "YOUR MATCH & INVITE CODE",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RoyalBlueSurface)
                        .border(1.5.dp, RoyalGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = roomCode,
                            color = RoyalGoldLight,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "mgludo.app/play?room=$roomCode",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }

                    Button(
                        onClick = {
                            SocialInviteUtils.copyToClipboard(context, SocialInviteUtils.getInviteMessage(roomCode))
                            copiedFeedback = true
                            onRewardEarned(1000, 5)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = RoyalNavyDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (copiedFeedback) "COPIED!" else "COPY",
                            color = RoyalNavyDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                AnimatedVisibility(
                    visible = copiedFeedback,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "✓ Invite message copied! Ready to paste anywhere.",
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "SHARE DIRECTLY ON SOCIAL APPS",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 1. WhatsApp Button
                SocialPlatformButton(
                    title = "WhatsApp",
                    subtitle = "Share to WhatsApp chats & status",
                    iconComposable = { WhatsAppIcon() },
                    buttonBrush = Brush.horizontalGradient(
                        listOf(Color(0xFF25D366), Color(0xFF128C7E))
                    ),
                    testTag = "invite_whatsapp_button",
                    onClick = {
                        SocialInviteUtils.shareToWhatsApp(context, roomCode)
                        onRewardEarned(2500, 10)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Instagram Button
                SocialPlatformButton(
                    title = "Instagram",
                    subtitle = "Share in Direct Messages or Story",
                    iconComposable = { InstagramIcon() },
                    buttonBrush = Brush.horizontalGradient(
                        listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCAF45))
                    ),
                    testTag = "invite_instagram_button",
                    onClick = {
                        SocialInviteUtils.shareToInstagram(context, roomCode)
                        onRewardEarned(2500, 10)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Facebook Button
                SocialPlatformButton(
                    title = "Facebook",
                    subtitle = "Share on feed, groups & Messenger",
                    iconComposable = { FacebookIcon() },
                    buttonBrush = Brush.horizontalGradient(
                        listOf(Color(0xFF1877F2), Color(0xFF0D47A1))
                    ),
                    testTag = "invite_facebook_button",
                    onClick = {
                        SocialInviteUtils.shareToFacebook(context, roomCode)
                        onRewardEarned(2500, 10)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. More Options (System Chooser)
                OutlinedButton(
                    onClick = {
                        SocialInviteUtils.shareGeneral(context, roomCode)
                        onRewardEarned(1000, 5)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("invite_more_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "More Share Options (SMS, Telegram, Discord...)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom footnote
                Text(
                    text = "🔒 Friends can join instantly by clicking your link or entering room code in Private Room.",
                    color = TextMuted.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SocialPlatformButton(
    title: String,
    subtitle: String,
    iconComposable: @Composable () -> Unit,
    buttonBrush: Brush,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(buttonBrush)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        iconComposable()
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp
                        )
                    }
                }

                // Action Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "INVITE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Authentic WhatsApp vector icon drawn with Canvas
 */
@Composable
fun WhatsAppIcon(modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer speech bubble
        val path = Path().apply {
            addOval(androidx.compose.ui.geometry.Rect(w * 0.08f, h * 0.08f, w * 0.92f, h * 0.92f))
            moveTo(w * 0.22f, h * 0.78f)
            lineTo(w * 0.12f, h * 0.94f)
            lineTo(w * 0.32f, h * 0.88f)
        }
        drawPath(path, Color.White)

        // Inside receiver silhouette
        drawCircle(
            color = Color(0xFF25D366),
            radius = w * 0.34f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
        // Little white telephone
        drawArc(
            color = Color.White,
            startAngle = 135f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.34f, h * 0.34f),
            size = Size(w * 0.32f, h * 0.32f),
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}

/**
 * Authentic Instagram camera glyph drawn with Canvas
 */
@Composable
fun InstagramIcon(modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.2.dp.toPx()

        // Outer rounded square camera body
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.12f, h * 0.12f),
            size = Size(w * 0.76f, h * 0.76f),
            cornerRadius = CornerRadius(w * 0.22f, h * 0.22f),
            style = Stroke(width = strokeWidth)
        )

        // Center lens circle
        drawCircle(
            color = Color.White,
            radius = w * 0.19f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = strokeWidth)
        )

        // Top right flash dot
        drawCircle(
            color = Color.White,
            radius = w * 0.055f,
            center = Offset(w * 0.69f, h * 0.31f),
            style = Fill
        )
    }
}

/**
 * Authentic Facebook "f" drawn with Canvas
 */
@Composable
fun FacebookIcon(modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Circle
        drawCircle(color = Color.White, radius = w * 0.44f, center = Offset(w * 0.5f, h * 0.5f))

        // Blue "f" letter
        val fPath = Path().apply {
            moveTo(w * 0.58f, h * 0.90f)
            lineTo(w * 0.46f, h * 0.90f)
            lineTo(w * 0.46f, h * 0.54f)
            lineTo(w * 0.36f, h * 0.54f)
            lineTo(w * 0.36f, h * 0.42f)
            lineTo(w * 0.46f, h * 0.42f)
            lineTo(w * 0.46f, h * 0.32f)
            // top curve
            cubicTo(w * 0.46f, h * 0.20f, w * 0.54f, h * 0.18f, w * 0.64f, h * 0.18f)
            lineTo(w * 0.64f, h * 0.30f)
            lineTo(w * 0.56f, h * 0.30f)
            cubicTo(w * 0.53f, h * 0.30f, w * 0.53f, h * 0.34f, w * 0.53f, h * 0.38f)
            lineTo(w * 0.53f, h * 0.42f)
            lineTo(w * 0.64f, h * 0.42f)
            lineTo(w * 0.62f, h * 0.54f)
            lineTo(w * 0.53f, h * 0.54f)
            lineTo(w * 0.53f, h * 0.90f)
            close()
        }
        drawPath(fPath, Color(0xFF1877F2), style = Fill)
    }
}
