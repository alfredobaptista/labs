package ao.uan.fcn.anunciosloc.transport.decentralized.protocol


import ao.uan.fc.dam.anunciosloc.transport.common.MessageType
import kotlinx.serialization.Serializable

@Serializable
data class ProtocolMessage(

    val messageId: String,

    val type: MessageType,

    val senderId: String,

    val timestamp: String,

    val payload: String
)