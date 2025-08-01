package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.Target
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.common.CommonVariable
import com.tapbi.spark.yokey.data.model.ThemeObject
import com.tapbi.spark.yokey.databinding.ItemThemeBinding
import com.tapbi.spark.yokey.ui.adapter.ItemsThemeAdapter.MyViewHolder
import timber.log.Timber

class ItemsThemeAdapter(
    private val context: Context,
    private var objectThemes: MutableList<ThemeObject>?
) : RecyclerView.Adapter<MyViewHolder>() {
    private var itemClickListener: ItemClickListener? = null
    var oldPositions = 0
    private var hashMapTheme: HashMap<Long, Long> = HashMap()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(ItemThemeBinding.inflate(LayoutInflater.from(parent.context)))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.onBinHolder(position)
    }

    override fun getItemCount(): Int {
        return if (objectThemes != null && objectThemes!!.isNotEmpty()) objectThemes!!.size else 0
    }

    inner class MyViewHolder(var itemThemeBinding: ItemThemeBinding) : RecyclerView.ViewHolder(
        itemThemeBinding.root
    ), View.OnClickListener {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun onBinHolder(position: Int) {
            if (objectThemes == null || (objectThemes?.size ?: 0) <= position) return
            if (objectThemes!![position] == null) return
            itemThemeBinding.tvThemeName.text = objectThemes!![position].name
            val mPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val subString: String? = objectThemes!![position].urlTheme
            var idFolder = "1"//end.substring(end.indexOf(".zip"))
            if (subString != null) {
                val end = subString!!.substring(
                    subString.indexOf(".com") + ".com".length + 1,
                    subString.length
                )
                idFolder = if (end.contains("/")) {
                    end.substring(0, end.indexOf("/"))
                } else {
                    end.substring(0, end.indexOf("."))
                }
            } else {
                idFolder = objectThemes!![position].id.toString()
            }
            if (mPrefs.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, "")!!
                    .equals(idFolder/*objectThemes!![position].id*/)
            ) {
//                Glide.with(context).asBitmap().placeholder(R.drawable.tick_off)
//                        .load(R.drawable.tick_on).into(itemThemeBinding.tickTheme)
                itemThemeBinding.tickTheme.setImageDrawable(
                    context.resources.getDrawable(
                        R.drawable.tick_on,
                        context.theme
                    )
                )
                oldPositions = position
            } else {
//                Glide.with(context).asBitmap().placeholder(R.drawable.tick_off)
//                        .load(R.drawable.tick_off).into(itemThemeBinding.tickTheme)
                itemThemeBinding.tickTheme.setImageDrawable(
                    context.resources.getDrawable(
                        R.drawable.tick_off,
                        context.theme
                    )
                )
            }

            Glide.with(context).asBitmap()
                .load(objectThemes!![position].preview)
                .override(400).listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>,
                        isFirstResource: Boolean
                    ): Boolean {
                        itemThemeBinding.shimmerViewLoading.visibility = View.VISIBLE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: Target<Bitmap>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        itemThemeBinding.shimmerViewLoading.visibility = View.GONE
                        return false
                    }

                })
                .into(BitmapImageViewTarget(itemThemeBinding.imgTheme))
            itemThemeBinding.tvThemeName.text = objectThemes!![position].name

            // itemView
            itemThemeBinding.vItemTheme.setOnClickListener { view ->
                if (itemClickListener != null) {
                    Timber.e("hachung objectThemes: ${objectThemes!![position].id}")
                    itemClickListener!!.onItemClickRecycleView(view, position, oldPosition)
                }
            }

            if (objectThemes!!.size % 2 == 0) {
                if (position == objectThemes!!.size - 1 || position == objectThemes!!.size - 2) {
                    itemThemeBinding.viewBottom.visibility = View.VISIBLE
                } else {
                    itemThemeBinding.viewBottom.visibility = View.GONE
                }
            } else {
                if (position == objectThemes!!.size - 1) {
                    itemThemeBinding.viewBottom.visibility = View.VISIBLE
                } else {
                    itemThemeBinding.viewBottom.visibility = View.GONE
                }
            }
        }

        override fun onClick(v: View) {}
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setObjectThemes(objectThemes: List<ThemeObject>?) {
        if (objectThemes != null) {
            for (i in 0 until objectThemes.size) {
                if (!hashMapTheme.containsKey(objectThemes[i].id)) {
                    hashMapTheme[objectThemes[i].id!!] = objectThemes[i].id!!
                    this.objectThemes!!.add(objectThemes[i])
                }
            }
            //this.objectThemes = objectThemes
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeObjectThemes(objectThemes: List<ThemeObject>?) {
        if (objectThemes != null) {
            this.objectThemes?.clear()
            this.objectThemes?.addAll(objectThemes)
            notifyDataSetChanged()
        }
    }

    interface ItemClickListener {
        fun onItemClickRecycleView(view: View?, position: Int, oldPosition: Int)
    }

    fun setItemClickListener(itemClickListener: ItemClickListener?) {
        this.itemClickListener = itemClickListener
    }
}