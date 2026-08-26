package com.colonydirect.app.ui.checkout

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.colonydirect.app.network.dto.AddressResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onBack: () -> Unit,
    onOrderSuccess: (orderId: String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Navigate on success
    LaunchedEffect(state.orderResult) {
        state.orderResult?.let { result ->
            onOrderSuccess(result.orderId)
            viewModel.resetOrderResult()
        }
    }

    if (state.showAddAddressForm) {
        AddAddressSheet(viewModel = viewModel, onDismiss = viewModel::hideAddAddress)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = viewModel::placeOrder,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    enabled = !state.isPlacingOrder && state.selectedAddressId != null
                ) {
                    if (state.isPlacingOrder) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Place Order")
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {

            // Delivery Address section
            item {
                SectionHeader("Delivery Address")
                Spacer(Modifier.height(8.dp))

                if (state.isLoadingAddresses) {
                    Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    state.addresses.forEach { address ->
                        AddressCard(
                            address = address,
                            selected = state.selectedAddressId == address.id,
                            onClick = { viewModel.selectAddress(address.id) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    FilledTonalButton(
                        onClick = viewModel::showAddAddress,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add New Address")
                    }
                }
            }

            // Payment method section
            item {
                SectionHeader("Payment Method")
                Spacer(Modifier.height(8.dp))
                PaymentMethodOption.values().forEach { method ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (state.selectedPaymentMethod == method) 2.dp else 1.dp,
                                color = if (state.selectedPaymentMethod == method) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.selectPaymentMethod(method) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.selectedPaymentMethod == method,
                            onClick = { viewModel.selectPaymentMethod(method) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(method.label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (state.selectedPaymentMethod == method) FontWeight.SemiBold else FontWeight.Normal)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Error display
            state.error?.let { err ->
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(err, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun AddressCard(address: AddressResponse, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(address.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(buildAddressLine(address), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(address.phoneNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (selected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

private fun buildAddressLine(a: AddressResponse): String {
    return listOfNotNull(
        "H# ${a.houseNumber}",
        a.street?.let { "St $it" },
        a.block?.let { "Block $it" },
        a.colony
    ).joinToString(", ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressSheet(viewModel: CheckoutViewModel, onDismiss: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Address") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = viewModel::saveAddress,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = !state.isSavingAddress
            ) {
                if (state.isSavingAddress) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save Address")
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { OutlinedTextField(value = state.addrFullName, onValueChange = viewModel::onAddrFullName, label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { OutlinedTextField(value = state.addrPhone, onValueChange = viewModel::onAddrPhone, label = { Text("Phone Number *") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { OutlinedTextField(value = state.addrColony, onValueChange = viewModel::onAddrColony, label = { Text("Colony / Area *") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { OutlinedTextField(value = state.addrBlock, onValueChange = viewModel::onAddrBlock, label = { Text("Block") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { OutlinedTextField(value = state.addrStreet, onValueChange = viewModel::onAddrStreet, label = { Text("Street") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { OutlinedTextField(value = state.addrHouseNumber, onValueChange = viewModel::onAddrHouseNumber, label = { Text("House Number *") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { OutlinedTextField(value = state.addrInstructions, onValueChange = viewModel::onAddrInstructions, label = { Text("Delivery Instructions") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }
            state.error?.let { err ->
                item {
                    Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
