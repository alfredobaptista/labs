package ao.uan.fc.anuncioslock.transport.common

enum class MessageType {
    DISCOVERY,
    DISCOVERY_ACK,
    PROFILE_QUERY,
    PROFILE_RESPONSE,
    OFFER,
    ACCEPT,
    REJECT,
    ANNOUNCEMENT_DATA,
    DELIVERY_ACK,
    MULE_TRANSFER,
    MULE_DELIVERY,
    MULE_ACK,
    PING,
    PONG,
    ERROR
}