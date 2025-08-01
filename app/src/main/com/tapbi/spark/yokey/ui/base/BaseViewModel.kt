package com.tapbi.spark.yokey.ui.base

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

open class BaseViewModel : ViewModel() {
    protected val compositeDisposable = CompositeDisposable()
    val job = Job()
    protected val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCleared() {
        compositeDisposable.clear()
        job.cancel()
        coroutineScope.cancel()
        super.onCleared()
    }
}