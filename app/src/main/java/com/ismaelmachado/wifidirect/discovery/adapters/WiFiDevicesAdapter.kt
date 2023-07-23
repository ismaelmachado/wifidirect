package com.ismaelmachado.wifidirect.discovery.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.ismaelmachado.wifidirect.discovery.databinding.DeviceItemBinding
import com.ismaelmachado.wifidirect.discovery.dtos.WiFiP2pServiceDto
import com.ismaelmachado.wifidirect.discovery.getDeviceStatus

class WiFiDevicesAdapter(
    context: Context,
    resource: Int,
    textViewResourceId: Int,
    private val items: List<WiFiP2pServiceDto>
) : ArrayAdapter<WiFiP2pServiceDto?>(context, resource, textViewResourceId, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            DeviceItemBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            DeviceItemBinding.bind(convertView)
        }
        with(items[position]) {
            binding.text1.text = "%s - %s".format(device?.deviceName, instanceName)
            binding.text2.text = context.getDeviceStatus(device?.status ?: -1)
        }
        return binding.root
    }

}