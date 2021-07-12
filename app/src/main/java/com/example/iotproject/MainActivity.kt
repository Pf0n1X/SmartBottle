package com.example.iotproject

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    companion object {
        val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        var devices: Set<BluetoothDevice>? = null;
        lateinit var devicesListView: ListView;
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        devicesListView = findViewById<ListView>(R.id.devices_list);
        pairedDevices()
    }

    private fun pairedDevices() {
        devices = adapter.bondedDevices;
        var list: ArrayList<String> = ArrayList<String>();

        // Fill the list with the devices and their names.
        if (devices?.size!! > 0) {
            for (bt in devices as MutableSet<BluetoothDevice>) {
                if (bt.name == "HC-05")
                    list.add(bt.name + "\n" + bt.address);
            }
        } else {

            // Show an alert if no device was found.
            Toast.makeText(applicationContext, resources.getString(R.string.bt_not_found_text), Toast.LENGTH_LONG).show();
        }

        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicesListView?.adapter = arrayAdapter;
        devicesListView?.setOnItemClickListener { av: AdapterView<*>?, v: View, arg2: Int, arg3: Long ->
            var name: String = (v as TextView).text.toString();
            var address: String = name.substring(name.length - 17);

            // Make an intent to start next activity.
            val i = Intent(this@MainActivity, Control::class.java)

            // Put the data got from device to the intent
            i.putExtra("add", address) // this will be received at control Activity

            startActivity(i)
        }
    }
}