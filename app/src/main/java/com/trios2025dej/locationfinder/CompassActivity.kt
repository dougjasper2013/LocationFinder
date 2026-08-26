package com.trios2025dej.locationfinder

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.TextView
import kotlin.math.roundToInt
import android.widget.ImageView

class CompassActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var accelerometer: Sensor? = null
    private var magneticField: Sensor? = null

    private lateinit var compassImage: ImageView
    private lateinit var bearingText: TextView
    private lateinit var directionText: TextView

    private val gravity = FloatArray(3)
    private val magnetic = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var haveGravity = false
    private var haveMagnetic = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_compass)

        val toolbar =
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        compassImage =
            findViewById(R.id.compassImage)

        bearingText =
            findViewById(R.id.bearingText)

        directionText =
            findViewById(R.id.directionText)

        sensorManager =
            getSystemService(SENSOR_SERVICE) as SensorManager

        accelerometer =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            )

        magneticField =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD
            )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {

        onBackPressedDispatcher.onBackPressed()

        return true
    }

    override fun onResume() {
        super.onResume()

        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        magneticField?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {

        when (event.sensor.type) {

            Sensor.TYPE_ACCELEROMETER -> {

                gravity[0] = event.values[0]
                gravity[1] = event.values[1]
                gravity[2] = event.values[2]

                haveGravity = true
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {

                magnetic[0] = event.values[0]
                magnetic[1] = event.values[1]
                magnetic[2] = event.values[2]

                haveMagnetic = true
            }
        }

        if (haveGravity && haveMagnetic) {

            calculateBearing()
        }
    }

    private fun calculateBearing() {

        val success = SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            gravity,
            magnetic
        )

        if (!success) {
            return
        }

        SensorManager.getOrientation(
            rotationMatrix,
            orientation
        )

        var bearing =
            Math.toDegrees(
                orientation[0].toDouble()
            )

        if (bearing < 0) {
            bearing += 360.0
        }

        val roundedBearing =
            bearing.roundToInt()

        bearingText.text =
            "$roundedBearing°"

        directionText.text =
            getDirection(roundedBearing)

        compassImage.rotation =
            -bearing.toFloat()
    }

    private fun getDirection(
        bearing: Int
    ): String {

        return when (bearing) {

            in 0..22 -> "North"

            in 23..67 -> "Northeast"

            in 68..112 -> "East"

            in 113..157 -> "Southeast"

            in 158..202 -> "South"

            in 203..247 -> "Southwest"

            in 248..292 -> "West"

            in 293..337 -> "Northwest"

            else -> "North"
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // No action required.
    }

}