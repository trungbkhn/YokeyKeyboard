package com.tapbi.spark.yokey.ui.custom.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.util.DisplayUtils

class CusTomButtonText : androidx.appcompat.widget.AppCompatTextView {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var color1 = Color.BLUE
    private var color2 = Color.GREEN
    private var radiusC = DisplayUtils.dp2px(100F)
    private var strokes = DisplayUtils.dp2px(0F)
    private var arrColor = intArrayOf()
    private var checkShowGradient = false

    constructor(context: Context?) : super(context!!) {
      //  setBackgroundColor(Color.TRANSPARENT)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        initData(attrs)
      //  setBackgroundColor(Color.TRANSPARENT)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!,
        attrs,
        defStyleAttr
    ) {
        //setBackgroundColor(Color.TRANSPARENT)
        initData(attrs)
    }

    override fun onDraw(canvas: Canvas) {
        drawGradient(canvas)
        super.onDraw(canvas)
    }

    private fun initData(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.CusTomButtonText) {
            color1 = this.getColor(R.styleable.CusTomButtonText_colorStart, Color.GREEN)
            color2 = this.getColor(R.styleable.CusTomButtonText_colorEnd, Color.BLUE)
            checkShowGradient = this.getBoolean(R.styleable.CusTomButtonText_icShowGradient, false)
            strokes = this.getDimensionPixelOffset(R.styleable.CusTomButtonText_stroke,
                DisplayUtils.dp2px(1F))
        }
        arrColor = intArrayOf(color1, color2)
    }

    private fun drawGradient(canvas: Canvas?) {
        paint.apply {
            strokeWidth = strokes.toFloat()
            if (checkShowGradient) {
                style = Paint.Style.FILL
                shader = LinearGradient(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    arrColor,
                    null,
                    Shader.TileMode.CLAMP
                )
            } else {
                style = Paint.Style.STROKE
                shader = null
                color = Color.parseColor("#BDBEDB")
            }
        }
        canvas!!.drawRoundRect(
            strokes.toFloat(),
            strokes.toFloat(),
            width.toFloat()-strokes,
            height.toFloat()-strokes,
            radiusC.toFloat(),
            radiusC.toFloat(),
            paint
        )
    }

    fun checkShowGradient(checkShowGradient: Boolean) {
        this.checkShowGradient = checkShowGradient
        invalidate()
    }
}