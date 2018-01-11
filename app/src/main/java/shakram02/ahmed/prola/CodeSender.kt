package shakram02.ahmed.prola

import android.util.Log
import com.esotericsoftware.kryonet.Client
import shakram02.ahmed.prola.utils.Event
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.LinkedBlockingQueue

/**
 * Sends Scanned codes to server
 */
class CodeSender(private val tcpPort: Int, private val udpPort: Int, private val timeoutMillis: Int) {
    private val client = Client()
    private val msgQueue = LinkedBlockingQueue<String>()
    private var networkThread = Thread()
    val onError = Event<String>()
    val onConnected = Event<String>()
    val onSent = Event<String>()
    private lateinit var clientThread: Thread
    private val networkTask = Runnable {
        clientThread = Thread(client)
        clientThread.start()

        val hostName = client.discoverHost(udpPort, timeoutMillis)

        if (!tryConnect(hostName)) {
            onError("Couldn't find host")
            return@Runnable
        }
        onConnected(hostName.hostAddress)

        try {
            senderLoop()
        } catch (e: InterruptedException) {
            // Ignore interrupted exception
        } catch (e: IOException) {
            Log.e("BarcodeSender", e.message)
            onError(e.message!!)
        } finally {
            clientThread.interrupt()
        }
    }

    /**
     * Starts code sender thread if it's not active
     */
    fun connect() {
        if (networkThread.isAlive) return

        networkThread = Thread(networkTask)
        networkThread.start()
    }

    /**
     * Puts the [msg] in send queue
     */
    fun send(msg: String) {
        msgQueue.put(msg)
    }

    /**
     * Disconnects the sender if it's connected,
     * otherwise this function has no effect
     */
    fun close() {
        if (!networkThread.isAlive) return

        Log.i("BarcodeSender", "CLOSING")
        networkThread.interrupt()
    }


    private fun tryConnect(hostName: InetAddress?): Boolean {
        if (hostName == null) return false

        return try {
            client.connect(timeoutMillis, hostName, tcpPort)
            true
        } catch (e: IOException) {
            onError(e.message!!)
            false
        }
    }

    private fun senderLoop() {
        while (!Thread.interrupted()) {
            val msg = msgQueue.take()

            if (!client.isConnected) {
                client.reconnect()
            }

            client.sendTCP(msg)
            onSent(msg)

            if (BuildConfig.DEBUG) {
                Log.i("BarcodeSender", "SENT:" + msg)
            }
        }
    }
}