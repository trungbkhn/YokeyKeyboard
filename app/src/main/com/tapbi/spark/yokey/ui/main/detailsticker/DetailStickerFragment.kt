package com.tapbi.spark.yokey.ui.main.detailsticker

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentDetailStickerBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.ads.nativetemplates.OnDecorationAds
import com.ironman.trueads.common.Common
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.data.local.entity.Sticker
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.base.BaseBindingDialogFragment
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.DisplayUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.File
import java.util.Objects

class DetailStickerFragment : BaseBindingDialogFragment<FragmentDetailStickerBinding, DetailStickerViewModel>() , View.OnClickListener {

    private var sticker: Sticker? = null
    private var destinationFile: File? = null
    private var fileSticker: File? = null
    private var isDownload = false
    private var isShow = false
    private var checkDownloadFinish = false
    override val layoutId: Int
        get() = R.layout.fragment_detail_sticker

    override fun getViewModel(): Class<DetailStickerViewModel> {
        return DetailStickerViewModel::class.java
    }

    override fun onCreatedView(view: View, savedInstanceState: Bundle?) {
        val contextWrapper = ContextWrapper(context)
        destinationFile = contextWrapper.getDir(requireContext().filesDir.name, Context.MODE_PRIVATE)
        initView()
        processRemoveAds(isRemoveAds)
        eventClick()
        listener()

    }

    private fun listener() {
        App.instance.stickerRepository?.resultDownload?.observe(viewLifecycleOwner){
            isDownload = false
            showHideLoading(false)
            if (it) {
                viewModel!!.insertStickerToDB(context, sticker)
            } else {
                if (isVisible) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.apply_sticker_fail),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        viewModel?._resultSticker?.observe(viewLifecycleOwner
        ) { aBoolean ->
            if (aBoolean) {
                showView()
            }
        }
        viewModel!!.resultInsert.observe(viewLifecycleOwner
        ) { aBoolean ->
            Timber.d("ducNQ : onChangeded: 3")
            if (aBoolean) {
                Toast.makeText(
                    context,
                    resources.getString(R.string.apply_sticker_done),
                    Toast.LENGTH_SHORT
                ).show()
                changeScreen(R.id.tryKeyboardFragment, com.tapbi.spark.yokey.common.Constant.TYPE_TRY_STICKER_KEYBOARD)
            } else {
                if (isVisible) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.apply_sticker_fail),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showView() {
        isDownload = false
        showHideLoading(false)
        if (isVisible) {
            checkDownloadFinish = true
            binding.btnApplySticker.visibility = View.VISIBLE
            binding.txtDescriptionDowload.visibility = View.INVISIBLE
            binding.ctlDownload.visibility = View.INVISIBLE
            if (binding!!.spinKitDetailActivity.visibility == View.VISIBLE) {
                showHideLoading(false)
            }
            Toast.makeText(
                context,
                resources.getString(R.string.apply_sticker_done),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun eventClick() {
        binding.btnApplySticker.setOnClickListener(this)
        binding.ctlDownload.setOnClickListener(this)
        binding.btnClose.setOnClickListener(this)
        binding.ctlPremium.setOnClickListener {
            if (checkDoubleClick()) {
                changeScreen(R.id.premiumFragment, com.tapbi.spark.yokey.common.Constant.TYPE_TRY_STICKER_KEYBOARD)
            }
        }
        binding.ctlPremium2.setOnClickListener {
            if (checkDoubleClick()) {
                changeScreen(R.id.premiumFragment, com.tapbi.spark.yokey.common.Constant.TYPE_TRY_STICKER_KEYBOARD)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (sticker != null) {
            instance.stickerRepository?.idShowResultDownload = sticker?.id
        }
    }

    private fun initView() {
        if (mainViewModel.mLiveDataDetailObject.value != null && mainViewModel.mLiveDataDetailObject.value is Sticker) {
            sticker = mainViewModel.mLiveDataDetailObject.value as Sticker
            mainViewModel.mLiveDataDetailObject.postValue(null)
        }
        if (sticker != null) {
            val width : Int = DisplayUtils.getScreenWidth() * 20 /37
            val params = binding.cvSticker.layoutParams as ConstraintLayout.LayoutParams
            params.height = width
            params.width = width
            binding.cvSticker.layoutParams = params

            instance.stickerRepository?.idShowResultDownload = sticker?.id
            Glide.with(requireContext()).asBitmap().load(sticker!!.thumb)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        binding.imgSticker.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
            binding!!.txtNameKeyboard.text = sticker!!.name
            fileSticker = File(destinationFile, Constant.FOLDER_STICKER + sticker!!.id)
            if (CommonUtil.checkStickerExist(sticker) && isExistFileSticker){
                binding.btnApplySticker.text = requireContext().getString(R.string.apply_sticker_done)
                binding.btnApplySticker.visibility = View.VISIBLE
                binding.txtDescriptionDowload.visibility = View.INVISIBLE
                binding.ctlDownload.visibility = View.INVISIBLE
            } else {
                binding.btnApplySticker.text = requireContext().getString(R.string.get_sticker)
                binding.btnApplySticker.visibility = View.INVISIBLE
                binding.txtDescriptionDowload.visibility = View.VISIBLE
                binding.ctlDownload.visibility = View.VISIBLE
            }
        } else {
            dismiss()
        }
//        binding.btnApplySticker.visibility = View.INVISIBLE
    }

    @SuppressLint("NonConstantResourceId")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnApplySticker -> {
                if (binding.btnApplySticker.text.toString().equals(requireContext().getString(R.string.apply_sticker_done), true)) {
                    changeScreen(R.id.tryKeyboardFragment, com.tapbi.spark.yokey.common.Constant.TYPE_TRY_STICKER_KEYBOARD)
                }else {
                    if (App.instance.checkConnectivityStatus() == -1) {
                        if (!isShow) {
                            isShow = true
                            delayShow()
                            Toast.makeText(
                                App.instance!!,
                                App.instance!!.getString(R.string.text_check_internet),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        if (sticker != null && !isDownload) {
                            showHideLoading(true)
                            isDownload = true
                            instance.stickerRepository?.downloadZipFileTheme(sticker)
                        }
                    }
                }
            }

            R.id.ctlDownload -> {
                if (checkDoubleClick()) {
                    if (App.instance.checkConnectivityStatus() == -1) {
                        if (!isShow) {
                            isShow = true
                            delayShow()
                            Toast.makeText(
                                App.instance!!,
                                App.instance!!.getString(R.string.text_check_internet),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        if (sticker != null && !isDownload) {
                            showHideLoading(true)
                            isDownload = true
                            instance.stickerRepository?.downloadZipFileTheme(sticker)
                        }
                    }
                }
            }

            R.id.btnClose -> dismiss()
        }
    }

    override fun onPause() {
        super.onPause()
        instance.stickerRepository?.idShowResultDownload = 0
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding!!.root.clearFocus()

    }

    private fun delayShow() {
        Handler().postDelayed({ isShow = false }, 1500)
    }

    private val isExistFileSticker: Boolean
        private get() {
            if (fileSticker != null && fileSticker!!.exists()) {
                val thumb = File(fileSticker!!.absoluteFile, sticker!!.id.toString() + "/thumb.png")
                val folder =
                    File(fileSticker!!.absoluteFile, sticker!!.id.toString() + "/" + sticker!!.id)
                if (thumb.exists() && folder.exists() && folder.list() != null) {
                    if (Objects.requireNonNull(folder.list()).isNotEmpty()) {
                        return true
                    }
                }
            }
            return false
        }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageReceived(event: MessageEvent) {

    }

    private fun showHideLoading(isShow: Boolean) {
        if (isShow) {
            binding.spinKitDetailActivity.visibility = View.VISIBLE
            binding.btnApplySticker.visibility = View.INVISIBLE
            binding.txtDescriptionDowload.visibility = View.INVISIBLE
            binding.ctlDownload.visibility = View.INVISIBLE
        } else {
            binding.spinKitDetailActivity.visibility = View.INVISIBLE
            if (isExistFileSticker) {
                binding.btnApplySticker.text = requireContext().getString(R.string.apply_sticker_done)
                binding.btnApplySticker.visibility = View.VISIBLE
                binding.txtDescriptionDowload.visibility = View.INVISIBLE
                binding.ctlDownload.visibility = View.INVISIBLE
            } else {
                binding.btnApplySticker.text = requireContext().getString(R.string.get_sticker)
                binding.btnApplySticker.visibility = View.INVISIBLE
                binding.txtDescriptionDowload.visibility = View.VISIBLE
                binding.ctlDownload.visibility = View.VISIBLE
            }
        }
    }

    override fun processRemoveAds(isRemoveAds: Boolean) {
        super.processRemoveAds(isRemoveAds)
        val mapId = Common.getMapIdAdmobApplovin(requireActivity(),R.array.admob_native_id_detail_sticker,R.array.applovin_native_id_detail_sticker)
        showAdsNative(binding.frAdsNative,mapId,object : OnDecorationAds {
            override fun onDecoration(network: String?) {
                binding.frAdsNative.getNativeAdView(network)?.setBackgroundResource(R.drawable.bg_ads_gray_detail)
            }

        })
        showViewAds(binding.ctlPremium, binding.ctlPremium2, binding.ctlContent, binding.frAdsNative)
    }

}