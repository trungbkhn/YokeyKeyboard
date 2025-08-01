package com.tapbi.spark.yokey.ui.custom.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet

class CustomBgEmoji : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context?) : super(context!!){

    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs){

    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}