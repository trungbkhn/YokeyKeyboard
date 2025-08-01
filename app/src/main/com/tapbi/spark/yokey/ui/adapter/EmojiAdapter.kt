package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.ItemEmojiBinding
import com.tapbi.spark.yokey.data.local.entity.Emoji
import java.util.*

class EmojiAdapter constructor(
    var listEmoji: List<Emoji>,
    var setFavouriteEmoji: SetFavouriteEmoji
) :
    RecyclerView.Adapter<EmojiAdapter.HolderEmoji>() {
    private var intArray =
        intArrayOf(R.drawable.circle_top1, R.drawable.circle_top2, R.drawable.circle_top3)
    private var maxWidth = 0

    class HolderEmoji(var binding: ItemEmojiBinding) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun changeList(listEmoji: List<Emoji>) {
        this.listEmoji = listEmoji
        notifyDataSetChanged()
    }

    fun changeItem(listEmoji: List<Emoji>, position: Int) {
        this.listEmoji = listEmoji
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderEmoji = HolderEmoji(
        ItemEmojiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: HolderEmoji,
        @SuppressLint("RecyclerView") position: Int
    ) {
        holder.binding.txtContent.text = listEmoji[position].content!!.trim()
        if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            holder.binding.txtContent.gravity = Gravity.END
        } else {
            holder.binding.txtContent.gravity = Gravity.START
        }
        holder.binding.txtTitle.text = listEmoji[position].title
        for (i in 0 until 3) {
            if (holder.adapterPosition == i) {
                holder.binding.textQuantilyItem.setBackgroundResource(intArray[i])
                holder.binding.textQuantilyItem.setTextColor(Color.WHITE)
            }
        }
        if (holder.layoutPosition >= 3) {
            holder.binding.textQuantilyItem.setBackgroundResource(R.color.color_transparent)
            holder.binding.textQuantilyItem.setTextColor(Color.parseColor("#777777"))
        }
        if (position == listEmoji.size - 1) {
            holder.binding.viewBottom.visibility = View.VISIBLE
        } else {
            holder.binding.viewBottom.visibility = View.GONE
        }
        if (position == 0) {
            holder.binding.viewTop.visibility = View.VISIBLE
        }else{
            holder.binding.viewTop.visibility = View.GONE
        }
        holder.binding.textQuantilyItem.text = "${position + 1}"

        if (listEmoji[position].favourite == 0) {
            holder.binding.imgFavourite.setImageResource(R.drawable.icon_heart_empty)
            holder.binding.countFavourite.text = listEmoji[position].count_favourite.toString()
        } else {
            holder.binding.imgFavourite.setImageResource(R.drawable.icon_heart_fill)
            holder.binding.countFavourite.text =
                (listEmoji[position].count_favourite!! + 1).toString()
        }

        holder.binding.imgFavourite.setOnClickListener {
            setFavouriteEmoji.changeStatusFavourite(position)

        }
    }

    override fun getItemCount(): Int = listEmoji.size

    interface SetFavouriteEmoji {
        fun changeStatusFavourite(position: Int)
    }
}