package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.RewardedAdDialog
import com.example.ui.components.formatCompactCoins
import com.example.ui.components.MgLudoRoyalBackground
import com.example.ui.theme.*
import com.example.viewmodel.LudoUiState
import com.example.viewmodel.LudoViewModel

/**
 * RoyalShopScreen faithfully implements all 5 Store sections from Screenshots 7, 8, 9, 10, 11:
 * - Top header with Currency Counters (1.25M Coins, 72 Cash), Back arrow, and "SALE" banner
 * - Decorative Magical Book & Vines header
 * - 5 Tabs:
 *   1. Themes (Screenshot 7: Easter & Wiccap pawns, Neon, Garden, Ocean boards)
 *   2. Coins (Screenshot 10: 500 ad to 560M coins with Rupee prices and extra bonuses)
 *   3. Cash (Screenshot 9: 2 cash ad, 1000 cash VIP bunny, emoji pack 2, enchanted dice)
 *   4. Chests (Screenshot 11: Emerald Wealth special offer, Diamond, Emerald, Ruby chests)
 *   5. Passes & Ads (Screenshot 8: No Ads 30D/Life, Voice Passes 1D, 3D, 30D)
 */
@Composable
fun RoyalShopScreen(
    uiState: LudoUiState,
    viewModel: LudoViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Coins") }
    val tabs = listOf("Coins", "Cash", "Themes", "Chests", "Passes")

    var showRewardedAdDialog by remember { mutableStateOf(false) }
    var adRewardType by remember { mutableStateOf("Coins") } // "Coins" or "Cash"

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2E63DE)) // Ludo Club Top Royal Blue
                    .statusBarsPadding()
            ) {
                // Top Action Bar: Back button, Coins, Cash, SALE Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFC107))
                            .clickable { onBack() }
                            .testTag("shop_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF333333),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Currency Pills
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Coins Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0F1E3D))
                                .border(1.dp, Color(0xFFFFA000), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪙", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatCompactCoins(uiState.userCoins),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00C853))
                                        .clickable {
                                            adRewardType = "Coins"
                                            showRewardedAdDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Cash Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0F1E3D))
                                .border(1.dp, Color(0xFF00E676), RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💵", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.userGems}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00C853))
                                        .clickable {
                                            adRewardType = "Cash"
                                            showRewardedAdDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // SALE Red Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFD50000))
                            .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SALE",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Decorative Magical Book Header with Vines & Pumpkins
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFF280E4B)), // Deep mystical purple
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🎃", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "📖 STORE 🪄",
                            color = Color(0xFFFFD54F),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🐱‍👤", fontSize = 22.sp)
                    }
                }

                // Tab Bar
                TabRow(
                    selectedTabIndex = tabs.indexOf(selectedTab),
                    containerColor = Color(0xFF1E0A3A),
                    contentColor = Color(0xFFFFD54F)
                ) {
                    tabs.forEach { tabName ->
                        Tab(
                            selected = selectedTab == tabName,
                            onClick = { selectedTab = tabName },
                            text = {
                                Text(
                                    text = tabName,
                                    color = if (selectedTab == tabName) Color(0xFFFFD54F) else Color(0xFFB39DDB),
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == tabName) FontWeight.Black else FontWeight.Bold
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // MG Ludo Royal Deep Blue Background
            MgLudoRoyalBackground()

            when (selectedTab) {
                // ==========================================
                // 1. COINS STORE (Screenshot 10)
                // ==========================================
                "Coins" -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Card 1: 500 Coins (Watch Ad)
                        item {
                            StoreCoinPackCard(
                                amount = "500",
                                price = "Watch Ad 🎬",
                                isAd = true,
                                icon = "🪙",
                                onBuy = {
                                    adRewardType = "Coins"
                                    showRewardedAdDialog = true
                                }
                            )
                        }

                        // Card 2: 40,000 Coins (₹13.29, Limited)
                        item {
                            StoreCoinPackCard(
                                amount = "40,000",
                                price = "₹13.29",
                                badge = "Limited",
                                badgeColor = Color(0xFF0288D1),
                                icon = "💰",
                                onBuy = {
                                    viewModel.rewardUserForAd(coins = 40000L, gems = 0)
                                    Toast.makeText(context, "Purchased 40,000 Coins! 🪙", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Card 3: 100 K Coins (₹36.19)
                        item {
                            StoreCoinPackCard(
                                amount = "100 K",
                                price = "₹36.19",
                                badge = "Limited",
                                badgeColor = Color(0xFF0288D1),
                                icon = "💰",
                                onBuy = {
                                    viewModel.rewardUserForAd(coins = 100000L, gems = 0)
                                    Toast.makeText(context, "Purchased 100 K Coins! 🪙", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Card 4: 226 K Coins (₹45.39, 50% extra)
                        item {
                            StoreCoinPackCard(
                                amount = "226 K",
                                originalAmount = "150,000",
                                price = "₹45.39",
                                bonusBanner = "First Purchase 50% EXTRA",
                                icon = "🪙",
                                onBuy = {
                                    viewModel.rewardUserForAd(coins = 226000L, gems = 0)
                                    Toast.makeText(context, "Purchased 226 K Coins! 🪙", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Card 5: 1.14 M Coins (₹114.19, 50% extra)
                        item {
                            StoreCoinPackCard(
                                amount = "1.14 M",
                                originalAmount = "760,000",
                                price = "₹114.19",
                                bonusBanner = "First Purchase 50% EXTRA",
                                icon = "🏺",
                                onBuy = {
                                    viewModel.rewardUserForAd(coins = 1140000L, gems = 0)
                                    Toast.makeText(context, "Purchased 1.14 M Coins! 🪙", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Card 6: 8.80 M Coins (₹549.79, 66% extra)
                        item {
                            StoreCoinPackCard(
                                amount = "8.80 M",
                                originalAmount = "5.20 M",
                                price = "₹549.79",
                                bonusBanner = "First Purchase 66% EXTRA",
                                icon = "📦",
                                onBuy = {
                                    viewModel.rewardUserForAd(coins = 8800000L, gems = 0)
                                    Toast.makeText(context, "Purchased 8.80 M Coins! 🪙", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Card 7: 22 M Coins (₹824.89, Popular)
                        item {
                            StoreCoinPackCard(
                                amount = "22 M",
                                originalAmount = "13.20 M",
                                price = "₹824.89",
                                badge = "Popular",
                                badgeColor = Color(0xFFD50000),
                                bonusBanner = "First Purchase 66% EXTRA",
                                icon = "🏰",
                                onBuy = {
                                    viewModel.rewardUserForAd(coins = 22000000L, gems = 0)
                                    Toast.makeText(context, "Purchased 22 M Coins! 🪙", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Card 8: 94.50 M Coins (₹1,649.00)
                        item {
                            StoreCoinPackCard(
                                amount = "94.50 M",
                                originalAmount = "54 M",
                                price = "₹1,649.00",
                                bonusBanner = "First Purchase 75% EXTRA",
                                icon = "🛒",
                                onBuy = {
                                    viewModel.rewardUserForAd(coins = 94500000L, gems = 0)
                                    Toast.makeText(context, "Purchased 94.50 M Coins! 🪙", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // Card 9: 560 M Coins (₹5,499.00, Best Value)
                        item {
                            StoreCoinPackCard(
                                amount = "560 M",
                                originalAmount = "280 M",
                                price = "₹5,499.00",
                                badge = "Best Value",
                                badgeColor = Color(0xFF7B1FA2),
                                bonusBanner = "First Purchase 100% EXTRA",
                                icon = "🚛",
                                onBuy = {
                                    viewModel.rewardUserForAd(coins = 560000000L, gems = 0)
                                    Toast.makeText(context, "Purchased 560 M Coins! 🪙", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // ==========================================
                // 2. CASH STORE (Screenshot 9)
                // ==========================================
                "Cash" -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Emoji pack 2 (500 Cash)
                        item {
                            StoreSpecialItemCard(
                                title = "Emoji pack 2",
                                price = "500 💵",
                                icon = "🤠😭",
                                onBuy = { Toast.makeText(context, "Unlocked Emoji Pack 2! ✨", Toast.LENGTH_SHORT).show() }
                            )
                        }

                        // Enchanted dice (2 M Coins)
                        item {
                            StoreSpecialItemCard(
                                title = "Enchanted dice",
                                price = "2 M 🪙",
                                icon = "🎲",
                                onBuy = { Toast.makeText(context, "Equipped Enchanted Dice! 🎲", Toast.LENGTH_SHORT).show() }
                            )
                        }

                        // 2 Cash (Watch Ad)
                        item {
                            StoreCoinPackCard(
                                amount = "2",
                                price = "Watch Ad 🎬",
                                isAd = true,
                                icon = "💵",
                                onBuy = {
                                    adRewardType = "Cash"
                                    showRewardedAdDialog = true
                                }
                            )
                        }

                        // 1000 Cash VIP Billionaire Bunny (₹249.00)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.rewardUserForAd(coins = 0, gems = 1000)
                                        Toast.makeText(context, "Unlocked 1,000 Cash + VIP Bunny! 🐰💵", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("1000", color = Color(0xFF00796B), fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("🐰🕶️💰", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF00C853))
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("₹249.00", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        // 50 Cash (₹13.29, Limited)
                        item {
                            StoreCoinPackCard(
                                amount = "50",
                                price = "₹13.29",
                                badge = "Limited",
                                badgeColor = Color(0xFF0288D1),
                                icon = "💵",
                                onBuy = {
                                    viewModel.rewardUserForAd(coins = 0, gems = 50)
                                    Toast.makeText(context, "Purchased 50 Cash! 💵", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // 70 Cash
                        item {
                            StoreCoinPackCard(
                                amount = "70",
                                price = "₹18.29",
                                icon = "💵",
                                onBuy = {
                                    viewModel.rewardUserForAd(coins = 0, gems = 70)
                                    Toast.makeText(context, "Purchased 70 Cash! 💵", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // 210 Cash (was 120, 75% extra)
                        item {
                            StoreCoinPackCard(
                                amount = "210",
                                originalAmount = "120",
                                price = "₹45.39",
                                bonusBanner = "75% EXTRA",
                                icon = "💵",
                                onBuy = {
                                    viewModel.rewardUserForAd(coins = 0, gems = 210)
                                    Toast.makeText(context, "Purchased 210 Cash! 💵", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // 450 Cash (was 250, 78% extra)
                        item {
                            StoreCoinPackCard(
                                amount = "450",
                                originalAmount = "250",
                                price = "₹91.19",
                                bonusBanner = "78% EXTRA",
                                icon = "💵",
                                onBuy = {
                                    viewModel.rewardUserForAd(coins = 0, gems = 450)
                                    Toast.makeText(context, "Purchased 450 Cash! 💵", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // ==========================================
                // 3. THEMES & SKINS (Screenshot 7)
                // ==========================================
                "Themes" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "Pawn Skins",
                                color = Color(0xFFFFD54F),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Easter & Wiccap Pawns Row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Easter Pawns
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.purchaseOrEquipShopItem(
                                                com.example.model.ShopItem("pawn_easter", "Easter Eggs", "Pawns", 150000L, 0, "🥚")
                                            )
                                            Toast.makeText(context, "Easter Pawns Equipped! 🥚", Toast.LENGTH_SHORT).show()
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7ECD8))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Easter", color = Color(0xFF4A148C), fontSize = 14.sp, fontWeight = FontWeight.Black)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("🥚🐣🥚🐣", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF00C853))
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🪙 150 K", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }

                                // Wiccap Pawns
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.purchaseOrEquipShopItem(
                                                com.example.model.ShopItem("pawn_wiccap", "Wiccap Hats", "Pawns", 150000L, 0, "🧙")
                                            )
                                            Toast.makeText(context, "Wiccap Witch Hat Pawns Equipped! 🧙", Toast.LENGTH_SHORT).show()
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7ECD8))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Wiccap", color = Color(0xFF4A148C), fontSize = 14.sp, fontWeight = FontWeight.Black)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("🧙‍♂️🧙‍♀️🧙‍♂️🧙‍♀️", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF00C853))
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🪙 150 K", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // Section: Board Skins
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFD50000))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Board", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        // Boards: Neon & Garden & Ocean
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Neon Board
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.selectBoardTheme("board_neon")
                                            Toast.makeText(context, "Neon Board Equipped! ⚡", Toast.LENGTH_SHORT).show()
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7ECD8))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Neon", color = Color(0xFF4A148C), fontSize = 14.sp, fontWeight = FontWeight.Black)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("🟦🟥🟨🟩", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF00C853))
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("💵 1 K", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }

                                // Garden Board
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.selectBoardTheme("board_garden")
                                            Toast.makeText(context, "Garden Board Equipped! 🌸", Toast.LENGTH_SHORT).show()
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7ECD8))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Garden", color = Color(0xFF4A148C), fontSize = 14.sp, fontWeight = FontWeight.Black)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("🌸🌿🌺🌼", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF00C853))
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("💵 3 K", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }

                                // Ocean Board
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            viewModel.selectBoardTheme("board_ocean")
                                            Toast.makeText(context, "Ocean Atlantis Board Equipped! 🌊", Toast.LENGTH_SHORT).show()
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7ECD8))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Ocean", color = Color(0xFF4A148C), fontSize = 14.sp, fontWeight = FontWeight.Black)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("🌊🐚🐬🏝️", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF00C853))
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("💵 2.5 K", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }

                        // Section: Custom Dice
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFD50000))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Custom Dice", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        // Custom Dice row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple("Imperial Gold", "🎲✨", "🪙 50 K"),
                                    Triple("Ruby Flame", "🔥🎲", "🪙 75 K"),
                                    Triple("Diamond Ice", "💎🎲", "💵 500"),
                                    Triple("Neon Matrix", "⚡🎲", "💵 1 K")
                                ).forEach { (diceName, emoji, cost) ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                Toast.makeText(context, "$diceName Dice Equipped! $emoji", Toast.LENGTH_SHORT).show()
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7ECD8))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(diceName, color = Color(0xFF4A148C), fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(emoji, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF00C853))
                                                    .padding(vertical = 2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(cost, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section: In-Game Emoji Packs
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFD50000))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("In-Game Emoji Packs", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple("Royal Maharaja", "👑🦁🐘", "🪙 100 K"),
                                    Triple("Funny Reaction", "😂🤣🤪", "🪙 50 K"),
                                    Triple("Taunt & Fire", "😜🔥🥊", "💵 800")
                                ).forEach { (packName, emojis, cost) ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                Toast.makeText(context, "$packName Pack Unlocked! $emojis", Toast.LENGTH_SHORT).show()
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7ECD8))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(packName, color = Color(0xFF4A148C), fontSize = 10.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(emojis, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF00C853))
                                                    .padding(vertical = 2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(cost, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. CHESTS & SPECIAL OFFERS (Screenshot 11)
                // ==========================================
                "Chests" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Emerald Wealth Special Offer Banner
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.rewardUserForAd(coins = 10000L, gems = 250)
                                        Toast.makeText(context, "Claimed Emerald Wealth Bundle! 🟢💎", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Emerald Wealth!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                        Text("⏱️ 18d 4h", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("💵", fontSize = 24.sp)
                                            Text("250 Cash", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🟢📦", fontSize = 28.sp)
                                            Text("Emerald Chest", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🪙", fontSize = 24.sp)
                                            Text("10 K Coins", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFD50000))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("60% OFF", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF00C853))
                                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                        ) {
                                            Text("₹274.59", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }

                        // Premium Chests Header
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFD50000))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Premium Chests (i)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        // 3 Premium Chests (Diamond, Emerald, Ruby)
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Diamond Chest (300 Cash)
                                StoreChestCard(
                                    title = "Diamond",
                                    icon = "💎📦",
                                    price = "300 💵",
                                    onBuy = { Toast.makeText(context, "Opened Diamond Chest! (+50K Coins) 💎", Toast.LENGTH_SHORT).show() }
                                )

                                // Emerald Chest (900 Cash)
                                StoreChestCard(
                                    title = "Emerald",
                                    icon = "🟢📦",
                                    price = "900 💵",
                                    onBuy = { Toast.makeText(context, "Opened Emerald Chest! (+150K Coins) 🟢", Toast.LENGTH_SHORT).show() }
                                )

                                // Ruby Chest (1800 Cash)
                                StoreChestCard(
                                    title = "Ruby",
                                    icon = "🔴📦",
                                    price = "1800 💵",
                                    onBuy = { Toast.makeText(context, "Opened Ruby Royal Chest! (+500K Coins) 🔴", Toast.LENGTH_SHORT).show() }
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 5. PASSES & NO ADS (Screenshot 8)
                // ==========================================
                "Passes" -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFD50000))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No ads", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        // No Ads: 30 Days & Life Time
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StorePassCard(
                                    title = "30 Days",
                                    icon = "🎬🚫",
                                    price = "₹45.39",
                                    onBuy = {
                                        viewModel.openInAppPurchase(
                                            com.example.ui.components.InAppPurchaseProduct(
                                                id = "pass_no_ads_30d",
                                                title = "No Ads (30 Days)",
                                                description = "Play 30 days without interstitial banner or pop-up ads",
                                                iconEmoji = "🎬🚫",
                                                priceFormatted = "₹45.39",
                                                unlocksNoAds = true
                                            )
                                        )
                                    }
                                )
                                StorePassCard(
                                    title = "Life Time",
                                    icon = "👑🚫",
                                    price = "₹458.09",
                                    onBuy = {
                                        viewModel.openInAppPurchase(
                                            com.example.ui.components.InAppPurchaseProduct(
                                                id = "pass_no_ads_lifetime",
                                                title = "Lifetime Ad-Free VIP",
                                                description = "Never see another ad forever across all game modes & tournaments",
                                                iconEmoji = "👑🚫",
                                                priceFormatted = "₹458.09",
                                                unlocksNoAds = true
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFD50000))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Voice (i)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        // Voice passes: 1 Day, 3 Days, 30 Days
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StorePassCard(
                                    title = "1 Day",
                                    icon = "🎙️",
                                    price = "₹45.39",
                                    onBuy = {
                                        viewModel.openInAppPurchase(
                                            com.example.ui.components.InAppPurchaseProduct(
                                                id = "pass_voice_1d",
                                                title = "1-Day Voice Pass",
                                                description = "24-hour full access to in-game HD voice chat lounge",
                                                iconEmoji = "🎙️",
                                                priceFormatted = "₹45.39",
                                                voiceDaysReward = 1
                                            )
                                        )
                                    }
                                )
                                StorePassCard(
                                    title = "3 Days",
                                    icon = "🎙️🎙️",
                                    price = "₹91.19",
                                    onBuy = {
                                        viewModel.openInAppPurchase(
                                            com.example.ui.components.InAppPurchaseProduct(
                                                id = "pass_voice_3d",
                                                title = "3-Day Voice Pass",
                                                description = "72-hour unlimited high-clarity voice communications",
                                                iconEmoji = "🎙️🎙️",
                                                priceFormatted = "₹91.19",
                                                voiceDaysReward = 3
                                            )
                                        )
                                    }
                                )
                                StorePassCard(
                                    title = "30 Days",
                                    icon = "🎙️🪙",
                                    price = "₹366.39",
                                    onBuy = {
                                        viewModel.openInAppPurchase(
                                            com.example.ui.components.InAppPurchaseProduct(
                                                id = "pass_voice_30d",
                                                title = "30-Day VIP Voice Pass",
                                                description = "30-day VIP crystal voice chat with opponents & royal friends",
                                                iconEmoji = "🎙️🪙",
                                                priceFormatted = "₹366.39",
                                                voiceDaysReward = 30
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Rewarded Video Ad Flow
    if (showRewardedAdDialog) {
        RewardedAdDialog(
            rewardTitle = if (adRewardType == "Cash") "2 Cash 💵" else "1,000 Coins 🪙",
            onRewardEarned = {
                if (adRewardType == "Cash") {
                    viewModel.rewardUserForAd(coins = 0, gems = 2)
                    Toast.makeText(context, "Earned 2 Cash! 💵", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.rewardUserForAd(coins = 1000L, gems = 0)
                    Toast.makeText(context, "Earned 1,000 Coins! 🪙", Toast.LENGTH_SHORT).show()
                }
                showRewardedAdDialog = false
            },
            onDismiss = { showRewardedAdDialog = false }
        )
    }

    // Real-Money In-App Purchase Gateway Simulation
    if (uiState.activeInAppProduct != null) {
        com.example.ui.components.InAppPurchaseGatewayDialog(
            product = uiState.activeInAppProduct,
            onPurchaseCompleted = { product ->
                viewModel.completeInAppPurchase(product)
                Toast.makeText(context, "Purchased ${product.title}! 🎉", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { viewModel.dismissInAppPurchase() }
        )
    }
}

@Composable
private fun StoreCoinPackCard(
    amount: String,
    price: String,
    originalAmount: String? = null,
    badge: String? = null,
    badgeColor: Color = Color(0xFF0288D1),
    bonusBanner: String? = null,
    isAd: Boolean = false,
    icon: String,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBuy() }
            .testTag("shop_pack_$amount"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7ECD8)),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFD7CCC8))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Amount
            Text(
                text = amount,
                color = Color(0xFF3E2723),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )

            if (originalAmount != null) {
                Text(
                    text = originalAmount,
                    color = Color(0xFFB71C1C),
                    fontSize = 10.sp,
                    textDecoration = TextDecoration.LineThrough
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Icon with optional badge
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 28.sp)
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor)
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(badge, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (bonusBanner != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFD50000))
                        .padding(vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(bonusBanner, color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(3.dp))
            }

            // Green Buy Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF00C853))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(price, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun StoreSpecialItemCard(
    title: String,
    price: String,
    icon: String,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBuy() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7ECD8))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color(0xFF4A148C), fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF00C853))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(price, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun StoreChestCard(
    title: String,
    icon: String,
    price: String,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBuy() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7ECD8))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color(0xFF3E2723), fontSize = 13.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(6.dp))
            Text(icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF00C853))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(price, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun StorePassCard(
    title: String,
    icon: String,
    price: String,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBuy() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7ECD8))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color(0xFF3E2723), fontSize = 13.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(6.dp))
            Text(icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF00C853))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(price, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
