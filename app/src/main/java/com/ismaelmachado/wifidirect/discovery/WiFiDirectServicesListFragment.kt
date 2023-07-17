package com.ismaelmachado.wifidirect.discovery

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.net.wifi.p2p.WifiP2pDevice.AVAILABLE
import android.net.wifi.p2p.WifiP2pDevice.CONNECTED
import android.net.wifi.p2p.WifiP2pDevice.FAILED
import android.net.wifi.p2p.WifiP2pDevice.INVITED
import android.net.wifi.p2p.WifiP2pDevice.UNAVAILABLE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.ListFragment

/**
 * A simple ListFragment that shows the available services as published by the
 * peers
 */
class WiFiDirectServicesListFragment : ListFragment() {

    interface DeviceClickListener {
        fun connectP2p(wifiP2pService: WiFiP2pService?)
    }

    inner class WiFiDevicesAdapter(
        context: Context,
        resource: Int,
        textViewResourceId: Int,
        private val items: List<WiFiP2pService?>
    ) : ArrayAdapter<WiFiP2pService?>(context, resource, textViewResourceId, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: run {
                val inflater = activity?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                inflater.inflate(android.R.layout.simple_list_item_2, null)
            }
            val nameText = view.findViewById<View>(android.R.id.text1) as TextView
            val statusText = view.findViewById<View>(android.R.id.text2) as TextView
            val service = items[position] ?: return view
            nameText.text = "%s - %s".format(service.device?.deviceName, service.instanceName)
            statusText.text = getDeviceStatus(service.device?.status ?: -1)
            return view
        }
    }

    companion object {

        fun getDeviceStatus(statusCode: Int) = when (statusCode) {
            CONNECTED -> "Connected"
            INVITED -> "Invited"
            FAILED -> "Failed"
            AVAILABLE -> "Available"
            UNAVAILABLE -> "Unavailable"
            else -> "Unknown"
        }

    }

    private var listAdapter: WiFiDevicesAdapter? = null
    var deviceClickListener: DeviceClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.devices_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val localContext = context ?: return

        listAdapter = WiFiDevicesAdapter(
            localContext,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            ArrayList()
        )
        setListAdapter(listAdapter)
    }

    override fun onListItemClick(listView: ListView, view: View, position: Int, id: Long) {
        deviceClickListener?.connectP2p(listView.getItemAtPosition(position) as WiFiP2pService)
        (view.findViewById<View>(android.R.id.text2) as TextView).text =
            getString(R.string.connecting)
    }

}
