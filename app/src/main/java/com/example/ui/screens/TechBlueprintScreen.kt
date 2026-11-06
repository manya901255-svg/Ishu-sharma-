package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Architectural Blueprint and Technical Documentation Screen.
 * Details the game loop, Unity C# script bridge, Fair RNG algorithm, and audio architecture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechBlueprintScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📐", fontSize = 20.sp)
                        Text(
                            text = "Technical Blueprint & Architecture",
                            fontFamily = CinzelFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = RoyalGoldLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("blueprint_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF07132B)
                )
            )
        },
        containerColor = RoyalNavyDark
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Architecture Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2653)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🏰 LUDO ROYAL ARCHITECTURAL ENGINE",
                        fontFamily = CinzelFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = RoyalGoldLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Full-stack mobile board game engineered with Jetpack Compose, high-performance 2D/3D Canvas rendering, and Unity-ready gameplay subsystems.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            // Section 1: Unity C# Game Subsystems
            BlueprintSectionCard(
                title = "🎮 Unity C# Subsystems (Included in /unity_scripts)",
                icon = "📦",
                items = listOf(
                    "DailyLoginRewardManager.cs" to "Tracks 7-day consecutive login streak with PlayerPrefs. Day 1 (500 coins) escalates up to Day 7 (25,000 coins). Auto-resets if a day is missed.",
                    "LuckySpinAndAdRewardManager.cs" to "4-hour cooldown wheel with weighted coin rewards (1,000 to 50,000 coins). Rewarded video ad simulation granting instant 1,000 coin bonuses.",
                    "LudoMatchGameManager.cs" to "Wager table manager (Beginner 500 entry / 1,000 win, Pro 10,000 entry / 20,000 win). Validates balance, deducts entry fees, credits 2X payout.",
                    "LudoReferralSystem.cs" to "Generates unique alphanumeric player referral codes, simulates Android Intent social sharing, and awards 5,000 coins per unique invite."
                )
            )

            // Section 2: Game Logic & Rules State Machine
            BlueprintSectionCard(
                title = "🎲 Core Rules & State Machine",
                icon = "⚙️",
                items = listOf(
                    "Pawn States" to "IN_BASE, ON_BOARD, IN_HOME_STRETCH, and FINISHED (Apex). Base release strictly requires a roll of 6.",
                    "Safe Zone Stars" to "8 Safe Star squares (indices 1, 9, 14, 22, 27, 35, 40, 48) where pawns cannot be captured.",
                    "Capture & Bonus Turn" to "Capturing an opponent token sends it back to base and grants an immediate bonus turn. Reaching the center finish apex also awards a bonus roll.",
                    "Three Consecutive Sixes" to "Rolling three consecutive sixes forfeits the turn to prevent roll-locking."
                )
            )

            // Section 3: Provably Fair Dice RNG
            BlueprintSectionCard(
                title = "🛡️ Provably Fair RNG System",
                icon = "🔒",
                items = listOf(
                    "SHA-256 Commitments" to "Cryptographic server seed commitment published before roll; verified upon completion with client seed & nonce.",
                    "Uniform Distribution" to "Dice values 1-6 generated with zero bias, matching international casino fairness standards."
                )
            )

            // Section 4: 3D Board Projection & 2D Classic Toggle
            BlueprintSectionCard(
                title = "📐 3D Isometric & 2D Dual Board Engine",
                icon = "🎨",
                items = listOf(
                    "3D Perspective Projection" to "Matrix tilt on rotationX with cameraDistance scaling, extruded wood plinth bevels, and counter-tilted vertical pawns.",
                    "Classic 2D Board" to "High-visibility standard 15x15 board with colored home corridors and safe stars, selectable anytime from Settings."
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Return to Game Button
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("blueprint_return_button"),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "RETURN TO LUDO PALACE",
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF040A18)
                )
            }
        }
    }
}

@Composable
private fun BlueprintSectionCard(
    title: String,
    icon: String,
    items: List<Pair<String, String>>
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1835)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A70)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(icon, fontSize = 18.sp)
                Text(
                    text = title,
                    fontFamily = CinzelFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = RoyalGoldLight
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            items.forEachIndexed { idx, (header, desc) ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "• $header",
                        color = Color(0xFF81D4FA),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = desc,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp
                    )
                }
                if (idx < items.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
