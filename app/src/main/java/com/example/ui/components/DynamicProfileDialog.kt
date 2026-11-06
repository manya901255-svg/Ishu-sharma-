package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ANCIENT_REGAL_AVATARS
import com.example.model.RegalAvatar
import com.example.ui.theme.*

/**
 * Dynamic Profile System Dialog:
 * Players can select regal, ancient-kingdom-themed avatars that appear next to their
 * names during voice chat and game sessions.
 */
@Composable
fun DynamicProfileDialog(
    currentAvatarId: String,
    currentTitle: String,
    playerName: String,
    onAvatarSelected: (avatarId: String, title: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAvatar by remember {
        mutableStateOf(
            ANCIENT_REGAL_AVATARS.firstOrNull { it.id == currentAvatarId } ?: ANCIENT_REGAL_AVATARS[0]
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .testTag("dynamic_profile_dialog"),
            color = Color(0xFF071B3B)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0C2450), Color(0xFF071B3B), Color(0xFF040F22))
                        )
                    )
                    .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "👑 ROYAL CITADEL AVATARS",
                            color = RoyalGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Ancient-Kingdom Dynamic Profile System",
                            color = Color(0xFF90CAF9),
                            fontSize = 10.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E3A70))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active Equipped Avatar Showcase Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F3B7A), Color(0xFF08224D))
                            )
                        )
                        .border(1.5.dp, selectedAvatar.crestColor, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Big Avatar Preview with glowing ring
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(selectedAvatar.crestColor.copy(alpha = 0.25f))
                                    .border(2.dp, selectedAvatar.crestColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedAvatar.emoji,
                                    fontSize = 34.sp
                                )
                            }
                            // Speaking Indicator Icon
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E5FF))
                                    .border(1.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎙️", fontSize = 9.sp)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${selectedAvatar.title} $playerName",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(selectedAvatar.emoji, fontSize = 13.sp)
                            }
                            Text(
                                text = "🏛️ ${selectedAvatar.kingdom}",
                                color = selectedAvatar.crestColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedAvatar.description,
                                color = Color(0xFFCFD8DC),
                                fontSize = 10.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "SELECT ANCIENT KINGDOM SOVEREIGN",
                    color = Color(0xFFFFD54F),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable List of Ancient-Kingdom Avatars
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ANCIENT_REGAL_AVATARS) { avatar ->
                        val isSelected = avatar.id == selectedAvatar.id
                        val isEquipped = avatar.id == currentAvatarId

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Color(0xFF143A70) else Color(0xFF092044)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF1E3A70),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedAvatar = avatar }
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Avatar circle
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(avatar.crestColor.copy(alpha = 0.2f))
                                            .border(1.5.dp, avatar.crestColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = avatar.emoji, fontSize = 22.sp)
                                    }

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${avatar.title} ${avatar.name}",
                                                color = Color.White,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isEquipped) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFF00C853))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text("ACTIVE", color = Color.White, fontSize = 7.5.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                        Text(
                                            text = avatar.kingdom,
                                            color = avatar.crestColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = avatar.description,
                                            color = Color(0xFFB0BEC5),
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Check indicator
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD54F)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color(0xFF071B3B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Equip / Confirm Button
                Button(
                    onClick = {
                        onAvatarSelected(selectedAvatar.id, selectedAvatar.title)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("equip_ancient_avatar_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "EQUIP ${selectedAvatar.name.uppercase()} AVATAR",
                        color = Color(0xFF3E2723),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
