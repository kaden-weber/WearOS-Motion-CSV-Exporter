package weber.kaden.watchwithcorrectapilevel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.Button
import java.io.File
import java.io.FileWriter
import java.io.IOException


class MainActivity : WearableActivity(), SensorEventListener2 {

    private var mSensorManager: SensorManager? = null
    private var mSensor: Sensor? = null

    private var isRecording = false
    private var sensorEvents = mutableListOf<SensorEvent>()
    private var writer: FileWriter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Enables Always-on
        setAmbientEnabled()

        isRecording = false
        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)
        stopButton.isEnabled = false

        mSensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        var mSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)


        startButton.setOnClickListener {
            startButton.isEnabled = false
            stopButton.isEnabled = true
            isRecording = true

            val file = File(getStorageDir(), "accel_" + System.currentTimeMillis() + ".csv")
            try {
                val writer = FileWriter(file)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mSensorManager?.registerListener(this, mSensor, 0)
        }

        stopButton.setOnClickListener {
            startButton.isEnabled = true
            stopButton.isEnabled = false
            isRecording = false
            mSensorManager?.flush(this)
            mSensorManager?.unregisterListener(this)
            // send data
        }


    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onFlushCompleted(p0: Sensor?) {
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        var label: String = ""
        if(isRecording) {
            if(p0?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                label = "ACCEL"
                sensorEvents.add(p0)
            }
        }
        p0?.let {
//            writer!!.write(
//                String.format(
//                    "%d; %s; %f; %f; %f; %f; %f; %f\n",
//                    p0.timestamp, label, p0.values[0], p0.values[1], p0.values[2])
//            )
        }
    }

    fun getStorageDir(): String {
        return getExternalFilesDir(null)?.absolutePath!!
    }
}
