package com.tapbi.spark.yokey.ui.main.home.emoji

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentEmojiStickerBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tapbi.spark.yokey.common.Constant.SCREEN_STICKER
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.adapter.ViewPagerEmojiStickerAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

class FragmentEmojiSticker : BaseBindingFragment<FragmentEmojiStickerBinding, StickerViewModel>() {
    override fun getViewModel(): Class<StickerViewModel> = StickerViewModel::class.java
    private lateinit var viewPagerEmojiStickerAdapter: ViewPagerEmojiStickerAdapter
    private val defaultScreen = Constant.KEY_SCREEN_THEME
    override val layoutId: Int
        get() = R.layout.fragment_emoji_sticker

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        setupViewPager()
        binding.tabLayoutEmojiSticker.selectTab(binding.tabLayoutEmojiSticker.getTabAt((activity as MainActivity).currentPageEmojiSticker))
        binding.pagerEmojiSticker.setCurrentItem(
            (activity as MainActivity).currentPageEmojiSticker,
            false
        )
        lifecycleScope.launchWhenStarted {
            mainViewModel.mFlowEvent.collect {
                if (it) {
                    Timber.d("ducNQlaunchWhenStarted ");
                    (activity as MainActivity).currentPageEmojiSticker = 1
                    binding.tabLayoutEmojiSticker.selectTab(binding.tabLayoutEmojiSticker.getTabAt(1))
                    binding.pagerEmojiSticker.setCurrentItem(1, false)
                    mainViewModel.mFlowEvent.emit(false)
                }
            }
        }
//        mainViewModel.mLiveEventScreenSticker.observe(viewLifecycleOwner) {
//            if (it) {
//                (activity as MainActivity).currentPageEmojiSticker = 1
//                binding.tabLayoutEmojiSticker.selectTab(binding.tabLayoutEmojiSticker.getTabAt(1))
//                binding.pagerEmojiSticker.setCurrentItem(1, false)
//                mainViewModel.mLiveEventScreenSticker.postValue(false)
//            }
//        }
    }


    override fun onPermissionGranted() {}

    private fun setupViewPager() {
        viewPagerEmojiStickerAdapter = ViewPagerEmojiStickerAdapter(this)
        binding.pagerEmojiSticker.adapter = viewPagerEmojiStickerAdapter
        binding.pagerEmojiSticker.offscreenPageLimit = 1
        binding.pagerEmojiSticker.isUserInputEnabled = false

        TabLayoutMediator(
            binding.tabLayoutEmojiSticker,
            binding.pagerEmojiSticker
        ) { tab, _ ->
            tab.setCustomView(R.layout.custom_layout_emoji)
        }.attach()
        binding.pagerEmojiSticker.post {
            binding.pagerEmojiSticker.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> (activity as MainActivity).currentPageEmojiSticker = 0
                        1 -> (activity as MainActivity).currentPageEmojiSticker = 1
                    }
                    super.onPageSelected(position)
                }
            })
        }
        binding.tabLayoutEmojiSticker.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                nonSelectTab(tab!!.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        for (i in 0 until 2) {
            if (binding.tabLayoutEmojiSticker.getTabAt(i) != null) {
                binding.tabLayoutEmojiSticker.getTabAt(i)!!.customView!!.findViewById<TextView>(
                    R.id.text_emoji_sticker
                ).text = resources.getString(Utils.listTabNameEmojiSticker[i])
                binding.tabLayoutEmojiSticker.getTabAt(i)!!.customView!!.findViewById<TextView>(
                    R.id.text_emoji_sticker
                ).setTextColor(resources.getColor(R.color.color_DEDEDE))
            }
        }
        initTabPager()

    }

    private fun initTabPager() {
        nonSelectTab(0)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun selectTab(position: Int) {
        for (i in 0 until 2) {
            if (binding.tabLayoutEmojiSticker.getTabAt(i) != null && binding.tabLayoutEmojiSticker.getTabAt(
                    position
                )!!.customView != null && position == i
            ) {
                binding.tabLayoutEmojiSticker.getTabAt(i)!!.customView!!.findViewById<TextView>(
                    R.id.text_emoji_sticker
                ).setTextColor(resources.getColor(R.color.color_7048FF))
                binding.tabLayoutEmojiSticker.getTabAt(i)!!.customView!!.findViewById<TextView>(
                    R.id.text_emoji_sticker
                ).setCompoundDrawablesWithIntrinsicBounds(
                    Utils.listTabChooseEmojiSticker[i],
                    0,
                    0,
                    0
                )
                binding.tabLayoutEmojiSticker.getTabAt(i)!!.customView!!.findViewById<TextView>(
                    R.id.text_emoji_sticker
                ).text = resources.getString(Utils.listTabNameEmojiSticker[i])
            }
        }
    }

    private fun nonSelectTab(position: Int) {
        for (i in 0 until 2) {
            if (binding.tabLayoutEmojiSticker.getTabAt(i) != null) {
                binding.tabLayoutEmojiSticker.getTabAt(i)!!.customView!!.findViewById<TextView>(
                    R.id.text_emoji_sticker
                ).setTextColor(resources.getColor(R.color.color_DEDEDE))
                binding.tabLayoutEmojiSticker.getTabAt(i)!!.customView!!.findViewById<TextView>(
                    R.id.text_emoji_sticker
                ).setCompoundDrawablesWithIntrinsicBounds(
                    Utils.listTabEmojiSticker[i],
                    0,
                    0,
                    0
                )
                binding.tabLayoutEmojiSticker.getTabAt(i)!!.customView!!.findViewById<TextView>(
                    R.id.text_emoji_sticker
                ).text = resources.getString(Utils.listTabNameEmojiSticker[i])
            }
        }
        selectTab(position)
    }

    override fun onResume() {
        EventBus.getDefault().register(this)

        super.onResume()
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessage(messageEvent: MessageEvent) {
        when (messageEvent.key) {
            SCREEN_STICKER -> {
                (activity as MainActivity).currentPageEmojiSticker = 1
                binding.pagerEmojiSticker.setCurrentItem(1, false)
                binding.tabLayoutEmojiSticker.selectTab(binding.tabLayoutEmojiSticker.getTabAt(1))
            }
        }
    }


}