package ao.uan.fc.dam.anuncioslock.transport.common

import ao.uan.fc.anuncioslock.data.model.Politica
import ao.uan.fc.anuncioslock.transport.common.DeliveryMode
import ao.uan.fc.anuncioslock.transport.common.MessageType
import kotlinx.serialization.Serializable


@Serializable
data class Message(
    val id: String,
    val autor: String,
    val conteudo: String,
    val localDestino: String,
    val infraestrutura: String,
    val politica: Politica? = null,
    val dataPublicacao: String,
    val dataExpiracao: String? = null,
    val modoEntrega: DeliveryMode = DeliveryMode.CENTRALIZED
)

@Serializable
data class TransportMessage(
    val id: String,
    val tipo: MessageType,
    val payload: String,
    val origem: String,
    val destino: String? = null,
    val saltos: Int = 0,
    val maxSaltos: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)