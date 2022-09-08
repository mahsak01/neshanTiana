package com.tiana.neshantiana.data.implement

import com.tiana.neshantiana.data.model.LocationAddress
import com.tiana.neshantiana.data.repository.LocationAddressRepository
import com.tiana.neshantiana.data.source.remote.LocationAddressDataSource
import io.reactivex.Single


class LocationAddressImplement(private val locationAddressRemoteDataSource: LocationAddressDataSource) :
    LocationAddressRepository {
    override fun getLocationAddress(latitude: String, longitude: String): Single<LocationAddress> =
        locationAddressRemoteDataSource.getLocationAddress(latitude, longitude)
}