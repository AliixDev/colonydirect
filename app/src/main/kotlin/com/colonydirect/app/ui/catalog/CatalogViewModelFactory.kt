package com.colonydirect.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.colonydirect.app.data.CartRepository
import com.colonydirect.app.data.CatalogRepository

class CatalogViewModelFactory(private val repo: CatalogRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatalogViewModel(repo) as T
}

class ProductDetailViewModelFactory(
    private val catalogRepo: CatalogRepository,
    private val cartRepo: CartRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ProductDetailViewModel(catalogRepo, cartRepo) as T
}
