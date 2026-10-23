package com.example.data.model

enum class PrivacyLevel(val label: String, val description: String) {
    PUBLIC("Public", "Visible to all Savor foodies"),
    FOLLOWERS_ONLY("Followers Only", "Visible only to your followers"),
    FRIENDS_ONLY("Friends-Only", "Visible only to mutual foodie friends"),
    CUSTOM_LIST("Custom Access List", "Only specific selected foodies"),
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

enum class DeliveryProvider(
    val displayName: String,
    val iconEmoji: String,
    val deliveryFee: String,
    val localRegionLabel: String,
    val matchingKeywords: List<String>
) {
    HEB("H-E-B Curbside & Delivery", "🍎", "$4.95 Same-Day", "Texas Exclusive", listOf("Austin", "Houston", "San Antonio", "Dallas", "Fort Worth", "TX", "Texas")),
    CENTRAL_MARKET("Central Market", "🥖", "$4.99 Local Gourmet", "Texas Exclusive", listOf("Austin", "Dallas", "Fort Worth", "San Antonio", "Houston", "TX", "Texas")),
    WHOLE_FOODS("Whole Foods Market", "🌿", "Free 2-Hour Prime", "US & Canada", listOf("US", "USA", "TX", "Austin", "New York", "NY", "CA", "Los Angeles", "Chicago", "Canada")),
    INSTACART("Instacart Express", "🥕", "$3.99 Same-Day", "US & Canada", listOf("US", "USA", "TX", "Austin", "NY", "CA", "IL", "Canada")),
    TRADER_JOES("Trader Joe's Delivery Partner", "🌺", "$5.00 Flat Rate", "US Nationwide", listOf("US", "USA", "TX", "Austin", "NY", "CA", "FL", "WA", "IL")),
    FRESH_DIRECT("FreshDirect", "🍏", "$5.99 Next-Day", "NY Metro Area", listOf("New York", "NY", "NYC", "Brooklyn", "Queens", "Jersey", "NJ")),
    TESCO("Tesco Grocery", "🛒", "£3.50 Same-Day", "United Kingdom", listOf("London", "UK", "United Kingdom", "Manchester", "Birmingham", "Edinburgh")),
    SAINSBURYS("Sainsbury's Online", "🍊", "£4.00 Delivery", "United Kingdom", listOf("London", "UK", "United Kingdom", "Manchester", "Glasgow")),
    CARREFOUR("Carrefour Express", "🥐", "€3.90 Express", "France & Europe", listOf("Paris", "France", "Lyon", "Marseille", "EU", "Europe")),
    SEVEN_AND_I("Seven & i Net Super", "🍙", "¥550 Net Delivery", "Japan", listOf("Tokyo", "Japan", "Osaka", "Kyoto"));

    companion object {
        fun forLocation(location: String): List<DeliveryProvider> {
            val loc = location.trim().lowercase()
            val matches = entries.filter { provider ->
                provider.matchingKeywords.any { keyword ->
                    loc.contains(keyword.lowercase())
                }
            }
            return if (matches.isNotEmpty()) matches else listOf(INSTACART, WHOLE_FOODS)
        }
    }
}

enum class BadgeStatus(val label: String) {
    ACTIVE("Active Verified"),
    PENDING_AUDIT("Pending Periodic Audit"),
    EXPIRED("Expired (Subscription Ended)"),
    REVOKED("Revoked (Threshold Not Met)")
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
    val badgeStatus: BadgeStatus = BadgeStatus.ACTIVE,
    val badgeRevocationReason: String? = null,
    val totalLikesReceived: Int = 0,
    val isFollowing: Boolean = false,
    val personalAllergens: List<Allergen> = listOf(Allergen.DAIRY, Allergen.PEANUTS) // Configurable in profile
) {
    val chefFlowers: Int
        get() = totalLikesReceived / 250

    val hasVerificationBadge: Boolean
        get() = badgeStatus == BadgeStatus.ACTIVE && (isVerified || isPremium || followersCount >= 10000)
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
    val customAllowedUsernames: List<String> = emptyList(),
    val videoAnalytics: VideoAnalytics? = null,
    val videoTranscodingQuality: VideoQuality = VideoQuality.AUTO,
    val videoFilterApplied: VideoFilter = VideoFilter.ORIGINAL,
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

// --- Localization & Languages ---
enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val flag: String) {
    ENGLISH("en", "English", "English (US)", "🇺🇸"),
    SPANISH("es", "Spanish", "Español", "🇪🇸"),
    FRENCH("fr", "French", "Français", "🇫🇷"),
    ITALIAN("it", "Italian", "Italiano", "🇮🇹"),
    JAPANESE("ja", "Japanese", "日本語", "🇯🇵")
}

// --- Video Filters & Editing ---
enum class VideoFilter(val displayName: String, val subtitle: String, val tintColorHex: Long) {
    ORIGINAL("Natural Kitchen", "Clean balanced tones", 0xFFFFFFFF),
    WARM_VINTAGE("Warm Vintage", "Rustic bistro glow", 0xFFFFE0B2),
    GOLDEN_SUNSET("Golden Sunset", "Amber sear highlight", 0xFFFFD54F),
    CRISP_CLARITY("Crisp Whites", "Modern high-key lighting", 0xFFE0F7FA),
    HERB_VIBRANT("Fresh Herb", "Boosted greens & produce", 0xFFC8E6C9),
    ARTISAN_NOIR("Artisan Noir", "Monochrome contrast", 0xFF757575)
}

enum class VideoQuality(val label: String, val resolution: String, val bandwidthSaved: String) {
    AUTO("Auto Adaptive", "Dynamic 1080p/720p", "Best for connection"),
    FHD_1080("1080p Full HD", "1920x1080 60fps", "Uncompressed Master"),
    HD_720("720p HD", "1280x720 30fps", "Saved 35% Data"),
    DATA_SAVER("480p Data Saver", "854x480 30fps", "Saved 65% Data")
}

data class VideoEditSettings(
    val filter: VideoFilter = VideoFilter.ORIGINAL,
    val speedMultiplier: Float = 1.0f,
    val startTrimSeconds: Float = 0.0f,
    val endTrimSeconds: Float = 45.0f,
    val brightness: Float = 1.0f,
    val saturation: Float = 1.0f,
    val audioEnabled: Boolean = true,
    val quality: VideoQuality = VideoQuality.AUTO
)

data class VideoAnalytics(
    val totalViews: Int = 2480,
    val avgWatchSeconds: Float = 36.4f,
    val totalDurationSeconds: Int = 45,
    val completionRatePercent: Float = 76.8f,
    val rewatchCount: Int = 412,
    val peakEngagementTimestamp: String = "0:22 (Sear & Sizzle)",
    val retentionCurve: List<Float> = listOf(100f, 94f, 88f, 83f, 79f, 76f, 73f)
)

// --- Live Streaming ---
data class LiveChatMessage(
    val id: String,
    val senderUsername: String,
    val senderAvatarUrl: String = "",
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isHost: Boolean = false
)

data class LiveStreamSession(
    val id: String,
    val hostId: String,
    val hostUsername: String,
    val hostDisplayName: String,
    val hostAvatarUrl: String = "",
    val isHostVerified: Boolean = false,
    val title: String,
    val recipeTitle: String? = null,
    val recipeId: String? = null,
    val viewersCount: Int = 384,
    val isLive: Boolean = true,
    val likesCount: Int = 1820,
    val chatComments: List<LiveChatMessage> = emptyList(),
    val startedAt: Long = System.currentTimeMillis() - 12 * 60 * 1000L
)

// --- Admin & Moderation ---
enum class ReportReason(val label: String) {
    INAPPROPRIATE_CONTENT("Inappropriate Content / Language"),
    RECIPE_PLAGIARISM("Plagiarized / Stolen Recipe"),
    ALLERGEN_MISLABEL("Severe Allergen Mislabeling"),
    SPAM_ADVERTISING("Spam or Unauthorized Commercial Ads"),
    COMMUNITY_SAFETY("Harassment or Community Guideline Violation")
}

enum class ReportStatus(val label: String) {
    PENDING("Pending Review"),
    RESOLVED_REMOVED("Recipe Removed"),
    RESOLVED_WARNED("Chef Warned"),
    DISMISSED("Dismissed / Compliant")
}

data class ModerationReport(
    val id: String,
    val reportedRecipeId: String,
    val reportedRecipeTitle: String,
    val reportedAuthorUsername: String,
    val reporterUsername: String,
    val reason: ReportReason,
    val details: String,
    val status: ReportStatus = ReportStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

// --- Group Messaging ---
data class GroupConversation(
    val id: String,
    val name: String,
    val description: String = "",
    val iconEmoji: String = "🍳",
    val memberUserIds: List<String>,
    val memberUsernames: List<String>,
    val lastMessage: String,
    val lastTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0
)

// --- Verification Badge Audit ---
data class VerificationAuditLog(
    val userId: String,
    val username: String,
    val status: BadgeStatus,
    val followersCount: Int,
    val isPremium: Boolean,
    val reason: String,
    val auditedAt: Long = System.currentTimeMillis()
)

// --- Refunds & Gifts ---
enum class RefundReason(val label: String) {
    ACCIDENTAL_PURCHASE("Accidental purchase"),
    NOT_USING_FEATURES("Not using premium tools enough"),
    BILLING_INQUIRY("Billing adjustment or account change"),
    DISSATISFIED("Content did not meet expectations")
}

data class SubscriptionRefund(
    val refundId: String,
    val planName: String = "Savor Pro Chef",
    val amountUsd: Double = 9.99,
    val reason: RefundReason,
    val requestedAt: Long = System.currentTimeMillis(),
    val status: String = "PROCESSED",
    val confirmationCode: String
)

data class PromoCodeItem(
    val code: String,
    val discountDescription: String,
    val freeDaysOrMonths: String,
    val isRedeemed: Boolean = false
)

// --- SMS Notifications ---
data class SmsNotificationSettings(
    val phoneNumber: String = "+1 (512) 555-0199",
    val isSmsAlertsEnabled: Boolean = false,
    val notifyCookingTimers: Boolean = true,
    val notifyLiveStreams: Boolean = true,
    val notifyCoAuthorInvites: Boolean = true
)

