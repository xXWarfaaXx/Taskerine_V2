package com.example.taskerine_v2.util

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object GeocodingHelper {

    suspend fun getLatLngFromAddress(context: Context, address: String): LatLng? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(address, 1)
                if (!results.isNullOrEmpty()) {
                    LatLng(results[0].latitude, results[0].longitude)
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}