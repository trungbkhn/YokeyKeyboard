package com.tapbi.spark.yokey.data.repository

import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant
import com.tapbi.spark.yokey.data.model.Language
import com.tapbi.spark.yokey.util.CommonUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.Callable
import javax.inject.Inject

class LanguageRepository  @Inject constructor(){

    fun loadLanguage(languageName: String, objectsLanguage: String): MutableList<Language> {
        val jsonArray = CommonUtil.getDataAssetLocal(
            App.instance,
            languageName,
            objectsLanguage
        )
        val listLanguage: MutableList<Language> = mutableListOf()
        if (jsonArray != null) {
            for (j in 0 until jsonArray.length()) {
                val language = jsonArray.getJSONObject(j).get("language").toString()
                val country = jsonArray.getJSONObject(j).get("country").toString()
                val languageCode = jsonArray.getJSONObject(j).get("languageCode").toString()

                listLanguage.add(Language(country, language, languageCode))
            }
        }
        return listLanguage
    }

//    private fun getLanguageCurrent(): Single<MutableList<Language>> {
//        return Single.fromCallable(object : Callable<MutableList<Language>> {
//            override fun call(): MutableList<Language> {
//                val listLanguage = loadLanguage(
//                    Constant.NAME_LANGUAGE_JSON,
//                    Constant.NAME_LANGUAGE
//                )
//                Timber.d("ducNQ getLanguageCurrent local: "+Locale.getDefault().language)
//                for (i in 0 until listLanguage.size) {
//                    Timber.d("ducNQ getLanguageCurrent: "+listLanguage[i].languageCode)
//                    if (Locale.getDefault().language == listLanguage[i].languageCode) {
//                        App.instance.languageCurPs = i
//                        App.instance.mPrefs?.edit()
//                            ?.putInt(Constant.PREF_POSITION_LANGUAGE_CURRENT,i)
//                            ?.apply()
//                        return listLanguage[i].languageCode
//                    }
//                }
//                return "en"
//            }
//        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//    }
  fun getLanguageCurrent():Single<MutableList<Language>>{
      return Single.fromCallable(object : Callable<MutableList<Language>>{
          override fun call(): MutableList<Language> {
              return loadLanguage( Constant.NAME_LANGUAGE_JSON,
                  Constant.NAME_LANGUAGE)
          }
      }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
  }
    fun getLanguage() {
        getLanguageCurrent().subscribe(object : SingleObserver<MutableList<Language>> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: MutableList<Language>) {
                App.instance.languageCurList = t
            }

            override fun onError(e: Throwable) {

            }
        })
    }
}