package com.tapbi.spark.yokey.ui.main.premium.sub

import android.os.Bundle
import android.view.View
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentInformationPremiumBinding
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.util.Constant

class InformationPremiumFragment :
    BaseBindingFragment<FragmentInformationPremiumBinding, InformationPremiumViewModel>() {
    override fun getViewModel(): Class<InformationPremiumViewModel> {
        return InformationPremiumViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_information_premium

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
    }

    override fun onPermissionGranted() {
    }

    companion object{
        fun newInstance(title : Int , decription : Int) : InformationPremiumFragment {
            val bundle = Bundle()
            bundle.putInt(Constant.TITLE_INFOR_PREMIUM,title)
            bundle.putInt(Constant.DES_INFOR_PREMIUM,decription)
            val fragment = InformationPremiumFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            binding.tvPremiumDespDownload.text =
                requireContext().resources.getString(requireArguments().getInt(Constant.TITLE_INFOR_PREMIUM))

            binding.tvDesPremiumDespDownload.text =
                requireContext().resources.getString(requireArguments().getInt(Constant.DES_INFOR_PREMIUM))

        }
    }


}