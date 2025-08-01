package com.tapbi.spark.yokey.ui.main.detailtheme

import android.content.Context
import android.content.ContextWrapper
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.common.CommonVariable
import com.tapbi.spark.yokey.common.LiveEvent
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity
import com.tapbi.spark.yokey.data.model.ThemeObject
import com.tapbi.spark.yokey.data.remote.ApiUtils
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.util.Objects
import javax.inject.Inject

@HiltViewModel
class DetailThemeViewModel @Inject constructor() : BaseViewModel() {
    @JvmField
    var mLiveEventDownTheme = LiveEvent<Boolean>()

    var mLiveEventUpdateThemeEntityDB = LiveEvent<Boolean>()

    var mLiveEventUpdateThemeEntity = LiveEvent<Boolean>()



    var fileDownload : String? = null


    fun gotoApplyTheme(themeObject: ThemeObject, themeEntity: ThemeEntity?) {
        val subString = themeObject.urlTheme
        var idFolder = "1" //end.substring(end.indexOf(".zip"));
        idFolder = if (subString != null) {
            val end =
                subString.substring(subString.indexOf(".com") + ".com".length + 1, subString.length)
            if (end.contains("/")) {
                end.substring(0, end.indexOf("/"))
            } else {
                end.substring(0, end.indexOf("."))
            }
        } else {
            themeObject.id.toString()
        }
        instance.mPrefs?.edit()?.putString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, idFolder)?.apply()
        instance.themeRepository?.updateCurrentThemeModel()
        // update themeEntityDB for ads and purchase
        updateThemeEntityDB(idFolder, themeObject, themeEntity)
    }

    fun updateThemeEntityDB(idPath: String, themeObject: ThemeObject, themeEntity : ThemeEntity?) {
        instance.themeRepository!!.updateThemeDB(
            themeObject,
            if(themeEntity != null) themeEntity.isMyTheme else 0
        ).subscribe(object : SingleObserver<Boolean> {
            override fun onSubscribe(d: Disposable) {}
            override fun onSuccess(aBoolean: Boolean) {
                    if (themeObject.id != null) {
                        val contextWrapper = ContextWrapper(instance)
                        val file = contextWrapper.getDir(instance.filesDir.name, Context.MODE_PRIVATE)
                        val strPath = "$file/$idPath/theme.json" //themeModel.getId()
                        // TODO: chungvv update local
                        if (File(strPath).exists() || Objects.requireNonNull<String>(
                                themeObject.id.toString()
                            ).toInt() > 6000 && themeObject.id.toString().toInt() < 7000
                             || Objects.requireNonNull<String>(
                                themeObject.id.toString()
                            ).toInt() > 4012 && themeObject.id.toString().toInt() < 5000

                            || Objects.requireNonNull<String>(
                                themeObject.id.toString()
                            ).toInt() > 3015 && themeObject.id.toString().toInt() < 4000

                            || Objects.requireNonNull<String>(
                                themeObject.id.toString()
                            ).toInt() > 2003 && themeObject.id.toString().toInt() < 3000
                        ) {
                            instance.mPrefs?.edit()?.putString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, idPath)?.apply()
                            instance.themeRepository!!.updateCurrentThemeModel()
                            mLiveEventUpdateThemeEntityDB.postValue(true)
                        } else {
                            mLiveEventUpdateThemeEntityDB.postValue(false)
                        }
                    } else {
                        mLiveEventUpdateThemeEntityDB.postValue(false)
                    }

            }

            override fun onError(e: Throwable) {
                mLiveEventUpdateThemeEntityDB.postValue(false)
            }
        })
    }

    fun downloadZipFileTheme(themeObject: ThemeObject) {
        Timber.e("hachung downloadZipFileTheme: ")
        val themesService = ApiUtils.downloadZipThemesService()
        val subString = themeObject.urlTheme
        val end =
            subString!!.substring(subString.indexOf(".com") + ".com".length + 1, subString.length)
        Timber.d("ducNQdownloadZipFileTheme $end")
        var idFolder = end.substring(end.indexOf(".zip"))
        idFolder = if (end.contains("/")) {
            end.substring(0, end.indexOf("/"))
        } else {
            end.substring(0, end.indexOf("."))
        }
        val call = themesService.downloadFileByUrl(end)
        val finalIdFolder = idFolder
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Timber.e("hachung onResponse: ${response.isSuccessful}")
                if (response.isSuccessful && themeObject.id != null) {
                    instance.themeRepository?.downloadAndSaveTheme(
                            finalIdFolder,
                            response.body()
                        )?.subscribe(object : SingleObserver<Boolean> {
                            override fun onSubscribe(d: Disposable) {}
                            override fun onSuccess(aBoolean: Boolean) {
                                fileDownload = finalIdFolder
                                mLiveEventDownTheme.postValue(aBoolean)
                            }

                            override fun onError(e: Throwable) {
                                mLiveEventDownTheme.postValue(false)
                            }
                        })
                } else {
                    mLiveEventDownTheme.postValue(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                t.printStackTrace()
                Timber.e("hachung onFailure: ")
                mLiveEventDownTheme.postValue(false)
            }
        })
    }

    fun gotoApplyThemeEntity(themeEntity: ThemeEntity) {
        instance.mPrefs?.edit()?.putString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, themeEntity.id)?.apply()
        instance.themeRepository?.updateCurrentThemeModel()
        updateThemeEntity(themeEntity)
    }

    private fun updateThemeEntity(themeEntity: ThemeEntity?) {
        instance.themeRepository?.updateThemeEntity(themeEntity, 1)?.subscribe(object : SingleObserver<Boolean> {
            override fun onSubscribe(d: Disposable) {}
            override fun onSuccess(aBoolean: Boolean) {
                mLiveEventUpdateThemeEntity.postValue(aBoolean)
            }

            override fun onError(e: Throwable) {
                mLiveEventUpdateThemeEntity.postValue(false)
            }
        })
    }

}