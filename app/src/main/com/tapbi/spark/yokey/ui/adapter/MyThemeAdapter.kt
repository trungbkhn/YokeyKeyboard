package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.ItemMyThemeBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.CommonVariable
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity
import com.tapbi.spark.yokey.ui.dialog.DialogDeleteTheme
import com.tapbi.spark.yokey.util.CommonUtil
import timber.log.Timber

class MyThemeAdapter(
    private var listThemeEntity: MutableList<ThemeEntity>,
    var context: Context,
    var iListenerMyTheme: IListenerMyTheme
) :
    RecyclerView.Adapter<MyThemeAdapter.HolderMyTheme>(), DialogDeleteTheme.IListenerDelete {
    private val dialogDeleteTheme: DialogDeleteTheme by lazy { DialogDeleteTheme(this, context) }
    private var themeEntityCurrent: ThemeEntity? = null
    private var currentPosition = -1;

    class HolderMyTheme(val binding: ItemMyThemeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderMyTheme =
        HolderMyTheme(
            ItemMyThemeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    @SuppressLint("NotifyDataSetChanged")
    fun changeList(listThemeEntity: MutableList<ThemeEntity>) {
        this.listThemeEntity = listThemeEntity
        notifyDataSetChanged()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(
        holder: HolderMyTheme,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val themeEntity = listThemeEntity[position]
        holder.binding.txtNameTheme.text = themeEntity.name
        if (themeEntity.id == App.instance.mPrefs!!.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, "")) {
            holder.binding.tickMyTheme.setImageResource(R.drawable.tick_on)
        } else {
            holder.binding.tickMyTheme.setImageResource(R.drawable.tick_off)
        }
        if (themeEntity.preview != null) Glide.with(context).load(themeEntity.preview)
            .into(holder.binding.imgMyTheme)
        if (listThemeEntity.size % 2 == 0) {
            if (position == listThemeEntity.size - 1 || position == listThemeEntity.size - 2) {
                holder.binding.viewBottom.visibility = View.VISIBLE
            } else {
                holder.binding.viewBottom.visibility = View.GONE
            }
        } else {
            if (position == listThemeEntity.size - 1) {
                holder.binding.viewBottom.visibility = View.VISIBLE
            } else {
                holder.binding.viewBottom.visibility = View.GONE
            }
        }
        holder.binding.imgDelete.setOnClickListener {
            if(listThemeEntity[position].id != App.instance.mPrefs!!.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, "")) {
                themeEntityCurrent = listThemeEntity[position]
                currentPosition = position
                dialogDeleteTheme?.apply {
                    show()
                }
            }else{
                CommonUtil.customToast(context,context.resources.getString(R.string.detail_theme_error_remove_theme_current))
            }
        }

        holder.binding.imgMyTheme.setOnClickListener {
            iListenerMyTheme.clickItemMyTheme(position)
        }
        Timber.e("ducNQ onResourceReady: "+themeEntity.preview);
        if (themeEntity.preview != null) Glide.with(context).asBitmap().load(themeEntity.preview)
             .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Timber.e("ducNQ onResourceReady: "+resource);
                    holder.binding.imgMyTheme.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

            })
    }

    override fun getItemCount(): Int {
        if (listThemeEntity.isNotEmpty()) return listThemeEntity.size
        return 0
    }


    override fun notDelete() {
        dialogDeleteTheme.dismiss()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun agreeDelete() {
        iListenerMyTheme.deleteItemMyTheme(themeEntityCurrent!!,listThemeEntity.size)
        listThemeEntity.removeAt(currentPosition)
        notifyDataSetChanged()
        dialogDeleteTheme.dismiss()
    }

    interface IListenerMyTheme {
        fun clickItemMyTheme(position: Int)
        fun deleteItemMyTheme(themeEntity: ThemeEntity, size:Int)
    }

}