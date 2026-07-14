package ao.uan.fc.dam.anuncioslock.transport.centralized


import ao.uan.fc.anunciosloc.core.cache.CacheManager
import ao.uan.fc.anunciosloc.core.network.AnunciosSoapClient
import ao.uan.fc.anunciosloc.data.model.Anuncio
import kotlinx.coroutines.flow.Flow

class CentralizedDeliveryManager(
    private val soapClient: AnunciosSoapClient,
    private val cacheManager: CacheManager
) {
    suspend fun postarMensagem(
        token: String,
        local: String,
        infra: String,
        texto: String,
        filtros: List<String> = emptyList(),
        tipo: String = "WHITELIST",
        dataExpiracao: String? = null
    ): Result<String> =
        soapClient.postarMensagem(token, local, infra, texto, filtros, tipo, dataExpiracao)

    suspend fun receberMensagens(token: String, infra: String): Result<List<Anuncio>> {
        val cached = cacheManager.obterAnuncios(infra).firstOrNull()
        if (cached != null && cached.isNotEmpty()) {
            return Result.success(cached)
        }
        val result = soapClient.receberMensagem(token, infra)
        result.onSuccess { lista ->
            cacheManager.salvarAnuncios(infra, lista)
        }
        return result
    }

    fun obterAnunciosCache(infra: String): Flow<List<Anuncio>> =
        cacheManager.obterAnuncios(infra)
}