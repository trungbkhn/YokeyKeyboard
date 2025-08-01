package com.flashkeyboard.led.ui.splash.onboard

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentOnboardBinding
import com.tapbi.spark.yokey.databinding.LayoutSelectThemeDefaultBinding

import com.bumptech.glide.Glide
import com.google.android.ads.nativetemplates.OnDecorationAds
import com.ironman.trueads.common.Common
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.CommonVariable
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.splash.SplashViewModel
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.DisplayUtils
import org.greenrobot.eventbus.EventBus


class OnboardFragment : BaseBindingFragment<FragmentOnboardBinding, SplashViewModel>() {

    private var type : Int = 0
    private var selectThemeDefaultBinding : LayoutSelectThemeDefaultBinding? = null
    private var idThemeDefault = Constant.ID_THEME_LED_DEFAULT
    private var idThemeSuggest = Constant.ID_THEME_DEFAULT
    private var pathThemeDefault: String? = null
    private var pathThemeSuggest: String? = null
    private var idChooseTheme : String? = null
    override fun getViewModel(): Class<SplashViewModel> {
        return SplashViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_onboard

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        if (type == 0){
            binding.tvTitle.visibility = View.VISIBLE
            binding.imgIcon.visibility = View.VISIBLE
            binding.tvTitle.setText(requireContext().getString(R.string.illuminate_your_experience_with_our_zomj_keyboard))
            Glide.with(this).load(R.drawable.ic_onboard_1).into(binding.imgIcon)

        }else if (type == 1){
            binding.tvTitle.visibility = View.VISIBLE
            binding.imgIcon.visibility = View.VISIBLE
            binding.tvTitle.setText(requireContext().getString(R.string.our_zomj_stickers_add_dynamic_hues))
            Glide.with(this).load(R.drawable.ic_onboard_2).into(binding.imgIcon)
        }else {
            binding.tvTitle.visibility = View.GONE
            binding.imgIcon.visibility = View.GONE
            initChooseThemeDefault()
        }
    }

    override fun onPermissionGranted() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            type = getInt("type")
        }

        if (type == 2) {
//            loadThemeChoose()
        }

    }

    override fun onResume() {
        super.onResume()
        if (type == 2) {
            selectThemeDefaultBinding?.apply {
                Log.d("duongcv", "onResume: " +selectThemeDefaultBinding!!.frAdsNative.height)

                var heightImage = (DisplayUtils.getScreenHeight() - selectThemeDefaultBinding!!.txtTitle.height - selectThemeDefaultBinding!!.frAdsNative.height - DisplayUtils.dp2px(200f))/2
                var widthImage = heightImage * 217 / 157
                val maxWidth = DisplayUtils.getScreenWidth() - DisplayUtils.dp2px(50f)
                if (widthImage > maxWidth) {
                    widthImage = maxWidth
                    heightImage = widthImage * 157 / 217
                }

                imgThemeCurrent.layoutParams?.width = widthImage
                imgThemeCurrent.layoutParams?.height = heightImage
                imgThemeSuggest.layoutParams?.width = widthImage
                imgThemeSuggest.layoutParams?.height = heightImage
                groupPreview.visibility = View.VISIBLE
                imgThemeSuggest.requestLayout()
                imgThemeCurrent.requestLayout()
            }
        }
    }

    private fun initChooseThemeDefault(){
        binding.viewStub.viewStub?.layoutResource = R.layout.layout_select_theme_default
        binding.viewStub.viewStub?.inflate()
        selectThemeDefaultBinding = binding.viewStub.binding as LayoutSelectThemeDefaultBinding?
        selectThemeDefaultBinding?.apply {
            ctlSelectThemeDefault.visibility = View.VISIBLE
            if(App.instance!!.billingManager!!.isPremium){
                frAdsNative.visibility=View.GONE
            }else{
                frAdsNative.visibility=View.VISIBLE
                val mapId = Common.getMapIdAdmobApplovin(requireActivity(),R.array.admob_native_id_onboard,R.array.applovin_native_id_onboard)
                showAdsNative(frAdsNative,mapId, object :OnDecorationAds{
                    override fun onDecoration(network: String?) {
                        frAdsNative.getNativeAdView(network)?.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.bg_ads_choose_theme))

                    }

                })
            }
        }
        eventListener()
    }


    private fun eventListener() {
        selectThemeDefaultBinding?.rbThemeCurrent?.setOnCheckedChangeListener { p0, p1 ->
            if (p1) {
                idChooseTheme = idThemeDefault
                selectThemeDefaultBinding?.rbThemeSuggest?.isChecked = false
            }
        }

        selectThemeDefaultBinding?.imgThemeCurrent?.setOnClickListener {
            idChooseTheme = idThemeDefault
            selectThemeDefaultBinding?.rbThemeSuggest?.isChecked = false
            selectThemeDefaultBinding?.rbThemeCurrent?.isChecked = true
        }

        selectThemeDefaultBinding?.imgThemeSuggest?.setOnClickListener {
            idChooseTheme = idThemeSuggest
            selectThemeDefaultBinding?.rbThemeSuggest?.isChecked = true
            selectThemeDefaultBinding?.rbThemeCurrent?.isChecked = false
        }

        selectThemeDefaultBinding?.rbThemeSuggest?.setOnCheckedChangeListener { p0, p1 ->
            if (p1) {
                idChooseTheme = idThemeSuggest
                selectThemeDefaultBinding?.rbThemeCurrent?.isChecked = false
            }
        }

        selectThemeDefaultBinding?.txtSkip?.setOnClickListener {
            if (idChooseTheme != null) {
                closeChooseTheme(idChooseTheme!!)
            }
            App.instance.mPrefs?.edit()?.putBoolean(Constant.CHECK_SELECT_THEME_DEFAULT, true)?.apply()
            EventBus.getDefault().post(MessageEvent(Constant.EVENT_GOTO_MAIN))
        }
    }

    private fun closeChooseTheme(idTheme: String) {
        val id = App.instance.mPrefs?.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, "0")
        App.instance.mPrefs?.edit()?.putString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, idTheme)?.apply()
//        if (!id.equals(idTheme)) {
        App.instance?.themeRepository?.updateCurrentThemeModel()
//        }
    }

    companion object {
        @JvmStatic
        fun newInstance(type: Int): OnboardFragment {
            val args = Bundle()
            args.putInt("type", type)
            val fragment = OnboardFragment()
            fragment.arguments = args
            return fragment
        }
    }

}