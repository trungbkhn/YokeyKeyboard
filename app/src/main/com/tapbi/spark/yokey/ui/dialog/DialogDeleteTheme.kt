package com.tapbi.spark.yokey.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import com.tapbi.spark.yokey.databinding.DialogDeleteThemeBinding
import com.tapbi.spark.yokey.data.local.entity.ThemeEntity

import com.tapbi.spark.yokey.util.Utils

class DialogDeleteTheme constructor(
    private val iListenerDelete: IListenerDelete,
    context: Context
) : Dialog(context) {
    private var binding: DialogDeleteThemeBinding? = null
    private var themeEntity: ThemeEntity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        initDialogDelete()
        super.onCreate(savedInstanceState)
    }

    fun getThemeEntity(themeEntity: ThemeEntity) {
        this.themeEntity = themeEntity
    }

    private fun initDialogDelete() {
        binding = DialogDeleteThemeBinding.inflate(LayoutInflater.from(context))
        setCancelable(true)
        window!!.requestFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding!!.root)
        this.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        Utils.setTextViewColor(context, binding!!.btnAgreeDelete, 4)
        binding!!.btnCancelDelete.setOnClickListener {
            iListenerDelete.notDelete()
        }
        binding!!.btnAgreeDelete.setOnClickListener {
            iListenerDelete.agreeDelete()
        }
    }

    interface IListenerDelete {
        fun notDelete()
        fun agreeDelete()
    }
}