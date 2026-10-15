package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Allergen
import com.example.data.model.CustomIngredientItem
import com.example.ui.SavorViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NutritionalDatabaseScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val customIngredients by viewModel.customIngredients.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = remember(customIngredients, searchQuery) {
        if (searchQuery.isBlank()) customIngredients
        else customIngredients.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("nutritional_database_screen")
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
                    text = "Nutritional Database",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_custom_ingredient_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Ingredient", tint = SavorAccentOrange)
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search ingredients or categories...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SavorAccentOrange) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Info Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SavorGoldenYellow.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SavorGoldenYellow.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = SavorAccentOrange)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Customize and manage macro values per 100g. Used by the automatic nutrition calculator when crafting recipes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .testTag("ingredient_item_${item.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = item.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(text = "${item.category} • Serving: ${item.defaultServing}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.deleteCustomIngredient(item.id) }) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = SavorCrimson)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Macro Breakdown Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(shape = RoundedCornerShape(8.dp), color = SavorAccentOrange.copy(alpha = 0.15f)) {
                            Text(text = "${item.caloriesPer100g} kcal / 100g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SavorAccentOrange, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF4CAF50).copy(alpha = 0.15f)) {
                            Text(text = "P: ${item.proteinPer100g}g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF4CAF50), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFF9800).copy(alpha = 0.15f)) {
                            Text(text = "C: ${item.carbsPer100g}g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFFF9800), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE91E63).copy(alpha = 0.15f)) {
                            Text(text = "F: ${item.fatPer100g}g", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFE91E63), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    if (item.allergens.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            item.allergens.forEach { alg ->
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(alg.colorHex).copy(alpha = 0.15f)) {
                                    Text(
                                        text = "${alg.icon} ${alg.label}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(alg.colorHex),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Produce") }
        var cal by remember { mutableStateOf("120") }
        var pro by remember { mutableStateOf("4") }
        var carb by remember { mutableStateOf("20") }
        var fat by remember { mutableStateOf("2") }
        var defaultServ by remember { mutableStateOf("100g") }
        val selectedAllergens = remember { mutableStateListOf<Allergen>() }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Custom Ingredient") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ingredient Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Grain, Meat, Dairy)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(value = cal, onValueChange = { cal = it }, label = { Text("Calories") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = pro, onValueChange = { pro = it }, label = { Text("Protein (g)") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(value = carb, onValueChange = { carb = it }, label = { Text("Carbs (g)") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("Fat (g)") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Allergens:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Allergen.values().forEach { alg ->
                            val isSel = selectedAllergens.contains(alg)
                            FilterChip(
                                selected = isSel,
                                onClick = { if (isSel) selectedAllergens.remove(alg) else selectedAllergens.add(alg) },
                                label = { Text("${alg.icon} ${alg.label}", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addCustomIngredient(
                                name = name.trim(),
                                category = category.trim(),
                                calories = cal.toIntOrNull() ?: 100,
                                protein = pro.toIntOrNull() ?: 0,
                                carbs = carb.toIntOrNull() ?: 0,
                                fat = fat.toIntOrNull() ?: 0,
                                allergens = selectedAllergens.toList(),
                                defaultServing = defaultServ
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)
                ) {
                    Text("Save Ingredient", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
