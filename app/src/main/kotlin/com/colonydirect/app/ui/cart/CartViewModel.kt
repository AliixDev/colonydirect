package com.colonydirect.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colonydirect.app.data.CartRepository
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.dto.CartResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val cart: CartResponse? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null
)

class CartViewModel(private val repo: CartRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init { loadCart() }

    fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repo.getCart()) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isLoading = false, cart = result.data)
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

    fun incrementItem(itemId: String, currentQty: Int) {
        updateItem(itemId, currentQty + 1)
    }

    fun decrementItem(itemId: String, currentQty: Int) {
        if (currentQty <= 1) removeItem(itemId) else updateItem(itemId, currentQty - 1)
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = repo.removeItem(itemId)) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isUpdating = false, cart = result.data)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isUpdating = false, error = result.message)
                }
                is NetworkResult.NetworkError -> _uiState.update {
                    it.copy(isUpdating = false, error = "No internet connection")
                }
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            repo.clearCart()
            _uiState.update { it.copy(isUpdating = false, cart = null) }
        }
    }

    private fun updateItem(itemId: String, qty: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true) }
            when (val result = repo.updateItem(itemId, qty)) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isUpdating = false, cart = result.data)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isUpdating = false, error = result.message)
                }
                is NetworkResult.NetworkError -> _uiState.update {
                    it.copy(isUpdating = false, error = "No internet connection")
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
