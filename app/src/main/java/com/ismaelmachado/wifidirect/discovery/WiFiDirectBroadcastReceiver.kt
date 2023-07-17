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
import android.os.Parcelable
import android.util.Log

 /**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 * @param manager WifiP2pManager system service
 * @param channel Wifi p2p channel
 * @param activity activity associated with the receiver
 */
class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager?,
    private val channel: Channel,
    private val activity: Activity
) : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "WiFiDirectBroadcastReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, action)

        if (WIFI_P2P_CONNECTION_CHANGED_ACTION == action) {
            if (manager == null) return

            val networkInfo = intent
                .getParcelableExtra<Parcelable>(EXTRA_NETWORK_INFO) as NetworkInfo?
            if (networkInfo?.isConnected == true) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                Log.d(
                    TAG,
                    "Connected to p2p network. Requesting network details"
                )
                manager.requestConnectionInfo(channel, activity as ConnectionInfoListener)
            } else {
                // It's a disconnect
            }
        } else if (WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action) {
            val device = intent
                .getParcelableExtra<Parcelable>(EXTRA_WIFI_P2P_DEVICE) as WifiP2pDevice?
            Log.d(TAG, "Device status -" + device?.status)
        }
    }

}
