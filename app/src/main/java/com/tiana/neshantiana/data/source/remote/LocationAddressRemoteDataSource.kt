package com.tiana.neshantiana.data.source.remote

import com.tiana.neshantiana.data.model.*
import com.tiana.neshantiana.data.model.information.ChangeLocationCustomerInformation
import com.tiana.neshantiana.data.model.information.CustomerAroundMeInformation
import com.tiana.neshantiana.data.model.information.CustomerScatteringInformation
import com.tiana.neshantiana.data.model.information.GetCustomerLocationInformation
import com.tiana.neshantiana.data.source.LocationAddressDataSource
import com.tiana.neshantiana.service.http.ApiServiceNeshan
import com.tiana.neshantiana.service.http.ApiServiceTiana
import io.reactivex.Completable
import io.reactivex.Single


class LocationAddressRemoteDataSource(
    private val apiServiceNeshan: ApiServiceNeshan,
    private val apiServiceTiana: ApiServiceTiana
) :
    LocationAddressDataSource {
    override fun getLocationAddress(
        latitude: String,
        longitude: String
    ): Single<LocationAddress> = apiServiceNeshan.getLocationAddress(latitude, longitude)

    override fun getCustomersAroundMe(
        customerAroundMeInformation: CustomerAroundMeInformation
    ): Single<List<CustomerAroundMe>> =
        apiServiceTiana.getCustomersAroundMe(customerAroundMeInformation)

    override fun getCustomerScattering(information: CustomerScatteringInformation): Single<List<CustomerAroundMe>> =
        apiServiceTiana.getCustomerScattering(information)

    override fun getCustomers(): Single<List<Customer>> = apiServiceTiana.getCustomers()

    override fun changeLocationCustomer(information: ChangeLocationCustomerInformation): Completable =
        apiServiceTiana.changeLocationCustomer(information)

    override fun getCustomerLocation(information: GetCustomerLocationInformation): Single<List<CustomerLocation>> =
        apiServiceTiana.getCustomerLocation(information)
}