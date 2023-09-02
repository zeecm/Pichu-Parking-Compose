package com.pichugroup.pichuparking.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal fun distanceBetweenTwoCoordinates(start: LatLng, end: LatLng): Double {
    // Haversine formula: https://en.wikipedia.org/wiki/Haversine_formula
    val differenceLongitude = end.longitude - start.longitude
    val differenceLatitude = end.latitude - start.latitude

    val halfChordLengthSquared = sin(differenceLatitude / 2).pow(2) + cos(start.latitude) * cos(end.latitude) * sin(differenceLongitude / 2).pow(2)
    val angularDistanceRadian = 2 * atan2(sqrt(halfChordLengthSquared), sqrt(1 - halfChordLengthSquared))

    val estimatedEarthRadius = 6371.0

    return estimatedEarthRadius * angularDistanceRadian
}