package com.tapbi.spark.yokey.ui.main.home.maintheme

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentThemeMainBinding
import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils
import com.google.android.material.tabs.TabLayoutMediator
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.adapter.ViewPagerThemesAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.custom.view.CustomLineGradient
import com.tapbi.spark.yokey.ui.custom.view.CustomTextViewGradient
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ThemeFragment : BaseBindingFragment<FragmentThemeMainBinding, ThemeViewModel>() {

    //  private val viewPagerThemesAdapter: ViewPagerThemesAdapter by lazy { ViewPagerThemesAdapter(this) }


    override fun getViewModel(): Class<ThemeViewModel> = ThemeViewModel::class.java

    override val layoutId: Int
        get() = R.layout.fragment_theme_main

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        setUpViewPager()
        listener()
    }

    override fun onPermissionGranted() {

    }

    fun listener() {
        binding.tvActiveKeyboard.setOnClickListener {
            (requireActivity() as MainActivity).showDialogPermission()
        }
    }

    private fun setUpViewPager() {
        binding.pagerThemes.adapter = ViewPagerThemesAdapter(this)
        binding.pagerThemes.offscreenPageLimit = 1
        TabLayoutMediator(binding.tabLayout, binding.pagerThemes) { tab, _ ->
            tab.setCustomView(R.layout.custom_layout_tablayout)
        }.attach()
        binding.pagerThemes.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                changeTabLayout(position)
                when (position) {
                    0 -> (activity as MainActivity).currentPageTheme = 0
                    1 -> (activity as MainActivity).currentPageTheme = 1
                    2 -> (activity as MainActivity).currentPageTheme = 2
                    3 -> (activity as MainActivity).currentPageTheme = 3
                    4 -> (activity as MainActivity).currentPageTheme = 4
                    5 -> (activity as MainActivity).currentPageTheme = 5
                }
                if (position == 5 || position == 1) {
                    binding.llCheckInternet.visibility = View.GONE
                } else {
                    checkInternetAndSetupView(App.instance.connectivityStatus != -1)
                }
                super.onPageSelected(position)
            }
        })
        for (i in 0 until 6) {
            if (binding.tabLayout.getTabAt(i) != null && binding.tabLayout.getTabAt(i)!!.customView != null)
                binding.tabLayout.getTabAt(i)!!.customView?.findViewById<CustomTextViewGradient>(
                    R.id.txtNameTabLayout
                )?.setText(Utils.listTabTheme[i])
        }
        changeTabLayout(0)

    }

    private fun changeTabLayout(position: Int) {
        for (i in 0..5) {
            initTabItem(i, false)
        }
        initTabItem(position, true)
    }

    private fun initTabItem(position: Int, active: Boolean) {
        binding.tabLayout.apply {
            val txtName =
                getTabAt(position)?.customView?.findViewById<CustomTextViewGradient>(R.id.txtNameTabLayout)
            val lineGradient =
                getTabAt(position)?.customView?.findViewById<CustomLineGradient>(R.id.lineTablayout)
            txtName?.isTextGradient(active)
            lineGradient?.visibility = if (active) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    override fun onResume() {
        binding.llCheckInternet.visibility = View.GONE
        if (App.instance.connectivityStatus == -1) {
            binding.llCheckInternet.visibility = View.VISIBLE
        }
        if (binding.pagerThemes.currentItem == 5 || binding.pagerThemes.currentItem == 1) {
            binding.llCheckInternet.visibility = View.GONE
        }
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt((activity as MainActivity).currentPageTheme))
        binding.pagerThemes.setCurrentItem((activity as MainActivity).currentPageTheme, false)
        EventBus.getDefault().register(this)
        super.onResume()
        checkActivateKeyboard()
    }

    fun checkActivateKeyboard(){
        if ((!UncachedInputMethodManagerUtils.isThisImeEnabled(requireContext(), App.instance.mImm) || !UncachedInputMethodManagerUtils.isThisImeCurrent(requireContext(), App.instance.mImm))){
            binding.tvActiveKeyboard.visibility = View.VISIBLE
        }else {
            binding.tvActiveKeyboard.visibility = View.GONE
        }
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    fun changeCurrentTheme() {
        binding.pagerThemes.currentItem = binding.pagerThemes.currentItem
    }

    fun checkInternetAndSetupView(isCheck: Boolean) {
        if (isCheck) binding.llCheckInternet.visibility =
            View.GONE else binding.llCheckInternet.visibility =
            View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageEvent(event: MessageEvent) {
        when (event.key) {
            Constant.KEY_SCREEN_MYTHEME -> {
                binding.pagerThemes.currentItem = 5
                binding.tabLayout.post {
                    changeTabLayout(5)
                }
            }
            Constant.CONNECT_INTERNET -> {
                binding.llCheckInternet.visibility = View.GONE
            }
            Constant.ACTION_CHANGE_STATE_ACTIVATE_KEYBOARD -> {
                checkActivateKeyboard()
            }
        }
    }
}