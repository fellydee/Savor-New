package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firestore.FirestoreRepository
import com.example.data.model.*
import com.example.data.repository.SavorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Feed : Screen()
    object Explore : Screen()
    object Create : Screen()
    object MealPlan : Screen()
    object Profile : Screen()
    data class RecipeDetail(val recipeId: String) : Screen()
    data class OtherProfile(val userId: String) : Screen()
    data class StoryViewer(val initialStoryId: String) : Screen()
    object Activity : Screen()
    object NotificationCenter : Screen()
    object Messages : Screen()
    data class Chat(val conversationId: String) : Screen()
    data class GroupChat(val groupId: String) : Screen()
    object ShoppingList : Screen()
    object Leaderboard : Screen()
    object Checkout : Screen()
    object Settings : Screen()
    data class CookingMode(val recipeId: String, val initialStepIndex: Int = 0) : Screen()
    object Collections : Screen()
    data class CollectionDetail(val collectionId: String) : Screen()
    object NutritionalDatabase : Screen()
    object AdminModeration : Screen()
    data class LiveStream(val sessionId: String) : Screen()
}

class SavorViewModel(
    val repository: SavorRepository = SavorRepository()
) : ViewModel() {

    var firestoreRepository: FirestoreRepository? = null

    // Current Screen Navigation Stack
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Feed)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenStack = mutableListOf<Screen>(Screen.Feed)

    // Modals & Sheets
    private val _showQuickAccessSheet = MutableStateFlow(false)
    val showQuickAccessSheet: StateFlow<Boolean> = _showQuickAccessSheet.asStateFlow()

    private val _showUpgradeModal = MutableStateFlow(false)
    val showUpgradeModal: StateFlow<Boolean> = _showUpgradeModal.asStateFlow()

    private val _activeShareRecipe = MutableStateFlow<Recipe?>(null)
    val activeShareRecipe: StateFlow<Recipe?> = _activeShareRecipe.asStateFlow()

    private val _activeMealPlanRecipe = MutableStateFlow<Recipe?>(null)
    val activeMealPlanRecipe: StateFlow<Recipe?> = _activeMealPlanRecipe.asStateFlow()

    private val _activeSubstitutionIngredient = MutableStateFlow<Ingredient?>(null)
    val activeSubstitutionIngredient: StateFlow<Ingredient?> = _activeSubstitutionIngredient.asStateFlow()

    private val _showUnitConverterSheet = MutableStateFlow(false)
    val showUnitConverterSheet: StateFlow<Boolean> = _showUnitConverterSheet.asStateFlow()

    private val _showVersionHistoryRecipe = MutableStateFlow<Recipe?>(null)
    val showVersionHistoryRecipe: StateFlow<Recipe?> = _showVersionHistoryRecipe.asStateFlow()

    private val _showCoAuthorPickerRecipe = MutableStateFlow<Recipe?>(null)
    val showCoAuthorPickerRecipe: StateFlow<Recipe?> = _showCoAuthorPickerRecipe.asStateFlow()

    private val _showAddToCollectionRecipe = MutableStateFlow<Recipe?>(null)
    val showAddToCollectionRecipe: StateFlow<Recipe?> = _showAddToCollectionRecipe.asStateFlow()

    private val _snackBarMessage = MutableStateFlow<String?>(null)
    val snackBarMessage: StateFlow<String?> = _snackBarMessage.asStateFlow()

    // Data streams from repository
    val currentUser = repository.currentUser
    val users = repository.users
    val recipes = repository.recipes
    val collections = repository.collections
    val reviews = repository.reviews
    val customIngredients = repository.customIngredients
    val stories = repository.stories
    val activities = repository.activities
    val conversations = repository.conversations
    val messages = repository.messages
    val groupConversations = repository.groupConversations
    val groupMessages = repository.groupMessages
    val appLanguage = repository.appLanguage
    val liveStreams = repository.liveStreams
    val activeLiveSession = repository.activeLiveSession
    val moderationReports = repository.moderationReports
    val verificationAudits = repository.verificationAudits
    val refundHistory = repository.refundHistory
    val promoCodes = repository.promoCodes
    val smsSettings = repository.smsSettings
    val comments = repository.comments
    val shoppingList = repository.shoppingList
    val mealPlan = repository.mealPlan
    val templates = repository.templates
    val isDarkMode = repository.isDarkMode
    val pushSettings = repository.pushSettings

    // Navigation functions
    fun navigateTo(screen: Screen) {
        if (screen != _currentScreen.value) {
            screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = screenStack.last()
            return true
        }
        return false
    }

    // Modal controls
    fun openQuickAccess() {
        _showQuickAccessSheet.value = true
    }

    fun closeQuickAccess() {
        _showQuickAccessSheet.value = false
    }

    fun openUpgradeModal() {
        _showUpgradeModal.value = true
    }

    fun closeUpgradeModal() {
        _showUpgradeModal.value = false
    }

    fun openShareMenu(recipe: Recipe) {
        _activeShareRecipe.value = recipe
    }

    fun closeShareMenu() {
        _activeShareRecipe.value = null
    }

    fun openAddToMealPlanDialog(recipe: Recipe) {
        if (!currentUser.value.isPremium) {
            openUpgradeModal()
            return
        }
        _activeMealPlanRecipe.value = recipe
    }

    fun closeAddToMealPlanDialog() {
        _activeMealPlanRecipe.value = null
    }

    fun openSubstitutions(ingredient: Ingredient) {
        _activeSubstitutionIngredient.value = ingredient
    }

    fun closeSubstitutions() {
        _activeSubstitutionIngredient.value = null
    }

    fun openUnitConverter() {
        _showUnitConverterSheet.value = true
    }

    fun closeUnitConverter() {
        _showUnitConverterSheet.value = false
    }

    fun openVersionHistory(recipe: Recipe) {
        _showVersionHistoryRecipe.value = recipe
    }

    fun closeVersionHistory() {
        _showVersionHistoryRecipe.value = null
    }

    fun openCoAuthorPicker(recipe: Recipe) {
        _showCoAuthorPickerRecipe.value = recipe
    }

    fun closeCoAuthorPicker() {
        _showCoAuthorPickerRecipe.value = null
    }

    fun openAddToCollection(recipe: Recipe) {
        _showAddToCollectionRecipe.value = recipe
    }

    fun closeAddToCollection() {
        _showAddToCollectionRecipe.value = null
    }

    fun showToast(msg: String) {
        _snackBarMessage.value = msg
    }

    fun clearToast() {
        _snackBarMessage.value = null
    }

    // Action delegates
    fun toggleLike(recipeId: String) = repository.toggleLike(recipeId)
    fun toggleSave(recipeId: String) = repository.toggleSave(recipeId)
    fun toggleFollow(userId: String) = repository.toggleFollow(userId)

    fun addRecipeReview(recipeId: String, rating: Int, comment: String) {
        repository.addRecipeReview(recipeId, rating, comment)
        showToast("Review submitted! Rating updated ⭐")
    }

    fun createCollection(title: String, description: String, isPublic: Boolean = true) {
        val id = repository.createCollection(title, description, isPublic)
        showToast("Collection '$title' created!")
    }

    fun toggleRecipeInCollection(collectionId: String, recipeId: String) {
        repository.toggleRecipeInCollection(collectionId, recipeId)
        showToast("Updated collection playlist!")
    }

    fun addRecipeVersion(recipeId: String, note: String, cookTime: Int?) {
        repository.addRecipeVersion(recipeId, note, cookTime)
        closeVersionHistory()
        showToast("New recipe version snapshot created!")
    }

    fun inviteCoAuthor(recipeId: String, user: User) {
        val coAuthor = CoAuthor(
            userId = user.id,
            username = user.username,
            displayName = user.displayName,
            avatarUrl = user.avatarUrl,
            isVerified = user.hasVerificationBadge
        )
        repository.addCoAuthor(recipeId, coAuthor)
        closeCoAuthorPicker()
        showToast("Added @${user.username} as Co-Author!")
    }

    fun addCustomIngredient(
        name: String,
        category: String,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        allergens: List<Allergen>,
        defaultServing: String
    ) {
        repository.addCustomIngredient(name, category, calories, protein, carbs, fat, allergens, defaultServing)
        showToast("Added '$name' to your nutritional database!")
    }

    fun deleteCustomIngredient(id: String) {
        repository.deleteCustomIngredient(id)
        showToast("Ingredient deleted.")
    }

    fun updateUserAllergens(allergens: List<Allergen>) {
        repository.updateUserAllergens(allergens)
        showToast("Allergen alerts profile updated!")
    }

    fun getRecipeAllergenConflicts(recipe: Recipe): List<Allergen> {
        return repository.getRecipeAllergenConflicts(recipe)
    }

    fun shareToStory(recipe: Recipe) {
        repository.shareRecipeToStory(recipe)
        closeShareMenu()
        showToast("Recipe shared to your story with original creator attribution!")
    }

    fun sendRecipeInDM(conversationId: String, recipe: Recipe) {
        repository.sendRecipeAsMessage(conversationId, recipe)
        closeShareMenu()
        showToast("Recipe card sent in direct message!")
    }

    fun addComment(recipeId: String, text: String) {
        if (text.isNotBlank()) {
            repository.addComment(recipeId, text.trim())
        }
    }

    fun addRecipeToShoppingList(recipe: Recipe) {
        repository.addRecipeToShoppingList(recipe)
        showToast("Ingredients from '${recipe.title}' added to shopping list!")
    }

    fun toggleShoppingItem(id: String) = repository.toggleShoppingItem(id)
    fun removeShoppingItem(id: String) = repository.removeShoppingItem(id)
    fun addCustomShoppingItem(name: String, amount: String, unit: String) =
        repository.addCustomShoppingItem(name, amount, unit)
    fun clearCompletedShoppingItems() = repository.clearCompletedShoppingItems()

    fun assignRecipeToMealSlot(day: Int, slot: MealSlot, recipe: Recipe) {
        repository.assignRecipeToMealSlot(day, slot, recipe)
        closeAddToMealPlanDialog()
        showToast("Assigned '${recipe.title}' to ${slot.label}!")
    }

    fun removeMealPlanEntry(id: String) = repository.removeMealPlanEntry(id)
    fun autoGenerateMealPlan() {
        repository.autoGenerateMealPlan()
        showToast("Weekly meal plan generated from your top-liked recipes!")
    }
    fun saveMealPlanTemplate(name: String, desc: String) {
        repository.saveMealPlanTemplate(name, desc)
        showToast("Saved meal plan template '$name'!")
    }
    fun loadMealPlanTemplate(template: MealPlanTemplate) {
        repository.loadMealPlanTemplate(template)
        showToast("Loaded template '${template.name}'!")
    }
    fun deleteMealPlanTemplate(id: String) = repository.deleteMealPlanTemplate(id)
    fun bulkAddWeekToShoppingList() {
        repository.bulkAddWeekToShoppingList()
        showToast("All ingredients for this week added to shopping list!")
    }

    fun upgradeToPremium() {
        repository.upgradeToPremium()
        closeUpgradeModal()
        showToast("Welcome to Savor Premium! Verification badge activated ⭐")
    }

    fun cancelPremium() {
        repository.cancelPremium()
        showToast("Premium subscription cancelled.")
    }

    fun toggleDarkMode() = repository.toggleDarkMode()
    fun togglePushSetting(type: ActivityType) = repository.togglePushSetting(type)
    fun markActivitiesAsRead() = repository.markActivitiesAsRead()
    fun markConversationAsRead(id: String) = repository.markConversationAsRead(id)

    fun syncFirebaseUser(userId: String, email: String, displayName: String, avatarUrl: String) {
        repository.syncFirebaseUser(userId, email, displayName, avatarUrl)
        viewModelScope.launch {
            try {
                firestoreRepository?.saveUserProfile(repository.currentUser.value)
            } catch (e: Exception) {
                // local fallback maintained
            }
        }
    }

    fun publishRecipe(recipe: Recipe) {
        repository.publishRecipe(recipe)
        viewModelScope.launch {
            try {
                firestoreRepository?.createRecipe(recipe)
            } catch (e: Exception) {
                // local fallback maintained
            }
        }
        showToast("Your recipe '${recipe.title}' is now live on Savor!")
    }

    fun postStory(mediaUrl: String, textOverlay: String) {
        repository.postStory(mediaUrl, textOverlay)
        showToast("Story posted! Visible for 24 hours.")
    }

    fun deleteStory(storyId: String) = repository.deleteStory(storyId)

    // --- Messaging ---
    fun sendMessage(conversationId: String, text: String, recipeCard: Recipe? = null) {
        val message = ChatMessage(
            id = "msg_${java.util.UUID.randomUUID().toString().take(8)}",
            conversationId = conversationId,
            senderId = currentUser.value.id,
            senderUsername = currentUser.value.username,
            text = text,
            recipeCard = recipeCard,
            timestamp = System.currentTimeMillis()
        )
        repository.sendMessage(conversationId, message)
    }

    // --- Group Messaging ---
    fun createGroupConversation(name: String, memberUsernames: List<String>): String {
        val id = repository.createGroupConversation(name = name, description = "", memberUsernames = memberUsernames)
        showToast("Group '$name' created!")
        return id
    }

    fun sendGroupMessage(groupId: String, text: String, recipeCard: Recipe? = null) {
        repository.sendGroupMessage(groupId, text, recipeCard)
    }

    // --- Multi-Language / Localization ---
    fun setLanguage(language: AppLanguage) {
        repository.setLanguage(language)
        showToast("Language switched to ${language.displayName}")
    }

    // --- Live Streaming ---
    fun startLiveStream(title: String, recipeTitle: String? = null, recipeId: String? = null): LiveStreamSession {
        val session = repository.startLiveStream(title, recipeTitle, recipeId)
        showToast("You are now LIVE! 🔴")
        return session
    }

    fun joinLiveStream(sessionId: String) {
        repository.joinLiveStream(sessionId)
    }

    fun leaveLiveStream() {
        repository.leaveLiveStream()
    }

    fun sendLiveComment(text: String) {
        repository.sendLiveComment(text)
    }

    fun sendLiveReaction() {
        repository.sendLiveReaction()
    }

    // --- Moderation & Admin ---
    fun reportRecipe(recipeId: String, reason: ReportReason, details: String) {
        repository.reportRecipe(recipeId, reason, details)
        showToast("Report submitted to Savor Trust & Safety team. Thank you!")
    }

    fun resolveModerationReport(reportId: String, action: ReportStatus) {
        repository.resolveModerationReport(reportId, action)
        val actionText = if (action == ReportStatus.RESOLVED_REMOVED) "Recipe removed and report resolved." else "Report dismissed."
        showToast(actionText)
    }

    fun auditVerificationBadges() {
        val results = repository.auditVerificationBadges()
        showToast("Badge audit complete: ${results.size} accounts reviewed.")
    }

    fun manuallySetUserBadgeStatus(userId: String, status: BadgeStatus, reason: String) {
        repository.manuallySetUserBadgeStatus(userId, status, reason)
        showToast("Updated badge status to ${status.name}")
    }

    // --- Subscription Refunds ---
    fun requestSubscriptionRefund(reason: RefundReason) {
        val refund = repository.requestSubscriptionRefund(reason)
        showToast("Refund of $9.99 processed. Confirmation: ${refund.confirmationCode}")
    }

    // --- Gift & Promo Codes ---
    fun redeemPromoCode(code: String) {
        val result = repository.redeemPromoCode(code)
        result.onSuccess { message ->
            showToast(message)
        }.onFailure { error ->
            showToast(error.message ?: "Failed to redeem code")
        }
    }

    fun generateGiftCode(recipientUsername: String): String {
        val code = repository.generateGiftCode(recipientUsername)
        showToast("Gift code generated: $code")
        return code
    }

    // --- SMS Notifications ---
    fun updateSmsSettings(settings: SmsNotificationSettings) {
        repository.updateSmsSettings(settings)
        showToast("SMS notification settings saved.")
    }
}
