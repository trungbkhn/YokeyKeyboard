package com.tapbi.spark.yokey.ui.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.android.billingclient.api.*
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.MainActivityKtBinding
import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils
import com.applovin.mediation.MaxAd
import com.hold1.keyboardheightprovider.KeyboardHeightProvider
import com.ironman.spark.billing.BillingManager
import com.ironman.spark.billing.BillingModel
import com.ironman.trueads.admob.ControlAds
import com.ironman.trueads.admob.ControlAds.admobInitialized
import com.ironman.trueads.admob.ControlAds.enableAutoRefresh
import com.ironman.trueads.applovin.ControlAdsMAX
import com.ironman.trueads.common.Common
import com.ironman.trueads.internetdetect.networkchecker.NetworkLiveData
import com.ironman.trueads.internetdetect.networkchecker.NetworkState
import com.ironman.trueads.ironsource.BannerAdIronSource
import com.ironman.trueads.ironsource.InterstitialAdIronSource
import com.ironman.trueads.multiads.InitMultiAdsListener
import com.ironman.trueads.multiads.MultiAdsControl
import com.ironman.trueads.multiads.ShowNativeAdsListener
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant.CHECK_LOAD_UPDATE_NEW_PHASE7
import com.tapbi.spark.yokey.common.Constant.OPEN_CROP_FRAGMENT
import com.tapbi.spark.yokey.data.local.entity.ItemFont
import com.tapbi.spark.yokey.data.local.entity.Sticker
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.ThemeObject
import com.tapbi.spark.yokey.ui.base.BaseBindingActivity
import com.tapbi.spark.yokey.ui.dialog.DialogPermission
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import company.librate.RateDialog
import timber.log.Timber

class MainActivity : BaseBindingActivity<MainActivityKtBinding, MainViewModel>(),
    KeyboardHeightProvider.KeyboardListener, DialogPermission.IListenerPermission,
    BillingManager.OnBillingListener {
    var currentPageTheme = 0
    var currentPageFont = 0
    var currentPageEmojiSticker = 0
    var currentPageEmoji = 0
    var currentPageSticker = 0
    private var inputMethodManager: InputMethodManager? = null
    lateinit var navControllers: NavController
    private lateinit var navHostFragment: NavHostFragment
    private var graph: NavGraph? = null
    var mPrefs: SharedPreferences? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var isLoadAdsNativeBack = false
    private var rateDialog: RateDialog? = null
    var currentPager = 0
    var isEnable = false
    var loadAd = false
    private var dialogPermission: DialogPermission? = null
    var typeTryKeyboard = com.tapbi.spark.yokey.common.Constant.TYPE_TRY_THEME_KEYBOARD
    var detailThemeObject: ThemeObject? = null
    var detailThemeEntity: ThemeEntity? = null
    var detailSticker: Sticker? = null
    var detailFont: ItemFont? = null
    private var showConsentLater = false

    override val layoutId: Int
        get() = R.layout.main_activity_kt

    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java

    override fun setupView(savedInstanceState: Bundle?) {

        navHostFragment = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?)!!
        navHostFragment.apply {
            navControllers = this.navController
            navControllers.addOnDestinationChangedListener(OnDestinationChangedListener { controller: NavController?, destination: NavDestination?, arguments: Bundle? ->
                if (destination != null && !App.instance!!.billingManager!!.isPremium) {
                    showConsentLater = false
                }
            })
        }

        inputMethodManager = this@MainActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        keyboardHeightProvider = KeyboardHeightProvider(this)
        keyboardHeightProvider!!.addKeyboardListener(this)
        ControlAds.configDelayShowAdsInterAdmob(App.instance)
        ControlAdsMAX.configDelayShowAdsInterApplovin(App.instance)
        App.instance.initConfigForKeyboard()
    }



    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("CommitPrefEdits")
    override fun setupData() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        loadAdsExitApp()
        if (!mPrefs!!.getBoolean(CHECK_LOAD_UPDATE_NEW_PHASE7, false)) {
            viewModel!!.loadEmojiDBPhase7()
            Handler(Looper.getMainLooper()).postDelayed({ viewModel!!.loadEmojiFromJson() }, 100)
            mPrefs!!.edit().putBoolean(CHECK_LOAD_UPDATE_NEW_PHASE7, true).apply()
        }

        viewModel?.mLiveEventScreen?.observe(this) {
            changeStartScreen(it.key, null)
        }
        NetworkLiveData.get().observe(
            this
        ) { networkState: NetworkState? ->
            if (networkState != null && networkState.isConnected) {
                if (!App.instance!!.billingManager!!.checkBillingAvailable()) {
                    App.instance!!.billingManager!!.retryBillingServiceIfNeeded(this)
                } else if (!App.instance!!.billingManager!!.isPremium && !admobInitialized
                ) {
//                    initAdsConsentOutSidePremium()
                }
            }
        }
        viewModel?.eventDismissLoadingAds?.observe(this) { aBoolean ->
            if (aBoolean != null && aBoolean) {
                setVisibilityProgressAds(View.GONE)
                viewModel?.eventDismissLoadingAds?.setValue(false)
            }
        }
    }

    private fun loadAdsExitApp() {
        if (App.instance!!.billingManager!!.isPremium) {
            return
        }
        binding?.rootLayout?.apply {
            if (findViewById<ConstraintLayout>(R.id.layout_ads_native_back) == null) {
                val inflater = LayoutInflater.from(this@MainActivity)
                inflater.inflate(R.layout.admob_native_ads_back_layout, binding?.rootLayout)
            }
            binding?.rootLayout!!.findViewById<ConstraintLayout>(R.id.layout_ads_native_back)!!.visibility =
                View.GONE
            val constraintLayout: ConstraintLayout? = binding?.rootLayout?.findViewById(R.id.layout_ads_native_back)
            val mapId = Common.getMapIdAdmobApplovin(this@MainActivity,R.array.admob_native_id_exit_app,R.array.applovin_native_id_exit_app)
            constraintLayout?.let {
                MultiAdsControl.showNativeAdExitApp(this@MainActivity,
                    1,
                    it,
                    mapId,
                    isMediaView = false,
                    enableAutoRelease = true,
                    listener = object : ShowNativeAdsListener {
                        override fun onAdRevenuePaid(adsType: Int, ad: MaxAd?) {

                        }

                        override fun onAdsNativeClicked(adsType: Int) {

                        }

                        override fun onAlreadyLoaderAdsAndShowAgain(adsType: Int) {
                            isLoadAdsNativeBack = true
                        }

                        override fun onLoadAdsNativeCompleted(adsType: Int) {
                            isLoadAdsNativeBack = true
                        }

                        override fun onLoadAdsNativeFail(adsType: Int) {

                        }

                        override fun onLoadAdsNativeNotShow(adsType: Int) {

                        }
                    }
                )
            }
        }

    }

    var countBackPress = 0;

    fun getCurrentId(): Int {
        return navControllers.currentDestination!!.id
    }

    fun enableClick() {
        isEnable = true
        Handler(Looper.getMainLooper()).postDelayed({ isEnable = false }, 1500)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (!isEnable) super.dispatchTouchEvent(ev) else isEnable
    }

    override fun onResume() {


//        CommonUtil.checkCurrentAppKeyBoard(this)

        super.onResume()
        changeStatusListenerShowKeyboard(true)
//        try {
//            try {
//                if (App.instance.mImm == null || App.instance.mImm?.inputMethodList?.isEmpty() ?: true) {}
//            }catch (e : Exception){
//                App.instance.mImm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//            }
//            if (!UncachedInputMethodManagerUtils.isThisImeCurrent(this, App.instance.mImm)) {
//                val intent = Intent()
//                intent.setClass(this, WelcomeActivity::class.java)
//                intent.flags = (Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
//                        or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                startActivity(intent)
//                finish()
//            }
//        } catch (e: java.lang.Exception) {
//            Timber.e(e)
//        }
        App.activityResumed()
        InterstitialAdIronSource.resumeInterstitialAdIronSource(this)


    }

    @SuppressLint("CommitPrefEdits")
    override fun onPause() {
        Timber.e("IronSource onPause")
        InterstitialAdIronSource.onPauseInterstitialAdIronSource(this)
        changeStatusListenerShowKeyboard(false)
        App.activityPaused()
//        App.instance.mPrefs!!.edit()
//            .putBoolean("hello", true).apply()
        super.onPause()
    }

    fun purchaseBilling(billingModel: BillingModel?) {
        if (App.instance.billingManager!!.checkBillingAvailable()) {
            var offerToken = ""
            billingModel?.let {
                BillingManager.purchaseBilling(this,it,this)
            }

        } else {
            Toast.makeText(
                this,
                getString(R.string.premium_billing_not_avaiable),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun queryCheckPro() {
        if (!App.instance.billingManager!!.checkBillingAvailable()) {
            Toast.makeText(
                this,
                getString(R.string.premium_billing_not_avaiable),
                Toast.LENGTH_SHORT
            ).show()
            App.instance.billingManager?.retryBillingServiceConnectionWithExponentialBackoff()
        } else {
            App.instance.billingManager?.queryCheckPro(this, this)
        }
    }

    override fun onHeightChanged(height: Int) {
        viewModel!!.mLiveEventKeyboardShow.postValue(height)
    }


    fun changeStartScreen(idStart: Int, bundle: Bundle?) {
        if (graph == null) {
            graph = navControllers.navInflater.inflate(R.navigation.main_nav)
        }

        // fix bug ZOMJ-716
//        if (getCurrentInNavGraph() != idStart) {
        try {
            navControllers.navigate(idStart, bundle)
        }catch (e : IllegalStateException){
            Timber.e("Duongcv " + e.message);
        }
        // graph!!.startDestination = idStart
        // navControllers.setGraph(graph!!, bundle)
    }


    fun changeStatusListenerShowKeyboard(status: Boolean) {
        if (keyboardHeightProvider != null) {
            if (status) keyboardHeightProvider!!.onResume()
            else keyboardHeightProvider!!.onPause()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onDestroy() {
        super.onDestroy()
        currentPager = 0
        if (keyboardHeightProvider != null) {
            keyboardHeightProvider!!.removeKeyboardListener(this)
            keyboardHeightProvider!!.onDestroy();
        }
        BannerAdIronSource.destroyBanner()
    }

    fun getMainViewModel(): MainViewModel {
        return viewModel!!
    }

    private fun pickerImage() {
        try {
            CommonUtil.hideSoftInput(this)
            mGetContentImage.launch("image/*")
        } catch (exception: Exception) {
            //showToastError(getString(R.string.add_theme_error_pick_image))
        }
    }


    fun changeOpenGallery() {
        pickerImage()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Constant.REQUEST_PERMISSION_GALLERY && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickerImage()
            } else {
                checkDoNotShowAgain(permissions[0])
            }
        }else if (requestCode == Constant.REQUEST_PERMISSION_SAVE && grantResults.isNotEmpty()) {

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private var mGetContentImage = (this as ComponentActivity).registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        // Handle the returned Uri
        if (uri != null) {
            val bundle = Bundle()
            bundle.putString(com.tapbi.spark.yokey.common.Constant.URI_IMAGE, uri.toString())
            viewModel?.mLiveEventCrop?.postValue(MessageEvent(OPEN_CROP_FRAGMENT, bundle))
            //  changeStartScreen(R.id.cropFragment,bundle)
//            cropImage.arguments = bundle
//            cropImage.show(supportFragmentManager, "cropImage")
        }
    }

    private fun checkDoNotShowAgain(permission: String?): Boolean {
        val b = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            permission!!
        )
        if (!b) {
            showDialog()
            return true
        }
        return false
    }

    fun backToScreen(idScreenBackTo: Int) {
//        NavOptions navOptions = new NavOptions.Builder().setPopUpTo(idScreenBackTo, true).build();
        navControllers.popBackStack(idScreenBackTo, false)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {

        super.onSaveInstanceState(outState, outPersistentState)
    }

    private fun showDialog() {
        AlertDialog.Builder(this).setMessage(getString(R.string.go_to_setting))
            .setPositiveButton(getString(R.string.yes_theme)) { _, _ ->
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri =
                    Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }.setNegativeButton(getString(R.string.no), null)
            .show()
    }



    override fun enabledView() {

    }

    override fun accept() {
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
//            EventBus.getDefault().post(MessageEvent(Constant.ACTION_CHANGE_STATE_ACTIVATE_KEYBOARD))
        }
    }


    fun initAdAndLoadAds() {
        if (!admobInitialized) {
            App.instance!!.initAds(this, object : InitMultiAdsListener {
                override fun onInitAllAdsCompleted(
                    networkAdsStateAll: Long,
                    canNextScreen: Boolean
                ) {
                    enableAutoRefresh = true
                    autoLoadsAds()
                    MultiAdsControl.loadAdsInterstitialManual(applicationContext)
                    MultiAdsControl.enableShowAdsOpenForeground(App.instance!!,true)
                    MultiAdsControl.loadAdsForShowLater(this@MainActivity)
                }

                override fun onLoadAdsOpen(networkAdsOpen: String?) {

                }

            })
        }
    }

    private fun getCurrentVisibleFragment(): Fragment? {
        try {
            val navHostFragment =
                supportFragmentManager.primaryNavigationFragment as NavHostFragment?
            val fragmentManager = navHostFragment!!.childFragmentManager
            val fragment = fragmentManager.primaryNavigationFragment
            if (fragment is Fragment) {
                return fragment
            }
        } catch (exception: java.lang.Exception) {
        }
        return null
    }

    override fun onUpdatePurchased(isPro: Boolean, needShowMessage: Boolean, needReload: Boolean) {

        runOnUiThread(Runnable {
            if (needShowMessage) {
                var messageShow = getString(R.string.premium_remove_ads_success)
                if (!isPro) {
                    messageShow = getString(R.string.pro_restore_failed)
                }

                CommonUtil.customToast(this, messageShow)

            }
            if (isPro) {
                ControlAds.enableAutoRefresh = false
                MultiAdsControl.enableShowAdsOpenForeground(App.instance!!,false)
            } else {
                initAdAndLoadAds()
            }
            viewModel?.mLiveDataRemoveAds?.postValue(isPro)
            if (isPro && needShowMessage) {
                CommonUtil.customToast(this, getString(R.string.premium_remove_ads_success))
            }
        })
    }

    fun setVisibilityProgressAds(visibilityProgressAds: Int) {
        Timber.e("setVisibilityProgressAds $visibilityProgressAds")
        binding?.bgLoadingAds?.visibility = visibilityProgressAds
    }





}