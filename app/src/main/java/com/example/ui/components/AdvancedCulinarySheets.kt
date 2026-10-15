package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.SavorViewModel
import com.example.ui.theme.*

// --- 1. Ingredient Substitution Sheet ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientSubstitutionSheet(
    ingredient: Ingredient,
    onDismiss: () -> Unit
) {
    val substitutes = remember(ingredient) {
        CulinaryTools.getSubstitutionsFor(ingredient.name)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("ingredient_substitution_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = SavorAccentOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Substitutions for '${ingredient.name}'",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = "Missing an ingredient or need a dietary swap? Try these tested alternatives:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            substitutes.forEach { sub ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SavorGoldenYellow.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sub.substituteName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SavorAccentOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = sub.dietaryBenefit,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SavorAccentOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ratio: ${sub.ratio}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = sub.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// --- 2. Live Culinary Unit Converter Sheet ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterSheet(
    onDismiss: () -> Unit
) {
    var inputValue by remember { mutableStateOf("1") }
    var selectedCategory by remember { mutableStateOf("Volume") } // Volume, Weight, Temp
    var fromUnit by remember { mutableStateOf("cups") }
    var toUnit by remember { mutableStateOf("ml") }

    val volumeUnits = listOf("cups" to "ml", "tbsp" to "ml", "tsp" to "ml", "ml" to "cups")
    val weightUnits = listOf("oz" to "g", "g" to "oz", "lbs" to "kg", "kg" to "lbs")
    val tempUnits = listOf("°F" to "°C", "°C" to "°F")

    val currentPairs = when (selectedCategory) {
        "Volume" -> volumeUnits
        "Weight" -> weightUnits
        else -> tempUnits
    }

    val result = remember(inputValue, fromUnit, toUnit) {
        val d = inputValue.toDoubleOrNull() ?: 1.0
        CulinaryTools.convertUnits(d, fromUnit, toUnit)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("unit_converter_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Scale, contentDescription = null, tint = SavorAccentOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Culinary Unit & Temp Converter",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Category Selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Volume", "Weight", "Temp").forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            val firstPair = when (cat) {
                                "Volume" -> volumeUnits.first()
                                "Weight" -> weightUnits.first()
                                else -> tempUnits.first()
                            }
                            fromUnit = firstPair.first
                            toUnit = firstPair.second
                        },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SavorAccentOrange,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Conversion Direction Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                currentPairs.forEach { (f, t) ->
                    val isSel = fromUnit == f && toUnit == t
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) SavorGoldenYellow.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, SavorAccentOrange) else null,
                        modifier = Modifier.clickable {
                            fromUnit = f
                            toUnit = t
                        }
                    ) {
                        Text(
                            text = "$f → $t",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSel) SavorAccentOrange else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("Enter Quantity ($fromUnit)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Conversion Output Display Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SavorGoldenYellow.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SavorGoldenYellow.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "${String.format("%.2f", result.convertedValue)} $toUnit",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                        color = SavorAccentOrange
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.formula,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// --- 3. Recipe Versioning & Edit History Dialog ---
@Composable
fun RecipeVersionHistoryDialog(
    recipe: Recipe,
    viewModel: SavorViewModel,
    onDismiss: () -> Unit
) {
    var showCreateSnapshot by remember { mutableStateOf(false) }
    var changeNote by remember { mutableStateOf("") }
    var adjustedCookTime by remember { mutableStateOf("${recipe.cookTimeMinutes}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = SavorAccentOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recipe Edit History & Versions")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${recipe.title} (Current: v1.${recipe.versions.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (recipe.versions.isEmpty()) {
                    Text("Original initial publication (v1.0).", style = MaterialTheme.typography.bodySmall)
                } else {
                    recipe.versions.forEach { ver ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = ver.versionNumber, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = SavorAccentOrange)
                                    Text(text = "By @${ver.editorUsername}", style = MaterialTheme.typography.labelSmall)
                                }
                                Text(text = ver.changelogNote, style = MaterialTheme.typography.bodySmall)
                                Text(text = "${ver.cookTimeMinutes} mins cook time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                if (showCreateSnapshot) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = changeNote,
                        onValueChange = { changeNote = it },
                        label = { Text("What did you change? (e.g. Added garlic confit)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (showCreateSnapshot) {
                Button(
                    onClick = {
                        if (changeNote.isNotBlank()) {
                            viewModel.addRecipeVersion(recipe.id, changeNote.trim(), adjustedCookTime.toIntOrNull())
                            showCreateSnapshot = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)
                ) {
                    Text("Save Version", color = Color.White)
                }
            } else {
                Button(
                    onClick = { showCreateSnapshot = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)
                ) {
                    Text("+ Log New Edit", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// --- 4. Co-Author Inviter Dialog ---
@Composable
fun CoAuthorPickerDialog(
    recipe: Recipe,
    viewModel: SavorViewModel,
    onDismiss: () -> Unit
) {
    val users by viewModel.users.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null, tint = SavorAccentOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Invite Co-Author")
            }
        },
        text = {
            Column {
                Text(
                    text = "Co-authors appear on the recipe post and share recognition & Chef Flower credits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                users.filter { it.id != currentUser.id }.forEach { candidate ->
                    val isAlready = recipe.coAuthors.any { it.userId == candidate.id }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable(enabled = !isAlready) {
                                viewModel.inviteCoAuthor(recipe.id, candidate)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Column {
                                Text(text = candidate.displayName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text(text = "@${candidate.username} • ${candidate.city}", style = MaterialTheme.typography.labelSmall)
                            }
                            if (isAlready) {
                                Text(text = "Co-Author", style = MaterialTheme.typography.labelSmall, color = SavorAccentOrange)
                            } else {
                                Text(text = "+ Add", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SavorAccentOrange)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

// --- 5. Add to Playlist / Collection Sheet ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCollectionSheet(
    recipe: Recipe,
    viewModel: SavorViewModel,
    onDismiss: () -> Unit
) {
    val collections by viewModel.collections.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_to_collection_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Add to Recipe Playlist",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Organize '${recipe.title}' into curated culinary collections.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            collections.forEach { col ->
                val isInCol = col.recipeIds.contains(recipe.id)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isInCol) SavorAccentOrange.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = if (isInCol) androidx.compose.foundation.BorderStroke(1.dp, SavorAccentOrange) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.toggleRecipeInCollection(col.id, recipe.id) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column {
                            Text(text = col.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(text = "${col.recipeIds.size} recipes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Checkbox(
                            checked = isInCol,
                            onCheckedChange = { viewModel.toggleRecipeInCollection(col.id, recipe.id) },
                            colors = CheckboxDefaults.colors(checkedColor = SavorAccentOrange)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
