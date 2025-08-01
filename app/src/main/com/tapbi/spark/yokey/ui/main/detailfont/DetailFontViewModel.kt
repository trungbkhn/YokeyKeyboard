package com.tapbi.spark.yokey.ui.main.detailfont

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.local.entity.ItemFont
import com.tapbi.spark.yokey.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

@HiltViewModel
class DetailFontViewModel @Inject constructor() : BaseViewModel() {

    @JvmField
    var resultLoadFont = MutableLiveData<ItemFont>()

    fun getFontById(id : Int, context: Context){
        App.instance.fontRepository?.loadFontDataBaseById(context, id)?.subscribe(object : SingleObserver<ItemFont>{
            override fun onSubscribe(d: Disposable) {

            }

            override fun onError(e: Throwable) {

            }

            override fun onSuccess(t: ItemFont) {
                resultLoadFont.postValue(t)
            }

        })
    }

}