package com.ismaelmachado.wifidirect.discovery

import android.os.Handler
import android.util.Log
import com.ismaelmachado.wifidirect.discovery.WiFiServiceDiscoveryActivity.Companion.SERVER_PORT
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class ClientSocketHandler(
    private val handler: Handler,
    private val mAddress: InetAddress
) : Thread() {

    companion object {
        private const val TAG = "ClientSocketHandler"
    }

    private var chat: ChatManager? = null

    override fun run() {
        val socket = Socket()
        try {
            socket.bind(null)
            socket.connect(
                InetSocketAddress(
                    mAddress.hostAddress,
                    SERVER_PORT
                ),
                5000
            )
            Log.d(TAG, "Launching the I/O handler")
            chat = ChatManager(socket, handler)
            Thread(chat).start()
        } catch (e: IOException) {
            Log.e(TAG, "IOException", e)
            try {
                socket.close()
            } catch (e1: IOException) {
                Log.e(TAG, "IOException", e1)
            }
            return
        }
    }

}
