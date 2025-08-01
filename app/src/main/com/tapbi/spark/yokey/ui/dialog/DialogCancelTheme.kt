package com.tapbi.spark.yokey.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import com.tapbi.spark.yokey.databinding.DialogCancelThemeBinding
import com.tapbi.spark.yokey.util.Utils

class DialogCancelTheme(context: Context, private val iListenerDialog: IListenerDialog) :
    Dialog(context) {
    private var binding:DialogCancelThemeBinding?=null
   // private val btnCanCel: TextView by lazy { findViewById(R.id.btn_not_cancel) }
   // private val btnYes: TextView by lazy { findViewById(R.id.btn_agree_cancel) }
   // private val btnImgCancel: ImageView by lazy { findViewById(R.id.img_cancel) }
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = DialogCancelThemeBinding.inflate(LayoutInflater.from(context))
        initDialog()
        super.onCreate(savedInstanceState)
    }

    private fun initDialog() {
        setCancelable(true)
        setContentView(binding!!.root)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        Utils.setTextViewColor(context,  binding!!.btnNotCancel, 2)
        Utils.setTextViewColor(context,   binding!!.btnAgreeCancel, 4)
        binding?.btnNotCancel?.setOnClickListener {
            iListenerDialog.notCancel()
        }
        binding?.btnAgreeCancel?.setOnClickListener {
            iListenerDialog.okCancel()
        }
        binding?.imgCancel?.setOnClickListener {
            dismiss()
        }
    }

    interface IListenerDialog {
        fun notCancel()
        fun okCancel()
    }
}