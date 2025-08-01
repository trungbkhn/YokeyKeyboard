package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.databinding.ItemFeaturedBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tapbi.spark.yokey.data.model.theme.ThemeModel

class FeaturedAdapter constructor(val context: Context,
                                  private var listFeaturedTheme: List<ThemeModel>,
                                  var iListenerThemeFeatured: IListenerThemeFeatured
) :
    RecyclerView.Adapter<FeaturedAdapter.HolderFeatured>() {
    //private var listFeaturedTheme: List<ThemeModel>? = null

    class HolderFeatured(val binding: ItemFeaturedBinding) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun changeListFeatured(listFeaturedTheme: List<ThemeModel>) {
        this.listFeaturedTheme = listFeaturedTheme
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderFeatured =
        HolderFeatured(
            ItemFeaturedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: HolderFeatured, position: Int) {
        holder.binding.txtNameTheme.text = listFeaturedTheme[position].nameKeyboard
        Glide.with(context).asBitmap().load(listFeaturedTheme[position].background?.backgroundImage)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    holder.binding.imgFeaturedTheme.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

            })

        holder.binding.imgFeaturedTheme.setOnClickListener {
            iListenerThemeFeatured.clickItemThemeFeatured(position)
        }
    }

    override fun getItemCount(): Int = listFeaturedTheme.size

    interface IListenerThemeFeatured {
        fun clickItemThemeFeatured(position: Int)

    }

}