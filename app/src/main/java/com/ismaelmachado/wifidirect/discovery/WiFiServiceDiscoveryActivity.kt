package com.ismaelmachado.wifidirect.discovery

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo.PBC
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Bundle
import android.os.Handler
import android.os.Handler.Callback
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ismaelmachado.wifidirect.discovery.WiFiChatFragment.MessageTarget
import com.ismaelmachado.wifidirect.discovery.WiFiDirectServicesListFragment.DeviceClickListener
import com.ismaelmachado.wifidirect.discovery.WiFiDirectServicesListFragment.WiFiDevicesAdapter
import java.io.IOException

class WiFiServiceDiscoveryActivity : AppCompatActivity(), DeviceClickListener, Callback,
    MessageTarget, ConnectionInfoListener {

    companion object {
        const val MESSAGE_READ = 0x400 + 1
        const val MY_HANDLE = 0x400 + 2
        const val SERVER_PORT = 4545

        private const val TAG = "wifidirectdemo"

        // TXT RECORD properties
        private const val TXTRECORD_PROP_AVAILABLE = "available"
        private const val SERVICE_INSTANCE = "_wifidemotest"
        private const val SERVICE_REG_TYPE = "_presence._tcp"

        private const val PERMISSIONS_REQUEST_CODE = 1001
    }

    override val handler: Handler = Handler(this)

    private var manager: WifiP2pManager? = null
    private val intentFilter = IntentFilter()
    private var channel: WifiP2pManager.Channel? = null
    private var receiver: BroadcastReceiver? = null

    private var serviceRequest: WifiP2pDnsSdServiceRequest? = null
    private var chatFragment: WiFiChatFragment? = null
    private var servicesList: WiFiDirectServicesListFragment? = null

    private var statusTxtView: TextView? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> if (grantResults[0] != PERMISSION_GRANTED) {
                Log.e(TAG, "Fine location permission is not granted!")
                finish()
            } else {
                startRegistrationAndDiscovery()
            }
        }
    }

    private fun initP2p(): Boolean {
        // Device capability definition check
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Log.e(TAG, "Wi-Fi Direct is not supported by this device.")
            return false
        }

        // Hardware capability check
        val wifiManager = getSystemService(WIFI_SERVICE) as? WifiManager
        if (wifiManager == null) {
            Log.e(TAG, "Cannot get Wi-Fi system service.")
            return false
        }
        if (!wifiManager.isP2pSupported) {
            Log.e(TAG, "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.")
            return false
        }
        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        if (manager == null) {
            Log.e(TAG, "Cannot get Wi-Fi Direct system service.")
            return false
        }
        channel = manager?.initialize(this, mainLooper, null)
        if (channel == null) {
            Log.e(TAG, "Cannot initialize Wi-Fi Direct.")
            return false
        }
        return true
    }

    /** Called when the activity is first created.  */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        statusTxtView = findViewById<View>(R.id.status_text) as TextView
        intentFilter.addAction(WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        if (!initP2p()) {
            finish()
        }
        val fragment = WiFiDirectServicesListFragment()
        servicesList = fragment
        supportFragmentManager
            .beginTransaction()
            .add(R.id.container_root, fragment, "services")
            .commit()
        if (checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
            // After this point you wait for callback in
            // onRequestPermissionsResult(int, String[], int[]) overridden method
        } else {
            startRegistrationAndDiscovery()
        }
    }

    override fun onRestart() {
        val fragment = supportFragmentManager.findFragmentByTag("services")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
        super.onRestart()
    }

    override fun onStop() {
        if (manager == null || channel == null) return

        manager?.removeGroup(channel, object : ActionListener {

            override fun onFailure(reasonCode: Int) {
                Log.d(TAG, "Disconnect failed. Reason :$reasonCode")
            }

            override fun onSuccess() {}

        })
        super.onStop()
    }

    /**
     * Registers a local service and then initiates a service discovery
     */
    @SuppressLint("MissingPermission")
    private fun startRegistrationAndDiscovery() {
        val record = HashMap<String, String>()
        record[TXTRECORD_PROP_AVAILABLE] = "visible"
        val service = WifiP2pDnsSdServiceInfo.newInstance(
            SERVICE_INSTANCE,
            SERVICE_REG_TYPE,
            record
        )
        manager?.addLocalService(channel, service, object : ActionListener {
            override fun onSuccess() {
                appendStatus("Added Local Service")
            }

            override fun onFailure(error: Int) {
                appendStatus("Failed to add a service")
            }
        })
        discoverService()
    }

    @SuppressLint("MissingPermission")
    private fun discoverService() {

        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */
        manager?.setDnsSdResponseListeners(
            channel,
            { instanceName, registrationType, srcDevice ->
                // A service has been discovered. Is this our app?
                if (instanceName.equals(SERVICE_INSTANCE, ignoreCase = true)) {

                    // update the UI and add the item the discovered
                    // device.
                    val fragment = supportFragmentManager
                        .findFragmentByTag("services") as WiFiDirectServicesListFragment
                    fragment.deviceClickListener = this
                    val adapter: WiFiDevicesAdapter = fragment.listAdapter as WiFiDevicesAdapter
                    val service = WiFiP2pService()
                    service.device = srcDevice
                    service.instanceName = instanceName
                    service.serviceRegistrationType = registrationType
                    adapter.add(service)
                    adapter.notifyDataSetChanged()
                    Log.d(TAG, "onBonjourServiceAvailable $instanceName")
                }
            }
        ) { _, record, device ->

            /**
             * A new TXT record is available. Pick up the advertised
             * buddy name.
             */
            Log.d(TAG, "${device.deviceName} is ${record[TXTRECORD_PROP_AVAILABLE]}")
        }

        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance()
        manager?.addServiceRequest(channel, serviceRequest,
            object : ActionListener {
                override fun onSuccess() {
                    appendStatus("Added service discovery request")
                }

                override fun onFailure(arg0: Int) {
                    appendStatus("Failed adding service discovery request")
                }
            })
        manager?.discoverServices(channel, object : ActionListener {
            override fun onSuccess() {
                appendStatus("Service discovery initiated")
            }

            override fun onFailure(arg0: Int) {
                appendStatus("Service discovery failed")
            }
        })
    }

    @SuppressLint("MissingPermission")
    override fun connectP2p(wifiP2pService: WiFiP2pService?) {
        val config = WifiP2pConfig()
        config.deviceAddress = wifiP2pService?.device?.deviceAddress
        config.wps.setup = PBC
        if (serviceRequest != null) manager?.removeServiceRequest(
            channel,
            serviceRequest,
            object : ActionListener {
                override fun onSuccess() {}
                override fun onFailure(arg0: Int) {}
            }
        )
        manager?.connect(channel, config, object : ActionListener {
            override fun onSuccess() {
                appendStatus("Connecting to service")
            }

            override fun onFailure(errorCode: Int) {
                appendStatus("Failed connecting to service")
            }
        })
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MESSAGE_READ -> {
                val readBuf = msg.obj as ByteArray
                // construct a string from the valid bytes in the buffer
                val readMessage = String(readBuf, 0, msg.arg1)
                Log.d(TAG, readMessage)
                chatFragment?.pushMessage("Buddy: $readMessage")
            }

            MY_HANDLE -> {
                chatFragment?.chatManager = msg.obj as ChatManager
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        receiver = WiFiDirectBroadcastReceiver(manager, channel ?: return, this)
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    override fun onConnectionInfoAvailable(p2pInfo: WifiP2pInfo) {
        val handler: Thread?
        /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}
         */
        if (p2pInfo.isGroupOwner) {
            Log.d(TAG, "Connected as group owner")
            try {
                handler = GroupOwnerSocketHandler((this as MessageTarget).handler)
                handler.start()
            } catch (e: IOException) {
                Log.d(TAG, "Failed to create a server thread - ${e.message}")
                return
            }
        } else {
            Log.d(TAG, "Connected as peer")
            val targetHandler = (this as MessageTarget).handler ?: return
            handler = ClientSocketHandler(targetHandler, p2pInfo.groupOwnerAddress)
            handler.start()
        }
        val fragment = WiFiChatFragment()
        chatFragment = fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_root, fragment)
            .commit()
        statusTxtView?.visibility = View.GONE
    }

    fun appendStatus(status: String) {
        val current = statusTxtView?.text.toString()
        statusTxtView?.text = getString(R.string.status, current, status)
    }

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KotlinWifiDirectServiceDiscoveryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
    */

}

/*
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KotlinWifiDirectServiceDiscoveryTheme {
        Greeting("Android")
    }
}
*/