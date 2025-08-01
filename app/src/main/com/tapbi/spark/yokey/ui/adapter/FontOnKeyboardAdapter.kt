package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.databinding.ItemChangeFontBinding
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.data.local.entity.ItemFont
import com.tapbi.spark.yokey.data.model.Font
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.repository.FontRepository
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import org.greenrobot.eventbus.EventBus

class FontOnKeyboardAdapter(
    private var listFont: ArrayList<ItemFont>,
    private val context: Context,
    private var changeFont: ChangeFontOnKeyboard
) : RecyclerView.Adapter<FontOnKeyboardAdapter.GetViewHolder>() {
    private var listTitle: ArrayList<String>
    private val SIZE_10 = CommonUtil.dpToPx(App.instance, 10)


    @SuppressLint("NotifyDataSetChanged")
    fun changeList(listFont: ArrayList<ItemFont>) {
        listTitle = ArrayList()
        this.listFont = listFont
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder =
        GetViewHolder(
            ItemChangeFontBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    @SuppressLint("CommitPrefEdits", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: GetViewHolder, position: Int) {
        val itemFont = listFont[position]
        holder.binding.txtItemChangeFont.contentDescription = listFont[position].textFont
        if (position == listFont.size - 1) {
            holder.binding.imgAddFont.visibility = View.VISIBLE
            holder.binding.ctlFont.layoutParams.width = CommonUtil.dpToPx(context, 188)
        } else {
            holder.binding.imgAddFont.visibility = View.GONE
            holder.binding.ctlFont.layoutParams.width = CommonUtil.dpToPx(context, 138)
        }
        if (holder.binding.txtItemChangeFont.layoutParams.width > holder.binding.view.layoutParams.width - SIZE_10) {
            val param = holder.binding.txtItemChangeFont.layoutParams
            param.width = holder.binding.view.layoutParams.width - SIZE_10
            holder.binding.txtItemChangeFont.layoutParams = param
            holder.binding.txtItemChangeFont.requestLayout()
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            holder.binding.txtItemChangeFont.maxLines = 1
        }

        if (listTitle.size > position) {
            holder.binding.txtItemChangeFont.text = listTitle[position]
        } else {
            val c1 = StringBuilder()
            c1.append("")
            if (instance.fontRepository == null) instance.fontRepository = FontRepository()
            if (instance.fontRepository!!.font == null) instance.fontRepository?.font = Font()
            val charSequences = instance.fontRepository?.font?.getFont(itemFont.textFont)
            CommonUtil.appendText(
                instance.fontRepository!!.font,
                charSequences,
                c1,
                itemFont.textFont,
                4, if (itemFont.textFont.equals(Constant.FONT_NORMAL)) 11 else 10
            )
            listTitle.add(c1.toString())
            holder.binding.txtItemChangeFont.text = c1.toString()

        }
        if (instance.fontRepository!!.key_Font == itemFont.textFont) {
            if (instance.fontRepository != null && instance.fontRepository!!.key_Font == Constant.FONT_SQUARE_DASHED) {
                holder.binding.txtItemChangeFont.isTextGradient(false)
                holder.binding.txtItemChangeFont.text = listTitle[position]
            } else {
                holder.binding.txtItemChangeFont.isTextGradient(true)
            }
        } else {
            holder.binding.txtItemChangeFont.isTextGradient(false)
        }
        holder.binding.view.setOnClickListener {
            instance.mPrefs!!.edit().putString(Constant.USING_FONT, itemFont.textFont).apply()
            instance.fontRepository!!.updateCurrentFont()
            changeFont.changFont()
            notifyDataSetChanged()
        }

        holder.binding.imgAddFont.setOnClickListener {
            EventBus.getDefault().post(MessageEvent(Constant.EVENT_ADD_FONT, null))
        }
    }

    override fun getItemCount(): Int = listFont.size


    inner class GetViewHolder(val binding: ItemChangeFontBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    init {
        listTitle = ArrayList()
    }

    interface ChangeFontOnKeyboard {
        fun changFont()
    }
}