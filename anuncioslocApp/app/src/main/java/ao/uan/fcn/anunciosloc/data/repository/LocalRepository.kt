package ao.uan.fc.dam.anuncioslock.data.repository

import ao.uan.fc.anunciosloc.core.cache.CacheManager
import ao.uan.fc.anunciosloc.core.network.AnunciosSoapClient
import ao.uan.fc.anunciosloc.data.model.Infraestrutura
import ao.uan.fc.anuncioslock.core.network.AnunciosSoapClient
import ao.uan.fcn.anunciosloc.data.model.Infraestrutura
import kotlinx.coroutines.flow.Flow
import org.osmdroid.tileprovider.cachemanager.CacheManager

class LocalRepository(
    private val soapClient: AnunciosSoapClient,
    private val cacheManager: CacheManager
) {
    suspend fun listarInfraestruturas(lat: Double, lon: Double, k: Int): Result<List<Infraestrutura>> {
        val cached = cacheManager.obterInfraestruturas().firstOrNull()
        if (cached != null && cached.isNotEmpty()) {
            return Result.success(cached)
        }
        val result = soapClient.listarInfraestruturas(lat, lon, k)
        result.onSuccess { lista ->
            cacheManager.salvarInfraestruturas(lista)
        }
        return result
    }

    fun obterInfraestruturasCache(): Flow<List<Infraestrutura>> =
        cacheManager.obterInfraestruturas()

    fun obterUltimaInfra(): Flow<String> =
        cacheManager.obterUltimaInfra()

    suspend fun salvarUltimaInfra(infra: String) =
        cacheManager.salvarUltimaInfra(infra)

    suspend fun criarLocal(token: String, infra: String, nome: String, lat: Double, lon: Double, raio: Int) =
        soapClient.criarLocal(token, infra, nome, lat, lon, raio)

    suspend fun limparCache() = cacheManager.limpar()
}