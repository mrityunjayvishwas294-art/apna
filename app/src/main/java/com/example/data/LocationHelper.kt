package com.example.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class ResolvedLocation(
  val areaName: String,
  val fullAddress: String,
  val latitude: Double = 0.0,
  val longitude: Double = 0.0
)

fun openLocationInMaps(
  context: Context,
  area: String,
  addressDetail: String,
  lat: Double = 0.0,
  lng: Double = 0.0
) {
  val query = if (lat != 0.0 && lng != 0.0) {
    "$lat,$lng(${android.net.Uri.encode("$area $addressDetail".trim())})"
  } else {
    android.net.Uri.encode("$area, $addressDetail".trim())
  }
  val uri = if (lat != 0.0 && lng != 0.0) {
    android.net.Uri.parse("geo:$lat,$lng?q=$query")
  } else {
    android.net.Uri.parse("geo:0,0?q=$query")
  }
  val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  try {
    context.startActivity(intent)
  } catch (_: Exception) {
    val webUri = if (lat != 0.0 && lng != 0.0) {
      android.net.Uri.parse("https://maps.google.com/?q=$lat,$lng")
    } else {
      android.net.Uri.parse("https://maps.google.com/?q=${android.net.Uri.encode("$area $addressDetail".trim())}")
    }
    try {
      context.startActivity(
        android.content.Intent(android.content.Intent.ACTION_VIEW, webUri).apply {
          addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      )
    } catch (_: Exception) {
      android.widget.Toast.makeText(context, "लोकेशन: $area, $addressDetail", android.widget.Toast.LENGTH_LONG).show()
    }
  }
}

fun hasLocationPermission(context: Context): Boolean {
  val fine = ContextCompat.checkSelfPermission(
    context,
    Manifest.permission.ACCESS_FINE_LOCATION
  ) == PackageManager.PERMISSION_GRANTED
  val coarse = ContextCompat.checkSelfPermission(
    context,
    Manifest.permission.ACCESS_COARSE_LOCATION
  ) == PackageManager.PERMISSION_GRANTED
  return fine || coarse
}

@SuppressLint("MissingPermission")
suspend fun fetchCurrentLocationAddress(context: Context): ResolvedLocation? = withContext(Dispatchers.IO) {
  if (!hasLocationPermission(context)) return@withContext null
  val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    ?: return@withContext null

  val providers = listOf(
    LocationManager.GPS_PROVIDER,
    LocationManager.NETWORK_PROVIDER,
    LocationManager.PASSIVE_PROVIDER
  )

  var bestLocation: Location? = null
  for (provider in providers) {
    try {
      if (locationManager.isProviderEnabled(provider)) {
        val loc = locationManager.getLastKnownLocation(provider)
        if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
          bestLocation = loc
        }
      }
    } catch (_: Exception) {
    }
  }

  val loc = bestLocation ?: return@withContext null
  val lat = String.format(Locale.US, "%.4f", loc.latitude)
  val lng = String.format(Locale.US, "%.4f", loc.longitude)

  return@withContext try {
    @Suppress("DEPRECATION")
    val geocoder = Geocoder(context, Locale.getDefault())
    @Suppress("DEPRECATION")
    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
    val addr = addresses?.firstOrNull()
    if (addr != null) {
      val areaParts = listOfNotNull(addr.subLocality, addr.locality ?: addr.subAdminArea)
        .distinct()
        .joinToString(", ")
        .ifBlank { "वर्तमान लोकेशन ($lat, $lng)" }
      val full = addr.getAddressLine(0)?.takeIf { it.isNotBlank() } ?: areaParts
      ResolvedLocation(
        areaName = areaParts,
        fullAddress = full,
        latitude = loc.latitude,
        longitude = loc.longitude
      )
    } else {
      ResolvedLocation(
        areaName = "वर्तमान लोकेशन ($lat, $lng)",
        fullAddress = "GPS: $lat, $lng",
        latitude = loc.latitude,
        longitude = loc.longitude
      )
    }
  } catch (_: Exception) {
    ResolvedLocation(
      areaName = "वर्तमान लोकेशन ($lat, $lng)",
      fullAddress = "GPS: $lat, $lng",
      latitude = loc.latitude,
      longitude = loc.longitude
    )
  }
}
