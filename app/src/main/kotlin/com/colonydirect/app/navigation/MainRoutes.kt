package com.colonydirect.app.navigation

object MainRoutes {
    const val DASHBOARD = "dashboard"
    const val CATALOG = "catalog"
    const val PRODUCT_DETAIL = "product_detail/{slug}"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val ORDER_SUCCESS = "order_success/{orderId}"
    const val ORDERS = "orders"
    const val ORDER_DETAIL = "order_detail/{orderId}"
    const val PROFILE = "profile"

    fun productDetail(slug: String) = "product_detail/$slug"
    fun orderSuccess(orderId: String) = "order_success/$orderId"
    fun orderDetail(orderId: String) = "order_detail/$orderId"
}
