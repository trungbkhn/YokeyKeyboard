package com.tapbi.spark.yokey

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ContextWrapper
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.*
import android.preference.PreferenceManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import com.android.inputmethod.accessibility.AccessibilityUtils
import com.android.inputmethod.latin.AudioAndHapticFeedbackManager
import com.android.inputmethod.latin.InputView
import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils
import com.ironman.spark.billing.BillingManager
import com.ironman.trueads.admob.ControlAds
import com.ironman.trueads.admob.ControlAds.createDebugSetting
import com.ironman.trueads.applovin.ControlAdsMAX
import com.ironman.trueads.common.Common.getListHashDeviceTapbi
import com.ironman.trueads.multiads.InitMultiAdsListener
import com.ironman.trueads.multiads.MultiAdsControl
import com.tapbi.spark.yokey.data.local.entity.Emoji
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity
import com.tapbi.spark.yokey.data.model.Language
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.theme.ThemeModel
import com.tapbi.spark.yokey.data.repository.*
import com.tapbi.spark.yokey.interfaces.ListenerConnectNetwork
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.LocaleUtils
import com.tapbi.spark.yokey.util.MyDebugTree
import com.tapbi.spark.yokey.util.MyThreadPoolExecutor
import com.tapbi.spark.yokey.data.repository.FontRepository
import com.tapbi.spark.yokey.data.repository.KeyboardLanguageRepository
import com.tapbi.spark.yokey.data.repository.LanguageRepository
import com.tapbi.spark.yokey.data.repository.StickerRepository
import com.tapbi.spark.yokey.data.repository.SymbolsReposition
import com.tapbi.spark.yokey.data.repository.ThemeRepository
import com.tapbi.spark.yokey.data.repository.TranslateRepository
import dagger.hilt.android.HiltAndroidApp
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.io.File
import javax.inject.Inject


@HiltAndroidApp
class App : Application() {


    var isTryKeyboard: Boolean = false

    @JvmField
    var createAdditionalSubtype: Boolean = false

    @set:Inject
    var languageRepository: LanguageRepository? = null

    @JvmField//require kotlin not create getter and setter in java
    @Inject
    var themeRepository: ThemeRepository? = null

    @JvmField
    @Inject
    var stickerRepository: StickerRepository? = null
    var languageCurList = mutableListOf<Language>()
    var languageCurPs = 0

    @JvmField
    var checkFirstTimeSetBg = false

    @JvmField
    @Inject
    var fontRepository: FontRepository? = null

//    @JvmField
//    @Inject
//    var stickerRepository: StickerRepository? = null

    @JvmField
    var symbolsReposition: SymbolsReposition? = null

    @JvmField
    @Inject
    var myThreadPoolExecutor : MyThreadPoolExecutor?=null
    @JvmField
    @Inject
    var keyboardLanguageRepository: KeyboardLanguageRepository? = null

    @JvmField
    var appDir: File? = null

    @JvmField
    var isShowEmoji = false

    @JvmField
    var listFontNotUse = mutableListOf<String>()

    @JvmField
    var listFontNotUsed = HashMap<String, String>()

    @JvmField
    var bitmap: Bitmap? = null

    @JvmField
    var checkBackGroundEmojiPalettesView = false

    @JvmField
    var mPrefs: SharedPreferences? = null
    var isShowPreview = false

    @JvmField
    var listEmojiDb = ArrayList<Emoji>()

    @JvmField
    var colorIconDefault = -1
    @JvmField
    var colorIconNew = -1

    @JvmField
    var colorIconCustomize = -1

    @JvmField
    var idScreen = 0
    @JvmField
    var checkScreen = false
    var listenerConnectNetwork: ListenerConnectNetwork? = null

    var sound = Constant.AUDIO_DEFAULT

    @JvmField
    var widthScreen = CommonUtil.getScreenWidth()

    @JvmField
    var heightScreen = 0

    @JvmField
    var nameFolder = System.currentTimeMillis().toString()
    var themeEntity: ThemeEntity? = null

    @JvmField
    var themeModel: ThemeModel?=null

    @JvmField
    var themeModelSound: ThemeModel?=null

    @JvmField
    var linkCurrentBg: String = "null"

    @JvmField
    var soundKey: String = Constant.AUDIO_DEFAULT

    @JvmField
    var effect: String = Constant.ID_NONE

    @JvmField
    var typeKey: Int = Constant.TYPE_KEY_2006

   // var pathCurrentBg = ""

    @JvmField
    var textCurrent = ""

    @JvmField
    var idCategorySymbols = 1

    @JvmField
    var keyboardHeight = 0

    @JvmField
    var idBgCurrent = 1

    @JvmField
    var idPath = "1002"

    @JvmField
    var idTheme = "0"

    @JvmField
    var blurBg = 0

    @JvmField
    var textSize = 0

    @JvmField
    var checkCustomTheme = false

    @JvmField
    var colorCurrent = "#ffffff"


    @JvmField
    var pathFolderBgCurrent = ""

    @JvmField
    var pathEffect = Constant.ID_NONE

    var contextWrapper: ContextWrapper? = null

    @JvmField
    var file: File? = null

    @JvmField
    var blurKillApp = 0
    var mutableListLanguage = mutableListOf<Language>()

    @JvmField
    @Inject
    var translateRepository: TranslateRepository? = null

    var handlerJob: Handler? = null
    @JvmField
    var checkDownloadBg = false

    var mImm: InputMethodManager? = null
    @JvmField
    var isScreenLandscape : Boolean = false
    private var android_id: String = ""
    @JvmField
     var typeEditing = Constant.TYPE_EDIT_NONE

    var mInputView: InputView? = null

    var billingManager: BillingManager? = null


    override fun onCreate() {
        super.onCreate()
        mImm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        symbolsReposition = SymbolsReposition(this)
        keyboardLanguageRepository!!.getAllLanguageDb(true).subscribe()
        appDir = getDir(filesDir.name, MODE_PRIVATE)
        contextWrapper = ContextWrapper(instance)
        file = contextWrapper!!.getDir(filesDir.name, MODE_PRIVATE)
        initHandlerThreadJob()
        handlerJob!!.sendEmptyMessage(Constant.MESSAGE_INIT_CONFIG)
        checkNetWorkConnection()
        initLog()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        BillingManager.init(this, listOf(getString(R.string.productID),getString(R.string.purchase_pro_id_permanently)), listOf(*resources.getStringArray(R.array.subscription_list)),"REMOVE_ADS")
        billingManager = BillingManager
        themeRepository!!.preloadData()
        stickerRepository?.updateListStickerOnkeyboard()
        if (mPrefs!!.getBoolean(com.tapbi.spark.yokey.common.Constant.CHECK_FIRST_TIME_SHOW_LANGUAGE, true))
        {
            languageRepository!!.getLanguage()
        }
         LocaleUtils.applyLocale(this)
        // Copy theme default from Asset when user use fist time
        CommonUtil.copyThemeFofFirstTimeOpenAppFromAssetToFile(this);
    }


    private fun checkNetWorkConnection() {
        val networkCallback: ConnectivityManager.NetworkCallback =
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    // network available
                    EventBus.getDefault().post(MessageEvent(Constant.CONNECT_INTERNET, null))
                    connectivityStatus = 1
                }

                override fun onLost(network: Network) {
                    EventBus.getDefault().post(MessageEvent(Constant.DISCONNECT_INTERNET, null))
                    connectivityStatus = -1
                }
            }
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
            } else {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
                connectivityManager.registerNetworkCallback(request, networkCallback)
            }
        } catch (exception: Exception) {
            val intentFilter = IntentFilter()
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED")
            listenerConnectNetwork = ListenerConnectNetwork()
            try {
                registerReceiver(listenerConnectNetwork, intentFilter)
            } catch (e: Exception) {
                unregisterReceiver(listenerConnectNetwork)
                registerReceiver(listenerConnectNetwork, intentFilter)
            }
        }

    }

    private fun initHandlerThreadJob() {
        val handlerThread = HandlerThread("config_thread")
        handlerThread.start()
        handlerJob = object : Handler(handlerThread.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                Log.d("duongcv", "handleMessage: " + msg.what)
                when (msg.what) {
                    Constant.MESSAGE_INIT_CONFIG -> instance.initConfigAsync()
                }
            }
        }
    }

    fun initConfigAsync() {
        if (UncachedInputMethodManagerUtils.isThisImeEnabled(this, mImm)) {
            checkUpdateLanguage(false)
        }
    }

    fun checkUpdateLanguage(fromConfigChanged: Boolean) {
        keyboardLanguageRepository?.updateLanguages(
            keyboardLanguageRepository?.checkSystemLocaleChange() ?: false,
            fromConfigChanged
        )
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(MyDebugTree())
        }
    }
    fun initAds(activity: Activity,  initMultiAdsListener: InitMultiAdsListener?) {
        val list: MutableList<String> = java.util.ArrayList(getListHashDeviceTapbi(this))
//        val list: MutableList<String> = ArrayList<Any?>(Common.INSTANCE.getListHashDeviceTapbi())

        list.add("A89F0A148F398FB52A0277FCD9B8C462")
        list.add("90D9022B87F8B06E4E65B08684F6A6A7")
        list.add("303E37FA4CDB74564D1C1E2BA6E3A09E")
        list.add("26B54C15D6D13F05BE30E7DE97C7F447")
        list.add("8C42079F834FBEBAF7DD96DD8EBE5487")
        list.add("D2914ACF8DC3D1A0D7E19F95ADDE7247")
        list.add("B4450C90F86F513AEF334A21987C0F66")
        list.add("B730FB960974868CE3BC0B175EB33496")
        list.add("4F1505FAAC208A3AE3C29D8BC7596AD7")
        list.add("56E2B11E1950D8A3DECAB0C11CB770CC")
        list.add("881A8CDA98CF3FD6F05631CA7B7B48B4")
        list.add("81A89BDB7696BCAEB85550C5FA8FE5BD")
        list.add("A2700B237DFADAC749D9FDB3882E22FF")
        list.add("12FF267531127826C5924159FA14C8C1")
        list.add("F399502DA515FD5D9FE512D2377E878C")
        list.add("47D481BB45CD94D21977A317366EDF52")
        list.add("17610C08332F50C97E6CD2CA4A49C6F5")
        list.add("BF730B5C34320F67CD989155EC9B8FD4")
        list.add("AFB325D4E9B651E9881AB3D394AED5D9")
        list.add("1CE782C7B6D2339F8DC28FA42127D5A7")
        list.add("52F0C06CB5FE55CF35995E2943C42503")
        list.add("DC1E4D9EE243EAF7A008427D8AEA063F")
        list.add("BB052A7EF4D2361E43075244EC1EDB81")
        list.add("3C62346842D71E7A11B081FC0A9C47A4")
        list.add("A6FBF3850FC1B311E5380FF03C0E0EC2")
        list.add("868BED09BCFC0CB45319E0D18E118F51")
        list.add("6DD24D5F8DA0A8D94BE55A138EFC8B41")
        list.add("12538C8CFF4E71F4B3DA3D9FF33BE582")
        list.add("D71314D933428CA09C4C49B26BD6422C")
        list.add("52FD6C041B9B0981393EEB469EC9CE67")
        createDebugSetting(activity.applicationContext, BuildConfig.DEBUG, true, list)
        ControlAds.setupUnitIdAdmobWaterFall(
            this,
            resources.getStringArray(R.array.admob_ads_open_id),
            resources.getStringArray(R.array.admob_interstitial_id),
            null
        )
        ControlAdsMAX.setupAds(
            this,
            resources.getStringArray(R.array.applovin_ads_open_id),
            resources.getStringArray(R.array.applovin_interstitial_id),
            null
        )
        MultiAdsControl.initAdsWithConsent(activity, BuildConfig.DEBUG,getString(R.string.ironsrc_app_key),false,initMultiAdsListener)

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        checkUpdateLanguage(true)
        super.onConfigurationChanged(newConfig)
    }


    var connectivityStatus: Int = 0

    fun checkConnectivityStatus(): Int {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (null != activeNetwork && activeNetwork.isConnectedOrConnecting) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                return 1
            }
            if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                return 0
            }
        }
        return -1
    }

    fun changeTypeEdit(type: Int) {
        typeEditing = type
    }

    fun getTypeEditing(): Int {
        return typeEditing
    }

    companion object {
        @JvmStatic
        lateinit var instance: App

        @JvmStatic
        var isActivityVisible = false
            private set

        fun activityResumed() {
            isActivityVisible = true
        }

        @SuppressLint("CommitPrefEdits")
        fun activityPaused() {
            isActivityVisible = false
        }

    }

    init {
        instance = this

    }

    fun initConfigForKeyboard(){
        // Timber.d("ducNQ : onCreateddd: ");
        com.android.inputmethod.latin.settings.Settings.init(this)
        AccessibilityUtils.init(instance)
        AudioAndHapticFeedbackManager.init(instance)

    }

}