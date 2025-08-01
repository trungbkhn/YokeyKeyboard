package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.data.repository.ClipboardRepository
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.tapbi.spark.yokey.databinding.ItemRclClipboardBinding
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber
import java.util.ArrayList

class ClipBoardAdapter(
    private val clipboardRepository: ClipboardRepository?,
    private val onClickClipBoardListener: OnClickClipBoardListener
) : ListAdapter<String?, ClipBoardAdapter.GetViewHolder>(
    diffCallback
) {
    private var color = instance.colorIconDefault
    fun changeList(listClipboard: ArrayList<String?>?) {
        submitList(listClipboard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder =
        GetViewHolder(
            ItemRclClipboardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    @SuppressLint("NotifyDataSetChanged")
    fun changeColor(color: Int) {
        this.color = color
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: GetViewHolder, position: Int) {
        val clipString = currentList[position]!!
        holder.binding.txtItemClipboard.setTextColor(instance.colorIconDefault)
        holder.binding.imgRemoveClipboard.setColorFilter(instance.colorIconDefault)
        holder.binding.txtItemClipboard.text = clipString
        holder.binding.txtItemClipboard.setOnClickListener {
            onClickClipBoardListener.onClickClipContent(
                clipString
            )
        }
        holder.binding.imgRemoveClipboard.setOnClickListener {
            removeClipString(
                clipString
            )
        }
    }

    inner class GetViewHolder(val binding: ItemRclClipboardBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    private fun removeClipString(content: String) {
        val list = ArrayList(currentList)
        for (s in list) {
            if (s == content) {
                list.remove(s)
                changeList(list)
                clipboardRepository?.refeshClipboardThread(list)?.subscribe(object : SingleObserver<Boolean> {
                    override fun onSubscribe(d: Disposable) {
                        
                    }

                    override fun onError(e: Throwable) {
                        Timber.e("Duongcv " + e.message);
                    }

                    override fun onSuccess(t: Boolean) {
                       
                    }
                })
                break
            }
        }
        if (list.size == 0) {
            onClickClipBoardListener.showText()
        }
        Timber.d("removeClipString " + list.size)
    }

    interface OnClickClipBoardListener {
        fun onClickClipContent(content: String?)
        fun showText()
    }

    companion object {
        val diffCallback: DiffUtil.ItemCallback<String?> =
            object : DiffUtil.ItemCallback<String?>() {
                override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                    return TextUtils.equals(oldItem, newItem)
                }

                override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                    return oldItem == newItem
                }

            }
    }
}