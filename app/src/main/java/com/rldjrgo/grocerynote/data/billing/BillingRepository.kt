package com.rldjrgo.grocerynote.data.billing

import android.app.Activity
import android.content.Context
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
import com.rldjrgo.grocerynote.data.local.SettingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

const val SKU_REMOVE_ADS = "remove_ads"

@Singleton
class BillingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsDataStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val purchaseListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch { purchases.forEach { handlePurchase(it) } }
        }
    }

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchaseListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    suspend fun start() {
        if (client.isReady) return
        suspendCancellableCoroutine<Unit> { cont ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (cont.isActive) cont.resume(Unit)
                }
                override fun onBillingServiceDisconnected() { /* swallowed */ }
            })
        }
        // Restore: in case user paid on another install.
        runCatching { restorePurchases() }
    }

    suspend fun productDetails(): ProductDetails? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SKU_REMOVE_ADS)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            ).build()
        return suspendCancellableCoroutine { cont ->
            client.queryProductDetailsAsync(params) { _, queryResult ->
                if (cont.isActive) {
                    val list = queryResult.productDetailsList
                    cont.resume(list.firstOrNull())
                }
            }
        }
    }

    suspend fun launchPurchase(activity: Activity) {
        val pd = productDetails() ?: return
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(pd)
                        .build()
                )
            ).build()
        client.launchBillingFlow(activity, params)
    }

    private suspend fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val purchases = suspendCancellableCoroutine<List<Purchase>> { cont ->
            client.queryPurchasesAsync(params) { _, list ->
                if (cont.isActive) cont.resume(list)
            }
        }
        purchases.forEach { handlePurchase(it) }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (!purchase.products.contains(SKU_REMOVE_ADS)) return
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        settings.setAdRemoved(true)
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            suspendCancellableCoroutine<BillingResult> { cont ->
                client.acknowledgePurchase(params) { result ->
                    if (cont.isActive) cont.resume(result)
                }
            }
        }
    }
}
