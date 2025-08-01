package com.tapbi.spark.yokey.ui.main.crop

import android.graphics.Bitmap
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.LiveEvent
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import com.tapbi.spark.yokey.util.Constant
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

@HiltViewModel
class CropImageViewModel @Inject constructor() : BaseViewModel() {
     var pathBm = LiveEvent<String>()

    fun convertBmToString(bitmap: Bitmap) {
        App.instance.themeRepository!!.getUri(bitmap,
            Constant.PATH_FILE_UN_ZIP_THEME_CUSTOMIZE_BACKGROUND,true).subscribe(object : SingleObserver<String>{
            override fun onSubscribe(d: Disposable) {

            }

            override fun onSuccess(t: String) {
              pathBm.postValue(t)
            }

            override fun onError(e: Throwable) {

            }


        })
    }
}