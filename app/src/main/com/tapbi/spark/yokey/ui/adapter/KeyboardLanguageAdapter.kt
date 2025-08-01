package com.tapbi.spark.yokey.ui.adapter

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.ItemRcvKeyboardLanguageBinding
import com.tapbi.spark.yokey.data.local.LanguageEntity
import timber.log.Timber
import java.util.*

class KeyboardLanguageAdapter() :
    RecyclerView.Adapter<KeyboardLanguageAdapter.LanguageViewHolder>() {

    var languageEntities: ArrayList<LanguageEntity> = ArrayList()
    var mPrefs: SharedPreferences? = null
    var context: Context? = null
    private var onItemLanguageClickListener: OnItemLanguageClickListener? = null
    private var isUseSystem = false
    private var pos = 0


    constructor  (
        context: Context,
        languageEntities: ArrayList<LanguageEntity>,
        mPrefs: SharedPreferences?
    ) : this() {
        this.languageEntities = languageEntities
        this.mPrefs = mPrefs
        this.context = context
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): LanguageViewHolder {
        return LanguageViewHolder(
            ItemRcvKeyboardLanguageBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: LanguageViewHolder, position: Int) {
        val languageEntity = languageEntities[position]
        Timber.e("ducNQ onBindViewHolder: " + languageEntity.isEnabled);
        Timber.e("ducNQ onBindViewHolder: " + languageEntity.locale);
        Timber.e("ducNQ onBindViewHolder:===================== ");
        viewHolder.binding.swLanguage.changeEnable(false)
        if (isUseSystem) {
            viewHolder.binding.getRoot().setBackground(null)
        } else {
            if (position == 0) {
                viewHolder.binding.getRoot()
                    .setBackgroundResource(R.drawable.bg_ripple_select_item_top10dp)
            } else if (position == itemCount - 1) {
                viewHolder.binding.getRoot()
                    .setBackgroundResource(R.drawable.bg_ripple_select_item_bottom10dp)
            } else {
                viewHolder.binding.getRoot().setBackgroundResource(R.drawable.bg_ripple_select_item)
            }
        }

        val name: String = languageEntity.displayName.trim()
        viewHolder.binding.tvLanguageName.setText(name)
          if (isUseSystem) {
              viewHolder.binding.cslLanguage.isEnabled = false
              viewHolder.binding.swLanguage.setCheck(false)
          } else {
              viewHolder.binding.cslLanguage.isEnabled = true
              viewHolder.binding.swLanguage.setCheck(languageEntity.isEnabled)
          }
        if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            viewHolder.binding.tvLanguageName.setTextDirection(View.TEXT_DIRECTION_RTL)
        }

        viewHolder.binding.cslLanguage.setOnClickListener {
//            if (!isUseSystem) {
            languageEntity.isEnabled = !languageEntity.isEnabled
            languageEntities[position] = languageEntity
            viewHolder.binding.swLanguage.setCheck(languageEntity.isEnabled)
            if (onItemLanguageClickListener != null) {
                onItemLanguageClickListener!!.onItemLanguageClick(
                    position,
                    languageEntity.isEnabled, isUseSystem
                )
            }
//            }
        }

    }

//    fun setPos(pos: Int) {
//        notifyItemChanged(this.pos)
//        this.pos = pos
//        notifyItemChanged(pos)
//    }

    fun setOnItemLanguageClickListener(onItemLanguageClickListener: OnItemLanguageClickListener) {
        this.onItemLanguageClickListener = onItemLanguageClickListener
    }


    fun setUseSystem(useSystem: Boolean) {
        this.isUseSystem = useSystem
        notifyDataSetChanged()
    }


    interface OnItemLanguageClickListener {
        fun onItemLanguageClick(position: Int, enable: Boolean, isUseSystem: Boolean)
    }

    override fun getItemCount(): Int {
        return languageEntities.size
    }

    class LanguageViewHolder(binding: ItemRcvKeyboardLanguageBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        var binding: ItemRcvKeyboardLanguageBinding

        init {
            this.binding = binding
        }

    }


}