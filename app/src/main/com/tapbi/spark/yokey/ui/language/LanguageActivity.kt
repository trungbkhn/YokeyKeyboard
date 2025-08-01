package com.tapbi.spark.yokey.ui.language

import android.content.*
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.ads.nativetemplates.OnDecorationAds
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.LayoutLanguageBinding
import com.ironman.trueads.common.Common
import com.ironman.trueads.multiads.MultiAdsControl
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant
import com.tapbi.spark.yokey.common.Constant.CHECK_FIRST_TIME_SHOW_LANGUAGE
import com.tapbi.spark.yokey.common.Constant.PREF_LANGUAGE_CURRENT
import com.tapbi.spark.yokey.common.Constant.PREF_POSITION_LANGUAGE_CURRENT
import com.tapbi.spark.yokey.data.model.Language
import com.tapbi.spark.yokey.ui.adapter.LanguageAdapter
import com.tapbi.spark.yokey.ui.base.BaseActivity
import com.tapbi.spark.yokey.ui.welcome.WelcomeActivity
import com.tapbi.spark.yokey.util.LocaleUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class LanguageActivity : BaseActivity(), LanguageAdapter.IClickLanguage {
    private var isPro: Boolean =false
    private lateinit var layoutLanguageBinding: LayoutLanguageBinding
    private lateinit var viewModel: LanguageActivityViewModel
    private var languageCountry: String = "English"
    private var positionLanguageCurrent: Int = -1
    private var positionCurrent: Int = -1
    private var list: MutableList<Language> = mutableListOf()
    private var languageCode: String = "en" //Locale.getDefault().language
    lateinit var mPrefs: SharedPreferences
    private lateinit var languageAdapter: LanguageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        layoutLanguageBinding = DataBindingUtil.setContentView(this, R.layout.layout_language)
        layoutLanguageBinding.layoutLoading.visibility = View.VISIBLE
        viewModel = ViewModelProvider(this)[LanguageActivityViewModel::class.java]
        isPro = App.instance.billingManager!!.isPremium
        setUpAdapter()
        setUpObserverData()
        setUpClick()
        focusLanguage()
        initAds()
    }

    private fun focusLanguage() {
        if (checkLanguageInList()) {
            mPrefs.edit().putString(Constant.PREF_LANGUAGE_CURRENT, Locale.getDefault().language).apply()
            for (i in 0 until App.instance.languageCurList.size) {
                if (App.instance.languageCurList[i].languageCode == mPrefs.getString(PREF_LANGUAGE_CURRENT, "en")) {
                    mPrefs.edit().putInt(PREF_POSITION_LANGUAGE_CURRENT, i).apply()
                    Handler(Looper.getMainLooper()).postDelayed({
                        (layoutLanguageBinding.rcAppLanguage.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(i, 0)
                        layoutLanguageBinding.layoutLoading.visibility = View.INVISIBLE
                    }, 800)
                    break
                }
            }
        } else {
            mPrefs.edit().putInt(PREF_POSITION_LANGUAGE_CURRENT, 4).apply()
            mPrefs.edit().putString(Constant.PREF_LANGUAGE_CURRENT, "en").apply()
            Handler(Looper.getMainLooper()).postDelayed({
                (layoutLanguageBinding.rcAppLanguage.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(4, 0)
                layoutLanguageBinding.layoutLoading.visibility = View.INVISIBLE
            }, 800)
        }
    }

    private fun checkLanguageInList(): Boolean {
        for (i in 0 until App.instance.languageCurList.size) {
            if (App.instance.languageCurList[i].languageCode == Locale.getDefault().language) {
                return true
            }
        }
        return false
    }

    private fun setUpAdapter() {
        languageAdapter = LanguageAdapter(this, this)
        layoutLanguageBinding.rcAppLanguage.adapter = languageAdapter
        layoutLanguageBinding.rcAppLanguage.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    private fun setUpObserverData() {
        viewModel.getLanguage()
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.stateFlowLanguage.collect {
                list.addAll(it)
                languageAdapter.changeDataLanguage(it)
            }
        }
//        Handler(Looper.getMainLooper()).postDelayed({
//            val ps = mPrefs.getInt(PREF_POSITION_LANGUAGE_CURRENT, 0)
//            (layoutLanguageBinding.rcAppLanguage.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
//                ps,
//                0
//            )
//        }, 100)
    }

    override fun onResume() {
        super.onResume()
    }
    private fun getLocaleChangedFlow() = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                if (intent.action == Intent.ACTION_LOCALE_CHANGED) {
                    val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        resources.configuration.locales.get(0)
                    } else {
                        resources.configuration.locale
                    }
                    if (locale != null) {
                        trySend(locale)
                    }
                }
            }
        }
        registerReceiver(receiver, IntentFilter(Intent.ACTION_LOCALE_CHANGED))

        awaitClose {
            unregisterReceiver(receiver)
        }
    }

    private fun setUpClick() {
        //languageCountry =
        layoutLanguageBinding.imgDoneLanguage.setOnClickListener {
            if (positionCurrent != positionLanguageCurrent) {
                Timber.d("ducNQ setUpClick: ")
                positionCurrent = positionLanguageCurrent
                mPrefs.edit()
                    .putString(
                        Constant.PREF_SETTING_LANGUAGE,
                        languageCode
                    )
                    .apply()
                mPrefs.edit()
                    .putString(
                        Constant.PREF_LANGUAGE_CURRENT,
                        languageCode
                    )
                    .apply()
                mPrefs.edit()
                    .putInt(
                        Constant.PREF_POSITION_LANGUAGE_CURRENT,
                        positionLanguageCurrent
                    )
                    .apply()
                LocaleUtils.applyLocale(this)
            }
            mPrefs.edit().putBoolean(CHECK_FIRST_TIME_SHOW_LANGUAGE, false).apply()
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
    }

    override fun clickLanguageCurrent(
        languageCode: String,
        language: String,
        position: Int,
        sizeList: Int,
        languageCountry: String
    ) {
        positionLanguageCurrent = position
        //this.languageCountry = languageCountry
        this.languageCode = languageCode

    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
        } else {
            configuration.locale = locale
        }
        resources
            .updateConfiguration(configuration, resources.displayMetrics)
    }
    private fun initAds() {
        if (isPro) {
            layoutLanguageBinding.frAds.visibility=View.GONE
            return
        }
        layoutLanguageBinding.frAds.visibility = View.VISIBLE
        //show ads native
        val mapId = Common.getMapIdAdmobApplovin(this,R.array.admob_native_id_language,R.array.applovin_native_id_language)
        MultiAdsControl.showNativeAdNoMedia(
            this,
            mapId,
            layoutLanguageBinding.frAds,
            null, null, object :OnDecorationAds{
                override fun onDecoration(network: String?) {
                    layoutLanguageBinding.frAds?.getNativeAdView(network)?.setBackgroundColor(ContextCompat.getColor(this@LanguageActivity,R.color.bg_ads_language))

                }

            })

    }
}