package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ActivityItem
import com.example.data.model.ActivityType
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.theme.*

@Composable
fun NotificationCenterScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val activities by viewModel.activities.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var onlyUnread by remember { mutableStateOf(false) }

    val categories = listOf("All", "Likes & Flowers", "Comments", "Collaborations", "Reviews", "System")

    val filteredActivities = remember(activities, selectedCategory, onlyUnread) {
        activities.filter { item ->
            val matchCat = when (selectedCategory) {
                "Likes & Flowers" -> item.type == ActivityType.LIKE || item.type == ActivityType.CHEF_FLOWER_MILESTONE
                "Comments" -> item.type == ActivityType.COMMENT
                "Collaborations" -> item.type == ActivityType.COLLABORATION || item.type == ActivityType.SHARE
                "Reviews" -> item.type == ActivityType.REVIEW
                "System" -> item.type == ActivityType.VERIFICATION_UPDATE
                else -> true
            }
            val matchUnread = if (onlyUnread) !item.isRead else true
            matchCat && matchUnread
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            ) {
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
                        text = "Notification Center",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    TextButton(onClick = { viewModel.markActivitiesAsRead() }) {
                        Text("Mark Read", color = SavorAccentOrange)
                    }
                }
            }
        },
        modifier = modifier.testTag("notification_center_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Filter Pills
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SavorAccentOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Unread Only Toggle
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${filteredActivities.size} alerts found",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Unread only",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = onlyUnread,
                            onCheckedChange = { onlyUnread = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SavorAccentOrange,
                                checkedTrackColor = SavorAccentOrange.copy(alpha = 0.35f)
                            )
                        )
                    }
                }
            }

            if (filteredActivities.isEmpty()) {
                item {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(40.dp)) {
                        Text("No notifications matching this filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filteredActivities, key = { it.id }) { item ->
                    ActivityNotificationRow(
                        item = item,
                        onClick = {
                            if (item.recipeId != null) {
                                viewModel.navigateTo(Screen.RecipeDetail(item.recipeId))
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
