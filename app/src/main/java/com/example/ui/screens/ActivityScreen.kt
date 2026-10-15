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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ActivityItem
import com.example.data.model.ActivityType
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*

@Composable
fun ActivityScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val activities by viewModel.activities.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.markActivitiesAsRead()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("activity_screen")
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
                    text = "Activity & Notifications",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                TextButton(onClick = { viewModel.markActivitiesAsRead() }) {
                    Text("Mark Read", color = SavorAccentOrange)
                }
            }
        }

        if (activities.isEmpty()) {
            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(40.dp)) {
                    Text("No activity notifications yet.")
                }
            }
        } else {
            items(activities, key = { it.id }) { item ->
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
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ActivityNotificationRow(
    item: ActivityItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (!item.isRead) SavorAccentOrange.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("activity_item_${item.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Icon Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        when (item.type) {
                            ActivityType.LIKE -> SavorCrimson.copy(alpha = 0.15f)
                            ActivityType.COMMENT -> SavorAccentOrange.copy(alpha = 0.15f)
                            ActivityType.FOLLOW -> SavorGoldenYellow.copy(alpha = 0.2f)
                            ActivityType.SHARE -> SavorAccentOrange.copy(alpha = 0.2f)
                            ActivityType.COLLABORATION -> SavorAccentOrange.copy(alpha = 0.2f)
                            ActivityType.CHEF_FLOWER_MILESTONE -> SavorGoldenYellow.copy(alpha = 0.25f)
                            ActivityType.VERIFICATION_UPDATE -> VerifiedBadgeColor.copy(alpha = 0.2f)
                            ActivityType.REVIEW -> SavorGoldenYellow.copy(alpha = 0.2f)
                        }
                    )
            ) {
                Icon(
                    imageVector = when (item.type) {
                        ActivityType.LIKE -> Icons.Default.Favorite
                        ActivityType.COMMENT -> Icons.Default.ModeComment
                        ActivityType.FOLLOW -> Icons.Default.PersonAdd
                        ActivityType.SHARE -> Icons.Default.Repeat
                        ActivityType.COLLABORATION -> Icons.Default.People
                        ActivityType.CHEF_FLOWER_MILESTONE -> Icons.Default.LocalFlorist
                        ActivityType.VERIFICATION_UPDATE -> Icons.Default.Verified
                        ActivityType.REVIEW -> Icons.Default.Star
                    },
                    contentDescription = null,
                    tint = when (item.type) {
                        ActivityType.LIKE -> SavorCrimson
                        ActivityType.COMMENT -> SavorAccentOrange
                        ActivityType.FOLLOW -> SavorAccentOrange
                        ActivityType.SHARE -> SavorAccentOrange
                        ActivityType.COLLABORATION -> SavorAccentOrange
                        ActivityType.CHEF_FLOWER_MILESTONE -> SavorGoldenYellow
                        ActivityType.VERIFICATION_UPDATE -> VerifiedBadgeColor
                        ActivityType.REVIEW -> SavorGoldenYellow
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.actorUsername,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (item.isActorVerified) {
                        Spacer(modifier = Modifier.width(3.dp))
                        VerificationBadge(modifier = Modifier.size(12.dp))
                    }
                }

                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (item.originalPosterUsername != null) {
                    Text(
                        text = "Originally posted by @${item.originalPosterUsername}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SavorAccentOrange
                    )
                }
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
