package com.tapbi.spark.yokey.data.repository;

import static com.tapbi.spark.yokey.util.Constant.FONT_STAMP;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.lifecycle.MutableLiveData;

import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.data.local.entity.ItemFont;
import com.tapbi.spark.yokey.data.local.db.ThemeDB;
import com.tapbi.spark.yokey.data.model.Font;
import com.tapbi.spark.yokey.data.model.MessageEvent;
import com.tapbi.spark.yokey.util.Constant;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class FontRepository {
    public String DEFAULT_USING_FONT_RECENT = Constant.FONT_NORMAL + "," + Constant.FONT_DUST + "," + Constant.FONT_SEPARATION_THIN_RIGHT + "," + Constant.FONT_SEPARATION_THIN + "," + Constant.FONT_TRIANGULAR_RIGHT + "," + Constant.FONT_TRIANGULAR + "," + Constant.FONT_DOT + "," + Constant.FONT_DOT_RIGHT
            + "," + Constant.FONT_SPECIAL + "," + Constant.FONT_ANGULAR;
    public MutableLiveData<ArrayList<ItemFont>> listFontsLiveData = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ItemFont>> listFontsLiveDataAll = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ItemFont>> listFontsLiveDataSans = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ItemFont>> listFontsLiveDataSerif = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ItemFont>> listFontsLiveDataDisplay = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ItemFont>> listFontsLiveDataHand = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ItemFont>> listFontsLiveDataScript = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ItemFont>> listFontsLiveDataTiktok = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ItemFont>> listFontsLiveDataInstagram = new MutableLiveData<>();
    public MutableLiveData<ArrayList<ItemFont>> listFontsLiveDataOther = new MutableLiveData<>();
    public ArrayList<ItemFont> listFontsIsAdd;
    public ArrayList<ItemFont> listAllFont;

    public ArrayList<ItemFont> listFontsTabAll = new ArrayList<>();
    public ArrayList<ItemFont> listFontsSans = new ArrayList<>();
    public ArrayList<ItemFont> listFontsSerif = new ArrayList<>();
    public ArrayList<ItemFont> listFontsDisplay = new ArrayList<>();
    public ArrayList<ItemFont> listFontsHand = new ArrayList<>();
    public ArrayList<ItemFont> listFontsScript = new ArrayList<>();
    public ArrayList<ItemFont> listFontsTiktok = new ArrayList<>();
    public ArrayList<ItemFont> listFontsInstagram = new ArrayList<>();
    public ArrayList<ItemFont> listFontsOther = new ArrayList<>();

    public Font font;

    public String key_Font;
    public CharSequence[] charSequences;
    private SharedPreferences mPrefs;

    @Inject
    public FontRepository() {
        font = new Font();
        listFontsIsAdd = new ArrayList<>();
        listAllFont = new ArrayList<>();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        if (mPrefs.getString(Constant.USING_FONT_RECENT, "").equals(""))
            mPrefs.edit().putString(Constant.USING_FONT_RECENT, DEFAULT_USING_FONT_RECENT).apply();

//        if (!mPrefs.getBoolean(Constant.IS_CHECK_SUPPORT_FONT, false)) {
//            mPrefs.edit().putBoolean(Constant.IS_CHECK_SUPPORT_FONT, true).apply();
//            loadFontDataBase(App.getInstance()).subscribe(new SingleObserver<List<ItemFont>>() {
//                @Override
//                public void onSubscribe(@NonNull Disposable d) {
//
//                }
//
//                @Override
//                public void onSuccess(@NonNull List<ItemFont> itemFonts) {
//                    if (itemFonts.size() > 0) {
//                        Paint paint = new Paint();
//                        for (ItemFont itemFont : itemFonts) {
//                            if (!isSupportFontOnApi(paint, font.getFont(itemFont.getTextFont())[0].toString())) {
//                                ThemeDB.getInstance(App.getInstance()).itemFontDAO().deleteFont(itemFont);
//                            }
//                        }
//                        init();
//                    }
//                }
//
//                @Override
//                public void onError(@NonNull Throwable e) {
//
//                }
//            });
//        } else {
        init();
//        }
    }

    private void init() {
        if (!mPrefs.getBoolean(Constant.CHECK_UPDATE_NEW_FONT_DATA, false)) {
            ThemeDB.getInstance(App.getInstance()).itemFontDAO()
                    .insertFontList(insertNewFont());
            mPrefs.edit().putBoolean(Constant.CHECK_UPDATE_NEW_FONT_DATA, true).apply();
            Timber.d("getBooleaned 1");
        }
        if (!mPrefs.getBoolean(Constant.CHECK_UPDATE_FONT_DATA, false)) {
            ThemeDB.getInstance(App.getInstance()).itemFontDAO().insertFontList(getListFontAddToDB());
            mPrefs.edit().putBoolean(Constant.CHECK_UPDATE_FONT_DATA, true).apply();
            Timber.d("getBooleaned 2");
        } else {
            if (!mPrefs.getBoolean(Constant.FIX_BUG_DUPLICATED_FONT_DOUBLE, false) && ThemeDB.getInstance(App.getInstance()).itemFontDAO() != null) {
                try {
                    List<ItemFont> itemFontList = ThemeDB.getInstance(App.getInstance()).itemFontDAO().getFontByTextFont(Constant.FONT_DOUBLE);
                    if (itemFontList != null && itemFontList.size() == 2) {
                        ThemeDB.getInstance(App.getInstance()).itemFontDAO().updateTextFont(itemFontList.get(1).getId(), Constant.FONT_BRACE);
                    }
                    mPrefs.edit().putBoolean(Constant.FIX_BUG_DUPLICATED_FONT_DOUBLE, true).apply();
                } catch (IllegalStateException ignored) {
                }
            }
        }
        if (!mPrefs.getBoolean(Constant.FIX_BUG_DUPLICATED_FONT_DOUBLE_V2, false) && ThemeDB.getInstance(App.getInstance()).itemFontDAO() != null) {
            boolean checkFont = ThemeDB.getInstance(App.getInstance()).itemFontDAO().getFontTextFont(Constant.FONT_TYPE_WRITE);
            boolean checkFontStamp = ThemeDB.getInstance(App.getInstance()).itemFontDAO().getFontTextFont(FONT_STAMP);
            if (checkFont) {
                ThemeDB.getInstance(App.getInstance()).itemFontDAO().deleteItem(Constant.FONT_TYPE_WRITE);
            }
            if (checkFontStamp) {
                ThemeDB.getInstance(App.getInstance()).itemFontDAO().deleteItem(FONT_STAMP);
            }
            mPrefs.edit().putBoolean(Constant.FIX_BUG_DUPLICATED_FONT_DOUBLE_V2, true).apply();
        }
        updateCurrentFont();
        loadListFontIsAdd();
    }

    public void updateCurrentFont() {
        key_Font = mPrefs.getString(Constant.USING_FONT, Constant.FONT_NORMAL);
        if (mPrefs.getString(Constant.USING_FONT, Constant.FONT_NORMAL).equals(FONT_STAMP)) {
            key_Font = Constant.FONT_NORMAL;
            mPrefs.edit().putString(Constant.USING_FONT, Constant.FONT_NORMAL).apply();
        }
        charSequences = font.getFont(key_Font);
    }

    private void addFontToList(String key_Font) {
        if (listFontsIsAdd != null) {
            listFontsIsAdd.add(new ItemFont(listAllFont.size(), key_Font, 1, "file:///android_asset/background/3.png", "Stay hungry Stay foolish", "Sans serif", false, true));
        }
    }

    public void loadListFontIsAdd() {
        loadAllFont(App.getInstance(), false);
        String fontRecent = mPrefs.getString(Constant.USING_FONT_RECENT, Constant.FONT_NORMAL);
        Timber.d("ducNQ : loadListFontIsAdd: " + fontRecent);
        String[] split = fontRecent.split(",");
        StringBuilder fontString = new StringBuilder();
        listFontsIsAdd.clear();
        fontString.append(Constant.FONT_NORMAL);
        addFontToList(Constant.FONT_NORMAL);
        if (!key_Font.equals(Constant.FONT_NORMAL) && !key_Font.equals(FONT_STAMP)) {
            fontString.append(",").append(key_Font);
            addFontToList(key_Font);
        }

        if (split.length > 0) {
            for (String s : split) {
                if (!s.equals(Constant.FONT_NORMAL) && !s.equals(key_Font) && !s.equals(FONT_STAMP)) {
                    if (listFontsIsAdd.size() < 10) {
                        fontString.append(",").append(s);
                        addFontToList(s);
                    } else break;
                }
            }
        }
        mPrefs.edit().putString(Constant.USING_FONT_RECENT, fontString.toString()).apply();
        EventBus.getDefault().post(new MessageEvent(Constant.EVENT_CHANGE_LIST_FONT));
        if (listFontsIsAdd.size() == 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadListFontIsAdd();
                }
            }, 1000);
        }
    }

    public void loadAllFont(Context context, boolean isPost) {
        loadFontDataBase(context).subscribe(new SingleObserver<List<ItemFont>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull List<ItemFont> itemFontList) {
                if (isPost) listFontsLiveData.postValue((ArrayList<ItemFont>) itemFontList);

                if (itemFontList.size() > 0) {
                    //  App.getInstance().nextIdNew = itemFontList.size()+1;
                    listAllFont.clear();
                    //listAllFont.addAll(new ArrayList<>(itemFontList));
                    listAllFont.addAll(itemFontList);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });
    }

    public Single<Boolean> updateFontToDB(Context context, ItemFont itemFont) {
        return Single.fromCallable(() -> {
                    ThemeDB.getInstance(context).itemFontDAO().updateIsAdd(itemFont.getId(), 1);
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    private void clearData() {
        listFontsTabAll.clear();
        listFontsSans.clear();
        listFontsSerif.clear();
        listFontsDisplay.clear();
        listFontsHand.clear();
        listFontsSans.clear();
        listFontsScript.clear();
        listFontsTiktok.clear();
        listFontsInstagram.clear();
        listFontsOther.clear();
    }

    public Single<List<ItemFont>> loadFontDataBase(Context context) {
        return Single.fromCallable(() -> {
                            ArrayList<ItemFont> itemFontArrayList = (ArrayList<ItemFont>) ThemeDB.getInstance(context).itemFontDAO().getAllFont();
                            clearData();
                            listFontsTabAll.addAll(itemFontArrayList);
                            for (int i = 0; i < itemFontArrayList.size(); i++) {
                                switch (itemFontArrayList.get(i).filterCategories) {
                                    case Constant.KEY_TAB_SANS:
                                        listFontsSans.add(itemFontArrayList.get(i));
                                        break;
                                    case Constant.KEY_TAB_SERIF:
                                        listFontsSerif.add(itemFontArrayList.get(i));
                                        break;
                                    case Constant.KEY_TAB_DISPLAY:
                                        listFontsDisplay.add(itemFontArrayList.get(i));
                                        break;
                                    case Constant.KEY_TAB_HAND:
                                        listFontsHand.add(itemFontArrayList.get(i));
                                        break;
                                    case Constant.KEY_TAB_SCRIPT:
                                        listFontsScript.add(itemFontArrayList.get(i));
                                        break;
                                    case Constant.KEY_TAB_TIKTOK:
                                        listFontsTiktok.add(itemFontArrayList.get(i));
                                        break;
                                    case Constant.KEY_TAB_INS:
                                        listFontsInstagram.add(itemFontArrayList.get(i));
                                        break;
                                    case Constant.KEY_TAB_OTHER:
                                        listFontsOther.add(itemFontArrayList.get(i));
                                        break;
                                }
                            }
                            return ThemeDB.getInstance(context).itemFontDAO().getAllFont();
                        }
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<ItemFont> loadFontDataBaseById(Context context, int id) {
        return Single.fromCallable(() -> {
                            return ThemeDB.getInstance(context).itemFontDAO().fetchOneFontByIdFont(id);
                        }
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void loadFontByCategoriThread(ArrayList<ItemFont> itemFonts, String key) {
        Single.fromCallable(() -> loadListFontByCategori(itemFonts, key)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new SingleObserver<ArrayList<ItemFont>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull ArrayList<ItemFont> itemFonts) {
                        switch (key) {
                            case Constant.KEY_TAB_ALL:
                                listFontsLiveDataAll.postValue(itemFonts);
                                break;
                            case Constant.KEY_TAB_SANS:
                                listFontsLiveDataSans.postValue(itemFonts);
                                break;
                            case Constant.KEY_TAB_SERIF:
                                listFontsLiveDataSerif.postValue(itemFonts);
                                break;
                            case Constant.KEY_TAB_DISPLAY:
                                listFontsLiveDataDisplay.postValue(itemFonts);
                                break;
                            case Constant.KEY_TAB_HAND:
                                listFontsLiveDataHand.postValue(itemFonts);
                                break;
                            case Constant.KEY_TAB_SCRIPT:
                                listFontsLiveDataScript.postValue(itemFonts);
                                break;
                            case Constant.KEY_TAB_TIKTOK:
                                listFontsLiveDataTiktok.postValue(itemFonts);
                                break;
                            case Constant.KEY_TAB_INS:
                                listFontsLiveDataInstagram.postValue(itemFonts);
                                break;
                            case Constant.KEY_TAB_OTHER:
                                listFontsLiveDataOther.postValue(itemFonts);
                                break;
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }


    private ArrayList<ItemFont> loadListFontByCategori(ArrayList<ItemFont> itemFonts, String key) {
        ArrayList<ItemFont> list = new ArrayList<>();
        if (itemFonts.size() > 0) {
            if (key.equals(Constant.KEY_TAB_ALL)) return itemFonts;
            for (ItemFont itemFont : itemFonts) {
                assert itemFont.filterCategories != null;
                if (itemFont.filterCategories.equals(key)) {
                    list.add(itemFont);
                }
            }
        }
        return list;
    }

    public ArrayList<ItemFont> getListFontAddToDB() {
        mPrefs.edit().putBoolean(Constant.IS_CHECK_SUPPORT_FONT, true).apply();
        int nextId = 0;
        Paint paint = new Paint();
        ArrayList<ItemFont> listFontsAdd = new ArrayList<>();
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ARROWS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ARROWS, 0, "file:///android_asset/background/1.png", "Stay hungry Stay foolish", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BIRDS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BIRDS, 0, "file:///android_asset/background/2.png", "Dream without fear Love without limits", "Tik Tok", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BUBBLES)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BUBBLES, 0, "file:///android_asset/background/3.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_NORMAL)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_NORMAL, 1, "file:///android_asset/background/3.png", "Stay hungry Stay foolish", "Sans serif", false, true));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_CIRCLES_FILLED)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_CIRCLES_FILLED, 0, "file:///android_asset/background/4.png",
//                    "Stay hungry Stay foolish", "Tik Tok", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_CIRCLES_OUTLINE)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_CIRCLES_OUTLINE, 0,
//                    "file:///android_asset/background/5.png", "Dream without fear Love without limits", "Display", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_COMIC)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_COMIC, 0, "file:///android_asset/background/6.png",
//                    "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_CLOUDS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_CLOUDS, 0, "file:///android_asset/background/7.png", "Stay hungry Stay foolish", "Display", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_EMOJI1)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_EMOJI1, 0, "file:///android_asset/background/8.png",
//                    "Dream without fear Love without limits", "Display", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_EMOJI2)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_EMOJI2, 0, "file:///android_asset/background/9.png",
//                    "Every day is a second chance", "Display", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_EMOJI4)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_EMOJI4, 0,
//                    "file:///android_asset/background/10.png", "Stay hungry Stay foolish", "Display", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HAPPY)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HAPPY, 0, "file:///android_asset/background/1.png",
//                    "Dream without fear Love without limits", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_MANGA)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_MANGA, 0, "file:///android_asset/background/2.png", "Every day is a second chance",
                    "Handwritten", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_RAY)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_RAY, 0, "file:///android_asset/background/4.png", "Dream without fear Love without limits",
//                    "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_RUNS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_RUNS, 0, "file:///android_asset/background/5.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SAD)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SAD, 0, "file:///android_asset/background/6.png", "Stay hungry Stay foolish", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SKY_LINE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SKY_LINE, 0, "file:///android_asset/background/7.png",
                    "Dream without fear Love without limits", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SLASH)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SLASH, 0, "file:///android_asset/background/8.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SQUARE_DASHED)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SQUARE_DASHED, 0, "file:///android_asset/background/9.png", "Stay hungry Stay foolish", "Script", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SQUARES_FILLED)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SQUARES_FILLED, 0, "file:///android_asset/background/10.png",
//                    "Dream without fear Love without limits", "Script", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SQUARES_OUTLINE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SQUARES_OUTLINE, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Script", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_STICKE_THROUGH)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_STICKE_THROUGH, 0, "file:///android_asset/background/1.png", "Stay hungry Stay foolish", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_STINKY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_STINKY, 0, "file:///android_asset/background/2.png", "Dream without fear Love without limits", "Tik Tok", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_STOP)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_STOP, 0, "file:///android_asset/background/3.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_TINY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_TINY, 0, "file:///android_asset/background/4.png", "Stay hungry Stay foolish", "Sans serif", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_UNDERLINE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_UNDERLINE, 0, "file:///android_asset/background/5.png", "Dream without fear Love without limits", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_UPSIDE_DOWN)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_UPSIDE_DOWN, 0, "file:///android_asset/background/6.png", "Every day is a second chance", "Sans serif", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ANCIENT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ANCIENT, 0, "file:///android_asset/background/7.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_NIGMATIC)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_NIGMATIC, 0, "file:///android_asset/background/8.png", "Every day is a second chance", "Handwritten", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HIGH_LIGHTS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HIGH_LIGHTS, 1, "file:///android_asset/background/10.png", "Every day is a second chance", "Display", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_MYTHOLOGY)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_MYTHOLOGY, 0, "file:///android_asset/background/1.png",
//                    "Every day is a second chance", "Handwritten", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SHALASY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SHALASY, 1, "file:///android_asset/background/2.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_RAILS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_RAILS, 0, "file:///android_asset/background/3.png", "Every day is a second chance", "Handwritten", true, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_COMIC_FUN)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_COMIC_FUN, 0, "file:///android_asset/background/4.png",
//                    "Every day is a second chance", "Handwritten", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_DOTIFY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_DOTIFY, 1, "file:///android_asset/background/5.png",
                    "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_WIDE_SPACE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_WIDE_SPACE, 0, "file:///android_asset/background/6.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_RUSCRIPT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_RUSCRIPT, 1, "file:///android_asset/background/7.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_RSUMNEZ)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_RSUMNEZ, 0, "file:///android_asset/background/8.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_STRIKE_THROUGH)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_STRIKE_THROUGH, 1, "file:///android_asset/background/9.png", "Every day is a second chance", "Display", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BRALMY)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BRALMY, 0, "file:///android_asset/background/10.png",
//                    "Every day is a second chance", "Handwritten", true, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_MODER_NOPHICS)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_MODER_NOPHICS, 0,
//                    "file:///android_asset/background/1.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_RETRO_TYPE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_RETRO_TYPE, 1, "file:///android_asset/background/1.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_TINY_WINGS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_TINY_WINGS, 1, "file:///android_asset/background/2.png", "Every day is a second chance", "Display", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_GOLDY)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_GOLDY, 0, "file:///android_asset/background/3.png",
//                    "Every day is a second chance", "Handwritten", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HZSOA)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HZSOA, 0, "file:///android_asset/background/4.png", "Every day is a second chance", "Serif", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_NOT_CHIFY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_NOT_CHIFY, 1, "file:///android_asset/background/5.png", "Every day is a second chance", "Serif", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HAMP_SHIRE)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HAMP_SHIRE, 1,
//                    "file:///android_asset/background/6.png", "Every day is a second chance", "Tik Tok", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SOULURGE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SOULURGE, 0, "file:///android_asset/background/7.png", "Every day is a second chance", "Instagram", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_RUFF_ROAD)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_RUFF_ROAD, 0, "file:///android_asset/background/8.png", "Every day is a second chance", "Instagram", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BRACKETS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BRACKETS, 1, "file:///android_asset/background/9.png", "Every day is a second chance", "Instagram", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SUNSHINE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SUNSHINE, 1, "file:///android_asset/background/10.png", "Every day is a second chance", "Instagram", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_EMPIRE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_EMPIRE, 1, "file:///android_asset/background/1.png", "Every day is a second chance", "Tik Tok", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_DEMONS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_DEMONS, 0, "file:///android_asset/background/2.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_UNDER_COVER)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_UNDER_COVER, 0, "file:///android_asset/background/3.png", "Every day is a second chance", "Other", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_GO_LEFT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_GO_LEFT, 1, "file:///android_asset/background/4.png", "Every day is a second chance", "Other", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_GO_RIGHT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_GO_RIGHT, 1, "file:///android_asset/background/5.png", "Every day is a second chance", "Tik Tok", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_POP_STAR)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_POP_STAR, 0, "file:///android_asset/background/6.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SHINY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SHINY, 1, "file:///android_asset/background/7.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SEASHORE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SEASHORE, 0, "file:///android_asset/background/8.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_TINY_MATE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_TINY_MATE, 1, "file:///android_asset/background/9.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_PONY_TAIL)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_PONY_TAIL, 0, "file:///android_asset/background/10.png", "Every day is a second chance", "Tik Tok", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_WHEEL)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_WHEEL, 0, "file:///android_asset/background/9.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_POSTER)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_POSTER, 0, "file:///android_asset/background/10.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_UPDERLINE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_UPDERLINE, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Other", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_UPPERLINE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_UPPERLINE, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Other", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HORROR_MUSIC)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HORROR_MUSIC, 1, "file:///android_asset/background/3.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_CLOUDY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_CLOUDY, 0, "file:///android_asset/background/4.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_MANIAC)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_MANIAC, 0, "file:///android_asset/background/5.png", "Every day is a second chance", "Handwritten", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_METHODOLOGY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_METHODOLOGY, 0, "file:///android_asset/background/6.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_FANTASY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_FANTASY, 0, "file:///android_asset/background/7.png", "Every day is a second chance", "Display", true, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_EPICURIOUS)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_EPICURIOUS, 0, "file:///android_asset/background/8.png",
//                    "Every day is a second chance", "Handwritten", true, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_MAGICAL)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_MAGICAL, 0, "file:///android_asset/background/9.png",
//                    "Every day is a second chance", "Instagram", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_FAIRY_TALES)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_FAIRY_TALES, 0, "file:///android_asset/background/10.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ALAFABIA)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ALAFABIA, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_AVENGER)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_AVENGER, 0, "file:///android_asset/background/2.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_WICHOLOGY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_WICHOLOGY, 0, "file:///android_asset/background/3.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_CURLS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_CURLS, 0, "file:///android_asset/background/4.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SPARKLE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SPARKLE, 0, "file:///android_asset/background/5.png", "Every day is a second chance", "Other", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_UPSIDE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_UPSIDE, 0, "file:///android_asset/background/6.png", "Every day is a second chance", "Other", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BLACK_CHODE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BLACK_CHODE, 0, "file:///android_asset/background/7.png", "Every day is a second chance", "Other", true, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HUNGARIAN)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HUNGARIAN, 0,
//                    "file:///android_asset/background/8.png", "Every day is a second chance", "Other", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_WAY_COOL)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_WAY_COOL, 0, "file:///android_asset/background/9.png", "Every day is a second chance", "Tik Tok", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SA_MUENZ)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SA_MUENZ, 0, "file:///android_asset/background/10.png", "Every day is a second chance", "Other", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_RAM_TRACK)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_RAM_TRACK, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Other", false, false));
        //  if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_STAMP)[0].toString()))
        //   listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_STAMP, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Script", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_PEE_WEE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_PEE_WEE, 0, "file:///android_asset/background/2.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_IM_NINJA)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_IM_NINJA, 0, "file:///android_asset/background/3.png", "Every day is a second chance", "Handwritten", true, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BLUE_EYES)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BLUE_EYES, 0, "file:///android_asset/background/4.png",
//                    "Every day is a second chance", "Script", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_MON_TEY)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_MON_TEY, 0, "file:///android_asset/background/5.png",
//                    "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SWORD_LINER)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SWORD_LINER, 0, "file:///android_asset/background/6.png", "Every day is a second chance", "Instagram", true, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_THIS_HULA)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_THIS_HULA, 0, "file:///android_asset/background/7.png",
//                    "Every day is a second chance", "Tik Tok", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_FLIPPER)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_FLIPPER, 0, "file:///android_asset/background/8.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_INFINITY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_INFINITY, 0, "file:///android_asset/background/9.png", "Every day is a second chance", "Display", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_GIORGIO_LOGY)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_GIORGIO_LOGY, 0, "file:///android_asset/background/10.png",
//                    "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_KUNG_FU)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_KUNG_FU, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HAPPY_FACE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HAPPY_FACE, 0, "file:///android_asset/background/2.png", "Every day is a second chance", "Display", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ROMTNUM)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ROMTNUM, 0, "file:///android_asset/background/3.png",
//                    "Every day is a second chance", "Tik Tok", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SANS_SKRIT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SANS_SKRIT, 0, "file:///android_asset/background/4.png", "Every day is a second chance", "Other", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_DOUBLE_LINE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_DOUBLE_LINE, 0, "file:///android_asset/background/5.png", "Every day is a second chance", "Other", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ANGER)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ANGER, 0, "file:///android_asset/background/6.png", "Every day is a second chance", "Display", false, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_MOGLI)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_MOGLI, 0,
//                    "file:///android_asset/background/7.png", "Every day is a second chance", "Other", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_DRAGON)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_DRAGON, 0, "file:///android_asset/background/8.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_TINY_CAPS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_TINY_CAPS, 0, "file:///android_asset/background/9.png", "Every day is a second chance", "Display", true, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SAMBA_WAYS)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SAMBA_WAYS, 0,
//                    "file:///android_asset/background/10.png", "Every day is a second chance", "Handwritten", true, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_JAKAS)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_JAKAS, 0,
//                    "file:///android_asset/background/1.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_NWOD_EDIT_PU)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_NWOD_EDIT_PU, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Tik Tok", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ROUND_STAMP)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ROUND_STAMP, 0, "file:///android_asset/background/2.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HIGHLIGHT_ME)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HIGHLIGHT_ME, 0, "file:///android_asset/background/3.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_QUESTION)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_QUESTION, 0, "file:///android_asset/background/4.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BRIDGE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BRIDGE, 0, "file:///android_asset/background/5.png", "Every day is a second chance", "Instagram", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HOTSPOT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HOTSPOT, 0, "file:///android_asset/background/6.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_FROZEN)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_FROZEN, 0, "file:///android_asset/background/7.png", "Every day is a second chance", "Tik Tok", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BUDDHA)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BUDDHA, 0, "file:///android_asset/background/8.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_PERSHIAN)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_PERSHIAN, 0, "file:///android_asset/background/9.png", "Every day is a second chance", "Display", true, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_EMPIRE_AGC)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_EMPIRE_AGC, 0,
//                    "file:///android_asset/background/10.png", "Every day is a second chance", "Handwritten", true, false));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SWAGO_LOGY)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SWAGO_LOGY, 0, "file:///android_asset/background/1.png",
//                    "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_RUSSIAN)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_RUSSIAN, 0, "file:///android_asset/background/6.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_RUNES)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_RUNES, 0, "file:///android_asset/background/7.png", "Every day is a second chance", "Handwritten", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_DOTS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_DOTS, 0, "file:///android_asset/background/8.png", "Every day is a second chance", "Display", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_STROKED)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_STROKED, 0, "file:///android_asset/background/9.png", "Every day is a second chance", "Display", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BOXIFY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BOXIFY, 0, "file:///android_asset/background/10.png", "Every day is a second chance", "Script", true, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BRAILLE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BRAILLE, 1, "file:///android_asset/background/1.png", "Every day is a second chance", "Handwritten", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HAT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HAT, 1, "file:///android_asset/background/2.png", "Every day is a second chance", "Display", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_TILDE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_TILDE, 1, "file:///android_asset/background/3.png", "Every day is a second chance", "Handwritten", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_UNDERLINE_2)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_UNDERLINE_2, 1, "file:///android_asset/background/4.png", "Every day is a second chance", "Serif", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_FRAME)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_FRAME, 1, "file:///android_asset/background/5.png", "Every day is a second chance", "Serif", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_IN_LOVE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_IN_LOVE, 1, "file:///android_asset/background/7.png", "Every day is a second chance", "Instagram", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_LINES)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_LINES, 1, "file:///android_asset/background/8.png", "Every day is a second chance", "Instagram", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SHARDS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SHARDS, 1, "file:///android_asset/background/9.png", "Every day is a second chance", "Instagram", false, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_DOUBLE_CURLY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_DOUBLE_CURLY, 1, "file:///android_asset/background/10.png", "Every day is a second chance", "Instagram", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_THIN_A)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_THIN_A, 1, "file:///android_asset/background/1.png", "Every day is a second chance", "Tik Tok", false, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ANGUI)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ANGUI, 1, "file:///android_asset/background/2.png", "Every day is a second chance", "Display", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_DOUBLE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_DOUBLE, 1, "file:///android_asset/background/3.png", "Every day is a second chance", "Other", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BRACE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BRACE, 1, "file:///android_asset/background/4.png", "Every day is a second chance", "Other", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SEPARATION)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SEPARATION, 1, "file:///android_asset/background/5.png", "Every day is a second chance", "Tik Tok", false, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SEPARATION_SQUIGGLY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SEPARATION_SQUIGGLY, 1, "file:///android_asset/background/6.png", "Every day is a second chance", "Display", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_PIPE_SUP_RIGHT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_PIPE_SUP_RIGHT, 1, "file:///android_asset/background/7.png", "Every day is a second chance", "Display", false, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_PIPE_SUP_LEFT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_PIPE_SUP_LEFT, 1, "file:///android_asset/background/8.png", "Every day is a second chance", "Display", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_THIN_DOWN_LEFT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_THIN_DOWN_LEFT, 1, "file:///android_asset/background/9.png", "Every day is a second chance", "Display", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_THIN_DOWN_RIGHT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_THIN_DOWN_RIGHT, 1, "file:///android_asset/background/10.png", "Every day is a second chance", "Tik Tok", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_THIN_UP_LEFT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_THIN_UP_LEFT, 1, "file:///android_asset/background/9.png", "Every day is a second chance", "Display", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_THIN_UP_RIGHT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_THIN_UP_RIGHT, 1, "file:///android_asset/background/10.png", "Every day is a second chance", "Display", false, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ROUND)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ROUND, 1, "file:///android_asset/background/1.png", "Every day is a second chance", "Other", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_LONG)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_LONG, 1, "file:///android_asset/background/1.png", "Every day is a second chance", "Other", true, true));
//        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_STARS)[0].toString()))
//            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_STARS, 1,
//                    "file:///android_asset/background/3.png", "Every day is a second chance", "Display", false, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ANGULAR)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ANGULAR, 1, "file:///android_asset/background/4.png", "Every day is a second chance", "Display", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SPECIAL)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SPECIAL, 1, "file:///android_asset/background/5.png", "Every day is a second chance", "Handwritten", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_DOT_RIGHT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_DOT_RIGHT, 1, "file:///android_asset/background/6.png", "Every day is a second chance", "Display", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_DOT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_DOT, 1, "file:///android_asset/background/8.png", "Every day is a second chance", "Handwritten", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_TRIANGULAR)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_TRIANGULAR, 1, "file:///android_asset/background/9.png", "Every day is a second chance", "Instagram", false, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_TRIANGULAR_RIGHT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_TRIANGULAR_RIGHT, 1, "file:///android_asset/background/10.png", "Every day is a second chance", "Handwritten", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SEPARATION_THIN)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SEPARATION_THIN, 1, "file:///android_asset/background/2.png", "Every day is a second chance", "Display", true, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SEPARATION_THIN_RIGHT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SEPARATION_THIN_RIGHT, 1, "file:///android_asset/background/4.png", "Every day is a second chance", "Display", false, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_DUST)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_DUST, 1, "file:///android_asset/background/5.png", "Every day is a second chance", "Other", false, true));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_EMOJI3)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_EMOJI3, 0, "file:///android_asset/background/3.png", "Stay hungry Stay foolish", "Display", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_GOTHIC)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_GOTHIC, 0, "file:///android_asset/background/4.png", "Dream without fear Love without limits", "Script", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_GOTHIC_BOLD)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_GOTHIC_BOLD, 0, "file:///android_asset/background/5.png", "Every day is a second chance", "Script", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_OUTLINE)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_OUTLINE, 0, "file:///android_asset/background/1.png", "Stay hungry Stay foolish", "Handwritten", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SANS)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SANS, 0, "file:///android_asset/background/2.png", "Dream without fear Love without limits", "Sans serif", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SANS_BOLD)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SANS_BOLD, 0, "file:///android_asset/background/3.png", "Every day is a second chance", "Sans serif", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SANS_BOLD_ITALIC)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SANS_BOLD_ITALIC, 0, "file:///android_asset/background/4.png", "Stay hungry Stay foolish", "Sans serif", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SANS_ITALIC)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SANS_ITALIC, 0, "file:///android_asset/background/5.png", "Dream without fear Love without limits", "Sans serif", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SCRIPT)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SCRIPT, 0, "file:///android_asset/background/6.png", "Stay hungry Stay foolish", "Script", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SCRIPT_BOLD)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SCRIPT_BOLD, 0, "file:///android_asset/background/7.png", "Dream without fear Love without limits", "Script", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SERIF_BOLD)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SERIF_BOLD, 0, "file:///android_asset/background/8.png", "Every day is a second chance", "Serif", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SERIF_BOLD_ITALIC)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SERIF_BOLD_ITALIC, 0, "file:///android_asset/background/9.png", "Stay hungry Stay foolish", "Sans serif", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SERIF_ITALIC)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SERIF_ITALIC, 0, "file:///android_asset/background/10.png", "Dream without fear Love without limits", "Sans serif", false, false));
            //  if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_TYPE_WRITE)[0].toString()))
            //     listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_TYPE_WRITE, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Serif", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_TYPEWRITER)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_TYPEWRITER, 0, "file:///android_asset/background/2.png", "Every day is a second chance", "Handwritten", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_DOUBLER_TRUCK)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_DOUBLER_TRUCK, 0, "file:///android_asset/background/3.png", "Every day is a second chance", "Display", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_FRACTURE)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_FRACTURE, 0, "file:///android_asset/background/4.png", "Every day is a second chance", "Display", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_FRACTURE_BOLD)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_FRACTURE_BOLD, 0, "file:///android_asset/background/5.png", "Every day is a second chance", "Display", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_CIRCLES_FILLED)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_CIRCLES_FILLED, 0, "file:///android_asset/background/4.png",
                        "Stay hungry Stay foolish", "Tik Tok", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_CIRCLES_OUTLINE)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_CIRCLES_OUTLINE, 0,
                        "file:///android_asset/background/5.png", "Dream without fear Love without limits", "Display", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_COMIC)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_COMIC, 0, "file:///android_asset/background/6.png",
                        "Every day is a second chance", "Handwritten", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_EMOJI1)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_EMOJI1, 0, "file:///android_asset/background/8.png",
                        "Dream without fear Love without limits", "Display", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_EMOJI2)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_EMOJI2, 0, "file:///android_asset/background/9.png",
                        "Every day is a second chance", "Display", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_RAY)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_RAY, 0, "file:///android_asset/background/4.png", "Dream without fear Love without limits",
                        "Display", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HAPPY)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HAPPY, 0, "file:///android_asset/background/1.png",
                        "Dream without fear Love without limits", "Display", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SQUARES_FILLED)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SQUARES_FILLED, 0, "file:///android_asset/background/10.png",
                        "Dream without fear Love without limits", "Script", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_MYTHOLOGY)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_MYTHOLOGY, 0, "file:///android_asset/background/1.png",
                        "Every day is a second chance", "Handwritten", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BRALMY)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BRALMY, 0, "file:///android_asset/background/10.png",
                        "Every day is a second chance", "Handwritten", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_COMIC_FUN)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_COMIC_FUN, 0, "file:///android_asset/background/4.png",
                        "Every day is a second chance", "Handwritten", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_GOLDY)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_GOLDY, 0, "file:///android_asset/background/3.png",
                        "Every day is a second chance", "Handwritten", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_MODER_NOPHICS)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_MODER_NOPHICS, 0,
                        "file:///android_asset/background/1.png", "Every day is a second chance", "Display", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_EPICURIOUS)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_EPICURIOUS, 0, "file:///android_asset/background/8.png",
                        "Every day is a second chance", "Handwritten", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_MAGICAL)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_MAGICAL, 0, "file:///android_asset/background/9.png",
                        "Every day is a second chance", "Instagram", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HUNGARIAN)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HUNGARIAN, 0,
                        "file:///android_asset/background/8.png", "Every day is a second chance", "Other", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HAMP_SHIRE)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HAMP_SHIRE, 1,
                        "file:///android_asset/background/6.png", "Every day is a second chance", "Tik Tok", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BLUE_EYES)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BLUE_EYES, 0, "file:///android_asset/background/4.png",
                        "Every day is a second chance", "Script", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_MON_TEY)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_MON_TEY, 0, "file:///android_asset/background/5.png",
                        "Every day is a second chance", "Display", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_THIS_HULA)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_THIS_HULA, 0, "file:///android_asset/background/7.png",
                        "Every day is a second chance", "Tik Tok", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ROMTNUM)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ROMTNUM, 0, "file:///android_asset/background/3.png",
                        "Every day is a second chance", "Tik Tok", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_GIORGIO_LOGY)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_GIORGIO_LOGY, 0, "file:///android_asset/background/10.png",
                        "Every day is a second chance", "Display", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_MOGLI)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_MOGLI, 0,
                        "file:///android_asset/background/7.png", "Every day is a second chance", "Other", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_JAKAS)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_JAKAS, 0,
                        "file:///android_asset/background/1.png", "Every day is a second chance", "Display", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_EMPIRE_AGC)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_EMPIRE_AGC, 0,
                        "file:///android_asset/background/10.png", "Every day is a second chance", "Handwritten", true, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SWAGO_LOGY)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SWAGO_LOGY, 0, "file:///android_asset/background/1.png",
                        "Every day is a second chance", "Handwritten", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_STARS)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_STARS, 1,
                        "file:///android_asset/background/3.png", "Every day is a second chance", "Display", false, true));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_EMOJI4)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_EMOJI4, 0,
                        "file:///android_asset/background/10.png", "Stay hungry Stay foolish", "Display", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SAMBA_WAYS)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SAMBA_WAYS, 0,
                        "file:///android_asset/background/10.png", "Every day is a second chance", "Handwritten", true, false));
        }
        //App.getInstance().nextIdNew = nextId;
        return listFontsAdd;
    }

    public MutableLiveData<ArrayList<ItemFont>> getLiveCategory(String key) {
        switch (key) {
            case Constant.KEY_TAB_ALL:
                return listFontsLiveDataAll;
            case Constant.KEY_TAB_SANS:
                return listFontsLiveDataSans;
            case Constant.KEY_TAB_SERIF:
                return listFontsLiveDataSerif;
            case Constant.KEY_TAB_DISPLAY:
                return listFontsLiveDataDisplay;
            case Constant.KEY_TAB_HAND:
                return listFontsLiveDataHand;
            case Constant.KEY_TAB_SCRIPT:
                return listFontsLiveDataScript;
            case Constant.KEY_TAB_TIKTOK:
                return listFontsLiveDataTiktok;
            case Constant.KEY_TAB_INS:
                return listFontsLiveDataInstagram;
            case Constant.KEY_TAB_OTHER:
                return listFontsLiveDataOther;
            default:
                return listFontsLiveData;
        }
    }

    public ArrayList<ItemFont> getCategoryFont(String key) {
        switch (key) {
//            case Constant.KEY_TAB_ALL:
//                return listFontsTabAll;
            case Constant.KEY_TAB_SANS:
                return listFontsSans;
            case Constant.KEY_TAB_SERIF:
                return listFontsSerif;
            case Constant.KEY_TAB_DISPLAY:
                return listFontsDisplay;
            case Constant.KEY_TAB_HAND:
                return listFontsHand;
            case Constant.KEY_TAB_SCRIPT:
                return listFontsScript;
            case Constant.KEY_TAB_TIKTOK:
                return listFontsTiktok;
            case Constant.KEY_TAB_INS:
                return listFontsInstagram;
            case Constant.KEY_TAB_OTHER:
                return listFontsOther;
            default:
                return listFontsTabAll;
        }
    }

    public ArrayList<ItemFont> insertNewFont() {
        mPrefs.edit().putBoolean(Constant.IS_CHECK_SUPPORT_FONT, true).apply();
        int nextId = 200;//App.getInstance().nextIdNew;
        Paint paint = new Paint();
        ArrayList<ItemFont> listFontsAdd = new ArrayList<>();
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_GREEK)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_GREEK, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Handwritten", false, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ANCHOR)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ANCHOR, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Handwritten", false, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_COUNTRY_CODE)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_COUNTRY_CODE, 0, "file:///android_asset/background/2.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_WIGGLY)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_WIGGLY, 0, "file:///android_asset/background/3.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_CRISS_CROSS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_CRISS_CROSS, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Handwritten", false, true));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SOVIET)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SOVIET, 0, "file:///android_asset/background/4.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ROCK_DOTS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ROCK_DOTS, 0, "file:///android_asset/background/5.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_STROKED_2)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_STROKED_2, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SUBSCRIPT)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SUBSCRIPT, 0, "file:///android_asset/background/2.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_CENSORED)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_CENSORED, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_DELTA)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_DELTA, 0, "file:///android_asset/background/8.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_LEFT_HANDED)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_LEFT_HANDED, 0, "file:///android_asset/background/7.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HIEROGLYPH)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HIEROGLYPH, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Handwritten", false, false));
        if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_CHESS)[0].toString()))
            listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_CHESS, 0, "file:///android_asset/background/9.png", "Every day is a second chance", "Handwritten", false, false));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_BLOCKS)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_BLOCKS, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Handwritten", false, true));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_CURVY_1)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_CURVY_1, 0, "file:///android_asset/background/2.png", "Every day is a second chance", "Handwritten", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_SHAKY)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_SHAKY, 0, "file:///android_asset/background/1.png", "Every day is a second chance", "Handwritten", false, true));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_STINGY)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_STINGY, 0, "file:///android_asset/background/3.png", "Every day is a second chance", "Handwritten", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_ORIENTAL)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_ORIENTAL, 0, "file:///android_asset/background/4.png", "Every day is a second chance", "Handwritten", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_FANCEE)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_FANCEE, 0, "file:///android_asset/background/7.png", "Every day is a second chance", "Handwritten", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_FAHRENHEIT)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_FAHRENHEIT, 0, "file:///android_asset/background/8.png", "Every day is a second chance", "Handwritten", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_HOURGLASS)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_HOURGLASS, 0, "file:///android_asset/background/6.png", "Every day is a second chance", "Handwritten", false, false));
            if (isSupportFontOnApi(paint, font.getFont(Constant.FONT_GLITCH)[0].toString()))
                listFontsAdd.add(new ItemFont(nextId++, Constant.FONT_GLITCH, 0, "file:///android_asset/background/9.png", "Every day is a second chance", "Handwritten", false, false));
        }
        return listFontsAdd;
    }

    private boolean isSupportFontOnApi(Paint paint, String text) {
//        boolean isEmojiRendered;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            isEmojiRendered = paint.hasGlyph(text);
//        } else {
//            isEmojiRendered = paint.measureText(text) > 7;
//        }
//        return isEmojiRendered;
        return true;
    }
}
