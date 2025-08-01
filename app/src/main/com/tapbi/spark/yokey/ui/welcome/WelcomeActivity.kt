package com.tapbi.spark.yokey.ui.welcome

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.LayoutLanguageBinding
import com.tapbi.spark.yokey.databinding.WelcomeActivityBinding
import com.android.inputmethod.latin.utils.LeakGuardHandlerWrapper
import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.ui.adapter.LanguageAdapter
import com.tapbi.spark.yokey.ui.base.BaseActivity
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.LocaleUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class WelcomeActivity : BaseActivity(), View.OnClickListener,
     LanguageAdapter.IClickLanguage {
    private var languageCountry: String = "English"
    private var positionLanguageCurrent: Int = 0

    private var handler: SettingsPoolingHandler? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var isShow = false
    private var languageCode: String = ""
    private lateinit var welcomeViewModel: WelcomeViewModel
    private var layoutLanguageBinding: LayoutLanguageBinding? = null
    private var checkMain = false
    lateinit var mPrefs: SharedPreferences
    private lateinit var binding: WelcomeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        welcomeViewModel = ViewModelProvider(this)[WelcomeViewModel::class.java]
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        LocaleUtils.applyLocale(this)
        handler = SettingsPoolingHandler(this@WelcomeActivity, App.instance.mImm)
        // init ads
        // Init open ads
        binding = DataBindingUtil.setContentView(this, R.layout.welcome_activity)

        startMain()
        binding.btnStartKeyboard.setOnClickListener(View.OnClickListener {
            binding.btnStartKeyboard.isEnabled = false

            handler!!.startPollingImeSettings()
        })

        binding.btnSelectInput.setOnClickListener(
            View.OnClickListener { App.instance.mImm?.showInputMethodPicker() }
        )
        binding.btnSelectInput.visibility = View.GONE
        binding.spinKitMain.visibility = View.GONE
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

    }

    private fun showChooseLanguage() {
        binding.viewStubSLanguage.viewStub!!.layoutResource = R.layout.layout_language
        binding.viewStubSLanguage.viewStub!!.inflate()
        layoutLanguageBinding =
            binding.viewStubSLanguage.binding as LayoutLanguageBinding
        eventClick()
        setUpData()
        layoutLanguageBinding?.cstlParent?.visibility = View.VISIBLE
    }

    private fun eventClick() {
        languageCountry =
            mPrefs.getString(com.tapbi.spark.yokey.common.Constant.PREF_LANGUAGE_COUNTRY, "English")!!
        layoutLanguageBinding?.imgDoneLanguage?.setOnClickListener {
            mPrefs.edit()
                .putString(com.tapbi.spark.yokey.common.Constant.PREF_SETTING_LANGUAGE, languageCode)
                .apply()
            mPrefs.edit()
                .putString(com.tapbi.spark.yokey.common.Constant.PREF_LANGUAGE_CURRENT, languageCode)
                .apply()
            mPrefs.edit()
                .putString(com.tapbi.spark.yokey.common.Constant.PREF_LANGUAGE_COUNTRY, languageCountry)
                .apply()
            mPrefs.edit()
                .putInt(
                    com.tapbi.spark.yokey.common.Constant.PREF_POSITION_LANGUAGE_CURRENT,
                    positionLanguageCurrent
                )
                .apply()
            layoutLanguageBinding?.cstlParent?.visibility = View.GONE
            recreate()
        }
    }

    private fun setUpData() {
        val languageAdapter = LanguageAdapter(this, this)
        layoutLanguageBinding?.rcAppLanguage?.adapter = languageAdapter
        layoutLanguageBinding?.rcAppLanguage?.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        setUpDataObserver(languageAdapter)
    }

    private fun setUpDataObserver(languageAdapter: LanguageAdapter) {
        welcomeViewModel.getLanguage()
        CoroutineScope(Dispatchers.Main).launch {
            welcomeViewModel.stateFlowLanguage.collect {
                languageAdapter.changeDataLanguage(it)
            }
        }
    }

    override fun onBackPressed() {
        if (layoutLanguageBinding != null && layoutLanguageBinding?.cstlParent?.visibility == View.VISIBLE) {
            layoutLanguageBinding?.cstlParent?.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    fun invokeLanguageAndInputSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_INPUT_METHOD_SETTINGS
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        startActivity(intent)
    }

    fun invokeSetupWizardOfThisIme() {
        handler!!.cancelPollingImeSettings()
        App.instance.checkUpdateLanguage(false)
        val intent = Intent()
        intent.setClass(this, WelcomeActivity::class.java)
        intent.flags = (Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                or Intent.FLAG_ACTIVITY_SINGLE_TOP
                or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        checkMain = false
        if (UncachedInputMethodManagerUtils.isThisImeEnabled(this, App.instance.mImm)) {
            autoShowPickerKeyboard()
            binding.btnSelectInput.visibility = View.VISIBLE
            binding.btnStartKeyboard.visibility = View.GONE
            binding.spinKitMain.visibility = View.GONE
        }
        binding.btnStartKeyboard.isEnabled = true
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        try {
            super.onWindowFocusChanged(hasFocus)
            Log.e("duongcv", "onWindowFocusChanged: ")
            startMain()
        } catch (e: NullPointerException) {

        }
    }

    override fun onClick(v: View) {}
    override fun onDestroy() {
        super.onDestroy()
        handler!!.cancelPollingImeSettings()
    }

    private fun delayShow() {
        isShow = true
        Handler(Looper.getMainLooper()).postDelayed({ isShow = false }, 2000)
    }

    private class SettingsPoolingHandler internal constructor(
        ownerInstance: WelcomeActivity?,
        private val mImmInHandler: InputMethodManager?
    ) : LeakGuardHandlerWrapper<WelcomeActivity?>(
        ownerInstance!!
    ) {
        override fun handleMessage(msg: Message) {
            val welcomeActivity = ownerInstance ?: return
            Log.e("duongcv", "handleMessage: " + msg.what)
            when (msg.what) {
                MSG_POLLING_IME_SETTINGS -> {
                    if (UncachedInputMethodManagerUtils.isThisImeEnabled(
                            welcomeActivity,
                            mImmInHandler
                        )
                    ) {

                        welcomeActivity.invokeSetupWizardOfThisIme()
                        return
                    }
                    startPollingImeSettings()
                }
            }
        }

        fun startPollingImeSettings() {
            sendMessageDelayed(
                obtainMessage(MSG_POLLING_IME_SETTINGS),
                IME_SETTINGS_POLLING_INTERVAL
            )
        }

        fun cancelPollingImeSettings() {
            removeMessages(MSG_POLLING_IME_SETTINGS)
        }

        companion object {
            private const val MSG_POLLING_IME_SETTINGS = 0
            private const val IME_SETTINGS_POLLING_INTERVAL: Long = 200
        }
    }

    private fun startMain() {
//        try {
//            try {
//                if (App.instance.mImm == null || App.instance.mImm?.inputMethodList?.isEmpty() ?: true) {
//
//                }
//            } catch (e: Exception) {
//                App.instance.mImm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//            }
//            if (UncachedInputMethodManagerUtils.isThisImeCurrent(this, App.instance.mImm)) {
                if (!checkMain) {
                    binding.btnSelectInput.visibility = View.GONE
                    binding.spinKitMain.visibility = View.VISIBLE
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = (Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                            or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    if (getIntent() != null) {
                        intent.putExtra(
                            Constant.KEY_OPEN_SCREEN,
                            getIntent().getIntExtra(
                                Constant.KEY_OPEN_SCREEN,
                                Constant.KEY_SCREEN_THEME
                            )
                        )
                        if (getIntent().getBundleExtra(Constant.DATA_BUNDLE) != null) intent.putExtra(
                            Constant.DATA_BUNDLE, getIntent().getBundleExtra(Constant.DATA_BUNDLE)
                        )
                    }
                    checkMain = true
                    startActivity(intent)
                    finish()
                }
//            }
//        } catch (e: java.lang.Exception) {
//            Timber.e(e)
//        }
    }

    private fun autoShowPickerKeyboard() {
        if (UncachedInputMethodManagerUtils.isThisImeEnabled(this, App.instance.mImm)) {
            Handler(Looper.getMainLooper()).postDelayed(
                { App.instance.mImm?.showInputMethodPicker() },
                500
            )
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
        this.languageCountry = languageCountry
        if (language == "English") {
            mPrefs.getString(com.tapbi.spark.yokey.common.Constant.PREF_LANGUAGE_DEFAULT_DEVICE, "")
                .apply {
                    this?.let { setLocale(it) }
                }
        } else {
            setLocale(languageCode)
        }
    }

    private fun setLocale(languageCode: String) {
        this.languageCode = languageCode
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.locale = locale
        resources
            .updateConfiguration(configuration, resources.displayMetrics)
    }
}