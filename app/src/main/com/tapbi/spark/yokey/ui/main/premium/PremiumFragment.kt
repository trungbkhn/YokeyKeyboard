package com.tapbi.spark.yokey.ui.main.premium

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.ProductDetails
import com.ironman.spark.billing.BillingManager
import com.ironman.spark.billing.BillingManager.findBillingModel
import com.ironman.spark.billing.BillingManager.mLiveDataBillingModels
import com.ironman.spark.billing.BillingModel
import com.ironman.spark.billing.PremiumLiveData
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentPremiumBinding
import com.ironman.trueads.multiads.MultiAdsControl
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.common.Constant
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.adapter.ViewPagerInforPremiumAdapter
import com.tapbi.spark.yokey.ui.base.BaseDialogFragment
import com.tapbi.spark.yokey.ui.main.MainActivity
import com.tapbi.spark.yokey.ui.main.MainViewModel
import com.tapbi.spark.yokey.util.ClickUtil
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant.CHECK_DOUBLE_CLICK
import org.greenrobot.eventbus.EventBus

class PremiumFragment : BaseDialogFragment(),
    View.OnClickListener {
    private val skuSubs: Array<String> by lazy { resources.getStringArray(R.array.subscription_list) }
    private var billingModel : BillingModel? = null
    private lateinit var mainViewModel: MainViewModel
    private lateinit var viewPagerInforPremiumAdapter: ViewPagerInforPremiumAdapter
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var binding: FragmentPremiumBinding
    private val runnable: Runnable by lazy {
        object : Runnable {
            override fun run() {
                val current = binding.vpShowInfo.currentItem
                binding.vpShowInfo.setCurrentItem(current + 1, true)
                handler.postDelayed(this, 2500)
            }
        }
    }
    private var isRemoveAds=false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPremiumBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initViewPagerShow() {
        viewPagerInforPremiumAdapter = ViewPagerInforPremiumAdapter(this)
        binding.vpShowInfo.adapter = viewPagerInforPremiumAdapter
        binding.vpShowInfo.offscreenPageLimit = 1
        binding.vpShowInfo.currentItem = 0
        binding.vpShowInfo.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        MultiAdsControl.enableShowAdsOpenForeground(App.instance,false)
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        MultiAdsControl.enableShowAdsOpenForeground(App.instance,false)
        initViewPagerShow()
        setUpView()
        listener()
        eventBack()
    }

    private fun eventBack() {
//        dialog?.setOnKeyListener { _, keyCode, event ->
//            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                if (event.action != KeyEvent.ACTION_DOWN) {
//                    dismiss()
//                    EventBus.getDefault().post(MessageEvent(CHECK_DOUBLE_CLICK))
//                }
//                return@setOnKeyListener true
//            } else return@setOnKeyListener false
//        }
    }

    private fun listener() {
        PremiumLiveData.get().observe(
            viewLifecycleOwner
        ) { isRemoveAds ->
            binding.btnContinue.visibility = if (isRemoveAds) View.GONE else View.VISIBLE
            this.isRemoveAds=isRemoveAds
            if (isRemoveAds) {
                dismiss()
            }
        }

        App.instance!!.billingManager!!.mLiveDataSkuInApp.observe(viewLifecycleOwner) { productDetails ->
            if (productDetails != null && App.instance!!.billingManager!!.mLiveDataSkuSubs.value != null) {
                setUpView()
            }
        }
        App.instance!!.billingManager!!.mLiveDataSkuSubs.observe(viewLifecycleOwner) { productDetails ->
            if (productDetails != null && App.instance!!.billingManager!!.mLiveDataSkuSubs.value != null) {
                setUpView()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        EventBus.getDefault().post(MessageEvent(CHECK_DOUBLE_CLICK))
        super.onDismiss(dialog)
    }

    @SuppressLint("SetTextI18n")
    private fun setUpView() {
        if(App.instance!!.billingManager!!.mLiveDataSkuInApp.value==null|| App.instance!!.billingManager!!.mLiveDataSkuSubs.value==null){
            binding.viewError.visibility=View.VISIBLE
        }else{
            binding.viewError.visibility=View.GONE
        }
        binding.imgExitPremium.isEnabled = true
        binding.moneyUses.setOnClickListener(this)
        binding.moneyMonth.setOnClickListener(this)
        binding.moneyYear.setOnClickListener(this)
        binding.imgExitPremium.setOnClickListener(this)
        binding.btnContinue.setOnClickListener(this)
        binding.tvCheckPro.setOnClickListener(this)
        binding.tvPrivacy.setOnClickListener(this)
        binding.btnRetry.setOnClickListener(this)
        binding.txtSale.text = getString(R.string.best_offer)+ Constant.SALE_50

        billingModel = findBillingModel(getString(R.string.purchase_pro_id_permanently), ProductType.INAPP)
        mLiveDataBillingModels.observe(this){ billingModels->
            billingModels.forEach {
                update_view_sub(it)
            }
        }

        setCheckAction(
            moneyUses = false,
            moneyMonth = false,
            moneyYear = true,
            billingModel = findBillingModel(
                skuSubs[1], BillingClient.ProductType.SUBS
            )
        )

    }

    private fun setCheckAction(
        moneyUses: Boolean,
        moneyMonth: Boolean,
        moneyYear: Boolean,
        billingModel: BillingModel?
    ) {
        this.billingModel = billingModel
        binding.moneyUses.isCheckActionUser(moneyUses)
        binding.moneyYear.isCheckActionUser(moneyYear)
        binding.moneyMonth.isCheckActionUser(moneyMonth)
    }

    fun update_view_sub(model: BillingModel){
        val prices= BillingManager.getPriceForDisplay(requireContext(),
            com.ironman.spark.billing.R.string.free_trial_then,model)
        var string_price = ""
        prices.forEach {
            string_price= string_price.plus(" ").plus(it)

        }
        when(model.productId){
            skuSubs[0]-> binding.moneyMonth.nameText =
                string_price
            skuSubs[1]-> binding.moneyYear.nameText =
                string_price

            requireContext().getString(R.string.purchase_pro_id_permanently)-> {
                var price = getString(R.string.premium_subscription_permanently)
                price = price.replace("%1${'$'}s", model.pricesOneTime.toString())
                binding.moneyUses.nameText = price
            }
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.money_uses -> {
                setCheckAction(
                    moneyUses = true,
                    moneyMonth = false,
                    moneyYear = false,
                    billingModel = findBillingModel(getString(R.string.purchase_pro_id_permanently) , BillingClient.ProductType.INAPP)
                )
            }

            R.id.money_year -> {
                setCheckAction(
                    moneyUses = false,
                    moneyMonth = false,
                    moneyYear = true,
                    billingModel = findBillingModel(
                        skuSubs[1], BillingClient.ProductType.SUBS
                    )
                )
            }

            R.id.money_month -> {
                setCheckAction(
                    moneyUses = false,
                    moneyMonth = true,
                    moneyYear = false,
                    billingModel = findBillingModel(
                        skuSubs[0], BillingClient.ProductType.SUBS
                    )
                )
            }

            R.id.img_exit_premium -> {
                if(ClickUtil.checkTime()) {
                    binding.imgExitPremium.isEnabled = false
                    dismiss()
                }
//                EventBus.getDefault().post(MessageEvent(CHECK_DOUBLE_CLICK))
                // iListenPre?.checkDoubleClick()
            }

            R.id.tv_check_pro -> {
                (activity as MainActivity).queryCheckPro()
            }

            R.id.btn_continue -> {
                if (billingModel != null) {
                    (requireActivity() as MainActivity).purchaseBilling(billingModel)
                } else {
                    CommonUtil.customToast(
                        requireContext(),
                        getString(R.string.premium_billing_error)
                    )
                }
            }

            R.id.tv_privacy -> {
                (activity as MainActivity).changeStartScreen(R.id.policyFragment,null)
            }
            R.id.btn_retry -> {
                if (App.instance!!.billingManager!!.checkBillingAvailable()) {
                    App.instance!!.billingManager!!.refreshPurchase(true, true ,activity as MainActivity)
                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.error_load_data_billing),
                        Toast.LENGTH_SHORT
                    ).show()
                    App.instance!!.billingManager!!.retryBillingServiceIfNeeded(activity as MainActivity)

                }
            }
        }
    }

    private fun findSkuPurchase(sku: String, type: String): ProductDetails? {
        var mproductDetailsList: List<ProductDetails>? = null
        mproductDetailsList = App.instance!!.billingManager!!.mLiveDataSkuSubs.value
        if (type == BillingClient.ProductType.INAPP) {
            mproductDetailsList =
                App.instance!!.billingManager!!.mLiveDataSkuInApp.value
        }
        if (mproductDetailsList != null) {
            for (productDetails in mproductDetailsList) {
                if (productDetails.productId == sku) {
                    return productDetails
                }
            }
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 2500)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if(!isRemoveAds){
            MultiAdsControl.enableShowAdsOpenForeground(App.instance,true)
        }

    }

}