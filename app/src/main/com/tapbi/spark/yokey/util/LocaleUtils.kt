package com.tapbi.spark.yokey.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.preference.PreferenceManager
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant
import com.tapbi.spark.yokey.common.Constant.PREF_CHECK_SET_LANGUAGE_DEFAULT
import com.tapbi.spark.yokey.common.Constant.PREF_LANGUAGE_DEFAULT_DEVICE
import com.tapbi.spark.yokey.data.local.SharedPreferenceHelper
import com.tapbi.spark.yokey.util.Utils.isAtLeastSdkVersion
import timber.log.Timber
import java.util.*

object LocaleUtils {
    @SuppressLint("CommitPrefEdits")
    fun applyLocale(context: Context) {
        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        val language = sharedPreferences.getString(Constant.PREF_SETTING_LANGUAGE, Locale.getDefault().language)
        Timber.d("ducNQ applyLocale: "+language)
        val locale = language?.let { Locale(it) }
        if (locale != null) {
            updateResource(context, locale)
        }
        if (context != context.applicationContext) {
            if (locale != null) {
                updateResource(context.applicationContext, locale)
            }
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

    //    fun applyLocale(context: Context) {
//        val preferences = PreferenceManager
//            .getDefaultSharedPreferences(context)
//        var localeString = preferences.getString(Constant.PREF_SETTING_LANGUAGE, Constant.LANGUAGE_EN)
//        val locale: Locale = context.resources.configuration.locale
//        if (TextUtils.isEmpty(localeString)) {
//            localeString = Constant.LANGUAGE_EN
//        }
//        // Timber.e("applyLocale $localeString")
//        val newLocale = Locale(localeString)
//        updateResource(context, locale)
//        if (context !== context.applicationContext) {
//            updateResource(context.applicationContext, locale)
//        }
//    }
    @SuppressLint("CommitPrefEdits")
    fun applyLocales(context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(PREF_CHECK_SET_LANGUAGE_DEFAULT, true)) {
            sharedPreferences.edit()
                .putString(PREF_LANGUAGE_DEFAULT_DEVICE, Locale.getDefault().language).apply()
            sharedPreferences.edit()
                .putString(Constant.PREF_LANGUAGE_CURRENT, Locale.getDefault().language).apply()
            sharedPreferences.edit().putBoolean(PREF_CHECK_SET_LANGUAGE_DEFAULT, false).apply()
        }
        val language = sharedPreferences.getString(
            Constant.PREF_SETTING_LANGUAGE,
            Locale.getDefault().language
        )
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.locale = locale
        context.resources
            .updateConfiguration(configuration, context.resources.displayMetrics)
    }

    private fun updateResource(context: Context, locale: Locale) {
        Locale.setDefault(locale)
        val resources = context.resources
        val current = getLocaleCompat(resources)
        if (current == locale) {
            return
        }
        val configuration = Configuration(resources.configuration)
        when {
            isAtLeastSdkVersion(Build.VERSION_CODES.N) -> {
                configuration.setLocale(locale)
            }
            isAtLeastSdkVersion(Build.VERSION_CODES.JELLY_BEAN_MR1) -> {
                configuration.setLocale(locale)
            }
            else -> {
                configuration.locale = locale
            }
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            context.createConfigurationContext(configuration)
//        } else {
//            resources.updateConfiguration(configuration, resources.displayMetrics)
//        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun getLocaleCompat(resources: Resources): Locale {
        val configuration = resources.configuration
        return if (isAtLeastSdkVersion(Build.VERSION_CODES.N)) configuration.locales[0] else configuration.locale
    }

    fun applyLocaleAndRestart(activity: Activity, localeString: String) {
        //  Timber.e("applyLocaleAndRestart $localeString")
        SharedPreferenceHelper.storeString(Constant.PREF_SETTING_LANGUAGE, localeString)
        applyLocale(activity)
    }


}