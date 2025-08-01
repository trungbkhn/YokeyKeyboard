package com.android.inputmethod.keyboard.effect

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import com.android.inputmethod.keyboard.Key
import com.android.inputmethod.keyboard.internal.KeyDrawParams
import com.android.inputmethod.keyboard.internal.KeyPreviewDrawParams
import com.android.inputmethod.keyboard.internal.KeyPreviewView
import com.android.inputmethod.keyboard.internal.KeyboardIconsSet
import com.android.inputmethod.latin.common.CoordinateUtils
import com.android.inputmethod.latin.utils.ViewLayoutUtils
import timber.log.Timber
import java.util.ArrayDeque
import java.util.HashMap

class EffectPreviewChoreographer() {
    private val mFreeKeyPreviewViews: ArrayDeque<KeyEffectView?> = ArrayDeque<KeyEffectView?>()

    // Map from {@link Key} to {@link KeyPreviewView} that is currently being displayed as key
    // preview.
    private val mShowingKeyPreviewViews: HashMap<Key, KeyEffectView?> =
        HashMap<Key, KeyEffectView?>()

    private var mParams: KeyPreviewDrawParams? = null
    private var context: Context? = null

    private var drawableEffect: Drawable? = null

    constructor(params: KeyPreviewDrawParams?) : this() {
        mParams = params
    }


    fun getKeyPreviewView(key: Key, placerView: ViewGroup): KeyEffectView? {
        var keyPreviewView: KeyEffectView? = mShowingKeyPreviewViews.remove(key)
        val context = placerView.context
        this.context = context

//        if (keyPreviewView != null && AppConstant.KEY_ID_EFFECT_POPUP_CHANGE.contains(idEffect)) {
//            Timber.e("keyPreviewView 1");
//            return keyPreviewView;
//        }
//        keyPreviewView = mFreeKeyPreviewViews.poll();
//        if (keyPreviewView != null && AppConstant.KEY_ID_EFFECT_POPUP_CHANGE.contains(idEffect)) {
//            Timber.e("keyPreviewView 2");
//            return keyPreviewView;
//        }

        //ColorKeyboard Convert width height popup preview from DP to Pixel for device
        // Todo: Show preview key when press
        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels
        keyPreviewView =
            KeyEffectView(context, null /* attrs */, width * 0.03, width * 0.03, drawableEffect)
        if(placerView!=null&&placerView.parent!=null){
            placerView.removeView(keyPreviewView)
        }
        placerView.addView(keyPreviewView, ViewLayoutUtils.newLayoutParam(placerView, 0, 0))
        return keyPreviewView
    }

    fun dismissKeyPreview(key: Key?, withAnimation: Boolean) {
        if (key == null) {
            return
        }
        val keyPreviewView: KeyEffectView = mShowingKeyPreviewViews[key] ?: return
        val tag: Any = keyPreviewView.getTag()
        if (withAnimation) {
            if (tag is KeyPreviewAnimators) {
                tag.startDismiss()
                return
            }
        }
        // Dismiss preview without animation.
        mShowingKeyPreviewViews.remove(key)
        if (tag is Animator) {
            tag.cancel()
        }
        keyPreviewView.setTag(null)
        keyPreviewView.setVisibility(View.INVISIBLE)
        mFreeKeyPreviewViews.add(keyPreviewView)
    }

    fun placeAndShowKeyPreview(
        key: Key, iconsSet: KeyboardIconsSet,
        drawParams: KeyDrawParams, keyboardViewWidth: Int, keyboardOrigin: IntArray,
        placerView: ViewGroup, withAnimation: Boolean, drawableEffect: Drawable?
    ) {
        this.drawableEffect = drawableEffect
        val keyPreviewView: KeyEffectView? = getKeyPreviewView(key, placerView)
        placeKeyPreview(
            key,
            keyPreviewView,
            iconsSet,
            drawParams,
            keyboardViewWidth,
            keyboardOrigin
        )
        Timber.e("hachung showKeyPreview: ${key.label}")
        showKeyPreview(key, keyPreviewView, withAnimation)
    }

    private fun placeKeyPreview(
        key: Key, keyPreviewView: KeyEffectView?,
        iconsSet: KeyboardIconsSet, drawParams: KeyDrawParams,
        keyboardViewWidth: Int, originCoords: IntArray
    ) {
        mParams!!.setGeometry(keyPreviewView)
        val previewWidth: Int = keyPreviewView!!.getMeasuredWidth()
        val previewHeight: Int = keyPreviewView.getMeasuredHeight() //mParams.mPreviewHeight;
        val keyDrawWidth = key.drawWidth
        // The key preview is horizontally aligned with the center of the visible part of the
        // parent key. If it doesn't fit in this {@link KeyboardView}, it is moved inward to fit and
        // the left/right background is used if such background is specified.
        val keyPreviewPosition: Int
        var previewX: Int = (key.drawX - (previewWidth - keyDrawWidth) / 2
                + CoordinateUtils.x(originCoords))
        if (previewX < 0) {
            previewX = 0
            keyPreviewPosition = KeyPreviewView.POSITION_LEFT
        } else if (previewX > keyboardViewWidth - previewWidth) {
            previewX = keyboardViewWidth - previewWidth
            keyPreviewPosition = KeyPreviewView.POSITION_RIGHT
        } else {
            keyPreviewPosition = KeyPreviewView.POSITION_MIDDLE
        }
        val hasMoreKeys = key.moreKeys != null
        // The key preview is placed vertically above the top edge of the parent key with an
        // arbitrary offset.
        val previewY: Int = (key.y - previewHeight
                + CoordinateUtils.y(originCoords))
        ViewLayoutUtils.placeViewAt(
            keyPreviewView, previewX, previewY, previewWidth, previewHeight
        )
        keyPreviewView!!.setPivotX(previewWidth.toFloat())
        keyPreviewView.setPivotY(previewHeight.toFloat())
    }

    fun showKeyPreview(
        key: Key, keyPreviewView: KeyEffectView?,
        withAnimation: Boolean
    ) {
        if (!withAnimation) {
            keyPreviewView!!.setVisibility(View.VISIBLE)
            mShowingKeyPreviewViews[key] = keyPreviewView
            keyPreviewView.animateProperty()
            return
        }

        // Show preview with animation.
        val showUpAnimator = createShowUpAnimator(key, keyPreviewView)
        val dismissAnimator = createDismissAnimator(key, keyPreviewView)
        val animators = KeyPreviewAnimators(showUpAnimator, dismissAnimator)
        keyPreviewView!!.setTag(animators)
        animators.startShowUp()
    }

    fun createShowUpAnimator(key: Key, keyPreviewView: KeyEffectView?): Animator {
        val showUpAnimator = mParams!!.createShowUpAnimator(keyPreviewView)
        showUpAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animator: Animator) {
                showKeyPreview(key, keyPreviewView, false /* withAnimation */)
            }
        })
        return showUpAnimator
    }

    private fun createDismissAnimator(key: Key, keyPreviewView: KeyEffectView?): Animator {
        val dismissAnimator = mParams!!.createDismissAnimator(keyPreviewView)
        dismissAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animator: Animator) {
                dismissKeyPreview(key, false /* withAnimation */)
            }
        })
        return dismissAnimator
    }

    private class KeyPreviewAnimators(
        private val mShowUpAnimator: Animator,
        private val mDismissAnimator: Animator
    ) :
        AnimatorListenerAdapter() {
        fun startShowUp() {
            mShowUpAnimator.start()
        }

        fun startDismiss() {
            if (mShowUpAnimator.isRunning) {
                mShowUpAnimator.addListener(this)
                return
            }
            mDismissAnimator.start()
        }

        override fun onAnimationEnd(animator: Animator) {
            mDismissAnimator.start()
        }
    }


    private fun showEffectClick(previewX: Int, previewY: Int) {
        val keyPreviewView = KeyEffectView(context)
    }
}