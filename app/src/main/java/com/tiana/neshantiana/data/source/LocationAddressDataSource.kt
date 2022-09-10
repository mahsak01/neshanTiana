package com.tiana.neshantiana.data.source

import com.tiana.neshantiana.data.model.*
import com.tiana.neshantiana.data.model.information.ChangeLocationCustomerInformation
import com.tiana.neshantiana.data.model.information.CustomerAroundMeInformation
import com.tiana.neshantiana.data.model.information.CustomerScatteringInformation
import com.tiana.neshantiana.data.model.information.GetCustomerLocationInformation
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body

interface LocationAddressDataSource {

        fun getLocationAddress(latitude:String, longitude:String): Single<LocationAddress>

        fun getCustomersAroundMe(customerAroundMeInformation: CustomerAroundMeInformation):Single<List<CustomerAroundMe>>

        fun getCustomerScattering(information: CustomerScatteringInformation):Single<List<CustomerAroundMe>>

        fun getCustomers(): Single<List<Customer>>

        fun changeLocationCustomer( information: ChangeLocationCustomerInformation): Completable

        fun getCustomerLocation( information: GetCustomerLocationInformation):Single<List<CustomerLocation>>


}