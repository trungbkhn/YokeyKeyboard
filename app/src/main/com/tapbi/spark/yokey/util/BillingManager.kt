//package com.keyboard.zomj.util
//
//import android.app.Activity
//import android.content.SharedPreferences
//import android.preference.PreferenceManager
//import com.android.billingclient.api.*
//import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
//import com.android.billingclient.api.QueryProductDetailsParams.Product
//import com.keyboard.zomj.R
//import com.google.gson.Gson
//import com.keyboard.zomj.App
//import com.keyboard.zomj.common.Constant
//import timber.log.Timber
//import java.util.*
//
//class BillingManager {
//    private var onBillingListener: OnBillingListener? = null
//    private var billingClient: BillingClient? = null
//    private var instance: BillingManager? = null
//    private var skuPro = ""
//    private var skuPermanently = ""
//    var mHistoryRecords: MutableList<PurchaseHistoryRecord> = ArrayList<PurchaseHistoryRecord>()
//    private var knownSubscriptionSKUs: List<String>? = null
//    var mPrefs: SharedPreferences? = null
//    var mActivity: Activity? = null
//    private var isPro = false
//    private val isConnected = false
//    private var isConnecting = false
//    private val mMapPurChased = HashMap<String, Boolean>()
//    private val mMapHistoryResult = HashMap<String, Boolean>()
//    private var lastHistoryRecord: PurchaseHistoryRecord? = null
//    private val mMapPurChaseQuery = HashMap<String, Boolean>()
//
//    constructor(mPrefs: SharedPreferences?){
//        this.mPrefs = mPrefs
//        instance = this
//    }
//
//    fun isPro(): Boolean {
//        return isPro
//    }
//
//    fun isConnected(): Boolean {
//        return isConnected
//    }
//
//    fun getInstance(): BillingManager? {
//        if (instance == null) {
//            instance =
//                BillingManager(PreferenceManager.getDefaultSharedPreferences(App.instance))
//        }
//
//        return instance
//    }
//
//
//    fun initBilling(activity: Activity, onBillingListener: OnBillingListener?) {
//        Timber.e("initBilling ")
//        isPro = mPrefs!!.getBoolean(Constant.IS_REMOVE_ADS, false)
//        skuPro = activity.resources.getString(R.string.product_remove_ads_id)
//        skuPermanently = activity.resources.getString(R.string.purchase_pro_id_permanently)
//        val skuSubs = activity.resources.getStringArray(R.array.subscription_list)
//        knownSubscriptionSKUs = listOf(*skuSubs)
//        this.onBillingListener = onBillingListener
//        billingClient = BillingClient.newBuilder(activity)
//            .setListener(purchasesUpdatedListener)
//            .enablePendingPurchases()
//            .build()
//        startConnection()
//    }
//
//    fun startConnection() {
//        if (!billingClient!!.isReady && !isConnecting) {
//            isConnecting = true
//            billingClient!!.startConnection(mBillingClientStateListener)
//        }
//    }
//
//    fun reConnection() {
//        if (!isConnecting) {
//            if (!billingClient!!.isReady) {
//                billingClient!!.startConnection(mBillingClientStateListener)
//            }
//        }
//    }
//
//    private fun isPermanently(sku: String?): Boolean {
//        return sku != null && (sku == skuPro || sku == skuPermanently)
//    }
//
//    fun queryPurchaseState(needToShowMessage: Boolean, needReload: Boolean) {
//        if (billingClient!!.isReady) {
//            /* request state in app */
//            mMapPurChaseQuery.clear()
//            val queryPurchasesParams = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
//            billingClient!!.queryPurchasesAsync(queryPurchasesParams) { billingResult: BillingResult, list: List<Purchase>? ->
//                var hasPurchase = false
//                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
//                } else {
//                    hasPurchase = processListPurChase(list, BillingClient.ProductType.INAPP)
//                }
//                checkFinishQueryAllPurchase(BillingClient.ProductType.INAPP, hasPurchase, needToShowMessage, needReload)
//            }
//
//
//            /* request state sub */
//            val queryPurchasesSubParams = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
//            billingClient!!.queryPurchasesAsync(queryPurchasesSubParams) { billingResult: BillingResult, list: List<Purchase>? ->
//                var hasPurchase = false
//                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
//                } else {
//                    hasPurchase = processListPurChase(list, BillingClient.ProductType.SUBS)
//                }
//                checkFinishQueryAllPurchase(BillingClient.ProductType.SUBS, hasPurchase, needToShowMessage, needReload)
//            }
//        }
//    }
//    private fun checkFinishQueryAllPurchase(type: String, value: Boolean, needToShowMessage: Boolean, needReload: Boolean) {
//        mMapPurChaseQuery[type] = value
//        if (mMapPurChaseQuery.containsKey(BillingClient.ProductType.SUBS) && mMapPurChaseQuery.containsKey(BillingClient.ProductType.INAPP)) {
//            isPro = !(!mMapPurChaseQuery[BillingClient.ProductType.SUBS]!! && !mMapPurChaseQuery[BillingClient.ProductType.INAPP]!!)
//            if (onBillingListener != null) {
//                onBillingListener!!.onUpdatePurchaseProSuccess(isPro, needToShowMessage, needReload)
//            }
//            if (onBillingListener != null) {
//                onBillingListener!!.onConnectionFinish(true, isPro)
//            }
//        }
//    }
//
//
//
//
//     private fun processListPurChase(purchases: List<Purchase>?, type: String): Boolean {
//        var hasPurchase = false
//         purchases?.apply {
//             for (purchase in purchases) {
//                 if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
//                     for (product in purchase.products) {
//                         if (isPermanently(product) || knownSubscriptionSKUs!!.contains(
//                                         product)) {
//                             /*only consume when test*/
////                        consumeBilling(purchase.getPurchaseToken());
//                             if (!hasPurchase) {
//                                 hasPurchase = true
//                             }
//                             Timber.e("processListPurChase  %s", purchase.originalJson)
//                             if (onBillingListener != null) {
//                                 isPro = true
//                                 onBillingListener!!.onUpdatePurchaseProSuccess(isPro = true, needShowMessage = false, needReload = true)
//                             }
//                         }
//                     }
//                     if (!purchase.isAcknowledged) {
//                         acknowledgePurchase(purchase.purchaseToken)
//                     }
//                 }
//             }
//         }
//
//        return hasPurchase
//    }
//
//
//    private fun processListSKuDetail(skuDetails: List<ProductDetails>, type: String) {
//        for (skuDetail in skuDetails) {
//            val gson = Gson()
//            Timber.e("processListSKuDetail  %s ProductDetails \n %s", skuDetail!!.productId, gson.toJson(skuDetail))
//        }
//        if (onBillingListener != null) {
//            if (type != BillingClient.ProductType.INAPP) {
//                onBillingListener!!.onSubsAvailable(skuDetails)
//            } else {
//                onBillingListener!!.onPurchaseAvailable(skuDetails)
//            }
//        }
//    }
//
//    private fun acknowledgePurchase(token: String) {
//        val purchaseParams: AcknowledgePurchaseParams =
//            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build()
//        billingClient!!.acknowledgePurchase(purchaseParams) { Timber.e("acknowledgePurchase token$token") }
//    }
//
//     fun queryAvailablePurchase() {
//        val skuList: MutableList<String> = ArrayList()
//        skuList.add(skuPro)
//        skuList.add(skuPermanently)
//        val productList: MutableList<Product> = ArrayList()
//        for (subSku in skuList) {
//            val product = Product.newBuilder()
//                    .setProductId(subSku)
//                    .setProductType(BillingClient.ProductType.INAPP)
//                    .build()
//            productList.add(product)
//        }
//        val params = QueryProductDetailsParams.newBuilder()
//        params.setProductList(productList)
//        billingClient!!.queryProductDetailsAsync(params.build()) { billingResult: BillingResult, list: List<ProductDetails>? ->
//            Timber.e("queryAvaiableSub %s ", billingResult.responseCode)
//            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                list?.let { processListSKuDetail(it, BillingClient.ProductType.INAPP) }
//            }
//        }
//    }
//
//     fun queryAvailableSubs() {
//        if (null != knownSubscriptionSKUs && !knownSubscriptionSKUs!!.isEmpty()) {
//            val productList: MutableList<Product> = ArrayList()
//            for (subSku in knownSubscriptionSKUs!!) {
//                val product = Product.newBuilder()
//                        .setProductId(subSku)
//                        .setProductType(BillingClient.ProductType.SUBS)
//                        .build()
//                productList.add(product)
//            }
//            val params = QueryProductDetailsParams.newBuilder()
//            params.setProductList(productList)
//            billingClient!!.queryProductDetailsAsync(params.build()) { billingResult: BillingResult, list: List<ProductDetails>? ->
//                Timber.e("queryAvaiableSub %s ", billingResult.responseCode)
//                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                    list?.let { processListSKuDetail(it, BillingClient.ProductType.SUBS) }
//                }
//            }
//        }
//    }
//
//    fun purchaseBilling(activity: Activity?, typeBilling: String?, productDetails: ProductDetails, offerToken: String?) {
//        val productDetailsParamsList: MutableList<ProductDetailsParams> = ArrayList()
//        if (productDetails.productType == BillingClient.ProductType.SUBS) {
//            productDetailsParamsList.add(ProductDetailsParams.newBuilder()
//                    .setProductDetails(productDetails)
//                    .setOfferToken(offerToken!!)
//                    .build())
//        } else {
//            productDetailsParamsList.add(ProductDetailsParams.newBuilder()
//                    .setProductDetails(productDetails)
//                    .build())
//        }
//        val billingFlowParams = BillingFlowParams.newBuilder()
//                .setProductDetailsParamsList(productDetailsParamsList)
//                .build()
//        billingClient!!.launchBillingFlow(activity!!, billingFlowParams)
//    }
//
//    fun consumeBilling(purchaseToken: String?) {
//        val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken!!).build()
//        billingClient!!.consumeAsync(consumeParams, mConsumeResponseListener)
//    }
//
//    fun checkBillingAvailable(): Boolean {
//        return billingClient != null && billingClient!!.isReady
//    }
//
//    private var mBillingClientStateListener: BillingClientStateListener = object : BillingClientStateListener {
//        override fun onBillingSetupFinished(billingResult: BillingResult) {
//            isConnecting = false
//            Timber.e("mBillingClientStateListener code %s result %s ", billingResult.responseCode, billingResult.debugMessage)
//            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                // The BillingClient is ready. You can query purchases here.
//                mHistoryRecords.clear()
//                queryPurchaseState(false, true)
//                queryAvailableSubs()
//                queryAvailablePurchase()
//            } else {
//                if (onBillingListener != null) {
//                    onBillingListener!!.onConnectionFinish(false, isPro)
//                }
//            }
//        }
//
//        override fun onBillingServiceDisconnected() {
//            Timber.e("onBillingServiceDisconnected")
//            if (onBillingListener != null) {
//                onBillingListener!!.onConnectionFinish(false, isPro)
//            }
//        }
//    }
//
//    private val mConsumeResponseListener = ConsumeResponseListener { billingResult: BillingResult, s: String? ->
//        Timber.e("mConsumeResponseListener %s ", billingResult.responseCode)
//        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//        }
//    }
//
//    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult: BillingResult, purchases: List<Purchase>? ->
//        Timber.e("purchasesUpdatedListener %s ", billingResult.responseCode)
//        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
//                && purchases != null) {
//            val gson = Gson()
//            for (purchase in purchases) {
//                Timber.e("purchases %s ", purchase!!.originalJson)
//                for (product in purchase.products) {
//                    if (isPermanently(product) || knownSubscriptionSKUs!!.contains(
//                                    product!!)) {
//                        isPro = true
//                        if (onBillingListener != null) {
//                            onBillingListener!!.onUpdatePurchaseProSuccess(true, true, true)
//                        }
//                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
//                            if (!purchase.isAcknowledged) {
//                                Timber.e("acknowledgePurchase %s ", purchase.products)
//                                acknowledgePurchase(purchase.purchaseToken)
//                            }
//                        }
//                    }
//                }
//            }
//            if (onBillingListener != null) {
//                onBillingListener!!.onPurchaseSuccess(purchases)
//            }
//        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
//            // Handle an error caused by a user cancelling the purchase flow.
//        } else {
//            // Handle any other error codes.
//            if (onBillingListener != null) {
//                onBillingListener!!.onPurchaseFailed(billingResult, purchases)
//            }
//        }
//    }
//
//    fun destroyBilling() {
//        if (billingClient != null) {
//            billingClient!!.endConnection()
//        }
//    }
//
//    interface OnBillingListener {
//        fun onUpdatePurchaseProSuccess(
//            isPro: Boolean,
//            needShowMessage: Boolean,
//            needReload: Boolean
//        )
//
//        fun onConnectionFinish(isConnected: Boolean, isPro: Boolean)
//        fun onPurchaseAvailable(skuDetails: List<ProductDetails>?)
//        fun onSubsAvailable(skuDetails: List<ProductDetails>?)
//        fun onPurchaseSuccess(purchases: List<Purchase>?)
//        fun onPurchaseFailed(billingResult: BillingResult?, purchases: List<Purchase>?)
//    }
//}