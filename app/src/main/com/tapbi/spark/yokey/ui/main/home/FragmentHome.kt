package com.tapbi.spark.yokey.ui.main.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayout
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.App.Companion.isActivityVisible
import com.tapbi.spark.yokey.data.local.entity.ItemFont
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.adapter.ViewPagerHomeAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.custom.view.CustomTextViewGradient
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.Constant.CHECK_DOUBLE_CLICK
import com.tapbi.spark.yokey.util.Constant.CHECK_LOAD_ADS
import com.tapbi.spark.yokey.util.MySharePreferences
import com.tapbi.spark.yokey.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber


class FragmentHome constructor() :
    BaseBindingFragment<FragmentHomeBinding, FragmentHomeViewModel>() {
    private var defaultScreen = Constant.KEY_SCREEN_THEME
    private var currentScreen = 0;
    private var isChangeTab = false
    val widthScreen = CommonUtil.getScreenWidth() - CommonUtil.dpToPx(App.instance, 38)
    override fun getViewModel(): Class<FragmentHomeViewModel> = FragmentHomeViewModel::class.java
    override val layoutId: Int
        get() = R.layout.fragment_home
    var itemFont: ItemFont? = null
    var waitingShowFont = false
    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        setUpData()
        initCurrentScreen(savedInstanceState)
        setUpViewPager()
        App.activityResumed()
        changeScreen()
        eventClick()
        val position = (activity as MainActivity).currentPager//instance.idScreen
        binding.viewPagerHome.setCurrentItem(position, false)
//        Timber.d("ducNQ onCreatedViewed: " + (activity as MainActivity).currentPager);
//        Timber.d("ducNQ onCreatedViewed: " + instance.idScreen);
        binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(if (position > 1) position + 1 else position))
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }
    private fun initCurrentScreen(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            defaultScreen = savedInstanceState.getInt(Constant.ID_SCREEN_CURRENT)
            isChangeTab = true
            currentScreen = when (defaultScreen) {
                Constant.KEY_SCREEN_THEME -> 0
                Constant.KEY_SCREEN_FONT -> 1
                Constant.KEY_SCREEN_STICKER -> 2
                Constant.KEY_SCREEN_MORE -> 3
                else -> 0
            }
        }
        Log.d("duongcv", "initCurrentScreen: " + currentScreen)
    }

    private fun eventClick() {
        binding.imgCustomizeTheme.setOnClickListener {
            if (checkDoubleClick()) {
//                binding.imgPremium.isEnabled = false
                instance.linkCurrentBg = "null"
                instance.typeKey = Constant.TYPE_KEY_2006
                instance.colorIconDefault = Color.WHITE
                instance.checkFirstTimeSetBg = false
                (activity as MainActivity).changeStartScreen(R.id.createThemeFragment, null)
            }
        }
        binding.imgPremium.setOnClickListener {
            if (checkDoubleClick()) {
//                binding.imgCustomizeTheme.isEnabled = false
                (activity as MainActivity).navControllers.navigate(R.id.premiumFragment)
            }
        }
    }

    private fun setUpData() {

    }


    override fun onPermissionGranted() {}

    private fun setUpViewPager() {
        binding.viewPagerHome.adapter = ViewPagerHomeAdapter(this)
        binding.viewPagerHome.isUserInputEnabled = false
        binding.viewPagerHome.animation = null
        initBottomNavigation()
        binding.viewPagerHome.isSaveFromParentEnabled = false
        binding.viewPagerHome.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Timber.d("ducNQ onPageSelected: ");
                when (position) {
                    0 -> {
                        (activity as? MainActivity)?.currentPager = 0
                        //  instance.idScreen = 0
                        binding.nameTitle.text = resources.getString(R.string.text_tab_themes)
//                        positionBottomNavigation(0)
                    }

                    1 -> {
                        //  instance.idScreen = 1
                        (activity as? MainActivity)?.currentPager = 1
                        binding.nameTitle.text = resources.getString(R.string.text_tab_fonts)
//                        positionBottomNavigation(1)
                    }

                    2 -> {
                        (activity as? MainActivity)?.currentPager = 2
                        //  instance.idScreen = 2
                        binding.nameTitle.text = resources.getString(R.string.text_tab_emoji)
//                        positionBottomNavigation(3)
                    }

                    3 -> {
//                        binding.viewPagerHome.post {
//                            if (activity is MainActivity) {
                        (activity as? MainActivity)?.currentPager = 3
                        //   instance.idScreen = 3
                        binding.nameTitle.text = resources.getString(R.string.text_tab_setting)
//                                positionBottomNavigation(4)
//                                binding.viewPagerHome.currentItem = 3
                        binding.tabLayoutBottomHome.selectTab(
                            binding.tabLayoutBottomHome.getTabAt(
                                4
                            )
                        )
//                            }
//                        }
                    }
                }
                super.onPageSelected(position)
            }
        })
        Log.d("duongcv", "setUpViewPager: " + currentScreen)
        binding.viewPagerHome.currentItem = currentScreen
//        positionBottomNavigation(currentScreen)
    }

    private fun initBottomNavigation() {
        binding.tabLayoutBottomHome.animation = null
        binding.tabLayoutBottomHome.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    if (!isChangeTab) {
                        Log.d("duongcv", "onTabSelected: " + tab.position)
                        if (tab.position != 2) positionBottomNavigation(tab.position)

                        if (tab.position < 2) {
                            binding.viewPagerHome.setCurrentItem(tab.position, false)
                            //binding.viewPagerHome.currentItem = tab.position
                        } else if (tab.position > 2) {
                            binding.viewPagerHome.setCurrentItem(tab.position - 1, false)
                            // binding.viewPagerHome.currentItem = tab.position - 1
                        }
                    } else {
                        isChangeTab = false
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })
        for (i in 0 until 5) {
            binding.tabLayoutBottomHome.addTab(
                binding.tabLayoutBottomHome.newTab()
                    .setCustomView(R.layout.custom_layout_bottom), i
            )
            if (i == 2) {
                binding.tabLayoutBottomHome.getTabAt(2)?.customView?.visibility = View.INVISIBLE
                binding.tabLayoutBottomHome.getTabAt(2)?.customView?.isEnabled = false
            } else {
                binding.tabLayoutBottomHome.getTabAt(i)?.customView?.findViewById<CustomTextViewGradient>(
                    R.id.txtNameBottom
                )?.text = resources.getString(Utils.listTabName[i])
            }
        }
        positionBottomNavigation(if (currentScreen >= 2) currentScreen + 1 else currentScreen)
    }

    private fun positionBottomNavigation(selectPosition: Int) {
        Log.d("duongcv", "positionBottomNavigation: " + selectPosition)
        for (i in 0 until 5) {
            if (i != 2 && binding.tabLayoutBottomHome.getTabAt(i) != null
                && binding.tabLayoutBottomHome.getTabAt(
                    i
                )!!.customView != null
            ) {
                binding.tabLayoutBottomHome.getTabAt(i)?.customView?.findViewById<CustomTextViewGradient>(
                    R.id.txtNameBottom
                )?.isTextGradient(false)
                binding.tabLayoutBottomHome.getTabAt(i)?.apply {
                    customView?.findViewById<ImageView>(R.id.imgIconBottom)
                        ?.setImageResource(Utils.listTabImage[i])
                }
            }
        }
        positionSelectTab(selectPosition)
    }

    private fun positionSelectTab(position: Int) {
        for (i in 0 until 5) {
            if (i != 2 && binding.tabLayoutBottomHome.getTabAt(i) != null && binding.tabLayoutBottomHome.getTabAt(
                    i
                )!!.customView != null && position == i
            ) {
                (binding.tabLayoutBottomHome.getTabAt(i)!!.customView?.findViewById(R.id.txtNameBottom) as CustomTextViewGradient).isTextGradient(
                    true
                )
                (binding.tabLayoutBottomHome.getTabAt(i)!!.customView?.findViewById(R.id.imgIconBottom) as ImageView).setImageResource(
                    Utils.listTabChooseImage[i]
                )
            }
        }
    }

    private fun changeScreen() {
        if (isAdded && activity != null) {
            val id = requireActivity().intent.getIntExtra(Constant.KEY_OPEN_SCREEN, defaultScreen)
            when (id) {
                Constant.KEY_SCREEN_FONT -> {
                    if (instance.idScreen == 1) {
                        binding.viewPagerHome.setCurrentItem(
                            1,
                            false
                        )
                        binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(1))
                    }
                    binding.viewPagerHome.setCurrentItem(
                        (activity as MainActivity).currentPager,
                        false
                    )
                    binding.tabLayoutBottomHome.selectTab(
                        binding.tabLayoutBottomHome.getTabAt(
                            (activity as MainActivity).currentPager
                        )
                    )
                    if ((activity as MainActivity).currentPager == 1) {
                        val bundle: Bundle? =
                            requireActivity().intent.getBundleExtra(Constant.DATA_BUNDLE)
                        if (bundle != null) {
                            itemFont = bundle.getParcelable(Constant.DATA_FONT_ADD)
                            if (itemFont != null) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    Log.d("duongcv", "changeScreen: ")
                                    if (isActivityVisible) {
                                        Log.d("duongcv", "changeScreen: set")
                                        mainViewModel.mLiveDataDetailObject.value = itemFont
                                        nexScreenAfterAds()
                                    } else {
                                        waitingShowFont = true
                                    }
                                }, 500)
                            }
                        }
                    }
                }

                Constant.KEY_SCREEN_LANGUAGE -> {
//                    binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(4))
//                    CommonUtil.invokeSubtypeEnablerOfThisIme(requireContext())
                    (activity as MainActivity).currentPager = 3
                    binding.viewPagerHome.setCurrentItem(3, false)
                    binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(4))
                    (activity as MainActivity).changeStartScreen(
                        R.id.keyboardLanguageFragment,
                        null
                    )
                }

                Constant.KEY_SCREEN_MORE -> {
                    /*  if (instance.idScreen == 3) {
                          binding.viewPagerHome.setCurrentItem(
                                  3,
                                  false
                          )
                          binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(4))
                      }*/
                    Timber.d("ducNQ changeScreened: " + System.currentTimeMillis());
                    binding.viewPagerHome.setCurrentItem(
                        (activity as MainActivity).currentPager,
                        false
                    )
                    binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(4))
                }

                Constant.KEY_SCREEN_STICKER -> {
                    if (instance.idScreen == 2) {
                        binding.viewPagerHome.setCurrentItem(
                            2,
                            false
                        )
                        binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(2))
                        CoroutineScope(Main).launch {
                            mainViewModel.mFlowEvent.emit(true)
                        }
                    }
                    binding.viewPagerHome.setCurrentItem(
                        (activity as MainActivity).currentPager,
                        false
                    )
                    binding.tabLayoutBottomHome.selectTab(
                        binding.tabLayoutBottomHome.getTabAt(
                           (activity as MainActivity).currentPager
                        )
                    )

                }

                Constant.KEY_SCREEN_EMOJIS -> {
                    if (instance.idScreen == 2) {
                        binding.viewPagerHome.setCurrentItem(
                            2,
                            false
                        )
                        binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(2))
                    }
                    binding.viewPagerHome.setCurrentItem(
                        (activity as MainActivity).currentPager,
                        false
                    )
                    binding.tabLayoutBottomHome.selectTab(
                        binding.tabLayoutBottomHome.getTabAt(
                           (activity as MainActivity).currentPager
                        )
                    )
                }
            }
            requireActivity().intent.putExtra(Constant.KEY_OPEN_SCREEN, Bundle())
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessage(messageEvent: MessageEvent) {
        Log.d("duongcv", "onMessageqq: " + messageEvent.key)
        EventBus.getDefault().removeAllStickyEvents()
        when (messageEvent.key) {
            com.tapbi.spark.yokey.common.Constant.SCREEN_MORE -> {
                Timber.d("ducNQonMessage ");
                (activity as MainActivity).currentPager = 3
                //   binding.viewPagerHome.post {
                Timber.d("ducNQonMessage: qq");
                binding.viewPagerHome.setCurrentItem(3, false)
                binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(4))
                //   }
                //  Timber.d("ducNQ changeScreened: a: "+binding.tabLayoutBottomHome);
            }

            com.tapbi.spark.yokey.common.Constant.SCREEN_STICKER -> {//SCREEN_EMOJI
                (activity as MainActivity).currentPager = 2
                binding.viewPagerHome.setCurrentItem(2, false)
                binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(2))
            }

            com.tapbi.spark.yokey.common.Constant.SCREEN_FONT -> {
                (activity as MainActivity).currentPager = 1
                binding.viewPagerHome.setCurrentItem(1, false)
                binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(1))
            }

            com.tapbi.spark.yokey.common.Constant.SCREEN_LANGUAGE -> {
//                CommonUtil.invokeSubtypeEnablerOfThisIme(requireContext())
                Log.d("duongcv", "onMessage: language")
                (activity as MainActivity).currentPager = 3
                binding.viewPagerHome.setCurrentItem(3, false)
                binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(4))
                (activity as MainActivity).changeStartScreen(R.id.keyboardLanguageFragment, null)
            }

            com.tapbi.spark.yokey.common.Constant.MH_MY_THEME -> {
                MySharePreferences.putBoolean(Constant.CHECK_LOAD_ADS, false, requireContext())
                (activity as MainActivity).currentPager = 0
                binding.viewPagerHome.setCurrentItem(0, false)
                binding.tabLayoutBottomHome.selectTab(
                    binding.tabLayoutBottomHome.getTabAt(
                        0
                    )
                )
            }

            com.tapbi.spark.yokey.common.Constant.SCREEN_EMOJIS -> {
                (activity as MainActivity).currentPager = 2
                binding.viewPagerHome.setCurrentItem(2, false)
                binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(2))
            }

            com.tapbi.spark.yokey.common.Constant.SCREEN_THEME -> {
                (activity as MainActivity).currentPager = 0
                binding.viewPagerHome.setCurrentItem(0, false)
                binding.tabLayoutBottomHome.selectTab(binding.tabLayoutBottomHome.getTabAt(0))
            }

            CHECK_DOUBLE_CLICK -> {
                binding.imgPremium.isEnabled = true
                binding.imgCustomizeTheme.isEnabled = true
            }

        }
    }

    override fun onResume() {
        super.onResume()
        if ((activity as MainActivity).detailThemeObject != null) {
            (activity as MainActivity).viewModel?.mLiveDataDetailObject?.value = (activity as MainActivity).detailThemeObject
            (activity as MainActivity).viewModel?.mLiveDataThemeEntity?.value = (activity as MainActivity).detailThemeEntity
            (activity as MainActivity).detailThemeObject = null
            (activity as MainActivity).detailThemeEntity = null
            (activity as MainActivity).changeStartScreen(R.id.detailThemeFragment, null)
            (activity as MainActivity).viewModel?.mLiveDataThemeEntity?.postValue(null)
            (activity as MainActivity).viewModel?.mLiveDataDetailObject?.postValue(null)
        }else if ((activity as MainActivity).detailSticker != null){
            (activity as MainActivity).viewModel?.mLiveDataDetailObject?.value = (activity as MainActivity).detailSticker
            (activity as MainActivity).detailSticker = null
            (activity as MainActivity).changeStartScreen(R.id.detailStickerFragment, null)
            (activity as MainActivity).viewModel?.mLiveDataDetailObject?.postValue(null)
        }else {
            MySharePreferences.putBoolean(CHECK_LOAD_ADS, true, requireContext())
            App.activityResumed()
            // Open myTheme after create theme
            val bundle = arguments
            if (bundle != null) {
                val idScreen = bundle.getInt(Constant.KEY_OPEN_SCREEN, -1)
                if (idScreen != -1 && !MySharePreferences.getBooleanValue(
                        CHECK_LOAD_ADS,
                        requireContext()
                    )
                ) {
                    //(activity as MainActivity).currentPager = 0
                    EventBus.getDefault().postSticky(MessageEvent(idScreen))
                }
            }
            Log.d("duongcv", "onResume: " + waitingShowFont + ": " + itemFont)
            if (waitingShowFont && itemFont != null) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (App.isActivityVisible) {
                        Log.d("duongcv", "onResume: show")
                        mainViewModel.mLiveDataDetailObject.value = itemFont
                        waitingShowFont = false
                        nexScreenAfterAds()
                    }
                }, 1000)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        requireActivity().intent = Intent()
        outState.putInt(Constant.ID_SCREEN_CURRENT, getScreenIdFromTab())
        super.onSaveInstanceState(outState)
    }

    private fun getScreenIdFromTab(): Int {
        return try {
            when (binding.viewPagerHome.currentItem) {
                1 -> Constant.KEY_SCREEN_FONT
                2 -> Constant.KEY_SCREEN_STICKER
                3 -> Constant.KEY_SCREEN_MORE
                else -> defaultScreen
            }
        } catch (e: Exception) {
            defaultScreen
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
//        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun processRemoveAds(isRemoveAds: Boolean) {
        super.processRemoveAds(isRemoveAds)
        binding.imgPremium.visibility = if (isRemoveAds) View.INVISIBLE else View.VISIBLE
    }
}