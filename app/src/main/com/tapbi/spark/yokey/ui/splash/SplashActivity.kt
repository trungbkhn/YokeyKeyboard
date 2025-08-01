package com.tapbi.spark.yokey.ui.splash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.applovin.mediation.MaxAd
import com.ironman.spark.billing.PremiumLiveData
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.ActivitySplashBinding
import com.tapbi.spark.yokey.databinding.LayoutOnboardingBinding
import com.ironman.trueads.admob.interstital.InterstitialAdAdmob.resetParam
import com.ironman.trueads.common.RemoteConfigControl
import com.ironman.trueads.multiads.InitMultiAdsListener
import com.ironman.trueads.multiads.MultiAdsControl
import com.ironman.trueads.multiads.ShowOpenAdsListener
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant.CHECK_FIRST_TIME_SHOW_LANGUAGE
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.adapter.ViewPagerOnboardAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingActivity
import com.tapbi.spark.yokey.ui.language.LanguageActivity
import com.tapbi.spark.yokey.ui.welcome.WelcomeActivity
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.DisplayUtils
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

class SplashActivity : BaseBindingActivity<ActivitySplashBinding, SplashViewModel>() {
    lateinit var mPrefs: SharedPreferences
    private val lottie: LottieAnimationView by lazy { findViewById(R.id.lottie_splash) }
    private var checkDismissAdsOpenApp = false
    private var isRunLottie = false

    private var valueAnimator: ValueAnimator? = null
    private var layoutOnboardingBinding: LayoutOnboardingBinding? = null
    private var viewPagerOnboardAdapter: ViewPagerOnboardAdapter? = null
    override val layoutId: Int
        get() = R.layout.activity_splash

    override fun getViewModel(): Class<SplashViewModel> {
        return SplashViewModel::class.java
    }

    override fun setupView(savedInstanceState: Bundle?) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        MultiAdsControl.setConfigForAdsOpenSplash(this)
        RemoteConfigControl.initRemoteConfig(applicationContext)
        EventBus.getDefault().register(this)
        isRunLottie = false
        lottie.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (App.instance!!.billingManager!!.isPremium) showViewSelectThemeDefault()
            }
        })
        App.instance.billingManager!!.refreshPurchase(false, false,null)
        PremiumLiveData.get().observe(this) { aBoolean ->
            handlerConsent.removeCallbacks(runnableDelayConsent)
            if (!aBoolean) {
                if(!checkDismissAdsOpenApp){
                    handlerConsent.postDelayed(runnableDelayConsent, 3000)
                }
            } else {
                showViewSelectThemeDefault()
            }
        }
    }

    override fun setupData() {
        viewModel?.checkUpdateThemeSticker(this)

        viewModel?.liveOpenAds?.observe(this, object  : Observer<Boolean> {
            override fun onChanged(value: Boolean) {
                showAdsOpen()
            }

        })
    }

    fun showAdsOpen() {
        MultiAdsControl.loadAndShowOpenAds(this,
            true, object : ShowOpenAdsListener {
                override fun onPrepareShowAdsOpenApp() {}
                override fun onAdRevenuePaid(ad: MaxAd?) {

                }

                override fun onAdsOpenClicked() {}
                override fun onLoadedAdsOpenApp() {
                }

                override fun onLoadFailAdsOpenApp() {
                    showViewSelectThemeDefault()

                }

                override fun onShowAdsOpenAppDismissed() {
                    showViewSelectThemeDefault()

                }

                override fun onAdsOpenLoadedButNotShow() {
                    showViewSelectThemeDefault()

                }
            })
    }

    private fun goToMain() {
        val intent = Intent()
        if (mPrefs.getBoolean(CHECK_FIRST_TIME_SHOW_LANGUAGE, true)) {
            if (!CommonUtil.isFirstInstall(this)) {
                mPrefs.edit().putBoolean(CHECK_FIRST_TIME_SHOW_LANGUAGE, false).apply()
            }
        }

        if (mPrefs.getBoolean(CHECK_FIRST_TIME_SHOW_LANGUAGE, true)) {
            intent.setClass(this, LanguageActivity::class.java)
        } else {
            intent.setClass(this@SplashActivity, WelcomeActivity::class.java)
            intent.flags = (Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (getIntent() != null) {
                intent.putExtra(
                    Constant.KEY_OPEN_SCREEN,
                    getIntent().getIntExtra(Constant.KEY_OPEN_SCREEN, Constant.KEY_SCREEN_THEME)
                )
                if (getIntent().getBundleExtra(Constant.DATA_BUNDLE) != null) intent.putExtra(
                    Constant.DATA_BUNDLE, getIntent().getBundleExtra(
                        Constant.DATA_BUNDLE
                    )
                )
            }
        }
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (!MultiAdsControl.checkAdsOpenIsShowing(App.instance)) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isRunLottie) lottie.playAnimation()
        if (checkDismissAdsOpenApp) {
            showViewSelectThemeDefault()
        }
    }

    fun initAdAndLoadAds() {
        App.instance!!.initAds(this, object : InitMultiAdsListener {

            override fun onInitAllAdsCompleted(networkAdsStateAll: Long, canNextScreen: Boolean) {
                resetParam()
                autoLoadsAds()
            }

            override fun onLoadAdsOpen(networkAdsOpen: String?) {
                viewModel?.let {
                    it.liveOpenAds.postValue(true)
                }?: run {
                    showViewSelectThemeDefault()
                }
            }
        })
    }

    private val handlerConsent = Handler(Looper.getMainLooper())
    private val runnableDelayConsent = Runnable {
        if (!isFinishing) {
            initAdAndLoadAds()
        }
    }

    private fun showViewSelectThemeDefault() {
        checkDismissAdsOpenApp = true
        if (!mPrefs!!.getBoolean(Constant.CHECK_SELECT_THEME_DEFAULT, false)) {
            if (layoutOnboardingBinding == null) {
                Timber.d("duongcv showViewSelectThemeDefault");
                binding?.viewStub?.viewStub?.layoutResource = R.layout.layout_onboarding
                binding?.viewStub?.viewStub?.inflate()
                layoutOnboardingBinding = binding?.viewStub?.binding as LayoutOnboardingBinding?
                viewPagerOnboardAdapter = ViewPagerOnboardAdapter(this)
                layoutOnboardingBinding?.pageOnboarding?.adapter = viewPagerOnboardAdapter
                layoutOnboardingBinding?.pageOnboarding?.isUserInputEnabled = false
                layoutOnboardingBinding?.pageOnboarding?.offscreenPageLimit = 2
                layoutOnboardingBinding?.indicatorView?.setSliderColor(Color.GRAY, Color.WHITE)
                layoutOnboardingBinding?.indicatorView?.setSliderWidth(
                    DisplayUtils.dp2px(15f).toFloat()
                )
                layoutOnboardingBinding?.indicatorView?.setSliderHeight(
                    DisplayUtils.dp2px(5f).toFloat()
                )
                layoutOnboardingBinding?.indicatorView?.setSlideMode(IndicatorSlideMode.WORM)
                layoutOnboardingBinding?.indicatorView?.setIndicatorStyle(IndicatorStyle.ROUND_RECT)
                layoutOnboardingBinding?.indicatorView?.setupWithViewPager(layoutOnboardingBinding!!.pageOnboarding)
                eventListener()
                valueAnimator = ValueAnimator.ofFloat(0f, 1f)
                valueAnimator?.apply {
                    duration = 300
                    addUpdateListener { animation ->
                        layoutOnboardingBinding?.ctlOnboard?.alpha =
                            animation.animatedValue as Float
                    }
                    start()
                }
            }
        } else {
            goToMain()
        }
    }

    fun eventListener() {
        layoutOnboardingBinding?.pageOnboarding?.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageSelected(position: Int) {
                if (position == 2) {
                    layoutOnboardingBinding?.indicatorView?.visibility = View.GONE
                    layoutOnboardingBinding?.imgNext?.visibility = View.GONE
                } else {
                    layoutOnboardingBinding?.indicatorView?.visibility = View.VISIBLE
                    layoutOnboardingBinding?.imgNext?.visibility = View.VISIBLE
                }
            }
        })

        layoutOnboardingBinding?.imgNext?.setOnClickListener {
            if (checkDoubleClick()) {
                val position = layoutOnboardingBinding?.pageOnboarding?.currentItem
                if (position != null && position < 2) {
                    layoutOnboardingBinding?.pageOnboarding?.currentItem = position + 1
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageReceived(event: MessageEvent) {
        when (event.key) {
            Constant.EVENT_GOTO_MAIN -> {
                layoutOnboardingBinding?.ctlOnboard?.visibility = View.GONE
                goToMain()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}