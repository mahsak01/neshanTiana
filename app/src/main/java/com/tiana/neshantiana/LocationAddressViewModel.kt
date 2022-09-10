package com.tiana.neshantiana

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tiana.neshantiana.common.NeshanTianaCompletableObserver
import com.tiana.neshantiana.common.NeshanTianaSingleObserver
import com.tiana.neshantiana.data.model.*
import com.tiana.neshantiana.data.model.information.ChangeLocationCustomerInformation
import com.tiana.neshantiana.data.model.information.CustomerAroundMeInformation
import com.tiana.neshantiana.data.model.information.CustomerScatteringInformation
import com.tiana.neshantiana.data.model.information.GetCustomerLocationInformation
import com.tiana.neshantiana.data.repository.LocationAddressRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class LocationAddressViewModel(private val locationAddressRepository: LocationAddressRepository) :
    ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    val locationAddressLiveData = MutableLiveData<LocationAddress>()
    val customersAroundMeLiveData = MutableLiveData<List<CustomerAroundMe>?>()
    val customerScatteringLiveData = MutableLiveData<List<CustomerAroundMe>>()
    val customersLiveData = MutableLiveData<List<Customer>>()
    val customerLocationLiveData = MutableLiveData<List<CustomerLocation>>()

    fun getLocationAddress(latitude: String, longitude: String) {
        locationAddressRepository.getLocationAddress(latitude, longitude)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : NeshanTianaSingleObserver<LocationAddress>(compositeDisposable) {
                override fun onSuccess(t: LocationAddress) {
                    locationAddressLiveData.value = t
                }
            })
    }

    fun getCustomersAroundMe(latitude: String, longitude: String, distance: Int) {
        locationAddressRepository.getCustomersAroundMe(
            CustomerAroundMeInformation(distance, "$longitude $latitude", 1.101, 1.101)
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :
                NeshanTianaSingleObserver<List<CustomerAroundMe>?>(compositeDisposable) {
                override fun onSuccess(t: List<CustomerAroundMe>) {
                    customersAroundMeLiveData.value = t
                }
            })
    }

    fun getCustomerScattering() {
        locationAddressRepository.getCustomerScattering(CustomerScatteringInformation(1.101, 1.101))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :
                NeshanTianaSingleObserver<List<CustomerAroundMe>>(compositeDisposable) {
                override fun onSuccess(t: List<CustomerAroundMe>) {
                    customerScatteringLiveData.value = t
                }
            })
    }

    fun getCustomers() {
        locationAddressRepository.getCustomers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :
                NeshanTianaSingleObserver<List<Customer>>(compositeDisposable) {
                override fun onSuccess(t: List<Customer>) {
                    customersLiveData.value = t
                }
            })
    }

    fun updateLocation(latitude: String, longitude: String, customerId: Double) {
        locationAddressRepository.changeLocationCustomer(
            ChangeLocationCustomerInformation(
                customerId,
                "$longitude $latitude",
                "mahsak01",
                "mahsa"
            )
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :
                NeshanTianaCompletableObserver(compositeDisposable) {
                override fun onComplete() {
                }
            })
    }

    fun getCustomerLocation(customerId: Double) {
        locationAddressRepository.getCustomerLocation(GetCustomerLocationInformation(customerId))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :
                NeshanTianaSingleObserver<List<CustomerLocation>>(compositeDisposable) {
                override fun onSuccess(t: List<CustomerLocation>) {
                    customerLocationLiveData.value = t
                }
            })
    }
}