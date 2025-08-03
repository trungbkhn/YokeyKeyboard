package com.tapbi.spark.yokey.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.common.LiveEvent
import com.tapbi.spark.yokey.data.model.ItemBackground
import com.tapbi.spark.yokey.databinding.ItemBackgroundBinding
import com.tapbi.spark.yokey.interfaces.IResultDownBackground
import com.tapbi.spark.objects.Background
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.Constant.PATH_FILE_DOWNLOADED_BACKGROUND
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class BackgroundAdapter(
    var listBackground: ArrayList<Background>,
    var context: Context,
    var licyclerOwner: LifecycleOwner,
    var listenerBackground: ListenerBackground,
    var pathCurrent: String?

) : RecyclerView.Adapter<BackgroundAdapter.GetViewHolder>() {

    var linkBgCurrent = ""
    val getFilesDir: File = instance.filesDir
    val storageDir = File(getFilesDir, Constant.PATH_FILE_UN_ZIP_THEME_CUSTOMIZE_BACKGROUND)
    private lateinit var liveDownloadBackground: MutableLiveData<Int>
    private lateinit var liveSetBackground: MutableLiveData<String>
    private lateinit var liveDownloadError: LiveEvent<Int>
    var isDownload: Boolean = false
    var listItemDownload: ArrayList<ItemBackground> = ArrayList()

    class GetViewHolder(var binding: ItemBackgroundBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    private var checkDownBackground = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GetViewHolder {
        val binding = ItemBackgroundBinding.inflate(LayoutInflater.from(context), parent, false)
        liveDownloadBackground = MutableLiveData()
        liveSetBackground = MutableLiveData()
        liveDownloadError = LiveEvent()
        liveDownloadError.observe(licyclerOwner) {
            if (it != null) {
                if (!isDownload && listItemDownload.size > 0) {
                    downloadBackground(listItemDownload.get(0))
                }
                checkDownBackground = true
                CommonUtil.customToast(
                    context,
                    context.resources.getString(R.string.error_down_background)
                )
                notifyItemChanged(it, true)
            }
        }
        liveSetBackground.observe(licyclerOwner) { path ->
            if (path != null) {
                listenerBackground.getBackground(path, listItemDownload.size)
            }
        }
        liveDownloadBackground.observe(
            licyclerOwner
        ) { position ->
            if (position != null) {
                if (!isDownload && listItemDownload.size > 0) {
                    downloadBackground(listItemDownload.get(0))
                }
                if (listBackground.size > position && CommonUtil.checkBackgroundThemeDownloaded(
                        listBackground[position].idBg.toString()
                    ) && instance.connectivityStatus != -1
                ) {
                    App.instance.idBgCurrent = position + 1
                    val imageFile = File(storageDir, listBackground[position].idBg.toString())
                    listenerBackground.getBackground(imageFile.absolutePath, listItemDownload.size)
                } else {
                    if (!checkDownBackground) {
                        CommonUtil.customToast(
                            context,
                            context.resources.getString(R.string.error_down_background)
                        )
                    }
                    notifyItemChanged(position)
                }
                checkDownBackground = false
            }
        }
        return GetViewHolder(binding)
    }

    fun changePath(path: String) {
        var path1: String = ""
        if (pathCurrent != null) path1 = pathCurrent as String;
        this.pathCurrent = path
        checkNotiItem(path1, pathCurrent)
//        notifyDataSetChanged()
    }

    private fun checkNotiItem(path1: String, path2: String?) {
        if (listBackground.size > 0) {
            for (i in 0 until listBackground.size) {
                if (listBackground[i].linkBg == path1 || listBackground[i].linkBg == path2 || path2!!.contains(
                        listBackground[i].idBg.toString()
                    ) || path1.contains(listBackground[i].idBg.toString())
                ) {
                    notifyItemChanged(i)
                }
            }
        }
    }

    override fun onBindViewHolder(
        holder: GetViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val ps = position
        val background: Background = listBackground[position]

        Glide.with(context).load(background.linkBgThumb).override(200).diskCacheStrategy(
            DiskCacheStrategy.ALL
        ).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                holder.binding.shimmerViewLoading.visibility = View.VISIBLE
                holder.binding.imgItemBackground.isEnabled = false
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                holder.binding.shimmerViewLoading.visibility = View.GONE
                holder.binding.imgItemBackground.isEnabled = true
                return false
            }

        })
            .into(holder.binding.imgItemBackground)
        if (CommonUtil.checkBackgroundThemeDownloaded(background.idBg.toString())) {
            holder.binding.statusDownload.visibility = View.GONE
            holder.binding.spinKitDownloadBackround.visibility = View.GONE
        } else {
            holder.binding.statusDownload.visibility = View.VISIBLE
            holder.binding.spinKitDownloadBackround.visibility = View.GONE
        }

        if (background.linkBg.contains(Constant.FOLDER_ASSET, true) || background.linkBg.contains(
                PATH_FILE_DOWNLOADED_BACKGROUND
            )
        ) {
            holder.binding.statusDownload.visibility = View.GONE
            holder.binding.spinKitDownloadBackround.visibility = View.GONE
        }

        if ((background.linkBg.contains(
                Constant.FOLDER_ASSET,
                true
            ) || background.linkBg.contains(
                PATH_FILE_DOWNLOADED_BACKGROUND
            )) && pathCurrent == background.linkBg
        ) {
            holder.binding.statusBackground.visibility = View.VISIBLE
        } else {
            val imageFile = File(storageDir, background.idBg.toString())
            if (imageFile.absoluteFile.toString() == pathCurrent) {
                holder.binding.statusBackground.visibility = View.VISIBLE
            } else {
                holder.binding.statusBackground.visibility = View.GONE
            }
        }

        holder.binding.imgItemBackground.setOnClickListener {
            Timber.e("ducNQqq onBindViewHolder: " + background.idBg);
            if (background.linkBg.contains(
                    Constant.FOLDER_ASSET,
                    true
                ) || background.linkBg.contains(PATH_FILE_DOWNLOADED_BACKGROUND)
            ) {
                listenerBackground.getBackground(background.linkBg, listItemDownload.size)
            } else {
                if (holder.binding.statusDownload.visibility == View.VISIBLE && holder.binding.spinKitDownloadBackround.visibility == View.GONE) {

                    if (App.instance.connectivityStatus == -1) {
                        CommonUtil.customToast(
                            context,
                            context.resources.getString(R.string.text_check_internet)
                        )
                    } else {
                        checkDownBackground = false
                        holder.binding.statusDownload.visibility = View.GONE
                        holder.binding.spinKitDownloadBackround.visibility = View.VISIBLE

                    }
                } else if (holder.binding.statusDownload.visibility == View.GONE && holder.binding.spinKitDownloadBackround.visibility == View.GONE) {
                    App.instance.idBgCurrent = position + 1
                    val imageFile = File(storageDir, background.idBg.toString())
                    listenerBackground.getBackground(imageFile.absolutePath, listItemDownload.size)
                }
            }
        }

    }

    private fun downloadBackground(itemBackground: ItemBackground) {
        isDownload = true
        if (listItemDownload.size > 0) {
            listItemDownload.removeAt(0)
        }
        CoroutineScope(Dispatchers.IO).launch {
            CommonUtil.downloadBackgroundToStorage(
                context,
                itemBackground.linkBg,
                itemBackground.idBg,
                object : IResultDownBackground {
                    override fun onDownBackgroundError() {
                        isDownload = false
                        liveDownloadError.postValue(itemBackground.position)
                    }

                    override fun onDownBackgroundSuccess() {
                        isDownload = false
                        liveDownloadBackground.postValue(itemBackground.position)
                    }
                })
            withContext(Dispatchers.Main) {

            }
        }
    }

    override fun getItemCount(): Int {
        if (listBackground.size > 0) return listBackground.size;
        return 0
    }

    interface ListenerBackground {
        fun getBackground(path: String, countDownload: Int)

        fun checkPermission()
    }
}