package ao.uan.fcn.anunciosloc.transport.decentralized.protocol

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ao.uan.fc.anunciosloc.ui.auth.LoginScreen
import ao.uan.fc.anunciosloc.ui.theme.AnunciosLockTheme
import ao.uan.fc.anunciosloc.utils.SessionManager
import kotlinx.coroutines.delay

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnunciosLockTheme {
                TelaSplash(this)
            }
        }
    }
}

@Composable
private fun TelaSplash(activity: ComponentActivity) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(250.dp)
        )
    }

    LaunchedEffect(Unit) {
        delay(2000L)
        val sessionManager = SessionManager(activity)
        val intent = if (sessionManager.isLoggedIn()) {
            Intent(activity, MainActivity::class.java)
        } else {
            Intent(activity, LoginScreen::class.java)
        }
        activity.startActivity(intent)
        activity.finish()
    }
}