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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.Recipe
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.components.FoodVisualThumbnail
import com.example.ui.components.OriginalPosterAttribution
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*

// --- DM Inbox Screen ---
@Composable
fun DirectMessagesInboxScreen(
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val conversations by viewModel.conversations.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dm_inbox_screen")
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Direct Messages",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        if (conversations.isEmpty()) {
            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(40.dp)) {
                    Text("No direct message conversations yet.")
                }
            }
        } else {
            items(conversations, key = { it.id }) { convo ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .clickable {
                            viewModel.markConversationAsRead(convo.id)
                            viewModel.navigateTo(Screen.Chat(convo.id))
                        }
                        .testTag("conversation_row_${convo.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SavorGoldenYellow)
                        ) {
                            Text(
                                text = convo.otherUser.displayName.take(1),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = convo.otherUser.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                if (convo.otherUser.hasVerificationBadge) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    VerificationBadge(modifier = Modifier.size(13.dp))
                                }
                            }
                            Text(
                                text = convo.lastMessage,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (convo.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (convo.unreadCount > 0) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(SavorAccentOrange)
                            ) {
                                Text(
                                    text = "${convo.unreadCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// --- Individual Chat Thread Screen ---
@Composable
fun ChatThreadScreen(
    conversationId: String,
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    val conversations by viewModel.conversations.collectAsState()
    val allMessages by viewModel.messages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val convo = conversations.find { it.id == conversationId }
    val threadMessages = allMessages[conversationId] ?: emptyList()

    var messageInput by remember { mutableStateOf("") }

    if (convo == null) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("Conversation not found.")
        }
        return
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = convo.otherUser.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            if (convo.otherUser.hasVerificationBadge) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerificationBadge(modifier = Modifier.size(13.dp))
                            }
                        }
                        Text(
                            text = "@${convo.otherUser.username} • ${convo.otherUser.city}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Message ${convo.otherUser.displayName}...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                val msg = ChatMessage(
                                    id = "msg_${System.currentTimeMillis()}",
                                    conversationId = conversationId,
                                    senderId = currentUser.id,
                                    senderUsername = currentUser.username,
                                    text = messageInput.trim(),
                                    timestamp = System.currentTimeMillis()
                                )
                                viewModel.repository.sendMessage(conversationId, msg)
                                messageInput = ""
                            }
                        },
                        modifier = Modifier.testTag("chat_send_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = SavorAccentOrange)
                    }
                }
            }
        },
        modifier = modifier.testTag("chat_thread_screen")
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(threadMessages, key = { it.id }) { msg ->
                val isMe = msg.senderId == currentUser.id

                Column(
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // If Message is a Rich Recipe Card Message
                    if (msg.recipeCard != null) {
                        RichRecipeCardMessageBubble(
                            recipe = msg.recipeCard,
                            isMe = isMe,
                            text = msg.text,
                            onRecipeClick = {
                                viewModel.navigateTo(Screen.RecipeDetail(msg.recipeCard.id))
                            }
                        )
                    } else {
                        // Regular text bubble
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            color = if (isMe) SavorAccentOrange else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Rich Recipe Card Message Bubble ---
@Composable
fun RichRecipeCardMessageBubble(
    recipe: Recipe,
    isMe: Boolean,
    text: String,
    onRecipeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .width(260.dp)
            .clickable { onRecipeClick() }
            .testTag("rich_recipe_card_message")
    ) {
        Column {
            FoodVisualThumbnail(
                recipe = recipe,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                // OP Attribution if reshared
                if (recipe.isReshared) {
                    OriginalPosterAttribution(
                        originalPosterUsername = recipe.originalPosterUsername!!,
                        isOriginalPosterVerified = recipe.originalPosterVerified,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "By @${recipe.authorUsername}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (recipe.isAuthorVerified) {
                        Spacer(modifier = Modifier.width(3.dp))
                        VerificationBadge(modifier = Modifier.size(11.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SavorAccentOrange.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "View Recipe & Steps →",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SavorAccentOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
