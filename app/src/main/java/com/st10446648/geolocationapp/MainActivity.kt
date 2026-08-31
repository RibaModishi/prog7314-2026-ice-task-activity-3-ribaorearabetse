package com.st10446648.geolocationapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.Location
import android.location.LocationListener

class MainActivity : AppCompatActivity() {

    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    private lateinit var getLocationButton: Button
    private lateinit var locationManager: LocationManager

    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        latitudeText = findViewById(R.id.latitudeText)
        longitudeText = findViewById(R.id.longitudeText)
        getLocationButton = findViewById(R.id.getLocationButton)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        getLocationButton.setOnClickListener {
            checkPermissionAndGetLocation()
        }
    }

    private fun checkPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required to show your coordinates.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Show a cached fix immediately, if one exists
        val lastKnownLocation: Location? = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: SecurityException) {
            null
        }

        if (lastKnownLocation != null) {
            displayLocation(lastKnownLocation)
        } else {
            Toast.makeText(this, "Fetching current location...", Toast.LENGTH_SHORT).show()
        }

        // Also request a fresh reading
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                displayLocation(location)
                locationManager.removeUpdates(this)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0L, 0f, locationListener
                )
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener
                )
            } else {
                Toast.makeText(
                    this,
                    "Please enable Location (GPS) on this device.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun displayLocation(location: Location) {
        latitudeText.text = "Latitude: ${location.latitude}"
        longitudeText.text = "Longitude: ${location.longitude}"
    }
}
