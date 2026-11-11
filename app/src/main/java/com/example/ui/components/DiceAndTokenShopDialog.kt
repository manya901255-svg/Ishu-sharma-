package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun DiceAndTokenShopDialog(
    equippedDiceSkinId: String,
    equippedTokenSkinId: String,
    equippedSoundTheme: String,
    userCoins: Long,
    onEquipDiceSkin: (String) -> Unit,
    onEquipTokenSkin: (String) -> Unit,
    onEquipSoundTheme: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var activeCategory by remember { mutableIntStateOf(0) } // 0: Dice Skins, 1: Token Designs, 2: Sound Themes

    val soundThemes = listOf(
        Triple("royal_court", "Royal Court (Acoustic Palace)", "📯 Authentic brass fanfares & solid teakwood clicks"),
        Triple("neon_synth", "Neon Synth (Arcade)", "⚡ High-voltage electronic arcade clicks & retro synth"),
        Triple("wooden_classic", "Wooden Classic (Traditional)", "🪵 Organic hand-carved mahogany clatter & natural acoustics"),
        Triple("modern_minimal", "Modern Minimal (Club)", "🎧 Crisp ultra-clean haptic clicks & subtle chimes")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("dice_and_token_shop_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalNavyDark),
            border = androidx.compose.foundation.BorderStroke(
                2.5.dp,
                Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "ROYAL ARMORY & CUSTOMIZER",
                                color = RoyalGold,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "Dice Skins, Tokens & Sound FX",
                                color = Color.White.copy(alpha = 0.7f),
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

                // Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF102754))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val tabs = listOf("Dice Skins 🎲", "Token Crests 👑", "Sound FX 🔊")
                    tabs.forEachIndexed { index, label ->
                        val isSelected = activeCategory == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) RoyalGold else Color.Transparent)
                                .clickable { activeCategory = index }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (activeCategory) {
                        0 -> {
                            // DICE SKINS
                            BUILT_IN_DICE_SKINS.forEach { skin ->
                                val isEquipped = skin.id == equippedDiceSkinId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isEquipped) Color(0xFF223A68) else Color(0xFF122244))
                                        .border(
                                            if (isEquipped) 2.dp else 1.dp,
                                            if (isEquipped) RoyalGold else Color(0xFF1E3A70),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onEquipDiceSkin(skin.id) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .shadow(6.dp, RoundedCornerShape(10.dp))
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Brush.linearGradient(listOf(skin.primaryColor, skin.accentColor)))
                                                .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(skin.iconEmoji, fontSize = 22.sp)
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                skin.name,
                                                color = Color.White,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                skin.description,
                                                color = Color.White.copy(alpha = 0.65f),
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    if (isEquipped) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(RoyalGold)
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("EQUIPPED", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { onEquipDiceSkin(skin.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Equip", color = RoyalGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // TOKEN CRESTS
                            BUILT_IN_TOKEN_SKINS.forEach { skin ->
                                val isEquipped = skin.id == equippedTokenSkinId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isEquipped) Color(0xFF223A68) else Color(0xFF122244))
                                        .border(
                                            if (isEquipped) 2.dp else 1.dp,
                                            if (isEquipped) RoyalGold else Color(0xFF1E3A70),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onEquipTokenSkin(skin.id) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .shadow(6.dp, CircleShape)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.radialGradient(listOf(Color(0xFF2E57A6), Color(0xFF122854)))
                                                )
                                                .border(2.dp, RoyalGold, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(skin.symbolEmoji, fontSize = 24.sp)
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    skin.name,
                                                    color = Color.White,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFFFB300).copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(skin.badgeTitle, color = RoyalGold, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                            Text(
                                                skin.description,
                                                color = Color.White.copy(alpha = 0.65f),
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    if (isEquipped) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(RoyalGold)
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("EQUIPPED", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { onEquipTokenSkin(skin.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Equip", color = RoyalGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // SOUND FX THEMES
                            soundThemes.forEach { (id, name, desc) ->
                                val isEquipped = id == equippedSoundTheme
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isEquipped) Color(0xFF223A68) else Color(0xFF122244))
                                        .border(
                                            if (isEquipped) 2.dp else 1.dp,
                                            if (isEquipped) RoyalGold else Color(0xFF1E3A70),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onEquipSoundTheme(id) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(desc, color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp)
                                    }

                                    if (isEquipped) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(RoyalGold)
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("ACTIVE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { onEquipSoundTheme(id) },
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Select", color = RoyalGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold)
                ) {
                    Text("Apply & Close", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
