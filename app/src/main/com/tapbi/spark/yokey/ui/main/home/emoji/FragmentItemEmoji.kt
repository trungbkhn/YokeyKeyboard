package com.tapbi.spark.yokey.ui.main.home.emoji

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentItemEmojiBinding
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant.CHECK_EMOJI_UPDATE_NEW_PHASE7
import com.tapbi.spark.yokey.data.local.entity.Emoji
import com.tapbi.spark.yokey.ui.adapter.EmojiAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.util.Constant.CHECK_KILLAPP_EMOJI
import timber.log.Timber

class FragmentItemEmoji constructor() :
    BaseBindingFragment<FragmentItemEmojiBinding, StickerViewModel>() {
    private var listEmoji = ArrayList<Emoji>()
    var type = 0
    private val emojiAdapter: EmojiAdapter by lazy {
        EmojiAdapter(listEmoji, object : EmojiAdapter.SetFavouriteEmoji {
            override fun changeStatusFavourite(position: Int) {
                if (listEmoji[position].favourite == 0) {
                    listEmoji[position].favourite = 1
                } else {
                    listEmoji[position].favourite = 0
                }
                viewModel.updateEmojiDB(listEmoji[position])
                emojiAdapter.changeItem(listEmoji, position)
            }
        })
    }

    override fun getViewModel(): Class<StickerViewModel> = StickerViewModel::class.java

    override val layoutId: Int
        get() = R.layout.fragment_item_emoji

    constructor(type: Int) : this() {
        this.type = type
    }

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        if(savedInstanceState!=null){
            type = savedInstanceState.getInt(CHECK_KILLAPP_EMOJI)
        }
        setUpData()
    }

    override fun onPermissionGranted() {}

    @SuppressLint("CommitPrefEdits")
    override fun onResume() {
        super.onResume()
    }

    @SuppressLint("CommitPrefEdits")
    private fun setUpData() {
        Timber.d("ducNQsetUpData " + type);
        viewModel.loadEmojiDB(type)
        viewModel.listEmojiTrending.observe(viewLifecycleOwner) { listEmojiTrending ->
            binding.spinKitEmoji.visibility = View.GONE
            listEmoji = listEmojiTrending as ArrayList<Emoji>
            emojiAdapter.changeList(listEmoji)
            App.instance.mPrefs!!.edit().putBoolean(CHECK_EMOJI_UPDATE_NEW_PHASE7, false)
                .apply()
        }
        binding.recyclerViewEmoji.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.recyclerViewEmoji.adapter = emojiAdapter
        binding.recyclerViewEmoji.itemAnimator = null
        binding.fastScroll.attachRecyclerView(binding.recyclerViewEmoji)
        binding.fastScroll.handlePressedColor = resources.getColor(R.color.scroll_bar)
        binding.fastScroll.attachAdapter(emojiAdapter)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CHECK_KILLAPP_EMOJI,type)
    }
}