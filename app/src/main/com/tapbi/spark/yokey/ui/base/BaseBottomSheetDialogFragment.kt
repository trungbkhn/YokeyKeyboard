package com.tapbi.spark.yokey.ui.base

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.google.android.ads.nativetemplates.OnDecorationAds
import com.tapbi.spark.yokey.R
import com.google.android.ads.nativetemplates.TemplateViewMultiAds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ironman.spark.billing.PremiumLiveData
import com.ironman.trueads.multiads.MultiAdsControl
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.ui.main.MainViewModel
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.DisplayUtils

abstract class BaseBottomSheetDialogFragment<B : ViewDataBinding, T : BaseViewModel> : BaseDialogBottomSheet() {
    var isRemoveAds = false
    lateinit var binding: B
    lateinit var viewModel: T
    lateinit var mainViewModel: MainViewModel
    abstract val layoutId: Int
    protected abstract fun onCreatedView(view: View, savedInstanceState: Bundle?)
    protected abstract fun getViewModel(): Class<T>
    @JvmField
    protected var activity: MainActivity? = null
    protected var hasAdsShowed = false
    @JvmField
    protected var timeClick: Long = 0
    var valueAnimator: ValueAnimator? = null
    @JvmField
    var isActive = false
    @JvmField
    var SIZE_48 = DisplayUtils.dp2px(48f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BaseBottomSheetDialog)

        if (getActivity() is MainActivity) {
            activity = getActivity() as MainActivity?
        }
        viewModel = ViewModelProvider(this).get<T>(getViewModel())
        mainViewModel = ViewModelProvider(requireActivity()).get(
            MainViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
//        keyboardHeightProvider = KeyboardHeightProvider(requireActivity(), binding!!.root)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isRemoveAds = App.instance!!.billingManager!!.isPremium
        PremiumLiveData.get().observe(viewLifecycleOwner) { aBoolean: Boolean? ->
            if (aBoolean != null) {
                isRemoveAds = aBoolean
                processRemoveAds(isRemoveAds)
            }
        }
        onCreatedView(view, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener(DialogInterface.OnShowListener { dialog ->
            val dialogg = dialog as BottomSheetDialog
            val bottomSheet =
                dialogg.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
//            dialogg.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            dialogg.window!!.setBackgroundDrawableResource(R.color.color_transparent)
            dialogg.window!!.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            dialogg.window!!.statusBarColor = Color.TRANSPARENT
            dialogg.window!!.attributes.windowAnimations = R.style.CustomAnimDialog
            if (bottomSheet == null) return@OnShowListener
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            BottomSheetBehavior.from(bottomSheet).skipCollapsed = true
            BottomSheetBehavior.from(bottomSheet).isHideable = true
            BottomSheetBehavior.from(bottomSheet).isDraggable = false
        })
        return dialog
    }

    fun showToastError(content: String?) {
        if (isAdded) {
            Toast.makeText(requireActivity(), content, Toast.LENGTH_SHORT).show()
        }
    }

    open fun processRemoveAds(isRemoveAds: Boolean) {}
    fun checkDoubleClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - timeClick >= Constant.MIN_DURATION_BETWEEN_CLICK) {
            timeClick = currentTime
            return true
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        isActive = false
//        if (keyboardHeightProvider != null) {
//            keyboardHeightProvider!!.onPause()
//        }
    }

    override fun onResume() {
        super.onResume()
        isActive = true
//        if (keyboardHeightProvider != null) {
//            keyboardHeightProvider!!.onResume()
//        }
    }
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mainViewModel.dissmissDialogDetail.value=true
    }
    protected open fun showAdsNative(frameLayoutAds: TemplateViewMultiAds, mapId: HashMap<String,String>, onDecorationAds: OnDecorationAds?) {
        if (isRemoveAds) {
            frameLayoutAds.visibility = View.GONE
        } else {
            frameLayoutAds.visibility = View.VISIBLE
            MultiAdsControl.showNativeAdNoMedia(
                (requireActivity() as AppCompatActivity),
                mapId,
                frameLayoutAds,
                null, null, onDecorationAds)
        }
    }

}