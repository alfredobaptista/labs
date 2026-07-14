package ao.uan.fcn.anunciosloc.data.model

import java.time.LocalDateTime

data class Notificacao(
    val id: String,
    val mensagem: String,
    val data: LocalDateTime,
    val lida: Boolean = false
)