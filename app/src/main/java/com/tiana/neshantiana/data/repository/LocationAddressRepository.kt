package com.tiana.neshantiana.data.repository

import com.tiana.neshantiana.data.model.LocationAddress
import io.reactivex.Single


interface LocationAddressRepository {

     fun getLocationAddress(latitude:String, longitude:String): Single<LocationAddress>
}