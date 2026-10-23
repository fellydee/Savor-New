package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Recipe
import com.example.data.model.User
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.components.ChefFlowerBadge
import com.example.ui.components.FoodVisualThumbnail
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    targetUserId: String? = null,
    viewModel: SavorViewModel,
    modifier: Modifier = Modifier
) {
    if (targetUserId != null) {
        BackHandler { viewModel.navigateBack() }
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.users.collectAsState()
    val recipes by viewModel.recipes.collectAsState()

    val isOwnProfile = targetUserId == null || targetUserId == currentUser.id
    val user: User = if (isOwnProfile) currentUser else (users.find { it.id == targetUserId } ?: currentUser)

    var selectedTab by remember { mutableStateOf(0) } // 0: Posts, 1: Saved (if own)
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Filter recipes for this profile
    val userRecipes = remember(recipes, user, currentUser) {
        if (isOwnProfile) {
            recipes.filter { it.authorId == user.id }
        } else {
            recipes.filter { it.authorId == user.id && viewModel.repository.canUserViewRecipe(it) }
        }
    }

    val savedRecipes = remember(recipes) {
        recipes.filter { it.isSavedByMe }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen")
    ) {
        // --- 1. Top Bar ---
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (!isOwnProfile) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(36.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (user.hasVerificationBadge) {
                        Spacer(modifier = Modifier.width(4.dp))
                        VerificationBadge()
                    }
                }

                if (isOwnProfile) {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Settings) },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                } else {
                    Spacer(modifier = Modifier.width(36.dp))
                }
            }
        }

        // --- 2. Profile Header (Avatar, Counts, Bio, Chef Flowers) ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(SavorStoryRingGradient)
                            .padding(3.dp)
                    ) {
                        if (user.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(SavorGoldenYellow)
                            ) {
                                Text(
                                    text = user.displayName.take(1).ifBlank { "U" },
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Stat Counts
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileStatColumn(count = userRecipes.size, label = "Recipes")
                        ProfileStatColumn(count = user.followersCount, label = "Followers")
                        ProfileStatColumn(count = user.followingCount, label = "Following")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name & City
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (user.isPremium) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SavorGoldenYellow.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "PRO CHEF",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = SavorAccentOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = SavorAccentOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = user.city,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bio
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Chef Flowers Badge Showcase
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SavorGoldenYellow.copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SavorGoldenYellow.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(Screen.Leaderboard) }
                        .testTag("profile_flower_showcase")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFlorist,
                                contentDescription = null,
                                tint = SavorAccentOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${user.chefFlowers} Chef Flowers",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${user.totalLikesReceived} total likes received (1 flower per 250 likes)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = "Leaderboard",
                            tint = SavorAccentOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Edit Profile (if own) OR Follow/Message (if other)
                if (isOwnProfile) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { showEditProfileDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_profile_button")
                        ) {
                            Text("Edit Profile")
                        }

                        Button(
                            onClick = { viewModel.navigateTo(Screen.ShoppingList) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SavorGoldenYellow),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_grocery_button")
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Grocery List", color = Color.Black)
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.toggleFollow(user.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.isFollowing) MaterialTheme.colorScheme.surfaceVariant else SavorAccentOrange
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_follow_button")
                        ) {
                            Text(
                                text = if (user.isFollowing) "Following" else "Follow",
                                color = if (user.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.Messages) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Message")
                        }
                    }
                }
            }
        }

        // --- 3. Tabs (Posts vs Saved) ---
        if (isOwnProfile) {
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = SavorAccentOrange,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("My Recipes (${userRecipes.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Saved (${savedRecipes.size})", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // --- 4. Recipe Grid (2 columns) ---
        val displayedRecipes = if (isOwnProfile && selectedTab == 1) savedRecipes else userRecipes

        if (displayedRecipes.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp)
                ) {
                    Text(
                        text = if (selectedTab == 1) "No saved recipes yet." else "No published recipes yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(displayedRecipes.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { itemRecipe ->
                        Box(modifier = Modifier.weight(1f)) {
                            ProfileRecipeGridCard(
                                recipe = itemRecipe,
                                onClick = { viewModel.navigateTo(Screen.RecipeDetail(itemRecipe.id)) }
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // --- Edit Profile Dialog ---
    if (showEditProfileDialog) {
        val context = LocalContext.current
        var editName by remember { mutableStateOf(user.displayName) }
        var editUsername by remember { mutableStateOf(user.username) }
        var editBio by remember { mutableStateOf(user.bio) }
        var editCity by remember { mutableStateOf(user.city) }
        var editAvatarUrl by remember { mutableStateOf(user.avatarUrl) }

        // Gallery Photo Picker launcher
        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                editAvatarUrl = uri.toString()
            }
        }

        // Camera photo launcher
        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            if (bitmap != null) {
                try {
                    val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    editAvatarUrl = file.toURI().toString()
                } catch (e: Exception) {
                    android.util.Log.e("ProfileScreen", "Failed to cache camera photo", e)
                }
            }
        }

        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                cameraLauncher.launch(null)
            }
        }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Photo Preview & Change Buttons
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (editAvatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = editAvatarUrl,
                                contentDescription = "Avatar Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("upload_profile_gallery_button")
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("take_profile_camera_button")
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_display_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username (@handle)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_username_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio (Share your culinary passion)") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("edit_bio_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editCity,
                        onValueChange = { editCity = it },
                        label = { Text("City & State (e.g. Austin, TX)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_city_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.repository.updateProfile(
                            displayName = editName,
                            bio = editBio,
                            city = editCity,
                            avatarUrl = editAvatarUrl,
                            username = editUsername
                        )
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SavorAccentOrange),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileStatColumn(
    count: Int,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (count >= 1000) "${count / 1000}k+" else "$count",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileRecipeGridCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            FoodVisualThumbnail(
                recipe = recipe,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = "${recipe.likesCount} likes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
