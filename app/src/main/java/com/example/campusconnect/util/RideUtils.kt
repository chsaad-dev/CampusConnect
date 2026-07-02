package com.example.campusconnect.util

import kotlin.math.*

object RideUtils {

    data class LatLng(val lat: Double, val lng: Double)

    private val locations = mapOf(
        "Main Gate" to LatLng(31.4707, 74.2403),
        "CS Department" to LatLng(31.4715, 74.2415),
        "Library" to LatLng(31.4720, 74.2420),
        "Hostel 1" to LatLng(31.4750, 74.2450),
        "Hostel 2" to LatLng(31.4760, 74.2460),
        "Cafeteria" to LatLng(31.4710, 74.2410),
        "City Center" to LatLng(31.4800, 74.3000),
        "Johar Town" to LatLng(31.4697, 74.2728),
        "Faisal Town" to LatLng(31.4850, 74.3050)
    )

    fun getCoordinates(locationName: String): LatLng? {
        return locations[locationName]
    }

    fun calculateDistance(start: LatLng, end: LatLng): Double {
        val r = 6371 // Radius of the earth in km
        val latDistance = Math.toRadians(end.lat - start.lat)
        val lonDistance = Math.toRadians(end.lng - start.lng)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(start.lat)) * cos(Math.toRadians(end.lat)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun calculateETA(from: String, to: String, avgSpeedKmh: Double = 40.0): String {
        val start = getCoordinates(from)
        val end = getCoordinates(to)

        if (start == null || end == null) return "N/A"

        val distance = calculateDistance(start, end)
        val timeInHours = distance / avgSpeedKmh
        val timeInMinutes = (timeInHours * 60).toInt()

        return if (timeInMinutes < 60) {
            "$timeInMinutes mins"
        } else {
            "${timeInMinutes / 60}h ${timeInMinutes % 60}m"
        }
    }
}
