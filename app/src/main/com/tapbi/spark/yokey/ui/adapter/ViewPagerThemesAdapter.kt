package com.tapbi.spark.yokey.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tapbi.spark.yokey.ui.main.home.maintheme.option.FragmentBackGroundTheme
import com.tapbi.spark.yokey.ui.main.home.maintheme.option.FragmentColorTheme
import com.tapbi.spark.yokey.ui.main.home.maintheme.option.FragmentFeatured
import com.tapbi.spark.yokey.ui.main.home.maintheme.option.FragmentHotTheme
import com.tapbi.spark.yokey.ui.main.home.maintheme.option.FragmentLEDTheme
import com.tapbi.spark.yokey.ui.main.home.maintheme.option.FragmentMyTheme

class ViewPagerThemesAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentHotTheme()
            1 -> FragmentFeatured()
            2 -> FragmentLEDTheme()
            3 -> FragmentColorTheme()
            4 -> FragmentBackGroundTheme()
            else-> FragmentMyTheme()
        }
    }
    override fun getItemCount(): Int = 6

}