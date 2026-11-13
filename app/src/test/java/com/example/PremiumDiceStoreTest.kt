package com.example

import android.app.Application
import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.engine.FairDiceEngine
import com.example.model.*
import com.example.ui.screens.GameScreen
import com.example.ui.screens.PremiumDiceStoreScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.ProfilePreferences
import com.example.viewmodel.LudoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PremiumDiceStoreTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var prefs: ProfilePreferences

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        application.getSharedPreferences("mg_ludo_profile_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        runBlocking(Dispatchers.IO) {
            AppDatabase.getDatabase(application).clearAllTables()
        }
        prefs = ProfilePreferences(application)
        prefs.setCurrentPlayerId(ProfilePreferences.DEFAULT_PLAYER_ID)
        prefs.setUserCoins(25_000L)
        prefs.setUserGems(120)
    }

    @Test
    fun `01 seeded catalog includes all 25 original dice across classic vip and special groups`() = runTest {
        val seed = PremiumDiceCatalog.buildDefaultSeedCatalog()
        assertEquals(25, seed.size)

        val expectedIds = listOf(
            "dice_imperial_gold",
            // Classic 9
            "dice_royal_blue", "dice_desert_warrior", "dice_orange_citrus",
            "dice_blue_royal_pattern", "dice_golden_palace", "dice_red_royal",
            "dice_festival", "dice_imperial_red", "dice_royal_mosaic",
            // VIP 4
            "dice_emerald_vip", "dice_sapphire_vip", "dice_ruby_vip", "dice_golden_leaf_vip",
            // Special 10 + Bonus Celestial Zodiac
            "dice_fire", "dice_nature", "dice_chocolate", "dice_panda", "dice_carnival",
            "dice_snowman", "dice_lion", "dice_sunset", "dice_cat", "dice_balloon",
            "dice_celestial_zodiac"
        )
        expectedIds.forEach { id ->
            assertNotNull("Missing expected dice id: $id", seed.firstOrNull { it.id == id })
        }

        val royalBlue = seed.first { it.id == "dice_royal_blue" }
        assertEquals(5000L, royalBlue.coinPrice)
        assertEquals(29, royalBlue.inrPrice)

        val emeraldVip = seed.first { it.id == "dice_emerald_vip" }
        assertEquals(75000L, emeraldVip.coinPrice)
        assertEquals(299, emeraldVip.inrPrice)
        assertEquals(DiceRarity.VIP, emeraldVip.rarity)

        val goldenLeaf = seed.first { it.id == "dice_golden_leaf_vip" }
        assertEquals(150000L, goldenLeaf.coinPrice)
        assertEquals(599, goldenLeaf.inrPrice)
        assertEquals(DiceRarity.VIP, goldenLeaf.rarity)
    }

    @Test
    fun `02 store UI filters by category rarity search and opens live preview detail popup`() {
        val viewModel = LudoViewModel(application)
        composeRule.setContent {
            val state by viewModel.uiState.collectAsState()
            MyApplicationTheme {
                PremiumDiceStoreScreen(uiState = state, viewModel = viewModel, onBack = {})
            }
        }

        // Default shows Royal Blue Dice in Classic group
        composeRule.onNodeWithTag("dice_card_dice_royal_blue").assertExists()

        // Filter to VIP collection
        composeRule.onNodeWithTag("dice_group_tab_VIP").performClick()
        composeRule.onNodeWithTag("dice_card_dice_emerald_vip").assertExists()
        composeRule.onNodeWithTag("dice_card_dice_royal_blue").assertDoesNotExist()

        // Filter to Special Themed
        composeRule.onNodeWithTag("dice_group_tab_SPECIAL_THEMED").performClick()
        composeRule.onNodeWithTag("dice_card_dice_fire").assertExists()

        // Search inside Special
        composeRule.onNodeWithTag("premium_dice_search_input").performTextInput("Panda")
        composeRule.onNodeWithTag("dice_card_dice_panda").assertExists()
        composeRule.onNodeWithTag("dice_card_dice_fire").assertDoesNotExist()

        // Open Detail Popup and Try Preview
        composeRule.onNodeWithTag("dice_card_dice_panda").performClick()
        composeRule.onNodeWithTag("dice_detail_dialog").assertExists()
        composeRule.onNodeWithTag("dice_try_preview_button").performClick()
        composeRule.onNodeWithText("Bamboo Wood Tap", substring = true).assertExists()
    }

    @Test
    fun `03 coin purchase confirmation deducts coins grants dice equips and blocks insufficient balance`() {
        val viewModel = LudoViewModel(application)
        composeRule.setContent {
            val state by viewModel.uiState.collectAsState()
            MyApplicationTheme {
                PremiumDiceStoreScreen(uiState = state, viewModel = viewModel, onBack = {})
            }
        }

        // 1. Purchase Royal Blue Dice (5,000 coins out of 25,000) via UI confirmation flow
        composeRule.onNodeWithTag("dice_card_dice_royal_blue").performClick()
        composeRule.onNodeWithTag("buy_dice_coins_button").performClick()
        composeRule.onNodeWithTag("confirm_coin_purchase_dialog").assertExists()
        composeRule.onNodeWithTag("confirm_coin_purchase_accept").performClick()

        composeRule.waitUntil(timeoutMillis = 3000L) {
            viewModel.uiState.value.diceStoreUiState.isOwned("dice_royal_blue")
        }
        assertEquals(20_000L, viewModel.uiState.value.userCoins)
        assertEquals("dice_royal_blue", viewModel.uiState.value.equippedDiceSkin)
        composeRule.onNodeWithTag("equipped_dice_indicator").assertExists()
        composeRule.onNodeWithTag("dice_detail_close_button").performClick()

        // 2. Attempt Golden Leaf VIP (150,000 coins > 20,000 coins) -> shows insufficient coins modal
        composeRule.onNodeWithTag("dice_group_tab_VIP").performClick()
        composeRule.onNodeWithTag("dice_card_dice_golden_leaf_vip").performClick()
        composeRule.onNodeWithTag("buy_dice_coins_button").performClick()
        composeRule.onNodeWithTag("confirm_coin_purchase_accept").performClick()

        composeRule.waitUntil(timeoutMillis = 3000L) {
            viewModel.uiState.value.diceStoreUiState.insufficientCoinsDialogDice != null
        }
        composeRule.onNodeWithTag("insufficient_coins_dialog").assertExists()
        assertFalse(viewModel.uiState.value.diceStoreUiState.isOwned("dice_golden_leaf_vip"))
        assertEquals(20_000L, viewModel.uiState.value.userCoins)
    }

    @Test
    fun `04 concurrent coin purchases are atomic and never double charge`() = runTest {
        val viewModel = LudoViewModel(application)
        viewModel.diceStoreRepository.ensureInitialized()

        val results = (1..6).map {
            async(Dispatchers.Default) {
                viewModel.diceStoreRepository.purchaseDiceWithCoins(diceId = "dice_royal_blue")
            }
        }.awaitAll()

        val successes = results.count { it.first is DiceCoinPurchaseResult.Success }
        val alreadyOwned = results.count { it.first is DiceCoinPurchaseResult.AlreadyOwned }
        assertEquals(1, successes)
        assertEquals(5, alreadyOwned)

        val finalSnap = viewModel.diceStoreRepository.getSnapshot()
        assertEquals(20_000L, finalSnap.goldCoins)
        assertTrue("dice_royal_blue" in finalSnap.ownedDiceIds)
    }

    @Test
    fun `05 play billing verification handles purchased pending failed cancelled duplicate token refund and restore`() = runTest {
        val viewModel = LudoViewModel(application)
        viewModel.diceStoreRepository.ensureInitialized()

        // 1. PENDING state does not grant dice
        val pendingPayload = BillingVerificationPayload(
            skuId = "mg_dice_ruby_vip",
            diceId = "dice_ruby_vip",
            purchaseToken = "tok_ruby_pending_001",
            orderId = "GPA.0001-PENDING",
            packageName = application.packageName,
            amountInr = 499,
            purchaseState = BillingPurchaseStatus.PENDING
        )
        val pendingRes = viewModel.processVerifiedBillingPayloadSync(pendingPayload)
        assertTrue(pendingRes is DiceBillingResult.Pending)
        assertFalse(viewModel.uiState.value.diceStoreUiState.isOwned("dice_ruby_vip"))
        assertTrue(viewModel.uiState.value.diceStoreUiState.isPendingBilling("dice_ruby_vip"))

        // 2. CANCELLED & FAILED do not grant dice
        val cancelRes = viewModel.processVerifiedBillingPayloadSync(
            pendingPayload.copy(
                skuId = "mg_dice_sapphire_vip",
                diceId = "dice_sapphire_vip",
                purchaseToken = "tok_sapphire_cancel",
                amountInr = 399,
                purchaseState = BillingPurchaseStatus.CANCELLED
            )
        )
        assertTrue(cancelRes is DiceBillingResult.Cancelled)
        assertFalse(viewModel.uiState.value.diceStoreUiState.isOwned("dice_sapphire_vip"))

        val failedRes = viewModel.processVerifiedBillingPayloadSync(
            pendingPayload.copy(
                skuId = "mg_dice_sapphire_vip",
                diceId = "dice_sapphire_vip",
                purchaseToken = "tok_sapphire_failed",
                amountInr = 399,
                purchaseState = BillingPurchaseStatus.FAILED
            )
        )
        assertTrue(failedRes is DiceBillingResult.Failed)
        assertFalse(viewModel.uiState.value.diceStoreUiState.isOwned("dice_sapphire_vip"))

        // 3. Verified PURCHASED grants & equips Ruby VIP + syncs to Inventory
        val verifiedPayload = pendingPayload.copy(
            purchaseToken = "tok_ruby_verified_999",
            orderId = "GPA.9999-RUBY",
            purchaseState = BillingPurchaseStatus.PURCHASED
        )
        val grantedRes = viewModel.processVerifiedBillingPayloadSync(verifiedPayload)
        assertTrue(grantedRes is DiceBillingResult.VerifiedGranted)
        assertTrue(viewModel.uiState.value.diceStoreUiState.isOwned("dice_ruby_vip"))
        assertEquals("dice_ruby_vip", viewModel.uiState.value.equippedDiceSkin)
        assertTrue(viewModel.uiState.value.inventoryUiState.isOwned("dice_ruby_vip"))

        // 4. Duplicate token replay is rejected cleanly
        val dupRes = viewModel.processVerifiedBillingPayloadSync(verifiedPayload)
        assertTrue(dupRes is DiceBillingResult.DuplicateTokenRejected)

        // 5. REFUNDED revokes ownership & unequips active dice back to starter
        val refundRes = viewModel.processVerifiedBillingPayloadSync(
            verifiedPayload.copy(purchaseState = BillingPurchaseStatus.REFUNDED)
        )
        assertTrue(refundRes is DiceBillingResult.RefundedRevoked)
        assertFalse(viewModel.uiState.value.diceStoreUiState.isOwned("dice_ruby_vip"))
        assertEquals("dice_imperial_gold", viewModel.uiState.value.equippedDiceSkin)

        // 6. Restore Purchases recovers a verified purchase after wipe/sync
        val emeraldPayload = BillingVerificationPayload(
            skuId = "mg_dice_emerald_vip",
            diceId = "dice_emerald_vip",
            purchaseToken = "tok_emerald_restore_777",
            orderId = "GPA.7777-EMERALD",
            packageName = application.packageName,
            amountInr = 299,
            purchaseState = BillingPurchaseStatus.PURCHASED
        )
        val (restoredCount, restoredSnap) = viewModel.diceStoreRepository.restorePurchases(
            playPurchases = listOf(emeraldPayload)
        )
        assertEquals(1, restoredCount)
        assertTrue("dice_emerald_vip" in restoredSnap.ownedDiceIds)
    }

    @Test
    fun `06 fair play RNG distribution remains uniform across all premium and VIP dice skins`() {
        val skins = listOf("dice_imperial_gold", "dice_ruby_vip", "dice_golden_leaf_vip", "dice_fire")
        skins.forEach { skin ->
            prefs.setEquippedDiceSkin(skin)
            val counts = IntArray(7)
            val trials = 6000
            FairDiceEngine.rollNewSeeds()
            repeat(trials) {
                val (value, _) = FairDiceEngine.rollDice()
                assertTrue("Roll out of 1..6 range: $value", value in 1..6)
                counts[value]++
            }
            // Expected count per face = 1000. Verify every face falls comfortably inside 800..1200
            for (face in 1..6) {
                assertTrue(
                    "Dice face $face skewed for $skin: ${counts[face]}",
                    counts[face] in 800..1200
                )
            }
        }
    }

    @Test
    fun `07 admin panel updates live price benefits availability and custom dice in store and room`() = runTest {
        val viewModel = LudoViewModel(application)
        viewModel.diceStoreRepository.ensureInitialized()

        val fireOriginal = viewModel.diceStoreRepository.getSnapshot().catalog.first { it.id == "dice_fire" }
        val fireUpdated = fireOriginal.copy(
            name = "Inferno Phoenix Dice",
            coinPrice = 9999L,
            inrPrice = 45,
            benefits = fireOriginal.benefits.copy(visualEffectSummary = "Admin Hotfix: Phoenix Blaze")
        )
        val afterUpdateSnap = viewModel.diceStoreRepository.adminSaveOrUpdateDice(fireUpdated)
        val loadedFire = afterUpdateSnap.catalog.first { it.id == "dice_fire" }
        assertEquals("Inferno Phoenix Dice", loadedFire.name)
        assertEquals(9999L, loadedFire.coinPrice)
        assertEquals(45, loadedFire.inrPrice)
        assertEquals("Admin Hotfix: Phoenix Blaze", loadedFire.benefits.visualEffectSummary)

        // Create a brand new Admin Dice
        val customDice = fireOriginal.copy(
            id = "dice_admin_diwali_gold",
            name = "Diwali Crown Dice",
            skuId = "mg_dice_admin_diwali_gold",
            coinPrice = 12345L,
            inrPrice = 55,
            sortOrder = 99
        )
        val afterCreateSnap = viewModel.diceStoreRepository.adminSaveOrUpdateDice(customDice)
        assertNotNull(afterCreateSnap.catalog.firstOrNull { it.id == "dice_admin_diwali_gold" })

        // Verify Inventory catalog synced dynamically
        assertNotNull(ItemCatalog.findById("dice_admin_diwali_gold"))

        // Verify Delete removes custom admin dice cleanly
        val afterDeleteSnap = viewModel.diceStoreRepository.adminDeleteDice("dice_admin_diwali_gold")
        assertNull(afterDeleteSnap.catalog.firstOrNull { it.id == "dice_admin_diwali_gold" })
    }

    @Test
    fun `08 equipped dice persists across app restart and integrates into active gameplay`() = runTest {
        val firstVm = LudoViewModel(application)
        firstVm.diceStoreRepository.ensureInitialized()

        val emeraldPayload = BillingVerificationPayload(
            skuId = "mg_dice_emerald_vip",
            diceId = "dice_emerald_vip",
            purchaseToken = "tok_emerald_persist_555",
            orderId = "GPA.5555-EMERALD",
            packageName = application.packageName,
            amountInr = 299,
            purchaseState = BillingPurchaseStatus.PURCHASED
        )
        firstVm.processVerifiedBillingPayloadSync(emeraldPayload)
        assertEquals("dice_emerald_vip", firstVm.uiState.value.equippedDiceSkin)

        // Recreate fresh ViewModel (simulating cold app restart)
        val restartedVm = LudoViewModel(application)
        val snap = restartedVm.diceStoreRepository.ensureInitialized()
        restartedVm.applyDiceStoreSnapshot(snap)
        assertTrue("dice_emerald_vip" in restartedVm.uiState.value.diceStoreUiState.ownedDiceIds)
        assertEquals("dice_emerald_vip", restartedVm.uiState.value.equippedDiceSkin)

        // Verify challenge progress increments & unlocks Balloon Dice after 15 rolls
        var challengeSnap = restartedVm.diceStoreRepository.incrementChallengeProgress(delta = 15).second
        assertTrue(challengeSnap.eventRewards["dice_balloon"]?.isCompleted == true)
        val (claimedOk, afterClaimSnap) = restartedVm.diceStoreRepository.claimEventOrChallengeDice(diceId = "dice_balloon")
        assertTrue(claimedOk)
        assertTrue("dice_balloon" in afterClaimSnap.ownedDiceIds)

        // Verify GameScreen mounts with equipped dice in gameplay
        composeRule.setContent {
            val state by restartedVm.uiState.collectAsState()
            MyApplicationTheme {
                GameScreen(uiState = state, viewModel = restartedVm)
            }
        }
        composeRule.onNodeWithTag("corner_dice_yellow").assertExists()
    }
}
