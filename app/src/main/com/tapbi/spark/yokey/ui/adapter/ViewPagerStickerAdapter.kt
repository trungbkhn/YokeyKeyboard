package com.tapbi.spark.yokey.ui.adapter

import androidx.fragment.app.Fragment
import com.tapbi.spark.yokey.ui.main.home.emoji.FragmentItemSticker.Companion.newInstance
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.*

class ViewPagerStickerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                Objects.requireNonNull(newInstance(2000, 0))
            }
            1 -> {
                Objects.requireNonNull(newInstance(1000, 0))
            }

            else -> {
                Objects.requireNonNull(newInstance(3000, 0))
            }
        }
    }

    override fun getItemCount(): Int = 3

}