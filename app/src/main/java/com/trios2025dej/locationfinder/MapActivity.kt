package com.trios2025dej.locationfinder

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale
import android.annotation.SuppressLint
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.core.app.ActivityCompat



class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var statusText: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    private var locationMarker: Marker? = null

    private var firstLocation = true

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        statusText = findViewById(R.id.statusText)

        mapView.setMultiTouchControls(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupLocationCallback()

        checkLocationPermission()

        val toolbar =
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkLocationPermission() {
        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )

        } else {

            startLocationUpdates()

        }
    }

    private fun setupLocationCallback() {

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateMapLocation(location)
                }
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {

        val locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                3000
            ).apply {
                setMinUpdateIntervalMillis(1000)
            }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )

    }

    private fun updateMapLocation(location: Location) {

        val latitude = location.latitude
        val longitude = location.longitude

        statusText.text =
            "Latitude: %6.f\nLongitude: %6.f"
                .format(latitude, longitude)

        val currentPosition =
            GeoPoint(latitude, longitude)

        if (locationMarker == null) {

            locationMarker = Marker(mapView).apply {
                position = currentPosition
                title = "Current Location"

                setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM
                )
            }

            mapView.overlays.add(locationMarker)

        }
        else {

            locationMarker?.position = currentPosition

        }

        if (firstLocation) {

            mapView.controller.setZoom(10.0)

            mapView.controller.setCenter(currentPosition)

            firstLocation = false

        }
        else {
            mapView.controller.animateTo(currentPosition)
        }

        mapView.invalidate()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if ( requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        }
        else {
            statusText.text =
                "Location permission was denied."
        }

    }

    override fun onResume() {
        super.onResume()

        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        fusedLocationClient.removeLocationUpdates(
            locationCallback
        )

        mapView.onPause()

    }

    override fun onSupportNavigateUp(): Boolean {

        onBackPressedDispatcher.onBackPressed()

        return true
    }

}