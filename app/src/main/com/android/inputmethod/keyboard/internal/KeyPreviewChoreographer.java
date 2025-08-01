/*
 * Copyright (C) 2014 The Android Open Source Project
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

import static com.tapbi.spark.yokey.util.CommonUtil.checkFileHDPI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.latin.common.CoordinateUtils;
import com.android.inputmethod.latin.utils.ViewLayoutUtils;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.Constant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

/**
 * This class controls pop up key previews. This class decides:
 * - what kind of key previews should be shown.
 * - where key previews should be placed.
 * - how key previews should be shown and dismissed.
 */
public final class KeyPreviewChoreographer {
    // Free {@link KeyPreviewView} pool that can be used for key preview.
    private final ArrayDeque<KeyPreviewView> mFreeKeyPreviewViews = new ArrayDeque<>();
    // Map from {@link Key} to {@link KeyPreviewView} that is currently being displayed as key
    // preview.

    private final HashMap<Key, KeyPreviewView> mShowingKeyPreviewViews = new HashMap<>();
    private final KeyPreviewDrawParams mParams;
    private ThemeModel themeModel;
    private Bitmap bmTextTheme6001 = null;

    public KeyPreviewChoreographer(final KeyPreviewDrawParams params) {
        mParams = params;
    }


    public KeyPreviewView getKeyPreviewView(final Key key, final ViewGroup placerView) {
        KeyPreviewView keyPreviewView = mShowingKeyPreviewViews.remove(key);
        final Context context = placerView.getContext();
        int colorText = Color.WHITE;
        Drawable bgText = null;
        String path = "key/1001/xxhdpi/btn_key_text.png";
        String pathStorage = null;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        if (themeModel != null) {
            if (themeModel.getId() != null && !themeModel.getId().isEmpty()) {
                // TODO: chungvv update local
                if (Long.parseLong(themeModel.getId()) > 6010 && Long.parseLong(themeModel.getId()) < 6030
                        || Long.parseLong(themeModel.getId()) > 3015 && Long.parseLong(themeModel.getId()) < 4000
                        || Long.parseLong(themeModel.getId()) > 4012 && Long.parseLong(themeModel.getId()) < 5000
                        || Long.parseLong(themeModel.getId()) > 2003 && Long.parseLong(themeModel.getId()) < 3000
                ) {
                    path = "themes/" + themeModel.getId() + checkFileHDPI(context) + "btn_key_text.png";
                }
            } else {
                path = "key/" + themeModel.getTypeKey() + checkFileHDPI(context) + "btn_key_text.png";
            }
            if (themeModel.getKey() != null && themeModel.getKey().getText() != null
                    && themeModel.getKey().getText().getTextColor() != null && !themeModel.getKey().getText().getTextColor().isEmpty()) {
                colorText = CommonUtil.hex2decimal(themeModel.getKey().getText().getTextColor());
            }

            if (themeModel.getTypeKey() == 3004 || themeModel.getTypeKey() == 3006) {
                pathStorage = Objects.requireNonNull(App.getInstance().file).toString() + "/" + themeModel.getTypeKey() + checkFileHDPI(context) + "popupkey.png";
            } else {
                if (themeModel.getPopup() != null && themeModel.getPopup().getMinKeyboard() != null) {
                    if (themeModel.getPopup().getMinKeyboard().getBgImage() != null && !themeModel.getPopup().getMinKeyboard().getBgImage().isEmpty()) {
                        String bgImage = themeModel.getPopup().getMinKeyboard().getBgImage();
                        pathStorage = CommonUtil.getPathImage(context, themeModel, key, bgImage);
                        if (!bgImage.equals("btn_key_text.png") && !bgImage.equals("btn_key_text.9.png")) {
                            if (themeModel.getPopup().getMinKeyboard().getTextColor() != null && !themeModel.getPopup().getMinKeyboard().getTextColor().isEmpty()) {
                                colorText= CommonUtil.hex2decimal(themeModel.getPopup().getMinKeyboard().getTextColor());
                            }
                        }
                    }

                } else {
                    pathStorage = CommonUtil.getPathImage(context, themeModel, key, Objects.requireNonNull(Objects.requireNonNull(themeModel.getKey()).getText()).getPressed());
                }

            }

            Timber.d("keyPreviewView 30");
        }
        if (App.getInstance().themeRepository != null) {
            if (path.contains("6001") && path.contains("btn_key_text")) {////Long.parseLong(Objects.requireNonNull(themeModel.getId()))>6000
                Bitmap bitmap = null;
                if (bmTextTheme6001 != null) {
                    if (bmTextTheme6001.getWidth() == key.getWidth() || bmTextTheme6001.getHeight() == key.getHeight()) {
                        bitmap = bmTextTheme6001;
                    }
                }
                if (bitmap == null) {
                    InputStream istr = null;
                    try {
                        istr = context.getAssets().open(path);
                        bitmap = BitmapFactory.decodeStream(istr);

                        if (key.getWidth() < key.getHeight()) {
                            int height = bitmap.getHeight() * key.getWidth() / bitmap.getWidth();
                            bitmap = Bitmap.createScaledBitmap(bitmap, key.getWidth(), height, true);
                        } else {
                            int width = bitmap.getWidth() * key.getHeight() / bitmap.getHeight();
                            bitmap = Bitmap.createScaledBitmap(bitmap, width, key.getHeight(), true);
                        }
                        bmTextTheme6001 = bitmap;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bitmap != null) {
                    bgText = new BitmapDrawable(context.getResources(), bitmap);
                }
            } else {
                if (App.getInstance().themeRepository.mapKeyDrawablePreview.containsKey(path)) {
                    bgText = App.getInstance().themeRepository.mapKeyDrawablePreview.get(path);
                } else if (pathStorage != null && App.getInstance().themeRepository.mapKeyDrawablePreview.containsKey(pathStorage)) {
                    bgText = App.getInstance().themeRepository.mapKeyDrawablePreview.get(pathStorage);
                } else {
                    if (pathStorage != null) {
                        if (new File(pathStorage).exists()) {
                            bgText = Drawable.createFromPath(pathStorage);
                            App.getInstance().themeRepository.mapKeyDrawablePreview.remove(pathStorage);
                            App.getInstance().themeRepository.mapKeyDrawablePreview.put(pathStorage, bgText);
                        } else {
                            if (pathStorage.startsWith(Constant.FOLDER_ASSET)) {
                                pathStorage = pathStorage.replaceFirst(Constant.FOLDER_ASSET, "");
                            }
                            bgText = getDrawableAsset(bgText, pathStorage);
                        }
                    } else {
                        bgText = getDrawableAsset(bgText, path);
                    }
                }
            }
        }
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;
        if (width > height) {
            height = metrics.widthPixels;
            width = metrics.heightPixels;
        }
        if (keyPreviewView != null) {
            setMeasurePreview(path, keyPreviewView, width, height, bgText, colorText);
            return keyPreviewView;
        }
        keyPreviewView = mFreeKeyPreviewViews.poll();
        if (keyPreviewView != null) {
            setMeasurePreview(path, keyPreviewView, width, height, bgText, colorText);
            return keyPreviewView;
        }
        keyPreviewView = new KeyPreviewView(context, null /* attrs */);
        //ColorKeyboard Convert width height popup preview from DP to Pixel for device
        // Todo: Show preview key when press ..Duongcv
        final float scale = context.getResources().getDisplayMetrics().density;
        setMeasurePreview(path, keyPreviewView, width, height, bgText, colorText);
        placerView.addView(keyPreviewView, ViewLayoutUtils.newLayoutParam(placerView, 0, 0));
        return keyPreviewView;
    }

    private void setMeasurePreview(String path, KeyPreviewView keyPreviewView, int width, int height, Drawable bgText, int colorText) {
        if (bgText != null) keyPreviewView.setBackground(bgText);
        if (themeModel != null && (themeModel.getTypeKey() == 3004 || (themeModel.getTypeKey() == 3006))) {
            keyPreviewView.setTextColor(Color.WHITE);
        } else {
            keyPreviewView.setTextColor(colorText);
        }
        if (path.contains("6001") && path.contains("btn_key_text") && bmTextTheme6001 != null) {
            keyPreviewView.setWidth((int) bmTextTheme6001.getWidth());
            keyPreviewView.setHeight((int) bmTextTheme6001.getHeight());
        } else {
            if (themeModel != null && (themeModel.getTypeKey() == 3004
                    || (themeModel.getTypeKey() == 3006)
                    || (themeModel.getTypeKey() == 6011)
                    || (themeModel.getTypeKey() == 6014))) {
                keyPreviewView.setWidth((int) (height * 0.07));
            } else {
                keyPreviewView.setWidth((int) (width * 0.095));//key.getWidth()
            }
            keyPreviewView.setHeight((int) (height * 0.07));
        }
    }

    private Drawable getDrawableAsset(Drawable bgText, String path) {
        try {
            if (path == null) return bgText;
            InputStream input = App.getInstance().getAssets().open(path);
            if (input == null) return bgText;
            try {
                bgText = Drawable.createFromStream(input, null);
            } catch (NullPointerException e) {
                Timber.e("Duongcv " + e.getMessage());
            }

            App.getInstance().themeRepository.mapKeyDrawablePreview.remove(path);
            App.getInstance().themeRepository.mapKeyDrawablePreview.put(path, bgText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bgText;
    }

    public boolean isShowingKeyPreview(final Key key) {
        return mShowingKeyPreviewViews.containsKey(key);
    }


    private void placeKeyPreview(final Key key, final KeyPreviewView keyPreviewView,
                                 final KeyboardIconsSet iconsSet, final KeyDrawParams drawParams,
                                 final int keyboardViewWidth, final int[] originCoords) {
        keyPreviewView.setPreviewVisual(key, iconsSet, drawParams);
        keyPreviewView.measure(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mParams.setGeometry(keyPreviewView);
        final int previewWidth = keyPreviewView.getMeasuredWidth();
        final int previewHeight = keyPreviewView.getMeasuredHeight();//mParams.mPreviewHeight;
        final int keyDrawWidth = key.getWidth();
        // The key preview is horizontally aligned with the center of the visible part of the
        // parent key. If it doesn't fit in this {@link KeyboardView}, it is moved inward to fit and
        // the left/right background is used if such background is specified.
        final int keyPreviewPosition;
        int previewX = key.getDrawX() - (previewWidth - keyDrawWidth) / 2
                + CoordinateUtils.x(originCoords);
        if (previewX < 0) {
            previewX = key.getWidth() / 5;
            keyPreviewPosition = KeyPreviewView.POSITION_LEFT;
        } else if (previewX > keyboardViewWidth - previewWidth) {
            previewX = keyboardViewWidth - previewWidth;
            keyPreviewPosition = KeyPreviewView.POSITION_RIGHT;
        } else {
            keyPreviewPosition = KeyPreviewView.POSITION_MIDDLE;
        }
        final boolean hasMoreKeys = (key.getMoreKeys() != null);
        keyPreviewView.setPreviewBackground(hasMoreKeys, keyPreviewPosition);
        //  keyPreviewView.setTextSize(key.selectTextSize(drawParams));
        // The key preview is placed vertically above the top edge of the parent key with an
        // arbitrary offset.
        final int previewY = key.getY() - previewHeight
                + CoordinateUtils.y(originCoords);

        ViewLayoutUtils.placeViewAt(
                keyPreviewView, previewX, previewY, previewWidth, previewHeight);
        keyPreviewView.setPivotX(previewWidth);
        keyPreviewView.setPivotY(previewHeight);
    }

    public void dismissKeyPreview(final Key key, final boolean withAnimation) {
        if (key == null) {
            return;
        }
        final KeyPreviewView keyPreviewView = mShowingKeyPreviewViews.get(key);
        if (keyPreviewView == null) {
            return;
        }
        final Object tag = keyPreviewView.getTag();
        if (withAnimation) {
            if (tag instanceof KeyPreviewAnimators) {
                final KeyPreviewAnimators animators = (KeyPreviewAnimators) tag;
                animators.startDismiss();
                return;
            }
        }
        // Dismiss preview without animation.
        mShowingKeyPreviewViews.remove(key);
        if (tag instanceof Animator) {
            ((Animator) tag).cancel();
        }
        keyPreviewView.setTag(null);
        keyPreviewView.setVisibility(View.INVISIBLE);
        mFreeKeyPreviewViews.add(keyPreviewView);
    }

    public void placeAndShowKeyPreview(final Key key, final KeyboardIconsSet iconsSet,
                                       final KeyDrawParams drawParams, final int keyboardViewWidth, final int[] keyboardOrigin,
                                       final ViewGroup placerView, final boolean withAnimation, ThemeModel themeModel) {
        this.themeModel = themeModel;
        final KeyPreviewView keyPreviewView = getKeyPreviewView(key, placerView);
        placeKeyPreview(key, keyPreviewView, iconsSet, drawParams, keyboardViewWidth, keyboardOrigin);
        showKeyPreview(key, keyPreviewView, withAnimation);
//        keyPreviewView.setTextColors(CommonUtil.hex2decimal(Objects.requireNonNull(Objects.requireNonNull(themeModel.getKey()).getText()).getTextColor()));
//
//        if (Objects.equals(themeModel.getTypeKeyboard(), Constants.ID_CATEGORY_COLOR) || Objects.equals(themeModel.getTypeKeyboard(), Constants.ID_CATEGORY_WALL)) {
//            keyPreviewView.setColorsRect(ColorUtils.blendARGB(CommonUtil.hex2decimal(Objects.requireNonNull(themeModel.getBackground()).getBackgroundColor()), Color.BLACK, 0.1f));
//            keyPreviewView.setType(Constants.ID_CATEGORY_COLOR);
//        } else if (Objects.equals(themeModel.getTypeKeyboard(), Constants.ID_CATEGORY_GRADIENT)) {
//            keyPreviewView.setColorRectGradient(ColorUtils.blendARGB(CommonUtil.hex2decimal(Objects.requireNonNull(themeModel.getBackground()).getFinishColor()), Color.BLACK, 0.1f),
//                    ColorUtils.blendARGB(CommonUtil.hex2decimal(Objects.requireNonNull(themeModel.getBackground()).getStartColor()), Color.BLACK, 0.1f));
//            keyPreviewView.setType(Constants.ID_CATEGORY_GRADIENT);
//        }
//

    }

    void showKeyPreview(final Key key, final KeyPreviewView keyPreviewView,
                        final boolean withAnimation) {
        if (!withAnimation) {
            keyPreviewView.setVisibility(View.VISIBLE);
            mShowingKeyPreviewViews.put(key, keyPreviewView);
            return;
        }

        // Show preview with animation.
        final Animator showUpAnimator = createShowUpAnimator(key, keyPreviewView);
        final Animator dismissAnimator = createDismissAnimator(key, keyPreviewView);
        final KeyPreviewAnimators animators = new KeyPreviewAnimators(
                showUpAnimator, dismissAnimator);
        keyPreviewView.setTag(animators);
        animators.startShowUp();
    }

    public Animator createShowUpAnimator(final Key key, final KeyPreviewView keyPreviewView) {
        final Animator showUpAnimator = mParams.createShowUpAnimator(keyPreviewView);
        showUpAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(final Animator animator) {
                showKeyPreview(key, keyPreviewView, false /* withAnimation */);
            }
        });
        return showUpAnimator;
    }

    private Animator createDismissAnimator(final Key key, final KeyPreviewView keyPreviewView) {
        final Animator dismissAnimator = mParams.createDismissAnimator(keyPreviewView);
        dismissAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animator) {
                dismissKeyPreview(key, false /* withAnimation */);
            }
        });
        return dismissAnimator;
    }

    private static class KeyPreviewAnimators extends AnimatorListenerAdapter {
        private final Animator mShowUpAnimator;
        private final Animator mDismissAnimator;

        public KeyPreviewAnimators(final Animator showUpAnimator, final Animator dismissAnimator) {
            mShowUpAnimator = showUpAnimator;
            mDismissAnimator = dismissAnimator;
        }

        public void startShowUp() {
            mShowUpAnimator.start();
        }

        public void startDismiss() {
            if (mShowUpAnimator.isRunning()) {
                mShowUpAnimator.addListener(this);
                return;
            }
            mDismissAnimator.start();
        }

        @Override
        public void onAnimationEnd(final Animator animator) {
            mDismissAnimator.start();
        }
    }
}
