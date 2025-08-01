package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.ItemRclSimpleButtonBinding
import com.tapbi.spark.yokey.data.model.SimpleButton

class SimpleButtonAdapter  (var listSimpleButton : ArrayList<SimpleButton>, var context: Context, var idCurrent : Int, var changeSimpleButton: ChangeSimpleButton) : RecyclerView.Adapter<SimpleButtonAdapter.GetViewHolder>() {


    class GetViewHolder(var binding : ItemRclSimpleButtonBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeList(listSimpleButton: ArrayList<SimpleButton>){
        this.listSimpleButton = listSimpleButton
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeId(id: Int){
        this.idCurrent = id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder {
        return GetViewHolder(ItemRclSimpleButtonBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: GetViewHolder, position: Int) {
        val simpleButton = listSimpleButton.get(position)
        holder.binding.imgSimpleButton.setImageResource(simpleButton.idResource)

        if(simpleButton.idType==idCurrent){
            holder.binding.imgUsingSimple.visibility = View.VISIBLE
            holder.binding.imgSimpleButton.setBackgroundResource(R.drawable.bg_select_simplebutton)
        }else{
            holder.binding.imgUsingSimple.visibility = View.GONE
            holder.binding.imgSimpleButton.setBackgroundResource(R.drawable.bg_unselect_simplebutton)
        }

        if(position == listSimpleButton.size-1){
            holder.binding.viewSpaceEnd.visibility = View.VISIBLE
        }else{
            holder.binding.viewSpaceEnd.visibility = View.GONE
        }

        holder.binding.imgSimpleButton.setOnClickListener {
            if(idCurrent!= simpleButton.idType){
                idCurrent = simpleButton.idType
                changeSimpleButton.changeSimpleButton(simpleButton.idType)
                notifyDataSetChanged()
            }
        }

    }

    override fun getItemCount(): Int {
        if(listSimpleButton.size>0) return listSimpleButton.size
        return 0
    }

    interface ChangeSimpleButton{
        fun changeSimpleButton (type : Int)
    }
}