package com.colonydirect.app

import android.content.Context
import com.colonydirect.app.data.AuthRepository
import com.colonydirect.app.data.CartRepository
import com.colonydirect.app.data.CatalogRepository
import com.colonydirect.app.data.CheckoutRepository
import com.colonydirect.app.data.DashboardRepository
import com.colonydirect.app.data.OrderRepository
import com.colonydirect.app.data.TokenStore
import com.colonydirect.app.network.ApiClient
import com.colonydirect.app.network.AuthApi
import com.colonydirect.app.network.CartApi
import com.colonydirect.app.network.CatalogApi
import com.colonydirect.app.network.CheckoutApi
import com.colonydirect.app.network.DashboardApi
import com.colonydirect.app.network.OrderApi

/**
 * Manual dependency graph — scoped to application lifetime.
 * Hilt could replace this in a future refactor, but manual DI keeps
 * the build configuration simple for now.
 */
object ServiceLocator {

    private lateinit var tokenStore: TokenStore
    private lateinit var authRepository: AuthRepository
    private lateinit var catalogRepository: CatalogRepository
    private lateinit var cartRepository: CartRepository
    private lateinit var checkoutRepository: CheckoutRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var dashboardRepository: DashboardRepository

    fun init(context: Context) {
        tokenStore = TokenStore(context.applicationContext, ApiClient.gson)
        val retrofit = ApiClient.buildRetrofit(tokenStore)

        authRepository = AuthRepository(retrofit.create(AuthApi::class.java), tokenStore)
        catalogRepository = CatalogRepository(retrofit.create(CatalogApi::class.java))
        cartRepository = CartRepository(retrofit.create(CartApi::class.java))
        checkoutRepository = CheckoutRepository(retrofit.create(CheckoutApi::class.java))
        orderRepository = OrderRepository(retrofit.create(OrderApi::class.java))
        dashboardRepository = DashboardRepository(retrofit.create(DashboardApi::class.java))
    }

    fun getTokenStore(): TokenStore = tokenStore
    fun getAuthRepository(): AuthRepository = authRepository
    fun getCatalogRepository(): CatalogRepository = catalogRepository
    fun getCartRepository(): CartRepository = cartRepository
    fun getCheckoutRepository(): CheckoutRepository = checkoutRepository
    fun getOrderRepository(): OrderRepository = orderRepository
    fun getDashboardRepository(): DashboardRepository = dashboardRepository
}
