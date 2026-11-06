package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ChatMessage
import com.example.model.LudoColor
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Preset Regal-Themed Emojis
val REGAL_THEMED_EMOJIS = listOf(
    "👑", "⚔️", "🦁", "💎",
    "🏆", "🦚", "🪷", "🛡️",
    "🤴", "👸", "🎲", "🔥",
    "🏰", "⚡", "🕊️", "🌟"
)

// Classic expressives
val CLASSIC_EXPRESSIVE_EMOJIS = listOf(
    "😍", "😎", "😜", "😂",
    "😡", "😟", "😭", "🥳"
)

// Preset Regal Quick Responses
val REGAL_QUICK_RESPONSES = listOf(
    "Hail the Maharaja! 👑",
    "Well played, worthy rival! ⚔️",
    "The Throne belongs to me! 🏆",
    "A lucky roll by royal decree! 🎲",
    "Show no mercy on the board! 🦁",
    "Peace between kingdoms! 🕊️",
    "By royal seal, strike! ⚡",
    "Bow before the crown! 👑",
    "A magnificent move! 💎",
    "Fortune favors the bold! 🌟",
    "To the Royal Palace! 🏛️",
    "Until we meet on the board again! 🏰"
)

val STANDARD_QUICK_PHRASES = listOf(
    "Nice move! ⚔️", "Hurry up! ⏳", "Well played! 🏆",
    "Oops! 😅", "Good Luck! 🍀", "Thank you! 🙏",
    "Don't kill please! 🥺", "The Throne is mine! 👑",
    "Haha! 😂", "Lucky roll! 🎲", "Good game! 🤝", "Bye! 👋"
)

/**
 * RoyalChatEmojiDialog: In-game Chat interface supporting:
 * 1. Scrollable message history with auto-scroll to latest.
 * 2. Player identification with individual player color badges (Red, Green, Yellow, Blue).
 * 3. Custom text input with soft-keyboard support, 100 character limit, and Send button.
 * 4. Fast 1-tap quick preset chips row.
 * 5. Regal decrees & animated emojis tab.
 */
@Composable
fun RoyalChatEmojiDialog(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit = {},
    onSendPhrase: (String) -> Unit,
    onSendEmoji: (String) -> Unit,
    currentSenderName: String = "You",
    currentSenderColor: LudoColor = LudoColor.RED,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(0) } // 0: Chat Room, 1: Royal Decrees, 2: Emojis
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Auto-scroll to latest message whenever new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .imePadding()
                .shadow(24.dp, RoundedCornerShape(22.dp))
                .testTag("regal_chat_overlay_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalNavyDark),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header with title, current sender badge, & close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💬", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ROYAL COURT CHAT",
                                color = RoyalGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Current Sender Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(currentSenderColor.primary.copy(alpha = 0.18f))
                                .border(1.dp, currentSenderColor.primary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(currentSenderColor.primary)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Chatting as $currentSenderName",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${currentSenderColor.title.uppercase()})",
                                color = currentSenderColor.primary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .testTag("chat_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RoyalBlueSurface)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("💬 Live Chat", "⚔️ Decrees", "👑 Emojis").forEachIndexed { index, title ->
                        val isSelected = selectedCategory == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) RoyalGold else Color.Transparent)
                                .clickable { selectedCategory = index }
                                .padding(vertical = 6.dp)
                                .testTag("chat_tab_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color(0xFF1A1A1A) else Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (selectedCategory) {
                    0 -> {
                        // 1. Live Chat Room: Messages list + Quick Chips + Text input field
                        Column(modifier = Modifier.weight(1f)) {
                            // Messages History Stream
                            if (messages.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F1E40).copy(alpha = 0.7f))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("👑", fontSize = 34.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "No royal messages yet!",
                                            color = RoyalGoldLight,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Type below or tap a quick phrase to greet the court.",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F1E40).copy(alpha = 0.6f))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(messages, key = { it.id }) { msg ->
                                        ChatMessageBubble(
                                            message = msg,
                                            isMe = msg.senderName == currentSenderName && msg.senderColor == currentSenderColor
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick-Chat Presets Chips Row
                            Text(
                                text = "QUICK RESPONSES",
                                color = RoyalGoldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(STANDARD_QUICK_PHRASES) { phrase ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFF142F6B))
                                            .border(1.dp, RoyalGoldDark.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                            .clickable {
                                                onSendPhrase(phrase)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                            .testTag("quick_chip_$phrase"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = phrase,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Bottom Input Field with Keyboard Action & Character Counter (Max 100)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0B1936))
                                    .border(1.2.dp, Brush.horizontalGradient(listOf(RoyalGoldDark, RoyalGoldLight)), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = inputText,
                                        onValueChange = { if (it.length <= 100) inputText = it },
                                        placeholder = {
                                            Text(
                                                "Type a royal message...",
                                                color = Color.White.copy(alpha = 0.45f),
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            cursorColor = RoyalGold
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            capitalization = KeyboardCapitalization.Sentences,
                                            imeAction = ImeAction.Send
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onSend = {
                                                if (inputText.isNotBlank()) {
                                                    onSendMessage(inputText)
                                                    inputText = ""
                                                    focusManager.clearFocus()
                                                }
                                            }
                                        ),
                                        singleLine = false,
                                        maxLines = 2,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("chat_input_textfield")
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Character limit badge (e.g. 24/100)
                                Text(
                                    text = "${inputText.length}/100",
                                    color = when {
                                        inputText.length >= 95 -> Color(0xFFEF5350)
                                        inputText.length >= 80 -> Color(0xFFFFB74D)
                                        else -> Color.White.copy(alpha = 0.4f)
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Send Button
                                val canSend = inputText.isNotBlank()
                                IconButton(
                                    onClick = {
                                        if (canSend) {
                                            onSendMessage(inputText)
                                            inputText = ""
                                            focusManager.clearFocus()
                                        }
                                    },
                                    enabled = canSend,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (canSend) Brush.linearGradient(listOf(RoyalGold, RoyalGoldDark))
                                            else Brush.linearGradient(listOf(Color(0xFF37474F), Color(0xFF263238)))
                                        )
                                        .testTag("chat_send_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (canSend) Color(0xFF1A1A1A) else Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // 2. Preset Regal Decrees
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "OFFICIAL ROYAL DECREES",
                                color = RoyalGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(REGAL_QUICK_RESPONSES.chunked(2)) { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        pair.forEach { response ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .shadow(2.dp, RoundedCornerShape(10.dp))
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFF132B66))
                                                    .border(1.2.dp, RoyalGoldDark.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        onSendPhrase(response)
                                                        selectedCategory = 0 // Switch to live chat to see response
                                                    }
                                                    .padding(horizontal = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = response,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // 3. Emojis & Stickers Grid
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "REGAL ANIMATED EMOJIS",
                                color = RoyalGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                REGAL_THEMED_EMOJIS.chunked(4).forEach { rowEmojis ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        rowEmojis.forEach { emoji ->
                                            RegalEmojiButton(
                                                emoji = emoji,
                                                isRegal = true,
                                                onClick = {
                                                    onSendEmoji(emoji)
                                                    selectedCategory = 0
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "CLASSIC EXPRESSIONS",
                                color = RoyalGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CLASSIC_EXPRESSIVE_EMOJIS.take(4).forEach { emoji ->
                                    RegalEmojiButton(
                                        emoji = emoji,
                                        isRegal = false,
                                        onClick = {
                                            onSendEmoji(emoji)
                                            selectedCategory = 0
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CLASSIC_EXPRESSIVE_EMOJIS.drop(4).take(4).forEach { emoji ->
                                    RegalEmojiButton(
                                        emoji = emoji,
                                        isRegal = false,
                                        onClick = {
                                            onSendEmoji(emoji)
                                            selectedCategory = 0
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Message bubble showing:
 * - Sender name
 * - Player color pill/badge (Red, Green, Yellow, Blue)
 * - Message timestamp (HH:mm)
 * - Text/Emoji bubble
 */
@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    isMe: Boolean
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_bubble_${message.id}"),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Sender info & Color Badge Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            // Player Color Badge Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(message.senderColor.primary.copy(alpha = 0.22f))
                    .border(1.dp, message.senderColor.primary, RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(message.senderColor.primary)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = message.senderColor.title.uppercase(),
                        color = message.senderColor.primary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = message.senderName,
                color = if (isMe) RoyalGold else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = formattedTime,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 9.sp
            )
        }

        // Message bubble
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(3.dp, RoundedCornerShape(12.dp))
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 2.dp,
                        bottomEnd = if (isMe) 2.dp else 12.dp
                    )
                )
                .background(
                    if (isMe) Brush.horizontalGradient(listOf(Color(0xFF0F3B82), Color(0xFF1565C0)))
                    else Brush.horizontalGradient(listOf(Color(0xFF1A2744), Color(0xFF223258)))
                )
                .border(
                    1.dp,
                    if (isMe) RoyalGold.copy(alpha = 0.7f) else message.senderColor.primary.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            if (message.isEmoji) {
                Text(
                    text = message.text,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun RegalEmojiButton(
    emoji: String,
    isRegal: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(if (isRegal) Color(0xFF142F6B) else Color(0xFF1E3875))
            .border(
                1.5.dp,
                if (isRegal) RoyalGold else Color.White.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .testTag("regal_chat_emoji_$emoji"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 26.sp
        )
    }
}

/**
 * Transient in-game chat message banner floating over matches
 */
@Composable
fun InGameChatBannerOverlay(
    message: String?,
    senderName: String?,
    senderColor: LudoColor? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null && senderName != null,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = modifier
    ) {
        if (message != null && senderName != null) {
            val badgeColor = senderColor?.primary ?: RoyalGold
            Box(
                modifier = Modifier
                    .shadow(10.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF0D47A1))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold)),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("in_game_chat_banner")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$senderName: ",
                        color = RoyalGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
