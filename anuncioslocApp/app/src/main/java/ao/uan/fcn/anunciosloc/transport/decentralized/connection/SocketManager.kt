package ao.uan.fcn.anunciosloc.transport.decentralized.connection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class SocketManager(
    private val port: Int
) {

    private var serverSocket: ServerSocket? = null

    suspend fun startServer(
        onMessageReceived: suspend (String) -> Unit
    ) = withContext(Dispatchers.IO) {

        serverSocket = ServerSocket(port)

        while (!serverSocket!!.isClosed) {

            val client = serverSocket!!.accept()

            launchClientHandler(
                client,
                onMessageReceived
            )
        }
    }

    private fun launchClientHandler(
        client: Socket,
        onMessageReceived: suspend (String) -> Unit
    ) {

        Thread {

            try {

                val reader = BufferedReader(
                    InputStreamReader(client.getInputStream())
                )

                val message = reader.readLine()

                if (message != null) {
                    kotlinx.coroutines.runBlocking {
                        onMessageReceived(message)
                    }
                }

                client.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()
    }

    suspend fun send(
        host: String,
        message: String
    ) = withContext(Dispatchers.IO) {

        Socket(host, port).use { socket ->

            val writer = PrintWriter(
                socket.getOutputStream(),
                true
            )

            writer.println(message)
        }
    }

    fun stop() {
        serverSocket?.close()
    }
}