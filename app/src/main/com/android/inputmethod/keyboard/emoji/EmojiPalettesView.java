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

import static com.android.inputmethod.latin.common.Constants.NOT_A_COORDINATE;
import static com.tapbi.spark.yokey.common.Constant.GPHY_TRENDING;

import static timber.log.Timber.d;
import static timber.log.Timber.e;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.tapbi.spark.yokey.R;
import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.KeyboardActionListener;
import com.android.inputmethod.keyboard.KeyboardLayoutSet;
import com.android.inputmethod.keyboard.KeyboardView;
import com.android.inputmethod.keyboard.internal.KeyDrawParams;
import com.android.inputmethod.keyboard.internal.KeyVisualAttributes;
import com.android.inputmethod.keyboard.internal.KeyboardIconsSet;
import com.android.inputmethod.keyboard.viewGif.ViewGif;
import com.android.inputmethod.latin.LatinIME;
import com.android.inputmethod.latin.RichInputMethodSubtype;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.settings.Settings;
import com.android.inputmethod.latin.utils.ResourceUtils;
import com.giphy.sdk.core.models.Media;
import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.data.local.entity.Emoji;
import com.tapbi.spark.yokey.data.local.entity.StickerRecent;
import com.tapbi.spark.yokey.data.local.entity.Symbols;
import com.tapbi.spark.yokey.data.model.MessageEvent;
import com.tapbi.spark.yokey.data.model.StickerOnKeyboard;
import com.tapbi.spark.yokey.data.model.theme.ThemeModel;
import com.tapbi.spark.yokey.feature.gif.GifAdapter;
import com.tapbi.spark.yokey.feature.gif.GifCategoryAdapter;
import com.tapbi.spark.yokey.ui.adapter.CategorySymbolsAdapter;
import com.tapbi.spark.yokey.ui.adapter.ContentSymbolsAdapter;
import com.tapbi.spark.yokey.ui.adapter.EmojiFavouriteAdapter;
import com.tapbi.spark.yokey.ui.adapter.ItemStickerOnKeyboardAdapter;
import com.tapbi.spark.yokey.ui.adapter.ItemTabStickerAdapter;
import com.tapbi.spark.yokey.ui.custom.view.CustomRecycleView;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.Constant;
import com.tapbi.spark.yokey.util.DisplayUtils;
import com.tapbi.spark.yokey.util.LocaleUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import timber.log.Timber;

/**
 * View class to implement Emoji palettes.
 * The Emoji keyboard consists of group of views layout/emoji_palettes_view.
 * * <ol>
 * <li> Emoji category tabs.
 * <li> Delete button.
 * <li> Emoji keyboard pages that can be scrolled by swiping horizontally or by selecting a tab.
 * <li> Back to main keyboard button and enter button.
 * </ol>
 * Because of the above reasons, this class doesn't extend {@link KeyboardView}.
 */
public final class EmojiPalettesView extends ConstraintLayout implements OnTabChangeListener,
        ViewPager.OnPageChangeListener, View.OnClickListener, View.OnTouchListener,
        EmojiPageKeyboardView.OnKeyEventListener, ViewGif.IListenerCLickGif {
    private int mFunctionalKeyBackgroundId;
    private int mSpacebarBackgroundId;
    private boolean mCategoryIndicatorEnabled;
    private int mCategoryIndicatorDrawableResId;
    private int mCategoryIndicatorBackgroundResId;
    private int mCategoryPageIndicatorColor;
    private int mCategoryPageIndicatorBackground;
    private EmojiPalettesAdapter mEmojiPalettesAdapter;
    private EmojiLayoutParams mEmojiLayoutParams;
    private DeleteKeyOnTouchListener mDeleteKeyOnTouchListener;
    private ViewGif viewGif;
    private String categoryGif = GPHY_TRENDING;
    private int radius = 0;
    private View mSpacebar;
    private int typeSymbol = 1;
    private EmojiPalettesView emojiPalettesView;
    // TODO: Remove this workaround.
    private View mSpacebarIcon;
    private TabHost mTabHost;
    private ViewPager mEmojiPager;
    private int mCurrentPagerPosition = 0;
    private EmojiCategoryPageIndicatorView mEmojiCategoryPageIndicatorView;
    private ProgressBar progressBar;
    private TextView textViewSearchGif;
    private Group llGif, llEmoji, llSymbols, llSticker;
    private ConstraintLayout ctlControl;
    private ImageView ivGif, ivSticker, ivEmoji, ivSymbols, ivDelete, ivKeyboard, imgAddSticker, imgNoRecentSticker, imgOverlay;
    private CategorySymbolsAdapter categorySymbolsAdapter;
    private int idCategorySymbolsPosition = 1;
    private ContentSymbolsAdapter contentSymbolsAdapter;
    private ArrayList<Symbols> listSymbols, listSymbolsEmoji, listSymbolsDecorative;
    // private GifManager gifManager;
    private GifAdapter gifAdapter;
    private TextView txtAddSticker, tvNoFavourite, tvNoRecentSticker;
    private ArrayList<Emoji> listEmoji;
    private HashMap<String, String> checkDuplicateEmoji = new HashMap<>();
    private EmojiFavouriteAdapter emojiFavouriteAdapter;
    private ArrayList<StickerOnKeyboard> listStickerOnKeyboard;
    private ArrayList<StickerRecent> listStickerRecent;
    private ItemStickerOnKeyboardAdapter itemStickerOnKeyboardAdapter;
    private ItemTabStickerAdapter itemTabStickerAdapter;
    private GifCategoryAdapter gifCategoryAdapter;
    private RecyclerView rcvSymbolsCategory, rcvSymbolsContent, rclTabSticker, rclSticker, rcvGifCategory;
    private CustomRecycleView rcvEmojiFavourite;
    private RecyclerView rcvGif;
    private EditText etSearch;
    private int categorySelected = 0;
    private String textSearch;
    private ThemeModel themeModel;
    private int colorNotUse = Color.parseColor("#1C1C1C");
    //   private int colorUse = Color.parseColor("#ff0000");
    private int colorUse = Color.parseColor("#FF56DDD5");
    private final int colorAlphaOverlay = Color.parseColor("#0D000000");
    private boolean isFinishFlate = false;
    private boolean categoryEmojiLoadDone = false;
    private float heightRowPercent;
    private KeyboardActionListener mKeyboardActionListener = KeyboardActionListener.EMPTY_LISTENER;
    private SharedPreferences prefs;
    private EmojiCategory mEmojiCategory;
    private AttributeSet attrs;
    private int defStyle;
    private Context context;
    private ConstraintLayout ctlDownloadGif;

    public EmojiPalettesView(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.emojiPalettesViewStyle);
    }

    public EmojiPalettesView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        this.attrs = attrs;
        this.defStyle = defStyle;
        this.context = context;
        init();
    }


    private void init() {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        heightRowPercent = (float) prefs.getFloat(com.tapbi.spark.yokey.common.Constant.HEIGHT_ROW_KEY, com.tapbi.spark.yokey.common.Constant.VALUE_HEIGHT_DEFAULT_KEYBOARD);
        final TypedArray keyboardViewAttr = context.obtainStyledAttributes(attrs,
                R.styleable.KeyboardView, defStyle, R.style.KeyboardView);
        final int keyBackgroundId = keyboardViewAttr.getResourceId(
                R.styleable.KeyboardView_keyBackground, 0);
        mFunctionalKeyBackgroundId = keyboardViewAttr.getResourceId(
                R.styleable.KeyboardView_functionalKeyBackground, keyBackgroundId);
        mSpacebarBackgroundId = keyboardViewAttr.getResourceId(
                R.styleable.KeyboardView_spacebarBackground, keyBackgroundId);
        keyboardViewAttr.recycle();
        final KeyboardLayoutSet.Builder builder = new KeyboardLayoutSet.Builder(
                context, null /* editorInfo */);
        final Resources res = context.getResources();
        mEmojiLayoutParams = new EmojiLayoutParams(res);
        builder.setSubtype(RichInputMethodSubtype.getEmojiSubtype());
        builder.setKeyboardGeometry(ResourceUtils.getDefaultKeyboardWidth(res),
                mEmojiLayoutParams.mEmojiKeyboardHeight);
        final KeyboardLayoutSet layoutSet = builder.build();
        final TypedArray emojiPalettesViewAttr = context.obtainStyledAttributes(attrs,
                R.styleable.EmojiPalettesView, defStyle, R.style.EmojiPalettesView);
        mEmojiCategory = new EmojiCategory(PreferenceManager.getDefaultSharedPreferences(context),
                res, layoutSet, emojiPalettesViewAttr, new EmojiCategory.OnEmojiLoadDataListener() {
            @Override
            public void onEmojiLoadDataFinish() {
                updateWhenEmojiLoadDone();
            }

            @Override
            public void onEmojiRefresh() {
                mEmojiPalettesAdapter = new EmojiPalettesAdapter(mEmojiCategory, EmojiPalettesView.this);
                mEmojiPager.setAdapter(mEmojiPalettesAdapter);
                setCurrentCategoryId(mEmojiCategory.getCurrentCategoryId(), true /* force */);
            }
        });
        mCategoryIndicatorEnabled = emojiPalettesViewAttr.getBoolean(
                R.styleable.EmojiPalettesView_categoryIndicatorEnabled, false);
        mCategoryIndicatorDrawableResId = emojiPalettesViewAttr.getResourceId(
                R.styleable.EmojiPalettesView_categoryIndicatorDrawable, 0);
        mCategoryIndicatorBackgroundResId = emojiPalettesViewAttr.getResourceId(
                R.styleable.EmojiPalettesView_categoryIndicatorBackground, 0);
        mCategoryPageIndicatorColor = emojiPalettesViewAttr.getColor(
                R.styleable.EmojiPalettesView_categoryPageIndicatorColor, 0);
        mCategoryPageIndicatorBackground = emojiPalettesViewAttr.getColor(
                R.styleable.EmojiPalettesView_categoryPageIndicatorBackground, 0);
        emojiPalettesViewAttr.recycle();
        mDeleteKeyOnTouchListener = new DeleteKeyOnTouchListener();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // The main keyboard expands to the entire this {@link KeyboardView}.
        initMeasure();
    }

    private void initMeasure() {
        final Resources res = getContext().getResources();
        final int width = ResourceUtils.getDefaultKeyboardWidth(res)
                + getPaddingLeft() + getPaddingRight();
        final int height = ResourceUtils.getDefaultKeyboardHeight(res)
                + res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height)
                + getPaddingTop() + getPaddingBottom();
        if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_NONE) {
            if (DisplayUtils.getScreenHeight() > DisplayUtils.getScreenWidth()) {
                int heightKbNew = (int) prefs.getFloat(com.tapbi.spark.yokey.common.Constant.HEIGHT_KEYBOARD_NEW, 0);
                setMeasuredDimension(width, heightKbNew != 0 ? heightKbNew + res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height) : height);
            } else {
                int heightKbNew = (int) prefs.getFloat(com.tapbi.spark.yokey.common.Constant.HEIGHT_KEYBOARD_NEW_VERTICAL, 0);
                setMeasuredDimension(width, heightKbNew != 0 ? (heightKbNew + res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height)) : height);
            }
        } else {
            setMeasuredDimension(width, height);
        }
    }

  /*  public void getThemeModel(ThemeModel themeModel) {
        this.themeModel = themeModel;
    }*/

    private void addTab(final TabHost host, final int categoryId) {
        final String tabId = EmojiCategory.getCategoryName(categoryId, 0 /* categoryPageId */);
        final TabHost.TabSpec tspec = host.newTabSpec(tabId);
        tspec.setContent(R.id.emoji_keyboard_dummy);
        final ImageView iconView = (ImageView) LayoutInflater.from(getContext()).inflate(
                R.layout.emoji_keyboard_tab_icon, null);
        // TODO: Replace background color with its own setting rather than using the
        //       category page indicator background as a workaround.
        iconView.setBackgroundColor(mCategoryPageIndicatorBackground);
        iconView.setImageResource(mEmojiCategory.getCategoryTabIcon(categoryId));
        iconView.setContentDescription(mEmojiCategory.getAccessibilityDescription(categoryId));
        iconView.setBackgroundColor(Color.TRANSPARENT);
        tspec.setIndicator(iconView);
        host.addTab(tspec);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (App.getInstance().stickerRepository.arrayListCategoryGif.isEmpty()) {
            App.getInstance().stickerRepository.loadDataGifCategory();
        }
        ctlControl = findViewById(R.id.ctlControl);
        emojiPalettesView = findViewById(R.id.emoji_palettes_view);
        mTabHost = (TabHost) findViewById(R.id.emoji_category_tabhost);

//        rcvEmojiFavourite.setInverse(true);
        mTabHost.setup();
        mTabHost.getTabWidget().setBackgroundColor(Color.TRANSPARENT);
        for (final EmojiCategory.CategoryProperties properties
                : mEmojiCategory.getShownCategories()) {
            addTab(mTabHost, properties.mCategoryId);
        }
        mTabHost.setOnTabChangedListener(this);
        final TabWidget tabWidget = mTabHost.getTabWidget();
        tabWidget.setStripEnabled(mCategoryIndicatorEnabled);
        if (mCategoryIndicatorEnabled) {
            // On TabWidget's strip, what looks like an indicator is actually a background.
            // And what looks like a background are actually left and right drawables.
            tabWidget.setBackgroundResource(mCategoryIndicatorDrawableResId);
            tabWidget.setLeftStripDrawable(mCategoryIndicatorBackgroundResId);
            tabWidget.setRightStripDrawable(mCategoryIndicatorBackgroundResId);
        }

        //gifManager = new GifManager();
        //  gifManager.setHandler(handler);
        // {@link #mAlphabetKeyLeft}, {@link #mAlphabetKeyRight, and spaceKey depend on
        // {@link View.OnClickListener} as well as {@link View.OnTouchListener}.
        // {@link View.OnTouchListener} is used as the trigger of key-press, while
        // {@link View.OnClickListener} is used as the trigger of key-release which does not occur
        // if the event is canceled by moving off the finger from the view.
        // The text on alphabet keys are set at
        // {@link #startEmojiPalettes(String,int,float,Typeface)}.
        llEmoji = findViewById(R.id.ll_emoji);
        llSymbols = findViewById(R.id.groupSymbols);
        llGif = findViewById(R.id.ll_gif);
        llSticker = findViewById(R.id.llSticker);
        mEmojiPager = (ViewPager) findViewById(R.id.emoji_keyboard_pager);
        tvNoFavourite = findViewById(R.id.tvNoFavourite);
        rcvEmojiFavourite = findViewById(R.id.rcvEmojiFavourite);
        mEmojiCategoryPageIndicatorView =
                (EmojiCategoryPageIndicatorView) findViewById(R.id.emoji_category_page_id_view);
        ivKeyboard = findViewById(R.id.iv_keyboard_text);
        ivSymbols = findViewById(R.id.iv_symbols);
        ivDelete = findViewById(R.id.iv_delete);
        ivEmoji = findViewById(R.id.iv_emoji);
        imgOverlay = findViewById(R.id.imgOverlay);
        ivGif = findViewById(R.id.iv_gif);
        ivSticker = findViewById(R.id.iv_sticker);
        rclTabSticker = findViewById(R.id.rclTabSticker);
        rclSticker = findViewById(R.id.rclStickerOnKeyboard);
        imgAddSticker = findViewById(R.id.imgAddSticker);
        txtAddSticker = findViewById(R.id.txtAddSticker);
        tvNoRecentSticker = findViewById(R.id.tvNoRecentSticker);
        imgNoRecentSticker = findViewById(R.id.imgNoRecentSticker);
        rcvSymbolsCategory = findViewById(R.id.rclCategorySymbols);
        rcvSymbolsContent = findViewById(R.id.rclContentSymbols);
        ivKeyboard.setTag(Constants.CODE_ALPHA_FROM_EMOJI);
        ivKeyboard.setOnTouchListener(this);
        ivKeyboard.setOnClickListener(this);
        progressBar = findViewById(R.id.progressBarGif);
        textViewSearchGif = findViewById(R.id.txtNoDataSearchGif);
        viewGif = findViewById(R.id.rv_gif);
        ivDelete.setTag(Constants.CODE_DELETE);
        ivDelete.setOnTouchListener(mDeleteKeyOnTouchListener);
        ctlDownloadGif = findViewById(R.id.ctlDownloadGif);
        ivGif.setOnClickListener(v -> {
//                    if (App.getInstance().checkConnectivityStatus() == -1) {
//                        Toast.makeText(App.getInstance(), R.string.pleaseTurnOnInternet, Toast.LENGTH_SHORT).show();
//                    }
                    onEmojiGifShow(Constant.TYPE_GIF);
                }
        );
        ivEmoji.setOnClickListener(v -> {
            onEmojiGifShow(Constant.TYPE_EMOJI);
        });
        ivSymbols.setOnClickListener(v -> onEmojiGifShow(Constant.TYPE_SYMBOLS));
        ivSticker.setOnClickListener(v -> {
            onEmojiGifShow(Constant.TYPE_STICKER);
        });
        onEmojiGifShow(Constant.TYPE_EMOJI);
        setupView();
        viewGif.setListenerGif(this);
    }

    private void setupView() {


        mEmojiLayoutParams.setPagerProperties(mEmojiPager, ctlControl);
        mEmojiPalettesAdapter = new EmojiPalettesAdapter(mEmojiCategory, this);

        mEmojiPager.setAdapter(mEmojiPalettesAdapter);
        mEmojiPager.setOnPageChangeListener(this);
        mEmojiPager.setOffscreenPageLimit(0);
        mEmojiPager.setPersistentDrawingCache(PERSISTENT_NO_CACHE);
        mTabHost.setCurrentTab(2);


        mEmojiCategoryPageIndicatorView.setColors(
                mCategoryPageIndicatorColor, mCategoryPageIndicatorBackground);
        mEmojiLayoutParams.setCategoryPageIdViewProperties(mEmojiCategoryPageIndicatorView);

        setCurrentCategoryId(mEmojiCategory.getCurrentCategoryId(), true /* force */);

        initGifUI();
        initSymbols();
        initSticker();
        initEmojiFavourite();
        setUpGifData();
        isFinishFlate = true;
        if (categoryEmojiLoadDone) {
            updateWhenEmojiLoadDone();
        }
    }

    private void initEmojiFavourite() {
        listEmoji = new ArrayList<>();
        emojiFavouriteAdapter = new EmojiFavouriteAdapter(listEmoji, getContext(), new EmojiFavouriteAdapter.SetEmojiFavourite() {
            @Override
            public void clickAddEmoji() {
                ((LatinIME) mKeyboardActionListener).openSetting(Constant.KEY_SCREEN_EMOJIS, false);
            }

            @Override
            public void setEmoji(@NonNull String emoji) {
                if (mKeyboardActionListener instanceof LatinIME) {
                    ((LatinIME) mKeyboardActionListener).sendTextToEditText(emoji);
                }
            }
        });
        rcvEmojiFavourite.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcvEmojiFavourite.setAdapter(emojiFavouriteAdapter);
    }

    private void loadAllEmojiFavourite() {
        App.getInstance().stickerRepository.loadEmojiFavourite().subscribe(new SingleObserver<ArrayList<Emoji>>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull ArrayList<Emoji> emojis) {
                if (emojis.size() > 0) {
                    listEmoji.clear();
                    listEmoji = emojis;
                    emojiFavouriteAdapter.changeList(listEmoji);
                    tvNoFavourite.setVisibility(GONE);
                }
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

            }
        });
        tvNoFavourite.setOnClickListener(v -> {
                    tvNoFavourite.setEnabled(false);
                    ((LatinIME) mKeyboardActionListener).openSetting(Constant.KEY_SCREEN_EMOJIS, false);
                }
        );
    }

    private void initSticker() {
        txtAddSticker.setVisibility(VISIBLE);
        listStickerOnKeyboard = new ArrayList<>();
        itemStickerOnKeyboardAdapter = new ItemStickerOnKeyboardAdapter(App.getInstance(), new ArrayList<>(), null);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(App.getInstance(), 4);
        rclSticker.setLayoutManager(gridLayoutManager);
        rclSticker.setAdapter(itemStickerOnKeyboardAdapter);
        rclSticker.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstvisiblePosition = gridLayoutManager.findFirstVisibleItemPosition();
                int lastvisiblePosition = gridLayoutManager.findLastVisibleItemPosition();
                itemStickerOnKeyboardAdapter.changeStatusAnimByPosition(firstvisiblePosition, lastvisiblePosition);
            }
        });
        itemTabStickerAdapter = new ItemTabStickerAdapter(listStickerOnKeyboard, App.getInstance(), new ItemTabStickerAdapter.ListenerPosition() {
            @Override
            public void getPosition(int position) {
                if (position == 0) {
                    App.getInstance().stickerRepository.loadStickerRecent();
                    convertListStickerRecentToListSticker();
                } else {
                    int pos = position;
                    tvNoRecentSticker.setVisibility(GONE);
                    imgNoRecentSticker.setVisibility(GONE);
                    if (listStickerOnKeyboard.size() > pos && itemStickerOnKeyboardAdapter != null) {

                        itemStickerOnKeyboardAdapter.changeList(listStickerOnKeyboard.get(pos).getListSticker());
                    }
                }
                rclSticker.scrollToPosition(0);

            }
        });
        rclTabSticker.setLayoutManager(new LinearLayoutManager(App.getInstance(), LinearLayoutManager.HORIZONTAL, false));
        rclTabSticker.setAdapter(itemTabStickerAdapter);


        itemStickerOnKeyboardAdapter.setListenerPathSticker(path -> {
            if (App.Companion.getInstance().isTryKeyboard()) {
                StickerRecent stickerRecent = new StickerRecent(path, String.valueOf(System.currentTimeMillis()));
                App.getInstance().stickerRepository.addStickerRecent(stickerRecent);
                EventBus.getDefault().post(new MessageEvent(Constant.EVENT_SEND_STICKER, null, path));
            } else {
                Uri contentUri = FileProvider.getUriForFile(App.getInstance(), App.getInstance().getPackageName(), new File(path));
                Timber.d("ducNQ : initStickered: ");
                if (!((LatinIME) mKeyboardActionListener).isGifSupport) {
                    if (!((LatinIME) mKeyboardActionListener).isToastText) {
                        Timber.e("hachung showTextNotSupportGif:");
                        showTextNotSupportGif(R.string.not_support_sticker);
                        ((LatinIME) mKeyboardActionListener).delayToastText();
                    }
                } else if (contentUri != null) {
                    boolean checkSticker = ((LatinIME) mKeyboardActionListener).commitGifImage(contentUri, path, null);
                    if (!checkSticker) {
                        Timber.e("hachung showTextNotSupportGif:");
                        showTextNotSupportGif(R.string.not_support_sticker);
                    }
                    StickerRecent stickerRecent = new StickerRecent(path, String.valueOf(System.currentTimeMillis()));
                    App.getInstance().stickerRepository.addStickerRecent(stickerRecent);
                    if (listStickerRecent == null) listStickerRecent = new ArrayList<>();
                    if (listStickerRecent.size() == 0) {
                        listStickerRecent.add(stickerRecent);
                    } else {
                        boolean check = false;
                        for (int i = 0; i < listStickerRecent.size(); i++) {
                            if (listStickerRecent.get(i).getLink().equals(path)) {
                                listStickerRecent.get(i).setTimeRecent(stickerRecent.getTimeRecent());
                                check = true;
                                break;
                            }
                        }
                        if (!check) listStickerRecent.add(stickerRecent);
                    }
                }
            }
        });

        txtAddSticker.setOnClickListener(v -> ((LatinIME) mKeyboardActionListener).openSetting(Constant.KEY_SCREEN_STICKER, false));
        imgAddSticker.setOnClickListener(v ->
                {
                    // imgAddSticker.setEnabled(false);
                    ((LatinIME) mKeyboardActionListener).openSetting(Constant.KEY_SCREEN_STICKER, false);
                }
        );
    }

    private void convertListStickerRecentToListSticker() {
        if (listStickerRecent == null || listStickerRecent.size() == 0) {
            if (itemTabStickerAdapter != null && itemTabStickerAdapter.getCurrentPossition() == 0 && llSticker.getVisibility() == VISIBLE) {
                tvNoRecentSticker.setVisibility(VISIBLE);
                imgNoRecentSticker.setVisibility(VISIBLE);
            }
            itemStickerOnKeyboardAdapter.changeList(new ArrayList<>());
        } else {
            tvNoRecentSticker.setVisibility(GONE);
            imgNoRecentSticker.setVisibility(GONE);
            ArrayList<String> listSticker = new ArrayList<>();
            Collections.sort(listStickerRecent, new Comparator<StickerRecent>() {
                @Override
                public int compare(StickerRecent stickerRecent, StickerRecent t1) {
                    return (int) (Long.parseLong(t1.getTimeRecent()) - Long.parseLong(stickerRecent.getTimeRecent()));
                }
            });
            for (StickerRecent stickerRecent : listStickerRecent) {
                if (listSticker.size() < 30) {
                    listSticker.add(stickerRecent.getLink());
                } else {
                    break;
                }
            }
            itemStickerOnKeyboardAdapter.changeList(listSticker);
        }
    }

    private void showSticker(int id) {
        if (App.getInstance().stickerRepository != null && App.getInstance().stickerRepository.stickerOnKeyboards != null && App.getInstance().stickerRepository.stickerOnKeyboards.size() > 0) {
            txtAddSticker.setVisibility(GONE);
            listStickerOnKeyboard.clear();
            listStickerOnKeyboard.addAll(App.getInstance().stickerRepository.stickerOnKeyboards);
            if (itemTabStickerAdapter != null && itemStickerOnKeyboardAdapter != null) {
                if (listStickerOnKeyboard.size() > id) {
                    itemTabStickerAdapter.changeList(listStickerOnKeyboard, id);
                    itemStickerOnKeyboardAdapter.changeList(listStickerOnKeyboard.get(id).getListSticker());
                } else {
                    itemTabStickerAdapter.changeList(listStickerOnKeyboard, 0);
                    convertListStickerRecentToListSticker();
                }
            }
        }
    }

    private void initSymbols() {
        categorySymbolsAdapter = new CategorySymbolsAdapter(getContext(), new CategorySymbolsAdapter.ChangeCategoryListener() {
            @Override
            public void changeCategory(int position) {
                idCategorySymbolsPosition = position;
                changeDataSymbols(listSymbols, listSymbolsEmoji, listSymbolsDecorative);
            }
        }, colorUse, colorNotUse);
        rcvSymbolsCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rcvSymbolsCategory.setAdapter(categorySymbolsAdapter);

        listSymbols = new ArrayList<>();
        contentSymbolsAdapter = new ContentSymbolsAdapter(listSymbols, getContext(), new ContentSymbolsAdapter.SetSymbols() {
            @Override
            public void setSymbols(@NonNull Symbols symbols) {
                if (listSymbols == null) listSymbols = new ArrayList<>();
                if (listSymbols.size() > 0) {
                    for (int i = 0; i < listSymbols.size(); i++) {
                        if (listSymbols.get(i).getId() == symbols.getId()) {
                            listSymbols.get(i).timeRecent = String.valueOf(System.currentTimeMillis());
                            break;
                        }
                    }
                }
                symbols.timeRecent = String.valueOf(System.currentTimeMillis());
                if (App.getInstance().symbolsReposition != null)
                    App.getInstance().symbolsReposition.updateSymbolsDB(symbols);
                if (mKeyboardActionListener instanceof LatinIME) {
                    ((LatinIME) mKeyboardActionListener).sendTextToEditText(symbols.contentSymbols);
                }
            }
        });
        rcvSymbolsContent.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rcvSymbolsContent.setAdapter(contentSymbolsAdapter);
        loadDataSymbol();
    }

    private void loadDataSymbol() {
        if (App.getInstance().symbolsReposition != null) {
            App.getInstance().symbolsReposition.getAllSymbols();
        }
    }

    public void changeDataSymbols(ArrayList<Symbols> listSymbols, ArrayList<Symbols> listSymbolsEmoji, ArrayList<Symbols> listSymbolsDecorative) {
        if (listSymbols != null) this.listSymbols = listSymbols;
        if (listSymbolsEmoji != null) this.listSymbolsEmoji = listSymbolsEmoji;
        if (listSymbolsDecorative != null) this.listSymbolsDecorative = listSymbolsDecorative;
        if (listSymbols == null || listSymbolsEmoji == null || listSymbolsDecorative == null)
            loadDataSymbol();
        if (idCategorySymbolsPosition == 0) {
            if (listSymbols != null) {
                try {
                    Collections.sort(listSymbols, new Comparator<Symbols>() {
                        @Override
                        public int compare(Symbols symbols, Symbols t1) {
                            return (int) (Long.parseLong(t1.timeRecent) - (Long.parseLong(symbols.timeRecent)));
                        }
                    });
                } catch (IllegalArgumentException ignored) {
                }
                ArrayList<Symbols> listRecent = new ArrayList<>();
                if (listSymbols.size() > 0) {
                    for (Symbols symbols : listSymbols) {
                        if (Long.parseLong(symbols.timeRecent) > 0) {
                            if (listRecent.size() < 30) {
                                listRecent.add(symbols);
                            } else {
                                break;
                            }
                        }
                    }
                }
                if (contentSymbolsAdapter != null) contentSymbolsAdapter.changeList(listRecent);
            }
        } else if (idCategorySymbolsPosition == 1) {
            if (contentSymbolsAdapter != null && listSymbolsEmoji != null)
                contentSymbolsAdapter.changeList(listSymbolsEmoji);
        } else {
            if (contentSymbolsAdapter != null && listSymbolsDecorative != null)
                contentSymbolsAdapter.changeList(listSymbolsDecorative);
        }
    }

    public void updateWhenEmojiLoadDone() {
        if (!isFinishFlate) {
            categoryEmojiLoadDone = true;
            return;
        }
        mTabHost.setup();
        for (final EmojiCategory.CategoryProperties properties : mEmojiCategory.getShownCategories()) {
            addTab(mTabHost, properties.mCategoryId);
        }
        mTabHost.setOnTabChangedListener(this);
        final TabWidget tabWidget = mTabHost.getTabWidget();
        tabWidget.setStripEnabled(mCategoryIndicatorEnabled);
        if (mCategoryIndicatorEnabled) {
            // On TabWidget's strip, what looks like an indicator is actually a background.
            // And what looks like a background are actually left and right drawables.
            tabWidget.setBackgroundResource(mCategoryIndicatorDrawableResId);
            tabWidget.setLeftStripDrawable(mCategoryIndicatorBackgroundResId);
            tabWidget.setRightStripDrawable(mCategoryIndicatorBackgroundResId);
        }
        mEmojiPager.addOnPageChangeListener(this);
        mEmojiPager.setOffscreenPageLimit(1);
        mEmojiPager.setPersistentDrawingCache(PERSISTENT_NO_CACHE);
//        mEmojiLayoutParams.setPagerProperties(mEmojiPager);

        mEmojiCategoryPageIndicatorView =
                (EmojiCategoryPageIndicatorView) findViewById(R.id.emoji_category_page_id_view);
        mEmojiCategoryPageIndicatorView.setColors(getContext().getResources().getColor(R.color.color_black_50), getContext().getResources().getColor(R.color.color_black_30));
        mEmojiLayoutParams.setCategoryPageIdViewProperties(mEmojiCategoryPageIndicatorView);
        mEmojiPalettesAdapter = new EmojiPalettesAdapter(mEmojiCategory, this);
        mEmojiPager.setAdapter(mEmojiPalettesAdapter);
        setCurrentCategoryId(mEmojiCategory.getCurrentCategoryId(), true /* force */);

    }

    public void setEnabledText() {
        tvNoFavourite.setEnabled(true);
    }

    public void setColorFilter(int color) {
        colorUse = color;
        // ColorUtils.blendARGB()
        colorNotUse = CommonUtil.manipulateColor(colorUse, 0.75f, 155);
        Timber.d("ducnq setColorFilter " + color);
        ivEmoji.setColorFilter(colorNotUse);
        ivGif.setColorFilter(colorNotUse);
        ivSymbols.setColorFilter(colorNotUse);
        ivSticker.setColorFilter(colorNotUse);
        switch (typeSymbol) {
            case Constant.TYPE_EMOJI:
                ivEmoji.setColorFilter(colorUse);
                break;
            case Constant.TYPE_GIF:
                ivGif.setColorFilter(colorUse);
                break;
            case Constant.TYPE_SYMBOLS:
                ivSymbols.setColorFilter(colorUse);
                break;
            case Constant.TYPE_STICKER:
                ivSticker.setColorFilter(colorUse);
                break;
        }
//        typeSymbol=Constant.TYPE_EMOJI;
//        if(typeSymbol==Constant.TYPE_EMOJI){
//            ivEmoji.setColorFilter(colorUse);
//            llEmoji.setVisibility(VISIBLE);
//            checkShowFavourite();
//        }

        changeColorTabHost(/*color*/);
    }

    private  void  setColorTextSticker(){
        if (imgAddSticker != null) imgAddSticker.setColorFilter(App.getInstance().colorIconNew);
        if (txtAddSticker != null) txtAddSticker.setTextColor(App.getInstance().colorIconNew);
    }

    public boolean getVisibleLayoutSearchGif() {
        if (llGif.getVisibility() == VISIBLE) {
            return true;
        }
        return false;
    }

    public void goneLayoutSearchGif(int visible) {
        llGif.setVisibility(visible);
    }

    private int typeCurrent = Constant.TYPE_EMOJI;

    public void onEmojiGifShow(int type) {
        typeCurrent = type;
        LocaleUtils.INSTANCE.applyLocale(context);
        ctlDownloadGif.setVisibility(GONE);
        colorUse = App.getInstance().colorIconDefault;
        colorNotUse = CommonUtil.manipulateColor(colorUse, 0.75f, 155);
        typeSymbol = type;
        llEmoji.setVisibility(GONE);
        llGif.setVisibility(GONE);
        llSymbols.setVisibility(GONE);
        llSticker.setVisibility(GONE);
        ivEmoji.setColorFilter(colorNotUse);
        ivGif.setColorFilter(colorNotUse);
        ivSymbols.setColorFilter(colorNotUse);
        ivSticker.setColorFilter(colorNotUse);
        tvNoFavourite.setVisibility(GONE);
        if (txtAddSticker != null) {
            txtAddSticker.setText(R.string.add_sticker);
        }
        if (etSearch != null) {
            etSearch.setHint(R.string.label_search_key);
        }
        if (tvNoFavourite != null) {
            tvNoFavourite.setText(R.string.add_more_emojis_combos);
        }
        if (rcvEmojiFavourite != null) {
            rcvEmojiFavourite.setVisibility(GONE);
        }
        if (textViewSearchGif != null) {
            textViewSearchGif.setText(R.string.no_data);
        }
        switch (type) {
            case Constant.TYPE_EMOJI:
                if (App.getInstance().mPrefs.getBoolean(Settings.PREF_AUTO_CAP, true)) {
                    App.getInstance().mPrefs.edit().putBoolean(Settings.SAVE_STATE_AUTO_CAPS_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, App.getInstance().mPrefs.getBoolean(Settings.PREF_AUTO_CAP, true)).apply();
                    App.getInstance().mPrefs.edit().putBoolean(Settings.PREF_AUTO_CAP, false).apply();
                }
                textViewSearchGif.setVisibility(INVISIBLE);
                progressBar.setVisibility(INVISIBLE);
                ivEmoji.setColorFilter(colorUse);
                llEmoji.setVisibility(VISIBLE);
                Timber.d("ducNQ : onEmojiGifShowed: " + textViewSearchGif.getVisibility());
                rcvEmojiFavourite.post(new Runnable() {
                    @Override
                    public void run() {
//                        rcvEmojiFavourite.setVisibility(GONE);
                        checkShowFavourite();
                    }
                });
//                rcvEmojiFavourite.setVisibility(GONE);
//                if(mTabHost!=null)mTabHost.setVisibility(VISIBLE);
//                if(mEmojiPager!=null)mEmojiPager.setVisibility(VISIBLE);
//                if(mEmojiCategoryPageIndicatorView!=null)mEmojiCategoryPageIndicatorView.setVisibility(VISIBLE);
                break;
            case Constant.TYPE_GIF:
                if (App.getInstance().mPrefs.getBoolean(Settings.PREF_AUTO_CAP, true)) {
                    App.getInstance().mPrefs.edit().putBoolean(Settings.SAVE_STATE_AUTO_CAPS_WHEN_CHANGE_SCREEN_EMOJI_STICKER_GIF, App.getInstance().mPrefs.getBoolean(Settings.PREF_AUTO_CAP, true)).apply();
                    App.getInstance().mPrefs.edit().putBoolean(Settings.PREF_AUTO_CAP, false).apply();
                }
                showViewGif();
                if (!etSearch.getText().toString().isEmpty()) {
                    gifCategoryAdapter.setPos(-1);
                }
//                if (gifManager != null) {
                gifCategoryAdapter.setPos(currentPositionGif);
                if (currentPositionGif == 0) {
                    viewGif.getGifByTrending();
                } else {
                    viewGif.getGifByCategory(categoryGif);
                }
                if (gifCategoryAdapter != null)
                    gifCategoryAdapter.changeColor(colorUse, colorNotUse);
                // }
                ivGif.setColorFilter(colorUse);
                resetGif();
                llGif.setVisibility(VISIBLE);
                break;
            case Constant.TYPE_SYMBOLS:
                textViewSearchGif.setVisibility(INVISIBLE);
                progressBar.setVisibility(INVISIBLE);
                ivSymbols.setColorFilter(colorUse);
                llSymbols.setVisibility(VISIBLE);
                categorySymbolsAdapter.changeColor(colorUse, colorNotUse);
                break;
            case Constant.TYPE_STICKER:
                imgAddSticker.setEnabled(true);
                textViewSearchGif.setVisibility(INVISIBLE);
                progressBar.setVisibility(INVISIBLE);
                ivSticker.setColorFilter(colorUse);
                llSticker.setVisibility(VISIBLE);
                tvNoRecentSticker.setVisibility(GONE);
                imgNoRecentSticker.setVisibility(GONE);
                showSticker(1);
                break;
        }
       setColorTextSticker();
        // changeColorTabHost(colorUse);
    }

    public void goneViewGif() {
        //   if (llGif != null) {
        llGif.setVisibility(GONE);
        //  }
//        if(llEmoji!=null){
//            llEmoji.setVisibility(VISIBLE);
//        }
    }

    private void setUpGifData() {
        currentPositionGif = 0;
        if (viewGif != null) {
            gifCategoryAdapter.setPos(0);
            //Timber.d("ducNQsetUpGifData " + etSearch.getText().toString());
            if (etSearch.getText().toString().isEmpty()) {
                viewGif.getGifByTrending();
            } else {
                viewGif.searchGif(etSearch.getText().toString());
            }
        }
//        if (gifManager != null) {
//            gifManager.loadCategories();
//            gifManager.getGiphyTrend();
//        }
    }

    private void initGifUI() {
        Timber.d("ducNQinitGifUI ");
        rcvGifCategory = (RecyclerView) findViewById(R.id.rv_category_gif);
        rcvGifCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rcvGifCategory.setItemAnimator(null);
        if (App.getInstance().stickerRepository != null) {
            gifCategoryAdapter = new GifCategoryAdapter(App.getInstance().stickerRepository.arrayListCategoryGif);
        }
        gifCategoryAdapter.setOnItemGifCategoryClickListener(onItemGifCategoryClickListener);
        rcvGifCategory.setAdapter(gifCategoryAdapter);

        //  rcvGif = (RecyclerView) findViewById(R.id.rv_gif);
        //  rcvGif.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //  rcvGif.addItemDecoration(new HorizontalSpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.padding_small)));
//        mEmojiLayoutParams.setRcvGifProperties(rcvGif);
        // gifAdapter = new GifAdapter(gifManager.media);

//        gifAdapter.setOnItemGifClickListener(onItemGifClickListener);
        // rcvGif.setAdapter(gifAdapter);
        etSearch = findViewById(R.id.et_search_gif);
        findViewById(R.id.click_search).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mKeyboardActionListener.onCodeInput(Constants.CODE_ALPHA_FROM_EMOJI_SEARCH, NOT_A_COORDINATE, NOT_A_COORDINATE, false, false);
                resetGif();
            }
        });


    }


    public void resetGif() {
        this.textSearch = "";
        etSearch.setText(textSearch);
    }

    public void showTextNotSupportGif(int s) {
        Timber.e("hachung showTextNotSupportGif:"+s);
        TextView textView = (TextView) findViewById(R.id.txtNotSupportGif);
        textView.setText(s);
        textView.setVisibility(VISIBLE);
        new Handler().postDelayed(() -> textView.setVisibility(INVISIBLE), 1000);
    }

    private final GifAdapter.OnItemGifClickListener onItemGifClickListener = new GifAdapter.OnItemGifClickListener() {
        @Override
        public void onItemGifClick(int position, Media media) {
            mKeyboardActionListener.onGifClick(media);
        }
    };
    private int currentPositionGif = 0;
    private final GifCategoryAdapter.OnItemGifCategoryClickListener onItemGifCategoryClickListener = new GifCategoryAdapter.OnItemGifCategoryClickListener() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onItemGifCategoryClick(int position, String category) {
//            if (App.getInstance().checkConnectivityStatus() == -1) {
//                Toast.makeText(App.getInstance(), R.string.pleaseTurnOnInternet, Toast.LENGTH_SHORT).show();
//            }
            gifCategoryAdapter.setPos(position);
            if (currentPositionGif != position) {
                currentPositionGif = position;
                progressBar.setVisibility(VISIBLE);
                categorySelected = position;
                categoryGif = category;
                if (position == 0) {
                    viewGif.getGifByTrending();
                } else {
                    viewGif.getGifByCategory(category);
                }
//                if (gifManager != null) {
//                    gifManager.media.clear();
//                    gifAdapter.notifyDataSetChanged();
//                    if (gifManager.gphApi != null) {
//                        categorySelected = position;
//                        if (position == 0) {
//                            gifManager.getGiphyTrend();
//                        } else {
//                            gifManager.getGiphyByCategory(position);
//                        }
//                    }
//                }
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        // Add here to the stack trace to nail down the {@link IllegalArgumentException} exception
        // in MotionEvent that sporadically happens.
        // TODO: Remove this override method once the issue has been addressed.
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onTabChanged(final String tabId) {
//        AudioAndHapticFeedbackManager.getInstance().performHapticAndAudioFeedback(Constants.CODE_UNSPECIFIED, this);
        final int categoryId = mEmojiCategory.getCategoryId(tabId);
        setCurrentCategoryId(categoryId, false /* force */);
        updateEmojiCategoryPageIdView();
        checkShowFavourite();
    }

    private void checkShowFavourite() {
        if (mTabHost.getCurrentTab() == 1) {
            if (listEmoji != null && listEmoji.size() == 0) {
                tvNoFavourite.setVisibility(VISIBLE);
                loadAllEmojiFavourite();
            }
            if (rcvEmojiFavourite != null) {
                rcvEmojiFavourite.setVisibility(VISIBLE);
                emojiFavouriteAdapter.changeList(listEmoji);
            }
        } else {
            tvNoFavourite.setVisibility(GONE);
            if (rcvEmojiFavourite != null) {
                rcvEmojiFavourite.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onPageSelected(final int position) {
        final Pair<Integer, Integer> newPos = mEmojiCategory.getCategoryIdAndPageIdFromPagePosition(position);
        setCurrentCategoryId(newPos.first /* categoryId */, false /* force */);
        mEmojiCategory.setCurrentCategoryPageId(newPos.second /* categoryPageId */);
        updateEmojiCategoryPageIdView();
        mCurrentPagerPosition = position;
        changeColorTabHost(/*colorUse*/);
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        // Ignore this message. Only want the actual page selected.
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset,
                               final int positionOffsetPixels) {
        mEmojiPalettesAdapter.onPageScrolled();
        final Pair<Integer, Integer> newPos =
                mEmojiCategory.getCategoryIdAndPageIdFromPagePosition(position);
        final int newCategoryId = newPos.first;
        final int newCategorySize = mEmojiCategory.getCategoryPageSize(newCategoryId);
        final int currentCategoryId = mEmojiCategory.getCurrentCategoryId();
        final int currentCategoryPageId = mEmojiCategory.getCurrentCategoryPageId();
        final int currentCategorySize = mEmojiCategory.getCurrentCategoryPageSize();
        if (newCategoryId == currentCategoryId) {
            mEmojiCategoryPageIndicatorView.setCategoryPageId(
                    newCategorySize, newPos.second, positionOffset);
        } else if (newCategoryId > currentCategoryId) {
            mEmojiCategoryPageIndicatorView.setCategoryPageId(
                    currentCategorySize, currentCategoryPageId, positionOffset);
        } else if (newCategoryId < currentCategoryId) {
            mEmojiCategoryPageIndicatorView.setCategoryPageId(
                    currentCategorySize, currentCategoryPageId, positionOffset - 1);
        }
    }

    /**
     * Called from {@link EmojiPageKeyboardView} through {@link OnTouchListener}
     * interface to handle touch events from View-based elements such as the space bar.
     * Note that this method is used only for observing {@link MotionEvent#ACTION_DOWN} to trigger
     * {@link KeyboardActionListener#onPressKey}. {@link KeyboardActionListener#onReleaseKey} will
     * be covered by {@link #onClick} as long as the event is not canceled.
     */
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        if (v.getId() == R.id.iv_keyboard_text) {
//            if(llGif.getVisibility()==VISIBLE){
//                llGif.setVisibility(GONE);
//            }

            EventBus.getDefault().post(new MessageEvent(Constant.EVENT_SHOW_MENU, null));
        }
        if (event.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        final Object tag = v.getTag();
        if (!(tag instanceof Integer)) {
            return false;
        }
        final int code = (Integer) tag;
        mKeyboardActionListener.onPressKey(
                code, 0 /* repeatCount */, true /* isSinglePointer */);
        // It's important to return false here. Otherwise, {@link #onClick} and touch-down visual
        // feedback stop working.
        return false;
    }

    /**
     * Called from {@link EmojiPageKeyboardView} through {@link OnClickListener}
     * interface to handle non-canceled touch-up events from View-based elements such as the space
     * bar.
     */
    @Override
    public void onClick(View v) {
        final Object tag = v.getTag();
        if (!(tag instanceof Integer)) {
            return;
        }
        final int code = (Integer) tag;
        mKeyboardActionListener.onCodeInput(code, NOT_A_COORDINATE, NOT_A_COORDINATE,
                false /* isKeyRepeat */, true);
        mKeyboardActionListener.onReleaseKey(code, false /* withSliding */);
        EventBus.getDefault().post(new MessageEvent(Constant.EVENT_CANCEL_SEARCH_GIF));
    }

    /**
     * Called from {@link EmojiPageKeyboardView} through
     * {@link com.android.inputmethod.keyboard.emoji.EmojiPageKeyboardView.OnKeyEventListener}
     * interface to handle touch events from non-View-based elements such as Emoji buttons.
     */
    @Override
    public void onPressKey(final Key key) {
        final int code = key.getCode();
        mKeyboardActionListener.onPressKey(code, 0 /* repeatCount */, true /* isSinglePointer */);
    }

    /**
     * Called from {@link EmojiPageKeyboardView} through
     * {@link com.android.inputmethod.keyboard.emoji.EmojiPageKeyboardView.OnKeyEventListener}
     * interface to handle touch events from non-View-based elements such as Emoji buttons.
     */
    @Override
    public void onReleaseKey(final Key key) {
        mEmojiPalettesAdapter.addRecentKey(key);
        mEmojiCategory.saveLastTypedCategoryPage();
        final int code = key.getCode();
        if (code == Constants.CODE_OUTPUT_TEXT) {
            mKeyboardActionListener.onTextEmojiInput(key.getOutputText());
        } else {
            mKeyboardActionListener.onCodeInput(code, NOT_A_COORDINATE, NOT_A_COORDINATE,
                    false /* isKeyRepeat */, true);
        }
        mKeyboardActionListener.onReleaseKey(code, false /* withSliding */);
    }

    public void setHardwareAcceleratedDrawingEnabled(final boolean enabled) {
        if (!enabled) return;
        // TODO: Should use LAYER_TYPE_SOFTWARE when hardware acceleration is off?
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    private static void setupAlphabetKey(final TextView alphabetKey, final String label,
                                         final KeyDrawParams params) {
        alphabetKey.setText(label);
        alphabetKey.setTextColor(params.mFunctionalTextColor);
        alphabetKey.setTextSize(TypedValue.COMPLEX_UNIT_PX, params.mLabelSize);
        alphabetKey.setTypeface(params.mTypeface);
    }

    public void startEmojiPalettes(final String switchToAlphaLabel,
                                   final KeyVisualAttributes keyVisualAttr,
                                   final KeyboardIconsSet iconSet) {
        //final int deleteIconResId = iconSet.getIconResourceId(KeyboardIconsSet.NAME_DELETE_KEY);
        // RGB Edited change icon Delete
        final int deleteIconResId = R.drawable.sym_keyboard_delete_lxx_light;
        //   if (deleteIconResId != 0) {
        //  }
        final int spacebarResId = iconSet.getIconResourceId(KeyboardIconsSet.NAME_SPACE_KEY);
        if (spacebarResId != 0) {
            // TODO: Remove this workaround to place the spacebar icon.
            mSpacebarIcon.setBackgroundResource(spacebarResId);
        }
        final KeyDrawParams params = new KeyDrawParams();
        params.updateParams(mEmojiLayoutParams.getActionBarHeight(), keyVisualAttr);
        mEmojiPager.setAdapter(mEmojiPalettesAdapter);
        mEmojiPager.setCurrentItem(mCurrentPagerPosition);
    }

    public void stopEmojiPalettes() {
        mEmojiPalettesAdapter.releaseCurrentKey(true /* withKeyRegistering */);
        mEmojiPalettesAdapter.flushPendingRecentKeys();
        mEmojiPager.setAdapter(null);
    }

    public void setKeyboardActionListener(final KeyboardActionListener listener) {
        mKeyboardActionListener = listener;
        mDeleteKeyOnTouchListener.setKeyboardActionListener(listener);
    }

    private void updateEmojiCategoryPageIdView() {
        if (mEmojiCategoryPageIndicatorView == null) {
            return;
        }
        mEmojiCategoryPageIndicatorView.setCategoryPageId(
                mEmojiCategory.getCurrentCategoryPageSize(),
                mEmojiCategory.getCurrentCategoryPageId(), 0.0f /* offset */);
    }


    public void setImgBackgroundEmojiPalettes(Bitmap bm, GradientDrawable gradientDrawable, int dominantColor, int color, int colorUse) {
        boolean isShowOverlay = false;
        setBackGroundEmojiPlateView(bm);
        Timber.d("ducdb setImgBackgroundEmojiPalettes " + System.currentTimeMillis());
        if (bm != null) {
            if (!CommonUtil.isColorDark(dominantColor)) {
                isShowOverlay = true;
            }
        } else if (gradientDrawable == null) {
            if (Color.alpha(dominantColor) < 130) {
                isShowOverlay = true;
            }
        }
        this.colorUse = colorUse;
        // colorNotUse = CommonUtil.manipulateColor(colorUse, 0.75f, 155);
        colorNotUse = CommonUtil.manipulateColor(colorUse, 0.6f, 255);
        if (mEmojiCategoryPageIndicatorView != null)
            mEmojiCategoryPageIndicatorView.setColors(colorUse, colorNotUse);
        if (gifCategoryAdapter != null) gifCategoryAdapter.changeColor(colorUse, colorNotUse);
        if (llEmoji != null)
            onEmojiGifShow(llEmoji.getVisibility() == VISIBLE ? Constant.TYPE_EMOJI : Constant.TYPE_GIF);
        if (imgAddSticker != null) imgAddSticker.setColorFilter(colorUse);
        if (txtAddSticker != null) txtAddSticker.setTextColor(colorUse);
        changeColorTabHost(/*colorUse*/);
    }

    /**
     * Quoc Duc update backGround EmojiPlateView
     *
     * @param bitmap
     */
    public void setBackGroundEmojiPlateView(Bitmap bitmap) {
        Timber.d("ducNQ : setBackGroundEmojiPlateView: " + bitmap);
        ThemeModel themeModel = App.getInstance().themeRepository.getCurrentThemeModel();
        if (App.getInstance().getTypeEditing() == 2 && !App.getInstance().checkFirstTimeSetBg) {
            bitmap = null;
            if (themeModel != null && themeModel.getBackground().getBackgroundColor() != null) {// fix not change back ground when go to create theme
                int colorIcon = CommonUtil.hex2decimal(themeModel.getBackground().getBackgroundColor());
                imgOverlay.setBackgroundColor(colorIcon);
            }
        }
        if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE && bitmap != null) {
            radius = App.getInstance().blurKillApp;
            themeModel.setTypeKeyboard(Constants.ID_CATEGORY_WALL);
        }
        if (themeModel != null && themeModel.getTypeKeyboard() != null) {
            switch (themeModel.getTypeKeyboard()) {
                case Constants.ID_CATEGORY_GRADIENT:
                    imgOverlay.setImageBitmap(null);
                    String[] strColorGradientBackground = {Objects.requireNonNull(themeModel.getBackground()).getStartColor(), themeModel.getBackground().getFinishColor()};
                    imgOverlay.setBackground(CommonUtil.getGradientDrawableBackground(strColorGradientBackground));
                    break;
                case Constants.ID_FEATURED:
                case Constants.ID_CATEGORY_WALL:
                    if (bitmap != null) {
                        Timber.d("ducNQgetTypeEditing " + App.getInstance().getTypeEditing());
                        if (App.getInstance().getTypeEditing() == Constant.TYPE_EDIT_CUSTOMIZE) {
                            Timber.d("setBackGroundEmojiPlateView 1: " + radius);
                            imgOverlay.setImageBitmap(CommonUtil.blurBitmap(App.getInstance(), bitmap, radius));
                        } else {
                            radius = 0;
                            App.getInstance().blurBg = 0;
                            if (App.getInstance().themeModel != null && App.getInstance().themeModel.getBackground() != null) {
                                imgOverlay.setImageBitmap(CommonUtil.blurBitmap(App.getInstance(), bitmap, App.getInstance().themeModel.getBackground().getRadiusBlur()));
                            } else imgOverlay.setImageBitmap(bitmap);
                        }
                    }
                    break;
                case Constants.ID_CATEGORY_RGB:
                    if (!Objects.requireNonNull(themeModel.getBackground()).getBackgroundColor().equals("null")) {
                        imgOverlay.setImageBitmap(null);
                        imgOverlay.setBackground(null);
                        imgOverlay.setBackgroundColor(CommonUtil.hex2decimal(themeModel.getBackground().getBackgroundColor()));
                    } else {
                        if (bitmap != null) {
                            imgOverlay.setImageBitmap(bitmap);
                        }
                    }
                    break;
                default:
                    if (Long.parseLong(Objects.requireNonNull(themeModel.getId())) > 3000 && Long.parseLong(themeModel.getId()) < 3010) {
                        if (bitmap != null) {
                            imgOverlay.setImageBitmap(bitmap);
                        }
                    } else {
                        imgOverlay.setImageBitmap(null);
                        int colorIcon = CommonUtil.hex2decimal(themeModel.getBackground().getBackgroundColor());
                        imgOverlay.setBackgroundColor(colorIcon);
                    }
                    break;
            }
        }
    }

    public void changeRadius(Context ctx, int radiusBlur) {
        if (App.getInstance().bitmap != null) {
            radius = radiusBlur;
            imgOverlay.setImageBitmap(CommonUtil.blurBitmap(ctx, App.getInstance().bitmap, radiusBlur));
        }
    }

    public void setOriginalEmojiPlateView() {
        imgOverlay.setImageBitmap(null);
        if (themeModel != null && themeModel.getBackground().getBackgroundColor() != null) {
            int colorIcon = CommonUtil.hex2decimal(themeModel.getBackground().getBackgroundColor());
            imgOverlay.setBackgroundColor(colorIcon);
        }
    }

    private void changeColorTabHost(/*int color*/) {
        if (mTabHost != null && mTabHost.getTabWidget() != null) {
            for (int i = 0; i < mTabHost.getTabWidget().getTabCount(); i++) {
                ImageView tabImageView = (ImageView) mTabHost.getTabWidget().getChildTabViewAt(i);
                tabImageView.setColorFilter(Color.TRANSPARENT);
                if (i == mTabHost.getCurrentTab()) {
                    tabImageView.setColorFilter(colorUse);
                } else {
                    if (App.getInstance().themeModel != null && Objects.equals(App.getInstance().themeModel.getId(), "6010")) {
                        tabImageView.setColorFilter(CommonUtil.hex2decimal(Objects.requireNonNull(Objects.requireNonNull(App.getInstance().themeModel.getKey()).getText()).getTextColor()));
                    } else tabImageView.setColorFilter(colorUse);
                }
            }
        }
    }

    private void setCurrentCategoryId(final int categoryId, final boolean force) {
        final int oldCategoryId = mEmojiCategory.getCurrentCategoryId();
        if (oldCategoryId == categoryId && !force) {
            return;
        }

        if (oldCategoryId == EmojiCategory.ID_RECENTS) {
            // Needs to save pending updates for recent keys when we get out of the recents
            // category because we don't want to move the recent emojis around while the user
            // is in the recents category.
            mEmojiPalettesAdapter.flushPendingRecentKeys();
        }

        mEmojiCategory.setCurrentCategoryId(categoryId);
        final int newTabId = mEmojiCategory.getTabIdFromCategoryId(categoryId);
        final int newCategoryPageId = mEmojiCategory.getPageIdFromCategoryId(categoryId);
        if (force || Objects.requireNonNull(mEmojiCategory.getCategoryIdAndPageIdFromPagePosition(
                mEmojiPager.getCurrentItem())).first != categoryId) {
            mEmojiPager.setCurrentItem(newCategoryPageId, false /* smoothScroll */);
        }
        if (force || mTabHost.getCurrentTab() != newTabId) {
            mTabHost.setCurrentTab(newTabId);
        }
    }

    @Override
    public void clickGif(@NonNull Media media) {
        mKeyboardActionListener.onGifClick(media);
    }

    @Override
    public void showProgressBar(int count) {
        if (typeCurrent != Constant.TYPE_GIF) return;
        if (progressBar != null)
            progressBar.setVisibility(INVISIBLE);
        if (textViewSearchGif != null) {
            if (count == 0) {
                Timber.d("ducNQ : showProgressBar: ");
                textViewSearchGif.setVisibility(VISIBLE);
              /*  new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textViewSearchGif.setVisibility(INVISIBLE);
                    }
                },500);*/
            } else {
                textViewSearchGif.setVisibility(INVISIBLE);
            }
        }

    }

    @Override
    public void downloadGif() {
        ctlDownloadGif.setVisibility(VISIBLE);
    }

    @Override
    public void finishDownloadGif() {
        ctlDownloadGif.setVisibility(INVISIBLE);

    }

    public void showViewGif() {
        etSearch.clearFocus();
        //rcvEmojiKb.setVisibility(INVISIBLE);
        viewGif.setVisibility(VISIBLE);
    }

    private class DeleteKeyOnTouchListener implements OnTouchListener {
        private KeyboardActionListener mKeyboardActionListener = KeyboardActionListener.EMPTY_LISTENER;

        public void setKeyboardActionListener(final KeyboardActionListener listener) {
            mKeyboardActionListener = listener;
        }

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    onTouchDown(v);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    final float x = event.getX();
                    final float y = event.getY();
                    if (x < 0.0f || v.getWidth() < x || y < 0.0f || v.getHeight() < y) {
                        // Stop generating key events once the finger moves away from the view area.
                        onTouchCanceled(v);
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    onTouchUp(v);
                    return true;
            }
            return false;
        }

        private void onTouchDown(final View v) {
//            if (!getVisibleLayoutSearchGif()) {
                mKeyboardActionListener.onPressKey(Constants.CODE_DELETE,
                        0 /* repeatCount */, true /* isSinglePointer */);
                v.setPressed(true /* pressed */);
//            }
        }

        private void onTouchUp(final View v) {
//            if (!getVisibleLayoutSearchGif()) {
                mKeyboardActionListener.onCodeInput(Constants.CODE_DELETE,
                        NOT_A_COORDINATE, NOT_A_COORDINATE, false /* isKeyRepeat */, true);
                mKeyboardActionListener.onReleaseKey(Constants.CODE_DELETE, false /* withSliding */);
                v.setPressed(false /* pressed */);
//            }
        }

        private void onTouchCanceled(final View v) {
            v.setBackgroundColor(Color.TRANSPARENT);
        }


    }

//    public Handler handler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            switch (msg.what) {
//                case GifManager.MSG_REQUEST_CATEGORY_COMPLETE:
//                    gifCategoryAdapter.notifyDataSetChanged();
//                    gifCategoryAdapter.setPos(0);
//                    break;
//
//                case GifManager.MSG_REQUEST_GIF_COMPLETE:
//                    gifAdapter.notifyDataSetChanged();
//                    break;
//
//                default:
//                    break;
//            }
//            return true;
//        }
//    });


    public void setTextSearch(String textSearch) {
        progressBar.setVisibility(VISIBLE);
        if (textViewSearchGif != null) {
            textViewSearchGif.setVisibility(INVISIBLE);
        }
        etSearch.clearFocus();
        this.textSearch = textSearch;
        etSearch.setText(textSearch);
        if (viewGif != null) {
            viewGif.searchGif(textSearch);
        }
        currentPositionGif = -1;
        gifCategoryAdapter.setPos(-1);
//        if (gifManager != null) {
//            gifCategoryAdapter.setPos(-1);
//            gifManager.getGiphySearch(textSearch);
//        }
        if (txtAddSticker != null) {
            txtAddSticker.setVisibility(INVISIBLE);
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        try {
            if (visibility == View.VISIBLE) {
                init();
                initText();
                initMeasure();
                setupView();
                if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);
            } else {
                listEmoji = new ArrayList<>();
                if (EventBus.getDefault().isRegistered(this))
                    EventBus.getDefault().unregister(this);
            }
        } catch (OutOfMemoryError ignored) {
        }
    }

    private void initText(){

        if (tvNoRecentSticker!=null){
            tvNoRecentSticker.setText(context.getString(R.string.no_recent_sticker_yet));
        }
        if (tvNoFavourite!=null){
            tvNoFavourite.setText(context.getString(R.string.add_more_emojis_combos));
        }
        if (txtAddSticker!=null){
            txtAddSticker.setText(context.getString(R.string.add_sticker));
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getKey()) {
            case Constant.EVENT_DATA_SYMBOLS:
                Bundle bundle = event.getBundle();
                if (bundle == null) break;
                changeDataSymbols(bundle.getParcelableArrayList(Constant.DATA_SYMBOLS)
                        , bundle.getParcelableArrayList(Constant.DATA_SYMBOLS_EMOJI), bundle.getParcelableArrayList(Constant.DATA_SYMBOLS_DECORATIVE));
                break;
            case Constant.EVENT_DATA_STICKER_RECENT:
                Bundle bundleRecent = event.getBundle();
                if (bundleRecent != null) {
                    listStickerRecent = bundleRecent.getParcelableArrayList(Constant.DATA_STICKER_RECENT);
                    if (rclTabSticker != null && itemTabStickerAdapter != null && itemTabStickerAdapter.getCurrentPossition() == 0) {
                        convertListStickerRecentToListSticker();
                    }
                }
                break;
            case Constant.EVENT_CHANGE_LAYOUT_EMOJI:
                mEmojiCategory.refreshEmoji();
                break;

        }
    }
}