package com.colonydirect.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.colonydirect.app.data.AuthRepository
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.dto.UserSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── UI State ─────────────────────────────────────────────────────────────────

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: UserSummary? = null,
    val errorMessage: String? = null
)

sealed interface AuthEvent {
    data object LoginSuccess : AuthEvent
    data object RegisterSuccess : AuthEvent
    data object LogoutSuccess : AuthEvent
    data class Error(val message: String) : AuthEvent
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<AuthEvent?>(null)
    val events: StateFlow<AuthEvent?> = _events.asStateFlow()

    init {
        // Observe stored login state on startup
        viewModelScope.launch {
            authRepository.isLoggedInFlow.collect { loggedIn ->
                _uiState.update { it.copy(isLoggedIn = loggedIn) }
            }
        }
        viewModelScope.launch {
            authRepository.currentUserFlow.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    // ─── Login ────────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        if (!validateLoginInput(email, password)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authRepository.login(email.trim(), password)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    _events.value = AuthEvent.LoginSuccess
                }
                is NetworkResult.Error -> {
                    val msg = mapServerError(result.code, result.message)
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
                NetworkResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No internet connection. Please check your network."
                        )
                    }
                }
            }
        }
    }

    // ─── Register ─────────────────────────────────────────────────────────────

    fun register(email: String, password: String, fullName: String, phone: String?) {
        if (!validateRegisterInput(email, password, fullName)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val phoneNorm = phone?.trim()?.ifBlank { null }
            when (val result = authRepository.register(email.trim(), password, fullName.trim(), phoneNorm)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    _events.value = AuthEvent.RegisterSuccess
                }
                is NetworkResult.Error -> {
                    val msg = mapServerError(result.code, result.message)
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                }
                NetworkResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No internet connection. Please check your network."
                        )
                    }
                }
            }
        }
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.logout()
            _uiState.update { it.copy(isLoading = false, isLoggedIn = false, currentUser = null) }
            _events.value = AuthEvent.LogoutSuccess
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
    fun consumeEvent() { _events.value = null }

    private fun validateLoginInput(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Email is required.") }; false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }; false
            }
            password.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Password is required.") }; false
            }
            else -> true
        }
    }

    private fun validateRegisterInput(email: String, password: String, fullName: String): Boolean {
        return when {
            fullName.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Full name is required.") }; false
            }
            email.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Email is required.") }; false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }; false
            }
            password.length < 8 -> {
                _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters.") }; false
            }
            else -> true
        }
    }

    private fun mapServerError(code: Int, message: String): String = when (code) {
        400 -> "Please check your details and try again."
        401 -> "Incorrect email or password."
        409 -> "An account with this email already exists."
        429 -> "Too many attempts. Please wait a moment and try again."
        in 500..599 -> "Server error. Please try again later."
        else -> message.ifBlank { "Something went wrong. Please try again." }
    }

    // ─── Factory ──────────────────────────────────────────────────────────────

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(authRepository) as T
    }
}
