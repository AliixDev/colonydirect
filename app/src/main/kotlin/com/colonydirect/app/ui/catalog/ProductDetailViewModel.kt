package com.colonydirect.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colonydirect.app.data.CartRepository
import com.colonydirect.app.data.CatalogRepository
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.dto.ProductDetailResponse
import com.colonydirect.app.network.dto.ProductVariantResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: ProductDetailResponse? = null,
    val selectedVariant: ProductVariantResponse? = null,
    val quantity: Int = 1,
    val isLoading: Boolean = false,
    val isAddingToCart: Boolean = false,
    val addedToCart: Boolean = false,
    val error: String? = null
)

class ProductDetailViewModel(
    private val catalogRepo: CatalogRepository,
    private val cartRepo: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun loadProduct(slug: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = catalogRepo.getProductBySlug(slug)) {
                is NetworkResult.Success -> {
                    val product = result.data
                    val firstVariant = product.variants.firstOrNull { it.active && it.stockQuantity > 0 }
                        ?: product.variants.firstOrNull()
                    _uiState.update {
                        it.copy(isLoading = false, product = product, selectedVariant = firstVariant)
                    }
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is NetworkResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, error = "No internet connection")
                }
            }
        }
    }

    fun selectVariant(variant: ProductVariantResponse) =
        _uiState.update { it.copy(selectedVariant = variant) }

    fun setQuantity(qty: Int) {
        if (qty in 1..99) _uiState.update { it.copy(quantity = qty) }
    }

    fun addToCart() {
        val state = _uiState.value
        val product = state.product ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingToCart = true, error = null) }
            when (val result = cartRepo.addItem(
                productId = product.id,
                variantId = state.selectedVariant?.id,
                quantity = state.quantity
            )) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isAddingToCart = false, addedToCart = true)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isAddingToCart = false, error = result.message)
                }
                is NetworkResult.NetworkError -> _uiState.update {
                    it.copy(isAddingToCart = false, error = "No internet connection")
                }
            }
        }
    }

    fun clearAddedToCart() = _uiState.update { it.copy(addedToCart = false) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
