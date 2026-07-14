package ao.uan.fcn.anunciosloc.transport.decentralized.protocol

import android.util.Log
import ao.uan.fc.anuncioslock.transport.common.MessageType

class DefaultProtocolHandler : ProtocolHandler {

    override suspend fun handle(
        message: ProtocolMessage
    ) {

        when (message.type) {

            MessageType.DISCOVER_PEERS -> {
                Log.d("PROTO", "DISCOVER_PEERS")
            }

            MessageType.PROFILE_REQUEST -> {
                Log.d("PROTO", "PROFILE_REQUEST")
            }

            MessageType.PROFILE_RESPONSE -> {
                Log.d("PROTO", "PROFILE_RESPONSE")
            }

            MessageType.ANNOUNCEMENT_OFFER -> {
                Log.d("PROTO", "ANNOUNCEMENT_OFFER")
            }

            MessageType.ANNOUNCEMENT_ACCEPT -> {
                Log.d("PROTO", "ANNOUNCEMENT_ACCEPT")
            }

            MessageType.ANNOUNCEMENT_REJECT -> {
                Log.d("PROTO", "ANNOUNCEMENT_REJECT")
            }

            MessageType.ANNOUNCEMENT_DATA -> {
                Log.d("PROTO", "ANNOUNCEMENT_DATA")
            }

            MessageType.MULE_FORWARD -> {
                Log.d("PROTO", "MULE_FORWARD")
            }

            MessageType.ACK -> {
                Log.d("PROTO", "ACK")
            }

            MessageType.PING -> {
                Log.d("PROTO", "PING")
            }

            MessageType.PONG -> {
                Log.d("PROTO", "PONG")
            }
        }
    }
}