package com.android.inputmethod.keyboard.viewGif

import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import com.tapbi.spark.yokey.App
import com.tapbi.spark.yokey.R
import com.tapbi.spark.yokey.util.DisplayUtils

class LoadingDrawable(val shape: Shape) : Drawable(), ValueAnimator.AnimatorUpdateListener {
    val paint = Paint().apply {
        color = Color.YELLOW
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    val animator: ValueAnimator = ValueAnimator.ofFloat(20.0f, 60f)
    var currentSize = 50f

    init {
        animator.addUpdateListener(this)
        animator.duration = 500
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = ValueAnimator.INFINITE
        animator.start()
    }

    override fun draw(p0: Canvas) {
        if (!animator.isRunning) {
        }
        when (shape) {
            Shape.Circle ->
                paint.color = App.instance.baseContext.resources.getColor(R.color.color_orange)//Color.parseColor("#FF555599")
            Shape.Rect ->
                paint.color = App.instance.baseContext.resources.getColor(R.color.setup_text_dark)
        }
        p0.drawRect(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat(), paint)
        val rect = Rect()
        paint.getTextBounds(
            App.instance.resources.getText(R.string.text_loading).toString(),
            0,
            App.instance.resources.getText(R.string.text_loading).toString().length,
            rect
        )
        paint.color = Color.WHITE
        paint.textSize = DisplayUtils.dp2px(14f).toFloat();
        p0.drawText(
            App.instance.resources.getText(R.string.text_loading).toString(),
            bounds.width().toFloat() / 2 - rect.width() / 2,
            bounds.height().toFloat() / 2 + rect.height() / 2,
            paint
        )
    }

    override fun setAlpha(p0: Int) {
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    override fun setColorFilter(p0: ColorFilter?) {
        paint.colorFilter = p0
    }

    enum class Shape {
        Rect,
        Circle
    }

    override fun onAnimationUpdate(p0: ValueAnimator) {
        try {
            currentSize = p0?.animatedValue as Float
            invalidateSelf()
        }catch (error : OutOfMemoryError){}
    }
}
