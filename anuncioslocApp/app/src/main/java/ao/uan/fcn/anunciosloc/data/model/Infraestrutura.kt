package ao.uan.fcn.anunciosloc.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Infraestrutura(
    val nome: String,
    val latitude: Double,
    val longitude: Double,
    val raio: Int,
    val capacidade: Int,
    val conexoesDisponiveis: Int,
    val totalAnuncios: Int,
    val totalEntregas: Int,
    val url: String? = null
)