package com.tapbi.spark.yokey.di

import com.tapbi.spark.yokey.common.Constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object NetWorkModule {
    @Provides
    @Singleton
    fun providesRetrofit(
        okHttpClient: OkHttpClient?
    ): Retrofit {
        return Retrofit.Builder().baseUrl(Constant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun providesOkHttpClientAppVersion(): OkHttpClient {
        val client: OkHttpClient.Builder = OkHttpClient.Builder()
            .connectTimeout(Constant.CONNECT_S.toLong(), TimeUnit.SECONDS)
            .writeTimeout(Constant.WRITE_S.toLong(), TimeUnit.SECONDS)
            .readTimeout(Constant.READ_S.toLong(), TimeUnit.SECONDS)
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
        client.addNetworkInterceptor(interceptor)
        return client.build()
    }
}