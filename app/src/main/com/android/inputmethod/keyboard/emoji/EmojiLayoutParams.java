/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.inputmethod.keyboard.emoji;

import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;

import com.tapbi.spark.yokey.R;
import com.android.inputmethod.latin.utils.ResourceUtils;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.common.Constant;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.DisplayUtils;

import timber.log.Timber;

final class EmojiLayoutParams {
    private static final int DEFAULT_KEYBOARD_ROWS = 4;

    public final int mEmojiPagerHeight;
    private final int mEmojiPagerBottomMargin;
    public final int mEmojiKeyboardHeight;
    private final int mEmojiCategoryPageIdViewHeight;
    public final int mEmojiActionBarHeight;
    public final int mKeyVerticalGap;
    private final int mKeyHorizontalGap;
    private final int mBottomPadding;
    private final int mTopPadding;
    private SharedPreferences mPrefs;
    public EmojiLayoutParams(final Resources res) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance().getBaseContext());
        float heightRowPercent = (float) mPrefs.getFloat(Constant.HEIGHT_ROW_KEY, Constant.VALUE_HEIGHT_DEFAULT_KEYBOARD);
        int defaultKeyboardHeight = (int) (ResourceUtils.getDefaultKeyboardHeight(res));
        int heightKbNew = 0;
        Timber.d("ducNQ : keyboardHeight: a: "+App.getInstance().getTypeEditing());
        if(App.getInstance().getTypeEditing() == com.tapbi.spark.yokey.util.Constant.TYPE_EDIT_NONE) {
            if (DisplayUtils.getScreenHeight() > DisplayUtils.getScreenWidth()) {
                heightKbNew = (int) mPrefs.getFloat(Constant.HEIGHT_KEYBOARD_NEW, 0);

            } else {
                heightKbNew = (int) mPrefs.getFloat(Constant.HEIGHT_KEYBOARD_NEW_VERTICAL, 0);
            }
        }
        if(heightKbNew!=0) defaultKeyboardHeight = heightKbNew;
        final int defaultKeyboardWidth = ResourceUtils.getDefaultKeyboardWidth(res);
        mKeyVerticalGap = (int) res.getFraction(R.fraction.config_key_vertical_gap_holo, defaultKeyboardHeight, defaultKeyboardHeight);
        mBottomPadding = (int) res.getFraction(R.fraction.config_keyboard_bottom_padding_holo, defaultKeyboardHeight, defaultKeyboardHeight);
        mTopPadding = (int) res.getFraction(R.fraction.config_keyboard_top_padding_holo, defaultKeyboardHeight, defaultKeyboardHeight);
        mKeyHorizontalGap = (int) (res.getFraction(R.fraction.config_key_horizontal_gap_holo, defaultKeyboardWidth, defaultKeyboardWidth));
        mEmojiCategoryPageIdViewHeight = (int) (res.getDimension(R.dimen.config_emoji_category_page_id_height));
        final int baseheight = defaultKeyboardHeight - mBottomPadding - mTopPadding + mKeyVerticalGap;
        mEmojiActionBarHeight = baseheight / DEFAULT_KEYBOARD_ROWS - (mKeyVerticalGap - mBottomPadding) / 2;

        mEmojiPagerHeight = defaultKeyboardHeight - CommonUtil.dpToPx(App.getInstance(),40) - mEmojiCategoryPageIdViewHeight;
        mEmojiPagerBottomMargin = 0;
        mEmojiKeyboardHeight = mEmojiPagerHeight - mEmojiPagerBottomMargin - 1;
    }

    public void setPagerProperties(final ViewPager vp, ConstraintLayout constraintLayout) {
        final ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) vp.getLayoutParams();
        lp.height = mEmojiKeyboardHeight;
        vp.setLayoutParams(lp);

        final ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();
        layoutParams.height = mEmojiKeyboardHeight + CommonUtil.dpToPx(App.getInstance(),42);
        constraintLayout.setLayoutParams(layoutParams);
    }

    public void setCategoryPageIdViewProperties(final View v) {
        final ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) v.getLayoutParams();
        lp.height = mEmojiCategoryPageIdViewHeight;
        v.setLayoutParams(lp);
    }

    public int getActionBarHeight() {
        return mEmojiActionBarHeight - mBottomPadding;
    }

    public void setActionBarProperties(final View ll) {
        final ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) ll.getLayoutParams();
        lp.height = getActionBarHeight();
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        ll.setLayoutParams(lp);
    }

    public void setKeyProperties(final View v) {
        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.leftMargin = mKeyHorizontalGap / 2;
        lp.rightMargin = mKeyHorizontalGap / 2;
        v.setLayoutParams(lp);
    }
}
