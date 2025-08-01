package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.ItemCategorySymbolsBinding
import com.tapbi.spark.yokey.App

class CategorySymbolsAdapter(
    var context: Context,
    var changeCategoryListener: ChangeCategoryListener,
    var colorUse: Int,
    var colorNotUse: Int
) : RecyclerView.Adapter<CategorySymbolsAdapter.GetViewHolder>() {

    private var id = 1;

    class GetViewHolder(var binding: ItemCategorySymbolsBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeColor(colorUse: Int, colorNotUse: Int) {
        this.colorUse = colorUse
        this.colorNotUse = colorNotUse
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder {
        return GetViewHolder(
            ItemCategorySymbolsBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: GetViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        when (position) {
            0 -> {
                holder.binding.imgItemCategory.setImageResource(R.drawable.ic_recent)
            }
            1 -> {
                holder.binding.imgItemCategory.setImageResource(R.drawable.ic_emoji_category)
            }
            else -> {
                holder.binding.imgItemCategory.setImageResource(R.drawable.ic_decorative_category)
            }
        }

        if (position == App.instance.idCategorySymbols) {
            holder.binding.imgItemCategory.setColorFilter(colorUse)
        } else {
            holder.binding.imgItemCategory.setColorFilter(colorNotUse)
        }

        holder.binding.imgItemCategory.setOnClickListener {
            if (position !=  App.instance.idCategorySymbols) {
                App.instance.idCategorySymbols = position
                changeCategoryListener.changeCategory(position)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = 3

    interface ChangeCategoryListener {
        fun changeCategory(position: Int)
    }
}