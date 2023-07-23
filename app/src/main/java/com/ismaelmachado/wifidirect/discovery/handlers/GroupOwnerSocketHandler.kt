package com.ismaelmachado.wifidirect.discovery.handlers

import android.os.Handler
import android.util.Log
import com.ismaelmachado.wifidirect.discovery.WiFiServiceDiscoveryActivity.Companion.SERVER_PORT
import com.ismaelmachado.wifidirect.discovery.managers.ChatManager
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit.SECONDS

/**
 * The implementation of a ServerSocket handler. This is used by the wifi p2p
 * group owner.
 */
class GroupOwnerSocketHandler(private val handler: Handler) : Thread() {

    companion object {
        private const val TAG = "GroupOwnerSocketHandler"
        private const val THREAD_COUNT = 10
    }

    private var socket: ServerSocket

    /**
     * A ThreadPool for client sockets.
     */
    private val pool = ThreadPoolExecutor(
        THREAD_COUNT,
        THREAD_COUNT,
        10,
        SECONDS,
        LinkedBlockingQueue()
    )

    init {
        try {
            socket = ServerSocket(SERVER_PORT)
            Log.d("GroupOwnerSocketHandler", "Socket Started")
        } catch (e: IOException) {
            Log.e(TAG, "IOException", e)
            pool.shutdownNow()
            throw e
        }
    }

    override fun run() {
        while (true) {
            try {
                // A blocking operation. Initiate a ChatManager instance when
                // there is a new connection
                pool.execute(ChatManager(socket.accept(), handler))
                Log.d(TAG, "Launching the I/O handler")
            } catch (e: IOException) {
                try {
                    if (!socket.isClosed) {
                        socket.close()
                    }
                } catch (ioe: IOException) {
                    Log.e(TAG, "IOException", ioe)
                }
                Log.e(TAG, "IOException", e)
                pool.shutdownNow()
                break
            }
        }
    }

}
