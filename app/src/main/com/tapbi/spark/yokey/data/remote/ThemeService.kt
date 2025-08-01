package com.tapbi.spark.yokey.data.remote

import com.tapbi.spark.yokey.data.model.PaginationUpdate
import com.tapbi.spark.objects.BackgroundList
import retrofit2.http.Body
import retrofit2.http.POST

interface ThemeService {

    @POST("apigetbackgroundzomjkeyboard")
    suspend fun getBackground(@Body paginationUpdate: PaginationUpdate): BackgroundList?
//    suspend fun getBackground(): BackgroundList?
}