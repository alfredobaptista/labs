package ao.uan.fcn.anunciosloc.features.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ao.uan.fc.dam.anuncioslock.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.login(username, password)
            result.onSuccess { token ->
                authRepository.salvarSessao(token, username, username)
                _loginState.value = LoginState.Success(token)
            }.onFailure { error ->
                _loginState.value = LoginState.Error(error.message ?: "Erro desconhecido")
            }
        }
    }

    fun register(nome: String, email: String, senha: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val result = authRepository.ativarUtilizador(nome, email, senha)
            result.onSuccess {
                _registerState.value = RegisterState.Success
            }.onFailure { error ->
                _registerState.value = RegisterState.Error(error.message ?: "Erro no registo")
            }
        }
    }

    fun logout(token: String) {
        viewModelScope.launch {
            authRepository.logout(token)
            authRepository.limparSessao()
            _loginState.value = LoginState.Idle
        }
    }

    fun limparEstado() {
        _loginState.value = LoginState.Idle
        _registerState.value = RegisterState.Idle
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}