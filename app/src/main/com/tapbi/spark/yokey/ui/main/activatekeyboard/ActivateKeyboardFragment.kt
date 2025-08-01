package com.tapbi.spark.yokey.ui.main.activatekeyboard

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentActivateKeyboardBinding
import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils
import com.google.android.ads.nativetemplates.OnDecorationAds
import com.ironman.trueads.common.Common
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.base.BaseBottomSheetDialogFragment
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ActivateKeyboardFragment : BaseBottomSheetDialogFragment<FragmentActivateKeyboardBinding, ActivateKeyboardViewModel>() {
    override fun getViewModel(): Class<ActivateKeyboardViewModel> {
        return ActivateKeyboardViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_activate_keyboard

    override fun onCreatedView(view: View, savedInstanceState: Bundle?) {

        processRemoveAds(isRemoveAds)
        listener()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }


    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {

        super.onDestroyView()

    }

    fun listener() {
        binding.viewClose.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                if(checkDoubleClick()){
                    dismiss()
                }
                return true
            }

        })

        binding.viewActivateKeyboard.setOnClickListener {
            invokeLanguageAndInputSettings()
        }

        binding.viewSelectKeyboard.setOnClickListener {
            App.instance.mImm?.showInputMethodPicker()
        }
    }

    override fun onResume() {
        super.onResume()
        setupView()

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setupView() {
        if (!UncachedInputMethodManagerUtils.isThisImeEnabled(requireContext(), App.instance.mImm)){
            binding.viewActivateKeyboard.setBackgroundResource(R.drawable.bg_grandient_corner_14)
            binding.viewSelectKeyboard.setBackgroundResource(R.drawable.bg_enable_corner_14)
            binding.viewActivateKeyboard.isEnabled = true
            binding.viewSelectKeyboard.isEnabled = false
            binding.tvActiveKeyboard.setTextColor(Color.WHITE)
            binding.tvSelectKeyboard.setTextColor(Color.BLACK)
            binding.tvActiveKeyboard.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
            binding.tvSelectKeyboard.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
        }else if (!UncachedInputMethodManagerUtils.isThisImeCurrent(requireContext(), App.instance.mImm)) {
            binding.viewActivateKeyboard.setBackgroundResource(R.drawable.bg_enable_corner_14)
            binding.viewSelectKeyboard.setBackgroundResource(R.drawable.bg_grandient_corner_14)
            binding.viewActivateKeyboard.isEnabled = false
            binding.viewSelectKeyboard.isEnabled = true
            binding.tvActiveKeyboard.setTextColor(Color.BLACK)
            binding.tvSelectKeyboard.setTextColor(Color.WHITE)
            binding.tvActiveKeyboard.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
            binding.tvSelectKeyboard.typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_medium)
            binding.tvActiveKeyboard.setCompoundDrawablesWithIntrinsicBounds(requireContext().getDrawable(R.drawable.ic_tick),null,null,null)
        }else {
            if(isAdded){
                dismiss()
            }

        }
    }


    fun invokeLanguageAndInputSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_INPUT_METHOD_SETTINGS
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        if (CommonUtil.isIntentAvailable(intent, requireContext())) {
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageReceived(event: MessageEvent) {
        when (event.key) {
            Constant.ACTION_CHANGE_STATE_ACTIVATE_KEYBOARD -> {
                if (UncachedInputMethodManagerUtils.isThisImeCurrent(requireContext(), App.instance.mImm)) {
                    this@ActivateKeyboardFragment.dismiss()
                }
            }
        }
    }

    override fun processRemoveAds(isRemoveAds: Boolean) {
        super.processRemoveAds(isRemoveAds)
            val mapId = Common.getMapIdAdmobApplovin(requireActivity(),R.array.admob_native_id_active_keyboard,R.array.applovin_native_id_active_keyboard)
            showAdsNative(binding.frAdsNative,mapId,object :OnDecorationAds{
                override fun onDecoration(network: String?) {
                    binding.frAdsNative.getNativeAdView(network)?.setBackgroundResource(R.drawable.bg_ads_gray_detail)
                }

            })



    }




}