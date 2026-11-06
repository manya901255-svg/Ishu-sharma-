package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.BGM_PLAYLIST
import com.example.model.BgmTrack
import com.example.model.MusicCategory
import com.example.model.MusicRepeatMode
import com.example.ui.theme.*

/**
 * LudoMusicPlayerDialog: Premium Royal Dark-Themed In-Game Background Music & Songs Playlist.
 * Features:
 * - Royal dark theme: Deep navy/black, slate grey, and rich gold accents.
 * - Tabs/Categories: Trending/Modern, Retro/Classic, Instrumental, and Favorites.
 * - Full Bollywood singer catalog (Arijit, Sonu Nigam, Kishore Kumar, Lata Mangeshkar, Shreya Ghoshal, etc.).
 * - Instrumental focus (Harmonica, Flute/Bansuri, Acoustic Guitar, Lo-Fi Piano).
 * - Full playback controls: Play/Pause, Next/Previous, Repeat Mode (All/One/Off), Volume Slider.
 * - Live animated 5-bar equalizer visualizer.
 * - Search filter for instantly locating singers or tracks.
 */
@Composable
fun LudoMusicPlayerDialog(
    currentTrack: BgmTrack? = null,
    isPlaying: Boolean,
    repeatMode: MusicRepeatMode,
    volume: Float,
    favoriteSongIds: Set<String>,
    onPlayPauseToggle: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onSelectTrack: (Int) -> Unit,
    onRepeatModeCycle: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<MusicCategory?>(null) } // null = All, or specific
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Filter tracks based on tab and search query
    val filteredTracks = remember(selectedCategory, showFavoritesOnly, searchQuery, favoriteSongIds) {
        BGM_PLAYLIST.filter { track ->
            val matchesCategory = when {
                showFavoritesOnly -> favoriteSongIds.contains(track.id)
                selectedCategory != null -> track.category == selectedCategory
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                track.title.contains(searchQuery, ignoreCase = true) ||
                        track.singer.contains(searchQuery, ignoreCase = true) ||
                        track.instrumentalStyle.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Brush.verticalGradient(listOf(RoyalGold, Color(0xFF00E5FF), RoyalGoldDark)), RoundedCornerShape(24.dp))
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = RoyalGold)
                .testTag("ludo_music_player_dialog"),
            color = Color(0xFF071126)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF071126), Color(0xFF0A1838), Color(0xFF040A18))
                        )
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Header with Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B))))
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎵", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MG लूडो Music Lounge",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "INSTRUMENTAL",
                                        color = Color(0xFF00E676),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Text(
                                text = "Soothing Bollywood Background Melodies",
                                color = RoyalGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF132247))
                            .border(1.dp, Color(0xFF2A437C), CircleShape)
                            .testTag("close_music_dialog_button")
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

                // 2. NOW PLAYING CARD (Hero Section)
                NowPlayingCard(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    isFavorite = currentTrack?.let { favoriteSongIds.contains(it.id) } ?: false,
                    onToggleFavorite = { currentTrack?.let { onToggleFavorite(it.id) } }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. PLAYBACK CONTROLS (Repeat, Prev, Big Play/Pause, Next, Volume)
                PlaybackControlsSection(
                    isPlaying = isPlaying,
                    repeatMode = repeatMode,
                    volume = volume,
                    onPlayPauseToggle = onPlayPauseToggle,
                    onNextTrack = onNextTrack,
                    onPreviousTrack = onPreviousTrack,
                    onRepeatModeCycle = onRepeatModeCycle,
                    onVolumeChange = onVolumeChange
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. CATEGORY TABS & SEARCH
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Category Filter Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CategoryPill(
                            label = "All (${BGM_PLAYLIST.size})",
                            icon = "🎼",
                            isSelected = selectedCategory == null && !showFavoritesOnly,
                            onClick = {
                                selectedCategory = null
                                showFavoritesOnly = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryPill(
                            label = "Modern",
                            icon = "🔥",
                            isSelected = selectedCategory == MusicCategory.TRENDING_MODERN && !showFavoritesOnly,
                            onClick = {
                                selectedCategory = MusicCategory.TRENDING_MODERN
                                showFavoritesOnly = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryPill(
                            label = "Retro",
                            icon = "📻",
                            isSelected = selectedCategory == MusicCategory.RETRO_CLASSIC && !showFavoritesOnly,
                            onClick = {
                                selectedCategory = MusicCategory.RETRO_CLASSIC
                                showFavoritesOnly = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryPill(
                            label = "Flute/Lofi",
                            icon = "🪈",
                            isSelected = selectedCategory == MusicCategory.INSTRUMENTAL && !showFavoritesOnly,
                            onClick = {
                                selectedCategory = MusicCategory.INSTRUMENTAL
                                showFavoritesOnly = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryPill(
                            label = "★",
                            icon = "❤️",
                            isSelected = showFavoritesOnly,
                            onClick = {
                                showFavoritesOnly = !showFavoritesOnly
                                if (showFavoritesOnly) selectedCategory = null
                            },
                            modifier = Modifier.width(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search text box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("music_search_field"),
                        placeholder = {
                            Text(
                                "Search singer (Arijit, Kishore, Lata...) or track...",
                                fontSize = 11.5.sp,
                                color = Color(0xFF88A0C7)
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = RoyalGold, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0F1E40),
                            unfocusedContainerColor = Color(0xFF0B1733),
                            focusedBorderColor = RoyalGold,
                            unfocusedBorderColor = Color(0xFF1E3566),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 5. PLAYLIST TRACKS LIST
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF08142D))
                        .border(1.2.dp, Color(0xFF1A2E56), RoundedCornerShape(14.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    if (filteredTracks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("🔇", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (BGM_PLAYLIST.isEmpty()) {
                                        "All songs have been removed from the playlist.\nOnly sound effects (SFX) & haptics will play."
                                    } else if (showFavoritesOnly) {
                                        "No favorite tracks saved yet.\nTap the heart icon on any track to add!"
                                    } else {
                                        "No songs found for '$searchQuery'"
                                    },
                                    color = Color(0xFF88A0C7),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsIndexed(filteredTracks) { _, track ->
                                val originalIndex = BGM_PLAYLIST.indexOfFirst { it.id == track.id }
                                val isCurrentTrack = currentTrack != null && track.id == currentTrack.id

                                TrackListItemRow(
                                    track = track,
                                    isCurrentTrack = isCurrentTrack,
                                    isPlaying = isPlaying && isCurrentTrack,
                                    isFavorite = favoriteSongIds.contains(track.id),
                                    onSelect = { onSelectTrack(originalIndex) },
                                    onToggleFavorite = { onToggleFavorite(track.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Now Playing Card with glowing border, active waveform visualizer, and favorite button.
 */
@Composable
private fun NowPlayingCard(
    track: BgmTrack?,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF102758), Color(0xFF0F1E42), Color(0xFF13224B))
                )
            )
            .border(1.5.dp, if (isPlaying && track != null) RoyalGold else Color(0xFF233B70), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        if (track == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF37474F), Color(0xFF1E2830))
                            )
                        )
                        .border(1.5.dp, Color(0xFF546E7A), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔇", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "All Songs Removed",
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Background music playlist has no songs",
                        color = RoyalGoldLight,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🔊 Sound Effects & Haptics Only",
                        color = Color(0xFF90CAF9),
                        fontSize = 10.5.sp
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Album Art Badge with Music Emoji
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFFB300), Color(0xFF8C5300))
                            )
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(track.iconEmoji, fontSize = 28.sp)
                }

            Spacer(modifier = Modifier.width(10.dp))

            // Title, Singer, Style
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.title,
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "🎤 ${track.singer}",
                    color = RoyalGoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "🪈 ${track.instrumentalStyle}",
                    color = Color(0xFF90CAF9),
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right side: Audio Wave Visualizer & Heart button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Heart Favorite
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(34.dp)
                ) {
                    Text(
                        text = if (isFavorite) "❤️" else "🤍",
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Animated Equalizer Visualizer
                AudioEqualizerVisualizer(isPlaying = isPlaying)
            }
        }
    }
}
}

/**
 * Animated 5-bar Audio Equalizer Visualizer
 */
@Composable
private fun AudioEqualizerVisualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = if (isPlaying) 16f else 4f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = if (isPlaying) 20f else 6f,
        animationSpec = infiniteRepeatable(tween(280, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = if (isPlaying) 18f else 5f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "b3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = if (isPlaying) 22f else 7f,
        animationSpec = infiniteRepeatable(tween(310, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "b4"
    )
    val bar5 by infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = if (isPlaying) 15f else 4f,
        animationSpec = infiniteRepeatable(tween(380, easing = LinearEasing), RepeatMode.Reverse),
        label = "b5"
    )

    Row(
        modifier = Modifier.height(22.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        listOf(bar1, bar2, bar3, bar4, bar5).forEach { barHeight ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF00E676), RoyalGold)
                        )
                    )
            )
        }
    }
}

/**
 * Playback Controls Section (Repeat, Prev, Big Play/Pause, Next, Volume Slider)
 */
@Composable
private fun PlaybackControlsSection(
    isPlaying: Boolean,
    repeatMode: MusicRepeatMode,
    volume: Float,
    onPlayPauseToggle: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onRepeatModeCycle: () -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0C193B))
            .border(1.2.dp, Color(0xFF1E3A70), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Controls Row: Repeat, Prev, Big Play/Pause, Next
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Repeat Mode Button
            IconButton(
                onClick = onRepeatModeCycle,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF13254E))
                    .testTag("repeat_mode_button")
            ) {
                Text(repeatMode.icon, fontSize = 16.sp)
            }

            // Previous Button
            IconButton(
                onClick = onPreviousTrack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF172C5B))
                    .border(1.dp, Color(0xFF2F4F91), CircleShape)
                    .testTag("previous_track_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Big Central Play / Pause Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFD700), Color(0xFFFF9100), Color(0xFFB8860B))
                        )
                    )
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onPlayPauseToggle() }
                    .testTag("play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color(0xFF061A4E),
                    modifier = Modifier.size(32.dp)
                )
            }

            // Next Button
            IconButton(
                onClick = onNextTrack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF172C5B))
                    .border(1.dp, Color(0xFF2F4F91), CircleShape)
                    .testTag("next_track_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Repeat Label Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF13254E))
                    .border(1.dp, Color(0xFF233B70), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = repeatMode.label,
                    color = Color(0xFF88A0C7),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Volume Slider Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (volume <= 0.01f) Icons.Default.VolumeOff else if (volume < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                contentDescription = "Volume",
                tint = RoyalGold,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                modifier = Modifier
                    .weight(1f)
                    .testTag("music_volume_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = RoyalGold,
                    activeTrackColor = RoyalGold,
                    inactiveTrackColor = Color(0xFF1E3566)
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "${(volume * 100).toInt()}%",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp)
            )
        }
    }
}

/**
 * Category Pill Button
 */
@Composable
private fun CategoryPill(
    label: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) RoyalGold else Color(0xFF102146))
            .border(
                1.dp,
                if (isSelected) Color.White else Color(0xFF1F386E),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                color = if (isSelected) Color(0xFF061A4E) else Color.White,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

/**
 * Individual track row in playlist
 */
@Composable
private fun TrackListItemRow(
    track: BgmTrack,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isCurrentTrack) Color(0xFF152A5A) else Color(0xFF0A1838)
            )
            .border(
                1.dp,
                if (isCurrentTrack) RoyalGold else Color(0xFF14244B),
                RoundedCornerShape(10.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Emoji Icon or Playing Equalizer
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isCurrentTrack) RoyalGold.copy(alpha = 0.25f) else Color(0xFF112248)),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying) {
                Text("▶", color = RoyalGold, fontSize = 14.sp, fontWeight = FontWeight.Black)
            } else {
                Text(track.iconEmoji, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Center: Track Title & Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isCurrentTrack) RoyalGoldLight else Color.White,
                fontSize = 12.5.sp,
                fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "${track.singer} • ${track.instrumentalStyle}",
                color = if (isCurrentTrack) Color(0xFF90CAF9) else Color(0xFF88A0C7),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Right: Duration & Heart
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = track.durationLabel,
                color = Color(0xFF88A0C7),
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(28.dp)
            ) {
                Text(
                    text = if (isFavorite) "❤️" else "🤍",
                    fontSize = 14.sp
                )
            }
        }
    }
}
