package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.components.DietaryTagChip
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCreateScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var currentStep by remember { mutableStateOf(1) } // 1: Media, 2: Cover/Crop, 3: Details

    // Step 1 State: Media
    var selectedMediaType by remember { mutableStateOf(MediaType.VIDEO) }
    var videoDurationInput by remember { mutableStateOf("45") } // simulated video length in seconds
    var mediaErrorMessage by remember { mutableStateOf<String?>(null) }

    // Step 2 State: Cover Frame
    var coverFramePosition by remember { mutableStateOf(0.5f) }

    // Step 3 State: Recipe Details
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("25") }
    var servings by remember { mutableStateOf("2") }
    val ingredients = remember {
        mutableStateListOf(
            Ingredient("Wild Salmon Fillets", "2", "fillets"),
            Ingredient("Garlic", "3", "cloves"),
            Ingredient("Olive oil", "2", "tbsp")
        )
    }
    val steps = remember {
        mutableStateListOf(
            "Prep ingredients and preheat cooking surface.",
            "Sear or saute until golden aromatic perfection.",
            "Plate and garnish with fresh herbs."
        )
    }
    val selectedTags = remember { mutableStateListOf(DietaryTag.KETO, DietaryTag.HIGH_PROTEIN) }
    var selectedPrivacy by remember { mutableStateOf(PrivacyLevel.PUBLIC) }

    // Nutrition values (auto-calculated for Premium users)
    var calories by remember { mutableStateOf("420") }
    var protein by remember { mutableStateOf("34") }
    var carbs by remember { mutableStateOf("8") }
    var fat by remember { mutableStateOf("26") }

    // Auto-calculate triggered whenever ingredients change
    fun runAutoCalc() {
        val s = servings.toIntOrNull() ?: 2
        val calc = NutritionCalculator.calculate(ingredients, s)
        calories = "${calc.calories}"
        protein = "${calc.protein}"
        carbs = "${calc.carbs}"
        fat = "${calc.fat}"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("recipe_create_screen")
    ) {
        // --- Flow Progress Stepper Header ---
        item {
            Column(modifier = Modifier.padding(vertical = 14.dp)) {
                Text(
                    text = "New Recipe Reel",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 3-step indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StepBubble(stepNumber = 1, title = "Media", isActive = currentStep == 1, isDone = currentStep > 1, modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.width(20.dp).height(2.dp).background(if (currentStep > 1) SavorAccentOrange else MaterialTheme.colorScheme.outline))
                    StepBubble(stepNumber = 2, title = "Cover", isActive = currentStep == 2, isDone = currentStep > 2, modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.width(20.dp).height(2.dp).background(if (currentStep > 2) SavorAccentOrange else MaterialTheme.colorScheme.outline))
                    StepBubble(stepNumber = 3, title = "Details", isActive = currentStep == 3, isDone = false, modifier = Modifier.weight(1f))
                }
            }
        }

        // --- STEP 1: Select Media ---
        if (currentStep == 1) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Step 1: Choose Media Type",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Upload high-res photos or short video reels up to 60 seconds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Video Reel Choice
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedMediaType == MediaType.VIDEO) SavorAccentOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (selectedMediaType == MediaType.VIDEO) androidx.compose.foundation.BorderStroke(2.dp, SavorAccentOrange) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedMediaType = MediaType.VIDEO
                                        mediaErrorMessage = null
                                    }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.MovieFilter, contentDescription = null, tint = SavorAccentOrange)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Video Reel (≤60s)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }

                            // Single Photo Choice
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedMediaType == MediaType.IMAGE) SavorAccentOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (selectedMediaType == MediaType.IMAGE) androidx.compose.foundation.BorderStroke(2.dp, SavorAccentOrange) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedMediaType = MediaType.IMAGE
                                        mediaErrorMessage = null
                                    }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = SavorGoldenYellow)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Photo Gallery", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Video Reel Duration Configuration & Validation
                        if (selectedMediaType == MediaType.VIDEO) {
                            Text(
                                text = "Simulated Video Duration (Seconds):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = videoDurationInput,
                                onValueChange = {
                                    videoDurationInput = it
                                    val sec = it.toIntOrNull() ?: 0
                                    if (sec > 60) {
                                        mediaErrorMessage = "Video must be 60 seconds or less"
                                    } else {
                                        mediaErrorMessage = null
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("video_duration_input")
                            )

                            if (mediaErrorMessage != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = mediaErrorMessage!!,
                                    color = SavorCrimson,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.testTag("video_error_message")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val sec = videoDurationInput.toIntOrNull() ?: 0
                                if (selectedMediaType == MediaType.VIDEO && sec > 60) {
                                    mediaErrorMessage = "Video must be 60 seconds or less"
                                } else {
                                    mediaErrorMessage = null
                                    currentStep = 2
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("step_1_next_button")
                        ) {
                            Text("Next: Select Cover Frame", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- STEP 2: Cover Frame Selection ---
        if (currentStep == 2) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Step 2: Choose Cover Frame",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Slide across the video timeline to select the most mouthwatering thumbnail cover.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Preview box with gradient
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFFFF7043),
                                            Color(0xFFFFB74D)
                                        )
                                    )
                                )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FilterVintage,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Cover Frame at ${(coverFramePosition * (videoDurationInput.toIntOrNull() ?: 45)).toInt()}s",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Scrub Timeline Position: ${(coverFramePosition * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Slider(
                            value = coverFramePosition,
                            onValueChange = { coverFramePosition = it },
                            colors = SliderDefaults.colors(
                                thumbColor = SavorAccentOrange,
                                activeTrackColor = SavorAccentOrange
                            ),
                            modifier = Modifier.testTag("cover_frame_slider")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 1 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }
                            Button(
                                onClick = {
                                    runAutoCalc()
                                    currentStep = 3
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                                modifier = Modifier
                                    .weight(2f)
                                    .testTag("step_2_next_button")
                            ) {
                                Text("Next: Recipe Details", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- STEP 3: Recipe Details, Nutrition & Privacy ---
        if (currentStep == 3) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Step 3: Recipe Information",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Recipe Title") },
                            placeholder = { Text("e.g. Crispy Chili Crunch Salmon") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recipe_title_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Short Story & Description") },
                            placeholder = { Text("Describe the flavors, origins, and key secrets...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recipe_desc_input"),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cook time & Servings
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = cookTime,
                                onValueChange = { cookTime = it },
                                label = { Text("Cook Time (mins)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = servings,
                                onValueChange = {
                                    servings = it
                                    runAutoCalc()
                                },
                                label = { Text("Servings") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Privacy Level Selector
                        Text(
                            text = "Recipe Privacy Level:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        PrivacyLevel.values().forEach { level ->
                            val isSelected = selectedPrivacy == level
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) SavorAccentOrange.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, SavorAccentOrange) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { selectedPrivacy = level }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedPrivacy = level },
                                        colors = RadioButtonDefaults.colors(selectedColor = SavorAccentOrange)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = level.label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text(text = level.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dietary Tags Multi-select
                        Text(
                            text = "Dietary Tags:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            DietaryTag.values().forEach { tag ->
                                val isSelected = selectedTags.contains(tag)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                                    },
                                    label = { Text(tag.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(tag.colorHex),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic Ingredients
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Ingredients (${ingredients.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            TextButton(
                                onClick = {
                                    ingredients.add(Ingredient("New Ingredient", "1", "serving"))
                                    runAutoCalc()
                                }
                            ) {
                                Text("+ Add Ingredient", color = SavorAccentOrange)
                            }
                        }

                        ingredients.forEachIndexed { index, ingredient ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                OutlinedTextField(
                                    value = ingredient.name,
                                    onValueChange = {
                                        ingredients[index] = ingredient.copy(name = it)
                                        runAutoCalc()
                                    },
                                    label = { Text("Name") },
                                    modifier = Modifier.weight(2f),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = ingredient.amount,
                                    onValueChange = {
                                        ingredients[index] = ingredient.copy(amount = it)
                                        runAutoCalc()
                                    },
                                    label = { Text("Qty") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        if (ingredients.size > 1) {
                                            ingredients.removeAt(index)
                                            runAutoCalc()
                                        }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = SavorCrimson)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nutritional Breakdown (Premium Auto-Calculate Feature)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SavorGoldenYellow.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SavorGoldenYellow.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("premium_nutrition_calculator_box")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = SavorAccentOrange)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Auto-Calculated Nutrition",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    if (currentUser.isPremium) {
                                        TextButton(onClick = { runAutoCalc() }) {
                                            Text("Recalculate", color = SavorAccentOrange)
                                        }
                                    }
                                }

                                if (currentUser.isPremium) {
                                    Text(
                                        text = "Auto-estimated per serving based on ingredients. You can manually adjust any value below:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = calories,
                                            onValueChange = { calories = it },
                                            label = { Text("Calories") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("calories_input"),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = protein,
                                            onValueChange = { protein = it },
                                            label = { Text("Protein (g)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = carbs,
                                            onValueChange = { carbs = it },
                                            label = { Text("Carbs (g)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = fat,
                                            onValueChange = { fat = it },
                                            label = { Text("Fat (g)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Auto-calculating calories and macronutrients is exclusive to Savor Premium members.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.openUpgradeModal() },
                                        colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)
                                    ) {
                                        Text("Unlock with Premium", color = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Submit Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 2 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }
                            Button(
                                onClick = {
                                    val safeTitle = title.ifBlank { "Chef's Daily Special" }
                                    val newRecipe = Recipe(
                                        id = "rec_${UUID.randomUUID().toString().take(8)}",
                                        authorId = currentUser.id,
                                        authorUsername = currentUser.username,
                                        authorAvatarUrl = currentUser.avatarUrl,
                                        isAuthorVerified = currentUser.hasVerificationBadge,
                                        isAuthorPremium = currentUser.isPremium,
                                        title = safeTitle,
                                        description = description,
                                        cookTimeMinutes = cookTime.toIntOrNull() ?: 25,
                                        servings = servings.toIntOrNull() ?: 2,
                                        mediaType = selectedMediaType,
                                        videoDurationSeconds = if (selectedMediaType == MediaType.VIDEO) videoDurationInput.toIntOrNull() ?: 45 else 0,
                                        foodArtGradientId = (1..6).random(),
                                        ingredients = ingredients.toList(),
                                        steps = steps.toList(),
                                        tags = selectedTags.toList(),
                                        privacyLevel = selectedPrivacy,
                                        nutrition = if (currentUser.isPremium) {
                                            NutritionInfo(
                                                calories = calories.toIntOrNull() ?: 400,
                                                protein = protein.toIntOrNull() ?: 30,
                                                carbs = carbs.toIntOrNull() ?: 15,
                                                fat = fat.toIntOrNull() ?: 20
                                            )
                                        } else null
                                    )
                                    viewModel.publishRecipe(newRecipe)
                                    viewModel.navigateTo(Screen.Feed)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                                modifier = Modifier
                                    .weight(2f)
                                    .height(48.dp)
                                    .testTag("publish_recipe_button")
                            ) {
                                Text("Publish Recipe", color = Color.White, fontWeight = FontWeight.Bold)
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
}

@Composable
fun StepBubble(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isDone: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isDone || isActive) SavorAccentOrange else MaterialTheme.colorScheme.surfaceVariant
                )
        ) {
            if (isDone) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal),
            color = if (isActive) SavorAccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
