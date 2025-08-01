package com.tapbi.spark.yokey.ui.main.crop

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentCropImageBinding
import com.android.inputmethod.keyboard.KeyboardSwitcher
import com.android.inputmethod.latin.settings.Settings
import com.android.inputmethod.latin.utils.ResourceUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant.CHECK_CROP_DONE
import com.tapbi.spark.yokey.common.Constant.URI_IMAGE
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.base.BaseDialogFragment
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import org.greenrobot.eventbus.EventBus
import java.util.*

class CropFragment constructor() : BaseDialogFragment() {

    private lateinit var binding: FragmentCropImageBinding
    private var imgPath = ""
    private var cropImageViewModel = CropImageViewModel()
    private var keyboardSwitcher: KeyboardSwitcher? = null
    private lateinit var IListenCrops: IListenCrop
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCropImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        keyboardSwitcher = KeyboardSwitcher.getInstance()
        setUpView()
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.action != KeyEvent.ACTION_DOWN) {
                    dismiss()
                }
                return@setOnKeyListener true
            } else return@setOnKeyListener false
        }
        super.onViewCreated(view, savedInstanceState)
    }

    //    constructor(IListenCrops: IListenCrop) : this() {
//        this.IListenCrops = IListenCrops
//    }
    fun setListenCrop(IListenCrops: IListenCrop) {
        this.IListenCrops = IListenCrops
    }

    override fun onStart() {
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
        super.onStart()
    }

    private fun setUpView() {
        binding.txtCrop.isEnabled = false

        var keyboardHeight = ResourceUtils.getKeyboardHeight(
            App.instance.resources, Settings.getInstance().current
        )
        if (keyboardSwitcher != null && keyboardSwitcher!!.visibleKeyboardView != null) {
            keyboardHeight = keyboardSwitcher!!.visibleKeyboardView.height
        }
        if (keyboardHeight == 0) {
            keyboardHeight = ResourceUtils.getDefaultKeyboardHeight(
                resources
            )
        }
        keyboardHeight += resources.getDimension(R.dimen.config_suggestions_strip_height)
            .toInt()
        var widthScreen = CommonUtil.getScreenWidth()
        var heightScreen = CommonUtil.getScreenHeight()
        if (widthScreen > heightScreen) {
            widthScreen = CommonUtil.getScreenHeight()
            heightScreen = keyboardHeight
        } else {
            heightScreen = keyboardHeight
        }
        // binding.imgCrop.setAspectRatio(CommonUtil.getScreenWidth(), keyboardHeight)
        binding.imgCrop.setAspectRatio(widthScreen, heightScreen)
        binding.progressCrop.visibility = View.VISIBLE
        arguments?.apply {
            imgPath = getString(URI_IMAGE, "")
            Glide.with(this@CropFragment).asBitmap().override(CommonUtil.getScreenWidth())
                .load(imgPath).into(object :
                    CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        if (isAdded) {
                            binding.txtCrop.isEnabled = true
                            binding.imgCrop.setImageBitmap(resource)
                            binding.progressCrop.visibility = View.INVISIBLE
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        if (isAdded) {
                            binding.progressCrop.visibility = View.GONE
                            CommonUtil.customToast(
                                requireContext(),
                                getString(R.string.an_error_select_the_image)
                            )
                            dismiss()
                        }
                        super.onLoadFailed(errorDrawable)
                    }
                })
        }
        binding.txtCrop.setOnClickListener {
            try {
                if (binding.imgCrop.croppedImage != null && binding.imgCrop.croppedImage.height > 0 && binding.imgCrop.croppedImage.width > 0) {
                    cropImageViewModel.convertBmToString(binding.imgCrop.croppedImage)
                    cropImageViewModel.pathBm.observe(viewLifecycleOwner) {
                        val bundle = Bundle()
                        bundle.putString(Constant.DATA_CHANGE_BACKGROUND_CUSTOMZIE, it)
                        EventBus.getDefault()
                            .post(MessageEvent(Constant.ACTION_CHANGE_BACKGROUND_CUSTOMZIE, bundle))
                        dismiss()
                        App.instance.linkCurrentBg = it
                        EventBus.getDefault().post(MessageEvent(CHECK_CROP_DONE))
                        // IListenCrops.checkCrop()
                    }
                }
            } catch (e: Exception) {

            }
            //
        }
        binding.txtCancelCrop.setOnClickListener {
            dismiss()
        }
        binding.btnBackCrop.setOnClickListener {
            dismiss()
        }
    }


    override fun onResume() {
        super.onResume()
    }

    interface IListenCrop {
        fun checkCrop()
    }
}