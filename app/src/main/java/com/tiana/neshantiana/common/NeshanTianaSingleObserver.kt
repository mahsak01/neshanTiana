package com.tiana.neshantiana.common

import android.util.Log
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlin.math.log


abstract class NeshanTianaSingleObserver<T>(private val compositeDisposable: CompositeDisposable) :
    SingleObserver<T> {
    override fun onSubscribe(d: Disposable) {
        compositeDisposable.add(d)
    }

    override fun onError(e: Throwable) {
        Log.ERROR
    }
}