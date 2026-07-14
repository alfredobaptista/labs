package ao.uan.fc.dam.anuncioslock.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Inicio : BottomNavItem("inicio", "Início", Icons.Default.Home)
    data object Mapa : BottomNavItem("mapa", "Mapa", Icons.Default.LocationOn)
    data object Postar : BottomNavItem("postar", "Postar", Icons.Default.AddCircle)
    data object Chat : BottomNavItem("chat", "Chat", Icons.Default.Chat)
    data object Perfil : BottomNavItem("perfil", "Perfil", Icons.Default.Person)
}