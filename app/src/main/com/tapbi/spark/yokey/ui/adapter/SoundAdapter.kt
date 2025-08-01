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
import com.tapbi.spark.yokey.data.model.Sound
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant

class SoundAdapter(var listSound: ArrayList<Sound>, var context: Context, var currentSound: String, var listenerChangeSound: ListenerChangeSound) : RecyclerView.Adapter<SoundAdapter.GetViewHolder>() {

    private val SIZE_10 = CommonUtil.dpToPx(context, 10)

    class GetViewHolder(var binding: ItemRclKeyEffectSoundBinding) : RecyclerView.ViewHolder(binding.root) {}

    @SuppressLint("NotifyDataSetChanged")
    fun changeListSound(listSound: ArrayList<Sound>) {
        if (listSound.size > 0) {
            this.listSound.clear()
            this.listSound.addAll(listSound)
            notifyDataSetChanged()
        }
    }

    fun changeFocusSound(currentSound: String) {
        this.currentSound = currentSound
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder {
        val binding = ItemRclKeyEffectSoundBinding.inflate(LayoutInflater.from(context), parent, false)
        return GetViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: GetViewHolder, position: Int) {
        val sound = listSound[position]
        holder.binding.imgEffectSound.setPadding(SIZE_10, SIZE_10, SIZE_10, SIZE_10)
        if (sound.id == Constant.AUDIO_DEFAULT) {
            holder.binding.imgEffectSound.setImageResource(R.drawable.ic_none_effect)
        } else {
            Glide.with(context).load(sound.image).into(holder.binding.imgEffectSound)
        }
        if (currentSound == sound.id) {
            holder.binding.imgUsingEffectSound.visibility = View.VISIBLE
        } else {
            holder.binding.imgUsingEffectSound.visibility = View.GONE
        }

        holder.binding.imgEffectSound.setOnClickListener {
            if (currentSound != sound.id) {
                currentSound = sound.id
                listenerChangeSound.changeSound(currentSound)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        if (listSound.size > 0) return listSound.size
        return 0
    }

    interface ListenerChangeSound {
        fun changeSound(sound: String)
    }
}