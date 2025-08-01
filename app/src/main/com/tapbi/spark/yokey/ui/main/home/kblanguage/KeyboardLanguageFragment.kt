package com.tapbi.spark.yokey.ui.main.home.kblanguage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ironman.trueads.common.Common
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.FragmentKeyboardLanguageBinding
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.data.local.LanguageEntity
import com.tapbi.spark.yokey.ui.adapter.KeyboardLanguageAdapter
import com.tapbi.spark.yokey.ui.base.BaseBindingFragment
import com.tapbi.spark.yokey.ui.custom.view.CustomSwitch
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import timber.log.Timber


class KeyboardLanguageFragment constructor() :
    BaseBindingFragment<FragmentKeyboardLanguageBinding, KeyboardLanguageViewModel>() {

    var keyboardLanguageAdapter: KeyboardLanguageAdapter? = null
    var languageEntities: ArrayList<LanguageEntity> = ArrayList<LanguageEntity>()
    var isUseSystemLanguage: Boolean = false
    var isResume = false
    override fun getViewModel(): Class<KeyboardLanguageViewModel> {
        return KeyboardLanguageViewModel::class.java
    }

    override val layoutId: Int
        get() = R.layout.fragment_keyboard_language

    override fun onCreatedView(view: View?, savedInstanceState: Bundle?) {
        isResume = true
        listener()
        processRemoveAds(isRemoveAds)
        initLanguageAdapter()
        viewModel.getAllKeyboardLanguage(requireContext(),isUseSystemLanguage)
        Timber.d("ducNQ onCreatedViewonBackPressed: " + App.instance.checkScreen);
//        if (!App.instance.checkScreen) {
//            Timber.d("ducNQ onCreatedViewssss: ");
//            requireActivity().onBackPressed()
//        }
    }

    private fun initLanguageAdapter() {
        isUseSystemLanguage =
            App.instance.mPrefs?.getBoolean(Constant.IS_USE_SYSTEM_LANGUAGE, true) ?: true
        /* if(isUseSystemLanguage){
             binding.cv.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_DFDFDF))
         }else{
             binding.cv.setCardBackgroundColor(Color.WHITE)
         }*/
        binding.swSystemLanguage.setCheck(isUseSystemLanguage)
        keyboardLanguageAdapter =
            KeyboardLanguageAdapter(requireContext(), languageEntities, App.instance.mPrefs)
        binding.rcvLanguage.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rcvLanguage.adapter = keyboardLanguageAdapter
        keyboardLanguageAdapter!!.setUseSystem(isUseSystemLanguage)
        keyboardLanguageAdapter!!.setOnItemLanguageClickListener(object :
            KeyboardLanguageAdapter.OnItemLanguageClickListener {
            override fun onItemLanguageClick(position: Int, enable: Boolean, isUseSystem: Boolean) {
                if (languageEntities.size > position) {
                    val languageEntity = languageEntities.get(position);
                    languageEntity.isEnabled = enable
                    languageEntities.set(position, languageEntity)
                    if (binding.swSystemLanguage.check) {
                        binding.swSystemLanguage.setCheck(false)
                    }
//                    if (enable) {
//                        App.instance.keyboardLanguageRepository?.insertKeyboardLanguageDb(
//                            languageEntity
//                        )
//                    } else {
//                        App.instance.keyboardLanguageRepository?.deleteKeyboardLanguageDbByLocale(
//                            languageEntity
//                        )
//                    }
                }
                if (checkZeroListEnable()) {
                    CommonUtil.setEnableDefaultSystem(languageEntities)
                    binding.swSystemLanguage.check = true
                    keyboardLanguageAdapter!!.languageEntities = languageEntities
                    keyboardLanguageAdapter!!.setUseSystem(true)
                }
                if (isUseSystem) {
                    binding.swSystemLanguage.check = false
                    App.instance.mPrefs?.edit()?.putBoolean(Constant.IS_USE_SYSTEM_LANGUAGE, false)
                        ?.apply()
                    if (keyboardLanguageAdapter != null) {
                        keyboardLanguageAdapter!!.setUseSystem(false)
                    }
                }
                // viewModel.updateLanguage(enable, languageEntities[position].id)
            }
        })

    }

    private fun checkZeroListEnable(): Boolean {
        if (languageEntities.isEmpty()) {
            return true
        }
        for (languageEntity in languageEntities) {
            if (languageEntity.isEnabled) {
                return false
            }
        }
        return true
    }

    override fun onPause() {
        isResume = false
        App.instance.keyboardLanguageRepository?.insertLanguage(languageEntities)?.subscribe()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        isResume = true
    }

    override fun onPermissionGranted() {

    }

    var listDemo = mutableListOf<LanguageEntity>()
    fun listener() {
        viewModel.listLanguageEntityLiveDataOther.observe(
            this
        ) {
            if (isResume) {
                languageEntities = it
                listDemo.clear()
                listDemo.addAll(it)
                keyboardLanguageAdapter?.languageEntities = languageEntities
                keyboardLanguageAdapter?.notifyDataSetChanged()
                Log.d("duongcv", "onChanged: isUseSystemLanguage " + isUseSystemLanguage)
            }
        }

        binding.swSystemLanguage.setOnChangeCheckListener(object :
            CustomSwitch.OnChangeCheckListener {
            override fun isCheck(customSwitch: CustomSwitch?, isCheck: Boolean) {
                App.instance.mPrefs?.edit()?.putBoolean(Constant.IS_USE_SYSTEM_LANGUAGE, isCheck)?.apply()
                if (isCheck) {
                    CommonUtil.setEnableDefaultSystem(languageEntities)
                    // todo: set locale current
                    if (!languageEntities.isEmpty()) {
                        App.instance.mPrefs?.edit()?.putString(
                            Constant.LOCALE_CURRENT_LANGUAGE,
                            languageEntities.get(0).locale
                        )?.apply();
                    }
                }
                if (keyboardLanguageAdapter != null) {
                    keyboardLanguageAdapter!!.setUseSystem(isCheck)
                }
                /*  if(isCheck){
                      binding.cv.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_DFDFDF))
                  }else{
                      binding.cv.setCardBackgroundColor(Color.WHITE)
                  }*/
//                viewModel.getAllKeyboardLanguage(requireContext(),isCheck)
            }
        })

        binding.imgBack.setOnClickListener {
            App.instance.checkScreen = false
            requireActivity().onBackPressed()
        }
    }

    override fun processRemoveAds(isRemoveAds: Boolean) {
        super.processRemoveAds(isRemoveAds)
        val mapId = Common.getMapIdAdmobApplovin(requireActivity(),R.array.admob_native_id_language,R.array.applovin_native_id_language)
        showAdsNative(binding.frAds,mapId,null)
    }

}
