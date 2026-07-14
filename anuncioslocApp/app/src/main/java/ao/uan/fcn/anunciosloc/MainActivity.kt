package ao.uan.fcn.anunciosloc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ao.uan.fc.anunciosloc.core.cache.SessionManager
import ao.uan.fc.anunciosloc.features.auth.LoginScreen
import ao.uan.fc.anunciosloc.features.auth.RegisterScreen
import ao.uan.fc.anunciosloc.features.home.HomeScreen
import ao.uan.fc.anunciosloc.navigation.Routes
import ao.uan.fc.anunciosloc.ui.theme.AnunciosLockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnunciosLockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = SessionManager(context)

    NavHost(
        navController = navController,
        startDestination = if (sessionManager.isLoggedIn()) Routes.HOME else Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                },
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen()
        }
    }
}