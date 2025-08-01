package com.tapbi.spark.yokey.ui.main.detailfont

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentDetailFontBinding
import com.bumptech.glide.Glide
import com.google.android.ads.nativetemplates.OnDecorationAds
import com.ironman.trueads.common.Common
import com.tapbi.spark.yokey.App.Companion.instance
import com.tapbi.spark.yokey.data.local.entity.ItemFont
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.base.BaseBindingDialogFragment
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class DetailFontFragment : BaseBindingDialogFragment<FragmentDetailFontBinding, DetailFontViewModel>() {

    private var itemFont : ItemFont? = null
    override val layoutId: Int
        get() = R.layout.fragment_detail_font

    override fun getViewModel(): Class<DetailFontViewModel> {
        return DetailFontViewModel::class.java
    }

    override fun onCreatedView(view: View, savedInstanceState: Bundle?) {
        if (mainViewModel.mLiveDataDetailObject.value != null && mainViewModel.mLiveDataDetailObject.value is ItemFont) {
            itemFont = mainViewModel.mLiveDataDetailObject.value as ItemFont
            mainViewModel.mLiveDataDetailObject.postValue(null)
        }
        if (savedInstanceState != null && itemFont == null){
            val idFont = savedInstanceState.getInt(com.tapbi.spark.yokey.common.Constant.SAVE_ID_FONT)
            viewModel.resultLoadFont.observe(this, object : Observer<ItemFont> {
                override fun onChanged(value: ItemFont) {
                    itemFont = value
                    init()
                }

            })
            viewModel.getFontById(idFont, requireContext())
        }else {
            init()
        }
        processRemoveAds(isRemoveAds)
        listener()
    }

    fun init() {
        if (itemFont != null) {
            setLayoutImagePreview(165, 107, binding.imgPreviewFont)
            val sp = instance.fontRepository?.font?.getFont(itemFont!!.textFont)
            val c = StringBuilder()
            c.append("")
            CommonUtil.appendText(instance.fontRepository!!.font, sp, c, itemFont!!.textDemo, 0)
            val c1 = StringBuilder()
            c1.append("")
            CommonUtil.appendText(instance.fontRepository!!.font, sp, c1, itemFont!!.textFont, 0)
            binding.txtNameFont.text =
                if (itemFont!!.textFont == Constant.FONT_STOP || itemFont!!.textFont == Constant.FONT_ROUND_STAMP) " $c1" else c1.toString()
            binding.txtDemoFont.setText(c.toString())
            binding.txtNameFont.setText(if (itemFont!!.textFont == Constant.FONT_STOP || itemFont!!.textFont == Constant.FONT_ROUND_STAMP) " $c1" else c1.toString())
            if (itemFont!!.isAdd == 0) {
                binding.btnApplyFont.setText(instance.resources.getString(R.string.txt_apply_font))
                binding.txtDescriptionDowload.visibility = View.VISIBLE
            } else {
                binding.btnApplyFont.setText(
                    instance.resources.getString(R.string.apply_font_done)
                )
                binding.txtDescriptionDowload.visibility = View.GONE
            }
            Glide.with(this).load(itemFont!!.imgBackground)
                .placeholder(R.drawable.placeholder_keyboard_theme).override(600)
                .into(binding.imgPreviewFont)
        }
    }



     fun listener() {
         binding.btnApplyFont.setOnClickListener {
             if (binding.btnApplyFont.getText().toString().equals(instance.resources.getString(R.string.apply_font_done), true)){
                 instance.mPrefs?.edit()?.putString(Constant.USING_FONT, itemFont!!.textFont)?.apply()
                 instance.fontRepository?.updateCurrentFont()
                 instance.fontRepository!!.loadListFontIsAdd()
                 changeScreen(R.id.tryKeyboardFragment, com.tapbi.spark.yokey.common.Constant.TYPE_TRY_FONT_KEYBOARD)
             }else {
                goToApplyFont()
             }
         }

         binding.imgClose.setOnClickListener {
             dismiss()
         }

         binding.ctlPremium.setOnClickListener {
             if (checkDoubleClick()){
                 changeScreen(R.id.premiumFragment, com.tapbi.spark.yokey.common.Constant.TYPE_TRY_FONT_KEYBOARD)
             }
         }
         binding.ctlPremium2.setOnClickListener {
             if (checkDoubleClick()){
                 changeScreen(R.id.premiumFragment, com.tapbi.spark.yokey.common.Constant.TYPE_TRY_FONT_KEYBOARD)
             }
         }
     }

    @SuppressLint("CheckResult")
    private fun goToApplyFont() {
        if ( itemFont != null &&  itemFont!!.textFont != null) {
            binding.spinKitDetailActivity.setVisibility(View.VISIBLE)
            binding.btnApplyFont.visibility = View.INVISIBLE
            instance.mPrefs?.edit()?.putString(Constant.USING_FONT, itemFont!!.textFont)?.apply()
            instance.fontRepository?.updateCurrentFont()
            binding.btnApplyFont.setText(instance.resources.getString(R.string.apply_font_done))
            binding.txtDescriptionDowload.visibility = View.GONE
            handler.postDelayed({
                if (isAdded && isResumed) {
                    if (itemFont!!.isAdd == 0) {
                        instance.fontRepository!!.updateFontToDB(instance, itemFont)
                            .subscribe { result ->
                                binding.spinKitDetailActivity.setVisibility(View.INVISIBLE)
                                binding.btnApplyFont.visibility = View.VISIBLE
                                if (result) {
                                    instance.fontRepository!!.loadListFontIsAdd()
                                    changeScreen(
                                        R.id.tryKeyboardFragment,
                                        com.tapbi.spark.yokey.common.Constant.TYPE_TRY_FONT_KEYBOARD
                                    )
                                }
                            }
                    } else {
                        dismiss()
                    }
                }
            }, 400)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageReceived(event: MessageEvent) {

    }
    override fun processRemoveAds(isRemoveAds: Boolean) {
        super.processRemoveAds(isRemoveAds)
        val mapId = Common.getMapIdAdmobApplovin(requireActivity(),R.array.admob_native_id_detail_font,R.array.applovin_native_id_detail_font)
        showAdsNative(binding.frAdsNative,mapId,object : OnDecorationAds {
            override fun onDecoration(network: String?) {
                binding.frAdsNative.getNativeAdView(network)?.setBackgroundResource(R.drawable.bg_ads_gray_detail)
            }

        })
        showViewAds(binding.ctlPremium, binding.ctlPremium2, binding.layoutContent, binding.frAdsNative)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(com.tapbi.spark.yokey.common.Constant.SAVE_ID_FONT, itemFont?.id ?: 0)
        super.onSaveInstanceState(outState)
    }

}