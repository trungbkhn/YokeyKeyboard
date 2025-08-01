package com.tapbi.spark.yokey.ui.custom.view

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.google.android.ads.nativetemplates.CheckTouchableLayout

class ClickableFrameLayout: FrameLayout, CheckTouchableLayout {
    var isTouchedDown = false
    var isClicked = false

    constructor(context: Context?) : super(context!!)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {

    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {

        ev?.let {
            when(it.action){
                MotionEvent.ACTION_DOWN-> isTouchedDown = true
                MotionEvent.ACTION_UP->{
                    if (isTouchedDown) {
                        val bound = Rect()
                        getGlobalVisibleRect(bound)
                        isClicked = bound.contains(it.rawX.toInt(), it.rawY.toInt())
                        isTouchedDown=false
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun resetTouch() {
        isClicked = false
    }

    override fun checkClicked(): Boolean {
        return isClicked
    }
}