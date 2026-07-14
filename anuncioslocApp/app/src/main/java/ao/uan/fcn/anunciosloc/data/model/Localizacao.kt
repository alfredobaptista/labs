package ao.uan.fcn.anunciosloc.data.model

import java.time.LocalDateTime

data class Localizacao(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val raio: Float? = null,
    val ssids: List<String> = emptyList(),
    val timestamp: LocalDateTime
)