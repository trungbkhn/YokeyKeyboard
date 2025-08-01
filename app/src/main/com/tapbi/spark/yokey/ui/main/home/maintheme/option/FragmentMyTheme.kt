package com.tapbi.spark.yokey.ui.main.home.maintheme.option

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentMyThemeBinding
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.ThemeObject
import com.tapbi.spark.yokey.ui.adapter.MyThemeAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.main.home.maintheme.ThemeViewModel
import com.tapbi.spark.yokey.util.Constant
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FragmentMyTheme : BaseBindingFragment<FragmentMyThemeBinding, ThemeViewModel>(),
    MyThemeAdapter.IListenerMyTheme {

    private lateinit var listMyTheme: MutableList<ThemeEntity>
    private lateinit var listMyThemeEntity: MutableList<ThemeEntity>
    private lateinit var myThemeAdapter: MyThemeAdapter
    private var themeEntityCurrent: ThemeEntity? = null
    override fun getViewModel(): Class<ThemeViewModel> = ThemeViewModel::class.java

    override val layoutId: Int get() = R.layout.fragment_my_theme

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {

    }

    override fun onPermissionGranted() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        listener()
    }

    private var mPrefs: SharedPreferences? = null
    private fun listener() {
        viewModel.liveListMyTheme.observe(
            viewLifecycleOwner
        ) { t ->
            if (t != null) {
                listMyThemeEntity = t
                binding.spinKitLoadMyTheme.visibility = View.GONE
                myThemeAdapter.changeList(t)
                if (t.size == 0) {
                    binding.textTheme.visibility = View.VISIBLE
                } else {
                    binding.textTheme.visibility = View.INVISIBLE
                }
            }
        }
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        viewModel.loadMyTheme()
    }

    private fun init() {
        listMyTheme = ArrayList()
        myThemeAdapter = MyThemeAdapter(listMyTheme, requireContext(), this)
        binding.rcMyTheme.animation = null
        binding.rcMyTheme.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rcMyTheme.adapter = myThemeAdapter
    }

    override fun clickItemMyTheme(position: Int) {
        if (checkDoubleClick()) {
            themeEntityCurrent = listMyThemeEntity[position]

            val themeObject: ThemeObject = ThemeObject()
            // App.instance.themeModel.popup.preview.bgImage
            themeObject.name = themeEntityCurrent!!.name
            themeObject.id = themeEntityCurrent!!.id.toLong()
            themeObject.preview = themeEntityCurrent!!.preview
            themeObject.typeKeyboard = themeEntityCurrent!!.typeKeyboard
            themeObject.downloadCount = themeEntityCurrent!!.downloadCount
            themeObject.isHotTheme = 0
            themeObject.idCategory = com.tapbi.spark.yokey.common.Constant.ID_THEME_LED
            themeObject.urlCoverTopTheme = themeEntityCurrent!!.urlCoverTopTheme
            themeObject.urlTheme = themeEntityCurrent!!.urlTheme
            mainViewModel.mLiveDataDetailObject.value = themeObject
            mainViewModel.mLiveDataThemeEntity.value = listMyThemeEntity[position]
            showAdsFull(getString(R.string.tag_inter_item_theme))
        }
    }

    override fun deleteItemMyTheme(themeEntity: ThemeEntity, size: Int) {
        viewModel.deleteTheme(themeEntity)
        if (size == 1) {
            binding.textTheme.visibility = View.VISIBLE
        } else {
            binding.textTheme.visibility = View.INVISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        myThemeAdapter.notifyDataSetChanged()
        EventBus.getDefault().register(this)
        super.onResume()
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(messageEvent: MessageEvent) {
        val key = messageEvent.key
        if (key == Constant.KEY_CHANGE_THEME || key == Constant.KEY_CHANGE_THEME_NOT_SHOW_PREVIEW) {
            myThemeAdapter.notifyDataSetChanged()
        }
    }
}