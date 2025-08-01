package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tapbi.spark.yokey.data.model.StickerOnKeyboard
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.ItemTabStickerBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import java.util.ArrayList

class ItemTabStickerAdapter(
    private var listSticker: ArrayList<StickerOnKeyboard>,
    private val context: Context,
    private val listenerPosition: ListenerPosition?
) : RecyclerView.Adapter<ItemTabStickerAdapter.GetViewHolder>() {
    private var current = 1
    private val mPrefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            context
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeList(stickerOnKeyboards: ArrayList<StickerOnKeyboard>, current: Int) {
        listSticker = stickerOnKeyboards
        this.current = current
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder =
        GetViewHolder(
            ItemTabStickerBinding.inflate(
                LayoutInflater.from(
                    context
                )
            )
        )

    fun getCurrentPossition(): Int {
        return current
    }

    @SuppressLint("NotifyDataSetChanged", "CommitPrefEdits")
    override fun onBindViewHolder(
        holder: GetViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        if (position == 0) {
            (holder.itemTabStickerBinding.imgItemTabSticker.setImageResource(R.drawable.ic_recent))
        } else {
            val path = listSticker[position].thumb
            Glide.with(context).asBitmap().load(path)
                .into(BitmapImageViewTarget(holder.itemTabStickerBinding.imgItemTabSticker))
        }
        if (current == position) {
            holder.itemTabStickerBinding.viewLine.visibility = View.VISIBLE
        } else {
            holder.itemTabStickerBinding.viewLine.visibility = View.GONE
        }
        holder.itemTabStickerBinding.cvItemTabSticker.setOnClickListener {
            if (current != position) {
                current = position
                listenerPosition?.getPosition(position)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = listSticker.size

    inner class GetViewHolder(val itemTabStickerBinding: ItemTabStickerBinding) :
        RecyclerView.ViewHolder(
            itemTabStickerBinding.root
        )

    interface ListenerPosition {
        fun getPosition(position: Int)
    }
}