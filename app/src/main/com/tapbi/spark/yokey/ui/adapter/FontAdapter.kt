package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import com.tapbi.spark.yokey.data.local.entity.ItemFont
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.databinding.ItemFontBinding
import com.tapbi.spark.yokey.util.CommonUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.data.model.Font
import com.tapbi.spark.yokey.util.Constant
import java.lang.StringBuilder
import java.util.ArrayList

class FontAdapter(private var listFont: ArrayList<ItemFont>?, private val context: Context) :
    RecyclerView.Adapter<FontAdapter.GetViewHolder>() {
    private var listTitle: ArrayList<String>
    private var listDemo: ArrayList<String>
    private val mPrefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            instance
        )
    }
    private var listenerChangeItemFont: ListenerChangeItemFont? = null
    var font = Font()
    var isAdd = 1

    @SuppressLint("NotifyDataSetChanged")
    fun changeList(listFont: ArrayList<ItemFont>?) {
        listTitle = ArrayList()
        listDemo = ArrayList()
        this.listFont = listFont
        notifyDataSetChanged()
    }

    fun changeListItem(listFont: ArrayList<ItemFont>?, position: Int) {
        this.listFont = listFont
        notifyItemChanged(position)
    }

    fun setListenerChangeItemFont(listenerChangeItemFont: ListenerChangeItemFont?) {
        this.listenerChangeItemFont = listenerChangeItemFont
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeIsAdd(isAdd: Int) {
        this.isAdd = isAdd
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder =
        GetViewHolder(
            ItemFontBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: GetViewHolder, position: Int) {
        if (listFont != null && listFont!!.size > position) {
            val itemFont : ItemFont? = listFont!![position]
            if (listTitle.size > position) {
                holder.binding.txtNameFont.text =
                    if (itemFont?.textFont== Constant.FONT_STOP || itemFont?.textFont.equals(
                            Constant.FONT_ROUND_STAMP
                        )
                    ) " " + listTitle[position] else listTitle[position]
                holder.binding.txtDemoFont.text = listDemo[position].trim { it <= ' ' }
            } else {
                val sp = font.getFont(itemFont?.textFont)
                val c = StringBuilder()
                c.append("")
                CommonUtil.appendText(font, sp, c, itemFont?.textDemo, 0)
                val c1 = StringBuilder()
                c1.append("")
                CommonUtil.appendText(font, sp, c1, itemFont?.textFont, 0)
                listTitle.add(c1.toString())
                listDemo.add(c.toString())
                holder.binding.txtNameFont.text =
                    if (itemFont?.textFont == Constant.FONT_STOP || itemFont?.textFont.equals(
                            Constant.FONT_ROUND_STAMP
                        )
                    ) " $c1" else c1.toString()
                holder.binding.txtDemoFont.text = c.toString()
            }
            itemFont?.apply {
                if (itemFont.isAdd == isAdd) {
                    holder.binding.cvAddFont.visibility = View.GONE
                } else {
                    holder.binding.cvAddFont.visibility = View.VISIBLE
                }

                //underline
                Glide.with(context).asBitmap().load(itemFont.imgBackground)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onLoadCleared(placeholder: Drawable?) {}
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            holder.binding.imgBackgroundFont.setImageBitmap(resource)
                        }
                    })
                holder.binding.itemFont.setOnClickListener {
                    if (listenerChangeItemFont != null) listenerChangeItemFont!!.getItem(
                        itemFont,
                        position
                    )
                }
            }


            if (listFont!!.size % 2 == 0) {
                if (position == listFont!!.size - 1 || position == listFont!!.size - 2) {
                    holder.binding.viewBottom.visibility = View.VISIBLE
                } else {
                    holder.binding.viewBottom.visibility = View.GONE
                }
            } else {
                if (position == listFont!!.size - 1) {
                    holder.binding.viewBottom.visibility = View.VISIBLE
                } else {
                    holder.binding.viewBottom.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (listFont != null) {
            listFont!!.size
        } else 0
    }

    inner class GetViewHolder(val binding: ItemFontBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    interface ListenerChangeItemFont {
        fun getItem(itemFont: ItemFont?, position: Int)
    }

    init {
        listTitle = ArrayList()
        listDemo = ArrayList()
    }
}