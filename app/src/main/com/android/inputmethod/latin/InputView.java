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

package com.android.inputmethod.latin;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;

import com.android.inputmethod.accessibility.AccessibilityUtils;
import com.android.inputmethod.keyboard.MainKeyboardView;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.settings.Settings;
import com.android.inputmethod.latin.suggestions.MoreSuggestionsView;
import com.android.inputmethod.latin.suggestions.SuggestionStripView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.common.CommonVariable;
import com.tapbi.spark.yokey.data.model.MessageEvent;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.Constant;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Objects;

import timber.log.Timber;

public final class InputView extends FrameLayout {
    private final Rect mInputViewRect = new Rect();
    private final int colorNotUse = Color.parseColor("#1C1C1C");
    public ConstraintLayout ctlOpenSetting;
    public TextView tvEnabelKeyboard;
    private MainKeyboardView mMainKeyboardView;
    private SuggestionStripView suggestionStripView;
    private KeyboardTopPaddingForwarder mKeyboardTopPaddingForwarder;
    private MoreSuggestionsViewCanceler mMoreSuggestionsViewCanceler;
    private MotionEventForwarder<?, ?> mActiveForwarder;
    // private ConstraintLayout cltFullKeyboard;
    private ImageView imgBackground;
    private boolean isLoadBitmap;
    private int colorIcon;
    private GradientDrawable gdBackground = null;
    private int colorUse = Color.parseColor("#000000");
    private int colorIconOrigin = Color.WHITE;
    private int dominantColor = Color.BLACK;
    private String pathImageBg = "";
    private SharedPreferences mPrefs;
    private Bitmap bmBackground;
    private int colorBackground = 0;
    private boolean isSearchEmoji;
    private ImageView imgBackSearch;
    private Group layoutSearch;
    private EditText edtSearchEmoji;
    private TextView txtNotSupportFont;

    public InputView(final Context context, final AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public static int adjustColorIfDark(int color) {
        if (isColorDark(color)) {
            return Color.WHITE;
        } else {
            return color;
        }
    }

    public static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        suggestionStripView = findViewById(R.id.suggestion_strip_view);
        suggestionStripView.setBackgroundColor(Color.TRANSPARENT);
        mMainKeyboardView = (MainKeyboardView) findViewById(R.id.keyboard_view);
        mMainKeyboardView.setBackgroundColor(Color.TRANSPARENT);
        imgBackground = findViewById(R.id.imgBackgroundKeyboard);
        txtNotSupportFont = findViewById(R.id.txtNotSupportFont);
        ctlOpenSetting = findViewById(R.id.ctlOpenSetting);
        tvEnabelKeyboard = findViewById(R.id.tvEnableKeyboard);
        // cltFullKeyboard = findViewById(R.id.cltFullKeyboard);
        initLayoutSearchGif();
        mKeyboardTopPaddingForwarder = new KeyboardTopPaddingForwarder(mMainKeyboardView, suggestionStripView);
        mMoreSuggestionsViewCanceler = new MoreSuggestionsViewCanceler(mMainKeyboardView, suggestionStripView);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public boolean isShowMenu() {
        if (suggestionStripView != null && suggestionStripView.isShowMenu()) {
            return true;
        }

        return false;
    }

    public void setTextNoti() {
        if (txtNotSupportFont != null) {
            txtNotSupportFont.setText(R.string.the_maximum_number_of_characters_has_been_reached);
        }
    }

    public void showCustomToast() {
        if (txtNotSupportFont != null) {
            txtNotSupportFont.setVisibility(VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (txtNotSupportFont != null) {
                        Timber.d("ducNQ : runsgerg: ");
                        txtNotSupportFont.setVisibility(INVISIBLE);
                    }
                }
            }, 1000);
        }
    }

    public void setKeyboardTopPadding(final int keyboardTopPadding) {
        mKeyboardTopPaddingForwarder.setKeyboardTopPadding(keyboardTopPadding);
    }

    @Override
    protected boolean dispatchHoverEvent(final MotionEvent event) {
        if (AccessibilityUtils.getInstance().isTouchExplorationEnabled()
                && mMainKeyboardView.isShowingMoreKeysPanel()) {
            // With accessibility mode on, discard hover events while a more keys keyboard is shown.
            // The {@link MoreKeysKeyboard} receives hover events directly from the platform.
            return true;
        }
        return super.dispatchHoverEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent me) {
        final Rect rect = mInputViewRect;
        getGlobalVisibleRect(rect);
        final int index = me.getActionIndex();
        final int x = (int) me.getX(index) + rect.left;
        final int y = (int) me.getY(index) + rect.top;

        // The touch events that hit the top padding of keyboard should be forwarded to
        // {@link SuggestionStripView}.
        if (mKeyboardTopPaddingForwarder.onInterceptTouchEvent(x, y, me)) {
            mActiveForwarder = mKeyboardTopPaddingForwarder;
            return true;
        }

        // To cancel {@link MoreSuggestionsView}, we should intercept a touch event to
        // {@link MainKeyboardView} and dismiss the {@link MoreSuggestionsView}.
        if (mMoreSuggestionsViewCanceler.onInterceptTouchEvent(x, y, me)) {
            mActiveForwarder = mMoreSuggestionsViewCanceler;
            return true;
        }

        mActiveForwarder = null;
        return false;
    }

    public void updateSuggestionBySubtype() {
        if (suggestionStripView != null) {
//            suggestionStripView.setSwitchSubtype(true);
        }

    }

    @Override
    public boolean onTouchEvent(final MotionEvent me) {
        if (mActiveForwarder == null) {
            return super.onTouchEvent(me);
        }

        final Rect rect = mInputViewRect;
        getGlobalVisibleRect(rect);
        final int index = me.getActionIndex();
        final int x = (int) me.getX(index) + rect.left;
        final int y = (int) me.getY(index) + rect.top;
        return mActiveForwarder.onTouchEvent(x, y, me);
    }

    public MainKeyboardView getMainKeyboardView() {
        return mMainKeyboardView;
    }

    public void changeShowLayoutMenu(int status) {
        suggestionStripView.setVisibility(status);
    }

    public void showHideSettingView(boolean isShow, boolean resetScroll, boolean needUpdateSettingNumber) {
//        if (settingMenuWrap != null) {
//            if (isShow) {
//                if (needUpdateSettingNumber) {
//                    currentSettingShowNumber = mPrefs.getBoolean(Constant.ACTION_SHOW_ROW_NUMBER, false);
//                }
//                settingMenuWrap.setVisibility(VISIBLE);
//                updateWhenShowSetting();
//                if (resetScroll) {
//                    settingMenuWrap.findViewById(R.id.layout_keyboard_setting_menu_scroll).post(() -> settingMenuWrap.scrollTo(0, 0));
//                }
//            } else {
//                settingMenuWrap.setVisibility(GONE);
//                if (currentSettingShowNumber != mPrefs.getBoolean(Constant.ACTION_SHOW_ROW_NUMBER, false)) {
//                    mPrefs.edit().putBoolean(Constant.ACTION_SHOW_ROW_NUMBER, currentSettingShowNumber).apply();
//                    EventBus.getDefault().post(new MessageEvent(Constant.EVENT_UPDATE_SETTING_VALUES));
//                }
//            }
//        }
    }

    public void setupBackgroundKeyboard(String color, String image) {
        if (mMainKeyboardView != null) {
            boolean checkRequest = App.getInstance().mPrefs.getBoolean(Constants.CHECK_REQUESTLAYOUT_MAINKEYBOARD, false);
            if (mMainKeyboardView.isLayoutRequested() || checkRequest) {
                updateBackgroundKeyboard(color, image);
                App.getInstance().mPrefs.edit().putBoolean(Constants.CHECK_REQUESTLAYOUT_MAINKEYBOARD, false).apply();
            } else {
                post(() -> {
                    Timber.d("ducNQ : setupBackgroundKeyboardaa: 2");
                    updateBackgroundKeyboard(color, image);
                });

            }
        }
    }

    // TODO: 4/10/2025 chungvv:  updateBackgroundKeyboard
    public void updateBackgroundKeyboard(String color, String image) {
        Timber.e("hachung getTypeEditing:"+App.getInstance().getTypeEditing());
        if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_NONE) {
            isLoadBitmap = false;
            gdBackground = null;
            colorIcon = Color.parseColor("#ffffff");
            colorUse = colorIcon;
            if (!color.equals("null") && image.equals("null")) {
                Timber.d("ducNQ : checked: 1");
                imgBackground.setImageBitmap(null);
                colorIcon = Color.parseColor("#" + color.substring(2));
                dominantColor = colorIcon;
                imgBackground.setBackgroundColor(colorIcon);
                mMainKeyboardView.setDominantColor(colorIcon);
                colorUse = colorIcon;
            } else if (color.equals("null") && !image.equals("null")) {
                isLoadBitmap = true;
                pathImageBg = image;
                Timber.e("hachung image:"+image);
                Glide.with(App.getInstance()).asBitmap().load(image).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Timber.d("ducNQ : checked: 2");
                        imgBackground.setImageBitmap(resource);
                        bmBackground = resource;
                        dominantColor = CommonUtil.getDominantColor(resource);
                        mMainKeyboardView.setDominantColor(dominantColor);
                        updateIcon(colorIcon, resource, dominantColor);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
            } else {
                Timber.d("ducNQ : checked: 3");
                imgBackground.setImageBitmap(null);
                imgBackground.setBackground(null);
                imgBackground.setBackgroundColor(Color.TRANSPARENT);
                ThemeModel themeModel = App.getInstance().themeRepository.getCurrentThemeModel();
                if (themeModel == null) {
                    String id = App.getInstance().mPrefs.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, "0");
                    if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE) {
                        id = Constant.ID_THEME_DEFAULT;
                    }
                    themeModel = CommonUtil.parserJsonFromFileTheme(getContext(), id);
                }
                if (themeModel == null || themeModel.getId() == null) return;
                //ContextWrapper contextWrapper = new ContextWrapper(getContext());
                File file = App.getInstance().appDir;//contextWrapper.getDir(getContext().getFilesDir().getName(), Context.MODE_PRIVATE);
                String strPathIconKeyText = file.toString() + "/" + themeModel.getId() + "/" + themeModel.getBackground().getBackgroundImage();
                if (themeModel.getBackground() != null && themeModel.getBackground().getBackgroundImage() != null) {
                    if (themeModel.getBackground().getBackgroundImage().contains(Constant.FOLDER_ASSET) || themeModel.getBackground().getBackgroundImage().contains(Constant.PATH_FILE_UN_ZIP_THEME_CUSTOMIZE_BACKGROUND)) {
                        strPathIconKeyText = themeModel.getBackground().getBackgroundImage();
                    }
                }
                Timber.e("hachung strPathIconKeyText:"+strPathIconKeyText);
                //Timber.d("updateBackgroundKeyboard "+themeModel.getNameKeyboard());
                Bitmap bm = App.getInstance().themeRepository.getBitmapBackground();
                // Timber.d("ducNQ : updateBackgroundKeyboarded: "+bm);
                colorUse = CommonUtil.hex2decimal(themeModel.getBackground().getStartColor());
                if (themeModel.getTypeKeyboard() != null) {
                    Timber.e("hachung getTypeKeyboard:"+themeModel.getTypeKeyboard());
                    switch (themeModel.getTypeKeyboard()) {

                        case Constants.ID_CATEGORY_GRADIENT:
                            String[] strColorGradientBackground = {themeModel.getBackground().getStartColor(), themeModel.getBackground().getFinishColor()};
                            colorIcon = CommonUtil.hex2decimal(themeModel.getBackground().getStartColor());
                            gdBackground = CommonUtil.getGradientDrawableBackground(strColorGradientBackground);
                            imgBackground.setBackground(CommonUtil.getGradientDrawableBackground(strColorGradientBackground));
                            break;
                        case Constants.ID_FEATURED:
                        case Constants.ID_CATEGORY_WALL:
                            // setBitmapToBackground(strPathIconKeyText, bm, App.getInstance().themeModel);
                            Timber.e("hachung :"+themeModel.getBackground().getBackgroundImage());
                            if (themeModel.getBackground() != null && themeModel.getBackground().getBackgroundImage() != null && !themeModel.getBackground().getBackgroundImage().equals("null")) {
                                setBitmapToBackground(strPathIconKeyText, bm, App.getInstance().themeModel);
                            } else {
                                imgBackground.setBackground(null);
                                colorIcon = CommonUtil.hex2decimal(themeModel.getBackground().getBackgroundColor());
                                imgBackground.setBackgroundColor(colorIcon);
                                mMainKeyboardView.setDominantColor(colorIcon);
                            }
                            break;
                        case Constants.ID_CATEGORY_RGB:
                            if (!themeModel.getBackground().getBackgroundColor().equals("null")) {
                                imgBackground.setBackground(null);
                                colorIcon = CommonUtil.hex2decimal(themeModel.getBackground().getBackgroundColor());
                                imgBackground.setBackgroundColor(colorIcon);
                                mMainKeyboardView.setDominantColor(colorIcon);
                            } else {
                                setBitmapToBackground(strPathIconKeyText, bm, themeModel);
                            }
                            break;
                        default:
                            //Long.parseLong(themeModel.getId()) > 3000 && Long.parseLong(themeModel.getId()) < 3008
                            if (/*Objects.equals(themeModel.getBackground().getBackgroundImage(), "bg_keyboard.jpg")&&*/
                                    Long.parseLong(themeModel.getId()) > 3000 && Long.parseLong(themeModel.getId()) < 3010
                                            || Long.parseLong(themeModel.getId()) > 3018 && Long.parseLong(themeModel.getId()) < 3023) {
                                setBitmapToBackground(strPathIconKeyText, bm, themeModel);
                            } else {
                                colorIcon = CommonUtil.hex2decimal(themeModel.getBackground().getBackgroundColor());

                                imgBackground.setBackgroundColor(colorIcon);
                            }
                            break;
                    }
                }

                if (Long.parseLong(themeModel.getId()) > 3000) {
//                    if (Long.parseLong(themeModel.getId()) == 6009 || Long.parseLong(themeModel.getId()) == 6007) {
//                        colorUse = Color.parseColor(mMainKeyboardView.getThemeModel().getMenuBar().getIconColor());//CommonUtil.hex2decimal(mMainKeyboardView.getThemeModel().getMenuBar().getIconColor());
//                    } else
                    if (mMainKeyboardView.getThemeModel() != null && mMainKeyboardView.getThemeModel().getMenuBar() != null &&
                            mMainKeyboardView.getThemeModel().getMenuBar().getIconColor() != null) {
                        colorUse = CommonUtil.hex2decimal(mMainKeyboardView.getThemeModel().getMenuBar().getIconColor());
                    }
                }
            }


            if (!isLoadBitmap) {
                updateIcon(colorIcon, null, dominantColor);
            }
        } else if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE) {
            color = mMainKeyboardView.getThemeModel().getBackground().getBackgroundColor();

            colorUse = Color.parseColor("#" + mMainKeyboardView.getThemeModel().getMenuBar().getIconColor().substring(2));
            pathImageBg = "";
            if (mMainKeyboardView.getThemeModel().getBackground().getBackgroundImage() != null && !mMainKeyboardView.getThemeModel().getBackground().getBackgroundImage().equals("null")) {
                pathImageBg = mMainKeyboardView.getThemeModel().getBackground().getBackgroundImage();
            } else {//fix bug kill app lose background
                pathImageBg = App.getInstance().linkCurrentBg;
            }
            Timber.e("hachung update background " + pathImageBg + " : " + color);
            if (pathImageBg != null && !pathImageBg.equals("") && !pathImageBg.equals("null")) {
                Glide.with(App.getInstance()).asBitmap().load(pathImageBg).priority(Priority.HIGH).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bmBackground = resource;
                        if (mMainKeyboardView.getThemeModel().getBackground() != null) {
                            if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE) {
                                changeRadius(getContext(), App.getInstance().blurKillApp);
                            } else {
                                changeRadius(getContext(), mMainKeyboardView.getThemeModel().getBackground().getRadiusBlur());
                            }
                        } else {
                            Timber.d("ducNQ : checked: 4");
                            imgBackground.setImageBitmap(resource);
                        }
                        dominantColor = CommonUtil.getDominantColor(resource);
                        mMainKeyboardView.setDominantColor(dominantColor);
                        updateIcon(colorIcon, resource, dominantColor);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        Log.d("TAG", "onLoadCleared: ");
                    }
                });
            } else {
                Timber.d("ducNQ : checked: 5");
                imgBackground.setImageBitmap(null);
                colorIcon = Color.parseColor("#" + color.substring(2));
                dominantColor = colorIcon;
                imgBackground.setBackgroundColor(colorIcon);
                mMainKeyboardView.setDominantColor(colorIcon);
            }
        }


    }

    public void updateColorIconMenu() {
        if (suggestionStripView != null) {
            suggestionStripView.changeColorIcon();
        }
    }

    private void setBitmapToBackground(String strPathIconKeyText, Bitmap bm, ThemeModel themeModel) {
        gdBackground = null;
        isLoadBitmap = true;

        if (bm != null) {
            Timber.e("hachung con btmap :");
            bmBackground = bm;
            if (themeModel.getBackground() != null) {
                changeRadius(getContext(), themeModel.getBackground().getRadiusBlur()/*themeModel.getBackground().getRadiusBlur()*/);
            } else {
                Timber.e("hachung setImageBitmap:"+bm);
                imgBackground.setImageBitmap(bm);
            }
            dominantColor = CommonUtil.getDominantColor(bm);
            mMainKeyboardView.setDominantColor(dominantColor);
            updateIcon(colorIcon, bm, dominantColor);
        } else {
            Glide.with(App.getInstance()).asBitmap().load(strPathIconKeyText).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    bmBackground = resource;
                    //Timber.d("ducNQ : setBitmapToBackgrounded:b "+bmBackground);
                    if (themeModel != null && themeModel.getBackground() != null) {
                        changeRadius(App.getInstance(), themeModel.getBackground().getRadiusBlur());
                    } else {
                        Timber.e("hachung Glide setImageBitmap:"+resource);
                        imgBackground.setImageBitmap(resource);
                    }
                    dominantColor = CommonUtil.getDominantColor(resource);
                    updateIcon(colorIcon, resource, dominantColor);
                    mMainKeyboardView.setDominantColor(dominantColor);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
        }
    }

    public boolean changeColorIcon() {
        if (App.getInstance().getTypeEditing() != Constant.TYPE_EDIT_CUSTOMIZE && App.getInstance().themeModel != null) {
            mMainKeyboardView.setThemeModel(App.getInstance().themeModel);
        }
        if (mMainKeyboardView.getThemeModel() != null && mMainKeyboardView.getThemeModel().getId() != null && mMainKeyboardView.getThemeModel().getMenuBar() != null) {
            if ((mMainKeyboardView.getThemeModel().getId() != null && Long.parseLong(mMainKeyboardView.getThemeModel().getId()) > 3000)
                    || mMainKeyboardView.getThemeModel().getId().equals(Constant.ID_THEME_DEFAULT)
                    ||mMainKeyboardView.getThemeModel().getId().equals("2004")) {
                if (Long.parseLong(mMainKeyboardView.getThemeModel().getId()) == 3001) {
                    App.getInstance().colorIconDefault = Color.WHITE;
                } else {
                    int color = CommonUtil.hex2decimal(mMainKeyboardView.getThemeModel().getMenuBar().getIconColor());
//                    if (App.getInstance().getTypeEditing() != Constant.TYPE_EDIT_CUSTOMIZE) {
//                        =
//                    } else {
//                        App.getInstance().colorIconDefault = CommonUtil.hex2decimal(mMainKeyboardView.getThemeModel().getMenuBar().getIconColor());
//                    }
                    App.getInstance().colorIconDefault = color;
                    if (App.getInstance().getTypeEditing() != Constant.TYPE_EDIT_CUSTOMIZE) {
                        if (Objects.equals(mMainKeyboardView.getThemeModel().getMenuBar().getIconColor(), "0x3A2127")
                                || Objects.equals(mMainKeyboardView.getThemeModel().getMenuBar().getIconColor(), "0x2D4F7F")
                                || Objects.equals(mMainKeyboardView.getThemeModel().getMenuBar().getIconColor(), "0x000000")
                                || Objects.equals(mMainKeyboardView.getThemeModel().getMenuBar().getIconColor(), "0x555555")
                        ) {

                            App.getInstance().colorIconNew = Color.WHITE;
                        } else {
                            App.getInstance().colorIconNew = color;
                        }
                    }
                }
                if (suggestionStripView != null) {
                    suggestionStripView.changeColorIcon();
                } else {
                    Timber.d("duongcv changeColorIcon null null null");
                }
                return true;
            }
        }
        return false;
    }


    public void updateIcon(int colorIcon, Bitmap bitmap, int dominantColor) {
        if (changeColorIcon()) return;
        colorIconOrigin = colorIcon;
        int color;
        if (bitmap == null) {
            bmBackground = null;
            Timber.d("updateIcon 1");
            colorBackground = dominantColor;
            color = CommonUtil.getContrastColor(colorIcon);
        } else {
            Timber.d("updateIcon 2");
            bmBackground = bitmap;
            colorIconOrigin = dominantColor;
            color = CommonUtil.getContrastColor(colorIconOrigin);
        }
        App.getInstance().colorIconDefault = color;

    }

    public void changeRadius(Context ctx, int radiusBlur) {
        Timber.d("duongcv changeRadius " + radiusBlur);
        if (bmBackground != null) {
            if (App.getInstance().checkBackGroundEmojiPalettesView) {
                App.getInstance().bitmap = bmBackground;
            }
            imgBackground.setImageBitmap(CommonUtil.blurBitmap(ctx, bmBackground, radiusBlur));
        } else {
            if (!App.getInstance().linkCurrentBg.equals("null")) {
                Glide.with(App.getInstance()).asBitmap().load(App.getInstance().linkCurrentBg).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bmBackground = resource;
                        App.getInstance().bitmap = bmBackground;
                        imgBackground.setImageBitmap(CommonUtil.blurBitmap(ctx, resource, radiusBlur));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
            }
        }
    }

    private void initLayoutSearchGif() {
        layoutSearch = findViewById(R.id.layout_search);
        edtSearchEmoji = findViewById(R.id.edt_search_emoji);

        imgBackSearch = findViewById(R.id.img_back_search_gif);
        imgBackSearch.setOnClickListener(v -> {
            hideSearchGif();
        });

    }

    public void setFalseGif() {
        if (mMainKeyboardView != null) {
            mMainKeyboardView.setSearchGif(false);
        }
    }

    public void hideSearchGif() {
        if (App.getInstance().mPrefs.getBoolean(Settings.SAVE_STATE_AUTO_CAPS_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, false)) {
            Timber.e("Duongcv " + "set true");
            App.getInstance().mPrefs.edit().putBoolean(Settings.PREF_AUTO_CAP, true).apply();
            App.getInstance().mPrefs.edit().putBoolean(Settings.SAVE_STATE_AUTO_CAPS_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, false).apply();

        }
        if (layoutSearch.getVisibility() == View.VISIBLE) {

            isSearchEmoji = false;
            if (mMainKeyboardView != null) {
                mMainKeyboardView.setSearchGif(false);
            }
            //   if(suggestionStripView.getVisibility()==GONE){
            suggestionStripView.setVisibility(VISIBLE);
            suggestionStripView.showUIMenu();
            //  }
            showHeaderKeyboard(Constant.HEADER_KEYBOARD_TYPE_MENU);
            EventBus.getDefault().post(new MessageEvent(Constant.EVENT_CANCEL_SEARCH_GIF));
        }
        Timber.d("ducNQ : hideSearchGif: " + isSearchEmoji);
    }

    public void searchGif() {
        isSearchEmoji = true;
        if (mMainKeyboardView != null) {
            mMainKeyboardView.setSearchGif(true);
        }
        resetEditText();
        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }

    }

    private void resetEditText() {
        edtSearchEmoji.setText("");
    }

    public boolean isSearchGif() {
        return isSearchEmoji;
    }

    public String getTextSearch() {
        return edtSearchEmoji.getText().toString();
    }

    public void appendTextToEditText(String t) {
        int startSelection = edtSearchEmoji.getSelectionStart();
        int endSelection = edtSearchEmoji.getSelectionEnd();

        String currentText = edtSearchEmoji.getText().toString();

        if (currentText.length() == 0 || (startSelection == endSelection) && (endSelection == currentText.length())) {
            edtSearchEmoji.append(t);
        } else {
            String afterDelete = currentText.substring(0, startSelection) + t + currentText.substring(endSelection);
            edtSearchEmoji.setText(afterDelete);
            edtSearchEmoji.setSelection(startSelection + t.length());
        }

        String beforeAppendText = edtSearchEmoji.getText().toString();
        if (beforeAppendText.length() >= 2) {
            String twoText = beforeAppendText.substring(beforeAppendText.length() - 2);
            if (twoText.equals("  ")) {
                String finalText = beforeAppendText.substring(0, beforeAppendText.length() - 2) + ". ";
                edtSearchEmoji.setText(finalText);
                edtSearchEmoji.setSelection(finalText.length());
            }
        }
    }

    public void deleteText() {
        int startSelection = edtSearchEmoji.getSelectionStart();
        int endSelection = edtSearchEmoji.getSelectionEnd();
        if (endSelection > 0) {
            String currentText = edtSearchEmoji.getText().toString();
            String afterDelete;
            if (startSelection != endSelection) {
                afterDelete = currentText.substring(0, startSelection) + currentText.substring(endSelection);
                edtSearchEmoji.setText(afterDelete);
                edtSearchEmoji.setSelection(startSelection);
            } else {
                afterDelete = currentText.substring(0, endSelection - 1) + currentText.substring(endSelection);
                edtSearchEmoji.setText(afterDelete);
                edtSearchEmoji.setSelection(endSelection - 1);
            }
        }
    }

    public Bitmap getBmBackground() {
        Timber.d("ducNQ : getBmBackgrounded:b " + bmBackground);
        return bmBackground;
    }

    public void setBitMapBackGround() {
        bmBackground = null;
    }

    public GradientDrawable getGdBackground() {
        return gdBackground;
    }

    public int getColorUse() {
        return colorUse;
    }

    public int getColorBackground() {
        return colorBackground;
    }

    public int getDominantColor() {
        return dominantColor;
    }

    public void goneLayoutSearch() {
        if (layoutSearch != null) {
            Timber.d("ducNQgoneLayoutSearch ");
            layoutSearch.setVisibility(GONE);
        }
    }

    public boolean getVisibleLayoutSearchGif() {
        if (layoutSearch.getVisibility() == VISIBLE) {
            return true;
        }
        return false;
    }

    public void showHeaderKeyboard(int typeHeader) {
        layoutSearch.setVisibility(GONE);
        isSearchEmoji = false;
        switch (typeHeader) {
            case Constant.HEADER_KEYBOARD_TYPE_MENU:
//                if (mPrefs.getBoolean(Constant.ACTION_SHOW_MENU_KEYBOARD, true)&&App.getInstance().getTypeEditing()!=Constant.TYPE_EDIT_CUSTOMIZE) {
                suggestionStripView.showUIMenu();
                if (mPrefs.getBoolean(com.android.inputmethod.latin.settings.Settings.PREF_SHOW_SUGGESTIONS, false))
                    suggestionStripView.setVisibility(VISIBLE);
//                } else {
//                    suggestionStripView.hideUIMenu();
//                }
//                suggestionStripView.setVisibility(GONE);
                break;
            case Constant.HEADER_KEYBOARD_TYPE_SUGGESTION:
//                if (mPrefs.getBoolean(Constant.ACTION_SHOW_MENU_KEYBOARD, true)) {
//                    if (findViewById(R.id.layout_menu).getVisibility() == VISIBLE) {
                suggestionStripView.hideUIMenu();
                if (mPrefs.getBoolean(com.android.inputmethod.latin.settings.Settings.PREF_SHOW_SUGGESTIONS, false))
                    suggestionStripView.setVisibility(VISIBLE);
                suggestionStripView.updateColorFilter(colorUse);
//                    }
//                } else {
//                    suggestionStripView.setVisibility(VISIBLE);
//                    suggestionStripView.updateColorFilter(colorUse);
//                }

                break;
            case Constant.HEADER_KEYBOARD_TYPE_SEARCH_GIF:
                suggestionStripView.hideUIMenuAndSuggest();
                suggestionStripView.setVisibility(GONE);
                isSearchEmoji = true;
                layoutSearch.setVisibility(VISIBLE);
                imgBackSearch.setColorFilter(App.getInstance().colorIconDefault);
                edtSearchEmoji.setHint(R.string.label_search_key);
                edtSearchEmoji.post(() -> edtSearchEmoji.requestFocus());
                break;
//            case Constant.HEADER_KEYBOARD_TYPE_NONE:
//                if (!mPrefs.getBoolean(Constant.ACTION_SHOW_MENU_KEYBOARD, true)) {
//                    findViewById(R.id.layout_menu).setVisibility(GONE);
//                }
//                break;

        }
    }

    /**
     * This class forwards series of {@link MotionEvent}s from <code>SenderView</code> to
     * <code>ReceiverView</code>.
     *
     * @param <SenderView>   a {@link View} that may send a {@link MotionEvent} to <ReceiverView>.
     * @param <ReceiverView> a {@link View} that receives forwarded {@link MotionEvent} from
     *                       <SenderView>.
     */
    private static abstract class
    MotionEventForwarder<SenderView extends View, ReceiverView extends View> {
        protected final SenderView mSenderView;
        protected final ReceiverView mReceiverView;

        protected final Rect mEventSendingRect = new Rect();
        protected final Rect mEventReceivingRect = new Rect();

        public MotionEventForwarder(final SenderView senderView, final ReceiverView receiverView) {
            mSenderView = senderView;
            mReceiverView = receiverView;
        }

        // Return true if a touch event of global coordinate x, y needs to be forwarded.
        protected abstract boolean needsToForward(final int x, final int y);

        // Translate global x-coordinate to <code>ReceiverView</code> local coordinate.
        protected int translateX(final int x) {
            return x - mEventReceivingRect.left;
        }

        // Translate global y-coordinate to <code>ReceiverView</code> local coordinate.
        protected int translateY(final int y) {
            return y - mEventReceivingRect.top;
        }

        /**
         * Callback when a {@link MotionEvent} is forwarded.
         *
         * @param me the motion event to be forwarded.
         */
        protected void onForwardingEvent(final MotionEvent me) {
        }

        // Returns true if a {@link MotionEvent} is needed to be forwarded to
        // <code>ReceiverView</code>. Otherwise returns false.
        public boolean onInterceptTouchEvent(final int x, final int y, final MotionEvent me) {
            // Forwards a {link MotionEvent} only if both <code>SenderView</code> and
            // <code>ReceiverView</code> are visible.
            if (mSenderView.getVisibility() != View.VISIBLE ||
                    mReceiverView.getVisibility() != View.VISIBLE) {
                return false;
            }
            mSenderView.getGlobalVisibleRect(mEventSendingRect);
            if (!mEventSendingRect.contains(x, y)) {
                return false;
            }

            if (me.getActionMasked() == MotionEvent.ACTION_DOWN) {
                // If the down event happens in the forwarding area, successive
                // {@link MotionEvent}s should be forwarded to <code>ReceiverView</code>.
                if (needsToForward(x, y)) {
                    return true;
                }
            }

            return false;
        }

        // Returns true if a {@link MotionEvent} is forwarded to <code>ReceiverView</code>.
        // Otherwise returns false.
        public boolean onTouchEvent(final int x, final int y, final MotionEvent me) {
            mReceiverView.getGlobalVisibleRect(mEventReceivingRect);
            // Translate global coordinates to <code>ReceiverView</code> local coordinates.
            me.setLocation(translateX(x), translateY(y));
            mReceiverView.dispatchTouchEvent(me);
            onForwardingEvent(me);
            return true;
        }
    }

    /**
     * This class forwards {@link MotionEvent}s happened in the top padding of
     * {@link MainKeyboardView} to {@link SuggestionStripView}.
     */
    private static class KeyboardTopPaddingForwarder
            extends MotionEventForwarder<MainKeyboardView, SuggestionStripView> {
        private int mKeyboardTopPadding;

        public KeyboardTopPaddingForwarder(final MainKeyboardView mainKeyboardView,
                                           final SuggestionStripView suggestionStripView) {
            super(mainKeyboardView, suggestionStripView);
        }

        public void setKeyboardTopPadding(final int keyboardTopPadding) {
            mKeyboardTopPadding = keyboardTopPadding;
        }

        private boolean isInKeyboardTopPadding(final int y) {
            return y < mEventSendingRect.top + mKeyboardTopPadding;
        }

        @Override
        protected boolean needsToForward(final int x, final int y) {
            // Forwarding an event only when {@link MainKeyboardView} is visible.
            // Because the visibility of {@link MainKeyboardView} is controlled by its parent
            // view in {@link KeyboardSwitcher#setMainKeyboardFrame()}, we should check the
            // visibility of the parent view.
            final View mainKeyboardFrame = (View) mSenderView.getParent();
            return mainKeyboardFrame.getVisibility() == View.VISIBLE && isInKeyboardTopPadding(y);
        }

        @Override
        protected int translateY(final int y) {
            final int translatedY = super.translateY(y);
            if (isInKeyboardTopPadding(y)) {
                // The forwarded event should have coordinates that are inside of the target.
                return Math.min(translatedY, mEventReceivingRect.height() - 1);
            }
            return translatedY;
        }
    }

    /**
     * This class forwards {@link MotionEvent}s happened in the {@link MainKeyboardView} to
     * {@link SuggestionStripView} when the {@link MoreSuggestionsView} is showing.
     * {@link SuggestionStripView} dismisses {@link MoreSuggestionsView} when it receives any event
     * outside of it.
     */
    private static class MoreSuggestionsViewCanceler
            extends MotionEventForwarder<MainKeyboardView, SuggestionStripView> {
        public MoreSuggestionsViewCanceler(final MainKeyboardView mainKeyboardView,
                                           final SuggestionStripView suggestionStripView) {
            super(mainKeyboardView, suggestionStripView);
        }

        @Override
        protected boolean needsToForward(final int x, final int y) {
            return mReceiverView.isShowingMoreSuggestionPanel() && mEventSendingRect.contains(x, y);
        }

        @Override
        protected void onForwardingEvent(final MotionEvent me) {
            if (me.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mReceiverView.dismissMoreSuggestionsPanel();
            }
        }
    }


}
