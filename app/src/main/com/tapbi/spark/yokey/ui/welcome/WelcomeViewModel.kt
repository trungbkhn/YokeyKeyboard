package com.tapbi.spark.yokey.ui.welcome

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant
import com.tapbi.spark.yokey.data.model.Language
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import com.tapbi.spark.yokey.util.CommonUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WelcomeViewModel @Inject constructor(): BaseViewModel() {
    var stateFlowLanguage = MutableStateFlow(mutableListOf<Language>())
    fun getLanguage() {
        viewModelScope.launch(Dispatchers.IO) {
            val mutableListLanguage = mutableListOf<Language>()
            mutableListLanguage.addAll(
                loadLanguage(
                    Constant.NAME_LANGUAGE_JSON,
                    Constant.NAME_LANGUAGE
                )
            )
            withContext(Dispatchers.Main) {
                stateFlowLanguage.emit(mutableListLanguage)
            }
        }
    }

    private fun loadLanguage(languageName: String, objectsLanguage: String): MutableList<Language> {
        val jsonArray = CommonUtil.getDataAssetLocal(
            App.instance,
            languageName,
            objectsLanguage
        )
        //  Timber.d("ducNQ loadLanguage: "+Thread.currentThread().name)
        val listLanguage: ArrayList<Language> = ArrayList()
        if (jsonArray != null) {
            for (j in 0 until jsonArray.length()) {
                val gson = Gson()
                val key = gson.fromJson(
                    jsonArray[j].toString(),
                    Language::class.java
                )
                listLanguage.add(key)
            }
        }
        return listLanguage
    }
}