package com.tiana.neshantiana.data.source.remote

import com.tiana.neshantiana.data.model.LocationAddress
import com.tiana.neshantiana.service.http.ApiService
import io.reactivex.Single


class LocationAddressRemoteDataSource(private val apiService: ApiService) :
    LocationAddressDataSource {
    override fun getLocationAddress(
        latitude: String,
        longitude: String
    ): Single<LocationAddress> = apiService.getLocationAddress(latitude, longitude)
}