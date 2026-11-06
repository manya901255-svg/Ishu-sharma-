package com.example.ui.components

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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.*

/**
 * Ludo Club & Royal Court Audio / Atmosphere Settings Dialog.
 * Controls BGM, SFX volume, Haptics, and Voice Chat effects.
 */
@Composable
fun LudoClubSoundSettingsDialog(
    isBgmEnabled: Boolean,
    bgmVolume: Float,
    isSfxEnabled: Boolean,
    sfxVolume: Float,
    isHapticsEnabled: Boolean,
    onToggleBgm: (Boolean) -> Unit,
    onBgmVolumeChange: (Float) -> Unit,
    onToggleSfx: (Boolean) -> Unit,
    onSfxVolumeChange: (Float) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onTestDice: () -> Unit,
    onTestPawn: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF207132B)),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("ludo_club_sound_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("🔊", fontSize = 20.sp)
                        Text(
                            text = "Audio & Sound Settings",
                            fontFamily = CinzelFontFamily,
                            color = RoyalGoldLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_sound_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // BGM Music Section
                AudioToggleRow(
                    title = "Background Music (BGM)",
                    subtitle = "Continuous Royal Soundtrack",
                    icon = "🎵",
                    enabled = isBgmEnabled,
                    volume = bgmVolume,
                    onToggle = onToggleBgm,
                    onVolumeChange = onBgmVolumeChange
                )

                Spacer(modifier = Modifier.height(12.dp))

                // SFX Section
                AudioToggleRow(
                    title = "Game Sound Effects (SFX)",
                    subtitle = "Dice roll, steps, captures & fanfares",
                    icon = "🎲",
                    enabled = isSfxEnabled,
                    volume = sfxVolume,
                    onToggle = onToggleSfx,
                    onVolumeChange = onSfxVolumeChange
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Haptic Feedback Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F2653))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📳", fontSize = 18.sp)
                        Column {
                            Text("Vibration & Haptics", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Tactile feel on dice roll & captures", color = Color(0xFFB0BEC5), fontSize = 9.sp)
                        }
                    }

                    Switch(
                        checked = isHapticsEnabled,
                        onCheckedChange = onToggleHaptics,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RoyalGold,
                            checkedTrackColor = Color(0xFF1B5E20)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Test Sound Buttons
                Text("TEST SOUND FX", color = RoyalGoldLight, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onTestDice,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF29B6F6))
                    ) {
                        Text("🎲 Dice Roll", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onTestPawn,
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
                    ) {
                        Text("♟️ Pawn Step", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("DONE", color = Color(0xFF040A18), fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AudioToggleRow(
    title: String,
    subtitle: String,
    icon: String,
    enabled: Boolean,
    volume: Float,
    onToggle: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F2653))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(icon, fontSize = 18.sp)
                Column {
                    Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = Color(0xFFB0BEC5), fontSize = 9.sp)
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = RoyalGold,
                    checkedTrackColor = Color(0xFF1B5E20)
                )
            )
        }

        if (enabled) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🔈", fontSize = 11.sp)
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = RoyalGold,
                        activeTrackColor = RoyalGoldLight,
                        inactiveTrackColor = Color(0xFF1E3A70)
                    )
                )
                Text("🔊", fontSize = 11.sp)
                Text(
                    text = "${(volume * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp)
                )
            }
        }
    }
}
