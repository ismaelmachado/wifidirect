package com.ismaelmachado.wifidirect.discovery

import android.os.Handler
import android.util.Log
import com.ismaelmachado.wifidirect.discovery.WiFiServiceDiscoveryActivity.Companion.MESSAGE_READ
import com.ismaelmachado.wifidirect.discovery.WiFiServiceDiscoveryActivity.Companion.MY_HANDLE
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
class ChatManager(
    private val socket: Socket? = null,
    private val handler: Handler
) : Runnable {

    companion object {
        private const val TAG = "ChatHandler"
    }

    private var iStream: InputStream? = null
    private var oStream: OutputStream? = null

    override fun run() {
        try {
            iStream = socket?.getInputStream()
            oStream = socket?.getOutputStream()
            val buffer = ByteArray(1024)
            var bytes: Int
            handler.obtainMessage(MY_HANDLE, this).sendToTarget()

            while (true) {
                try {
                    // Read from the InputStream
                    bytes = iStream?.read(buffer) ?: 0
                    if (bytes == -1) break

                    // Send the obtained bytes to the UI Activity
                    Log.d(TAG, "Rec:" + buffer.contentToString())
                    handler.obtainMessage(
                        MESSAGE_READ,
                        bytes,
                        -1,
                        buffer
                    ).sendToTarget()
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "IOException", e)
        } finally {
            try {
                socket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "IOException", e)
            }
        }
    }

    fun write(msg: String) {
        val buffer = msg.toByteArray()
        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    oStream?.write(buffer)
                } catch (e: IOException) {
                    Log.e(TAG, "Exception during write", e)
                }
            }
        }
        thread.start()
    }

}
