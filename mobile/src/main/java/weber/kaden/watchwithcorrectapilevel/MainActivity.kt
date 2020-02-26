package weber.kaden.watchwithcorrectapilevel

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream


private const val DATA_ITEM_PATH = "/event_list"

class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    override fun onDataChanged(buffer: DataEventBuffer) {

        for (event in buffer) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val asset =
                    dataMapItem.dataMap.getAsset(DATA_ITEM_PATH)
                GlobalScope.launch {
                    getFileFromAsset(applicationContext, asset)
                }
            }
        }
    }

    private suspend fun getFileFromAsset(context: Context, asset: Asset) {
        val assetInputStream: InputStream? =
            Tasks.await(Wearable.getDataClient(context).getFdForAsset(asset))?.inputStream

        if (assetInputStream != null) {
            val file = File(getStorageDir(), "accel_" + System.currentTimeMillis() + ".csv")
            file.copyInputStreamToFile(assetInputStream)
        }
    }

    fun getStorageDir(): String {
        return getExternalFilesDir(null)?.absolutePath!!
    }

}


fun File.copyInputStreamToFile(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
        inputStream.copyTo(fileOut)
    }
}
