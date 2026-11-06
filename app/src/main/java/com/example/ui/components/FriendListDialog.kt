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
import androidx.compose.material.icons.filled.*
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
import com.example.model.MOCK_ROYAL_FRIENDS
import com.example.model.RoyalFriend
import com.example.model.getCrownAvatarEmoji
import com.example.ui.theme.*

@Composable
fun FriendListDialog(
    userNickname: String,
    userCrownAvatar: String,
    userTitle: String,
    userCustomPhotoUri: String? = null,
    keysSentCount: Int = 1,
    keysReceivedCount: Int = 1,
    onInviteToMatch: (RoyalFriend) -> Unit,
    onSendKey: (RoyalFriend) -> Unit = {},
    onClaimReceivedKey: (String) -> Unit = {},
    onOpenVault: () -> Unit = {},
    onShareInviteLink: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Online, 1: All Friends, 2: Add Friend
    var searchQuery by remember { mutableStateOf("") }
    var friendsList by remember { mutableStateOf(MOCK_ROYAL_FRIENDS) }
    var invitedFriendIds by remember { mutableStateOf(setOf<String>()) }
    var sentKeyFriendIds by remember { mutableStateOf(setOf<String>()) }
    var newFriendIdInput by remember { mutableStateOf("") }
    var addFriendFeedback by remember { mutableStateOf<String?>(null) }

    val filteredFriends = remember(selectedTab, searchQuery, friendsList) {
        val base = when (selectedTab) {
            0 -> friendsList.filter { it.isOnline }
            1 -> friendsList
            else -> friendsList
        }
        if (searchQuery.isBlank()) {
            base
        } else {
            base.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("friend_list_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalNavyDark),
            border = androidx.compose.foundation.BorderStroke(
                2.5.dp,
                Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👥", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Royal Friends",
                                color = RoyalGold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${friendsList.count { it.isOnline }} Online Kings & Queens",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_friend_list_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // User's own status card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0D2552), Color(0xFF143673))
                            )
                        )
                        .border(1.dp, Color(0xFF2A5298), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(RoyalGoldLight, RoyalGoldDark)))
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!userCustomPhotoUri.isNullOrBlank()) {
                                coil.compose.AsyncImage(
                                    model = userCustomPhotoUri,
                                    contentDescription = "User DP",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Text(getCrownAvatarEmoji(userCrownAvatar), fontSize = 22.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userNickname.ifBlank { "You" },
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RoyalGoldDark)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(userTitle, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E676))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Status: Online in Lobby",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onShareInviteLink,
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("invite_external_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold)
                    ) {
                        Text("Share Link", color = RoyalGoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Golden Vault Key Exchange Progress Card (Requires 3 keys received from friends)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("key_exchange_vault_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2653)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, RoyalGold)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔑", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Keys Received from Friends: $keysReceivedCount/3",
                                        color = if (keysReceivedCount >= 3) Color(0xFF00E676) else RoyalGoldLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = if (keysReceivedCount >= 3) "🔓 Vault Unlocked! Ready to claim" else "🔐 Vault locked until 3 keys received",
                                        color = if (keysReceivedCount >= 3) Color(0xFF00E676) else TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            // 3 Key Progress Badges with Green Ticks when received
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (i in 1..3) {
                                    val isFilled = i <= keysReceivedCount
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(if (isFilled) Color(0xFF00C853) else Color(0xFF0B1736))
                                            .border(1.2.dp, if (isFilled) Color.White else Color(0xFF1E3A6E), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isFilled) "🔑" else "🔒",
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When friends send you keys and they reach 3, only then the rewards will open: +🪙 5,000 Coins & +💎 25 Gems!",
                            color = TextMuted,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Unlock condition: ONLY when keys received becomes 3
                        if (keysReceivedCount >= 3) {
                            Button(
                                onClick = onOpenVault,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .testTag("open_vault_reward_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🎉 3/3 KEYS COLLECTED! OPEN REWARD 🎁", color = RoyalNavyDark, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        disabledContainerColor = Color(0xFF14244D)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("LOCKED: $keysReceivedCount/3 Keys Received 🔒", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onClaimReceivedKey("Prince Aryan") },
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("nudge_receive_key_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Get Friend Key 🔑", color = RoyalNavyDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Tabs (Online, All Friends, Add Friend)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0B1736))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TabButton(
                        label = "Online (${friendsList.count { it.isOnline }})",
                        isSelected = selectedTab == 0,
                        modifier = Modifier.weight(1f),
                        testTag = "tab_online_friends"
                    ) {
                        selectedTab = 0
                    }
                    TabButton(
                        label = "All (${friendsList.size})",
                        isSelected = selectedTab == 1,
                        modifier = Modifier.weight(1f),
                        testTag = "tab_all_friends"
                    ) {
                        selectedTab = 1
                    }
                    TabButton(
                        label = "+ Add",
                        isSelected = selectedTab == 2,
                        modifier = Modifier.weight(0.8f),
                        testTag = "tab_add_friend"
                    ) {
                        selectedTab = 2
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab != 2) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("search_friends_input"),
                        placeholder = { Text("Search friends...", color = TextMuted, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = RoyalGold,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalGold,
                            unfocusedBorderColor = Color(0xFF1E3A6E),
                            focusedContainerColor = Color(0xFF091633),
                            unfocusedContainerColor = Color(0xFF091633),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Friends List
                    if (filteredFriends.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👑", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No royal friends match '$searchQuery'" else "No friends currently in this tab",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredFriends, key = { it.id }) { friend ->
                                val isKeySent = friend.id in sentKeyFriendIds
                                FriendCardItem(
                                    friend = friend,
                                    isInvited = friend.id in invitedFriendIds,
                                    isKeySent = isKeySent,
                                    onInviteClick = {
                                        invitedFriendIds = invitedFriendIds + friend.id
                                        onInviteToMatch(friend)
                                    },
                                    onSendKeyClick = {
                                        sentKeyFriendIds = sentKeyFriendIds + friend.id
                                        onSendKey(friend)
                                    },
                                    onToggleFavorite = {
                                        friendsList = friendsList.map {
                                            if (it.id == friend.id) it.copy(isFavorite = !it.isFavorite) else it
                                        }
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Add Friend Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Add New Royal Rival",
                            color = RoyalGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter your friend's Player ID or Royal Tag (e.g. MG-5821)",
                            color = TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = newFriendIdInput,
                            onValueChange = {
                                newFriendIdInput = it
                                addFriendFeedback = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_friend_id_input"),
                            label = { Text("Player ID or Username", color = RoyalGoldLight) },
                            placeholder = { Text("MG-xxxx or Royal Name", color = TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalGold,
                                unfocusedBorderColor = Color(0xFF1E3A6E),
                                focusedContainerColor = Color(0xFF091633),
                                unfocusedContainerColor = Color(0xFF091633),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (newFriendIdInput.isNotBlank()) {
                                    val newFriend = RoyalFriend(
                                        id = "f_custom_${System.currentTimeMillis()}",
                                        name = newFriendIdInput.trim(),
                                        title = "Yuvraj",
                                        crownAvatarId = "crown_kohinoor_diamond",
                                        isOnline = true,
                                        statusText = "Online • Just Added! 🎲",
                                        level = 15,
                                        coins = 35000L,
                                        wins = 20
                                    )
                                    friendsList = listOf(newFriend) + friendsList
                                    addFriendFeedback = "Added ${newFriend.name} to your Royal Friends list! 👑"
                                    newFriendIdInput = ""
                                    selectedTab = 0
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("submit_add_friend_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Send Royal Friend Request", color = RoyalNavyDark, fontWeight = FontWeight.Bold)
                        }

                        if (addFriendFeedback != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = addFriendFeedback!!,
                                color = Color(0xFF00E676),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick Share Promo
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1B3D)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23447A))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📢 Share MG Ludo with Buddies", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Send an invite link on WhatsApp or Instagram and receive 5,000 Coins bonus when they join!",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onShareInviteLink,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("share_invite_promo_button")
                                ) {
                                    Text("Invite via WhatsApp 📲", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) RoyalGold else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) RoyalNavyDark else Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
        )
    }
}

@Composable
private fun FriendCardItem(
    friend: RoyalFriend,
    isInvited: Boolean,
    isKeySent: Boolean,
    onInviteClick: () -> Unit,
    onSendKeyClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("friend_card_${friend.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (friend.isOnline) Color(0xFF0D2147) else Color(0xFF0A1833)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (friend.isFavorite) RoyalGold else if (friend.isOnline) Color(0xFF1E427B) else Color(0xFF13274A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Avatar + Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Crown Avatar with Online Badge
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(RoyalGoldLight, RoyalGoldDark)
                                )
                            )
                            .border(1.5.dp, if (friend.isOnline) Color(0xFF00E676) else Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getCrownAvatarEmoji(friend.crownAvatarId), fontSize = 24.sp)
                    }

                    // Online indicator badge with green tick
                    if (friend.isOnline) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(17.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C853))
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Online with green tick",
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF78909C))
                                .border(1.dp, Color(0xFF0A1833), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Name, title, level, status
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = friend.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1565C0))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("Lv.${friend.level}", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (friend.isOnline) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1B5E20))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Online ✓", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = friend.statusText,
                            color = if (friend.isOnline) Color(0xFF81C784) else TextMuted,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙 ${formatCompactCoins(friend.coins)}", color = RoyalGoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🏆 ${friend.wins} Wins", color = TextMuted, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right: Key Button + Challenge / Nudge Button + Favorite
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Send Key Button
                if (isKeySent) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1B3D2F))
                            .border(1.dp, Color(0xFF00E676), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text("🔑 Sent ✓", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onSendKeyClick,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("send_key_friend_${friend.id}"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold)
                    ) {
                        Text("🔑 Key", color = RoyalGoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (friend.isOnline) {
                    Button(
                        onClick = onInviteClick,
                        enabled = !isInvited,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("invite_friend_${friend.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isInvited) Color(0xFF2E7D32) else Color(0xFF00C853),
                            disabledContainerColor = Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (isInvited) "Invited ✓" else "Play",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onInviteClick,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("invite_friend_${friend.id}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF455A64))
                    ) {
                        Text(
                            text = if (isInvited) "Sent ✓" else "Nudge",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (friend.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (friend.isFavorite) RoyalGold else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
