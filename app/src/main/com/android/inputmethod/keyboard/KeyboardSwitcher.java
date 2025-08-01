/*
 * Copyright (C) 2008 The Android Open Source Project
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

import static com.tapbi.spark.yokey.util.Constant.HEADER_KEYBOARD_TYPE_MENU;
import static com.tapbi.spark.yokey.util.Constant.KEY_BOARD;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodSubtype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.inputmethod.compat.InputMethodServiceCompatUtils;
import com.android.inputmethod.event.Event;
import com.android.inputmethod.keyboard.KeyboardLayoutSet.KeyboardLayoutSetException;
import com.android.inputmethod.keyboard.emoji.EmojiPalettesView;
import com.android.inputmethod.keyboard.internal.KeyboardState;
import com.android.inputmethod.keyboard.internal.KeyboardTextsSet;
import com.android.inputmethod.keyboard.translate.ViewChooseLanguage;
import com.android.inputmethod.keyboard.translate.ViewTranslate;
import com.android.inputmethod.latin.InputView;
import com.android.inputmethod.latin.LatinIME;
import com.android.inputmethod.latin.RichInputMethodManager;
import com.android.inputmethod.latin.RichInputMethodSubtype;
import com.android.inputmethod.latin.WordComposer;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.define.ProductionFlags;
import com.android.inputmethod.latin.settings.Settings;
import com.android.inputmethod.latin.settings.SettingsValues;
import com.android.inputmethod.latin.utils.CapsModeUtils;
import com.android.inputmethod.latin.utils.LanguageOnSpacebarUtils;
import com.android.inputmethod.latin.utils.RecapitalizeStatus;
import com.android.inputmethod.latin.utils.ResourceUtils;
import com.android.inputmethod.latin.utils.ScriptUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.data.local.entity.ItemFont;
import com.tapbi.spark.yokey.data.model.ChangeToFontNormal;
import com.tapbi.spark.yokey.data.model.MessageEvent;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.ui.custom.view.CopyPasteSelectionView;
import com.tapbi.spark.yokey.ui.custom.view.ViewBgKeyKb;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.Constant;
import com.tapbi.spark.yokey.util.DisplayUtils;
import com.tapbi.spark.yokey.util.MySharePreferences;

import org.greenrobot.eventbus.EventBus;

import javax.annotation.Nonnull;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public final class KeyboardSwitcher implements KeyboardState.SwitchActions {
    private static final String TAG = KeyboardSwitcher.class.getSimpleName();
    private static final KeyboardSwitcher sInstance = new KeyboardSwitcher();
    // TODO: The following {@link KeyboardTextsSet} should be in {@link KeyboardLayoutSet}.
    private final KeyboardTextsSet mKeyboardTextsSet = new KeyboardTextsSet();
    public KeyboardState mState;
    public KeyboardLayoutSet mKeyboardLayoutSet;
    private String colorCustom = "null";
    private String pathCustom = "null";
    private InputView mCurrentInputView;
    private View mMainKeyboardFrame;
    private int typeKey = Constant.TYPE_KEY_2006;
    private MainKeyboardView mKeyboardView;
    private EmojiPalettesView mEmojiPalettesView;
    private CopyPasteSelectionView copyPasteSelectionView;
    private LatinIME mLatinIME;
    private RichInputMethodManager mRichImm;
    private boolean mIsHardwareAcceleratedDrawingEnabled;
    private SharedPreferences mPrefs;
    private String colorCurrentKillApp = "#FFF0F0F0";
    private ViewTranslate viewTranslate;
    private KeyboardTheme mKeyboardTheme;
    private Context mThemeContext;
    private boolean mIsVietnameseType;
    private boolean isTelexVietnamese;
    private boolean isQwertyVietnamese;
    private boolean isTelexVietnameseSimple;
    private View viewBgDragHeight;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private boolean mIsKoreanType;
    private ViewChooseLanguage viewChooseLanguage;

    private KeyboardSwitcher() {
        // Intentional empty constructor for singleton.
    }

    public static KeyboardSwitcher getInstance() {
        return sInstance;
    }

    public static void init(final LatinIME latinIme) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(latinIme);
        sInstance.initInternal(latinIme);
    }

    public boolean ismIsVietnameseType() {
        return mIsVietnameseType;
    }

    public boolean isTelexVietnamese() {
        return isTelexVietnamese;
    }

    public boolean isTelexVietnameseSimple() {
        return isTelexVietnameseSimple;
    }

    //    public SuggestionStripView getSuggestionStripView() {
//        return suggestionStripView;
//    }
    public int getHeightKeyboard() {
        if (mKeyboardView != null) {
            return mKeyboardView.getHeight();
        } else {
            return 660;
        }
    }

    public int getWidthKeyboard() {
        if (mKeyboardView != null) {
            return mKeyboardView.getWidth();
        } else {
            return DisplayUtils.getScreenWidth();
        }
    }

    public void changeColorNavigationBar() {
        if (mLatinIME != null) {
            mLatinIME.changeColorNavigationBar();
        }
    }

    public LatinIME getmLatinIME() {
        return mLatinIME;
    }

    public void loadKeyboardAsyncDone(final int currentAutoCapsState, final int currentRecapitalizeState) {
        try {
            //  Timber.d("ducNQonLoadKeyboard 2");
            mState.onLoadKeyboard(currentAutoCapsState, currentRecapitalizeState);
            mKeyboardTextsSet.setLocale(mRichImm.getCurrentSubtypeLocale(), mThemeContext);
        } catch (KeyboardLayoutSetException e) {
            Log.w(TAG, "loading keyboard failed: " + e.mKeyboardId, e.getCause());
        }
    }

    public void changeConfigLanguageBySubtype(Keyboard keyboard) {
        //   Log.d("duongcv", "changeConfigLanguageBySubtype: " + keyboard.mId.mSubtype.getLocale().getLanguage());
        if (keyboard != null) {
            mIsVietnameseType = keyboard.mId.mSubtype.getLocale().getLanguage().equals("vi_VN") ||
                    keyboard.mId.mSubtype.getLocale().getLanguage().equals("vi");
            mIsKoreanType = keyboard.mId.mSubtype.getLocale().toString().equals("ko_KR") ||
                    keyboard.mId.mSubtype.getLocale().toString().equals("ko");
            String inputType = mPrefs.getString(Constant.INPUT_TYPE_VIETNAMESE, Constant.INPUT_TYPE_VIETNAMESE_DEFAULT);
            switch (inputType) {
                case Constant.INPUT_TYPE_VIETNAMESE_TELEX_SIMPLE:
                    isTelexVietnamese = true;
                    isTelexVietnameseSimple = true;
                    isQwertyVietnamese = false;
                    break;

                case Constant.INPUT_TYPE_VIETNAMESE_VNI:
                    isTelexVietnamese = false;
                    isTelexVietnameseSimple = false;
                    isQwertyVietnamese = false;
                    break;
                case Constant.INPUT_TYPE_QWERTY_VIETNAMESE:
                    isTelexVietnamese = false;
                    isTelexVietnameseSimple = false;
                    isQwertyVietnamese = true;
                    break;
                default:
                    isTelexVietnamese = true;
                    isTelexVietnameseSimple = false;
                    isQwertyVietnamese = false;
                    break;
            }
            if (mLatinIME != null) {
                mLatinIME.changeConfigLanguageBySubtype();
            }
        }

    }

    public boolean isQwertyVietnamese() {
        return isQwertyVietnamese;
    }

    public boolean ismIsKoreanType() {
        return mIsKoreanType;
    }

    public ViewChooseLanguage getViewChooseLanguage() {
        return viewChooseLanguage;
    }

    public void setShowViewBgDragInKbView(boolean isShow) {
        if (viewBgDragHeight != null) {
            viewBgDragHeight.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }
    }

    public ViewTranslate getViewTranslate() {
        return viewTranslate;
    }

    private void closeTranslate() {
        if (mLatinIME.isViewTranslateShow()) {
            mLatinIME.closeTranslate();
        }
    }

    private void initInternal(final LatinIME latinIme) {
        mLatinIME = latinIme;
        mRichImm = RichInputMethodManager.getInstance();
        mState = new KeyboardState(this, this);
        mIsHardwareAcceleratedDrawingEnabled =
                InputMethodServiceCompatUtils.enableHardwareAcceleration(mLatinIME);
    }

    public void updateKeyboardTheme(String color, String pathImage) {
        if (mCurrentInputView != null) {
            if (color != null && pathImage != null) {
                if (!color.equals("null") || !pathImage.equals("null"))
                    App.getInstance().mPrefs.edit().putBoolean(Constants.CHECK_REQUESTLAYOUT_MAINKEYBOARD, true).apply();
            }
            // Timber.d("ducNQ : updateKeyboardTheme: " + pathImage);
            mCurrentInputView.setupBackgroundKeyboard(color, pathImage);
            updateBackGroundEmojiPlateView(pathImage);
        }
    }

    public void setOriginalBitmapEmojiPlateView() {
        if (mEmojiPalettesView != null) {
            mEmojiPalettesView.setOriginalEmojiPlateView();
        }
    }

    private void updateBackGroundEmojiPlateView(String pathImage) {
        if (mEmojiPalettesView != null) {
            Glide.with(App.getInstance().getBaseContext()).asBitmap().load(pathImage).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    // App.getInstance().bitmap=resource;
                    mEmojiPalettesView.setBackGroundEmojiPlateView(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });

        }
    }

    public boolean getViewKeyBoardDemo(ThemeModel themeModel, String name) {
        if (mCurrentInputView != null) {
            ViewBgKeyKb viewBgKeyKb = mCurrentInputView.findViewById(R.id.lyKey);
            setViewDemoKey(viewBgKeyKb, themeModel, name, mKeyboardView);
            return true;
        }
        return false;
    }

    public void setViewDemoKey(ViewBgKeyKb viewBgKeyKb, ThemeModel themeModel, String name, KeyboardView keyboardView) {
//        if (mCurrentInputView != null) {
        if (keyboardView.getKeyboard() != null) {
            String path = themeModel.getBackground().getBackgroundImage();
            Timber.e("hachung path:" + path);
            if (path != null && !path.equals("null")) {
                Timber.e("Duongcv " + path);
                Glide.with(App.getInstance()).asBitmap().load(path).priority(Priority.HIGH).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Timber.e("Duongcv load bitmap");
                        int blur = themeModel.getBackground().getRadiusBlur();
                        Drawable drawable;
                        if (blur == 0) {
                            viewBgKeyKb.setListKey(keyboardView.getKeyboard(), keyboardView.getKeyDrawParams(), themeModel, resource, name, keyboardView.fullLanguageDisplay);
                        } else {
                            viewBgKeyKb.setListKey(keyboardView.getKeyboard(), keyboardView.getKeyDrawParams(), themeModel, CommonUtil.blurBitmap(App.getInstance(), resource, blur), name, keyboardView.fullLanguageDisplay);
                        }

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                });
            } else {
                Timber.e("Duongcv path null" + path);
                viewBgKeyKb.setListKey(keyboardView.getKeyboard(), keyboardView.getKeyDrawParams(), themeModel, null, name, keyboardView.fullLanguageDisplay);
            }
        }
//        }
    }

    public void changeBlurBackground(int radiusBlur) {
        if (mCurrentInputView != null) {
            App.getInstance().blurBg = radiusBlur;
            mCurrentInputView.changeRadius(App.getInstance(), radiusBlur);
            //Timber.d("ducNQchangeBlurBackground " + App.getInstance().bitmap);
            if (App.getInstance().bitmap != null) {
                mEmojiPalettesView.changeRadius(App.getInstance(), radiusBlur);
            }
            //else{

            // }
        }
    }

    public void setOriginalEmojiPlateView() {
        if (mEmojiPalettesView != null) {
            mEmojiPalettesView.changeRadius(App.getInstance(), App.getInstance().blurBg);
        }
    }

    public void setOriginalBitmapBg() {
        if (mCurrentInputView != null) {
            mCurrentInputView.setBitMapBackGround();
        }
    }


    public void changeColorIcon() {
        Timber.d("ducNQ : changeColorMenu: " + mCurrentInputView);
        if (mCurrentInputView != null) {
            mCurrentInputView.changeColorIcon();
        }
    }

    public void setColorMenu(String colorMenu) {
        if (mKeyboardView != null) {
            mKeyboardView.setColorMenu(colorMenu);
        }
    }

    public void changeTextColor(String colorText) {
        colorCurrentKillApp = colorText;
        //  Timber.d("ducNQ : changeTextColored:b "+System.currentTimeMillis());
        if (mKeyboardView != null) {//kill app mKeyboard null
            //        Timber.d("ducNQ : changeTextColored:c "+System.currentTimeMillis());
            mKeyboardView.changeTextColor(colorText);
        }
    }

    public void changeTypeKey(int typeKey) {
        this.typeKey = typeKey;
        Timber.d("ducNQ : changeTypeKey: " + mKeyboardView);
        if (mKeyboardView != null) {
            mKeyboardView.changeTypeKey(typeKey);
        }
    }

    public void updateStateEmoji() {
        mState.setEmojiKeyboard();
    }


    private boolean updateKeyboardThemeAndContextThemeWrapper(final Context context,
                                                              final KeyboardTheme keyboardTheme) {

        Timber.e("Duongcv " + "updateKeyboardThemeAndContextThemeWrapper");
//        if (mThemeContext == null || !keyboardTheme.equals(mKeyboardTheme)) {
        mKeyboardTheme = keyboardTheme;
        mThemeContext = new ContextThemeWrapper(context, keyboardTheme.mStyleId);
        KeyboardLayoutSet.onKeyboardThemeChanged();
        return true;
//        }
//        return false;
    }

    public void updateSuggestionBySubtype() {
        if (mCurrentInputView != null) {
            mCurrentInputView.updateSuggestionBySubtype();
        }
    }


    public void loadKeyboard(final EditorInfo editorInfo, final SettingsValues settingsValues,
                             final int currentAutoCapsState, final int currentRecapitalizeState, boolean isLoadAsync) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mThemeContext);
        final KeyboardLayoutSet.Builder builder = new KeyboardLayoutSet.Builder(
                mThemeContext, editorInfo);
        final Resources res = mThemeContext.getResources();
        final int keyboardWidth = ResourceUtils.getDefaultKeyboardWidth(res);
        final int keyboardHeight = ResourceUtils.getKeyboardHeight(res, settingsValues);
        Log.d("duongcv", "loadKeyboard: " + keyboardWidth + ":" + keyboardHeight);
        builder.setKeyboardGeometry(keyboardWidth, keyboardHeight);
        builder.setSubtype(mRichImm.getCurrentSubtype());
        builder.setVoiceInputKeyEnabled(settingsValues.mShowsVoiceInputKey);
//        List<LanguageEntity> list = App.getInstance().keyboardLanguageRepository.mLanguageEntities;
//        if (list.size() > 0) {
        builder.setLanguageSwitchKeyEnabled(true);
//        }
//        builder.setLanguageSwitchKeyEnabled(mLatinIME.shouldShowLanguageSwitchKey());
        builder.setSplitLayoutEnabledByUser(ProductionFlags.IS_SPLIT_KEYBOARD_SUPPORTED
                && settingsValues.mIsSplitKeyboardEnabled);
        InputMethodSubtype inputMethodSubtype = mRichImm.rollbackToDefaultSubtype();
        RichInputMethodSubtype richInputMethodSubtype = RichInputMethodSubtype.getRichInputMethodSubtype(inputMethodSubtype);
        builder.setSubtype(richInputMethodSubtype);
        if (getKeyboard() != null) {
            getKeyboard().mId.mSubtype = richInputMethodSubtype;
        }
        if (mKeyboardView != null) {
            mKeyboardView.setSearchGif(mLatinIME.isSearchGif());
        }
        Timber.d("ducNQ : loadKeyboarded: " + builder.getStubtype().getLocale().getLanguage());
        mKeyboardLayoutSet = builder.build();
        mIsVietnameseType = builder.getStubtype().getLocale().getLanguage().equals("vi_VN") ||
                builder.getStubtype().getLocale().getLanguage().equals("vi");
        mIsKoreanType = builder.getStubtype().getLocale().toString().equals("ko_KR") ||
                builder.getStubtype().getLocale().toString().equals("ko");
        Settings.setLanguageKeyBoardCurrent(builder.getStubtype().getLocale().getLanguage());
        if (mIsVietnameseType || mIsKoreanType) {
            if (mLatinIME != null) {
                mLatinIME.changeConfigLanguageBySubtype();
            }
        }
        if (mLatinIME != null) {
            mLatinIME.resetDictionaryFacilitatorIfNecessary(builder.getStubtype().getLocale());
        }

        boolean isKeyboardChange = mPrefs.getBoolean(com.tapbi.spark.yokey.common.Constant.KEYBOARD_CHANGE_LISTENER, false);
        if (isKeyboardChange) {
            mPrefs.edit().putBoolean(com.tapbi.spark.yokey.common.Constant.KEYBOARD_CHANGE_LISTENER, false).apply();
            if (mKeyboardView != null) mKeyboardView.resetSizeEmoji();
            KeyboardLayoutSet.onKeyboardChanged();
        }
        if (!isLoadAsync) {
            try {
                mState.onLoadKeyboard(currentAutoCapsState, currentRecapitalizeState);
                mKeyboardTextsSet.setLocale(mRichImm.getCurrentSubtypeLocale(), mThemeContext);
            } catch (KeyboardLayoutSetException e) {
                Log.w(TAG, "loading keyboard failed: " + e.mKeyboardId, e.getCause());
            }
        }
    }

    public void saveKeyboardState() {
        if (getKeyboard() != null || isShowingEmojiPalettes()) {
            mState.onSaveKeyboardState();
        }
    }

    public void onHideWindow() {
        if (mKeyboardView != null) {
            mKeyboardView.onHideWindow();
        }
        closeTranslate();
    }

    public void changeColorText(String colorCurrentKillApp) {
        this.colorCurrentKillApp = colorCurrentKillApp;
        changeTextColor(colorCurrentKillApp);
    }

    public void setKeyboard(
            @Nonnull final int keyboardId,
            @Nonnull final KeyboardSwitchState toggleState) {

        // Make {@link MainKeyboardView} visible and hide {@link EmojiPalettesView}.
        if (mLatinIME != null && mLatinIME.getEditSelectrionView() != null && mLatinIME.getEditSelectrionView().getVisibility() == View.VISIBLE) {
            return;
        }
        final SettingsValues currentSettingsValues = Settings.getInstance().getCurrent();
        setMainKeyboardFrame(currentSettingsValues, toggleState);
        if (keyboardId == KeyboardId.ELEMENT_ALPHABET_AUTOMATIC_SHIFTED && !mLatinIME.isSearchGif()) {
            if (mLatinIME.mSuggestionStripView != null && mLatinIME.mSuggestionStripView.getVisibility() == View.VISIBLE) {
                if (!mLatinIME.mSuggestionStripView.isShowMenu() && mLatinIME.mSuggestionStripView.mSuggestionsStrip.getVisibility() != View.VISIBLE) {
                    EventBus.getDefault().post(new MessageEvent(Constant.EVENT_SHOW_MENU));
                }
            } else {
                EventBus.getDefault().post(new MessageEvent(Constant.EVENT_SHOW_MENU));
            }
        }
        // TODO: pass this object to setKeyboard instead of getting the current values.
        if (mKeyboardLayoutSet == null) return;
        final Keyboard newKeyboard = mKeyboardLayoutSet.getKeyboard(keyboardId);
        if (mKeyboardView != null && mKeyboardView.getHeight() > 0) {
            setKeyboardAsync(keyboardId);
        } else {
            doAfterSetKeyboardAsync(newKeyboard);
        }


        // TODO: 3/22/2021 kb vi hoangld
        mIsVietnameseType = newKeyboard.mId.mSubtype.getLocale().toString().contains("vi_VN") ||
                newKeyboard.mId.mSubtype.getLocale().toString().contains("vi");

        isTelexVietnamese = true;
        isTelexVietnameseSimple = false;

    }

    public void clearResource() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }

    public void setKeyboardAsync(final int keyboardId) {
        clearResource();
        Single.fromCallable(() -> mKeyboardLayoutSet.getKeyboard(keyboardId)).subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()).doOnSuccess(newKeyboard -> {
            Log.d("duongcv", "setKeyboardAsync: ");
            doAfterSetKeyboardAsync(newKeyboard);
        }).doOnSubscribe(disposable -> mCompositeDisposable.add(disposable)).doOnError(throwable -> {
            throwable.printStackTrace();
        }).subscribe();
    }

    public void doAfterSetKeyboardAsync(Keyboard newKeyboard) {
        Log.d("duongcv", "doAfterSetKeyboardAsync: ");
        final SettingsValues currentSettingsValues = Settings.getInstance().getCurrent();
        final MainKeyboardView keyboardView = mKeyboardView;
        final Keyboard oldKeyboard = keyboardView.getKeyboard();
        if (MySharePreferences.getBooleanValue(KEY_BOARD, App.getInstance().getBaseContext())) {
            mKeyboardView.changeTypeKey(this.typeKey);
            MySharePreferences.putBoolean(KEY_BOARD, false, App.getInstance().getBaseContext());
        }
        if (getViewTranslate() != null) {
            getViewTranslate().setKeyboardNumber(mKeyboardLayoutSet.isTypeNumberKeyboard());
            if (getViewTranslate().getVisibility() == View.VISIBLE) {
                getViewTranslate().startShowTranslate();
            }
        }


        keyboardView.setKeyboard(newKeyboard);
        mCurrentInputView.setKeyboardTopPadding(newKeyboard.mTopPadding);
        if (mLatinIME.isSearchGif()) {
            mCurrentInputView.showHeaderKeyboard(Constant.HEADER_KEYBOARD_TYPE_SEARCH_GIF);
        }
        keyboardView.setKeyPreviewPopupEnabled(
                currentSettingsValues.mKeyPreviewPopupOn,
                currentSettingsValues.mKeyPreviewPopupDismissDelay);
        keyboardView.setKeyPreviewAnimationParams(
                currentSettingsValues.mHasCustomKeyPreviewAnimationParams,
                currentSettingsValues.mKeyPreviewShowUpStartXScale,
                currentSettingsValues.mKeyPreviewShowUpStartYScale,
                currentSettingsValues.mKeyPreviewShowUpDuration,
                currentSettingsValues.mKeyPreviewDismissEndXScale,
                currentSettingsValues.mKeyPreviewDismissEndYScale,
                currentSettingsValues.mKeyPreviewDismissDuration);
        keyboardView.updateShortcutKey(mRichImm.isShortcutImeReady());
        keyboardView.setFullLanguageDisplay(newKeyboard.mId.mSubtype.getFullDisplayName());
        final boolean subtypeChanged = (oldKeyboard == null)
                || !newKeyboard.mId.mSubtype.equals(oldKeyboard.mId.mSubtype);
        final int languageOnSpacebarFormatType = LanguageOnSpacebarUtils
                .getLanguageOnSpacebarFormatType(newKeyboard.mId.mSubtype);
        final boolean hasMultipleEnabledIMEsOrSubtypes = mRichImm
                .hasMultipleEnabledIMEsOrSubtypes(true /* shouldIncludeAuxiliarySubtypes */);
        keyboardView.startDisplayLanguageOnSpacebar(subtypeChanged, languageOnSpacebarFormatType,
                hasMultipleEnabledIMEsOrSubtypes);
    }

    public Keyboard getKeyboard() {
        if (mKeyboardView != null) {
            return mKeyboardView.getKeyboard();
        }
        return null;
    }

    // TODO: Remove this method. Come up with a more comprehensive way to reset the keyboard layout
    // when a keyboard layout set doesn't get reloaded in LatinIME.onStartInputViewInternal().
    public void resetKeyboardStateToAlphabet(final int currentAutoCapsState,
                                             final int currentRecapitalizeState) {
        mState.onResetKeyboardStateToAlphabet(currentAutoCapsState, currentRecapitalizeState);
    }

    public void onPressKey(final int code, final boolean isSinglePointer,
                           final int currentAutoCapsState, final int currentRecapitalizeState) {
        mState.onPressKey(code, isSinglePointer, currentAutoCapsState, currentRecapitalizeState);
    }

    public void onReleaseKey(final int code, final boolean withSliding,
                             final int currentAutoCapsState, final int currentRecapitalizeState) {
        mState.onReleaseKey(code, withSliding, currentAutoCapsState, currentRecapitalizeState);
    }

    public void onFinishSlidingInput(final int currentAutoCapsState,
                                     final int currentRecapitalizeState) {
        mState.onFinishSlidingInput(currentAutoCapsState, currentRecapitalizeState);
    }

    @Override
    public void setAlphabetKeyboardTranslate() {
        setKeyboard(KeyboardId.ELEMENT_ALPHABET, KeyboardSwitchState.OTHER);
        mLatinIME.startTranslate();
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetKeyboard");
        }
        Log.d("duongcv", "setAlphabetKeyboard: ");
//        mLatinIME.showMenuHeader();
        setKeyboard(KeyboardId.ELEMENT_ALPHABET, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetManualShiftedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetManualShiftedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET_MANUAL_SHIFTED, KeyboardSwitchState.OTHER);
    }

    public void updateShiftState() {
        mState.onUpdateShiftState(mLatinIME.getCurrentAutoCapsState(), mLatinIME.getCurrentRecapitalizeState());
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetAutomaticShiftedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetAutomaticShiftedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET_AUTOMATIC_SHIFTED, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetShiftLockedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetShiftLockedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCKED, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetShiftLockShiftedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetShiftLockShiftedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCK_SHIFTED, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setSymbolsKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setSymbolsKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_SYMBOLS, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setSymbolsShiftedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setSymbolsShiftedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_SYMBOLS_SHIFTED, KeyboardSwitchState.SYMBOLS_SHIFTED);
    }

    @Override
    public void setAlphabetKeyboardSearchGif() {
        mLatinIME.changeTypeView(Constant.KEYBOARD_VIEW_TYPE_KEY);
        mLatinIME.startSearchGif();
        mKeyboardView.setSearchGif(mLatinIME.isSearchGif());
        setKeyboard(KeyboardId.ELEMENT_ALPHABET, KeyboardSwitchState.OTHER);
    }

    public boolean isImeSuppressedByHardwareKeyboard(
            @Nonnull final SettingsValues settingsValues,
            @Nonnull final KeyboardSwitchState toggleState) {
        return settingsValues.mHasHardwareKeyboard && toggleState == KeyboardSwitchState.HIDDEN;
    }

    public void onCodeInput(final int code, final int currentAutoCapsState,
                            final int currentRecapitalizeState) {
        mState.onCodeInput(code, currentAutoCapsState, currentRecapitalizeState);
    }

    private void setMainKeyboardFrame(
            @Nonnull final SettingsValues settingsValues,
            @Nonnull final KeyboardSwitchState toggleState) {
        final int visibility = isImeSuppressedByHardwareKeyboard(settingsValues, toggleState)
                ? View.GONE : View.VISIBLE;
        Timber.e("hachung visibility:" + visibility);
        mKeyboardView.setVisibility(visibility);
        App.getInstance().isShowEmoji = false;
        mLatinIME.refeshView();
        // The visibility of {@link #mKeyboardView} must be aligned with {@link #MainKeyboardFrame}.
        // @see #getVisibleKeyboardView() and
        // @see LatinIME#onComputeInset(android.inputmethodservice.InputMethodService.Insets)
        mMainKeyboardFrame.setVisibility(visibility);
        mEmojiPalettesView.setVisibility(View.GONE);
        mEmojiPalettesView.stopEmojiPalettes();

    }

    /**
     * Click emoji from keyboard
     */
    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setEmojiKeyboard(boolean check) {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setEmojiKeyboard");
        }
        if (getViewTranslate() != null) {
            getViewTranslate().setVisibility(View.GONE);
            getViewTranslate().closeTranslate();
        }
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
                if (mCurrentInputView != null && mCurrentInputView instanceof InputView) {
                    ((InputView) mCurrentInputView).hideSearchGif();
                }
                final Keyboard keyboard = mKeyboardLayoutSet.getKeyboard(KeyboardId.ELEMENT_ALPHABET);
                mMainKeyboardFrame.setVisibility(View.GONE);
                // The visibility of {@link #mKeyboardView} must be aligned with {@link #MainKeyboardFrame}.
                // @see #getVisibleKeyboardView() and
                // @see LatinIME#onComputeInset(android.inputmethodservice.InputMethodService.Insets)
                mKeyboardView.setVisibility(View.GONE);
                if (mEmojiPalettesView == null) {
                    initEmojiPalettesView();
                } else {
                    mEmojiPalettesView.getLayoutParams().height = mMainKeyboardFrame.getHeight();
                    mEmojiPalettesView.getLayoutParams().width = mMainKeyboardFrame.getWidth();
                }
                mEmojiPalettesView.startEmojiPalettes(
                        mKeyboardTextsSet.getText(KeyboardTextsSet.SWITCH_TO_ALPHA_KEY_LABEL),
                        mKeyboardView.getKeyVisualAttribute(), keyboard.mIconsSet);
                mEmojiPalettesView.setVisibility(View.VISIBLE);
                if (App.getInstance().themeRepository != null && mCurrentInputView != null && mEmojiPalettesView != null) {
                    mEmojiPalettesView.setBackGroundEmojiPlateView(mCurrentInputView.getBmBackground());
                    //   }
                    if (App.getInstance().themeModel != null && App.getInstance().themeModel.getMenuBar() != null) {
                        mEmojiPalettesView.setColorFilter(App.getInstance().colorIconDefault);
                    }
                }
                if (mCurrentInputView != null) {
                    mCurrentInputView.showHeaderKeyboard(HEADER_KEYBOARD_TYPE_MENU);
                }


                if (check) {
                    if (mEmojiPalettesView != null) {
                        mEmojiPalettesView.onEmojiGifShow(Constant.TYPE_EMOJI);
                    }
                }
                App.getInstance().isShowEmoji = true;
//            }
//        }, 10);
    }

    public void updateBgForEmojiPalettes() {
        if (mEmojiPalettesView != null) {
           /* if (App.getInstance().themeRepository != null && mCurrentInputView != null && mEmojiPalettesView != null) {
                if (mCurrentInputView.getBmBackground() == null
                        && App.getInstance().themeModel != null &&
                        App.getInstance().themeModel.getBackground() != null
                        && App.getInstance().themeModel.getBackground().getBackgroundImage() != null) {
                    File file = App.getInstance().appDir;
                    String strPathIconKeyText = file.toString() + "/" + App.getInstance().themeModel.getId() + "/" + App.getInstance().themeModel.getBackground().getBackgroundImage();
                    Glide.with(App.getInstance()).asBitmap().load(strPathIconKeyText).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@androidx.annotation.NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            mEmojiPalettesView.setImgBackgroundEmojiPalettes(resource, mCurrentInputView.getGdBackground(),
                                    mCurrentInputView.getDominantColor(), mCurrentInputView.getColorBackground(), mCurrentInputView.getColorUse());
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
                }else{*/
            mEmojiPalettesView.setImgBackgroundEmojiPalettes(mCurrentInputView.getBmBackground(), mCurrentInputView.getGdBackground(),
                    mCurrentInputView.getDominantColor(), mCurrentInputView.getColorBackground(), mCurrentInputView.getColorUse());
            //}
            //  }
        }
    }

    public KeyboardSwitchState getKeyboardSwitchState() {
        boolean hidden = !isShowingEmojiPalettes()
                && (mKeyboardLayoutSet == null
                || mKeyboardView == null
                || !mKeyboardView.isShown());
        KeyboardSwitchState state;
        if (hidden) {
            return KeyboardSwitchState.HIDDEN;
        } else if (isShowingEmojiPalettes()) {
            return KeyboardSwitchState.EMOJI;
        } else if (isShowingKeyboardId(KeyboardId.ELEMENT_SYMBOLS_SHIFTED)) {
            return KeyboardSwitchState.SYMBOLS_SHIFTED;
        }
        return KeyboardSwitchState.OTHER;
    }

    public void onToggleKeyboard(@Nonnull final KeyboardSwitchState toggleState) {
        KeyboardSwitchState currentState = getKeyboardSwitchState();
        Log.w(TAG, "onToggleKeyboard() : Current = " + currentState + " : Toggle = " + toggleState);
        Timber.d("ducNQ : onToggleKeyboard: " + currentState);
        Timber.d("ducNQ : onToggleKeyboard: " + toggleState);
        if (currentState == toggleState) {
            mLatinIME.stopShowingInputView();
            mLatinIME.hideWindow();
            setAlphabetKeyboard();
        } else {
            mLatinIME.startShowingInputView(true);
            if (toggleState == KeyboardSwitchState.EMOJI) {
                Timber.d("ducNQ startShowingInputView");
                setEmojiKeyboard(false);
            } else {
                mEmojiPalettesView.stopEmojiPalettes();
                mEmojiPalettesView.setVisibility(View.GONE);

                mMainKeyboardFrame.setVisibility(View.VISIBLE);
                mKeyboardView.setVisibility(View.VISIBLE);
                App.getInstance().isShowEmoji = false;
                setKeyboard(toggleState.mKeyboardId, toggleState);
            }
        }
    }

    // Future method for requesting an updating to the shift state.
    @Override
    public void requestUpdatingShiftState(final int autoCapsFlags, final int recapitalizeMode) {
        if (DEBUG_ACTION) {
            Log.d(TAG, "requestUpdatingShiftState: "
                    + " autoCapsFlags=" + CapsModeUtils.flagsToString(autoCapsFlags)
                    + " recapitalizeMode=" + RecapitalizeStatus.modeToString(recapitalizeMode));
        }
        mState.onUpdateShiftState(autoCapsFlags, recapitalizeMode);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void startDoubleTapShiftKeyTimer() {
        if (DEBUG_TIMER_ACTION) {
            Log.d(TAG, "startDoubleTapShiftKeyTimer");
        }
        final MainKeyboardView keyboardView = getMainKeyboardView();
        if (keyboardView != null) {
            keyboardView.startDoubleTapShiftKeyTimer();
        }
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void cancelDoubleTapShiftKeyTimer() {
        if (DEBUG_TIMER_ACTION) {
            Log.d(TAG, "setAlphabetKeyboard");
        }
        final MainKeyboardView keyboardView = getMainKeyboardView();
        if (keyboardView != null) {
            keyboardView.cancelDoubleTapShiftKeyTimer();
        }
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public boolean isInDoubleTapShiftKeyTimeout() {
        if (DEBUG_TIMER_ACTION) {
            Log.d(TAG, "isInDoubleTapShiftKeyTimeout");
        }
        final MainKeyboardView keyboardView = getMainKeyboardView();
        return keyboardView != null && keyboardView.isInDoubleTapShiftKeyTimeout();
    }

    /**
     * Updates state machine to figure out when to automatically switch back to the previous mode.
     */
    public void onEvent(final Event event, final int currentAutoCapsState,
                        final int currentRecapitalizeState) {
        if (mLatinIME.isSearchGif()) {
            App.getInstance().mPrefs.edit().putBoolean(Settings.PREF_AUTO_CAP, false).apply();
        } else {
            if (App.getInstance().mPrefs.getBoolean(Settings.SAVE_STATE_AUTO_CAPS_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, false)) {
                App.getInstance().mPrefs.edit().putBoolean(Settings.PREF_AUTO_CAP, true).apply();
                App.getInstance().mPrefs.edit().putBoolean(Settings.SAVE_STATE_AUTO_CAPS_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, false).apply();
            }
        }
        mState.onEvent(event, currentAutoCapsState, currentRecapitalizeState);
    }

    public boolean isShowingKeyboardId(@Nonnull int... keyboardIds) {
        if (mKeyboardView == null || !mKeyboardView.isShown()) {
            return false;
        }
        if (mKeyboardView.getKeyboard() != null) {
            int activeKeyboardId = mKeyboardView.getKeyboard().mId.mElementId;
            for (int keyboardId : keyboardIds) {
                if (activeKeyboardId == keyboardId) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isShowingEmojiPalettes() {
        return mEmojiPalettesView != null && mEmojiPalettesView.isShown();
    }

    public boolean isShowingMoreKeysPanel() {
        if (isShowingEmojiPalettes()) {
            return false;
        }
        return mKeyboardView.isShowingMoreKeysPanel();
    }

    public View getVisibleKeyboardView() {
        if (isShowingEmojiPalettes()) {
            return mEmojiPalettesView;
        }
        return mKeyboardView;
    }

    public MainKeyboardView getMainKeyboardView() {
        return mKeyboardView;
    }

    public void deallocateMemory() {
        if (mKeyboardView != null) {
            mKeyboardView.cancelAllOngoingEvents();
            mKeyboardView.deallocateMemory();
        }
        if (mEmojiPalettesView != null) {
            mEmojiPalettesView.stopEmojiPalettes();
        }
    }

    public View onCreateInputView(final boolean isHardwareAcceleratedDrawingEnabled) {
        if (mKeyboardView != null) {
            mKeyboardView.closing();
        }

        updateKeyboardThemeAndContextThemeWrapper(
                mLatinIME, KeyboardTheme.getKeyboardTheme(mLatinIME /* context */));
        //  Timber.d("ducNQ : onCreateInputViewed: ");
        mCurrentInputView = (InputView) LayoutInflater.from(mThemeContext).inflate(
                R.layout.input_view, null);
        mMainKeyboardFrame = mCurrentInputView.findViewById(R.id.main_keyboard_frame);
//        mEmojiPalettesView = mCurrentInputView.findViewById(
//                R.id.emoji_palettes_view);
        copyPasteSelectionView = mCurrentInputView.findViewById(R.id.edit_selection_view);
        mKeyboardView = mCurrentInputView.findViewById(R.id.keyboard_view);
        Timber.d("ducNQ : changeTextColored:a " + System.currentTimeMillis());
        mKeyboardView.setHardwareAcceleratedDrawingEnabled(isHardwareAcceleratedDrawingEnabled);
        mKeyboardView.setKeyboardActionListener(mLatinIME);
//        mEmojiPalettesView.setHardwareAcceleratedDrawingEnabled(
//                isHardwareAcceleratedDrawingEnabled);
//        mEmojiPalettesView.setKeyboardActionListener(mLatinIME);
        initEmojiPalettesView();
        copyPasteSelectionView.setKeyboardActionListener(mLatinIME);
        viewBgDragHeight = mMainKeyboardFrame.findViewById(R.id.vBgDragHeight);
        viewTranslate = mMainKeyboardFrame.findViewById(R.id.viewTranslate);
        viewChooseLanguage = mCurrentInputView.findViewById(R.id.viewChooseLanguage);
        return mCurrentInputView;
    }

    public int getKeyboardShiftMode() {
        final Keyboard keyboard = getKeyboard();
        if (keyboard == null) {
            return WordComposer.CAPS_MODE_OFF;
        }
        switch (keyboard.mId.mElementId) {
            case KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCKED:
            case KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCK_SHIFTED:
                return WordComposer.CAPS_MODE_MANUAL_SHIFT_LOCKED;
            case KeyboardId.ELEMENT_ALPHABET_MANUAL_SHIFTED:
                return WordComposer.CAPS_MODE_MANUAL_SHIFTED;
            case KeyboardId.ELEMENT_ALPHABET_AUTOMATIC_SHIFTED:
                return WordComposer.CAPS_MODE_AUTO_SHIFTED;
            default:
                return WordComposer.CAPS_MODE_OFF;
        }
    }

    private void initEmojiPalettesView() {
        ViewStub stub = mCurrentInputView.findViewById(R.id.stubEmojiPalettes);
        stub.inflate();
        mEmojiPalettesView = mCurrentInputView.findViewById(R.id.emoji_palettes_view);
        mEmojiPalettesView.setVisibility(View.GONE);
        mEmojiPalettesView.setBackground(null);
        mEmojiPalettesView.setHardwareAcceleratedDrawingEnabled(
                mKeyboardView.isHardwareAcceleratedDrawingEnabled);
        mEmojiPalettesView.setKeyboardActionListener(mLatinIME);
        mEmojiPalettesView.getLayoutParams().height = mMainKeyboardFrame.getHeight();
        mEmojiPalettesView.getLayoutParams().width = mMainKeyboardFrame.getWidth();
    }

    public int getCurrentKeyboardScriptId() {
        if (null == mKeyboardLayoutSet) {
            return ScriptUtils.SCRIPT_UNKNOWN;
        }
        return mKeyboardLayoutSet.getScriptId();
    }
//
//    public void hideSuggestion() {
//       // if(mEmojiPalettesView!=null)
//
//        mLatinIME.setSuggestedWords(SuggestedWords.getEmptyInstance());
//
//           // mLatinIME.hideSuggestions();
//       // mEmojiPalettesView.hideSuggestion();
//    }

    public void resetFontKeyBoard() {
        if (mKeyboardView != null) {
            mKeyboardView.loadKeyFont();
            mKeyboardView.invalidate();
        }
    }

    public void checkLanguageToSetFontNormalKeyBoard() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mKeyboardView.isUsingLanguageKeyboardOtherQwerty = Constant.LIST_LANGUAGE_KEYBOARD_OTHER_QWERTY.contains(Settings.getLanguageInputMethod());
            if (mKeyboardView.isUsingLanguageKeyboardOtherQwerty && mThemeContext != null && !Constant.FONT_NORMAL.equals(mPrefs.getString(Constant.USING_FONT, Constant.FONT_NORMAL))) {
                if (mKeyboardView.charSequenceFont.length != 53) {
                    ItemFont fontNormal = new ItemFont();
                    fontNormal.setId(0);
                    fontNormal.setTextFont(Constant.FONT_NORMAL);
                    // mPrefs.edit().putString(Constant.USING_FONT, Constant.FONT_NORMAL).apply();
                    mKeyboardView.loadKeyFont();
                    EventBus.getDefault().post(new ChangeToFontNormal(true, fontNormal));
                }
            }
        }, 500);
    }

    public void setThemeModel(ThemeModel themeModel) {
        if (mKeyboardView != null) mKeyboardView.setThemeModel(themeModel);
    }

    public enum KeyboardSwitchState {
        HIDDEN(-1),
        SYMBOLS_SHIFTED(KeyboardId.ELEMENT_SYMBOLS_SHIFTED),
        EMOJI(KeyboardId.ELEMENT_EMOJI_RECENTS),
        OTHER(-1);

        final int mKeyboardId;

        KeyboardSwitchState(int keyboardId) {
            mKeyboardId = keyboardId;
        }
    }
}
