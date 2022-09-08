package com.tiana.neshantiana

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tiana.neshantiana.data.model.Location

class LocationShareViewModel : ViewModel() {

    val locationLiveData = MutableLiveData<Location>()

    fun set(location: Location){
        locationLiveData.value=location
    }
}