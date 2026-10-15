package com.example.ui.screens

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
fun FeedScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    val recipes by viewModel.recipes.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.users.collectAsState()

    // Filter recipes by privacy permissions
    val visibleRecipes = remember(recipes, currentUser, users) {
        recipes.filter { viewModel.repository.canUserViewRecipe(it) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("feed_screen")
    ) {
        // --- 1. Orange-to-Gold Gradient Hero Banner ---
        item {
            FeedHeroBanner(currentUser = currentUser)
        }

        // --- 2. Stories Bar ---
        item {
            StoriesBar(
                stories = stories,
                currentUser = currentUser,
                onStoryClick = { storyId -> viewModel.navigateTo(Screen.StoryViewer(storyId)) },
                onCreateStoryClick = {
                    viewModel.postStory("", "Cooking vibes today! 🍳")
                }
            )
        }

        // --- 3. Recipe Posts ---
        items(visibleRecipes, key = { it.id }) { recipe ->
            FeedRecipeCard(
                recipe = recipe,
                viewModel = viewModel
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- Gradient Hero Banner ---
@Composable
fun FeedHeroBanner(
    currentUser: User,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFF25A19), // hsl(22 93% 55%) Accent Orange
                        Color(0xFFFFB800)  // hsl(43 100% 50%) Golden Yellow
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
            .testTag("feed_hero_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SAVOR SOCIAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    ChefFlowerBadge(flowerCount = currentUser.chefFlowers)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Welcome back, ${currentUser.displayName.substringBefore(" ")}!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
                Text(
                    text = "Discover fresh culinary inspiration & trending reels in ${currentUser.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
            ) {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

// --- Stories Bar ---
@Composable
fun StoriesBar(
    stories: List<Story>,
    currentUser: User,
    onStoryClick: (String) -> Unit,
    onCreateStoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .testTag("stories_bar")
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // "Your Story" (+) Bubble
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onCreateStoryClick() }
                        .testTag("add_story_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, SavorAccentOrange.copy(alpha = 0.4f), CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = SavorAccentOrange,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your Story",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }

            // Active Story Avatars
            items(stories, key = { it.id }) { story ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onStoryClick(story.id) }
                        .testTag("story_avatar_${story.id}")
                ) {
                    // Instagram-Style Gradient Story Ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(SavorStoryRingGradient)
                            .padding(2.5.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(2.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(SavorGoldenYellow)
                            ) {
                                Text(
                                    text = story.authorUsername.take(2).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = story.authorUsername,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 66.dp)
                        )
                        if (story.isAuthorVerified) {
                            Spacer(modifier = Modifier.width(2.dp))
                            VerificationBadge(modifier = Modifier.size(11.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- Feed Recipe Card ---
@Composable
fun FeedRecipeCard(
    recipe: Recipe,
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    val heartColor by animateColorAsState(
        targetValue = if (recipe.isLikedByMe) SavorCrimson else MaterialTheme.colorScheme.onSurfaceVariant
    )

    var showCommentField by remember { mutableStateOf(false) }
    var commentInput by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .testTag("recipe_card_${recipe.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Header: Author info & options
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        viewModel.navigateTo(Screen.OtherProfile(recipe.authorId))
                    }
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
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = recipe.authorUsername,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (recipe.isAuthorVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerificationBadge()
                            }
                        }
                        Text(
                            text = "${recipe.cookTimeMinutes} mins • ${recipe.servings} servings",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Privacy Indicator Chip
                if (recipe.privacyLevel != PrivacyLevel.PUBLIC) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SavorAccentOrange.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = recipe.privacyLevel.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SavorAccentOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Original Poster Attribution (Reshare banner)
            if (recipe.isReshared) {
                OriginalPosterAttribution(
                    originalPosterUsername = recipe.originalPosterUsername!!,
                    isOriginalPosterVerified = recipe.originalPosterVerified,
                    onClick = {
                        // Could navigate to original author
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            // Media (Video Reel or Photo)
            if (recipe.mediaType == MediaType.VIDEO) {
                VideoReelPlayer(
                    recipe = recipe,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .clickable { viewModel.navigateTo(Screen.RecipeDetail(recipe.id)) }
                )
            } else {
                FoodVisualThumbnail(
                    recipe = recipe,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.navigateTo(Screen.RecipeDetail(recipe.id)) }
                )
            }

            // Dietary Tags
            if (recipe.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    recipe.tags.forEach { tag ->
                        DietaryTagChip(tag = tag)
                    }
                }
            }

            // Actions Bar (Like, Comment, Share, Save)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Like Button
                    IconButton(
                        onClick = { viewModel.toggleLike(recipe.id) },
                        modifier = Modifier.testTag("like_button_${recipe.id}")
                    ) {
                        Icon(
                            imageVector = if (recipe.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = heartColor
                        )
                    }
                    Text(
                        text = "${recipe.likesCount}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Comment Button
                    IconButton(
                        onClick = { showCommentField = !showCommentField },
                        modifier = Modifier.testTag("comment_button_${recipe.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ModeComment,
                            contentDescription = "Comment",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${recipe.commentsCount}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Share Button (Story, DM, External)
                    IconButton(
                        onClick = { viewModel.openShareMenu(recipe) },
                        modifier = Modifier.testTag("share_button_${recipe.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Bookmark / Save Button
                IconButton(
                    onClick = { viewModel.toggleSave(recipe.id) },
                    modifier = Modifier.testTag("save_button_${recipe.id}")
                ) {
                    Icon(
                        imageVector = if (recipe.isSavedByMe) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (recipe.isSavedByMe) SavorAccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Title & Description
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        viewModel.navigateTo(Screen.RecipeDetail(recipe.id))
                    }
                )
                if (recipe.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Nutritional Macro Pills (if available)
            if (recipe.nutrition != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${recipe.nutrition.calories} kcal",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SavorAccentOrange
                    )
                    Text(
                        text = "• ${recipe.nutrition.protein}g protein",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• ${recipe.nutrition.carbs}g carbs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• ${recipe.nutrition.fat}g fat",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Inline Comment Expansion
            if (showCommentField) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = { Text("Add a comment for the chef...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("inline_comment_input"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentInput.isNotBlank()) {
                                viewModel.addComment(recipe.id, commentInput)
                                commentInput = ""
                                showCommentField = false
                            }
                        },
                        modifier = Modifier.testTag("submit_comment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Post",
                            tint = SavorAccentOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
