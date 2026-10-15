package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.theme.*

@Composable
fun CheckoutScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    var selectedPlan by remember { mutableStateOf("annual") } // "monthly" or "annual"
    var cardNumber by remember { mutableStateOf("4242 •••• •••• 4242") }
    var expiryDate by remember { mutableStateOf("08/28") }
    var cvv by remember { mutableStateOf("888") }
    var billingZip by remember { mutableStateOf("78701") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("checkout_screen")
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
                    text = "Savor Premium Membership",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Benefits Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SavorGradientBrush)
                        ) {
                            Icon(imageVector = Icons.Default.Stars, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "All-Access Culinary Studio", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(text = "Unlock professional meal prep tools & verification", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val benefits = listOf(
                        "⭐ Instant Verified Chef Badge on Profile & Posts",
                        "📅 7-Day Weekly Meal Planning Calendar & Saved Templates",
                        "⚡ Automated Calorie & Macronutrient Calculations",
                        "🤖 1-Tap Auto-Generation from Your Top-Liked Recipes",
                        "🛒 Bulk 1-Tap Grocery List Aggregation",
                        "🔒 Full Access to Premium & Followers-Only Recipes"
                    )

                    benefits.forEach { b ->
                        Text(
                            text = b,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Plan Selection Options
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Choose Subscription Plan:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Annual Plan Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (selectedPlan == "annual") SavorGoldenYellow.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    if (selectedPlan == "annual") 2.dp else 1.dp,
                    if (selectedPlan == "annual") SavorAccentOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedPlan = "annual" }
                    .testTag("plan_annual_option")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPlan == "annual",
                            onClick = { selectedPlan = "annual" },
                            colors = RadioButtonDefaults.colors(selectedColor = SavorAccentOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Annual Plan", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(6.dp), color = SavorCrimson) {
                                    Text(text = "SAVE 33%", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                            Text(text = "$79.99 / year ($6.66/month billed annually)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Monthly Plan Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (selectedPlan == "monthly") SavorGoldenYellow.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    if (selectedPlan == "monthly") 2.dp else 1.dp,
                    if (selectedPlan == "monthly") SavorAccentOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedPlan = "monthly" }
                    .testTag("plan_monthly_option")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    RadioButton(
                        selected = selectedPlan == "monthly",
                        onClick = { selectedPlan = "monthly" },
                        colors = RadioButtonDefaults.colors(selectedColor = SavorAccentOrange)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Monthly Plan", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(text = "$9.99 / month • Cancel anytime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Payment Form
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Payment Information:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Card Number") },
                        leadingIcon = { Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = SavorAccentOrange) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_number_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text("MM/YY") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { cvv = it },
                            label = { Text("CVV") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = billingZip,
                            onValueChange = { billingZip = it },
                            label = { Text("Billing Zip") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = SavorCrimson,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Confirm Button
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (cardNumber.isBlank() || cvv.isBlank()) {
                        errorMessage = "Payment failed. Please check your card details and try again."
                    } else {
                        errorMessage = null
                        isProcessing = true
                        viewModel.upgradeToPremium()
                        viewModel.navigateTo(Screen.Settings)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_purchase_button")
            ) {
                Text(
                    text = if (selectedPlan == "annual") "Start Annual Premium ($79.99/yr)" else "Start Monthly Premium ($9.99/mo)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Renews automatically. Cancel anytime in Settings without cancellation fees.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
