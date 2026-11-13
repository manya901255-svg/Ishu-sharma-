package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.GameMode
import com.example.model.PlayerReport
import com.example.ui.theme.*

@Composable
fun RoyalAdminDashboardDialog(
    isPinVerified: Boolean,
    onVerifyPin: (String) -> Boolean,
    playerReports: List<PlayerReport>,
    userCoins: Long,
    userGems: Int,
    onGrantCoins: (Long) -> Unit,
    onGrantGems: (Int) -> Unit,
    onResetDailyRewards: () -> Unit,
    onResetDailyMissions: () -> Unit,
    onClearReports: () -> Unit,
    diceCatalog: List<com.example.model.PremiumDiceItem> = emptyList(),
    onSaveDice: (com.example.model.PremiumDiceItem) -> Unit = {},
    onDeleteDice: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Economy & Economy, 1: Modes & Rules, 2: Moderation, 3: Telemetry

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("royal_admin_dashboard_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF07142E)),
            border = androidx.compose.foundation.BorderStroke(
                2.5.dp,
                Brush.linearGradient(listOf(Color(0xFF00E5FF), RoyalGold, Color(0xFFFF9100)))
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
                        Text("🛡️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "ADMIN DASHBOARD",
                                color = RoyalGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "Royal Chamber Management",
                                color = Color(0xFF80D8FF),
                                fontSize = 10.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isPinVerified) {
                    // PIN Verification View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = RoyalGold,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Royal Master Access PIN",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Enter the 4-digit Chamber PIN (Default: 1234)",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.5.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // PIN Display Dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (0..3).forEach { index ->
                                val filled = index < enteredPin.length
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (filled) RoyalGold else Color.White.copy(alpha = 0.2f)
                                        )
                                        .border(
                                            1.5.dp,
                                            if (pinError) Color(0xFFFF5252) else RoyalGoldLight,
                                            CircleShape
                                        )
                                )
                            }
                        }

                        if (pinError) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "❌ Incorrect PIN! Try 1234",
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Keypad (1..9, 0, Backspace)
                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("C", "0", "⌫")
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            keys.forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    row.forEach { key ->
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp, 44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF142952))
                                                .border(1.dp, Color(0xFF264882), RoundedCornerShape(12.dp))
                                                .clickable {
                                                    pinError = false
                                                    when (key) {
                                                        "C" -> enteredPin = ""
                                                        "⌫" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                                        else -> {
                                                            if (enteredPin.length < 4) {
                                                                val next = enteredPin + key
                                                                enteredPin = next
                                                                if (next.length == 4) {
                                                                    val success = onVerifyPin(next)
                                                                    if (!success) {
                                                                        pinError = true
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key,
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Authenticated Chamber Tabs (horizontally scrollable for 5 tabs)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF102754))
                            .horizontalScroll(rememberScrollState())
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabLabels = listOf("Economy 🪙", "Modes 🎮", "Safety 🛡️", "Telemetry 📊", "Dice Admin 🎲")
                        tabLabels.forEachIndexed { index, label ->
                            val isSelected = activeTab == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) RoyalGold else Color.Transparent)
                                    .clickable { activeTab = index }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("admin_tab_$index"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (activeTab) {
                            0 -> {
                                // ECONOMY CONTROLS
                                Text(
                                    "Player Wealth & Reward Resets",
                                    color = RoyalGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF142952))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text("Current Coins", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                            Text("🪙 $userCoins", color = RoyalGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF142952))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text("Current Gems", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                            Text("💎 $userGems", color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Button(
                                    onClick = { onGrantCoins(50000L) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                                ) {
                                    Text("Grant +50,000 Gold Coins 🪙", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { onGrantGems(100) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF))
                                ) {
                                    Text("Grant +100 Royal Gems 💎", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = onResetDailyRewards,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                                ) {
                                    Text("Reset Daily Login & Spin Timers 🎡", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = onResetDailyMissions,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                                ) {
                                    Text("Refresh Daily Quests & Missions 📜", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            1 -> {
                                // GAME MODES INSPECTOR
                                Text(
                                    "Active Game Modes Matrix",
                                    color = RoyalGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                GameMode.values().forEach { mode ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF142952))
                                            .border(1.dp, Color(0xFF203A68), RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(mode.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(mode.subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 9.5.sp, maxLines = 1)
                                        }
                                        Text(
                                            "🪙 ${mode.defaultWager}",
                                            color = RoyalGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            2 -> {
                                // MODERATION & REPORTS
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Player Reports (${playerReports.size})",
                                        color = RoyalGold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (playerReports.isNotEmpty()) {
                                        TextButton(onClick = onClearReports) {
                                            Text("Clear All", color = Color(0xFFFF5252), fontSize = 11.sp)
                                        }
                                    }
                                }

                                if (playerReports.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "🛡️ No reported players. Court is harmonious!",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 12.sp
                                        )
                                    }
                                } else {
                                    playerReports.forEach { report ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF1C243B))
                                                .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(report.reportedPlayerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(report.status, color = Color(0xFFFF8A80), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(report.reason, color = Color(0xFFFFCDD2), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                            3 -> {
                                // TELEMETRY & RNG AUDIT
                                Text(
                                    "System Health & Fair RNG Telemetry",
                                    color = RoyalGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                val telemetryMetrics = listOf(
                                    "Provably Fair Engine" to "HMAC_SHA256 Client-Server Seed",
                                    "Audio Engine" to "44 Indian Soundtracks • Dual Player Pool",
                                    "Voice Chat Network" to "WebRTC P2P Acoustic Echo Cancellation",
                                    "Wallpaper System" to "5 Target Slots • Persistent Room DB",
                                    "Rendering Pipeline" to "Jetpack Compose Hardware Acceleration (60fps)"
                                )

                                telemetryMetrics.forEach { (metric, value) ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF122448))
                                            .padding(10.dp)
                                    ) {
                                        Text(metric, color = Color(0xFF80D8FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(value, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                            4 -> {
                                // LIVE DICE STORE ADMIN EDITOR
                                AdminDiceManagementPanel(
                                    diceCatalog = diceCatalog,
                                    onSaveDice = onSaveDice,
                                    onDeleteDice = onDeleteDice
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDiceManagementPanel(
    diceCatalog: List<com.example.model.PremiumDiceItem>,
    onSaveDice: (com.example.model.PremiumDiceItem) -> Unit,
    onDeleteDice: (String) -> Unit
) {
    var selectedDiceId by remember { mutableStateOf<String?>(diceCatalog.firstOrNull()?.id) }
    var isCreatingNew by remember { mutableStateOf(false) }

    val activeSource = remember(selectedDiceId, diceCatalog, isCreatingNew) {
        if (isCreatingNew) null
        else diceCatalog.firstOrNull { it.id == selectedDiceId } ?: diceCatalog.firstOrNull()
    }

    var nameInput by remember(activeSource, isCreatingNew) {
        mutableStateOf(activeSource?.name ?: "Maharaja Nebula Dice")
    }
    var coinPriceInput by remember(activeSource, isCreatingNew) {
        mutableStateOf((activeSource?.coinPrice ?: 12000L).toString())
    }
    var inrPriceInput by remember(activeSource, isCreatingNew) {
        mutableStateOf((activeSource?.inrPrice ?: 69).toString())
    }
    var benefitSummaryInput by remember(activeSource, isCreatingNew) {
        mutableStateOf(activeSource?.benefits?.visualEffectSummary ?: "Cosmic purple vortex spin with golden sparks")
    }
    var selectedGroup by remember(activeSource, isCreatingNew) {
        mutableStateOf(activeSource?.collectionGroup ?: com.example.model.DiceCollectionGroup.SPECIAL_THEMED)
    }
    var selectedRarity by remember(activeSource, isCreatingNew) {
        mutableStateOf(activeSource?.rarity ?: com.example.model.DiceRarity.EPIC)
    }
    var selectedAvailability by remember(activeSource, isCreatingNew) {
        mutableStateOf(activeSource?.availabilityType ?: com.example.model.DiceAvailabilityType.ALWAYS_AVAILABLE)
    }
    var selectedAnim by remember(activeSource, isCreatingNew) {
        mutableStateOf(activeSource?.benefits?.animationStyle ?: com.example.model.DiceAnimationStyle.METEOR_SPIN)
    }
    var selectedParticle by remember(activeSource, isCreatingNew) {
        mutableStateOf(activeSource?.benefits?.particleEffect ?: com.example.model.DiceParticleEffect.GOLD_SPARKLE)
    }
    var selectedSound by remember(activeSource, isCreatingNew) {
        mutableStateOf(activeSource?.benefits?.soundEffect ?: com.example.model.DiceSoundEffect.GOLDEN_CHIME)
    }
    var canBuyCoins by remember(activeSource, isCreatingNew) {
        mutableStateOf(activeSource?.canBuyWithCoins ?: true)
    }
    var canBuyInr by remember(activeSource, isCreatingNew) {
        mutableStateOf(activeSource?.canBuyWithInr ?: true)
    }
    var isPublished by remember(activeSource, isCreatingNew) {
        mutableStateOf(activeSource?.isPublished ?: true)
    }
    var isAvailable by remember(activeSource, isCreatingNew) {
        mutableStateOf(activeSource?.isAvailable ?: true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_dice_management_panel"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Dice Catalog (${diceCatalog.size})",
                color = RoyalGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = { isCreatingNew = !isCreatingNew },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("admin_new_dice_toggle")
            ) {
                Text(
                    text = if (isCreatingNew) "Edit Existing" else "+ Add New Dice",
                    color = Color(0xFF00E5FF),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!isCreatingNew && diceCatalog.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                diceCatalog.forEach { d ->
                    val chosen = d.id == activeSource?.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (chosen) RoyalGold else Color(0xFF142952))
                            .border(1.dp, if (chosen) Color.White else Color(0xFF284888), RoundedCornerShape(8.dp))
                            .clickable { selectedDiceId = d.id }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("admin_select_dice_${d.id}")
                    ) {
                        Text(
                            text = "${d.iconEmoji} ${d.name}",
                            color = if (chosen) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Dice Name", color = Color(0xFF80D8FF), fontSize = 10.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = RoyalGold,
                unfocusedBorderColor = Color(0xFF3949AB)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_dice_name_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = coinPriceInput,
                onValueChange = { coinPriceInput = it.filter { ch -> ch.isDigit() } },
                label = { Text("Coins", color = RoyalGold, fontSize = 10.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = RoyalGold,
                    unfocusedBorderColor = Color(0xFF3949AB)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_dice_coin_input")
            )
            OutlinedTextField(
                value = inrPriceInput,
                onValueChange = { inrPriceInput = it.filter { ch -> ch.isDigit() } },
                label = { Text("INR (₹)", color = Color(0xFF00E676), fontSize = 10.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color(0xFF3949AB)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_dice_inr_input")
            )
        }

        OutlinedTextField(
            value = benefitSummaryInput,
            onValueChange = { benefitSummaryInput = it },
            label = { Text("Fair Benefit / Effect Summary", color = Color(0xFF80D8FF), fontSize = 10.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = RoyalGold,
                unfocusedBorderColor = Color(0xFF3949AB)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_dice_benefit_input")
        )

        // Cycle selectors for Rarity, Group, Availability, Animation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AdminCycleChip(
                label = "Rarity: ${selectedRarity.label}",
                modifier = Modifier.weight(1f)
            ) {
                val values = com.example.model.DiceRarity.entries
                selectedRarity = values[(values.indexOf(selectedRarity) + 1) % values.size]
            }
            AdminCycleChip(
                label = "Group: ${selectedGroup.badge}",
                modifier = Modifier.weight(1f)
            ) {
                val values = com.example.model.DiceCollectionGroup.entries
                selectedGroup = values[(values.indexOf(selectedGroup) + 1) % values.size]
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AdminCycleChip(
                label = "Mode: ${selectedAvailability.label}",
                modifier = Modifier.weight(1f)
            ) {
                val values = com.example.model.DiceAvailabilityType.entries
                selectedAvailability = values[(values.indexOf(selectedAvailability) + 1) % values.size]
            }
            AdminCycleChip(
                label = "Anim: ${selectedAnim.displayName}",
                modifier = Modifier.weight(1f)
            ) {
                val values = com.example.model.DiceAnimationStyle.entries
                selectedAnim = values[(values.indexOf(selectedAnim) + 1) % values.size]
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AdminCycleChip(
                label = "Particles: ${selectedParticle.displayName}",
                modifier = Modifier.weight(1f)
            ) {
                val values = com.example.model.DiceParticleEffect.entries
                selectedParticle = values[(values.indexOf(selectedParticle) + 1) % values.size]
            }
            AdminCycleChip(
                label = "Sound: ${selectedSound.displayName}",
                modifier = Modifier.weight(1f)
            ) {
                val values = com.example.model.DiceSoundEffect.entries
                selectedSound = values[(values.indexOf(selectedSound) + 1) % values.size]
            }
        }

        // Toggles: Coins / INR / Available / Published
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AdminTogglePill("Coins", canBuyCoins, Modifier.weight(1f)) { canBuyCoins = !canBuyCoins }
            AdminTogglePill("INR ₹", canBuyInr, Modifier.weight(1f)) { canBuyInr = !canBuyInr }
            AdminTogglePill("Avail", isAvailable, Modifier.weight(1f)) { isAvailable = !isAvailable }
            AdminTogglePill("Pub", isPublished, Modifier.weight(1f)) { isPublished = !isPublished }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val base = activeSource
                    val cleanSlug = nameInput.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "custom" }
                    val newId = if (isCreatingNew || base == null) "dice_admin_${cleanSlug}_${System.currentTimeMillis() % 10000}" else base.id
                    val updatedItem = com.example.model.PremiumDiceItem(
                        id = newId,
                        name = nameInput.ifBlank { "Custom Dice" },
                        description = base?.description ?: "Admin crafted custom royal dice",
                        collectionGroup = selectedGroup,
                        rarity = selectedRarity,
                        coinPrice = coinPriceInput.toLongOrNull() ?: 0L,
                        inrPrice = inrPriceInput.toIntOrNull() ?: 0,
                        skuId = base?.skuId ?: "mg_$newId",
                        canBuyWithCoins = canBuyCoins,
                        canBuyWithInr = canBuyInr,
                        isAvailable = isAvailable,
                        availabilityType = selectedAvailability,
                        eventEndTimeMillis = if (selectedAvailability == com.example.model.DiceAvailabilityType.LIMITED_TIME || selectedAvailability == com.example.model.DiceAvailabilityType.DAILY_OFFER) {
                            (base?.eventEndTimeMillis ?: 0L).takeIf { it > System.currentTimeMillis() } ?: (System.currentTimeMillis() + 86400000L)
                        } else 0L,
                        challengeRequirementText = base?.challengeRequirementText.orEmpty(),
                        challengeTargetCount = base?.challengeTargetCount ?: 0,
                        benefits = com.example.model.DiceBenefits(
                            animationStyle = selectedAnim,
                            particleEffect = selectedParticle,
                            soundEffect = selectedSound,
                            visualEffectSummary = benefitSummaryInput,
                            vipStatusLabel = if (selectedRarity == com.example.model.DiceRarity.VIP) "VIP Royal Member" else base?.benefits?.vipStatusLabel
                        ),
                        primaryColorHex = base?.primaryColorHex ?: 0xFF3F51B5,
                        secondaryColorHex = base?.secondaryColorHex ?: 0xFF1A237E,
                        accentColorHex = base?.accentColorHex ?: 0xFFFFD54F,
                        pipColorHex = base?.pipColorHex ?: 0xFFFFFFFF,
                        iconEmoji = base?.iconEmoji ?: "🎲",
                        isStarterDefault = base?.isStarterDefault ?: false,
                        isPublished = isPublished,
                        sortOrder = base?.sortOrder ?: (diceCatalog.size + 1),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSaveDice(updatedItem)
                    selectedDiceId = updatedItem.id
                    isCreatingNew = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_save_dice_button")
            ) {
                Text(
                    text = if (isCreatingNew) "Create & Publish 🎲" else "Save Live Changes ✓",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }

            if (!isCreatingNew && activeSource != null && !activeSource.isStarterDefault) {
                Button(
                    onClick = {
                        onDeleteDice(activeSource.id)
                        selectedDiceId = diceCatalog.firstOrNull { it.id != activeSource.id }?.id
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("admin_delete_dice_button")
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AdminCycleChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF142952))
            .border(1.dp, Color(0xFF2E5499), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun AdminTogglePill(
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) Color(0xFF00695C) else Color(0xFF37474F))
            .border(1.dp, if (enabled) Color(0xFF00E676) else Color.Gray, RoundedCornerShape(8.dp))
            .clickable { onToggle() }
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (enabled) "$label ✓" else "$label ✕",
            color = Color.White,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

