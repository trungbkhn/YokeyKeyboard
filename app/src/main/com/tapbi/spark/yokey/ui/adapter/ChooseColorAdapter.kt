package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.databinding.ItemChooseColorBinding
import timber.log.Timber

class ChooseColorAdapter(var listColor : ArrayList<String>, var context : Context, var listenerChooseColor: ListenerChooseColor) : RecyclerView.Adapter<ChooseColorAdapter.GetViewHolder>() {

    var currentColor : String = "#ffffff"

    class GetViewHolder(var binding : ItemChooseColorBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeColorCurrent(color : String){
        currentColor = color
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeListColor(listColor : ArrayList<String>){
        this.listColor = listColor
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder {
       val binding = ItemChooseColorBinding.inflate(LayoutInflater.from(context), parent, false)
        return GetViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: GetViewHolder, position: Int) {
        val color = listColor[position]

        if(color.contains("#ffffff",true)){
            holder.binding.imgColor.setColorFilter(Color.TRANSPARENT)
        }else{
            holder.binding.imgColor.setColorFilter(Color.parseColor(color))
        }

         Timber.d("ducNQonBindViewHolder "+currentColor);
        if(currentColor.equals(color,true)){
            holder.binding.imgChooseColor.visibility = View.VISIBLE
        }else{
            holder.binding.imgChooseColor.visibility = View.INVISIBLE
        }


        if(position == listColor.size-1){
            holder.binding.viewSpaceEnd.visibility = View.VISIBLE
        }else{
            holder.binding.viewSpaceEnd.visibility = View.GONE
        }

        holder.binding.imgColor.setOnClickListener {
            if(!currentColor.equals(color,true)){
                currentColor =color
                 Timber.d("ducNQbinding "+color);
                listenerChooseColor.chooseColor(color)
                notifyDataSetChanged()

            }
        }

    }

    override fun getItemCount(): Int {
        if(listColor.size>0) return  listColor.size
        return 0
    }

    interface ListenerChooseColor{
        fun chooseColor(color : String)
    }
}