package com.tapbi.spark.yokey.ui.base

import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class
BaseFragment : Fragment() {
    var timeClick: Long = 0
    protected var isRemoveAds = false
    open fun checkDoubleClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - timeClick >= 700) {
            timeClick = currentTime
            return true
        }
        return false
    }

}