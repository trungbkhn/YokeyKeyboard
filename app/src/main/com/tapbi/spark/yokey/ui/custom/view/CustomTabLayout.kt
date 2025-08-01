package com.tapbi.spark.yokey.ui.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.google.android.material.tabs.TabLayout
import com.tapbi.spark.yokey.util.DisplayUtils

class CustomTabLayout : TabLayout {
    private var paint: Paint? = null
    private val SIZE_4 = DisplayUtils.dp2px(4f)

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        paint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(SIZE_4.toFloat(), 0f, 6f, Color.parseColor("#DEDEDE"))
        }
        setLayerType(LAYER_TYPE_SOFTWARE, paint)
    }

    private fun drawShadow(canvas: Canvas?) {
        canvas!!.drawRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat() - 20f,
            paint!!
        )

    }

    override fun onDraw(canvas: Canvas) {
        drawShadow(canvas)
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}