package com.ismaelmachado.wifidirect.discovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import com.ismaelmachado.wifidirect.discovery.databinding.DeviceItemBinding
import com.ismaelmachado.wifidirect.discovery.databinding.FragmentDevicesListBinding

/**
 * A simple ListFragment that shows the available services as published by the
 * peers
 */
class DeviceListFragment : Fragment() {

    interface DeviceClickListener {
        fun connectP2p(wifiP2PServiceDto: WiFiP2pServiceDto?)
    }

    private lateinit var binding: FragmentDevicesListBinding
    private lateinit var listAdapter: WiFiDevicesAdapter
    var deviceClickListener: DeviceClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentDevicesListBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val localContext = context ?: return

        listAdapter = WiFiDevicesAdapter(
            localContext,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            ArrayList()
        )
        binding.list.adapter = listAdapter

        binding.list.onItemClickListener = OnItemClickListener { parent, itemView, position, _ ->
            val device = parent.getItemAtPosition(position) as WiFiP2pServiceDto
            deviceClickListener?.connectP2p(device)
            DeviceItemBinding.bind(itemView).text2.text = getString(R.string.connecting)
        }
    }

    fun addDevice(service: WiFiP2pServiceDto) {
        listAdapter.add(service)
        listAdapter.notifyDataSetChanged()
        binding.empty.visibility = GONE
    }

}
