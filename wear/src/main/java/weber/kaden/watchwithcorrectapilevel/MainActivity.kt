package weber.kaden.watchwithcorrectapilevel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener2
import android.hardware.SensorManager
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.widget.Button
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.*
import java.io.*


private const val DATA_ITEM_PATH = "/event_list"

class MainActivity : WearableActivity(), SensorEventListener2, DataClient.OnDataChangedListener {

    private var mSensorManager: SensorManager? = null
    private var mAccelSensor: Sensor? = null
    private var mGyroSensor: Sensor? = null

    private var isRecording = false
    private var sensorEvents = mutableListOf<SensorEvent>()

    private var file: File? = null
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
        mAccelSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mGyroSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        startButton.setOnClickListener {
            startButton.isEnabled = false
            stopButton.isEnabled = true
            isRecording = true

            file = File(getStorageDir(), "accel_" + System.currentTimeMillis() + ".csv")
            try {
                writer = FileWriter(file)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mSensorManager?.registerListener(this, mAccelSensor, SensorManager.SENSOR_DELAY_FASTEST)
            mSensorManager?.registerListener(this, mGyroSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }

        stopButton.setOnClickListener {
            startButton.isEnabled = true
            stopButton.isEnabled = false
            isRecording = false
            mSensorManager?.flush(this)
            mSensorManager?.unregisterListener(this)

            writer!!.close()

            sendData()
        }


    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onFlushCompleted(p0: Sensor?) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        var label: String = ""
        if (isRecording) {
            val sensorType = event?.sensor?.type
            if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                label = "ACCEL"
                sensorEvents.add(event)
            }
            else if (sensorType == Sensor.TYPE_GYROSCOPE) {
                label = "GYRO"
            }

            event?.let {
                writer!!.write(
                    String.format(
                        "%d; %s; %f; %f; %f;\n",
                        event.timestamp, label, event.values[0], event.values[1], event.values[2]
                    )
                )
            }
        }
    }

    private fun getStorageDir(): String {
        return getExternalFilesDir(null)?.absolutePath!!
    }

    private fun sendData() {
        val asset = Asset.createFromBytes(file!!.readBytes())


        val putDataReq: PutDataRequest = PutDataMapRequest.create(DATA_ITEM_PATH).run {
            dataMap.putAsset(DATA_ITEM_PATH, asset)
            asPutDataRequest()
        }
        val dataClient: DataClient = Wearable.getDataClient(applicationContext)
        dataClient.putDataItem(putDataReq)
    }

    override fun onDataChanged(p0: DataEventBuffer) {

    }
}
