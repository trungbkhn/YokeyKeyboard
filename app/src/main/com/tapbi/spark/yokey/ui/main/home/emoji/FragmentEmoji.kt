package com.tapbi.spark.yokey.ui.main.home.emoji

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentEmojiBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tapbi.spark.yokey.ui.adapter.ViewPagerItemEmoji
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.custom.view.CustomLineGradient
import com.tapbi.spark.yokey.ui.custom.view.CustomTextViewGradient
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.util.Utils

class FragmentEmoji : BaseBindingFragment<FragmentEmojiBinding, StickerViewModel>() {
    private lateinit var viewPagerItemEmoji: ViewPagerItemEmoji
    override fun getViewModel(): Class<StickerViewModel> = StickerViewModel::class.java

    override val layoutId: Int
        get() = R.layout.fragment_emoji

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        setupViewPager()
    }

    override fun onPermissionGranted() {}

    private fun setupViewPager() {
        viewPagerItemEmoji = ViewPagerItemEmoji(this)
        binding.pagerEmoji.adapter = viewPagerItemEmoji
        binding.pagerEmoji.offscreenPageLimit = 1
        binding.pagerEmoji.isUserInputEnabled = false
        TabLayoutMediator(binding.tabLayoutEmoji, binding.pagerEmoji) { tab, _ ->
            tab.setCustomView(R.layout.custom_layout_tablayout)
        }.attach()
        binding.pagerEmoji.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> (activity as MainActivity).currentPageEmoji = 0
                    1 -> (activity as MainActivity).currentPageEmoji = 1
                    2 -> (activity as MainActivity).currentPageEmoji = 2
                }
                super.onPageSelected(position)
            }
        })
        binding.tabLayoutEmoji.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                changeTabLayout(tab!!.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        for (i in 0 until 3) {
            binding.tabLayoutEmoji.getTabAt(i)!!.customView!!.findViewById<CustomTextViewGradient>(
                R.id.txtNameTabLayout
            )!!.text = resources.getString(Utils.listTabNameEmoji[i])
            binding.tabLayoutEmoji.getTabAt(i)!!.customView?.findViewById<CustomTextViewGradient>(
                R.id.txtNameTabLayout
            )!!.textSize = 15f
        }
        changeTabLayout(0)
    }

    override fun onResume() {
        binding.pagerEmoji.setCurrentItem((activity as MainActivity).currentPageEmoji, false)
        binding.tabLayoutEmoji.selectTab(binding.tabLayoutEmoji.getTabAt((activity as MainActivity).currentPageEmoji))
        super.onResume()
    }

    private fun changeTabLayout(position: Int) {
        for (i in 0 until 3) {
            initTabItem(i, false)
        }
        initTabItem(position, true)
    }

    private fun initTabItem(position: Int, active: Boolean) {
        binding.tabLayoutEmoji.getTabAt(position)?.apply {
            val txtName = customView?.findViewById<CustomTextViewGradient>(R.id.txtNameTabLayout)
            val lineGradient = customView?.findViewById<CustomLineGradient>(R.id.lineTablayout)
            txtName?.isTextGradient(active)
            lineGradient?.visibility = if (active) View.VISIBLE else View.INVISIBLE
        }
    }

}