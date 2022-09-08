package com.tiana.neshantiana

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tiana.neshantiana.common.NeshanTianaSingleObserver
import com.tiana.neshantiana.data.model.LocationAddress
import com.tiana.neshantiana.data.repository.LocationAddressRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class LocationAddressViewModel(private val locationAddressRepository: LocationAddressRepository) :
    ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    val locationAddressLiveData = MutableLiveData<LocationAddress>()

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
}