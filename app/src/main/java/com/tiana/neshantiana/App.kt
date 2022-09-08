package com.tiana.neshantiana

import android.app.Application
import com.tiana.neshantiana.data.implement.LocationAddressImplement
import com.tiana.neshantiana.data.repository.LocationAddressRepository
import com.tiana.neshantiana.data.source.remote.LocationAddressRemoteDataSource
import com.tiana.neshantiana.service.http.createApiServiceInstance
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val myModules = module {
            single { createApiServiceInstance() }
            factory<LocationAddressRepository> {
                LocationAddressImplement(
                    LocationAddressRemoteDataSource(
                        get()
                    )
                )
            }
            viewModel { LocationAddressViewModel(get()) }
        }

        startKoin {
            androidContext(this@App)
            modules(myModules)
        }

    }
}