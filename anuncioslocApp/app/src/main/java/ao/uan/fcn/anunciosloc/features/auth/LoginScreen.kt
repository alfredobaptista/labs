package ao.uan.fcn.anunciosloc.features.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ao.uan.fc.anunciosloc.AnunciosLocApp
import ao.uan.fc.anunciosloc.core.utils.validarNome
import ao.uan.fc.anunciosloc.core.utils.validarSenhaLogin
import ao.uan.fc.anunciosloc.ui.components.BotaoPrincipal
import ao.uan.fc.anunciosloc.ui.components.CampoNome
import ao.uan.fc.anunciosloc.ui.components.CampoSenha
import ao.uan.fc.anunciosloc.ui.components.LogoApp
import ao.uan.fc.anunciosloc.ui.theme.AnunciosLockTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> onLoginSuccess()
            is LoginState.Error -> {
                Toast.makeText(context, (loginState as LoginState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.limparEstado()
            }
            else -> Unit
        }
    }

    AnunciosLockTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Bem-vindo", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(8.dp))
            LogoApp()
            Spacer(Modifier.height(32.dp))

            CampoNome(
                valor = username,
                erro = usernameError,
                tocado = true,
                fieldFontSize = 15.sp,
                errorFontSize = 12.sp,
                iconSize = 20.dp,
                onValueChange = {
                    username = it
                    usernameError = validarNome(it)
                }
            )
            Spacer(Modifier.height(16.dp))

            CampoSenha(
                valor = password,
                erro = passwordError,
                tocado = true,
                visivel = passwordVisible,
                fieldFontSize = 15.sp,
                errorFontSize = 12.sp,
                iconSize = 20.dp,
                onValueChange = {
                    password = it
                    passwordError = validarSenhaLogin(it)
                },
                onToggleVisibilidade = { passwordVisible = !passwordVisible }
            )
            Spacer(Modifier.height(32.dp))

            BotaoPrincipal(
                texto = "ENTRAR",
                onClick = {
                    usernameError = validarNome(username)
                    passwordError = validarSenhaLogin(password)
                    if (usernameError == null && passwordError == null) {
                        viewModel.login(username, password)
                    }
                },
                enabled = loginState !is LoginState.Loading
            )

            if (loginState is LoginState.Loading) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onRegisterClick) {
                Text("Criar conta", color = Color(0xFF2196F3), fontSize = 16.sp, fontWeight = FontWeight.Medium, textDecoration = TextDecoration.Underline)
            }
        }
    }
}