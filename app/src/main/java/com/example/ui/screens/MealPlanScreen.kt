package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.components.FoodVisualThumbnail
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val mealPlan by viewModel.mealPlan.collectAsState()
    val templates by viewModel.templates.collectAsState()

    // Premium Gate Check
    if (!currentUser.isPremium) {
        MealPlanPremiumGate(
            onUpgradeClick = { viewModel.navigateTo(Screen.Checkout) },
            modifier = modifier
        )
        return
    }

    var selectedDayIndex by remember { mutableStateOf(0) } // 0..6
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    var showTemplatesSheet by remember { mutableStateOf(false) }
    var templateToLoad by remember { mutableStateOf<MealPlanTemplate?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("meal_plan_screen")
    ) {
        // --- Header & Pro Badge ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Weekly Meal Calendar",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SavorGoldenYellow.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "PREMIUM",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = SavorAccentOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Structured nutrition & meal planning for peak wellness",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar (Auto-Generate, Templates, Bulk Add to Cart)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Auto-Generate Button
                    Button(
                        onClick = { viewModel.autoGenerateMealPlan() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("auto_generate_meal_plan_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto-Generate", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Templates Button
                    OutlinedButton(
                        onClick = { showTemplatesSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("templates_button")
                    ) {
                        Icon(imageVector = Icons.Default.Bookmarks, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Templates", fontSize = 12.sp)
                    }

                    // Bulk Add Week to Grocery
                    IconButton(
                        onClick = { viewModel.bulkAddWeekToShoppingList() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SavorGoldenYellow.copy(alpha = 0.2f))
                            .testTag("bulk_add_grocery_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = "Add Week to Cart", tint = SavorAccentOrange)
                    }
                }
            }
        }

        // --- 7-Day Day Selector Tabs ---
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(days.size) { idx ->
                    val isSelected = selectedDayIndex == idx
                    val daySlots = mealPlan.filter { it.dayOfWeek == idx }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) SavorAccentOrange else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .width(80.dp)
                            .clickable { selectedDayIndex = idx }
                            .testTag("day_tab_$idx")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = days[idx].take(3),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${daySlots.size} meals",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // --- Meal Slots for Selected Day (Breakfast, Lunch, Dinner, Snack) ---
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "${days[selectedDayIndex]} Menu",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                MealSlot.values().forEach { slot ->
                    val entry = mealPlan.find { it.dayOfWeek == selectedDayIndex && it.slot == slot }
                    MealSlotAssignmentCard(
                        slot = slot,
                        entry = entry,
                        onRecipeClick = {
                            if (entry != null) {
                                viewModel.navigateTo(Screen.RecipeDetail(entry.recipe.id))
                            }
                        },
                        onRemoveClick = {
                            if (entry != null) {
                                viewModel.removeMealPlanEntry(entry.id)
                            }
                        },
                        onBrowseClick = {
                            viewModel.navigateTo(Screen.Explore)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // --- Save Template Quick CTA ---
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Save Current Week as Template",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Reuse this 7-day rotation anytime with 1 tap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showSaveTemplateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SavorGoldenYellow),
                        modifier = Modifier.testTag("save_as_template_button")
                    ) {
                        Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // --- Save Template Dialog ---
    if (showSaveTemplateDialog) {
        var templateName by remember { mutableStateOf("Weekly Wellness Rotation") }
        var templateDesc by remember { mutableStateOf("Balanced meals with high-protein dinners.") }

        AlertDialog(
            onDismissRequest = { showSaveTemplateDialog = false },
            title = { Text("Save Meal Plan Template") },
            text = {
                Column {
                    OutlinedTextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        label = { Text("Template Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = templateDesc,
                        onValueChange = { templateDesc = it },
                        label = { Text("Description") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (templateName.isNotBlank()) {
                            viewModel.saveMealPlanTemplate(templateName.trim(), templateDesc.trim())
                            showSaveTemplateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)
                ) {
                    Text("Save Template", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveTemplateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Templates Sheet ---
    if (showTemplatesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTemplatesSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Saved Meal Plan Templates",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Load a pre-configured 7-day culinary plan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (templates.isEmpty()) {
                    Text("No templates saved yet.", modifier = Modifier.padding(vertical = 12.dp))
                } else {
                    templates.forEach { tmpl ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tmpl.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = tmpl.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${tmpl.entries.size} assigned meal slots",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SavorAccentOrange
                                    )
                                }

                                Row {
                                    Button(
                                        onClick = {
                                            templateToLoad = tmpl
                                            showTemplatesSheet = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Load", color = Color.White)
                                    }
                                    IconButton(onClick = { viewModel.deleteMealPlanTemplate(tmpl.id) }) {
                                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = SavorCrimson)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Overwrite Confirmation Dialog when Loading Template ---
    templateToLoad?.let { tmpl ->
        AlertDialog(
            onDismissRequest = { templateToLoad = null },
            title = { Text("Overwrite Weekly Plan?") },
            text = {
                Text("Loading '${tmpl.name}' will replace your currently assigned meal slots for all 7 days. Do you wish to continue?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loadMealPlanTemplate(tmpl)
                        templateToLoad = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)
                ) {
                    Text("Load & Replace", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToLoad = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- Meal Slot Assignment Card ---
@Composable
fun MealSlotAssignmentCard(
    slot: MealSlot,
    entry: MealPlanEntry?,
    onRecipeClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (entry != null) SavorGoldenYellow.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SavorAccentOrange.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = when (slot) {
                        MealSlot.BREAKFAST -> Icons.Default.WbSunny
                        MealSlot.LUNCH -> Icons.Default.Restaurant
                        MealSlot.DINNER -> Icons.Default.DinnerDining
                        MealSlot.SNACK -> Icons.Default.Cookie
                    },
                    contentDescription = null,
                    tint = SavorAccentOrange,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slot.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (entry != null) {
                    Text(
                        text = entry.recipe.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { onRecipeClick() }
                    )
                    if (entry.recipe.nutrition != null) {
                        Text(
                            text = "${entry.recipe.nutrition.calories} kcal • ${entry.recipe.nutrition.protein}g protein",
                            style = MaterialTheme.typography.labelSmall,
                            color = SavorAccentOrange
                        )
                    }
                } else {
                    Text(
                        text = "No recipe assigned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (entry != null) {
                IconButton(onClick = onRemoveClick) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                TextButton(onClick = onBrowseClick) {
                    Text("+ Assign", color = SavorAccentOrange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- Free User Upgrade Gate for Meal Plan ---
@Composable
fun MealPlanPremiumGate(
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("meal_plan_premium_gate")
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(SavorGradientBrush)
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Savor Meal Planner",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "A structured weekly culinary system designed for health and taste",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                val features = listOf(
                    "📅 7-Day Interactive Meal Calendar",
                    "🤖 1-Tap Auto-Generation from Top Liked Recipes",
                    "📋 Weekly Reusable Templates",
                    "🛒 Bulk Ingredient Aggregation to Grocery List",
                    "⚡ Calorie & Macronutrient Tracking"
                )

                features.forEach { feat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = feat, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onUpgradeClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("gate_go_premium_button")
                ) {
                    Text("Go Premium to Unlock", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
