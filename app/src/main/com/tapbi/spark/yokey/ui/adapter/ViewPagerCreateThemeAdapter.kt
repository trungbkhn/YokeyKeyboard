package com.tapbi.spark.yokey.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tapbi.spark.yokey.data.model.theme.ThemeModel
import com.tapbi.spark.yokey.ui.main.customize.control_createtheme.BackgroundControlFragment
import com.tapbi.spark.yokey.ui.main.customize.control_createtheme.EffectControlFragment
import com.tapbi.spark.yokey.ui.main.customize.control_createtheme.KeyControlFragment
import com.tapbi.spark.yokey.ui.main.customize.control_createtheme.SoundControlFragment

class ViewPagerCreateThemeAdapter(fragment: Fragment, var themeModel : ThemeModel) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BackgroundControlFragment.newInstance(themeModel)
            1 -> KeyControlFragment.newInstance(themeModel)
            2 -> EffectControlFragment.newInstance()
            else -> SoundControlFragment.newInstance()
        }
    }
}