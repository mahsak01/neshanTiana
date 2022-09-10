package com.tiana.neshantiana.service.http

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.tiana.neshantiana.data.model.*
import com.tiana.neshantiana.data.model.information.ChangeLocationCustomerInformation
import com.tiana.neshantiana.data.model.information.CustomerAroundMeInformation
import com.tiana.neshantiana.data.model.information.CustomerScatteringInformation
import com.tiana.neshantiana.data.model.information.GetCustomerLocationInformation
import io.reactivex.Completable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import io.reactivex.Single
import retrofit2.http.*

interface ApiServiceTiana {

    @POST("tiana/api/locationController/GetcustomerAroundMe")
    fun getCustomersAroundMe(@Body information: CustomerAroundMeInformation): Single<List<CustomerAroundMe>>

    @POST("tiana/api/locationController/GetCustomerScattering")
    fun getCustomerScattering(@Body information: CustomerScatteringInformation): Single<List<CustomerAroundMe>>

    @GET("tiana/api/customer")
    fun getCustomers(): Single<List<Customer>>

    @POST("tiana/api/locationController/customerInfoUpdateLocation")
    fun changeLocationCustomer(@Body information: ChangeLocationCustomerInformation):Completable

    @POST("tiana/api/locationController/GetCustomerGeographicalLocation")
    fun getCustomerLocation(@Body information: GetCustomerLocationInformation):Single<List<CustomerLocation>>
}

fun createApiServiceTianaInstance(): ApiServiceTiana {

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            val oldRequest = it.request()
            val newRequestBuilder = oldRequest.newBuilder()
            newRequestBuilder.addHeader(
                "Authorization",
                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy93cy8yMDA1LzA1L2lkZW50aXR5L2NsYWltcy9uYW1laWRlbnRpZmllciI6IjAuMTMzIiwiaXNzIjoiOTQuMTgzLjIuNzkiLCJhdWQiOiI5NC4xODMuMi43OSJ9.X1_vb4VTGEePtcLUL2OZ_TcHQDGOHYkT5D_r0l9bG34"
            )
            return@addInterceptor it.proceed(newRequestBuilder.build())
        }.build()

    val retrofit = Retrofit.Builder()
        .baseUrl("http://94.183.2.79:19745/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()


    return retrofit.create(ApiServiceTiana::class.java)
}