package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val recipes by viewModel.recipes.collectAsState()
    val commentsMap by viewModel.comments.collectAsState()
    val reviewsMap by viewModel.reviews.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val recipe = recipes.find { it.id == recipeId }

    if (recipe == null) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("Recipe not found.")
        }
        return
    }

    // Privacy Check
    val canView = viewModel.repository.canUserViewRecipe(recipe)
    if (!canView) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("recipe_access_denied_view")
        ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = SavorAccentOrange, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (recipe.privacyLevel == PrivacyLevel.FOLLOWERS_ONLY) "This recipe is only visible to followers." else "This recipe is only visible to Savor Premium members.",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (recipe.privacyLevel == PrivacyLevel.PREMIUM_ONLY) {
                Button(onClick = { viewModel.openUpgradeModal() }, colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)) {
                    Text("Upgrade to Premium", color = Color.White)
                }
            } else {
                Button(onClick = { viewModel.toggleFollow(recipe.authorId) }, colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)) {
                    Text("Follow @${recipe.authorUsername}", color = Color.White)
                }
            }
        }
        return
    }

    val recipeComments = commentsMap[recipe.id] ?: emptyList()
    val recipeReviews = reviewsMap[recipe.id] ?: emptyList()

    // Allergen Conflict Check with user's personal allergens
    val allergenConflicts = remember(recipe, currentUser) {
        viewModel.getRecipeAllergenConflicts(recipe)
    }

    var commentText by remember { mutableStateOf("") }
    var reviewRating by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    var showReviewInput by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf<ReportReason>(ReportReason.ALLERGEN_MISLABEL) }
    var reportDetails by remember { mutableStateOf("") }
    val checkedSteps = remember { mutableStateMapOf<Int, Boolean>() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("recipe_detail_screen")
    ) {
        // --- 1. Top Bar Navigation ---
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                IconButton(onClick = { viewModel.navigateBack() }, modifier = Modifier.testTag("back_button")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Report Recipe
                    IconButton(onClick = { showReportDialog = true }, modifier = Modifier.testTag("report_recipe_button")) {
                        Icon(imageVector = Icons.Default.Flag, contentDescription = "Report Recipe", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // Add to Playlist / Collection
                    IconButton(onClick = { viewModel.openAddToCollection(recipe) }) {
                        Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = "Add to Playlist", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    // Version History
                    IconButton(onClick = { viewModel.openVersionHistory(recipe) }) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "Versions", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    // Unit Converter
                    IconButton(onClick = { viewModel.openUnitConverter() }) {
                        Icon(imageVector = Icons.Default.Scale, contentDescription = "Convert Units", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    // Share
                    IconButton(onClick = { viewModel.openShareMenu(recipe) }, modifier = Modifier.testTag("detail_share_button")) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }

        // --- 2. Media Player (Vertical Video Reel or Photo) ---
        item {
            if (recipe.mediaType == MediaType.VIDEO) {
                VideoReelPlayer(recipe = recipe, modifier = Modifier.padding(horizontal = 16.dp))
            } else {
                FoodVisualThumbnail(
                    recipe = recipe,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            // Video Studio Specs & Analytics (Views, Watch Time, Completion Rate, Retention)
            if (recipe.mediaType == MediaType.VIDEO) {
                val analytics = recipe.videoAnalytics ?: VideoAnalytics(
                    totalViews = 1240,
                    avgWatchSeconds = (recipe.videoDurationSeconds * 0.76f),
                    completionRatePercent = 78f,
                    retentionCurve = listOf(100f, 91f, 82f, 75f, 64f)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = SavorAccentOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Advanced Video Analytics",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            // Transcoding Quality Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SavorAccentOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = recipe.videoTranscodingQuality.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SavorAccentOrange,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (recipe.videoFilterApplied != VideoFilter.ORIGINAL) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Filter: ${recipe.videoFilterApplied.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${analytics.totalViews}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Views", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${analytics.avgWatchSeconds.toInt()}s / ${recipe.videoDurationSeconds}s", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Avg Watch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${analytics.completionRatePercent.toInt()}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
                                    Text("Completion", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Audience Retention Drop-off Graph
                        Text(
                            text = "Audience Retention Curve:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            analytics.retentionCurve.forEachIndexed { i, percent ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height((percent * 0.28f).dp)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(SavorAccentOrange.copy(alpha = 0.5f + (i * 0.08f)))
                                    )
                                    Text(
                                        text = "${percent.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Allergen Alert Banner (Proactive Dietary Warning) ---
        if (allergenConflicts.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SavorCrimson.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SavorCrimson.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("allergen_warning_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.WarningAmber, contentDescription = "Allergen Warning", tint = SavorCrimson, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Dietary Restriction Alert",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = SavorCrimson
                            )
                            Text(
                                text = "Contains ${allergenConflicts.joinToString(" & ") { it.label }} matching your allergy profile. Tap any ingredient below for smart plant-based substitutes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // --- 4. Header, Title, Co-Authors, Difficulty & Skill Badges ---
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                if (recipe.isReshared) {
                    OriginalPosterAttribution(
                        originalPosterUsername = recipe.originalPosterUsername!!,
                        isOriginalPosterVerified = recipe.originalPosterVerified,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Difficulty Badge & Technique Skills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(recipe.difficulty.colorHex).copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(recipe.difficulty.colorHex))
                    ) {
                        Text(
                            text = recipe.difficulty.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(recipe.difficulty.colorHex),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    recipe.skillTags.forEach { skill ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = skill.label,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Authors & Co-Authors Showcase
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.OtherProfile(recipe.authorId)) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SavorGoldenYellow)
                        ) {
                            Text(
                                text = recipe.authorUsername.take(2).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "@${recipe.authorUsername}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                if (recipe.isAuthorVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    VerificationBadge()
                                }
                            }
                            if (recipe.coAuthors.isNotEmpty()) {
                                Text(
                                    text = "With co-author @${recipe.coAuthors.first().username}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SavorAccentOrange
                                )
                            }
                        }
                    }

                    // Co-Author button
                    TextButton(onClick = { viewModel.openCoAuthorPicker(recipe) }) {
                        Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null, tint = SavorAccentOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Co-Authors", color = SavorAccentOrange, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ratings Score Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showReviewInput = !showReviewInput }
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = SavorGoldenYellow, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.ratingAverage} ★",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = " (${recipe.ratingCount} culinary reviews) • Tap to Rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Pills (Time, Servings, Likes)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                            Text(text = "Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${recipe.cookTimeMinutes}m", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                            Text(text = "Servings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${recipe.servings} people", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                            Text(text = "Likes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${recipe.likesCount}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // --- 5. Hands-Free Cooking Mode CTA Banner ---
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SavorAccentOrange.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SavorAccentOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { viewModel.navigateTo(Screen.CookingMode(recipe.id)) }
                    .testTag("start_cooking_mode_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(SavorAccentOrange)
                        ) {
                            Icon(imageVector = Icons.Default.SoupKitchen, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Start Cooking Mode", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SavorAccentOrange)
                            Text(text = "Step-by-step assistant with built-in timers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = SavorAccentOrange, modifier = Modifier.size(28.dp))
                }
            }
        }

        // --- 6. Meal Kit & Restaurant Partnership Showcase ---
        if (recipe.mealKitPartner != null) {
            val partner = recipe.mealKitPartner
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SavorGoldenYellow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("partner_mealkit_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(8.dp), color = SavorGoldenYellow) {
                                    Text(
                                        text = if (partner.type == PartnerType.MEAL_KIT) "MEAL KIT PARTNER" else "LOCAL RESTAURANT",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = partner.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Text(text = partner.price, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = SavorAccentOrange))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = partner.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = partner.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Code: ${partner.promoCode} (${partner.discountDescription})", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SavorAccentOrange)
                            Button(
                                onClick = { viewModel.showToast("Promo code '${partner.promoCode}' copied! Opening order...") },
                                colors = ButtonDefaults.buttonColors(containerColor = SavorGoldenYellow),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Order Now", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- 7. Nutritional Breakdown (Calories & Macros) ---
        if (recipe.nutrition != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("nutrition_breakdown_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = SavorAccentOrange)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Nutrition Per Serving", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Text(text = "${recipe.nutrition.calories} Calories", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = SavorAccentOrange))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            MacroBarItem(label = "Protein", grams = recipe.nutrition.protein, color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                            MacroBarItem(label = "Carbs", grams = recipe.nutrition.carbs, color = Color(0xFFFF9800), modifier = Modifier.weight(1f))
                            MacroBarItem(label = "Fat", grams = recipe.nutrition.fat, color = Color(0xFFE91E63), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // --- 8. Action Buttons (Grocery List, Meal Plan) ---
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Button(
                    onClick = { viewModel.addRecipeToShoppingList(recipe) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SavorGoldenYellow),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("add_to_shopping_list_button")
                ) {
                    Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add to Grocery", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.openAddToMealPlanDialog(recipe) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("add_to_meal_plan_button")
                ) {
                    Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Meal Plan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- 9. Ingredients List with Smart Substitution Suggestions ---
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ingredients (${recipe.ingredients.size})",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { viewModel.openUnitConverter() }) {
                        Icon(imageVector = Icons.Default.Scale, contentDescription = null, tint = SavorAccentOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unit Converter", color = SavorAccentOrange)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                recipe.ingredients.forEach { ingredient ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = ingredient.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                    if (ingredient.allergen != null) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color(ingredient.allergen.colorHex).copy(alpha = 0.2f)) {
                                            Text(text = ingredient.allergen.label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(ingredient.allergen.colorHex), modifier = Modifier.padding(horizontal = 4.dp))
                                        }
                                    }
                                }
                                Text(text = "${ingredient.amount} ${ingredient.unit}", style = MaterialTheme.typography.labelSmall, color = SavorAccentOrange)
                            }

                            // Swap / Substitution Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SavorGoldenYellow.copy(alpha = 0.2f),
                                modifier = Modifier.clickable { viewModel.openSubstitutions(ingredient) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = SavorAccentOrange, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(text = "Substitutes", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SavorAccentOrange)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 10. Step-by-Step Instructions & Cooking Mode Banner ---
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                // Interactive Cooking Mode Hero Banner
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SavorAccentOrange.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SavorAccentOrange.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SavorAccentOrange)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OutdoorGrill,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Interactive Cooking Mode",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Full-screen kitchen guide with step timers & awake screen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.navigateTo(Screen.CookingMode(recipe.id, 0)) },
                            colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("start_cooking_mode_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Step-by-Step Instructions",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${recipe.steps.size} Steps",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                recipe.steps.forEachIndexed { index, step ->
                    val isChecked = checkedSteps[index] ?: false
                    val stepMinutes = recipe.stepTimersMinutes.getOrNull(index) ?: run {
                        val m = Regex("""(\d+)\s*(?:minute|min|minutes|mins)""", RegexOption.IGNORE_CASE).find(step)
                        m?.groupValues?.get(1)?.toIntOrNull()
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { checkedSteps[index] = !isChecked }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(if (isChecked) SavorGoldenYellow else SavorAccentOrange)
                                ) {
                                    if (isChecked) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(text = "${index + 1}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )

                                    // Interactive Clickable Timer Chip
                                    if (stepMinutes != null && stepMinutes > 0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = SavorAccentOrange.copy(alpha = 0.12f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, SavorAccentOrange.copy(alpha = 0.35f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    viewModel.navigateTo(Screen.CookingMode(recipe.id, index))
                                                }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Timer,
                                                    contentDescription = "Start Timer",
                                                    tint = SavorAccentOrange,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "⏱️ $stepMinutes min timer • Tap to start",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = SavorAccentOrange
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 11. Ratings & Reviews Section ---
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Reviews & Ratings (${recipeReviews.size})", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    TextButton(onClick = { showReviewInput = !showReviewInput }) {
                        Text("+ Write Review", color = SavorAccentOrange)
                    }
                }

                // Interactive 5-Star Review Submission Input
                if (showReviewInput) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Select Star Rating:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                (1..5).forEach { star ->
                                    IconButton(onClick = { reviewRating = star }, modifier = Modifier.size(36.dp)) {
                                        Icon(
                                            imageVector = if (star <= reviewRating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "$star stars",
                                            tint = SavorGoldenYellow
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                label = { Text("Share taste tips or substitutions you made...") },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (reviewText.isNotBlank()) {
                                        viewModel.addRecipeReview(recipe.id, reviewRating, reviewText)
                                        reviewText = ""
                                        showReviewInput = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)
                            ) {
                                Text("Submit Review", color = Color.White)
                            }
                        }
                    }
                }

                recipeReviews.forEach { rev ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "@${rev.authorUsername}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    if (rev.isAuthorVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        VerificationBadge(modifier = Modifier.size(12.dp))
                                    }
                                }
                                Row {
                                    repeat(rev.rating) {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = SavorGoldenYellow, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = rev.comment, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // --- 12. Comments Section ---
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(text = "Chef Comments (${recipeComments.size})", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Leave a comment for this chef...") },
                        modifier = Modifier.weight(1f).testTag("detail_comment_input"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.addComment(recipe.id, commentText)
                                commentText = ""
                            }
                        },
                        modifier = Modifier.testTag("detail_post_comment_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Post", tint = SavorAccentOrange)
                    }
                }
            }
        }

        items(recipeComments, key = { it.id }) { comment ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(SavorGoldenYellow)
                    ) {
                        Text(text = comment.authorUsername.take(1).uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "@${comment.authorUsername}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            if (comment.isAuthorVerified) {
                                Spacer(modifier = Modifier.width(3.dp))
                                VerificationBadge(modifier = Modifier.size(12.dp))
                            }
                        }
                        Text(text = comment.text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Recipe to Trust & Safety") },
            text = {
                Column {
                    Text(
                        text = "Why are you reporting '${recipe.title}'?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ReportReason.values().forEach { reason ->
                        val isSelected = reportReason == reason
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reportReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { reportReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = SavorAccentOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = reason.label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportDetails,
                        onValueChange = { reportDetails = it },
                        label = { Text("Additional details (optional)") },
                        placeholder = { Text("Describe specific hazard or issue...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reportRecipe(recipe.id, reportReason, reportDetails)
                        showReportDialog = false
                        reportDetails = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Submit Report", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MacroBarItem(
    label: String,
    grams: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${grams}g",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
