package com.colonydirect.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colonydirect.app.data.AuthRepository
import com.colonydirect.app.network.dto.UserSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserSummary? = null,
    val isLoggingOut: Boolean = false
)

class ProfileViewModel(private val authRepo: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepo.getCurrentUserFlow.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            authRepo.logout()
            _uiState.update { it.copy(isLoggingOut = false) }
            onDone()
        }
    }
}
