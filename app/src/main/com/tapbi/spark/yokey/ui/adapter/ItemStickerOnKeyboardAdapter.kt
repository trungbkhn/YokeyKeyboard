package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.tapbi.spark.yokey.databinding.ItemStickerOnkeyboardBinding
import com.tapbi.spark.yokey.ui.custom.view.blurBg.RealtimeBlurViewKB
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import timber.log.Timber
import java.util.ArrayList
import java.util.HashMap

class ItemStickerOnKeyboardAdapter(
    private val context: Context,
    private var listSticker: ArrayList<String>?,
    private val imgBackground: ImageView?
) : RecyclerView.Adapter<ItemStickerOnKeyboardAdapter.GetViewHolder>() {
    private var listenerPathSticker: ListenerPathSticker? = null
    private val listRealTimeBlur: MutableMap<Int, RealtimeBlurViewKB?>? = HashMap()
    private var id = -1
    private val mPrefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            context
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeList(listSticker: ArrayList<String>) {
        if (listRealTimeBlur != null && listRealTimeBlur.isNotEmpty() && this.listSticker != null && this.listSticker!!.size > 0) {
            for (i in listSticker.indices) {
                if (listRealTimeBlur.containsKey(i) && listRealTimeBlur[i] != null) {
                    listRealTimeBlur[i]!!.changeIsRealTime(false)
                }
            }
            listRealTimeBlur.clear()
        }
        this.listSticker = listSticker
        notifyDataSetChanged()
    }

    fun setListenerPathSticker(listenerPathSticker: ListenerPathSticker?) {
        this.listenerPathSticker = listenerPathSticker
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder =
        GetViewHolder(
            ItemStickerOnkeyboardBinding.inflate(
                LayoutInflater.from(
                    context
                )
            )
        )

    @SuppressLint("NotifyDataSetChanged", "CommitPrefEdits")
    override fun onBindViewHolder(
        holder: GetViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        if (listSticker != null && listSticker!!.size > position && listSticker!![position] != null) {
            val path: String = listSticker!![position]
            Glide.with(context).asBitmap().load(path).override(200)
                .into(BitmapImageViewTarget(holder.binding.imgItemSticker))

            holder.binding.cvSettingSticker.setOnClickListener {
                Timber.e("ducNQ onBindViewHolder: " + path);
                notifyDataSetChanged()
                if (listenerPathSticker != null) listenerPathSticker!!.getPath(path)
            }
        }
    }

    fun changeStatusAnimByPosition(start: Int, end: Int) {
        if (listRealTimeBlur != null && listRealTimeBlur.isNotEmpty()) {
            for (i in listSticker!!.indices) {
                if (i in start..end) {
                    if (listRealTimeBlur.containsKey(i) && listRealTimeBlur[i] != null) {
                        listRealTimeBlur[i]!!.changeIsRealTime(true)
                    }
                } else {
                    if (listRealTimeBlur.containsKey(i) && listRealTimeBlur[i] != null) {
                        listRealTimeBlur[i]!!.changeIsRealTime(false)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = listSticker!!.size

    inner class GetViewHolder(val binding: ItemStickerOnkeyboardBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    interface ListenerPathSticker {
        fun getPath(path: String?)
    }
}