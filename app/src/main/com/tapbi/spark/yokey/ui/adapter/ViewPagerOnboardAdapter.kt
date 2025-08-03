package com.tapbi.spark.yokey.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tapbi.spark.yokey.ui.splash.onboard.OnboardFragment
import com.tapbi.spark.yokey.ui.splash.SplashActivity

class ViewPagerOnboardAdapter (activity: SplashActivity) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return OnboardFragment.newInstance(position)
    }

    override fun getItemCount(): Int {
        return 3
    }
}