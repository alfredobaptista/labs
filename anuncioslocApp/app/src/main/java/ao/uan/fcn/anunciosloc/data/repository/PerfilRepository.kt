package ao.uan.fc.dam.anuncioslock.data.repository

import ao.uan.fc.anunciosloc.core.cache.CacheManager
import ao.uan.fc.anunciosloc.core.network.AnunciosSoapClient
import ao.uan.fc.anuncioslock.core.network.AnunciosSoapClient
import kotlinx.coroutines.flow.Flow
import org.osmdroid.tileprovider.cachemanager.CacheManager

class PerfilRepository(
    private val soapClient: AnunciosSoapClient,
    private val cacheManager: CacheManager
) {
    suspend fun enviarPerfil(token: String, pares: List<String>) =
        soapClient.enviarPerfil(token, pares)

    suspend fun listarChavesPerfis() =
        soapClient.listarChavesPerfis()

    suspend fun consultarSaldo(token: String) =
        soapClient.consultarSaldo(token)

    suspend fun salvarPerfilLocal(perfil: Map<String, String>) =
        cacheManager.salvarPerfil(perfil)

    fun obterPerfilLocal(): Flow<Map<String, String>> =
        cacheManager.obterPerfil()
}