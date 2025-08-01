package com.tapbi.spark.yokey.ui.main.home.kblanguage

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.local.LanguageEntity
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class KeyboardLanguageViewModel @Inject constructor() : BaseViewModel() {

    var listLanguageEntityLiveDataOther = MutableLiveData<ArrayList<LanguageEntity>>()

    fun getAllKeyboardLanguage(context: Context, checkUse: Boolean) {

//        App.instance.keyboardLanguageRepository!!.allLanguageLocal.subscribe(object : SingleObserver<ArrayList<LanguageEntity>>{
//            override fun onSubscribe(d: Disposable) {
//
//            }
//
//            override fun onError(e: Throwable) {
//
//            }
//
//            override fun onSuccess(t: ArrayList<LanguageEntity>) {
//                listLanguageEntityLiveDataOther.postValue(t)
//            }
//        })

        getAllKeyboarLanguageThread(context, checkUse).subscribe(object :
            SingleObserver<ArrayList<LanguageEntity>> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onError(e: Throwable) {
            }

            override fun onSuccess(t: ArrayList<LanguageEntity>) {
                listLanguageEntityLiveDataOther.postValue(t)
            }
        })

    }

    fun updateLanguage(isEnabled: Boolean, id: Int) {
        App.instance.keyboardLanguageRepository?.updateLanguage(isEnabled, id)
            ?.subscribe(object : SingleObserver<Boolean> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onSuccess(t: Boolean) {

                }

                override fun onError(e: Throwable) {

                }
            })
    }

    fun getAllKeyboarLanguageThread(
        context: Context,
        checkUse: Boolean
    ): Single<ArrayList<LanguageEntity>> {
        return Single.zip<List<LanguageEntity>, ArrayList<LanguageEntity>, ArrayList<LanguageEntity>>(
            App.instance.keyboardLanguageRepository!!.getAllLanguageDb(true),
            App.instance.keyboardLanguageRepository!!.allLanguageLocal
        ) { languageEntitiesDb: List<LanguageEntity>, languageEntitiesLocal: ArrayList<LanguageEntity> ->
            if (App.instance.mPrefs?.getBoolean(Constant.IS_USE_SYSTEM_LANGUAGE, true) ?: false) {
                CommonUtil.setEnableDefaultSystem(languageEntitiesLocal)
            } else {
                for (i in languageEntitiesLocal.indices) {
                    val languageEntity = languageEntitiesLocal[i]
                    if (languageEntitiesDb.isNotEmpty()) {
                        for (languageDBEntity in languageEntitiesDb) {
                            if (languageDBEntity.locale.equals(languageEntity.locale) && languageDBEntity.name.equals(languageEntity.name)) {
                                languageEntitiesLocal[i].isEnabled = true
                            }
                        }
                    } else {
                        var localeLanguage = Utils.getLocaleStringResource(context)
                        if (!languageEntitiesLocal[i].locale.contains("_")) {
                            localeLanguage = Locale.getDefault().language
                        }
                        if (languageEntitiesLocal[i].locale.equals(localeLanguage) && !checkUse) {
                            languageEntitiesLocal[i].isEnabled = true
                        }
                    }
                }
                for (i in 0 until languageEntitiesLocal.size - 1) {
                    for (j in i + 1 until languageEntitiesLocal.size) {
                        if (!languageEntitiesLocal[i].isEnabled && languageEntitiesLocal[j].isEnabled) {
                            val languageEntity = languageEntitiesLocal[j]
                            languageEntitiesLocal[j] = languageEntitiesLocal[i]
                            languageEntitiesLocal[i] = languageEntity
                            break
                        }
                    }
                }
            }
            languageEntitiesLocal
        }.subscribeOn(Schedulers.io())
    }

}