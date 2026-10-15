package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val currentUser by viewModel.currentUser.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val pushSettings by viewModel.pushSettings.collectAsState()

    var showCancelConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen")
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Settings & Subscription",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // --- Section 1: Subscription Tier Management ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (currentUser.isPremium) "Savor Premium Active" else "Free Foodie Plan",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (currentUser.isPremium) SavorAccentOrange else MaterialTheme.colorScheme.onSurface
                                )
                                if (currentUser.isPremium) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    VerificationBadge()
                                }
                            }
                            Text(
                                text = if (currentUser.isPremium)
                                    "Renews automatically on Nov 15, 2026"
                                else
                                    "Upgrade to unlock full meal calendar & macro tracking",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (currentUser.isPremium) {
                        OutlinedButton(
                            onClick = { showCancelConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SavorCrimson),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cancel_premium_button")
                        ) {
                            Text("Cancel Premium Subscription")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.navigateTo(Screen.Checkout) },
                            colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_upgrade_button")
                        ) {
                            Text("Upgrade to Premium ($9.99/mo)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Section 2: Appearance & Theme ---
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = SavorAccentOrange
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Dark Theme", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = if (isDark) "Active: Midnight charcoal & warm cream" else "Active: Sunlit cream & orange",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SavorAccentOrange,
                            checkedTrackColor = SavorAccentOrange.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        // --- Section 2.5: Personal Allergen & Dietary Alert Settings ---
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Personal Allergen & Dietary Alerts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Select your allergies. Savor will warn you automatically whenever a recipe contains them.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    for (allergen in Allergen.entries) {
                        val isAllergic = currentUser.personalAllergens.contains(allergen)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = allergen.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = allergen.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                            Switch(
                                checked = isAllergic,
                                onCheckedChange = {
                                    val updated = if (isAllergic) {
                                        currentUser.personalAllergens - allergen
                                    } else {
                                        currentUser.personalAllergens + allergen
                                    }
                                    viewModel.updateUserAllergens(updated)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SavorCrimson,
                                    checkedTrackColor = SavorCrimson.copy(alpha = 0.35f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- Section 2.8: Culinary Studio Tools ---
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Culinary Studio Tools",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    ListItem(
                        headlineContent = { Text("Nutritional Database & Custom Ingredients", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("View, add and edit calories & macros for custom ingredients") },
                        leadingContent = { Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = SavorAccentOrange) },
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.NutritionalDatabase) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ListItem(
                        headlineContent = { Text("Recipe Playlists & Collections", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Organize themed cookbooks and shareable recipe playlists") },
                        leadingContent = { Icon(imageVector = Icons.Default.CollectionsBookmark, contentDescription = null, tint = SavorGoldenYellow) },
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.Collections) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ListItem(
                        headlineContent = { Text("In-App Notification Center", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Categorized alerts for likes, flowers, reviews and collaborations") },
                        leadingContent = { Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = SavorAccentOrange) },
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.NotificationCenter) }
                    )
                }
            }
        }

        // --- Section 3: Push Notification Preferences ---
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Push Notification Preferences",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    NotificationToggleRow(
                        title = "Likes on Recipes",
                        subtitle = "When fellow foodies like your reels & dishes",
                        isChecked = pushSettings[ActivityType.LIKE] ?: true,
                        onToggle = { viewModel.togglePushSetting(ActivityType.LIKE) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    NotificationToggleRow(
                        title = "Comments & Feedback",
                        subtitle = "When chefs comment on your culinary creations",
                        isChecked = pushSettings[ActivityType.COMMENT] ?: true,
                        onToggle = { viewModel.togglePushSetting(ActivityType.COMMENT) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    NotificationToggleRow(
                        title = "New Followers",
                        subtitle = "When a new foodie joins your culinary circle",
                        isChecked = pushSettings[ActivityType.FOLLOW] ?: true,
                        onToggle = { viewModel.togglePushSetting(ActivityType.FOLLOW) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    NotificationToggleRow(
                        title = "Reshares of Your Recipes",
                        subtitle = "When someone reshares your post with attribution",
                        isChecked = pushSettings[ActivityType.SHARE] ?: true,
                        onToggle = { viewModel.togglePushSetting(ActivityType.SHARE) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    NotificationToggleRow(
                        title = "Chef Flower Milestones",
                        subtitle = "Celebrations every 250 likes received across your recipes",
                        isChecked = pushSettings[ActivityType.CHEF_FLOWER_MILESTONE] ?: true,
                        onToggle = { viewModel.togglePushSetting(ActivityType.CHEF_FLOWER_MILESTONE) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    NotificationToggleRow(
                        title = "Verification Status Updates",
                        subtitle = "When you qualify for the official Verified Chef badge",
                        isChecked = pushSettings[ActivityType.VERIFICATION_UPDATE] ?: true,
                        onToggle = { viewModel.togglePushSetting(ActivityType.VERIFICATION_UPDATE) }
                    )
                }
            }
        }

        // --- Section 4: Account Actions ---
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Logged in as ${currentUser.displayName} (@${currentUser.username})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Email: ${currentUser.email} • City: ${currentUser.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { viewModel.showToast("Logged out of session.") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Out")
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text("Cancel Premium Subscription?") },
            text = {
                Text("Your premium benefits and verification status will remain active until the end of your current billing period.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelPremium()
                        showCancelConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SavorCrimson)
                ) {
                    Text("Confirm Cancellation", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("Keep Premium")
                }
            }
        )
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = SavorAccentOrange,
                checkedTrackColor = SavorAccentOrange.copy(alpha = 0.35f)
            )
        )
    }
}
