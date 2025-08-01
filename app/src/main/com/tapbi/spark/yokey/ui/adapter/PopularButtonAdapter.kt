package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.databinding.ItemRclPopularButtonBinding
import com.bumptech.glide.Glide
import com.tapbi.spark.yokey.data.model.PopularButton

class PopularButtonAdapter(var listPopular : ArrayList<PopularButton>, var context : Context, var popularCurrent : String, var listenerChangePopularButton: ListenerChangePopularButton) : RecyclerView.Adapter<PopularButtonAdapter.GetViewHolder>() {



    class GetViewHolder(var binding : ItemRclPopularButtonBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder {
        val binding = ItemRclPopularButtonBinding.inflate(LayoutInflater.from(context), parent, false)
        return GetViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changePopularButton(popular: String){
        popularCurrent = popular
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeList(listPopular: ArrayList<PopularButton>){
        this.listPopular = listPopular;
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: GetViewHolder, position: Int) {
        val popularButton = listPopular[position]
        if(popularButton.id == popularCurrent){
            holder.binding.imgUsingPopular.visibility = View.VISIBLE
            holder.binding.imgPopularButtonChoose.visibility = View.VISIBLE
        }else{
            holder.binding.imgUsingPopular.visibility = View.GONE
            holder.binding.imgPopularButtonChoose.visibility = View.GONE
        }
        Glide.with(context).load(popularButton.image).into(holder.binding.imgPopularButton)

        holder.binding.imgPopularButton.setOnClickListener {
            if(popularButton.id != popularCurrent){
                popularCurrent = popularButton.id
                listenerChangePopularButton.changePopular(popularCurrent)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        if(listPopular.size>0)return listPopular.size
        return 0
    }

    interface ListenerChangePopularButton{
        fun changePopular(popular : String)
    }
}