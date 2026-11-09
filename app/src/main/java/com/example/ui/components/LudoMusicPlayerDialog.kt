package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.BGM_PLAYLIST
import com.example.model.BgmTrack
import com.example.model.MusicCategory
import com.example.model.MusicRepeatMode
import com.example.model.UserPlaylist
import com.example.ui.theme.*

/**
 * LudoMusicPlayerDialog: Advanced Indian Online Music Streaming System for MG Ludo.
 *
 * Features:
 * 1. 15 Indian Music Categories (Bollywood, Old Hindi, Punjabi, Bhojpuri, Haryanvi, Devotional, etc.)
 * 2. Live Online Music Streaming via authorized CDN streaming audio URLs
 * 3. Real-time Search by song name, singer, album, or movie
 * 4. Advanced Player with Seek Bar, duration, play/pause, next/previous, repeat, shuffle & volume
 * 5. High-Resolution Album Artwork via Coil AsyncImage
 * 6. Playlists (Trending, Curated, Custom User Playlists, Recently Played, Favorites)
 * 7. Background audio playback continuing across all Ludo game screens
 */
@Composable
fun LudoMusicPlayerDialog(
    currentTrack: BgmTrack? = null,
    isPlaying: Boolean,
    repeatMode: MusicRepeatMode,
    isShuffleEnabled: Boolean = false,
    volume: Float,
    favoriteSongIds: Set<String>,
    isBuffering: Boolean = false,
    playbackPositionMs: Int = 0,
    playbackDurationMs: Int = 0,
    selectedCategory: MusicCategory = MusicCategory.TRENDING,
    categoryTracks: List<BgmTrack> = emptyList(),
    isLoadingCategory: Boolean = false,
    searchResults: List<BgmTrack> = emptyList(),
    isSearching: Boolean = false,
    searchQuery: String = "",
    recentlyPlayed: List<BgmTrack> = emptyList(),
    userPlaylists: List<UserPlaylist> = emptyList(),
    favoriteTracks: List<BgmTrack> = emptyList(),
    onPlayPauseToggle: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onSelectTrack: (Int) -> Unit = {},
    onPlayTrackDirect: (BgmTrack, List<BgmTrack>) -> Unit = { _, _ -> },
    onSeekPosition: (Int) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSelectCategory: (MusicCategory) -> Unit = {},
    onCreatePlaylist: (String) -> Unit = {},
    onAddTrackToPlaylist: (String, BgmTrack) -> Unit = { _, _ -> },
    onRemoveTrackFromPlaylist: (String, String) -> Unit = { _, _ -> },
    onRepeatModeCycle: () -> Unit,
    onToggleShuffle: () -> Unit = {},
    onVolumeChange: (Float) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleFavoriteTrack: (BgmTrack) -> Unit = {},
    onRestoreDefaultPlaylist: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0: Explore/Categories, 1: Search, 2: Playlists, 3: Favorites
    var showAddToPlaylistDialog by remember { mutableStateOf<BgmTrack?>(null) }
    var showCreatePlaylistModal by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var selectedPlaylistForView by remember { mutableStateOf<UserPlaylist?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .shadow(24.dp, RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF070E24)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(
                            listOf(RoyalGold, Color(0xFF1E3A70), RoyalGoldDark)
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
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
                                        text = "MG Indian Music Lounge",
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
                                            text = "100% LEGAL & LICENSED (CC/CC0)",
                                            color = Color(0xFF00E676),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Text(
                                    text = "Free Legal Indian Music • Hindi, Punjabi, Bhojpuri, Marathi & Regional",
                                    color = RoyalGoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF172C5B))
                                .border(1.dp, Color(0xFF2F4F91), CircleShape)
                                .testTag("close_music_dialog_btn")
                        ) {
                            Text("✕", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Top Navigation Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0C193B))
                            .border(1.dp, Color(0xFF1E3A70), RoundedCornerShape(12.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val tabs = listOf(
                            Triple(0, "Explore", "🪕"),
                            Triple(1, "Search", "🔍"),
                            Triple(2, "Playlists", "📑"),
                            Triple(3, "Favorites", "❤️")
                        )
                        tabs.forEach { (index, title, emoji) ->
                            val isSelected = activeTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) Brush.horizontalGradient(listOf(Color(0xFF1976D2), Color(0xFF0D47A1)))
                                        else androidx.compose.ui.graphics.SolidColor(Color.Transparent)
                                    )
                                    .clickable { activeTab = index }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(emoji, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = title,
                                        color = if (isSelected) RoyalGold else Color(0xFF88A0C7),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Main Content Area depending on Active Tab
                    Box(modifier = Modifier.weight(1f)) {
                        when (activeTab) {
                            0 -> ExploreTabContent(
                                selectedCategory = selectedCategory,
                                categoryTracks = categoryTracks,
                                isLoadingCategory = isLoadingCategory,
                                currentTrack = currentTrack,
                                isPlaying = isPlaying,
                                favoriteSongIds = favoriteSongIds,
                                onSelectCategory = onSelectCategory,
                                onPlayTrack = { track -> onPlayTrackDirect(track, categoryTracks) },
                                onToggleFavorite = { track ->
                                    onToggleFavorite(track.id)
                                    onToggleFavoriteTrack(track)
                                },
                                onAddToPlaylist = { showAddToPlaylistDialog = it }
                            )
                            1 -> SearchTabContent(
                                searchQuery = searchQuery,
                                searchResults = searchResults,
                                isSearching = isSearching,
                                currentTrack = currentTrack,
                                isPlaying = isPlaying,
                                favoriteSongIds = favoriteSongIds,
                                onSearchQueryChange = onSearchQueryChange,
                                onPlayTrack = { track -> onPlayTrackDirect(track, searchResults) },
                                onToggleFavorite = { track ->
                                    onToggleFavorite(track.id)
                                    onToggleFavoriteTrack(track)
                                },
                                onAddToPlaylist = { showAddToPlaylistDialog = it }
                            )
                            2 -> PlaylistsTabContent(
                                userPlaylists = userPlaylists,
                                recentlyPlayed = recentlyPlayed,
                                favoriteTracks = favoriteTracks,
                                currentTrack = currentTrack,
                                isPlaying = isPlaying,
                                favoriteSongIds = favoriteSongIds,
                                selectedPlaylist = selectedPlaylistForView,
                                onSelectPlaylistForView = { selectedPlaylistForView = it },
                                onCreatePlaylistClick = { showCreatePlaylistModal = true },
                                onRestoreDefaultPlaylist = onRestoreDefaultPlaylist,
                                onPlayTrack = { track, list -> onPlayTrackDirect(track, list) },
                                onToggleFavorite = { track ->
                                    onToggleFavorite(track.id)
                                    onToggleFavoriteTrack(track)
                                },
                                onRemoveFromPlaylist = { plId, trackId -> onRemoveTrackFromPlaylist(plId, trackId) }
                            )
                            3 -> FavoritesTabContent(
                                favoriteTracks = favoriteTracks,
                                currentTrack = currentTrack,
                                isPlaying = isPlaying,
                                favoriteSongIds = favoriteSongIds,
                                onPlayTrack = { track -> onPlayTrackDirect(track, favoriteTracks) },
                                onToggleFavorite = { track ->
                                    onToggleFavorite(track.id)
                                    onToggleFavoriteTrack(track)
                                },
                                onAddToPlaylist = { showAddToPlaylistDialog = it }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Advanced Docked Music Player Bar with Seek Bar and Playback Controls
                    AdvancedBottomPlayerBar(
                        currentTrack = currentTrack,
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        repeatMode = repeatMode,
                        isShuffleEnabled = isShuffleEnabled,
                        volume = volume,
                        playbackPositionMs = playbackPositionMs,
                        playbackDurationMs = playbackDurationMs,
                        isFavorite = currentTrack != null && favoriteSongIds.contains(currentTrack.id),
                        onPlayPauseToggle = onPlayPauseToggle,
                        onNextTrack = onNextTrack,
                        onPreviousTrack = onPreviousTrack,
                        onRepeatModeCycle = onRepeatModeCycle,
                        onToggleShuffle = onToggleShuffle,
                        onVolumeChange = onVolumeChange,
                        onSeekPosition = onSeekPosition,
                        onToggleFavorite = {
                            if (currentTrack != null) {
                                onToggleFavorite(currentTrack.id)
                                onToggleFavoriteTrack(currentTrack)
                            }
                        }
                    )
                }
            }
        }
    }

    // Modal: Add to Custom Playlist
    if (showAddToPlaylistDialog != null) {
        val targetTrack = showAddToPlaylistDialog!!
        AlertDialog(
            onDismissRequest = { showAddToPlaylistDialog = null },
            title = { Text("Add to Playlist", color = RoyalGold, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Add '${targetTrack.title}' to your custom playlist:",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (userPlaylists.isEmpty()) {
                        Text(
                            text = "No custom playlists yet. Tap below to create one!",
                            color = Color(0xFF88A0C7),
                            fontSize = 12.sp
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                            items(userPlaylists) { pl ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF13254E))
                                        .clickable {
                                            onAddTrackToPlaylist(pl.id, targetTrack)
                                            showAddToPlaylistDialog = null
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(pl.iconEmoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = pl.name,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("+ Add", color = RoyalGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showAddToPlaylistDialog = null
                    showCreatePlaylistModal = true
                }) {
                    Text("+ Create New Playlist", color = RoyalGold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddToPlaylistDialog = null }) {
                    Text("Cancel", color = Color(0xFF88A0C7))
                }
            },
            containerColor = Color(0xFF0C193B)
        )
    }

    // Modal: Create New Playlist
    if (showCreatePlaylistModal) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistModal = false },
            title = { Text("Create New Playlist", color = RoyalGold, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        placeholder = { Text("e.g. Punjabi Party, Romantic Evening", color = Color(0xFF88A0C7)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = RoyalGold,
                            unfocusedBorderColor = Color(0xFF1E3A70)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreatePlaylistModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold)
                ) {
                    Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistModal = false }) {
                    Text("Cancel", color = Color(0xFF88A0C7))
                }
            },
            containerColor = Color(0xFF0C193B)
        )
    }
}

/**
 * 1. Explore Tab: Horizontal category chips & list of songs in selected Indian category
 */
@Composable
private fun ExploreTabContent(
    selectedCategory: MusicCategory,
    categoryTracks: List<BgmTrack>,
    isLoadingCategory: Boolean,
    currentTrack: BgmTrack?,
    isPlaying: Boolean,
    favoriteSongIds: Set<String>,
    onSelectCategory: (MusicCategory) -> Unit,
    onPlayTrack: (BgmTrack) -> Unit,
    onToggleFavorite: (BgmTrack) -> Unit,
    onAddToPlaylist: (BgmTrack) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Categories Horizontal Scrollable Chips with regional Indian languages
        val categories = listOf(
            MusicCategory.TRENDING,
            MusicCategory.BOLLYWOOD_NEW,
            MusicCategory.BOLLYWOOD_OLD,
            MusicCategory.HINDI,
            MusicCategory.PUNJABI,
            MusicCategory.HARYANVI,
            MusicCategory.BHOJPURI,
            MusicCategory.RAJASTHANI,
            MusicCategory.MARATHI,
            MusicCategory.SOUTH_INDIAN,
            MusicCategory.DEVOTIONAL,
            MusicCategory.ROMANTIC,
            MusicCategory.SAD,
            MusicCategory.PARTY_DJ,
            MusicCategory.INSTRUMENTAL
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) Brush.horizontalGradient(listOf(RoyalGold, Color(0xFFFF8F00)))
                            else Brush.horizontalGradient(listOf(Color(0xFF13254E), Color(0xFF0F1E3D)))
                        )
                        .border(
                            1.2.dp,
                            if (isSelected) Color.White else Color(0xFF1E3A70),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onSelectCategory(category) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(category.icon, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = category.label,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Content
        if (isLoadingCategory) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = RoyalGold, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading ${selectedCategory.label} Songs...", color = Color(0xFF90CAF9), fontSize = 12.sp)
                }
            }
        } else if (categoryTracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No songs found for ${selectedCategory.label}.", color = Color(0xFF88A0C7), fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(categoryTracks) { index, track ->
                    SongItemCard(
                        track = track,
                        index = index + 1,
                        isCurrentTrack = currentTrack?.id == track.id,
                        isPlaying = isPlaying && currentTrack?.id == track.id,
                        isFavorite = favoriteSongIds.contains(track.id),
                        onPlayClick = { onPlayTrack(track) },
                        onToggleFavorite = { onToggleFavorite(track) },
                        onAddToPlaylist = { onAddToPlaylist(track) }
                    )
                }
            }
        }
    }
}

/**
 * 2. Search Tab: Search Bar + Live Online Results
 */
@Composable
private fun SearchTabContent(
    searchQuery: String,
    searchResults: List<BgmTrack>,
    isSearching: Boolean,
    currentTrack: BgmTrack?,
    isPlaying: Boolean,
    favoriteSongIds: Set<String>,
    onSearchQueryChange: (String) -> Unit,
    onPlayTrack: (BgmTrack) -> Unit,
    onToggleFavorite: (BgmTrack) -> Unit,
    onAddToPlaylist: (BgmTrack) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search song, singer, movie, album...", color = Color(0xFF88A0C7), fontSize = 12.5.sp) },
            leadingIcon = { Text("🔍", fontSize = 16.sp) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Text("✕", color = Color(0xFF88A0C7), fontSize = 13.sp)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = RoyalGold,
                unfocusedBorderColor = Color(0xFF1E3A70),
                focusedContainerColor = Color(0xFF0C193B),
                unfocusedContainerColor = Color(0xFF0C193B)
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = RoyalGold, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Searching Indian Music Catalog...", color = Color(0xFF90CAF9), fontSize = 12.sp)
                }
            }
        } else if (searchQuery.isNotBlank() && searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No songs found for '$searchQuery'.\nTry another singer, song, or movie name.",
                    color = Color(0xFF88A0C7),
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else if (searchResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(searchResults) { index, track ->
                    SongItemCard(
                        track = track,
                        index = index + 1,
                        isCurrentTrack = currentTrack?.id == track.id,
                        isPlaying = isPlaying && currentTrack?.id == track.id,
                        isFavorite = favoriteSongIds.contains(track.id),
                        onPlayClick = { onPlayTrack(track) },
                        onToggleFavorite = { onToggleFavorite(track) },
                        onAddToPlaylist = { onAddToPlaylist(track) }
                    )
                }
            }
        } else {
            // Suggested Quick Searches
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Popular Searches", color = RoyalGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val popularSuggestions = listOf("Arijit Singh", "Kesariya", "Sidhu Moosewala", "Hanuman Chalisa", "Kishore Kumar", "Jawan Chaleya", "Shreya Ghoshal", "Bhojpuri Superhits")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    popularSuggestions.take(4).forEach { tag ->
                        SuggestionChip(
                            onClick = { onSearchQueryChange(tag) },
                            label = { Text(tag, color = Color.White, fontSize = 10.sp) },
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    popularSuggestions.drop(4).forEach { tag ->
                        SuggestionChip(
                            onClick = { onSearchQueryChange(tag) },
                            label = { Text(tag, color = Color.White, fontSize = 10.sp) },
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. Playlists Tab: Curated & User Created Playlists & Recently Played
 */
@Composable
private fun PlaylistsTabContent(
    userPlaylists: List<UserPlaylist>,
    recentlyPlayed: List<BgmTrack>,
    favoriteTracks: List<BgmTrack>,
    currentTrack: BgmTrack?,
    isPlaying: Boolean,
    favoriteSongIds: Set<String>,
    selectedPlaylist: UserPlaylist?,
    onSelectPlaylistForView: (UserPlaylist?) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onRestoreDefaultPlaylist: () -> Unit = {},
    onPlayTrack: (BgmTrack, List<BgmTrack>) -> Unit,
    onToggleFavorite: (BgmTrack) -> Unit,
    onRemoveFromPlaylist: (String, String) -> Unit
) {
    if (selectedPlaylist != null) {
        // Detailed Playlist View
        val playlistTracks = (recentlyPlayed + favoriteTracks).distinctBy { it.id }.filter { selectedPlaylist.trackIds.contains(it.id) }
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onSelectPlaylistForView(null) }) {
                    Text("←", color = RoyalGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "${selectedPlaylist.iconEmoji} ${selectedPlaylist.name}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text("${playlistTracks.size} songs", color = Color(0xFF88A0C7), fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (playlistTracks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "This playlist is empty.\nSearch or explore songs and tap '+ Add' to include them!",
                        color = Color(0xFF88A0C7),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(playlistTracks) { index, track ->
                        SongItemCard(
                            track = track,
                            index = index + 1,
                            isCurrentTrack = currentTrack?.id == track.id,
                            isPlaying = isPlaying && currentTrack?.id == track.id,
                            isFavorite = favoriteSongIds.contains(track.id),
                            onPlayClick = { onPlayTrack(track, playlistTracks) },
                            onToggleFavorite = { onToggleFavorite(track) },
                            onAddToPlaylist = {}
                        )
                    }
                }
            }
        }
    } else {
        // Playlists Overview
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Restored Royal & Indian Soundtrack Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Restored Royal & Indian Playlist", color = RoyalGold, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                        Text("${BGM_PLAYLIST.size} songs • Continuous in-game playback", color = Color(0xFF00E676), fontSize = 10.sp)
                    }
                    Button(
                        onClick = onRestoreDefaultPlaylist,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("▶ Play All", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            itemsIndexed(BGM_PLAYLIST) { index, track ->
                SongItemCard(
                    track = track,
                    index = index + 1,
                    isCurrentTrack = currentTrack?.id == track.id,
                    isPlaying = isPlaying && currentTrack?.id == track.id,
                    isFavorite = favoriteSongIds.contains(track.id),
                    onPlayClick = { onPlayTrack(track, BGM_PLAYLIST) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    onAddToPlaylist = {}
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Custom Playlists", color = RoyalGold, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    Row {
                        Button(
                            onClick = onRestoreDefaultPlaylist,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3A28)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🔄 Restore", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = onCreatePlaylistClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13254E)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("+ New Playlist", color = RoyalGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (userPlaylists.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0C193B))
                            .border(1.dp, Color(0xFF1E3A70), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No custom playlists created yet. Tap '+ New Playlist' to create one!", color = Color(0xFF88A0C7), fontSize = 12.sp)
                    }
                }
            } else {
                items(userPlaylists) { pl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0C193B))
                            .border(1.dp, Color(0xFF1E3A70), RoundedCornerShape(12.dp))
                            .clickable { onSelectPlaylistForView(pl) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pl.iconEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pl.name, color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            Text("${pl.trackIds.size} songs", color = RoyalGoldLight, fontSize = 11.sp)
                        }
                        Text("View →", color = RoyalGold, fontSize = 12.sp)
                    }
                }
            }

            // Recently Played Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Recently Played (Online & Songs)", color = RoyalGold, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            }

            if (recentlyPlayed.isEmpty()) {
                item {
                    Text("No recently played songs yet.", color = Color(0xFF88A0C7), fontSize = 12.sp)
                }
            } else {
                itemsIndexed(recentlyPlayed.take(10)) { index, track ->
                    SongItemCard(
                        track = track,
                        index = index + 1,
                        isCurrentTrack = currentTrack?.id == track.id,
                        isPlaying = isPlaying && currentTrack?.id == track.id,
                        isFavorite = favoriteSongIds.contains(track.id),
                        onPlayClick = { onPlayTrack(track, recentlyPlayed) },
                        onToggleFavorite = { onToggleFavorite(track) },
                        onAddToPlaylist = {}
                    )
                }
            }
        }
    }
}

/**
 * 4. Favorites Tab: Saved Indian songs
 */
@Composable
private fun FavoritesTabContent(
    favoriteTracks: List<BgmTrack>,
    currentTrack: BgmTrack?,
    isPlaying: Boolean,
    favoriteSongIds: Set<String>,
    onPlayTrack: (BgmTrack) -> Unit,
    onToggleFavorite: (BgmTrack) -> Unit,
    onAddToPlaylist: (BgmTrack) -> Unit
) {
    if (favoriteTracks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("❤️", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No favorite songs saved yet.\nTap the heart icon on any song to save it here!",
                    color = Color(0xFF88A0C7),
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(favoriteTracks) { index, track ->
                SongItemCard(
                    track = track,
                    index = index + 1,
                    isCurrentTrack = currentTrack?.id == track.id,
                    isPlaying = isPlaying && currentTrack?.id == track.id,
                    isFavorite = true,
                    onPlayClick = { onPlayTrack(track) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    onAddToPlaylist = { onAddToPlaylist(track) }
                )
            }
        }
    }
}

/**
 * Individual Song Item Card with Album Art, Title, Artist, Duration, Play and Actions
 */
@Composable
private fun SongItemCard(
    track: BgmTrack,
    index: Int,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onPlayClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrentTrack) Brush.horizontalGradient(listOf(Color(0xFF132D63), Color(0xFF0F1E3D)))
                else androidx.compose.ui.graphics.SolidColor(Color(0xFF0C193B))
            )
            .border(
                width = if (isCurrentTrack) 1.5.dp else 0.8.dp,
                color = if (isCurrentTrack) RoyalGold else Color(0xFF1E3A70),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onPlayClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail: Coil AsyncImage or Emoji
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1A237E))
                .border(1.dp, if (isCurrentTrack) RoyalGold else Color(0xFF283593), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!track.albumArtUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = track.albumArtUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(track.iconEmoji, fontSize = 20.sp)
            }

            if (isCurrentTrack && isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    MiniEqualizerBars()
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Title and details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isCurrentTrack) RoyalGold else Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.subtitle,
                color = Color(0xFFB0BEC5),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 1.dp)
            ) {
                // License Pill (Green)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF00E676).copy(alpha = 0.15f))
                        .border(0.6.dp, Color(0xFF00E676), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "📜 ${track.licenseType}",
                        color = Color(0xFF00E676),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (track.year.isNotEmpty()) "${track.year} • ${track.durationLabel}" else track.durationLabel,
                    color = Color(0xFF78909C),
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Add to Playlist Button
        IconButton(
            onClick = onAddToPlaylist,
            modifier = Modifier.size(30.dp)
        ) {
            Text("➕", fontSize = 12.sp)
        }

        // Heart Favorite Toggle
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(30.dp)
        ) {
            Text(if (isFavorite) "❤️" else "🤍", fontSize = 14.sp)
        }

        // Quick Play/Pause Icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isCurrentTrack && isPlaying) RoyalGold else Color(0xFF172C5B))
                .clickable(onClick = onPlayClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isCurrentTrack && isPlaying) "⏸" else "▶",
                color = if (isCurrentTrack && isPlaying) Color.Black else Color.White,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 5. Advanced Bottom Docked Player Bar with Live Seek Bar and Full Controls
 */
@Composable
private fun AdvancedBottomPlayerBar(
    currentTrack: BgmTrack?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    repeatMode: MusicRepeatMode,
    isShuffleEnabled: Boolean,
    volume: Float,
    playbackPositionMs: Int,
    playbackDurationMs: Int,
    isFavorite: Boolean,
    onPlayPauseToggle: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onRepeatModeCycle: () -> Unit,
    onToggleShuffle: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSeekPosition: (Int) -> Unit,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0C193B))
            .border(1.2.dp, Color(0xFF1E3A70), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // Track Info Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A237E))
                    .border(1.dp, RoyalGold, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (currentTrack?.albumArtUrl?.isNotEmpty() == true) {
                    AsyncImage(
                        model = currentTrack.albumArtUrl,
                        contentDescription = currentTrack.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(currentTrack?.iconEmoji ?: "🎵", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentTrack?.title ?: "Select any song to stream",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentTrack?.subtitle ?: "All Indian Categories Available",
                    color = RoyalGoldLight,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (currentTrack != null) {
                    Text(
                        text = "📜 ${currentTrack.licenseType} • Free App Streaming",
                        color = Color(0xFF00E676),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (currentTrack != null) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                    Text(if (isFavorite) "❤️" else "🤍", fontSize = 16.sp)
                }
            }
        }

        // Live Seek Bar Slider
        val currentSec = playbackPositionMs / 1000
        val totalSec = if (playbackDurationMs > 0) playbackDurationMs / 1000 else (currentTrack?.durationSeconds ?: 210)
        val curMins = currentSec / 60
        val curRemSec = currentSec % 60
        val totMins = totalSec / 60
        val totRemSec = totalSec % 60
        val posFormatted = String.format("%d:%02d", curMins, curRemSec)
        val durFormatted = String.format("%d:%02d", totMins, totRemSec)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(posFormatted, color = Color(0xFF90CAF9), fontSize = 10.sp)
            Slider(
                value = if (totalSec > 0) currentSec.toFloat() / totalSec.toFloat() else 0f,
                onValueChange = { fraction ->
                    val targetMs = (fraction * totalSec * 1000).toInt()
                    onSeekPosition(targetMs)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .padding(horizontal = 6.dp),
                colors = SliderDefaults.colors(
                    thumbColor = RoyalGold,
                    activeTrackColor = RoyalGold,
                    inactiveTrackColor = Color(0xFF1E3A70)
                )
            )
            Text(durFormatted, color = Color(0xFF88A0C7), fontSize = 10.sp)
        }

        // Playback Controls Row: Shuffle, Prev, Play/Pause/Buffer, Next, Repeat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(
                onClick = onToggleShuffle,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isShuffleEnabled) Color(0xFF2E2405) else Color(0xFF13254E))
                    .border(1.dp, if (isShuffleEnabled) RoyalGold else Color.Transparent, CircleShape)
            ) {
                Text("🔀", fontSize = 13.sp)
            }

            // Prev
            IconButton(
                onClick = onPreviousTrack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF172C5B))
                    .border(1.dp, Color(0xFF2F4F91), CircleShape)
            ) {
                Text("⏮", color = Color.White, fontSize = 14.sp)
            }

            // Big Play/Pause with Buffering indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(RoyalGold, Color(0xFFFF8F00), Color(0xFF8C5300)))
                    )
                    .border(2.dp, Color.White, CircleShape)
                    .clickable(onClick = onPlayPauseToggle),
                contentAlignment = Alignment.Center
            ) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (isPlaying) "⏸" else "▶",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Next
            IconButton(
                onClick = onNextTrack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF172C5B))
                    .border(1.dp, Color(0xFF2F4F91), CircleShape)
            ) {
                Text("⏭", color = Color.White, fontSize = 14.sp)
            }

            // Repeat
            IconButton(
                onClick = onRepeatModeCycle,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF13254E))
            ) {
                Text(repeatMode.icon, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Music Volume Slider (Separate from Game sounds)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔊", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Music Vol", color = Color(0xFF90CAF9), fontSize = 10.sp)
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .padding(horizontal = 6.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF90CAF9),
                    activeTrackColor = Color(0xFF1976D2),
                    inactiveTrackColor = Color(0xFF1E3A70)
                )
            )
            Text("${(volume * 100).toInt()}%", color = Color.White, fontSize = 10.sp)
        }
    }
}

/**
 * Animated Equalizer Bars for Active Song
 */
@Composable
private fun MiniEqualizerBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(360, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(20.dp)
    ) {
        listOf(h1, h2, h3).forEach { barHeight ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(RoyalGold)
            )
        }
    }
}
