package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.Screen
import com.example.ui.SavorViewModel
import com.example.ui.auth.SignInScreen
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.SavorTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val savorViewModel: SavorViewModel = viewModel()
            val isDark by savorViewModel.isDarkMode.collectAsState()
            val currentScreen by savorViewModel.currentScreen.collectAsState()
            val showQuickAccess by savorViewModel.showQuickAccessSheet.collectAsState()
            val showUpgrade by savorViewModel.showUpgradeModal.collectAsState()
            val activeShareRecipe by savorViewModel.activeShareRecipe.collectAsState()
            val activeMealPlanRecipe by savorViewModel.activeMealPlanRecipe.collectAsState()
            val activeSubstitution by savorViewModel.activeSubstitutionIngredient.collectAsState()
            val showUnitConverter by savorViewModel.showUnitConverterSheet.collectAsState()
            val showVersionHistoryRecipe by savorViewModel.showVersionHistoryRecipe.collectAsState()
            val showCoAuthorPickerRecipe by savorViewModel.showCoAuthorPickerRecipe.collectAsState()
            val showAddToCollectionRecipe by savorViewModel.showAddToCollectionRecipe.collectAsState()
            val toastMessage by savorViewModel.snackBarMessage.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(toastMessage) {
                toastMessage?.let {
                    snackbarHostState.showSnackbar(it)
                    savorViewModel.clearToast()
                }
            }

            SavorTheme(darkTheme = isDark) {
                val auth = remember { FirebaseAuth.getInstance() }
                var currentFirebaseUser by remember { mutableStateOf(auth.currentUser) }

                DisposableEffect(auth) {
                    val listener = FirebaseAuth.AuthStateListener { fbAuth ->
                        currentFirebaseUser = fbAuth.currentUser
                    }
                    auth.addAuthStateListener(listener)
                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }

                if (currentFirebaseUser == null) {
                    SignInScreen(
                        onAuthSuccess = {
                            currentFirebaseUser = auth.currentUser
                        }
                    )
                } else {
                    val isStoryViewer = currentScreen is Screen.StoryViewer
                    val isChatThread = currentScreen is Screen.Chat
                    val isCookingMode = currentScreen is Screen.CookingMode

                    val showTopBar = !isStoryViewer && !isChatThread && !isCookingMode
                    val showBottomNav = !isCookingMode && (
                        currentScreen is Screen.Feed ||
                        currentScreen is Screen.Explore ||
                        currentScreen is Screen.Create ||
                        currentScreen is Screen.MealPlan ||
                        currentScreen is Screen.Profile
                    )

                    Scaffold(
                    topBar = {
                        if (showTopBar) {
                            SavorTopBar(viewModel = savorViewModel)
                        }
                    },
                    bottomBar = {
                        if (showBottomNav) {
                            SavorBottomNavigation(viewModel = savorViewModel)
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (val scr = currentScreen) {
                            is Screen.Feed -> FeedScreen(viewModel = savorViewModel)
                            is Screen.Explore -> ExploreScreen(viewModel = savorViewModel)
                            is Screen.Create -> RecipeCreateScreen(viewModel = savorViewModel)
                            is Screen.MealPlan -> MealPlanScreen(viewModel = savorViewModel)
                            is Screen.Profile -> ProfileScreen(targetUserId = null, viewModel = savorViewModel)
                            is Screen.RecipeDetail -> RecipeDetailScreen(recipeId = scr.recipeId, viewModel = savorViewModel)
                            is Screen.OtherProfile -> ProfileScreen(targetUserId = scr.userId, viewModel = savorViewModel)
                            is Screen.StoryViewer -> StoryViewerScreen(initialStoryId = scr.initialStoryId, viewModel = savorViewModel)
                            is Screen.Activity -> ActivityScreen(viewModel = savorViewModel)
                            is Screen.NotificationCenter -> NotificationCenterScreen(viewModel = savorViewModel)
                            is Screen.Messages -> DirectMessagesInboxScreen(viewModel = savorViewModel)
                            is Screen.Chat -> ChatThreadScreen(conversationId = scr.conversationId, viewModel = savorViewModel)
                            is Screen.ShoppingList -> ShoppingListScreen(viewModel = savorViewModel)
                            is Screen.Leaderboard -> LeaderboardScreen(viewModel = savorViewModel)
                            is Screen.Checkout -> CheckoutScreen(viewModel = savorViewModel)
                            is Screen.Settings -> SettingsScreen(viewModel = savorViewModel)
                            is Screen.CookingMode -> CookingModeScreen(
                                recipeId = scr.recipeId,
                                initialStepIndex = scr.initialStepIndex,
                                viewModel = savorViewModel
                            )
                            is Screen.Collections -> CollectionsScreen(viewModel = savorViewModel)
                            is Screen.CollectionDetail -> CollectionDetailScreen(collectionId = scr.collectionId, viewModel = savorViewModel)
                            is Screen.NutritionalDatabase -> NutritionalDatabaseScreen(viewModel = savorViewModel)
                        }
                    }

                    // Bottom Sheets & Dialogs
                    if (showQuickAccess) {
                        QuickAccessBottomSheet(viewModel = savorViewModel)
                    }

                    if (showUpgrade) {
                        UpgradePromptModal(viewModel = savorViewModel)
                    }

                    activeShareRecipe?.let { recipe ->
                        ShareModalSheet(recipe = recipe, viewModel = savorViewModel)
                    }

                    activeMealPlanRecipe?.let { recipe ->
                        AddToMealPlanDialog(recipe = recipe, viewModel = savorViewModel)
                    }

                    activeSubstitution?.let { ingredient ->
                        IngredientSubstitutionSheet(
                            ingredient = ingredient,
                            onDismiss = { savorViewModel.closeSubstitutions() }
                        )
                    }

                    if (showUnitConverter) {
                        UnitConverterSheet(
                            onDismiss = { savorViewModel.closeUnitConverter() }
                        )
                    }

                    showVersionHistoryRecipe?.let { recipe ->
                        RecipeVersionHistoryDialog(
                            recipe = recipe,
                            viewModel = savorViewModel,
                            onDismiss = { savorViewModel.closeVersionHistory() }
                        )
                    }

                    showCoAuthorPickerRecipe?.let { recipe ->
                        CoAuthorPickerDialog(
                            recipe = recipe,
                            viewModel = savorViewModel,
                            onDismiss = { savorViewModel.closeCoAuthorPicker() }
                        )
                    }

                    showAddToCollectionRecipe?.let { recipe ->
                        AddToCollectionSheet(
                            recipe = recipe,
                            viewModel = savorViewModel,
                            onDismiss = { savorViewModel.closeAddToCollection() }
                        )
                    }
                }
            }
        }
    }
}
}

