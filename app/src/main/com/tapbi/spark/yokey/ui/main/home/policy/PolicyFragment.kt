package com.tapbi.spark.yokey.ui.main.home.policy

import android.os.Bundle
import android.view.View
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentPolicyBinding
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment

class PolicyFragment : BaseBindingFragment<FragmentPolicyBinding, PolicyViewModel>() {
    override fun getViewModel(): Class<PolicyViewModel> {
        return PolicyViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_policy

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        //try {
            binding.webViewPolicy.loadUrl(requireContext().getString(R.string.policy_url_rgb))
      //  } catch (e: SecurityException) {
     //       e.printStackTrace()
       // }
    }

    override fun onPermissionGranted() {}
}