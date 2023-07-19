@file:Suppress("DEPRECATION")

package com.ismaelmachado.wifidirect.discovery

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
import android.net.wifi.p2p.WifiP2pManager.EXTRA_NETWORK_INFO
import android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_P2P_DEVICE
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Parcelable
import android.util.Log

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 * @param manager WifiP2pManager system service
 * @param channel Wifi p2p channel
 * @param listener associated with the receiver
 */
class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager?,
    private val channel: Channel?,
    private val listener: ConnectionInfoListener
) : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "WiFiDirectBroadcastReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, action)

        if (WIFI_P2P_CONNECTION_CHANGED_ACTION == action) {
            val networkInfo = intent.getExtra(EXTRA_NETWORK_INFO, NetworkInfo::class.java)
            if (networkInfo?.isConnected == true) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                Log.d(
                    TAG,
                    "Connected to p2p network. Requesting network details"
                )
                manager?.requestConnectionInfo(channel, listener)
            } else {
                // It's a disconnect
            }
        } else if (WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action) {
            val device = intent.getExtra(EXTRA_WIFI_P2P_DEVICE, WifiP2pDevice::class.java)
            Log.d(TAG, "Device status - %s".format(context.getDeviceStatus(device?.status ?: -1)))
        }
    }

     @Suppress("UNCHECKED_CAST")
     private fun <T> Intent.getExtra(name: String, clazz: Class<T>): T? {
         return if (SDK_INT >= TIRAMISU) {
             getParcelableExtra(name, clazz)
         } else {
             getParcelableExtra<Parcelable>(name) as T?
         }
     }

}
