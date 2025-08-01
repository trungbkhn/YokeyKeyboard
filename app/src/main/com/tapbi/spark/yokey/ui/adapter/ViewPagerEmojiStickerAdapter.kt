package com.tapbi.spark.yokey.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tapbi.spark.yokey.ui.main.home.emoji.FragmentEmoji
import com.tapbi.spark.yokey.ui.main.home.emoji.StickerFragment

class ViewPagerEmojiStickerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentEmoji()
            else -> StickerFragment()
        }
    }
}