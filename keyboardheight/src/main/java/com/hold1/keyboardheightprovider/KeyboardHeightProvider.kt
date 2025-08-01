package com.hold1.keyboardheightprovider

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import timber.log.Timber

/**
 * Created by Cristian Holdunu on 11/01/2019.
 */
class KeyboardHeightProvider(private val activity: Activity,private var parentView: View? = null) : PopupWindow(activity) {

    private var resizableView: View
    private var lastKeyboardHeight = -1
    private var  handler = Handler()
    private var keyboardListeners = ArrayList<KeyboardListener>()
    private var keyboardHeight =0
    private var orientation : Int = 0
    private val runnable = Runnable {
        notifyKeyboardHeightChanged(keyboardHeight, orientation)
    }

    init {
        contentView = View.inflate(activity, R.layout.keyboard_popup, null)
        resizableView = contentView.findViewById(R.id.keyResizeContainer)
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED

        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT
    }

    fun onResume() {
        if(parentView==null) {
            parentView = activity.findViewById(android.R.id.content)
        }
        parentView?.postDelayed({
            resizableView.viewTreeObserver.addOnGlobalLayoutListener(getGlobalLayoutListener())
            if (!isShowing && parentView?.windowToken != null) {
                activity.apply {
                    if (!isFinishing || !isDestroyed) {
                        try {
                            dismiss()
                            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0)
                        } catch (ex: Exception) {
                            ex.printStackTrace()

                        }

                    }
                }
            }
        }, 0)
    }

    fun onPause() {
        resizableView.viewTreeObserver.removeOnGlobalLayoutListener(getGlobalLayoutListener())
        try{
            dismiss()
        }catch ( ex: Exception){
            ex.printStackTrace()
        }

    }
    fun onDestroy() {
        try{
            dismiss()
        }catch ( ex: Exception){
            ex.printStackTrace()
        }
    }
    private fun getGlobalLayoutListener() = ViewTreeObserver.OnGlobalLayoutListener {
        computeKeyboardState()
    }

    private fun computeKeyboardState() {
        val screenSize = Point()
        activity.windowManager.defaultDisplay.getSize(screenSize)
        val rect = Rect()
        resizableView.getWindowVisibleDisplayFrame(rect)
        orientation = activity.resources.configuration.orientation

        keyboardHeight = screenSize.y + topCutoutHeight - rect.bottom
        KeyboardInfo.keyboardState = if (keyboardHeight > 0) KeyboardInfo.STATE_OPENED else KeyboardInfo.STATE_CLOSED
        if (keyboardHeight > 0) {
            KeyboardInfo.keyboardHeight = keyboardHeight
        }
//        if (keyboardHeight != lastKeyboardHeight)
//            notifyKeyboardHeightChanged(keyboardHeight, orientation)
        if (keyboardHeight != lastKeyboardHeight && rect.bottom>0){
            if(keyboardHeight>0){
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable,200)
            }
            else{
                notifyKeyboardHeightChanged(keyboardHeight, orientation)
            }
        }
        lastKeyboardHeight = keyboardHeight
    }

    private val topCutoutHeight: Int
        get() {
            val decorView = activity.window.decorView ?: return 0
            var cutOffHeight = 0
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                decorView.rootWindowInsets?.let { windowInsets ->
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        val displayCutout = windowInsets.displayCutout
                        if (displayCutout != null) {
                            val list = displayCutout.boundingRects
                            for (rect in list) {
                                if (rect.top == 0) {
                                    cutOffHeight += rect.bottom - rect.top
                                }
                            }
                        }
                    }
                }
            }
            return cutOffHeight
        }

    fun addKeyboardListener(listener: KeyboardListener) {
        keyboardListeners.add(listener)
    }

    fun removeKeyboardListener(listener: KeyboardListener) {
        keyboardListeners.remove(listener)
    }

    private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
        keyboardListeners.forEach {
            it.onHeightChanged(height)
        }
    }

    fun hideKeyboard() {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(parentView?.windowToken, 0)
    }

    interface KeyboardListener {
        fun onHeightChanged(height: Int)
    }
}