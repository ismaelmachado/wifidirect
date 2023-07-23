package com.ismaelmachado.wifidirect.discovery

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice.AVAILABLE
import android.net.wifi.p2p.WifiP2pDevice.CONNECTED
import android.net.wifi.p2p.WifiP2pDevice.FAILED
import android.net.wifi.p2p.WifiP2pDevice.INVITED
import android.net.wifi.p2p.WifiP2pDevice.UNAVAILABLE
import android.net.wifi.p2p.WifiP2pManager.BUSY
import android.net.wifi.p2p.WifiP2pManager.ERROR
import android.net.wifi.p2p.WifiP2pManager.P2P_UNSUPPORTED
import android.os.Build
import androidx.appcompat.app.AppCompatActivity

fun Context.getDeviceStatus(statusCode: Int) = when (statusCode) {
    CONNECTED -> getString(R.string.connected)
    INVITED -> getString(R.string.invited)
    FAILED -> getString(R.string.failed)
    AVAILABLE -> getString(R.string.available)
    UNAVAILABLE -> getString(R.string.unavailable)
    else -> getString(R.string.unknown)
}

fun Context.getFailureReason(reason: Int) = when (reason) {
    P2P_UNSUPPORTED -> getString(R.string.unsupported)
    ERROR -> getString(R.string.internal_error)
    BUSY -> getString(R.string.busy)
    else -> getString(R.string.unknown)
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun ContextWrapper.registerBroadcastReceiver(
    receiver: BroadcastReceiver?,
    intentFilter: IntentFilter
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, intentFilter, AppCompatActivity.RECEIVER_NOT_EXPORTED)
    } else {
        registerReceiver(receiver, intentFilter)
    }
}