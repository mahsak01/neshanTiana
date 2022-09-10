package com.tiana.neshantiana.common

import android.text.SpannableString
import android.text.style.UnderlineSpan
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.MapView
import java.util.concurrent.Executors

const val earthRadiusKm: Double = 6372.8
fun underlineText(text: String): SpannableString {
    val spannableString = SpannableString(text)
    spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, 0)
    return spannableString
}

fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val originLat = Math.toRadians(lat1)
    val destinationLat = Math.toRadians(lat2)
    val a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) + Math.pow(
        Math.sin(dLon / 2),
        2.toDouble()
    ) * Math.cos(originLat) * Math.cos(destinationLat)
    val c = 2 * Math.asin(Math.sqrt(a))
    return earthRadiusKm * c * 1000
}
