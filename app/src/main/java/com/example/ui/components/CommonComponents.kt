package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.theme.*

// --- Chef Flower Badge ---
@Composable
fun ChefFlowerBadge(
    flowerCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SavorGoldenYellow.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, SavorGoldenYellow.copy(alpha = 0.5f)),
        modifier = modifier.testTag("chef_flower_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFlorist,
                contentDescription = "Chef Flower",
                tint = SavorAccentOrange,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$flowerCount",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// --- Verification Badge ---
@Composable
fun VerificationBadge(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(17.dp)
            .clip(CircleShape)
            .background(VerifiedBadgeColor)
            .testTag("verification_badge")
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Verified Chef",
            tint = Color.White,
            modifier = Modifier.size(11.dp)
        )
    }
}

// --- Original Poster Attribution ---
@Composable
fun OriginalPosterAttribution(
    originalPosterUsername: String,
    isOriginalPosterVerified: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = modifier
            .testTag("original_poster_attribution")
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = "Reshared",
                tint = SavorAccentOrange,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Originally posted by ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "@$originalPosterUsername",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = SavorAccentOrange
            )
            if (isOriginalPosterVerified) {
                Spacer(modifier = Modifier.width(3.dp))
                VerificationBadge(modifier = Modifier.size(13.dp))
            }
        }
    }
}

// --- Dietary Tag Chip ---
@Composable
fun DietaryTagChip(
    tag: DietaryTag,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(tag.colorHex).copy(alpha = 0.16f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(tag.colorHex).copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Text(
            text = tag.label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color(tag.colorHex),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// --- Food Visual Thumbnail (Image or Colorful Culinary Vector Canvas) ---
@Composable
fun FoodVisualThumbnail(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val gradientColors = when (recipe.foodArtGradientId % 6) {
        1 -> listOf(Color(0xFFFF7043), Color(0xFFFFB74D)) // Salmon sunset
        2 -> listOf(Color(0xFF66BB6A), Color(0xFF26A69A)) // Matcha emerald
        3 -> listOf(Color(0xFF8D6E63), Color(0xFFFF7043)) // Smoky BBQ
        4 -> listOf(Color(0xFFFFCA28), Color(0xFFFFA726)) // Golden Turmeric
        5 -> listOf(Color(0xFFAB47BC), Color(0xFFEC407A)) // Berry Feta
        else -> listOf(Color(0xFF263238), Color(0xFF455A64)) // Wagyu dark
    }

    Box(
        modifier = modifier
            .background(Brush.linearGradient(gradientColors))
    ) {
        if (recipe.thumbnailCoverUrl.isNotBlank()) {
            AsyncImage(
                model = recipe.thumbnailCoverUrl,
                contentDescription = recipe.title,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant culinary vector representation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (recipe.mediaType == MediaType.VIDEO) Icons.Default.PlayCircleFilled else Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// --- Video Reel Player ---
@Composable
fun VideoReelPlayer(
    recipe: Recipe,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }

    // Simulated Reel playback progress
    val progressAnim = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (recipe.videoDurationSeconds.coerceAtLeast(15) * 1000),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { isPlaying = !isPlaying }
            .testTag("video_reel_player")
    ) {
        FoodVisualThumbnail(
            recipe = recipe,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradient scrim overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )

        // Reel Badge (Duration & Reel tag)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = "Reel",
                tint = SavorGoldenYellow,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "REEL 0:${recipe.videoDurationSeconds.coerceAtLeast(15)}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        // Mute/Unmute Toggle Button
        IconButton(
            onClick = { isMuted = !isMuted },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .testTag("reel_mute_button")
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        // Center Pause Indicator
        AnimatedVisibility(
            visible = !isPlaying,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Paused",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Bottom Progress Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progressAnim.value)
                    .background(SavorAccentOrange)
            )
        }
    }
}

// --- Savor Brand Logo ---
@Composable
fun SavorBrandLogo(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("savor_logo_header")
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_savor_logo),
            contentDescription = "Savor Logo",
            modifier = Modifier.size(34.dp)
        )
        Spacer(modifier = Modifier.width(9.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Savor",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.6).sp,
                    fontSize = 24.sp
                ),
                color = SavorAccentOrange
            )
            Spacer(modifier = Modifier.width(3.dp))
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(SavorGoldenYellow)
            )
        }
    }
}

// --- Savor Mobile Top Bar with Activity Badge ---
@Composable
fun SavorTopBar(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val convos by viewModel.conversations.collectAsState()

    val totalUnread = activities.count { !it.isRead } + convos.sumOf { it.unreadCount }

    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Savor Brand Logo + Wordmark
            SavorBrandLogo(
                onClick = { viewModel.navigateTo(Screen.Feed) },
                modifier = Modifier.testTag("savor_logo_header")
            )

            // Action Buttons (Dark mode & Activity / Quick-Access)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Theme Toggle
                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Messages Shortcut
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.Messages) },
                    modifier = Modifier.testTag("messages_shortcut_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Direct Messages",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Activity / Notifications Button with Unread Badge
                Box(
                    modifier = Modifier.testTag("activity_header_button")
                ) {
                    IconButton(
                        onClick = { viewModel.openQuickAccess() }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Activity Notifications",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (totalUnread > 0) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = 4.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(SavorCrimson)
                                .testTag("header_unread_badge")
                        ) {
                            Text(
                                text = if (totalUnread > 9) "9+" else "$totalUnread",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Savor Bottom Navigation Bar ---
@Composable
fun SavorBottomNavigation(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = currentScreen is Screen.Feed,
            onClick = { viewModel.navigateTo(Screen.Feed) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Feed) Icons.Default.Home else Icons.Outlined.Home,
                    contentDescription = "Feed"
                )
            },
            label = { Text("Feed") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SavorAccentOrange,
                indicatorColor = SavorAccentOrange.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Explore,
            onClick = { viewModel.navigateTo(Screen.Explore) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Explore) Icons.Default.Search else Icons.Outlined.Search,
                    contentDescription = "Explore"
                )
            },
            label = { Text("Explore") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SavorAccentOrange,
                indicatorColor = SavorAccentOrange.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Create,
            onClick = { viewModel.navigateTo(Screen.Create) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Create Recipe",
                    tint = SavorAccentOrange,
                    modifier = Modifier.size(28.dp)
                )
            },
            label = { Text("Create") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SavorAccentOrange,
                indicatorColor = SavorAccentOrange.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentScreen is Screen.MealPlan,
            onClick = { viewModel.navigateTo(Screen.MealPlan) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.MealPlan) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = "Meal Plan"
                )
            },
            label = { Text("Meal Plan") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SavorAccentOrange,
                indicatorColor = SavorAccentOrange.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Profile,
            onClick = { viewModel.navigateTo(Screen.Profile) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Profile) Icons.Default.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SavorAccentOrange,
                indicatorColor = SavorAccentOrange.copy(alpha = 0.15f)
            )
        )
    }
}

// --- Quick-Access Bottom Sheet ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAccessBottomSheet(
    viewModel: SavorViewModel
) {
    val activities by viewModel.activities.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeQuickAccess() },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("quick_access_bottom_sheet")
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
                Text(
                    text = "Quick Access",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { viewModel.closeQuickAccess() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section 1: Recent Notifications
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = {
                        viewModel.closeQuickAccess()
                        viewModel.navigateTo(Screen.Activity)
                    }
                ) {
                    Text("View All", color = SavorAccentOrange)
                }
            }

            val recentActivities = activities.take(3)
            if (recentActivities.isEmpty()) {
                Text(
                    text = "No recent notifications yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                recentActivities.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (!item.isRead) SavorAccentOrange.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.closeQuickAccess()
                                viewModel.navigateTo(Screen.Activity)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SavorAccentOrange.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = when (item.type) {
                                        ActivityType.LIKE -> Icons.Default.Favorite
                                        ActivityType.COMMENT -> Icons.Default.Comment
                                        ActivityType.FOLLOW -> Icons.Default.PersonAdd
                                        ActivityType.SHARE -> Icons.Default.Repeat
                                        ActivityType.COLLABORATION -> Icons.Default.People
                                        ActivityType.CHEF_FLOWER_MILESTONE -> Icons.Default.LocalFlorist
                                        ActivityType.VERIFICATION_UPDATE -> Icons.Default.Verified
                                        ActivityType.REVIEW -> Icons.Default.Star
                                    },
                                    contentDescription = null,
                                    tint = SavorAccentOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.actorUsername} ${item.message}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.Normal),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (!item.isRead) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SavorAccentOrange)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Unread Direct Messages
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Direct Messages",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = {
                        viewModel.closeQuickAccess()
                        viewModel.navigateTo(Screen.Messages)
                    }
                ) {
                    Text("Go to Inbox", color = SavorAccentOrange)
                }
            }

            val unreadConvos = conversations.filter { it.unreadCount > 0 }
            if (unreadConvos.isEmpty()) {
                Text(
                    text = "No unread messages. Catch up with fellow chefs!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                unreadConvos.forEach { convo ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.closeQuickAccess()
                                viewModel.navigateTo(Screen.Chat(convo.id))
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SavorGoldenYellow)
                            ) {
                                Text(
                                    text = convo.otherUser.displayName.take(1),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = convo.otherUser.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = convo.lastMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(SavorAccentOrange)
                            ) {
                                Text(
                                    text = "${convo.unreadCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- Upgrade Prompt Modal ---
@Composable
fun UpgradePromptModal(
    viewModel: SavorViewModel
) {
    AlertDialog(
        onDismissRequest = { viewModel.closeUpgradeModal() },
        modifier = Modifier.testTag("upgrade_prompt_modal"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = SavorGoldenYellow,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Savor Premium Feature",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Unlock the full culinary studio experience with Savor Premium:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                val benefits = listOf(
                    "📅 Weekly Meal Planning Calendar & Templates",
                    "⚡ Auto-calculated Calories & Macros per Serving",
                    "🤖 1-Tap Meal Plan Auto-Generation from Top Liked Recipes",
                    "⭐ Instant Verification Badge Eligibility",
                    "🔒 Access Exclusive Premium Chef Recipes"
                )
                benefits.forEach { benefit ->
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.closeUpgradeModal()
                    viewModel.navigateTo(Screen.Checkout)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                modifier = Modifier.testTag("modal_go_premium_button")
            ) {
                Text("Go Premium", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeUpgradeModal() }) {
                Text("Maybe Later")
            }
        }
    )
}

// --- Share Modal Sheet ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareModalSheet(
    recipe: Recipe,
    viewModel: SavorViewModel
) {
    val context = LocalContext.current
    val conversations by viewModel.conversations.collectAsState()

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeShareMenu() },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("share_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Share Recipe",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Option 1: Share to Stories
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.shareToStory(recipe) }
                    .testTag("share_to_story_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = "Share to Story",
                        tint = SavorAccentOrange
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Share to Your Story",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Embeds recipe card with original poster attribution",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Option 2: Send as Message in DM
            Text(
                text = "Send via Direct Message",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )

            conversations.forEach { convo ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { viewModel.sendRecipeInDM(convo.id, recipe) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SavorGoldenYellow)
                        ) {
                            Text(
                                text = convo.otherUser.displayName.take(1),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Send to ${convo.otherUser.displayName}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }

            // Option 3: Share Externally (Native Android Share Sheet)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out this delicious recipe on Savor: '${recipe.title}' by @${recipe.originalPosterUsername ?: recipe.authorUsername}! https://savor.app/recipe/${recipe.id}"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Recipe via"))
                        viewModel.closeShareMenu()
                    }
                    .testTag("share_external_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Externally",
                        tint = SavorGoldenYellow
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Share Externally",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "WhatsApp, Messages, Telegram, or Copy Link",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- Add to Meal Plan Dialog ---
@Composable
fun AddToMealPlanDialog(
    recipe: Recipe,
    viewModel: SavorViewModel
) {
    var selectedDay by remember { mutableStateOf(0) }
    var selectedSlot by remember { mutableStateOf(MealSlot.DINNER) }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    AlertDialog(
        onDismissRequest = { viewModel.closeAddToMealPlanDialog() },
        modifier = Modifier.testTag("add_to_meal_plan_dialog"),
        title = {
            Text(
                text = "Add to Meal Plan",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = SavorAccentOrange
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select Day of Week:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    days.forEachIndexed { index, day ->
                        val isSelected = selectedDay == index
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SavorAccentOrange else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedDay = index }
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Select Meal Slot:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                MealSlot.values().forEach { slot ->
                    val isSelected = selectedSlot == slot
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) SavorGoldenYellow.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, SavorGoldenYellow) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { selectedSlot = slot }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedSlot = slot },
                                colors = RadioButtonDefaults.colors(selectedColor = SavorAccentOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = slot.label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.assignRecipeToMealSlot(selectedDay, selectedSlot, recipe)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                modifier = Modifier.testTag("confirm_assign_meal_slot_button")
            ) {
                Text("Add to Plan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeAddToMealPlanDialog() }) {
                Text("Cancel")
            }
        }
    )
}
