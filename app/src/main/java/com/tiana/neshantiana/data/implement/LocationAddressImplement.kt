package com.tiana.neshantiana.data.implement

import com.tiana.neshantiana.data.model.*
import com.tiana.neshantiana.data.model.information.*
import com.tiana.neshantiana.data.repository.LocationAddressRepository
import com.tiana.neshantiana.data.source.LocationAddressDataSource
import io.reactivex.Completable
import io.reactivex.Single


class LocationAddressImplement(private val locationAddressRemoteDataSource: LocationAddressDataSource) :
    LocationAddressRepository {

    override fun getLocationAddress(latitude: String, longitude: String): Single<LocationAddress> =
        locationAddressRemoteDataSource.getLocationAddress(latitude, longitude)

    override fun getCustomersAroundMe(
        customerAroundMeInformation: CustomerAroundMeInformation
    ): Single<List<CustomerAroundMe>> =
        locationAddressRemoteDataSource.getCustomersAroundMe(customerAroundMeInformation)

    override fun getCustomerScattering(information: CustomerScatteringInformation): Single<List<CustomerAroundMe>> =
        locationAddressRemoteDataSource.getCustomerScattering(information)

    override fun getCustomers(): Single<List<Customer>> =
        locationAddressRemoteDataSource.getCustomers()

    override fun changeLocationCustomer(information: ChangeLocationCustomerInformation): Completable =
        locationAddressRemoteDataSource.changeLocationCustomer(information)

    override fun getCustomerLocation(information: GetCustomerLocationInformation): Single<List<CustomerLocation>> =
        locationAddressRemoteDataSource.getCustomerLocation(information)

    override fun getCustomerLastVisit(information: GetCustomerLastVisit): Single<List<CustomerLocation>> =
        locationAddressRemoteDataSource.getCustomerLastVisit(information)


}