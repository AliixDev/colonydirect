package com.colonydirect.app.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.colonydirect.app.navigation.MainNavGraph
import com.colonydirect.app.navigation.MainRoutes

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(MainRoutes.DASHBOARD, "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Catalog : BottomNavItem(MainRoutes.CATALOG, "Shop", Icons.Filled.Storefront, Icons.Outlined.Storefront)
    object Cart : BottomNavItem(MainRoutes.CART, "Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart)
    object Orders : BottomNavItem(MainRoutes.ORDERS, "Orders", Icons.Filled.Receipt, Icons.Outlined.Receipt)
    object Profile : BottomNavItem(MainRoutes.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Catalog,
    BottomNavItem.Cart,
    BottomNavItem.Orders,
    BottomNavItem.Profile
)

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Routes that should NOT show the bottom nav (full-screen detail screens)
    val fullScreenRoutes = setOf(
        MainRoutes.PRODUCT_DETAIL,
        MainRoutes.CHECKOUT,
        MainRoutes.ORDER_DETAIL
    )
    val showBottomBar = currentDestination?.route?.let { route ->
        fullScreenRoutes.none { route.startsWith(it.substringBefore("{")) }
    } ?: true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { _ ->
        MainNavGraph(navController = navController, onLogout = onLogout)
    }
}
