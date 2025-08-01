package com.tapbi.spark.yokey.ui.main.home.setting.control

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.util.Log
import com.tapbi.spark.yokey.R
import com.android.inputmethod.latin.settings.CustomInputStyleSettingsFragment
import com.tapbi.spark.yokey.common.Constant
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.util.LocaleUtils
import org.greenrobot.eventbus.EventBus

class CustomInputMethodSettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleUtils.applyLocale(this)
        Log.d("duongcv", "onCreate: CustomInputMethodSettingsActivity")
        setContentView(R.layout.custom_input_method_activity)
        loadFragment(CustomInputStyleSettingsFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        val fm = fragmentManager
        val fragmentTransaction = fm.beginTransaction()
        fragmentTransaction.replace(R.id.custom_fragment_input_method, fragment)
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        EventBus.getDefault().postSticky(MessageEvent(Constant.NOT_BACK_MH_MY_THEME))
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        Log.d("duongcv" ,"onResume: CustomInputMethodSettingsActivity")
    }
}
