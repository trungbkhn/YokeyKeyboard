package com.tapbi.spark.yokey.ui.main.home

import android.os.Bundle
import android.view.View
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentHomeBinding
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment

class HomeFragment : BaseBindingFragment<FragmentHomeBinding, HomeViewModel>() {
    override fun getViewModel(): Class<HomeViewModel> {
        return HomeViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_home

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
    }

    override fun onPermissionGranted() {
    }

}