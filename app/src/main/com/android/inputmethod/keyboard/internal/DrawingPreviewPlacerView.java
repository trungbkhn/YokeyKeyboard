/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.inputmethod.keyboard.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.latin.common.CoordinateUtils;
import com.tapbi.spark.yokey.feature.ThemeGradientControl;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.util.DisplayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public final class DrawingPreviewPlacerView extends RelativeLayout {
    private final int[] mKeyboardViewOrigin = CoordinateUtils.newInstance();
    private ThemeGradientControl themeGradientControl;
    private final ArrayList<AbstractDrawingPreview> mPreviews = new ArrayList<>();

    public DrawingPreviewPlacerView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public void setHardwareAcceleratedDrawingEnabled(final boolean enabled) {
        if (!enabled) return;
        final Paint layerPaint = new Paint();
        layerPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        setLayerType(LAYER_TYPE_HARDWARE, layerPaint);
    }

    public void addPreview(final AbstractDrawingPreview preview) {
        if (mPreviews.indexOf(preview) < 0) {
            mPreviews.add(preview);
        }
    }

    public void setKeyboardViewGeometry(final int[] originCoords, final int width,
                                        final int height) {
        CoordinateUtils.copy(mKeyboardViewOrigin, originCoords);
        final int count = mPreviews.size();
        for (int i = 0; i < count; i++) {
            mPreviews.get(i).setKeyboardViewGeometry(originCoords, width, height);
        }
    }

    public void deallocateMemory() {
        final int count = mPreviews.size();
        for (int i = 0; i < count; i++) {
            mPreviews.get(i).onDeallocateMemory();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        deallocateMemory();
    }

//    @Override
//    public void onDraw(final Canvas canvas) {
//        super.onDraw(canvas);
//        final int originX = CoordinateUtils.x(mKeyboardViewOrigin);
//        final int originY = CoordinateUtils.y(mKeyboardViewOrigin);
//        canvas.translate(originX, originY);
//        final int count = mPreviews.size();
//        for (int i = 0; i < count; i++) {
//            mPreviews.get(i).drawPreview(canvas);
//        }
//        canvas.translate(-originX, -originY);
//    }


    public void setKeyPreview(Key key, Paint paintPreview, Paint paint, float keyRadius, ThemeModel mThemeModel, boolean mWithPreview) {
        this.key = key;
        this.paintPreview = paintPreview;
        this.paint = paint;
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        this.keyRadius = keyRadius;
        themeModel = mThemeModel;
        withPreview = mWithPreview;
        invalidate();
    }

    private Key key;
    private Paint paintPreview;
    private Paint paint;
    private float keyRadius;
    private ThemeModel themeModel;
    private boolean withPreview;
    private Map<String, Bitmap> previewPopup = new HashMap<>();


    @Override
    public void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        final int originX = CoordinateUtils.x(mKeyboardViewOrigin);
        final int originY = CoordinateUtils.y(mKeyboardViewOrigin);
        canvas.translate(originX, originY);
        final int count = mPreviews.size();
//        for (int i = 0; i < count; i++) {
//            mPreviews.get(i).drawPreview(canvas);
//        }
//        for (AbstractDrawingPreview mPreview : mPreviews) {
//            mPreview.drawPreview(canvas);
//        }

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());


        if (key != null && key.ismPressed() && mPrefs.getBoolean(com.android.inputmethod.latin.settings.Settings.PREF_POPUP_ON, true)) {
            if (themeModel.getTypeKeyboard() != null && themeModel.getTypeKeyboard().equalsIgnoreCase("rgb")) {
                drawPreview(key, canvas);
            } else {
                //drawPreviewNotRGB(key, canvas);
            }

        }

        if (key != null && key.ismPressed() && withPreview && !key.noKeyPreview()) {
            Timber.e("hachung drawKeyWhenPress: " + withPreview);
            drawKeyWhenPress(key, canvas);
        }
        canvas.translate(-originX, -originY);
    }


    private void drawPreviewNotRGB(final Key key, final Canvas canvas) {
        //String strPathIconKeyText = CommonUtil.getPathImageForPreviewPressKey(getContext(),themeModel.getId(),themeModel.getPopup().getPreview().getBgImage());
        String strPathIconKeyText = CommonUtil.getPathImage(getContext(), themeModel, key, themeModel.getKey().getText().getPressed());
        Bitmap bitmap = BitmapFactory.decodeFile(strPathIconKeyText);

        if (bitmap != null) {
            canvas.drawBitmap(bitmap, key.getX(), key.getY() - key.getHeight() - 10, paintPreview);
        }

        // The gesture threshold expressed in dip
        float GESTURE_THRESHOLD_DIP = 23.0f;
        // Convert the dips to pixels
        float scale = getContext().getResources().getDisplayMetrics().density;
        float sizeText = (int) (GESTURE_THRESHOLD_DIP * scale + 0.5f);

        String label = key.getLabel();
        if (label != null) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(sizeText);

            paint.setColor(CommonUtil.hex2decimal(themeModel.getPopup().getPreview().getTextColor()));
            canvas.drawText(label, key.getX() + key.getWidth() / 2f, key.getY() - key.getHeight() / 3f - 10, paint);
        }

    }

    private void drawKeyWhenPress(final Key key, final Canvas canvas) {
        String strPathIconKeyText = CommonUtil.getPathImage(getContext(), themeModel, key, themeModel.getKey().getText().getPressed());
        Bitmap bitmap = BitmapFactory.decodeFile(strPathIconKeyText);
        if (bitmap != null) {
            int widthScreen = CommonUtil.getScreenWidth();
            int heightScreen = CommonUtil.getScreenHeight();
            if (widthScreen < heightScreen) {
                canvas.drawBitmap(bitmap, key.getX(), key.getY(), paintPreview);
            }
        }

        // The gesture threshold expressed in dip
//        float GESTURE_THRESHOLD_DIP = 25.0f;
        // Convert the dips to pixels
//        float scale = getContext().getResources().getDisplayMetrics().density;
//        float sizeText = (int) (GESTURE_THRESHOLD_DIP * scale + 0.5f);

//        String label = key.getLabel();
//        if (label != null) {
//            paint.setTextAlign(Paint.Align.CENTER);
//            paint.setTextSize(sizeText);
//            paint.setColor(CommonUtil.hex2decimal(themeModel.getPopup().getPreview().getTextColor()));
//            float ratio = 0.2f;
//            float keyOffsetX = key.getWidth() * ratio;
//            float keyOffsetY = key.getHeight() * ratio;
//            float offsetY = (keyOffsetY + key.getHeight());
//            canvas.drawText(label, key.getX() + key.getWidth() / 2f, key.getY() - key.getHeight() / 2f + offsetY, paint);
//        } else if (key.getCode() != Constants.CODE_SPACE) {
//        }
    }


    /**
     * drawPreview led
     */
    private void drawPreview(final Key key, final Canvas canvas) {
        LinearGradient linearGradient = new LinearGradient(0, 0, 0, 0,
                new int[]{Color.BLACK, Color.BLACK}, null, Shader.TileMode.REPEAT);
        paintPreview.setStyle(Paint.Style.FILL_AND_STROKE);
        float ratio = 0.2f;
        float keyOffsetX = key.getWidth() * ratio;
        float keyOffsetY = key.getHeight() * ratio;
        float offsetY = (keyOffsetY + key.getHeight());
        float left = key.getX() - keyOffsetX;
        float right = key.getX() + key.getWidth() + keyOffsetX;
        if (left <= 0) {
            left = key.getX();
        }
        boolean checkRight = false;
        if (right >= DisplayUtils.getScreenWidth()) {
            checkRight = true;
            right = DisplayUtils.getScreenWidth() - keyOffsetX;
        }
        RectF rectF = new RectF(left, key.getY() - offsetY - keyOffsetY,
                right, key.getY() + key.getHeight() - offsetY + keyOffsetY);
        canvas.drawRoundRect(rectF,
                keyRadius, keyRadius, paintPreview);
        paintPreview.setStyle(Paint.Style.STROKE);
        String label = key.getLabel();
        if (label != null) {
            Shader shader = paint.getShader();
            paint.setShader(linearGradient);
            float size = paint.getTextSize() * (1 + ratio);
            paint.setTextSize(size);
            float xAxis = key.getX() + key.getWidth() / 2f;
            if (checkRight) {
                xAxis = key.getX() + rectF.width() / 3f;
            }
            canvas.drawText(label, 0, 1, xAxis, key.getY() + key.getHeight() / 2f - offsetY, paint);
            paint.setShader(shader);
            size = paint.getTextSize() / (1 + ratio);
            paint.setTextSize(size);
        }
    }

}
