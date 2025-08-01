package com.tapbi.spark.yokey.data.repository;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tapbi.spark.yokey.App;
import com.tapbi.spark.yokey.data.remote.RetrofitClient;
import com.tapbi.spark.yokey.data.remote.translate.ApiTranslate;

import java.net.URLEncoder;
import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;
import timber.log.Timber;

public class TranslateRepository {
    ApiTranslate apiTranslate;
    private SharedPreferences mPrefs;

    @Inject
    public TranslateRepository() {
        apiTranslate = RetrofitClient.getApiTranslate();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
    }

    public Single<String> getTranslate(String inputLanguage, String outputLanguage, String content) {
        String contentEncoder = content;
        try {
            contentEncoder = URLEncoder.encode(content, "UTF-8");
        } catch (Exception e) {
            Timber.e(e);
        }
//        Timber.e("contentEncoder: " + contentEncoder);
        String body = "async=translate,sl:" + inputLanguage + ",tl:" + outputLanguage + ",st:" + contentEncoder
                + ",id:,qc:true,ac:true,_id:tw-async-translate,_pms:s,_fmt:pc";

        return apiTranslate.getTranslate(body);
    }

//    public String translateLanguage(String inputLanguage, String outputLanguage, String content) {
//        Translate translate = TranslateOptions.newBuilder().setApiKey("AIzaSyCQeqLCTFYB56C19c2P-Mb0CgCgvcAmay0").build().getService();
//        Detection detection = translate.detect(content);
//        String detectedLanguage = detection.getLanguage();
//        Translation translation = translate.translate(
//                content,
//                Translate.TranslateOption.sourceLanguage(inputLanguage),
//                Translate.TranslateOption.targetLanguage(outputLanguage));
//        return translation.getTranslatedText();
//    }

//    public Single<String> getTranslateLanguage(String inputLanguage, String outputLanguage, String content){
//        return Single.fromCallable(new Callable<String>() {
//            @Override
//            public String call() throws Exception {
//                return translateLanguage(inputLanguage,outputLanguage,content);
//            }
//        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
//    }
    public ArrayList<String> getListLanguageRecent(String key, ArrayList<String> defaultValue) {
        ArrayList<String> list = new ArrayList<>();
        try {
            String[] split = mPrefs.getString(key, "").split("_&_");
            if (split.length > 0) {
                for (int i = split.length - 1; i >= 0; i--) {
                    if (!split[i].equals("")) {
                        list.add(split[i]);
                    }
                }
            }
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        if (list.size() == 0) {
            return defaultValue;
        }
        return list;
    }

    public void addListLanguageRecent(String key, ArrayList<String> list) {
        StringBuilder contentSave = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                contentSave = new StringBuilder(list.get(i));
            } else {
                contentSave.append("_&_").append(list.get(i));
            }
        }

        mPrefs.edit().putString(key, contentSave.toString()).commit();
    }
}
