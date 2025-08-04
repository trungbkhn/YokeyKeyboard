package com.tapbi.spark.yokey.ui.base

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.ads.nativetemplates.OnDecorationAds
import com.tapbi.spark.yokey.R
import com.google.android.ads.nativetemplates.TemplateViewMultiAds
import com.google.android.material.imageview.ShapeableImageView
import com.ironman.trueads.multiads.MultiAdsControl
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.ui.main.MainViewModel
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.DisplayUtils
import org.greenrobot.eventbus.EventBus

abstract class BaseBindingDialogFragment<B : ViewDataBinding, T : BaseViewModel> : DialogFragment() {
    lateinit var binding: B
    lateinit var viewModel: T
    lateinit var mainViewModel: MainViewModel
    abstract val layoutId: Int
    protected abstract fun onCreatedView(view: View, savedInstanceState: Bundle?)
    protected abstract fun getViewModel(): Class<T>
    @JvmField
    protected var activity: MainActivity? = null
    @JvmField
    protected var isRemoveAds = false
    protected var hasAdsShowed = false
    @JvmField
    protected var viewAds: View? = null
    protected var timeClick: Long = 0
    var valueAnimator: ValueAnimator? = null
    @JvmField
    var isActive = false
    @JvmField
    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getActivity() is MainActivity) {
            activity = getActivity() as MainActivity?
        }
        EventBus.getDefault().register(this)
        viewModel = ViewModelProvider(this).get<T>(getViewModel())
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val window = dialog.window
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            window.addFlags(WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setWindowAnimations(R.style.CustomStyleAnimDialogFragment)
        }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mainViewModel.dissmissDialogDetail.value=true
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = DisplayUtils.getScreenHeight()
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isRemoveAds = App.instance.billingManager!!.isPremium
        onCreatedView(view, savedInstanceState)

        mainViewModel.mLiveDataRemoveAds.observe(viewLifecycleOwner) { aBoolean ->
            isRemoveAds = aBoolean
            processRemoveAds(isRemoveAds)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    override fun onResume() {
        super.onResume()
        isActive = true

    }

    fun setLayoutImagePreview(wb: Int, hb: Int, image : ShapeableImageView) {
        val w = DisplayUtils.getScreenWidth() - DisplayUtils.dp2px(70f)
        val params = image.layoutParams as ConstraintLayout.LayoutParams
        params.height = w * hb / wb
        image.layoutParams = params
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

    fun showViewAds(ctlPremium : ConstraintLayout, ctlPremium2 : ConstraintLayout, layoutContent : ConstraintLayout , frAds : TemplateViewMultiAds){
        if (!isRemoveAds) {
            (frAds.layoutParams as ConstraintLayout.LayoutParams).topMargin = CommonUtil.dpToPx(requireActivity(),
                CommonUtil.marginAdsNativeDetail().toInt())
            if (CommonUtil.positionAdsNativeDetail().toInt() == 0) {
                ctlPremium.visibility = View.VISIBLE
                ctlPremium2.visibility = View.GONE
//                (layoutContent.layoutParams as ConstraintLayout.LayoutParams).bottomMargin = DisplayUtils.dp2px(40f)
            }else {
                ctlPremium.visibility = View.GONE
                ctlPremium2.visibility = View.VISIBLE
//                (layoutContent.layoutParams as ConstraintLayout.LayoutParams).bottomMargin = 0
            }
        }else {
            ctlPremium.visibility = View.GONE
            ctlPremium2.visibility = View.GONE
            (layoutContent.layoutParams as ConstraintLayout.LayoutParams).topMargin = (DisplayUtils.getScreenHeight() - DisplayUtils.dp2px(330f)) / 2
        }
    }

}