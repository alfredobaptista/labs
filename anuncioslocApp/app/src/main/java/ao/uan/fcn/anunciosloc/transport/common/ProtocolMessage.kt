package ao.uan.fcn.anunciosloc.transport.common


import ao.uan.fc.anuncioslock.transport.common.MessageType
import kotlinx.serialization.Serializable

@Serializable
data class ProtocolMessage(

    val id: String,

    val type: MessageType,

    val senderId: String,

    val timestamp: String,

    val payload: String
)