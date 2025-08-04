package com.tapbi.spark.yokey.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.android.inputmethod.latin.InputView
import com.android.inputmethod.latin.utils.UncachedInputMethodManagerUtils
import com.google.android.ads.nativetemplates.OnDecorationAds
import com.google.android.ads.nativetemplates.TemplateViewMultiAds
import com.ironman.spark.billing.BillingManager
import com.ironman.spark.billing.PremiumLiveData
import com.ironman.trueads.multiads.InterstitialAdsLiteListener
import com.ironman.trueads.multiads.MultiAdsControl
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.data.model.theme.ThemeModel
import com.tapbi.spark.yokey.interfaces.ActiveKeyboardListener
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.ui.main.MainViewModel
import com.tapbi.spark.yokey.util.Utils.showDialogPermission
import timber.log.Timber


abstract class BaseBindingFragment<B : ViewDataBinding, T : BaseViewModel> :
    BaseFragment() {
    lateinit var binding: B
    lateinit var viewModel: T

    lateinit var mainViewModel: MainViewModel
    protected abstract fun getViewModel(): Class<T>
    abstract val layoutId: Int
     var inputMethodManager: InputMethodManager? = null
    var isShowKeyboard = false
    var mInputView : InputView? = null
    var isActivateKeyboard : Boolean = false


    //val myDataStore: MyDataStore by lazy { MyDataStore(requireContext()) }
    protected abstract fun onCreatedView(view: View?, savedInstanceState: Bundle?)
    protected abstract fun onPermissionGranted()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        viewModel = ViewModelProvider(this)[getViewModel()]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        lifecycle.addObserver(BillingManager)
        isRemoveAds= App.instance!!.billingManager!!.isPremium
        PremiumLiveData.get().observe(viewLifecycleOwner) { aBoolean ->
            if (aBoolean != null) {
                isRemoveAds = aBoolean
                processRemoveAds(isRemoveAds)
            }
        }
        onCreatedView(view, savedInstanceState)
    }

    @SuppressLint("CommitPrefEdits")
    fun showHideKeyboard(edtView: EditText?) {
        if (isShowKeyboard) {
            isShowKeyboard = false
            edtView?.setText("")
            edtView?.clearFocus()
            inputMethodManager!!.hideSoftInputFromWindow(edtView?.windowToken, 0)
        } else {
            edtView?.requestFocus()
            inputMethodManager!!.showSoftInput(edtView, 0)
        }
    }

    fun showAdsFull(positionAdName :String) {
        if (App.instance!!.billingManager!!.isPremium) {
            nexScreenAfterAds()
        } else {
            MultiAdsControl.showInterstitialLite(requireActivity(),positionAdName,false, object :InterstitialAdsLiteListener{
                override fun onInterstitialAdsNextScreen(adsType: Int) {
                    nexScreenAfterAds()
                }

                override fun onInterstitialAdsShowFully(adsType: Int) {
                    mainViewModel.eventDismissLoadingAds.postValue(true)
                }

                override fun onPrepareShowInterstitialAds(adsType: Int) {
                    if (isAdded) {
                        (requireActivity() as MainActivity).setVisibilityProgressAds(View.VISIBLE)
                    }
                }
            })
        }
    }
    open fun nexScreenAfterAds(){
        mainViewModel.mLiveDataNextAfterAds.postValue(true)
    }


    open fun processRemoveAds(isRemoveAds: Boolean) {}

    fun checkActivateKeyboard(idScreen : Int, isShowKb : Boolean, groupInputText : Group?, edtInput : EditText?, mInputView : InputView?, ctlPreviewInputView : ConstraintLayout
                              , imgShowKeyboard : CardView?, spaceBottom : View?, isChangeBuilder : Boolean, theme : ThemeModel?) : InputView?  {
        if (UncachedInputMethodManagerUtils.isThisImeEnabled(requireContext(), inputMethodManager) && UncachedInputMethodManagerUtils.isThisImeCurrent(requireContext(), inputMethodManager)) {
            isActivateKeyboard = true
            mInputView?.parent?.apply {
                Timber.d("duongcv active keyboard remove");
                ctlPreviewInputView.removeView(mInputView)
            }
            groupInputText?.visibility = View.VISIBLE
            imgShowKeyboard?.visibility = View.VISIBLE
            spaceBottom?.visibility = View.VISIBLE

            return mInputView
        } else {
            isActivateKeyboard = false
            groupInputText?.visibility = View.GONE
            imgShowKeyboard?.visibility = View.GONE
            spaceBottom?.visibility = View.GONE

            var isAdded = false
            for (i in ctlPreviewInputView.childCount - 1 downTo 0) {
                val child: View = ctlPreviewInputView.getChildAt(i)
                if (child is InputView) {
                    isAdded = true
                    break
                }
            }

            if ((mInputView == null || !isAdded) && theme != null) {

                return mainViewModel.initPreview(
                    requireContext(),
                    ctlPreviewInputView,
                    theme,
                    isChangeBuilder,
                    object : ActiveKeyboardListener {
                        override fun active() {
                            if (checkDoubleClick() && isResumed) {
                                showDialogPermission(requireContext())
                            }
                        }

                    })
            }else {
                mInputView?.mainKeyboardView?.setThemeForKeyboard()
                mInputView?.updateBackgroundKeyboard(theme?.background?.backgroundColor, theme?.background?.backgroundImage)
                mInputView?.updateColorIconMenu()
                mInputView?.mainKeyboardView?.invalidateAllKeys()
            }
//            if (idScreen == R.id.createThemeFragment) {
//                mainViewModel.mLiveEventUpdatePagerControl.value = true
//            }


            return mInputView
        }
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

    fun popBackStack() {
        findNavController().popBackStack()
    }

    fun popBackStack(id: Int, isInclusive: Boolean) {
        findNavController().popBackStack(id, isInclusive)
    }

    fun navigateScreenWithSlide(bundle: Bundle?, id: Int) {
        val navBuilder = NavOptions.Builder()
        navBuilder.setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
        findNavController().navigate(id, bundle, navBuilder.build())
    }

    fun navigateScreen(bundle: Bundle?, id: Int) {
        val navBuilder = NavOptions.Builder()
        findNavController().navigate(id, bundle, navBuilder.build())
    }

    fun navigateScreenAndPopBackStackWithSlide(
        bundle: Bundle? = null,
        idNavigate: Int,
        idPopBackStack: Int,
        inclusive: Boolean = true
    ) {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .setPopUpTo(idPopBackStack, inclusive)
            .build()
        findNavController().navigate(idNavigate, bundle, navOptions)
    }

    fun navigateScreenAndPopBackStack(
        bundle: Bundle? = null,
        idNavigate: Int,
        idPopBackStack: Int,
        inclusive: Boolean = true
    ) {
//        val navOptions = NavOptions.Builder()
//            .setEnterAnim(R.anim.slide_in_right)
//            .setExitAnim(R.anim.slide_out_left)
//            .setPopEnterAnim(R.anim.slide_in_left)
//            .setPopExitAnim(R.anim.slide_out_right)
//            .setPopUpTo(idPopBackStack, inclusive)
//            .build()
        val navOptions = NavOptions.Builder()
            .setPopUpTo(idPopBackStack, inclusive)
            .build()

        findNavController().navigate(idNavigate, bundle, navOptions)
    }

}