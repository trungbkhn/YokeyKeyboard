package com.tapbi.spark.yokey.ui.splash

import android.annotation.SuppressLint
import android.os.Bundle
import com.applovin.mediation.MaxAd
import com.ironman.trueads.multiads.MultiAdsControl
import com.ironman.trueads.multiads.ShowOpenAdsListener
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.ActivitySplashBinding
import com.tapbi.spark.yokey.ui.base.BaseBindingActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseBindingActivity<ActivitySplashBinding, SplashViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_splash

    override fun getViewModel(): Class<SplashViewModel> {
        return SplashViewModel::class.java
    }

    override fun setupView(savedInstanceState: Bundle?) {
    }

    override fun setupData() {
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

                }

                override fun onShowAdsOpenAppDismissed() {

                }

                override fun onAdsOpenLoadedButNotShow() {

                }
            })
    }
}
