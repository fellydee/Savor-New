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

    var selectedTab by remember { mutableStateOf(0) } // 0: Global, 1: My City, 2: Americas, 3: Europe & Asia

    val allChefs = remember(users, currentUser) {
        (users + currentUser).distinctBy { it.id }.sortedByDescending { it.chefFlowers * 250 + it.followersCount }
    }

    val displayChefs = remember(allChefs, selectedTab, currentUser) {
        when (selectedTab) {
            0 -> allChefs // Global
            1 -> allChefs.filter { it.city.equals(currentUser.city, ignoreCase = true) }
            2 -> allChefs.filter { it.city in listOf("Austin, TX", "Brooklyn, NY", "Chicago, IL", "Portland, OR", "San Francisco, CA") }
            3 -> allChefs.filter { it.city in listOf("Bologna, Italy", "Seoul, South Korea", "Tokyo, Japan", "Paris, France") }
            else -> allChefs
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                                text = "Global Chef Leaderboard",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = SavorGoldenYellow)
                        }
                        Text(
                            text = "Top ranked culinary creators worldwide",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Region Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Global 🌍") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("${currentUser.city} 📍") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Americas 🗽") }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Europe & Asia 🏛️") }
                    )
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
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocalFlorist, contentDescription = null, tint = SavorAccentOrange, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Chef Flowers & Global Standing",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "1 Chef Flower = 250 Recipe Likes. Post authentic recipes & live streams to earn your verified Chef status!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (displayChefs.isEmpty()) {
                item {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(40.dp)) {
                        Text("No chefs found in this region yet.")
                    }
                }
            } else {
                itemsIndexed(displayChefs, key = { _, user -> user.id }) { index, user ->
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
                                if (isMe) {
                                    viewModel.navigateTo(Screen.Profile)
                                } else {
                                    viewModel.navigateTo(Screen.OtherProfile(user.id))
                                }
                            }
                            .testTag("leaderboard_row_$rank")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            // Rank Badge
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(rankColor)
                            ) {
                                Text(
                                    text = if (rank <= 3) "#$rank" else "$rank",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = if (rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Avatar
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SavorGoldenYellow)
                            ) {
                                Text(
                                    text = user.displayName.take(1),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Name & Title
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.displayName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (user.hasVerificationBadge) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        VerificationBadge(modifier = Modifier.size(13.dp))
                                    }
                                }
                                Text(
                                    text = "@${user.username} • ${user.city}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${user.followersCount} followers",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Flowers
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
}
