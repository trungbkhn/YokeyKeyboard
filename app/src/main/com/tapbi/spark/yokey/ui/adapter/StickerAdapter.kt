package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tapbi.spark.yokey.data.local.entity.Sticker
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.databinding.ItemStickerBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import java.util.*

class StickerAdapter(private var listSticker: ArrayList<Sticker>?, private val context: Context) :
    RecyclerView.Adapter<StickerAdapter.GetViewHolder>() {
    private var listenerChangeItemSticker: ListenerChangeItemSticker? = null
    @SuppressLint("NotifyDataSetChanged")
    fun changeList(listSticker: ArrayList<Sticker>?) {
        this.listSticker = listSticker
        notifyDataSetChanged()
    }

    fun setListenerChangeItemFont(listenerChangeItemSticker: ListenerChangeItemSticker?) {
        this.listenerChangeItemSticker = listenerChangeItemSticker
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder {
        val layoutInflater = LayoutInflater.from(
            context
        )
        val binding = ItemStickerBinding.inflate(layoutInflater)
        return GetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GetViewHolder, position: Int) {
        val sticker: Sticker? = listSticker!![position]

        //underline
        sticker?.apply {
            Glide.with(context).asBitmap().load(sticker.thumb)
                .into(BitmapImageViewTarget(holder.binding.imgThumb))
            holder.binding.txtNameSticker.text = sticker.name
            holder.binding.cv.setOnClickListener {
                if (listenerChangeItemSticker != null){
                    listenerChangeItemSticker!!.getItem(
                        sticker,
                        position
                    )
                }
            }
        }
      
        if (listSticker!!.size % 2 == 0) {
            if (position == listSticker!!.size - 1 || position == listSticker!!.size - 2) {
                holder.binding.viewBottom.visibility = View.VISIBLE
            } else {
                holder.binding.viewBottom.visibility = View.GONE
            }
        } else {
            if (position == listSticker!!.size - 1) {
                holder.binding.viewBottom.visibility = View.VISIBLE
            } else {
                holder.binding.viewBottom.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return if (listSticker != null) {
            listSticker!!.size
        } else 0
    }

    inner class GetViewHolder(val binding: ItemStickerBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    interface ListenerChangeItemSticker {
        fun getItem(itemSticker: Sticker?, position: Int)
    }
}