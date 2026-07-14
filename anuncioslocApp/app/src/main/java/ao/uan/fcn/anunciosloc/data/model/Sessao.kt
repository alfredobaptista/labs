package ao.uan.fc.anunciosloc.data.model

data class Sessao(
    val token: String,
    val username: String,
    val email: String,
    val dataInicio: Long = System.currentTimeMillis(),
    var ativa: Boolean = true
)