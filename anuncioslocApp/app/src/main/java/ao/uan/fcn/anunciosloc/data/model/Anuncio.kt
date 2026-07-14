package ao.uan.fc.anuncioslock.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Anuncio(
    val id: String,
    val autor: String,
    val nomeLocal: String,
    val nomeInfraestrutura: String,
    val conteudo: String,
    val dataPublicacao: String,
    val politica: Politica? = null
)