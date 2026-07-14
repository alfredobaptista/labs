package ao.uan.fc.dam.anuncioslock.data.repository

import ao.uan.fc.anunciosloc.core.cache.SessionManager
import ao.uan.fc.anunciosloc.core.network.AnunciosSoapClient
import ao.uan.fc.anunciosloc.data.model.Sessao
import ao.uan.fc.anuncioslock.core.network.AnunciosSoapClient
import ao.uan.fc.dam.anuncioslock.core.cache.SessionManager

class AuthRepository(
    private val soapClient: AnunciosSoapClient,
    private val sessionManager: SessionManager
) {
    suspend fun login(username: String, password: String) =
        soapClient.login(username, password)

    suspend fun logout(token: String) =
        soapClient.logout(token)

    suspend fun ativarUtilizador(nome: String, email: String, senha: String) =
        soapClient.ativarUtilizador(nome, email, senha)

    fun salvarSessao(token: String, username: String, email: String) =
        sessionManager.salvarSessao(token, username, email)

    fun getSessao(): Sessao? = sessionManager.getSessao()
    fun getToken(): String? = sessionManager.getToken()
    fun getUsername(): String? = sessionManager.getUsername()
    fun getEmail(): String? = sessionManager.getEmail()
    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
    fun limparSessao() = sessionManager.limparSessao()
}