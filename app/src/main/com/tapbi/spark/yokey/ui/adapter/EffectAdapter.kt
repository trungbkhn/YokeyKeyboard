package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.ItemRclKeyEffectSoundBinding
import com.bumptech.glide.Glide
import com.tapbi.spark.yokey.data.model.Effect
import com.tapbi.spark.yokey.util.Constant

class EffectAdapter(
    var listEffect: ArrayList<Effect>,
    var context: Context,
    var currentEffect: String,
    var listenerChangeEffect: ListenerChangeEffect
) : RecyclerView.Adapter<EffectAdapter.GetViewHolder>() {
    class GetViewHolder(var binding: ItemRclKeyEffectSoundBinding) :
        RecyclerView.ViewHolder(binding.root) {}


    @SuppressLint("NotifyDataSetChanged")
    fun changeListEffect(listEffect: ArrayList<Effect>) {
        if (listEffect.size > 0) {
            this.listEffect.clear()
            this.listEffect.addAll(listEffect)
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeFocusEffect(currentEffect: String) {
        this.currentEffect = currentEffect
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder {
        val binding =
            ItemRclKeyEffectSoundBinding.inflate(LayoutInflater.from(context), parent, false)
        return GetViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: GetViewHolder, position: Int) {
        val effect = listEffect[position]
        if (effect.id == Constant.ID_NONE) {
            holder.binding.imgEffectSound.setImageResource(R.drawable.ic_none_effect)
        } else {
            Glide.with(context).load(effect.preview).into(holder.binding.imgEffectSound)
        }

        if (currentEffect == effect.id) {
            holder.binding.imgUsingEffectSound.visibility = View.VISIBLE
        } else {
            holder.binding.imgUsingEffectSound.visibility = View.GONE
        }

        holder.binding.imgEffectSound.setOnClickListener {
            if (currentEffect != effect.id) {
                currentEffect = effect.id
                listenerChangeEffect.changeEffect(currentEffect)
                notifyDataSetChanged()
            }
        }

    }

    override fun getItemCount(): Int {
        if (listEffect.size > 0) return listEffect.size
        return 0
    }

    interface ListenerChangeEffect {
        fun changeEffect(effect: String)
    }
}