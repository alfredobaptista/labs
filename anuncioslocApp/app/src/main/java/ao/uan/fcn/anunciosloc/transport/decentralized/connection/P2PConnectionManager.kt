package ao.uan.fcn.anunciosloc.transport.decentralized.connection

class P2PConnectionManager(
    private val socketManager: SocketManager
) {

    suspend fun connect(
        host: String,
        message: String
    ) {
        socketManager.send(
            host = host,
            message = message
        )
    }
}