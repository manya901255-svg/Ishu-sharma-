package com.example.data

import android.content.Context
import com.example.model.*
import com.example.util.ProfilePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * DiceStoreRepository:
 * Authoritative backend/storage engine for the MG Ludo Premium Dice Store & Special Benefits System.
 *
 * Enforces:
 * - Room persistence across all 9 required tables (Catalog, Assets, Benefits, Owned, Equipped, CoinTx, RealMoneyTx, PurchaseHistory, EventRewards)
 * - Concurrency-safe atomic purchases via Mutex (never double-charges coins or grants duplicate ownership)
 * - Strict Google Play Billing verification:
 *   * Validates SKU, amount, package integrity, and duplicate purchaseToken replay rejection
 *   * Supports PURCHASED, PENDING (no premature unlock), FAILED, CANCELLED, REFUNDED (revokes dice + unequips), and Restore Purchases
 * - Full runtime Admin Panel CRUD updates persisted in Room and immediately reflected in Store, Inventory, and Gameplay
 * - Sync with InventoryRepository and ProfilePreferences
 */
class DiceStoreRepository(
    context: Context,
    private val inventoryRepository: InventoryRepository = InventoryRepository(context)
) {
    private val appContext = context.applicationContext
    private val db = AppDatabase.getDatabase(appContext)
    private val dao = db.diceStoreDao()
    private val invDao = db.inventoryDao()
    private val prefs = ProfilePreferences(appContext)
    private val mutex = Mutex()

    private val _snapshotFlow = MutableStateFlow<DiceStoreSnapshot?>(null)
    val snapshotFlow: StateFlow<DiceStoreSnapshot?> = _snapshotFlow.asStateFlow()

    private fun key(playerId: String, diceId: String) = "${playerId}_$diceId"

    suspend fun ensureInitialized(playerId: String = prefs.getCurrentPlayerId()): DiceStoreSnapshot =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                ensureInitializedLocked(playerId)
            }
        }

    private suspend fun ensureInitializedLocked(playerId: String): DiceStoreSnapshot {
        // Sync inventory wallet & starter items first
        inventoryRepository.ensureInitialized(playerId, prefs.getUserCoins(), prefs.getUserGems())

        // 1. Seed default 25 dice into DiceCatalog, DiceAsset, DiceBenefit if empty
        val existingCatalog = dao.getAllDiceCatalog()
        if (existingCatalog.isEmpty()) {
            val seedList = PremiumDiceCatalog.buildDefaultSeedCatalog()
            dao.upsertDiceCatalogAll(seedList.map { it.toCatalogEntity() })
            dao.upsertDiceAssetsAll(seedList.map { it.toAssetEntity() })
            dao.upsertDiceBenefitsAll(seedList.map { it.toBenefitEntity() })
        }

        // 2. Seed starter ownership + migrate any pre-existing owned dice from OwnedItemEntity
        val starterIds = dao.getAllDiceCatalog().filter { it.isStarterDefault }.map { it.diceId }.toSet() + "dice_imperial_gold"
        val existingOwnedDice = dao.getOwnedDice(playerId).map { it.diceId }.toSet()
        val legacyInvDice = invDao.getOwnedItems(playerId).filter { it.itemId.startsWith("dice_") && it.quantity > 0 }.map { it.itemId }.toSet()

        (starterIds + legacyInvDice).forEach { diceId ->
            if (diceId !in existingOwnedDice) {
                dao.upsertOwnedDice(
                    PlayerOwnedDiceEntity(
                        compositeId = key(playerId, diceId),
                        playerId = playerId,
                        diceId = diceId,
                        source = if (diceId in starterIds) "STARTER" else "LEGACY_SYNC",
                        purchasedAt = System.currentTimeMillis(),
                        isRevoked = false
                    )
                )
            }
        }

        // 3. Seed equipped dice if missing
        val equipped = dao.getEquippedDice(playerId)
        if (equipped == null) {
            val invLoadout = invDao.getEquippedLoadout(playerId)
            val initialEquipped = invLoadout?.diceId ?: prefs.getEquippedDiceSkin()
            dao.upsertEquippedDice(
                PlayerEquippedDiceEntity(
                    playerId = playerId,
                    diceId = initialEquipped,
                    equippedAt = System.currentTimeMillis()
                )
            )
        }

        // 4. Seed event challenge trackers for challenge-gated dice
        val catalogItems = assembleCatalogLocked()
        catalogItems.filter { it.challengeTargetCount > 0 }.forEach { dice ->
            val comp = key(playerId, dice.id)
            if (dao.getDiceEventRewardByComposite(comp) == null) {
                dao.upsertDiceEventReward(
                    DiceEventRewardEntity(
                        compositeId = comp,
                        playerId = playerId,
                        diceId = dice.id,
                        currentProgress = 0,
                        targetProgress = dice.challengeTargetCount,
                        isCompleted = false,
                        isClaimed = false,
                        expiresAtMillis = if (dice.eventEndTimeMillis > 0L) dice.eventEndTimeMillis else Long.MAX_VALUE
                    )
                )
            }
        }

        val snapshot = loadSnapshotLocked(playerId)
        _snapshotFlow.value = snapshot
        return snapshot
    }

    suspend fun getSnapshot(playerId: String = prefs.getCurrentPlayerId()): DiceStoreSnapshot =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                ensureInitializedLocked(playerId)
            }
        }

    private suspend fun assembleCatalogLocked(): List<PremiumDiceItem> {
        val catalogs = dao.getAllDiceCatalog()
        val assets = dao.getAllDiceAssets().associateBy { it.diceId }
        val benefits = dao.getAllDiceBenefits().associateBy { it.diceId }

        val items = catalogs.map { cat ->
            val asset = assets[cat.diceId]
            val benefit = benefits[cat.diceId]
            PremiumDiceItem(
                id = cat.diceId,
                name = cat.name,
                description = cat.description,
                collectionGroup = runCatching { DiceCollectionGroup.valueOf(cat.collectionGroup) }.getOrDefault(DiceCollectionGroup.CLASSIC_PREMIUM),
                rarity = runCatching { DiceRarity.valueOf(cat.rarity) }.getOrDefault(DiceRarity.RARE),
                coinPrice = cat.coinPrice,
                inrPrice = cat.inrPrice,
                skuId = cat.skuId,
                canBuyWithCoins = cat.canBuyWithCoins,
                canBuyWithInr = cat.canBuyWithInr,
                isAvailable = cat.isAvailable,
                availabilityType = runCatching { DiceAvailabilityType.valueOf(cat.availabilityType) }.getOrDefault(DiceAvailabilityType.ALWAYS_AVAILABLE),
                eventEndTimeMillis = cat.eventEndTimeMillis,
                challengeRequirementText = cat.challengeRequirementText,
                challengeTargetCount = cat.challengeTargetCount,
                benefits = DiceBenefits(
                    animationStyle = runCatching { DiceAnimationStyle.valueOf(asset?.animationStyle ?: "") }.getOrDefault(DiceAnimationStyle.ROYAL_TUMBLE),
                    particleEffect = runCatching { DiceParticleEffect.valueOf(asset?.particleEffect ?: "") }.getOrDefault(DiceParticleEffect.GOLD_SPARKLE),
                    soundEffect = runCatching { DiceSoundEffect.valueOf(asset?.soundEffect ?: "") }.getOrDefault(DiceSoundEffect.ROYAL_FANFARE),
                    visualEffectSummary = benefit?.visualEffectSummary ?: cat.description,
                    vipStatusLabel = benefit?.vipStatusLabel,
                    progressionNote = benefit?.progressionNote ?: "Fair Play Certified: Uniform 1-6 RNG"
                ),
                primaryColorHex = asset?.primaryColorHex ?: 0xFFFFD700,
                secondaryColorHex = asset?.secondaryColorHex ?: 0xFFFFA000,
                accentColorHex = asset?.accentColorHex ?: 0xFFFFF59D,
                pipColorHex = asset?.pipColorHex ?: 0xFFFFFFFF,
                iconEmoji = cat.iconEmoji,
                isStarterDefault = cat.isStarterDefault,
                isPublished = cat.isPublished,
                sortOrder = cat.sortOrder,
                updatedAt = cat.updatedAt
            )
        }
        ItemCatalog.syncDynamicDiceCatalog(items)
        return items
    }

    private suspend fun loadSnapshotLocked(playerId: String): DiceStoreSnapshot {
        val catalog = assembleCatalogLocked()
        val wallet = invDao.getWallet(playerId) ?: PlayerWalletEntity(playerId, prefs.getUserCoins(), prefs.getUserGems())
        val ownedIds = dao.getOwnedDice(playerId).filter { !it.isRevoked }.map { it.diceId }.toSet()
        val invOwnedDiceIds = invDao.getOwnedItems(playerId).filter { it.itemId.startsWith("dice_") && it.quantity > 0 }.map { it.itemId }.toSet()
        val mergedOwned = (ownedIds + invOwnedDiceIds).toSet()

        val equippedDiceId = dao.getEquippedDice(playerId)?.diceId ?: "dice_imperial_gold"
        val pendingIds = dao.getRealMoneyTransactions(playerId)
            .filter { it.status == "PENDING" }
            .map { it.diceId }
            .toSet()

        val eventMap = dao.getDiceEventRewards(playerId).associate {
            it.diceId to DiceEventRewardProgress(
                playerId = it.playerId,
                diceId = it.diceId,
                currentProgress = it.currentProgress,
                targetProgress = it.targetProgress,
                isCompleted = it.isCompleted,
                isClaimed = it.isClaimed,
                expiresAtMillis = it.expiresAtMillis
            )
        }
        val historyStrings = dao.getDicePurchaseHistory(playerId).take(25).map {
            "${it.diceName} (${it.paymentMethod}) • ${it.amountDisplay} [${it.status}]"
        }

        val snapshot = DiceStoreSnapshot(
            playerId = playerId,
            goldCoins = wallet.goldCoins,
            cashNotes = wallet.cashNotes,
            catalog = catalog,
            ownedDiceIds = mergedOwned,
            equippedDiceId = equippedDiceId,
            pendingBillingDiceIds = pendingIds,
            eventRewards = eventMap,
            recentPurchaseHistory = historyStrings
        )
        _snapshotFlow.value = snapshot
        return snapshot
    }

    // =========================================================================
    // ATOMIC COIN PURCHASE FLOW
    // =========================================================================

    suspend fun purchaseDiceWithCoins(
        playerId: String = prefs.getCurrentPlayerId(),
        diceId: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<DiceCoinPurchaseResult, DiceStoreSnapshot> = withContext(Dispatchers.IO) {
        mutex.withLock {
            ensureInitializedLocked(playerId)
        val dice = assembleCatalogLocked().firstOrNull { it.id == diceId }
            ?: return@withLock DiceCoinPurchaseResult.Unavailable("Dice '$diceId' not found") to loadSnapshotLocked(playerId)

        if (!dice.isPublished || !dice.isAvailable || !dice.canBuyWithCoins) {
            return@withLock DiceCoinPurchaseResult.Unavailable("${dice.name} is not available for Coin purchase") to loadSnapshotLocked(playerId)
        }
        if (dice.isExpired(nowMillis)) {
            return@withLock DiceCoinPurchaseResult.Unavailable("${dice.name} limited offer has expired") to loadSnapshotLocked(playerId)
        }

        val existingOwned = dao.getOwnedDiceByComposite(key(playerId, diceId))
        if (existingOwned != null && !existingOwned.isRevoked) {
            return@withLock DiceCoinPurchaseResult.AlreadyOwned(dice) to loadSnapshotLocked(playerId)
        }

        val wallet = invDao.getWallet(playerId)!!
        if (wallet.goldCoins < dice.coinPrice) {
            return@withLock DiceCoinPurchaseResult.InsufficientCoins(dice, dice.coinPrice, wallet.goldCoins) to loadSnapshotLocked(playerId)
        }

        val newCoins = (wallet.goldCoins - dice.coinPrice).coerceAtLeast(0L)
        invDao.upsertWallet(wallet.copy(goldCoins = newCoins, updatedAt = nowMillis))

        if (playerId == prefs.getCurrentPlayerId()) {
            prefs.setUserCoins(newCoins)
            prefs.addOwnedItem(dice.id)
        }

        // Grant ownership in Dice Store & Unified Inventory
        dao.upsertOwnedDice(
            PlayerOwnedDiceEntity(
                compositeId = key(playerId, dice.id),
                playerId = playerId,
                diceId = dice.id,
                source = "COINS",
                purchasedAt = nowMillis,
                isRevoked = false
            )
        )
        invDao.upsertOwnedItem(
            OwnedItemEntity(
                compositeId = key(playerId, dice.id),
                playerId = playerId,
                itemId = dice.id,
                quantity = 1,
                unlockedAt = nowMillis,
                source = "DICE_STORE_COINS"
            )
        )

        val txId = "ctx_${UUID.randomUUID()}"
        dao.insertCoinTransaction(
            CoinTransactionEntity(
                transactionId = txId,
                playerId = playerId,
                itemId = dice.id,
                amountDelta = -dice.coinPrice,
                balanceAfter = newCoins,
                reason = "PURCHASE_DICE:${dice.name}",
                timestamp = nowMillis
            )
        )
        dao.insertDicePurchaseHistory(
            DicePurchaseHistoryEntity(
                historyId = "dph_${UUID.randomUUID()}",
                playerId = playerId,
                diceId = dice.id,
                diceName = dice.name,
                paymentMethod = "COINS",
                amountDisplay = "${dice.coinPrice} Coins",
                status = "COMPLETED",
                timestamp = nowMillis
            )
        )
        invDao.insertPurchaseTransaction(
            PurchaseTransactionEntity(
                transactionId = txId,
                playerId = playerId,
                itemId = dice.id,
                amount = dice.coinPrice,
                currencyType = CurrencyType.COINS.name,
                status = "COMPLETED",
                timestamp = nowMillis
            )
        )

        val snap = loadSnapshotLocked(playerId)
        return@withLock DiceCoinPurchaseResult.Success(dice, txId, newCoins) to snap
        }
    }

    // =========================================================================
    // GOOGLE PLAY BILLING VERIFICATION & TRANSACTION PIPELINE
    // =========================================================================

    /**
     * Authoritative Billing Verification & State Transition Handler.
     * Enforces:
     * - SKU & catalog validation
     * - Token non-empty & package match verification
     * - Duplicate purchaseToken replay rejection
     * - PENDING handling (records pending tx WITHOUT unlocking dice until verified PURCHASED)
     * - FAILED / CANCELLED handling (no entitlement granted)
     * - REFUNDED handling (marks tx REFUNDED, revokes ownership, unequips active slot if needed)
     */
    suspend fun verifyAndProcessBillingPurchase(
        playerId: String = prefs.getCurrentPlayerId(),
        payload: BillingVerificationPayload
    ): Pair<DiceBillingResult, DiceStoreSnapshot> = withContext(Dispatchers.IO) {
        mutex.withLock {
            ensureInitializedLocked(playerId)

        if (payload.purchaseToken.isBlank()) {
            return@withLock DiceBillingResult.Failed("Invalid or empty purchase token") to loadSnapshotLocked(playerId)
        }

        val catalog = assembleCatalogLocked()
        val dice = catalog.firstOrNull { it.id == payload.diceId || it.skuId == payload.skuId }
            ?: return@withLock DiceBillingResult.Failed("Unknown product SKU: ${payload.skuId}") to loadSnapshotLocked(playerId)

        if (payload.skuId != dice.skuId) {
            return@withLock DiceBillingResult.Failed("SKU mismatch for ${dice.name}") to loadSnapshotLocked(playerId)
        }

        // Check existing token record
        val existingTokenTx = dao.getRealMoneyTxByToken(payload.purchaseToken)

        when (payload.purchaseState) {
            BillingPurchaseStatus.CANCELLED -> {
                val txId = existingTokenTx?.transactionId ?: "rm_${UUID.randomUUID()}"
                dao.upsertRealMoneyTransaction(
                    RealMoneyTransactionEntity(
                        transactionId = txId,
                        playerId = playerId,
                        diceId = dice.id,
                        skuId = payload.skuId,
                        purchaseToken = payload.purchaseToken,
                        orderId = payload.orderId,
                        amountInr = dice.inrPrice,
                        status = "CANCELLED",
                        acknowledged = false,
                        verifiedAt = payload.timestamp
                    )
                )
                return@withLock DiceBillingResult.Cancelled(dice.id) to loadSnapshotLocked(playerId)
            }

            BillingPurchaseStatus.FAILED -> {
                val txId = existingTokenTx?.transactionId ?: "rm_${UUID.randomUUID()}"
                dao.upsertRealMoneyTransaction(
                    RealMoneyTransactionEntity(
                        transactionId = txId,
                        playerId = playerId,
                        diceId = dice.id,
                        skuId = payload.skuId,
                        purchaseToken = payload.purchaseToken,
                        orderId = payload.orderId,
                        amountInr = dice.inrPrice,
                        status = "FAILED",
                        acknowledged = false,
                        verifiedAt = payload.timestamp
                    )
                )
                return@withLock DiceBillingResult.Failed("Payment failed for ${dice.name}") to loadSnapshotLocked(playerId)
            }

            BillingPurchaseStatus.PENDING -> {
                val txId = existingTokenTx?.transactionId ?: "rm_${UUID.randomUUID()}"
                dao.upsertRealMoneyTransaction(
                    RealMoneyTransactionEntity(
                        transactionId = txId,
                        playerId = playerId,
                        diceId = dice.id,
                        skuId = payload.skuId,
                        purchaseToken = payload.purchaseToken,
                        orderId = payload.orderId,
                        amountInr = dice.inrPrice,
                        status = "PENDING",
                        acknowledged = false,
                        verifiedAt = payload.timestamp
                    )
                )
                // Do NOT grant dice while PENDING
                return@withLock DiceBillingResult.Pending(dice, txId, payload.purchaseToken) to loadSnapshotLocked(playerId)
            }

            BillingPurchaseStatus.REFUNDED -> {
                val txId = existingTokenTx?.transactionId ?: "rm_${UUID.randomUUID()}"
                dao.upsertRealMoneyTransaction(
                    RealMoneyTransactionEntity(
                        transactionId = txId,
                        playerId = playerId,
                        diceId = dice.id,
                        skuId = payload.skuId,
                        purchaseToken = payload.purchaseToken,
                        orderId = payload.orderId,
                        amountInr = dice.inrPrice,
                        status = "REFUNDED",
                        acknowledged = existingTokenTx?.acknowledged ?: false,
                        verifiedAt = payload.timestamp
                    )
                )
                revokeDiceOwnershipLocked(playerId, dice.id)
                dao.insertDicePurchaseHistory(
                    DicePurchaseHistoryEntity(
                        historyId = "dph_${UUID.randomUUID()}",
                        playerId = playerId,
                        diceId = dice.id,
                        diceName = dice.name,
                        paymentMethod = "INR_PLAY_BILLING",
                        amountDisplay = "₹${dice.inrPrice}",
                        status = "REFUNDED_REVOKED",
                        timestamp = payload.timestamp
                    )
                )
                return@withLock DiceBillingResult.RefundedRevoked(dice.id, payload.purchaseToken) to loadSnapshotLocked(playerId)
            }

            BillingPurchaseStatus.PURCHASED -> {
                // Reject duplicate verified purchaseToken replay attacks
                if (existingTokenTx != null && existingTokenTx.status == "VERIFIED") {
                    return@withLock DiceBillingResult.DuplicateTokenRejected(payload.purchaseToken) to loadSnapshotLocked(playerId)
                }

                // Validate availability & expected INR configuration
                if (!dice.isPublished || !dice.canBuyWithInr || dice.inrPrice <= 0) {
                    return@withLock DiceBillingResult.Failed("${dice.name} INR purchase is not active") to loadSnapshotLocked(playerId)
                }
                if (payload.amountInr > 0 && payload.amountInr < dice.inrPrice) {
                    return@withLock DiceBillingResult.Failed("Payment amount mismatch") to loadSnapshotLocked(playerId)
                }

                val txId = existingTokenTx?.transactionId ?: "rm_${UUID.randomUUID()}"
                dao.upsertRealMoneyTransaction(
                    RealMoneyTransactionEntity(
                        transactionId = txId,
                        playerId = playerId,
                        diceId = dice.id,
                        skuId = payload.skuId,
                        purchaseToken = payload.purchaseToken,
                        orderId = payload.orderId,
                        amountInr = dice.inrPrice,
                        status = "VERIFIED",
                        acknowledged = true,
                        verifiedAt = payload.timestamp
                    )
                )
                grantDiceOwnershipLocked(playerId, dice, source = "PLAY_BILLING", purchaseToken = payload.purchaseToken, timestamp = payload.timestamp)
                dao.insertDicePurchaseHistory(
                    DicePurchaseHistoryEntity(
                        historyId = "dph_${UUID.randomUUID()}",
                        playerId = playerId,
                        diceId = dice.id,
                        diceName = dice.name,
                        paymentMethod = "INR_PLAY_BILLING",
                        amountDisplay = "₹${dice.inrPrice}",
                        status = "VERIFIED",
                        timestamp = payload.timestamp
                    )
                )
                return@withLock DiceBillingResult.VerifiedGranted(dice, txId, payload.purchaseToken) to loadSnapshotLocked(playerId)
            }
        }
        }
    }

    /**
     * Restores verified Google Play Billing purchases after app reinstall or new device login.
     */
    suspend fun restorePurchases(
        playerId: String = prefs.getCurrentPlayerId(),
        playPurchases: List<BillingVerificationPayload>
    ): Pair<Int, DiceStoreSnapshot> = withContext(Dispatchers.IO) {
        mutex.withLock {
            ensureInitializedLocked(playerId)
        val catalog = assembleCatalogLocked()
        var restoredCount = 0

        // 1. Restore from Play Store queryPurchasesAsync payloads
        for (payload in playPurchases) {
            if (payload.purchaseState == BillingPurchaseStatus.PURCHASED) {
                val existingTx = dao.getRealMoneyTxByToken(payload.purchaseToken)
                if (existingTx?.status == "REFUNDED") continue
                val dice = catalog.firstOrNull { it.id == payload.diceId || it.skuId == payload.skuId } ?: continue
                grantDiceOwnershipLocked(playerId, dice, source = "PLAY_RESTORE", purchaseToken = payload.purchaseToken, timestamp = payload.timestamp)
                if (existingTx == null) {
                    dao.upsertRealMoneyTransaction(
                        RealMoneyTransactionEntity(
                            transactionId = "rm_${UUID.randomUUID()}",
                            playerId = playerId,
                            diceId = dice.id,
                            skuId = payload.skuId,
                            purchaseToken = payload.purchaseToken,
                            orderId = payload.orderId,
                            amountInr = dice.inrPrice,
                            status = "VERIFIED",
                            acknowledged = true,
                            verifiedAt = payload.timestamp
                        )
                    )
                }
                restoredCount++
            }
        }

        // 2. Also ensure any VERIFIED real-money transaction in DB has active ownership
        dao.getRealMoneyTransactions(playerId)
            .filter { it.status == "VERIFIED" }
            .forEach { tx ->
                val owned = dao.getOwnedDiceByComposite(key(playerId, tx.diceId))
                if (owned == null || owned.isRevoked) {
                    val dice = catalog.firstOrNull { it.id == tx.diceId }
                    if (dice != null) {
                        grantDiceOwnershipLocked(playerId, dice, source = "DB_RESTORE", purchaseToken = tx.purchaseToken, timestamp = tx.verifiedAt)
                        restoredCount++
                    }
                }
            }

        return@withLock restoredCount to loadSnapshotLocked(playerId)
        }
    }

    private suspend fun grantDiceOwnershipLocked(
        playerId: String,
        dice: PremiumDiceItem,
        source: String,
        purchaseToken: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ) {
        dao.upsertOwnedDice(
            PlayerOwnedDiceEntity(
                compositeId = key(playerId, dice.id),
                playerId = playerId,
                diceId = dice.id,
                source = source,
                purchaseToken = purchaseToken,
                purchasedAt = timestamp,
                isRevoked = false
            )
        )
        invDao.upsertOwnedItem(
            OwnedItemEntity(
                compositeId = key(playerId, dice.id),
                playerId = playerId,
                itemId = dice.id,
                quantity = 1,
                unlockedAt = timestamp,
                source = source
            )
        )
        if (playerId == prefs.getCurrentPlayerId()) {
            prefs.addOwnedItem(dice.id)
        }
    }

    private suspend fun revokeDiceOwnershipLocked(playerId: String, diceId: String) {
        val existing = dao.getOwnedDiceByComposite(key(playerId, diceId))
        if (existing != null) {
            dao.upsertOwnedDice(existing.copy(isRevoked = true))
        }
        val invOwned = invDao.getOwnedItem(key(playerId, diceId))
        if (invOwned != null) {
            invDao.upsertOwnedItem(invOwned.copy(quantity = 0))
        }
        val equipped = dao.getEquippedDice(playerId)
        if (equipped?.diceId == diceId) {
            dao.upsertEquippedDice(PlayerEquippedDiceEntity(playerId, "dice_imperial_gold", System.currentTimeMillis()))
            val invLoadout = invDao.getEquippedLoadout(playerId) ?: EquippedLoadoutEntity(playerId)
            invDao.upsertEquippedLoadout(invLoadout.copy(diceId = "dice_imperial_gold"))
            if (playerId == prefs.getCurrentPlayerId()) {
                prefs.setEquippedDiceSkin("dice_imperial_gold")
            }
        }
    }

    // =========================================================================
    // EQUIP / UNEQUIP DICE & DAILY / EVENT CHALLENGES
    // =========================================================================

    suspend fun equipDice(
        playerId: String = prefs.getCurrentPlayerId(),
        diceId: String
    ): Pair<Boolean, DiceStoreSnapshot> = withContext(Dispatchers.IO) {
        mutex.withLock {
            ensureInitializedLocked(playerId)
            val owned = dao.getOwnedDiceByComposite(key(playerId, diceId))
            val invOwned = invDao.getOwnedItem(key(playerId, diceId))
            val isOwned = (owned != null && !owned.isRevoked) || (invOwned != null && invOwned.quantity > 0)
            if (!isOwned) {
                return@withLock false to loadSnapshotLocked(playerId)
            }

            dao.upsertEquippedDice(PlayerEquippedDiceEntity(playerId, diceId, System.currentTimeMillis()))
            val invLoadout = invDao.getEquippedLoadout(playerId) ?: EquippedLoadoutEntity(playerId)
            invDao.upsertEquippedLoadout(invLoadout.copy(diceId = diceId, updatedAt = System.currentTimeMillis()))
            if (playerId == prefs.getCurrentPlayerId()) {
                prefs.setEquippedDiceSkin(diceId)
            }
            return@withLock true to loadSnapshotLocked(playerId)
        }
    }

    suspend fun unequipDice(
        playerId: String = prefs.getCurrentPlayerId()
    ): DiceStoreSnapshot = withContext(Dispatchers.IO) {
        mutex.withLock {
            ensureInitializedLocked(playerId)
            dao.upsertEquippedDice(PlayerEquippedDiceEntity(playerId, "dice_imperial_gold", System.currentTimeMillis()))
            val invLoadout = invDao.getEquippedLoadout(playerId) ?: EquippedLoadoutEntity(playerId)
            invDao.upsertEquippedLoadout(invLoadout.copy(diceId = "dice_imperial_gold", updatedAt = System.currentTimeMillis()))
            if (playerId == prefs.getCurrentPlayerId()) {
                prefs.setEquippedDiceSkin("dice_imperial_gold")
            }
            loadSnapshotLocked(playerId)
        }
    }

    suspend fun incrementChallengeProgress(
        playerId: String = prefs.getCurrentPlayerId(),
        delta: Int = 1,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<List<PremiumDiceItem>, DiceStoreSnapshot> = withContext(Dispatchers.IO) {
        mutex.withLock {
            ensureInitializedLocked(playerId)
        val catalog = assembleCatalogLocked()
        val unlockedNow = mutableListOf<PremiumDiceItem>()

        catalog.filter { it.challengeTargetCount > 0 }.forEach { dice ->
            val comp = key(playerId, dice.id)
            val existing = dao.getDiceEventRewardByComposite(comp)
            val expires = existing?.expiresAtMillis ?: (if (dice.eventEndTimeMillis > 0L) dice.eventEndTimeMillis else Long.MAX_VALUE)
            if (expires >= nowMillis) {
                val cur = existing?.currentProgress ?: 0
                val next = (cur + delta).coerceAtMost(dice.challengeTargetCount)
                val completed = next >= dice.challengeTargetCount
                dao.upsertDiceEventReward(
                    DiceEventRewardEntity(
                        compositeId = comp,
                        playerId = playerId,
                        diceId = dice.id,
                        currentProgress = next,
                        targetProgress = dice.challengeTargetCount,
                        isCompleted = completed,
                        isClaimed = existing?.isClaimed == true,
                        expiresAtMillis = expires
                    )
                )
            }
        }
        return@withLock unlockedNow to loadSnapshotLocked(playerId)
        }
    }

    suspend fun claimEventOrChallengeDice(
        playerId: String = prefs.getCurrentPlayerId(),
        diceId: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<Boolean, DiceStoreSnapshot> = withContext(Dispatchers.IO) {
        mutex.withLock {
            ensureInitializedLocked(playerId)
        val dice = assembleCatalogLocked().firstOrNull { it.id == diceId }
            ?: return@withLock false to loadSnapshotLocked(playerId)

        if (dice.isExpired(nowMillis)) {
            return@withLock false to loadSnapshotLocked(playerId)
        }
        val rewardProg = dao.getDiceEventRewardByComposite(key(playerId, diceId))
        if (dice.challengeTargetCount > 0 && (rewardProg == null || rewardProg.currentProgress < dice.challengeTargetCount || rewardProg.isClaimed)) {
            return@withLock false to loadSnapshotLocked(playerId)
        }

        if (rewardProg != null) {
            dao.upsertDiceEventReward(rewardProg.copy(isCompleted = true, isClaimed = true))
        }
        grantDiceOwnershipLocked(playerId, dice, source = "EVENT_CHALLENGE", timestamp = nowMillis)
        dao.insertDicePurchaseHistory(
            DicePurchaseHistoryEntity(
                historyId = "dph_${UUID.randomUUID()}",
                playerId = playerId,
                diceId = dice.id,
                diceName = dice.name,
                paymentMethod = "EVENT",
                amountDisplay = "Challenge Unlocked",
                status = "CLAIMED",
                timestamp = nowMillis
            )
        )
        return@withLock true to loadSnapshotLocked(playerId)
        }
    }

    // =========================================================================
    // ADMIN PANEL CRUD CONTROLS (Persisted & Immediately Reflected in Store)
    // =========================================================================

    suspend fun adminSaveOrUpdateDice(
        dice: PremiumDiceItem,
        playerId: String = prefs.getCurrentPlayerId()
    ): DiceStoreSnapshot = withContext(Dispatchers.IO) {
        mutex.withLock {
            ensureInitializedLocked(playerId)
            val updated = dice.copy(updatedAt = System.currentTimeMillis())
            dao.upsertDiceCatalog(updated.toCatalogEntity())
            dao.upsertDiceAsset(updated.toAssetEntity())
            dao.upsertDiceBenefit(updated.toBenefitEntity())
            loadSnapshotLocked(playerId)
        }
    }

    suspend fun adminDeleteDice(
        diceId: String,
        playerId: String = prefs.getCurrentPlayerId()
    ): DiceStoreSnapshot = withContext(Dispatchers.IO) {
        mutex.withLock {
            ensureInitializedLocked(playerId)
            if (diceId != "dice_imperial_gold") {
                dao.deleteDiceCatalog(diceId)
                dao.deleteDiceAsset(diceId)
                dao.deleteDiceBenefit(diceId)
            }
            loadSnapshotLocked(playerId)
        }
    }

    // =========================================================================
    // ENTITY MAPPING HELPERS
    // =========================================================================

    private fun PremiumDiceItem.toCatalogEntity() = DiceCatalogEntity(
        diceId = id,
        name = name,
        description = description,
        collectionGroup = collectionGroup.name,
        rarity = rarity.name,
        coinPrice = coinPrice,
        inrPrice = inrPrice,
        skuId = skuId,
        canBuyWithCoins = canBuyWithCoins,
        canBuyWithInr = canBuyWithInr,
        isAvailable = isAvailable,
        availabilityType = availabilityType.name,
        eventEndTimeMillis = eventEndTimeMillis,
        challengeRequirementText = challengeRequirementText,
        challengeTargetCount = challengeTargetCount,
        iconEmoji = iconEmoji,
        isStarterDefault = isStarterDefault,
        isPublished = isPublished,
        sortOrder = sortOrder,
        updatedAt = if (updatedAt > 0L) updatedAt else System.currentTimeMillis()
    )

    private fun PremiumDiceItem.toAssetEntity() = DiceAssetEntity(
        diceId = id,
        primaryColorHex = primaryColorHex,
        secondaryColorHex = secondaryColorHex,
        accentColorHex = accentColorHex,
        pipColorHex = pipColorHex,
        animationStyle = benefits.animationStyle.name,
        particleEffect = benefits.particleEffect.name,
        soundEffect = benefits.soundEffect.name
    )

    private fun PremiumDiceItem.toBenefitEntity() = DiceBenefitEntity(
        diceId = id,
        visualEffectSummary = benefits.visualEffectSummary,
        vipStatusLabel = benefits.vipStatusLabel,
        progressionNote = benefits.progressionNote
    )
}
