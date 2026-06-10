package com.example.taskerine_v2.ui.components

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskerine_v2.util.GeocodingHelper
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun LocationPickerMap(
    onLocationPicked: (address: String, latLng: LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val london = LatLng(51.5074, -0.1278)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(london, 11f)
    }
    var markerPosition by remember { mutableStateOf<LatLng?>(null) }
    var resolvedAddress by remember { mutableStateOf("") }

    Column {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true),
                onMapClick = { latLng ->
                    markerPosition = latLng
                    // Reverse geocode to get address
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        val geocoder = android.location.Geocoder(
                            context,
                            java.util.Locale.getDefault()
                        )
                        try {
                            @Suppress("DEPRECATION")
                            val results = withContext(kotlinx.coroutines.Dispatchers.IO) {
                                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                            }
                            if (!results.isNullOrEmpty()) {
                                val addr = results[0]
                                resolvedAddress = buildString {
                                    if (!addr.thoroughfare.isNullOrBlank()) append(addr.thoroughfare)
                                    if (!addr.locality.isNullOrBlank()) {
                                        if (isNotEmpty()) append(", ")
                                        append(addr.locality)
                                    }
                                    if (!addr.countryName.isNullOrBlank()) {
                                        if (isNotEmpty()) append(", ")
                                        append(addr.countryName)
                                    }
                                }
                                onLocationPicked(resolvedAddress, latLng)
                            }
                        } catch (e: Exception) {
                            resolvedAddress = "${latLng.latitude}, ${latLng.longitude}"
                            onLocationPicked(resolvedAddress, latLng)
                        }
                    }
                }
            ) {
                markerPosition?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Task location"
                    )
                }
            }
        }

        if (resolvedAddress.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "📍 $resolvedAddress",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Tap on the map to set the task location",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private suspend fun <T> withContext(
    context: kotlin.coroutines.CoroutineContext,
    block: suspend () -> T
): T = kotlinx.coroutines.withContext(context) { block() }

