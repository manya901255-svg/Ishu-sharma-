package com.example

import android.app.Application
import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.InventoryRepository
import com.example.model.*
import com.example.ui.screens.InventoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.ProfilePreferences
import com.example.viewmodel.AppScreen
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
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class InventorySystemTest {

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

    // 1. Category switching works across all tabs
    @Test
    fun `01 category switching displays respective collections`() {
        val viewModel = LudoViewModel(application)
        composeRule.setContent {
            val state by viewModel.uiState.collectAsState()
            MyApplicationTheme {
                InventoryScreen(uiState = state, viewModel = viewModel, onBack = {})
            }
        }

        // Default category is DICE -> Medusa visible
        composeRule.onNodeWithTag("inventory_tab_DICE").assertExists()
        composeRule.onNodeWithText("Medusa").assertExists()

        // Switch to FRAME -> Campfire visible
        composeRule.onNodeWithTag("inventory_tab_FRAME").performClick()
        composeRule.onNodeWithText("Campfire").assertExists()

        // Switch to EMOJI -> 18-slot deck and Squall visible
        composeRule.onNodeWithTag("inventory_tab_EMOJI").performClick()
        composeRule.onNodeWithTag("emoji_deck_slot_0").assertExists()
        composeRule.onNodeWithText("Charmed").assertExists()

        // Switch to BOARD -> Luxor visible
        composeRule.onNodeWithTag("inventory_tab_BOARD").performClick()
        composeRule.onNodeWithText("Luxor").assertExists()

        // Switch to PAWN -> Roasty & trail toggle visible
        composeRule.onNodeWithTag("inventory_tab_PAWN").performClick()
        composeRule.onNodeWithTag("pawn_trail_toggle").assertExists()
        composeRule.onNodeWithText("Roasty").assertExists()

        // Switch to POWERUP -> XP Booster visible
        composeRule.onNodeWithTag("inventory_tab_POWERUP").performClick()
        composeRule.onNodeWithText("XP Booster").assertExists()

        // Switch to OWNED -> starter items visible
        composeRule.onNodeWithTag("inventory_tab_OWNED").performClick()
        composeRule.onNodeWithText("Imperial Gold").assertExists()
    }

    // 2. Real-time search bar filtering
    @Test
    fun `02 search filters items by real name in real time`() {
        val viewModel = LudoViewModel(application)
        composeRule.setContent {
            val state by viewModel.uiState.collectAsState()
            MyApplicationTheme {
                InventoryScreen(uiState = state, viewModel = viewModel, onBack = {})
            }
        }

        composeRule.onNodeWithTag("inventory_search_input").performTextInput("Medusa")
        composeRule.onNodeWithTag("inventory_item_card_dice_medusa").assertExists()
        composeRule.onAllNodesWithText("Medusa").assertCountEquals(2)
        composeRule.onNodeWithText("Scorpius").assertDoesNotExist()
    }

    // 3. Owned filter & dynamic counter accuracy (not hardcoded)
    @Test
    fun `03 owned filter and dynamic owned counter calculate real unique owned items`() = runTest {
        val viewModel = LudoViewModel(application)
        val initialSnap = viewModel.inventoryRepository.ensureInitialized()
        viewModel.applyInventorySnapshot(initialSnap)
        val diceSnap = viewModel.diceStoreRepository.ensureInitialized()
        viewModel.applyDiceStoreSnapshot(diceSnap)

        val starterDiceCount = ItemCatalog.DICE_ITEMS.count { it.defaultOwned }
        assertEquals(starterDiceCount, viewModel.uiState.value.inventoryUiState.uniqueOwnedCountFor(InventoryCategory.DICE))

        // Buy 1 new dice skin -> counter increases dynamically
        val result = viewModel.purchaseInventoryItemSync("dice_wilder")
        assertTrue(result is PurchaseResult.Success)
        assertEquals(starterDiceCount + 1, viewModel.uiState.value.inventoryUiState.uniqueOwnedCountFor(InventoryCategory.DICE))

        val totalDiceCount = ItemCatalog.itemsForCategory(InventoryCategory.DICE).size

        composeRule.setContent {
            val state by viewModel.uiState.collectAsState()
            MyApplicationTheme {
                InventoryScreen(uiState = state, viewModel = viewModel, onBack = {})
            }
        }

        composeRule.onNodeWithTag("inventory_owned_counter")
            .assertTextContains("Owned: ${starterDiceCount + 1} / $totalDiceCount")

        composeRule.onNodeWithTag("inventory_filter_OWNED").performClick()
        composeRule.onNodeWithText("Wilder").assertExists()
        composeRule.onNodeWithText("Medusa").assertDoesNotExist()
    }

    // 4. Buying items deducts exact Coins or Cash balance
    @Test
    fun `04 buying items deducts exact coins or cash balance`() = runTest {
        val repo = InventoryRepository(application)
        repo.ensureInitialized(initialCoins = 25_000L, initialCash = 120)

        // Purchase coin dice: Medusa costs 4,500 Coins
        val (coinRes, snap1) = repo.purchaseItem(itemId = "dice_medusa")
        assertTrue(coinRes is PurchaseResult.Success)
        assertEquals(20_500L, snap1.currency.goldCoins)
        assertEquals(120, snap1.currency.cashNotes)

        // Purchase cash dice: Scorpius costs 35 Cash
        val (cashRes, snap2) = repo.purchaseItem(itemId = "dice_scorpius")
        assertTrue(cashRes is PurchaseResult.Success)
        assertEquals(20_500L, snap2.currency.goldCoins)
        assertEquals(85, snap2.currency.cashNotes)
    }

    // 5. Purchasing with insufficient balance fails cleanly with zero currency deduction
    @Test
    fun `05 insufficient balance purchase fails cleanly without deducting currency`() = runTest {
        val repo = InventoryRepository(application)
        repo.ensureInitialized(initialCoins = 500L, initialCash = 10)
        repo.syncWalletBalance(goldCoins = 500L, cashNotes = 10)

        // Captaino costs 8,500 Coins -> should fail
        val (coinFail, snap1) = repo.purchaseItem(itemId = "dice_captaino")
        assertTrue(coinFail is PurchaseResult.InsufficientBalance)
        assertEquals(500L, snap1.currency.goldCoins)
        assertFalse(snap1.isOwned("dice_captaino"))

        // Bounty frame costs 60 Cash -> should fail
        val (cashFail, snap2) = repo.purchaseItem(itemId = "frame_bounty")
        assertTrue(cashFail is PurchaseResult.InsufficientBalance)
        assertEquals(10, snap2.currency.cashNotes)
        assertFalse(snap2.isOwned("frame_bounty"))
    }

    // 6. Purchased item immediately appears in Owned section
    @Test
    fun `06 purchased item immediately appears in owned tab`() = runTest {
        val viewModel = LudoViewModel(application)
        viewModel.applyInventorySnapshot(viewModel.inventoryRepository.ensureInitialized())

        val res = viewModel.purchaseInventoryItemSync("frame_volt")
        assertTrue(res is PurchaseResult.Success)
        assertTrue(viewModel.uiState.value.inventoryUiState.isOwned("frame_volt"))

        composeRule.setContent {
            val state by viewModel.uiState.collectAsState()
            MyApplicationTheme {
                InventoryScreen(uiState = state, viewModel = viewModel, onBack = {})
            }
        }

        composeRule.onNodeWithTag("inventory_category_tabs_row")
            .performScrollToNode(hasTestTag("inventory_tab_OWNED"))
        composeRule.onNodeWithTag("inventory_tab_OWNED").performClick()
        composeRule.waitForIdle()
        assertEquals(
            "Expected OWNED category active, was ${viewModel.uiState.value.inventoryUiState.selectedCategory}, owned=${viewModel.uiState.value.inventoryUiState.ownedItemIds}",
            InventoryCategory.OWNED,
            viewModel.uiState.value.inventoryUiState.selectedCategory
        )
        composeRule.onNodeWithTag("inventory_items_grid")
            .performScrollToNode(hasTestTag("inventory_item_card_frame_volt"))
        composeRule.onNodeWithTag("inventory_item_card_frame_volt").assertExists()
    }

    // 7. Equipping items persists across screen navigation and reload
    @Test
    fun `07 equipped dice frame board and pawn persist across navigation`() = runTest {
        val viewModel = LudoViewModel(application)
        val repo = viewModel.inventoryRepository
        repo.ensureInitialized()
        repo.purchaseItem(itemId = "frame_campfire")
        val (_, snap) = repo.equipItem(itemId = "frame_campfire")
        viewModel.applyInventorySnapshot(snap)

        assertEquals("frame_campfire", viewModel.uiState.value.equippedFrameId)

        viewModel.navigateTo(AppScreen.HOME)
        viewModel.navigateTo(AppScreen.PROFILE)
        viewModel.navigateTo(AppScreen.GAME)
        viewModel.navigateTo(AppScreen.INVENTORY)

        assertEquals("frame_campfire", viewModel.uiState.value.equippedFrameId)
        assertEquals("frame_campfire", ProfilePreferences(application).getEquippedFrameId())
    }

    // 8. Equipped Dice skin applies to gameplay
    @Test
    fun `08 equipped dice skin applies to gameplay state`() = runTest {
        val viewModel = LudoViewModel(application)
        viewModel.inventoryRepository.ensureInitialized()
        val res = viewModel.purchaseInventoryItemSync("dice_mysticat")
        assertTrue(res is PurchaseResult.Success)

        viewModel.startNewGame(GameMode.VS_AI, playerCount = 2)
        assertEquals("dice_mysticat", viewModel.uiState.value.equippedDiceSkin)
    }

    // 9. Equipped Board theme applies to gameplay
    @Test
    fun `09 equipped board theme applies to gameplay state`() = runTest {
        val viewModel = LudoViewModel(application)
        viewModel.inventoryRepository.ensureInitialized()
        val res = viewModel.purchaseInventoryItemSync("board_campout")
        assertTrue(res is PurchaseResult.Success)

        viewModel.startNewGame(GameMode.VS_AI, playerCount = 2)
        assertEquals("board_campout", viewModel.uiState.value.equippedBoardTheme)
        assertEquals("Midnight Campout Trail", getBoardTheme(viewModel.uiState.value.equippedBoardTheme).name)
    }

    // 10. Equipped Pawn skin + Trail ON/OFF toggle applies and persists
    @Test
    fun `10 pawn skin and trail toggle persist and apply to gameplay`() = runTest {
        val viewModel = LudoViewModel(application)
        val repo = viewModel.inventoryRepository
        repo.ensureInitialized()
        val (buyRes, _) = repo.purchaseItem(itemId = "pawn_roasty")
        assertTrue(buyRes is PurchaseResult.Success)
        val (_, equipSnap) = repo.equipItem(itemId = "pawn_roasty")
        viewModel.applyInventorySnapshot(equipSnap)

        val trailOffSnap = repo.setPawnTrailEnabled(enabled = false)
        viewModel.applyInventorySnapshot(trailOffSnap)

        assertEquals("pawn_roasty", viewModel.uiState.value.equippedPawnSkin)
        assertFalse(viewModel.uiState.value.isPawnTrailEnabled)
        assertFalse(ProfilePreferences(application).isPawnTrailEnabled())

        val trailOnSnap = repo.setPawnTrailEnabled(enabled = true)
        viewModel.applyInventorySnapshot(trailOnSnap)
        assertTrue(viewModel.uiState.value.isPawnTrailEnabled)
    }

    // 11. Customizing the 18-slot Emoji Deck (add, replace, remove, swap)
    @Test
    fun `11 customizing 18-slot emoji deck add replace swap and remove works cleanly`() = runTest {
        val repo = InventoryRepository(application)
        repo.ensureInitialized()
        repo.purchaseItem(itemId = "emoji_charmed")
        repo.purchaseItem(itemId = "emoji_applause")

        // Assign to slot 2 and slot 5
        val (ok1, s1) = repo.assignEmojiToDeckSlot(slotIndex = 2, emojiItemId = "emoji_charmed")
        assertTrue(ok1)
        assertEquals("emoji_charmed", s1.emojiDeck.slots[2])

        val (ok2, s2) = repo.assignEmojiToDeckSlot(slotIndex = 5, emojiItemId = "emoji_applause")
        assertTrue(ok2)
        assertEquals("emoji_applause", s2.emojiDeck.slots[5])

        // Swap slots 2 and 5
        val s3 = repo.swapEmojiDeckSlots(fromSlot = 2, toSlot = 5)
        assertEquals("emoji_applause", s3.emojiDeck.slots[2])
        assertEquals("emoji_charmed", s3.emojiDeck.slots[5])

        // Attempting to assign an UNOWNED emoji must fail cleanly
        val (unownedAttempt, s4) = repo.assignEmojiToDeckSlot(slotIndex = 3, emojiItemId = "emoji_clasher")
        assertFalse(unownedAttempt)
        assertNull(s4.emojiDeck.slots[3])

        // Remove slot 2
        val s5 = repo.removeEmojiFromDeckSlot(slotIndex = 2)
        assertNull(s5.emojiDeck.slots[2])
    }

    // 12. Activating Powerups updates quantities, enforces cooldown/limits, and applies real effects
    @Test
    fun `12 activating powerups enforces cooldown limits and grants real effects`() = runTest {
        val viewModel = LudoViewModel(application)
        val repo = viewModel.inventoryRepository
        repo.ensureInitialized()

        // Buy 2 Extra Powerup consumables
        val r1 = viewModel.purchaseInventoryItemSync("powerup_extra_powerup")
        val r2 = viewModel.purchaseInventoryItemSync("powerup_extra_powerup")
        assertTrue(r1 is PurchaseResult.Success && r2 is PurchaseResult.Success)
        assertEquals(2, viewModel.uiState.value.inventoryUiState.getQuantity("powerup_extra_powerup"))

        val beforeLightning = viewModel.uiState.value.lightningCount
        val act1 = viewModel.activateInventoryPowerupSync("powerup_extra_powerup")
        assertTrue(act1 is PowerupActivationResult.Activated)
        assertEquals(1, viewModel.uiState.value.inventoryUiState.getQuantity("powerup_extra_powerup"))
        assertEquals(beforeLightning + 2, viewModel.uiState.value.lightningCount)

        // Immediate second activation hits cooldown cleanly without consuming remaining quantity
        val act2 = viewModel.activateInventoryPowerupSync("powerup_extra_powerup")
        assertTrue(act2 is PowerupActivationResult.OnCooldown)
        assertEquals(1, viewModel.uiState.value.inventoryUiState.getQuantity("powerup_extra_powerup"))
    }

    // 13. Verified Ad completion increments progress (0/50 -> 50/50 unlock) & deduplicates transactions
    @Test
    fun `13 verified ad increments progress unlocks at 50 and deduplicates repeat ids`() = runTest {
        val repo = InventoryRepository(application)
        repo.ensureInitialized(initialCoins = 1000L, initialCash = 10)

        // Verified ad #1
        val (res1, snap1) = repo.recordVerifiedAdCompletion(
            adTransactionId = "ad_tx_unique_1",
            verified = true,
            coinsReward = 500L,
            cashReward = 2
        )
        assertTrue(res1 is AdVerificationResult.Verified)
        assertEquals(1500L, snap1.currency.goldCoins)
        assertEquals(12, snap1.currency.cashNotes)
        assertEquals(1, snap1.eventProgress.first { it.eventOrItemId == "emoji_guy_crying" }.currentProgress)
        assertFalse(snap1.isOwned("emoji_guy_crying"))

        // Replay attack with exact same adTransactionId must be rejected cleanly
        val (dupRes, dupSnap) = repo.recordVerifiedAdCompletion(
            adTransactionId = "ad_tx_unique_1",
            verified = true,
            coinsReward = 500L,
            cashReward = 2
        )
        assertTrue(dupRes is AdVerificationResult.DuplicateTransaction)
        assertEquals(1500L, dupSnap.currency.goldCoins)
        assertEquals(1, dupSnap.eventProgress.first { it.eventOrItemId == "emoji_guy_crying" }.currentProgress)

        // Watch remaining 49 verified ads -> unlocks Guy Crying at 50/50
        var latest = dupSnap
        for (i in 2..50) {
            val (_, s) = repo.recordVerifiedAdCompletion(
                adTransactionId = "ad_tx_unique_$i",
                verified = true,
                coinsReward = 100L
            )
            latest = s
        }
        assertEquals(50, latest.eventProgress.first { it.eventOrItemId == "emoji_guy_crying" }.currentProgress)
        assertTrue(latest.isOwned("emoji_guy_crying"))
    }

    // 14. Skipped/failed Ads grant ZERO currency and ZERO progress
    @Test
    fun `14 skipped or failed ad grants zero currency and zero progress`() = runTest {
        val repo = InventoryRepository(application)
        val initial = repo.ensureInitialized(initialCoins = 5000L, initialCash = 20)
        val initialCoins = initial.currency.goldCoins
        val initialCash = initial.currency.cashNotes

        val (res, snap) = repo.recordVerifiedAdCompletion(
            adTransactionId = "ad_skipped_tx",
            verified = false,
            coinsReward = 1000L,
            cashReward = 10
        )
        assertTrue(res is AdVerificationResult.NotVerified)
        assertEquals(initialCoins, snap.currency.goldCoins)
        assertEquals(initialCash, snap.currency.cashNotes)
        assertEquals(0, snap.eventProgress.first { it.eventOrItemId == "emoji_guy_crying" }.currentProgress)
        assertFalse(snap.isOwned("emoji_guy_crying"))
    }

    // 15. Persistence after app restart (new LudoViewModel & InventoryRepository reload from Room)
    @Test
    fun `15 coins cash owned items equipped loadout and deck persist across restart`() = runTest {
        val repo1 = InventoryRepository(application)
        repo1.ensureInitialized(initialCoins = 30_000L, initialCash = 200)
        repo1.purchaseItem(itemId = "dice_medusa")
        repo1.equipItem(itemId = "dice_medusa")
        repo1.purchaseItem(itemId = "board_luxor")
        repo1.equipItem(itemId = "board_luxor")
        repo1.purchaseItem(itemId = "emoji_rage")
        repo1.assignEmojiToDeckSlot(slotIndex = 4, emojiItemId = "emoji_rage")

        // Simulate full app restart by instantiating fresh InventoryRepository & LudoViewModel
        val repo2 = InventoryRepository(application)
        val reloaded = repo2.getInventorySnapshot()
        assertEquals(24_000L, reloaded.currency.goldCoins)
        assertEquals(140, reloaded.currency.cashNotes)
        assertTrue(reloaded.isOwned("dice_medusa"))
        assertTrue(reloaded.isOwned("board_luxor"))
        assertTrue(reloaded.isOwned("emoji_rage"))
        assertEquals("dice_medusa", reloaded.equippedItems.diceId)
        assertEquals("board_luxor", reloaded.equippedItems.boardId)
        assertEquals("emoji_rage", reloaded.emojiDeck.slots[4])

        val restartedVm = LudoViewModel(application)
        restartedVm.applyInventorySnapshot(reloaded)
        assertEquals("dice_medusa", restartedVm.uiState.value.equippedDiceSkin)
        assertEquals("board_luxor", restartedVm.uiState.value.equippedBoardTheme)
        assertEquals(24_000L, restartedVm.uiState.value.userCoins)
        assertEquals(140, restartedVm.uiState.value.userGems)
    }

    // 16. Account logout / switch / re-login isolates and restores player inventory intact
    @Test
    fun `16 switching player accounts isolates and restores each inventory`() = runTest {
        val vm = LudoViewModel(application)
        val playerA = "player_alpha"
        val playerB = "player_beta"

        vm.switchPlayerAccountSync(playerA, defaultCoins = 15_000L, defaultCash = 50)
        vm.purchaseInventoryItemSync("dice_wilder") // 3,000 coins -> 12,000 remaining
        assertTrue(vm.uiState.value.inventoryUiState.isOwned("dice_wilder"))
        assertEquals(12_000L, vm.uiState.value.userCoins)

        // Switch to Player B -> must have fresh inventory without Player A's Wilder dice
        vm.switchPlayerAccountSync(playerB, defaultCoins = 40_000L, defaultCash = 90)
        assertFalse(vm.uiState.value.inventoryUiState.isOwned("dice_wilder"))
        assertEquals(40_000L, vm.uiState.value.userCoins)
        assertEquals(90, vm.uiState.value.userGems)

        // Log back into Player A -> Player A's Wilder dice & 12,000 coins return intact
        vm.switchPlayerAccountSync(playerA)
        assertTrue(vm.uiState.value.inventoryUiState.isOwned("dice_wilder"))
        assertEquals(12_000L, vm.uiState.value.userCoins)
    }

    // 17. Concurrency protection: simultaneous purchase requests cannot double-deduct or duplicate
    @Test
    fun `17 concurrent purchase calls are serialized by mutex without double deduction`() = runTest {
        val repo = InventoryRepository(application)
        repo.ensureInitialized(initialCoins = 5_000L, initialCash = 100)
        repo.syncWalletBalance(goldCoins = 5_000L, cashNotes = 100)

        // Launch 2 simultaneous purchases of the same 4,500-coin item (Medusa)
        val results = listOf(
            async { repo.purchaseItem(itemId = "dice_medusa") },
            async { repo.purchaseItem(itemId = "dice_medusa") }
        ).awaitAll()

        val successes = results.count { it.first is PurchaseResult.Success }
        val alreadyOwned = results.count { it.first is PurchaseResult.AlreadyOwned }

        assertEquals("Exactly 1 purchase must succeed", 1, successes)
        assertEquals("Second simultaneous purchase must return AlreadyOwned", 1, alreadyOwned)

        val finalSnap = repo.getInventorySnapshot()
        assertEquals("Balance must be deducted only once (5000 - 4500 = 500)", 500L, finalSnap.currency.goldCoins)
        assertTrue(finalSnap.currency.goldCoins >= 0L)
        assertEquals(1, repo.getPurchaseHistory().count { it.itemId == "dice_medusa" })
    }
}
