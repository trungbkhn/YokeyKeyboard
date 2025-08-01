package com.tapbi.spark.yokey.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tapbi.spark.yokey.ui.main.home.emoji.FragmentItemEmoji

class ViewPagerItemEmoji(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentItemEmoji(0)
            1 -> FragmentItemEmoji(1)
            else -> FragmentItemEmoji(2)
        }
    }
}