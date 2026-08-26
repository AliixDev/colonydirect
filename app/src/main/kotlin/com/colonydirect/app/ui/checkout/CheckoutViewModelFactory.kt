package com.colonydirect.app.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.colonydirect.app.data.CheckoutRepository

class CheckoutViewModelFactory(private val repo: CheckoutRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CheckoutViewModel(repo) as T
}
