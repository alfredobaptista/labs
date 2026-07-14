package ao.uan.fc.dam.anuncioslock.core.cache

import android.content.Context
import android.content.SharedPreferences
import ao.uan.fc.dam.anuncioslock.data.model.Sessao

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("anunciosloc_prefs", Context.MODE_PRIVATE)

    fun salvarSessao(token: String, username: String, email: String) {
        prefs.edit().apply {
            putString("TOKEN", token)
            putString("USERNAME", username)
            putString("EMAIL", email)
            putBoolean("IS_LOGGED_IN", true)
            putLong("SESSION_START", System.currentTimeMillis())
            apply()
        }
    }

    fun getSessao(): Sessao? {
        val token = prefs.getString("TOKEN", null)
        val username = prefs.getString("USERNAME", null)
        val email = prefs.getString("EMAIL", null)
        val isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false)
        val dataInicio = prefs.getLong("SESSION_START", 0)
        return if (isLoggedIn && token != null && username != null && email != null) {
            Sessao(token, username, email, dataInicio, true)
        } else null
    }

    fun getToken(): String? = prefs.getString("TOKEN", null)
    fun getUsername(): String? = prefs.getString("USERNAME", null)
    fun getEmail(): String? = prefs.getString("EMAIL", null)
    fun isLoggedIn(): Boolean = prefs.getBoolean("IS_LOGGED_IN", false)
    fun limparSessao() {
        prefs.edit().clear().apply()
    }
}