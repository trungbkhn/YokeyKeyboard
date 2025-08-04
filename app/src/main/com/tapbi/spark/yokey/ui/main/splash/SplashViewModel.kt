package com.tapbi.spark.yokey.ui.main.splash

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor() : BaseViewModel() {

    val liveOpenAds : MutableLiveData<Boolean> = MutableLiveData()


        @SuppressLint("ResourceType")
        fun checkUpdateThemeSticker(context: Context){
            viewModelScope.launch(Dispatchers.IO) {
//                FirebaseRemoteConfig.getInstance().setDefaultsAsync(R.xml.remote_config_defaults)
                val lastStickerVersion =
                    FirebaseRemoteConfig.getInstance().getLong(Constant.STICKER_LAST_VERSION)
                Log.d("duongcv", "checkUpdateThemeSticker: " + lastStickerVersion)
                if (lastStickerVersion > (App.instance.mPrefs?.getInt(
                        Constant.STICKER_LAST_VERSION,
                        1
                    ) ?: 1)
                ) {
                    App.instance.stickerRepository?.checkUpdateSticker(
                        App.instance.mPrefs?.getInt(
                            Constant.STICKER_LAST_VERSION, 1) ?: 1, context)
                }

                val lastThemeVersion =
                    FirebaseRemoteConfig.getInstance().getLong(Constant.THEME_LAST_VERSION)
                Log.d("duongcv", "checkUpdateThemeSticker: " + lastThemeVersion)
                if (lastThemeVersion > (App.instance.mPrefs?.getInt(Constant.THEME_LAST_VERSION, 1)
                        ?: 1)
                ) {
                    App.instance.themeRepository?.checkUpdateTheme(
                        App.instance.mPrefs?.getInt(
                            Constant.THEME_LAST_VERSION,
                            1
                        ) ?: 1
                    )
                }

                val lastBackgroundVersion =
                    FirebaseRemoteConfig.getInstance().getLong(Constant.BACKGROUND_LAST_VERSION)
                if (lastBackgroundVersion > (App.instance.mPrefs?.getInt(
                        Constant.BACKGROUND_LAST_VERSION,
                        0
                    ) ?: 0)
                ) {
                    App.instance.mPrefs?.edit()?.putBoolean(Constant.SAVE_BACKGROUND, false)
                        ?.apply()
                    App.instance.mPrefs?.edit()
                        ?.putInt(Constant.BACKGROUND_LAST_VERSION, lastBackgroundVersion.toInt())
                        ?.apply()
                }
            }
        }

}