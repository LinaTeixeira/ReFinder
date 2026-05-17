package pt.ua.icm.refinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pt.ua.icm.refinder.data.repository.AuthRepository
import pt.ua.icm.refinder.ui.navigation.BottomNavItem
import pt.ua.icm.refinder.ui.screens.home.HomeScreen
import pt.ua.icm.refinder.ui.screens.profile.ProfileScreen
import pt.ua.icm.refinder.ui.screens.report.ReportScreen
import pt.ua.icm.refinder.ui.screens.search.SearchScreen
import pt.ua.icm.refinder.ui.screens.auth.AuthScreen
import pt.ua.icm.refinder.ui.screens.detail.ItemDetailScreen
import pt.ua.icm.refinder.ui.screens.locker.LockerSelectionScreen
import pt.ua.icm.refinder.ui.screens.map.MapScreen
import pt.ua.icm.refinder.ui.screens.profile.MyItemsScreen
import pt.ua.icm.refinder.ui.screens.admin.AdminClaimsScreen
import pt.ua.icm.refinder.ui.screens.admin.AdminPinValidationScreen
import pt.ua.icm.refinder.ui.screens.admin.AdminQrValidationScreen
import pt.ua.icm.refinder.ui.screens.admin.AdminPickupHistoryScreen
import pt.ua.icm.refinder.ui.screens.notifications.NotificationsScreen
import pt.ua.icm.refinder.ui.screens.support.HelpSupportScreen
import pt.ua.icm.refinder.ui.theme.ReFinderTheme
import pt.ua.icm.refinder.ui.theme.RefinderBackground
import pt.ua.icm.refinder.ui.theme.RefinderPrimary
import pt.ua.icm.refinder.ui.theme.RefinderSurface
import pt.ua.icm.refinder.ui.theme.RefinderTextSecondary

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
        BottomNavItem.Search,
        BottomNavItem.Report,
        BottomNavItem.Map,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != "auth"

    Scaffold(
        containerColor = RefinderBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = RefinderSurface
                ) {
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
                            label = { Text(item.label) },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = RefinderBackground,
                                selectedTextColor = RefinderPrimary,
                                indicatorColor = RefinderPrimary,
                                unselectedIconColor = RefinderTextSecondary,
                                unselectedTextColor = RefinderTextSecondary
                            )
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

            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onItemClick = { itemId ->
                        navController.navigate("itemDetail/$itemId")
                    }
                )
            }
            composable(
                route = "itemDetail/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable

                ItemDetailScreen(
                    itemId = itemId,
                    onBack = { navController.popBackStack() },
                    onDepositInLocker = { clickedItemId ->
                        navController.navigate("lockerSelection/$clickedItemId")
                    },
                    onMatchClick = { matchItemId ->
                        navController.navigate("itemDetail/$matchItemId")
                    }
                )
            }
            composable(
                route = "lockerSelection/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable

                LockerSelectionScreen(
                    itemId = itemId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(BottomNavItem.Report.route) { ReportScreen() }
            composable(BottomNavItem.Search.route) {
                SearchScreen(
                    onItemClick = { itemId ->
                        navController.navigate("itemDetail/$itemId")
                    }
                )
            }
            composable(BottomNavItem.Map.route) {
                MapScreen(
                    onItemClick = { itemId ->
                        navController.navigate("itemDetail/$itemId")
                    }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(navController = navController)
            }
            composable("myItems") {
                MyItemsScreen(navController)
            }
            composable("helpSupport") {
                HelpSupportScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("notifications") {
                NotificationsScreen(
                    onBack = { navController.popBackStack() },
                    onItemClick = { itemId ->
                        navController.navigate("itemDetail/$itemId")
                    }
                )
            }
            composable("adminClaims") {
                AdminClaimsScreen(
                    navController = navController,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("adminPinValidation") {
                AdminPinValidationScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("adminQrValidation") {
                AdminQrValidationScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("adminPickupHistory") {
                AdminPickupHistoryScreen(
                    onBack = { navController.popBackStack() }
                )
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