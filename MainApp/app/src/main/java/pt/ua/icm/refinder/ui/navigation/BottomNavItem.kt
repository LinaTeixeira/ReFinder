package pt.ua.icm.refinder.ui.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Search : BottomNavItem("search", "Pesquisar", Icons.Default.Search)
    object Report : BottomNavItem("report", "Registar", Icons.Default.Add)
    object Map : BottomNavItem("map", "Mapa", Icons.Default.LocationOn)

    object Profile : BottomNavItem("profile", "Perfil", Icons.Default.Person)
}
