package com.tapbi.spark.yokey.ui.splash.onboard

import android.os.Bundle
import android.view.View
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentOnboardBinding

import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.splash.SplashViewModel


class OnboardFragment : BaseBindingFragment<FragmentOnboardBinding, SplashViewModel>() {

    override fun getViewModel(): Class<SplashViewModel> {
        return SplashViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_onboard

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
    }

    override fun onPermissionGranted() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    companion object {
        @JvmStatic
        fun newInstance(type: Int): OnboardFragment {
            val args = Bundle()
            args.putInt("type", type)
            val fragment = OnboardFragment()
            fragment.arguments = args
            return fragment
        }
    }

}