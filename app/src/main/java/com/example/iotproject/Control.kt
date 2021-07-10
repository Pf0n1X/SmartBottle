package com.example.iotproject

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ubidots.ApiClient
import com.ubidots.Value
import com.ubidots.Variable
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import me.itangqi.waveloadingview.*;
import java.text.SimpleDateFormat
import kotlin.math.PI

class Control : AppCompatActivity() {

    companion object {
        lateinit var bluetooth: BluetoothAdapter;
        lateinit var btSocket: BluetoothSocket;
        val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        lateinit var btnGetData: Button;
        lateinit var waterAmountView: WaveLoadingView;
        lateinit var waterDrankView: WaveLoadingView;
        const val BOTTLE_FULL_CONTENTS: Float = 400.0f;
        const val BOTTLE_HEIGHT_IN_CM: Float = 10.0f;
        const val BOTTLE_BOTTOM_RADIUS = 3.0;
        const val BOTTLE_TOP_RADIUS = 4.25;
        const val ACCEPTED_ERROR = 35;
        const val RECOMMENDED_WATER_AMOUNT_PER_DAY = 3700;
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        btnGetData = findViewById<Button>(R.id.get_data);
        waterAmountView = findViewById(R.id.water_amount);
        waterDrankView = findViewById(R.id.water_drank);
        btnGetData.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getData()
            };
        }

//// This UUID is unique and fix id for this device
//        static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        var address: String? = intent.getStringExtra("add");

        try {
            // This will connect the device with address as passed
            bluetooth = BluetoothAdapter.getDefaultAdapter();
            var hc: BluetoothDevice = bluetooth.getRemoteDevice(address);
            btSocket = hc.createInsecureRfcommSocketToServiceRecord(myUUID)!!;

            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

//          Now you will start the connection
            btSocket.connect();
            getData();
        } catch (e: IOException) {
            e.printStackTrace();
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun getData() {
        var byteCount: Int = btSocket.inputStream.available();
        if (byteCount != 0) {
            val buffer = ByteArray(byteCount)
            val bytes: Int = btSocket.inputStream.read(buffer);

            val readMessage = String(buffer, 0, bytes);
            var res: String = "";
            for (b in buffer) {
                res += " " + b;
            }
            System.out.println("Message :: $readMessage");
            var respArray: List<String> = readMessage.replace("\r", "").split("\n");

            var distanceInCM: Float = respArray[respArray.size - 2].toFloat() - 0.5f;

            // PREV
//                var currentBottleContent: Float = Math.max(
//                    bottleFullContents - distanceInCM.toInt() * bottleFullContents / bottleHeightInCm,
//                    0.0f
//                );
            // PREV

            val T: Float = (PI.toFloat() / 3) *
                    (Math.pow(BOTTLE_BOTTOM_RADIUS, 2.0) +
                            Math.pow(BOTTLE_TOP_RADIUS, 2.0) +
                            BOTTLE_TOP_RADIUS * BOTTLE_BOTTOM_RADIUS).toFloat();
            var currentBottleContent: Float = Math.max(
                (BOTTLE_HEIGHT_IN_CM - distanceInCM) * T,
                0.0f
            );

            waterAmountView.progressValue =
                (currentBottleContent / BOTTLE_FULL_CONTENTS * 100).toInt();
            waterAmountView.centerTitle = currentBottleContent.toInt().toString();
            ApiUbidots().execute(currentBottleContent.toInt());
        }
    }

    inner class ApiUbidots : AsyncTask<Int?, Void?, Double?>() {
        private val API_KEY = "BBFF-a4095742cfa851bd3cf35a92bf04aa04fcf"
        private val WATER_LEVEL_VARIABLE_ID = "60d8cff31d8472463cd36846"
        private val WATER_DRANK_VARIABLE_ID = "60db576e1d84726d361c0b1d"

        override fun onPostExecute(result: Double?) {
            super.onPostExecute(result)

            if (result != null) {
                waterDrankView.progressValue = (result.toFloat() / RECOMMENDED_WATER_AMOUNT_PER_DAY * 100).toInt();
                waterDrankView.centerTitle = (result).toInt().toString() + " / " + RECOMMENDED_WATER_AMOUNT_PER_DAY;
            }
        }

        override fun doInBackground(vararg params: Int?): Double? {
            val apiClient = ApiClient(API_KEY)
            val waterLevel: Variable = apiClient.getVariable(WATER_LEVEL_VARIABLE_ID)
            val waterDrank: Variable = apiClient.getVariable(WATER_DRANK_VARIABLE_ID)

            params[0]?.let {
                val lastVal: Double = waterLevel.values.get(0).value;
                val waterDiff: Double = lastVal - it;

                if (waterDiff > ACCEPTED_ERROR) {
                    waterDrank.saveValue(waterDiff.toInt());
                }

                waterLevel.saveValue(it.toInt());

                val sameDayFilter: List<Value> = waterDrank.values.filter { v ->
                    var date: Date = Date(v.timestamp);
                    var curDate: Date = Date();
                    val fmt: SimpleDateFormat = SimpleDateFormat("yyyMMdd");
                    fmt.timeZone = TimeZone.getTimeZone("GMT+3");
                    fmt.format(date).equals(fmt.format(curDate));
                };

                val sameDaySum: Double = sameDayFilter.sumByDouble { v ->
                    v.value
                };

                return sameDaySum;
            }

            return null
        }
    }
}