package com.tapbi.spark.yokey.ui.main.home.emoji

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant
import com.tapbi.spark.yokey.common.Constant.CHECK_EMOJI_UPDATE_NEW_PHASE7
import com.tapbi.spark.yokey.common.Constant.CHECK_UPDATE_EMOJI_PHASE7
import com.tapbi.spark.yokey.data.local.entity.Emoji
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import com.tapbi.spark.yokey.util.CommonUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StickerViewModel @Inject constructor() : BaseViewModel() {
    val listEmojiTrending = MutableLiveData<List<Emoji>>()

    @SuppressLint("CommitPrefEdits")
    fun loadEmojiFromJson(type: Int) {
       // if (App.instance.mPrefs!!.getBoolean(CHECK_EMOJI_UPDATE_NEW_PHASE7, true)) {
            viewModelScope.launch(IO) {
                val listEmojiSticker = ArrayList<Emoji>()
                if (App.instance.listEmojiDb.isNullOrEmpty()) {
                    Timber.d("loadEmojiFromJson true")
                    listEmojiSticker.addAll(loadEmoji(Constant.NAME_EMOJI_TRENDING, Constant.NAME_OBJECT_EMOJI_TRENDING))
                    listEmojiSticker.addAll(loadEmoji(Constant.NAME_EMOJI_NEW_PHASE7, Constant.NAME_OBJECT_EMOJI_NEW_PHASE7))
                    if (type == 0) {
                        insertDataEmojiToDB(listEmojiSticker)
                    }
                    App.instance.mPrefs!!.edit().putBoolean(CHECK_UPDATE_EMOJI_PHASE7, false)
                        .apply()
                } else {
                    Timber.d("loadEmojiFromJson false")
                    listEmojiSticker.addAll(App.instance.listEmojiDb)
                    if (App.instance.mPrefs!!.getBoolean(CHECK_UPDATE_EMOJI_PHASE7, true)) {
                        listEmojiSticker.addAll(
                            addEmojiUpdate(
                                type,
                                Constant.NAME_EMOJI_NEW_PHASE7,
                                Constant.NAME_OBJECT_EMOJI_NEW_PHASE7
                            )
                        )
                        App.instance.mPrefs!!.edit().putBoolean(CHECK_UPDATE_EMOJI_PHASE7, false)
                            .apply()
                    }
                    App.instance.listEmojiDb.clear()
                }
                if (listEmojiSticker.isNotEmpty()) {
                    val list = ArrayList<Emoji>()
                    listEmojiSticker.asFlow().filter {
                        it.type == type
                    }.collect {
                        list.add(it)
                        withContext(Dispatchers.Main) {
                            listEmojiTrending.postValue(list)
                        }
                    }
                }
           // }
        }
    }

    private fun addEmojiUpdate(
        type: Int,
        emojiName: String,
        objectsEmoji: String
    ): ArrayList<Emoji> {
        val listEmojiUpdate =
            loadEmoji(emojiName, objectsEmoji)
        if (type == 0) {
            insertDataEmojiToDB(listEmojiUpdate)
        }
        return listEmojiUpdate
    }

    private fun loadEmoji(emojiName: String, objectsEmoji: String): ArrayList<Emoji> {
        val jsonArray = CommonUtil.getDataAssetLocal(
            App.instance,
            emojiName,
            objectsEmoji
        )
        val listEmoji: ArrayList<Emoji> = ArrayList()
        if (jsonArray != null) {
            for (j in 0 until jsonArray.length()) {
                val gson = Gson()
                val key = gson.fromJson(
                    jsonArray[j].toString(),
                    Emoji::class.java
                )
                listEmoji.add(key)
            }
        }
        return listEmoji
    }

    private fun insertDataEmojiToDB(listEmoji: ArrayList<Emoji>) {
        viewModelScope.launch {
            withContext(IO) {
                App.instance.stickerRepository!!.insertDataEmoji(listEmoji)
            }
        }
    }


    fun loadEmojiDB(type: Int) {
        App.instance.stickerRepository!!.loadEmojiByType(type)
            .subscribe(object : SingleObserver<ArrayList<Emoji>> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.add(d)
                }

                @SuppressLint("CommitPrefEdits")
                override fun onSuccess(listEmoji: ArrayList<Emoji>) {
                        if (listEmoji != null && listEmoji.size > 0) {
                            listEmojiTrending.postValue(listEmoji)
                            Timber.d("loadEmojiFromJson " + listEmoji.size)
                            Timber.d("loadEmojiFromJson 2")
                        } else {
                            loadEmojiFromJson(type)
                            App.instance.mPrefs!!.edit()
                                .putBoolean(CHECK_EMOJI_UPDATE_NEW_PHASE7, false)
                                .apply()
                            Timber.d("loadEmojiFromJson 3")
                        }
                }

                override fun onError(e: Throwable) {
                }
            })
    }

    fun insertEmojiDB(emoji: Emoji) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                App.instance.stickerRepository!!.insertEmojiDB(emoji)
            }
        }
    }

    fun updateEmojiDB(emoji: Emoji) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                App.instance.stickerRepository!!.updateEmojiDB(emoji)
            }
        }
    }

    fun deleteEmojiDB(emoji: Emoji) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                App.instance.stickerRepository!!.deleteEmojiDB(emoji)
            }
        }
    }
}