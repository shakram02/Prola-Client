package shakram02.ahmed.scanz

import com.esotericsoftware.kryonet.Client
import java.net.InetAddress
import java.util.concurrent.LinkedBlockingQueue

/**
 * Sends Scanned codes to server
 */
class CodeSender(private val tcpPort: Int, private val udpPort: Int, private val timeoutMillis: Int) : Thread() {
    private val client = Client()
    private val messageQueue = LinkedBlockingQueue<String>()

    override fun run() {
        client.start()
        val hostAddress: InetAddress = client.discoverHost(udpPort, timeoutMillis)
        client.connect(timeoutMillis, hostAddress, tcpPort, udpPort)

        val msg: String = messageQueue.take()
        client.sendTCP(msg)
    }

    fun send(msg: String) {
        messageQueue.put(msg)
    }

    fun close() {
        client.close()
    }
}