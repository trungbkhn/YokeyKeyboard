package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.databinding.ItemBackgroundControlAdapterBinding
import com.tapbi.spark.objects.BackgroundList

class BackgroundControlAdapter(
    var listBackground: ArrayList<BackgroundList>, var context: Context,
    var licyclerOwner: LifecycleOwner, var listenerBackground: BackgroundAdapter.ListenerBackground
) : RecyclerView.Adapter<BackgroundControlAdapter.GetViewHolder>() {

    var pathCurrent: String = ""
    var backgroundAdapter0 : BackgroundAdapter? = null
    var backgroundAdapter1 : BackgroundAdapter? = null
    class GetViewHolder(var binding: ItemBackgroundControlAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun getItemCount(): Int {
        if (listBackground.size > 0) return listBackground.size
        return 0
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeList(listBackground: ArrayList<BackgroundList>) {
        this.listBackground.clear()
        this.listBackground.addAll(listBackground)
        notifyDataSetChanged()
    }

    fun changePath(pathCurrent: String) {
        this.pathCurrent = pathCurrent
//        notifyDataSetChanged()
//        backgroundAdapter.changePath(pathCurrent)
        if(backgroundAdapter0!=null)backgroundAdapter0!!.changePath(pathCurrent)
        if(backgroundAdapter1!=null)backgroundAdapter1!!.changePath(pathCurrent)
    }

    override fun onBindViewHolder(
        holder: GetViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

        val backgroundList = listBackground[position]
        holder.binding.txtTitleItemBackgroundControl.text = backgroundList.title
        if (position == listBackground.size - 1) {
            holder.binding.vBottomCustomize.visibility = View.VISIBLE
        } else {
            holder.binding.vBottomCustomize.visibility = View.GONE
        }
        initBackgroundAdapter(backgroundList)
        val gridManager = GridLayoutManager(context, 4)
        holder.binding.rclBackgroundControl.itemAnimator = null
        holder.binding.rclBackgroundControl.layoutManager = gridManager
        if(position==0){
            backgroundAdapter0 = initBackgroundAdapter(backgroundList)
            holder.binding.rclBackgroundControl.adapter = backgroundAdapter0
        }else{
            backgroundAdapter1 = initBackgroundAdapter(backgroundList)
            holder.binding.rclBackgroundControl.adapter = backgroundAdapter1

        }

//        backgroundAdapter = initBackgroundAdapter(backgroundList)
//        holder.binding.rclBackgroundControl.adapter = backgroundAdapter
    }

    private fun initBackgroundAdapter(backgroundList: BackgroundList) : BackgroundAdapter {
        for(i in 0 until backgroundList.backgroundJson.size){
            if(backgroundList.backgroundJson[i].idBg == 1020){
                backgroundList.backgroundJson.removeAt(i)
                break
            }
        }
        val backgroundAdapter = BackgroundAdapter(
            backgroundList.backgroundJson,
            context,
            licyclerOwner,
            object : BackgroundAdapter.ListenerBackground {
                override fun getBackground(path: String, countDownload : Int) {
                    if (pathCurrent != path) {
                        pathCurrent = path
                        listenerBackground.getBackground(path, countDownload)
                    }
                }

                override fun checkPermission() {}

            },
            pathCurrent
        )
        backgroundAdapter.changePath(pathCurrent)
        return backgroundAdapter;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder {
        val binding =
            ItemBackgroundControlAdapterBinding.inflate(LayoutInflater.from(context), parent, false)
        return GetViewHolder(binding)
    }
}