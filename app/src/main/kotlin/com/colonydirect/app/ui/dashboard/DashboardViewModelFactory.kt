package com.colonydirect.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.colonydirect.app.data.DashboardRepository
import com.colonydirect.app.data.OrderRepository

class DashboardViewModelFactory(
    private val dashboardRepo: DashboardRepository,
    private val orderRepo: OrderRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DashboardViewModel(dashboardRepo, orderRepo) as T
}
