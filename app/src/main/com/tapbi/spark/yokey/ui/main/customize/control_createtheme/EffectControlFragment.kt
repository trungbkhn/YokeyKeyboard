package com.tapbi.spark.yokey.ui.main.customize.control_createtheme

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentEffectControlBinding
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.model.Effect
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.ui.adapter.EffectAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.main.customize.CreateThemeViewModel
import com.tapbi.spark.yokey.util.Constant
import com.tapbi.spark.yokey.util.Constant.EFFECT_FOCUS_KILLAPP
import org.greenrobot.eventbus.EventBus

class EffectControlFragment : BaseBindingFragment<FragmentEffectControlBinding, CreateThemeViewModel>() {

    private lateinit var listEffect : ArrayList<Effect>
    private lateinit var effectAdapter: EffectAdapter

    companion object {
        fun newInstance(): EffectControlFragment {
            val args = Bundle()
            val fragment = EffectControlFragment()
            fragment.arguments = args
            return fragment
        }
    }
    override fun getViewModel(): Class<CreateThemeViewModel> {
        return CreateThemeViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_effect_control

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {

    }

    override fun onPermissionGranted() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRclEffect()
        if(savedInstanceState!=null){
            if(effectAdapter!=null){
                App.instance.effect = savedInstanceState.getString(EFFECT_FOCUS_KILLAPP)!!
                effectAdapter.apply { changeFocusEffect(savedInstanceState.getString(EFFECT_FOCUS_KILLAPP)!!) }
            }
        }
    }

    private fun initRclEffect() {
        listEffect  = ArrayList()
        effectAdapter = EffectAdapter(listEffect, requireContext(), Constant.ID_NONE, object : EffectAdapter.ListenerChangeEffect{
            override fun changeEffect(effect: String) {
                val bundle = Bundle()
                bundle.putString(Constant.DATA_CHANGE_EFFECT_CUSTOMZIE,effect)
                App.instance.effect = effect
                EventBus.getDefault().post(MessageEvent(Constant.ACTION_CHANGE_EFFECT_CUSTOMZIE, bundle))
            }

        })
        val gridLayoutManager = GridLayoutManager(requireContext(), 5)
        binding.rclKeyEffect.layoutManager = gridLayoutManager
        binding.rclKeyEffect.adapter = effectAdapter
        viewModel.liveDataListEffect.observe(viewLifecycleOwner,
            { listEffect -> effectAdapter.changeListEffect(listEffect!!) })
        viewModel.loadListEffect()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EFFECT_FOCUS_KILLAPP, App.instance.effect)
        super.onSaveInstanceState(outState)
    }
}