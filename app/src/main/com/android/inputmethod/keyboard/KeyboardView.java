/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.tapbi.spark.yokey.R;
import com.android.inputmethod.keyboard.internal.KeyDrawParams;
import com.android.inputmethod.keyboard.internal.KeyVisualAttributes;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.settings.Settings;
import com.android.inputmethod.latin.utils.TypefaceUtils;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.data.model.Font;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.Constant;
import com.tapbi.spark.yokey.common.CommonVariable;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.feature.ThemeGradientControl;
import com.tapbi.spark.yokey.feature.ThemesLEDControl;

import java.util.HashSet;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.tapbi.spark.yokey.common.CommonVariable.VALUE_DEFAULT;
import static com.tapbi.spark.yokey.common.CommonVariable.VALUE_SPEED_COLOR_DEFAULT;

import timber.log.Timber;

public class KeyboardView extends RelativeLayout {
    private final KeyVisualAttributes mKeyVisualAttributes;
    private final int mDefaultKeyLabelFlags;
    private final float mKeyHintLetterPadding;
    private final String mKeyPopupHintLetter;
    private final float mKeyPopupHintLetterPadding;
    private final float mKeyShiftedLetterHintPadding;
    private final float mVerticalCorrection;
    // The maximum key label width in the proportion to the key width.
    public static final float MAX_LABEL_RATIO = 0.90f;
    private Keyboard mKeyboard;
    private Typeface typeface;

    public CharSequence[] charSequenceFont;
    private final Font itemFont = new Font();
    public String keyFont;
    public boolean isUsingLanguageKeyboardOtherQwerty;
    private SharedPreferences mPrefs;
    public boolean isSearchGif;

    public boolean isHardwareAcceleratedDrawingEnabled = false;


    @Nonnull
    private final KeyDrawParams mKeyDrawParams = new KeyDrawParams();

    // Drawing
    /**
     * True if all keys should be drawn
     */
    private boolean mInvalidateAllKeys;

    /**
     * The working rectangle for clipping
     */
    private final Rect mClipRect = new Rect();
    /**
     * The keys that should be drawn
     */
    private final HashSet<Key> mInvalidatedKeys = new HashSet<>();
    /**
     * The keyboard bitmap buffer for faster updates
     */
    private Bitmap mOffscreenBuffer;
    /**
     * The canvas for the above mutable keyboard bitmap
     */
    @Nonnull
    private final Canvas mOffscreenCanvas = new Canvas();
    @Nonnull
    private Paint mPaint = new Paint();
    private final Paint.FontMetrics mFontMetrics = new Paint.FontMetrics();
    public ThemeModel themeModel;

    public String fullLanguageDisplay = "";
    private Shader mShader;
    private Paint paintIconFilter = new Paint();
    private Paint paintRect = new Paint();
    private Paint paintIcon = new Paint();
    private Paint paintPreview = new Paint();
    private Matrix matrixT = new Matrix();
    private int mTranslate = 0;
    private Matrix mMatrix = new Matrix();
    private int[] colors = new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF0000FF, 0xFF00FFFF, 0xFFFF00FF, 0xFFFF0000};
    private final float mKeyTextShadowRadius;
    private static final float KET_TEXT_SHADOW_RADIUS_DISABLED = -1.0f;
    private final float mSpacebarIconWidthRatio;
    private float radiusKey;
    private float keyPaddingX;
    private float keyPaddingY;
    float radiusKeyDefault;
    float strokeWidthDefault;
    final TypedArray keyboardViewAttr;
    private final Rect mKeyBackgroundPadding = new Rect();
    private final Drawable mKeyBackground;
    private final Drawable mFunctionalKeyBackground;
    private final Drawable mSpacebarBackground;
    public Drawable drawableEffectClick;
    private ThemeGradientControl themeGradientControl;
    public static String SPECIAL_CHARACTERS_CHANGE_FONT =
            "èéẻẽẹêềếểễệỳýỷỹỵùúủũụưừứửữựìíỉĩịòóỏõọôồốổỗộơờớởỡợàáảãạăằắẳẵặâầấẩẫậđ      éèêëēúûüùūíîïīìóôöòœøōõàáâäæãåāßçñ      ёъ";

    public static String RUSSIAN_CHANGE_FONT = "йцукенгшщзхфывапролджэячсмитьбюёъ";
    public static String SYMBOL_NOT_CHANGE_FONT = "?123";
    public static String SYMBOL_LANGUAGE_GREEK_CHANGE_FONT = "π";
    public ThemesLEDControl themesLEDControl;

    public KeyboardView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.keyboardViewStyle);
    }

    public KeyboardView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        themeGradientControl = new ThemeGradientControl();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        keyboardViewAttr = context.obtainStyledAttributes(attrs,
                R.styleable.KeyboardView, defStyle, R.style.KeyboardView);
        mKeyHintLetterPadding = keyboardViewAttr.getDimension(
                R.styleable.KeyboardView_keyHintLetterPadding, 0.0f);
        mKeyPopupHintLetter = keyboardViewAttr.getString(
                R.styleable.KeyboardView_keyPopupHintLetter);
        mKeyPopupHintLetterPadding = keyboardViewAttr.getDimension(
                R.styleable.KeyboardView_keyPopupHintLetterPadding, 0.0f);
        mKeyShiftedLetterHintPadding = keyboardViewAttr.getDimension(
                R.styleable.KeyboardView_keyShiftedLetterHintPadding, 0.0f);
        mVerticalCorrection = keyboardViewAttr.getDimension(
                R.styleable.KeyboardView_verticalCorrection, 0.0f);
        mKeyTextShadowRadius = keyboardViewAttr.getFloat(R.styleable.KeyboardView_keyTextShadowRadius, KET_TEXT_SHADOW_RADIUS_DISABLED);
        mSpacebarIconWidthRatio = keyboardViewAttr.getFloat(R.styleable.KeyboardView_spacebarIconWidthRatio, 1.0f);
        mKeyBackground = keyboardViewAttr.getDrawable(R.styleable.KeyboardView_keyBackground);
        mKeyBackground.getPadding(mKeyBackgroundPadding);
        final Drawable functionalKeyBackground = keyboardViewAttr.getDrawable(R.styleable.KeyboardView_functionalKeyBackground);
        mFunctionalKeyBackground = (functionalKeyBackground != null) ? functionalKeyBackground
                : mKeyBackground;
        final Drawable spacebarBackground = keyboardViewAttr.getDrawable(
                R.styleable.KeyboardView_spacebarBackground);
        mSpacebarBackground = (spacebarBackground != null) ? spacebarBackground : mKeyBackground;

        keyboardViewAttr.recycle();

        @SuppressLint("CustomViewStyleable") final TypedArray keyAttr = context.obtainStyledAttributes(attrs,
                R.styleable.Keyboard_Key, defStyle, R.style.KeyboardView);
        mDefaultKeyLabelFlags = keyAttr.getInt(R.styleable.Keyboard_Key_keyLabelFlags, 0);
        mKeyVisualAttributes = KeyVisualAttributes.newInstance(keyAttr);
        keyAttr.recycle();
        radiusKeyDefault = radiusKey = context.getResources().getDisplayMetrics().density * 6;
        setThemeForKeyboard();
        mPaint.setAntiAlias(true);
        loadKeyFont();
    }

    public void setFullLanguageDisplay(String fullLanguageDisplay) {
        this.fullLanguageDisplay = fullLanguageDisplay;
    }

    public void setThemeForKeyboard() {
        if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE) {
            themeModel = App.getInstance().themeRepository.getDefaultThemeModel();
        }else {
            themeModel = App.getInstance().themeRepository.getCurrentThemeModel();
        }
        if (themeModel == null) {
            String id = mPrefs.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, "0");
            if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE) {
                id = Constant.ID_THEME_DEFAULT;
            }
            themeModel = CommonUtil.parserJsonFromFileTheme(App.getInstance(), id);
        }
        if (themeModel != null) {
            if (themeModel.getEffect().equals(Constant.ID_NONE)) drawableEffectClick = null;
            else
                drawableEffectClick = CommonUtil.getDrawableEffectClick(App.getInstance(), themeModel.getEffect());
        }
        radiusKeyDefault = App.getInstance().getResources().getDisplayMetrics().density * 6;
        strokeWidthDefault = App.getInstance().getResources().getDisplayMetrics().density;
        keyPaddingX = App.getInstance().getResources().getDisplayMetrics().density * 0;
        keyPaddingY = App.getInstance().getResources().getDisplayMetrics().density * 0;


        if (themeModel != null) {
            if ("rgb".equals(themeModel.getTypeKeyboard())) {
                if (themesLEDControl == null) themesLEDControl = new ThemesLEDControl();
                themesLEDControl.setStrokeWidth(App.getInstance(), themeModel.getKey().getLed().getStrokeWidth(), paintRect, paintPreview, paintIcon);
                typeface = themesLEDControl.getTypeface(getContext(), themeModel.getFont());
            }
        } else {
            CommonUtil.copyThemeFofFirstTimeOpenAppFromAssetToFile(getContext());
        }

    }

    @Nullable
    public KeyVisualAttributes getKeyVisualAttribute() {
        return mKeyVisualAttributes;
    }

    // Get value for Mainkeyboardview draw popup

    public float getRadiusKey() {
        return radiusKey;
    }

    public Paint getPaintPreview() {
        return paintPreview;
    }

    public Paint getmPaint() {
        return mPaint;
    }

    public ThemeModel getThemeModel() {
        return themeModel;
    }

    public void changeTextColor(String colorText) {
        themeModel.getKey().getText().setTextColor(colorText);
        invalidate();
    }

    public void changeTypeKey(int typeKey) {
        themeModel.setTypeKey(typeKey);
        invalidate();
    }


    public void setHardwareAcceleratedDrawingEnabled(final boolean enabled) {
        isHardwareAcceleratedDrawingEnabled = enabled;
        if (!enabled) return;
        // TODO: Should use LAYER_TYPE_SOFTWARE when hardware acceleration is off?
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    /**
     * Attaches a keyboard to this view. The keyboard can be switched at any time and the
     * view will re-layout itself to accommodate the keyboard.
     *
     * @param keyboard the keyboard to display in this view
     * @see Keyboard
     * @see #getKeyboard()
     */
    public void setKeyboard(final Keyboard keyboard) {
        mKeyboard = keyboard;
        final int keyHeight = keyboard.mMostCommonKeyHeight - keyboard.mVerticalGap;
        mKeyDrawParams.updateParams(keyHeight, mKeyVisualAttributes);

//        mKeyDrawParams.updateParams(keyHeight, keyboard.mKeyVisualAttributes);
        invalidateAllKeys();
        requestLayout();

    }

    /**
     * Returns the current keyboard being displayed by this view.
     *
     * @return the currently attached keyboard
     * @see #setKeyboard(Keyboard)
     */
    @Nullable
    public Keyboard getKeyboard() {
        return mKeyboard;
    }

    protected float getVerticalCorrection() {
        return mVerticalCorrection;
    }

    @Nonnull
    protected KeyDrawParams getKeyDrawParams() {
        return mKeyDrawParams;
    }

    protected void updateKeyDrawParams(final int keyHeight) {
        mKeyDrawParams.updateParams(keyHeight, mKeyVisualAttributes);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final Keyboard keyboard = getKeyboard();
        if (keyboard == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // The main keyboard expands to the entire this {@link KeyboardView}.
        final int width = keyboard.mOccupiedWidth + getPaddingLeft() + getPaddingRight();
        final int height = keyboard.mOccupiedHeight + getPaddingTop() + getPaddingBottom();
        Timber.e("Duongcv " + width +":"+height);
//        if(DisplayUtils.getScreenWidth()> DisplayUtils.getScreenHeight()){
        setMeasuredDimension(width, height);
//        }else{
//            int heightKbNew = (int) mPrefs.getFloat(com.keyboard.zomj.common.Constant.HEIGHT_KEYBOARD_NEW, 0);
//            setMeasuredDimension(width, heightKbNew != 0 ? heightKbNew : height);
//        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
//        if (canvas.isHardwareAccelerated()) {
        onDrawKeyboard(canvas);
//            return;
//        }

//        final boolean bufferNeedsUpdates = mInvalidateAllKeys || !mInvalidatedKeys.isEmpty();
//        if (bufferNeedsUpdates || mOffscreenBuffer == null) {
//            if (maybeAllocateOffscreenBuffer()) {
//                mInvalidateAllKeys = true;
//                // TODO: Stop using the offscreen canvas even when in software rendering
//                mOffscreenCanvas.setBitmap(mOffscreenBuffer);
//            }
//            onDrawKeyboard(mOffscreenCanvas);
//        }
//        canvas.drawBitmap(mOffscreenBuffer, 0.0f, 0.0f, null);

    }

    private boolean maybeAllocateOffscreenBuffer() {
        final int width = getWidth();
        final int height = getHeight();
        if (width == 0 || height == 0) {
            return false;
        }
        if (mOffscreenBuffer != null && mOffscreenBuffer.getWidth() == width
                && mOffscreenBuffer.getHeight() == height) {
            return false;
        }
        freeOffscreenBuffer();
        mOffscreenBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        return true;
    }

    private void freeOffscreenBuffer() {
        mOffscreenCanvas.setBitmap(null);
        mOffscreenCanvas.setMatrix(null);
        if (mOffscreenBuffer != null) {
            mOffscreenBuffer.recycle();
            mOffscreenBuffer = null;
        }
    }

    public String getThemeKeyBgStyle() {
        return themeModel.getKey().getLed().getStyle();
    }

    public float getThemeKeyBgAlpha() {
        return themeModel.getKey().getLed().getAlpha();
    }

    public int getHeightWithMargin() {
        return getHeight() - 10;
    }

    private void onDrawKeyboard(@Nonnull final Canvas canvas) {
        final Paint paint = mPaint;
        Keyboard keyboard = getKeyboard();
        if (keyboard == null) {
            return;
        }

        final boolean drawAllKeys = mInvalidateAllKeys || mInvalidatedKeys.isEmpty();
        final boolean isHardwareAccelerated = canvas.isHardwareAccelerated();


        matrixT.setRotate(0, getWidth() / 2f, getHeight() / 2f);
        if (mPaint.getAlpha() != 255) {
            mPaint.setAlpha(255);
        }
        if (themeModel == null || themeModel.getTypeKeyboard() == null) return;
        if (themeModel.getTypeKeyboard().equalsIgnoreCase("rgb")) {
            if (!App.getInstance().isShowEmoji) {
                float speed = 0;
                if (themeModel.getKey() != null && themeModel.getKey().getLed() != null && themeModel.getKey().getLed().getSpeed() != null) {
                    speed = themeModel.getKey().getLed().getSpeed();
                }
                mTranslate += (VALUE_SPEED_COLOR_DEFAULT * speed / VALUE_DEFAULT);
                if (mTranslate >= 30000) mTranslate = 0;
                mShader = ThemesLEDControl.getLEDStyle(this, mTranslate, colors, themeModel);
                //setup for paint border, icon, preview, icon filter;
                mPaint.setShader(mShader);
                paintRect.setShader(mShader);
                paintIcon.setShader(mShader);
                paintPreview.setShader(mShader);
                paintIconFilter.setShader(mShader);
            }
        } else {
            mPaint = new Paint();
            if (mPaint.getAlpha() != 255) {
                mPaint.setAlpha(255);
            }
            paintIconFilter = new Paint();
            paintRect = new Paint();
            paintIcon = new Paint();
            paintPreview = new Paint();
        }
        if (themeModel.getTypeKeyboard().equalsIgnoreCase("rgb")) postInvalidateAllKeys();
        //set background

        // TODO: Confirm if it's really required to draw all keys when hardware acceleration is on.
        if (drawAllKeys || isHardwareAccelerated) {
            // Draw all keys.
            for (final Key key : keyboard.getSortedKeys()) {
                onDrawKey(key, canvas, paint);
            }
        } else {
            // Draw invalidated keys.
            for (final Key key : mInvalidatedKeys) {
                if (keyboard.hasKey(key)) {
                    onDrawKey(key, canvas, paint);
                }


            }
        }
        mInvalidatedKeys.clear();
        mInvalidateAllKeys = false;
    }

    private void onDrawKey(final Key key, final Canvas canvas,
                           final Paint paint) {
        final Keyboard keyboard = getKeyboard();
        if (keyboard == null) {
            return;
        }
        final int keyHeight = keyboard.mMostCommonKeyHeight - keyboard.mVerticalGap;
        final KeyVisualAttributes attr = key.getVisualAttributes();
        final KeyDrawParams params = mKeyDrawParams.mayCloneAndUpdateParams(keyHeight, attr);
        params.mAnimAlpha = Constants.Color.ALPHA_OPAQUE;
        onDrawKeyTopVisuals(key, canvas, paint, params, isSearchGif, paintIconFilter, paintIcon, mShader);

    }

    // Draw key top visuals.
    protected void onDrawKeyTopVisuals(final Key key, final Canvas canvas, final Paint paint,
                                       final KeyDrawParams params, boolean isSearchGif, Paint paintIconFilter,
                                       Paint paintIcon, Shader mShader) {
        if (themeModel == null || themeModel.getTypeKeyboard() == null) return;
        switch (themeModel.getTypeKeyboard()) {
            case "rgb":
                if (themesLEDControl == null) themesLEDControl = new ThemesLEDControl();
                // Draw key label.
                themesLEDControl.drawKeyLabel(this, getKeyboard(), key, canvas, paint, params, typeface, isUsingLanguageKeyboardOtherQwerty);
                // Draw hint label.
                themesLEDControl.drawHintLabelKey(key, canvas, paint, params, mDefaultKeyLabelFlags, mKeyShiftedLetterHintPadding, mKeyHintLetterPadding);
                // Draw key icon.
                themesLEDControl.drawIconKey(getContext(), getKeyboard(), key, canvas, paintIconFilter, paintIcon, params, isSearchGif, this, mShader);
                // Draw key Rect
                themesLEDControl.drawRectBorderKey(key, canvas, paintRect, keyPaddingX, keyPaddingY, radiusKeyDefault * themeModel.getKey().getLed().getRadius(),
                        themeModel.getKey().getLed().getStyle(), (int) themeModel.getKey().getLed().getAlpha(), themeModel.getKey().getLed().getStrokeWidth());
                break;
            case "color":
            case "background":
            case "gradient":

            default:



                // Draw key icon.
                themeGradientControl.drawIconKey(getContext(), getKeyboard(), key, canvas, params, isSearchGif, themeModel, mSpacebarIconWidthRatio, paint,false);

                // Draw key label.
                // Todo: Change font keyboard Duongcv
                themeGradientControl.drawKeyLabel(this, getContext(), getKeyboard(), key, canvas, paint, params, themeModel, mKeyTextShadowRadius, isUsingLanguageKeyboardOtherQwerty);

                // Draw hint label.
                themeGradientControl.drawHintLabel(key, paint, params, themeModel, mDefaultKeyLabelFlags, mKeyShiftedLetterHintPadding, mFontMetrics, mKeyHintLetterPadding, canvas);

                //Draw popup hint
                if (key.hasPopupHint() && key.getMoreKeys() != null) {
                    themeGradientControl.drawKeyPopupHint(getContext(), key, canvas, paint, params, mKeyPopupHintLetter, themeModel, mKeyHintLetterPadding, mKeyPopupHintLetterPadding);
                }
                break;

        }
    }

    public void resetSizeEmoji() {
        if (themesLEDControl != null) themesLEDControl.sizeEmoji = 0;
        if (themeGradientControl != null) themeGradientControl.sizeEmoji = 0;
    }

    // Draw popup hint "..." at the bottom right corner of the key.
    protected void drawKeyPopupHint(@Nonnull final Key key, @Nonnull final Canvas canvas,
                                    @Nonnull final Paint paint, @Nonnull final KeyDrawParams params) {
        if (TextUtils.isEmpty(mKeyPopupHintLetter)) {
            return;
        }
        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();

        paint.setTypeface(params.mTypeface);
        paint.setTextSize(params.mHintLetterSize);
        paint.setColor(params.mHintLabelColor);
        paint.setTextAlign(Align.CENTER);
        final float hintX = keyWidth - mKeyHintLetterPadding
                - TypefaceUtils.getReferenceCharWidth(paint) / 2.0f;
        final float hintY = keyHeight - mKeyPopupHintLetterPadding;
        String strPathIconKeyText = CommonUtil.getPathImage(getContext(), themeModel, key, themeModel.getPopup().getMinKeyboard().getBgImage());
        drawIcon(canvas, Objects.requireNonNull(CommonUtil.getImage9PathFromLocal(getContext(), strPathIconKeyText,false)), key.getX(), key.getY(), key.getDrawWidth(), key.getHeight());
        canvas.drawText(mKeyPopupHintLetter, hintX, hintY, paint);
    }

    protected void drawKeyWhenPressSpaceAndSpecial(@Nonnull final Key key, @Nonnull final Canvas canvas, @Nonnull final KeyDrawParams params) {
        ThemeGradientControl themeGradientControl = new ThemeGradientControl();
        themeGradientControl.drawIconKeyPress(getContext(), key, canvas, themeModel);
    }


    protected void drawIcon(@Nonnull final Canvas canvas, @Nonnull final Drawable icon,
                            final int x, final int y, final int width, final int height) {
        if (icon instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
            if (bitmap != null && paintIconFilter != null) {
                int offX = (width - bitmap.getWidth()) / 2;
                int offY = (height - bitmap.getHeight()) / 2;
                canvas.drawRect(x + offX, y + offY, x + offX + bitmap.getWidth(), y + offY + bitmap.getHeight(), paintIcon);
                canvas.drawBitmap(bitmap, x + offX, y + offY, paintIconFilter);
            }

        } else {
            icon.setBounds(x, y, x + width, y + height);
            canvas.drawRect(icon.getBounds(), paintIcon);
            icon.draw(canvas);
        }
    }


    //    // Draw key background.
    protected void onDrawKeyBackground(@Nonnull final Key key, @Nonnull final Canvas canvas,
                                       @Nonnull final Drawable background) {
        final int keyWidth = key.getDrawWidth();
        final int keyHeight = key.getHeight();
        final int bgWidth, bgHeight, bgX, bgY;
        if (key.needsToKeepBackgroundAspectRatio(mDefaultKeyLabelFlags)
                // HACK: To disable expanding normal/functional key background.
                && !key.hasCustomActionLabel()) {
            final int intrinsicWidth = background.getIntrinsicWidth();
            final int intrinsicHeight = background.getIntrinsicHeight();
            final float minScale = Math.min(
                    keyWidth / (float) intrinsicWidth, keyHeight / (float) intrinsicHeight);
            bgWidth = (int) (intrinsicWidth * minScale);
            bgHeight = (int) (intrinsicHeight * minScale);
            bgX = (keyWidth - bgWidth) / 2;
            bgY = (keyHeight - bgHeight) / 2;
        } else {
            final Rect padding = mKeyBackgroundPadding;
            bgWidth = keyWidth + padding.left + padding.right;
            bgHeight = keyHeight + padding.top + padding.bottom;
            bgX = -padding.left;
            bgY = -padding.top;
        }
        final Rect bounds = background.getBounds();
        if (bgWidth != bounds.right || bgHeight != bounds.bottom) {
            background.setBounds(key.getX(), key.getY(), key.getX() + key.getWidth(), key.getY() + key.getHeight());
            // background.setAlpha(60);
        }

        background.draw(canvas);

    }


    public Paint newLabelPaint(@Nullable final Key key) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        if (key == null) {
            paint.setTypeface(mKeyDrawParams.mTypeface);
            paint.setTextSize(mKeyDrawParams.mLabelSize);
        } else {
            paint.setColor(key.selectTextColor(mKeyDrawParams));
            paint.setTypeface(key.selectTypeface(mKeyDrawParams));
            paint.setTextSize(key.selectTextSize(mKeyDrawParams));
        }
        return paint;
    }

    /**
     * Requests a redraw of the entire keyboard. Calling {@link #invalidate} is not sufficient
     * because the keyboard renders the keys to an off-screen buffer and an invalidate() only
     * draws the cached buffer.
     *
     * @see #invalidateKey(Key)
     */
    public void invalidateAllKeys() {
        mInvalidatedKeys.clear();
        mInvalidateAllKeys = true;
        invalidate();
    }


    public void postInvalidateAllKeys() {
        mInvalidatedKeys.clear();
        mInvalidateAllKeys = true;
        invalidate(0, 0, 500, 500);

    }

    /**
     * Invalidates a key so that it will be redrawn on the next repaint. Use this method if only
     * one key is changing it's content. Any changes that affect the position or size of the key
     * may not be honored.
     *
     * @param key key in the attached {@link Keyboard}.
     * @see #invalidateAllKeys
     */
    public void invalidateKey(@Nullable final Key key) {
        if (mInvalidateAllKeys || key == null) {
            return;
        }
        mInvalidatedKeys.add(key);
        final int x = key.getX() + getPaddingLeft();
        final int y = key.getY() + getPaddingTop();
        invalidate(x, y, x + key.getWidth(), y + key.getHeight());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        freeOffscreenBuffer();
    }

    public void deallocateMemory() {
        freeOffscreenBuffer();
    }

    protected void loadKeyFont() {
        keyFont = mPrefs.getString(Constant.USING_FONT, Constant.FONT_NORMAL);
        charSequenceFont = itemFont.getFont(keyFont);
        getIsUsingLanguageKeyboardOtherQwerty();
    }

    protected void getIsUsingLanguageKeyboardOtherQwerty() {
        isUsingLanguageKeyboardOtherQwerty = Constant.LIST_LANGUAGE_KEYBOARD_OTHER_QWERTY.contains(Settings.getLanguageKeyBoardCurrent());
    }

    public void setThemeModel(ThemeModel themeModel) {
        this.themeModel = themeModel;
        invalidate();
    }

    public void setColorMenu(String colorMenu) {
        if (themeModel != null && themeModel.getMenuBar() != null) {
            if (App.getInstance().typeEditing == Constant.TYPE_EDIT_CUSTOMIZE) {
                themeModel.getMenuBar().setIconColor(colorMenu);
            }
        }
    }

    public void setEffect(String effect) {
        themeModel.setEffect(effect);
    }

    public void changeEffect(String effect) {
        //   Timber.d("ducNQchangeEffect " + effect);
        //   Timber.d("ducNQchangeEffect " + App.getInstance().getTypeEditing());
        if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE) {
            themeModel.setEffect(effect);
            drawableEffectClick = CommonUtil.getDrawableEffectClick(App.getInstance(), effect);
            //   Timber.d("ducNQ : ducNQchangeEffect: "+drawableEffectClick);
        }
    }

    public void setSearchGif(boolean searchGif) {
        Timber.d("ducNQ : setSearchGif: " + isSearchGif);
        isSearchGif = searchGif;
    }

}
