package ao.uan.fc.anuncioslock.data.model


import kotlinx.serialization.Serializable

@Serializable
data class Politica(
    val tipo: String = "WHITELIST",
    val filtros: List<String> = emptyList(),
    val dataExpiracao: String? = null
)