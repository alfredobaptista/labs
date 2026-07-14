package ao.uan.fcn.anunciosloc.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Local(
    val nome: String,
    val tipoLocalizacao: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val raio: Int? = null,
    val listaSSID: List<String> = emptyList(),
    val criador: String
)