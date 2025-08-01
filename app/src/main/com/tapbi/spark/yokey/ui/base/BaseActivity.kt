package com.tapbi.spark.yokey.ui.base

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.tapbi.spark.yokey.R
import com.ironman.trueads.common.Common
import com.ironman.trueads.multiads.MultiAdsControl
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.util.LocaleUtils
import dagger.hilt.android.AndroidEntryPoint
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.util.Constant


@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleUtils.applyLocale(this)
        if(App.instance!!.billingManager!!.isPremium ){
            MultiAdsControl.enableShowAdsOpenForeground(App.instance,false)
        }else{
            if(this !is MainActivity){
                autoLoadsAds()
            }

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun attachBaseContext(newBase: Context) {
        LocaleUtils.applyLocale(newBase)
        super.attachBaseContext(newBase)
    }

    open fun changeViewFull(isDark: Boolean) {
        val window = window
        showHideSystemUI(isDark)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    open fun showHideSystemUI(isDark: Boolean) {
        var systemUiVisibility = 0
        val winParams = window.attributes
        window.decorView.fitsSystemWindows = false
    }
    fun autoLoadsAds(){
        if(!App.instance!!.billingManager!!.isPremium ){
            if (!App.instance.mPrefs!!.getBoolean(Constant.CHECK_SELECT_THEME_DEFAULT, false)) {
                autoLoadsNativeInternal(listOf(getString(R.string.tag_native_select_theme)),R.array.admob_native_id_onboard,R.array.applovin_native_id_onboard)
            }
            autoLoadsNativeInternal(listOf(getString(R.string.tag_native_language)),R.array.admob_native_id_language,R.array.applovin_native_id_language)
            autoLoadsNativeInternal(listOf(getString(R.string.tag_native_detail_sticker)),R.array.admob_native_id_detail_sticker,R.array.applovin_native_id_detail_sticker)
            autoLoadsNativeInternal(listOf(getString(R.string.tag_native_detail_theme)),R.array.admob_native_id_detail_theme,R.array.applovin_native_id_detail_theme)
            autoLoadsNativeInternal(listOf(getString(R.string.tag_native_detail_font)),R.array.admob_native_id_detail_font,R.array.applovin_native_id_detail_font)
            autoLoadsNativeInternal(listOf(getString(R.string.tag_native_active_keyboard)),R.array.admob_native_id_active_keyboard,R.array.applovin_native_id_active_keyboard)
            autoLoadsNativeInternal(listOf(getString(R.string.tag_native_try_keyboard)),R.array.admob_native_id_try_keyboard,R.array.applovin_native_id_try_keyboard)
            autoLoadsNativeInternal(listOf(getString(R.string.tag_native_exit_app)),R.array.admob_native_id_exit_app,R.array.applovin_native_id_exit_app)
            autoLoadsNativeInternal(listOf(getString(R.string.tag_native_setting)),R.array.admob_native_id_setting,R.array.applovin_native_id_setting)
            MultiAdsControl.loadAdsInterstitialDetectInternet(applicationContext,this)
        }
    }
    fun autoLoadsNativeInternal(tags:List<String>, idAdmob: Int, idApplovin: Int){
        val mapId = Common.getMapIdAdmobApplovin(this,idAdmob,idApplovin)
        MultiAdsControl.loadAdsNativeDetectInternet(this,tags,mapId)
    }




}