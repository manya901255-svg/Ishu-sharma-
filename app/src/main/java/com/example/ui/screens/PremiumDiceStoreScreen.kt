package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.components.Realistic3DDice
import com.example.ui.components.RewardedAdDialog
import com.example.ui.components.formatCompactCoins
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalNavyDark
import com.example.viewmodel.LudoUiState
import com.example.viewmodel.LudoViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun PremiumDiceStoreScreen(
    uiState: LudoUiState,
    viewModel: LudoViewModel,
    onBack: () -> Unit
) {
    var showRewardedAdDialog by remember { mutableStateOf(false) }
    var adRewardType by remember { mutableStateOf("Coins") }

    Scaffold(
        containerColor = Color(0xFF0A0E2A),
        topBar = {
            PremiumDiceStoreTopBar(
                userCoins = uiState.userCoins,
                userCash = uiState.userGems,
                isBillingConnected = uiState.diceStoreUiState.isBillingConnected,
                onBack = onBack,
                onAddCoinsClick = {
                    adRewardType = "Coins"
                    showRewardedAdDialog = true
                },
                onAddCashClick = {
                    adRewardType = "Cash"
                    showRewardedAdDialog = true
                },
                onRestorePurchases = { viewModel.requestPlayBillingRestore() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1D0D4A),
                            Color(0xFF121542),
                            Color(0xFF090D28)
                        )
                    )
                )
        ) {
            PremiumDiceCatalogContent(
                uiState = uiState,
                viewModel = viewModel,
                onAddCoinsClick = {
                    adRewardType = "Coins"
                    showRewardedAdDialog = true
                },
                showStandaloneBanner = true
            )
        }
    }

    if (showRewardedAdDialog) {
        RewardedAdDialog(
            rewardTitle = if (adRewardType == "Cash") "5 Cash 💵" else "500 Coins 🪙",
            onRewardEarned = {
                if (adRewardType == "Coins") {
                    viewModel.rewardUserForAd(coins = 500L, gems = 0)
                } else {
                    viewModel.rewardUserForAd(coins = 0L, gems = 5)
                }
                viewModel.clearDiceStoreMessages()
            },
            onDismiss = { showRewardedAdDialog = false }
        )
    }
}

@Composable
private fun PremiumDiceStoreTopBar(
    userCoins: Long,
    userCash: Int,
    isBillingConnected: Boolean,
    onBack: () -> Unit,
    onAddCoinsClick: () -> Unit,
    onAddCashClick: () -> Unit,
    onRestorePurchases: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF280B57), Color(0xFF162562), Color(0xFF280B57))
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button (min 48dp touch target)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFC107))
                    .border(1.5.dp, Color.White, CircleShape)
                    .clickable { onBack() }
                    .testTag("premium_dice_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1B0A38)
                )
            }

            // Currency Balances + Quick Add '+'
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurrencyHeaderPill(
                    emoji = "🪙",
                    amountText = formatCompactCoins(userCoins),
                    borderColor = Color(0xFFFFD54F),
                    plusTag = "premium_dice_add_coins_button",
                    onPlusClick = onAddCoinsClick
                )
                CurrencyHeaderPill(
                    emoji = "💵",
                    amountText = "$userCash",
                    borderColor = Color(0xFF00E676),
                    plusTag = "premium_dice_add_cash_button",
                    onPlusClick = onAddCashClick
                )
            }

            // Restore Play Purchases Button
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B3A75))
                    .border(1.dp, if (isBillingConnected) Color(0xFF00E676) else Color(0xFF64B5F6), RoundedCornerShape(12.dp))
                    .clickable { onRestorePurchases() }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("restore_purchases_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restore Purchases",
                        tint = Color(0xFF80D8FF),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Restore",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrencyHeaderPill(
    emoji: String,
    amountText: String,
    borderColor: Color,
    plusTag: String,
    onPlusClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0A1636))
            .border(1.3.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(emoji, fontSize = 14.sp)
        Text(
            text = amountText,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Color(0xFF00C853))
                .clickable { onPlusClick() }
                .testTag(plusTag),
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun PremiumDiceCatalogContent(
    uiState: LudoUiState,
    viewModel: LudoViewModel,
    onAddCoinsClick: () -> Unit,
    onOpenFullStore: (() -> Unit)? = null,
    showStandaloneBanner: Boolean = false
) {
    val diceUi = uiState.diceStoreUiState
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // Live countdown ticker for Daily Offers & Limited-Time Event Dice
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val publishedCatalog = remember(diceUi.catalog) {
        diceUi.catalog.filter { it.isPublished }
    }

    val filteredDice = remember(
        publishedCatalog,
        diceUi.selectedGroup,
        diceUi.selectedRarity,
        diceUi.searchQuery
    ) {
        publishedCatalog.filter { dice ->
            val groupMatch = when (diceUi.selectedGroup) {
                "ALL" -> true
                "CLASSIC_PREMIUM" -> dice.collectionGroup == DiceCollectionGroup.CLASSIC_PREMIUM
                "VIP" -> dice.collectionGroup == DiceCollectionGroup.VIP || dice.rarity == DiceRarity.VIP
                "SPECIAL_THEMED" -> dice.collectionGroup == DiceCollectionGroup.SPECIAL_THEMED
                "DAILY_EVENT" -> dice.collectionGroup == DiceCollectionGroup.DAILY_EVENT ||
                    dice.availabilityType != DiceAvailabilityType.ALWAYS_AVAILABLE
                else -> true
            }
            val rarityMatch = diceUi.selectedRarity == null || dice.rarity == diceUi.selectedRarity
            val q = diceUi.searchQuery.trim().lowercase()
            val searchMatch = q.isEmpty() ||
                dice.name.lowercase().contains(q) ||
                dice.description.lowercase().contains(q) ||
                dice.benefits.visualEffectSummary.lowercase().contains(q)
            groupMatch && rarityMatch && searchMatch
        }
    }

    val timedOffers = remember(publishedCatalog, nowMillis) {
        publishedCatalog.filter {
            it.eventEndTimeMillis > nowMillis &&
                (it.availabilityType == DiceAvailabilityType.DAILY_OFFER ||
                    it.availabilityType == DiceAvailabilityType.LIMITED_TIME)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        if (showStandaloneBanner) {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF19124F)),
                border = BorderStroke(
                    1.8.dp,
                    Brush.horizontalGradient(listOf(RoyalGold, Color(0xFFE040FB), Color(0xFF00E5FF)))
                )
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
                            text = "✨ PREMIUM DICE STORE",
                            color = RoyalGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${diceUi.ownedDiceIds.size}/${publishedCatalog.size} Owned • Fair Play Certified 1-6 RNG",
                            color = Color(0xFFD1C4E9),
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF00C853))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "FAIR 1-6 RNG",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        } else if (onOpenFullStore != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onOpenFullStore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_full_premium_dice_store_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                border = BorderStroke(1.5.dp, RoyalGold)
            ) {
                Text(
                    text = "✨ OPEN FULL PREMIUM DICE STORE (${publishedCatalog.size} 3D DICE) →",
                    color = RoyalGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Live Status / Billing / Error Feedback Banner
        AnimatedVisibility(
            visible = diceUi.statusBanner != null || diceUi.errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val isError = diceUi.errorMessage != null
            val message = diceUi.errorMessage ?: diceUi.statusBanner.orEmpty()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isError) Color(0xFF5D101B) else Color(0xFF0F3D31))
                    .border(1.dp, if (isError) Color(0xFFFF5252) else Color(0xFF00E676), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { viewModel.clearDiceStoreMessages() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss message", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Daily Offers & Limited-Time Countdown Ticker Banner
        if (timedOffers.isNotEmpty()) {
            val leadOffer = timedOffers.first()
            val remainingSec = ((leadOffer.eventEndTimeMillis - nowMillis).coerceAtLeast(0L) / 1000L)
            val hrs = remainingSec / 3600L
            val mins = (remainingSec % 3600L) / 60L
            val secs = remainingSec % 60L
            val formattedTimer = String.format("%02dh %02dm %02ds", hrs, mins, secs)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF4A148C), Color(0xFF1A237E)))
                    )
                    .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                    .clickable { viewModel.openDiceDetail(leadOffer.id) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("daily_offer_countdown_banner"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⏳ ${leadOffer.availabilityType.label.uppercase()}: ${leadOffer.name}",
                        color = RoyalGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Ends in $formattedTimer • Tap to inspect",
                        color = Color(0xFFE1BEE7),
                        fontSize = 10.sp
                    )
                }
                Text(leadOffer.iconEmoji, fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Field
        OutlinedTextField(
            value = diceUi.searchQuery,
            onValueChange = { viewModel.updateDiceStoreSearch(it) },
            placeholder = {
                Text("Search 25+ 3D Dice, VIP, Zodiac, Fire...", color = Color(0xFF9FA8DA), fontSize = 12.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search dice", tint = Color(0xFFFFD54F))
            },
            trailingIcon = {
                if (diceUi.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateDiceStoreSearch("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.White)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoyalGold,
                unfocusedBorderColor = Color(0xFF4527A0),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF0E1338),
                unfocusedContainerColor = Color(0xFF0E1338)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("premium_dice_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Collection Category Tabs
        val groups = listOf(
            "ALL" to "All (${publishedCatalog.size})",
            "CLASSIC_PREMIUM" to "Classic",
            "VIP" to "VIP 👑",
            "SPECIAL_THEMED" to "Special 🎨",
            "DAILY_EVENT" to "Daily/Events ⏳"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groups.forEach { (key, label) ->
                val isSelected = diceUi.selectedGroup == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF6F00)))
                            else Brush.horizontalGradient(listOf(Color(0xFF1F1654), Color(0xFF17266B)))
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color.White else Color(0xFF5E35B1),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { viewModel.selectDiceStoreGroup(key) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("dice_group_tab_$key"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color(0xFF1B0A38) else Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Rarity Filter Chips
        val rarities: List<DiceRarity?> = listOf(
            null,
            DiceRarity.COMMON,
            DiceRarity.RARE,
            DiceRarity.EPIC,
            DiceRarity.LEGENDARY,
            DiceRarity.VIP
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rarities.forEach { rarity ->
                val isSelected = diceUi.selectedRarity == rarity
                val label = rarity?.label ?: "All Rarities"
                val accent = rarity?.primaryBorderColor ?: Color(0xFF80D8FF)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) accent.copy(alpha = 0.25f) else Color(0xFF0E143B))
                        .border(1.dp, if (isSelected) accent else Color(0xFF283593), RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectDiceStoreRarity(rarity) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("dice_rarity_filter_${rarity?.name ?: "ALL"}")
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color(0xFFB39DDB),
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2-Column 3D Dice Grid
        if (filteredDice.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎲", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No dice match your current filter.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = {
                        viewModel.selectDiceStoreGroup("ALL")
                        viewModel.selectDiceStoreRarity(null)
                        viewModel.updateDiceStoreSearch("")
                    }) {
                        Text("Reset Filters", color = RoyalGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("premium_dice_grid")
            ) {
                items(filteredDice, key = { it.id }) { dice ->
                    val isOwned = diceUi.isOwned(dice.id)
                    val isEquipped = diceUi.equippedDiceId == dice.id
                    val isPending = diceUi.isPendingBilling(dice.id)
                    val challenge = diceUi.eventRewards[dice.id]

                    PremiumDiceGridCard(
                        dice = dice,
                        isOwned = isOwned,
                        isEquipped = isEquipped,
                        isPending = isPending,
                        challenge = challenge,
                        nowMillis = nowMillis,
                        onCardClick = { viewModel.openDiceDetail(dice.id) }
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "Provably Fair Guarantee: All dice share identical 1-6 RNG probability. Premium dice unlock bespoke 3D animations, particles, sound themes, and VIP prestige.",
                        color = Color(0xFF9FA8DA),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
                    )
                }
            }
        }
    }

    // Detail Modal Popup
    val detailDice = diceUi.selectedDetailDiceId?.let { id ->
        diceUi.catalog.firstOrNull { it.id == id }
    }
    if (detailDice != null) {
        DiceDetailPopupDialog(
            dice = detailDice,
            isOwned = diceUi.isOwned(detailDice.id),
            isEquipped = diceUi.equippedDiceId == detailDice.id,
            isPendingBilling = diceUi.isPendingBilling(detailDice.id),
            isProcessingPurchase = diceUi.isProcessingPurchase,
            userCoins = uiState.userCoins,
            challengeProgress = diceUi.eventRewards[detailDice.id],
            onTryPreview = { viewModel.previewDiceSoundAndEffect(detailDice) },
            onConfirmCoinBuy = { viewModel.purchasePremiumDiceWithCoins(detailDice.id) },
            onBuyWithInr = { viewModel.launchGooglePlayDicePurchase(activity, detailDice) },
            onEquip = { viewModel.equipPremiumDice(detailDice.id) },
            onUnequip = { viewModel.unequipPremiumDice() },
            onClaimChallenge = { viewModel.claimDiceChallengeReward(detailDice.id) },
            onDismiss = { viewModel.dismissDiceDetail() }
        )
    }

    // Insufficient Coins Modal
    val shortDice = diceUi.insufficientCoinsDialogDice
    if (shortDice != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearDiceStoreMessages() },
            containerColor = Color(0xFF14184A),
            modifier = Modifier.testTag("insufficient_coins_dialog"),
            title = {
                Text("Insufficient Gold Coins 🪙", color = RoyalGold, fontWeight = FontWeight.Black)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${shortDice.name} requires 🪙 ${formatCompactCoins(shortDice.coinPrice)} Gold Coins, and you currently have 🪙 ${formatCompactCoins(uiState.userCoins)}.",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "You can earn free Gold Coins via Watch Ad, Daily Tasks, or buy instantly with Google Play (${shortDice.formattedInrPrice}).",
                        color = Color(0xFFD1C4E9),
                        fontSize = 11.5.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearDiceStoreMessages()
                        onAddCoinsClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    modifier = Modifier.testTag("insufficient_add_coins_button")
                ) {
                    Text("Get Free Coins (+500)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearDiceStoreMessages() }) {
                    Text("Close", color = Color(0xFFB39DDB))
                }
            }
        )
    }
}

@Composable
private fun PremiumDiceGridCard(
    dice: PremiumDiceItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    isPending: Boolean,
    challenge: DiceEventRewardProgress?,
    nowMillis: Long,
    onCardClick: () -> Unit
) {
    val borderColor = when {
        isEquipped -> Color(0xFF00E676)
        dice.rarity == DiceRarity.VIP -> Color(0xFFFFD700)
        else -> dice.rarity.primaryBorderColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(18.dp))
            .clickable { onCardClick() }
            .testTag("dice_card_${dice.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13194B)),
        border = BorderStroke(
            width = if (isEquipped || dice.rarity == DiceRarity.VIP) 2.dp else 1.2.dp,
            brush = Brush.linearGradient(
                listOf(borderColor, dice.rarity.secondaryBorderColor)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF211761),
                            Color(0xFF111847),
                            Color(0xFF0B1033)
                        )
                    )
                )
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Badges Row: Rarity + VIP / Group
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(dice.rarity.badgeBgColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = dice.rarity.label.uppercase(),
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                if (dice.benefits.vipStatusLabel != null || dice.collectionGroup == DiceCollectionGroup.VIP) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFB300))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("👑 VIP", color = Color(0xFF1B0A38), fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                    }
                } else {
                    Text(dice.iconEmoji, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3D Rotating Dice Showcase Box
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                dice.rarity.glowColor.copy(alpha = 0.22f),
                                Color(0xFF090E2B)
                            )
                        )
                    )
                    .border(1.dp, dice.rarity.primaryBorderColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Realistic3DDice(
                    diceValue = 5,
                    isRolling = false,
                    playerColor = LudoColor.YELLOW,
                    isTurnActive = false,
                    diceSize = 56.dp,
                    diceSkinId = dice.id,
                    diceItemOverride = dice,
                    autoRotatePreview = true,
                    onRollClick = onCardClick
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dice.name,
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = dice.benefits.animationStyle.displayName + " • " + dice.benefits.soundEffect.displayName,
                color = Color(0xFFC5CAE9),
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            if (dice.eventEndTimeMillis > nowMillis && dice.availabilityType != DiceAvailabilityType.ALWAYS_AVAILABLE) {
                val minsLeft = ((dice.eventEndTimeMillis - nowMillis) / 60000L).coerceAtLeast(1L)
                Text(
                    text = "⏳ ${minsLeft / 60}h ${minsLeft % 60}m left",
                    color = Color(0xFFFFD54F),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status / Price Strip
            when {
                isEquipped -> {
                    StatusActionChip(
                        text = "EQUIPPED ✓",
                        bgColor = Color(0xFF00C853),
                        textColor = Color.White
                    )
                }
                isOwned -> {
                    StatusActionChip(
                        text = "OWNED • TAP TO EQUIP",
                        bgColor = Color(0xFFFFB300),
                        textColor = Color(0xFF1B0A38)
                    )
                }
                isPending -> {
                    StatusActionChip(
                        text = "⏳ PENDING PLAY",
                        bgColor = Color(0xFF0288D1),
                        textColor = Color.White
                    )
                }
                challenge != null && dice.challengeTargetCount > 0 -> {
                    StatusActionChip(
                        text = "🎯 ${challenge.currentProgress}/${challenge.targetProgress} CHALLENGE",
                        bgColor = Color(0xFF6A1B9A),
                        textColor = Color(0xFFFFD54F)
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (dice.canBuyWithCoins && dice.coinPrice > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1B3A75))
                                    .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🪙 ${formatCompactCoins(dice.coinPrice)}",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1
                                )
                            }
                        }
                        if (dice.canBuyWithInr && dice.inrPrice > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(0.85f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF00C853))
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "₹${dice.inrPrice}",
                                    color = Color.White,
                                    fontSize = 10.sp,
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
private fun StatusActionChip(
    text: String,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Composable
private fun DiceDetailPopupDialog(
    dice: PremiumDiceItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    isPendingBilling: Boolean,
    isProcessingPurchase: Boolean,
    userCoins: Long,
    challengeProgress: DiceEventRewardProgress?,
    onTryPreview: () -> Unit,
    onConfirmCoinBuy: () -> Unit,
    onBuyWithInr: () -> Unit,
    onEquip: () -> Unit,
    onUnequip: () -> Unit,
    onClaimChallenge: () -> Unit,
    onDismiss: () -> Unit
) {
    var isPreviewRolling by remember { mutableStateOf(false) }
    var previewDiceValue by remember { mutableIntStateOf(6) }
    var showCoinConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isPreviewRolling) {
        if (isPreviewRolling) {
            delay(900L)
            isPreviewRolling = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .testTag("dice_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101542)),
            border = BorderStroke(
                2.dp,
                Brush.linearGradient(listOf(dice.rarity.primaryBorderColor, RoyalGold, dice.rarity.secondaryBorderColor))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(dice.rarity.badgeBgColor)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(dice.rarity.label.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF28186E))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(dice.collectionGroup.badge, color = Color(0xFFE1BEE7), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("dice_detail_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close detail", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${dice.iconEmoji} ${dice.name}",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = dice.description,
                    color = Color(0xFFC5CAE9),
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Large 3D Rotating Preview Arena
                Box(
                    modifier = Modifier
                        .size(146.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(dice.rarity.glowColor.copy(alpha = 0.28f), Color(0xFF070B22))
                            )
                        )
                        .border(1.5.dp, dice.rarity.primaryBorderColor, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Realistic3DDice(
                        diceValue = previewDiceValue,
                        isRolling = isPreviewRolling,
                        playerColor = LudoColor.YELLOW,
                        isTurnActive = false,
                        diceSize = 96.dp,
                        diceSkinId = dice.id,
                        diceItemOverride = dice,
                        autoRotatePreview = !isPreviewRolling,
                        onRollClick = {
                            previewDiceValue = Random.nextInt(1, 7)
                            isPreviewRolling = true
                            onTryPreview()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Try Preview Button
                OutlinedButton(
                    onClick = {
                        previewDiceValue = Random.nextInt(1, 7)
                        isPreviewRolling = true
                        onTryPreview()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("dice_try_preview_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Try Preview", tint = Color(0xFF00E5FF))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPreviewRolling) "Rolling Preview ($previewDiceValue)..." else "Try Live 3D Roll & Sound Preview",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Special Benefits Card (Fair-Play Compliant)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF171E59))
                        .border(1.dp, Color(0xFF3949AB), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "✨ SPECIAL BENEFITS (FAIR PLAY)",
                        color = RoyalGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    BenefitLine("🎲 Roll Animation", dice.benefits.animationStyle.displayName)
                    BenefitLine("✨ Particle Trail", dice.benefits.particleEffect.displayName)
                    BenefitLine("🔊 Sound Effect", dice.benefits.soundEffect.displayName)
                    if (dice.benefits.vipStatusLabel != null) {
                        BenefitLine("👑 VIP Prestige", dice.benefits.vipStatusLabel)
                    }
                    Text(
                        text = dice.benefits.visualEffectSummary,
                        color = Color(0xFFE8EAF6),
                        fontSize = 10.5.sp
                    )
                    Text(
                        text = "⚖️ ${dice.benefits.progressionNote}",
                        color = Color(0xFF69F0AE),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                if (isOwned) {
                    if (isEquipped) {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFF00C853),
                                disabledContentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("equipped_dice_indicator")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CURRENTLY EQUIPPED IN GAME", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }

                        if (!dice.isStarterDefault) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onUnequip,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFFF8A80)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("unequip_dice_button")
                            ) {
                                Text("Unequip (Revert to Starter)", color = Color(0xFFFFCDD2), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = onEquip,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("equip_dice_button")
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF1B0A38))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("EQUIP THIS DICE", color = Color(0xFF1B0A38), fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                } else {
                    if (isPendingBilling) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF01579B))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⏳ Google Play payment is pending verification. It will unlock automatically once confirmed.",
                                color = Color.White,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (dice.canBuyWithCoins && dice.coinPrice > 0) {
                            Button(
                                onClick = { showCoinConfirmDialog = true },
                                enabled = !isProcessingPurchase && dice.isAvailable,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("buy_dice_coins_button")
                            ) {
                                Text(
                                    text = "🪙 ${formatCompactCoins(dice.coinPrice)}",
                                    color = Color(0xFF1B0A38),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (dice.canBuyWithInr && dice.inrPrice > 0) {
                            Button(
                                onClick = onBuyWithInr,
                                enabled = !isProcessingPurchase && dice.isAvailable,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("buy_dice_inr_button")
                            ) {
                                Text(
                                    text = "Pay ₹${dice.inrPrice}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    if (dice.challengeTargetCount > 0 && challengeProgress != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onClaimChallenge,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("claim_dice_challenge_button")
                        ) {
                            Text(
                                text = if (challengeProgress.isCompleted) {
                                    "🎁 CLAIM FREE CHALLENGE DICE!"
                                } else {
                                    "🎯 ${dice.challengeRequirementText} (${challengeProgress.currentProgress}/${challengeProgress.targetProgress})"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Coin Purchase Confirmation Dialog
    if (showCoinConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCoinConfirmDialog = false },
            containerColor = Color(0xFF14194A),
            modifier = Modifier.testTag("confirm_coin_purchase_dialog"),
            title = {
                Text("Confirm Dice Purchase 🎲", color = RoyalGold, fontWeight = FontWeight.Black)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Unlock ${dice.name} for 🪙 ${formatCompactCoins(dice.coinPrice)} Gold Coins?",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your balance: 🪙 ${formatCompactCoins(userCoins)}",
                        color = Color(0xFFC5CAE9),
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCoinConfirmDialog = false
                        onConfirmCoinBuy()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    modifier = Modifier.testTag("confirm_coin_purchase_accept")
                ) {
                    Text("Confirm Buy", color = Color(0xFF1B0A38), fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCoinConfirmDialog = false },
                    modifier = Modifier.testTag("confirm_coin_purchase_cancel")
                ) {
                    Text("Cancel", color = Color(0xFFB39DDB))
                }
            }
        )
    }
}

@Composable
private fun BenefitLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFFB39DDB), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
