package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.components.ChefFlowerBadge
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*

@Composable
fun LeaderboardScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.users.collectAsState()

    // Filter chefs from the same city as currentUser and sort by chef flowers descending
    val cityChefs = remember(users, currentUser) {
        val allCityUsers = (users + currentUser)
            .distinctBy { it.id }
            .filter { it.city.equals(currentUser.city, ignoreCase = true) }
            .sortedByDescending { it.chefFlowers }

        allCityUsers
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("leaderboard_screen")
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "City Chef Leaderboard",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = SavorGoldenYellow)
                    }
                    Text(
                        text = "Ranked by Chef Flowers in ${currentUser.city}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Explanation Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SavorGoldenYellow.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SavorGoldenYellow.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocalFlorist, contentDescription = null, tint = SavorAccentOrange, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "1 Chef Flower = 250 Likes Received",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Keep posting reels & recipes to grow your culinary flower garden and climb the city podium!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (cityChefs.isEmpty()) {
            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(40.dp)) {
                    Text("No other chefs from your city yet. Be the first to share a recipe!")
                }
            }
        } else {
            itemsIndexed(cityChefs, key = { _, user -> user.id }) { index, user ->
                val isMe = user.id == currentUser.id
                val rank = index + 1

                val rankColor = when (rank) {
                    1 -> Color(0xFFFFD700) // Gold
                    2 -> Color(0xFFC0C0C0) // Silver
                    3 -> Color(0xFFCD7F32) // Bronze
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isMe) SavorAccentOrange.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
                    border = if (isMe) androidx.compose.foundation.BorderStroke(2.dp, SavorAccentOrange) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            if (!isMe) viewModel.navigateTo(Screen.OtherProfile(user.id))
                        }
                        .testTag("leaderboard_row_${user.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        // Rank Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(rankColor)
                        ) {
                            Text(
                                text = "#$rank",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Avatar
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SavorGoldenYellow)
                        ) {
                            Text(
                                text = user.displayName.take(1),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Username & City
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isMe) "${user.displayName} (You)" else user.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isMe) SavorAccentOrange else MaterialTheme.colorScheme.onSurface
                                )
                                if (user.hasVerificationBadge) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    VerificationBadge(modifier = Modifier.size(13.dp))
                                }
                            }
                            Text(
                                text = "${user.totalLikesReceived} likes received across all recipes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Chef Flowers Badge
                        ChefFlowerBadge(flowerCount = user.chefFlowers)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
