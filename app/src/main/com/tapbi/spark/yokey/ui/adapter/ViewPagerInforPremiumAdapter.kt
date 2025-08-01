package com.tapbi.spark.yokey.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.ui.main.premium.sub.InformationPremiumFragment

class ViewPagerInforPremiumAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val listTitle = ArrayList<Int>()
    private val listDescription = ArrayList<Int>()

    init {
        listTitle.add(R.string.unlock_all_themes)
        listTitle.add(R.string.premium_desp_remove_ads)
        listTitle.add(R.string.unlock_pro_features)
        listTitle.add(R.string.unlock_all_style_custom_led)
        listDescription.add(R.string.download_and_use_free_150_theme)
        listDescription.add(R.string.des_remove_ads)
        listDescription.add(R.string.des_unlock_pro_features)
        listDescription.add(R.string.des_unlock_all_style_custom_led)
    }



    override fun getItemCount(): Int {
        return Integer.MAX_VALUE
    }

    override fun createFragment(position: Int): Fragment {
        return when (position % 4) {
            1 -> InformationPremiumFragment.newInstance(
                listTitle[1], listDescription[1]
            )
            2 -> InformationPremiumFragment.newInstance(
                listTitle[2], listDescription[2]
            )
            3 -> InformationPremiumFragment.newInstance(
                listTitle[3], listDescription[3]
            )
            else -> InformationPremiumFragment.newInstance(
                listTitle[0], listDescription[0]
            )
        }
    }
}