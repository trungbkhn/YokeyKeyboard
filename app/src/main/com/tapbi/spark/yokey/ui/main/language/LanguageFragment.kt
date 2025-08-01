package com.tapbi.spark.yokey.ui.main.language


import android.annotation.TargetApi
import android.app.Dialog
import android.content.*
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
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
import com.tapbi.spark.yokey.common.Constant.PREF_LANGUAGE_CURRENT
import com.tapbi.spark.yokey.common.Constant.PREF_POSITION_LANGUAGE_CURRENT
import com.tapbi.spark.yokey.ui.adapter.LanguageAdapter
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.util.LocaleUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.*

class LanguageFragment : DialogFragment(), LanguageAdapter.IClickLanguage {
    private lateinit var languageAdapter: LanguageAdapter
    private lateinit var binding: LayoutLanguageBinding
    private val viewModel: LanguageViewModel by activityViewModels()
    private var languageCode: String = ""
    private var languageCountry: String = "English"
    private var positionLanguageCurrent: Int = 0
    private var sizeListCurrent: Int = 0
    private lateinit var sharedPreferences: SharedPreferences
    private var isPro =false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutLanguageBinding.inflate(inflater)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.layoutLoading.visibility = View.VISIBLE
        return binding.root//super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun getTheme(): Int {
        return R.style.Language
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawableResource(R.color.color_transparent)
        //   dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog.window!!.setLayout(width, height)
        return dialog
    }

    override fun onStart() {
//        val dialog = dialog
//        if (dialog != null) {
//            val width = ViewGroup.LayoutParams.MATCH_PARENT
//            val height = ViewGroup.LayoutParams.MATCH_PARENT
//            dialog.window!!.setLayout(width, height)
//            Timber.d("ducNQ onCreateDialogss: ")
//        }
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
//        getLocaleChangedFlow()
//            .onEach {
//                Log.d("ducNQ", "onResumed: "+it.language)
//            }.launchIn(CoroutineScope(Dispatchers.Main))
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.action != KeyEvent.ACTION_DOWN) {
                    dismiss()
                }
                return@setOnKeyListener true
            } else return@setOnKeyListener false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAdapter()
        setUpDataObserver()
        setUpClick()
        isPro = App.instance.billingManager!!.isPremium
        initAds()
    }
    private fun getLocaleChangedFlow() = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                if (intent.action == Intent.ACTION_LOCALE_CHANGED) {
                    val locale = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
                    {
                        requireContext().resources.configuration.locales.get(0)
                    }else{
                        requireContext().resources.configuration.locale
                    }
                    if (locale != null) {
                        trySend(locale)
                    }
                }
            }
        }
        requireContext().registerReceiver(receiver, IntentFilter(Intent.ACTION_LOCALE_CHANGED))

        awaitClose {
            requireContext().unregisterReceiver(receiver)
        }
    }

    private fun setUpDataObserver() {
        viewModel.getLanguage()
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.stateFlowLanguage.collect {
                languageAdapter.changeDataLanguage(it)
            }
        }

        positionLanguageCurrent = sharedPreferences.getInt(PREF_POSITION_LANGUAGE_CURRENT, 0)
        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded && !(requireActivity() as MainActivity).isFinishing) {
                (binding.rcAppLanguage.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                    positionLanguageCurrent,
                    0
                )
            }
            binding.layoutLoading.visibility = View.INVISIBLE
        }, 600)

    }

    private fun setUpClick() {
        languageCode = sharedPreferences.getString(PREF_LANGUAGE_CURRENT, "en")!!
        binding.imgDoneLanguage.setOnClickListener {
            if (languageCode == sharedPreferences.getString(PREF_LANGUAGE_CURRENT, "en")) {
                dismiss()
            } else {
                restartApp()
                sharedPreferences.edit().putString(Constant.PREF_SETTING_LANGUAGE, languageCode)
                    .apply()
                sharedPreferences.edit().putString(PREF_LANGUAGE_CURRENT, languageCode).apply()
                sharedPreferences.edit()
                    .putInt(PREF_POSITION_LANGUAGE_CURRENT, positionLanguageCurrent)
                    .apply()
                LocaleUtils.applyLocale(requireContext())
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    //
    private fun setUpAdapter() {
        languageAdapter = LanguageAdapter(this, requireContext())
        binding.rcAppLanguage.adapter = languageAdapter
        binding.rcAppLanguage.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
    }

    override fun clickLanguageCurrent(
        languageCode: String,
        language: String,
        position: Int,
        sizeList: Int,
        languageCountry: String
    ) {
        positionLanguageCurrent = position
        sizeListCurrent = sizeList
        this.languageCountry = languageCountry
        this.languageCode = languageCode
    }

    private fun restartApp() {
        App.instance.mInputView = null
        (requireActivity() as MainActivity).finish()
        startActivity(Intent(context, MainActivity::class.java))
    }

    private fun getSystemLocaleLegacy(config: Configuration): Locale? {
        return config.locale
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun getSystemLocale(config: Configuration): Locale? {
        return config.locales[0]
    }

    private fun setSystemLocaleLegacy(config: Configuration, locale: Locale?) {
        config.locale = locale
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun setSystemLocale(config: Configuration, locale: Locale?) {
        config.setLocale(locale)
    }
    private fun initAds() {
        if (isPro) {
            binding.frAds.visibility=View.GONE
            return
        }
        binding.frAds.visibility = View.VISIBLE
        //show ads native
        val mapIds= Common.getMapIdAdmobApplovin(requireActivity(),R.array.admob_native_id_language,R.array.applovin_native_id_language)
        MultiAdsControl.showNativeAdNoMedia(activity as AppCompatActivity,
            mapIds,
            binding.frAds,
            null, null, object: OnDecorationAds{
                override fun onDecoration(network: String?) {
                    binding.frAds?.getNativeAdView(network)?.setBackgroundColor(ContextCompat.getColor(requireActivity(),R.color.bg_ads_language))

                }

            })
    }

//    override fun onBackPressed() {
//        AppOpenAdAdmob.getInstance(application).enableShowOpenForeground = false
//        App.getInstance().delayEnableAdOpenForeground()
//        super.onBackPressed()
//    }
}