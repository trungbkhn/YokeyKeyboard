/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.keyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.ColorUtils;

import com.android.inputmethod.accessibility.AccessibilityUtils;
import com.android.inputmethod.accessibility.MoreKeysKeyboardAccessibilityDelegate;
import com.android.inputmethod.keyboard.internal.KeyDrawParams;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.common.CoordinateUtils;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.feature.ThemeGradientControl;
import com.tapbi.spark.yokey.feature.ThemesLEDControl;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.DisplayUtils;

import java.util.Objects;

import timber.log.Timber;

/**
 * A view that renders a virtual {@link MoreKeysKeyboard}. It handles rendering of keys and
 * detecting key presses and touch movements.
 */
public class MoreKeysKeyboardView extends KeyboardView implements MoreKeysPanel {
    protected final KeyDetector mKeyDetector;
    private final int[] mCoordinates = CoordinateUtils.newInstance();
    private final Drawable mDivider;
    protected KeyboardActionListener mListener;
    protected MoreKeysKeyboardAccessibilityDelegate mAccessibilityDelegate;
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Controller mController = EMPTY_CONTROLLER;
    private int mOriginX;
    private int mOriginY;
    private Key mCurrentKey;
    private int mActivePointerId;

    public MoreKeysKeyboardView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.moreKeysKeyboardViewStyle);
    }

    public MoreKeysKeyboardView(final Context context, final AttributeSet attrs,
                                final int defStyle) {

        super(context, attrs, defStyle);
        final TypedArray moreKeysKeyboardViewAttr = context.obtainStyledAttributes(attrs,
                R.styleable.MoreKeysKeyboardView, defStyle, R.style.MoreKeysKeyboardView);
        mDivider = moreKeysKeyboardViewAttr.getDrawable(R.styleable.MoreKeysKeyboardView_divider);
        if (mDivider != null) {
            // TODO: Drawable itself should have an alpha value.
            mDivider.setAlpha(128);
        }
        moreKeysKeyboardViewAttr.recycle();
        mKeyDetector = new MoreKeysDetector(getResources().getDimension(
                R.dimen.config_more_keys_keyboard_slide_allowance));


    }

    @Override
    protected void onDraw(Canvas canvas) {
//        drawBackGroundMoreKeyBoard(canvas);
        ThemeModel themeModel = getThemeModel();
        if (themeModel != null && themeModel.getTypeKeyboard() != null) {
            if (themeModel.getTypeKeyboard().equals(Constants.ID_CATEGORY_GRADIENT)
                    || themeModel.getTypeKeyboard().equals(Constants.ID_CATEGORY_COLOR)
                    || themeModel.getTypeKeyboard().equals(Constants.ID_CATEGORY_WALL)
                    || themeModel.getTypeKeyboard().equals(Constants.ID_FEATURED)) {
                setColorForKeyMore(canvas);
            }
        }

        super.onDraw(canvas);
    }

    //draw when move key more
    private void setColorForKeyMore(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (mCurrentKey != null) {
            if (getThemeModel().getTypeKeyboard().equals(Constants.ID_CATEGORY_GRADIENT)) {
                int[] colorArray = new int[]{CommonUtil.hex2decimal(getThemeModel().getBackground().getStartColor()),
                        CommonUtil.hex2decimal(getThemeModel().getBackground().getFinishColor())};
                paint.setShader(new LinearGradient(mCurrentKey.getX(), mCurrentKey.getY() - mCurrentKey.getHeight() / 8f
                        , mCurrentKey.getDrawWidth() + mCurrentKey.getX(), mCurrentKey.getHeight() + mCurrentKey.getY(), colorArray,
                        null, Shader.TileMode.CLAMP));
            } else if (getThemeModel().getTypeKeyboard().equals(Constants.ID_CATEGORY_COLOR) || getThemeModel().getTypeKeyboard().equals(Constants.ID_CATEGORY_WALL)) {
                paint.setShader(null);
                paint.setColor(ColorUtils.blendARGB(CommonUtil.hex2decimal(getThemeModel().getBackground().getBackgroundColor()), Color.BLACK, 0.1f));
            } else if (getThemeModel().getTypeKeyboard().equals(Constants.ID_FEATURED)) {
                if (getThemeModel() != null) {
                    if (getThemeModel().getId() != null && Long.parseLong(getThemeModel().getId()) > 6010 && Long.parseLong(getThemeModel().getId()) < 6030) {
                        paint.setShader(null);
                        if (getThemeModel().getBackground() != null && getThemeModel().getBackground().getBackgroundColor() != null) {
                            paint.setColor(ColorUtils.blendARGB(CommonUtil.hex2decimal(getThemeModel().getBackground().getBackgroundColor()), Color.BLACK, 0.1f));
                        }
                    } else {
                        paint.setShader(null);
                        if (getThemeModel().getKey() != null && getThemeModel().getKey().getText() != null) {
                            paint.setColor(ColorUtils.blendARGB(CommonUtil.hex2decimal(getThemeModel().getKey().getText().getTextColor()), Color.BLACK, 0.1f));

                        }
                    }
                }
            }
            paint.setStyle(Paint.Style.FILL);
            int left = mCurrentKey.getX() + 7;
            int top = mCurrentKey.getY() + 4;
            canvas.drawRoundRect(left, top,
                    mCurrentKey.getWidth() + left,
                    mCurrentKey.getHeight() + top, DisplayUtils.dp2px(4), DisplayUtils.dp2px(4), paint);
        }
    }

    private void drawBackGroundMoreKeyBoard(Canvas canvas) {
        switch (Objects.requireNonNull(getThemeModel().getTypeKeyboard())) {
            case Constants.ID_CATEGORY_GRADIENT:
                int startColor = ColorUtils.blendARGB(CommonUtil.hex2decimal(getThemeModel().getBackground().getFinishColor()),
                        Color.BLACK, 0.2f);
                int finishColor = ColorUtils.blendARGB(CommonUtil.hex2decimal(getThemeModel().getBackground().getStartColor()),
                        Color.BLACK, 0.2f);
                int[] colorArray = new int[]{startColor,
                        finishColor};
                paint.setStyle(Paint.Style.FILL);
                paint.setShader(new LinearGradient(0, 0, getWidth(), getHeight(), colorArray, null, Shader.TileMode.CLAMP));
                canvas.drawRoundRect(0, 0, getWidth(), getHeight(), DisplayUtils.dp2px(4), DisplayUtils.dp2px(4), paint);
                break;
            case Constants.ID_CATEGORY_COLOR:
                paint.setColor(ColorUtils.blendARGB(CommonUtil.hex2decimal(getThemeModel().getBackground().getBackgroundColor()),
                        Color.BLACK, 0.2f));
                paint.setStyle(Paint.Style.FILL);
                paint.setShader(null);
                canvas.drawRoundRect(0, 0, getWidth(), getHeight(), DisplayUtils.dp2px(4), DisplayUtils.dp2px(4), paint);
                break;
            case Constants.ID_CATEGORY_WALL:
                paint.setColor(CommonUtil.hex2decimal(getThemeModel().getBackground().getBackgroundColor()));
                paint.setStyle(Paint.Style.FILL);
                paint.setShader(null);
                canvas.drawRoundRect(0, 0, getWidth(), getHeight(), DisplayUtils.dp2px(3f), DisplayUtils.dp2px(3f), paint);
                break;
        }
    }

    @Override
    public void setKeyboard(final Keyboard keyboard) {
        super.setKeyboard(keyboard);
        setThemeForKeyboard();
        mKeyDetector.setKeyboard(keyboard, -getPaddingLeft(), -getPaddingTop() + getVerticalCorrection());
        if (AccessibilityUtils.getInstance().isAccessibilityEnabled()) {
            if (mAccessibilityDelegate == null) {
                mAccessibilityDelegate = new MoreKeysKeyboardAccessibilityDelegate(
                        this, mKeyDetector);
                mAccessibilityDelegate.setOpenAnnounce(R.string.spoken_open_more_keys_keyboard);
                mAccessibilityDelegate.setCloseAnnounce(R.string.spoken_close_more_keys_keyboard);
            }
            mAccessibilityDelegate.setKeyboard(keyboard);
        } else {
            mAccessibilityDelegate = null;
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final Keyboard keyboard = getKeyboard();

        if (keyboard != null) {
            final int width = keyboard.mOccupiedWidth + getPaddingLeft() + getPaddingRight();
            final int height = keyboard.mOccupiedHeight + getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDrawKeyTopVisuals(final Key key, final Canvas canvas, final Paint paint,
                                       final KeyDrawParams params, boolean isSearchGif, Paint paintIconFilter, Paint paintIcon, Shader shader) {
        if (!key.isSpacer() || !(key instanceof MoreKeysKeyboard.MoreKeyDivider)
                || mDivider == null) {
            if (getThemeModel().getTypeKeyboard().equalsIgnoreCase("rgb")) {
                //super.onDrawKeyTopVisuals(key, canvas, paint, params);
                if (themesLEDControl == null) themesLEDControl = new ThemesLEDControl();
                themesLEDControl.drawKeyLabel(this, getKeyboard(), key, canvas, paintIcon, params, themesLEDControl.getTypeface(getContext(), getThemeModel().getFont()), false);
                themesLEDControl.drawIconKey(App.getInstance(), getKeyboard(), key, canvas, paintIconFilter, paintIcon, params, isSearchGif, this, shader);
            } else {
//                setBackground(CommonUtil.getImage9PathFromLocal(getContext(), CommonUtil.getPathImage(getContext(), getThemeModel(), key, getThemeModel().getPopup().getMinKeyboard().getBgImage())));
                ThemeGradientControl themeGradientControl = new ThemeGradientControl();
                // Draw key label.
                themeGradientControl.drawKeyLabelMoreKeyboardView(getKeyboard(), key, canvas, paint, params, isSearchGif, getThemeModel(),
                        10, false);
            }
            return;
        }
        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();
        final int iconWidth = Math.min(mDivider.getIntrinsicWidth(), keyWidth);
        final int iconHeight = mDivider.getIntrinsicHeight();
        final int iconX = (keyWidth - iconWidth) / 2; // Align horizontally center
        final int iconY = (keyHeight - iconHeight) / 2; // Align vertically center

        drawIcon(canvas, mDivider, key.getX(), key.getY(), iconX, iconY);
    }

    @Override
    public void showMoreKeysPanel(final View parentView, final Controller controller,
                                  final int pointX, final int pointY, final KeyboardActionListener listener) {
        Timber.e("hachung showMoreKeysPanel:");
        mController = controller;
        mListener = listener;
        final View container = getContainerView();

        // The coordinates of panel's left-top corner in parentView's coordinate system.
        // We need to consider background drawable paddings.
        //final int x = pointX - getDefaultCoordX() - container.getPaddingLeft() - getPaddingLeft();
//        final int x = pointX - container.getPaddingLeft() - getPaddingLeft();
        final int x = pointX - getDefaultCoordX() - container.getPaddingLeft() - getPaddingLeft();
        final int y = pointY - container.getMeasuredHeight() + container.getPaddingBottom()
                + getPaddingBottom();

        parentView.getLocationInWindow(mCoordinates);
        // Ensure the horizontal position of the panel does not extend past the parentView edges.
        final int maxX = parentView.getMeasuredWidth() - container.getMeasuredWidth();
        final int panelX = Math.max(0, Math.min(maxX, x)) + CoordinateUtils.x(mCoordinates);
        final int panelY = y + CoordinateUtils.y(mCoordinates);
        container.setX(panelX);
        container.setY(panelY);

        mOriginX = x + container.getPaddingLeft();
        mOriginY = y + container.getPaddingTop();
        controller.onShowMoreKeysPanel(this);
        final MoreKeysKeyboardAccessibilityDelegate accessibilityDelegate = mAccessibilityDelegate;
        if (accessibilityDelegate != null
                && AccessibilityUtils.getInstance().isAccessibilityEnabled()) {
            accessibilityDelegate.onShowMoreKeysKeyboard();
        }
    }

    /**
     * Returns the default x coordinate for showing this panel.
     */
    protected int getDefaultCoordX() {
        return ((MoreKeysKeyboard) getKeyboard()).getDefaultCoordX();
    }

    @Override
    public void onDownEvent(final int x, final int y, final int pointerId, final long eventTime) {
        mActivePointerId = pointerId;
        mCurrentKey = detectKey(x, y);
        if (mCurrentKey != null) {
            if (mCurrentKey.getLabel() != null) {
                updateReleaseKeyGraphics(mCurrentKey);
            }
        }
    }

    @Override
    public void onMoveEvent(final int x, final int y, final int pointerId, final long eventTime) {
        if (mActivePointerId != pointerId) {
            return;
        }
        final boolean hasOldKey = (mCurrentKey != null);
        mCurrentKey = detectKey(x, y);
        if (hasOldKey && mCurrentKey == null) {
            // A more keys keyboard is canceled when detecting no key.
            Log.d("duongcv", "onMoveEvent: cancel");
            mController.onCancelMoreKeysPanel();
        }
    }

    @Override
    public void onUpEvent(final int x, final int y, final int pointerId, final long eventTime) {
        if (mActivePointerId != pointerId) {
            return;
        }
        // Calling {@link #detectKey(int,int,int)} here is harmless because the last move event and
        // the following up event share the same coordinates.
        mCurrentKey = detectKey(x, y);
//        if(keyDown!=null && !checkMove){
//            updateReleaseKeyGraphics(keyDown);
//            onKeyInput(keyDown, x, y);
//            keyDown = null;
//        }else
        if (mCurrentKey != null) {
            updateReleaseKeyGraphics(mCurrentKey);
            onKeyInput(mCurrentKey, x, y);
            mCurrentKey = null;
        }
    }

    /**
     * Performs the specific action for this panel when the user presses a key on the panel.
     */
    protected void onKeyInput(final Key key, final int x, final int y) {
        final int code = key.getCode();
        if (code == Constants.CODE_OUTPUT_TEXT) {
            mListener.onTextInput(mCurrentKey.getOutputText());
        } else if (code != Constants.CODE_UNSPECIFIED) {
            if (getKeyboard().hasProximityCharsCorrection(code)) {
                mListener.onCodeInput(code, x, y, false /* isKeyRepeat */, false);
            } else {
                mListener.onCodeInput(code, Constants.NOT_A_COORDINATE, Constants.NOT_A_COORDINATE,
                        false /* isKeyRepeat */, false);
            }
        }
    }

    private Key detectKey(int x, int y) {
        final Key oldKey = mCurrentKey;
        final Key newKey = mKeyDetector.detectHitKey(x, y);
        if (newKey == oldKey) {
            return newKey;
        }
        // A new key is detected.
        if (oldKey != null) {
            updateReleaseKeyGraphics(oldKey);
            invalidateKey(oldKey);
        }
        if (newKey != null) {
            updatePressKeyGraphics(newKey);
            invalidateKey(newKey);
        }
        return newKey;
    }

    private void updateReleaseKeyGraphics(final Key key) {
        key.onReleased();
        invalidateKey(key);
    }

    private void updatePressKeyGraphics(final Key key) {
        key.onPressed();
        invalidateKey(key);
    }

    @Override
    public void dismissMoreKeysPanel() {
        if (!isShowingInParent()) {
            return;
        }
        final MoreKeysKeyboardAccessibilityDelegate accessibilityDelegate = mAccessibilityDelegate;
        if (accessibilityDelegate != null
                && AccessibilityUtils.getInstance().isAccessibilityEnabled()) {
            accessibilityDelegate.onDismissMoreKeysKeyboard();
        }
        mController.onDismissMoreKeysPanel();
    }

    @Override
    public int translateX(final int x) {
        return x - mOriginX;
    }

    @Override

    public int translateY(final int y) {
        return y - mOriginY;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent me) {
        final int action = me.getActionMasked();
        final long eventTime = me.getEventTime();
        final int index = me.getActionIndex();
        // todo: Duongcv : check touch more key suggestion
        final int x = (int) me.getX(index);
//        final int y = (int) me.getY(index);
//        final int x = (int) (me.getX() + 0.5f);
        final int y = (int) (me.getY() + 0.5f) + 20;
//        Log.d("duongcv", "onTouchEvent: " + x+":"+y);
//        Log.d("duongcv", "onTouchEvent: get" + me.getX()+":"+me.getY());
        final int pointerId = me.getPointerId(index);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                onDownEvent(x, y, pointerId, eventTime);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                onUpEvent(x, y, pointerId, eventTime);
                break;
            case MotionEvent.ACTION_MOVE:
                onMoveEvent(x, y, pointerId, eventTime);
                break;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onHoverEvent(final MotionEvent event) {
        final MoreKeysKeyboardAccessibilityDelegate accessibilityDelegate = mAccessibilityDelegate;
        if (accessibilityDelegate != null
                && AccessibilityUtils.getInstance().isTouchExplorationEnabled()) {
            return accessibilityDelegate.onHoverEvent(event);
        }
        return super.onHoverEvent(event);
    }

    private View getContainerView() {
        return (View) getParent();
    }

    @Override
    public void showInParent(final ViewGroup parentView) {
        removeFromParent();
        parentView.addView(getContainerView());
    }

    @Override
    public void removeFromParent() {
        final View containerView = getContainerView();
        final ViewGroup currentParent = (ViewGroup) containerView.getParent();
        if (currentParent != null) {
            currentParent.removeView(containerView);
        }
    }

    @Override
    public boolean isShowingInParent() {
        return (getContainerView().getParent() != null);
    }
}
