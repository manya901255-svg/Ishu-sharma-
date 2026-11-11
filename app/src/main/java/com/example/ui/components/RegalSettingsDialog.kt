package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BGM_PLAYLIST
import com.example.model.BgmTrack
import com.example.model.REGAL_BOARD_THEMES
import com.example.model.RegalBoardTheme
import com.example.ui.theme.*

@Composable
fun RegalSettingsDialog(
    isBgmEnabled: Boolean,
    bgmVolume: Float,
    isSfxEnabled: Boolean,
    sfxVolume: Float,
    isHapticsEnabled: Boolean,
    selectedBoardThemeId: String,
    selectedSfxPreset: String = "royal_court",
    currentBgmTrack: BgmTrack? = null,
    currentBgmTrackIndex: Int = -1,
    bgmPlaylist: List<BgmTrack> = emptyList(),
    onToggleBgm: (Boolean) -> Unit,
    onBgmVolumeChange: (Float) -> Unit,
    onNextBgmTrack: () -> Unit = {},
    onPrevBgmTrack: () -> Unit = {},
    onSelectBgmTrack: (Int) -> Unit = {},
    onOpenMusicPlayer: () -> Unit = {},
    onToggleSfx: (Boolean) -> Unit,
    onSfxVolumeChange: (Float) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onSelectBoardTheme: (String) -> Unit,
    onSelectSfxPreset: (String) -> Unit = {},
    onTestDice: () -> Unit = {},
    onTestPawn: () -> Unit = {},
    onTestFanfare: () -> Unit = {},
    onTestCapture: () -> Unit = {},
    onTestStarSafe: () -> Unit = {},
    onTestSixRolled: () -> Unit = {},
    onOpenWallpaperCustomizer: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Sound & Atmosphere, 1: Regal Board Themes, 2: Wallpapers

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp)
                .shadow(20.dp, RoundedCornerShape(22.dp))
                .testTag("regal_settings_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = RoyalNavyDark),
            border = androidx.compose.foundation.BorderStroke(
                2.5.dp,
                Brush.linearGradient(listOf(RoyalGoldLight, RoyalGold, RoyalGoldDark))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👑", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ROYAL SETTINGS",
                            color = RoyalGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs (Audio & Sound / Regal Board Themes)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RoyalBlueSurface)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tab 0: Audio & Sound
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == 0) RoyalGold else Color.Transparent)
                            .clickable { activeTab = 0 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (activeTab == 0) Color.Black else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sound & Music",
                                color = if (activeTab == 0) Color(0xFF1A1A1A) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Tab 1: Board Themes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == 1) RoyalGold else Color.Transparent)
                            .clickable { activeTab = 1 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = if (activeTab == 1) Color.Black else Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Boards",
                                color = if (activeTab == 1) Color(0xFF1A1A1A) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Tab 2: Wallpapers
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == 2) RoyalGold else Color.Transparent)
                            .clickable { activeTab = 2 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎨", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Wallpapers",
                                color = if (activeTab == 2) Color(0xFF1A1A1A) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (activeTab) {
                    0 -> {
                        // TAB 0: SOUND & MUSIC CONTROLS
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Ambient Background Music Toggle & Slider
                            SoundSettingItem(
                                icon = Icons.Default.MusicNote,
                                title = "Background Music",
                                subtitle = if (isBgmEnabled && currentBgmTrack != null) {
                                    "${currentBgmTrack.iconEmoji} ${currentBgmTrack.title} • ${currentBgmTrack.singer}"
                                } else if (isBgmEnabled) {
                                    "Tap to open Music Lounge (${bgmPlaylist.size} original songs)"
                                } else {
                                    "Music Paused • ${bgmPlaylist.size} original songs available"
                                },
                                isEnabled = isBgmEnabled,
                                volume = bgmVolume,
                                onToggle = onToggleBgm,
                                onVolumeChange = onBgmVolumeChange
                            )

                            // Notice when all songs have been removed from playlist
                            if (bgmPlaylist.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0A1E46))
                                        .border(1.dp, Color(0xFF1E3A70), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "🎵 44 Original Bollywood & Punjabi tracks available in Music Lounge.",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            } else if (isBgmEnabled && currentBgmTrack != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(RoyalBlueSurface)
                                        .border(1.5.dp, RoyalGold, RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    // Playlist Header & Sequential Mode Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🎶 NOW PLAYING (${currentBgmTrackIndex + 1}/${bgmPlaylist.size})",
                                            color = RoyalGoldLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                                .border(1.dp, Color(0xFF00E676), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "🔄 One After Other",
                                                color = Color(0xFF00E676),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Currently Active Song Details & Quick Next/Previous Controls
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0A1E46))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(text = currentBgmTrack.iconEmoji, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = currentBgmTrack.title,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${currentBgmTrack.subtitle} • ${currentBgmTrack.durationLabel}",
                                                    color = Color(0xFFFFD54F),
                                                    fontSize = 10.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = onPrevBgmTrack,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SkipPrevious,
                                                    contentDescription = "Previous Song",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = onNextBgmTrack,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SkipNext,
                                                    contentDescription = "Next Song",
                                                    tint = RoyalGold,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Button to open full music player & jukebox modal
                                    Button(
                                        onClick = onOpenMusicPlayer,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF6A1B9A)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "🎶 Open Royal Music Player (${bgmPlaylist.size} Songs)",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "UPCOMING IN QUEUE (${bgmPlaylist.size} songs continuous loop):",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Display first 6 upcoming songs in queue preview
                                    bgmPlaylist.take(8).forEachIndexed { index, track ->
                                        val isCurrent = index == currentBgmTrackIndex
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isCurrent) Color(0xFF1B3D7A) else Color(0xFF0F224D)
                                                )
                                                .border(
                                                    width = if (isCurrent) 1.dp else 0.5.dp,
                                                    color = if (isCurrent) RoyalGold else Color(0x33FFFFFF),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable { onSelectBgmTrack(index) }
                                                .padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isCurrent) "▶️" else "${index + 1}.",
                                                    fontSize = 10.sp,
                                                    color = if (isCurrent) Color(0xFF00E676) else TextMuted
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${track.iconEmoji} ${track.title}",
                                                    color = if (isCurrent) RoyalGoldLight else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                    maxLines = 1
                                                )
                                            }
                                            Text(
                                                text = track.durationLabel,
                                                color = if (isCurrent) RoyalGold else TextMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            // 2. Sound Effects Toggle & Slider
                            SoundSettingItem(
                                icon = Icons.Default.VolumeUp,
                                title = "Sound Effects (SFX)",
                                subtitle = "Dice Rolling, Steps & Fanfare",
                                isEnabled = isSfxEnabled,
                                volume = sfxVolume,
                                onToggle = onToggleSfx,
                                onVolumeChange = onSfxVolumeChange
                            )

                            // SFX Sound Style Preset Selection
                            if (isSfxEnabled) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(RoyalBlueSurface)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "SFX SOUND STYLE",
                                        color = RoyalGoldLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val presets = listOf(
                                            Triple("royal_court", "👑 Royal", "Regal & Grand"),
                                            Triple("classic_ludo", "🎲 Classic", "Snappy Board"),
                                            Triple("arcade_punch", "⚡ Arcade", "Energetic Pop")
                                        )
                                        presets.forEach { (id, label, desc) ->
                                            val isSelected = selectedSfxPreset == id
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) RoyalGold else Color(0xFF142B63))
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) RoyalGoldLight else Color(0xFF28488C),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { onSelectSfxPreset(id) }
                                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = label,
                                                        color = if (isSelected) Color(0xFF1A1A1A) else Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = desc,
                                                        color = if (isSelected) Color(0xFF333333) else TextMuted,
                                                        fontSize = 8.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Quick SFX Testing Buttons (Live sound preview)
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        SoundTestButton("🎲 Dice Roll", onTestDice, Modifier.weight(1f))
                                        SoundTestButton("♟️ Pawn Step", onTestPawn, Modifier.weight(1f))
                                        SoundTestButton("⚔️ Strike", onTestCapture, Modifier.weight(1f))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        SoundTestButton("👑 Six!", onTestSixRolled, Modifier.weight(1f))
                                        SoundTestButton("⭐ Star Safe", onTestStarSafe, Modifier.weight(1f))
                                        SoundTestButton("🎺 Fanfare", onTestFanfare, Modifier.weight(1f))
                                    }
                                }
                            }

                            // 3. Tactile Haptics Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RoyalBlueSurface)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = RoyalGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Haptic Feedback",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Tactile rolls & captures",
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                Switch(
                                    checked = isHapticsEnabled,
                                    onCheckedChange = onToggleHaptics,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = RoyalGold,
                                        uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                        uncheckedTrackColor = Color(0xFF2C3E6B)
                                    )
                                )
                            }

                            // 4. Customize Wallpaper Option (User Request)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onOpenWallpaperCustomizer() }
                                    .border(1.5.dp, RoyalGold, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF0F2652)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(RoyalGold, Color(0xFFFFA000)))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🎨", fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Customize Wallpaper",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFF00C853))
                                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                                ) {
                                                    Text("NEW", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                            Text(
                                                text = "Personalize Home, Board, Lobby & Rooms",
                                                color = RoyalGoldLight,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Open Customizer",
                                        tint = RoyalGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: REGAL BOARD THEMES SELECTION
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                        ) {
                            Text(
                                text = "SELECT 'REGAL' BOARD THEME",
                                color = RoyalGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(REGAL_BOARD_THEMES) { theme ->
                                    val isSelected = theme.id == selectedBoardThemeId
                                    RegalBoardThemeItem(
                                        theme = theme,
                                        isSelected = isSelected,
                                        onSelect = { onSelectBoardTheme(theme.id) }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: WALLPAPERS STUDIO OVERVIEW
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 380.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "CUSTOMIZE GAME BACKGROUNDS",
                                color = RoyalGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )

                            Text(
                                text = "Choose from 12 imperial themes or upload custom pictures from your phone gallery for Home, Board, Lobby, Profile, and Private Room.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            // Quick Action Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onOpenWallpaperCustomizer() }
                                    .border(2.dp, Brush.linearGradient(listOf(RoyalGold, Color(0xFF00E5FF))), RoundedCornerShape(14.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF091C3E))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🖼️", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Open Wallpaper Studio",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Presets • Gallery Upload • Zoom & Crop",
                                        color = RoyalGoldLight,
                                        fontSize = 10.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onOpenWallpaperCustomizer,
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Launch Customizer", color = Color(0xFF101C30), fontWeight = FontWeight.Black, fontSize = 12.sp)
                                    }
                                }
                            }

                            // 5 Areas preview chips
                            Text(
                                text = "5 INDEPENDENT CUSTOMIZABLE SECTIONS:",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )

                            listOf(
                                "🏠 Home Screen" to "Main lobby & game mode carousel",
                                "🎲 Ludo Board" to "In-game battleground board background",
                                "⚔️ Game Lobby" to "Online wager & room selector",
                                "👑 Profile" to "Player statistics & identity modal",
                                "🏰 Private Room" to "Custom room matches with friends"
                            ).forEach { (title, subtitle) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF081836))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(title, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        Text(subtitle, color = TextMuted, fontSize = 9.sp)
                                    }
                                    Text("🎨", fontSize = 14.sp)
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
private fun SoundSettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    volume: Float,
    onToggle: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalBlueSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF233B78))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isEnabled) RoyalGold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isEnabled) RoyalGold else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = RoyalGold,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                        uncheckedTrackColor = Color(0xFF2C3E6B)
                    )
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vol", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = RoyalGoldLight,
                            activeTrackColor = RoyalGold,
                            inactiveTrackColor = Color(0xFF2C3E6B)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(volume * 100).toInt()}%",
                        color = RoyalGoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundTestButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF132B66))
            .border(1.dp, Color(0xFF32549E), RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RegalBoardThemeItem(
    theme: RegalBoardTheme,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isSelected) 6.dp else 1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .testTag("regal_theme_${theme.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF183372) else RoyalBlueSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) RoyalGold else Color(0xFF233B78)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme Color Swatches Preview
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(3.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(theme.previewGradient))
                    .border(1.5.dp, theme.borderColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Mini center court representation
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.trackBgColor)
                        .border(1.dp, theme.accentStarColor, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⭐", fontSize = 9.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = theme.subtitle,
                    color = TextMuted,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }

            // Equipped status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) RoyalGold else Color(0xFF0F265C))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isSelected) "EQUIPPED ✓" else "APPLY",
                    color = if (isSelected) Color(0xFF1A1A1A) else RoyalGoldLight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
