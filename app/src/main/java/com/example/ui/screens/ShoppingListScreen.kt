package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryProvider
import com.example.data.model.ShoppingItem
import com.example.ui.SavorViewModel
import com.example.ui.theme.*

@Composable
fun ShoppingListScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }
    val context = LocalContext.current

    val shoppingList by viewModel.shoppingList.collectAsState()
    var selectedProvider by remember { mutableStateOf(DeliveryProvider.INSTACART) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    val estimatedTotal = remember(shoppingList) {
        shoppingList.sumOf { it.estimatedPriceUsd }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("shopping_list_screen")
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = "Culinary Shopping List",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                IconButton(
                    onClick = { showAddItemDialog = true },
                    modifier = Modifier.testTag("add_shopping_item_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item", tint = SavorAccentOrange)
                }
            }
        }

        // Summary Card
        item {
            val pendingCount = shoppingList.count { !it.isChecked }
            val completedCount = shoppingList.count { it.isChecked }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SavorGoldenYellow.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SavorGoldenYellow.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "$pendingCount items remaining ($completedCount in cart)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Ingredients consolidated from recipes & meal plan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (completedCount > 0) {
                        TextButton(onClick = { viewModel.clearCompletedShoppingItems() }) {
                            Text("Clear Done", color = SavorCrimson)
                        }
                    }
                }
            }
        }

        // Grocery Delivery Integration Card
        if (shoppingList.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SavorAccentOrange.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("grocery_delivery_integration_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = SavorAccentOrange)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "1-Tap Grocery Delivery", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Text(
                                text = "Est. $${String.format("%.2f", estimatedTotal)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = SavorAccentOrange)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select your preferred grocery partner to auto-fill your cart:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Delivery Provider Selector Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DeliveryProvider.values().forEach { provider ->
                                val isSel = selectedProvider == provider
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) SavorAccentOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSel) androidx.compose.foundation.BorderStroke(1.5.dp, SavorAccentOrange) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedProvider = provider }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                    ) {
                                        Text(text = provider.iconEmoji, fontSize = 18.sp)
                                        Text(
                                            text = provider.name.replace("_", " "),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val cartItemsText = shoppingList.joinToString("\n") { "• ${it.amount} ${it.unit} ${it.name}" }
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Savor Grocery Cart (${selectedProvider.displayName}):\n\n$cartItemsText")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Order via ${selectedProvider.displayName}"))
                                viewModel.showToast("Exported cart to ${selectedProvider.displayName}!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("order_grocery_partner_button")
                        ) {
                            Text("Order via ${selectedProvider.displayName}", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        if (shoppingList.isEmpty()) {
            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(40.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = SavorAccentOrange, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Your shopping list is empty.", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Add ingredients from recipes or meal plans to start shopping!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(shoppingList, key = { it.id }) { item ->
                ShoppingItemRow(
                    item = item,
                    onToggle = { viewModel.toggleShoppingItem(item.id) },
                    onDelete = { viewModel.removeShoppingItem(item.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showAddItemDialog) {
        var itemName by remember { mutableStateOf("") }
        var itemAmount by remember { mutableStateOf("1") }
        var itemUnit by remember { mutableStateOf("item") }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Add Grocery Item") },
            text = {
                Column {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Ingredient / Item Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = itemAmount,
                            onValueChange = { itemAmount = it },
                            label = { Text("Amount") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = itemUnit,
                            onValueChange = { itemUnit = it },
                            label = { Text("Unit") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (itemName.isNotBlank()) {
                            viewModel.addCustomShoppingItem(itemName.trim(), itemAmount.trim(), itemUnit.trim())
                            showAddItemDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (item.isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clickable { onToggle() }
            .testTag("shopping_item_${item.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = SavorAccentOrange)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (item.sourceRecipeTitle != null) {
                    Text(
                        text = "From: ${item.sourceRecipeTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SavorAccentOrange.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "${item.amount} ${item.unit}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SavorAccentOrange,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
