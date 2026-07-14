package ao.uan.fcn.anunciosloc.transport.decentralized.protocol

import ao.uan.fcn.anunciosloc.transport.common.ProtocolMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

object JsonProtocol {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun encode(message: ProtocolMessage): String {
        return json.encodeToString(message)
    }

    fun decode(raw: String): ProtocolMessage {
        return json.decodeFromString(raw)
    }
}