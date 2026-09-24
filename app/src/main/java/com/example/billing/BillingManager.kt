package com.example.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        private const val PREFS_NAME = "cryptocycles_billing_prefs"
        private const val KEY_IS_PRO = "key_is_pro_active"

        const val PRODUCT_PRO_MONTHLY = "pro_monthly"
        const val PRODUCT_PRO_YEARLY = "pro_yearly"

        const val DEFAULT_MONTHLY_PRICE = "€2.99"
        const val DEFAULT_YEARLY_PRICE = "€29.99"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isProUnlocked = MutableStateFlow(prefs.getBoolean(KEY_IS_PRO, false))
    val isProUnlocked: StateFlow<Boolean> = _isProUnlocked.asStateFlow()

    private val _monthlyPrice = MutableStateFlow(DEFAULT_MONTHLY_PRICE)
    val monthlyPrice: StateFlow<String> = _monthlyPrice.asStateFlow()

    private val _yearlyPrice = MutableStateFlow(DEFAULT_YEARLY_PRICE)
    val yearlyPrice: StateFlow<String> = _yearlyPrice.asStateFlow()

    private val _monthlyProductDetails = MutableStateFlow<ProductDetails?>(null)
    private val _yearlyProductDetails = MutableStateFlow<ProductDetails?>(null)

    private val _billingStatusMessage = MutableStateFlow<String?>(null)
    val billingStatusMessage: StateFlow<String?> = _billingStatusMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    @Volatile
    private var isConnecting = false

    private val billingClient: BillingClient? = try {
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .build()
    } catch (t: Throwable) {
        Log.e(TAG, "Billing client unavailable on this device/emulator", t)
        null
    }

    init {
        startConnection()
    }

    fun startConnection(onConnected: (() -> Unit)? = null) {
        val client = billingClient ?: return
        if (client.isReady) {
            queryProductDetails()
            queryActivePurchases()
            onConnected?.invoke()
            return
        }

        if (isConnecting) {
            Log.d(TAG, "BillingClient already connecting, ignoring duplicate startConnection request.")
            return
        }

        isConnecting = true
        try {
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    isConnecting = false
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Billing client setup successfully.")
                        queryProductDetails()
                        queryActivePurchases()
                        onConnected?.invoke()
                    } else {
                        Log.w(TAG, "Billing setup failed with response code: ${billingResult.responseCode} - ${billingResult.debugMessage}")
                    }
                }

                override fun onBillingServiceDisconnected() {
                    isConnecting = false
                    Log.w(TAG, "Billing service disconnected. Will retry on next user request.")
                }
            })
        } catch (e: Exception) {
            isConnecting = false
            Log.e(TAG, "Failed to initiate billingClient.startConnection", e)
        }
    }

    fun queryProductDetails() {
        val client = billingClient ?: return
        if (!client.isReady) {
            startConnection { queryProductDetails() }
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_PRO_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_PRO_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        try {
            client.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
                val productDetailsList = productDetailsResult?.productDetailsList
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                    Log.d(TAG, "Products queried successfully: ${productDetailsList.size} found")
                    for (product in productDetailsList) {
                        val offer = product.subscriptionOfferDetails?.firstOrNull()
                        val pricingPhase = offer?.pricingPhases?.pricingPhaseList?.lastOrNull()
                        val formattedPrice = pricingPhase?.formattedPrice

                        if (product.productId == PRODUCT_PRO_MONTHLY) {
                            _monthlyProductDetails.value = product
                            if (!formattedPrice.isNullOrBlank()) {
                                _monthlyPrice.value = formattedPrice
                            }
                        } else if (product.productId == PRODUCT_PRO_YEARLY) {
                            _yearlyProductDetails.value = product
                            if (!formattedPrice.isNullOrBlank()) {
                                _yearlyPrice.value = formattedPrice
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "QueryProductDetails failed: ${billingResult.responseCode} - ${billingResult.debugMessage}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in queryProductDetailsAsync", e)
        }
    }

    fun queryActivePurchases(onFinished: ((Boolean) -> Unit)? = null) {
        val client = billingClient ?: run {
            onFinished?.invoke(_isProUnlocked.value)
            return
        }
        if (!client.isReady) {
            startConnection { queryActivePurchases(onFinished) }
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        try {
            client.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    var hasActivePro = false
                    for (purchase in purchases) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            if (purchase.products.contains(PRODUCT_PRO_MONTHLY) || purchase.products.contains(PRODUCT_PRO_YEARLY)) {
                                hasActivePro = true
                                handlePurchase(purchase)
                            }
                        }
                    }

                    if (hasActivePro) {
                        updateProState(true)
                    }

                    onFinished?.invoke(hasActivePro)
                } else {
                    Log.w(TAG, "QueryPurchases failed: ${billingResult.responseCode} - ${billingResult.debugMessage}")
                    onFinished?.invoke(_isProUnlocked.value)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in queryPurchasesAsync", e)
            onFinished?.invoke(_isProUnlocked.value)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User canceled billing purchase flow.")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "Item already owned. Synchronizing active purchase.")
                queryActivePurchases()
                updateProState(true)
            }
            else -> {
                Log.w(TAG, "onPurchasesUpdated error: ${billingResult.responseCode} - ${billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            updateProState(true)
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged successfully.")
                    } else {
                        Log.w(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
                    }
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "Purchase is pending.")
        }
    }

    fun launchPurchaseFlow(
        activity: Activity,
        productId: String,
        onLaunched: ((Boolean, String?) -> Unit)? = null
    ) {
        val client = billingClient ?: run {
            onLaunched?.invoke(false, "Google Play Billing is not available on this device.")
            return
        }
        if (!client.isReady) {
            startConnection {
                launchPurchaseFlow(activity, productId, onLaunched)
            }
            return
        }

        val productDetails = if (productId == PRODUCT_PRO_MONTHLY) {
            _monthlyProductDetails.value
        } else {
            _yearlyProductDetails.value
        }

        if (productDetails == null) {
            // Re-query and launch or provide informative feedback
            queryProductDetails()
            onLaunched?.invoke(false, "Connecting to Google Play Store...")
            return
        }

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            onLaunched?.invoke(false, "Subscription offer token not available.")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val responseCode = client.launchBillingFlow(activity, billingFlowParams).responseCode
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            onLaunched?.invoke(true, null)
        } else {
            onLaunched?.invoke(false, "Billing flow error code: $responseCode")
        }
    }

    fun restorePurchases(onResult: (isSuccess: Boolean, message: String) -> Unit) {
        _isLoading.value = true
        queryActivePurchases { hasActivePro ->
            _isLoading.value = false
            if (hasActivePro) {
                onResult(true, "Pro subscription active & restored!")
            } else {
                onResult(false, "No active subscription found on this Google account.")
            }
        }
    }

    fun updateProState(unlocked: Boolean) {
        _isProUnlocked.value = unlocked
        prefs.edit().putBoolean(KEY_IS_PRO, unlocked).apply()
    }
}
