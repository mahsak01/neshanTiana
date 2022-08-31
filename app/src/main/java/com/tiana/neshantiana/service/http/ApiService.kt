package com.tiana.neshantiana.service.http

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.tiana.neshantiana.data.model.LocationAddress
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import io.reactivex.Single
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory

interface ApiService {

    @GET("reverse?")
    fun getLocationAddress(
        @Query("lat") latitude: String,
        @Query("lng") longitude: String
    ): Single<LocationAddress>
}

fun createApiServiceInstance(): ApiService {

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            val oldRequest = it.request()
            val newRequestBuilder = oldRequest.newBuilder()
            newRequestBuilder.addHeader("Api-Key", "service.030ffda5273847449d5d2799184cc70a")
            return@addInterceptor it.proceed(newRequestBuilder.build())
        }.build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.neshan.org/v5/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()


    return retrofit.create(ApiService::class.java)
}