package com.tapbi.spark.yokey.data.remote.translate;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiTranslate {
    @POST("async/translate")
    @Headers({"content-type: application/x-www-form-urlencoded",
            "user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36"})
    Single<String> getTranslate(@Body String body);
}
