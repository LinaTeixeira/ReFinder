package pt.ua.icm.refinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pt.ua.icm.refinder.data.repository.AuthRepository
import pt.ua.icm.refinder.ui.navigation.BottomNavItem
import pt.ua.icm.refinder.ui.screens.home.HomeScreen
import pt.ua.icm.refinder.ui.screens.ProfileScreen
import pt.ua.icm.refinder.ui.screens.report.ReportScreen
import pt.ua.icm.refinder.ui.screens.SearchScreen
import pt.ua.icm.refinder.ui.screens.auth.AuthScreen
import pt.ua.icm.refinder.ui.theme.ReFinderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReFinderTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val authRepository = AuthRepository()

    val startDestination = if (authRepository.isUserLoggedIn()) {
        BottomNavItem.Home.route
    } else {
        "auth"
    }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Report,
        BottomNavItem.Search,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != "auth"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("auth") {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo("auth") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(BottomNavItem.Home.route) { HomeScreen() }
            composable(BottomNavItem.Report.route) { ReportScreen() }
            composable(BottomNavItem.Search.route) { SearchScreen() }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(navController = navController)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ReFinderTheme {
        MainScreen()
    }
}