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

package com.android.inputmethod.latin.suggestions;

import static com.android.inputmethod.latin.common.Constants.NOT_A_COORDINATE;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tapbi.spark.yokey.R;
import com.android.inputmethod.accessibility.AccessibilityUtils;
import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.keyboard.MainKeyboardView;
import com.android.inputmethod.keyboard.MoreKeysPanel;
import com.android.inputmethod.latin.AudioAndHapticFeedbackManager;
import com.android.inputmethod.latin.LatinIME;
import com.android.inputmethod.latin.SuggestedWords;
import com.android.inputmethod.latin.SuggestedWords.SuggestedWordInfo;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.define.DebugFlags;
import com.android.inputmethod.latin.settings.Settings;
import com.android.inputmethod.latin.settings.SettingsValues;
import com.android.inputmethod.latin.suggestions.MoreSuggestionsView.MoreSuggestionsListener;
import com.android.inputmethod.latin.utils.ImportantNoticeUtils;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.ui.adapter.FontOnKeyboardAdapter;
import com.tapbi.spark.yokey.data.model.MessageEvent;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.util.Constant;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import timber.log.Timber;

public final class SuggestionStripView extends RelativeLayout implements OnClickListener,
        OnLongClickListener {

    public interface Listener {
        public void showImportantNoticeContents();

        public void pickSuggestionManually(SuggestedWordInfo word);

        public void onCodeInput(int primaryCode, int x, int y, boolean isKeyRepeat, boolean isEmoji);

        public void onEmojiClick();

        public void onStickerClick();
    }

    static final boolean DBG = DebugFlags.DEBUG_ENABLED;
    private static final float DEBUG_INFO_TEXT_SIZE_IN_DIP = 6.0f;

    public final ViewGroup mSuggestionsStrip;
    public ImageView mVoiceKey;
    private final ImageView mEmojiKey;
    private ImageView mSettingKey, imgClipboard, imgSticker, imgTranslate;
    private final View mImportantNoticeStrip;
    private RecyclerView rclFontKeyboard;
    private FontOnKeyboardAdapter fontOnKeyboardAdapter;
    MainKeyboardView mMainKeyboardView;
    TextView word;
    private final View mMoreSuggestionsContainer;
    private final MoreSuggestionsView mMoreSuggestionsView;
    private final MoreSuggestions.Builder mMoreSuggestionsBuilder;

    private final ArrayList<TextView> mWordViews = new ArrayList<>();
    private final ArrayList<TextView> mDebugInfoViews = new ArrayList<>();
    private final ArrayList<View> mDividerViews = new ArrayList<>();

    Listener mListener;
    private SuggestedWords mSuggestedWords = SuggestedWords.getEmptyInstance();
    private int mStartIndexOfMoreSuggestions;

    private final SuggestionStripLayoutHelper mLayoutHelper;
    private final StripVisibilityGroup mStripVisibilityGroup;

    private ThemeModel themeModel;
    private int color;

    private ImageView imgSelection;

    private Group layoutMenu;

    private LatinIME latinIME;

    private static class StripVisibilityGroup {
        private final View mSuggestionStripView;
        private final View mSuggestionsStrip;
        private final View mImportantNoticeStrip;

        public StripVisibilityGroup(final View suggestionStripView,
                                    final ViewGroup suggestionsStrip, final View importantNoticeStrip) {
            mSuggestionStripView = suggestionStripView;
            mSuggestionsStrip = suggestionsStrip;
            mImportantNoticeStrip = importantNoticeStrip;
            showSuggestionsStrip();
        }

        public void setLayoutDirection(final boolean isRtlLanguage) {
            final int layoutDirection = isRtlLanguage ? ViewCompat.LAYOUT_DIRECTION_RTL
                    : ViewCompat.LAYOUT_DIRECTION_LTR;
            ViewCompat.setLayoutDirection(mSuggestionStripView, layoutDirection);
            ViewCompat.setLayoutDirection(mSuggestionsStrip, layoutDirection);
            ViewCompat.setLayoutDirection(mImportantNoticeStrip, layoutDirection);
        }

        public void showSuggestionsStrip() {
            mSuggestionsStrip.setVisibility(VISIBLE);
            //mImportantNoticeStrip.setVisibility(INVISIBLE);

        }


        public void showImportantNoticeStrip() {
            mSuggestionsStrip.setVisibility(INVISIBLE);
            //  mImportantNoticeStrip.setVisibility(VISIBLE);
        }

        public boolean isShowingImportantNoticeStrip() {
            return mImportantNoticeStrip.getVisibility() == GONE;
        }
    }

    /**
     * Construct a {@link SuggestionStripView} for showing suggestions to be picked by the user.
     *
     * @param context
     * @param attrs
     */
    public SuggestionStripView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.suggestionStripViewStyle);
    }

    public SuggestionStripView(final Context context, final AttributeSet attrs,
                               final int defStyle) {
        super(context, attrs, defStyle);
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.suggestions_strip, this);

        mSuggestionsStrip = findViewById(R.id.suggestions_strip);
        mVoiceKey = findViewById(R.id.suggestions_strip_voice_key);
        mEmojiKey = findViewById(R.id.suggestions_strip_emoji);
        mSettingKey = findViewById(R.id.suggestions_strip_setting_key);
        imgClipboard = findViewById(R.id.imgClipBoard);
        imgSticker = findViewById(R.id.imgSticker);
        imgSelection = findViewById(R.id.selection);
        imgTranslate = findViewById(R.id.translate);
        initRclFont();
        mImportantNoticeStrip = findViewById(R.id.important_notice_strip);
        mStripVisibilityGroup = new StripVisibilityGroup(this, mSuggestionsStrip,
                mImportantNoticeStrip);

        for (int pos = 0; pos < SuggestedWords.MAX_SUGGESTIONS; pos++) {
            word = new TextView(context, null, R.attr.suggestionWordStyle);
            word.setContentDescription(getResources().getString(R.string.spoken_empty_suggestion));
            word.setTextColor(Color.GREEN);
            word.setOnClickListener(this);
            word.setOnLongClickListener(this);
            mWordViews.add(word);
            final View divider = inflater.inflate(R.layout.suggestion_divider, null);

            mDividerViews.add(divider);
            final TextView info = new TextView(context, null, R.attr.suggestionWordStyle);
            //        info.setTextColor(Color.WHITE);
            info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEBUG_INFO_TEXT_SIZE_IN_DIP);
            mDebugInfoViews.add(info);
        }

        mLayoutHelper = new SuggestionStripLayoutHelper(
                context, attrs, defStyle, mWordViews, mDividerViews, mDebugInfoViews);

        mMoreSuggestionsContainer = inflater.inflate(R.layout.more_suggestions, null);
        mMoreSuggestionsView = mMoreSuggestionsContainer
                .findViewById(R.id.more_suggestions_view);
        mMoreSuggestionsBuilder = new MoreSuggestions.Builder(context, mMoreSuggestionsView);

        final Resources res = context.getResources();
        mMoreSuggestionsModalTolerance = res.getDimensionPixelOffset(
                R.dimen.config_more_suggestions_modal_tolerance);
        mMoreSuggestionsSlidingDetector = new GestureDetector(
                context, mMoreSuggestionsSlidingListener);

        final TypedArray keyboardAttr = context.obtainStyledAttributes(attrs,
                R.styleable.Keyboard, defStyle, R.style.SuggestionStripView);


        layoutMenu = findViewById(R.id.layoutGroup);


        //todo rgb keyboard edit
        keyboardAttr.recycle();
        mVoiceKey.setOnClickListener(this);
        mEmojiKey.setOnClickListener(this);
        mSettingKey.setOnClickListener(this);

        imgSelection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //    isSelection = true;
                latinIME.closeTranslate();
                layoutMenu.setVisibility(GONE);
                latinIME.showSelectionView();
                //  EventBus.getDefault().post(new MessageEvent(Constant.EVENT_SELECTION, null));

            }
        });

        imgClipboard.setOnClickListener(v -> {
                    latinIME.closeTranslate();
                    EventBus.getDefault().post(new MessageEvent(Constant.EVENT_SHOW_CLIPBOARD_VIEW_KEYBOARD, null));
                }
        );

        imgSticker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                latinIME.closeTranslate();
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onStickerClick();
                    }
                }, 20);
//                EventBus.getDefault().post(new MessageEvent(Constant.EVENT_STICKER, null));
            }
        });
        imgTranslate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (latinIME.isViewTranslateShow()) {
                    latinIME.resetComposingState(true);
                    clickHideTranslate();
                } else {

                    mListener.onCodeInput(Constants.CODE_ALPHA_FROM_TRANSLATE, NOT_A_COORDINATE, NOT_A_COORDINATE, false, false);
                }
                EventBus.getDefault().post(new MessageEvent(Constant.EVENT_TRANSLATE, null));
            }
        });
    }

    private void clickHideTranslate() {
        latinIME.closeTranslate();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }

    private void initRclFont() {
        rclFontKeyboard = findViewById(R.id.rclFontKeyboard);
        fontOnKeyboardAdapter = new FontOnKeyboardAdapter(App.getInstance().fontRepository.listFontsIsAdd, getContext(), new FontOnKeyboardAdapter.ChangeFontOnKeyboard() {
            @Override
            public void changFont() {
                if (mMainKeyboardView != null) mMainKeyboardView.invalidate();
            }
        });
        rclFontKeyboard.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rclFontKeyboard.setAdapter(fontOnKeyboardAdapter);
    }

    public void setLatinIME(LatinIME latinIME) {
        this.latinIME = latinIME;
    }


    public void hideUIMenuAndSuggest() {
        if (layoutMenu != null) layoutMenu.setVisibility(GONE);
        if (mSuggestionsStrip != null) mSuggestionsStrip.setVisibility(GONE);
    }

    public boolean isShowMenu(){
        if (layoutMenu != null && layoutMenu.getVisibility() == View.VISIBLE){
            return true;
        }
        return false;
    }

    private void showUISuggest() {
        if (mSuggestionsStrip != null) mSuggestionsStrip.setVisibility(VISIBLE);
        if (layoutMenu != null) layoutMenu.setVisibility(GONE);

    }

    public void showUIMenu() {
        if (mSuggestionsStrip != null) mSuggestionsStrip.setVisibility(GONE);
        if (layoutMenu != null) {
            layoutMenu.setVisibility(VISIBLE);
        }
    }

    public void changeListFont() {
        if (fontOnKeyboardAdapter != null)
            fontOnKeyboardAdapter.changeList(App.getInstance().fontRepository.listFontsIsAdd);
    }

    public void changeFont() {
        if (fontOnKeyboardAdapter != null) fontOnKeyboardAdapter.notifyDataSetChanged();
    }

    public void hideUIMenu() {
        if (layoutMenu != null) layoutMenu.setVisibility(GONE);
    }


    public void updateColorFilter(int colorFilter) {
        this.color = colorFilter;
//        imgBackMenu.setColorFilter(color);
        updateSuggestionTextColor(color);

    }

    private void updateSuggestionTextColor(int color) {
        if (mSuggestionsStrip.getChildCount() > 0) {
            for (int i = 0; i < mSuggestionsStrip.getChildCount(); i++) {
                if (mSuggestionsStrip.getChildAt(i) instanceof TextView) {
                    ((TextView) mSuggestionsStrip.getChildAt(i)).setTextColor(color);
                }
            }
        }
    }


    /**
     * A connection back to the input method.
     *
     * @param listener
     */
    public void setListener(final Listener listener, final View inputView) {
        mListener = listener;
        mMainKeyboardView = inputView.findViewById(R.id.keyboard_view);
    }

    public void updateVisibility(final boolean shouldBeVisible, final boolean isFullscreenMode) {
        final int visibility = shouldBeVisible ? VISIBLE : (isFullscreenMode ? GONE : INVISIBLE);
//        setVisibility(visibility);
        final SettingsValues currentSettingsValues = Settings.getInstance().getCurrent();
        // mVoiceKey.setVisibility(currentSettingsValues.mShowsVoiceInputKey ? VISIBLE : INVISIBLE);
        mVoiceKey.setVisibility(VISIBLE);

        //Todo Setbackground for menu RGB Keyboard
//        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
//        themeModel = App.getInstance().themeRepository.getCurrentThemeModel();
//        if(themeModel==null) {
//            String id = mPrefs.getString(CommonVariable.ID_THEME_KEYBOARD_CURRENT, "0");
//            themeModel = CommonUtil.parserJsonFromFileTheme(getContext(), id);
//        }
//        String strColor = String.format("#%06X", 0xFFFFFF & CommonUtil.hex2decimal(themeModel.getMenuBar().getTextColor()));
//        mVoiceKey.setColorFilter(Color.parseColor(strColor), PorterDuff.Mode.MULTIPLY);
//        mEmojiKey.setColorFilter(Color.parseColor(strColor), PorterDuff.Mode.MULTIPLY);
//        mSettingKey.setColorFilter(Color.parseColor(strColor), PorterDuff.Mode.MULTIPLY);
//        imgSelection.setColorFilter(Color.parseColor(strColor), PorterDuff.Mode.MULTIPLY);
//        imgSticker.setColorFilter(Color.parseColor(strColor), PorterDuff.Mode.MULTIPLY);
//        imgClipboard.setColorFilter(Color.parseColor(strColor), PorterDuff.Mode.MULTIPLY);

        changeColorIcon();
    }

    public void changeColorIcon() {
        Timber.e("Duongcv : changeColorIconed: "+App.getInstance().colorIconDefault);
        mVoiceKey.setColorFilter(App.getInstance().colorIconDefault);
        mEmojiKey.setColorFilter(App.getInstance().colorIconDefault);
        mSettingKey.setColorFilter(App.getInstance().colorIconDefault);
        imgSelection.setColorFilter(App.getInstance().colorIconDefault);
        imgSticker.setColorFilter(App.getInstance().colorIconDefault);
        imgClipboard.setColorFilter(App.getInstance().colorIconDefault);
        imgTranslate.setColorFilter(App.getInstance().colorIconDefault);
        if (latinIME != null) {
            latinIME.initColor();
            latinIME.setColorFilterPlateView(App.getInstance().colorIconDefault);
            latinIME.setColorTextClipBoard(App.getInstance().colorIconNew);
            latinIME.setColorForCopyPasteSelectionView(App.getInstance().colorIconNew);
        }
      //  Timber.d("changeColorIcon");
    }

    public void setSuggestions(final SuggestedWords suggestedWords, final boolean isRtlLanguage) {
        clear();
    //    Timber.d("ducNQ : setSuggestions:1 " + latinIME.mInputLogic.mConnection.getmComposingText().length());
    //    Timber.d("ducNQ : setSuggestions:2 " + suggestedWords.size());
        if (suggestedWords.size() == 0 || suggestedWords.getWordCountToShow(
                Settings.getInstance().getCurrent().mShouldShowLxxSuggestionUi) == 0 || (latinIME != null && latinIME.mInputLogic != null && latinIME.mInputLogic.mConnection != null &&
                latinIME.mInputLogic.mConnection.getmComposingText().length() == 0)) {
            showUIMenu();
//            App.getInstance().mPrefs.edit().putBoolean(com.android.inputmethod.latin.settings.Settings.PREF_SHOW_SUGGESTIONS,
//                    true).apply();
        } else {
//            App.getInstance().mPrefs.edit().putBoolean(com.android.inputmethod.latin.settings.Settings.PREF_SHOW_SUGGESTIONS,
//                    false).apply();
            if (App.getInstance().mPrefs.getBoolean(com.android.inputmethod.latin.settings.Settings.PREF_SHOW_SUGGESTIONS, false)) {
                showUISuggest();
                mStripVisibilityGroup.setLayoutDirection(isRtlLanguage);
                mSuggestedWords = suggestedWords;
                mStartIndexOfMoreSuggestions = mLayoutHelper.layoutAndReturnStartIndexOfMoreSuggestions(
                        getContext(), mSuggestedWords, mSuggestionsStrip, this, themeModel);
                mStripVisibilityGroup.showSuggestionsStrip();
            }
        }
    }

    public void hideSuggest() {
        if (mSuggestionsStrip != null) {
            mSuggestionsStrip.setVisibility(GONE);
        }
    }

    public void setMoreSuggestionsHeight(final int remainingHeight) {
        mLayoutHelper.setMoreSuggestionsHeight(remainingHeight);
    }

    // This method checks if we should show the important notice (checks on permanent storage if
    // it has been shown once already or not, and if in the setup wizard). If applicable, it shows
    // the notice. In all cases, it returns true if it was shown, false otherwise.
    public boolean maybeShowImportantNoticeTitle() {
        final SettingsValues currentSettingsValues = Settings.getInstance().getCurrent();
        if (!ImportantNoticeUtils.shouldShowImportantNotice(getContext(), currentSettingsValues)) {
            return false;
        }
        if (getWidth() <= 0) {
            return false;
        }
        final String importantNoticeTitle = ImportantNoticeUtils.getSuggestContactsNoticeTitle(
                getContext());
        if (TextUtils.isEmpty(importantNoticeTitle)) {
            return false;
        }
        if (isShowingMoreSuggestionPanel()) {
            dismissMoreSuggestionsPanel();
        }
        mLayoutHelper.layoutImportantNotice(mImportantNoticeStrip, importantNoticeTitle);
        mStripVisibilityGroup.showImportantNoticeStrip();
        mImportantNoticeStrip.setOnClickListener(this);
        return true;
    }

    public void clear() {
        mSuggestionsStrip.removeAllViews();
        removeAllDebugInfoViews();
        mStripVisibilityGroup.showSuggestionsStrip();
        dismissMoreSuggestionsPanel();
    }

    private void removeAllDebugInfoViews() {
        // The debug info views may be placed as children views of this {@link SuggestionStripView}.
        for (final View debugInfoView : mDebugInfoViews) {
            final ViewParent parent = debugInfoView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(debugInfoView);
            }
        }
    }

    private final MoreSuggestionsListener mMoreSuggestionsListener = new MoreSuggestionsListener() {
        @Override
        public void onSuggestionSelected(final SuggestedWordInfo wordInfo) {
            mListener.pickSuggestionManually(wordInfo);
            dismissMoreSuggestionsPanel();
        }

        @Override
        public void onCancelInput() {
            dismissMoreSuggestionsPanel();
        }
    };

    private final MoreKeysPanel.Controller mMoreSuggestionsController =
            new MoreKeysPanel.Controller() {
                @Override
                public void onDismissMoreKeysPanel() {
                    mMainKeyboardView.onDismissMoreKeysPanel();
                }

                @Override
                public void onShowMoreKeysPanel(final MoreKeysPanel panel) {
                    mMainKeyboardView.onShowMoreKeysPanel(panel);
                }

                @Override
                public void onCancelMoreKeysPanel() {
                    dismissMoreSuggestionsPanel();
                }
            };

    public boolean isShowingMoreSuggestionPanel() {
        return mMoreSuggestionsView.isShowingInParent();
    }

    public void dismissMoreSuggestionsPanel() {
        mMoreSuggestionsView.dismissMoreKeysPanel();
    }

    @Override
    public boolean onLongClick(final View view) {
        AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(
                Constants.NOT_A_CODE, this);
        return showMoreSuggestions();
    }

    boolean showMoreSuggestions() {
        final Keyboard parentKeyboard = mMainKeyboardView.getKeyboard();
        if (parentKeyboard == null) {
            return false;
        }
        final SuggestionStripLayoutHelper layoutHelper = mLayoutHelper;
        if (mSuggestedWords.size() <= mStartIndexOfMoreSuggestions) {
            return false;
        }
        final int stripWidth = getWidth();
        final View container = mMoreSuggestionsContainer;
        final int maxWidth = stripWidth - container.getPaddingLeft() - container.getPaddingRight();
        final MoreSuggestions.Builder builder = mMoreSuggestionsBuilder;
        builder.layout(mSuggestedWords, mStartIndexOfMoreSuggestions, maxWidth,
                (int) (maxWidth * layoutHelper.mMinMoreSuggestionsWidth),
                layoutHelper.getMaxMoreSuggestionsRow(), parentKeyboard);
        mMoreSuggestionsView.setKeyboard(builder.build());
        container.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final MoreKeysPanel moreKeysPanel = mMoreSuggestionsView;
        final int pointX = stripWidth / 2;
        final int pointY = -layoutHelper.mMoreSuggestionsBottomGap;
        moreKeysPanel.showMoreKeysPanel(this, mMoreSuggestionsController, pointX, pointY,
                mMoreSuggestionsListener);
        mOriginX = mLastX;
        mOriginY = mLastY;
        for (int i = 0; i < mStartIndexOfMoreSuggestions; i++) {
            mWordViews.get(i).setPressed(false);
        }
        return true;
    }

    // Working variables for {@link onInterceptTouchEvent(MotionEvent)} and
    // {@link onTouchEvent(MotionEvent)}.
    private int mLastX;
    private int mLastY;
    private int mOriginX;
    private int mOriginY;
    private final int mMoreSuggestionsModalTolerance;
    private boolean mNeedsToTransformTouchEventToHoverEvent;
    private boolean mIsDispatchingHoverEventToMoreSuggestions;
    private final GestureDetector mMoreSuggestionsSlidingDetector;
    private final GestureDetector.OnGestureListener mMoreSuggestionsSlidingListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onScroll(MotionEvent down, MotionEvent me, float deltaX, float deltaY) {
                    final float dy = me.getY() - down.getY();
                    if (deltaY > 0 && dy < 0) {
                        return showMoreSuggestions();
                    }
                    return false;
                }
            };

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent me) {
        if (mStripVisibilityGroup.isShowingImportantNoticeStrip()) {
            return false;
        }
        // Detecting sliding up finger to show {@link MoreSuggestionsView}.
        if (!mMoreSuggestionsView.isShowingInParent()) {
            mLastX = (int) me.getX();
            mLastY = (int) me.getY();
            return mMoreSuggestionsSlidingDetector.onTouchEvent(me);
        }
        if (mMoreSuggestionsView.isInModalMode()) {
            return false;
        }

        final int action = me.getAction();
        final int index = me.getActionIndex();
        final int x = (int) me.getX(index);
        final int y = (int) me.getY(index);
        if (Math.abs(x - mOriginX) >= mMoreSuggestionsModalTolerance
                || mOriginY - y >= mMoreSuggestionsModalTolerance) {
            // Decided to be in the sliding suggestion mode only when the touch point has been moved
            // upward. Further {@link MotionEvent}s will be delivered to
            // {@link #onTouchEvent(MotionEvent)}.
            mNeedsToTransformTouchEventToHoverEvent =
                    AccessibilityUtils.getInstance().isTouchExplorationEnabled();
            mIsDispatchingHoverEventToMoreSuggestions = false;
            return true;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            // Decided to be in the modal input mode.
            mMoreSuggestionsView.setModalMode();
        }
        return false;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event) {
        // Don't populate accessibility event with suggested words and voice key.
        return true;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent me) {
        if (!mMoreSuggestionsView.isShowingInParent()) {
            // Ignore any touch event while more suggestions panel hasn't been shown.
            // Detecting sliding up is done at {@link #onInterceptTouchEvent}.
            return true;
        }
        // In the sliding input mode. {@link MotionEvent} should be forwarded to
        // {@link MoreSuggestionsView}.
        final int index = me.getActionIndex();
        final int x = mMoreSuggestionsView.translateX((int) me.getX(index));
        final int y = mMoreSuggestionsView.translateY((int) me.getY(index));
        me.setLocation(x, y);
        if (!mNeedsToTransformTouchEventToHoverEvent) {
            mMoreSuggestionsView.onTouchEvent(me);
            return true;
        }
        // In sliding suggestion mode with accessibility mode on, a touch event should be
        // transformed to a hover event.
        final int width = mMoreSuggestionsView.getWidth();
        final int height = mMoreSuggestionsView.getHeight();
        final boolean onMoreSuggestions = (x >= 0 && x < width && y >= 0 && y < height);
        if (!onMoreSuggestions && !mIsDispatchingHoverEventToMoreSuggestions) {
            // Just drop this touch event because dispatching hover event isn't started yet and
            // the touch event isn't on {@link MoreSuggestionsView}.
            return true;
        }
        final int hoverAction;
        if (onMoreSuggestions && !mIsDispatchingHoverEventToMoreSuggestions) {
            // Transform this touch event to a hover enter event and start dispatching a hover
            // event to {@link MoreSuggestionsView}.
            mIsDispatchingHoverEventToMoreSuggestions = true;
            hoverAction = MotionEvent.ACTION_HOVER_ENTER;
        } else if (me.getActionMasked() == MotionEvent.ACTION_UP) {
            // Transform this touch event to a hover exit event and stop dispatching a hover event
            // after this.
            mIsDispatchingHoverEventToMoreSuggestions = false;
            mNeedsToTransformTouchEventToHoverEvent = false;
            hoverAction = MotionEvent.ACTION_HOVER_EXIT;
        } else {
            // Transform this touch event to a hover move event.
            hoverAction = MotionEvent.ACTION_HOVER_MOVE;
        }
        me.setAction(hoverAction);
        mMoreSuggestionsView.onHoverEvent(me);
        return true;
    }

    @Override
    public void onClick(final View view) {
        AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(
                Constants.CODE_UNSPECIFIED, this);

        Log.d("duongcv", "onClick: ");
        if (view == mImportantNoticeStrip) {
            latinIME.closeTranslate();
            mListener.showImportantNoticeContents();
            return;
        }
        if (view == mVoiceKey) {
            Log.d("duongcv", "onClick: voice");
            mListener.onCodeInput(Constants.CODE_SHORTCUT,
                    Constants.SUGGESTION_STRIP_COORDINATE, Constants.SUGGESTION_STRIP_COORDINATE,
                    false /* isKeyRepeat */, false);
            return;
        }
        if (view == mEmojiKey) {
            latinIME.closeTranslate();
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mListener.onEmojiClick();
                    latinIME.setColorFilterPlateView(App.getInstance().colorIconDefault);
                }
            }, 10);


        }
        if (view == mSettingKey) {
            latinIME.closeTranslate();
            //todo setting in keyboard show
            if (latinIME != null) latinIME.showSettingView();
        }


        final Object tag = view.getTag();
        // {@link Integer} tag is set at
        // {@link SuggestionStripLayoutHelper#setupWordViewsTextAndColor(SuggestedWords,int)} and
        // {@link SuggestionStripLayoutHelper#layoutPunctuationSuggestions(SuggestedWords,ViewGroup}
        if (tag instanceof Integer) {
            final int index = (Integer) tag;
            if (index >= mSuggestedWords.size()) {
                return;
            }
            final SuggestedWordInfo wordInfo = mSuggestedWords.getInfo(index);
            mListener.pickSuggestionManually(wordInfo);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dismissMoreSuggestionsPanel();
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        // Called by the framework when the size is known. Show the important notice if applicable.
        // This may be overriden by showing suggestions later, if applicable.
        if (oldw <= 0 && w > 0) {
            maybeShowImportantNoticeTitle();
        }
    }

}
