package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.ItemFontOnKeyboardBinding
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.data.local.entity.ItemFont
import com.tapbi.spark.yokey.data.model.Font
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.Constant.FONT_ROUND_STAMP
import timber.log.Timber

class AllFontOnKeyboardAdapter(
    /* private var listFont: ArrayList<ItemFont>,*/
    private val context: Context
) : RecyclerView.Adapter<AllFontOnKeyboardAdapter.GetViewHolder>() {
    private var listTitle: ArrayList<String> = ArrayList()
    private var listFont: ArrayList<ItemFont> = ArrayList()
    private var listenerChangeItemFont: ListenerChangeItemFont? = null
    var font = Font()
    private var idCurrent = -1
    private var strColor = -1

    @SuppressLint("NotifyDataSetChanged")
    fun changeList(listFonts: ArrayList<ItemFont>) {
        idCurrent = -1
        this.listFont.clear()
        this.listFont.addAll(listFonts)
        strColor = instance.colorIconDefault
        notifyDataSetChanged()
    }

    fun changeListItem(listFont: ArrayList<ItemFont>, position: Int) {
        this.listFont = listFont
        notifyItemChanged(position)
    }

    fun setListenerChangeItemFont(listenerChangeItemFont: ListenerChangeItemFont?) {
        this.listenerChangeItemFont = listenerChangeItemFont
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder =
        GetViewHolder(
            ItemFontOnKeyboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    @SuppressLint("CommitPrefEdits", "SetTextI18n")
    override fun onBindViewHolder(
        holder: GetViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {


        val itemFont = listFont[position]

        holder.binding.txtTitleFont.setTextColor(strColor)
        if (listTitle.size > position) {
            if (listFont[position].textFont.equals(FONT_ROUND_STAMP)) {
                holder.binding.txtTitleFont.text = "\t" + listTitle[position]
            } else {
                holder.binding.txtTitleFont.text = listTitle[position]
            }
        } else {
            val sp = font.getFont(itemFont.textFont)
            val c1 = StringBuilder()
            c1.append("")
            CommonUtil.appendText(font, sp, c1, itemFont.textFont, 4)
            listTitle.add(c1.toString())
            holder.binding.txtTitleFont.text = c1.toString()
        }
        if (itemFont.isAdd == 1) {
            holder.binding.imgAddFont.visibility = View.GONE
            holder.binding.imgUsingFont.visibility = View.VISIBLE
            if (instance.fontRepository!!.key_Font == itemFont.textFont) {
                holder.binding.imgUsingFont.setImageResource(R.drawable.ic_circle_fill)
                idCurrent = position
            } else {
                holder.binding.imgUsingFont.setImageResource(R.drawable.ic_circle_stroke)
            }
        } else {
            holder.binding.imgAddFont.visibility = View.VISIBLE
            holder.binding.imgUsingFont.visibility = View.GONE
        }
        if (listFont[position].textFont.equals(FONT_ROUND_STAMP)) {
            holder.binding.txtTitleFont.text = "\t" + listTitle[position]
        }
//        if(instance.listFontNotUsed.isEmpty()){
//            idCurrent=-1
//        }
        holder.itemView.setOnClickListener {
            Timber.d("imgUsingFont " + listFont[position].textFont)
            if (idCurrent != position && holder.binding.imgUsingFont.visibility == View.VISIBLE/*&&!instance.listFontNotUsed.containsKey(listFont[position].textFont)*/) {
                instance.mPrefs!!.edit().putString(Constant.USING_FONT, itemFont.textFont).apply()
                instance.fontRepository!!.updateCurrentFont()
                instance.fontRepository!!.loadListFontIsAdd()
                if (idCurrent >= 0) notifyItemChanged(idCurrent)
                idCurrent = position
                // notifyDataSetChanged()
                notifyItemChanged(idCurrent, true)
            }/* else {
                if (instance.listFontNotUsed.isNotEmpty()) {
                    if (instance.listFontNotUsed.containsKey(listFont[position].textFont)) {
                        Toast.makeText(
                            instance,
                            instance.resources.getString(R.string.not_support_font),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }*/
        }
        holder.binding.imgAddFont.setOnClickListener {
            if (listenerChangeItemFont != null) {
                listenerChangeItemFont!!.getItem(itemFont, position)
            }
        }
    }

    override fun getItemCount(): Int = listFont.size


    class GetViewHolder(val binding: ItemFontOnKeyboardBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    interface ListenerChangeItemFont {
        fun getItem(itemFont: ItemFont?, position: Int)
    }

    init {
        listTitle = ArrayList()
    }
}