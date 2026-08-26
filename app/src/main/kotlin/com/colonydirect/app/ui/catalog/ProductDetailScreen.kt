package com.colonydirect.app.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.colonydirect.app.network.dto.ProductVariantResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    slug: String,
    viewModel: ProductDetailViewModel,
    onBack: () -> Unit,
    onGoToCart: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(slug) { viewModel.loadProduct(slug) }

    // Show snackbar when added to cart
    LaunchedEffect(state.addedToCart) {
        if (state.addedToCart) viewModel.clearAddedToCart()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.product?.name ?: "Product") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (state.product != null) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quantity selector
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { viewModel.setQuantity(state.quantity - 1) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }
                            Text("${state.quantity}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.setQuantity(state.quantity + 1) }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                        Button(
                            onClick = {
                                if (state.addedToCart) onGoToCart() else viewModel.addToCart()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !state.isAddingToCart && (state.selectedVariant?.stockQuantity ?: 0) > 0
                        ) {
                            if (state.isAddingToCart) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else if (state.addedToCart) {
                                Text("Go to Cart")
                            } else {
                                Text("Add to Cart")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.product != null) {
            val product = state.product!!
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    // Hero image placeholder
                    Box(
                        Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(product.name.take(3).uppercase(), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item {
                    Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    product.description?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // Variant selector
                if (product.variants.size > 1) {
                    item {
                        Text("Select Size / Weight", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        product.variants.filter { it.active }.forEach { variant ->
                            VariantChip(
                                variant = variant,
                                selected = state.selectedVariant?.id == variant.id,
                                onClick = { viewModel.selectVariant(variant) }
                            )
                        }
                    }
                } else if (product.variants.size == 1) {
                    item {
                        val v = product.variants.first()
                        val price = v.discountPriceAmount ?: v.priceAmount
                        Text("Rs ${price.toInt()} / ${v.unitValue} ${v.unit}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                // Stock info
                state.selectedVariant?.let { variant ->
                    item {
                        val inStock = variant.stockQuantity > 0
                        Text(
                            if (inStock) "In stock (${variant.stockQuantity} available)" else "Out of stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (inStock) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) } // bottom bar breathing room
            }
        } else {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    ?: Text("Product not found")
            }
        }
    }
}

@Composable
private fun VariantChip(variant: ProductVariantResponse, selected: Boolean, onClick: () -> Unit) {
    val inStock = variant.stockQuantity > 0
    val price = variant.discountPriceAmount ?: variant.priceAmount
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = inStock, onClick = onClick)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${variant.unitValue} ${variant.unit} — ${variant.variantName}", style = MaterialTheme.typography.bodyMedium)
        Column(horizontalAlignment = Alignment.End) {
            Text("Rs ${price.toInt()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (!inStock) Text("Out of stock", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
