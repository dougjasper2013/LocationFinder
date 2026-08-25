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

class CompassActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager

    private var accelerometer: Sensor? = null
    private var magneticField: Sensor? = null

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}