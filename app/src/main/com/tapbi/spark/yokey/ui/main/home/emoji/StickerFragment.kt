package com.tapbi.spark.yokey.ui.main.home.emoji

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentStickerBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.tapbi.spark.yokey.ui.adapter.ViewPagerStickerAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.custom.view.CustomLineGradient
import com.tapbi.spark.yokey.ui.custom.view.CustomTextViewGradient
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.util.Utils
import java.util.*

class StickerFragment : BaseBindingFragment<FragmentStickerBinding, StickerViewModel>() {
//    private val viewPagerStickerAdapter: ViewPagerStickerAdapter by lazy {
//        ViewPagerStickerAdapter(
//            this
//        )
//    }

    override fun getViewModel(): Class<StickerViewModel> = StickerViewModel::class.java

    override val layoutId: Int get() = R.layout.fragment_sticker

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        setUpViewPagerSticker()
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt((activity as MainActivity).currentPageSticker))
        binding.pagerSticker.setCurrentItem((activity as MainActivity).currentPageSticker, false)
    }

    override fun onPermissionGranted() {

    }

    private fun setUpViewPagerSticker() {
        binding.pagerSticker.adapter = ViewPagerStickerAdapter(this)
        binding.pagerSticker.offscreenPageLimit = 2
        TabLayoutMediator(binding.tabLayout, binding.pagerSticker) { tab, _ ->
            tab.setCustomView(R.layout.custom_layout_tablayout)
        }.attach()
        binding.pagerSticker.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                changeTabLayoutSticker(position)
                when (position) {
                    0 -> (activity as MainActivity).currentPageSticker = 0
                    1 -> (activity as MainActivity).currentPageSticker = 1
                    2 -> (activity as MainActivity).currentPageSticker = 2
                }
                super.onPageSelected(position)
            }
        })
        for (i in 0 until 3) {
            if (binding.tabLayout.getTabAt(i) != null && binding.tabLayout.getTabAt(i)!!.customView != null) {
                binding.tabLayout.getTabAt(i)!!.customView?.findViewById<CustomTextViewGradient>(
                    R.id.txtNameTabLayout
                )?.setText(Utils.listTabSticker[i])
                binding.tabLayout.getTabAt(i)!!.customView?.findViewById<CustomTextViewGradient>(
                    R.id.txtNameTabLayout
                )!!.textSize = 15f
            }
        }
        changeTabLayoutSticker(0)
    }

    override fun onResume() {
        super.onResume()
    }

    private fun changeTabLayoutSticker(position: Int) {
        for (i in 0..8) {
            initTabItem(i, false)
        }
        initTabItem(position, true)
    }


    private fun initTabItem(position: Int, active: Boolean) {
        binding.tabLayout.getTabAt(position)?.apply {
            val txtName = customView?.findViewById<CustomTextViewGradient>(R.id.txtNameTabLayout)
            val lineGradient = customView?.findViewById<CustomLineGradient>(R.id.lineTablayout)
            txtName?.isTextGradient(active)
            lineGradient?.visibility = if (active) View.VISIBLE else View.INVISIBLE
        }

    }

    fun changeCurrentTheme() {
        binding.pagerSticker.currentItem = binding.pagerSticker.currentItem
    }
}