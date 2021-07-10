//package com.example.iotproject
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.content.Context
//import android.content.Intent
//import android.content.pm.ActivityInfo
//import android.graphics.Color
//import android.os.AsyncTask
//import android.os.Bundle
//import android.preference.PreferenceManager
//import android.util.Log
//import android.view.*
//import android.widget.*
//import android.widget.AdapterView.OnItemClickListener
//import androidx.appcompat.app.AppCompatActivity
//import java.util.*
//
//class MainActivity : AppCompatActivity() {
//    private var search: Button? = null
//    private var connect: Button? = null
//    private var listView: ListView? = null
//    private var mBTAdapter: BluetoothAdapter? = null
//    private var mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//    private var mBufferSize = 50000 //Default
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        search = findViewById<View>(R.id.search) as Button
//        connect = findViewById<View>(R.id.connect) as Button
//        listView = findViewById<View>(R.id.listview) as ListView
//        if (savedInstanceState != null) {
//            val list = savedInstanceState.getParcelableArrayList<BluetoothDevice>(DEVICE_LIST)
//            if (list != null) {
//                initList(list)
//                val adapter = listView!!.adapter as MyAdapter
//                val selectedIndex = savedInstanceState.getInt(DEVICE_LIST_SELECTED)
//                if (selectedIndex != -1) {
//                    adapter.setSelectedIndex(selectedIndex)
//                    connect?.isEnabled = true
//                }
//            } else {
//                initList(ArrayList())
//            }
//        } else {
//            initList(ArrayList())
//        }
//        search!!.setOnClickListener {
//            mBTAdapter = BluetoothAdapter.getDefaultAdapter()
//            if (mBTAdapter == null) {
//                Toast.makeText(applicationContext, "Bluetooth not found", Toast.LENGTH_SHORT).show()
//            } else if (!mBTAdapter?.isEnabled!!) {
//                val enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                startActivityForResult(enableBT, BT_ENABLE_REQUEST)
//            } else {
//                SearchDevices().execute()
//            }
//        }
//        connect!!.setOnClickListener {
//            val device = (listView!!.adapter as MyAdapter).selectedItem
//            val intent = Intent(applicationContext, Controlling::class.java)
//            intent.putExtra(DEVICE_EXTRA, device)
//            intent.putExtra(DEVICE_UUID, mDeviceUUID.toString())
//            intent.putExtra(BUFFER_SIZE, mBufferSize)
//            startActivity(intent)
//        }
//    }
//
//    override fun onPause() {
//// TODO Auto-generated method stub
//        super.onPause()
//    }
//
//    override fun onStop() {
//// TODO Auto-generated method stub
//        super.onStop()
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        when (requestCode) {
//            BT_ENABLE_REQUEST -> if (resultCode == RESULT_OK) {
//                msg("Bluetooth Enabbulb successfully")
//                SearchDevices().execute()
//            } else {
//                msg("Bluetooth couldn't be enabbulb")
//            }
//            SETTINGS -> {
//                val prefs = PreferenceManager.getDefaultSharedPreferences(this)
//                val uuid = prefs.getString("prefUuid", "Null")
//                mDeviceUUID = UUID.fromString(uuid)
//                Log.d(TAG, "UUID: $uuid")
//                val bufSize = prefs.getString("prefTextBuffer", "Null")
//                mBufferSize = bufSize!!.toInt()
//                val orientation = prefs.getString("prefOrientation", "Null")
//                Log.d(TAG, "Orientation: $orientation")
//                if (orientation == "Landscape") {
//                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//                } else if (orientation == "Portrait") {
//                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//                } else if (orientation == "Auto") {
//                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
//                }
//            }
//            else -> {
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }
//
//    /**
//     * Quick way to call the Toast
//     * @param str
//     */
//    private fun msg(str: String) {
//        Toast.makeText(applicationContext, str, Toast.LENGTH_SHORT).show()
//    }
//
//    /**
//     * Initialize the List adapter
//     * @param objects
//     */
//    private fun initList(objects: List<BluetoothDevice>) {
//        val adapter: MyAdapter = MyAdapter(applicationContext, R.layout.list_item, R.id.lstContent, objects)
//        listView!!.adapter = adapter
//        listView!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
//            adapter.setSelectedIndex(position)
//            connect.setEnabbulb(true)
//        }
//    }
//
//    /**
//     * Searches for paired devices. Doesn't do a scan! Only devices which are paired through Settings->Bluetooth
//     * will show up with this. I didn't see any need to re-build the wheel over here
//     * @author ryder
//     */
//    private inner class SearchDevices : AsyncTask<Void?, Void?, List<BluetoothDevice>>() {
//        protected override fun doInBackground(vararg params: Void): List<BluetoothDevice> {
//            val pairedDevices = mBTAdapter!!.bondedDevices
//            val listDevices: MutableList<BluetoothDevice> = ArrayList()
//            for (device in pairedDevices) {
//                listDevices.add(device)
//            }
//            return listDevices
//        }
//
//        override fun onPostExecute(listDevices: List<BluetoothDevice>) {
//            super.onPostExecute(listDevices)
//            if (listDevices.size > 0) {
//                val adapter = listView!!.adapter as MyAdapter
//                adapter.replaceItems(listDevices)
//            } else {
//                msg("No paired devices found, please pair your serial BT device and try again")
//            }
//        }
//    }
//
//    /**
//     * Custom adapter to show the current devices in the list. This is a bit of an overkill for this
//     * project, but I figured it would be good learning
//     * Most of the code is lifted from somewhere but I can't find the link anymore
//     * @author ryder
//     */
//    private inner class MyAdapter(private val context: Context, resource: Int, textViewResourceId: Int, var entireList: List<BluetoothDevice>) : ArrayAdapter<BluetoothDevice>(context, resource, textViewResourceId, entireList) {
//        private var selectedIndex: Int
//        private val selectedColor = Color.parseColor("#abcdef")
//
//        fun setSelectedIndex(position: Int) {
//            selectedIndex = position
//            notifyDataSetChanged()
//        }
//
//        val selectedItem: BluetoothDevice
//            get() = entireList[selectedIndex]
//
//        override fun getCount(): Int {
//            return entireList.size
//        }
//
//        override fun getItem(position: Int): BluetoothDevice {
//            return entireList[position]
//        }
//
//        override fun getItemId(position: Int): Long {
//            return position.toLong()
//        }
//
//        private inner class ViewHolder {
//            var tv: TextView? = null
//        }
//
//        fun replaceItems(list: List<BluetoothDevice>) {
//            entireList = list
//            notifyDataSetChanged()
//        }
//
//        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//            var vi = convertView
//            val holder: ViewHolder
//            if (convertView == null) {
//                vi = LayoutInflater.from(context).inflate(R.layout.list_item, null)
//                holder = ViewHolder()
//                holder.tv = vi!!.findViewById<View>(R.id.lstContent) as TextView
//                vi.tag = holder
//            } else {
//                holder = vi!!.tag as ViewHolder
//            }
//            if (selectedIndex != -1 && position == selectedIndex) {
//                holder.tv!!.setBackgroundColor(selectedColor)
//            } else {
//                holder.tv!!.setBackgroundColor(Color.WHITE)
//            }
//            val device = entireList[position]
//            holder.tv!!.text = """${device.name}
// ${device.address}"""
//            return vi
//        }
//
//        init {
//            selectedIndex = -1
//        }
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//// Inflate the menu; this adds items to the action bar if it is present.
//        //getMenuInflater().inflate(R.menu.homescreen, menu);
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.action_settings -> {
//                val intent = Intent(this@MainActivity, PreferencesActivity::class.java)
//                startActivityForResult(intent, SETTINGS)
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    companion object {
//        private const val BT_ENABLE_REQUEST = 10 // This is the code we use for BT Enable
//        private const val SETTINGS = 20
//        const val DEVICE_EXTRA = "com.example.bluetoothlight.SOCKET"
//        const val DEVICE_UUID = "com.example.bluetoothlight.uuid"
//        private const val DEVICE_LIST = "com.example.bluetoothlight.devicelist"
//        private const val DEVICE_LIST_SELECTED = "com.example.bluetoothlight.devicelistselected"
//        const val BUFFER_SIZE = "com.example.bluetoothlight.buffersize"
//        private const val TAG = "BlueTest5-MainActivity"
//    }
//}
package com.example.iotproject

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ubidots.ApiClient
import com.ubidots.DataSource
import com.ubidots.Variable


class MainActivity : AppCompatActivity() {
    companion object {
        val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        var devices: Set<BluetoothDevice>? = null;
        var devicesListView: ListView? = null;
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        devicesListView = findViewById<ListView>(R.id.devices_list);

//        val api: ApiClient = ApiClient("");
//        val ds: DataSource = api.createDataSource("myNewDS")
//        val variable: Variable = ds.createVariable("myNewVar", "CM");
//        variable.saveValue(10);
        pairedDevices()
    }

    fun pairedDevices() {
        devices = adapter.bondedDevices;
        var list: ArrayList<String> = ArrayList<String>();

        if (devices?.size!! > 0) {
            for (bt in devices as MutableSet<BluetoothDevice>) {
                list.add(bt.name + "\n" + bt.address);
            }
        } else {
            Toast.makeText(applicationContext, "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicesListView?.adapter = arrayAdapter;
        devicesListView?.setOnItemClickListener { av: AdapterView<*>?, v: View, arg2: Int, arg3: Long ->
            var name: String = (v as TextView).text.toString();
            var address: String = name.substring(name.length - 17);

            // Make an intent to start next activity.
            // Make an intent to start next activity.
            val i = Intent(this@MainActivity, Control::class.java)
            // Put the data got from device to the intent
            // Put the data got from device to the intent
            i.putExtra("add", address) // this will be received at control Activity

            startActivity(i)
        }


    }
}