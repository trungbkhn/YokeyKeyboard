package com.tapbi.spark.yokey.ui.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import com.tapbi.spark.yokey.util.DisplayUtils

class CustomRadioButton : androidx.appcompat.widget.AppCompatTextView {
    private lateinit var paint: Paint
    private var isCheckChangeDraw: Boolean = false
    private val SIZE_1 : Int = DisplayUtils.dp2px(1.5f)
    private val SIZE_5 : Int = DisplayUtils.dp2px(5f)


    constructor(context: Context) : super(context) {
        initData()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initData()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initData()
    }

    private fun initData() {

    }

    fun setChangeDrawCircle(isCheckChangeDraw: Boolean) {
        this.isCheckChangeDraw = isCheckChangeDraw
        invalidate()
    }

    private fun drawCircle(canvas: Canvas) {
        paint = Paint(Paint(Paint.ANTI_ALIAS_FLAG)).apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
            if (isCheckChangeDraw) {
                color = Color.BLUE
            }
            strokeWidth = SIZE_1.toFloat()
            val radius = (measuredWidth / 2f ) - SIZE_5
            val x = measuredWidth.toFloat() / 2f
            val path = Path()
            path.addCircle(x, x, radius ,Path.Direction.CCW)
            canvas.drawPath(path, this)
            if (isCheckChangeDraw) {
                style = Paint.Style.FILL
                color = Color.BLUE
                val pathCircleFill = Path()
                pathCircleFill.addCircle(x, x,
                    radius - SIZE_1,
                    Path.Direction.CCW
                )
                canvas.drawPath(pathCircleFill, this)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawCircle(canvas)
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}