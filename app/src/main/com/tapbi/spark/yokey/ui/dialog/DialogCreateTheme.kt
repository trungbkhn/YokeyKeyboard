package com.tapbi.spark.yokey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.databinding.DialogCreateThemeBinding
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.util.CommonUtil
import kotlin.math.roundToInt

class DialogCreateTheme(context: Activity, private val iListenerCreate: IListenerCreate) :
        Dialog(context) {
    private var activity = context
    private var binding: DialogCreateThemeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        initDialog()
        super.onCreate(savedInstanceState)
    }

    private fun initDialog() {
        setCancelable(true)
        binding = DialogCreateThemeBinding.inflate(LayoutInflater.from(context))
        setContentView(binding!!.root)
        var width: Int = App.instance.resources.displayMetrics.widthPixels
        try {
            width = (width * 0.9f).roundToInt()
        } catch (e: ClassCastException) {
            width -= 100
        }
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        binding!!.btnCancelCreate.setOnClickListener {
            iListenerCreate.cancelCreate()
        }
        binding!!.btnAgreeCreate.setOnClickListener {
            if (binding!!.edtEnterName.text == null || binding!!.edtEnterName.text.trim().isEmpty()) {
                CommonUtil.customToast(context, context.resources.getString(R.string.enter_name_keyboard))
            } else {
                    iListenerCreate.agreeCreate(binding!!.edtEnterName.text.toString().trim())
            }
        }

        setOnShowListener {
            if (isShowing) {
                binding!!.edtEnterName.setText("")
//                binding?.edtEnterName?.requestFocus()
                iListenerCreate.showDialog()
            }
        }
    }


    interface IListenerCreate {
        fun cancelCreate()
        fun agreeCreate(nameKeyboard: String)
        fun showDialog()
    }

    fun changeRequest(request: Boolean) {
        if (request) {

            binding?.edtEnterName?.setText("");
            binding?.edtEnterName?.requestFocus()
            val inputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(binding?.edtEnterName, InputMethodManager.SHOW_IMPLICIT)
        } else binding?.edtEnterName?.clearFocus()
    }




}