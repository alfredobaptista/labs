package ao.uan.fcn.anunciosloc.transport.decentralized.connection

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket

class SocketServer(
    private val port: Int,
    private val onMessageReceived: (String) -> Unit
) {

    private var serverSocket: ServerSocket? = null
    private var running = false

    fun start() {
        running = true

        Thread {
            try {
                serverSocket = ServerSocket(port)

                while (running) {

                    val client = serverSocket?.accept() ?: continue

                    val reader = BufferedReader(
                        InputStreamReader(client.getInputStream())
                    )

                    val message = reader.readLine()

                    if (message != null) {
                        onMessageReceived(message)
                    }

                    client.close()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun stop() {
        running = false
        serverSocket?.close()
    }
}