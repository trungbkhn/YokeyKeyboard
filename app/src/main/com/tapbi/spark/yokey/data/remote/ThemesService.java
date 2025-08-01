package com.tapbi.spark.yokey.data.remote;

import com.tapbi.spark.yokey.data.model.PaginationTheme;
import com.tapbi.spark.yokey.data.model.PaginationUpdate;

import io.reactivex.rxjava3.core.Observable;

import com.tapbi.spark.yokey.data.model.ThemeObjectList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface ThemesService {
    @GET("apirgbtheme")
    Call<ThemeObjectList> getListLEDThemes();

    @GET("apigetgradienttheme")
    Call<ThemeObjectList> getListGradientThemes();

    @GET("getthemecolor")
    Call<ThemeObjectList> getListColorThemes();

    @GET("apigetwalltheme")
    Call<ThemeObjectList> getListBackgroundThemes();

    @GET("gethottheme")
    Call<ThemeObjectList> getListHotThemes();

    //Todo Test
//    @GET("apigethotthemtest")
//    Call<ThemeObjectList> getListHotThemes();

    @GET("gettopthemecover")
    Call<ThemeObjectList> getListTopThemes();

    @Streaming
    @GET
    Call<ResponseBody> downloadFileByUrl(@Url String fileUrl);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileByUrlNew();

    @POST("getthemezomjkeyboard")
    Observable<ThemeObjectList> getListLEDThemesd(@Body PaginationTheme paginationTheme);


    @POST("getthemezomjkeyboard")
    Call<ThemeObjectList> getListLEDThemes(@Body PaginationTheme paginationTheme);


    @POST("ZomjCheckUpdateTheme")
    Call<ThemeObjectList> checkUpdateTheme(@Body PaginationUpdate paginationUpdate);


//    @POST("getthemezomjkeyboard")
//    Call<ThemeObjectList> getListThemes(@Body PaginationTheme paginationTheme);
}
