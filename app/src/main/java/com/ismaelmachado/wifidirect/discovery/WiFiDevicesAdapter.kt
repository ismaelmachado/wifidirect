package com.ismaelmachado.wifidirect.discovery

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice.AVAILABLE
import android.net.wifi.p2p.WifiP2pDevice.CONNECTED
import android.net.wifi.p2p.WifiP2pDevice.FAILED
import android.net.wifi.p2p.WifiP2pDevice.INVITED
import android.net.wifi.p2p.WifiP2pDevice.UNAVAILABLE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.ismaelmachado.wifidirect.discovery.databinding.DeviceItemBinding

class WiFiDevicesAdapter(
    context: Context,
    resource: Int,
    textViewResourceId: Int,
    private val items: List<WiFiP2pServiceDto>
) : ArrayAdapter<WiFiP2pServiceDto?>(context, resource, textViewResourceId, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView != null) {
            DeviceItemBinding.bind(convertView)
        } else {
            DeviceItemBinding.inflate(LayoutInflater.from(context), parent, false)
        }
        with(items[position]) {
            binding.text1.text = "%s - %s".format(device?.deviceName, instanceName)
            binding.text2.text = context.getDeviceStatus(device?.status ?: -1)
        }
        return binding.root
    }

}