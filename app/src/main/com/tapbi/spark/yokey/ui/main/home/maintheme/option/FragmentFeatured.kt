package com.tapbi.spark.yokey.ui.main.home.maintheme.option

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentFeaturedBinding
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.ThemeObject
import com.tapbi.spark.yokey.ui.adapter.ItemsThemeAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.main.home.maintheme.ThemeViewModel
import com.tapbi.spark.yokey.util.Constant
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FragmentFeatured : BaseBindingFragment<FragmentFeaturedBinding, ThemeViewModel>(),
    ItemsThemeAdapter.ItemClickListener {
    private var objectThemeModel: ArrayList<ThemeObject> = ArrayList()

    // private var objectTheme: ThemeObject = ThemeObject()
    private lateinit var itemsThemeAdapter: ItemsThemeAdapter /*by lazy {
        ItemsThemeAdapter(
            requireContext(), objectThemeModel
        )
    }*/

    override fun getViewModel(): Class<ThemeViewModel> = ThemeViewModel::class.java

    override val layoutId: Int
        get() = R.layout.fragment_featured

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        setUpData()
        setUpAdapter()
    }

    override fun onPermissionGranted() {

    }

    private fun setUpData() {
        viewModel.loadThemeFeatured()
        viewModel.liveListThemeFeatured.observe(viewLifecycleOwner) {
            for (i in 0 until it.size) {
                val objectTheme: ThemeObject = ThemeObject()
                objectTheme.name = it[i].nameKeyboard
                objectTheme.id = it[i].id?.toLong()
                objectTheme.preview = it[i].popup?.preview?.bgImage
                objectTheme.typeKeyboard = it[i].typeKeyboard
                objectThemeModel.add(objectTheme)
            }
            App.instance.themeRepository?.listThemeFeaturedNew?.let { it1 ->
                objectThemeModel.addAll(
                    it1
                )
            }
            itemsThemeAdapter.setObjectThemes(objectThemeModel)
            App.instance.themeRepository?.addThemeTryKeyboardThread(objectThemeModel)
        }
    }

    private fun setUpAdapter() {
        itemsThemeAdapter =  ItemsThemeAdapter(
                requireContext(), mutableListOf()
        )
        binding.rcFeatured.adapter = itemsThemeAdapter
        binding.rcFeatured.itemAnimator = null
        binding.rcFeatured.layoutManager = GridLayoutManager(requireContext(), 2)
        itemsThemeAdapter.setItemClickListener(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        itemsThemeAdapter.notifyDataSetChanged()
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
            itemsThemeAdapter.notifyDataSetChanged()
        }
    }

    override fun onItemClickRecycleView(view: View?, position: Int, oldPosition: Int) {
        if(checkDoubleClick()) {
            mainViewModel.mLiveDataDetailObject.value = objectThemeModel[position]
            showAdsFull(getString(R.string.tag_inter_item_theme))
        }
    }


}