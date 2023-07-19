package com.ismaelmachado.wifidirect.discovery

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice.AVAILABLE
import android.net.wifi.p2p.WifiP2pDevice.CONNECTED
import android.net.wifi.p2p.WifiP2pDevice.FAILED
import android.net.wifi.p2p.WifiP2pDevice.INVITED
import android.net.wifi.p2p.WifiP2pDevice.UNAVAILABLE

fun Context.getDeviceStatus(statusCode: Int) = when (statusCode) {
    CONNECTED -> getString(R.string.connected)
    INVITED -> getString(R.string.invited)
    FAILED -> getString(R.string.failed)
    AVAILABLE -> getString(R.string.available)
    UNAVAILABLE -> getString(R.string.unavailable)
    else -> getString(R.string.unknown)
}