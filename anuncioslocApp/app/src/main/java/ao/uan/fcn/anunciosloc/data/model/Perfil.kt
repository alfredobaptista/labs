package ao.uan.fc.anuncioslock.data.model


import kotlinx.serialization.Serializable

@Serializable
data class Perfil(
    val chave: String,
    val valor: String
)