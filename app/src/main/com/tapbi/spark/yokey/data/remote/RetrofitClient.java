package com.tapbi.spark.yokey.data.remote;

import com.tapbi.spark.yokey.data.remote.translate.ApiTranslate;
import com.tapbi.spark.yokey.util.Constant;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {


    private static Retrofit retrofitDownloadZipFileTheme = null;

    private static Retrofit retrofitGetLEDTheme = null;
    private static Retrofit retrofitGetGradientTheme = null;
    private static Retrofit retrofitGetColorTheme = null;
    private static Retrofit retrofitGetBackgroundTheme = null;
    private static Retrofit retrofitGetHotTheme = null;
    private static Retrofit retrofitGetTopTheme = null;

    public static Retrofit downloadZIPFileTheme(String baseUrlGetThemes) {
        if (retrofitDownloadZipFileTheme == null) {
            retrofitDownloadZipFileTheme = new Retrofit.Builder()
                    .baseUrl(baseUrlGetThemes)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitDownloadZipFileTheme;

    }

    public static Retrofit getLEDTheme(String baseUrlGetThemes) {
        if (retrofitGetLEDTheme == null) {
            retrofitGetLEDTheme = new Retrofit.Builder()
                    .baseUrl(baseUrlGetThemes)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitGetLEDTheme;

    }

    public static Retrofit.Builder getCommonRetrofitBuilder(String url){
        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create());
    }


    public static Retrofit getGradientTheme(String baseUrlGetThemes) {
        if (retrofitGetGradientTheme == null) {
            retrofitGetGradientTheme = new Retrofit.Builder()
                    .baseUrl(baseUrlGetThemes)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitGetGradientTheme;

    }

    public static Retrofit getColorTheme(String baseUrlGetThemes) {
        if (retrofitGetColorTheme == null) {
            retrofitGetColorTheme = new Retrofit.Builder()
                    .baseUrl(baseUrlGetThemes)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitGetColorTheme;

    }


    public static Retrofit getBackgroundTheme(String baseUrlGetThemes) {
        if (retrofitGetBackgroundTheme == null) {
            retrofitGetBackgroundTheme = new Retrofit.Builder()
                    .baseUrl(baseUrlGetThemes)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitGetBackgroundTheme;

    }

    public static Retrofit getHotTheme(String baseUrlGetThemes) {
        if (retrofitGetHotTheme == null) {
            retrofitGetHotTheme = new Retrofit.Builder()
                    .baseUrl(baseUrlGetThemes)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitGetHotTheme;

    }

    public static Retrofit getTopTheme(String baseUrlGetThemes) {
        if (retrofitGetTopTheme == null) {
            retrofitGetTopTheme = new Retrofit.Builder()
                    .baseUrl(baseUrlGetThemes)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitGetTopTheme;

    }
    private static Retrofit retrofitTranslate = null;
    public static Retrofit getRetrofitInstanceTranslate(String url) {
        if (retrofitTranslate == null) {
            retrofitTranslate = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();
        }
        return retrofitTranslate;
    }
    public static Retrofit getRetrofitServer(String baseUrlGet) {
        return new Retrofit.Builder()
                .baseUrl(baseUrlGet)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    public static ApiTranslate getApiTranslate() {
        return getRetrofitInstanceTranslate(Constant.ROOT_URL_TRANSLATE).create(ApiTranslate.class);
    }
}
