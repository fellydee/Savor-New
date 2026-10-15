package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Story
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.components.FoodVisualThumbnail
import com.example.ui.components.OriginalPosterAttribution
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*

@Composable
fun StoryViewerScreen(
    initialStoryId: String,
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val stories by viewModel.stories.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var currentIndex by remember {
        mutableStateOf(stories.indexOfFirst { it.id == initialStoryId }.coerceAtLeast(0))
    }

    if (stories.isEmpty() || currentIndex !in stories.indices) {
        LaunchedEffect(Unit) { viewModel.navigateBack() }
        return
    }

    val currentStory = stories[currentIndex]
    val isMyStory = currentStory.authorId == currentUser.id

    // Progress Animation per story (5 seconds)
    val progress = remember { Animatable(0f) }

    LaunchedEffect(currentIndex) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
        )
        // Auto-advance
        if (currentIndex < stories.size - 1) {
            currentIndex++
        } else {
            viewModel.navigateBack()
        }
    }

    fun nextStory() {
        if (currentIndex < stories.size - 1) {
            currentIndex++
        } else {
            viewModel.navigateBack()
        }
    }

    fun prevStory() {
        if (currentIndex > 0) {
            currentIndex--
        } else {
            viewModel.navigateBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("story_viewer_screen")
    ) {
        // --- Story Visual Canvas ---
        val bgGradients = when (currentIndex % 3) {
            0 -> listOf(Color(0xFFE65100), Color(0xFFFFB300))
            1 -> listOf(Color(0xFF00695C), Color(0xFF4DB6AC))
            else -> listOf(Color(0xFF880E4F), Color(0xFFFF4081))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(bgGradients))
        ) {
            // Optional Reshared Recipe Card in Story
            if (currentStory.resharedRecipe != null) {
                val reshared = currentStory.resharedRecipe
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(24.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.navigateTo(Screen.RecipeDetail(reshared.id))
                            }
                            .testTag("story_reshared_recipe_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Original poster attribution
                            OriginalPosterAttribution(
                                originalPosterUsername = reshared.originalPosterUsername ?: reshared.authorUsername,
                                isOriginalPosterVerified = reshared.originalPosterVerified || reshared.isAuthorVerified,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            FoodVisualThumbnail(
                                recipe = reshared,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = reshared.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap to view full recipe & cook instructions 👨‍🍳",
                                style = MaterialTheme.typography.labelSmall,
                                color = SavorAccentOrange
                            )
                        }
                    }
                }
            } else {
                // Large text overlay
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(32.dp)
                ) {
                    Text(
                        text = currentStory.textOverlay,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // --- Tap Zones (Left 30% = prev, Right 70% = next) ---
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { prevStory() }
            )
            Box(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { nextStory() }
            )
        }

        // --- Story Header Overlays (Top safe area) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Segment Progress Bars at Top
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                stories.forEachIndexed { idx, _ ->
                    val segmentProgress = when {
                        idx < currentIndex -> 1f
                        idx == currentIndex -> progress.value
                        else -> 0f
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = segmentProgress)
                                .background(Color.White)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Author Info & Close Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SavorGoldenYellow)
                    ) {
                        Text(
                            text = currentStory.authorUsername.take(2).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentStory.authorUsername,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            if (currentStory.isAuthorVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerificationBadge(modifier = Modifier.size(13.dp))
                            }
                        }
                        Text(
                            text = "2h ago",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isMyStory) {
                        IconButton(
                            onClick = {
                                viewModel.deleteStory(currentStory.id)
                                viewModel.navigateBack()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("close_story_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }

        // --- Bottom Story Footer (Own Viewers Count or Reply) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp)
        ) {
            if (isMyStory) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "142 foodies viewed your story",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Send message to ${currentStory.authorUsername}...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = { viewModel.showToast("Sent a reaction to @${currentStory.authorUsername}!") }
                    ) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = "Love", tint = SavorCrimson, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }
}
