package com.tapbi.spark.yokey.ui.main.home.setting

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentSettingMainBinding
import com.android.inputmethod.latin.settings.Settings
import com.google.android.ads.nativetemplates.OnDecorationAds
import com.ironman.trueads.common.Common
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.custom.view.CustomSwitch
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.ui.main.home.setting.control.CustomInputMethodSettingsActivity
import com.tapbi.spark.yokey.ui.main.language.LanguageFragment
import com.tapbi.spark.yokey.util.Constant.HIDE_RATE_APPS
import com.tapbi.spark.yokey.util.MySharePreferences
import company.librate.RateDialog
import java.util.*

class SettingFragment : BaseBindingFragment<FragmentSettingMainBinding, SettingViewModel>(),
    CustomSwitch.OnChangeCheckListener,
    View.OnClickListener, RateDialog.IListenerRate {
    private lateinit var languageFragment: LanguageFragment
    private var isShow = false
    private var rateDialog: RateDialog? = null

    companion object {
        fun newInstance(): SettingFragment {
            return SettingFragment().apply {
                arguments = bundleOf()
            }
        }
    }

    override fun getViewModel(): Class<SettingViewModel> = SettingViewModel::class.java

    override val layoutId: Int
        get() = R.layout.fragment_setting_main

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        initView()
        checkCustomSetting()

        //Timber.d("ducNQ onCreatedView: "+getCurrentLocale(requireContext()).language)
    }

    override fun onPermissionGranted() {

    }
    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.getResources().getConfiguration().getLocales().get(0)
        } else {
            context.getResources().getConfiguration().locale
        }
    }
    override fun onResume() {
        checkCustomSetting()
        hideRateApp()
        super.onResume()
    }

    private fun checkCustomSetting() {
        binding.switchAutoCorrect.setCheck(App.instance.mPrefs!!.getBoolean(Settings.PREF_AUTO_CORRECTION, true))
        binding.switchDoubleSpace.setCheck(
            App.instance.mPrefs!!.getBoolean(
                Settings.PREF_KEY_USE_DOUBLE_SPACE_PERIOD,
                true
            )
        )
        binding.switchShowSuggestions.setCheck(
            App.instance.mPrefs!!.getBoolean(
                Settings.PREF_SHOW_SUGGESTIONS,
                false
            )
        )
        binding.switchPopupKeyPress.setCheck(App.instance.mPrefs!!.getBoolean(Settings.PREF_POPUP_ON, true))
        binding.switchSoundKeyPress.setCheck(App.instance.mPrefs!!.getBoolean(Settings.PREF_SOUND_ON, false))
        binding.switchVibration.setCheck(App.instance.mPrefs!!.getBoolean(Settings.PREF_VIBRATE_ON, true))
        binding.switchAutoCaption.setCheck(App.instance.mPrefs!!.getBoolean(Settings.PREF_AUTO_CAP, true))
        hideRateApp()
        super.onResume()
    }

    private fun initView() {
        binding.vLanguage.setOnClickListener(this)
        binding.vCustomInput.setOnClickListener(this)
        binding.vHelp.setOnClickListener(this)
        binding.vRate.setOnClickListener(this)
        binding.vPolicy.setOnClickListener(this)
        binding.vLanguageApp.setOnClickListener(this)
        binding.switchAutoCaption.setOnChangeCheckListener(this)
        binding.switchDoubleSpace.setOnChangeCheckListener(this)
        binding.switchShowSuggestions.setOnChangeCheckListener(this)
        binding.switchPopupKeyPress.setOnChangeCheckListener(this)
        binding.switchVibration.setOnChangeCheckListener(this)
        binding.switchAutoCorrect.setOnChangeCheckListener(this)
        binding.switchSoundKeyPress.setOnChangeCheckListener(this)
        binding.switchSoundKeyPress.setOnChangeCheckListener(this)
    }

    @SuppressLint("CommitPrefEdits")
    override fun isCheck(customSwitch: CustomSwitch?, isChecked: Boolean) {
        when (customSwitch!!.id) {
            R.id.switch_auto_correct -> {
                App.instance.mPrefs!!.edit()?.putBoolean(Settings.PREF_AUTO_CORRECTION, isChecked)?.apply()
                binding.switchAutoCorrect.setCheck(isChecked)
            }
            R.id.switch_double_space -> {
                App.instance.mPrefs!!.edit()?.putBoolean(Settings.PREF_KEY_USE_DOUBLE_SPACE_PERIOD, isChecked)
                    ?.apply()
                binding.switchDoubleSpace.setCheck(isChecked)
            }
            R.id.switch_show_suggestions -> {
                App.instance.mPrefs!!.edit()?.putBoolean(Settings.PREF_SHOW_SUGGESTIONS, isChecked)?.apply()
                binding.switchShowSuggestions.setCheck(isChecked)
            }
            R.id.switch_popup_key_press -> {
                App.instance.mPrefs!!.edit()?.putBoolean(Settings.PREF_POPUP_ON, isChecked)?.apply()
                binding.switchPopupKeyPress.setCheck(isChecked)
            }
            R.id.switch_sound_key_press -> {
                App.instance.mPrefs!!.edit()?.putBoolean(Settings.PREF_SOUND_ON, isChecked)?.apply()
                binding.switchSoundKeyPress.setCheck(isChecked)
            }
            R.id.switch_vibration -> {
                App.instance.mPrefs!!.edit()?.putBoolean(Settings.PREF_VIBRATE_ON, isChecked)?.apply()
                binding.switchVibration.setCheck(isChecked)
            }
            R.id.switch_auto_caption -> {
                App.instance.mPrefs!!.edit()?.putBoolean(Settings.PREF_AUTO_CAP, isChecked)?.apply()
                binding.switchAutoCaption.setCheck(isChecked)
            }
        }
    }

    override fun onClick(v: View?) {
        if (!(requireActivity() as MainActivity).isEnable) {
            if (activity is MainActivity) (activity as MainActivity?)!!.enableClick()
            when (v!!.id) {
                R.id.v_language -> {
//                    CommonUtil.invokeSubtypeEnablerOfThisIme(requireContext())
                    (activity as MainActivity).changeStartScreen(R.id.keyboardLanguageFragment, null)
                }
                R.id.v_custom_input -> {
                    startActivity(Intent(context, CustomInputMethodSettingsActivity::class.java))
                }
                R.id.v_help -> sendFeedback(requireContext())
                R.id.v_rate -> {
                    rateDialog = RateDialog(
                        activity,
                        resources.getString(R.string.mail_feedback_rgb_keyboard),
                        false, this
                    )
                    rateDialog!!.show()
                }
                R.id.v_policy -> {
                   (activity as MainActivity).changeStartScreen(R.id.policyFragment,null)
                    //CommonUtil.policy(requireContext())
                }
                R.id.vLanguageApp -> {
                    setUpLanguageFragment()
                }
            }
        }
    }

    private fun setUpLanguageFragment() {
        languageFragment = LanguageFragment()
        if (parentFragmentManager.findFragmentByTag("languageFragment") == null)
        {
            languageFragment.enterTransition
            languageFragment.show(parentFragmentManager, "languageFragment")
        }
    }

    private fun callFeedback() {
        try {
            val pInfo: PackageInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionAppRGB = pInfo.versionName
            val packageManager = requireContext().packageManager
            val applicationInfo: ApplicationInfo? = try {
                packageManager.getApplicationInfo(requireContext().packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            val applicationName =
                (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "(unknown)") as String
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(
                    Intent.EXTRA_EMAIL,
                    arrayOf(resources.getString(R.string.mail_feedback_rgb_keyboard))
                )
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Feedback & Help $applicationName, Version: $versionAppRGB"
                )
                putExtra(Intent.EXTRA_TEXT, "")
                startActivity(Intent.createChooser(this, "Send mail for help!"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun sendFeedback(context: Context) {
        try {
            val pInfo: PackageInfo
            pInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_CONFIGURATIONS
            )
            val versionAppRGB = pInfo.versionName
            val packageManager = context.packageManager
            val applicationInfo: ApplicationInfo?
            applicationInfo = try {
                packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_CONFIGURATIONS
                )
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            val applicationName =
                (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "(unknown)") as String
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@gmail.com"))
            emailIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "App Report:" + applicationName + "- version:" + versionAppRGB + "-" + Build.MODEL + "-Android:" + Build.VERSION.SDK_INT
            )
            emailIntent.putExtra(Intent.EXTRA_TEXT, "")
            context.startActivity(Intent.createChooser(emailIntent, "Send mail for help!"))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    private fun delayShow() {
        isShow = true
        Handler(Looper.getMainLooper()).postDelayed({ isShow = false }, 2000)
    }

    override fun stateRate() {
        MySharePreferences.putBoolean(HIDE_RATE_APPS, true, requireContext())
        hideRateApp()
    }

    override fun resetCurrentPager() {

    }

    private fun hideRateApp() {
        if (MySharePreferences.getBooleanValue(HIDE_RATE_APPS, requireContext())) {
            binding.btnRate.visibility = View.GONE
            binding.vLineRateApp.visibility = View.GONE
            binding.vRate.visibility = View.GONE
        }
    }

    override fun processRemoveAds(isRemoveAds: Boolean) {
        super.processRemoveAds(isRemoveAds)
        val mapIds = Common.getMapIdAdmobApplovin(requireActivity(),R.array.admob_native_id_setting,R.array.applovin_native_id_setting)
        showAdsNative(binding.frAdsNative,mapIds,object :OnDecorationAds{
            override fun onDecoration(network: String?) {
                binding.frAdsNative.getNativeAdView(network)?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.bg_ads_setting
                    )
                )
            }

        })
    }

}