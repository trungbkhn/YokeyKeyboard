package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.databinding.ItemEmojiFavouriteBinding
import com.tapbi.spark.yokey.data.local.entity.Emoji

class EmojiFavouriteAdapter(
    var listEmoji: ArrayList<Emoji>,
    var context: Context,
    var setEmojiFavourite: SetEmojiFavourite
) : RecyclerView.Adapter<EmojiFavouriteAdapter.GetViewHolder>() {
    class GetViewHolder(var binding: ItemEmojiFavouriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder {
        return GetViewHolder(
            ItemEmojiFavouriteBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeList(listEmoji: ArrayList<Emoji>) {
        this.listEmoji = listEmoji
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: GetViewHolder, position: Int) {
        val emoji = listEmoji[position]
        if (listEmoji.size == 0) {
            holder.binding.llEmoji.visibility = View.GONE
            holder.binding.btnAddEmoji.visibility = View.VISIBLE
            holder.binding.vLineEmoji.visibility = View.GONE
        }

        if (listEmoji.size - 1 == position) {
            holder.binding.btnAddEmoji.visibility = View.VISIBLE
        } else {
            holder.binding.btnAddEmoji.visibility = View.GONE
        }


        holder.binding.tvContentEmoji.text = emoji.content
        holder.binding.txtTitleEmoji.text = emoji.title
        holder.binding.llEmoji.setOnClickListener {
            emoji.content?.let { it1 -> setEmojiFavourite.setEmoji(it1) }
        }
        holder.binding.btnAddEmoji.setOnClickListener {
            setEmojiFavourite.clickAddEmoji()
            //EventBus.getDefault().post(MessageEvent(Constant.MH_EMOJI))
        }
    }

    override fun getItemCount(): Int {
        if (listEmoji.size > 0) return listEmoji.size
        return 0
    }

    interface SetEmojiFavourite {
        fun setEmoji(emoji: String)
        fun clickAddEmoji()
    }


}