package com.tapbi.spark.yokey.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tapbi.spark.yokey.ui.main.home.font.FragmentItemFont
import com.tapbi.spark.yokey.util.Constant

class ViewPagerFontAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentItemFont(Constant.KEY_TAB_ALL)
            1 -> FragmentItemFont(Constant.KEY_TAB_SANS)
            2 -> FragmentItemFont(Constant.KEY_TAB_SERIF)
            3 -> FragmentItemFont(Constant.KEY_TAB_DISPLAY)
            4 -> FragmentItemFont(Constant.KEY_TAB_HAND)
            5 -> FragmentItemFont(Constant.KEY_TAB_SCRIPT)
            6 -> FragmentItemFont(Constant.KEY_TAB_TIKTOK)
            7 -> FragmentItemFont(Constant.KEY_TAB_INS)
            else -> FragmentItemFont(Constant.KEY_TAB_OTHER)
        }
    }

    override fun getItemCount(): Int = 9

}