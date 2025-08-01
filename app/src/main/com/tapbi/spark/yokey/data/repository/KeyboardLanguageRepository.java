package com.tapbi.spark.yokey.data.repository;

import static com.android.inputmethod.latin.common.LocaleUtils.constructLocaleFromString;

import android.content.Context;
import android.os.Build;
import android.os.LocaleList;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.common.MessageEvent;
import com.tapbi.spark.yokey.data.local.LanguageEntity;
import com.tapbi.spark.yokey.data.local.db.ThemeDB;
import com.tapbi.spark.yokey.util.CommonUtil;
import com.tapbi.spark.yokey.util.Constant;
import com.tapbi.spark.yokey.util.LocaleUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class KeyboardLanguageRepository {

    public ArrayList<String> mLocaleSystemArray = new ArrayList<>();
    public List<LanguageEntity> mLanguageEntities = new ArrayList<>();

    @Inject
    public KeyboardLanguageRepository() {
        mLocaleSystemArray = CommonUtil.getStringArrayPref(Constant.PREF_SYSTEM_LOCALE);
    }

    public Single<Boolean> updateLanguage(Boolean isEnabled, int id) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                ThemeDB.getInstance(App.getInstance()).languageDAO().updateLanguage(isEnabled, id);
                return true;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<ArrayList<LanguageEntity>> getAllLanguageLocal() {
        return Single.fromCallable(() -> {
            LocaleUtils.INSTANCE.applyLocale(App.getInstance());
            ArrayList<LanguageEntity> languageLocalEntities = new ArrayList<>();
            InputMethodInfo inputMethodInfo = CommonUtil.getInfoThisIme();
            if (inputMethodInfo == null) {
                return languageLocalEntities;
            }
            if (!App.getInstance().createAdditionalSubtype) {
                InputMethodSubtype[] inputMethodSubtypes = CommonUtil.getAdditionalSubtypes(App.getInstance());
                App.getInstance().getMImm().setAdditionalInputMethodSubtypes(inputMethodInfo.getId(), inputMethodSubtypes);
                inputMethodInfo = CommonUtil.getInfoThisIme();
                if (inputMethodInfo == null) {
                    return languageLocalEntities;
                }
                App.getInstance().createAdditionalSubtype = true;
            }

            int count = inputMethodInfo.getSubtypeCount();
            for (int i = 0; i < count; i++) {
                if (i < inputMethodInfo.getSubtypeCount()) {
                    InputMethodSubtype subtype = inputMethodInfo.getSubtypeAt(i);
                    if (subtype.getLocale().isEmpty()) {
                        continue;
                    }
                    LanguageEntity languageEntity = CommonUtil.convertIMSubtypeToLanguageEntity(subtype);
                    languageLocalEntities.add(languageEntity);
                }
            }
            Collections.sort(languageLocalEntities, (o1, o2) -> o1.locale.compareTo(o2.locale));
            return languageLocalEntities;
        }).subscribeOn(Schedulers.io());
    }

    public Completable insertLanguage(ArrayList<LanguageEntity> languageEntities) {
        return Completable.fromAction(() -> {
            ThemeDB.getInstance(App.getInstance()).languageDAO().deleteAllData();
            ArrayList<LanguageEntity> languageEntitiesTemp = new ArrayList<>();
            languageEntitiesTemp.addAll(languageEntities);
            if (App.getInstance().mPrefs.getBoolean(Constant.IS_USE_SYSTEM_LANGUAGE, true)) {
                Collections.sort(languageEntitiesTemp, (languageEntity, t1) -> languageEntity.indexList - t1.indexList);
            }
            for (LanguageEntity languageEntity : languageEntitiesTemp) {
                if (languageEntity.isEnabled) {
                    ThemeDB.getInstance(App.getInstance()).languageDAO().insert(languageEntity);
                }
            }

        }).doOnComplete(() -> {
            getAllLanguageDb(true).subscribe();
        }).subscribeOn(Schedulers.io());
    }

    public Single<List<LanguageEntity>> getAllLanguageDb(boolean needToNotifyUpdate) {
        return Single.fromCallable(() -> {
            List<LanguageEntity> languageEntities = ThemeDB.getInstance(App.getInstance()).languageDAO().getGetAllLanguages();
            if (needToNotifyUpdate) {
                mLanguageEntities = new ArrayList<>();

                if ((languageEntities == null || languageEntities.isEmpty()) && !App.getInstance().mPrefs.getBoolean(Constant.IS_USE_SYSTEM_LANGUAGE, true)) {
                    ArrayList<String> stringsLocale = CommonUtil.getStringArrayPref(Constant.PREF_SYSTEM_LOCALE);
                    InputMethodInfo inputMethodInfo = CommonUtil.getInfoThisIme();
                    if (inputMethodInfo != null) {
                        InputMethodSubtype[] inputMethodSubtypes = CommonUtil.getAdditionalSubtypes(App.getInstance());
                        if(inputMethodSubtypes != null){
                            if (App.getInstance().getMImm() == null ) {
                                App.getInstance().setMImm((InputMethodManager) App.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE));
                            }
                            App.getInstance().getMImm().setAdditionalInputMethodSubtypes(inputMethodInfo.getId(), inputMethodSubtypes);
                        }

                        int count = inputMethodInfo.getSubtypeCount();
                        for (int i = 0; i < count; i++) {
                            if (i < inputMethodInfo.getSubtypeCount()) {
                                InputMethodSubtype subtype = inputMethodInfo.getSubtypeAt(i);
                                if (subtype.getLocale().isEmpty()) {
                                    continue;
                                }
                                for (String localString : stringsLocale) {
                                    Locale locale = constructLocaleFromString(localString);
                                    if (locale.getLanguage().contains(subtype.getLocale())){
                                        LanguageEntity languageEntity = CommonUtil.convertIMSubtypeToLanguageEntity(subtype);
                                        languageEntities.add(languageEntity);
                                        break;
                                    }
                                }

                            }
                        }
                    }
                }

                if (languageEntities != null && !languageEntities.isEmpty()) {
                    try {
                        mLanguageEntities.addAll(languageEntities);
                    }catch (ArrayIndexOutOfBoundsException e){
                        Timber.e("Duongcv " + e.getMessage());
                    }
                }

                EventBus.getDefault().post(new MessageEvent(Constant.EVENT_LOAD_LANGUAGE_DB));
            }
            return languageEntities;
        }).subscribeOn(Schedulers.io());
    }

    public void deleteKeyboardLanguageDbByLocale(LanguageEntity languageEntity) {
        Single.fromCallable(() -> {
            if (ThemeDB.getInstance(App.getInstance()) != null && ThemeDB.getInstance(App.getInstance()).languageDAO() != null) {
                ThemeDB.getInstance(App.getInstance()).languageDAO().deleteKeyboardLanguageByLocale(languageEntity.locale);
                List<LanguageEntity> languageEntities = ThemeDB.getInstance(App.getInstance()).languageDAO().getGetAllLanguages();
                mLanguageEntities.clear();
                mLanguageEntities.addAll(languageEntities);
                EventBus.getDefault().post(new MessageEvent(Constant.EVENT_LOAD_LANGUAGE_DB));
            }
            return true;
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void insertKeyboardLanguageDb(LanguageEntity languageEntity) {
        Single.fromCallable(() -> {
            if (ThemeDB.getInstance(App.getInstance()) != null && ThemeDB.getInstance(App.getInstance()).languageDAO() != null) {
                ThemeDB.getInstance(App.getInstance()).languageDAO().deleteKeyboardLanguageByLocale(languageEntity.locale);
                ThemeDB.getInstance(App.getInstance()).languageDAO().insert(languageEntity);
                List<LanguageEntity> languageEntities = ThemeDB.getInstance(App.getInstance()).languageDAO().getGetAllLanguages();
                mLanguageEntities.clear();
                mLanguageEntities.addAll(languageEntities);
                EventBus.getDefault().post(new MessageEvent(Constant.EVENT_LOAD_LANGUAGE_DB));
            }
            return true;
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public void getAllLanguages(String noDownload, boolean isCheckDownloadDictionaryInDB) {
        if (isCheckDownloadDictionaryInDB) {
            List<LanguageEntity> entities = new ArrayList<>();
            entities.addAll(mLanguageEntities);
//            Single.just(entities).subscribe(new SingleObserver<List<LanguageEntity>>() {
//                @Override
//                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
//                }
//
//                @Override
//                public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<LanguageEntity> languageEntities) {
//                    if (languageEntities.size() > 0) {
//                        if (getConnectivityStatus() != -1) {
//                            boolean isCheck = mPref.getBoolean(Constant.CHECK_UPDATE_VERSION, false);
//                            if (!isCheck) {
//                                mPref.edit().putBoolean(Constant.CHECK_UPDATE_VERSION, true).apply();
//                            }
//                        } else {
//                            if (mPref.getString(Constant.DICTIONARY_FIRST_INSTALLATION, "").equals("")) {
//                                StringBuilder stringBuffer = new StringBuilder();
//                                for (LanguageEntity languageEntity : languageEntities) {
//                                    if(languageEntity!=null){
//                                        String locale = languageEntity.locale;
//                                        if (locale.contains("_")) {
//                                            String[] split = locale.split("_");
//                                            locale = split[0];
//                                        }
//                                        stringBuffer.append(locale).append("_");
//                                    }
//
//                                }
//                                mPref.edit().putString(Constant.DICTIONARY_FIRST_INSTALLATION, stringBuffer.toString()).apply();
//                            }
//                        }
//                    }
//                }

//                @Override
//                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

//                }
//            });
        } else {
//            String[] dic = Constant.DICTIONARY_LOCAL.split("_");
//            sDownloadState.isFeedbackDownloadAll = true;
//            sDownloadState.numberSuccessDictionary = 0;
//            sDownloadState.numberTotalDictionary = 0;
//            sDownloadState.numberChangeDictionary = 0;
//            ContextWrapper contextWrapper = new ContextWrapper(App.getInstance());
//            File destinationFile = contextWrapper.getDir(App.getInstance().getFilesDir().getName(), Context.MODE_PRIVATE);
//            for (String s : dic) {
//                String id = "main_" + s + ".dict";
//                File file = new File(destinationFile, id);
//                if (!file.exists() && !s.equals("en") && !s.equals("ru")) {
//                    sDownloadState.numberTotalDictionary += 1;
//                    downloadDictionary(s, 0);
//                }
//            }
        }
    }

    public void setDefaultLanguageFirstTime() {
        getAllLanguageLocal().flatMapCompletable(languageEntities -> {
            Timber.e("setDefaultLanguageFirstTime" + languageEntities.size());
            CommonUtil.setEnableDefaultSystem(languageEntities);
            return insertLanguage(languageEntities);
        }).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onComplete() {
                if (App.getInstance().mPrefs != null)
                    App.getInstance().mPrefs.edit().putBoolean(Constant.IS_FIRST_INIT_LANGUAGE, true).apply();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                if (App.getInstance().mPrefs != null)
                    App.getInstance().mPrefs.edit().putBoolean(Constant.IS_FIRST_INIT_LANGUAGE, false).apply();
                e.printStackTrace();
            }
        });
    }


    public Single<ArrayList<LanguageEntity>> getLanguageAdapter(boolean returnAll) {
        return Single.zip(getAllLanguageDb(false), getAllLanguageLocal(), (languageEntitiesDb, languageEntitiesLocal) -> {
            if (App.getInstance().mPrefs.getBoolean(Constant.IS_USE_SYSTEM_LANGUAGE, true)) {
                CommonUtil.setEnableDefaultSystem(languageEntitiesLocal);
            } else {
                for (int i = 0; i < languageEntitiesLocal.size(); i++) {
                    LanguageEntity languageEntity = languageEntitiesLocal.get(i);
                    for (LanguageEntity languageDBEntity : languageEntitiesDb) {
                        if (languageDBEntity.locale.equals(languageEntity.locale) && languageDBEntity.name.equals(languageEntity.name)) {
                            languageEntitiesLocal.get(i).isEnabled = true;
                        }
                    }
                }
            }
            return languageEntitiesLocal;
        }).subscribeOn(Schedulers.io());
    }

    public void updateLanguages(boolean systemLocalChanged, boolean fromConfigChanged) {
        if (App.getInstance().mPrefs != null) {
            if (!App.getInstance().mPrefs.getBoolean(Constant.IS_FIRST_INIT_LANGUAGE, false)) {
                Timber.e("updateLanguages first_time");
                setDefaultLanguageFirstTime();
            } else {
                Timber.e("updateLanguages other_time systemLocalChanged %s", systemLocalChanged);
                if (systemLocalChanged) {
                    getLanguageAdapter(false).flatMapCompletable(this::insertLanguage)
                            .doOnError(Throwable::printStackTrace)
                            .subscribe();
                } else {
                    if (!fromConfigChanged) {
                        if (App.getInstance().mPrefs.getBoolean(Constant.IS_FIRST_INIT_LANGUAGE, false) && !App.getInstance().mPrefs.getBoolean(Constant.IS_UPGRADE_LANGUAGE, false)) {
                            App.getInstance().mPrefs.edit().putBoolean(Constant.IS_UPGRADE_LANGUAGE, true).apply();
                            getLanguageAdapter(false).flatMapCompletable(this::insertLanguage)
                                    .doOnError(Throwable::printStackTrace)
                                    .subscribe();
                        } else {
                            getAllLanguageDb(true).subscribe();
                        }

                    }
                }
                Timber.d("ducNQ : runonMessageReceived: ");
                EventBus.getDefault().post(new MessageEvent(Constant.EVENT_CHANGE_LANGUAGE));
            }
        }
    }

    public boolean checkSystemLocaleChange() {
        ArrayList<String> mLocalesNew = getSystemLocale();
        if (!mLocaleSystemArray.equals(mLocalesNew)) {
            mLocaleSystemArray = mLocalesNew;
            CommonUtil.setStringArrayPref(Constant.PREF_SYSTEM_LOCALE, mLocalesNew);
            return true;
        }
        return false;
    }

    public ArrayList<String> getSystemLocale() {
        ArrayList<String> mLocales = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = LocaleList.getDefault();
            for (int i = 0; i < localeList.size(); i++) {
                mLocales.add(localeList.get(i).toString());
            }
        } else {
            mLocales.add(Locale.getDefault().toString());
        }
        return mLocales;
    }


}
