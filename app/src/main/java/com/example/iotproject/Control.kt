package com.example.iotproject

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ubidots.ApiClient
import com.ubidots.Value
import com.ubidots.Variable
import me.itangqi.waveloadingview.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI


class Control : AppCompatActivity() {

    companion object {
        lateinit var bluetooth: BluetoothAdapter;
        lateinit var btSocket: BluetoothSocket;
        val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        lateinit var btnGetData: Button;
        lateinit var tvTimestamp: TextView;
        lateinit var waterAmountView: WaveLoadingView;
        lateinit var waterDrankView: WaveLoadingView;
        lateinit var toast: Toast;
        const val BOTTLE_FULL_CONTENTS: Float = 400.0f;
        const val BOTTLE_HEIGHT_IN_CM: Float = 10.0f;
        const val BOTTLE_BOTTOM_RADIUS = 3.0;
        const val BOTTLE_TOP_RADIUS = 4.25;
        const val ACCEPTED_ERROR = 35;
        const val RECOMMENDED_WATER_AMOUNT_PER_DAY = 3700;
        const val CHANNEL_ID: String = "smart_bottle_channel";
        const val NOTIFICATION_DELAY = 1000 * 60 * 60 * 2; // 2 hours
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        btnGetData = findViewById<Button>(R.id.get_data);
        tvTimestamp = findViewById<TextView>(R.id.tv_timestamp);
        waterAmountView = findViewById(R.id.water_amount);
        waterDrankView = findViewById(R.id.water_drank);
        btnGetData.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getData()
            };
        }

        createNotificationChannel();

        toast = Toast.makeText(this, "Attempting Connection...", Toast.LENGTH_SHORT);
        toast.show();
        BluetoothConnect(this).execute();
    }
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun getData() {
        val byteCount: Int = btSocket.inputStream.available();

        if (byteCount != 0) {
            val buffer = ByteArray(byteCount)
            val bytes: Int = btSocket.inputStream.read(buffer);
            val readMessage = String(buffer, 0, bytes);
            val respArray: List<String> = readMessage.replace("\r", "").split("\n");
            val distanceInCM: Float = respArray[respArray.size - 2].toFloat() - 0.5f;
            val T: Float = (PI.toFloat() / 3) *
                    (Math.pow(BOTTLE_BOTTOM_RADIUS, 2.0) +
                            Math.pow(BOTTLE_TOP_RADIUS, 2.0) +
                            BOTTLE_TOP_RADIUS * BOTTLE_BOTTOM_RADIUS).toFloat();
            val calculatedContents: Float = ((BOTTLE_HEIGHT_IN_CM - distanceInCM) * T);
            val currentBottleContent: Float = Math.max(
                calculatedContents,
                0.0f
            );
            waterAmountView.progressValue =
                (currentBottleContent / BOTTLE_FULL_CONTENTS * 100).toInt();
            waterAmountView.centerTitle = currentBottleContent.toInt().toString();
            waterDrankView.centerTitle = resources.getString(R.string.loading_text);
            ApiUbidots().execute(currentBottleContent.toInt());
        }
    }

    private fun createNotificationChannel() {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendDrinkingReminder() {
        val intent: Intent = Intent(this, ReminderBroadcast::class.java);
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        val alarmManager: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager;
        val notifTime: Long = System.currentTimeMillis() + NOTIFICATION_DELAY;
        alarmManager.set(AlarmManager.RTC_WAKEUP, notifTime, pendingIntent);
    }

    inner class BluetoothConnect(context: Context): AsyncTask<Void?, Void?, Void?>() {

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            btnGetData.isEnabled = true;
            toast.cancel();
            getData();
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun doInBackground(vararg params: Void?): Void? {

            //  This UUID is unique and fix id for this device
            //  static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            val address: String? = intent.getStringExtra("add");

            try {
                // This will connect the device with address as passed
                bluetooth = BluetoothAdapter.getDefaultAdapter();
                var hc: BluetoothDevice = bluetooth.getRemoteDevice(address);
                btSocket = hc.createInsecureRfcommSocketToServiceRecord(myUUID)!!;

                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                // Now you will start the connection
                if (!btSocket.isConnected) {
                    btSocket.connect();
                }
            } catch (e: IOException) {
                e.printStackTrace();
                finish()
            }

            return null;
        }
    }

    inner class ApiUbidots : AsyncTask<Int?, Void?, Double?>() {
        private val API_KEY = "BBFF-a4095742cfa851bd3cf35a92bf04aa04fcf"
        private val WATER_LEVEL_VARIABLE_ID = "60d8cff31d8472463cd36846"
        private val WATER_DRANK_VARIABLE_ID = "60db576e1d84726d361c0b1d"

        override fun onPostExecute(result: Double?) {
            super.onPostExecute(result)

            if (result != null) {
                waterDrankView.progressValue =
                    (result.toFloat() / RECOMMENDED_WATER_AMOUNT_PER_DAY * 100).toInt();
                waterDrankView.centerTitle =
                    (result).toInt().toString() + " / " + RECOMMENDED_WATER_AMOUNT_PER_DAY;
            }
        }

        override fun doInBackground(vararg params: Int?): Double? {
            val apiClient = ApiClient(API_KEY)
            val waterLevel: Variable = apiClient.getVariable(WATER_LEVEL_VARIABLE_ID)
            val waterDrank: Variable = apiClient.getVariable(WATER_DRANK_VARIABLE_ID)

            params[0]?.let {
                val lastVal: Double = waterLevel.values.get(0).value;
                val waterDiff: Double = lastVal - it;
                val lastDrinkingTimestamp = waterDrank.values.get(0).timestamp;

                // Check if the water change wasn't greater than the allowed error range.
                if (waterDiff > ACCEPTED_ERROR) {
                    waterDrank.saveValue(waterDiff.toInt());
                    sendDrinkingReminder();
                    Handler(Looper.getMainLooper()).post(Runnable {
                        val fmt: SimpleDateFormat = SimpleDateFormat("HH:mm dd.MM.yyyy");
                        fmt.timeZone = TimeZone.getTimeZone("GMT+3");
                        tvTimestamp.text = fmt.format(Date().time);
                    });
                } else {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        val fmt: SimpleDateFormat = SimpleDateFormat("HH:mm dd.MM.yyyy");
                        fmt.timeZone = TimeZone.getTimeZone("GMT+3");
                        tvTimestamp.text = fmt.format(lastDrinkingTimestamp);
                    });
                }

                // Save the current water level to the cloud.
                waterLevel.saveValue(it.toInt());

                // Filter the cloud data so that the app only has the current day's data left.
                val sameDayFilter: List<Value> = waterDrank.values.filter { v ->
                    var date: Date = Date(v.timestamp);
                    var curDate: Date = Date();
                    val fmt: SimpleDateFormat = SimpleDateFormat("yyyMMdd");
                    fmt.timeZone = TimeZone.getTimeZone("GMT+3");
                    fmt.format(date).equals(fmt.format(curDate));
                };

                // Sum the current day's values.
                val sameDaySum: Double = sameDayFilter.sumByDouble { v ->
                    v.value
                };

                return sameDaySum;
            }

            return null
        }
    }
}