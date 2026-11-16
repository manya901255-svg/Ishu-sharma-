package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
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
import com.example.model.DEFAULT_SEASON_PASS_TIERS
import com.example.model.SeasonPassTier
import com.example.ui.theme.*

/**
 * RoyalSeasonPassDialog: Season Pass system allowing players to level up and claim
 * every reward inside it (coins, gems, crowns, pawn skins, and mystery chest).
 */
@Composable
fun RoyalSeasonPassDialog(
    playerLevel: Int = 1,
    claimedBonusIds: Set<String> = emptySet(),
    onClaimTierReward: (level: Int, coins: Long, gems: Int) -> Unit = { _, _, _ -> },
    onClaimAll: (totalCoins: Long, totalGems: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val tiers = DEFAULT_SEASON_PASS_TIERS
    val unlockedUnclaimedTiers = remember(tiers, playerLevel, claimedBonusIds) {
        tiers.filter { tier ->
            val key = "season_pass_t${tier.level}"
            playerLevel >= tier.level && key !in claimedBonusIds
        }
    }
    val unlockedCoins = unlockedUnclaimedTiers.sumOf { it.freeRewardCoins + it.vipRewardCoins }
    val unlockedGems = unlockedUnclaimedTiers.sumOf { it.freeRewardGems + it.vipRewardGems }
    val unlockedCount = tiers.count { playerLevel >= it.level }
    val canClaimAnyUnlocked = unlockedUnclaimedTiers.isNotEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .shadow(28.dp, RoundedCornerShape(24.dp))
                .testTag("royal_season_pass_dialog"),
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
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text("👑", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "MAHARAJA ROYAL PASS",
                                color = RoyalGold,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Level $playerLevel • $unlockedCount/${tiers.size} Tiers Unlocked",
                                color = Color(0xFF00E676),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_season_pass_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13295C)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("READY TO CLAIM", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🪙 +${formatCompactCoins(unlockedCoins)}", color = RoyalGoldLight, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("💎 +$unlockedGems", color = SapphireBlueLight, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (unlockedCount > 0) Color(0xFF2E7D32) else Color(0xFF37474F))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "LVL $playerLevel PASS",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (canClaimAnyUnlocked) {
                                    unlockedUnclaimedTiers.forEach { t ->
                                        onClaimTierReward(t.level, t.freeRewardCoins + t.vipRewardCoins, t.freeRewardGems + t.vipRewardGems)
                                    }
                                    if (unlockedUnclaimedTiers.isEmpty()) {
                                        onClaimAll(unlockedCoins, unlockedGems)
                                    }
                                }
                            },
                            enabled = canClaimAnyUnlocked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("claim_all_season_pass_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB300),
                                disabledContainerColor = Color(0xFF1B3D2F)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (canClaimAnyUnlocked) {
                                    "🎁 CLAIM UNLOCKED (+${formatCompactCoins(unlockedCoins)} COINS)"
                                } else {
                                    "PLAY MATCHES TO LEVEL UP & UNLOCK TIERS"
                                },
                                color = if (canClaimAnyUnlocked) RoyalNavyDark else Color(0xFF90CAF9),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Season 1 Tier Progression (Play matches for XP)",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tiers, key = { it.level }) { tier ->
                        val isUnlocked = playerLevel >= tier.level
                        val isClaimed = "season_pass_t${tier.level}" in claimedBonusIds
                        SeasonPassTierRow(
                            tier = tier,
                            isUnlocked = isUnlocked,
                            isClaimed = isClaimed,
                            onClaimTier = {
                                onClaimTierReward(
                                    tier.level,
                                    tier.freeRewardCoins + tier.vipRewardCoins,
                                    tier.freeRewardGems + tier.vipRewardGems
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeasonPassTierRow(
    tier: SeasonPassTier,
    isUnlocked: Boolean,
    isClaimed: Boolean,
    onClaimTier: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("season_pass_tier_${tier.level}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF091B3D)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isClaimed -> Color(0xFF00E676)
                isUnlocked -> RoyalGold
                else -> Color(0xFF1E3A6E)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isClaimed -> Color(0xFF1B5E20)
                                isUnlocked -> RoyalGoldDark
                                else -> Color(0xFF1C2C4D)
                            }
                        )
                        .border(1.5.dp, if (isClaimed) Color(0xFF00E676) else RoyalGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("T${tier.level}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Free: ${tier.freeRewardTitle}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "VIP: ${tier.vipRewardTitle}",
                        color = RoyalGoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            when {
                isClaimed -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1B3D2F))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("CLAIMED ✓", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                isUnlocked -> {
                    Button(
                        onClick = onClaimTier,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("claim_tier_${tier.level}_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("CLAIM", color = RoyalNavyDark, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF14244D))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Lvl ${tier.level}", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
