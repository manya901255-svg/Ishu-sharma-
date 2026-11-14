package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.*
import com.example.ui.components.FramedAvatarBox
import com.example.ui.components.Realistic3DDice
import com.example.ui.components.RewardedAdDialog
import com.example.ui.components.formatCompactCoins
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.viewmodel.InventoryUiState
import com.example.viewmodel.LudoUiState
import com.example.viewmodel.LudoViewModel
import java.util.UUID

@Composable
fun InventoryScreen(
    uiState: LudoUiState,
    viewModel: LudoViewModel,
    onBack: () -> Unit
) {
    val invState = uiState.inventoryUiState
    val selectedCategory = invState.selectedCategory

    var selectedDeckSlot by remember { mutableIntStateOf(0) }
    var swapSourceSlot by remember { mutableIntStateOf(-1) }
    var itemPendingConfirmation by remember { mutableStateOf<InventoryItem?>(null) }
    var showTopUpDialog by remember { mutableStateOf(false) }
    var topUpCurrency by remember { mutableStateOf(CurrencyType.COINS) }
    var showAdDialog by remember { mutableStateOf(false) }
    var adRewardCoins by remember { mutableLongStateOf(1000L) }
    var adRewardCash by remember { mutableIntStateOf(0) }
    var adRewardTitle by remember { mutableStateOf("1,000 Gold Coins + Progress") }

    val baseItemsForCategory = remember(selectedCategory, invState.ownedItemIds, uiState.diceStoreUiState.catalog) {
        if (selectedCategory == InventoryCategory.OWNED) {
            ItemCatalog.ALL_ITEMS.filter { invState.isOwned(it.id) }
        } else {
            ItemCatalog.itemsForCategory(selectedCategory)
        }
    }

    val filteredItems = remember(
        baseItemsForCategory,
        invState.searchQuery,
        invState.selectedFilter,
        invState.ownedItemIds
    ) {
        val query = invState.searchQuery.trim().lowercase()
        baseItemsForCategory.filter { item ->
            val matchesQuery = query.isEmpty() ||
                item.name.lowercase().contains(query) ||
                item.description.lowercase().contains(query)

            val owned = invState.isOwned(item.id)
            val matchesFilter = when (invState.selectedFilter) {
                InventoryFilter.ALL -> true
                InventoryFilter.OWNED -> owned
                InventoryFilter.UNOWNED -> !owned
                InventoryFilter.SPECIAL -> item.isSpecial || item.rarity == ItemRarity.LEGENDARY || item.rarity == ItemRarity.MYTHIC
                InventoryFilter.EVENTS -> item.isEventOnly || item.eventId != null || item.currencyType == CurrencyType.EVENT
                InventoryFilter.ADS -> item.currencyType == CurrencyType.AD_REWARD
                InventoryFilter.CHESTS -> item.unlockRequirement.contains("Chest", ignoreCase = true) || item.currencyType == CurrencyType.CHEST
            }
            matchesQuery && matchesFilter
        }
    }

    val totalInCategory = remember(selectedCategory, uiState.diceStoreUiState.catalog) {
        if (selectedCategory == InventoryCategory.OWNED) ItemCatalog.ALL_ITEMS.size
        else ItemCatalog.itemsForCategory(selectedCategory).size
    }
    val ownedCountInCategory = remember(selectedCategory, invState.ownedItemIds) {
        if (selectedCategory == InventoryCategory.OWNED) invState.totalUniqueOwnedCount()
        else invState.uniqueOwnedCountFor(selectedCategory)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A1647),
                        Color(0xFF1A1456),
                        Color(0xFF2B1058),
                        Color(0xFF120C3A)
                    )
                )
            )
            .systemBarsPadding()
            .testTag("inventory_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TOP HEADER
            InventoryTopHeader(
                goldCoins = uiState.userCoins,
                cashNotes = uiState.userGems,
                onBack = onBack,
                onAddCoins = {
                    topUpCurrency = CurrencyType.COINS
                    showTopUpDialog = true
                },
                onAddCash = {
                    topUpCurrency = CurrencyType.CASH
                    showTopUpDialog = true
                }
            )

            // 2. CATEGORY TABS
            InventoryCategoryTabsRow(
                selectedCategory = selectedCategory,
                onSelectCategory = { viewModel.selectInventoryCategory(it) }
            )

            // 3. ERROR / STATUS BANNER
            AnimatedVisibility(visible = invState.errorMessage != null) {
                invState.errorMessage?.let { err ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFB71C1C))
                            .border(1.dp, Color(0xFFFF8A80), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("inventory_error_banner"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "⚠️ $err",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearInventoryError() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 4. COMMON COLLECTION HEADER + SEARCH + FILTERS
            InventorySearchAndFiltersSection(
                category = selectedCategory,
                ownedCount = ownedCountInCategory,
                totalCount = totalInCategory,
                searchQuery = invState.searchQuery,
                selectedFilter = invState.selectedFilter,
                onSearchChange = { viewModel.updateInventorySearch(it) },
                onSelectFilter = { viewModel.selectInventoryFilter(it) }
            )

            // 5. MAIN COLLECTION CONTENT GRID
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val columns = when {
                    maxWidth >= 840.dp -> 4
                    maxWidth >= 600.dp -> 3
                    else -> 2
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("inventory_items_grid")
                ) {
                    // Category-specific header strip (Emoji 18-slot deck or Pawn Trail toggle)
                    if (selectedCategory == InventoryCategory.EMOJI) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmojiDeckPanel(
                                slots = invState.emojiDeckSlots,
                                selectedSlotIndex = selectedDeckSlot,
                                swapSourceSlot = swapSourceSlot,
                                onSelectSlot = { idx ->
                                    if (swapSourceSlot in 0 until EmojiDeck.MAX_SLOTS && swapSourceSlot != idx) {
                                        viewModel.swapEmojiDeckSlots(swapSourceSlot, idx)
                                        swapSourceSlot = -1
                                        selectedDeckSlot = idx
                                    } else {
                                        selectedDeckSlot = idx
                                    }
                                },
                                onStartSwap = { idx ->
                                    swapSourceSlot = if (swapSourceSlot == idx) -1 else idx
                                },
                                onRemoveSlot = { idx ->
                                    viewModel.removeEmojiFromDeckSlot(idx)
                                    if (swapSourceSlot == idx) swapSourceSlot = -1
                                }
                            )
                        }
                    } else if (selectedCategory == InventoryCategory.PAWN) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            PawnTrailControlCard(
                                isTrailEnabled = invState.isPawnTrailEnabled,
                                onToggleTrail = { viewModel.setPawnTrailEnabled(it) }
                            )
                        }
                    }

                    if (filteredItems.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyInventoryStateCard(
                                category = selectedCategory,
                                hasQueryOrFilter = invState.searchQuery.isNotBlank() || invState.selectedFilter != InventoryFilter.ALL,
                                onResetFilters = {
                                    viewModel.updateInventorySearch("")
                                    viewModel.selectInventoryFilter(InventoryFilter.ALL)
                                }
                            )
                        }
                    } else {
                        items(filteredItems, key = { it.id }) { item ->
                            val isOwned = invState.isOwned(item.id)
                            val isEquipped = isItemEquipped(item, invState)
                            val qty = invState.getQuantity(item.id)
                            val activePowerup = invState.activePowerups.firstOrNull { it.itemId == item.id }
                            val progress = invState.eventProgressMap[item.id]
                            val deckSlotIndex = invState.emojiDeckSlots.indexOf(item.id)

                            InventoryItemCard(
                                item = item,
                                isOwned = isOwned,
                                isEquipped = isEquipped,
                                quantity = qty,
                                activePowerup = activePowerup,
                                eventProgress = progress,
                                emojiAssignedSlotIndex = deckSlotIndex,
                                selectedDeckSlot = selectedDeckSlot,
                                showCategoryBadge = selectedCategory == InventoryCategory.OWNED,
                                playerAvatarId = uiState.playerCrownAvatar,
                                customPhotoUri = uiState.customProfilePhotoUri,
                                onCardClick = { itemPendingConfirmation = item },
                                onPrimaryAction = {
                                    when {
                                        item.category == InventoryCategory.POWERUP -> {
                                            if (qty > 0) viewModel.activateInventoryPowerup(item.id)
                                            else itemPendingConfirmation = item
                                        }
                                        item.category == InventoryCategory.EMOJI -> {
                                            if (isOwned) {
                                                viewModel.assignEmojiToDeckSlot(selectedDeckSlot, item.id)
                                            } else if (item.currencyType == CurrencyType.AD_REWARD) {
                                                adRewardCoins = 500L
                                                adRewardCash = 0
                                                adRewardTitle = "Progress +1 toward ${item.name} & +500 Coins"
                                                showAdDialog = true
                                            } else {
                                                itemPendingConfirmation = item
                                            }
                                        }
                                        isOwned && !isEquipped -> {
                                            viewModel.equipInventoryItem(item.id)
                                        }
                                        isOwned && isEquipped && selectedCategory == InventoryCategory.OWNED -> {
                                            viewModel.unequipInventoryCategory(item.category)
                                        }
                                        !isOwned -> {
                                            itemPendingConfirmation = item
                                        }
                                    }
                                },
                                onSecondaryAction = {
                                    if (item.category == InventoryCategory.POWERUP) {
                                        itemPendingConfirmation = item
                                    } else if (isOwned && isEquipped && !item.defaultOwned) {
                                        viewModel.unequipInventoryCategory(item.category)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // ITEM DETAIL & PURCHASE CONFIRMATION DIALOG
        itemPendingConfirmation?.let { item ->
            InventoryItemDetailDialog(
                item = item,
                invState = invState,
                userCoins = uiState.userCoins,
                userCash = uiState.userGems,
                playerAvatarId = uiState.playerCrownAvatar,
                customPhotoUri = uiState.customProfilePhotoUri,
                selectedDeckSlot = selectedDeckSlot,
                onConfirmPurchase = {
                    viewModel.purchaseInventoryItem(item.id)
                    itemPendingConfirmation = null
                },
                onEquip = {
                    viewModel.equipInventoryItem(item.id)
                    itemPendingConfirmation = null
                },
                onUnequip = {
                    viewModel.unequipInventoryCategory(item.category)
                    itemPendingConfirmation = null
                },
                onAssignEmoji = {
                    viewModel.assignEmojiToDeckSlot(selectedDeckSlot, item.id)
                    itemPendingConfirmation = null
                },
                onActivatePowerup = {
                    viewModel.activateInventoryPowerup(item.id)
                    itemPendingConfirmation = null
                },
                onWatchAd = {
                    itemPendingConfirmation = null
                    adRewardCoins = 500L
                    adRewardCash = 0
                    adRewardTitle = "Unlock Progress: ${item.name} (+500 Coins)"
                    showAdDialog = true
                },
                onOpenTopUp = { currency ->
                    itemPendingConfirmation = null
                    topUpCurrency = currency
                    showTopUpDialog = true
                },
                onDismiss = { itemPendingConfirmation = null }
            )
        }

        // PURCHASE / ACTIVATION SUCCESS DIALOG
        if (invState.lastSuccessItem != null) {
            PurchaseSuccessDialog(
                item = invState.lastSuccessItem,
                message = invState.lastSuccessMessage ?: "Item Unlocked!",
                onDismiss = { viewModel.dismissInventoryPurchaseSuccess() }
            )
        }

        // ADD COINS / CASH DIALOG
        if (showTopUpDialog) {
            InventoryTopUpDialog(
                currencyType = topUpCurrency,
                currentCash = uiState.userGems,
                onWatchAdForCoins = {
                    showTopUpDialog = false
                    adRewardCoins = 2500L
                    adRewardCash = 0
                    adRewardTitle = "2,500 Gold Coins"
                    showAdDialog = true
                },
                onWatchAdForCash = {
                    showTopUpDialog = false
                    adRewardCoins = 0L
                    adRewardCash = 15
                    adRewardTitle = "15 Cash Notes"
                    showAdDialog = true
                },
                onExchangeCashForCoins = { cashCost, coinsReward ->
                    viewModel.exchangeGemsForCoins(cashCost, coinsReward)
                    showTopUpDialog = false
                },
                onDismiss = { showTopUpDialog = false }
            )
        }

        // VERIFIED REWARDED AD DIALOG
        if (showAdDialog) {
            RewardedAdDialog(
                rewardTitle = adRewardTitle,
                onRewardEarned = {
                    viewModel.rewardUserForAd(
                        coins = adRewardCoins,
                        gems = adRewardCash,
                        adTransactionId = "inv_ad_${UUID.randomUUID()}",
                        verified = true,
                        targetProgressItemId = "emoji_guy_crying"
                    )
                },
                onAdSkippedOrFailed = {
                    viewModel.recordAdSkippedOrFailed("inv_ad_skip_${UUID.randomUUID()}")
                },
                onDismiss = { showAdDialog = false }
            )
        }
    }
}

private fun isItemEquipped(item: InventoryItem, invState: InventoryUiState): Boolean {
    return when (item.category) {
        InventoryCategory.DICE -> invState.equippedDiceId == item.id
        InventoryCategory.FRAME -> invState.equippedFrameId == item.id
        InventoryCategory.BOARD -> invState.equippedBoardId == item.id
        InventoryCategory.PAWN -> invState.equippedPawnId == item.id
        InventoryCategory.EMOJI -> invState.emojiDeckSlots.contains(item.id)
        InventoryCategory.POWERUP -> invState.activePowerups.any { it.itemId == item.id && it.isCurrentlyActive() }
        InventoryCategory.OWNED -> false
    }
}

@Composable
private fun InventoryTopHeader(
    goldCoins: Long,
    cashNotes: Int,
    onBack: () -> Unit,
    onAddCoins: () -> Unit,
    onAddCash: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp),
        color = Color(0xFF0A1F5A),
        border = BorderStroke(1.dp, Color(0x5500E5FF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button + Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2979FF), Color(0xFF0D47A1))
                            )
                        )
                        .border(1.5.dp, Color(0xFF80D8FF), CircleShape)
                        .clickable { onBack() }
                        .testTag("inventory_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "INVENTORY",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Royal Armory & Deck",
                        color = RoyalGoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Currency Pills (Gold Coins + Cash Notes)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurrencyWalletPill(
                    emoji = "🪙",
                    valueText = formatCompactCoins(goldCoins),
                    gradient = listOf(Color(0xFF1F3B8E), Color(0xFF0B1F54)),
                    borderColor = RoyalGold,
                    pillTag = "inventory_coins_pill",
                    plusTag = "inventory_add_coins_btn",
                    onPlusClick = onAddCoins
                )

                CurrencyWalletPill(
                    emoji = "💵",
                    valueText = cashNotes.toString(),
                    gradient = listOf(Color(0xFF165B33), Color(0xFF0A311B)),
                    borderColor = Color(0xFF00E676),
                    pillTag = "inventory_cash_pill",
                    plusTag = "inventory_add_cash_btn",
                    onPlusClick = onAddCash
                )
            }
        }
    }
}

@Composable
private fun CurrencyWalletPill(
    emoji: String,
    valueText: String,
    gradient: List<Color>,
    borderColor: Color,
    pillTag: String,
    plusTag: String,
    onPlusClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(38.dp)
            .shadow(4.dp, RoundedCornerShape(19.dp))
            .clip(RoundedCornerShape(19.dp))
            .background(Brush.verticalGradient(gradient))
            .border(1.5.dp, borderColor, RoundedCornerShape(19.dp))
            .padding(start = 8.dp, end = 4.dp)
            .testTag(pillTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = valueText,
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF00E676), Color(0xFF00C853))
                    )
                )
                .border(1.dp, Color.White, CircleShape)
                .clickable { onPlusClick() }
                .testTag(plusTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add currency",
                tint = Color(0xFF003300),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun InventoryCategoryTabsRow(
    selectedCategory: InventoryCategory,
    onSelectCategory: (InventoryCategory) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_category_tabs_row")
    ) {
        items(InventoryCategory.entries) { category ->
            val selected = category == selectedCategory
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.04f else 1.0f,
                label = "tabScale"
            )
            val borderColor by animateColorAsState(
                targetValue = if (selected) Color(0xFF00E5FF) else Color(0x44FFFFFF),
                label = "tabBorder"
            )

            Box(
                modifier = Modifier
                    .scale(scale)
                    .height(48.dp)
                    .shadow(if (selected) 10.dp else 2.dp, RoundedCornerShape(14.dp), ambientColor = Color(0xFF00E5FF), spotColor = Color(0xFF00E5FF))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selected) {
                            Brush.verticalGradient(listOf(Color(0xFF1976D2), Color(0xFF0D47A1)))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFF281859), Color(0xFF1B1040)))
                        }
                    )
                    .border(
                        width = if (selected) 2.5.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .testTag("inventory_tab_${category.name}")
                    .clickable { onSelectCategory(category) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(category.icon, fontSize = 18.sp)
                    Text(
                        text = category.shortTitle,
                        color = if (selected) Color.White else Color(0xFFCFD8DC),
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun InventorySearchAndFiltersSection(
    category: InventoryCategory,
    ownedCount: Int,
    totalCount: Int,
    searchQuery: String,
    selectedFilter: InventoryFilter,
    onSearchChange: (String) -> Unit,
    onSelectFilter: (InventoryFilter) -> Unit
) {
    val availableFilters = remember(category) {
        buildList {
            add(InventoryFilter.ALL)
            add(InventoryFilter.OWNED)
            add(InventoryFilter.UNOWNED)
            add(InventoryFilter.SPECIAL)
            add(InventoryFilter.EVENTS)
            if (category == InventoryCategory.EMOJI) add(InventoryFilter.ADS)
            if (category == InventoryCategory.DICE) add(InventoryFilter.CHESTS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // Collection Name & Owned Counter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${category.icon} ${category.title}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            Box(
                modifier = Modifier
                    .semantics(mergeDescendants = true) {}
                    .testTag("inventory_owned_counter")
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x5500E5FF))
                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Owned: $ownedCount / $totalCount",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = "Search ${category.shortTitle.lowercase()} by name...",
                    color = Color(0x99FFFFFF),
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF00E5FF)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = Color.White
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF151748),
                unfocusedContainerColor = Color(0xFF12143F),
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0x5580D8FF),
                cursorColor = Color(0xFF00E5FF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inventory_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availableFilters) { filter ->
                val active = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(
                            if (active) Brush.horizontalGradient(listOf(Color(0xFF00B0FF), Color(0xFF2979FF)))
                            else Brush.horizontalGradient(listOf(Color(0xFF251A52), Color(0xFF1A123E)))
                        )
                        .border(
                            width = if (active) 1.5.dp else 1.dp,
                            color = if (active) Color(0xFF80D8FF) else Color(0x33FFFFFF),
                            shape = RoundedCornerShape(17.dp)
                        )
                        .clickable { onSelectFilter(filter) }
                        .padding(horizontal = 14.dp)
                        .testTag("inventory_filter_${filter.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        color = if (active) Color.White else Color(0xFFB0BEC5),
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.Black else FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun EmojiDeckPanel(
    slots: List<String?>,
    selectedSlotIndex: Int,
    swapSourceSlot: Int,
    onSelectSlot: (Int) -> Unit,
    onStartSwap: (Int) -> Unit,
    onRemoveSlot: (Int) -> Unit
) {
    val filledCount = slots.count { it != null }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161347)),
        border = BorderStroke(1.5.dp, Color(0xFF00E5FF))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "😀 Custom 18-Slot Emoji Deck ($filledCount/18)",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (swapSourceSlot >= 0) {
                            "Tap another slot to swap with Slot #${swapSourceSlot + 1}"
                        } else {
                            "Selected: Slot #${selectedSlotIndex + 1} • Tap owned emoji below to equip"
                        },
                        color = Color(0xFF80D8FF),
                        fontSize = 10.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3 rows x 6 slots = 18 slots
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (col in 0 until 6) {
                            val idx = row * 6 + col
                            val itemId = slots.getOrNull(idx)
                            val item = itemId?.let { ItemCatalog.findById(it) }
                            val isSelected = idx == selectedSlotIndex
                            val isSwapSource = idx == swapSourceSlot

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isSwapSource -> Color(0xFF6A1B9A)
                                            isSelected -> Color(0xFF0D47A1)
                                            item != null -> Color(0xFF231D5E)
                                            else -> Color(0xFF100E36)
                                        }
                                    )
                                    .border(
                                        width = if (isSelected || isSwapSource) 2.dp else 1.dp,
                                        color = when {
                                            isSwapSource -> Color(0xFFFFD54F)
                                            isSelected -> Color(0xFF00E5FF)
                                            item != null -> Color(0x6680D8FF)
                                            else -> Color(0x33FFFFFF)
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectSlot(idx) }
                                    .testTag("emoji_deck_slot_$idx"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item != null) {
                                    Text(item.iconEmoji, fontSize = 20.sp)
                                    // Remove button top-right
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFD50000))
                                            .clickable { onRemoveSlot(idx) }
                                            .testTag("emoji_deck_remove_$idx"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✕", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                    }
                                    // Swap button bottom-left
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00B0FF))
                                            .clickable { onStartSwap(idx) }
                                            .testTag("emoji_deck_swap_$idx"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "Swap slot",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "${idx + 1}",
                                        color = Color(0x66FFFFFF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
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

@Composable
private fun PawnTrailControlCard(
    isTrailEnabled: Boolean,
    onToggleTrail: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1654)),
        border = BorderStroke(1.5.dp, Color(0xFF00E5FF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "✨ Pawn Movement Trail Effects",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (isTrailEnabled) "Active: Tokens glow & leave royal trails in matches"
                    else "Disabled: Clean classic pawn movement on board",
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp
                )
            }
            Switch(
                checked = isTrailEnabled,
                onCheckedChange = onToggleTrail,
                modifier = Modifier.testTag("pawn_trail_toggle"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00E676),
                    uncheckedThumbColor = Color(0xFFCFD8DC),
                    uncheckedTrackColor = Color(0xFF37474F)
                )
            )
        }
    }
}

@Composable
private fun InventoryItemCard(
    item: InventoryItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    quantity: Int,
    activePowerup: ActivePowerups?,
    eventProgress: EventProgress?,
    emojiAssignedSlotIndex: Int,
    selectedDeckSlot: Int,
    showCategoryBadge: Boolean,
    playerAvatarId: String,
    customPhotoUri: String?,
    onCardClick: () -> Unit,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit
) {
    val rarityColor = Color(item.rarity.badgeColorHex)
    val glowColor = if (isEquipped) Color(0xFF00E676) else Color(item.rarity.glowColorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isEquipped) 12.dp else 6.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = glowColor,
                spotColor = glowColor
            )
            .clickable { onCardClick() }
            .testTag("inventory_item_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped) Color(0xFF1A296B) else Color(0xFF171347)
        ),
        border = BorderStroke(
            width = if (isEquipped) 2.2.dp else 1.2.dp,
            color = if (isEquipped) Color(0xFF00E676) else rarityColor.copy(alpha = 0.75f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Rarity + Status / Owned Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(rarityColor.copy(alpha = 0.25f))
                        .border(1.dp, rarityColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.rarity.label,
                        color = Color(item.rarity.glowColorHex),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                if (item.category == InventoryCategory.POWERUP) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF283593))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "x$quantity",
                            color = RoyalGoldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (isEquipped) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF00C853))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "EQUIPPED",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (isOwned) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0288D1))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OWNED",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            if (showCategoryBadge) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.category.icon} ${item.category.shortTitle}",
                    color = Color(0xFF80D8FF),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visual Item Preview per Category
            InventoryItemVisualPreview(
                item = item,
                playerAvatarId = playerAvatarId,
                customPhotoUri = customPhotoUri
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name & Description / Special Tags
            Text(
                text = item.name,
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Category specific sub-info (4 Pawn Color Previews + Trail OR Progress Bar 0/50 OR Powerup Timer)
            when {
                item.category == InventoryCategory.PAWN -> {
                    if (item.pawnColorHexes.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            item.pawnColorHexes.forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(hex))
                                        .border(1.dp, Color.White, CircleShape)
                                )
                            }
                        }
                    }
                    Text(
                        text = "✨ ${item.trailName}",
                        color = Color(0xFF80D8FF),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                item.progressTarget > 0 && !isOwned -> {
                    val cur = eventProgress?.currentProgress ?: 0
                    val tgt = item.progressTarget
                    val frac = (cur.toFloat() / tgt.toFloat()).coerceIn(0f, 1f)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFF0A0D30))
                                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(frac)
                                    .align(Alignment.CenterStart)
                                    .background(Color(0xFF00E676))
                            )
                            Text(
                                text = "$cur/$tgt",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                item.category == InventoryCategory.POWERUP -> {
                    val now = System.currentTimeMillis()
                    val isActiveNow = activePowerup?.isCurrentlyActive(now) == true
                    Text(
                        text = if (isActiveNow) {
                            val mins = ((activePowerup!!.activeUntilMillis - now) / 60000L).coerceAtLeast(1L)
                            "🔥 ACTIVE (${mins}m left)"
                        } else {
                            "Limit: ${activePowerup?.dailyUsedCount ?: 0}/${item.dailyLimit} • CD ${item.cooldownSeconds}s"
                        },
                        color = if (isActiveNow) Color(0xFF00E676) else Color(0xFFCFD8DC),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                else -> {
                    Text(
                        text = if (isOwned) item.description else item.unlockRequirement,
                        color = Color(0xFFB0BEC5),
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Action Buttons
            if (item.category == InventoryCategory.POWERUP) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GlossyActionButton(
                        text = "Activate",
                        enabled = quantity > 0,
                        gradient = if (quantity > 0) listOf(Color(0xFF00E676), Color(0xFF00C853))
                        else listOf(Color(0xFF455A64), Color(0xFF263238)),
                        textColor = if (quantity > 0) Color(0xFF003300) else Color(0xFF90A4AE),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("powerup_activate_${item.id}"),
                        onClick = onPrimaryAction
                    )

                    GlossyActionButton(
                        text = "${item.currencyType.symbol} ${item.price}",
                        enabled = true,
                        gradient = listOf(Color(0xFFFFB300), Color(0xFFFF8F00)),
                        textColor = Color(0xFF3E2723),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("powerup_buy_${item.id}"),
                        onClick = onSecondaryAction
                    )
                }
            } else if (item.category == InventoryCategory.EMOJI && isOwned) {
                GlossyActionButton(
                    text = if (emojiAssignedSlotIndex >= 0) "Slot #${emojiAssignedSlotIndex + 1} ✓"
                    else "Use Slot #${selectedDeckSlot + 1}",
                    enabled = true,
                    gradient = if (emojiAssignedSlotIndex >= 0) listOf(Color(0xFF00C853), Color(0xFF1B5E20))
                    else listOf(Color(0xFF2979FF), Color(0xFF0D47A1)),
                    textColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_action_btn_${item.id}"),
                    onClick = onPrimaryAction
                )
            } else if (isOwned) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GlossyActionButton(
                        text = if (isEquipped) "Equipped ✓" else "Equip",
                        enabled = true,
                        gradient = if (isEquipped) listOf(Color(0xFF00C853), Color(0xFF1B5E20))
                        else listOf(Color(0xFF00B0FF), Color(0xFF1565C0)),
                        textColor = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inventory_action_btn_${item.id}"),
                        onClick = onPrimaryAction
                    )
                    if (isEquipped && !item.defaultOwned) {
                        GlossyActionButton(
                            text = "Reset",
                            enabled = true,
                            gradient = listOf(Color(0xFF546E7A), Color(0xFF37474F)),
                            textColor = Color.White,
                            modifier = Modifier
                                .weight(0.75f)
                                .testTag("inventory_unequip_btn_${item.id}"),
                            onClick = onSecondaryAction
                        )
                    }
                }
            } else {
                val (btnText, grad, tColor) = when (item.currencyType) {
                    CurrencyType.AD_REWARD -> Triple(
                        "Watch Ad 🎬",
                        listOf(Color(0xFF00E5FF), Color(0xFF0288D1)),
                        Color.White
                    )
                    CurrencyType.EVENT -> Triple(
                        "Event Only 🏆",
                        listOf(Color(0xFF8E24AA), Color(0xFF4A148C)),
                        Color.White
                    )
                    CurrencyType.CASH -> Triple(
                        "💵 ${item.price}",
                        listOf(Color(0xFF00E676), Color(0xFF00A152)),
                        Color(0xFF003300)
                    )
                    else -> Triple(
                        "🪙 ${formatCompactCoins(item.price)}",
                        listOf(Color(0xFFFFD54F), Color(0xFFFF8F00)),
                        Color(0xFF3E2723)
                    )
                }
                GlossyActionButton(
                    text = btnText,
                    enabled = true,
                    gradient = grad,
                    textColor = tColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_action_btn_${item.id}"),
                    onClick = onPrimaryAction
                )
            }
        }
    }
}

@Composable
private fun InventoryItemVisualPreview(
    item: InventoryItem,
    playerAvatarId: String,
    customPhotoUri: String?
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.radialGradient(
                    listOf(
                        item.primaryColor.copy(alpha = 0.35f),
                        item.secondaryColor.copy(alpha = 0.65f),
                        Color(0xFF0D0E2B)
                    )
                )
            )
            .border(1.dp, item.accentColor.copy(alpha = 0.55f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (item.category) {
            InventoryCategory.DICE -> {
                Realistic3DDice(
                    diceValue = 6,
                    isRolling = false,
                    playerColor = LudoColor.YELLOW,
                    isTurnActive = false,
                    diceSize = 50.dp,
                    diceSkinId = item.id,
                    onRollClick = {}
                )
            }

            InventoryCategory.FRAME -> {
                FramedAvatarBox(
                    frameId = item.id,
                    size = 54.dp,
                    shape = RoundedCornerShape(12.dp),
                    showCornerBadge = true
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1F164C)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!customPhotoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = customPhotoUri,
                                contentDescription = "Avatar preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(getCrownAvatarEmoji(playerAvatarId), fontSize = 24.sp)
                        }
                    }
                }
            }

            InventoryCategory.BOARD -> {
                val theme = getBoardTheme(item.id)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.outerFrameColor)
                        .border(2.dp, theme.borderColor, RoundedCornerShape(10.dp))
                        .padding(5.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFE53935), RoundedCornerShape(3.dp)))
                            Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF43A047), RoundedCornerShape(3.dp)))
                        }
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFDD835), RoundedCornerShape(3.dp)))
                            Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF1E88E5), RoundedCornerShape(3.dp)))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(theme.trackBgColor)
                            .border(1.5.dp, theme.accentStarColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.iconEmoji, fontSize = 9.sp)
                    }
                }
            }

            else -> {
                Text(item.iconEmoji, fontSize = 34.sp)
            }
        }
    }
}

@Composable
private fun GlossyActionButton(
    text: String,
    enabled: Boolean,
    gradient: List<Color>,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .shadow(if (enabled) 4.dp else 0.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(gradient))
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.5f else 0.15f), RoundedCornerShape(10.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyInventoryStateCard(
    category: InventoryCategory,
    hasQueryOrFilter: Boolean,
    onResetFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1652)),
        border = BorderStroke(1.dp, Color(0x5580D8FF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(category.icon, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasQueryOrFilter) "No matching items found" else "No owned items yet",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (hasQueryOrFilter) "Try clearing your search or switching filter pills."
                else "Unlock items in Dice, Frames, Emojis, Boards, Pawns, or Powerups!",
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            if (hasQueryOrFilter) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onResetFilters,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF))
                ) {
                    Text("Reset Search & Filters", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun InventoryItemDetailDialog(
    item: InventoryItem,
    invState: InventoryUiState,
    userCoins: Long,
    userCash: Int,
    playerAvatarId: String,
    customPhotoUri: String?,
    selectedDeckSlot: Int,
    onConfirmPurchase: () -> Unit,
    onEquip: () -> Unit,
    onUnequip: () -> Unit,
    onAssignEmoji: () -> Unit,
    onActivatePowerup: () -> Unit,
    onWatchAd: () -> Unit,
    onOpenTopUp: (CurrencyType) -> Unit,
    onDismiss: () -> Unit
) {
    val isOwned = invState.isOwned(item.id)
    val isEquipped = isItemEquipped(item, invState)
    val canAfford = when (item.currencyType) {
        CurrencyType.COINS -> userCoins >= item.price
        CurrencyType.CASH -> userCash >= item.price
        CurrencyType.FREE -> true
        else -> false
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(22.dp))
                .testTag("inventory_detail_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14184E)),
            border = BorderStroke(2.dp, Color(item.rarity.glowColorHex))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.rarity.label.uppercase(),
                        color = Color(item.rarity.glowColorHex),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                InventoryItemVisualPreview(
                    item = item,
                    playerAvatarId = playerAvatarId,
                    customPhotoUri = customPhotoUri
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    color = Color(0xFFCFD8DC),
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0C1036))
                        .border(1.dp, Color(0x4480D8FF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = item.unlockRequirement,
                        color = RoyalGoldLight,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isOwned && (item.currencyType == CurrencyType.COINS || item.currencyType == CurrencyType.CASH)) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val availableText = if (item.currencyType == CurrencyType.COINS) {
                        "Your Balance: 🪙 ${formatCompactCoins(userCoins)}"
                    } else {
                        "Your Balance: 💵 $userCash"
                    }
                    Text(
                        text = availableText,
                        color = if (canAfford) Color(0xFF00E676) else Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!canAfford) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = { onOpenTopUp(item.currencyType) }) {
                            Text("Get More ${item.currencyType.label} +", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("inventory_cancel_buy_btn"),
                        border = BorderStroke(1.dp, Color(0x66FFFFFF))
                    ) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    when {
                        item.category == InventoryCategory.POWERUP -> {
                            if (invState.getQuantity(item.id) > 0) {
                                Button(
                                    onClick = onActivatePowerup,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                                ) {
                                    Text("Activate 🚀", color = Color(0xFF003300), fontWeight = FontWeight.Black)
                                }
                            }
                            Button(
                                onClick = onConfirmPurchase,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("inventory_confirm_buy_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                            ) {
                                Text("Buy (${item.currencyType.symbol} ${item.price})", color = Color(0xFF3E2723), fontWeight = FontWeight.Black)
                            }
                        }
                        item.category == InventoryCategory.EMOJI && isOwned -> {
                            Button(
                                onClick = onAssignEmoji,
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF))
                            ) {
                                Text("Put in Slot #${selectedDeckSlot + 1}", fontWeight = FontWeight.Black)
                            }
                        }
                        isOwned -> {
                            Button(
                                onClick = if (isEquipped && !item.defaultOwned) onUnequip else onEquip,
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEquipped) Color(0xFF546E7A) else Color(0xFF00E676)
                                )
                            ) {
                                Text(
                                    text = if (isEquipped && !item.defaultOwned) "Unequip" else if (isEquipped) "Equipped ✓" else "Equip Now",
                                    color = if (isEquipped) Color.White else Color(0xFF003300),
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        item.currencyType == CurrencyType.AD_REWARD -> {
                            Button(
                                onClick = onWatchAd,
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF))
                            ) {
                                Text("Watch Ad 🎬", fontWeight = FontWeight.Black)
                            }
                        }
                        item.currencyType == CurrencyType.EVENT -> {
                            Button(
                                onClick = onConfirmPurchase,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("inventory_confirm_buy_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                            ) {
                                Text("Claim Event 🏆", fontWeight = FontWeight.Black)
                            }
                        }
                        else -> {
                            Button(
                                onClick = onConfirmPurchase,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("inventory_confirm_buy_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canAfford) Color(0xFF00E676) else Color(0xFFE53935)
                                )
                            ) {
                                Text(
                                    text = "Confirm ${item.currencyType.symbol} ${item.price}",
                                    color = if (canAfford) Color(0xFF003300) else Color.White,
                                    fontWeight = FontWeight.Black
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
private fun PurchaseSuccessDialog(
    item: InventoryItem,
    message: String,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "successPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(22.dp))
                .testTag("inventory_success_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14184E)),
            border = BorderStroke(2.dp, Color(0xFF00E676))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Color(0x3300E676))
                        .border(2.dp, Color(0xFF00E676), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.iconEmoji, fontSize = 36.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "UNLOCKED!",
                    color = Color(0xFF00E676),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("inventory_success_ok_btn")
                ) {
                    Text("Awesome!", color = Color(0xFF003300), fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun InventoryTopUpDialog(
    currencyType: CurrencyType,
    currentCash: Int,
    onWatchAdForCoins: () -> Unit,
    onWatchAdForCash: () -> Unit,
    onExchangeCashForCoins: (Int, Long) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inventory_topup_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14184E)),
            border = BorderStroke(2.dp, RoyalGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (currencyType == CurrencyType.COINS) "🪙 Get Gold Coins" else "💵 Get Cash Notes",
                    color = RoyalGoldLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = if (currencyType == CurrencyType.COINS) onWatchAdForCoins else onWatchAdForCash,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF)),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = if (currencyType == CurrencyType.COINS) "🎬 Watch Verified Ad (+2,500 Coins)"
                        else "🎬 Watch Verified Ad (+15 Cash)",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }

                if (currencyType == CurrencyType.COINS) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onExchangeCashForCoins(10, 5000L) },
                        enabled = currentCash >= 10,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "💵 Exchange 10 Cash → 🪙 5,000 Coins",
                            color = Color(0xFF003300),
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color(0xFFB0BEC5))
                }
            }
        }
    }
}
