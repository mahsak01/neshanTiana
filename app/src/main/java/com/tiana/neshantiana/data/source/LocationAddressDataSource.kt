package com.tiana.neshantiana.data.source.remote

import com.tiana.neshantiana.data.model.LocationAddress
import io.reactivex.Single

interface LocationAddressDataSource {

        fun getLocationAddress(latitude:String, longitude:String): Single<LocationAddress>
}