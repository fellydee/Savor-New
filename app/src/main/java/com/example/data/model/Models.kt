package com.example.data.model

enum class PrivacyLevel(val label: String, val description: String) {
    PUBLIC("Public", "Visible to all Savor foodies"),
    FOLLOWERS_ONLY("Followers Only", "Visible only to your followers"),
    PREMIUM_ONLY("Premium Only", "Exclusive to Savor Premium members")
}

enum class DietaryTag(val label: String, val colorHex: Long) {
    VEGAN("Vegan", 0xFF4CAF50),
    VEGETARIAN("Vegetarian", 0xFF8BC34A),
    KETO("Keto", 0xFFFF9800),
    GLUTEN_FREE("Gluten-Free", 0xFFE91E63),
    DAIRY_FREE("Dairy-Free", 0xFF00BCD4),
    LOW_CARB("Low-Carb", 0xFF9C27B0),
    HIGH_PROTEIN("High-Protein", 0xFFF44336),
    PALEO("Paleo", 0xFF795548)
}

enum class Allergen(val label: String, val icon: String, val colorHex: Long) {
    DAIRY("Dairy", "🥛", 0xFF42A5F5),
    GLUTEN("Gluten", "🌾", 0xFFFFB300),
    EGGS("Eggs", "🥚", 0xFFFFCA28),
    PEANUTS("Peanuts", "🥜", 0xFF8D6E63),
    TREE_NUTS("Tree Nuts", "🌰", 0xFF6D4C41),
    SHELLFISH("Shellfish", "🦐", 0xFFFF7043),
    FISH("Fish", "🐟", 0xFF26A69A),
    SOY("Soy", "🫘", 0xFF9CCC65),
    SESAME("Sesame", "🌱", 0xFFD4E157)
}

enum class DifficultyLevel(val label: String, val colorHex: Long) {
    BEGINNER("Beginner (Easy)", 0xFF4CAF50),
    INTERMEDIATE("Intermediate", 0xFFFF9800),
    ADVANCED("Advanced Chef", 0xFFF44336),
    MASTERCLASS("Masterclass", 0xFF9C27B0)
}

enum class SkillTag(val label: String) {
    KNIFE_SKILLS("Knife Skills"),
    SLOW_BRAISE("Slow Braise"),
    SOUS_VIDE("Sous-Vide"),
    EMULSION("Emulsion / Sauces"),
    BAKING("Artisan Baking"),
    SEARING("Cast Iron Sear"),
    FERMENTATION("Fermentation"),
    SMOKING("Post Oak Smoking")
}

enum class MediaType {
    IMAGE,
    VIDEO
}

data class Ingredient(
    val name: String,
    val amount: String = "1",
    val unit: String = "serving",
    val allergen: Allergen? = null
)

data class NutritionInfo(
    val calories: Int = 0,
    val protein: Int = 0, // grams
    val carbs: Int = 0,   // grams
    val fat: Int = 0      // grams
)

data class CustomIngredientItem(
    val id: String,
    val name: String,
    val category: String,
    val caloriesPer100g: Int,
    val proteinPer100g: Int,
    val carbsPer100g: Int,
    val fatPer100g: Int,
    val allergens: List<Allergen> = emptyList(),
    val defaultServing: String = "100g"
)

data class CoAuthor(
    val userId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String = "",
    val isVerified: Boolean = false
)

data class RecipeVersion(
    val versionNumber: String,
    val timestamp: Long,
    val editorUsername: String,
    val changelogNote: String,
    val ingredientsSummary: String,
    val cookTimeMinutes: Int
)

data class SubstituteOption(
    val substituteName: String,
    val ratio: String,
    val notes: String,
    val dietaryBenefit: String
)

data class RecipeReview(
    val id: String,
    val recipeId: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String = "",
    val isAuthorVerified: Boolean = false,
    val rating: Int, // 1 to 5
    val comment: String,
    val timestamp: Long = System.currentTimeMillis(),
    val helpfulCount: Int = 0
)

data class RecipeCollection(
    val id: String,
    val title: String,
    val description: String,
    val creatorId: String,
    val creatorUsername: String,
    val coverGradientId: Int = 1,
    val recipeIds: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val followersCount: Int = 0
)

enum class PartnerType {
    MEAL_KIT,
    RESTAURANT
}

data class MealKitPartner(
    val id: String,
    val name: String,
    val type: PartnerType,
    val title: String,
    val description: String,
    val price: String,
    val deliveryTimeOrLocation: String,
    val promoCode: String,
    val discountDescription: String
)

enum class DeliveryProvider(val displayName: String, val iconEmoji: String, val deliveryFee: String) {
    INSTACART("Instacart Express", "🥕", "$3.99 or Free with Plus"),
    WHOLE_FOODS("Amazon Fresh / Whole Foods", "🌿", "Free 2-Hour Prime"),
    HEB("H-E-B Curbside & Delivery", "🍎", "$4.95 Same-Day"),
    KROGER("Kroger Delivery", "🛒", "$6.95 Direct Fleet")
}

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val city: String,
    val avatarUrl: String = "",
    val bio: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isPremium: Boolean = false,
    val isVerified: Boolean = false,
    val totalLikesReceived: Int = 0,
    val isFollowing: Boolean = false,
    val personalAllergens: List<Allergen> = listOf(Allergen.DAIRY, Allergen.PEANUTS) // Configurable in profile
) {
    val chefFlowers: Int
        get() = totalLikesReceived / 250

    val hasVerificationBadge: Boolean
        get() = isVerified || isPremium || followersCount >= 10000
}

data class Recipe(
    val id: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String = "",
    val isAuthorVerified: Boolean = false,
    val isAuthorPremium: Boolean = false,
    val title: String,
    val description: String = "",
    val cookTimeMinutes: Int = 25,
    val servings: Int = 2,
    val mediaType: MediaType = MediaType.IMAGE,
    val mediaUrl: String = "",
    val videoDurationSeconds: Int = 0,
    val thumbnailCoverUrl: String = "",
    val foodArtGradientId: Int = 0,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val tags: List<DietaryTag> = emptyList(),
    val difficulty: DifficultyLevel = DifficultyLevel.INTERMEDIATE,
    val skillTags: List<SkillTag> = emptyList(),
    val allergens: List<Allergen> = emptyList(),
    val coAuthors: List<CoAuthor> = emptyList(),
    val versions: List<RecipeVersion> = emptyList(),
    val ratingAverage: Float = 4.8f,
    val ratingCount: Int = 18,
    val stepTimersMinutes: List<Int?> = emptyList(),
    val mealKitPartner: MealKitPartner? = null,
    val privacyLevel: PrivacyLevel = PrivacyLevel.PUBLIC,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val shareCount: Int = 0,
    val originalPosterUsername: String? = null,
    val originalPosterVerified: Boolean = false,
    val originalRecipeId: String? = null,
    val nutrition: NutritionInfo? = null,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val isLikedByMe: Boolean = false,
    val isSavedByMe: Boolean = false
) {
    val isReshared: Boolean
        get() = !originalPosterUsername.isNullOrBlank()
}

data class Comment(
    val id: String,
    val recipeId: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String = "",
    val isAuthorVerified: Boolean = false,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Story(
    val id: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarUrl: String = "",
    val isAuthorVerified: Boolean = false,
    val mediaUrl: String = "",
    val textOverlay: String = "",
    val resharedRecipe: Recipe? = null,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val expiresAtTimestamp: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
    val viewerUserIds: List<String> = emptyList()
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAtTimestamp
}

enum class ActivityType {
    LIKE,
    COMMENT,
    FOLLOW,
    SHARE,
    COLLABORATION,
    CHEF_FLOWER_MILESTONE,
    VERIFICATION_UPDATE,
    REVIEW
}

data class ActivityItem(
    val id: String,
    val type: ActivityType,
    val actorUsername: String,
    val actorAvatarUrl: String = "",
    val isActorVerified: Boolean = false,
    val recipeId: String? = null,
    val recipeTitle: String? = null,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val originalPosterUsername: String? = null
)

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderUsername: String,
    val text: String,
    val recipeCard: Recipe? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = true
)

data class Conversation(
    val id: String,
    val otherUser: User,
    val lastMessage: String,
    val lastTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)

data class ShoppingItem(
    val id: String,
    val name: String,
    val amount: String,
    val unit: String,
    val isChecked: Boolean = false,
    val sourceRecipeTitle: String? = null,
    val estimatedPriceUsd: Double = 3.49
)

enum class MealSlot(val label: String, val iconName: String) {
    BREAKFAST("Breakfast", "wb_sunny"),
    LUNCH("Lunch", "restaurant"),
    DINNER("Dinner", "dinner_dining"),
    SNACK("Snack", "cookie")
}

data class MealPlanEntry(
    val id: String,
    val dayOfWeek: Int, // 0 = Monday, 6 = Sunday
    val slot: MealSlot,
    val recipe: Recipe
)

data class MealPlanTemplate(
    val id: String,
    val name: String,
    val description: String = "",
    val entries: List<MealPlanEntry>
)
