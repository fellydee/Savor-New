package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.RecipeCollection
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.components.FoodVisualThumbnail
import com.example.ui.theme.*

@Composable
fun CollectionsScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val collections by viewModel.collections.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("collections_screen")
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
                    text = "Recipe Playlists & Books",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("create_collection_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Playlist", tint = SavorAccentOrange)
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SavorGoldenYellow.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SavorGoldenYellow.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.QueueMusic, contentDescription = null, tint = SavorAccentOrange, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Curate Culinary Playlists", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(text = "Group themed dishes into cookbooks, share with friends, or follow masterchef playlists.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        items(collections, key = { it.id }) { col ->
            RecipeCollectionCard(
                collection = col,
                onClick = { viewModel.navigateTo(Screen.CollectionDetail(col.id)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showCreateDialog) {
        var colTitle by remember { mutableStateOf("") }
        var colDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Recipe Playlist", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = colTitle,
                        onValueChange = { colTitle = it },
                        label = { Text("Playlist Title (e.g. Summer Tapas)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = colDesc,
                        onValueChange = { colDesc = it },
                        label = { Text("Description") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (colTitle.isNotBlank()) {
                            viewModel.createCollection(colTitle.trim(), colDesc.trim())
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)
                ) {
                    Text("Create", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RecipeCollectionCard(
    collection: RecipeCollection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = when (collection.coverGradientId % 4) {
        0 -> listOf(Color(0xFFF25A19), Color(0xFFFFB800))
        1 -> listOf(Color(0xFF00897B), Color(0xFF4DB6AC))
        2 -> listOf(Color(0xFF6A1B9A), Color(0xFFAB47BC))
        else -> listOf(Color(0xFFC2185B), Color(0xFFFF8A80))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("collection_card_${collection.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Brush.horizontalGradient(gradientColors))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = collection.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "${collection.recipeIds.size} Recipes • Curated by @${collection.creatorUsername}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    ) {
                        Icon(imageVector = Icons.Default.CollectionsBookmark, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = collection.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${collection.followersCount} foodies following this playlist",
                        style = MaterialTheme.typography.labelSmall,
                        color = SavorAccentOrange
                    )
                    Text(
                        text = "Explore Recipes →",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SavorAccentOrange
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionDetailScreen(
    collectionId: String,
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val collections by viewModel.collections.collectAsState()
    val recipes by viewModel.recipes.collectAsState()

    val col = collections.find { it.id == collectionId }

    if (col == null) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("Collection not found.")
        }
        return
    }

    val colRecipes = recipes.filter { col.recipeIds.contains(it.id) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("collection_detail_screen")
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(text = col.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(text = "${colRecipes.size} recipes in playlist", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(text = col.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        items(colRecipes, key = { it.id }) { recipe ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { viewModel.navigateTo(Screen.RecipeDetail(recipe.id)) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    FoodVisualThumbnail(
                        recipe = recipe,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = recipe.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(text = "By @${recipe.authorUsername} • ${recipe.cookTimeMinutes}m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (recipe.nutrition != null) {
                            Text(text = "${recipe.nutrition.calories} kcal • ${recipe.nutrition.protein}g protein", style = MaterialTheme.typography.labelSmall, color = SavorAccentOrange)
                        }
                    }
                    IconButton(onClick = { viewModel.toggleRecipeInCollection(col.id, recipe.id) }) {
                        Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
