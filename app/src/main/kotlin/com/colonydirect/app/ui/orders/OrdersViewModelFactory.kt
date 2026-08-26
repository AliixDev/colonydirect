package com.colonydirect.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.colonydirect.app.data.OrderRepository

class OrdersViewModelFactory(private val repo: OrderRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = OrdersViewModel(repo) as T
}
