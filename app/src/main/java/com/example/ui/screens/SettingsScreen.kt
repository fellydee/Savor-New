package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
    val currentLang by viewModel.appLanguage.collectAsState()
    val smsSettings by viewModel.smsSettings.collectAsState()
    val refundHistory by viewModel.refundHistory.collectAsState()

    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var showRefundDialog by remember { mutableStateOf(false) }
    var selectedRefundReason by remember { mutableStateOf(RefundReason.ACCIDENTAL_PURCHASE) }
    var showGiftDialog by remember { mutableStateOf(false) }
    var giftRecipientUsername by remember { mutableStateOf("") }
    var promoCodeInput by remember { mutableStateOf("") }

    // SMS local state
    var smsEnabled by remember(smsSettings) { mutableStateOf(smsSettings.isSmsAlertsEnabled) }
    var smsPhone by remember(smsSettings) { mutableStateOf(smsSettings.phoneNumber) }
    var smsTimers by remember(smsSettings) { mutableStateOf(smsSettings.notifyCookingTimers) }
    var smsLive by remember(smsSettings) { mutableStateOf(smsSettings.notifyLiveStreams) }
    var smsCoAuthor by remember(smsSettings) { mutableStateOf(smsSettings.notifyCoAuthorInvites) }

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
                    text = "Settings & Subscriptions",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // --- Section 1: Single Premium Subscription Plan ("Savor Pro Chef") ---
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
                                    text = if (currentUser.isPremium) "Savor Pro Chef Active" else "Single Premium Plan: Savor Pro Chef",
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
                                    "$9.99/mo • Unlimited meal planning, verified badge & studio tools"
                                else
                                    "$9.99/mo • Savor's only premium tier for professional foodies & creators",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (currentUser.isPremium) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showCancelConfirmDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cancel_premium_button")
                            ) {
                                Text("Cancel Tier")
                            }
                            Button(
                                onClick = { showRefundDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("request_refund_button")
                            ) {
                                Text("Request Refund", color = Color.White)
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.navigateTo(Screen.Checkout) },
                            colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_upgrade_button")
                        ) {
                            Text("Upgrade to Savor Pro Chef ($9.99/mo)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Display refund history if any
                    if (refundHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Refund History (${refundHistory.size} processed):",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                refundHistory.take(2).forEach { r ->
                                    Text(
                                        text = "• $${r.amountUsd} refunded (${r.confirmationCode}) - ${r.reason.label}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Section 2: Gift Subscriptions & Promotional Codes ---
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Gift Subscriptions & Promo Codes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = promoCodeInput,
                            onValueChange = { promoCodeInput = it },
                            label = { Text("Enter Promo or Gift Code") },
                            placeholder = { Text("e.g. SAVORCHEF2026") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("promo_code_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (promoCodeInput.isNotBlank()) {
                                    viewModel.redeemPromoCode(promoCodeInput)
                                    promoCodeInput = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                            modifier = Modifier.testTag("redeem_promo_button")
                        ) {
                            Text("Redeem", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showGiftDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gift_subscription_button")
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = SavorAccentOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gift 1-Month Savor Pro Chef to a Friend")
                    }
                }
            }
        }

        // --- Section 3: Verification Badge Status & Expiry ---
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Creator Verification & Badge Lifecycle",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = if (currentUser.hasVerificationBadge) SavorAccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Badge Status: ${currentUser.badgeStatus.name}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        StatusBadge(status = currentUser.badgeStatus)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentUser.badgeStatus == BadgeStatus.ACTIVE)
                            "Your verified badge is active. Verification remains valid with an active Savor Pro Chef subscription or 10,000+ followers."
                        else
                            "Notice: ${currentUser.badgeRevocationReason ?: "Follower count or subscription lapsed."}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = { viewModel.navigateTo(Screen.AdminModeration) },
                        modifier = Modifier.testTag("open_admin_moderation_button")
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Admin & Moderation Hub")
                    }
                }
            }
        }

        // --- Section 4: Multi-Language / Localization ---
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Language & Localization",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Language: ${currentLang.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppLanguage.values().forEach { lang ->
                            val isSelected = currentLang == lang
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setLanguage(lang) },
                                label = { Text(lang.displayName) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        // --- Section 5: Push Notifications vs Email Disclosure ---
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Push Notifications",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Email Disabled Disclosure Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SavorAccentOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instant Push Delivery Only: Email notifications are disabled to keep your culinary alerts immediate and avoid inbox clutter.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    NotificationToggleRow(
                        title = "Likes & Recipe Reactions",
                        subtitle = "Alerts when someone loves your recipe reel",
                        isChecked = pushSettings[ActivityType.LIKE] ?: true,
                        onToggle = { viewModel.togglePushSetting(ActivityType.LIKE) }
                    )
                    HorizontalDivider()
                    NotificationToggleRow(
                        title = "Comments & Questions",
                        subtitle = "Alerts when fellow foodies ask about steps",
                        isChecked = pushSettings[ActivityType.COMMENT] ?: true,
                        onToggle = { viewModel.togglePushSetting(ActivityType.COMMENT) }
                    )
                    HorizontalDivider()
                    NotificationToggleRow(
                        title = "New Followers & Co-Authors",
                        subtitle = "Alerts for new cooking community members",
                        isChecked = pushSettings[ActivityType.FOLLOW] ?: true,
                        onToggle = { viewModel.togglePushSetting(ActivityType.FOLLOW) }
                    )
                }
            }
        }

        // --- Section 6: SMS Notifications ---
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "SMS Notifications",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Enable Text Alerts", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(text = "Receive urgent step alerts & live stream pings via SMS", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = smsEnabled,
                            onCheckedChange = {
                                smsEnabled = it
                                viewModel.updateSmsSettings(
                                    smsSettings.copy(
                                        isSmsAlertsEnabled = it,
                                        phoneNumber = smsPhone,
                                        notifyCookingTimers = smsTimers,
                                        notifyLiveStreams = smsLive,
                                        notifyCoAuthorInvites = smsCoAuthor
                                    )
                                )
                            }
                        )
                    }

                    if (smsEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = smsPhone,
                            onValueChange = { smsPhone = it },
                            label = { Text("Mobile Phone Number") },
                            placeholder = { Text("+1 (555) 234-5678") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = smsTimers, onCheckedChange = { smsTimers = it })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cooking step timer completion alerts", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = smsLive, onCheckedChange = { smsLive = it })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Live kitchen masterclass start alerts", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = smsCoAuthor, onCheckedChange = { smsCoAuthor = it })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Co-author recipe collaboration invitations", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.updateSmsSettings(
                                    SmsNotificationSettings(
                                        phoneNumber = smsPhone,
                                        isSmsAlertsEnabled = true,
                                        notifyCookingTimers = smsTimers,
                                        notifyLiveStreams = smsLive,
                                        notifyCoAuthorInvites = smsCoAuthor
                                    )
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save SMS Settings")
                        }
                    }
                }
            }
        }

        // --- Section 7: Appearance & Theme ---
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
                                text = if (isDark) "Midnight charcoal & warm cream" else "Sunlit cream & orange",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = SavorAccentOrange)
                    )
                }
            }
        }

        // --- Section 8: Account Information & Log Out ---
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
                Column(modifier = Modifier.padding(16.dp)) {
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

    // Cancel Subscription Confirmation Dialog
    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text("Cancel Savor Pro Chef?") },
            text = {
                Text("Your Savor Pro Chef features and verification badge will remain active until the end of your billing cycle.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelPremium()
                        showCancelConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Cancellation", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("Keep Plan")
                }
            }
        )
    }

    // Refund Dialog
    if (showRefundDialog) {
        AlertDialog(
            onDismissRequest = { showRefundDialog = false },
            title = { Text("Request Savor Pro Chef Refund ($9.99)") },
            text = {
                Column {
                    Text(
                        text = "Please select the primary reason for your refund request. If processed, $9.99 will be returned to your original payment method immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    RefundReason.values().forEach { reason ->
                        val isSelected = selectedRefundReason == reason
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRefundReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedRefundReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = SavorAccentOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = reason.label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestSubscriptionRefund(selectedRefundReason)
                        showRefundDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Process Refund ($9.99)", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRefundDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Gift Subscription Dialog
    if (showGiftDialog) {
        AlertDialog(
            onDismissRequest = { showGiftDialog = false },
            title = { Text("Gift Savor Pro Chef") },
            text = {
                Column {
                    Text(
                        text = "Generate a 1-Month Savor Pro Chef voucher code for a foodie friend:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = giftRecipientUsername,
                        onValueChange = { giftRecipientUsername = it },
                        label = { Text("Recipient's Username") },
                        placeholder = { Text("e.g. foodielover23") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val recipient = giftRecipientUsername.ifBlank { "friend" }
                        viewModel.generateGiftCode(recipient)
                        showGiftDialog = false
                        giftRecipientUsername = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange)
                ) {
                    Text("Generate Gift Code", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGiftDialog = false }) {
                    Text("Cancel")
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
