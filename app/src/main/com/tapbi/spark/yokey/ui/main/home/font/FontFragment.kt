package com.tapbi.spark.yokey.ui.main.home.font

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentFontBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.local.db.ThemeDB
import com.tapbi.spark.yokey.data.local.entity.ItemFont
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.adapter.ViewPagerFontAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.custom.view.CustomLineGradient
import com.tapbi.spark.yokey.ui.custom.view.CustomTextViewGradient
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.Utils
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

class FontFragment : BaseBindingFragment<FragmentFontBinding, FontViewModel>() {
    private val viewPagerFontAdapter: ViewPagerFontAdapter by lazy { ViewPagerFontAdapter(this) }
    override fun getViewModel(): Class<FontViewModel> = FontViewModel::class.java

    override val layoutId: Int
        get() = R.layout.fragment_font

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        setUpViewPagerFont()
        setUpData()
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt((activity as MainActivity).currentPageFont))
        binding.pagerFont.setCurrentItem((activity as MainActivity).currentPageFont, false)
    }

    override fun onPermissionGranted() {

    }

    @SuppressLint("CommitPrefEdits")
    private fun setUpData() {
        App.instance.fontRepository?.loadAllFont(context, true)
        App.instance.fontRepository?.listFontsLiveData?.observe(
            viewLifecycleOwner
        ) { itemFonts: ArrayList<ItemFont?>? ->
            if (itemFonts == null || itemFonts.size == 0) {
                Timber.d("ducNQ setUpDatagg: 1");
                ThemeDB.getInstance(context)?.itemFontDAO()
                    ?.insertFontList(App.instance.fontRepository!!.listFontAddToDB)
                ThemeDB.getInstance(context)?.itemFontDAO()
                    ?.insertFontList(App.instance.fontRepository!!.insertNewFont())
                App.instance.fontRepository!!.loadAllFont(context, true)
               // App.instance.mPrefs!!.edit().putBoolean(Constant.CHECK_UPDATE_NEW_FONT_DATA, true).apply()
            } else {
                Timber.d("ducNQ setUpDatagg: 2");
                Bundle().apply {
                    putParcelableArrayList(
                        Constant.DATA_FONT,
                        itemFonts
                    )
                    val messageEvent = MessageEvent(Constant.SEND_DATA_FONT, null)
                    messageEvent.bundle = this
                    EventBus.getDefault().postSticky(messageEvent)
                }
            }
        }
    }

    private fun setUpViewPagerFont() {
        binding.pagerFont.adapter = ViewPagerFontAdapter(this)//viewPagerFontAdapter
        binding.pagerFont.offscreenPageLimit = 1
        TabLayoutMediator(binding.tabLayout, binding.pagerFont) { tab, _ ->
            tab.setCustomView(R.layout.custom_layout_tablayout)
        }.attach()
        binding.pagerFont.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                changeTabLayout(position)
                when (position) {
                    0 -> (activity as MainActivity).currentPageFont = 0
                    1 -> (activity as MainActivity).currentPageFont = 1
                    2 -> (activity as MainActivity).currentPageFont = 2
                    3 -> (activity as MainActivity).currentPageFont = 3
                    4 -> (activity as MainActivity).currentPageFont = 4
                    5 -> (activity as MainActivity).currentPageFont = 5
                    6 -> (activity as MainActivity).currentPageFont = 6
                    7 -> (activity as MainActivity).currentPageFont = 7
                    8 -> (activity as MainActivity).currentPageFont = 8
                }
                super.onPageSelected(position)
            }
        })
        for (i in 0 until 9) {
            if (binding.tabLayout.getTabAt(i) != null && binding.tabLayout.getTabAt(i)!!.customView != null) {
                binding.tabLayout.getTabAt(i)!!.customView?.findViewById<CustomTextViewGradient>(
                    R.id.txtNameTabLayout
                )?.setText(Utils.listTabFont[i])
            }
        }
        changeTabLayout(0)
    }

    private fun changeTabLayout(position: Int) {
        for (i in 0..8) {
            initTabItem(i, false)
        }
        initTabItem(position, true)
    }

    override fun onResume() {
        super.onResume()
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
        binding.pagerFont.currentItem = binding.pagerFont.currentItem
    }

    fun checkInternetAndSetupView(isCheck: Boolean) {
        if (isCheck) binding.llCheckInternet.visibility =
            View.GONE else binding.llCheckInternet.visibility =
            View.VISIBLE
    }


}