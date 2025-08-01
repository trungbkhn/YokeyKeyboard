package com.tapbi.spark.yokey.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tapbi.spark.yokey.util.Constant;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ClipboardRepository {
    private SharedPreferences mPrefs;
    private Context context;

    public ClipboardRepository(Context context) {
        this.context = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Single<ArrayList<String>> getListClipboardThread(){
        return Single.fromCallable(this::getAllClipboard).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Boolean> addClipboardThread(String content){
        return Single.just(addClipboard(content)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Boolean> refeshClipboardThread(List<String> strings){
        return Single.just(refreshClipboard(strings)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private ArrayList<String> getAllClipboard(){
        ArrayList<String> list = new ArrayList<>();
        try {
            String[] split = mPrefs.getString(Constant.CONTENT_CLIPBOARD,"").split("_&_");
            if(split.length>0){
                for (int i = split.length-1; i >= 0; i--) {
                    if(!split[i].equals("")){
                        list.add(split[i]);
                    }
                }
            }
        }catch (OutOfMemoryError exception){
            exception.printStackTrace();
        }
        return list;
    }

    private boolean addClipboard(String content){
        String[] split = mPrefs.getString(Constant.CONTENT_CLIPBOARD,"").split("_&_");
        if(split.length==0){
            mPrefs.edit().putString(Constant.CONTENT_CLIPBOARD,content).apply();
        }else{
            for (String value : split) {
                if (value.equals(content)) {
                    return false;
                }
            }
            StringBuilder s = new StringBuilder();
            if(split.length > 15){
                s.append(split[split.length-15]);
                for (int i = split.length-14; i < split.length; i++) {
                    s.append("_&_").append(split[i]);
                }
                s.append("_&_").append(content);
            }else{
                s.append(mPrefs.getString(Constant.CONTENT_CLIPBOARD,"")).append("_&_").append(content);
            }
            mPrefs.edit().putString(Constant.CONTENT_CLIPBOARD,s.toString()).apply();
        }
        return true;
    }

    private boolean refreshClipboard(List<String> strings){
        if(strings.size()>0){
            StringBuffer s = new StringBuffer();
            for (int i = strings.size()-1; i >= 0; i--) {
                if(i==strings.size()-1)s.append(strings.get(i));
                else{
                    s.append("_&_").append(strings.get(i));
                }
            }
            mPrefs.edit().putString(Constant.CONTENT_CLIPBOARD,s.toString()).apply();
        }else{
            mPrefs.edit().putString(Constant.CONTENT_CLIPBOARD,"").apply();
        }
        return true;
    }
}
