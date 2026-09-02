package com.trios2025dej.locationfinder

import android.os.Bundle
import android.os.Build
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale
import android.content.Intent
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    private lateinit var addressText: TextView
    private lateinit var locationButton: Button

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineLocationGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            if (fineLocationGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Precise location permission is required.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar =
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        latitudeText = findViewById(R.id.latitudeText)
        longitudeText = findViewById(R.id.longitudeText)
        addressText = findViewById(R.id.addressText)
        locationButton = findViewById(R.id.locationButton)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        locationButton.setOnClickListener {
            checkLocationPermission()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.action_compass -> {

                val intent =
                    Intent(this, CompassActivity::class.java)

                startActivity(intent)

                true
            }

            R.id.action_live_location -> {

                val intent =
                    Intent(this, LiveLocationActivity::class.java)

                startActivity(intent)

                true
            }

            R.id.action_map -> {

                val intent =
                    Intent(this, MapActivity::class.java)

                startActivity(intent)

                true
            }

            R.id.action_live_map_location -> {

                val intent =
                    Intent(
                        this,
                        LiveMapLocationActivity::class.java
                    )

                startActivity(intent)

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkLocationPermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            getCurrentLocation()

        } else {

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getCurrentLocation() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        addressText.text = "Finding location..."

        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
            .addOnSuccessListener { location: Location? ->

                if (location != null) {

                    displayLocation(location)

                } else {

                    latitudeText.text = "--.------"
                    longitudeText.text = "--.------"
                    addressText.text =
                        "Unable to determine current location."
                }
            }
            .addOnFailureListener {

                addressText.text =
                    "Error obtaining location."

                Toast.makeText(
                    this,
                    "Unable to obtain location.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun displayLocation(location: Location) {

        val latitude = location.latitude
        val longitude = location.longitude

        latitudeText.text =
            String.format(Locale.US, "%.6f", latitude)

        longitudeText.text =
            String.format(Locale.US, "%.6f", longitude)

        getAddress(latitude, longitude)
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

        val geocoder = Geocoder(
            this,
            Locale.getDefault()
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            // Android 13 / API 33 and newer
            // Use the modern asynchronous Geocoder API.

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

                                val address = addresses[0]

                                addressText.text =
                                    address.getAddressLine(0)

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

            // Android 12 / API 32 and older
            // Use the older synchronous Geocoder API.
            //
            // This method is deprecated on API 33+,
            // but is still the appropriate API for
            // devices below API 33.

            Thread {

                try {

                    val addresses = geocoder.getFromLocation(
                        latitude,
                        longitude,
                        1
                    )

                    runOnUiThread {

                        if (!addresses.isNullOrEmpty()) {

                            val address = addresses[0]

                            addressText.text =
                                address.getAddressLine(0)

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

}