package com.tapbi.spark.yokey.ui.main.detailsticker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.LiveEvent
import com.tapbi.spark.yokey.data.local.entity.Sticker
import com.tapbi.spark.yokey.data.remote.ApiUtils
import com.tapbi.spark.yokey.data.remote.StickerService
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject


@HiltViewModel
class DetailStickerViewModel @Inject  constructor() : BaseViewModel() {

    private val stickerServiceDownload: StickerService
    @JvmField
    var resultDownload = MutableLiveData<Boolean>()
    @JvmField
    var resultInsert = MutableLiveData<Boolean>()
    private val resultSticker = LiveEvent<Boolean>()
    @JvmField
    var _resultSticker: LiveData<Boolean> = resultSticker
    @JvmField
    var idShowResultDownload = 0

    init {
        stickerServiceDownload = ApiUtils.downloadZipStickerService()
    }


    fun insertStickerToDB(context: Context?, sticker: Sticker?) {
        if (sticker != null) {
            sticker.isDownload = 1
            App.instance!!.stickerRepository!!.insertStickerDatabase(context, sticker)
                .subscribe(object : SingleObserver<Boolean> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onSuccess(aBoolean: Boolean) {
                        App.instance!!.stickerRepository!!.updateListStickerOnkeyboard()
                        resultInsert.postValue(true)
                        //                    App.instance!!.stickerRepository.resultDownload.setValue(false);
                    }

                    override fun onError(e: Throwable) {
                        resultInsert.postValue(false)
                    }
                })
        }
    }
}