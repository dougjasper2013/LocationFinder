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
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

import java.util.Locale

class LiveMapLocationActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    private lateinit var addressText: TextView
    private lateinit var statusText: TextView

    private lateinit var mapView: MapView

    private lateinit var maplibreMap: MapLibreMap

    private lateinit var locationRequest: LocationRequest

    private lateinit var locationCallback: LocationCallback

    private var currentLocationMarker: Marker? = null

    private var mapIsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MapLibre.getInstance(this)

        setContentView(R.layout.activity_live_map_location)

        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        latitudeText =
            findViewById(R.id.latitudeText)

        longitudeText =
            findViewById(R.id.longitudeText)

        addressText =
            findViewById(R.id.addressText)

        statusText =
            findViewById(R.id.statusText)

        mapView =
            findViewById(R.id.mapView)

        mapView.getMapAsync { map ->

            maplibreMap = map

            maplibreMap.setStyle(
                "https://demotiles.maplibre.org/style.json"
            ) {

                mapIsReady = true

                statusText.text =
                    "Map ready. Waiting for location..."
            }
        }

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                2000L
            )
                .setMinUpdateIntervalMillis(1000L)
                .build()

        locationCallback =
            object : LocationCallback() {

                override fun onLocationResult(
                    locationResult: LocationResult
                ) {

                    for (location in locationResult.locations) {

                        displayLocation(location)
                    }
                }
            }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()

        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()

        mapView.onResume()

        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()

        stopLocationUpdates()

        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()

        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()

        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()

        mapView.onDestroy()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {

        super.onSaveInstanceState(outState)

        mapView.onSaveInstanceState(outState)
    }

    private fun startLocationUpdates() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(
                this,
                "Location permission is required.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )

        statusText.text =
            "Receiving live location updates..."
    }

    private fun stopLocationUpdates() {

        fusedLocationClient.removeLocationUpdates(
            locationCallback
        )

        statusText.text =
            "Location updates stopped."
    }

    private fun displayLocation(
        location: Location
    ) {

        val latitude =
            location.latitude

        val longitude =
            location.longitude

        latitudeText.text =
            String.format(
                Locale.US,
                "Latitude: %.6f",
                latitude
            )

        longitudeText.text =
            String.format(
                Locale.US,
                "Longitude: %.6f",
                longitude
            )

        getAddress(
            latitude,
            longitude
        )

        updateMapLocation(
            latitude,
            longitude
        )

        statusText.text =
            "Location updated"
    }

    private fun updateMapLocation(
        latitude: Double,
        longitude: Double
    ) {

        /*
         * Do nothing until the map and its style
         * have finished loading.
         */
        if (!mapIsReady) {
            return
        }

        val currentLatLng =
            LatLng(
                latitude,
                longitude
            )

        /*
         * If this is the first location, create
         * the marker.
         */
        if (currentLocationMarker == null) {

            currentLocationMarker =
                maplibreMap.addMarker(
                    MarkerOptions()
                        .position(currentLatLng)
                        .title("Current Location")
                )

            /*
             * Zoom to the initial location.
             */
            maplibreMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    currentLatLng,
                    5.0
                )
            )

        } else {

            /*
             * Move the existing marker.
             */
            currentLocationMarker?.position =
                currentLatLng

            /*
             * Redraw the marker in its new position.
             */
            maplibreMap.updateMarker(
                currentLocationMarker!!
            )

            /*
             * Move the map camera to follow
             * the current location.
             */
            maplibreMap.animateCamera(
                CameraUpdateFactory.newLatLng(
                    currentLatLng
                )
            )
        }
    }

    private fun getAddress(
        latitude: Double,
        longitude: Double
    ) {

        if (!Geocoder.isPresent()) {

            addressText.text =
                "Address lookup is not available on this device."

            return
        }

        val geocoder =
            Geocoder(
                this,
                Locale.getDefault()
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            /*
             * Android 13 / API 33 and newer.
             */
            geocoder.getFromLocation(
                latitude,
                longitude,
                1,
                object : Geocoder.GeocodeListener {

                    override fun onGeocode(
                        addresses: MutableList<Address>
                    ) {

                        runOnUiThread {

                            if (addresses.isNotEmpty()) {

                                addressText.text =
                                    addresses[0]
                                        .getAddressLine(0)

                            } else {

                                addressText.text =
                                    "No address found."
                            }
                        }
                    }

                    override fun onError(
                        errorMessage: String?
                    ) {

                        runOnUiThread {

                            addressText.text =
                                "Unable to determine address."
                        }
                    }
                }
            )

        } else {

            /*
             * Android 12 / API 32 and older.
             */
            Thread {

                try {

                    val addresses =
                        geocoder.getFromLocation(
                            latitude,
                            longitude,
                            1
                        )

                    runOnUiThread {

                        if (
                            !addresses.isNullOrEmpty()
                        ) {

                            addressText.text =
                                addresses[0]
                                    .getAddressLine(0)

                        } else {

                            addressText.text =
                                "No address found."
                        }
                    }

                } catch (e: Exception) {

                    runOnUiThread {

                        addressText.text =
                            "Unable to determine address."
                    }
                }

            }.start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {

        onBackPressedDispatcher.onBackPressed()

        return true
    }

}