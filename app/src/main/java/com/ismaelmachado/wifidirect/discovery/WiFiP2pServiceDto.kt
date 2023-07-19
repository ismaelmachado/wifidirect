package com.ismaelmachado.wifidirect.discovery

import android.net.wifi.p2p.WifiP2pDevice

/**
 * A structure to hold service information.
 */
data class WiFiP2pServiceDto(
    var device: WifiP2pDevice? = null,
    var instanceName: String? = null,
    var serviceRegistrationType: String? = null
)
