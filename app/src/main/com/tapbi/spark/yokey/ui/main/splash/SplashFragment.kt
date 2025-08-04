package com.tapbi.spark.yokey.ui.main.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentSplashBinding
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment

@SuppressLint("CustomSplashScreen")
class SplashFragment : BaseBindingFragment<FragmentSplashBinding, SplashViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_splash

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        navigateScreen(null, R.id.onboardFragment)
    }

    override fun onPermissionGranted() {
    }

    override fun getViewModel(): Class<SplashViewModel> {
        return SplashViewModel::class.java
    }

}
