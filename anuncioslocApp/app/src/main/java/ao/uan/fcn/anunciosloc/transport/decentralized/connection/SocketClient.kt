package ao.uan.fcn.anunciosloc.transport.decentralized.connection

package ao.uan.fcn.anunciosloc.transport.decentralized.connection

import java.io.PrintWriter
import java.net.Socket

class SocketClient {

    fun send(
        host: String,
        port: Int,
        message: String
    ) {

        Thread {
            try {

                Socket(host, port).use { socket ->

                    val writer = PrintWriter(
                        socket.getOutputStream(),
                        true
                    )

                    writer.println(message)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}