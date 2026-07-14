package ao.uan.fcn.anunciosloc.transport.common

interface Transport {

    suspend fun start()

    suspend fun stop()

    suspend fun send(message: ProtocolMessage)
}