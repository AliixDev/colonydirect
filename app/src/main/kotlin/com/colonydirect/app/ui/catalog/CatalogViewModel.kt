package com.colonydirect.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colonydirect.app.data.CatalogRepository
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.dto.CategoryResponse
import com.colonydirect.app.network.dto.ProductDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val categories: List<CategoryResponse> = emptyList(),
    val products: List<ProductDetailResponse> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val isLoadingCategories: Boolean = false,
    val isLoadingProducts: Boolean = false,
    val hasMorePages: Boolean = false,
    val currentPage: Int = 0,
    val error: String? = null
)

class CatalogViewModel(private val repo: CatalogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadProducts(reset = true)
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true, error = null) }
            when (val result = repo.getCategories()) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isLoadingCategories = false, categories = result.data)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoadingCategories = false, error = result.message)
                }
                is NetworkResult.NetworkError -> _uiState.update {
                    it.copy(isLoadingCategories = false, error = "No internet connection")
                }
            }
        }
    }

    fun selectCategory(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadProducts(reset = true)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun search() {
        loadProducts(reset = true)
    }

    fun loadProducts(reset: Boolean = false) {
        val state = _uiState.value
        if (state.isLoadingProducts) return
        val page = if (reset) 0 else state.currentPage + 1

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProducts = true, error = null) }
            val q = state.searchQuery.takeIf { it.isNotBlank() }
            when (val result = repo.searchProducts(
                categoryId = state.selectedCategoryId,
                query = q,
                page = page
            )) {
                is NetworkResult.Success -> {
                    val newProducts = if (reset) result.data.content
                    else state.products + result.data.content
                    _uiState.update {
                        it.copy(
                            isLoadingProducts = false,
                            products = newProducts,
                            currentPage = result.data.number,
                            hasMorePages = !result.data.last
                        )
                    }
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoadingProducts = false, error = result.message)
                }
                is NetworkResult.NetworkError -> _uiState.update {
                    it.copy(isLoadingProducts = false, error = "No internet connection")
                }
            }
        }
    }

    fun loadNextPage() {
        if (_uiState.value.hasMorePages) loadProducts(reset = false)
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
