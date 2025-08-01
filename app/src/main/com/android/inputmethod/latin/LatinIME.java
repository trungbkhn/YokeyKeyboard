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

package com.android.inputmethod.latin;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.android.inputmethod.latin.common.Constants.ImeOption.FORCE_ASCII;
import static com.android.inputmethod.latin.common.Constants.ImeOption.NO_MICROPHONE;
import static com.android.inputmethod.latin.common.Constants.ImeOption.NO_MICROPHONE_COMPAT;
import static com.tapbi.spark.yokey.common.Constant.SCREEN_STICKER;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.inputmethod.accessibility.AccessibilityUtils;
import com.android.inputmethod.annotations.UsedForTesting;
import com.android.inputmethod.compat.BuildCompatUtils;
import com.android.inputmethod.compat.EditorInfoCompatUtils;
import com.android.inputmethod.compat.InputMethodServiceCompatUtils;
import com.android.inputmethod.compat.SuggestionSpanUtils;
import com.android.inputmethod.compat.ViewOutlineProviderCompatUtils;
import com.android.inputmethod.compat.ViewOutlineProviderCompatUtils.InsetsUpdater;
import com.android.inputmethod.dictionarypack.DictionaryPackConstants;
import com.android.inputmethod.event.Event;
import com.android.inputmethod.event.HardwareEventDecoder;
import com.android.inputmethod.event.HardwareKeyboardEventDecoder;
import com.android.inputmethod.event.InputTransaction;
import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.keyboard.KeyboardActionListener;
import com.android.inputmethod.keyboard.KeyboardId;
import com.android.inputmethod.keyboard.KeyboardLayoutSet;
import com.android.inputmethod.keyboard.KeyboardSwitcher;
import com.android.inputmethod.keyboard.MainKeyboardView;
import com.android.inputmethod.keyboard.StateKeyboardInfo;
import com.android.inputmethod.keyboard.emoji.EmojiPalettesView;
import com.android.inputmethod.keyboard.translate.ViewChooseLanguage;
import com.android.inputmethod.keyboard.translate.ViewTranslate;
import com.android.inputmethod.latin.Suggest.OnGetSuggestedWordsCallback;
import com.android.inputmethod.latin.SuggestedWords.SuggestedWordInfo;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.common.CoordinateUtils;
import com.android.inputmethod.latin.common.InputPointers;
import com.android.inputmethod.latin.common.StringUtils;
import com.android.inputmethod.latin.define.DebugFlags;
import com.android.inputmethod.latin.define.ProductionFlags;
import com.android.inputmethod.latin.inputlogic.InputLogic;
import com.android.inputmethod.latin.permissions.PermissionsManager;
import com.android.inputmethod.latin.personalization.PersonalizationHelper;
import com.android.inputmethod.latin.settings.Settings;
import com.android.inputmethod.latin.settings.SettingsValues;
import com.android.inputmethod.latin.spellcheck.VietnameseSpellChecker;
import com.android.inputmethod.latin.suggestions.SuggestionStripView;
import com.android.inputmethod.latin.suggestions.SuggestionStripViewAccessor;
import com.android.inputmethod.latin.touchinputconsumer.GestureConsumer;
import com.android.inputmethod.latin.utils.ApplicationUtils;
import com.android.inputmethod.latin.utils.ImportantNoticeUtils;
import com.android.inputmethod.latin.utils.InputTypeUtils;
import com.android.inputmethod.latin.utils.JniUtils;
import com.android.inputmethod.latin.utils.LeakGuardHandlerWrapper;
import com.android.inputmethod.latin.utils.StatsUtils;
import com.android.inputmethod.latin.utils.StatsUtilsManager;
import com.android.inputmethod.latin.utils.SubtypeLocaleUtils;
import com.android.inputmethod.latin.utils.ViewLayoutUtils;
import com.giphy.sdk.core.models.Image;
import com.giphy.sdk.core.models.Media;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.R;
import com.tapbi.spark.yokey.common.FloatingKb;
import com.tapbi.spark.yokey.data.local.entity.ItemFont;
import com.tapbi.spark.yokey.data.model.Font;
import com.tapbi.spark.yokey.data.model.LanguageTranslate;
import com.tapbi.spark.yokey.data.model.MessageEvent;
import com.tapbi.spark.yokey.data.repository.ClipboardRepository;
import com.tapbi.spark.yokey.ui.adapter.AllFontOnKeyboardAdapter;
import com.tapbi.spark.yokey.ui.adapter.ClipBoardAdapter;
import com.tapbi.spark.yokey.ui.custom.view.CopyPasteSelectionView;
import com.tapbi.spark.yokey.ui.custom.view.DragChangeSizeHeightKbView;
import com.tapbi.spark.yokey.ui.custom.view.blurBg.RealtimeBlurViewKB;
import com.tapbi.spark.yokey.ui.main.MainActivity;
import com.tapbi.spark.yokey.ui.splash.SplashActivity;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.Constant;
import com.tapbi.spark.yokey.util.DisplayUtils;
import com.tapbi.spark.yokey.util.LocaleUtils;
import com.tapbi.spark.yokey.util.UpdateAppManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Input method implementation for Qwerty'ish keyboard.
 */
public class LatinIME extends InputMethodService implements KeyboardActionListener,
        SuggestionStripView.Listener, SuggestionStripViewAccessor,
        DictionaryFacilitator.DictionaryInitializationListener,
        PermissionsManager.PermissionsResultCallback, ViewTranslate.IListenerTranslate, ViewChooseLanguage.IListenerSetLanguageTranslate {
    static final String TAG = LatinIME.class.getSimpleName();
    static final long DELAY_WAIT_FOR_DICTIONARY_LOAD_MILLIS = TimeUnit.SECONDS.toMillis(2);
    static final long DELAY_DEALLOCATE_MEMORY_MILLIS = TimeUnit.SECONDS.toMillis(10);
    private static final boolean TRACE = false;
    private static final int EXTENDED_TOUCHABLE_REGION_HEIGHT = 100;
    private static final int PERIOD_FOR_AUDIO_AND_HAPTIC_FEEDBACK_IN_KEY_REPEAT = 2;
    private static final int PENDING_IMS_CALLBACK_DURATION_MILLIS = 800;
    /**
     * The name of the scheme used by the Package Manager to warn of a new package installation,
     * replacement or removal.
     */
    private static final String SCHEME_PACKAGE = "package";
    private static final int SPACE_STATE_NONE = 0;
    private static final int SPACE_STATE_DOUBLE = 1;
    private static final int SPACE_STATE_SWAP_PUNCTUATION = 2;
    private static final int SPACE_STATE_WEAK = 3;
    private static final int SPACE_STATE_PHANTOM = 4;
    private static final StringBuilder mTempCurrentWord = new StringBuilder(20);
    private static final int QUICK_PRESS = 200;
    private final static int[] VN_DOUBLE_CHARS_TELEX = {
            'd',
            'a',
            'e',
            'o',
            'w'};
    private final static int[] VN_DOUBLE_CHARS_VNI = {
            '9',
            '6',
            '7',
            '8'};
    private static final int[][] VN_CHARS_TO_FIND_TELEX = {
            {
                    'd',        /* d */
                    'D',
                    'đ',        /* fall back */
                    'Đ'
            },
            {
                    'a', 'á', 'à', 'ả', 'ã', 'ạ',       /* a */
                    'A', 'Á', 'À', 'Ả', 'Ã', 'Ạ',
                    'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ',
                    'Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ',
                    'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ',       /* fall back */
                    'Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ'
            },
            {
                    'e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ',       /* e */
                    'E', 'É', 'È', 'Ẻ', 'Ẽ', 'Ẹ',
                    'ê', 'ế', 'ề', 'ể', 'ễ', 'ệ',       /* fall back */
                    'Ê', 'Ế', 'Ề', 'Ể', 'Ễ', 'Ệ'
            },
            {
                    'o', 'ó', 'ò', 'ỏ', 'õ', 'ọ',       /* o */
                    'O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ',
                    'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ',
                    'Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ',
                    'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ',       /* fall back */
                    'Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ'
            },
            {
                    'a', 'á', 'à', 'ả', 'ã', 'ạ',       /* w */
                    'A', 'Á', 'À', 'Ả', 'Ã', 'Ạ',
                    'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ',
                    'Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ',
                    'o', 'ó', 'ò', 'ỏ', 'õ', 'ọ',
                    'O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ',
                    'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ',
                    'Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ',
                    'u', 'ú', 'ù', 'ủ', 'ũ', 'ụ',
                    'U', 'Ú', 'Ù', 'Ủ', 'Ũ', 'Ụ',
                    'ư', 'Ư',       /* fall back */
                    'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ',
                    'Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ',
                    'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ',
                    'Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ',
                    'ứ', 'ừ', 'ử', 'ữ', 'ự',
                    'Ứ', 'Ừ', 'Ử', 'Ữ', 'Ự'
            }
    };
    private static final int[][] VN_CHARS_TO_FIND_VNI = {
            {
                    'd',        /* 9 */
                    'D',
                    'đ',        /*fall back*/
                    'Đ'
            },
            {
                    'a', 'á', 'à', 'ả', 'ã', 'ạ',        /* 6 */
                    'A', 'Á', 'À', 'Ả', 'Ã', 'Ạ',
                    'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ',
                    'Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ',
                    'e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ',
                    'E', 'É', 'È', 'Ẻ', 'Ẽ', 'Ẹ',
                    'o', 'ó', 'ò', 'ỏ', 'õ', 'ọ',
                    'O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ',
                    'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ',
                    'Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ',

                    'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ',       /* fall back */
                    'Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ',
                    'ê', 'ế', 'ề', 'ể', 'ễ', 'ệ',
                    'Ê', 'Ế', 'Ề', 'Ể', 'Ễ', 'Ệ',
                    'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ',
                    'Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ',
            },
            {
                    'o', 'ó', 'ò', 'ỏ', 'õ', 'ọ',        /* 7 */
                    'O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ',
                    'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ',
                    'Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ',
                    'u', 'ú', 'ù', 'ủ', 'ũ', 'ụ',
                    'U', 'Ú', 'Ù', 'Ủ', 'Ũ', 'Ụ',
                    'ư', 'Ư',
                    'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ',       /* fall back */
                    'Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ',
                    'ứ', 'ừ', 'ử', 'ữ', 'ự',
                    'Ứ', 'Ừ', 'Ử', 'Ữ', 'Ự'
            },
            {
                    'a', 'á', 'à', 'ả', 'ã', 'ạ',       /* 8 */
                    'A', 'Á', 'À', 'Ả', 'Ã', 'Ạ',
                    'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ',
                    'Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ',
                    'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ',       /* fall back */
                    'Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ',
            }
    };
    private static final int[][] VN_CHARS_TO_REPLACE_TELEX = {
            {'đ',
                    'Đ',
                    'd',
                    'D'
            },
            {'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ',
                    'Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ',
                    'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ',
                    'Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ',
                    'a', 'á', 'à', 'ả', 'ã', 'ạ',
                    'A', 'Á', 'À', 'Ả', 'Ã', 'Ạ'
            },
            {
                    'ê', 'ế', 'ề', 'ể', 'ễ', 'ệ',
                    'Ê', 'Ế', 'Ề', 'Ể', 'Ễ', 'Ệ',
                    'e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ',
                    'E', 'É', 'È', 'Ẻ', 'Ẽ', 'Ẹ'
            },
            {
                    'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ',
                    'Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ',
                    'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ',
                    'Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ',
                    'o', 'ó', 'ò', 'ỏ', 'õ', 'ọ',
                    'O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ'
            },
            {
                    'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ',
                    'Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ',
                    'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ',
                    'Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ',
                    'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ',
                    'Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ',
                    'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ',
                    'Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ',
                    'ư', 'ứ', 'ừ', 'ử', 'ữ', 'ự',
                    'Ư', 'Ứ', 'Ừ', 'Ử', 'Ữ', 'Ự',
                    'u', 'U',
                    'a', 'á', 'à', 'ả', 'ã', 'ạ',
                    'A', 'Á', 'À', 'Ả', 'Ã', 'Ạ',
                    'o', 'ó', 'ò', 'ỏ', 'õ', 'ọ',
                    'O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ',
                    'ú', 'ù', 'ủ', 'ũ', 'ụ',
                    'Ú', 'Ù', 'Ủ', 'Ũ', 'Ụ'
            }
    };
    private static final int[] VN_FALLBACKS_TELEX = {
            2,
            24,
            12,
            24,
            60
    };
    private static final int[] VN_TONE_MARKERS_TELEX = {'z', 's', 'f', 'r', 'x', 'j'};
    private static final int[] VN_TONE_MARKERS_QWERTY_VIETNAM = {'z', '´', '`', '᾿', '˜', '․', 's', 'f', 'r', 'x', 'j'};
    private static final String[][] VN_DOUBLE_VOWELS_FIND_TELEX = {
            /*d*/
            {/*empty*/},
            /*a*/
            {
                    "au", "áu", "àu", "ảu", "ãu", "ạu",
                    "ay", "áy", "ày", "ảy", "ãy", "ạy",
                    "ua", "úa", "ùa", "ủa", "ũa", "ụa",
                    "uă", "uắ", "uằ", "uẳ", "uẵ", "uặ",
                    "ưa", "ứa", "ừa", "ửa", "ữa", "ựa",

                    "ai", "ái", "ài", "ải", "ãi", "ại",     /* fall back */
                    "ao", "áo", "ào", "ảo", "ão", "ạo",
                    "ia", "ía", "ìa", "ỉa", "ĩa", "ịa",
                    "oa", "óa", "òa", "ỏa", "õa", "ọa",
                    "oă", "oắ", "oằ", "oẳ", "oẵ", "oặ",
                    "âu", "ấu", "ầu", "ẩu", "ẫu", "ậu",
                    "ây", "ấy", "ầy", "ẩy", "ẫy", "ậy",
                    "uâ", "uấ", "uầ", "uẩ", "uẫ", "uậ"
            },
            /*e*/
            {
                    "ie", "íe", "ìe", "ỉe", "ĩe", "ịe",
                    "ye", "ýe", "ỳe", "ỷe", "ỹe", "ỵe",

                    "êu", "ếu", "ều", "ểu", "ễu", "ệu",     /* fall back */
                    "iê", "iế", "iề", "iể", "iễ", "iệ",
                    "yê", "yế", "yề", "yể", "yễ", "yệ",
                    "uê", "uế", "uề", "uể", "uễ", "uệ",
                    "eo", "éo", "èo", "ẻo", "ẽo", "ẹo",
                    "oe", "óe", "òe", "ỏe", "õe", "ọe"
            },
            /*o*/
            {
                    "oi", "ói", "òi", "ỏi", "õi", "ọi",
                    "ơi", "ới", "ời", "ởi", "ỡi", "ợi",
                    "uo", "úo", "ùo", "ủo", "ũo", "ụo",
                    "uơ", "uớ", "uờ", "uở", "uỡ", "uợ",
                    "ươ", "ướ", "ườ", "ưở", "ưỡ", "ượ",

                    "ao", "áo", "ào", "ảo", "ão", "ạo",     /* fall back */
                    "eo", "éo", "èo", "ẻo", "ẽo", "ẹo",
                    "oa", "óa", "òa", "ỏa", "õa", "ọa",
                    "oă", "oắ", "oằ", "oẳ", "oẵ", "oặ",
                    "oe", "óe", "òe", "ỏe", "õe", "ọe",
                    "ôi", "ối", "ồi", "ổi", "ỗi", "ội",
                    "uô", "uố", "uồ", "uổ", "uỗ", "uộ"
            },
            /*w-missing*/
            {
                    "ua", "úa", "ùa", "ủa", "ũa", "ụa",
                    "uo", "úo", "ùo", "ủo", "ũo", "ụo",
                    "uô", "uố", "uồ", "uổ", "uỗ", "uộ",
                    "uu", "úu", "ùu", "ủu", "ũu", "ụu",

                    "uâ", "uấ", "uầ", "uẩ", "uẫ", "uậ",     /* fall back */
                    "êu", "ếu", "ều", "ểu", "ễu", "ệu",
                    "uê", "uế", "uề", "uể", "uễ", "uệ",
                    "uy", "úy", "ùy", "ủy", "ũy", "ụy",
                    "uă", "uắ", "uằ", "uẳ", "uẵ", "uặ",
                    "ưa", "ứa", "ừa", "ửa", "ữa", "ựa",
                    "uơ", "uớ", "uờ", "uở", "uỡ", "uợ",
                    "ươ", "ướ", "ườ", "ưở", "ưỡ", "ượ",
                    "ưu", "ứu", "ừu", "ửu", "ữu", "ựu",
                    "au", "áu", "àu", "ảu", "ãu", "ạu",
                    "âu", "ấu", "ầu", "ẩu", "ẫu", "ậu",
                    "oe", "óe", "òe", "ỏe", "õe", "ọe",
                    "ôe", "ốe", "ồe", "ổe", "ỗe", "ộe",
                    "ơe", "ớe", "ờe", "ởe", "ỡe", "ợe",
                    "ao", "ào", "áo", "ảo", "ão", "ạo",
            }};
    private static final String[][] VN_DOUBLE_VOWELS_REPLACE_TELEX = {
            /*d*/
            {/*empty*/},
            /*a*/
            {
                    "âu", "ấu", "ầu", "ẩu", "ẫu", "ậu",
                    "ây", "ấy", "ầy", "ẩy", "ẫy", "ậy",
                    "uâ", "uấ", "uầ", "uẩ", "uẫ", "uậ",
                    "uâ", "uấ", "uầ", "uẩ", "uẫ", "uậ",
                    "uâ", "uấ", "uầ", "uẩ", "uẫ", "uậ",

                    "ai", "ái", "ài", "ải", "ãi", "ại",     /* fall back */
                    "ao", "áo", "ào", "ảo", "ão", "ạo",
                    "ia", "ía", "ìa", "ỉa", "ĩa", "ịa",
                    "oa", "óa", "òa", "ỏa", "õa", "ọa",
                    "oă", "oắ", "oằ", "oẳ", "oẵ", "oặ",
                    "au", "áu", "àu", "ảu", "ãu", "ạu",
                    "ay", "áy", "ày", "ảy", "ãy", "ạy",
                    "ua", "úa", "ùa", "ủa", "ũa", "ụa"
            },
            /*e*/
            {
                    "iê", "iế", "iề", "iể", "iễ", "iệ",
                    "yê", "yế", "yề", "yể", "yễ", "yệ",

                    "eu", "éu", "èu", "ẻu", "ẽu", "ẹu",     /* fall back */
                    "ie", "ié", "iè", "iẻ", "iẽ", "iẹ",
                    "ye", "yé", "yè", "yẻ", "yẽ", "yẹ",
                    "ue", "ué", "uè", "uẻ", "uẽ", "uẹ",
                    "eo", "éo", "èo", "ẻo", "ẽo", "ẹo",
                    "oe", "óe", "òe", "ỏe", "õe", "ọe"
            },
            /*o*/
            {
                    "ôi", "ối", "ồi", "ổi", "ỗi", "ội",
                    "ôi", "ối", "ồi", "ổi", "ỗi", "ội",
                    "uô", "uố", "uồ", "uổ", "uỗ", "uộ",
                    "uô", "uố", "uồ", "uổ", "uỗ", "uộ",
                    "uô", "uố", "uồ", "uổ", "uỗ", "uộ",

                    "ao", "áo", "ào", "ảo", "ão", "ạo",     /* fall back */
                    "eo", "éo", "èo", "ẻo", "ẽo", "ẹo",
                    "oa", "óa", "òa", "ỏa", "õa", "ọa",
                    "oă", "oắ", "oằ", "oẳ", "oẵ", "oặ",
                    "oe", "óe", "òe", "ỏe", "õe", "ọe",
                    "oi", "ói", "òi", "ỏi", "õi", "ọi",
                    "uo", "úo", "ùo", "ủo", "ũo", "ụo",
            },
            /*w-missing*/
            {
                    "ưa", "ứa", "ừa", "ửa", "ữa", "ựa",
                    "ươ", "ướ", "ườ", "ưở", "ưỡ", "ượ",
                    "uơ", "uớ", "uờ", "uở", "uỡ", "uợ",
                    "ưu", "ứu", "ừu", "ửu", "ữu", "ựu",

                    "uâ", "uấ", "uầ", "uẩ", "uẫ", "uậ",     /* fall back */
                    "êu", "ếu", "ều", "ểu", "ễu", "ệu",
                    "uê", "uế", "uề", "uể", "uễ", "uệ",
                    "uy", "úy", "ùy", "ủy", "ũy", "ụy",
                    "ua", "úa", "ùa", "ủa", "ũa", "ụa",
                    "ua", "úa", "ùa", "ủa", "ũa", "ụa",
                    "uo", "úo", "ùo", "ủo", "ũo", "ụo",
                    "uo", "úo", "ùo", "ủo", "ũo", "ụo",
                    "uu", "úu", "ùu", "ủu", "ũu", "ụu",
                    "au", "áu", "àu", "ảu", "ãu", "ạu",
                    "âu", "ấu", "ầu", "ẩu", "ẫu", "ậu",
                    "oe", "óe", "òe", "ỏe", "õe", "ọe",
                    "ôe", "ốe", "ồe", "ổe", "ỗe", "ộe",
                    "ơe", "ớe", "ờe", "ởe", "ỡe", "ợe",
                    "ao", "ào", "áo", "ảo", "ão", "ạo",
            }};
    private static final int[] VN_DOUBLE_FALLBACKS_TELEX = {
            0,
            30,
            12,
            30,
            24
    };
    /*VNI*/
    private static final int[][] VN_CHARS_TO_REPLACE_VNI = {
            {
                    'đ',        /*9*/
                    'Đ',
                    'd',
                    'D'
            },
            {
                    'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ',       /*6*/
                    'Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ',
                    'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ',
                    'Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ',
                    'ê', 'ế', 'ề', 'ể', 'ễ', 'ệ',
                    'Ê', 'Ế', 'Ề', 'Ể', 'Ễ', 'Ệ',
                    'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ',
                    'Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ',
                    'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ',
                    'Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ',

                    'a', 'á', 'à', 'ả', 'ã', 'ạ',
                    'A', 'Á', 'À', 'Ả', 'Ã', 'Ạ',
                    'e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ',
                    'E', 'É', 'È', 'Ẻ', 'Ẽ', 'Ẹ',
                    'o', 'ó', 'ò', 'ỏ', 'õ', 'ọ',
                    'O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ',
            },
            {
                    'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ',       /*7*/
                    'Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ',
                    'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ',
                    'Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ',
                    'ư', 'ứ', 'ừ', 'ử', 'ữ', 'ự',
                    'Ư', 'Ứ', 'Ừ', 'Ử', 'Ữ', 'Ự',
                    'u', 'U',
                    'o', 'ó', 'ò', 'ỏ', 'õ', 'ọ',
                    'O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ',
                    'ú', 'ù', 'ủ', 'ũ', 'ụ',
                    'Ú', 'Ù', 'Ủ', 'Ũ', 'Ụ'
            },
            {
                    'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ',       /*8*/
                    'Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ',
                    'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ',
                    'Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ',
                    'a', 'á', 'à', 'ả', 'ã', 'ạ',
                    'A', 'Á', 'À', 'Ả', 'Ã', 'Ạ'
            }
    };
    private static final int[] VN_FALLBACKS_VNI = {
            2,
            60,
            36,
            24
    };
    private static final int[] VN_TONE_MARKERS_VNI = {'0', '1', '2', '3', '4', '5'};
    private static final String[][] VN_DOUBLE_VOWELS_FIND_VNI = {
            /* 9 */
            {
                    /* empty */
            },
            /* 6 */
            {
                    "au", "áu", "àu", "ảu", "ãu", "ạu",
                    "ay", "áy", "ày", "ảy", "ãy", "ạy",
                    "ua", "úa", "ùa", "ủa", "ũa", "ụa",
                    "uă", "uắ", "uằ", "uẳ", "uẵ", "uặ",
                    "ưa", "ứa", "ừa", "ửa", "ữa", "ựa",
                    "ie", "íe", "ìe", "ỉe", "ĩe", "ịe",
                    "ye", "ýe", "ỳe", "ỷe", "ỹe", "ỵe",
                    "oi", "ói", "òi", "ỏi", "õi", "ọi",
                    "ơi", "ới", "ời", "ởi", "ỡi", "ợi",
                    "uo", "úo", "ùo", "ủo", "ũo", "ụo",
                    "uơ", "uớ", "uờ", "uở", "uỡ", "uợ",
                    "ươ", "ướ", "ườ", "ưở", "ưỡ", "ượ",

                    "ai", "ái", "ài", "ải", "ãi", "ại",     /* fall back */
                    "ao", "áo", "ào", "ảo", "ão", "ạo",
                    "ia", "ía", "ìa", "ỉa", "ĩa", "ịa",
                    "oa", "óa", "òa", "ỏa", "õa", "ọa",
                    "oă", "oắ", "oằ", "oẳ", "oẵ", "oặ",
                    "âu", "ấu", "ầu", "ẩu", "ẫu", "ậu",
                    "ây", "ấy", "ầy", "ẩy", "ẫy", "ậy",
                    "uâ", "uấ", "uầ", "uẩ", "uẫ", "uậ",
                    "êu", "ếu", "ều", "ểu", "ễu", "ệu",
                    "iê", "iế", "iề", "iể", "iễ", "iệ",
                    "yê", "yế", "yề", "yể", "yễ", "yệ",
                    "uê", "uế", "uề", "uể", "uễ", "uệ",
                    "eo", "éo", "èo", "ẻo", "ẽo", "ẹo",
                    "oe", "óe", "òe", "ỏe", "õe", "ọe",
                    "ao", "áo", "ào", "ảo", "ão", "ạo",
                    "eo", "éo", "èo", "ẻo", "ẽo", "ẹo",
                    "oa", "óa", "òa", "ỏa", "õa", "ọa",
                    "oă", "oắ", "oằ", "oẳ", "oẵ", "oặ",
                    "oe", "óe", "òe", "ỏe", "õe", "ọe",
                    "ôi", "ối", "ồi", "ổi", "ỗi", "ội",
                    "uô", "uố", "uồ", "uổ", "uỗ", "uộ",
            },
            /* 7 missing */
            {
                    "uo", "úo", "ùo", "ủo", "ũo", "ụo",
                    "uô", "uố", "uồ", "uổ", "uỗ", "uộ",
                    "uu", "úu", "ùu", "ủu", "ũu", "ụu",

                    "êu", "ếu", "ều", "ểu", "ễu", "ệu",     /* fall back */
                    "uê", "uế", "uề", "uể", "uễ", "uệ",
                    "uy", "úy", "ùy", "ủy", "ũy", "ụy",
                    "uă", "uắ", "uằ", "uẳ", "uẵ", "uặ",
                    "ưa", "ứa", "ừa", "ửa", "ữa", "ựa",
                    "uơ", "uớ", "uờ", "uở", "uỡ", "uợ",
                    "ươ", "ướ", "ườ", "ưở", "ưỡ", "ượ",
                    "ưu", "ứu", "ừu", "ửu", "ữu", "ựu",
                    "au", "áu", "àu", "ảu", "ãu", "ạu",
                    "âu", "ấu", "ầu", "ẩu", "ẫu", "ậu",
                    "oe", "óe", "òe", "ỏe", "õe", "ọe",
                    "ôe", "ốe", "ồe", "ổe", "ỗe", "ộe",
                    "ơe", "ớe", "ờe", "ởe", "ỡe", "ợe",
            },
            /* 8 */
            {
                    "ua", "úa", "ùa", "ủa", "ũa", "ụa",

                    "uâ", "uấ", "uầ", "uẩ", "uẫ", "uậ",     /* fall back */
            }};
    private static final String[][] VN_DOUBLE_VOWELS_REPLACE_VNI = {
            /* 9 */
            {
                    /* empty */
            },
            /* 6 */
            {
                    "âu", "ấu", "ầu", "ẩu", "ẫu", "ậu",
                    "ây", "ấy", "ầy", "ẩy", "ẫy", "ậy",
                    "uâ", "uấ", "uầ", "uẩ", "uẫ", "uậ",
                    "uâ", "uấ", "uầ", "uẩ", "uẫ", "uậ",
                    "uâ", "uấ", "uầ", "uẩ", "uẫ", "uậ",
                    "iê", "iế", "iề", "iể", "iễ", "iệ",
                    "yê", "yế", "yề", "yể", "yễ", "yệ",
                    "ôi", "ối", "ồi", "ổi", "ỗi", "ội",
                    "ôi", "ối", "ồi", "ổi", "ỗi", "ội",
                    "uô", "uố", "uồ", "uổ", "uỗ", "uộ",
                    "uô", "uố", "uồ", "uổ", "uỗ", "uộ",
                    "uô", "uố", "uồ", "uổ", "uỗ", "uộ",

                    "ai", "ái", "ài", "ải", "ãi", "ại",     /* fall back */
                    "ao", "áo", "ào", "ảo", "ão", "ạo",
                    "ia", "ía", "ìa", "ỉa", "ĩa", "ịa",
                    "oa", "óa", "òa", "ỏa", "õa", "ọa",
                    "oă", "oắ", "oằ", "oẳ", "oẵ", "oặ",
                    "au", "áu", "àu", "ảu", "ãu", "ạu",
                    "ay", "áy", "ày", "ảy", "ãy", "ạy",
                    "ua", "úa", "ùa", "ủa", "ũa", "ụa",
                    "eu", "éu", "èu", "ẻu", "ẽu", "ẹu",
                    "ie", "ié", "iè", "iẻ", "iẽ", "iẹ",
                    "ye", "yé", "yè", "yẻ", "yẽ", "yẹ",
                    "ue", "ué", "uè", "uẻ", "uẽ", "uẹ",
                    "eo", "éo", "èo", "ẻo", "ẽo", "ẹo",
                    "oe", "óe", "òe", "ỏe", "õe", "ọe",
                    "ao", "áo", "ào", "ảo", "ão", "ạo",
                    "eo", "éo", "èo", "ẻo", "ẽo", "ẹo",
                    "oa", "óa", "òa", "ỏa", "õa", "ọa",
                    "oă", "oắ", "oằ", "oẳ", "oẵ", "oặ",
                    "oe", "óe", "òe", "ỏe", "õe", "ọe",
                    "oi", "ói", "òi", "ỏi", "õi", "ọi",
                    "uo", "úo", "ùo", "ủo", "ũo", "ụo",
            },
            /* 7 missing */
            {

                    "ươ", "ướ", "ườ", "ưở", "ưỡ", "ượ",
                    "uơ", "uớ", "uờ", "uở", "uỡ", "uợ",
                    "ưu", "ứu", "ừu", "ửu", "ữu", "ựu",

                    "êu", "ếu", "ều", "ểu", "ễu", "ệu",     /* fall back*/
                    "uê", "uế", "uề", "uể", "uễ", "uệ",
                    "uy", "úy", "ùy", "ủy", "ũy", "ụy",
                    "ua", "úa", "ùa", "ủa", "ũa", "ụa",
                    "ua", "úa", "ùa", "ủa", "ũa", "ụa",
                    "uo", "úo", "ùo", "ủo", "ũo", "ụo",
                    "uo", "úo", "ùo", "ủo", "ũo", "ụo",
                    "uu", "úu", "ùu", "ủu", "ũu", "ụu",
                    "au", "áu", "àu", "ảu", "ãu", "ạu",
                    "âu", "ấu", "ầu", "ẩu", "ẫu", "ậu",
                    "oe", "óe", "òe", "ỏe", "õe", "ọe",
                    "ôe", "ốe", "ồe", "ổe", "ỗe", "ộe",
                    "ơe", "ớe", "ờe", "ởe", "ỡe", "ợe",
            },
            /* 8 */
            {
                    "ưa", "ứa", "ừa", "ửa", "ữa", "ựa",

                    "uâ", "uấ", "uầ", "uẩ", "uẫ", "uậ",     /* fall back*/
            }};
    private static final int[] VN_DOUBLE_FALLBACKS_VNI = {
            0,      /* 9 */
            72,     /* 6 */
            12,     /* 7 */
            30     /* 8 */
    };
    private static final int[] VN_VOWELS = VietnameseSpellChecker.VN_VOWELS;
    private static final int MAX_VOWELS_SEQUENCE = 3;
    private static final char[] VN_CHAR_ADVANCED_VOWEL = {
            'a', 'á', 'à', 'ả', 'ã', 'ạ',
            'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ',
            'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ',
            'e', 'é', 'è', 'ẻ', 'ẽ', 'ẹ',
            'ê', 'ế', 'ề', 'ể', 'ễ', 'ệ',
            'o', 'ó', 'ò', 'ỏ', 'õ', 'ọ',
            'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ',
            'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ',
            'u', 'ú', 'ù', 'ủ', 'ũ', 'ụ',
            'ư', 'ứ', 'ừ', 'ử', 'ữ', 'ự',
            'A', 'Á', 'À', 'Ả', 'Ã', 'Ạ',
            'Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ',
            'Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ',
            'E', 'É', 'È', 'Ẻ', 'Ẽ', 'Ẹ',
            'Ê', 'Ế', 'Ề', 'Ể', 'Ễ', 'Ệ',
            'O', 'Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ',
            'Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ',
            'Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ',
            'U', 'Ú', 'Ù', 'Ủ', 'Ũ', 'Ụ',
            'Ư', 'Ứ', 'Ừ', 'Ử', 'Ữ', 'Ự'
    };
    private static final String[] VN_TRIPLE_VOWELS = {"IÊU", "YÊU", "OAI", "OAO", "OAY", "OEO", "UAO", "UÂY",
            "UÔI", "ƯƠI", "UYA", "UYÊ", "UYU"};
    private static int orientation = 1;

    // Loading the native library eagerly to avoid unexpected UnsatisfiedLinkError at the initial
    // JNI call as much as possible.
    static {
        JniUtils.loadNativeLibrary();
    }

    @UsedForTesting
    public final KeyboardSwitcher mKeyboardSwitcher;
    public final UIHandler mHandler = new UIHandler(this);
    public final Font font = new Font();
    final Settings mSettings;
    // We expect to have only one decoder in almost all cases, hence the default capacity of 1.
    // If it turns out we need several, it will get grown seamlessly.
    final SparseArray<HardwareEventDecoder> mHardwareEventDecoders = new SparseArray<>(1);
    private final DictionaryFacilitator mDictionaryFacilitator =
            DictionaryFacilitatorProvider.getDictionaryFacilitator(
                    false /* isNeededForSpellChecking */);
    public final InputLogic mInputLogic = new InputLogic(this /* LatinIME */,
            this /* SuggestionStripViewAccessor */, mDictionaryFacilitator);
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            loadKeyboard();
        }
    };
    private final SubtypeState mSubtypeState = new SubtypeState();
    // Object for reacting to adding/removing a dictionary pack.
    private final BroadcastReceiver mDictionaryPackInstallReceiver =
            new DictionaryPackInstallBroadcastReceiver(this);
    private final BroadcastReceiver mDictionaryDumpBroadcastReceiver =
            new DictionaryDumpBroadcastReceiver(this);
    private final boolean mIsHardwareAcceleratedDrawingEnabled;
    // receive ringer mode change.
    private final BroadcastReceiver mRingerModeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                AudioAndHapticFeedbackManager.getInstance().onRingerModeChanged();
            }
        }
    };
    private final int[] vowelIndexes = new int[MAX_VOWELS_SEQUENCE];
    public SharedPreferences mPrefs;
    // TODO: Move these {@link View}s to {@link KeyboardSwitcher}.
    public View mInputView;
    public SuggestionStripView mSuggestionStripView;
    public boolean mIsVietnameseSubType = false;
    public boolean isGifSupport = false;
    public boolean isToastText = false;
    boolean isDoubleSpace;
    private ViewChooseLanguage viewChooseLanguage;
    private SuggestedWords suggestionWord;
    private Handler handlerDelay = new Handler();
    private ViewTranslate viewTranslate;
    private InsetsUpdater mInsetsUpdater;
    private int heightKb = 0;
    private RichInputMethodManager mRichImm;
    private EmojiAltPhysicalKeyDetector mEmojiAltPhysicalKeyDetector;
    private StatsUtilsManager mStatsUtilsManager;
    private boolean isTelexVietnamese = true;
    private boolean isTelexVietnameseSimple = false;
    // Working variable for {@link #startShowingInputView()} and
    // {@link #onEvaluateInputViewShown()}.
    private boolean mIsExecutingStartShowingInputView;
    private boolean isSwitchSubtype = false;
    private AlertDialog mOptionsDialog;
    private GestureConsumer mGestureConsumer = GestureConsumer.NULL_GESTURE_CONSUMER;
    private CopyPasteSelectionView editSelectrionView;
    private ConstraintLayout layoutClipboard, layoutAllFont, ctlAddFont;
    private TextView txtAddFont, txtCancelFont;
    private ClipboardRepository clipboardRepository;
    private ImageView imgBackClipboard, imgBackAllFont;
    private RecyclerView rclClipboard, rclAllFont;
    private TextView txtNodataClipboard, txtTitleClipboard, txtTitleFont;
    private ProgressBar spinKitViewClipboard, spinKitViewFont;
    private ClipBoardAdapter clipBoardAdapter;
    private AllFontOnKeyboardAdapter allFontOnKeyboardAdapter;
    private ItemFont itemFontAdd;
    private ArrayList<ItemFont> itemFonts;
    private boolean showSettingInternal = false;
    private ConstraintLayout layoutSettingView;
    private AppCompatTextView tvSettingTheme, tvSettingFont, tvSettingVibrate, tvSettingSound,
            tvSettingLanguage, tvSettingCorrection, tvSettingSuggestion, tvSettingMore, tvResize;
    private boolean isClick = false;
    private int typeOldViewDisplay = Constant.KEYBOARD_VIEW_TYPE_KEY;
    private File file;
    private String url;
    public Handler handlerCommitGif = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (file == null || url == null) {
                return true;
            }
            Uri contentUri = FileProvider.getUriForFile(LatinIME.this, LatinIME.this.getPackageName(), file);
            Uri linkUri = Uri.parse(url);
            if (contentUri != null) {
                if (linkUri.isAbsolute()) {
                    commitGifImage(contentUri, url, linkUri);
                } else {
                    commitGifImage(contentUri, url, null);
                }
            }
            return true;
        }
    });
    private File cachePath;
    private WordComposer mWordComposer;
    private boolean isTypeInputPassword = false;
    private boolean mIsAutoCorrectionIndicatorOn;
    private long mLastKeyTime;
    private int mSpaceState;
    private int mLastWConverted = 0;
    private long timeAddDragView = 0;
    private int mLastSelectionStart = -1;
    private boolean isQwertyVietnamese = false;
    private boolean mIsKoreaSubType = false;
    private DragChangeSizeHeightKbView changeSizeHeightKbView;
    private int SIZE_30 = CommonUtil.dpToPx(App.getInstance(), 30);
    private boolean isChangeSize;
    private SettingsValues currentSettingsValues;
    private float scale = FloatingKb.SCALE_DEFAULT;

    public LatinIME() {
        super();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        mSettings = Settings.getInstance();
        mKeyboardSwitcher = KeyboardSwitcher.getInstance();
        mStatsUtilsManager = StatsUtilsManager.getInstance();
        mIsHardwareAcceleratedDrawingEnabled =
                InputMethodServiceCompatUtils.enableHardwareAcceleration(this);
        Log.i(TAG, "Hardware accelerated drawing: " + mIsHardwareAcceleratedDrawingEnabled);
    }

    // A helper method to split the code point and the key code. Ultimately, they should not be
    // squashed into the same variable, and this method should be removed.
    // public for testing, as we don't want to copy the same logic into test code
    @Nonnull
    public static Event createSoftwareKeypressEvent(final int keyCodeOrCodePoint, final int keyX,
                                                    final int keyY, final boolean isKeyRepeat) {
        final int keyCode;
        final int codePoint;
        if (keyCodeOrCodePoint <= 0) {
            keyCode = keyCodeOrCodePoint;
            codePoint = Event.NOT_A_CODE_POINT;
        } else {
            keyCode = Event.NOT_A_KEY_CODE;
            codePoint = keyCodeOrCodePoint;
        }
        return Event.createSoftwareKeypressEvent(codePoint, keyCode, keyX, keyY, isKeyRepeat);
    }

    private static void removeTrailingSpaceWhileInBatchEdit(final RichInputConnection ic) {
        if (ic == null) return;
        final CharSequence lastOne = ic.getTextBeforeCursor(1, 0);
        if (lastOne != null && lastOne.length() == 1
                && lastOne.charAt(0) == Keyboard.CODE_SPACE) {
            ic.deleteSurroundingText(1, 0);
        }
    }

    static private void sendUpDownEnterOrBackspace(final int code,
                                                   final RichInputConnection ic) {
        final long eventTime = SystemClock.uptimeMillis();
        ic.sendKeyEvent(new KeyEvent(eventTime, eventTime,
                KeyEvent.ACTION_DOWN, code, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
        ic.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime,
                KeyEvent.ACTION_UP, code, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
    }

    private static boolean canBeFollowedByPeriod(final int codePoint) {
        // TODO: Check again whether there really ain't a better way to check this.
        // TODO: This should probably be language-dependant...
        return Character.isLetterOrDigit(codePoint)
                || codePoint == Keyboard.CODE_SINGLE_QUOTE
                || codePoint == Keyboard.CODE_DOUBLE_QUOTE
                || codePoint == Keyboard.CODE_CLOSING_PARENTHESIS
                || codePoint == Keyboard.CODE_CLOSING_SQUARE_BRACKET
                || codePoint == Keyboard.CODE_CLOSING_CURLY_BRACKET
                || codePoint == Keyboard.CODE_CLOSING_ANGLE_BRACKET;
    }

    private static boolean isAlphabet(int code) {
        return Character.isLetter(code);
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
        Settings.init(this);
        DebugFlags.init(PreferenceManager.getDefaultSharedPreferences(this));
        RichInputMethodManager.init(this);
        mRichImm = RichInputMethodManager.getInstance();
        KeyboardSwitcher.init(this);
        AudioAndHapticFeedbackManager.init(this);
        AccessibilityUtils.init(this);
        mStatsUtilsManager.onCreate(this /* context */, mDictionaryFacilitator);
        super.onCreate();
        SIZE_30 = CommonUtil.dpToPx(App.getInstance(), 30);
        mHandler.onCreate();
        LocaleUtils.INSTANCE.applyLocale(this);
        // TODO: Resolve mutual dependencies of {@link #loadSettings()} and
        // {@link #resetDictionaryFacilitatorIfNecessary()}.
        loadSettings();
        resetDictionaryFacilitatorIfNecessary();

        // Register to receive ringer mode change.
        final IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(mRingerModeChangeReceiver, filter);

        // Register to receive installation and removal of a dictionary pack.
        final IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addDataScheme(SCHEME_PACKAGE);
        registerReceiver(mDictionaryPackInstallReceiver, packageFilter);

        final IntentFilter newDictFilter = new IntentFilter();
        newDictFilter.addAction(DictionaryPackConstants.NEW_DICTIONARY_INTENT_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mDictionaryPackInstallReceiver, newDictFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mDictionaryPackInstallReceiver, newDictFilter);

        }

        final IntentFilter dictDumpFilter = new IntentFilter();
        dictDumpFilter.addAction(DictionaryDumpBroadcastReceiver.DICTIONARY_DUMP_INTENT_ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mDictionaryDumpBroadcastReceiver, dictDumpFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mDictionaryDumpBroadcastReceiver, dictDumpFilter);
        }
        StatsUtils.onCreate(mSettings.getCurrent(), mRichImm);
        mWordComposer = mInputLogic.getmWordComposer();
    }

    // Has to be package-visible for unit tests
    @UsedForTesting
    void loadSettings() {
        final Locale locale = mRichImm.getCurrentSubtypeLocale();
        final EditorInfo editorInfo = getCurrentInputEditorInfo();
        final InputAttributes inputAttributes = new InputAttributes(
                editorInfo, isFullscreenMode(), getPackageName());
        mSettings.loadSettings(this, locale, inputAttributes);
        currentSettingsValues = mSettings.getCurrent();
        //final SettingsValues currentSettingsValues = mSettings.getCurrent();
        AudioAndHapticFeedbackManager.getInstance().onSettingsChanged(currentSettingsValues);
        // This method is called on startup and language switch, before the new layout has
        // been displayed. Opening dictionaries never affects responsivity as dictionaries are
        // asynchronously loaded.
        if (!mHandler.hasPendingReopenDictionaries()) {
            resetDictionaryFacilitator(locale);
        }
        refreshPersonalizationDictionarySession(currentSettingsValues);
        resetDictionaryFacilitatorIfNecessary();
        mStatsUtilsManager.onLoadSettings(this /* context */, currentSettingsValues);
    }

    private void refreshPersonalizationDictionarySession(
            final SettingsValues currentSettingsValues) {
        if (!currentSettingsValues.mUsePersonalizedDicts) {
            // Remove user history dictionaries.
            PersonalizationHelper.removeAllUserHistoryDictionaries(this);
            mDictionaryFacilitator.clearUserHistoryDictionary(this);
        }
    }

    // Note that this method is called from a non-UI thread.
    @Override
    public void onUpdateMainDictionaryAvailability(final boolean isMainDictionaryAvailable) {
        final MainKeyboardView mainKeyboardView = mKeyboardSwitcher.getMainKeyboardView();
        if (mainKeyboardView != null) {
            mainKeyboardView.setMainDictionaryAvailability(isMainDictionaryAvailable);
        }
        if (mHandler.hasPendingWaitForDictionaryLoad()) {
            mHandler.cancelWaitForDictionaryLoad();
            mHandler.postResumeSuggestions(false /* shouldDelay */);
        }
    }

    public void resetDictionaryFacilitatorIfNecessary() {
        final Locale subtypeSwitcherLocale = mRichImm.getCurrentSubtypeLocale();
        final Locale subtypeLocale;
        if (subtypeSwitcherLocale == null) {
            // This happens in very rare corner cases - for example, immediately after a switch
            // to LatinIME has been requested, about a frame later another switch happens. In this
            // case, we are about to go down but we still don't know it, however the system tells
            // us there is no current subtype.
            Log.e(TAG, "System is reporting no current subtype.");
            subtypeLocale = getResources().getConfiguration().locale;
        } else {
            subtypeLocale = subtypeSwitcherLocale;
        }
        Log.d("duongcv", "resetDictionaryFacilitatorIfNecessary: ");
        if (mDictionaryFacilitator.isForLocale(subtypeLocale)
                && mDictionaryFacilitator.isForAccount(mSettings.getCurrent().mAccount)) {
            return;
        }
        Log.d("duongcv", "resetDictionaryFacilitatorIfNecessary: " + subtypeLocale.getLanguage());
        resetDictionaryFacilitator(subtypeLocale);
    }

    public void resetDictionaryFacilitatorIfNecessary(Locale locale) {
        Log.d("duongcvv", "resetDictionaryFacilitatorIfNecessary: " + locale.getLanguage());
        mRichImm.setLocale(locale);
        resetDictionaryFacilitator(locale);
    }

    /**
     * Reset the facilitator by loading dictionaries for the given locale and
     * the current settings values.
     *
     * @param locale the locale
     */
    // TODO: make sure the current settings always have the right locales, and read from them.
    private void resetDictionaryFacilitator(final Locale locale) {
        final SettingsValues settingsValues = mSettings.getCurrent();
        mDictionaryFacilitator.resetDictionaries(this /* context */, locale,
                settingsValues.mUseContactsDict, settingsValues.mUsePersonalizedDicts,
                false /* forceReloadMainDictionary */,
                settingsValues.mAccount, "" /* dictNamePrefix */,
                this /* DictionaryInitializationListener */);
        if (settingsValues.mAutoCorrectionEnabledPerUserSettings) {
            mInputLogic.mSuggest.setAutoCorrectionThreshold(
                    settingsValues.mAutoCorrectionThreshold);
        }
        mInputLogic.mSuggest.setPlausibilityThreshold(settingsValues.mPlausibilityThreshold);
    }

    /**
     * Reset suggest by loading the main dictionary of the current locale.
     */
    /* package private */ void resetSuggestMainDict() {
        final SettingsValues settingsValues = mSettings.getCurrent();
        mDictionaryFacilitator.resetDictionaries(this /* context */,
                mDictionaryFacilitator.getLocale(), settingsValues.mUseContactsDict,
                settingsValues.mUsePersonalizedDicts,
                true /* forceReloadMainDictionary */,
                settingsValues.mAccount, "" /* dictNamePrefix */,
                this /* DictionaryInitializationListener */);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        mDictionaryFacilitator.closeDictionaries();
        mSettings.onDestroy();
        unregisterReceiver(mRingerModeChangeReceiver);
        unregisterReceiver(mDictionaryPackInstallReceiver);
        unregisterReceiver(mDictionaryDumpBroadcastReceiver);
        mStatsUtilsManager.onDestroy(this /* context */);
        super.onDestroy();
    }

    @UsedForTesting
    public void recycle() {
        unregisterReceiver(mDictionaryPackInstallReceiver);
        unregisterReceiver(mDictionaryDumpBroadcastReceiver);
        unregisterReceiver(mRingerModeChangeReceiver);
        mInputLogic.recycle();
    }

    private boolean isImeSuppressedByHardwareKeyboard() {
        final KeyboardSwitcher switcher = KeyboardSwitcher.getInstance();
        return !onEvaluateInputViewShown() && switcher.isImeSuppressedByHardwareKeyboard(
                mSettings.getCurrent(), switcher.getKeyboardSwitchState());
    }

    @Override
    public void onConfigureWindow(Window win, boolean isFullscreen, boolean isCandidatesOnly) {
        super.onConfigureWindow(win, isFullscreen, isCandidatesOnly);
    }

    @Override
    public void onConfigurationChanged(final Configuration conf) {
        SettingsValues settingsValues = mSettings.getCurrent();
        App.getInstance().isScreenLandscape = conf.orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (settingsValues.mDisplayOrientation != conf.orientation) {
            mHandler.startOrientationChanging();
            mInputLogic.onOrientationChange(mSettings.getCurrent());
        }
        if (settingsValues.mHasHardwareKeyboard != Settings.readHasHardwareKeyboard(conf)) {
            // If the state of having a hardware keyboard changed, then we want to reload the
            // settings to adjust for that.
            // TODO: we should probably do this unconditionally here, rather than only when we
            // have a change in hardware keyboard configuration.
            loadSettings();
            settingsValues = mSettings.getCurrent();
            if (isImeSuppressedByHardwareKeyboard()) {
                // We call cleanupInternalStateForFinishInput() because it's the right thing to do;
                // however, it seems at the moment the framework is passing us a seemingly valid
                // but actually non-functional InputConnection object. So if this bug ever gets
                // fixed we'll be able to remove the composition, but until it is this code is
                // actually not doing much.
                cleanupInternalStateForFinishInput();
            }
        }
        handlerDelay.removeCallbacks(runnable);
        handlerDelay.postDelayed(runnable, 1000);
        super.onConfigurationChanged(conf);
    }

    @Override
    public View onCreateInputView() {
        StatsUtils.onCreateInputView();
        return mKeyboardSwitcher.onCreateInputView(mIsHardwareAcceleratedDrawingEnabled);
    }

    @Override
    public void setInputView(final View view) {
        super.setInputView(view);
        mInputView = view;
        Timber.d("ducNQ : setInputView: ");
        //viewChooseLanguage
        mInsetsUpdater = ViewOutlineProviderCompatUtils.setInsetsOutlineProvider(view);

        editSelectrionView = view.findViewById(R.id.edit_selection_view);
        viewTranslate = mKeyboardSwitcher.getViewTranslate();
        viewChooseLanguage = mKeyboardSwitcher.getViewChooseLanguage();
        editSelectrionView.setRichInputConnection(this, mInputLogic.mConnection);
        initViewClipboard(view);
        initViewSetting(view);
        initViewAllFont(view);
        updateSoftInputWindowLayoutParameters();
        viewChooseLanguage.setListener(this);
        viewTranslate.setListenerTranslate(this);
        mSuggestionStripView = view.findViewById(R.id.suggestion_strip_view);
        mSuggestionStripView.setLatinIME(this);
        if (hasSuggestionStripView()) {
            mSuggestionStripView.setListener(this, view);
            ((InputView) mInputView).getMainKeyboardView().getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                if (((InputView) mInputView).getMainKeyboardView().getVisibility() == VISIBLE) {
                    if (mInputView != null && mInputView instanceof InputView) {
                        ((InputView) mInputView).showHideSettingView(false, true, true);
                    }
                    if (layoutClipboard != null && layoutClipboard.getVisibility() == VISIBLE) {
                        showClipboard(GONE, VISIBLE);
                    }
//                    if (editSelectrionView != null && editSelectrionView.getVisibility() == VISIBLE) {
//                        showSettingInternal = false;
//                        showMenuHeader();
//                        Timber.e("hachung editSelectrionView GONE:");
//                        editSelectrionView.setVisibility(GONE);
//                    }
                }
            });
        }
    }

    public void delayToastText() {
        isToastText = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isToastText = false;
            }
        }, 2000);
    }

    private void delayClick() {
        isClick = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isClick = false;
            }
        }, 500);
    }

    public void hideViewChangeSize() {
        //  if(layoutSettingView!=null&&tvResize!=null){


        //  layoutSettingView.findViewById(R.id.view_resize).setVisibility(GONE);
        //    tvResize.setVisibility(GONE);
        // }
    }

    private void initViewSetting(View view) {
        layoutSettingView = view.findViewById(R.id.layout_setting_theme);
        tvSettingTheme = layoutSettingView.findViewById(R.id.tv_setting_theme);
        tvSettingFont = layoutSettingView.findViewById(R.id.tv_settting_font);
        tvSettingVibrate = layoutSettingView.findViewById(R.id.tv_setting_vibrate);
        tvSettingSound = layoutSettingView.findViewById(R.id.tv_setting_sound);
        tvSettingLanguage = layoutSettingView.findViewById(R.id.tv_setting_language);
        tvSettingCorrection = layoutSettingView.findViewById(R.id.tv_setting_auto_correct);
        tvSettingSuggestion = layoutSettingView.findViewById(R.id.tv_setting_suggestion);
        tvSettingMore = layoutSettingView.findViewById(R.id.tv_setting_more);
        tvResize = layoutSettingView.findViewById(R.id.tv_resize);
        changeImageSetting(tvSettingTheme, R.drawable.ic_setting_theme, R.string.text_tab_themes);
        changeImageSetting(tvSettingFont, R.drawable.ic_setting_font, R.string.fonts);
        changeImageSetting(tvSettingLanguage, R.drawable.ic_setting_language, R.string.language);
        changeImageSetting(tvSettingMore, R.drawable.ic_setting_more, R.string.more);
        updateViewVibrate(mPrefs.getBoolean(Settings.PREF_VIBRATE_ON, true));
        updateViewSound(mPrefs.getBoolean(Settings.PREF_SOUND_ON, false));
        updateViewAutoCorrection(mPrefs.getBoolean(Settings.PREF_AUTO_CORRECTION, false));
        updateViewSuggestion(mPrefs.getBoolean(com.android.inputmethod.latin.settings.Settings.PREF_SHOW_SUGGESTIONS, false));
        ((RealtimeBlurViewKB) layoutSettingView.findViewById(R.id.view_setting_theme)).setDecorView(view.findViewById(R.id.imgBackgroundKeyboard), true);
        ((RealtimeBlurViewKB) layoutSettingView.findViewById(R.id.view_setting_font)).setDecorView(view.findViewById(R.id.imgBackgroundKeyboard), true);
        ((RealtimeBlurViewKB) layoutSettingView.findViewById(R.id.view_setting_vibrate)).setDecorView(view.findViewById(R.id.imgBackgroundKeyboard), true);
        ((RealtimeBlurViewKB) layoutSettingView.findViewById(R.id.view_setting_sound)).setDecorView(view.findViewById(R.id.imgBackgroundKeyboard), true);
        ((RealtimeBlurViewKB) layoutSettingView.findViewById(R.id.view_setting_language)).setDecorView(view.findViewById(R.id.imgBackgroundKeyboard), true);
        ((RealtimeBlurViewKB) layoutSettingView.findViewById(R.id.view_setting_auto_correction)).setDecorView(view.findViewById(R.id.imgBackgroundKeyboard), true);
        ((RealtimeBlurViewKB) layoutSettingView.findViewById(R.id.view_setting_suggestion)).setDecorView(view.findViewById(R.id.imgBackgroundKeyboard), true);
        ((RealtimeBlurViewKB) layoutSettingView.findViewById(R.id.view_setting_more)).setDecorView(view.findViewById(R.id.imgBackgroundKeyboard), true);
        ((RealtimeBlurViewKB) layoutSettingView.findViewById(R.id.view_resize)).setDecorView(view.findViewById(R.id.imgBackgroundKeyboard), true);

        layoutSettingView.findViewById(R.id.view_resize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocaleUtils.INSTANCE.applyLocale(LatinIME.this);
                reloadKeyBoard();
                showDragChangeSizeKb(true);
            }
        });
        layoutSettingView.findViewById(R.id.view_setting_font).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllFont();
            }
        });
        layoutSettingView.findViewById(R.id.view_setting_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isClick) {
                    delayClick();
                    openSetting(Constant.KEY_SCREEN_THEME, false);
                }
            }
        });
        layoutSettingView.findViewById(R.id.view_setting_vibrate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocaleUtils.INSTANCE.applyLocale(LatinIME.this);
                boolean isVibrate = mPrefs.getBoolean(com.android.inputmethod.latin.settings.Settings.PREF_VIBRATE_ON, true);
                mPrefs.edit().putBoolean(com.android.inputmethod.latin.settings.Settings.PREF_VIBRATE_ON, !isVibrate).apply();
                updateViewVibrate(!isVibrate);
                AudioAndHapticFeedbackManager.getInstance().onSettingsChanged(Settings.getInstance().getCurrent());
            }
        });
        layoutSettingView.findViewById(R.id.view_setting_sound).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocaleUtils.INSTANCE.applyLocale(LatinIME.this);
                boolean isSound = mPrefs.getBoolean(com.android.inputmethod.latin.settings.Settings.PREF_SOUND_ON, false);
                mPrefs.edit().putBoolean(com.android.inputmethod.latin.settings.Settings.PREF_SOUND_ON, !isSound).apply();
                AudioAndHapticFeedbackManager.getInstance().onSettingsChanged(Settings.getInstance().getCurrent());
                AudioAndHapticFeedbackManager.getInstance().loadSound();
                updateViewSound(!isSound);
            }
        });
        layoutSettingView.findViewById(R.id.view_setting_language).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isClick) {
                    delayClick();
                    openSetting(Constant.KEY_SCREEN_LANGUAGE, false);
                }
            }
        });
        layoutSettingView.findViewById(R.id.view_setting_auto_correction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocaleUtils.INSTANCE.applyLocale(LatinIME.this);
                boolean isAutoCorrect = mPrefs.getBoolean(com.android.inputmethod.latin.settings.Settings.PREF_AUTO_CORRECTION, false);
                mPrefs.edit().putBoolean(com.android.inputmethod.latin.settings.Settings.PREF_AUTO_CORRECTION, !isAutoCorrect).apply();
                updateViewAutoCorrection(!isAutoCorrect);
            }
        });
        layoutSettingView.findViewById(R.id.view_setting_suggestion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocaleUtils.INSTANCE.applyLocale(LatinIME.this);
                boolean isSuggestion = mPrefs.getBoolean(com.android.inputmethod.latin.settings.Settings.PREF_SHOW_SUGGESTIONS, false);
                //  MySharePreferences.putBoolean("hello",isSuggestion,App.getInstance().getBaseContext());

                mPrefs.edit().putBoolean(com.android.inputmethod.latin.settings.Settings.PREF_SHOW_SUGGESTIONS, !isSuggestion).apply();
                updateViewSuggestion(!isSuggestion);
            }
        });
        layoutSettingView.findViewById(R.id.view_setting_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isClick) {
                    delayClick();
                    openSetting(Constant.KEY_SCREEN_MORE, false);
                    EventBus.getDefault().post(new MessageEvent(90));
                }
            }
        });

    }

    public void initColor() {
        int color = App.getInstance().colorIconNew;
        tvSettingTheme.setTextColor(color);
        tvSettingTheme.getCompoundDrawables()[1].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        tvSettingFont.setTextColor(color);
        tvSettingFont.getCompoundDrawables()[1].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        tvSettingVibrate.setTextColor(color);
        tvSettingVibrate.getCompoundDrawables()[1].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        tvSettingSound.setTextColor(color);
        tvSettingSound.getCompoundDrawables()[1].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        tvSettingLanguage.setTextColor(color);
        tvSettingLanguage.getCompoundDrawables()[1].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        tvSettingCorrection.setTextColor(color);
        tvSettingCorrection.getCompoundDrawables()[1].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        tvSettingSuggestion.setTextColor(color);
        tvSettingSuggestion.getCompoundDrawables()[1].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        tvSettingMore.setTextColor(color);
        tvResize.setTextColor(color);
        tvSettingMore.getCompoundDrawables()[1].setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    public void openSetting(int keyOpenScreen, boolean isAddFont) {
        Intent intent = new Intent(App.getInstance(), SplashActivity.class);

        if (App.isActivityVisible()) {//|| isAddFont
            intent = new Intent(App.getInstance(), MainActivity.class);
        }
        intent.putExtra(Constant.KEY_OPEN_SCREEN, keyOpenScreen);
        if (keyOpenScreen == Constant.KEY_SCREEN_FONT && itemFontAdd != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constant.DATA_FONT_ADD, itemFontAdd);
            intent.putExtra(Constant.DATA_BUNDLE, bundle);
        }
        if (keyOpenScreen == Constant.KEY_SCREEN_MORE) {
            App.getInstance().idScreen = 3;
            EventBus.getDefault().postSticky(new MessageEvent(com.tapbi.spark.yokey.common.Constant.SCREEN_MORE));
        } else if (keyOpenScreen == Constant.KEY_SCREEN_STICKER) {
            App.getInstance().idScreen = 2;
            EventBus.getDefault().postSticky(new MessageEvent(SCREEN_STICKER));
        } else if (keyOpenScreen == Constant.KEY_SCREEN_FONT) {
            App.getInstance().idScreen = 1;
            EventBus.getDefault().postSticky(new MessageEvent(com.tapbi.spark.yokey.common.Constant.SCREEN_FONT));
        } else if (keyOpenScreen == Constant.KEY_SCREEN_LANGUAGE) {
            App.getInstance().idScreen = 3;
            EventBus.getDefault().postSticky(new MessageEvent(com.tapbi.spark.yokey.common.Constant.SCREEN_LANGUAGE));
        } else if (keyOpenScreen == Constant.KEY_SCREEN_EMOJIS) {
            App.getInstance().idScreen = 2;
            EventBus.getDefault().postSticky(new MessageEvent(com.tapbi.spark.yokey.common.Constant.SCREEN_EMOJIS));
        } else if (keyOpenScreen == Constant.KEY_SCREEN_THEME) {
            App.getInstance().idScreen = 0;
            EventBus.getDefault().postSticky(new MessageEvent(com.tapbi.spark.yokey.common.Constant.SCREEN_THEME));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateViewAutoCorrection(boolean isCheck) {
        if (tvSettingCorrection != null)
            changeStatusSetting(tvSettingCorrection, R.drawable.ic_setting_auto_correction, R.drawable.ic_off_correction, isCheck, getString(R.string.automatic_ncorrection), getString(R.string.correction_off));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateViewSuggestion(boolean isCheck) {
        if (tvSettingSuggestion != null)
            changeStatusSetting(tvSettingSuggestion, R.drawable.ic_setting_suggestion, R.drawable.ic_off_suggestion, isCheck, getString(R.string.show_n_suggestions), getString(R.string.suggestion_off));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateViewVibrate(boolean isCheck) {
        if (tvSettingVibrate != null)
            changeStatusSetting(tvSettingVibrate, R.drawable.ic_setting_vibrate, R.drawable.ic_setting_silent, isCheck, getString(R.string.vibrate), getString(R.string.silent));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateViewSound(boolean isCheck) {
        if (tvSettingSound != null)
            changeStatusSetting(tvSettingSound, R.drawable.ic_setting_sound, R.drawable.ic_setting_mute, isCheck, getString(R.string.sound), getString(R.string.mute));
    }

    public void startTranslate() {
        if (viewChooseLanguage != null) {
            viewChooseLanguage.getListInput();
            viewChooseLanguage.getListOutput();
        }
        mInputLogic.mConnection.finishComposingText();
        viewTranslate.startShowTranslate();

    }

    public boolean isViewTranslateShow() {
        try {
            return viewTranslate.getVisibility() == View.VISIBLE;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isTranslationFunctionEnabled() {
        try {
            return isViewTranslateShow() && viewTranslate.isTranslatable();
        } catch (NullPointerException e) {
            return false;
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void changeStatusSetting(AppCompatTextView textView, int drawable1, int drawable2, boolean isCheck, String text1, String text2) {
        Drawable img;
        if (isCheck) {
            img = getDrawable(drawable1);
            textView.setText(text1);
        } else {
            img = getDrawable(drawable2);
            textView.setText(text2);
        }
        img.setBounds(0, 0, SIZE_30, SIZE_30);
        int color = App.getInstance().colorIconDefault;
        img.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        textView.setCompoundDrawables(null, img, null, null);
    }

    private void changeImageSetting(AppCompatTextView textView, int drawable1, int text1) {
        Drawable img = AppCompatResources.getDrawable(this, drawable1);//getDrawable(drawable1);
        if (img == null) return;
        textView.setText(text1);
        img.setBounds(0, 0, SIZE_30, SIZE_30);
        int color = App.getInstance().colorIconDefault;
        img.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        textView.setCompoundDrawables(null, img, null, null);
    }

    public void showMenuHeader() {
//        if (mSuggestionStripView != null) mSuggestionStripView.showUIMenu();
        if (mInputView != null)
            ((InputView) mInputView).showHeaderKeyboard(Constant.HEADER_KEYBOARD_TYPE_MENU);
        typeOldViewDisplay = Constant.KEYBOARD_VIEW_TYPE_KEY;
    }

    public void showSelectionView() {
        editSelectrionView.showEditSelection();
        Timber.e("hachung editSelectrionView VISIBLE:");
        editSelectrionView.setVisibility(VISIBLE);
        showInputView(false);
        if (layoutSettingView != null) layoutSettingView.setVisibility(GONE);
    }

    public void showSettingView() {
        if (layoutSettingView != null) {
            if (layoutSettingView.getVisibility() == VISIBLE) {
                Timber.d("ducNQ : showSettingView: 1");
                showInputView(true);
                layoutSettingView.setVisibility(GONE);
            } else {
                showInputView(false);
                Timber.e("hachung editSelectrionView GONE:");
                if (editSelectrionView != null) editSelectrionView.setVisibility(GONE);
                layoutSettingView.setVisibility(VISIBLE);
                if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_NONE) {
                    layoutSettingView.findViewById(R.id.view_resize).setVisibility(VISIBLE);
                    tvResize.setVisibility(VISIBLE);
                } else {
                    layoutSettingView.findViewById(R.id.view_resize).setVisibility(INVISIBLE);
                    tvResize.setVisibility(INVISIBLE);
                }
                initColor();
            }
        }
    }

    private void showUIMenu() {
        //    layoutSuggest.setVisibility(GONE);
//           layoutMenu.setVisibility(VISIBLE);
        //   layoutSearch.setVisibility(GONE);
    }

    @Override
    public void setCandidatesView(final View view) {
        // To ensure that CandidatesView will never be set.
    }

    @Override
    public void onStartInput(final EditorInfo editorInfo, final boolean restarting) {
        mHandler.onStartInput(editorInfo, restarting);
        if (editSelectrionView != null) editSelectrionView.hideEditSelection();
        if (layoutClipboard != null) showClipboard(GONE, VISIBLE);
        if (layoutSettingView != null) layoutSettingView.setVisibility(GONE);
        if (layoutAllFont != null) layoutAllFont.setVisibility(GONE);
        if (mSuggestionStripView != null) mSuggestionStripView.showUIMenu();
        if (mInputView != null)
            ((InputView) mInputView).showHeaderKeyboard(Constant.HEADER_KEYBOARD_TYPE_MENU);
        showInputView(true);
        Timber.d("ducNQ : showSettingView: 2");
        updateViewVibrate(mPrefs.getBoolean(Settings.PREF_VIBRATE_ON, true));
        updateViewSound(mPrefs.getBoolean(Settings.PREF_SOUND_ON, false));
        updateViewAutoCorrection(mPrefs.getBoolean(Settings.PREF_AUTO_CORRECTION, false));
        updateViewSuggestion(mPrefs.getBoolean(Settings.PREF_SHOW_SUGGESTIONS, false));
        if (tvSettingTheme != null) {
            changeImageSetting(tvSettingTheme, R.drawable.ic_setting_theme, R.string.text_tab_themes);
        }
        if (tvSettingFont != null) {
            changeImageSetting(tvSettingFont, R.drawable.ic_setting_font, R.string.fonts);
        }
        if (tvSettingLanguage != null) {
            changeImageSetting(tvSettingLanguage, R.drawable.ic_setting_language, R.string.language);
        }
        if (tvSettingMore != null) {
            changeImageSetting(tvSettingMore, R.drawable.ic_setting_more, R.string.more);
        }
        if (tvResize != null) {
            tvResize.setText(R.string.change_size);
        }
        if (txtTitleFont != null) {
            txtTitleFont.setText(R.string.fonts);
        }
        if (txtAddFont != null) {
            txtAddFont.setText(R.string.add_font);
        }
        if (txtCancelFont != null) {
            txtCancelFont.setText(R.string.cancel);
        }
    }

    @Override
    public void onStartInputView(final EditorInfo editorInfo, final boolean restarting) {
        mHandler.onStartInputView(editorInfo, restarting);
        mStatsUtilsManager.onStartInputView();
        isTypeInputPassword = mKeyboardSwitcher != null && mKeyboardSwitcher.getKeyboard() != null && mKeyboardSwitcher.getKeyboard().mId.passwordInput();
    }

    public void refeshView() {
        if (layoutSettingView != null) layoutSettingView.setVisibility(GONE);
    }

    @Override
    public void onFinishInputView(final boolean finishingInput) {
        StatsUtils.onFinishInputView();
        mHandler.onFinishInputView(finishingInput);
        mStatsUtilsManager.onFinishInputView();
        mGestureConsumer = GestureConsumer.NULL_GESTURE_CONSUMER;
        showDragChangeSizeKb(false);
    }

    @Override
    public void onFinishInput() {
        mHandler.onFinishInput();
    }

    @Override
    public void onCurrentInputMethodSubtypeChanged(final InputMethodSubtype subtype) {
        // Note that the calling sequence of onCreate() and onCurrentInputMethodSubtypeChanged()
        // is not guaranteed. It may even be called at the same time on a different thread.
        InputMethodSubtype oldSubtype = mRichImm.getCurrentSubtype().getRawSubtype();
        StatsUtils.onSubtypeChanged(oldSubtype, subtype);
        mRichImm.onSubtypeChanged(subtype);
        mInputLogic.onSubtypeChanged(SubtypeLocaleUtils.getCombiningRulesExtraValue(subtype),
                mSettings.getCurrent());
        Timber.d("ducNQ : onCurrentInputMethodSubtypeChanged: ");
        if (App.getInstance().typeEditing != Constant.TYPE_EDIT_CUSTOMIZE) {
            loadKeyboard();
        }
    }



    public CopyPasteSelectionView getEditSelectrionView() {
        return editSelectrionView;
    }

    void onStartInputInternal(final EditorInfo editorInfo, final boolean restarting) {
        super.onStartInput(editorInfo, restarting);
        EventBus.getDefault().post(new MessageEvent(Constant.ACTION_CHANGE_STATE_ACTIVATE_KEYBOARD));
        // If the primary hint language does not match the current subtype language, then try
        // to switch to the primary hint language.
        // TODO: Support all the locales in EditorInfo#hintLocales.
        final Locale primaryHintLocale = EditorInfoCompatUtils.getPrimaryHintLocale(editorInfo);
        if (primaryHintLocale == null) {
            return;
        }
        final InputMethodSubtype newSubtype = mRichImm.findSubtypeByLocale(primaryHintLocale);
        if (newSubtype == null || newSubtype.equals(mRichImm.getCurrentSubtype().getRawSubtype())) {
            return;
        }
        mHandler.postSwitchLanguage(newSubtype);
    }

    @SuppressWarnings("deprecation")
    void onStartInputViewInternal(final EditorInfo editorInfo, final boolean restarting) {
        super.onStartInputView(editorInfo, restarting);
        Timber.d("onGifClick compareMimeTypes " + isGifSupport);
        mDictionaryFacilitator.onStartInput();
        // Switch to the null consumer to handle cases leading to early exit below, for which we
        // also wouldn't be consuming gesture data.
        mGestureConsumer = GestureConsumer.NULL_GESTURE_CONSUMER;
        mRichImm.refreshSubtypeCaches();
        final KeyboardSwitcher switcher = mKeyboardSwitcher;
        switcher.updateKeyboardTheme("null", "null");
        Timber.d("ducNQ : onStartInputViewInternal: ");
        final MainKeyboardView mainKeyboardView = switcher.getMainKeyboardView();
        if (mainKeyboardView != null) mainKeyboardView.setThemeForKeyboard();
        // If we are starting input in a different text field from before, we'll have to reload
        // settings, so currentSettingsValues can't be final.
        SettingsValues currentSettingsValues = mSettings.getCurrent();

        try {
            String[] mimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo);
            for (String mimeType : mimeTypes) {
                if (ClipDescription.compareMimeTypes(mimeType, "image/png")) {
                    isGifSupport = validatePackageName(editorInfo);
                    Timber.d("onGifClick compareMimeTypes " + isGifSupport);
                    break;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return;
        }

        if (editorInfo == null) {
            Log.e(TAG, "Null EditorInfo in onStartInputView()");
            if (DebugFlags.DEBUG_ENABLED) {
                throw new NullPointerException("Null EditorInfo in onStartInputView()");
            }
            return;
        }
        if (DebugFlags.DEBUG_ENABLED) {
            Log.d(TAG, "onStartInputView: editorInfo:"
                    + String.format("inputType=0x%08x imeOptions=0x%08x",
                    editorInfo.inputType, editorInfo.imeOptions));
            Log.d(TAG, "All caps = "
                    + ((editorInfo.inputType & InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) != 0)
                    + ", sentence caps = "
                    + ((editorInfo.inputType & InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) != 0)
                    + ", word caps = "
                    + ((editorInfo.inputType & InputType.TYPE_TEXT_FLAG_CAP_WORDS) != 0));
        }
        Log.i(TAG, "Starting input. Cursor position = "
                + editorInfo.initialSelStart + "," + editorInfo.initialSelEnd);
        // TODO: Consolidate these checks with {@link InputAttributes}.
        if (InputAttributes.inPrivateImeOptions(null, NO_MICROPHONE_COMPAT, editorInfo)) {
            Log.w(TAG, "Deprecated private IME option specified: " + editorInfo.privateImeOptions);
            Log.w(TAG, "Use " + getPackageName() + "." + NO_MICROPHONE + " instead");
        }
        if (InputAttributes.inPrivateImeOptions(getPackageName(), FORCE_ASCII, editorInfo)) {
            Log.w(TAG, "Deprecated private IME option specified: " + editorInfo.privateImeOptions);
            Log.w(TAG, "Use EditorInfo.IME_FLAG_FORCE_ASCII flag instead");
        }

        // In landscape mode, this method gets called without the input view being created.
        if (mainKeyboardView == null) {
            return;
        }

        // Update to a gesture consumer with the current editor and IME state.
        mGestureConsumer = GestureConsumer.newInstance(editorInfo,
                mInputLogic.getPrivateCommandPerformer(),
                mRichImm.getCurrentSubtypeLocale(),
                switcher.getKeyboard());

        // Forward this event to the accessibility utilities, if enabled.
        final AccessibilityUtils accessUtils = AccessibilityUtils.getInstance();
        if (accessUtils.isTouchExplorationEnabled()) {
            accessUtils.onStartInputViewInternal(mainKeyboardView, editorInfo, restarting);
        }

        final boolean inputTypeChanged = !currentSettingsValues.isSameInputType(editorInfo);
        final boolean isDifferentTextField = !restarting || inputTypeChanged;

        StatsUtils.onStartInputView(editorInfo.inputType,
                Settings.getInstance().getCurrent().mDisplayOrientation,
                !isDifferentTextField);

        // The EditorInfo might have a flag that affects fullscreen mode.
        // Note: This call should be done by InputMethodService?
        updateFullscreenMode();

        // ALERT: settings have not been reloaded and there is a chance they may be stale.
        // In the practice, if it is, we should have gotten onConfigurationChanged so it should
        // be fine, but this is horribly confusing and must be fixed AS SOON AS POSSIBLE.

        // In some cases the input connection has not been reset yet and we can't access it. In
        // this case we will need to call loadKeyboard() later, when it's accessible, so that we
        // can go into the correct mode, so we need to do some housekeeping here.
        final boolean needToCallLoadKeyboardLater;
        final Suggest suggest = mInputLogic.mSuggest;
        if (!isImeSuppressedByHardwareKeyboard()) {
            // The app calling setText() has the effect of clearing the composing
            // span, so we should reset our state unconditionally, even if restarting is true.
            // We also tell the input logic about the combining rules for the current subtype, so
            // it can adjust its combiners if needed.
            mInputLogic.startInput(mRichImm.getCombiningRulesExtraValueOfCurrentSubtype(),
                    currentSettingsValues);

            resetDictionaryFacilitatorIfNecessary();

            // TODO[IL]: Can the following be moved to InputLogic#startInput?
            if (!mInputLogic.mConnection.resetCachesUponCursorMoveAndReturnSuccess(
                    editorInfo.initialSelStart, editorInfo.initialSelEnd,
                    false /* shouldFinishComposition */)) {
                // Sometimes, while rotating, for some reason the framework tells the app we are not
                // connected to it and that means we can't refresh the cache. In this case, schedule
                // a refresh later.
                // We try resetting the caches up to 5 times before giving up.
                mHandler.postResetCaches(isDifferentTextField, 5 /* remainingTries */);
                // mLastSelection{Start,End} are reset later in this method, no need to do it here
                needToCallLoadKeyboardLater = true;
            } else {
                // When rotating, and when input is starting again in a field from where the focus
                // didn't move (the keyboard having been closed with the back key),
                // initialSelStart and initialSelEnd sometimes are lying. Make a best effort to
                // work around this bug.
                mInputLogic.mConnection.tryFixLyingCursorPosition();
                mHandler.postResumeSuggestionsForStartInput(true /* shouldDelay */);
                needToCallLoadKeyboardLater = false;
            }
        } else {
            // If we have a hardware keyboard we don't need to call loadKeyboard later anyway.
            needToCallLoadKeyboardLater = false;
        }

        if (isDifferentTextField ||
                !currentSettingsValues.hasSameOrientation(getResources().getConfiguration())) {
            loadSettings();
        }
        if (isDifferentTextField) {
            mainKeyboardView.closing();
            currentSettingsValues = mSettings.getCurrent();

            if (currentSettingsValues.mAutoCorrectionEnabledPerUserSettings) {
                suggest.setAutoCorrectionThreshold(
                        currentSettingsValues.mAutoCorrectionThreshold);
            }
            suggest.setPlausibilityThreshold(currentSettingsValues.mPlausibilityThreshold);
            if (mainKeyboardView.getVisibility() == VISIBLE) {
                switcher.loadKeyboard(editorInfo, currentSettingsValues, getCurrentAutoCapsState(),
                        getCurrentRecapitalizeState(), false);
            }
            if (switcher.mKeyboardLayoutSet != null && switcher.mKeyboardLayoutSet.isTypeNumberKeyboard()) {// fix bug 599
                if (mSuggestionStripView != null) {
                    mSuggestionStripView.setVisibility(GONE);
                }
            } else {
                if (mSuggestionStripView != null) {
                    mSuggestionStripView.setVisibility(VISIBLE);
                }
            }
            if (needToCallLoadKeyboardLater) {
                // If we need to call loadKeyboard again later, we need to save its state now. The
                // later call will be done in #retryResetCaches.
                switcher.saveKeyboardState();
            }
        } else if (restarting) {
            // TODO: Come up with a more comprehensive way to reset the keyboard layout when
            // a keyboard layout set doesn't get reloaded in this method.
            switcher.resetKeyboardStateToAlphabet(getCurrentAutoCapsState(),
                    getCurrentRecapitalizeState());
            // In apps like Talk, we come here when the text is sent and the field gets emptied and
            // we need to re-evaluate the shift state, but not the whole layout which would be
            // disruptive.
            // Space state must be updated before calling updateShiftState
            switcher.requestUpdatingShiftState(getCurrentAutoCapsState(),
                    getCurrentRecapitalizeState());
        }
        // This will set the punctuation suggestions if next word suggestion is off;
        // otherwise it will clear the suggestion strip.
        setNeutralSuggestionStrip();

        mHandler.cancelUpdateSuggestionStrip();

        mainKeyboardView.setMainDictionaryAvailability(
                mDictionaryFacilitator.hasAtLeastOneInitializedMainDictionary());
        mainKeyboardView.setKeyPreviewPopupEnabled(currentSettingsValues.mKeyPreviewPopupOn,
                currentSettingsValues.mKeyPreviewPopupDismissDelay);
        mainKeyboardView.setSlidingKeyInputPreviewEnabled(
                currentSettingsValues.mSlidingKeyInputPreviewEnabled);
        mainKeyboardView.setGestureHandlingEnabledByUser(
                currentSettingsValues.mGestureInputEnabled,
                currentSettingsValues.mGestureTrailEnabled,
                currentSettingsValues.mGestureFloatingPreviewTextEnabled);

        if (TRACE) Debug.startMethodTracing("/data/trace/latinime");

    }

    @Override
    public void onWindowShown() {
        super.onWindowShown();
        setNavigationBarVisibility(isInputViewShown());
    }

    public void changeColorNavigationBar() {
        setNavigationBarVisibility(isInputViewShown());
    }

    // TODO: Revise the language switch key behavior to make it much smarter and more reasonable.

    @Override
    public void onWindowHidden() {
        super.onWindowHidden();
        final MainKeyboardView mainKeyboardView = mKeyboardSwitcher.getMainKeyboardView();
        if (mainKeyboardView != null) {
            mainKeyboardView.closing();
        }
        setNavigationBarVisibility(false);
    }

    void onFinishInputInternal() {
        super.onFinishInput();

        mDictionaryFacilitator.onFinishInput(this);
        final MainKeyboardView mainKeyboardView = mKeyboardSwitcher.getMainKeyboardView();
        if (mainKeyboardView != null) {
            mainKeyboardView.closing();
        }
    }

    void onFinishInputViewInternal(final boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        cleanupInternalStateForFinishInput();
        if (editSelectrionView != null) editSelectrionView.hideEditSelection();
        if (layoutClipboard != null) showClipboard(GONE, VISIBLE);
        if (layoutSettingView != null) layoutSettingView.setVisibility(GONE);
        if (layoutAllFont != null) layoutAllFont.setVisibility(GONE);
//        if (mSuggestionStripView != null) mSuggestionStripView.showUIMenu();
        if (mInputView != null)
            ((InputView) mInputView).showHeaderKeyboard(Constant.HEADER_KEYBOARD_TYPE_MENU);
    }

    private void cleanupInternalStateForFinishInput() {
        // Remove pending messages related to update suggestions
        mHandler.cancelUpdateSuggestionStrip();
        // Should do the following in onFinishInputInternal but until JB MR2 it's not called :(
        mInputLogic.finishInput();
    }

    protected void deallocateMemory() {
        mKeyboardSwitcher.deallocateMemory();
    }

    @Override
    public void onUpdateSelection(final int oldSelStart, final int oldSelEnd,
                                  final int newSelStart, final int newSelEnd,
                                  final int composingSpanStart, final int composingSpanEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                composingSpanStart, composingSpanEnd);
        if (DebugFlags.DEBUG_ENABLED) {
            Log.i(TAG, "onUpdateSelection: oss=" + oldSelStart + ", ose=" + oldSelEnd
                    + ", nss=" + newSelStart + ", nse=" + newSelEnd
                    + ", cs=" + composingSpanStart + ", ce=" + composingSpanEnd);
        }

        // This call happens whether our view is displayed or not, but if it's not then we should
        // not attempt recorrection. This is true even with a hardware keyboard connected: if the
        // view is not displayed we have no means of showing suggestions anyway, and if it is then
        // we want to show suggestions anyway.
        final SettingsValues settingsValues = mSettings.getCurrent();
        if (isInputViewShown()
                && mInputLogic.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                settingsValues)) {
            mKeyboardSwitcher.requestUpdatingShiftState(getCurrentAutoCapsState(),
                    getCurrentRecapitalizeState());
        }

        if (mIsVietnameseSubType) {

            final boolean selectionChanged = (newSelStart != composingSpanStart
                    || newSelEnd != composingSpanEnd) && mLastSelectionStart != newSelStart;
            final boolean noComposingSpan = composingSpanStart == -1 && composingSpanEnd == -1;
            if ((!mWordComposer.isComposingWord()) || selectionChanged || noComposingSpan) {
                // mInputLogic.resetEntireInputState(newSelStart, newSelEnd, false);
                resetEntireInputState();
                mHandler.postUpdateShiftState();
            }
            mLastSelectionStart = newSelStart;
        }
    }

    private void resetEntireInputState() {
        resetComposingState(true /* alsoResetLastComposedWord */);
        // updateSuggestions();
        final RichInputConnection ic = mInputLogic.mConnection;
        if (ic != null) {
            ic.finishComposingText();
        }
    }

    /**
     * This is called when the user has clicked on the extracted text view,
     * when running in fullscreen mode.  The default implementation hides
     * the suggestions view when this happens, but only if the extracted text
     * editor has a vertical scroll bar because its text doesn't fit.
     * Here we override the behavior due to the possibility that a re-correction could
     * cause the suggestions strip to disappear and re-appear.
     */
    @Override
    public void onExtractedTextClicked() {
        if (mSettings.getCurrent().needsToLookupSuggestions()) {
            return;
        }

        super.onExtractedTextClicked();
    }

    /**
     * This is called when the user has performed a cursor movement in the
     * extracted text view, when it is running in fullscreen mode.  The default
     * implementation hides the suggestions view when a vertical movement
     * happens, but only if the extracted text editor has a vertical scroll bar
     * because its text doesn't fit.
     * Here we override the behavior due to the possibility that a re-correction could
     * cause the suggestions strip to disappear and re-appear.
     */
    @Override
    public void onExtractedCursorMovement(final int dx, final int dy) {
        if (mSettings.getCurrent().needsToLookupSuggestions()) {
            return;
        }

        super.onExtractedCursorMovement(dx, dy);
    }

    @Override
    public void hideWindow() {
        mKeyboardSwitcher.onHideWindow();

        if (TRACE) Debug.stopMethodTracing();
        if (isShowingOptionDialog()) {
            mOptionsDialog.dismiss();
            mOptionsDialog = null;
        }
        isGifSupport = false;
        super.hideWindow();
    }

    @Override
    public void onDisplayCompletions(final CompletionInfo[] applicationSpecifiedCompletions) {
        if (DebugFlags.DEBUG_ENABLED) {
            Log.i(TAG, "Received completions:");
            if (applicationSpecifiedCompletions != null) {
                for (int i = 0; i < applicationSpecifiedCompletions.length; i++) {
                    Log.i(TAG, "  #" + i + ": " + applicationSpecifiedCompletions[i]);
                }
            }
        }
        if (!mSettings.getCurrent().isApplicationSpecifiedCompletionsOn()) {
            return;
        }
        // If we have an update request in flight, we need to cancel it so it does not override
        // these completions.
        mHandler.cancelUpdateSuggestionStrip();
        if (applicationSpecifiedCompletions == null) {
            setNeutralSuggestionStrip();
            return;
        }

        final ArrayList<SuggestedWords.SuggestedWordInfo> applicationSuggestedWords =
                SuggestedWords.getFromApplicationSpecifiedCompletions(
                        applicationSpecifiedCompletions);
        final SuggestedWords suggestedWords = new SuggestedWords(applicationSuggestedWords,
                null /* rawSuggestions */,
                null /* typedWord */,
                false /* typedWordValid */,
                false /* willAutoCorrect */,
                false /* isObsoleteSuggestions */,
                SuggestedWords.INPUT_STYLE_APPLICATION_SPECIFIED /* inputStyle */,
                SuggestedWords.NOT_A_SEQUENCE_NUMBER);
        // When in fullscreen mode, show completions generated by the application forcibly
        setSuggestedWords(suggestedWords);
    }

    @Override
    public void onComputeInsets(final Insets outInsets) {
        super.onComputeInsets(outInsets);
        // This method may be called before {@link #setInputView(View)}.
        if (mInputView == null) {
            return;
        }
        final SettingsValues settingsValues = mSettings.getCurrent();
        final View visibleKeyboardView = mKeyboardSwitcher.getVisibleKeyboardView();
        if (visibleKeyboardView == null || !hasSuggestionStripView()) {
            return;
        }
        final int inputHeight = mInputView.getHeight();
        if (isImeSuppressedByHardwareKeyboard() && !visibleKeyboardView.isShown()) {
            // If there is a hardware keyboard and a visible software keyboard view has been hidden,
            // no visual element will be shown on the screen.
            outInsets.contentTopInsets = inputHeight;
            outInsets.visibleTopInsets = inputHeight;
            mInsetsUpdater.setInsets(outInsets);
            return;
        }
        int suggestionsHeight = (!mKeyboardSwitcher.isShowingEmojiPalettes() && mSuggestionStripView.getVisibility() == VISIBLE) || isSearchGif() || layoutClipboard.getVisibility() == VISIBLE || layoutAllFont.getVisibility() == VISIBLE ? mSuggestionStripView.getHeight() : 0;
        if (typeOldViewDisplay == Constant.KEYBOARD_VIEW_TYPE_EMOJI) {
            if (mKeyboardSwitcher.getVisibleKeyboardView() instanceof EmojiPalettesView && mKeyboardSwitcher.isShowingEmojiPalettes()) {
                suggestionsHeight = 0;
            }
        }
        int visibleTopY = inputHeight - visibleKeyboardView.getHeight() - suggestionsHeight;
        mSuggestionStripView.setMoreSuggestionsHeight(visibleTopY);
        // Need to set expanded touchable region only if a keyboard view is being shown.
        if (visibleKeyboardView.isShown()) {
            final int touchLeft = 0;
            int touchTop = mKeyboardSwitcher.isShowingMoreKeysPanel() ? 0 : visibleTopY;
            final int touchRight = visibleKeyboardView.getWidth();
            final int touchBottom = inputHeight
                    // Extend touchable region below the keyboard.
                    + EXTENDED_TOUCHABLE_REGION_HEIGHT;
            if (changeSizeHeightKbView != null && changeSizeHeightKbView.getVisibility() == View.VISIBLE) {
                setDragChangeHeightSizeKbView();
                //Timber.e("changeSizeHeightKbView.getHeight(): " + changeSizeHeightKbView.getHeight() + " inputHeight: " + inputHeight);
                touchTop = Math.min(touchTop, touchBottom - changeSizeHeightKbView.getHeight());
            }
            if (isViewTranslateShow()) {
                touchTop = touchTop - viewTranslate.getHeight();
                visibleTopY = visibleTopY - viewTranslate.getHeight();
            }
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_REGION;
            if (isChooseLanguageTranslate()) {
                outInsets.touchableRegion.set(touchLeft, 0, touchRight, touchBottom);
            } else {
                outInsets.touchableRegion.set(touchLeft, touchTop, touchRight, touchBottom);
            }
            // outInsets.touchableRegion.set(touchLeft, touchTop, touchRight, touchBottom);
        }

        outInsets.contentTopInsets = visibleTopY;
        outInsets.visibleTopInsets = visibleTopY;
        mInsetsUpdater.setInsets(outInsets);
    }

    public boolean isChooseLanguageTranslate() {
        try {
            return viewChooseLanguage.getVisibility() == View.VISIBLE;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void startShowingInputView(final boolean needsToLoadKeyboard) {
        mIsExecutingStartShowingInputView = true;
        // This {@link #showWindow(boolean)} will eventually call back
        // {@link #onEvaluateInputViewShown()}.
        showWindow(true /* showInput */);
        mIsExecutingStartShowingInputView = false;
        if (needsToLoadKeyboard) {
            loadKeyboard();
        }
    }

    public void stopShowingInputView() {
        showWindow(false /* showInput */);
    }

    @Override
    public boolean onShowInputRequested(final int flags, final boolean configChange) {
        if (isImeSuppressedByHardwareKeyboard()) {
            return true;
        }
        return super.onShowInputRequested(flags, configChange);
    }

    @Override
    public boolean onEvaluateInputViewShown() {
        if (mIsExecutingStartShowingInputView) {
            return true;
        }
        return super.onEvaluateInputViewShown();
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        final SettingsValues settingsValues = mSettings.getCurrent();
        if (isImeSuppressedByHardwareKeyboard()) {
            // If there is a hardware keyboard, disable full screen mode.
            return false;
        }
        // Reread resource value here, because this method is called by the framework as needed.
        try {
            final boolean isFullscreenModeAllowed = Settings.readUseFullscreenMode(getResources());
            if (super.onEvaluateFullscreenMode() && isFullscreenModeAllowed) {
                // TODO: Remove this hack. Actually we should not really assume NO_EXTRACT_UI
                // implies NO_FULLSCREEN. However, the framework mistakenly does.  i.e. NO_EXTRACT_UI
                // without NO_FULLSCREEN doesn't work as expected. Because of this we need this
                // hack for now.  Let's get rid of this once the framework gets fixed.
                final EditorInfo ei = getCurrentInputEditorInfo();
                return !(ei != null && ((ei.imeOptions & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0));
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public void updateFullscreenMode() {
        super.updateFullscreenMode();
        updateSoftInputWindowLayoutParameters();
    }

    private void updateSoftInputWindowLayoutParameters() {
        // Override layout parameters to expand {@link SoftInputWindow} to the entire screen.
        // See {@link InputMethodService#setinputView(View)} and
        // {@link SoftInputWindow#updateWidthHeight(WindowManager.LayoutParams)}.
        final Window window = getWindow().getWindow();
        ViewLayoutUtils.updateLayoutHeightOf(window, LayoutParams.MATCH_PARENT);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // This method may be called before {@link #setInputView(View)}.
        if (mInputView != null) {
            // In non-fullscreen mode, {@link InputView} and its parent inputArea should expand to
            // the entire screen and be placed at the bottom of {@link SoftInputWindow}.
            // In fullscreen mode, these shouldn't expand to the entire screen and should be
            // coexistent with {@link #mExtractedArea} above.
            // See {@link InputMethodService#setInputView(View) and
            // com.android.internal.R.layout.input_method.xml.
//            if (System.currentTimeMillis() - timeAddDragView > 100) {
//                timeAddDragView = System.currentTimeMillis();
//                Log.d("duongcv", "updateSoftInputWindowLayoutParameters: ");
//                mInputView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        initDragHeightSizeKb(window, (float) mPrefs.getFloat(com.keyboard.zomj.common.Constant.HEIGHT_ROW_KEY, com.keyboard.zomj.common.Constant.VALUE_HEIGHT_DEFAULT_KEYBOARD));
//                    }
//                });
//            } else {
            if (System.currentTimeMillis() - timeAddDragView > 100) {
                timeAddDragView = System.currentTimeMillis();
                Log.d("duongcv", "updateSoftInputWindowLayoutParameters: ");
                mInputView.post(new Runnable() {
                    @Override
                    public void run() {
                        initDragHeightSizeKb(window, (float) mPrefs.getFloat(com.tapbi.spark.yokey.common.Constant.HEIGHT_ROW_KEY, com.tapbi.spark.yokey.common.Constant.VALUE_HEIGHT_DEFAULT_KEYBOARD));
                    }
                });
            } else {
//                if(changeSizeHeightKbView!=null)changeSizeHeightKbView.requestLayoutView();
            }

            final int layoutHeight = isFullscreenMode() ? LayoutParams.WRAP_CONTENT : LayoutParams.MATCH_PARENT;
            final View inputArea = window.findViewById(android.R.id.inputArea);
            ViewLayoutUtils.updateLayoutHeightOf(inputArea, layoutHeight);
            ViewLayoutUtils.updateLayoutGravityOf(inputArea, Gravity.BOTTOM);
            ViewLayoutUtils.updateLayoutHeightOf(mInputView, layoutHeight);
            isChangeSize = true;
        }
    }

    public int getCurrentAutoCapsState() {
        return mInputLogic.getCurrentAutoCapsState(mSettings.getCurrent());
    }

    public int getCurrentRecapitalizeState() {
        return mInputLogic.getCurrentRecapitalizeState();
    }

    /**
     * @param codePoints code points to get coordinates for.
     * @return x, y coordinates for this keyboard, as a flattened array.
     */
    public int[] getCoordinatesForCurrentKeyboard(final int[] codePoints) {
        final Keyboard keyboard = mKeyboardSwitcher.getKeyboard();
        if (null == keyboard) {
            return CoordinateUtils.newCoordinateArray(codePoints.length,
                    Constants.NOT_A_COORDINATE, Constants.NOT_A_COORDINATE);
        }
        return keyboard.getCoordinates(codePoints);
    }

    // Callback for the {@link SuggestionStripView}, to call when the important notice strip is
    // pressed.
    @Override
    public void showImportantNoticeContents() {
        PermissionsManager.get(this).requestPermissions(
                this /* PermissionsResultCallback */,
                null /* activity */, permission.READ_CONTACTS);
    }

    @Override
    public void onRequestPermissionsResult(boolean allGranted) {
        ImportantNoticeUtils.updateContactsNoticeShown(this /* context */);
        setNeutralSuggestionStrip();
    }

    public void displaySettingsDialog() {
        if (isShowingOptionDialog()) {
            return;
        }
//        showSubtypeSelectorAndSettings();
    }

    @Override
    public boolean onCustomRequest(final int requestCode) {
        if (isShowingOptionDialog()) return false;
        switch (requestCode) {
            case Constants.CUSTOM_CODE_SHOW_INPUT_METHOD_PICKER:
                if (mRichImm.hasMultipleEnabledIMEsOrSubtypes(true /* include aux subtypes */)) {
                    mRichImm.getInputMethodManager().showInputMethodPicker();
                    return true;
                }
                return false;
        }
        return false;
    }

    private boolean isShowingOptionDialog() {
        return mOptionsDialog != null && mOptionsDialog.isShowing();
    }

    public void switchLanguage(final InputMethodSubtype subtype) {
        final IBinder token = getWindow().getWindow().getAttributes().token;
        mRichImm.setInputMethodAndSubtype(token, subtype);
    }

    /**
     * Click language switch in keyboard
     */
    public void switchToNextSubtype() {
//        final IBinder token = getWindow().getWindow().getAttributes().token;
//        if (shouldSwitchToOtherInputMethods()) {
//            mRichImm.switchToNextInputMethod(token, true /* onlyCurrentIme */);
//            mKeyboardSwitcher.checkLanguageToSetFontNormalKeyBoard();
//            if (!App.getInstance().listFontNotUsed.isEmpty()) {
//                App.getInstance().listFontNotUsed.clear();
//            }
//            if (isViewTranslateShow() && viewTranslate != null) {
//                viewTranslate.startShowTranslate();
//            }
//            Timber.d("duc switchToNextSubtype " + System.currentTimeMillis());
//            Log.e("duongcv", "switchToNextSubtype: " + Constant.LIST_LANGUAGE_KEYBOARD_OTHER_QWERTY.contains(Settings.getLanguageInputMethod()));
//            return;
//        }
//        mSubtypeState.switchSubtype(token, mRichImm);

        final IBinder token = getWindow().getWindow().getAttributes().token;
        boolean isSwitched = mSubtypeState.switchSubtypeRGB(token, mRichImm);
        if (isSwitched) {
            isSwitchSubtype = true;
            mKeyboardSwitcher.updateSuggestionBySubtype();
            mHandler.postReopenDictionaries();
            loadSettings();
            loadKeyboardAsync();
//            if (!CommonUtil.checkImeStyleEnter(App.getInstance().keyId) && checkLanguageSupportFont()
//                    && mPrefs.getBoolean(Settings.PREF_COOL_FONT, false)) {
//                CommonUtil.customToast(getBaseContext(), getBaseContext().getResources().getString(R.string.language_current_not_support_font));
//            }
        }
    }

    public void loadKeyboardAsync() {
        Single.fromCallable(() -> {
                    StateKeyboardInfo stateKeyboardInfo = new StateKeyboardInfo(getCurrentAutoCapsState(), getCurrentRecapitalizeState());
                    if (mKeyboardSwitcher.getMainKeyboardView() != null) {
                        // Reload keyboard because the current language has been changed.
                        Timber.d("ducNQMessageEvent 2");
                        mKeyboardSwitcher.loadKeyboard(getCurrentInputEditorInfo(), mSettings.getCurrent(),
                                stateKeyboardInfo.currentAutoCapsState, stateKeyboardInfo.currentRecapitalizeState, true);

                    }
                    return stateKeyboardInfo;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(stateKeyboardInfo -> {
                    mRichImm.onSubtypeChanged(mRichImm.getCurrentInputMethodSubtype());
                    mInputLogic.onSubtypeChanged(SubtypeLocaleUtils.getCombiningRulesExtraValue(mRichImm.getCurrentInputMethodSubtype()),
                            mSettings.getCurrent());
                    try {
                        mKeyboardSwitcher.loadKeyboardAsyncDone(stateKeyboardInfo.currentAutoCapsState, stateKeyboardInfo.currentRecapitalizeState);
                    } catch (KeyboardLayoutSet.KeyboardLayoutSetException e) {
                        Log.w(TAG, "loading keyboard failed: " + e.mKeyboardId, e.getCause());
                    }
                    mKeyboardSwitcher.changeConfigLanguageBySubtype(mKeyboardSwitcher.getKeyboard());

                    if (showSettingInternal) {
                        if (mKeyboardSwitcher.getMainKeyboardView() != null) {
                            mKeyboardSwitcher.getMainKeyboardView().setGestureHandlingEnabledByUser(
                                    Settings.getInstance().getCurrent().mGestureInputEnabled,
                                    Settings.getInstance().getCurrent().mGestureTrailEnabled,
                                    Settings.getInstance().getCurrent().mGestureFloatingPreviewTextEnabled);
                        }

                    }
                }).doOnError(throwable -> {
                    isSwitchSubtype = false;
                    throwable.printStackTrace();
                }).subscribe();
    }

    public void changeConfigLanguageBySubtype() {
        mIsVietnameseSubType = mKeyboardSwitcher.ismIsVietnameseType();
        if (mIsVietnameseSubType) {
            isTelexVietnamese = mKeyboardSwitcher.isTelexVietnamese();
            isTelexVietnameseSimple = mKeyboardSwitcher.isTelexVietnameseSimple();
            isQwertyVietnamese = mKeyboardSwitcher.isQwertyVietnamese();
        }
        mIsKoreaSubType = mKeyboardSwitcher.ismIsKoreanType();
    }

    // TODO: Instead of checking for alphabetic keyboard here, separate keycodes for
    // alphabetic shift and shift while in symbol layout and get rid of this method.
    private int getCodePointForKeyboard(final int codePoint) {
        if (Constants.CODE_SHIFT == codePoint) {
            final Keyboard currentKeyboard = mKeyboardSwitcher.getKeyboard();
            if (null != currentKeyboard && currentKeyboard.mId.isAlphabetKeyboard()) {
                return codePoint;
            }
            return Constants.CODE_SYMBOL_SHIFT;
        }
        return codePoint;
    }

    // Implementation of {@link KeyboardActionListener}.
    @Override
    public void onCodeInput(final int codePoint, final int x, final int y,
                            final boolean isKeyRepeat, boolean isEmoji) {
        // TODO: this processing does not belong inside LatinIME, the caller should be doing this.
        final MainKeyboardView mainKeyboardView = mKeyboardSwitcher.getMainKeyboardView();
        mIsVietnameseSubType = mKeyboardSwitcher.ismIsVietnameseType();

        if (mIsVietnameseSubType) {
            isTelexVietnamese = mKeyboardSwitcher.isTelexVietnamese();
            isTelexVietnameseSimple = mKeyboardSwitcher.isTelexVietnameseSimple();
        }
        Log.d("duongcv", "onCodeInput: " + mIsVietnameseSubType + ":" + isTelexVietnamese + ":" + isTelexVietnameseSimple);

        // x and y include some padding, but everything down the line (especially native
        // code) needs the coordinates in the keyboard frame.
        // TODO: We should reconsider which coordinate system should be used to represent
        // keyboard event. Also we should pull this up -- LatinIME has no business doing
        // this transformation, it should be done already before calling onEvent.
        final int keyX = mainKeyboardView.getKeyX(x);
        final int keyY = mainKeyboardView.getKeyY(y);
        final Event event = createSoftwareKeypressEvent(getCodePointForKeyboard(codePoint), keyX, keyY, isKeyRepeat);

        if (mIsVietnameseSubType) {  // tiếng Việt
            if (!isSearchGif()) {
                final int codeToSend;
                if (Constants.CODE_SHIFT == codePoint) {
                    // TODO: Instead of checking for alphabetic keyboard here, separate keycodes for
                    // alphabetic shift and shift while in symbol layout.
                    final Keyboard currentKeyboard = mKeyboardSwitcher.getKeyboard();
                    if (null != currentKeyboard && currentKeyboard.mId.isAlphabetKeyboard()) {
                        codeToSend = codePoint;
                    } else {
                        codeToSend = Constants.CODE_SYMBOL_SHIFT;
                    }
                } else {
                    codeToSend = codePoint;
                }
                // onCodeOtherLanguage(event,isEmoji);
                onCodeVietnamese(codeToSend, x, y, event);
            } else {
                onCodeOtherLanguage(event, isEmoji);
                // onEvent(event, isEmoji);
            }
        } else {//các ngôn ngữ khác
            if (codePoint == Constants.CODE_EMOJI && viewTranslate != null && viewTranslate.getVisibility() == VISIBLE) {
//                KeyboardSwitcher.getInstance().updateStateEmoji();
                closeTranslate();
                handlerDelay.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onEmojiClick();
                        setColorFilterPlateView(App.getInstance().colorIconDefault);
                    }
                }, 10);
            }
            onCodeOtherLanguage(event, isEmoji);
            // onEvent(event, isEmoji);
        }
    }

    public void cancelSearchGif() {
        mKeyboardSwitcher.setAlphabetKeyboard();
    }

    public void startSearchGif() {
        mInputLogic.mConnection.finishComposingText();
        if (mInputView instanceof InputView) {
            ((InputView) mInputView).searchGif();
            if (editSelectrionView != null) {
                Timber.e("hachung editSelectrionView GONE:");
                editSelectrionView.setVisibility(View.GONE);
            }
            typeOldViewDisplay = Constant.KEYBOARD_VIEW_TYPE_KEY;
        }
    }

    public void searchGif() {
        mInputLogic.resetComposingState(true);
        if (mInputView != null)
            ((InputView) mInputView).showHeaderKeyboard(Constant.HEADER_KEYBOARD_TYPE_MENU);
        mKeyboardSwitcher.setEmojiKeyboard(false);
        if (mKeyboardSwitcher.getVisibleKeyboardView() instanceof EmojiPalettesView) {
            ((EmojiPalettesView) mKeyboardSwitcher.getVisibleKeyboardView()).setTextSearch(((InputView) mInputView).getTextSearch());

        }

    }

    public void changeTypeView(int typeOldViewDisplay) {
        this.typeOldViewDisplay = typeOldViewDisplay;
    }

    public boolean isSearchGif() {
        if (mInputView == null || !(mInputView instanceof InputView)) {
            return false;
        }
        Timber.d("ducNQ : isSearchGifed: " + ((InputView) mInputView).isSearchGif());

        return ((InputView) mInputView).isSearchGif();
    }

    public void deleteTextSearchGif() {
        if (mInputView instanceof InputView) {
            ((InputView) mInputView).deleteText();
        }
    }

    @Override
    public void onEmojiClick() {
//        mPrefs.edit().putInt(Settings.SAVE_STATE_SHIFT_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, mKeyboardSwitcher.mState.mAlphabetShiftState.mState).apply();
        if (mPrefs.getBoolean(Settings.PREF_AUTO_CAP, true)) {
            mPrefs.edit().putBoolean(Settings.SAVE_STATE_AUTO_CAPS_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, mPrefs.getBoolean(Settings.PREF_AUTO_CAP, true)).apply();
            mPrefs.edit().putBoolean(Settings.PREF_AUTO_CAP, false).apply();
        }
        Timber.e("Duongcv " + "set false");
        if (layoutSettingView != null) layoutSettingView.setVisibility(GONE);
        KeyboardSwitcher.getInstance().updateStateEmoji();
        typeOldViewDisplay = Constant.KEYBOARD_VIEW_TYPE_EMOJI;
        mKeyboardSwitcher.updateBgForEmojiPalettes();
        if (mKeyboardSwitcher.getVisibleKeyboardView() instanceof EmojiPalettesView) {
            ((EmojiPalettesView) mKeyboardSwitcher.getVisibleKeyboardView()).onEmojiGifShow(Constant.TYPE_EMOJI);
            ((EmojiPalettesView) mKeyboardSwitcher.getVisibleKeyboardView()).setEnabledText();
        }
    }

    @Override
    public void onStickerClick() {
        if (layoutSettingView != null) layoutSettingView.setVisibility(GONE);
        KeyboardSwitcher.getInstance().updateStateEmoji();
        typeOldViewDisplay = Constant.KEYBOARD_VIEW_TYPE_EMOJI;
        mKeyboardSwitcher.updateBgForEmojiPalettes();
        if (mKeyboardSwitcher.getVisibleKeyboardView() instanceof EmojiPalettesView) {
            ((EmojiPalettesView) mKeyboardSwitcher.getVisibleKeyboardView()).onEmojiGifShow(Constant.TYPE_STICKER);
        }
    }

    @Override
    public void onGifClick(Media media) {
//        mPrefs.edit().putInt(Settings.SAVE_STATE_SHIFT_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, mKeyboardSwitcher.mState.mAlphabetShiftState.mState).apply();
        if (mPrefs.getBoolean(Settings.PREF_AUTO_CAP, true)) {
            mPrefs.edit().putBoolean(Settings.SAVE_STATE_AUTO_CAPS_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, mPrefs.getBoolean(Settings.PREF_AUTO_CAP, true)).apply();
            mPrefs.edit().putBoolean(Settings.PREF_AUTO_CAP, false).apply();
        }
        Timber.e("Duongcv " + "set false");
//        KeyboardSwitcher.getInstance().updateStateEmoji();
//        typeOldViewDisplay = Constant.KEYBOARD_VIEW_TYPE_EMOJI;
//        mKeyboardSwitcher.updateBgForEmojiPalettes();
//        if (mKeyboardSwitcher.getVisibleKeyboardView() instanceof EmojiPalettesView) {
//            ((EmojiPalettesView) mKeyboardSwitcher.getVisibleKeyboardView()).onEmojiGifShow(false);
//        }
        final Image image = media.getImages().getFixedHeightSmall();
        url = image.getGifUrl();
        final Context context = this;
        Timber.d("onGifClick " + isGifSupport);
        if (!isGifSupport) {
            Timber.d("onGifClick on " + isGifSupport);
            // CommonUtil.customToast(App.getInstance(), "This EditText not support gif!");
            Toast.makeText(App.getInstance(), "This editText not support gif!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cachePath == null) {
            cachePath = new File(context.getCacheDir(), "gifs");
            if (!cachePath.exists()) {
                if (!cachePath.mkdirs()) {
                }
            }
        }
        file = new File(cachePath, "/" + media.getId() + ".gif");
        handlerCommitGif.sendMessage(new Message());
//        if (mKeyboardSwitcher.getVisibleKeyboardView() instanceof EmojiPalettesView){
//            Timber.d("mKeyboardSwitcher");
//            ((EmojiPalettesView) mKeyboardSwitcher.getVisibleKeyboardView()).resetGif();
//        }
    }

    // This method is public for testability of LatinIME, but also in the future it should
    // completely replace #onCodeInput.
    public void onEvent(@Nonnull final Event event, boolean isEmoji) {
        if (Constants.CODE_SHORTCUT == event.mKeyCode) {
            mRichImm.switchToShortcutIme(this);
        }
//        if (isSearchGif()) {
//            mInputLogic.onCodeSearchGifInput(mSettings.getCurrent(), event,
//                    mKeyboardSwitcher.getKeyboardShiftMode(),
//                    mKeyboardSwitcher.getCurrentKeyboardScriptId(), mHandler);
//            mKeyboardSwitcher.onEvent(event, getCurrentAutoCapsState(), getCurrentRecapitalizeState());
//        } else {
        final InputTransaction completeInputTransaction =
                mInputLogic.onCodeInput(mSettings.getCurrent(), event,
                        mKeyboardSwitcher.getKeyboardShiftMode(),
                        mKeyboardSwitcher.getCurrentKeyboardScriptId(), mHandler, isEmoji);
        updateStateAfterInputTransaction(completeInputTransaction);
        mKeyboardSwitcher.onEvent(event, getCurrentAutoCapsState(), getCurrentRecapitalizeState());


        //  }
    }

    public void clearSuggestions() {
        setSuggestions(SuggestedWords.EMPTY, false);
        // setAutoCorrectionIndicator(false);
    }

    private void setSuggestions(final SuggestedWords words, final boolean isAutoCorrection) {
        if (mSuggestionStripView != null && isCheckShowSuggestion()) {
            Log.d("hachung", "setSuggestions: " + words.mSuggestedWordInfoList.size());
            Timber.e("hachung mSuggestedWordInfoList:" + words.mSuggestedWordInfoList.size());
            mSuggestionStripView.setSuggestions(words, false);
            // mKeyboardSwitcher.onAutoCorrectionStateChanged(isAutoCorrection);
        }
    }

    // Called from PointerTracker through the KeyboardActionListener interface
    @Override
    public void onTextInput(String rawText) {
        // TODO: have the keyboard pass the correct key code when we need it.
        Timber.d("ducNQ : onTextInput: " + rawText);
        if (rawText == null) {
            return;
        }

        if (rawText.length() > 2) {
        } else {
            if (isTranslationFunctionEnabled()) {
                viewTranslate.appendTextToEditText(rawText);
                return;
            }
            Font font = new Font();
            String key_font = mPrefs.getString(Constant.USING_FONT, Constant.FONT_NORMAL);
            CharSequence[] sp = font.getFont(key_font);
            rawText = CommonUtil.replaceTextFontOUTPUT(Constant.LIST_LANGUAGE_KEYBOARD_OTHER_QWERTY.contains(Settings.getLanguageKeyBoardCurrent()), sp, rawText, rawText, font, key_font);

        }
        inputText(rawText);
    }

    public void appendTextTranslate(String rawText) {
        if (viewTranslate != null) {
            viewTranslate.appendTextToEditText(rawText);
        }
    }

    private void onCodeOtherLanguage(Event event, boolean isEmoji) {
        if (isSearchGif()) {
            mInputLogic.onCodeSearchGifInput(mSettings.getCurrent(), event,
                    mKeyboardSwitcher.getKeyboardShiftMode(),
                    mKeyboardSwitcher.getCurrentKeyboardScriptId(), mHandler);
            if (App.getInstance().mPrefs.getBoolean(Settings.PREF_AUTO_CAP, true)) {
                App.getInstance().mPrefs.edit().putBoolean(Settings.SAVE_STATE_AUTO_CAPS_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, App.getInstance().mPrefs.getBoolean(Settings.PREF_AUTO_CAP, true)).apply();
                App.getInstance().mPrefs.edit().putBoolean(Settings.PREF_AUTO_CAP, false).apply();
            }
            mKeyboardSwitcher.onEvent(event, getCurrentAutoCapsState(), getCurrentRecapitalizeState());
        } else if (isTranslationFunctionEnabled()) {
            translateOtherLanguage(event, isEmoji);
        } else {
            //codeInputOtherLanguage(event.mCodePoint, event);
            onEvent(event, isEmoji);
        }
    }

    // onKeyDown and onKeyUp are the main events we are interested in. There are two more events
    // related to handling of hardware key events that we may want to implement in the future:
    // boolean onKeyLongPress(final int keyCode, final KeyEvent event);
    // boolean onKeyMultiple(final int keyCode, final int count, final KeyEvent event);

    private void translateOtherLanguage(Event event, boolean isEmoji) {
        if (event.mKeyCode == Constants.CODE_DELETE) {
            viewTranslate.deleteText();
        } else if (event.mCodePoint == 10 || event.mKeyCode == -11) {
            mInputLogic.mConnection.finishComposingText();
            Timber.d("ducNQ : checkSuggest: 1");
            mInputLogic.mConnection.setComposingText(" ", 1);
            mInputLogic.mConnection.finishComposingText();
            viewTranslate.doneCharacterTranslate();
        } else if (event.mKeyCode >= 0) {
            char[] currencySymbol = Character.toChars(event.mCodePoint);
            viewTranslate.appendTextToEditText(String.valueOf(currencySymbol[0]));
            if (mKeyboardSwitcher.getKeyboardShiftMode() != WordComposer.CAPS_MODE_OFF
                    && mKeyboardSwitcher.getKeyboardShiftMode() != WordComposer.CAPS_MODE_MANUAL_SHIFT_LOCKED) {
                mKeyboardSwitcher.requestUpdatingShiftState(WordComposer.CAPS_MODE_OFF,
                        getCurrentRecapitalizeState());
            }
        } else {
            onEvent(event, isEmoji);
            //codeInputOtherLanguage(event.mCodePoint, event);
        }

    }

    private void inputText(String rawText) {
        final Event event = Event.createSoftwareTextEvent(rawText, Constants.CODE_OUTPUT_TEXT);
        final InputTransaction completeInputTransaction =
                mInputLogic.onTextInput(mSettings.getCurrent(), event,
                        mKeyboardSwitcher.getKeyboardShiftMode(), mHandler);
        updateStateAfterInputTransaction(completeInputTransaction);
        mKeyboardSwitcher.onEvent(event, getCurrentAutoCapsState(), getCurrentRecapitalizeState());
    }

    @Override
    public void onTextEmojiInput(String text) {
        inputText(text);
    }

    @Override
    public void onImageClick(String path) {
    }

    public boolean commitGifImage(Uri contentUri, String imageDescription, Uri linkUri) {
        InputContentInfoCompat inputContentInfo = new InputContentInfoCompat(
                contentUri,
                new ClipDescription(imageDescription, new String[]{"image/png", "image/gif"}),
                linkUri
        );
        Timber.d("ducNQ : commitGifImage: " + imageDescription);
        InputConnection inputConnection = getCurrentInputConnection();
        EditorInfo editorInfo = getCurrentInputEditorInfo();
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            flags |= InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
        } else {
            try {
                grantUriPermission(
                        editorInfo.packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) {

            }
        }
        try {
            return InputConnectionCompat.commitContent(inputConnection, editorInfo, inputContentInfo, flags, new Bundle());
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean validatePackageName(@Nullable EditorInfo editorInfo) {
        if (editorInfo == null) {
            return false;
        }
        final String packageName = editorInfo.packageName;
        if (packageName == null) {
            return false;
        }

        // In Android L MR-1 and prior devices, EditorInfo.packageName is not a reliable identifier
        // of the target application because:
        //   1. the system does not verify it [1]
        //   2. InputMethodManager.startInputInner() had filled EditorInfo.packageName with
        //      view.getContext().getPackageName() [2]
        // [1]: https://android.googlesource.com/platform/frameworks/base/+/a0f3ad1b5aabe04d9eb1df8bad34124b826ab641
        // [2]: https://android.googlesource.com/platform/frameworks/base/+/02df328f0cd12f2af87ca96ecf5819c8a3470dc8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        }

        final InputBinding inputBinding = getCurrentInputBinding();
        if (inputBinding == null) {
            // Due to b.android.com/225029, it is possible that getCurrentInputBinding() returns
            // null even after onStartInputView() is called.
            // TODO: Come up with a way to work around this bug....
            return false;
        }
        final int packageUid = inputBinding.getUid();

        final AppOpsManager appOpsManager =
                (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        try {
            appOpsManager.checkPackage(packageUid, packageName);
        } catch (Exception e) {
            return false;
        }
        return true;

    }

    @Override
    public void onStartBatchInput() {

        mInputLogic.onStartBatchInput(mSettings.getCurrent(), mKeyboardSwitcher, mHandler);
        mGestureConsumer.onGestureStarted(
                mRichImm.getCurrentSubtypeLocale(),
                mKeyboardSwitcher.getKeyboard());
    }

    @Override
    public void onUpdateBatchInput(final InputPointers batchPointers) {
        mInputLogic.onUpdateBatchInput(batchPointers);
    }

    @Override
    public void onEndBatchInput(final InputPointers batchPointers) {
        mInputLogic.onEndBatchInput(batchPointers);
        mGestureConsumer.onGestureCompleted(batchPointers);
    }

    @Override
    public void onCancelBatchInput() {
        mInputLogic.onCancelBatchInput(mHandler);
        mGestureConsumer.onGestureCanceled();
    }

    /**
     * To be called after the InputLogic has gotten a chance to act on the suggested words by the
     * IME for the full gesture, possibly updating the TextView to reflect the first suggestion.
     * <p>
     * This method must be run on the UI Thread.
     *
     * @param suggestedWords suggested words by the IME for the full gesture.
     */
    public void onTailBatchInputResultShown(final SuggestedWords suggestedWords) {
        mGestureConsumer.onImeSuggestionsProcessed(suggestedWords,
                mInputLogic.getComposingStart(), mInputLogic.getComposingLength(),
                mDictionaryFacilitator);
    }

    // This method must run on the UI Thread.
    void showGesturePreviewAndSuggestionStrip(@Nonnull final SuggestedWords suggestedWords,
                                              final boolean dismissGestureFloatingPreviewText) {
        showSuggestionStrip(suggestedWords);
        final MainKeyboardView mainKeyboardView = mKeyboardSwitcher.getMainKeyboardView();
        mainKeyboardView.showGestureFloatingPreviewText(suggestedWords,
                dismissGestureFloatingPreviewText /* dismissDelayed */);
    }

    // Called from PointerTracker through the KeyboardActionListener interface
    @Override
    public void onFinishSlidingInput() {
        // User finished sliding input.
        mKeyboardSwitcher.onFinishSlidingInput(getCurrentAutoCapsState(),
                getCurrentRecapitalizeState());
    }

    // Called from PointerTracker through the KeyboardActionListener interface
    @Override
    public void onCancelInput() {
        // User released a finger outside any key
        // Nothing to do so far.
    }

    public boolean hasSuggestionStripView() {
        return null != mSuggestionStripView;
    }

    public void setSuggestedWords(SuggestedWords suggestedWords) {
        final SettingsValues currentSettingsValues = mSettings.getCurrent();
        mInputLogic.setSuggestedWords(suggestedWords);
        // TODO: Modify this when we support suggestions with hard keyboard
        if (!hasSuggestionStripView()) {
            return;
        }
        if (!onEvaluateInputViewShown()) {
            return;
        }

        final boolean shouldShowImportantNotice = ImportantNoticeUtils.shouldShowImportantNotice(this, currentSettingsValues);
        final boolean shouldShowSuggestionCandidates = currentSettingsValues.mInputAttributes.mShouldShowSuggestions && currentSettingsValues.isSuggestionsEnabledPerUserSettings();
        final boolean shouldShowSuggestionsStripUnlessPassword = shouldShowImportantNotice
                || currentSettingsValues.mShowsVoiceInputKey
                || shouldShowSuggestionCandidates
                || currentSettingsValues.isApplicationSpecifiedCompletionsOn();
        final boolean shouldShowSuggestionsStrip = shouldShowSuggestionsStripUnlessPassword
                && !currentSettingsValues.mInputAttributes.mIsPasswordField;
        mSuggestionStripView.updateVisibility(shouldShowSuggestionsStrip, isFullscreenMode());
        if (!shouldShowSuggestionsStrip) {
            return;
        }

        final boolean isEmptyApplicationSpecifiedCompletions =
                currentSettingsValues.isApplicationSpecifiedCompletionsOn()
                        && suggestedWords.isEmpty();
        final boolean noSuggestionsFromDictionaries = suggestedWords.isEmpty()
                || suggestedWords.isPunctuationSuggestions()
                || isEmptyApplicationSpecifiedCompletions;
        final boolean isBeginningOfSentencePrediction = (suggestedWords.mInputStyle
                == SuggestedWords.INPUT_STYLE_BEGINNING_OF_SENTENCE_PREDICTION);
        final boolean noSuggestionsToOverrideImportantNotice = noSuggestionsFromDictionaries
                || isBeginningOfSentencePrediction;
//        if (shouldShowImportantNotice && noSuggestionsToOverrideImportantNotice) {
//            if (mSuggestionStripView.maybeShowImportantNoticeTitle()) {
//                return;
//            }
//        }

        if (currentSettingsValues.isSuggestionsEnabledPerUserSettings()
                || currentSettingsValues.isApplicationSpecifiedCompletionsOn()
                // We should clear the contextual strip if there is no suggestion from dictionaries.
                || noSuggestionsFromDictionaries) {
            if (isCheckShowSuggestion()) {
                Log.d("duongcv", "setSuggestedWords: " + suggestedWords);
                mSuggestionStripView.setSuggestions(suggestedWords,
                        mRichImm.getCurrentSubtype().isRtlSubtype());
            }
        }
    }

    // TODO[IL]: Move this out of LatinIME.
    public void getSuggestedWords(final int inputStyle, final int sequenceNumber,
                                  final OnGetSuggestedWordsCallback callback) {
        final Keyboard keyboard = mKeyboardSwitcher.getKeyboard();
        if (keyboard == null) {
            callback.onGetSuggestedWords(SuggestedWords.getEmptyInstance());
            return;
        }
        mInputLogic.getSuggestedWords(mSettings.getCurrent(), keyboard,
                mKeyboardSwitcher.getKeyboardShiftMode(), inputStyle, sequenceNumber, callback);
    }

    @Override
    public void showSuggestionStrip(final SuggestedWords suggestedWords) {
        if (suggestedWords.isEmpty()) {
            setNeutralSuggestionStrip();
        } else {
            setSuggestedWords(suggestedWords);
        }
        // Cache the auto-correction in accessibility code so we can speak it if the user
        // touches a key that will insert it.
        AccessibilityUtils.getInstance().setAutoCorrection(suggestedWords);
    }

    // Called from {@link SuggestionStripView} through the {@link SuggestionStripView#Listener}
    // interface
    @Override
    public void pickSuggestionManually(final SuggestedWordInfo suggestionInfo) {
        final InputTransaction completeInputTransaction = mInputLogic.onPickSuggestionManually(
                mSettings.getCurrent(), suggestionInfo,
                mKeyboardSwitcher.getKeyboardShiftMode(),
                mKeyboardSwitcher.getCurrentKeyboardScriptId(),
                mHandler);
        updateStateAfterInputTransaction(completeInputTransaction);
    }

    // This will show either an empty suggestion strip (if prediction is enabled) or
    // punctuation suggestions (if it's disabled).
    @Override
    public void setNeutralSuggestionStrip() {
        final SettingsValues currentSettings = mSettings.getCurrent();
        final SuggestedWords neutralSuggestions = currentSettings.mBigramPredictionEnabled
                ? SuggestedWords.getEmptyInstance()
                : currentSettings.mSpacingAndPunctuations.mSuggestPuncList;
        setSuggestedWords(neutralSuggestions);
    }

    // Outside LatinIME, only used by the {@link InputTestsBase} test suite.
    @UsedForTesting
    void loadKeyboard() {
        // Since we are switching languages, the most urgent thing is to let the keyboard graphics
        // update. LoadKeyboard does that, but we need to wait for buffer flip for it to be on
        // the screen. Anything we do right now will delay this, so wait until the next frame
        // before we do the rest, like reopening dictionaries and updating suggestions. So we
        // post a message.
        mHandler.postReopenDictionaries();
        loadSettings();
        if (mKeyboardSwitcher.getMainKeyboardView() != null) {
            // Reload keyboard because the current language has been changed.
            mKeyboardSwitcher.loadKeyboard(getCurrentInputEditorInfo(), mSettings.getCurrent(),
                    getCurrentAutoCapsState(), getCurrentRecapitalizeState(), false);
        }
    }

    /**
     * After an input transaction has been executed, some state must be updated. This includes
     * the shift state of the keyboard and suggestions. This method looks at the finished
     * inputTransaction to find out what is necessary and updates the state accordingly.
     *
     * @param inputTransaction The transaction that has been executed.
     */
    public void updateStateAfterInputTransaction(final InputTransaction inputTransaction) {
        switch (inputTransaction.getRequiredShiftUpdate()) {
            case InputTransaction.SHIFT_UPDATE_LATER:
                mHandler.postUpdateShiftState();
                break;
            case InputTransaction.SHIFT_UPDATE_NOW:
                mKeyboardSwitcher.requestUpdatingShiftState(getCurrentAutoCapsState(),
                        getCurrentRecapitalizeState());
                break;
            default: // SHIFT_NO_UPDATE
        }
        if (inputTransaction.requiresUpdateSuggestions()) {
            final int inputStyle;
            if (inputTransaction.mEvent.isSuggestionStripPress()) {
                // Suggestion strip press: no input.
                inputStyle = SuggestedWords.INPUT_STYLE_NONE;
            } else if (inputTransaction.mEvent.isGesture()) {
                inputStyle = SuggestedWords.INPUT_STYLE_TAIL_BATCH;
            } else {
                inputStyle = SuggestedWords.INPUT_STYLE_TYPING;
            }
            mHandler.postUpdateSuggestionStrip(inputStyle);
        }
        if (inputTransaction.didAffectContents()) {
            mSubtypeState.setCurrentSubtypeHasBeenUsed();
        }
    }

    private void hapticAndAudioFeedback(final int code, final int repeatCount) {
        final MainKeyboardView keyboardView = mKeyboardSwitcher.getMainKeyboardView();
        if (keyboardView != null && keyboardView.isInDraggingFinger()) {
            // No need to feedback while finger is dragging.
            return;
        }
        if (repeatCount > 0) {
            if (code == Constants.CODE_DELETE && !mInputLogic.mConnection.canDeleteCharacters()) {
                // No need to feedback when repeat delete key will have no effect.
                return;
            }
            // TODO: Use event time that the last feedback has been generated instead of relying on
            // a repeat count to thin out feedback.
            if (repeatCount % PERIOD_FOR_AUDIO_AND_HAPTIC_FEEDBACK_IN_KEY_REPEAT == 0) {
                return;
            }
        }
        final AudioAndHapticFeedbackManager feedbackManager =
                AudioAndHapticFeedbackManager.getInstance();
        if (repeatCount == 0) {
            // TODO: Reconsider how to perform haptic feedback when repeating key.
            feedbackManager.performHapticFeedback(keyboardView);
        }
        feedbackManager.performAudioFeedback(code);
    }

    // Callback of the {@link KeyboardActionListener}. This is called when a key is depressed;
    // release matching call is {@link #onReleaseKey(int,boolean)} below.
    @Override
    public void onPressKey(final int primaryCode, final int repeatCount,
                           final boolean isSinglePointer) {
        mKeyboardSwitcher.onPressKey(primaryCode, isSinglePointer, getCurrentAutoCapsState(), getCurrentRecapitalizeState());
        hapticAndAudioFeedback(primaryCode, repeatCount);
    }

    // Callback of the {@link KeyboardActionListener}. This is called when a key is released;
    // press matching call is {@link #onPressKey(int,int,boolean)} above.
    @Override
    public void onReleaseKey(final int primaryCode, final boolean withSliding) {
        mKeyboardSwitcher.onReleaseKey(primaryCode, withSliding, getCurrentAutoCapsState(),
                getCurrentRecapitalizeState());
    }

    private HardwareEventDecoder getHardwareKeyEventDecoder(final int deviceId) {
        final HardwareEventDecoder decoder = mHardwareEventDecoders.get(deviceId);
        if (null != decoder) return decoder;
        // TODO: create the decoder according to the specification
        final HardwareEventDecoder newDecoder = new HardwareKeyboardEventDecoder(deviceId);
        mHardwareEventDecoders.put(deviceId, newDecoder);
        return newDecoder;
    }

    // Hooks for hardware keyboard
    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent keyEvent) {
        if (mEmojiAltPhysicalKeyDetector == null) {
            mEmojiAltPhysicalKeyDetector = new EmojiAltPhysicalKeyDetector(
                    getApplicationContext().getResources());
        }
        mEmojiAltPhysicalKeyDetector.onKeyDown(keyEvent);
        if (!ProductionFlags.IS_HARDWARE_KEYBOARD_SUPPORTED) {
            return super.onKeyDown(keyCode, keyEvent);
        }
        final Event event = getHardwareKeyEventDecoder(
                keyEvent.getDeviceId()).decodeHardwareKey(keyEvent);
        // If the event is not handled by LatinIME, we just pass it to the parent implementation.
        // If it's handled, we return true because we did handle it.
        if (event.isHandled()) {
            Log.d("duongcv", "onKeyDown: ");
            mInputLogic.onCodeInput(mSettings.getCurrent(), event,
                    mKeyboardSwitcher.getKeyboardShiftMode(),
                    // TODO: this is not necessarily correct for a hardware keyboard right now
                    mKeyboardSwitcher.getCurrentKeyboardScriptId(),
                    mHandler, false);
            return true;
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent keyEvent) {
        if (mEmojiAltPhysicalKeyDetector == null) {
            mEmojiAltPhysicalKeyDetector = new EmojiAltPhysicalKeyDetector(
                    getApplicationContext().getResources());
        }
        mEmojiAltPhysicalKeyDetector.onKeyUp(keyEvent);
        if (!ProductionFlags.IS_HARDWARE_KEYBOARD_SUPPORTED) {
            return super.onKeyUp(keyCode, keyEvent);
        }
        final long keyIdentifier = keyEvent.getDeviceId() << 32 + keyEvent.getKeyCode();
        if (mInputLogic.mCurrentlyPressedHardwareKeys.remove(keyIdentifier)) {
            return true;
        }
        return super.onKeyUp(keyCode, keyEvent);
    }

    void launchSettings(final String extraEntryValue) {
//        mInputLogic.commitTyped(mSettings.getCurrent(), LastComposedWord.NOT_A_SEPARATOR);
//        requestHideSelf(0);
//        final MainKeyboardView mainKeyboardView = mKeyboardSwitcher.getMainKeyboardView();
//        if (mainKeyboardView != null) {
//            mainKeyboardView.closing();
//        }
//        final Intent intent = new Intent();
//        intent.setClass(LatinIME.this, SettingsActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
//                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra(SettingsActivity.EXTRA_SHOW_HOME_AS_UP, false);
//        intent.putExtra(SettingsActivity.EXTRA_ENTRY_KEY, extraEntryValue);
//        startActivity(intent);
    }

    private void showSubtypeSelectorAndSettings() {
//        final CharSequence title = getString(R.string.english_ime_input_options);
//        // TODO: Should use new string "Select active input modes".
//        final CharSequence languageSelectionTitle = getString(R.string.language_selection_title);
//        final CharSequence[] items = new CharSequence[]{
//                languageSelectionTitle,
//                getString(ApplicationUtils.getActivityTitleResId(this, SettingsActivity.class))
//        };
//        final String imeId = mRichImm.getInputMethodIdOfThisIme();
//        final OnClickListener listener = new OnClickListener() {
//            @Override
//            public void onClick(DialogInterface di, int position) {
//                di.dismiss();
//                switch (position) {
//                    case 0:
//                        final Intent intent = IntentUtils.getInputLanguageSelectionIntent(
//                                imeId,
//                                Intent.FLAG_ACTIVITY_NEW_TASK
//                                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
//                                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        intent.putExtra(Intent.EXTRA_TITLE, languageSelectionTitle);
//                        startActivity(intent);
//                        break;
//                    case 1:
//                        launchSettings(SettingsActivity.EXTRA_ENTRY_VALUE_LONG_PRESS_COMMA);
//                        break;
//                }
//            }
//        };
//        final AlertDialog.Builder builder = new AlertDialog.Builder(
//                DialogUtils.getPlatformDialogThemeContext(this));
//        builder.setItems(items, listener).setTitle(title);
//        final AlertDialog dialog = builder.create();
//        dialog.setCancelable(true /* cancelable */);
//        dialog.setCanceledOnTouchOutside(true /* cancelable */);
//        showOptionDialog(dialog);
    }

    private void initViewAllFont(View view) {
        layoutAllFont = view.findViewById(R.id.layoutFont);
        imgBackAllFont = layoutAllFont.findViewById(R.id.imgViewBackFont);
        txtTitleFont = layoutAllFont.findViewById(R.id.txtTitleFont);
        rclAllFont = layoutAllFont.findViewById(R.id.rclFont);
        spinKitViewFont = layoutAllFont.findViewById(R.id.spinkitFont);
        ctlAddFont = layoutAllFont.findViewById(R.id.cllAddFont);
        txtAddFont = layoutAllFont.findViewById(R.id.txtAddFont);
        txtCancelFont = layoutAllFont.findViewById(R.id.txtCancelFont);
        itemFonts = new ArrayList<>();
        allFontOnKeyboardAdapter = new AllFontOnKeyboardAdapter(/*itemFonts,*/ this);
        rclAllFont.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rclAllFont.setAdapter(allFontOnKeyboardAdapter);
        allFontOnKeyboardAdapter.setListenerChangeItemFont(new AllFontOnKeyboardAdapter.ListenerChangeItemFont() {
            @Override
            public void getItem(ItemFont itemFont, int position) {
                ctlAddFont.setVisibility(VISIBLE);
                itemFontAdd = itemFont;
                txtAddFont.setEnabled(true);
            }
        });

        layoutAllFont.setOnClickListener(view1 -> {
        });
        imgBackAllFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputView(true);
                Timber.d("ducNQ : showSettingView: 3");
//                if (mSuggestionStripView != null) mSuggestionStripView.showUIMenu();
                if (mInputView != null)
                    ((InputView) mInputView).showHeaderKeyboard(Constant.HEADER_KEYBOARD_TYPE_MENU);
                layoutAllFont.setVisibility(GONE);
            }
        });

        ctlAddFont.setOnClickListener(view12 -> {
        });

        txtCancelFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ctlAddFont != null) ctlAddFont.setVisibility(GONE);
            }
        });

        txtAddFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtAddFont.setEnabled(false);
                openSetting(Constant.KEY_SCREEN_FONT, true);
            }
        });

    }

    public void showAllFont() {
        if (ctlAddFont != null) ctlAddFont.setVisibility(GONE);
        itemFonts = App.getInstance().fontRepository.listAllFont;
        if (itemFonts != null && itemFonts.size() > 0 && allFontOnKeyboardAdapter != null) {
            allFontOnKeyboardAdapter.changeList(itemFonts);
        }
        if (mSuggestionStripView != null) mSuggestionStripView.hideUIMenu();
        layoutAllFont.setVisibility(VISIBLE);
        layoutClipboard.setVisibility(GONE);
        showInputView(false);
        int color = App.getInstance().colorIconDefault;
        imgBackAllFont.setColorFilter(color);
        txtTitleFont.setTextColor(color);
        if (editSelectrionView != null) {
            Timber.e("hachung editSelectrionView GONE:");
            editSelectrionView.setVisibility(GONE);
        }
        if (layoutSettingView != null) layoutSettingView.setVisibility(GONE);
        showSettingInternal = false;
    }

    //Todo: Duongcv setup init clipboard
    private void initViewClipboard(View view) {
        clipboardRepository = new ClipboardRepository(this);
        layoutClipboard = view.findViewById(R.id.layoutClipboard);
        imgBackClipboard = layoutClipboard.findViewById(R.id.imgViewBackClipboard);
        txtTitleClipboard = layoutClipboard.findViewById(R.id.txtTitleClipboard);
        rclClipboard = layoutClipboard.findViewById(R.id.rclClipboard);
        txtNodataClipboard = layoutClipboard.findViewById(R.id.txtNodataClipboard);
        spinKitViewClipboard = layoutClipboard.findViewById(R.id.spinkitClipboard);
        clipBoardAdapter = new ClipBoardAdapter(clipboardRepository, new ClipBoardAdapter.OnClickClipBoardListener() {
            @Override
            public void onClickClipContent(@Nullable String content) {
                Timber.d("duongcv : commit: " + content);
                mInputLogic.mConnection.setComposingText(content, 1);
                mInputLogic.mConnection.finishComposingText();
            }

            @Override
            public void showText() {
                txtNodataClipboard.setVisibility(VISIBLE);
            }
        });
//        clipBoardAdapter = new ClipBoardAdapter(clipboardRepository, content -> {
//            mInputLogic.mConnection.setComposingText(content, 1);
//            mInputLogic.mConnection.finishComposingText();
//        });
        rclClipboard.setItemAnimator(null);
        rclClipboard.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rclClipboard.setAdapter(clipBoardAdapter);
        imgBackClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutClipboard.getVisibility() == VISIBLE) {
                    showInputView(true);
                    Timber.d("ducNQ : showSettingView: 4");
                    showClipboard(GONE, VISIBLE);
//                    if (mSuggestionStripView != null) mSuggestionStripView.showUIMenu();
                    if (mInputView != null)
                        ((InputView) mInputView).showHeaderKeyboard(Constant.HEADER_KEYBOARD_TYPE_MENU);
                    clipboardRepository.refeshClipboardThread(clipBoardAdapter.getCurrentList()).subscribe();
                }
            }
        });
        layoutClipboard.setOnClickListener(v -> {

        });
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(() -> {
            try {
                ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    CharSequence textToPaste = clipData.getItemAt(0).getText();
                    if (textToPaste != null) {
                        clipboardRepository.addClipboardThread(textToPaste.toString()).subscribe(new SingleObserver<Boolean>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(@NonNull Boolean aBoolean) {
                                loadDataClipboard();
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {

                            }
                        });
                    }
                }
            } catch (SecurityException e) {
            }


        });
    }

    public void setDragChangeHeightSizeKbView() {
        if (changeSizeHeightKbView != null && isChangeSize) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) (mKeyboardSwitcher.getHeightKeyboard() + changeSizeHeightKbView.heightDrag / 2));
            params.gravity = Gravity.BOTTOM;
            changeSizeHeightKbView.setLayoutParams(params);
            //dragChangeSizeKb.setTranslationY((float) - ((mKeyboardSwitcher.getHeightKeyboard() +suggestionsHeight) * (1 - scale)));
            isChangeSize = false;
            //mInputView.setTranslationY((float) ((mKeyboardSwitcher.getHeightKeyboard()) * (1 - scale)) + suggestionsHeight * (scale));
        }
    }

    public void setScaleValues(float valuesScale) {
        this.scale = valuesScale;
        reloadKeyBoard();
    }

    public void showDragChangeSizeKb(boolean isShow) {
        if (isShow) {
            changeSizeHeightKbView.showView(true);
        } else {
            if (changeSizeHeightKbView != null) changeSizeHeightKbView.showView(false);
        }
    }

    public void setCurrentSettingsValues(SettingsValues settingsValues) {
        this.currentSettingsValues = settingsValues;
    }

    public void reloadKeyBoard() {
        mKeyboardSwitcher.loadKeyboard(getCurrentInputEditorInfo(),
                mSettings.getCurrent(), getCurrentAutoCapsState(),
                getCurrentRecapitalizeState(), false);
    }

    public void initDragHeightSizeKb(Window window, float scale) {
        if (changeSizeHeightKbView != null && changeSizeHeightKbView.getParent() != null) {
            ((FrameLayout) changeSizeHeightKbView.getParent()).removeView(changeSizeHeightKbView);
//            changeSizeHeightKbView.invalidate();
//            changeSizeHeightKbView.requestLayout();
//            changeSizeHeightKbView.requestLayoutView();
        }
        changeSizeHeightKbView = new DragChangeSizeHeightKbView(App.getInstance().getBaseContext());
        changeSizeHeightKbView.setLatinIME(LatinIME.this, currentSettingsValues, scale);
        Dialog dialog = getWindow();
        dialog.addContentView(changeSizeHeightKbView, new FrameLayout.LayoutParams(KeyboardSwitcher.getInstance().getWidthKeyboard(), KeyboardSwitcher.getInstance().getHeightKeyboard()));
        ViewLayoutUtils.updateLayoutGravityOf(changeSizeHeightKbView, Gravity.BOTTOM);
        changeSizeHeightKbView.requestLayoutView();
        changeSizeHeightKbView.showView(false);
    }

    private void loadDataClipboard() {
        rclClipboard.setVisibility(View.GONE);
        txtNodataClipboard.setVisibility(View.GONE);
        spinKitViewClipboard.setVisibility(VISIBLE);
        clipboardRepository.getListClipboardThread().subscribe(new SingleObserver<ArrayList<String>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull ArrayList<String> strings) {
                spinKitViewClipboard.setVisibility(View.GONE);
                if (strings.size() == 0) {
                    txtNodataClipboard.setVisibility(VISIBLE);
                } else {
                    rclClipboard.setVisibility(VISIBLE);
                    if (clipBoardAdapter != null) clipBoardAdapter.changeList(strings);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });
    }

    public void setColorTextClipBoard(int color) {
        if (clipBoardAdapter != null) {
            clipBoardAdapter.changeColor(color);
        }
    }

    public void setColorForCopyPasteSelectionView(int color) {
        if (editSelectrionView != null) {
            editSelectrionView.setColorFilter(color);
        }
    }

    public void showInputView(boolean isShow) {
         if (mInputView != null && ((InputView) mInputView).getMainKeyboardView() != null) {
            ((InputView) mInputView).getMainKeyboardView().setVisibility(isShow ? VISIBLE : View.INVISIBLE);
        }
    }

    private void showClipboard(int statusClipboard, int statusLayoutMenu) {
        LocaleUtils.INSTANCE.applyLocale(this);
        if (mInputView != null && mInputView instanceof InputView) {
            ((InputView) mInputView).changeShowLayoutMenu(statusLayoutMenu);
        }
        layoutClipboard.setVisibility(statusClipboard);
        if (txtTitleClipboard != null) {
            txtTitleClipboard.setText(R.string.clipboard);
        }
        if (txtNodataClipboard != null) {
            txtNodataClipboard.setText(R.string.there_is_no_data_in_the_clipboard);
        }
        if (statusClipboard == VISIBLE) {
            int color = App.getInstance().colorIconDefault;
            imgBackClipboard.setColorFilter(color);
            txtNodataClipboard.setTextColor(color);
            txtTitleClipboard.setTextColor(color);
        }
        if (editSelectrionView != null) {
            Timber.e("hachung editSelectrionView GONE:");
            editSelectrionView.setVisibility(GONE);
        }
        if (layoutSettingView != null) layoutSettingView.setVisibility(GONE);
        showSettingInternal = false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getKey()) {
            case Constant.EVENT_EMOJI:
                closeTranslate();
                handlerDelay.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onEmojiClick();
                        setColorFilterPlateView(App.getInstance().colorIconDefault);
                    }
                }, 10);
                break;
            case Constant.EVENT_SHOW_CLIPBOARD_VIEW_KEYBOARD:
                showClipboard(VISIBLE, INVISIBLE);
                showInputView(false);
                if (mSuggestionStripView != null) mSuggestionStripView.hideUIMenu();
                loadDataClipboard();
                break;
            case Constant.EVENT_ADD_FONT:
                closeTranslate();
                showAllFont();
                break;
            case Constant.KEY_MAX_LENGTH_TRANSLATE:
                Timber.d("ducNQ : onMessageEventaaa: ");
                if (mInputView != null && mInputView instanceof InputView) {
                    ((InputView) mInputView).setTextNoti();
                    ((InputView) mInputView).showCustomToast();
                }
                break;
            case Constant.KEY_CHECK_SUPPORT_FONT:
                // Timber.d("duc changeFontwww " + System.currentTimeMillis());
                if (mInputView != null && mInputView instanceof InputView) {
                    ((InputView) mInputView).showCustomToast();
                }
                if (allFontOnKeyboardAdapter != null)
                    allFontOnKeyboardAdapter.notifyDataSetChanged();
                if (mSuggestionStripView != null) mSuggestionStripView.changeFont();
                break;
            case Constant.EVENT_CANCEL_SEARCH_GIF:
                typeOldViewDisplay = Constant.KEYBOARD_VIEW_TYPE_KEY;
                cancelSearchGif();
                break;
            case Constant.EVENT_LOAD_LANGUAGE_DB:
                if (mRichImm != null) {
                    mRichImm.refreshSubtypeCaches();
                }
                UpdateAppManager.checkUpdateVersion();
                break;
            case Constant.EVENT_CHANGE_LIST_FONT:
                if (mSuggestionStripView != null) mSuggestionStripView.changeListFont();
                break;
            // case Constant.EVENT_STICKER:
            case Constant.EVENT_SHOW_MENU:
            case Constant.EVENT_TRANSLATE:
                if (mSuggestionStripView != null) {
                    mSuggestionStripView.setVisibility(VISIBLE);
                    mSuggestionStripView.showUIMenu();
                    if (mInputView != null && mInputView instanceof InputView) {
                        ((InputView) mInputView).goneLayoutSearch();
                    }
                }
                if (mInputView != null && mInputView instanceof InputView) {
                    ((InputView) mInputView).setFalseGif();
                }
                break;

//            case Constant.KEY_CHANGE_DATA_STICKER_ONKEYBOARD:
//                Bundle bundle = event.getBundle();
//                if(bundle!=null) {
//                    listStickerOnKeyboard = bundle.getParcelableArrayList(Constant.DATA_STICKER_ONKEYBOARD);
//                    if (itemTabStickerAdapter != null && itemStickerOnKeyboardAdapter != null) {
//                        itemTabStickerAdapter.changeList(listStickerOnKeyboard);
//                        itemStickerOnKeyboardAdapter.changeList(listStickerOnKeyboard.get(0).getListSticker());
//                    }
//                }
//                break;
        }
    }

    public void sendTextToEditText(String text) {
        Event event1 = Event.createSoftwareTextEvent(text, Constants.CODE_OUTPUT_TEXT);
        final Event event = event1;
        final InputTransaction completeInputTransaction =
                mInputLogic.onTextInput(mSettings.getCurrent(), event,
                        mKeyboardSwitcher.getKeyboardShiftMode(), mHandler);
        updateStateAfterInputTransaction(completeInputTransaction);
        mKeyboardSwitcher.onEvent(event, getCurrentAutoCapsState(), getCurrentRecapitalizeState());
    }

    // TODO: Move this method out of {@link LatinIME}.
    private void showOptionDialog(final AlertDialog dialog) {
        final IBinder windowToken = mKeyboardSwitcher.getMainKeyboardView().getWindowToken();
        if (windowToken == null) {
            return;
        }

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = windowToken;
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        mOptionsDialog = dialog;
        dialog.show();
    }

    @UsedForTesting
    SuggestedWords getSuggestedWordsForTest() {
        // You may not use this method for anything else than debug
        return DebugFlags.DEBUG_ENABLED ? mInputLogic.mSuggestedWords : null;
    }

    // DO NOT USE THIS for any other purpose than testing. This is information private to LatinIME.
    @UsedForTesting
    void waitForLoadingDictionaries(final long timeout, final TimeUnit unit)
            throws InterruptedException {
        mDictionaryFacilitator.waitForLoadingDictionariesForTesting(timeout, unit);
    }

    // DO NOT USE THIS for any other purpose than testing. This can break the keyboard badly.
    @UsedForTesting
    void replaceDictionariesForTest(final Locale locale) {
        final SettingsValues settingsValues = mSettings.getCurrent();
        mDictionaryFacilitator.resetDictionaries(this, locale,
                settingsValues.mUseContactsDict, settingsValues.mUsePersonalizedDicts,
                false /* forceReloadMainDictionary */,
                settingsValues.mAccount, "", /* dictionaryNamePrefix */
                this /* DictionaryInitializationListener */);
    }

    // DO NOT USE THIS for any other purpose than testing.
    @UsedForTesting
    void clearPersonalizedDictionariesForTest() {
        mDictionaryFacilitator.clearUserHistoryDictionary(this);
    }

    @UsedForTesting
    List<InputMethodSubtype> getEnabledSubtypesForTest() {
        return (mRichImm != null) ? mRichImm.getMyEnabledInputMethodSubtypeList(
                true /* allowsImplicitlySelectedSubtypes */) : new ArrayList<InputMethodSubtype>();
    }

    public void dumpDictionaryForDebug(final String dictName) {
        if (!mDictionaryFacilitator.isActive()) {
            resetDictionaryFacilitatorIfNecessary();
        }
        mDictionaryFacilitator.dumpDictionaryForDebug(dictName);
    }

    public void debugDumpStateAndCrashWithException(final String context) {
        final SettingsValues settingsValues = mSettings.getCurrent();
        final StringBuilder s = new StringBuilder(settingsValues.toString());
        s.append("\nAttributes : ").append(settingsValues.mInputAttributes)
                .append("\nContext : ").append(context);
        throw new RuntimeException(s.toString());
    }

    @Override
    protected void dump(final FileDescriptor fd, final PrintWriter fout, final String[] args) {
        super.dump(fd, fout, args);

        final Printer p = new PrintWriterPrinter(fout);
        p.println("LatinIME state :");
        p.println("  VersionCode = " + ApplicationUtils.getVersionCode(this));
        p.println("  VersionName = " + ApplicationUtils.getVersionName(this));
        final Keyboard keyboard = mKeyboardSwitcher.getKeyboard();
        final int keyboardMode = keyboard != null ? keyboard.mId.mMode : -1;
        p.println("  Keyboard mode = " + keyboardMode);
        final SettingsValues settingsValues = mSettings.getCurrent();
        p.println(settingsValues.dump());
        p.println(mDictionaryFacilitator.dump(this /* context */));
        // TODO: Dump all settings values
    }

    public boolean shouldSwitchToOtherInputMethods() {
        // TODO: Revisit here to reorganize the settings. Probably we can/should use different
        // strategy once the implementation of
        // {@link InputMethodManager#shouldOfferSwitchingToNextInputMethod} is defined well.
        final boolean fallbackValue = mSettings.getCurrent().mIncludesOtherImesInLanguageSwitchList;
        final IBinder token = getWindow().getWindow().getAttributes().token;
        if (token == null) {
            return fallbackValue;
        }
        return mRichImm.shouldOfferSwitchingToNextInputMethod(token, fallbackValue);
    }

    public boolean shouldShowLanguageSwitchKey() {
//        // TODO: Revisit here to reorganize the settings. Probably we can/should use different
//        // strategy once the implementation of
//        // {@link InputMethodManager#shouldOfferSwitchingToNextInputMethod} is defined well.
//        final boolean fallbackValue = mSettings.getCurrent().isLanguageSwitchKeyEnabled();
//        final IBinder token = getWindow().getWindow().getAttributes().token;
//        if (token == null) {
//            return fallbackValue;
//        }
//        return mRichImm.shouldOfferSwitchingToNextInputMethod(token, fallbackValue);
//        if (!(!Settings.ENABLE_SHOW_LANGUAGE_SWITCH_KEY_SETTINGS
//                ? Settings.readShowsLanguageSwitchKey(mPrefs) : true)) {
//            return false;
//        }
        final RichInputMethodManager imm = RichInputMethodManager.getInstance();
//        if (mIncludesOtherImesInLanguageSwitchList) {
//            return imm.hasMultipleEnabledIMEsOrSubtypes(false /* include aux subtypes */);
//        }
        Log.d("duongcv", "shouldShowLanguageSwitchKey: " + imm.hasMultipleEnabledSubtypesInThisIme(false));
        return imm.hasMultipleEnabledSubtypesInThisIme(false /* include aux subtypes */);
    }

    private void setNavigationBarVisibility(final boolean visible) {
        if (BuildCompatUtils.EFFECTIVE_SDK_INT > Build.VERSION_CODES.M) {
            // For N and later, IMEs can specify Color.TRANSPARENT to make the navigation bar
            // transparent.  For other colors the system uses the default color.
            getWindow().getWindow().setNavigationBarColor(
                    visible ? Color.BLACK : Color.TRANSPARENT);
        }
    }

    private boolean isCheckShowSuggestion() {
        if (layoutClipboard != null && layoutClipboard.getVisibility() == VISIBLE
                || editSelectrionView != null && editSelectrionView.getVisibility() == VISIBLE || layoutAllFont != null && layoutAllFont.getVisibility() == VISIBLE
        ) {
            return false;
        }
        return true;
    }

    private CharSequence getTextWithUnderline(final CharSequence text) {
        return mIsAutoCorrectionIndicatorOn
                ? SuggestionSpanUtils.getTextWithAutoCorrectionIndicatorUnderline(this, text.toString(), null)
                : text;
    }

    private CharSequence charResultChangeFont(CharSequence textInput) {
        String rawTextKQ = (String) textInput;
        if (!rawTextKQ.equals(" ")) {
            String key_font = mPrefs.getString(Constant.USING_FONT, Constant.FONT_NORMAL);
            CharSequence[] sp = font.getFont(key_font);
            if (!isTypeInputPassword()) {
                rawTextKQ = CommonUtil.replaceTextFontOUTPUT(Constant.LIST_LANGUAGE_KEYBOARD_OTHER_QWERTY.contains(Settings.getLanguageKeyBoardCurrent()), sp, rawTextKQ, rawTextKQ, font, key_font);
            }
        }

        return rawTextKQ;
    }

    public boolean isTypeInputPassword() {
        return isTypeInputPassword;
    }

    public void onCodeVietnamese(int primaryCode, int x, int y, Event event) {
        //Timber.e("mSettings.isWordSeparator onCodeVietnamese" + primaryCode);
        final long when = SystemClock.uptimeMillis();
        if (primaryCode != Constants.CODE_DELETE || when > mLastKeyTime + QUICK_PRESS) {
            mInputLogic.setmDeleteCount(0);
        }
        mLastKeyTime = when;
        final int spaceState = mSpaceState;
        if (!mWordComposer.isComposingWord()) mIsAutoCorrectionIndicatorOn = false;

        // TODO: Consolidate the double space timer, mLastKeyTime, and the space state.
        if (primaryCode != Keyboard.CODE_SPACE) {
            mHandler.cancelDoubleSpacesTimer();
        }

        boolean didAutoCorrect = false;
        switch (primaryCode) {
            case Constants.CODE_DELETE:
                final InputTransaction inputTransaction = new InputTransaction(mSettings.getCurrent(),
                        mWordComposer.processEvent(event), SystemClock.uptimeMillis(), mSpaceState,
                        mInputLogic.getActualCapsMode(mSettings.getCurrent(), mKeyboardSwitcher.getKeyboardShiftMode()));
                final Event tmpEvent = Event.createSoftwareKeypressEvent(Constants.CODE_ENTER,
                        event.mKeyCode, event.mX, event.mY, event.isKeyRepeat());
                mInputLogic.handleBackspaceEvent(tmpEvent, inputTransaction, mKeyboardSwitcher.getCurrentKeyboardScriptId());
                inputTransaction.setDidAffectContents();
                mSpaceState = SPACE_STATE_NONE;
                if (!isSearchGif() && editSelectrionView.getVisibility() != VISIBLE)
                    showMenuHeader();
                //next
//                switchToNextSubtype();
                break;

            case Constants.CODE_SHIFT:
            case Constants.CODE_SWITCH_ALPHA_SYMBOL:
                // Shift and symbol key is handled in onPressKey() and onReleaseKey().
                break;

            case Constants.CODE_SYMBOL_SHIFT:
                // Note: Calling back to the keyboard on the symbol Shift key is handled in
                // {@link #onPressKey(int,int,boolean)} and {@link #onReleaseKey(int,boolean)}.
                break;

            case Constants.CODE_CAPSLOCK:
                // Note: Changing keyboard to shift lock state is handled in
                // {@link KeyboardSwitcher#onCodeInput(int)}.
                break;

            case Constants.CODE_SETTINGS:
                Timber.d("ducNQCODE_SETTINGS ");
                displaySettingsActivity();
                break;

            case Constants.CODE_SHORTCUT:
                mRichImm.switchToShortcutIme(this);
                break;

            case Constants.CODE_SHIFT_ENTER:
                final InputTransaction inputTransaction1 = new InputTransaction(mSettings.getCurrent(),
                        mWordComposer.processEvent(event), SystemClock.uptimeMillis(), mSpaceState,
                        mInputLogic.getActualCapsMode(mSettings.getCurrent(), mKeyboardSwitcher.getKeyboardShiftMode()));
                final Event tmpEvent1 = Event.createSoftwareKeypressEvent(Constants.CODE_ENTER,
                        event.mKeyCode, event.mX, event.mY, event.isKeyRepeat());
                mInputLogic.handleNonSpecialCharacterEvent(tmpEvent1, inputTransaction1, mHandler, false, true);
                // Shift + Enter is treated as a functional key but it results in adding a new
                // line, so that does affect the contents of the editor.
                inputTransaction1.setDidAffectContents();
                break;

            case Constants.CODE_ACTION_NEXT:
                mInputLogic.performEditorAction(EditorInfo.IME_ACTION_NEXT);
                break;

            case Constants.CODE_ACTION_PREVIOUS:
                mInputLogic.performEditorAction(EditorInfo.IME_ACTION_PREVIOUS);
                break;

            case Constants.CODE_LANGUAGE_SWITCH:
                switchToNextSubtype();
                break;

            case Constants.CODE_ALPHA_FROM_EMOJI:
                // Note: Switching back from Emoji keyboard to the main keyboard is being
                // handled in {@link KeyboardState#onCodeInput(int,int)}.
                break;
            case Constants.CODE_ALPHA_FROM_EMOJI_SEARCH:
                break;
            case Constants.CODE_ALPHA_FROM_TRANSLATE:
                break;
            case Constants.CODE_EMOJI:
                if (layoutSettingView != null) layoutSettingView.setVisibility(GONE);
                KeyboardSwitcher.getInstance().updateStateEmoji();
                typeOldViewDisplay = Constant.KEYBOARD_VIEW_TYPE_EMOJI;
                mKeyboardSwitcher.updateBgForEmojiPalettes();

                if (mKeyboardSwitcher.getVisibleKeyboardView() instanceof EmojiPalettesView) {
                    ((EmojiPalettesView) mKeyboardSwitcher.getVisibleKeyboardView()).onEmojiGifShow(Constant.TYPE_EMOJI);
                }
                break;

            case Constants.CODE_ENTER:
                Timber.d("ducNQ : onCodeVietnamese: ");
                final EditorInfo editorInfo = getCurrentInputEditorInfo();
                final int imeOptionsActionId =
                        InputTypeUtils.getImeOptionsActionIdFromEditorInfo(editorInfo);
                if (InputTypeUtils.IME_ACTION_CUSTOM_LABEL == imeOptionsActionId) {
                    mInputLogic.performEditorAction(editorInfo.actionId);
                } else if (EditorInfo.IME_ACTION_NONE != imeOptionsActionId) {
                    mInputLogic.performEditorAction(imeOptionsActionId);
                } else {
                    final InputTransaction inputTransaction2 = new InputTransaction(mSettings.getCurrent(),
                            mWordComposer.processEvent(event), SystemClock.uptimeMillis(), mSpaceState,
                            mInputLogic.getActualCapsMode(mSettings.getCurrent(), mKeyboardSwitcher.getKeyboardShiftMode()));
                    mInputLogic.handleNonSpecialCharacterEvent(event, inputTransaction2, mHandler, false, true);
                }
                break;

            case Constants.CODE_TAB:

                break;

            default:
                mSpaceState = SPACE_STATE_NONE;
                //translateOtherLanguage(event,false);
                if (mSettings.isWordSeparator(primaryCode)) {
                    Log.d("duongcv", "onCodeVietnamese: fifi");
                    setSpaceSugeestion(false);
                    didAutoCorrect = handleSeparator(primaryCode, x, y, spaceState);
                    //  Timber.e("mSettings.isWordSeparator(primaryCode)" + primaryCode);
                } else {
                    //Timber.e("mSettings.isWordSeparator" + primaryCode);
                    setSpaceSugeestion(true);
                    final Keyboard keyboard = mKeyboardSwitcher.getKeyboard();
                    if (keyboard != null && keyboard.hasProximityCharsCorrection(primaryCode)) {
                        Log.e("duongcv", "onCodeVietnamese: ");
                        Timber.e("hachung onCodeVietnamese:");
                        handleCharacter(primaryCode, x, y, spaceState);
                    } else {
                        Log.e("duongcv", "onCodeVietnamese: -1");
                        Timber.e("hachung onCodeVietnamese: -1");
                        handleCharacter(primaryCode, -1, -1, spaceState);
                    }
                }
                mHandler.postResumeSuggestions(true /* shouldIncludeResumedWordInSuggestions */,
                        false /* shouldDelay */);
                //mExpectingUpdateSelection = true;
                break;
        }
        mKeyboardSwitcher.onCodeInput(primaryCode, getCurrentAutoCapsState(), getCurrentRecapitalizeState());
        // Reset after any single keystroke, except shift and symbol-shift
        if (!didAutoCorrect && primaryCode != Constants.CODE_SHIFT
                && primaryCode != Constants.CODE_SWITCH_ALPHA_SYMBOL)
            mInputLogic.mLastComposedWord.deactivate();
    }

    private void setSpaceSugeestion(boolean sendKeyCodePoint) {
        if (mInputLogic.isSpaceSuggestion) {
            mInputLogic.isSpaceSuggestion = false;
            mInputLogic.setSpaceStateNone();
            if (sendKeyCodePoint) {
                mInputLogic.sendKeyCodePoint(mSettings.getCurrent(), Constants.CODE_SPACE, false);
            }

        }
    }

    public void setColorFilterPlateView(int color) {
        if (mKeyboardSwitcher.getVisibleKeyboardView() instanceof EmojiPalettesView) {
            Timber.e("hachung setColorFilterPlateView :"+color);
            ((EmojiPalettesView) mKeyboardSwitcher.getVisibleKeyboardView()).setColorFilter(color);
        }
    }

    public void displaySettingsActivity() {
        if (!isClick) {
            delayClick();
            openSetting(Constant.KEY_SCREEN_MORE, false);
//            EventBus.getDefault().post(new MessageEvent(90));
        }
    }

    private boolean handleSeparator(final int primaryCode, final int x, final int y,
                                    final int spaceState) {
        if (mIsVietnameseSubType) {
            adjustAccent(true);

        }

        boolean didAutoCorrect = false;
        // Handle separator
        final RichInputConnection ic = mInputLogic.mConnection;
        if (ic != null) {
            ic.beginBatchEdit();
        }
        if (mWordComposer.isCursorFrontOrMiddleOfComposingWord()) {
            // If we are in the middle of a recorrection, we need to commit the recorrection
            // first so that we can insert the separator at the current cursor position.
            mInputLogic.resetEntireInputState(mInputLogic.mConnection.getExpectedSelectionStart(),
                    mInputLogic.mConnection.getExpectedSelectionEnd(), true /* clearSuggestionStrip */);
        }
        if (mWordComposer.isComposingWord()) {
            commitTyped(ic, primaryCode);
        }

        final boolean swapWeakSpace = maybeStripSpaceWhileInBatchEdit(ic, primaryCode, spaceState,
                KeyboardActionListener.SUGGESTION_STRIP_COORDINATE == x);

        if (SPACE_STATE_PHANTOM == spaceState &&
                mSettings.getCurrent().isPhantomSpacePromotingSymbol(primaryCode)) {
            Timber.e("hachung code space:");
            sendKeyCodePoint(Keyboard.CODE_SPACE);
        }
        sendKeyCodePoint(primaryCode);

        if (Keyboard.CODE_SPACE == primaryCode) {

            if (isSuggestionsRequested() && mSettings.getCurrent().mUseDoubleSpacePeriod) {
                if (maybeDoubleSpaceWhileInBatchEdit(ic)) {
                    mSpaceState = SPACE_STATE_DOUBLE;
                } /*else if (!isShowingPunctuationList()) {
                    mSpaceState = SPACE_STATE_WEAK;
				}*/
            }

            mHandler.startDoubleSpacesTimer();
            // if (!isCursorTouchingWord()) {
            // mHandler.cancelUpdateSuggestions();
            // mHandler.postUpdateBigramPredictions();
            // }
        } else {
            if (swapWeakSpace) {
                swapSwapperAndSpaceWhileInBatchEdit(ic);
                mSpaceState = SPACE_STATE_SWAP_PUNCTUATION;
            } else if (SPACE_STATE_PHANTOM == spaceState) {
                // If we are in phantom space state, and the user presses a separator, we want to
                // stay in phantom space state so that the next keypress has a chance to add the
                // space. For example, if I type "Good dat", pick "day" from the suggestion strip
                // then insert a comma and go on to typing the next word, I want the space to be
                // inserted automatically before the next word, the same way it is when I don't
                // input the comma.
                mSpaceState = SPACE_STATE_PHANTOM;
            }

            // Set punctuation right away. onUpdateSelection will fire but tests whether it is
            // already displayed or not, so it's okay.
            // setPunctuationSuggestions();
        }

        // Utils.Stats.onSeparator((char) primaryCode, x, y);

        if (ic != null) {
            ic.endBatchEdit();
        }

        return didAutoCorrect;
    }

    private void adjustAccent(boolean fixUWOW) {
        final boolean isComposingWord = mWordComposer.isComposingWord();
        final RichInputConnection ic = mInputLogic.mConnection;
        if (ic == null) return;

        mTempCurrentWord.setLength(0);
        StringBuilder currentWord = mTempCurrentWord;
        Timber.e("hachung currentWord:" + currentWord + "/mTempCurrentWord: " + mTempCurrentWord + "/fixUWOW: " + fixUWOW);
        int beforeLength = -1, afterLength = -1;
        if (!isComposingWord) {
            CharSequence beforeText = ic.getTextBeforeCursor(10, 0);
            CharSequence afterText = ic.getTextAfterCursor(10, 0);
            if (beforeText != null) {
                for (int i = beforeText.length() - 1; i >= 0; i--) {
                    if (mSettings.isWordSeparator(beforeText.charAt(i))) {
                        beforeLength = beforeText.length() - i - 1;
                        currentWord.append(beforeText.subSequence(i + 1, beforeText.length()));
                        break;
                    }
                }
                if (beforeLength == -1) {
                    beforeLength = beforeText.length();
                    currentWord.append(beforeText);
                }
            }

            if (afterText != null) {
                for (int i = 0; i < afterText.length(); i++) {
                    if (mSettings.isWordSeparator(afterText.charAt(i))) {
                        afterLength = i;
                        currentWord.append(afterText.subSequence(0, i));
                        break;
                    }
                }
                if (afterLength == -1) {
                    afterLength = afterText.length();
                    currentWord.append(afterText);
                }
            }
        } else {
            currentWord.append(mWordComposer.getTypedWord());
        }
        if (currentWord.length() > 0 && !VietnameseSpellChecker.adjustAccent(currentWord, VietnameseSpellChecker.ACCENT_AUTO, fixUWOW)) {
            return;
        }

        if (isComposingWord) {
            ic.beginBatchEdit();
            mWordComposer.setComposingWord(currentWord, mKeyboardSwitcher.getKeyboard());
            CharSequence textOutput = charResultChangeFont(getTextWithUnderline(mWordComposer.getTypedWord()));
            Timber.d("duongcv : commit : " + textOutput);
            ic.setComposingText(textOutput, 1);
            //ic.setComposingText(getTextWithUnderline(mWordComposer.getTypedWord()), 1);
            mHandler.postUpdateSuggestions();
            ic.endBatchEdit();
        } else {
            ic.deleteSurroundingText(beforeLength, afterLength);
            Timber.e("hachung currentWord:" + currentWord);
            ic.commitText(currentWord, 1);
        }
    }

    public void commitTyped(final RichInputConnection ic, final int separatorCode) {
        mInputLogic.commitTyped(mSettings.getCurrent(), String.valueOf((char) separatorCode));
    }

    private boolean maybeStripSpaceWhileInBatchEdit(final RichInputConnection ic,
                                                    final int code,
                                                    final int spaceState, final boolean isFromSuggestionStrip) {
        if (Keyboard.CODE_ENTER == code && SPACE_STATE_SWAP_PUNCTUATION == spaceState) {
            removeTrailingSpaceWhileInBatchEdit(ic);
            return false;
        } else if ((SPACE_STATE_WEAK == spaceState
                || SPACE_STATE_SWAP_PUNCTUATION == spaceState)
                && isFromSuggestionStrip) {
            if (mSettings.getCurrent().isWeakSpaceSwapper(code)) {
                return true;
            } else {
                if (mSettings.getCurrent().isWeakSpaceStripper(code)) {
                    removeTrailingSpaceWhileInBatchEdit(ic);
                }
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean ismIsVietnameseSubType() {
        return mIsVietnameseSubType;
    }

    // using language Vietnamese
    private void sendKeyCodePoint(int code) {
        // TODO: Remove this special handling of digit letters.
        // For backward compatibility. See {@link InputMethodService#sendKeyChar(char)}.
        if (code >= '0' && code <= '9') {
            super.sendKeyChar((char) code);
            return;
        }

        // 16 is android.os.Build.VERSION_CODES.JELLY_BEAN but we can't write it because
        // we want to be able to compile against the Ice Cream Sandwich SDK.

        //text vi hoangld
        RichInputConnection ic = mInputLogic.mConnection;
        if (ic != null) {
            if (Keyboard.CODE_ENTER == code) {
                // Backward compatibility mode. Before Jelly bean, the keyboard would simulate
                // a hardware keyboard event on pressing enter or delete. This is bad for many
                // reasons (there are race conditions with commits) but some applications are
                // relying on this behavior so we continue to support it for older apps.
                sendUpDownEnterOrBackspace(KeyEvent.KEYCODE_ENTER, ic);
            } else {
                String rawTextKQ = StringUtils.newSingleCodePointString(code);
                if (!rawTextKQ.equals(" ") && !isTypeInputPassword() && !(mKeyboardSwitcher.getVisibleKeyboardView() instanceof EmojiPalettesView)) {
                    //Timber.e("rawTextKQ if: " + rawTextKQ);
                    String key_font = mPrefs.getString(Constant.USING_FONT, Constant.FONT_NORMAL);
                    CharSequence[] sp = font.getFont(key_font);
                    rawTextKQ = CommonUtil.replaceTextFontOUTPUT(Constant.LIST_LANGUAGE_KEYBOARD_OTHER_QWERTY.contains(Settings.getLanguageKeyBoardCurrent()), sp, rawTextKQ, rawTextKQ, font, key_font);
                }
                Timber.e("hachung commit " + rawTextKQ);
                ic.commitText(rawTextKQ, 1);
            }
        }
    }

    public boolean isSuggestionsRequested() {
        return true;
        // return mInputAttributes.mIsSettingsSuggestionStripOn
        // 		&& (mCorrectionMode > 0 || isShowingSuggestionsStrip());
    }

    private boolean maybeDoubleSpaceWhileInBatchEdit(final RichInputConnection ic) {
        if (ic == null) return false;
        final CharSequence lastThree = ic.getTextBeforeCursor(3, 0);
        if (lastThree != null && lastThree.length() == 3
                && canBeFollowedByPeriod(lastThree.charAt(0))
                && lastThree.charAt(1) == Keyboard.CODE_SPACE
                && lastThree.charAt(2) == Keyboard.CODE_SPACE
                && mHandler.isAcceptingDoubleSpaces()) {
            mHandler.cancelDoubleSpacesTimer();
            ic.deleteSurroundingText(2, 0);
            ic.commitText(". ", 1);
            mKeyboardSwitcher.updateShiftState();
            return true;
        }
        return false;
    }

    private void swapSwapperAndSpaceWhileInBatchEdit(final RichInputConnection ic) {
        if (null == ic) return;
        CharSequence lastTwo = ic.getTextBeforeCursor(2, 0);
        // It is guaranteed lastTwo.charAt(1) is a swapper - else this method is not called.
        if (lastTwo != null && lastTwo.length() == 2
                && lastTwo.charAt(0) == Keyboard.CODE_SPACE) {
            ic.deleteSurroundingText(2, 0);
            mKeyboardSwitcher.updateShiftState();
        }
    }

    private void handleCharacter(final int primaryCode, final int x,
                                 final int y, final int spaceState) {
        //Timber.e("handleCharacter: ");
        boolean bb = handleVietnameseCharacter(primaryCode, 0, 0, spaceState);
        Timber.e("hachung bb:" + bb);
        if (bb) { // is vietnam
            adjustAccent(false);
            return;
        }
        final RichInputConnection ic = mInputLogic.mConnection;
        if (null != ic) ic.beginBatchEdit();
        // TODO: if ic is null, does it make any sense to call this?
        handleCharacterWhileInBatchEdit(primaryCode, x, y, spaceState, ic);
        if (null != ic) ic.endBatchEdit();

        if (mIsVietnameseSubType) {
            adjustAccent(false);
        }
    }

    private boolean handleVietnameseCharacter(int originalTypedChar, int x, int y,
                                              int spaceState) {
        final int typedChar = Character.toLowerCase(originalTypedChar);
        if (!shouldHandleVietnameseCharacter(typedChar)) {
            return false;
        }
        final boolean isComposingWord = mWordComposer.isComposingWord();

        final RichInputConnection ic = mInputLogic.mConnection;
        if (ic == null) return false;

        mTempCurrentWord.setLength(0);
        StringBuilder currentWord = mTempCurrentWord;
        if (!isComposingWord) {
            currentWord.append(ic.getTextBeforeCursor(10, 0));
            for (int i = currentWord.length() - 1; i >= 0; i--) {
                if (mSettings.isWordSeparator(currentWord.charAt(i))) {
                    currentWord.delete(0, i + 1);
                    break;
                }
            }
        } else {
            currentWord.append(mWordComposer.getTypedWord());
        }
        if (currentWord.length() == 0) {
            if (typedChar == 'w') {
                mLastWConverted = originalTypedChar;
//                if (isTelexVietnamese && !isTelexVietnameseSimple) {
//                    int value = (originalTypedChar == 'w' ? 'ư' : 'Ư');
//                    handleCharacterWhileInBatchEdit((char) value, x, y, spaceState, ic);
//                } else {
                handleCharacterWhileInBatchEdit((char) originalTypedChar, x, y, spaceState, ic);
                // }
                return true;
            }
            return false;
        }
        if (typedChar != 'w' && mLastWConverted != 0) {
            mLastWConverted = 0;
        }

        if (!VietnameseSpellChecker.isVietnameseWord(currentWord)) {
            return false;
        }

        int replacedCharIndex = -1;
        String replacedChar = "";
        int typedCharPos = -1;
        boolean shouldKeepCurrentChar = false;
        boolean isBefore = false;
        final int[] VN_DOUBLE_CHARS = isTelexVietnamese ? VN_DOUBLE_CHARS_TELEX : VN_DOUBLE_CHARS_VNI;
        for (int i = 0; i < VN_DOUBLE_CHARS.length; i++) {
            if (typedChar == VN_DOUBLE_CHARS[i]) {
                typedCharPos = i;
                break;
            }
        }
        if (typedCharPos != -1) {
            StringBuilder ch = new StringBuilder();
            int wordLength = currentWord.length();
            boolean isUpperFirst = false;
            boolean isUpperSecond = false;
            for (int i = wordLength - 1; i >= 0; i--) {
                int charAt = currentWord.charAt(i);
                final int[] VN_CHARS_TO_FIND = isTelexVietnamese ? VN_CHARS_TO_FIND_TELEX[typedCharPos] : VN_CHARS_TO_FIND_VNI[typedCharPos];
                for (int j = 0; j < VN_CHARS_TO_FIND.length; j++) {
                    if (charAt == VN_CHARS_TO_FIND[j]) {
                        isUpperSecond = Character.isUpperCase(charAt);
                        ch.append((char) charAt);
                        replacedCharIndex = i;
                        replacedChar = String.valueOf((char) (isTelexVietnamese ?
                                VN_CHARS_TO_REPLACE_TELEX[typedCharPos][j] : VN_CHARS_TO_REPLACE_VNI[typedCharPos][j]));

                        if (i > 0) {
                            isBefore = true;
                            int charPre = currentWord.charAt(i - 1);
                            for (char advancedVowel : VN_CHAR_ADVANCED_VOWEL) {
                                if (advancedVowel == charPre) {
                                    ch.insert(0, (char) charPre);
                                    isUpperFirst = Character.isUpperCase(charPre);
                                    break;
                                }
                            }
							/*if (i > 1) {
								charPre = currentWord.charAt(i - 2);
								for (char advancedVowel : VN_CHAR_ADVANCED_VOWEL) {
									if (advancedVowel == charPre) {
										ch.insert(0, String.valueOf((char) charPre));
										break;
									}
								}
							}*/
                        }
                        if (ch.length() == 1) {
                            if (i < wordLength - 1) {
                                isBefore = false;
                                int charNext = currentWord.charAt(i + 1);
                                for (char aVN_CHAR_ADVANCED_VOWEL : VN_CHAR_ADVANCED_VOWEL) {
                                    if (aVN_CHAR_ADVANCED_VOWEL == charNext) {
                                        ch.append((char) charNext);
                                        isUpperFirst = isUpperSecond;
                                        isUpperSecond = Character.isUpperCase(charNext);
                                        break;
                                    }
                                }
								/*if (i < wordLength - 2) {
									charNext = currentWord.charAt(i + 2);
									for (char aVN_CHAR_ADVANCED_VOWEL : VN_CHAR_ADVANCED_VOWEL) {
										if (aVN_CHAR_ADVANCED_VOWEL == charNext) {
											ch.append(String.valueOf((char) charNext));
											break;
										}
									}
								}*/
                            }
                        }
                        if (isTelexVietnamese && !isTelexVietnameseSimple) {
                            if (typedChar == 'w') {
                                if (mLastWConverted != 0 && currentWord.toString().toLowerCase().equals("ư")) {
                                    replacedChar = String.valueOf((char) mLastWConverted);
                                    mLastWConverted = 0;
                                    break;
                                }
                            }
                        }
                        final int[] VN_FALLBACKS = isTelexVietnamese ? VN_FALLBACKS_TELEX : VN_FALLBACKS_VNI;
                        if (j >= VN_FALLBACKS[typedCharPos]) {
                            shouldKeepCurrentChar = true;
                        }
                        break;
                    }
                }
                if (replacedCharIndex != -1) break; // found, stop now
            }
            if (ch.length() > 1) {
                final String[][] VN_DOUBLE_VOWELS_FIND = isTelexVietnamese ?
                        VN_DOUBLE_VOWELS_FIND_TELEX : VN_DOUBLE_VOWELS_FIND_VNI;
                for (int i = 0; i < VN_DOUBLE_VOWELS_FIND[typedCharPos].length; i++) {
                    String doubleChar = VN_DOUBLE_VOWELS_FIND[typedCharPos][i];
                    if (ch.toString().toUpperCase().equals(doubleChar.toUpperCase())) {
                        replacedChar = isTelexVietnamese ?
                                VN_DOUBLE_VOWELS_REPLACE_TELEX[typedCharPos][i] : VN_DOUBLE_VOWELS_REPLACE_VNI[typedCharPos][i];
                        if (isUpperFirst && isUpperSecond) {
                            replacedChar = String.valueOf(Character.toUpperCase(replacedChar.charAt(0)))
                                    + Character.toUpperCase(replacedChar.charAt(1));
                        } else {
                            if (isUpperFirst) {
                                replacedChar = String.valueOf(Character.toUpperCase(replacedChar.charAt(0)))
                                        + replacedChar.charAt(1);
                            }
                            if (isUpperSecond) {
                                replacedChar = String.valueOf(replacedChar.charAt(0))
                                        + Character.toUpperCase(replacedChar.charAt(1));
                            }
                        }
                        if (isBefore) {
                            replacedCharIndex -= 1;
                        }
                        final int[] VN_DOUBLE_FALLBACKS = isTelexVietnamese ? VN_DOUBLE_FALLBACKS_TELEX : VN_DOUBLE_FALLBACKS_VNI;
                        if (i >= VN_DOUBLE_FALLBACKS[typedCharPos]) {
                            shouldKeepCurrentChar = true;
                        }
                        break;
                    }
                }
            }
        } else {
            // process markers: s f r x j
            final int[] VN_TONE_MAKERS = isTelexVietnamese ? VN_TONE_MARKERS_TELEX : VN_TONE_MARKERS_VNI;
            for (int i = 0; i < VN_TONE_MAKERS.length; i++) {
                if (typedChar == VN_TONE_MAKERS[i]) {
                    typedCharPos = i;
                    break;
                }
            }
            if (typedCharPos != -1) {
                int sequenceLength = 0;
                int lastVowelIndex = -1;
                // finding vowels sequence
                int lastCharIndex = currentWord.length() - 1;
                for (int i = lastCharIndex; i >= 0; i--) {
                    int currentChar = currentWord.charAt(i);
                    int vowelIndex = -1;
                    for (int j = 0; j < VN_VOWELS.length; j++) {
                        if (currentChar == VN_VOWELS[j]) {
                            vowelIndex = j;
                            vowelIndexes[sequenceLength] = j;
                            sequenceLength += 1;
                            if (lastVowelIndex == -1) lastVowelIndex = i;
                            break;
                        }
                    }

                    if (vowelIndex != -1 &&
                            !(('a' <= currentChar && currentChar <= 'z') ||
                                    ('A' <= currentChar && currentChar <= 'Z'))) {
                        break;
                    }
                    if ((vowelIndex == -1 && lastVowelIndex != -1) || sequenceLength >= MAX_VOWELS_SEQUENCE)
                        break;
                }

                if (lastVowelIndex != -1) {
                    if (sequenceLength == 3) {
                        int middleVowel = currentWord.charAt(lastVowelIndex - 1);
                        middleVowel = VN_VOWELS[(vowelIndexes[1] / 6) * 6];
                        if (middleVowel != 'y' && middleVowel != 'Y') {
                            replacedCharIndex = lastVowelIndex - 1;
                        } else {
                            replacedCharIndex = lastVowelIndex;
                        }
                    } else if (sequenceLength == 2) {
                        replacedCharIndex = lastVowelIndex - 1; // first vowel by default

                        if (lastVowelIndex - sequenceLength >= 0) {
                            int consonant = currentWord.charAt(lastVowelIndex - sequenceLength);
                            int firstVowel = currentWord.charAt(lastVowelIndex - sequenceLength + 1);
                            firstVowel = VN_VOWELS[(vowelIndexes[0] / 6) * 6];

                            if ((consonant == 'Q' || consonant == 'q') ||
                                    ((consonant == 'G' || consonant == 'g') && (firstVowel == 'i' || firstVowel == 'I'))) {
                                replacedCharIndex += 1;
                            } else if (lastVowelIndex < lastCharIndex) { // co phu am phia sau
                                replacedCharIndex += 1;
                            }
                        } else if (lastVowelIndex < lastCharIndex) { // co phu am phia sau
                            replacedCharIndex += 1;
                        }
                    } else {
                        replacedCharIndex = lastVowelIndex;
                    }

                    int row = vowelIndexes[lastVowelIndex - replacedCharIndex] / 6;
                    replacedChar = String.valueOf((char) VN_VOWELS[row * 6 + typedCharPos]);
                    Timber.d("ducNQ : handleVietnameseCharacterss: replacedChar: " + replacedChar);
                    Timber.d("ducNQ : handleVietnameseCharacterss: typedCharPos: " + typedCharPos);
                    Timber.d("ducNQ : handleVietnameseCharacterss: row: " + row);
                    Timber.d("ducNQ : handleVietnameseCharacterss: lastVowelIndex: " + lastVowelIndex);
                    Timber.d("ducNQ : handleVietnameseCharacterss: replacedCharIndex: " + replacedCharIndex);
                    if (replacedChar.equals(String.valueOf((char) currentWord.charAt(replacedCharIndex)))) {
                        replacedChar = String.valueOf((char) VN_VOWELS[row * 6]);
                        shouldKeepCurrentChar = true;
                    }
                }
            }
        }

        if (replacedCharIndex != -1) {
            ic.beginBatchEdit();

            if (isComposingWord) {
                mWordComposer.setCharAt(replacedCharIndex, replacedChar);
                CharSequence textOutput = charResultChangeFont(getTextWithUnderline(mWordComposer.getTypedWord()));
                Timber.d("ducNQ : checkSuggest: 5");
                ic.setComposingText(textOutput, 1);
                //ic.setComposingText(getTextWithUnderline(mWordComposer.getTypedWord()), 1);
                mHandler.postUpdateSuggestions();
            } else {
                StringBuilder s = new StringBuilder(currentWord);
                s.replace(replacedCharIndex, replacedCharIndex + replacedChar.length(), replacedChar);
                currentWord = s;
                // currentWord.setCharAt(replacedCharIndex, (char) replacedChar);
                ic.deleteSurroundingText(currentWord.length(), 0);
                //Timber.e("handleVietnameseCharacter ic.commitText");
                ic.commitText(currentWord, 1);
            }
            ic.endBatchEdit();

            return !shouldKeepCurrentChar; // return false to call the original handleCharacter

        } else if (typedChar == 'w') {
            mLastWConverted = originalTypedChar;
//            if (isTelexVietnamese && !isTelexVietnameseSimple) {
//                int value = (originalTypedChar == 'w' ? 'ư' : 'Ư');
//                handleCharacterWhileInBatchEdit((char) value, x, y, spaceState, ic);
//            } else {
            handleCharacterWhileInBatchEdit((char) originalTypedChar, x, y, spaceState, ic);
            //  }
            return true;
        }

        return false;
    }

    private boolean shouldHandleVietnameseCharacter(int primaryCode) {
        return mIsVietnameseSubType && isVietnameseToneMarker(primaryCode);
    }

    private boolean isVietnameseToneMarker(int typedChar) {
        if (isTelexVietnamese) {
            return typedChar == 'a' || // aa -> â
                    typedChar == 'e' || // ee -> ê
                    typedChar == 'o' || // oo -> ô
                    typedChar == 'w' || // uw -> ư, ow -> ơ, aw -> ă
                    typedChar == 'd' || // dd -> đ;
                    typedChar == 'z' || // á -> a
                    typedChar == 's' || // as -> á
                    typedChar == 'f' || // af -> à
                    typedChar == 'r' || // ar -> ả
                    typedChar == 'x' || // ax -> ã
                    typedChar == 'j';   // aj -> ạ
        } else {
            return typedChar == '6' || // a6 -> â, e6 -> ê, o6 -> ô
                    typedChar == '7' || // o7 -> ơ, u7 -> ư
                    typedChar == '8' || // a8 -> ă
                    typedChar == '9' || // d9 -> đ;
                    typedChar == '0' || // á0 -> a
                    typedChar == '1' || // a1 -> á
                    typedChar == '2' || // a2 -> à
                    typedChar == '3' || // a3 -> ả
                    typedChar == '4' || // a4 -> ã
                    typedChar == '5';   // a5 -> ạ
        }
    }

    private void handleCharacterWhileInBatchEdit(final int primaryCode,
                                                 final int x, final int y, final int spaceState, RichInputConnection ic) {
        boolean isComposingWord = mWordComposer.isComposingWord();
        if (SPACE_STATE_PHANTOM == spaceState &&
                !mSettings.isWordSeparator(primaryCode)) {
            if (isComposingWord) {
                // Sanity check
                throw new RuntimeException("Should not be composing here");
            }
            sendKeyCodePoint(Keyboard.CODE_SPACE);
        }
        if (mWordComposer.isCursorFrontOrMiddleOfComposingWord()) {
            // If we are in the middle of a recorrection, we need to commit the recorrection
            // first so that we can insert the character at the current cursor position.
            mInputLogic.resetEntireInputState(mInputLogic.mConnection.getExpectedSelectionStart(),
                    mInputLogic.mConnection.getExpectedSelectionEnd(), true /* clearSuggestionStrip */);
            isComposingWord = false;
        }
        // if (!isComposingWord
        // 		// We only start composing if this is a word code point. Essentially that means it's a
        // 		// a letter or a word connector.
        // 		&& mSettings.getCurrent().isWordCodePoint(primaryCode)
        // 		// We never go into composing state if suggestions are not requested.
        // 		&& mSettings.getCurrent().needsToLookupSuggestions() &&
        // 		// In languages with spaces, we only start composing a word when we are not already
        // 		// touching a word. In languages without spaces, the above conditions are sufficient.
        // 		(!mInputLogic.mConnection.isCursorTouchingWord(mSettings.getCurrent().mSpacingAndPunctuations)
        // 				|| !mSettings.getCurrent().mSpacingAndPunctuations.mCurrentLanguageHasSpaces)) {
        // 	// Reset entirely the composing state anyway, then start composing a new word unless
        // 	// the character is a word connector. The idea here is, word connectors are not
        // 	// separators and they should be treated as normal characters, except in the first
        // 	// position where they should not start composing a word.
        // 	isComposingWord = !mSettings.getCurrent().mSpacingAndPunctuations.isWordConnector(primaryCode);
        // 	// Here we don't need to reset the last composed word. It will be reset
        // 	// when we commit this one, if we ever do; if on the other hand we backspace
        // 	// it entirely and resume suggestions on the previous word, we'd like to still
        // 	// have touch coordinates for it.
        // 	resetComposingState(false /* alsoResetLastComposedWord */);
        // }
        // NOTE: isCursorTouchingWord() is a blocking IPC call, so it often takes several
        // dozen milliseconds. Avoid calling it as much as possible, since we are on the UI
        // thread here.
        if (!isComposingWord && (isAlphabet(primaryCode)
                || mSettings.isWordSeparator(primaryCode))
                && isSuggestionsRequested()
                && !isCursorTouchingWord()) {
            // 	// Reset entirely the composing state anyway, then start composing a new word unless
            // 	// the character is a single quote. The idea here is, single quote is not a
            // 	// separator and it should be treated as a normal character, except in the first
            // 	// position where it should not start composing a word.
            isComposingWord = (Keyboard.CODE_SINGLE_QUOTE != primaryCode);
            // 	// Here we don't need to reset the last composed word. It will be reset
            // 	// when we commit this one, if we ever do; if on the other hand we backspace
            // 	// it entirely and resume suggestions on the previous word, we'd like to still
            // 	// have touch coordinates for it.
            resetComposingState(false /* alsoResetLastComposedWord */);
            clearSuggestions();
        }
        if (isComposingWord) {
            mWordComposer.add(primaryCode, mKeyboardSwitcher.getMainKeyboardView().getKeyX(x), mKeyboardSwitcher.getMainKeyboardView().getKeyY(y));
            if (ic != null) {
                // If it's the first letter, make note of auto-caps state
                if (mWordComposer.size() == 1) {
                    mWordComposer.setmCapitalizedMode(getCurrentAutoCapsState());
                }
                CharSequence textOutput = charResultChangeFont(getTextWithUnderline(mWordComposer.getTypedWord()));
                Log.d("duongcv commit", "handleCharacterWhileInBatchEdit: " + textOutput);
                Timber.d("ducNQ : checkSuggest: 6");

                ic.setComposingText(textOutput, 1);
            }
            mHandler.postUpdateSuggestions();
        } else {
            final boolean swapWeakSpace = mInputLogic.isSwapWeakSpace();

            sendKeyCodePoint(primaryCode);

            if (swapWeakSpace) {
                swapSwapperAndSpaceWhileInBatchEdit(ic);
                mSpaceState = SPACE_STATE_WEAK;
            }
            // Some characters are not word separators, yet they don't start a new
            // composing span. For these, we haven't changed the suggestion strip, and
            // if the "add to dictionary" hint is shown, we should do so now. Examples of
            // such characters include single quote, dollar, and others; the exact list is
            // the list of characters for which we enter handleCharacterWhileInBatchEdit
            // that don't match the test if ((isAlphabet...)) at the top of this method.
//
//            if (null != mSuggestionStripView && mSuggestionStripView.dismissAddToDictionaryHint()) {
//                // mHandler.postUpdateBigramPredictions();
//            }
        }
        // Utils.Stats.onNonSeparator((char) primaryCode, x, y);
    }

    public boolean isCursorTouchingWord() {
        final RichInputConnection ic = mInputLogic.mConnection;
        if (ic == null) return false;
        CharSequence before = ic.getTextBeforeCursor(1, 0);
        CharSequence after = ic.getTextAfterCursor(1, 0);
        if (!TextUtils.isEmpty(before) && !mSettings.isWordSeparator(before.charAt(0))
                && !mSettings.isWordSeparator(before.charAt(0))) {
            return true;
        }
        return !TextUtils.isEmpty(after) && !mSettings.isWordSeparator(after.charAt(0))
                && !mSettings.isWordSeparator(after.charAt(0));
    }

    public void resetComposingState(final boolean alsoResetLastComposedWord) {
        mWordComposer.reset();
        if (alsoResetLastComposedWord) {
            mInputLogic.mLastComposedWord = LastComposedWord.NOT_A_COMPOSED_WORD;
        }
    }

    @Override
    public void resultTranslate(String output) {
        Timber.d("ducNQresultTranslate " + output);
        resetComposingState(true);
        Timber.d("ducNQ : checkSuggest: 7");
        resultTranslateThread(output);
//        mInputLogic.mConnection.setComposingText(output/*output*/, 1);
    }

    private Single<String> outputTranslate(String output) {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return mInputLogic.replaceTextSuggestion(output);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private void resultTranslateThread(String output) {
        outputTranslate(output).subscribe(new SingleObserver<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull String s) {
                mInputLogic.mConnection.setComposingText(s/*output*/, 1);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });
    }

    @Override
    public void closeTranslate() {
        resetComposingState(true);
        mInputLogic.mConnection.finishComposingText();
        //  mInputLogic.mConnection.setLengthText();
        viewTranslate.closeTranslate();
        viewChooseLanguage.hideView();
    }

    @Override
    public void showLanguageInput() {
        if (viewChooseLanguage.getHeight() == 0) {
            viewChooseLanguage.getLayoutParams().height = DisplayUtils.getScreenHeight();
        }
        if (viewChooseLanguage.getWidth() == 0) {
            viewChooseLanguage.getLayoutParams().width = DisplayUtils.getScreenWidth();
        }
        viewChooseLanguage.showInput();
    }

    @Override
    public void showLanguageOutput() {
        if (viewChooseLanguage.getHeight() == 0) {
            viewChooseLanguage.getLayoutParams().height = DisplayUtils.getScreenHeight();
        }
        if (viewChooseLanguage.getWidth() == 0) {
            viewChooseLanguage.getLayoutParams().width = DisplayUtils.getScreenWidth();
        }
        viewChooseLanguage.showOutput();
    }

    @Override
    public void reverserLanguage() {
        viewChooseLanguage.reverseLanguage();
    }

    @Override
    public void onChangeLayoutViewTranslate() {
        KeyboardSwitcher.getInstance().setKeyboard(0, KeyboardSwitcher.getInstance().getKeyboardSwitchState());
    }

    @Override
    public void onChooseLanguageInput(LanguageTranslate languageTranslate) {
        if (viewTranslate != null) {
            viewTranslate.setInputLanguage(languageTranslate);
        }
    }

    @Override
    public void onChooseLanguageOutput(LanguageTranslate languageTranslate) {
        if (viewTranslate != null) {
            viewTranslate.setEnabled();
            viewTranslate.setOutputLanguage(languageTranslate);
        }
    }

    @Override
    public void onCloseChooseLanguage() {
        viewTranslate.setEnabled();
    }

    public static final class UIHandler extends LeakGuardHandlerWrapper<LatinIME> {
        private static final int MSG_UPDATE_SHIFT_STATE = 0;
        private static final int MSG_PENDING_IMS_CALLBACK = 1;
        private static final int MSG_UPDATE_SUGGESTION_STRIP = 2;
        private static final int MSG_SHOW_GESTURE_PREVIEW_AND_SUGGESTION_STRIP = 3;
        private static final int MSG_RESUME_SUGGESTIONS = 4;
        private static final int MSG_REOPEN_DICTIONARIES = 5;
        private static final int MSG_UPDATE_TAIL_BATCH_INPUT_COMPLETED = 6;
        private static final int MSG_RESET_CACHES = 7;
        private static final int MSG_WAIT_FOR_DICTIONARY_LOAD = 8;
        private static final int MSG_DEALLOCATE_MEMORY = 9;
        private static final int MSG_RESUME_SUGGESTIONS_FOR_START_INPUT = 10;
        private static final int MSG_SWITCH_LANGUAGE_AUTOMATICALLY = 11;
        private static final int MSG_SPACE_TYPED = 12;
        private static final int MSG_UPDATE_SUGGESTIONS = 13;
        // Update this when adding new messages
        private static final int MSG_LAST = MSG_SWITCH_LANGUAGE_AUTOMATICALLY;

        private static final int ARG1_NOT_GESTURE_INPUT = 0;
        private static final int ARG1_DISMISS_GESTURE_FLOATING_PREVIEW_TEXT = 1;
        private static final int ARG1_SHOW_GESTURE_FLOATING_PREVIEW_TEXT = 2;
        private static final int ARG2_UNUSED = 0;
        private static final int ARG1_TRUE = 1;
        private static final int ARG1_FALSE = 0;


        private int mDelayInMillisecondsToUpdateSuggestions;
        private int mDelayInMillisecondsToUpdateShiftState;
        // Working variables for the following methods.
        private boolean mIsOrientationChanging;
        private boolean mPendingSuccessiveImsCallback;
        private boolean mHasPendingStartInput;
        private boolean mHasPendingFinishInputView;
        private boolean mHasPendingFinishInput;
        private EditorInfo mAppliedEditorInfo;


        public UIHandler(@Nonnull final LatinIME ownerInstance) {
            super(ownerInstance);
        }

        public void onCreate() {
            final LatinIME latinIme = getOwnerInstance();
            if (latinIme == null) {
                return;
            }
            final Resources res = latinIme.getResources();
            mDelayInMillisecondsToUpdateSuggestions = res.getInteger(
                    R.integer.config_delay_in_milliseconds_to_update_suggestions);
            mDelayInMillisecondsToUpdateShiftState = res.getInteger(
                    R.integer.config_delay_in_milliseconds_to_update_shift_state);
        }

        @Override
        public void handleMessage(final Message msg) {
            final LatinIME latinIme = getOwnerInstance();
            if (latinIme == null) {
                return;
            }
            final KeyboardSwitcher switcher = latinIme.mKeyboardSwitcher;
            switch (msg.what) {
                case MSG_UPDATE_SUGGESTION_STRIP:
                    cancelUpdateSuggestionStrip();
                    latinIme.mInputLogic.performUpdateSuggestionStripSync(latinIme.mSettings.getCurrent(), msg.arg1 /* inputStyle */);
                    break;
                case MSG_UPDATE_SHIFT_STATE:
                    switcher.requestUpdatingShiftState(latinIme.getCurrentAutoCapsState(),
                            latinIme.getCurrentRecapitalizeState());
                    break;
                case MSG_SHOW_GESTURE_PREVIEW_AND_SUGGESTION_STRIP:
                    if (msg.arg1 == ARG1_NOT_GESTURE_INPUT) {
                        final SuggestedWords suggestedWords = (SuggestedWords) msg.obj;
                        latinIme.showSuggestionStrip(suggestedWords);
                    } else {
                        latinIme.showGesturePreviewAndSuggestionStrip((SuggestedWords) msg.obj,
                                msg.arg1 == ARG1_DISMISS_GESTURE_FLOATING_PREVIEW_TEXT);
                    }
                    break;
                case MSG_RESUME_SUGGESTIONS:
                    latinIme.mInputLogic.restartSuggestionsOnWordTouchedByCursor(
                            latinIme.mSettings.getCurrent(), false /* forStartInput */,
                            latinIme.mKeyboardSwitcher.getCurrentKeyboardScriptId());
                    break;
                case MSG_RESUME_SUGGESTIONS_FOR_START_INPUT:
                    latinIme.mInputLogic.restartSuggestionsOnWordTouchedByCursor(
                            latinIme.mSettings.getCurrent(), true /* forStartInput */,
                            latinIme.mKeyboardSwitcher.getCurrentKeyboardScriptId());
                    break;
                case MSG_REOPEN_DICTIONARIES:
                    // We need to re-evaluate the currently composing word in case the script has
                    // changed.
                    postWaitForDictionaryLoad();
                    latinIme.resetDictionaryFacilitatorIfNecessary();
                    break;
                case MSG_UPDATE_TAIL_BATCH_INPUT_COMPLETED:
                    final SuggestedWords suggestedWords = (SuggestedWords) msg.obj;
//                    if(latinIme.isViewTranslateShow()){
//                        for (int i = 0; i < suggestedWords.getWord(0).length(); i++) {
//                            latinIme.appendTextTranslate(String.valueOf(suggestedWords.getWord(0).charAt(i)));// check translate when swipe key
//                        }
//                    }
                    latinIme.mInputLogic.onUpdateTailBatchInputCompleted(
                            latinIme.mSettings.getCurrent(),
                            suggestedWords, latinIme.mKeyboardSwitcher);
                    latinIme.onTailBatchInputResultShown(suggestedWords);
                    break;
                case MSG_RESET_CACHES:
                    final SettingsValues settingsValues = latinIme.mSettings.getCurrent();
                    if (latinIme.mInputLogic.retryResetCachesAndReturnSuccess(
                            msg.arg1 == ARG1_TRUE /* tryResumeSuggestions */,
                            msg.arg2 /* remainingTries */, this /* handler */)) {
                        // If we were able to reset the caches, then we can reload the keyboard.
                        // Otherwise, we'll do it when we can.
                        latinIme.mKeyboardSwitcher.loadKeyboard(latinIme.getCurrentInputEditorInfo(),
                                settingsValues, latinIme.getCurrentAutoCapsState(),
                                latinIme.getCurrentRecapitalizeState(), false);
                    }
                    break;
                case MSG_WAIT_FOR_DICTIONARY_LOAD:
                    Log.i(TAG, "Timeout waiting for dictionary load");
                    break;
                case MSG_DEALLOCATE_MEMORY:
//                    latinIme.deallocateMemory();
                    break;
                case MSG_SWITCH_LANGUAGE_AUTOMATICALLY:
                    latinIme.switchLanguage((InputMethodSubtype) msg.obj);
                    break;
            }
        }

        public boolean isAcceptingDoubleSpaces() {
            return hasMessages(MSG_SPACE_TYPED);
        }

        public void startDoubleSpacesTimer() {
            removeMessages(MSG_SPACE_TYPED);
            sendMessageDelayed(obtainMessage(MSG_SPACE_TYPED), 1000);
        }

        public void cancelDoubleSpacesTimer() {
            removeMessages(MSG_SPACE_TYPED);
        }

        public void postUpdateSuggestions() {
            removeMessages(MSG_UPDATE_SUGGESTIONS);
            sendMessageDelayed(obtainMessage(MSG_UPDATE_SUGGESTIONS), 100);
        }

        public void postUpdateSuggestionStrip(final int inputStyle) {
            sendMessageDelayed(obtainMessage(MSG_UPDATE_SUGGESTION_STRIP, inputStyle,
                    0 /* ignored */), mDelayInMillisecondsToUpdateSuggestions);
        }

        public void postReopenDictionaries() {
            sendMessage(obtainMessage(MSG_REOPEN_DICTIONARIES));
        }

        private void postResumeSuggestionsInternal(final boolean shouldDelay,
                                                   final boolean forStartInput) {
            final LatinIME latinIme = getOwnerInstance();
            if (latinIme == null) {
                return;
            }
            if (!latinIme.mSettings.getCurrent().isSuggestionsEnabledPerUserSettings()) {
                return;
            }
            removeMessages(MSG_RESUME_SUGGESTIONS);
            removeMessages(MSG_RESUME_SUGGESTIONS_FOR_START_INPUT);
            final int message = forStartInput ? MSG_RESUME_SUGGESTIONS_FOR_START_INPUT
                    : MSG_RESUME_SUGGESTIONS;
            if (shouldDelay) {
                sendMessageDelayed(obtainMessage(message),
                        mDelayInMillisecondsToUpdateSuggestions);
            } else {
                sendMessage(obtainMessage(message));
            }
        }

        public void postResumeSuggestions(final boolean shouldDelay) {
            postResumeSuggestionsInternal(shouldDelay, false /* forStartInput */);
        }

        public void postResumeSuggestions(final boolean shouldIncludeResumedWordInSuggestions,
                                          final boolean shouldDelay) {
            final LatinIME latinIme = getOwnerInstance();
            if (latinIme == null) {
                return;
            }
            if (!latinIme.mSettings.getCurrent().isSuggestionsEnabledPerUserSettings()) {
                return;
            }

            removeMessages(MSG_RESUME_SUGGESTIONS);
            if (shouldDelay) {
                sendMessageDelayed(obtainMessage(MSG_RESUME_SUGGESTIONS,
                        shouldIncludeResumedWordInSuggestions ? ARG1_TRUE : ARG1_FALSE,
                        0 /* ignored */), mDelayInMillisecondsToUpdateSuggestions);
            } else {
                sendMessage(obtainMessage(MSG_RESUME_SUGGESTIONS,
                        shouldIncludeResumedWordInSuggestions ? ARG1_TRUE : ARG1_FALSE,
                        0 /* ignored */));
            }
        }

        public void postResumeSuggestionsForStartInput(final boolean shouldDelay) {
            postResumeSuggestionsInternal(shouldDelay, true /* forStartInput */);
        }

        public void postResetCaches(final boolean tryResumeSuggestions, final int remainingTries) {
            removeMessages(MSG_RESET_CACHES);
            sendMessage(obtainMessage(MSG_RESET_CACHES, tryResumeSuggestions ? 1 : 0,
                    remainingTries, null));
        }

        public void postWaitForDictionaryLoad() {
            sendMessageDelayed(obtainMessage(MSG_WAIT_FOR_DICTIONARY_LOAD),
                    DELAY_WAIT_FOR_DICTIONARY_LOAD_MILLIS);
        }

        public void cancelWaitForDictionaryLoad() {
            removeMessages(MSG_WAIT_FOR_DICTIONARY_LOAD);
        }

        public boolean hasPendingWaitForDictionaryLoad() {
            return hasMessages(MSG_WAIT_FOR_DICTIONARY_LOAD);
        }

        public void cancelUpdateSuggestionStrip() {
            removeMessages(MSG_UPDATE_SUGGESTION_STRIP);
        }

        public boolean hasPendingUpdateSuggestions() {
            return hasMessages(MSG_UPDATE_SUGGESTION_STRIP);
        }

        public boolean hasPendingReopenDictionaries() {
            return hasMessages(MSG_REOPEN_DICTIONARIES);
        }

        public void postUpdateShiftState() {
            removeMessages(MSG_UPDATE_SHIFT_STATE);
            sendMessageDelayed(obtainMessage(MSG_UPDATE_SHIFT_STATE),
                    mDelayInMillisecondsToUpdateShiftState);
        }

        public void postDeallocateMemory() {
            sendMessageDelayed(obtainMessage(MSG_DEALLOCATE_MEMORY),
                    DELAY_DEALLOCATE_MEMORY_MILLIS);
        }

        public void cancelDeallocateMemory() {
            removeMessages(MSG_DEALLOCATE_MEMORY);
        }

        public boolean hasPendingDeallocateMemory() {
            return hasMessages(MSG_DEALLOCATE_MEMORY);
        }

        @UsedForTesting
        public void removeAllMessages() {
            for (int i = 0; i <= MSG_LAST; ++i) {
                removeMessages(i);
            }
        }

        public void showGesturePreviewAndSuggestionStrip(final SuggestedWords suggestedWords,
                                                         final boolean dismissGestureFloatingPreviewText) {
            removeMessages(MSG_SHOW_GESTURE_PREVIEW_AND_SUGGESTION_STRIP);
            final int arg1 = dismissGestureFloatingPreviewText
                    ? ARG1_DISMISS_GESTURE_FLOATING_PREVIEW_TEXT
                    : ARG1_SHOW_GESTURE_FLOATING_PREVIEW_TEXT;
            obtainMessage(MSG_SHOW_GESTURE_PREVIEW_AND_SUGGESTION_STRIP, arg1,
                    ARG2_UNUSED, suggestedWords).sendToTarget();
        }

        public void showSuggestionStrip(final SuggestedWords suggestedWords) {
            removeMessages(MSG_SHOW_GESTURE_PREVIEW_AND_SUGGESTION_STRIP);
            obtainMessage(MSG_SHOW_GESTURE_PREVIEW_AND_SUGGESTION_STRIP,
                    ARG1_NOT_GESTURE_INPUT, ARG2_UNUSED, suggestedWords).sendToTarget();
        }

        public void showTailBatchInputResult(final SuggestedWords suggestedWords) {
            obtainMessage(MSG_UPDATE_TAIL_BATCH_INPUT_COMPLETED, suggestedWords).sendToTarget();
        }

        public void postSwitchLanguage(final InputMethodSubtype subtype) {
            obtainMessage(MSG_SWITCH_LANGUAGE_AUTOMATICALLY, subtype).sendToTarget();
        }

        public void startOrientationChanging() {
            removeMessages(MSG_PENDING_IMS_CALLBACK);
            resetPendingImsCallback();
            mIsOrientationChanging = true;
            final LatinIME latinIme = getOwnerInstance();
            if (latinIme == null) {
                return;
            }
            if (latinIme.isInputViewShown()) {
                latinIme.mKeyboardSwitcher.saveKeyboardState();
            }
        }

        private void resetPendingImsCallback() {
            mHasPendingFinishInputView = false;
            mHasPendingFinishInput = false;
            mHasPendingStartInput = false;
        }

        private void executePendingImsCallback(final LatinIME latinIme, final EditorInfo editorInfo,
                                               boolean restarting) {
            if (mHasPendingFinishInputView) {
                latinIme.onFinishInputViewInternal(mHasPendingFinishInput);
            }
            if (mHasPendingFinishInput) {
                latinIme.onFinishInputInternal();
            }
            if (mHasPendingStartInput) {
                latinIme.onStartInputInternal(editorInfo, restarting);
            }
            resetPendingImsCallback();
        }

        public void onStartInput(final EditorInfo editorInfo, final boolean restarting) {
            if (hasMessages(MSG_PENDING_IMS_CALLBACK)) {
                // Typically this is the second onStartInput after orientation changed.
                mHasPendingStartInput = true;
            } else {
                if (mIsOrientationChanging && restarting) {
                    // This is the first onStartInput after orientation changed.
                    mIsOrientationChanging = false;
                    mPendingSuccessiveImsCallback = true;
                }
                final LatinIME latinIme = getOwnerInstance();
                if (latinIme != null) {
                    executePendingImsCallback(latinIme, editorInfo, restarting);
                    latinIme.onStartInputInternal(editorInfo, restarting);
                }
            }
        }

        public void onStartInputView(final EditorInfo editorInfo, final boolean restarting) {
            if (hasMessages(MSG_PENDING_IMS_CALLBACK) && KeyboardId.equivalentEditorInfoForKeyboard(editorInfo, mAppliedEditorInfo)) {
                // Typically this is the second onStartInputView after orientation changed.
                resetPendingImsCallback();
            } else {
                if (mPendingSuccessiveImsCallback) {
                    // This is the first onStartInputView after orientation changed.
                    mPendingSuccessiveImsCallback = false;
                    resetPendingImsCallback();
                    sendMessageDelayed(obtainMessage(MSG_PENDING_IMS_CALLBACK),
                            PENDING_IMS_CALLBACK_DURATION_MILLIS);
                }

                final LatinIME latinIme = getOwnerInstance();
                if (latinIme != null) {
                    executePendingImsCallback(latinIme, editorInfo, restarting);
                    latinIme.onStartInputViewInternal(editorInfo, restarting);
                    mAppliedEditorInfo = editorInfo;

                }
                cancelDeallocateMemory();
            }
        }

        public void onFinishInputView(final boolean finishingInput) {
            // Todo: Duongcv resest save state shift
//            App.getInstance().mPrefs.edit().putInt(Settings.SAVE_STATE_SHIFT_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, AlphabetShiftState.UNSHIFTED).apply();
            if (App.getInstance().mPrefs.getBoolean(Settings.SAVE_STATE_AUTO_CAPS_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, false)) {
                Timber.e("Duongcv " + "set true");
                App.getInstance().mPrefs.edit().putBoolean(Settings.PREF_AUTO_CAP, true).apply();
                App.getInstance().mPrefs.edit().putBoolean(Settings.SAVE_STATE_AUTO_CAPS_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, false).apply();
            }
            if (hasMessages(MSG_PENDING_IMS_CALLBACK)) {
                // Typically this is the first onFinishInputView after orientation changed.
                mHasPendingFinishInputView = true;
            } else {
                final LatinIME latinIme = getOwnerInstance();
                if (latinIme != null) {
                    latinIme.onFinishInputViewInternal(finishingInput);
                    mAppliedEditorInfo = null;
                }
                if (!hasPendingDeallocateMemory()) {
                    postDeallocateMemory();
                }
            }
        }

        public void onFinishInput() {
            if (hasMessages(MSG_PENDING_IMS_CALLBACK)) {
                // Typically this is the first onFinishInput after orientation changed.
                mHasPendingFinishInput = true;
            } else {
                final LatinIME latinIme = getOwnerInstance();
                if (latinIme != null) {
                    executePendingImsCallback(latinIme, null, false);
                    latinIme.onFinishInputInternal();
                }
            }
        }
    }

    static final class SubtypeState {
        private InputMethodSubtype mLastActiveSubtype;
        private boolean mCurrentSubtypeHasBeenUsed;

        public void setCurrentSubtypeHasBeenUsed() {
            mCurrentSubtypeHasBeenUsed = true;
        }

        public void switchSubtype(final IBinder token, final RichInputMethodManager richImm) {
            final InputMethodSubtype currentSubtype = richImm.getInputMethodManager()
                    .getCurrentInputMethodSubtype();
            final InputMethodSubtype lastActiveSubtype = mLastActiveSubtype;
            final boolean currentSubtypeHasBeenUsed = mCurrentSubtypeHasBeenUsed;
            if (currentSubtypeHasBeenUsed) {
                mLastActiveSubtype = currentSubtype;
                mCurrentSubtypeHasBeenUsed = false;
            }
            if (currentSubtypeHasBeenUsed
                    && richImm.checkIfSubtypeBelongsToThisImeAndEnabled(lastActiveSubtype)
                    && !currentSubtype.equals(lastActiveSubtype)) {
                richImm.setInputMethodAndSubtype(token, lastActiveSubtype);
                return;
            }
            richImm.switchToNextInputMethod(token, true /* onlyCurrentIme */);
        }

        private boolean switchSubtypeRGB(final IBinder token, final RichInputMethodManager richImm) {
            return richImm.switchToNextInputMethod(token, true /* onlyCurrentIme */);
        }
    }
}
