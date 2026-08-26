package com.colonydirect.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.colonydirect.app.data.CartRepository

class CartViewModelFactory(private val repo: CartRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CartViewModel(repo) as T
}
