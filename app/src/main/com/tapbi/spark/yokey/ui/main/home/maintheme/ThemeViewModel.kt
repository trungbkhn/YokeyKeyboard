package com.tapbi.spark.yokey.ui.main.home.maintheme

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity
import com.tapbi.spark.yokey.data.model.theme.ThemeModel
import com.tapbi.spark.yokey.data.remote.ThemesService
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import com.tapbi.spark.yokey.util.CommonUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor() : BaseViewModel() {

    val liveListMyTheme = MutableLiveData<MutableList<ThemeEntity>>()
    val liveListThemeFeatured = MutableLiveData<MutableList<ThemeModel>>()
    private var themesService: ThemesService? = null



    fun loadMyTheme() {
        App.instance.themeRepository!!.loadAllThemeByIsMyTheme(1)
                .subscribe(object : SingleObserver<MutableList<ThemeEntity>> {
                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)

                    }

                    override fun onSuccess(t: MutableList<ThemeEntity>) {
                        liveListMyTheme.postValue(t)
                    }

                    override fun onError(e: Throwable) {

                    }
                })
    }

    fun deleteTheme(themeEntity: ThemeEntity) {
        App.instance.themeRepository!!.deleteItemMyTheme(themeEntity)
                .subscribe(object : SingleObserver<Boolean> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(t: Boolean) {

                    }

                    override fun onError(e: Throwable) {

                    }
                })
    }


    fun loadThemeFeatured() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val listThemeFeatured: ArrayList<ThemeModel> = ArrayList()
                for (i in 6000 until 6011) {
                    val folder = "key/${i}/theme.json"//"key/6001/theme.json"
                    val jsonArray = CommonUtil.getDataAssetLocal(
                            App.instance,
                            folder,
                            "ThemeFeatured"
                    )
                    if (jsonArray != null) {
                        for (j in 0 until jsonArray.length()) {
                            val gson = Gson()
                            val key = gson.fromJson(
                                    jsonArray[j].toString(),
                                    ThemeModel::class.java
                            )
                            listThemeFeatured.add(key)
                        }
                    }
                }
                liveListThemeFeatured.postValue(listThemeFeatured)
            }
        }
    }


}