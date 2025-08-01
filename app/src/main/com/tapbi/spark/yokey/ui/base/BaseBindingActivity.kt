package com.tapbi.spark.yokey.ui.base

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.ui.main.MainActivity

abstract class BaseBindingActivity<B : ViewDataBinding?, VM : BaseViewModel> :
    BaseActivity() {
    var binding: B? = null
    public var viewModel: VM? = null
    abstract val layoutId: Int
    protected var timeClick: Long = 0

    abstract fun getViewModel(): Class<VM>
    abstract fun setupView(savedInstanceState: Bundle?)
    abstract fun setupData()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutId)
        viewModel = ViewModelProvider(this)[getViewModel()]
        setupView(savedInstanceState)
        if(this is MainActivity){
            lifecycle.addObserver(App.instance!!.billingManager!!)
        }
        setupData()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun checkDoubleClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - timeClick >= 800) {
            timeClick = currentTime
            return true
        }
        return false
    }
}