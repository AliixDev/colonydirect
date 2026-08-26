package com.colonydirect.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colonydirect.app.data.OrderRepository
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.dto.OrderDetailResponse
import com.colonydirect.app.network.dto.OrderSummaryResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrdersUiState(
    val orders: List<OrderSummaryResponse> = emptyList(),
    val selectedOrder: OrderDetailResponse? = null,
    val isLoading: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val isCancelling: Boolean = false,
    val hasMorePages: Boolean = false,
    val currentPage: Int = 0,
    val error: String? = null
)

class OrdersViewModel(private val repo: OrderRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init { loadOrders(reset = true) }

    fun loadOrders(reset: Boolean = false) {
        val state = _uiState.value
        if (state.isLoading) return
        val page = if (reset) 0 else state.currentPage + 1

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repo.getOrders(page = page)) {
                is NetworkResult.Success -> {
                    val newOrders = if (reset) result.data.content
                    else state.orders + result.data.content
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            orders = newOrders,
                            currentPage = result.data.number,
                            hasMorePages = !result.data.last
                        )
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

    fun loadOrderDetail(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true, selectedOrder = null, error = null) }
            when (val result = repo.getOrderById(orderId)) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isLoadingDetail = false, selectedOrder = result.data)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoadingDetail = false, error = result.message)
                }
                is NetworkResult.NetworkError -> _uiState.update {
                    it.copy(isLoadingDetail = false, error = "No internet connection")
                }
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, error = null) }
            when (val result = repo.cancelOrder(orderId)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isCancelling = false, selectedOrder = result.data) }
                    loadOrders(reset = true)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isCancelling = false, error = result.message)
                }
                is NetworkResult.NetworkError -> _uiState.update {
                    it.copy(isCancelling = false, error = "No internet connection")
                }
            }
        }
    }

    fun clearSelectedOrder() = _uiState.update { it.copy(selectedOrder = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
