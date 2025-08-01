package com.tapbi.spark.yokey.ui.main.customize

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tapbi.spark.yokey.R
import com.google.gson.Gson
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.common.LiveEvent
import com.tapbi.spark.yokey.data.local.db.ThemeDB
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity
import com.tapbi.spark.yokey.data.model.*
import com.tapbi.spark.yokey.data.model.theme.ThemeModel
import com.tapbi.spark.yokey.data.remote.ApiUtils
import com.tapbi.spark.objects.Background
import com.tapbi.spark.objects.BackgroundList
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.data.model.Effect
import com.tapbi.spark.yokey.data.model.PaginationUpdate
import com.tapbi.spark.yokey.data.model.PopularButton
import com.tapbi.spark.yokey.data.model.SimpleButton
import com.tapbi.spark.yokey.data.model.Sound
import com.tapbi.spark.yokey.data.model.ThemeObject
import com.tapbi.spark.yokey.util.Constant

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class CreateThemeViewModel @Inject constructor() : BaseViewModel() {

    private val api = ApiUtils.getBackground()
    val liveDataListBackground = MutableLiveData<ArrayList<BackgroundList>>()
    val liveDataListColor = MutableLiveData<ArrayList<String>>()
    val liveDataListPopular = MutableLiveData<ArrayList<PopularButton>>()
    val liveDataListEffect = MutableLiveData<ArrayList<Effect>>()
    val liveDataListSound = MutableLiveData<ArrayList<Sound>>()
    val liveDataResultSaveTheme = LiveEvent<Boolean>()
    val liveDataListSimpleButton = MutableLiveData<ArrayList<SimpleButton>>()
    val liveDataListMyTheme = MutableLiveData<MutableList<ThemeEntity>>()
    val liveDataNextScreen = LiveEvent<Boolean>()
    val liveDataNameKeyboard = MutableLiveData<String>()


    private suspend fun loadBackgroundFromServer(): BackgroundList {
        if (App.instance.mPrefs?.getBoolean(com.tapbi.spark.yokey.common.Constant.SAVE_BACKGROUND, false) ?: false) {
            Log.d("duongcv", "loadBackgroundFromServer: off")
            var backgroundList = BackgroundList()
            backgroundList.backgroundJson = ThemeDB.getInstance(App.instance)?.backgroundDAO()?.allBackground as java.util.ArrayList<Background>
            backgroundList.title = "Hot"
            return backgroundList
        }else {
            Log.d("duongcv", "loadBackgroundFromServer: server")
            if (instance.connectivityStatus != -1) {
                val lastVersion = App.instance.mPrefs?.getInt(com.tapbi.spark.yokey.common.Constant.BACKGROUND_LAST_VERSION, 0) ?: 0
                val response = api.getBackground(PaginationUpdate(lastVersion))
                if (response != null) {
                    response.title = App.instance.resources.getString(R.string.txt_name_hot_theme)
                    if (lastVersion < response.lastVersion) {
                        App.instance.mPrefs?.edit()
                            ?.putInt(com.tapbi.spark.yokey.common.Constant.BACKGROUND_LAST_VERSION, response.lastVersion)
                            ?.apply()
                    }
                    if (response.backgroundJson.size > 0) {
                        addBackgroundToDB(response.backgroundJson)
                    }
                    return response
                }
            }
            return BackgroundList();
        }
    }

    private suspend fun addBackgroundToDB(backgroundDB : ArrayList<Background>){
        coroutineScope.launch {
            ThemeDB.getInstance(App.instance)?.backgroundDAO()?.insertBackgroundList(backgroundDB)
            App.instance.mPrefs?.edit()?.putBoolean(com.tapbi.spark.yokey.common.Constant.SAVE_BACKGROUND, true)?.apply()
        }
    }

    private fun loadListBackgroundGradient(): BackgroundList {
        val lb: ArrayList<String> = CommonUtil.loadListImageFromFolderAsset(
            App.instance,
            "background_gradient"
        )
        val listBackground = ArrayList<Background>()
        if (lb.size > 0) {
            for (i in 0 until lb.size) {
                listBackground.add(Background(lb[i], lb[i], 1, 0))
            }
        }
        return BackgroundList(
            App.instance.resources.getString(R.string.txt_name_color_theme),
            listBackground
        )
    }

    private fun loadImageDisconnect(path: String): BackgroundList {
        val listBg = App.instance.symbolsReposition!!.loadAllImageDownloaded(path)
        return if (listBg != null) {
            BackgroundList(
                instance.resources.getString(R.string.txt_name_hot_theme),
                listBg as java.util.ArrayList<Background>
            )
        } else BackgroundList()
    }


    fun loadDataBackground(path: String = instance.appDir.toString() + Constant.PATH_FILE_DOWNLOADED_BACKGROUND) {
        val process1 = viewModelScope.async(Dispatchers.IO) { loadBackgroundFromServer() }
        val process2 = viewModelScope.async(Dispatchers.IO) { loadListBackgroundGradient() }
        val process3 = viewModelScope.async(Dispatchers.IO) { loadImageDisconnect(path) }
        viewModelScope.launch(CoroutineExceptionHandler { _, _ ->
            run {
                val backgroundList = loadListBackgroundGradient()
                val backgroundListErrorNetWork = loadImageDisconnect(path)
                val listBackgroundList = ArrayList<BackgroundList>()
                listBackgroundList.add(backgroundListErrorNetWork)
                listBackgroundList.add(backgroundList)
                liveDataListBackground.postValue(listBackgroundList)
            }
        }) {
            if (App.instance.connectivityStatus != -1 || (App.instance.mPrefs?.getBoolean(com.tapbi.spark.yokey.common.Constant.SAVE_BACKGROUND, false) ?: false)) {
                val result = awaitAll(process1, process2)
                liveDataListBackground.postValue(result as ArrayList<BackgroundList>?)
            } else {
                loadBackgroundWhenErrorInternet(process2, process3)
            }
        }
    }

    private suspend fun loadBackgroundWhenErrorInternet(
        process2: Deferred<BackgroundList>,
        process3: Deferred<BackgroundList>
    ) {
        val result = awaitAll(process3, process2)//process2.await()
        // val listBackgroundList = ArrayList<BackgroundList>() )
        //  listBackgroundList.add(result)
        // liveDataListBackground.postValue(listBackgroundList)
        liveDataListBackground.postValue(result as ArrayList<BackgroundList>?)
    }


    fun loadListColorThread() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val listColor = loadListColor()
                liveDataListColor.postValue(listColor)
            }
        }
    }

    private fun loadListColor(): ArrayList<String> {
        val listColor = ArrayList<String>()
        listColor.add("#ffffff")
        listColor.add("#000000")
        listColor.add("#f7a325")
        listColor.add("#454D66")
        listColor.add("#122C91")
        listColor.add("#F9C46B")
        listColor.add("#E74645")
        listColor.add("#FF7B54")
        listColor.add("#309975")
        listColor.add("#FDFA66")
        listColor.add("#D527B7")
        return listColor
    }

    fun loadListPopular() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val jsonArray = CommonUtil.getDataAssetLocal(
                    App.instance,
                    com.tapbi.spark.yokey.common.Constant.NAME_FILE_ASSET_POPULAR_JSON,
                    com.tapbi.spark.yokey.common.Constant.NAME_OBJECT_POPULAR_JSON
                )
                if (jsonArray != null) {
                    val listPopularButton: ArrayList<PopularButton> = ArrayList()
                    for (j in 0 until jsonArray.length()) {
                        val gson = Gson()
                        val key = gson.fromJson(
                            jsonArray[j].toString(),
                            PopularButton::class.java
                        )
                        listPopularButton.add(key)
                    }
                    liveDataListPopular.postValue(listPopularButton)
                }
            }
        }
    }

    fun loadListEffect() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val jsonArray = CommonUtil.getDataAssetLocal(
                    App.instance,
                    com.tapbi.spark.yokey.common.Constant.NAME_FILE_ASSET_EFFECT_JSON,
                    com.tapbi.spark.yokey.common.Constant.NAME_OBJECT_EFFECT_JSON
                )
                if (jsonArray != null) {
                    val listEffect: ArrayList<Effect> = ArrayList()
                    for (j in 0 until jsonArray.length()) {
                        val gson = Gson()
                        val key = gson.fromJson(
                            jsonArray[j].toString(),
                            Effect::class.java
                        )
                        listEffect.add(key)
                    }
                    listEffect.add(
                        0, Effect(
                            Constant.ID_NONE,
                            Constant.ID_NONE,
                            Constant.ID_NONE,
                            Constant.ID_NONE
                        )
                    )
                    liveDataListEffect.postValue(listEffect)
                }
            }
        }
    }

    fun loadListSound() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val jsonArray = CommonUtil.getDataAssetLocal(
                    App.instance,
                    com.tapbi.spark.yokey.common.Constant.NAME_FILE_ASSER_SOUND_JSON,
                    com.tapbi.spark.yokey.common.Constant.NAME_OBJECT_SOUND_JSON
                )
                if (jsonArray != null) {
                    val listSound: ArrayList<Sound> = ArrayList()
                    for (j in 0 until jsonArray.length()) {
                        val gson = Gson()
                        val key = gson.fromJson(
                            jsonArray[j].toString(),
                            Sound::class.java
                        )
                        listSound.add(key)
                    }
                    listSound.add(
                        0,
                        Sound(
                            Constant.AUDIO_DEFAULT,
                            Constant.AUDIO_DEFAULT,
                            Constant.AUDIO_DEFAULT,
                            0
                        )
                    )
                    liveDataListSound.postValue(listSound)
                }
            }
        }
    }

    fun createTheme(themeModel: ThemeModel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val isCreateResult =
                    App.instance.themeRepository!!.createThemeDataFolder(themeModel)
                if (isCreateResult) {
                    saveThemeDB(themeModel)
                } else {
                    liveDataResultSaveTheme.postValue(false)
                }
            }
        }
    }

    private fun saveThemeDB(themeModel: ThemeModel) {
        val themeObject: ThemeObject = ThemeObject()

        themeObject.id = themeModel.id?.toLong()
        themeObject.name = themeModel.nameKeyboard
        val storageDir = File(
            App.instance.filesDir,
            Constant.PATH_FILE_UN_ZIP_THEME_CUSTOMIZE_BACKGROUND
        )
        val imageFile = File(storageDir, themeModel.id!!);
        themeObject.preview = imageFile.absolutePath
        App.instance.themeRepository!!.updateThemeDB(themeObject, 1)
            .subscribe(object : SingleObserver<Boolean> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onSuccess(t: Boolean) {
                    liveDataResultSaveTheme.postValue(true)
                }

                override fun onError(e: Throwable) {
                    liveDataResultSaveTheme.postValue(false)
                }
            })
    }

    fun loadDataSimpleButton() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val listSimpleButton = ArrayList<SimpleButton>()
                listSimpleButton.add(SimpleButton(2002, R.drawable.ic_type_stroke_5))
                listSimpleButton.add(SimpleButton(2001, R.drawable.ic_type_none))
                listSimpleButton.add(SimpleButton(2003, R.drawable.ic_type_stroke_10))
                listSimpleButton.add(SimpleButton(2004, R.drawable.ic_type_stroke_15))
                listSimpleButton.add(SimpleButton(2005, R.drawable.ic_type_stroke_25))
                listSimpleButton.add(SimpleButton(2006, R.drawable.ic_type_fill_5))
                listSimpleButton.add(SimpleButton(2007, R.drawable.ic_type_fill_10))
                listSimpleButton.add(SimpleButton(2008, R.drawable.ic_type_fill_15))
                listSimpleButton.add(SimpleButton(2009, R.drawable.ic_type_fill_25))
                liveDataListSimpleButton.postValue(listSimpleButton)
            }
        }
    }

    fun loadMyTheme() {
        instance.themeRepository!!.loadAllThemeByIsMyTheme(1)
            .subscribe(object : SingleObserver<MutableList<ThemeEntity>> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                override fun onSuccess(t: MutableList<ThemeEntity>) {
                    liveDataListMyTheme.postValue(t)
                }

                override fun onError(e: Throwable) {
                }
            })
    }

    fun writeBmToFolder(bitmap: Bitmap) {
        App.instance.themeRepository!!.getUri(
            bitmap,
            Constant.PATH_FILE_DOWNLOADED_BACKGROUND, false
        ).subscribe(object : SingleObserver<String> {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: String) {
                //pathBm.postValue(t)
            }

            override fun onError(e: Throwable) {

            }


        })
    }


}
