package com.tapbi.spark.yokey.data.remote;

import com.tapbi.spark.yokey.data.model.ListSticker;
import com.tapbi.spark.yokey.data.model.PaginationObj;
import com.tapbi.spark.yokey.data.model.PaginationUpdate;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface StickerService {

    @POST("ZomjKeyboardGetSticker")
    Call<ListSticker> getListSticker(@Body PaginationObj paginationObject);

    @POST("ZomjCheckUpdateSticker")
    Call<ListSticker> checkUpdateSticker(@Body PaginationUpdate paginationUpdate);


    @Streaming
    @GET
    Call<ResponseBody> downloadFileByUrl(@Url String fileUrl);
}
