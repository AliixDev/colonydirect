package com.colonydirect.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.colonydirect.app.ServiceLocator
import com.colonydirect.app.ui.cart.CartScreen
import com.colonydirect.app.ui.cart.CartViewModel
import com.colonydirect.app.ui.cart.CartViewModelFactory
import com.colonydirect.app.ui.catalog.CatalogScreen
import com.colonydirect.app.ui.catalog.CatalogViewModel
import com.colonydirect.app.ui.catalog.CatalogViewModelFactory
import com.colonydirect.app.ui.catalog.ProductDetailScreen
import com.colonydirect.app.ui.catalog.ProductDetailViewModel
import com.colonydirect.app.ui.catalog.ProductDetailViewModelFactory
import com.colonydirect.app.ui.checkout.CheckoutScreen
import com.colonydirect.app.ui.checkout.CheckoutViewModel
import com.colonydirect.app.ui.checkout.CheckoutViewModelFactory
import com.colonydirect.app.ui.dashboard.DashboardScreen
import com.colonydirect.app.ui.dashboard.DashboardViewModel
import com.colonydirect.app.ui.dashboard.DashboardViewModelFactory
import com.colonydirect.app.ui.orders.OrderDetailScreen
import com.colonydirect.app.ui.orders.OrderSuccessScreen
import com.colonydirect.app.ui.orders.OrdersScreen
import com.colonydirect.app.ui.orders.OrdersViewModel
import com.colonydirect.app.ui.orders.OrdersViewModelFactory
import com.colonydirect.app.ui.profile.ProfileScreen
import com.colonydirect.app.ui.profile.ProfileViewModel
import com.colonydirect.app.ui.profile.ProfileViewModelFactory

@Composable
fun MainNavGraph(navController: NavHostController, onLogout: () -> Unit) {
    // Shared ViewModels — single instances for the main session
    val catalogVm: CatalogViewModel = viewModel(
        factory = CatalogViewModelFactory(ServiceLocator.getCatalogRepository())
    )
    val cartVm: CartViewModel = viewModel(
        factory = CartViewModelFactory(ServiceLocator.getCartRepository())
    )
    val ordersVm: OrdersViewModel = viewModel(
        factory = OrdersViewModelFactory(ServiceLocator.getOrderRepository())
    )
    val dashboardVm: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(
            ServiceLocator.getDashboardRepository(),
            ServiceLocator.getOrderRepository()
        )
    )
    val profileVm: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(ServiceLocator.getAuthRepository())
    )

    val userName = ServiceLocator.getTokenStore().cachedUserName()

    NavHost(navController = navController, startDestination = MainRoutes.DASHBOARD) {

        composable(MainRoutes.DASHBOARD) {
            DashboardScreen(
                viewModel = dashboardVm,
                userName = userName,
                onOrderClick = { orderId ->
                    navController.navigate(MainRoutes.orderDetail(orderId))
                },
                onViewAllOrders = { navController.navigate(MainRoutes.ORDERS) }
            )
        }

        composable(MainRoutes.CATALOG) {
            CatalogScreen(
                viewModel = catalogVm,
                onProductClick = { slug ->
                    navController.navigate(MainRoutes.productDetail(slug))
                }
            )
        }

        composable(
            route = MainRoutes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("slug") { type = NavType.StringType })
        ) { backStack ->
            val slug = backStack.arguments?.getString("slug") ?: return@composable
            val productDetailVm: ProductDetailViewModel = viewModel(
                key = "product_$slug",
                factory = ProductDetailViewModelFactory(
                    ServiceLocator.getCatalogRepository(),
                    ServiceLocator.getCartRepository()
                )
            )
            ProductDetailScreen(
                slug = slug,
                viewModel = productDetailVm,
                onBack = { navController.popBackStack() },
                onGoToCart = {
                    navController.navigate(MainRoutes.CART) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(MainRoutes.CART) {
            CartScreen(
                viewModel = cartVm,
                onCheckout = { navController.navigate(MainRoutes.CHECKOUT) },
                onBrowseCatalog = { navController.navigate(MainRoutes.CATALOG) }
            )
        }

        composable(MainRoutes.CHECKOUT) {
            val checkoutVm: CheckoutViewModel = viewModel(
                factory = CheckoutViewModelFactory(ServiceLocator.getCheckoutRepository())
            )
            CheckoutScreen(
                viewModel = checkoutVm,
                onBack = { navController.popBackStack() },
                onOrderSuccess = { orderId ->
                    cartVm.loadCart()       // refresh cart after order
                    dashboardVm.loadDashboard() // refresh dashboard stats
                    navController.navigate(MainRoutes.orderSuccess(orderId)) {
                        popUpTo(MainRoutes.CART) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = MainRoutes.ORDER_SUCCESS,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStack ->
            val orderId = backStack.arguments?.getString("orderId") ?: ""
            OrderSuccessScreen(
                orderId = orderId,
                onViewOrder = {
                    navController.navigate(MainRoutes.orderDetail(orderId)) {
                        launchSingleTop = true
                    }
                },
                onContinueShopping = {
                    navController.navigate(MainRoutes.CATALOG) {
                        popUpTo(MainRoutes.DASHBOARD) { inclusive = false }
                    }
                }
            )
        }

        composable(MainRoutes.ORDERS) {
            OrdersScreen(
                viewModel = ordersVm,
                onOrderClick = { orderId ->
                    navController.navigate(MainRoutes.orderDetail(orderId))
                }
            )
        }

        composable(
            route = MainRoutes.ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStack ->
            val orderId = backStack.arguments?.getString("orderId") ?: return@composable
            OrderDetailScreen(
                orderId = orderId,
                viewModel = ordersVm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(MainRoutes.PROFILE) {
            ProfileScreen(
                viewModel = profileVm,
                onLogout = onLogout
            )
        }
    }
}
