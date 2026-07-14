package ao.uan.fcn.anunciosloc.transport.decentralized.connection


class GroupConnectionManager(
    private val socketManager: SocketManager
) {

    suspend fun broadcast(
        hosts: List<String>,
        message: String
    ) {

        hosts.forEach { host ->

            socketManager.send(
                host = host,
                message = message
            )
        }
    }
}