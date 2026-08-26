package com.colonydirect.app.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colonydirect.app.data.CheckoutRepository
import com.colonydirect.app.network.NetworkResult
import com.colonydirect.app.network.dto.AddressRequest
import com.colonydirect.app.network.dto.AddressResponse
import com.colonydirect.app.network.dto.CheckoutResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PaymentMethodOption(val label: String, val apiValue: String) {
    COD("Cash on Delivery", "COD"),
    ONLINE("Online Payment", "ONLINE")
}

data class CheckoutUiState(
    val addresses: List<AddressResponse> = emptyList(),
    val selectedAddressId: String? = null,
    val selectedPaymentMethod: PaymentMethodOption = PaymentMethodOption.COD,
    val isLoadingAddresses: Boolean = false,
    val isPlacingOrder: Boolean = false,
    val orderResult: CheckoutResponse? = null,
    val error: String? = null,
    // Add address form
    val showAddAddressForm: Boolean = false,
    val addrFullName: String = "",
    val addrPhone: String = "",
    val addrColony: String = "",
    val addrBlock: String = "",
    val addrStreet: String = "",
    val addrHouseNumber: String = "",
    val addrInstructions: String = "",
    val isSavingAddress: Boolean = false
)

class CheckoutViewModel(private val repo: CheckoutRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init { loadAddresses() }

    fun loadAddresses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAddresses = true, error = null) }
            when (val result = repo.getAddresses()) {
                is NetworkResult.Success -> {
                    val addresses = result.data
                    val defaultId = addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id
                    _uiState.update {
                        it.copy(isLoadingAddresses = false, addresses = addresses, selectedAddressId = defaultId)
                    }
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isLoadingAddresses = false, error = result.message)
                }
                is NetworkResult.NetworkError -> _uiState.update {
                    it.copy(isLoadingAddresses = false, error = "No internet connection")
                }
            }
        }
    }

    fun selectAddress(id: String) = _uiState.update { it.copy(selectedAddressId = id) }
    fun selectPaymentMethod(method: PaymentMethodOption) = _uiState.update { it.copy(selectedPaymentMethod = method) }
    fun showAddAddress() = _uiState.update { it.copy(showAddAddressForm = true) }
    fun hideAddAddress() = _uiState.update { it.copy(showAddAddressForm = false) }

    fun onAddrFullName(v: String) = _uiState.update { it.copy(addrFullName = v) }
    fun onAddrPhone(v: String) = _uiState.update { it.copy(addrPhone = v) }
    fun onAddrColony(v: String) = _uiState.update { it.copy(addrColony = v) }
    fun onAddrBlock(v: String) = _uiState.update { it.copy(addrBlock = v) }
    fun onAddrStreet(v: String) = _uiState.update { it.copy(addrStreet = v) }
    fun onAddrHouseNumber(v: String) = _uiState.update { it.copy(addrHouseNumber = v) }
    fun onAddrInstructions(v: String) = _uiState.update { it.copy(addrInstructions = v) }

    fun saveAddress() {
        val s = _uiState.value
        if (s.addrFullName.isBlank() || s.addrPhone.isBlank() || s.addrColony.isBlank() || s.addrHouseNumber.isBlank()) {
            _uiState.update { it.copy(error = "Full name, phone, colony, and house number are required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingAddress = true, error = null) }
            val req = AddressRequest(
                fullName = s.addrFullName,
                phoneNumber = s.addrPhone,
                colony = s.addrColony,
                block = s.addrBlock.takeIf { it.isNotBlank() },
                street = s.addrStreet.takeIf { it.isNotBlank() },
                houseNumber = s.addrHouseNumber,
                deliveryInstructions = s.addrInstructions.takeIf { it.isNotBlank() },
                isDefault = s.addresses.isEmpty()
            )
            when (val result = repo.addAddress(req)) {
                is NetworkResult.Success -> {
                    val newAddr = result.data
                    val updated = s.addresses + newAddr
                    _uiState.update {
                        it.copy(
                            isSavingAddress = false,
                            addresses = updated,
                            selectedAddressId = newAddr.id,
                            showAddAddressForm = false,
                            addrFullName = "", addrPhone = "", addrColony = "",
                            addrBlock = "", addrStreet = "", addrHouseNumber = "", addrInstructions = ""
                        )
                    }
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isSavingAddress = false, error = result.message)
                }
                is NetworkResult.NetworkError -> _uiState.update {
                    it.copy(isSavingAddress = false, error = "No internet connection")
                }
            }
        }
    }

    fun placeOrder() {
        val s = _uiState.value
        val addressId = s.selectedAddressId
        if (addressId == null) {
            _uiState.update { it.copy(error = "Please select a delivery address") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingOrder = true, error = null) }
            when (val result = repo.processCheckout(addressId, s.selectedPaymentMethod.apiValue)) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(isPlacingOrder = false, orderResult = result.data)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(isPlacingOrder = false, error = result.message)
                }
                is NetworkResult.NetworkError -> _uiState.update {
                    it.copy(isPlacingOrder = false, error = "No internet connection")
                }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun resetOrderResult() = _uiState.update { it.copy(orderResult = null) }
}
