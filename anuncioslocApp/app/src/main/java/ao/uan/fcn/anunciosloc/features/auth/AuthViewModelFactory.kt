package ao.uan.fcn.anunciosloc.features.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ao.uan.fc.anunciosloc.AnunciosLocApp

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = context.applicationContext as AnunciosLocApp
        return AuthViewModel(app.authRepository) as T
    }
}