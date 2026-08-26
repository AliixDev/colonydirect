package com.colonydirect.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colonydirect.app.data.DashboardRepository
import com.colonydirect.app.data.OrderRepository
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.dto.CustomerDashboardSummaryResponse
import com.colonydirect.app.network.dto.OrderSummaryResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val summary: CustomerDashboardSummaryResponse? = null,
    val recentOrders: List<OrderSummaryResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(
    private val dashboardRepo: DashboardRepository,
    private val orderRepo: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val summaryDeferred = async { dashboardRepo.getCustomerSummary() }
            val ordersDeferred = async { orderRepo.getOrders(page = 0, size = 5) }

            val summaryResult = summaryDeferred.await()
            val ordersResult = ordersDeferred.await()

            val summary = if (summaryResult is NetworkResult.Success) summaryResult.data else null
            val orders = if (ordersResult is NetworkResult.Success) ordersResult.data.content else emptyList()
            val err = when {
                summaryResult is NetworkResult.Error -> summaryResult.message
                summaryResult is NetworkResult.NetworkError -> "No internet connection"
                else -> null
            }
            _uiState.update {
                it.copy(isLoading = false, summary = summary, recentOrders = orders, error = err)
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
