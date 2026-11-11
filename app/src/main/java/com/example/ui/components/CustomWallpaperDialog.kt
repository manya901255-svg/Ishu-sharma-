package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.BUILT_IN_WALLPAPERS
import com.example.model.WallpaperCategory
import com.example.model.WallpaperItem
import com.example.model.WallpaperTarget
import com.example.ui.theme.*
import java.io.File

@Composable
fun CustomWallpaperDialog(
    initialTarget: WallpaperTarget = WallpaperTarget.HOME,
    wallpapersPerTarget: Map<String, String>,
    customUploadedWallpapers: List<String>,
    favoriteWallpaperIds: Set<String>,
    recentWallpaperIds: List<String>,
    onApplyWallpaper: (target: WallpaperTarget, idOrPath: String) -> Unit,
    onUploadImageUri: (Uri, WallpaperTarget) -> Unit,
    onDeleteUploadedWallpaper: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onResetTarget: (WallpaperTarget) -> Unit,
    onResetAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTarget by remember { mutableStateOf(initialTarget) }
    var selectedCategory by remember { mutableStateOf(WallpaperCategory.ALL) }
    var pickedImageForCropUri by remember { mutableStateOf<Uri?>(null) }

    // Android Photo Picker launcher (zero-permission Play-Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Verify file size & type
            try {
                val cr = context.contentResolver
                val type = cr.getType(uri) ?: "image/*"
                if (!type.startsWith("image/")) {
                    Toast.makeText(context, "Please select an image file (JPG, PNG, WebP)", Toast.LENGTH_SHORT).show()
                    return@rememberLauncherForActivityResult
                }
                pickedImageForCropUri = uri
            } catch (e: Exception) {
                pickedImageForCropUri = uri
            }
        }
    }

    // Active wallpaper for selected target
    val currentActiveId = wallpapersPerTarget[selectedTarget.id] ?: selectedTarget.defaultWallpaperId

    // All available wallpapers combined
    val allWallpapers: List<WallpaperItem> = remember(customUploadedWallpapers) {
        val userItems = customUploadedWallpapers.mapIndexed { idx, path ->
            WallpaperItem(
                id = path,
                title = "My Wallpaper #${idx + 1}",
                description = "Custom photo from phone gallery",
                localFilePath = path,
                isCustomUpload = true,
                tags = listOf("custom", "user", "photo")
            )
        }
        userItems + BUILT_IN_WALLPAPERS
    }

    // Filtered by active category
    val displayedWallpapers = remember(selectedCategory, allWallpapers, favoriteWallpaperIds, recentWallpaperIds) {
        when (selectedCategory) {
            WallpaperCategory.ALL -> allWallpapers
            WallpaperCategory.DEFAULT -> allWallpapers.filter { !it.isCustomUpload }
            WallpaperCategory.MY_UPLOADS -> allWallpapers.filter { it.isCustomUpload }
            WallpaperCategory.FAVORITES -> allWallpapers.filter { favoriteWallpaperIds.contains(it.id) }
            WallpaperCategory.RECENT -> {
                val recentMap = recentWallpaperIds.withIndex().associate { it.value to it.index }
                allWallpapers.filter { recentMap.containsKey(it.id) }
                    .sortedBy { recentMap[it.id] ?: Int.MAX_VALUE }
            }
        }
    }

    // Main Modal Dialog
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.82f))
                .systemBarsPadding()
                .testTag("custom_wallpaper_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.93f)
                    .shadow(24.dp, RoundedCornerShape(24.dp)),
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
                        .padding(14.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎨", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "CUSTOMIZE WALLPAPERS",
                                    color = RoyalGold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Personalize backgrounds for each game area",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
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

                    // 1. TARGET SELECTION TABS (Home, Board, Lobby, Profile, Private Room)
                    Text(
                        text = "SELECT AREA TO CUSTOMIZE:",
                        color = RoyalGoldLight,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WallpaperTarget.entries.forEach { target ->
                            val isSelected = target == selectedTarget
                            val hasCustom = wallpapersPerTarget.containsKey(target.id) &&
                                wallpapersPerTarget[target.id] != target.defaultWallpaperId

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            Brush.horizontalGradient(listOf(Color(0xFF29B6F6), Color(0xFF0288D1)))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color(0xFF0C2145), Color(0xFF142B57)))
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 1.8.dp else 1.dp,
                                        color = if (isSelected) RoyalGoldLight else Color(0x3340C4FF),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedTarget = target }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(target.iconEmoji, fontSize = 13.sp)
                                    Text(
                                        text = target.title,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                    )
                                    if (hasCustom) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF00E676))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. ACTIVE AREA STATUS & UPLOAD / RESET ACTION ROW
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1C3C)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFD54F))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Editing: ${selectedTarget.iconEmoji} ${selectedTarget.title}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedTarget.subtitle,
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Upload Button
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00C853)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Upload",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                // Reset Target Button
                                OutlinedButton(
                                    onClick = {
                                        onResetTarget(selectedTarget)
                                        Toast.makeText(context, "${selectedTarget.title} reset to default", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFD54F)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset",
                                        tint = RoyalGoldLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Reset", fontSize = 11.sp, color = RoyalGoldLight)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. CATEGORY CHIPS (All, Presets, My Uploads, Favorites, Recent)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WallpaperCategory.entries.forEach { category ->
                            val isSelected = category == selectedCategory
                            val count = when (category) {
                                WallpaperCategory.ALL -> allWallpapers.size
                                WallpaperCategory.DEFAULT -> BUILT_IN_WALLPAPERS.size
                                WallpaperCategory.MY_UPLOADS -> customUploadedWallpapers.size
                                WallpaperCategory.FAVORITES -> favoriteWallpaperIds.size
                                WallpaperCategory.RECENT -> recentWallpaperIds.size
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = {
                                    Text(
                                        text = "${category.iconEmoji} ${category.title} ($count)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF0F2752),
                                    labelColor = Color.White.copy(alpha = 0.85f),
                                    selectedContainerColor = RoyalGold,
                                    selectedLabelColor = Color(0xFF101B2E)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0x33FFD54F),
                                    selectedBorderColor = RoyalGoldLight
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. WALLPAPERS GRID
                    if (displayedWallpapers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🖼️", fontSize = 36.sp)
                                Text(
                                    text = if (selectedCategory == WallpaperCategory.MY_UPLOADS) {
                                        "No uploaded wallpapers yet.\nTap 'Upload' to pick one from your phone!"
                                    } else if (selectedCategory == WallpaperCategory.FAVORITES) {
                                        "No favorites marked yet.\nTap the heart icon on any wallpaper to add it here!"
                                    } else {
                                        "No wallpapers in this category."
                                    },
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(displayedWallpapers, key = { it.id }) { item ->
                                val isEquipped = item.id == currentActiveId
                                val isFavorite = favoriteWallpaperIds.contains(item.id)

                                WallpaperGridCard(
                                    item = item,
                                    isEquipped = isEquipped,
                                    isFavorite = isFavorite,
                                    target = selectedTarget,
                                    onSelect = {
                                        onApplyWallpaper(selectedTarget, item.id)
                                        Toast.makeText(context, "Applied to ${selectedTarget.title}!", Toast.LENGTH_SHORT).show()
                                    },
                                    onToggleFavorite = { onToggleFavorite(item.id) },
                                    onDelete = if (item.isCustomUpload) {
                                        { onDeleteUploadedWallpaper(item.id) }
                                    } else null
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. FOOTER ACTIONS (Reset All / Done)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                onResetAll()
                                Toast.makeText(context, "All wallpapers reset to default!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Reset All",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset All Areas", color = Color(0xFFFF5252), fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .height(40.dp)
                                .shadow(8.dp, RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalGold
                            )
                        ) {
                            Text("Done", color = Color(0xFF141F32), fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // 6. INTERACTIVE ZOOM / CROP / PAN PREVIEW MODAL
    pickedImageForCropUri?.let { uri ->
        ImageCropAdjusterModal(
            imageUri = uri,
            target = selectedTarget,
            onApply = {
                onUploadImageUri(uri, selectedTarget)
                pickedImageForCropUri = null
                Toast.makeText(context, "Custom wallpaper applied to ${selectedTarget.title}!", Toast.LENGTH_SHORT).show()
            },
            onCancel = { pickedImageForCropUri = null }
        )
    }
}

/**
 * Single Card item in the Wallpaper Grid.
 */
@Composable
private fun WallpaperGridCard(
    item: WallpaperItem,
    isEquipped: Boolean,
    isFavorite: Boolean,
    target: WallpaperTarget,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(if (isEquipped) 12.dp else 4.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isEquipped) 2.5.dp else 1.dp,
                color = if (isEquipped) RoyalGold else Color(0x3340C4FF),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF091730))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Visual
            when {
                item.isCustomUpload && item.localFilePath != null -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(item.localFilePath))
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                item.id == "default_royal_navy" -> {
                    MgLudoRoyalBackground(modifier = Modifier.fillMaxSize())
                }
                item.drawableRes != null -> {
                    Image(
                        painter = painterResource(id = item.drawableRes),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                item.gradientColors != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(item.gradientColors.map { Color(it) })
                            )
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0B1938))
                    )
                }
            }

            // Top Shade for buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                        )
                    )
            )

            // Top Controls (Favorite heart & Delete trash)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Heart Favorite
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFF1744) else Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Delete custom
                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Bottom Info Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f), Color.Black.copy(alpha = 0.95f))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isEquipped) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RoyalGold)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFF101B2E),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Text(
                    text = item.description,
                    color = TextMuted,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Interactive Image Crop, Zoom, and Pan Adjuster Modal.
 * Allows users to inspect high-resolution images, pinch to zoom, pan to position,
 * and preview how it will appear on the selected target area.
 */
@Composable
private fun ImageCropAdjusterModal(
    imageUri: Uri,
    target: WallpaperTarget,
    onApply: () -> Unit,
    onCancel: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PREVIEW & ADJUST WALLPAPER",
                            color = RoyalGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Target: ${target.iconEmoji} ${target.title} • Pinch to zoom & pan",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onCancel) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Zoom/Pan Canvas Container
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF040B18)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, RoyalGold)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 4f)
                                    offsetX = (offsetX + pan.x).coerceIn(-500f, 500f)
                                    offsetY = (offsetY + pan.y).coerceIn(-500f, 500f)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Preview Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                        )

                        // Visual grid guide overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            // Soft grid lines
                            drawLine(
                                color = Color.White.copy(alpha = 0.2f),
                                start = Offset(w / 3f, 0f),
                                end = Offset(w / 3f, h),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.2f),
                                start = Offset(2f * w / 3f, 0f),
                                end = Offset(2f * w / 3f, h),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.2f),
                                start = Offset(0f, h / 3f),
                                end = Offset(w, h / 3f),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.2f),
                                start = Offset(0f, 2f * h / 3f),
                                end = Offset(w, 2f * h / 3f),
                                strokeWidth = 1f
                            )
                        }

                        // Zoom level badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Zoom: ${String.format("%.1fx", scale)}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick zoom controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scale = (scale - 0.25f).coerceAtLeast(1f) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10274E))
                    ) {
                        Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedButton(
                        onClick = {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFD54F))
                    ) {
                        Text("Reset Position", fontSize = 11.sp, color = RoyalGoldLight)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = { scale = (scale + 0.25f).coerceAtMost(4f) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10274E))
                    ) {
                        Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom confirmation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onApply,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply to ${target.title}", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
