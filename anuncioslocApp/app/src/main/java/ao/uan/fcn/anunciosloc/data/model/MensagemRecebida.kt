package ao.uan.fcn.anunciosloc.data.model


import ao.uan.fc.anuncioslock.data.model.Anuncio
import java.time.LocalDateTime

data class MensagemRecebida(
    val id: String,
    val anuncio: Anuncio,
    val dataRecepcao: LocalDateTime,
    val lida: Boolean = false
)