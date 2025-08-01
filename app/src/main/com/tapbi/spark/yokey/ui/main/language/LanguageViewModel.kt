package com.tapbi.spark.yokey.ui.main.language

import androidx.lifecycle.viewModelScope
import com.tapbi.spark.yokey.common.Constant
import com.tapbi.spark.yokey.data.model.Language
import com.tapbi.spark.yokey.data.repository.LanguageRepository
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor() : BaseViewModel() {
    private var languageRepository = LanguageRepository()
    var stateFlowLanguage = MutableStateFlow(mutableListOf<Language>())
    fun getLanguage() {
        viewModelScope.launch(Dispatchers.IO) {
            val mutableListLanguage = mutableListOf<Language>()
            mutableListLanguage.addAll(
                languageRepository.loadLanguage(
                    Constant.NAME_LANGUAGE_JSON,
                    Constant.NAME_LANGUAGE
                )
            )
            withContext(Dispatchers.Main) {
                stateFlowLanguage.emit(mutableListLanguage)
            }
        }
    }
}