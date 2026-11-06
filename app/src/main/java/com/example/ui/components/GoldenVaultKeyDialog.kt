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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.util.SocialInviteUtils

data class FriendKeySlot(
    val id: Int,
    val name: String,
    val avatar: String,
    val level: Int
)

val DEFAULT_FRIEND_SLOTS = listOf(
    FriendKeySlot(1, "Prince Aryan", "👑", 21),
    FriendKeySlot(2, "Princess Diya", "👸", 19),
    FriendKeySlot(3, "Commander Kabir", "⚔️", 24)
)

/**
 * GoldenVaultKeyDialog: Friends can send keys to each other.
 * As explicitly required: In the key section, when friends send us the key and that key becomes 3,
 * only then the rewards will open.
 */
@Composable
fun GoldenVaultKeyDialog(
    keysReceivedCount: Int = 1,
    keysSentCount: Int = 1,
    onSendKeyToFriend: (String) -> Unit = {},
    onReceiveKeyFromFriend: (String) -> Unit = {},
    onClaimVaultReward: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var sentKeyNames by remember { mutableStateOf(setOf<String>()) }
    val isVaultUnlocked = keysReceivedCount >= 3

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("golden_vault_key_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalNavyDark),
            border = androidx.compose.foundation.BorderStroke(
                3.dp,
                Brush.linearGradient(
                    if (isVaultUnlocked) listOf(Color(0xFF00E676), RoyalGold, Color(0xFF00C853))
                    else listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark)
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔑", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Royal Key Vault",
                                color = RoyalGold,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Exchange keys with royal friends",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_vault_key_dialog_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Vault Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                if (isVaultUnlocked) listOf(Color(0xFF0A2B18), Color(0xFF06180E))
                                else listOf(Color(0xFF132958), Color(0xFF0C1B3B))
                            )
                        )
                        .border(
                            1.5.dp,
                            if (isVaultUnlocked) Color(0xFF00E676) else Color(0xFF2A5298),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isVaultUnlocked) "🔓 3/3 KEYS COLLECTED — REWARDS UNLOCKED!" else "🔐 VAULT LOCKED ($keysReceivedCount/3 Keys Received)",
                                color = if (isVaultUnlocked) Color(0xFF00E676) else RoyalGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "When friends send us the key and that key becomes 3, only then the rewards will open!",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3 Key Slots Progress with Glow
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (slot in 1..3) {
                                val isCollected = slot <= keysReceivedCount
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCollected) {
                                                    Brush.radialGradient(listOf(Color(0xFF00E676), Color(0xFF1B5E20)))
                                                } else {
                                                    Brush.radialGradient(listOf(Color(0xFF1E3A6D), Color(0xFF0D1D3A)))
                                                }
                                            )
                                            .border(
                                                2.dp,
                                                if (isCollected) Color(0xFF00E676) else Color(0xFF3B68B0),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCollected) {
                                            Text("🔑", fontSize = 24.sp)
                                        } else {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = "Locked Slot",
                                                tint = Color.White.copy(alpha = 0.4f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isCollected) "Key $slot ✓" else "Key $slot",
                                        color = if (isCollected) Color(0xFF00E676) else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive Chest Box (Opens with finger tap when 3/3 keys!)
                        var chestFeedback by remember { mutableStateOf<String?>(null) }
                        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "vault_finger_bounce")
                        val fingerOffset by infiniteTransition.animateFloat(
                            initialValue = -4f,
                            targetValue = 10f,
                            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                animation = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                            ),
                            label = "vault_finger_offset"
                        )

                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF071838).copy(alpha = 0.6f))
                                .border(1.5.dp, if (isVaultUnlocked) Color(0xFF00E676) else Color(0xFF1E3A70), RoundedCornerShape(16.dp))
                                .clickable {
                                    if (isVaultUnlocked) {
                                        onClaimVaultReward()
                                    } else {
                                        chestFeedback = "Chest locked! Need 3 keys from friends to open ($keysReceivedCount/3 received)"
                                    }
                                }
                                .testTag("vault_chest_box_tap_target"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isVaultUnlocked) "🎁" else "📦",
                                fontSize = 54.sp
                            )
                            if (!isVaultUnlocked) {
                                Text(
                                    text = "⛓️🔒⛓️",
                                    fontSize = 22.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .offset(y = fingerOffset.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("👆", fontSize = 28.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFF9800))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("TAP TO OPEN!", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        if (chestFeedback != null && !isVaultUnlocked) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = chestFeedback ?: "",
                                color = Color(0xFFFF5252),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Chest Contents: 🪙 500,000 Coins + 💵 5,000 Cash",
                            color = RoyalGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Friends Key Exchange List
                Text(
                    text = "👥 Send & Receive Keys with Friends:",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                var friendSentCounts by remember { mutableStateOf(mapOf<String, Int>()) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DEFAULT_FRIEND_SLOTS.forEach { friend ->
                        val timesSent = friendSentCounts[friend.name] ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F2044))
                                .border(1.dp, if (timesSent > 0) Color(0xFF00E676) else Color(0xFF1D3B73), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22427C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(friend.avatar, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(friend.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF00E676))
                                        )
                                    }
                                    Text(
                                        text = if (timesSent > 0) "Lvl ${friend.level} • Sent: $timesSent times 🔑" else "Lvl ${friend.level} • Online ✓",
                                        color = if (timesSent > 0) Color(0xFF81C784) else Color(0xFFA5D6A7),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Unlimited sends allowed - button is always clickable
                                Button(
                                    onClick = {
                                        val current = friendSentCounts[friend.name] ?: 0
                                        friendSentCounts = friendSentCounts + (friend.name to current + 1)
                                        onSendKeyToFriend(friend.name)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(32.dp).testTag("send_key_button_${friend.id}")
                                ) {
                                    Text(
                                        text = if (timesSent > 0) "Send Again 🔑" else "Send Key 🔑",
                                        color = RoyalNavyDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                // Friend sends key back action until 3 keys reached
                                if (keysReceivedCount < 3) {
                                    Button(
                                        onClick = { onReceiveKeyFromFriend(friend.name) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(32.dp).testTag("receive_key_button_${friend.id}")
                                    ) {
                                        Text("Get Key 🎁", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Reward Claim Action (Active ONLY when keys == 3)
                if (isVaultUnlocked) {
                    Button(
                        onClick = onClaimVaultReward,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("claim_vault_key_reward_button")
                    ) {
                        Text(
                            text = "🎉 OPEN REWARDS (+🪙 500,000 & +💵 5,000) 🔓",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFF14244D),
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("locked_vault_key_reward_button")
                    ) {
                        Text(
                            text = "LOCKED: $keysReceivedCount/3 KEYS (Need 3 to Open)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
