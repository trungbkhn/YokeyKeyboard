package com.tapbi.spark.yokey.ui.main.detailtheme

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentDetailThemeBinding
import com.bumptech.glide.Glide
import com.google.android.ads.nativetemplates.OnDecorationAds
import com.ironman.trueads.common.Common
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.common.CommonVariable
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.data.model.ThemeObject
import com.tapbi.spark.yokey.ui.base.BaseBindingDialogFragment
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.LocaleUtils.applyLocale
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.File


class DetailThemeFragment :  BaseBindingDialogFragment<FragmentDetailThemeBinding, DetailThemeViewModel>() {

    private var fileTheme: File? = null
    private var themeModel: ThemeObject? = null
    private var themeEntity: ThemeEntity? = null
    private var isDownload = false

    override val layoutId: Int
    get() = R.layout.fragment_detail_theme

    override fun getViewModel(): Class<DetailThemeViewModel> {
        return DetailThemeViewModel::class.java
    }

    override fun onCreatedView(view: View, savedInstanceState: Bundle?) {
        isActive = true

        if (mainViewModel.mLiveDataThemeEntity.value != null && mainViewModel.mLiveDataThemeEntity.value is ThemeEntity) {
                themeEntity = mainViewModel.mLiveDataThemeEntity.value as ThemeEntity
        }
        if (mainViewModel.mLiveDataDetailObject.value != null && mainViewModel.mLiveDataDetailObject.value is ThemeObject) {
            themeModel = mainViewModel.mLiveDataDetailObject.value as ThemeObject
        }

        mainViewModel.mLiveDataThemeEntity.postValue(null)
        mainViewModel.mLiveDataDetailObject.postValue(null)
        if (themeModel == null && themeEntity == null && isActive) dismiss()

        initView()
        processRemoveAds(isRemoveAds)
        listener()
        observer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isActive = false
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    fun initView() {
        if (themeModel != null) {
            Timber.e("hachung id : ${themeModel!!.id}")
            binding.imgPreviewThemeDetail.setScaleType(ImageView.ScaleType.FIT_XY)
            if (themeEntity != null && themeEntity!!.isMyTheme == 1) {
                setLayoutImagePreview(180, 111, binding.imgPreviewThemeDetail)
                Glide.with(this).load(themeModel!!.preview)
                    .placeholder(R.drawable.placeholder_keyboard_theme).override(800)
                    .into(binding.imgPreviewThemeDetail)
            } else {
                setLayoutImagePreview(18, 13, binding.imgPreviewThemeDetail)
                Glide.with(this).asBitmap().load(themeModel!!.preview)
                    .placeholder(R.drawable.placeholder_keyboard_theme).override(800)
                    .into(binding.imgPreviewThemeDetail)
            }
            binding.txtNameKeyboard.text = themeModel!!.name
            initButtonApply(themeEntity, themeModel)
        } else if (themeEntity != null) {
            binding.imgPreviewThemeDetail.setScaleType(ImageView.ScaleType.FIT_XY)
            setLayoutImagePreview(18, 13, binding.imgPreviewThemeDetail)
            Glide.with(this).load(themeEntity!!.preview)
                .placeholder(R.drawable.placeholder_keyboard_theme).override(600)
                .into(binding.imgPreviewThemeDetail)
            binding.txtNameKeyboard.text = themeEntity!!.name
           initButtonApply(themeEntity, themeModel)
        }else {
            dismiss()
        }


    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initButtonApply(themeEntity: ThemeEntity?, themeObject: ThemeObject?) {
        val contextWrapper = ContextWrapper(instance)
        val destinationFile = contextWrapper.getDir(requireContext().filesDir.name, Context.MODE_PRIVATE)
        if (themeEntity != null) {
            fileTheme = File(destinationFile, themeEntity.id)
            if (fileTheme!!.exists()) {
                if (!themeEntity.id.equals(instance.mPrefs?.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, ""), ignoreCase = true)) {
                    binding.btnApplyTheme.text = instance.resources.getString(R.string.apply_theme)
                    binding.btnApplyTheme.visibility = View.VISIBLE
                    binding.txtDescriptionDowload.visibility = View.INVISIBLE
                    binding.ctlDownload.visibility = View.INVISIBLE
                    binding.btnApplyTheme.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        null,
                        null
                    )
                } else {
                    binding.btnApplyTheme.text = instance.resources.getString(R.string.apply_theme_done)
                    binding.btnApplyTheme.visibility = View.VISIBLE
                    binding.txtDescriptionDowload.visibility = View.INVISIBLE
                    binding.ctlDownload.visibility = View.INVISIBLE
                    binding.btnApplyTheme.setCompoundDrawablesWithIntrinsicBounds(
                        requireContext().getDrawable(
                            R.drawable.ic_applied
                        ), null, null, null
                    )
                }
            }else {
                binding.btnApplyTheme.text = requireContext().getString(R.string.txt_get_theme)
                binding.btnApplyTheme.visibility = View.INVISIBLE
                binding.txtDescriptionDowload.visibility = View.VISIBLE
                binding.ctlDownload.visibility = View.VISIBLE
                binding.btnApplyTheme.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
                )
            }
        } else if (themeObject != null) {
            val subString = themeObject.urlTheme
            var idFolder = "1" //end.substring(end.indexOf(".zip"));
            idFolder = if (subString != null) {
                val end = subString.substring(
                    subString.indexOf(".com") + ".com".length + 1,
                    subString.length
                )
                if (end.contains("/")) {
                    end.substring(0, end.indexOf("/"))
                } else {
                    end.substring(0, end.indexOf("."))
                }
            } else {
                themeObject.id.toString()
            }
            // TODO: chungvv update local
            fileTheme = File(destinationFile, idFolder)
            if (fileTheme!!.exists() ||
                themeModel!!.id.toString().toInt() > 6000 && themeModel!!.id.toString().toInt() < 7000
                ||themeModel!!.id.toString().toInt() > 4012 && themeModel!!.id.toString().toInt() < 5000
                ||themeModel!!.id.toString().toInt() > 3015 && themeModel!!.id.toString().toInt() < 4000
                ||themeModel!!.id.toString().toInt() > 2003 && themeModel!!.id.toString().toInt() < 3000
                ) {
                if (!idFolder.equals(instance.mPrefs?.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, ""), ignoreCase = true)) {
                    binding.btnApplyTheme.text = instance.resources.getString(R.string.apply_theme)
                    binding.btnApplyTheme.visibility = View.VISIBLE
                    binding.txtDescriptionDowload.visibility = View.INVISIBLE
                    binding.ctlDownload.visibility = View.INVISIBLE
                    binding.btnApplyTheme.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                } else {
                    binding.btnApplyTheme.text = instance.resources.getString(R.string.apply_theme_done)
                    binding.btnApplyTheme.visibility = View.VISIBLE
                    binding.txtDescriptionDowload.visibility = View.INVISIBLE
                    binding.ctlDownload.visibility = View.INVISIBLE
                    binding.btnApplyTheme.setCompoundDrawablesWithIntrinsicBounds(
                        requireContext().getDrawable(
                            R.drawable.ic_applied
                        ), null, null, null
                    )
                }
            }else {
                binding.btnApplyTheme.text = requireContext().getString(R.string.txt_get_theme)
                binding.btnApplyTheme.visibility = View.INVISIBLE
                binding.txtDescriptionDowload.visibility = View.VISIBLE
                binding.ctlDownload.visibility = View.VISIBLE
                binding.btnApplyTheme.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
                )
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageReceived(event: MessageEvent) {

    }

    fun listener() {
        binding.btnApplyTheme.setOnClickListener{
            if (checkDoubleClick()){
                applyLocale(requireContext())
                if (isDownload && binding.btnApplyTheme.text.toString().equals(instance.resources.getString(R.string.apply_theme_done), true)) {
                    changeScreen(R.id.tryKeyboardFragment, com.tapbi.spark.yokey.common.Constant.TYPE_TRY_THEME_KEYBOARD)
                }else if (!isDownload) {
                    if (!binding.btnApplyTheme.text.toString().equals(instance.resources.getString(R.string.apply_theme_done), true)) {
                        if (instance.connectivityStatus != -1) {
                            isDownload = true
                        }
                        applyTheme()
                    } else {

                    }
                }

            }
        }

        binding.ctlDownload.setOnClickListener {
            if (checkDoubleClick()){
                 if (!isDownload) {
                    if (!binding.btnApplyTheme.text.toString().equals(instance.resources.getString(R.string.apply_theme_done), true)) {
                        if (instance.connectivityStatus != -1) {
                            isDownload = true
                        }
                        applyTheme()
                    }
                }

            }
        }

        binding.imgClose.setOnClickListener {
            if(isAdded){
                dismiss()
            }

        }

        binding.ctlPremium.setOnClickListener {
            if (checkDoubleClick()) {
                changeScreen(R.id.premiumFragment, com.tapbi.spark.yokey.common.Constant.TYPE_TRY_THEME_KEYBOARD)
            }
        }
        binding.ctlPremium2.setOnClickListener {
            if (checkDoubleClick()) {
                changeScreen(R.id.premiumFragment, com.tapbi.spark.yokey.common.Constant.TYPE_TRY_THEME_KEYBOARD)
            }
        }
    }

    fun observer(){
        viewModel.mLiveEventUpdateThemeEntityDB.observe(viewLifecycleOwner) { result ->
            if (result){
                if (isVisible) {
                    changeScreen(R.id.tryKeyboardFragment, com.tapbi.spark.yokey.common.Constant.TYPE_TRY_THEME_KEYBOARD)
                } else {
                    EventBus.getDefault().post(MessageEvent(Constant.KEY_CHANGE_THEME_NOT_SHOW_PREVIEW))
                }
            }else {
                if (isVisible) {
                    binding.spinKitDetailActivity.visibility = View.INVISIBLE
                    binding.btnApplyTheme.visibility = View.VISIBLE
                    CommonUtil.customToast(requireContext(), instance.resources.getString(R.string.apply_theme_fail))
                }
            }
        }

        viewModel.mLiveEventDownTheme.observe(viewLifecycleOwner){ result ->
            if (isVisible) {
                if (result) {
                    if (viewModel.fileDownload != null) {
                        viewModel.updateThemeEntityDB(viewModel.fileDownload!!, themeModel!!, themeEntity)
                    }
                } else {
                    binding.spinKitDetailActivity.setVisibility(View.INVISIBLE)
                    binding.btnApplyTheme.visibility = View.VISIBLE
                    CommonUtil.customToast(requireContext(), instance.resources.getString(R.string.apply_theme_fail))
                }
                isDownload = false
            }
        }

        viewModel.mLiveEventUpdateThemeEntity.observe(viewLifecycleOwner){result ->
            if (isAdded) {
                val contextWrapper = ContextWrapper(instance)
                val file = contextWrapper.getDir(instance.filesDir.name, Context.MODE_PRIVATE)
                val strPath = file.toString() + "/" + themeEntity!!.id + "/theme.json"
                if (File(strPath).exists()) {
                    instance.mPrefs?.edit()?.putString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, themeEntity!!.id)?.apply()
                    instance.themeRepository!!.updateCurrentThemeModel()
                    if (isVisible) {
                        changeScreen(R.id.tryKeyboardFragment, com.tapbi.spark.yokey.common.Constant.TYPE_TRY_THEME_KEYBOARD)
                    } else {
                        EventBus.getDefault()
                            .post(MessageEvent(Constant.KEY_CHANGE_THEME_NOT_SHOW_PREVIEW))
                    }
                } else {
                    if (isVisible) {
                        CommonUtil.customToast(requireContext(), instance.resources.getString(R.string.apply_theme_fail))
                        binding.spinKitDetailActivity.setVisibility(View.INVISIBLE)
                        binding.btnApplyTheme.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun applyTheme() {
        if (themeModel != null && themeEntity == null) {
            binding.spinKitDetailActivity.setVisibility(View.VISIBLE)
            binding.btnApplyTheme.visibility = View.INVISIBLE
            binding.txtDescriptionDowload.visibility = View.INVISIBLE
            binding.ctlDownload.visibility = View.INVISIBLE
            // check file theme exists or no and show keyboard and save theme
            // TODO: chungvv update local
            if (fileTheme!!.exists() || themeModel!!.id.toString().toInt() > 6000 && themeModel!!.id.toString().toInt() < 7000
                || themeModel!!.id.toString().toInt() > 4012 && themeModel!!.id.toString().toInt() < 5000
                || themeModel!!.id.toString().toInt() > 3015 && themeModel!!.id.toString().toInt() < 4000
                || themeModel!!.id.toString().toInt() > 2003 && themeModel!!.id.toString().toInt() < 3000
                ) {

                viewModel.gotoApplyTheme(themeModel!!, themeEntity)
            } else {
                if (instance.connectivityStatus == -1) {
                    CommonUtil.customToast(requireContext(), instance.resources.getString(R.string.text_check_internet))
                    binding.spinKitDetailActivity.setVisibility(View.INVISIBLE)
                    binding.btnApplyTheme.visibility = View.VISIBLE
                } else {
                    viewModel.downloadZipFileTheme(themeModel!!)
                }
            }
        } else if (themeEntity != null) {
            binding.spinKitDetailActivity.setVisibility(View.VISIBLE)
            binding.btnApplyTheme.visibility = View.INVISIBLE
            binding.txtDescriptionDowload.visibility = View.INVISIBLE
            binding.ctlDownload.visibility = View.INVISIBLE
            if (fileTheme!!.exists()) {
                viewModel.gotoApplyThemeEntity(themeEntity!!)
            } else {

            }
        }
    }

    override fun processRemoveAds(isRemoveAds: Boolean) {
        super.processRemoveAds(isRemoveAds)
        val mapId = Common.getMapIdAdmobApplovin(requireActivity(),R.array.admob_native_id_detail_theme,R.array.applovin_native_id_detail_theme)
        showAdsNative(binding.frAdsNative,mapId,object : OnDecorationAds {
            override fun onDecoration(network: String?) {
                binding.frAdsNative.getNativeAdView(network)?.setBackgroundResource(R.drawable.bg_ads_gray_detail)
            }

        })
        showViewAds(binding.ctlPremium, binding.ctlPremium2, binding.layoutContent, binding.frAdsNative)



    }



}