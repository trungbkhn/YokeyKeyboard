package com.tapbi.spark.yokey.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.data.model.MessageEvent
import com.tapbi.spark.yokey.databinding.DialogPermissionPrivacyBinding
import com.tapbi.spark.yokey.util.CommonUtil
import com.tapbi.spark.yokey.util.Constant
import org.greenrobot.eventbus.EventBus

class DialogPermission(context: Context) : Dialog(context) {

    private val binding: DialogPermissionPrivacyBinding by lazy {
        DialogPermissionPrivacyBinding.inflate(
            LayoutInflater.from(context)
        )
    }
    private var iListenerPermission: IListenerPermission? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDialogPermission()
        val window = window
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            window.addFlags(WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = (CommonUtil.getScreenWidth() * 0.9).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setWindowAnimations(R.style.CustomAnimDialog)
        }
    }

    fun setListenerPermission(iListenerPermission: IListenerPermission) {
        this.iListenerPermission = iListenerPermission
    }

    private fun initDialogPermission() {
        setCancelable(true)
        setContentView(binding.root)
        val startText = context.resources.getString(R.string.zomj_keyboard)
        val centerText = context.resources.getString(R.string.never_collects_personal_data)
        val endText = context.resources.getString(R.string.such_as_password_credit_card_number_etc)
        binding.txtHowDo.text = CommonUtil.setBoldString(startText, centerText, endText)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.btnAccept.setOnClickListener {
            iListenerPermission?.accept()
        }
        binding.txtProtect.setOnClickListener {
            EventBus.getDefault().post(MessageEvent(Constant.EVENT_SHOW_POLICY))
            dismiss()

//            CommonUtil.policy(context)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
           iListenerPermission?.enabledView()
        }
        return super.onTouchEvent(event)
    }

    interface IListenerPermission {
        fun enabledView()

        fun accept()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        iListenerPermission?.enabledView()
       // Toast.makeText(context,"hello", Toast.LENGTH_LONG).show()
    }
}