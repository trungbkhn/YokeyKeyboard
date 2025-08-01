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

package com.android.inputmethod.latin.settings;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import androidx.core.app.ActivityCompat;
import android.view.MenuItem;

import com.android.inputmethod.latin.permissions.PermissionsManager;
import com.android.inputmethod.latin.utils.FragmentUtils;
import com.android.inputmethod.latin.utils.StatsUtils;
import com.tapbi.spark.yokey.ui.welcome.WelcomeActivity;

public final class SettingsActivity extends PreferenceActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String DEFAULT_FRAGMENT = SettingsFragment.class.getName();

    public static final String EXTRA_SHOW_HOME_AS_UP = "show_home_as_up";
    public static final String EXTRA_ENTRY_KEY = "entry";
    public static final String EXTRA_ENTRY_VALUE_LONG_PRESS_COMMA = "long_press_comma";
    public static final String EXTRA_ENTRY_VALUE_APP_ICON = "app_icon";
    public static final String EXTRA_ENTRY_VALUE_NOTICE_DIALOG = "important_notice";
    public static final String EXTRA_ENTRY_VALUE_SYSTEM_SETTINGS = "system_settings";

    private boolean mShowHomeAsUp;

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        final ActionBar actionBar = getActionBar();
        final Intent intent = getIntent();
        if (actionBar != null) {
            mShowHomeAsUp = intent.getBooleanExtra(EXTRA_SHOW_HOME_AS_UP, true);
            actionBar.setDisplayHomeAsUpEnabled(mShowHomeAsUp);
            actionBar.setHomeButtonEnabled(mShowHomeAsUp);
        }
        StatsUtils.onSettingsActivity(
                intent.hasExtra(EXTRA_ENTRY_KEY) ? intent.getStringExtra(EXTRA_ENTRY_KEY)
                        : EXTRA_ENTRY_VALUE_SYSTEM_SETTINGS);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (mShowHomeAsUp && item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Intent getIntent() {
        Intent intent = new Intent();
        intent.setClass(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
        return intent;
    }

    @Override
    public boolean isValidFragment(final String fragmentName) {
        return FragmentUtils.isValidFragment(fragmentName);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        PermissionsManager.get(this).onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
