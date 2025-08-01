package com.tapbi.spark.yokey.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tapbi.spark.yokey.ui.main.home.emoji.FragmentEmojiSticker
import com.tapbi.spark.yokey.ui.main.home.font.FontFragment
import com.tapbi.spark.yokey.ui.main.home.maintheme.ThemeFragment
import com.tapbi.spark.yokey.ui.main.home.setting.SettingFragment

class ViewPagerHomeAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ThemeFragment()
            1 -> FontFragment()
            2 -> FragmentEmojiSticker()
            else -> SettingFragment()
        }
    }

}