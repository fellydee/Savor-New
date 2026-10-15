package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ExploreScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<DietaryTag?>(null) }

    val recipes by viewModel.recipes.collectAsState()
    val users by viewModel.users.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Filter permitted recipes
    val permittedRecipes = remember(recipes, currentUser, users) {
        recipes.filter { viewModel.repository.canUserViewRecipe(it) }
    }

    // Trending this week: sorted by likesCount descending
    val trendingRecipes = remember(permittedRecipes) {
        permittedRecipes.sortedByDescending { it.likesCount }.take(5)
    }

    // Filter by search query & dietary tag
    val filteredRecipes = remember(permittedRecipes, searchQuery, selectedTag) {
        permittedRecipes.filter { recipe ->
            val matchesQuery = searchQuery.isBlank() ||
                recipe.title.contains(searchQuery, ignoreCase = true) ||
                recipe.description.contains(searchQuery, ignoreCase = true) ||
                recipe.ingredients.any { it.name.contains(searchQuery, ignoreCase = true) } ||
                recipe.tags.any { it.label.contains(searchQuery, ignoreCase = true) }

            val matchesTag = selectedTag == null || recipe.tags.contains(selectedTag)

            matchesQuery && matchesTag
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("explore_screen")
    ) {
        // --- 1. Gradient-Styled Search Bar ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SavorGradientBrush)
                        .padding(2.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Search recipes, ingredients, or tags...", fontSize = 14.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = SavorAccentOrange
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = SavorAccentOrange
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("explore_search_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Culinary Tools Shortcuts
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SavorGoldenYellow.copy(alpha = 0.18f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(Screen.Collections) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CollectionsBookmark, contentDescription = null, tint = SavorAccentOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Playlists", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SavorAccentOrange.copy(alpha = 0.15f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.navigateTo(Screen.NutritionalDatabase) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = SavorAccentOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Nutrition DB", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.openUnitConverter() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Scale, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Converter", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // --- 2. Dietary Tag Filter Chips ---
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedTag == null,
                        onClick = { selectedTag = null },
                        label = { Text("All Recipes") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SavorAccentOrange,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_all")
                    )
                }
                items(DietaryTag.values()) { tag ->
                    val isSelected = selectedTag == tag
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTag = if (isSelected) null else tag },
                        label = { Text(tag.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(tag.colorHex),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_${tag.name}")
                    )
                }
            }
        }

        // --- 3. "Trending This Week" Section ---
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Trending",
                            tint = SavorAccentOrange,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trending This Week",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Most-Liked",
                        style = MaterialTheme.typography.labelSmall,
                        color = SavorAccentOrange
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(trendingRecipes, key = { "trend_${it.id}" }) { recipe ->
                        TrendingRecipeItem(
                            recipe = recipe,
                            onClick = { viewModel.navigateTo(Screen.RecipeDetail(recipe.id)) }
                        )
                    }
                }
            }
        }

        // --- 4. People Discovery Section ---
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Chefs",
                            tint = SavorGoldenYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chefs to Discover",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(onClick = { viewModel.navigateTo(Screen.Leaderboard) }) {
                        Text("City Rankings", color = SavorAccentOrange, style = MaterialTheme.typography.labelMedium)
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users.filter { it.id != currentUser.id }, key = { "user_${it.id}" }) { user ->
                        ChefDiscoveryCard(
                            user = user,
                            onProfileClick = { viewModel.navigateTo(Screen.OtherProfile(user.id)) },
                            onToggleFollow = { viewModel.toggleFollow(user.id) }
                        )
                    }
                }
            }
        }

        // --- 5. Recipe Grid Header ---
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = null,
                    tint = SavorAccentOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (searchQuery.isNotBlank() || selectedTag != null) "Search Results (${filteredRecipes.size})" else "Discover Recipes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // --- 6. 2-Column Recipe Grid Items ---
        items(filteredRecipes.chunked(2)) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { recipe ->
                    Box(modifier = Modifier.weight(1f)) {
                        ExploreGridCard(
                            recipe = recipe,
                            onClick = { viewModel.navigateTo(Screen.RecipeDetail(recipe.id)) }
                        )
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- Trending Recipe Carousel Item ---
@Composable
fun TrendingRecipeItem(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .width(220.dp)
            .clickable { onClick() }
            .testTag("trending_item_${recipe.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                FoodVisualThumbnail(recipe = recipe, modifier = Modifier.fillMaxSize())

                // Reel Badge if video
                if (recipe.mediaType == MediaType.VIDEO) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "REEL 0:${recipe.videoDurationSeconds}s",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Likes badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Likes",
                        tint = SavorCrimson,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.likesCount}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${recipe.authorUsername}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- Chef Discovery Card ---
@Composable
fun ChefDiscoveryCard(
    user: User,
    onProfileClick: () -> Unit,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(160.dp)
            .testTag("chef_card_${user.id}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(SavorStoryRingGradient)
                    .padding(2.dp)
                    .clickable { onProfileClick() }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(SavorGoldenYellow)
                ) {
                    Text(
                        text = user.displayName.take(1),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.displayName.substringBefore(" "),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user.hasVerificationBadge) {
                    Spacer(modifier = Modifier.width(3.dp))
                    VerificationBadge(modifier = Modifier.size(13.dp))
                }
            }

            Text(
                text = user.city,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))
            ChefFlowerBadge(flowerCount = user.chefFlowers)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onToggleFollow,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (user.isFollowing) MaterialTheme.colorScheme.surfaceVariant else SavorAccentOrange
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .testTag("follow_button_${user.id}")
            ) {
                Text(
                    text = if (user.isFollowing) "Following" else "Follow",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (user.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                )
            }
        }
    }
}

// --- 2-Column Grid Card ---
@Composable
fun ExploreGridCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("grid_card_${recipe.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                FoodVisualThumbnail(recipe = recipe, modifier = Modifier.fillMaxSize())

                if (recipe.mediaType == MediaType.VIDEO) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                if (recipe.isReshared) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = SavorAccentOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "@${recipe.originalPosterUsername}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SavorAccentOrange,
                            maxLines = 1
                        )
                    }
                }

                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${recipe.cookTimeMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = SavorCrimson,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${recipe.likesCount}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
