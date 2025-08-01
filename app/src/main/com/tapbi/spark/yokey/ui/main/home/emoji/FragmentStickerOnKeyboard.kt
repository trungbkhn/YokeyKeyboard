package com.tapbi.spark.yokey.ui.main.home.emoji

import android.os.Bundle
import android.view.View
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentStickerOnKeyboardBinding
import com.tapbi.spark.yokey.data.local.entity.Sticker
import com.tapbi.spark.yokey.ui.adapter.ItemStickerOnKeyboardAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import java.util.ArrayList

class FragmentStickerOnKeyboard(private val sticker: Sticker) : BaseBindingFragment<FragmentStickerOnKeyboardBinding, StickerViewModel>() {
    private val itemStickerOnKeyboardAdapter: ItemStickerOnKeyboardAdapter? = null
    private val listSticker: ArrayList<String>? = null
    override fun getViewModel(): Class<StickerViewModel> = StickerViewModel::class.java

    override val layoutId: Int
        get() = R.layout.fragment_sticker_on_keyboard

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {

    }

    override fun onPermissionGranted() {

    }

}