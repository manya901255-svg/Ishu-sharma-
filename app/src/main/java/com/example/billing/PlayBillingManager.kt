package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.model.BillingPurchaseStatus
import com.example.model.BillingVerificationPayload
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

sealed class BillingClientEvent {
    data class ProductDetailsLoaded(val skuCount: Int) : BillingClientEvent()
    data class PurchaseUpdated(val payloads: List<BillingVerificationPayload>) : BillingClientEvent()
    data class UserCancelled(val message: String = "Purchase cancelled by user") : BillingClientEvent()
    data class BillingError(val responseCode: Int, val debugMessage: String) : BillingClientEvent()
    data class RestoredPurchases(val payloads: List<BillingVerificationPayload>) : BillingClientEvent()
    data class ServiceUnavailable(val reason: String) : BillingClientEvent()
}

/**
 * PlayBillingManager:
 * Real Google Play Billing Library (9.1.0) client controller for MG Ludo Premium Dice Store.
 * Supports:
 * - Play Store connection & automatic reconnection
 * - ProductDetails querying for all catalog SKUs
 * - Real-time BillingFlow launch from an active Android Activity
 * - PurchasesUpdatedListener handling PURCHASED, PENDING, USER_CANCELED, and error codes
 * - Server-side acknowledgement after verification
 * - Active purchases restoration (queryPurchasesAsync)
 */
class PlayBillingManager(
    context: Context,
    private val skuToDiceIdResolver: (String) -> String?
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "PlayBillingManager"
    }

    private val appContext = context.applicationContext
    private val productDetailsCache = ConcurrentHashMap<String, ProductDetails>()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _events = MutableSharedFlow<BillingClientEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<BillingClientEvent> = _events.asSharedFlow()

    private var billingClient: BillingClient? = null
    private var lastRequestedSku: String? = null
    private var lastRequestedDiceId: String? = null
    private var lastRequestedInrAmount: Int = 0

    fun initialize(skusToQuery: List<String> = emptyList()) {
        if (billingClient != null) {
            if (_isConnected.value && skusToQuery.isNotEmpty()) {
                queryProductDetails(skusToQuery)
            }
            return
        }
        try {
            val client = BillingClient.newBuilder(appContext)
                .setListener(this)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build()
                )
                .enableAutoServiceReconnection()
                .build()
            billingClient = client
            startConnection(skusToQuery)
        } catch (t: Throwable) {
            Log.w(TAG, "BillingClient unavailable in this environment: ${t.message}")
            _isConnected.value = false
        }
    }

    private fun startConnection(skusToQuery: List<String>) {
        val client = billingClient ?: return
        try {
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _isConnected.value = true
                        if (skusToQuery.isNotEmpty()) {
                            queryProductDetails(skusToQuery)
                        }
                        queryExistingPurchases()
                    } else {
                        _isConnected.value = false
                        _events.tryEmit(
                            BillingClientEvent.ServiceUnavailable(
                                "Google Play Billing unavailable (${billingResult.debugMessage.ifBlank { "code ${billingResult.responseCode}" }})"
                            )
                        )
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _isConnected.value = false
                }
            })
        } catch (t: Throwable) {
            Log.w(TAG, "startConnection exception: ${t.message}")
            _isConnected.value = false
        }
    }

    fun queryProductDetails(skuIds: List<String>) {
        val client = billingClient ?: return
        if (!_isConnected.value || skuIds.isEmpty()) return

        val productList = skuIds.distinct().map { sku ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, queryResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                queryResult.productDetailsList.forEach { details ->
                    productDetailsCache[details.productId] = details
                }
                _events.tryEmit(BillingClientEvent.ProductDetailsLoaded(queryResult.productDetailsList.size))
            }
        }
    }

    fun getLocalizedPriceOrFallback(skuId: String, fallbackInr: Int): String {
        val details = productDetailsCache[skuId]
        val formatted = details?.oneTimePurchaseOfferDetails?.formattedPrice
        return if (!formatted.isNullOrBlank()) formatted else "₹$fallbackInr"
    }

    /**
     * Launches the genuine Google Play purchase sheet for the given SKU.
     * Never grants entitlement client-side before PurchasesUpdatedListener fires & backend verification passes.
     */
    fun launchDicePurchaseFlow(
        activity: Activity?,
        skuId: String,
        diceId: String,
        expectedInrAmount: Int,
        obfuscatedPlayerId: String
    ): Boolean {
        lastRequestedSku = skuId
        lastRequestedDiceId = diceId
        lastRequestedInrAmount = expectedInrAmount

        val client = billingClient
        if (activity == null || client == null || !_isConnected.value) {
            _events.tryEmit(
                BillingClientEvent.ServiceUnavailable(
                    "Google Play Store billing service is not connected on this device/emulator."
                )
            )
            return false
        }

        val details = productDetailsCache[skuId]
        if (details == null) {
            queryProductDetails(listOf(skuId))
            _events.tryEmit(
                BillingClientEvent.BillingError(
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                    "Product SKU '$skuId' is not yet configured or loaded from Play Console."
                )
            )
            return false
        }

        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .setObfuscatedAccountId(obfuscatedPlayerId)
            .build()

        val result = client.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _events.tryEmit(BillingClientEvent.BillingError(result.responseCode, result.debugMessage))
            return false
        }
        return true
    }

    fun queryExistingPurchases() {
        val client = billingClient ?: return
        if (!_isConnected.value) return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val payloads = purchases.mapNotNull { mapPurchaseToPayload(it) }
                if (payloads.isNotEmpty()) {
                    _events.tryEmit(BillingClientEvent.RestoredPurchases(payloads))
                }
            }
        }
    }

    fun acknowledgePurchase(purchaseToken: String) {
        val client = billingClient ?: return
        if (!_isConnected.value || purchaseToken.isBlank()) return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        client.acknowledgePurchase(params) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "Acknowledge purchase warning: ${result.debugMessage}")
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val payloads = purchases?.mapNotNull { mapPurchaseToPayload(it) } ?: emptyList()
                if (payloads.isNotEmpty()) {
                    _events.tryEmit(BillingClientEvent.PurchaseUpdated(payloads))
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _events.tryEmit(BillingClientEvent.UserCancelled())
            }
            else -> {
                _events.tryEmit(BillingClientEvent.BillingError(billingResult.responseCode, billingResult.debugMessage))
            }
        }
    }

    private fun mapPurchaseToPayload(purchase: Purchase): BillingVerificationPayload? {
        val sku = purchase.products.firstOrNull() ?: return null
        val diceId = skuToDiceIdResolver(sku) ?: lastRequestedDiceId ?: return null
        val status = when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> BillingPurchaseStatus.PURCHASED
            Purchase.PurchaseState.PENDING -> BillingPurchaseStatus.PENDING
            else -> BillingPurchaseStatus.FAILED
        }
        return BillingVerificationPayload(
            skuId = sku,
            diceId = diceId,
            purchaseToken = purchase.purchaseToken,
            orderId = purchase.orderId ?: "GP-${purchase.purchaseTime}",
            packageName = purchase.packageName.ifBlank { appContext.packageName },
            amountInr = lastRequestedInrAmount,
            purchaseState = status,
            timestamp = purchase.purchaseTime
        )
    }

    fun endConnection() {
        try {
            billingClient?.endConnection()
        } catch (_: Exception) {}
        billingClient = null
        _isConnected.value = false
    }
}
