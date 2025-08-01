package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.databinding.ItemContentSymbolsBinding
import com.tapbi.spark.yokey.data.local.entity.Symbols

class ContentSymbolsAdapter(var listSymbols : ArrayList<Symbols>, var context : Context, var setSymbols : SetSymbols) : RecyclerView.Adapter<ContentSymbolsAdapter.GetViewHolder>() {
    class GetViewHolder(var binding : ItemContentSymbolsBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder {
        return GetViewHolder(ItemContentSymbolsBinding.inflate(LayoutInflater.from(context),parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeList(listSymbols: ArrayList<Symbols>){
        this.listSymbols = listSymbols
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: GetViewHolder, position: Int) {
        val symbols = listSymbols[position]
        holder.binding.tvContentSymbols.text = symbols.contentSymbols
        holder.binding.tvContentSymbols.setOnClickListener {
            setSymbols.setSymbols(symbols)
        }
    }

    override fun getItemCount(): Int {
        if(listSymbols.size>0) return listSymbols.size
        return 0
    }

    interface SetSymbols{
        fun setSymbols(symbols: Symbols)
    }
}