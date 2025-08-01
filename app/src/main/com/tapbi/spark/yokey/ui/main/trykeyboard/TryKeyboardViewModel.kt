package com.tapbi.spark.yokey.ui.main.trykeyboard

import androidx.lifecycle.MutableLiveData
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.local.entity.ItemFont
import com.tapbi.spark.yokey.data.local.entity.Sticker
import com.tapbi.spark.yokey.data.model.ThemeObject
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class TryKeyboardViewModel @Inject constructor() : BaseViewModel()  {
    var listTheme = MutableLiveData<ArrayList<ThemeObject>>()
    var listSticker = MutableLiveData<ArrayList<Sticker>>()
    var listItemFont = MutableLiveData<ArrayList<ItemFont>>()

    // TODO: load list theme
    fun loadListTheme() {
        val listThemeTry = App.instance.themeRepository?.listThemeTryKeyboard
        if (listThemeTry != null && listThemeTry.size > 6){
            var startPos = 0
            val theme = App.instance.themeRepository?.currentThemeModel
            if (theme?.id != null) {
                for (i in 0 until listThemeTry.size) {
                    try {
                        if (listThemeTry[i].id.toString().equals(theme.id.toString(), true)) {
                            startPos = i
                            break
                        }
                    }catch (ex : Exception){

                    }

                }
            }
            val array = loadRandom(listThemeTry.size - 1, startPos)
            val listSuggestion: ArrayList<ThemeObject> = ArrayList()
            if (array.isNotEmpty()){
                for (element in array){
                    if (element < listThemeTry.count()) {
                        listSuggestion.add(listThemeTry[element])
                    }
                }
            }
            listTheme.postValue(listSuggestion)
        }
    }

    fun loadRandom(total : Int, start : Int) : IntArray {
        val posOne = checkPosition(start, total)
        val posTwo = checkPosition(posOne, total)
        val posThree = checkPosition(posTwo, total)
        val posFour = checkPosition(posThree, total)
        val posFive = checkPosition(posFour, total)
        val posSix = checkPosition(posFive, total)
        Timber.d("duongcv$total:$posOne:$posTwo:$posThree:$posFour:$posFive:$posSix");
        return intArrayOf(posOne, posTwo, posThree, posFour, posFive, posSix)
    }

    fun checkPosition(current : Int , total: Int) : Int{
        return if (current < total) {
            current + 1
        }else {
            0
        }
    }

    fun loadListSticker() {
        val listStickerTry = App.instance.stickerRepository?.listStickerTryKeyboard
        if (listStickerTry != null && listStickerTry.size > 6){
            val random = Random.nextInt(listStickerTry.size)
            val array = loadRandom(listStickerTry.size - 1, random)
            val listSuggestion: ArrayList<Sticker> = ArrayList()
            if (array.isNotEmpty()){
                for (element in array){
                    if (element < listStickerTry.count()) {
                        listSuggestion.add(listStickerTry[element])
                    }
                }
            }
            listSticker.postValue(listSuggestion)
        }
    }

    fun loadListItemFont() {
        val listFont = App.instance.fontRepository?.listAllFont
        if (listFont != null && listFont.size > 6){
            val random = Random.nextInt(listFont.size)
            val array = loadRandom(listFont.size - 1, random)
            val listSuggestion: ArrayList<ItemFont> = ArrayList()
            if (array.isNotEmpty()){
                for (element in array){
                    listSuggestion.add(listFont[element])
                }
            }
            listItemFont.postValue(listSuggestion)
        }
    }
}