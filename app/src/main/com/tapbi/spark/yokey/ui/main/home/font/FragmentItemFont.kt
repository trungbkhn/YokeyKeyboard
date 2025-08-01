package com.tapbi.spark.yokey.ui.main.home.font

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentItemFontBinding
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.data.local.entity.ItemFont
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.adapter.FontAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.util.Constant
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FragmentItemFont constructor() :
    BaseBindingFragment<FragmentItemFontBinding, FontViewModel>(),
    FontAdapter.ListenerChangeItemFont {
    private val listFonts = ArrayList<ItemFont>()
    private var itemFontCurrent: ItemFont? = null
    private val fontAdapter: FontAdapter by lazy { FontAdapter(listFonts, requireContext()) }
    private var keyTab = Constant.KEY_TAB_ALL
    private var listFontAll: ArrayList<ItemFont>? = null
    private var currentPosition = 0
    override fun getViewModel(): Class<FontViewModel> = FontViewModel::class.java

    constructor(key_tab: String) : this() {
        this.keyTab = key_tab
    }

    override val layoutId: Int
        get() = R.layout.fragment_item_font

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        binding.spinKit.visibility = View.VISIBLE
        setUpAdapter()
        setUpData()
        //Timber.d("ducNQ onCreatedViewzz: "+ instance.fontRepository!!.listAllFont.size);
    }

    override fun onPermissionGranted() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
    }

    private fun setUpAdapter() {
        binding.recyclerViewFont.adapter = fontAdapter
        binding.recyclerViewFont.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewFont.itemAnimator = null
    }

    private fun setUpData() {
        listFonts.clear()
        listFonts.addAll(instance.fontRepository!!.getCategoryFont(keyTab))
        binding.spinKit.visibility = View.GONE
        fontAdapter.changeList(instance.fontRepository!!.getCategoryFont(keyTab))
/*        instance.fontRepository!!.getLiveCategory(keyTab)
            .observe(viewLifecycleOwner,
                { itemFonts ->
                    listFonts.clear()
                    listFonts.addAll(itemFonts)
                    binding.spinKit.visibility = View.GONE
                    fontAdapter.changeList(listFonts)
                })*/
        if (listFonts.size > 0) {
            binding.spinKit.visibility = View.GONE
            fontAdapter.changeList(listFonts)
        } else {
            if (listFontAll != null && listFontAll!!.size > 0) instance.fontRepository!!.loadFontByCategoriThread(
                listFontAll,
                keyTab
            )
        }
        fontAdapter.setListenerChangeItemFont(this)
    }
    override fun onResume() {
        if (listFonts.size == 0) {
            setUpData()
        }
        super.onResume()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessage(messageEvent: MessageEvent) {
        val key = messageEvent.key
        if (key == Constant.SEND_DATA_FONT) {
            val bundle = messageEvent.bundle
            listFontAll = bundle?.getParcelableArrayList(Constant.DATA_FONT)
            if (listFontAll != null && listFontAll!!.size > 0) {
                if (listFonts.size == 0) {
                    instance.fontRepository!!.loadFontByCategoriThread(listFontAll, keyTab)
                } else {
                    for (itemFont in listFontAll!!) {
                        for (i in listFonts.indices) {
                            if (itemFont.id == listFonts[i].id) {
                                listFonts[i].isAdd = itemFont.isAdd
                                fontAdapter.changeListItem(listFonts, i)
                                break
                            }
                        }
                    }
                }
            }
        }
        if (key == Constant.KEY_CHANGE_FONT) {
            instance.fontRepository!!.loadAllFont(requireContext(), true)
        }
    }

    override fun getItem(itemFont: ItemFont?, position: Int) {
        if(checkDoubleClick()) {
            mainViewModel.mLiveDataDetailObject.value = itemFont
            showAdsFull(getString(R.string.tag_inter_item_font))
            currentPosition = position
        }
    }


}