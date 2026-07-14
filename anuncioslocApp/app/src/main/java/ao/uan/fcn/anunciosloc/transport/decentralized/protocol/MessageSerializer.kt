package ao.uan.fcn.anunciosloc.transport.decentralized.protocol

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object MessageSerializer {

    private val json = Json {

        prettyPrint = false

        ignoreUnknownKeys = true

        encodeDefaults = true
    }

    fun serialize(
        message: ProtocolMessage
    ): String {

        return json.encodeToString(message)
    }

    fun deserialize(
        raw: String
    ): ProtocolMessage {

        return json.decodeFromString(raw)
    }
}