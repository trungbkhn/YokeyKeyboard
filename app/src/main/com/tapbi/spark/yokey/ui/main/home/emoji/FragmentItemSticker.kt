package com.tapbi.spark.yokey.ui.main.home.emoji

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.GridLayoutManager
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentItemStickerBinding
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.data.local.entity.Sticker
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.adapter.StickerAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.util.Constant
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FragmentItemSticker constructor() :
    BaseBindingFragment<FragmentItemStickerBinding, StickerViewModel>() {
    private var idCategory = 0
    private var sortKey = 0
    private val stickerAdapter: StickerAdapter by lazy {
        StickerAdapter(
            listSticker,
            requireContext()
        )
    }
    private val listSticker: ArrayList<Sticker> by lazy { ArrayList() }

    constructor(idCategory: Int, sortKey: Int) : this() {
        this.idCategory = idCategory
        this.sortKey = sortKey
    }

    companion object {
        @JvmStatic
        fun newInstance(idCategory: Int, sortKey: Int): FragmentItemSticker {
            return FragmentItemSticker().apply {
                try {
                    arguments = bundleOf(
                        "idCategory" to idCategory,
                        "sortKey" to sortKey
                    )
                }catch (e : IllegalArgumentException){}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        if (arguments != null) {
            idCategory = requireArguments().getInt("idCategory")
            sortKey = requireArguments().getInt("sortKey")
        }
        super.onCreate(savedInstanceState)
    }

    override fun getViewModel(): Class<StickerViewModel> = StickerViewModel::class.java

    override val layoutId: Int
        get() = R.layout.fragment_item_sticker

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        initAdapterSticker()
        listenerData()
        binding.spinKit.visibility = View.VISIBLE
        if (instance.connectivityStatus != -1) {
            reloadData(Constant.CONNECT_INTERNET)
        } else {
            reloadData(Constant.DISCONNECT_INTERNET)
        }
    }

    override fun onPermissionGranted() {}

    override fun onResume() {
        if (instance.connectivityStatus != -1) {
            reloadData(Constant.CONNECT_INTERNET)
        } else {
            reloadData(Constant.DISCONNECT_INTERNET)
        }
        super.onResume()
    }

    private fun initAdapterSticker() {
        binding.recyclerViewSticker.adapter = stickerAdapter
        binding.recyclerViewSticker.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewSticker.itemAnimator = null
    }

    private fun listenerData() {
        instance.stickerRepository!!.getLiveData(idCategory)
            .observe(viewLifecycleOwner) {
                if (it != null && !it.isEmpty()) {
                    listSticker.clear()
                    listSticker.addAll(it)
                    stickerAdapter.changeList(listSticker)
                    App.instance.stickerRepository?.addStickerTryKeyboardThread(listSticker)

                }else {
                    if (listSticker.size == 0 && App.instance.checkConnectivityStatus() == -1) {
                        binding.llCheckInternet.visibility = View.VISIBLE
                    }else {
                        binding.llCheckInternet.visibility = View.GONE
                    }
                }

                binding.spinKit.visibility = View.GONE
            }
        stickerAdapter.setListenerChangeItemFont(object : StickerAdapter.ListenerChangeItemSticker {
            override fun getItem(itemSticker: Sticker?, position: Int) {
                if (checkDoubleClick()) {
                    mainViewModel.mLiveDataDetailObject.value = itemSticker
                    showAdsFull(getString(R.string.tag_inter_item_sticker))
                }

            }
        })

//        instance.stickerRepository!!.getLiveStickerData(idCategory)
//            .observe(viewLifecycleOwner
//            ) { stickers ->
//                if (binding.spinKit.visibility == View.VISIBLE) binding.spinKit.visibility = View.GONE
//                if (stickers != null && stickers.isNotEmpty()) {
//                    val contextWrapper = ContextWrapper(instance)
//                    val destinationFile =
//                            contextWrapper.getDir(instance.filesDir.name, Context.MODE_PRIVATE)
//                    val list = ArrayList<Sticker>()
//                    for (sticker in stickers) {
//                        val fileThumb = File(
//                                destinationFile,
//                                Constant.FOLDER_STICKER + sticker?.id + "/" + sticker?.id + "/thumb.png"
//                        )
//                        list.add(
//                                Sticker(
//                                        sticker!!.id,
//                                        sticker.name,
//                                        fileThumb.absolutePath,
//                                        sticker.url,
//                                        sticker.idCategory,
//                                        sticker.nameCategory,
//                                        sticker.isDownload
//                                )
//                        )
//                    }
//                    if (list.size > 0) {
//                        binding.llCheckInternet.visibility = View.GONE
//                        stickerAdapter.changeList(list)
//                    } else binding.llCheckInternet.visibility = View.VISIBLE
//                } else {
//                    binding.llCheckInternet.visibility = View.VISIBLE
//                }
//            }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessage(messageEvent: MessageEvent) {
        val key = messageEvent.key
        if (key == Constant.KEY_CHANGE_STICKER || key == Constant.KEY_CHANGE_STICKER_NOT_SHOW_PREVIEW) {
            stickerAdapter.notifyDataSetChanged()
        }else if (key == Constant.CONNECT_INTERNET) {
            if (instance.connectivityStatus != -1 && (listSticker.size == 0 || binding.llCheckInternet.visibility == View.VISIBLE)) {
                binding.spinKit.visibility = View.VISIBLE
                reloadData(key)
            }
        }

    }

    private fun reloadData(key: Int) {
        if (!(App.instance.mPrefs?.getBoolean(com.tapbi.spark.yokey.common.Constant.SAVE_STICKER + idCategory.toString(), false) ?: false) ) {
            if (key == Constant.CONNECT_INTERNET) {
                binding.llCheckInternet.visibility = View.GONE
                if (listSticker.size == 0) {
                    binding.spinKit.visibility = View.VISIBLE
                    Log.d("duongcv", "reloadData: sticker server" + idCategory)
                    instance.stickerRepository!!.loadData(
                        sortKey,
                        idCategory,
                        ArrayList(listSticker),
                        requireContext()
                    )
                } else {
                    stickerAdapter.changeList(listSticker)
                }
            } else if (key == Constant.DISCONNECT_INTERNET) {
                if (listSticker.size == 0) instance.stickerRepository!!.loadStickerDB(idCategory) else {
                    binding.llCheckInternet.visibility = View.GONE
                    stickerAdapter.changeList(listSticker)
                }
            }
        }else {
            Log.d("duongcv", "reloadData: sticker local" + idCategory)
            binding.llCheckInternet.visibility = View.GONE
            App.instance.stickerRepository?.getAllStickerByCategory(idCategory)?.subscribe(object :SingleObserver<MutableList<Sticker>>{
                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {

                }

                override fun onSuccess(t: MutableList<Sticker>) {
                    instance.stickerRepository!!.getLiveData(idCategory).postValue(t)
                }

            })
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}