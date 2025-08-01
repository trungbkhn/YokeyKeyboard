package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tapbi.spark.yokey.databinding.ItemAppLanguageBinding
import com.tapbi.spark.yokey.common.Constant.PREF_LANGUAGE_CURRENT
import com.tapbi.spark.yokey.data.model.Language
import java.util.Locale

class LanguageAdapter constructor(
    private val iClickLanguage: IClickLanguage,
    private val context: Context
) :
    Adapter<LanguageAdapter.HolderLanguage>() {
    private var mutableListLanguage = mutableListOf<Language>()
    private var languageCodeCurrent: String = ""
    private var languageCountry: String = "Dafault"
    private var checkCurrentLanguage = false
    private var positionCurrent = -1;
    fun changeDataLanguage(mutableListLanguage: MutableList<Language>) {
        this.mutableListLanguage.clear()
        this.mutableListLanguage.addAll(mutableListLanguage)
        notifyDataSetChanged()
    }

    class HolderLanguage constructor(val binding: ItemAppLanguageBinding) :
        ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderLanguage =
        HolderLanguage(
            ItemAppLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: HolderLanguage, position: Int) {
        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        //val languageDevice =
          //  sharedPreferences.getString(Constant.PREF_LANGUAGE_DEFAULT_DEVICE, "")!!
        focusLanguage(holder, sharedPreferences, holder.adapterPosition)
        if (holder.adapterPosition == mutableListLanguage.size - 1) {
            holder.binding.viewBottom.visibility = View.VISIBLE
        } else {
            holder.binding.viewBottom.visibility = View.GONE
        }
        holder.binding.txtNameLanguage.text = mutableListLanguage[position].language
        holder.binding.btnViewItemLanguage.setOnClickListener {
            if (holder.adapterPosition < mutableListLanguage.size && holder.adapterPosition >= 0) {
                clickLanguage(holder, holder.adapterPosition, sharedPreferences)
            }
        }
    }

    private fun clickLanguage(
        holder: HolderLanguage,
        position: Int,
        sharedPreferences: SharedPreferences,
    ) {
        if (positionCurrent != position) {
            positionCurrent = position
            languageCodeCurrent = mutableListLanguage[position].languageCode
            this.languageCountry = mutableListLanguage[position].country
            holder.binding.radioButton.setChangeDrawCircle(true)
            iClickLanguage.clickLanguageCurrent(
                languageCodeCurrent,
                mutableListLanguage[position].language,
                position,
                mutableListLanguage.size,
                mutableListLanguage[position].country
            )
            checkCurrentLanguage = true
            notifyDataSetChanged()
        }
    }

    private fun focusLanguage(
        holder: HolderLanguage,
        sharedPreferences: SharedPreferences,
        position: Int
    ) {
        val language = sharedPreferences.getString(PREF_LANGUAGE_CURRENT, Locale.getDefault().language)
        Log.d("ducNQ", "focusLanguage: "+language)
        Log.d("ducNQ", "focusLanguage: ee "+Locale.getDefault().language)
        if ((languageCodeCurrent == mutableListLanguage[position].languageCode && checkCurrentLanguage) ||
            ((language.equals(mutableListLanguage[position].languageCode)) && !checkCurrentLanguage)) {
            holder.binding.radioButton.setChangeDrawCircle(true)
        } else {
            holder.binding.radioButton.setChangeDrawCircle(false)
        }
    }

    override fun getItemCount(): Int = mutableListLanguage.size

    interface IClickLanguage {
        fun clickLanguageCurrent(
            languageCode: String,
            language: String,
            position: Int,
            sizeList: Int,
            languageCountry: String
        )
    }
}