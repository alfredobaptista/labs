package ao.uan.fcn.anunciosloc.features.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ao.uan.fcn.anunciosloc.AnunciosLocApp
import ao.uan.fcn.anunciosloc.core.cache.SessionManager
import ao.uan.fcn.anunciosloc.features.auth.AuthViewModel
import ao.uan.fcn.anunciosloc.features.auth.AuthViewModelFactory
import ao.uan.fcn.anunciosloc.navigation.BottomNavItem
import ao.uan.fcn.anunciosloc.ui.components.BottomNavigationBar
import ao.uan.fcn.anunciosloc.ui.tabs.*

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val sessao = sessionManager.getSessao()

    if (sessao == null) {
        // Se não estiver logado, redirecionar (na prática, o navController faria isso)
        return
    }

    val token = sessao.token
    val username = sessao.username

    var selectedNavItem by remember { mutableStateOf<BottomNavItem>(BottomNavItem.Inicio) }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(context)
    )

    val app = context.applicationContext as AnunciosLocApp

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Olá, $username", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
                    TextButton(
                        onClick = {
                            authViewModel.logout(token)
                            // Navegar para login (com NavController)
                            Toast.makeText(context, "Logout realizado", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Sair", color = Color.Red)
                    }
                }
            }
        },
        bottomBar = { BottomNavigationBar(selectedNavItem) { selectedNavItem = it } }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedNavItem) {
                is BottomNavItem.Inicio -> InicioTab(
                    token = token,
                    localRepository = app.localRepository,
                    anuncioRepository = app.anuncioRepository
                )
                is BottomNavItem.Mapa -> MapaTab(
                    token = token,
                    localRepository = app.localRepository
                )
                is BottomNavItem.Postar -> PostarTab(
                    token = token,
                    anuncioRepository = app.anuncioRepository,
                    localRepository = app.localRepository
                )
                is BottomNavItem.Chat -> ChatTab(
                    token = token,
                    username = username
                )
                is BottomNavItem.Perfil -> PerfilTab(
                    token = token,
                    username = username,
                    email = sessao.email,
                    perfilRepository = app.perfilRepository
                )
            }
        }
    }
}