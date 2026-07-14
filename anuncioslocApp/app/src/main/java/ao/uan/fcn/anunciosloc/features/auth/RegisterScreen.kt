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
import ao.uan.fcn.anunciosloc.core.utils.*
import ao.uan.fcn.anunciosloc.ui.components.BotaoPrincipal
import ao.uan.fcn.anunciosloc.ui.components.CampoEmail
import ao.uan.fcn.anunciosloc.ui.components.CampoNome
import ao.uan.fcn.anunciosloc.ui.components.CampoSenha
import ao.uan.fcn.anunciosloc.ui.components.LogoApp
import ao.uan.fcn.anunciosloc.ui.theme.AnunciosLocTheme

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confSenha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var confSenhaVisivel by remember { mutableStateOf(false) }

    var nomeErro by remember { mutableStateOf<String?>(null) }
    var emailErro by remember { mutableStateOf<String?>(null) }
    var senhaErro by remember { mutableStateOf<String?>(null) }
    var confSenhaErro by remember { mutableStateOf<String?>(null) }

    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> {
                Toast.makeText(context, "Conta ativada com sucesso!", Toast.LENGTH_LONG).show()
                viewModel.limparEstado()
                onRegisterSuccess()
            }
            is RegisterState.Error -> {
                Toast.makeText(
                    context,
                    (registerState as RegisterState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.limparEstado()
            }
            else -> Unit
        }
    }

    fun validarCampos(): Boolean {
        nomeErro = validarNome(nome)
        emailErro = validarEmail(email)
        senhaErro = validarSenha(senha)
        confSenhaErro = validarConfSenha(confSenha, senha)
        return listOf(nomeErro, emailErro, senhaErro, confSenhaErro).all { it == null }
    }

    AnunciosLocTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ATIVAR CONTA", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Text("Digite os dados fornecidos pelo administrador", fontSize = 12.sp, color = Color(0xFFE65100))
            Spacer(Modifier.height(8.dp))
            LogoApp(tamanho = 150)
            Spacer(Modifier.height(24.dp))

            CampoNome(
                valor = nome,
                erro = nomeErro,
                tocado = true,
                onValueChange = {
                    nome = it
                    nomeErro = validarNome(it)
                }
            )
            Spacer(Modifier.height(12.dp))

            CampoEmail(
                valor = email,
                erro = emailErro,
                tocado = true,
                onValueChange = {
                    email = it
                    emailErro = validarEmail(it)
                }
            )
            Spacer(Modifier.height(12.dp))

            CampoSenha(
                valor = senha,
                erro = senhaErro,
                tocado = true,
                visivel = senhaVisivel,
                onValueChange = {
                    senha = it
                    senhaErro = validarSenha(it)
                    if (confSenha.isNotBlank()) {
                        confSenhaErro = validarConfSenha(confSenha, it)
                    }
                },
                onToggleVisibilidade = { senhaVisivel = !senhaVisivel }
            )
            Spacer(Modifier.height(12.dp))

            CampoSenha(
                valor = confSenha,
                erro = confSenhaErro,
                tocado = true,
                visivel = confSenhaVisivel,
                label = "Confirmar Palavra-passe",
                onValueChange = {
                    confSenha = it
                    confSenhaErro = validarConfSenha(it, senha)
                },
                onToggleVisibilidade = { confSenhaVisivel = !confSenhaVisivel }
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = "⚠️ O utilizador deve ser pré-cadastrado pelo administrador.",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(24.dp))

            BotaoPrincipal(
                texto = "ATIVAR CONTA",
                onClick = {
                    if (validarCampos()) {
                        viewModel.register(nome, email, senha)
                    }
                },
                enabled = registerState !is RegisterState.Loading
            )

            if (registerState is RegisterState.Loading) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onRegisterSuccess) {
                Text(
                    text = "Já tem conta? Login",
                    color = Color(0xFF2196F3),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}