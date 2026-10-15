package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class SavorRepository {

    // Current Authenticated User
    private val _currentUser = MutableStateFlow(
        User(
            id = "user_me",
            username = "chef_elena",
            displayName = "Elena Rostova",
            email = "elena@savor.app",
            city = "Austin, TX",
            avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150",
            bio = "Culinary explorer & food storyteller in Austin 🌶️ Artisan tacos & sourdough enthusiast.",
            followersCount = 3850,
            followingCount = 42,
            isPremium = false,
            isVerified = false,
            totalLikesReceived = 1420, // 5 Chef Flowers
            personalAllergens = listOf(Allergen.DAIRY, Allergen.PEANUTS)
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Community Users
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Recipes Feed
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    // Recipe Collections / Playlists
    private val _collections = MutableStateFlow<List<RecipeCollection>>(emptyList())
    val collections: StateFlow<List<RecipeCollection>> = _collections.asStateFlow()

    // Recipe Reviews: recipeId -> List<RecipeReview>
    private val _reviews = MutableStateFlow<Map<String, List<RecipeReview>>>(emptyMap())
    val reviews: StateFlow<Map<String, List<RecipeReview>>> = _reviews.asStateFlow()

    // Custom Ingredient Database (Nutritional database management)
    private val _customIngredients = MutableStateFlow<List<CustomIngredientItem>>(emptyList())
    val customIngredients: StateFlow<List<CustomIngredientItem>> = _customIngredients.asStateFlow()

    // Active Stories
    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    // Activity Notifications
    private val _activities = MutableStateFlow<List<ActivityItem>>(emptyList())
    val activities: StateFlow<List<ActivityItem>> = _activities.asStateFlow()

    // Direct Messages & Conversations
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    // Comments map: recipeId -> List<Comment>
    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<Comment>>> = _comments.asStateFlow()

    // Shopping List
    private val _shoppingList = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shoppingList: StateFlow<List<ShoppingItem>> = _shoppingList.asStateFlow()

    // Meal Plan Entries
    private val _mealPlan = MutableStateFlow<List<MealPlanEntry>>(emptyList())
    val mealPlan: StateFlow<List<MealPlanEntry>> = _mealPlan.asStateFlow()

    // Saved Meal Plan Templates
    private val _templates = MutableStateFlow<List<MealPlanTemplate>>(emptyList())
    val templates: StateFlow<List<MealPlanTemplate>> = _templates.asStateFlow()

    // App Preferences
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _pushSettings = MutableStateFlow(
        mapOf(
            ActivityType.LIKE to true,
            ActivityType.COMMENT to true,
            ActivityType.FOLLOW to true,
            ActivityType.SHARE to true,
            ActivityType.COLLABORATION to true,
            ActivityType.CHEF_FLOWER_MILESTONE to true,
            ActivityType.VERIFICATION_UPDATE to true,
            ActivityType.REVIEW to true
        )
    )
    val pushSettings: StateFlow<Map<ActivityType, Boolean>> = _pushSettings.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val userMarco = User(
            id = "user_marco",
            username = "marco_roma",
            displayName = "Marco Silvestri",
            email = "marco@savor.app",
            city = "Austin, TX",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
            bio = "Michelin-trained pasta master bringing authentic Roman trattoria flavors to Austin.",
            followersCount = 14200,
            followingCount = 120,
            isPremium = true,
            isVerified = true,
            totalLikesReceived = 8500, // 34 Chef Flowers
            isFollowing = true,
            personalAllergens = listOf(Allergen.SHELLFISH)
        )

        val userJin = User(
            id = "user_jin",
            username = "sushi_jin",
            displayName = "Chef Jin Tanaka",
            email = "jin@savor.app",
            city = "Austin, TX",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
            bio = "Edomae sushi artisan & knife technique mentor. Omotenashi philosophy.",
            followersCount = 21500,
            followingCount = 85,
            isPremium = true,
            isVerified = true,
            totalLikesReceived = 12500, // 50 Chef Flowers
            isFollowing = true
        )

        val userClara = User(
            id = "user_clara",
            username = "clara_pastry",
            displayName = "Clara Dupont",
            email = "clara@savor.app",
            city = "New York, NY",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
            bio = "Parisian viennoiserie, laminated pastries, and modern sourdough creations.",
            followersCount = 18900,
            followingCount = 210,
            isPremium = true,
            isVerified = true,
            totalLikesReceived = 9250, // 37 Chef Flowers
            isFollowing = false
        )

        val userTaco = User(
            id = "user_taco",
            username = "taco_artisan",
            displayName = "Mateo Gutierrez",
            email = "mateo@savor.app",
            city = "Austin, TX",
            avatarUrl = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150",
            bio = "Heirloom corn nixtamalization, wood-smoked barbacoa, and salsa macha lab.",
            followersCount = 8400,
            followingCount = 310,
            isPremium = false,
            isVerified = false,
            totalLikesReceived = 4750, // 19 Chef Flowers
            isFollowing = true
        )

        val userGreen = User(
            id = "user_green",
            username = "green_bowl",
            displayName = "Aria Chen",
            email = "aria@savor.app",
            city = "Austin, TX",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
            bio = "Plant-powered nutritionist crafting vibrant bowls, fermented tonics, and seed milks.",
            followersCount = 3100,
            followingCount = 95,
            isPremium = false,
            isVerified = false,
            totalLikesReceived = 1500, // 6 Chef Flowers
            isFollowing = false
        )

        _users.value = listOf(userMarco, userJin, userClara, userTaco, userGreen)

        // Seed Nutritional Database / Custom Ingredients
        _customIngredients.value = listOf(
            CustomIngredientItem(
                id = "ing_1",
                name = "Wild Alaskan Sockeye Salmon",
                category = "Seafood",
                caloriesPer100g = 216,
                proteinPer100g = 27,
                carbsPer100g = 0,
                fatPer100g = 11,
                allergens = listOf(Allergen.FISH),
                defaultServing = "170g fillet"
            ),
            CustomIngredientItem(
                id = "ing_2",
                name = "Grass-Fed European Butter",
                category = "Dairy",
                caloriesPer100g = 717,
                proteinPer100g = 1,
                carbsPer100g = 1,
                fatPer100g = 81,
                allergens = listOf(Allergen.DAIRY),
                defaultServing = "14g (1 tbsp)"
            ),
            CustomIngredientItem(
                id = "ing_3",
                name = "Ceremonial Uji Matcha",
                category = "Pantry",
                caloriesPer100g = 324,
                proteinPer100g = 30,
                carbsPer100g = 39,
                fatPer100g = 5,
                allergens = emptyList(),
                defaultServing = "2g (1 tsp)"
            ),
            CustomIngredientItem(
                id = "ing_4",
                name = "Greek Sheep & Goat Feta",
                category = "Dairy",
                caloriesPer100g = 264,
                proteinPer100g = 14,
                carbsPer100g = 4,
                fatPer100g = 21,
                allergens = listOf(Allergen.DAIRY),
                defaultServing = "30g"
            ),
            CustomIngredientItem(
                id = "ing_5",
                name = "Organic Black Winter Truffle Butter",
                category = "Gourmet",
                caloriesPer100g = 720,
                proteinPer100g = 1,
                carbsPer100g = 2,
                fatPer100g = 80,
                allergens = listOf(Allergen.DAIRY),
                defaultServing = "15g"
            )
        )

        // Seed Recipes with Versions, Co-Authors, Allergens, Difficulty, Timers, and Partners
        val recipe1 = Recipe(
            id = "recipe_salmon",
            authorId = userMarco.id,
            authorUsername = userMarco.username,
            authorAvatarUrl = userMarco.avatarUrl,
            isAuthorVerified = true,
            isAuthorPremium = true,
            title = "Tuscan Sun-Dried Tomato Salmon Reel",
            description = "Crispy pan-seared wild salmon simmered in a velvety garlic, spinach, and sun-dried tomato sauce. Ready in 20 minutes!",
            cookTimeMinutes = 20,
            servings = 2,
            mediaType = MediaType.VIDEO,
            videoDurationSeconds = 45,
            foodArtGradientId = 1,
            difficulty = DifficultyLevel.INTERMEDIATE,
            skillTags = listOf(SkillTag.SEARING, SkillTag.EMULSION),
            allergens = listOf(Allergen.FISH, Allergen.DAIRY),
            coAuthors = listOf(
                CoAuthor(userMarco.id, userMarco.username, userMarco.displayName, userMarco.avatarUrl, true),
                CoAuthor(_currentUser.value.id, _currentUser.value.username, _currentUser.value.displayName, _currentUser.value.avatarUrl, false)
            ),
            versions = listOf(
                RecipeVersion("v1.0", System.currentTimeMillis() - 7 * 86400000L, "marco_roma", "Initial Roman coastal recipe", "Salmon, Cream, Garlic, Spinach", 22),
                RecipeVersion("v1.1", System.currentTimeMillis() - 2 * 86400000L, "chef_elena", "Adjusted sun-dried tomato acidity and added chili flake finish", "Salmon, Cream, Garlic, Spinach, Sun-dried tomatoes, Chili", 20)
            ),
            ratingAverage = 4.9f,
            ratingCount = 42,
            stepTimersMinutes = listOf(null, 4, 2, 5, 2),
            mealKitPartner = MealKitPartner(
                id = "partner_salmon_kit",
                name = "Farm-to-Table Fresh",
                type = PartnerType.MEAL_KIT,
                title = "Order Pre-Portioned Tuscan Salmon Kit",
                description = "Get wild Alaskan salmon, fresh herb bundle, and cream coulis delivered to your doorstep.",
                price = "$24.99 (Serves 2)",
                deliveryTimeOrLocation = "Same-Day Delivery in Austin",
                promoCode = "SAVORSALMON20",
                discountDescription = "20% off your first culinary delivery kit"
            ),
            ingredients = listOf(
                Ingredient("Wild Salmon Fillets", "2", "fillets", Allergen.FISH),
                Ingredient("Olive oil", "2", "tbsp"),
                Ingredient("Garlic", "4", "cloves"),
                Ingredient("Spinach", "2", "cups"),
                Ingredient("Tomato", "0.5", "cup"),
                Ingredient("Heavy Cream", "0.5", "cup", Allergen.DAIRY),
                Ingredient("Parmesan Cheese", "0.25", "cup", Allergen.DAIRY)
            ),
            steps = listOf(
                "Pat salmon fillets dry and season generously with sea salt and cracked black pepper.",
                "Sear skin-side down in hot olive oil for 4 minutes until golden and crisp, then flip for 2 minutes.",
                "Remove salmon, saute minced garlic and sun-dried tomatoes until fragrant.",
                "Pour in cream, fold in fresh baby spinach and parmesan cheese until reduced to a silky sauce.",
                "Return salmon to the skillet, spoon pan sauce over the top, and finish with fresh basil."
            ),
            tags = listOf(DietaryTag.KETO, DietaryTag.GLUTEN_FREE, DietaryTag.HIGH_PROTEIN),
            privacyLevel = PrivacyLevel.PUBLIC,
            likesCount = 428,
            commentsCount = 36,
            shareCount = 18,
            nutrition = NutritionInfo(calories = 490, protein = 38, carbs = 9, fat = 34),
            isLikedByMe = true,
            isSavedByMe = true
        )

        val recipe2 = Recipe(
            id = "recipe_matcha_donut",
            authorId = _currentUser.value.id,
            authorUsername = _currentUser.value.username,
            authorAvatarUrl = _currentUser.value.avatarUrl,
            isAuthorVerified = false,
            isAuthorPremium = false,
            title = "Matcha Glazed Brioche Donuts",
            description = "Pillow-soft brioche donuts crowned with an emerald Uji matcha glaze and edible gold flakes.",
            cookTimeMinutes = 45,
            servings = 6,
            mediaType = MediaType.IMAGE,
            foodArtGradientId = 2,
            difficulty = DifficultyLevel.ADVANCED,
            skillTags = listOf(SkillTag.BAKING, SkillTag.EMULSION),
            allergens = listOf(Allergen.GLUTEN, Allergen.DAIRY, Allergen.EGGS),
            coAuthors = listOf(
                CoAuthor(userClara.id, userClara.username, userClara.displayName, userClara.avatarUrl, true)
            ),
            versions = listOf(
                RecipeVersion("v1.0", System.currentTimeMillis() - 10 * 86400000L, "clara_pastry", "Parisian brioche base", "Bread flour, Butter, Eggs, Milk", 50),
                RecipeVersion("v2.0", System.currentTimeMillis() - 3 * 86400000L, "chef_elena", "Infused with ceremonial Kyoto matcha glaze", "Bread flour, Butter, Eggs, Milk, Uji Matcha", 45)
            ),
            ratingAverage = 4.8f,
            ratingCount = 28,
            stepTimersMinutes = listOf(90, 30, 3, 2, 5),
            originalPosterUsername = userClara.username,
            originalPosterVerified = true,
            originalRecipeId = "orig_matcha_1",
            ingredients = listOf(
                Ingredient("Bread flour", "3", "cups", Allergen.GLUTEN),
                Ingredient("Butter", "6", "tbsp", Allergen.DAIRY),
                Ingredient("Egg", "2", "large", Allergen.EGGS),
                Ingredient("Milk", "0.75", "cup", Allergen.DAIRY),
                Ingredient("Honey", "2", "tbsp"),
                Ingredient("Uji Matcha Powder", "2", "tbsp")
            ),
            steps = listOf(
                "Knead rich brioche dough until smooth and elastic, proof for 1.5 hours until doubled in volume.",
                "Roll out and punch into donut rings; allow a second 30-minute rise on parchment.",
                "Fry gently in vegetable oil at 350°F (175°C) for 90 seconds per side until golden puff.",
                "Whisk ceremonial matcha with powdered sugar and warm milk into a glossy glaze.",
                "Dip warm donuts deeply into the matcha glaze and set on wire rack to cool."
            ),
            tags = listOf(DietaryTag.VEGETARIAN),
            privacyLevel = PrivacyLevel.PUBLIC,
            likesCount = 312,
            commentsCount = 24,
            shareCount = 45,
            nutrition = NutritionInfo(calories = 310, protein = 6, carbs = 42, fat = 14)
        )

        val recipe3 = Recipe(
            id = "recipe_brisket_tacos",
            authorId = userTaco.id,
            authorUsername = userTaco.username,
            authorAvatarUrl = userTaco.avatarUrl,
            isAuthorVerified = false,
            isAuthorPremium = false,
            title = "Smoky Austin Brisket Tacos",
            description = "14-hour post oak smoked brisket folded inside handmade heirloom tortillas with avocado cream and pickled red onions.",
            cookTimeMinutes = 35,
            servings = 4,
            mediaType = MediaType.VIDEO,
            videoDurationSeconds = 38,
            foodArtGradientId = 3,
            difficulty = DifficultyLevel.INTERMEDIATE,
            skillTags = listOf(SkillTag.SMOKING, SkillTag.KNIFE_SKILLS),
            allergens = emptyList(), // Gluten-Free & Dairy-Free!
            coAuthors = emptyList(),
            versions = listOf(
                RecipeVersion("v1.0", System.currentTimeMillis() - 5 * 86400000L, "taco_artisan", "Signature Austin food truck release", "Beef brisket, Avocado, Corn Tortillas", 35)
            ),
            ratingAverage = 5.0f,
            ratingCount = 64,
            stepTimersMinutes = listOf(3, 2, 2, 1),
            mealKitPartner = MealKitPartner(
                id = "partner_bbq_spot",
                name = "Trattoria & Smokehouse ATX",
                type = PartnerType.RESTAURANT,
                title = "Dine & Taste Live at Artisan Comal",
                description = "Visit our partner kitchen on East 6th St in Austin. Show your Savor app for free craft beverage.",
                price = "$16 / Plate",
                deliveryTimeOrLocation = "1204 E 6th St, Austin, TX",
                promoCode = "SAVORATX",
                discountDescription = "Complimentary smoked salsa trio with any taco plate"
            ),
            ingredients = listOf(
                Ingredient("Beef brisket", "1", "lb"),
                Ingredient("Avocado", "2", "medium"),
                Ingredient("Onion", "1", "medium"),
                Ingredient("Garlic", "3", "cloves"),
                Ingredient("Lime", "2", "fruit"),
                Ingredient("Cilantro", "1", "bunch")
            ),
            steps = listOf(
                "Slice tender smoked brisket into bite-sized morsels and crisp briefly on a smoking hot comal.",
                "Mash ripe avocados with lime juice, minced garlic, sea salt, and a splash of Mexican crema.",
                "Warm pressed heirloom corn tortillas directly over an open flame until speckled.",
                "Layer generous mounds of brisket, dollop with avocado cream, pickled onions, and cotija."
            ),
            tags = listOf(DietaryTag.KETO, DietaryTag.GLUTEN_FREE, DietaryTag.HIGH_PROTEIN),
            privacyLevel = PrivacyLevel.PUBLIC,
            likesCount = 590,
            commentsCount = 52,
            shareCount = 84,
            nutrition = NutritionInfo(calories = 420, protein = 29, carbs = 18, fat = 26),
            isLikedByMe = true
        )

        val recipe4 = Recipe(
            id = "recipe_lentil_dahl",
            authorId = userGreen.id,
            authorUsername = userGreen.username,
            authorAvatarUrl = userGreen.avatarUrl,
            isAuthorVerified = false,
            isAuthorPremium = false,
            title = "Golden Turmeric Lentil Dahl",
            description = "Soul-warming red lentils simmered with coconut milk, freshly grated turmeric root, cumin seeds, and tender spinach.",
            cookTimeMinutes = 25,
            servings = 4,
            mediaType = MediaType.IMAGE,
            foodArtGradientId = 4,
            difficulty = DifficultyLevel.BEGINNER,
            skillTags = listOf(SkillTag.SLOW_BRAISE),
            allergens = emptyList(), // Completely allergen-free
            ratingAverage = 4.7f,
            ratingCount = 19,
            stepTimersMinutes = listOf(2, 5, 1, 20),
            ingredients = listOf(
                Ingredient("Red Lentils", "1.5", "cups"),
                Ingredient("Coconut Milk", "1", "can"),
                Ingredient("Spinach", "2", "cups"),
                Ingredient("Garlic", "4", "cloves"),
                Ingredient("Onion", "1", "diced"),
                Ingredient("Tomato", "1", "cup")
            ),
            steps = listOf(
                "Sizzle cumin seeds and minced ginger in coconut oil until bursting with aroma.",
                "Add diced onions and garlic, cooking slowly until deep golden brown.",
                "Stir in red lentils, ground turmeric, crushed tomatoes, and rich coconut milk.",
                "Simmer gently for 20 minutes until creamy; fold in baby spinach until wilted."
            ),
            tags = listOf(DietaryTag.VEGAN, DietaryTag.GLUTEN_FREE, DietaryTag.DAIRY_FREE),
            privacyLevel = PrivacyLevel.PUBLIC,
            likesCount = 210,
            commentsCount = 19,
            shareCount = 12,
            nutrition = NutritionInfo(calories = 340, protein = 18, carbs = 48, fat = 9)
        )

        val recipe5 = Recipe(
            id = "recipe_feta_dip",
            authorId = userMarco.id,
            authorUsername = userMarco.username,
            authorAvatarUrl = userMarco.avatarUrl,
            isAuthorVerified = true,
            isAuthorPremium = true,
            title = "Whipped Feta & Roasted Garlic Dip",
            description = "Followers-only secret recipe! Silky whipped Greek feta with slow-roasted confit garlic cloves and hot honey drizzle.",
            cookTimeMinutes = 15,
            servings = 4,
            mediaType = MediaType.IMAGE,
            foodArtGradientId = 5,
            difficulty = DifficultyLevel.BEGINNER,
            skillTags = listOf(SkillTag.EMULSION),
            allergens = listOf(Allergen.DAIRY),
            ratingAverage = 4.9f,
            ratingCount = 31,
            stepTimersMinutes = listOf(35, 3, 1, 1),
            ingredients = listOf(
                Ingredient("Cheese (Feta)", "8", "oz", Allergen.DAIRY),
                Ingredient("Yogurt", "0.5", "cup", Allergen.DAIRY),
                Ingredient("Garlic", "1", "whole head"),
                Ingredient("Olive oil", "3", "tbsp"),
                Ingredient("Honey", "1.5", "tbsp")
            ),
            steps = listOf(
                "Roast whole garlic head in olive oil at 375°F until cloves are caramelized and buttery.",
                "Blend room-temperature feta cheese and Greek yogurt in food processor until ultra-smooth.",
                "Squeeze roasted garlic paste into the whipped feta and pulse until incorporated.",
                "Transfer to serving dish, create swirls with back of spoon, and top with hot chili honey."
            ),
            tags = listOf(DietaryTag.VEGETARIAN, DietaryTag.KETO, DietaryTag.LOW_CARB),
            privacyLevel = PrivacyLevel.FOLLOWERS_ONLY,
            likesCount = 340,
            commentsCount = 28,
            shareCount = 31,
            nutrition = NutritionInfo(calories = 260, protein = 9, carbs = 11, fat = 20)
        )

        val recipe6 = Recipe(
            id = "recipe_truffle_ribeye",
            authorId = userJin.id,
            authorUsername = userJin.username,
            authorAvatarUrl = userJin.avatarUrl,
            isAuthorVerified = true,
            isAuthorPremium = true,
            title = "A5 Wagyu Ribeye with Black Truffle Butter",
            description = "Exclusive Savor Premium masterclass recipe. Reverse-seared Wagyu with compound black winter truffle butter.",
            cookTimeMinutes = 30,
            servings = 2,
            mediaType = MediaType.VIDEO,
            videoDurationSeconds = 52,
            foodArtGradientId = 6,
            difficulty = DifficultyLevel.MASTERCLASS,
            skillTags = listOf(SkillTag.SEARING, SkillTag.SOUS_VIDE, SkillTag.KNIFE_SKILLS),
            allergens = listOf(Allergen.DAIRY),
            ratingAverage = 5.0f,
            ratingCount = 89,
            stepTimersMinutes = listOf(45, 25, 4, 8),
            ingredients = listOf(
                Ingredient("Beef (Wagyu Ribeye)", "16", "oz"),
                Ingredient("Butter", "4", "tbsp", Allergen.DAIRY),
                Ingredient("Black Truffle", "1", "tbsp shaved"),
                Ingredient("Garlic", "2", "cloves"),
                Ingredient("Rosemary", "2", "sprigs")
            ),
            steps = listOf(
                "Bring steak to room temp for 45 minutes; season heavily with flake salt.",
                "Roast gently in low oven at 225°F until internal temp hits 118°F.",
                "Hard sear in screaming hot cast iron with foaming butter, rosemary, and smashed garlic.",
                "Rest for 8 minutes, slice thick against grain, and melt black truffle butter over hot slices."
            ),
            tags = listOf(DietaryTag.KETO, DietaryTag.HIGH_PROTEIN, DietaryTag.GLUTEN_FREE),
            privacyLevel = PrivacyLevel.PREMIUM_ONLY,
            likesCount = 780,
            commentsCount = 64,
            shareCount = 95,
            nutrition = NutritionInfo(calories = 620, protein = 44, carbs = 1, fat = 49)
        )

        _recipes.value = listOf(recipe1, recipe2, recipe3, recipe4, recipe5, recipe6)

        // Seed Recipe Playlists / Collections
        _collections.value = listOf(
            RecipeCollection(
                id = "col_1",
                title = "20-Minute Weeknight Wonders",
                description = "Rapid, restaurant-quality dishes designed for effortless after-work gourmet dinners.",
                creatorId = _currentUser.value.id,
                creatorUsername = _currentUser.value.username,
                coverGradientId = 1,
                recipeIds = listOf(recipe1.id, recipe4.id, recipe5.id),
                followersCount = 420
            ),
            RecipeCollection(
                id = "col_2",
                title = "Austin Fire Pit & Smoker Feast",
                description = "Post oak woods, charred salsa macha, and pitmaster secrets from Texas.",
                creatorId = userTaco.id,
                creatorUsername = userTaco.username,
                coverGradientId = 3,
                recipeIds = listOf(recipe3.id, recipe6.id),
                followersCount = 1250
            ),
            RecipeCollection(
                id = "col_3",
                title = "Master Chef Technique Series",
                description = "Laminated brioches, reverse sear A5 Wagyu, and delicate Roman emulsion sauces.",
                creatorId = userJin.id,
                creatorUsername = userJin.username,
                coverGradientId = 6,
                recipeIds = listOf(recipe2.id, recipe6.id, recipe1.id),
                followersCount = 2840
            )
        )

        // Seed Recipe Reviews
        _reviews.value = mapOf(
            recipe1.id to listOf(
                RecipeReview(
                    id = "rev_1",
                    recipeId = recipe1.id,
                    authorId = userJin.id,
                    authorUsername = userJin.username,
                    authorAvatarUrl = userJin.avatarUrl,
                    isAuthorVerified = true,
                    rating = 5,
                    comment = "The sear on the salmon skin along with the gentle acid of the sun-dried tomatoes is balanced to perfection.",
                    helpfulCount = 12
                ),
                RecipeReview(
                    id = "rev_2",
                    recipeId = recipe1.id,
                    authorId = userGreen.id,
                    authorUsername = userGreen.username,
                    authorAvatarUrl = userGreen.avatarUrl,
                    isAuthorVerified = false,
                    rating = 5,
                    comment = "Used coconut cream instead of dairy cream per the Savor substitution tool and it came out sublime!",
                    helpfulCount = 8
                )
            ),
            recipe3.id to listOf(
                RecipeReview(
                    id = "rev_3",
                    recipeId = recipe3.id,
                    authorId = _currentUser.value.id,
                    authorUsername = _currentUser.value.username,
                    authorAvatarUrl = _currentUser.value.avatarUrl,
                    isAuthorVerified = false,
                    rating = 5,
                    comment = "Cooked this with the interactive step timer on Saturday. The post oak aroma is legendary.",
                    helpfulCount = 15
                )
            )
        )

        // Seed Active Stories
        val now = System.currentTimeMillis()
        val story1 = Story(
            id = "story_marco",
            authorId = userMarco.id,
            authorUsername = userMarco.username,
            authorAvatarUrl = userMarco.avatarUrl,
            isAuthorVerified = true,
            mediaUrl = "story_pasta",
            textOverlay = "Fresh Tagliolini rolling on the marble counter this morning! 🍝✨",
            createdAtTimestamp = now - 2 * 3600 * 1000L,
            expiresAtTimestamp = now + 22 * 3600 * 1000L
        )

        val story2 = Story(
            id = "story_taco",
            authorId = userTaco.id,
            authorUsername = userTaco.username,
            authorAvatarUrl = userTaco.avatarUrl,
            isAuthorVerified = false,
            mediaUrl = "story_tacos",
            textOverlay = "Austin foodies: Fire pit is burning red oak! Come grab tacos! 🔥",
            resharedRecipe = recipe3,
            createdAtTimestamp = now - 5 * 3600 * 1000L,
            expiresAtTimestamp = now + 19 * 3600 * 1000L
        )

        val story3 = Story(
            id = "story_jin",
            authorId = userJin.id,
            authorUsername = userJin.username,
            authorAvatarUrl = userJin.avatarUrl,
            isAuthorVerified = true,
            mediaUrl = "story_sushi",
            textOverlay = "Tsukiji market fish selection just arrived via cold chain. 🍣🇯🇵",
            createdAtTimestamp = now - 1 * 3600 * 1000L,
            expiresAtTimestamp = now + 23 * 3600 * 1000L
        )

        _stories.value = listOf(story1, story2, story3)

        // Seed Initial Activity Notifications
        _activities.value = listOf(
            ActivityItem(
                id = "act_collab_1",
                type = ActivityType.COLLABORATION,
                actorUsername = userMarco.username,
                actorAvatarUrl = userMarco.avatarUrl,
                isActorVerified = true,
                recipeId = recipe1.id,
                recipeTitle = recipe1.title,
                message = "invited you as a Co-Author on 'Tuscan Sun-Dried Tomato Salmon Reel'",
                timestamp = now - 10 * 60 * 1000L,
                isRead = false
            ),
            ActivityItem(
                id = "act_rev_1",
                type = ActivityType.REVIEW,
                actorUsername = userJin.username,
                actorAvatarUrl = userJin.avatarUrl,
                isActorVerified = true,
                recipeId = recipe1.id,
                recipeTitle = recipe1.title,
                message = "left a 5-star review on your co-authored recipe",
                timestamp = now - 25 * 60 * 1000L,
                isRead = false
            ),
            ActivityItem(
                id = "act_share_1",
                type = ActivityType.SHARE,
                actorUsername = userTaco.username,
                actorAvatarUrl = userTaco.avatarUrl,
                isActorVerified = userTaco.isVerified,
                recipeId = recipe2.id,
                recipeTitle = recipe2.title,
                message = "reshared your recipe post to their story",
                timestamp = now - 45 * 60 * 1000L,
                isRead = false,
                originalPosterUsername = userClara.username
            ),
            ActivityItem(
                id = "act_flower_1",
                type = ActivityType.CHEF_FLOWER_MILESTONE,
                actorUsername = "Savor Kitchen",
                isActorVerified = true,
                message = "Congratulations! You earned your 5th Chef Flower from 1,250 likes!",
                timestamp = now - 2 * 3600 * 1000L,
                isRead = false
            ),
            ActivityItem(
                id = "act_like_1",
                type = ActivityType.LIKE,
                actorUsername = userMarco.username,
                actorAvatarUrl = userMarco.avatarUrl,
                isActorVerified = userMarco.isVerified,
                recipeId = recipe2.id,
                recipeTitle = recipe2.title,
                message = "liked your recipe Matcha Glazed Brioche Donuts",
                timestamp = now - 3 * 3600 * 1000L,
                isRead = true
            )
        )

        // Seed Direct Messages & Conversations
        val convoMarco = Conversation(
            id = "conv_marco",
            otherUser = userMarco,
            lastMessage = "Sent a recipe: Tuscan Sun-Dried Tomato Salmon Reel",
            lastTimestamp = now - 30 * 60 * 1000L,
            unreadCount = 1
        )
        val convoJin = Conversation(
            id = "conv_jin",
            otherUser = userJin,
            lastMessage = "Let me know when you visit Austin downtown!",
            lastTimestamp = now - 6 * 3600 * 1000L,
            unreadCount = 0
        )
        _conversations.value = listOf(convoMarco, convoJin)

        _messages.value = mapOf(
            "conv_marco" to listOf(
                ChatMessage(
                    id = "msg_1",
                    conversationId = "conv_marco",
                    senderId = userMarco.id,
                    senderUsername = userMarco.username,
                    text = "Ciao Elena! Loved your taco reel yesterday! Have you tried cooking salmon with sun-dried tomatoes?",
                    timestamp = now - 40 * 60 * 1000L
                ),
                ChatMessage(
                    id = "msg_2",
                    conversationId = "conv_marco",
                    senderId = userMarco.id,
                    senderUsername = userMarco.username,
                    text = "Check out this reel I posted today! The sauce is incredible.",
                    recipeCard = recipe1,
                    timestamp = now - 30 * 60 * 1000L,
                    isRead = false
                )
            ),
            "conv_jin" to listOf(
                ChatMessage(
                    id = "msg_3",
                    conversationId = "conv_jin",
                    senderId = userJin.id,
                    senderUsername = userJin.username,
                    text = "Elena, we are doing a chef collaboration dinner next month in Austin.",
                    timestamp = now - 12 * 3600 * 1000L
                ),
                ChatMessage(
                    id = "msg_4",
                    conversationId = "conv_jin",
                    senderId = _currentUser.value.id,
                    senderUsername = _currentUser.value.username,
                    text = "I would be honored! Let's feature Mexican nixtamal and Japanese crudo.",
                    timestamp = now - 8 * 3600 * 1000L
                ),
                ChatMessage(
                    id = "msg_5",
                    conversationId = "conv_jin",
                    senderId = userJin.id,
                    senderUsername = userJin.username,
                    text = "Let me know when you visit Austin downtown!",
                    timestamp = now - 6 * 3600 * 1000L
                )
            )
        )

        // Seed Comments
        _comments.value = mapOf(
            recipe1.id to listOf(
                Comment(
                    id = "c1",
                    recipeId = recipe1.id,
                    authorId = userJin.id,
                    authorUsername = userJin.username,
                    authorAvatarUrl = userJin.avatarUrl,
                    isAuthorVerified = true,
                    text = "The sear on that salmon skin is restaurant grade! Beautiful color.",
                    timestamp = now - 2 * 3600 * 1000L
                ),
                Comment(
                    id = "c2",
                    recipeId = recipe1.id,
                    authorId = _currentUser.value.id,
                    authorUsername = _currentUser.value.username,
                    authorAvatarUrl = _currentUser.value.avatarUrl,
                    isAuthorVerified = false,
                    text = "Making this for dinner tonight! Adding it straight to my shopping list.",
                    timestamp = now - 1 * 3600 * 1000L
                )
            )
        )

        // Seed Shopping List with estimated prices
        _shoppingList.value = listOf(
            ShoppingItem("shop_1", "Wild Salmon Fillets", "2", "fillets", false, recipe1.title, 12.99),
            ShoppingItem("shop_2", "Baby Spinach", "2", "cups", true, recipe1.title, 2.49),
            ShoppingItem("shop_3", "Avocados", "4", "medium", false, recipe3.title, 4.00),
            ShoppingItem("shop_4", "Uji Ceremonial Matcha", "1", "tin", false, recipe2.title, 18.50),
            ShoppingItem("shop_5", "Organic Coconut Milk", "2", "cans", true, recipe4.title, 3.98)
        )

        // Seed Weekly Meal Plan
        _mealPlan.value = listOf(
            MealPlanEntry("mp_1", 0, MealSlot.DINNER, recipe1),
            MealPlanEntry("mp_2", 1, MealSlot.LUNCH, recipe4),
            MealPlanEntry("mp_3", 2, MealSlot.DINNER, recipe3),
            MealPlanEntry("mp_4", 3, MealSlot.SNACK, recipe2),
            MealPlanEntry("mp_5", 4, MealSlot.DINNER, recipe5)
        )

        // Seed Meal Plan Templates
        _templates.value = listOf(
            MealPlanTemplate(
                id = "tmpl_1",
                name = "High-Protein Keto Week",
                description = "Nutritious salmon, wagyu ribeye, and smoked brisket with under 20g net carbs per day.",
                entries = listOf(
                    MealPlanEntry("t1", 0, MealSlot.DINNER, recipe1),
                    MealPlanEntry("t2", 1, MealSlot.DINNER, recipe3),
                    MealPlanEntry("t3", 2, MealSlot.LUNCH, recipe5),
                    MealPlanEntry("t4", 3, MealSlot.DINNER, recipe6)
                )
            ),
            MealPlanTemplate(
                id = "tmpl_2",
                name = "Plant & Seafood Balance",
                description = "Golden turmeric dahls, fresh wild salmon, and antioxidant matcha treats.",
                entries = listOf(
                    MealPlanEntry("t5", 0, MealSlot.LUNCH, recipe4),
                    MealPlanEntry("t6", 1, MealSlot.DINNER, recipe1),
                    MealPlanEntry("t7", 2, MealSlot.SNACK, recipe2)
                )
            )
        )
    }

    // --- Reviews & Ratings ---

    fun addRecipeReview(recipeId: String, rating: Int, comment: String) {
        val newReview = RecipeReview(
            id = "rev_${UUID.randomUUID().toString().take(8)}",
            recipeId = recipeId,
            authorId = _currentUser.value.id,
            authorUsername = _currentUser.value.username,
            authorAvatarUrl = _currentUser.value.avatarUrl,
            isAuthorVerified = _currentUser.value.hasVerificationBadge,
            rating = rating.coerceIn(1, 5),
            comment = comment.trim(),
            timestamp = System.currentTimeMillis()
        )

        _reviews.update { map ->
            val list = map[recipeId] ?: emptyList()
            map + (recipeId to (listOf(newReview) + list))
        }

        // Recalculate average rating
        val updatedReviews = _reviews.value[recipeId] ?: emptyList()
        val avg = updatedReviews.map { it.rating }.average().toFloat()

        _recipes.update { list ->
            list.map {
                if (it.id == recipeId) {
                    it.copy(
                        ratingAverage = (Math.round(avg * 10) / 10.0).toFloat(),
                        ratingCount = updatedReviews.size
                    )
                } else it
            }
        }

        val recipe = _recipes.value.find { it.id == recipeId }
        recipe?.let {
            addActivityNotification(
                ActivityItem(
                    id = UUID.randomUUID().toString(),
                    type = ActivityType.REVIEW,
                    actorUsername = _currentUser.value.username,
                    actorAvatarUrl = _currentUser.value.avatarUrl,
                    recipeId = it.id,
                    recipeTitle = it.title,
                    message = "rated your recipe $rating stars: '$comment'",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // --- Collections / Playlists ---

    fun createCollection(title: String, description: String, isPublic: Boolean = true): String {
        val id = "col_${UUID.randomUUID().toString().take(8)}"
        val newCollection = RecipeCollection(
            id = id,
            title = title,
            description = description,
            creatorId = _currentUser.value.id,
            creatorUsername = _currentUser.value.username,
            coverGradientId = (1..6).random(),
            isPublic = isPublic
        )
        _collections.update { listOf(newCollection) + it }
        return id
    }

    fun toggleRecipeInCollection(collectionId: String, recipeId: String) {
        _collections.update { list ->
            list.map { col ->
                if (col.id == collectionId) {
                    val contains = col.recipeIds.contains(recipeId)
                    val updatedIds = if (contains) col.recipeIds - recipeId else col.recipeIds + recipeId
                    col.copy(recipeIds = updatedIds)
                } else col
            }
        }
    }

    // --- Recipe Versioning & Collaboration ---

    fun addRecipeVersion(recipeId: String, changelogNote: String, newCookTime: Int?) {
        _recipes.update { list ->
            list.map { recipe ->
                if (recipe.id == recipeId) {
                    val currentVersionCount = recipe.versions.size + 1
                    val newVersion = RecipeVersion(
                        versionNumber = "v1.$currentVersionCount",
                        timestamp = System.currentTimeMillis(),
                        editorUsername = _currentUser.value.username,
                        changelogNote = changelogNote,
                        ingredientsSummary = recipe.ingredients.joinToString(", ") { it.name },
                        cookTimeMinutes = newCookTime ?: recipe.cookTimeMinutes
                    )
                    recipe.copy(
                        versions = listOf(newVersion) + recipe.versions,
                        cookTimeMinutes = newCookTime ?: recipe.cookTimeMinutes
                    )
                } else recipe
            }
        }
    }

    fun addCoAuthor(recipeId: String, coAuthor: CoAuthor) {
        _recipes.update { list ->
            list.map { recipe ->
                if (recipe.id == recipeId) {
                    val existing = recipe.coAuthors.any { it.userId == coAuthor.userId }
                    if (!existing) {
                        recipe.copy(coAuthors = recipe.coAuthors + coAuthor)
                    } else recipe
                } else recipe
            }
        }

        addActivityNotification(
            ActivityItem(
                id = UUID.randomUUID().toString(),
                type = ActivityType.COLLABORATION,
                actorUsername = _currentUser.value.username,
                actorAvatarUrl = _currentUser.value.avatarUrl,
                recipeId = recipeId,
                message = "added @${coAuthor.username} as a Co-Author",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // --- Custom Ingredient Database Management ---

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
        val newIng = CustomIngredientItem(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            name = name,
            category = category,
            caloriesPer100g = calories,
            proteinPer100g = protein,
            carbsPer100g = carbs,
            fatPer100g = fat,
            allergens = allergens,
            defaultServing = defaultServing
        )
        _customIngredients.update { listOf(newIng) + it }
    }

    fun updateCustomIngredient(item: CustomIngredientItem) {
        _customIngredients.update { list ->
            list.map { if (it.id == item.id) item else it }
        }
    }

    fun deleteCustomIngredient(id: String) {
        _customIngredients.update { list -> list.filter { it.id != id } }
    }

    // --- Allergen Detection ---

    fun updateUserAllergens(allergens: List<Allergen>) {
        _currentUser.update { it.copy(personalAllergens = allergens) }
    }

    fun getRecipeAllergenConflicts(recipe: Recipe): List<Allergen> {
        val userAllergens = _currentUser.value.personalAllergens
        return recipe.allergens.filter { userAllergens.contains(it) }
    }

    // --- Delivery Integration Calculation ---

    fun getEstimatedGroceryTotal(): Double {
        return _shoppingList.value.sumOf { it.estimatedPriceUsd }
    }

    // --- User & Auth Operations ---

    fun login(email: String) {
        val user = _users.value.find { it.email.equals(email, ignoreCase = true) }
        if (user != null) {
            _currentUser.value = user
        }
    }

    fun register(username: String, email: String, city: String, bio: String) {
        val newUser = User(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            username = username.lowercase().replace(" ", "_"),
            displayName = username,
            email = email,
            city = city.ifBlank { "Austin, TX" },
            bio = bio,
            followersCount = 0,
            followingCount = 0,
            isPremium = false,
            isVerified = false,
            totalLikesReceived = 0
        )
        _currentUser.value = newUser
        _users.update { it + newUser }
    }

    fun updateProfile(displayName: String, bio: String, city: String) {
        _currentUser.update {
            it.copy(displayName = displayName, bio = bio, city = city)
        }
    }

    fun toggleFollow(userId: String) {
        _users.update { userList ->
            userList.map { user ->
                if (user.id == userId) {
                    val willFollow = !user.isFollowing
                    val newFollowerCount = if (willFollow) user.followersCount + 1 else (user.followersCount - 1).coerceAtLeast(0)
                    user.copy(
                        isFollowing = willFollow,
                        followersCount = newFollowerCount,
                        isVerified = user.isPremium || newFollowerCount >= 10000
                    )
                } else user
            }
        }
        val targetUser = _users.value.find { it.id == userId }
        if (targetUser != null && targetUser.isFollowing) {
            _currentUser.update { it.copy(followingCount = it.followingCount + 1) }
        } else {
            _currentUser.update { it.copy(followingCount = (it.followingCount - 1).coerceAtLeast(0)) }
        }
    }

    // --- Subscription & Verification ---

    fun upgradeToPremium() {
        _currentUser.update { user ->
            user.copy(
                isPremium = true,
                isVerified = true
            )
        }
        addActivityNotification(
            ActivityItem(
                id = UUID.randomUUID().toString(),
                type = ActivityType.VERIFICATION_UPDATE,
                actorUsername = "Savor Kitchen",
                isActorVerified = true,
                message = "You are now verified as a Savor Premium Chef! ⭐",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun cancelPremium() {
        _currentUser.update { user ->
            val stillVerified = user.followersCount >= 10000
            user.copy(
                isPremium = false,
                isVerified = stillVerified
            )
        }
    }

    // --- Likes & Chef Flowers ---

    fun toggleLike(recipeId: String) {
        var likedRecipe: Recipe? = null
        _recipes.update { list ->
            list.map { recipe ->
                if (recipe.id == recipeId) {
                    val newLiked = !recipe.isLikedByMe
                    val newLikesCount = if (newLiked) recipe.likesCount + 1 else (recipe.likesCount - 1).coerceAtLeast(0)
                    val updated = recipe.copy(
                        isLikedByMe = newLiked,
                        likesCount = newLikesCount
                    )
                    likedRecipe = updated
                    updated
                } else recipe
            }
        }

        likedRecipe?.let { recipe ->
            if (recipe.authorId == _currentUser.value.id) {
                val oldLikes = _currentUser.value.totalLikesReceived
                val oldFlowers = oldLikes / 250
                val newLikes = if (recipe.isLikedByMe) oldLikes + 1 else (oldLikes - 1).coerceAtLeast(0)
                val newFlowers = newLikes / 250
                _currentUser.update { it.copy(totalLikesReceived = newLikes) }

                if (newFlowers > oldFlowers) {
                    addActivityNotification(
                        ActivityItem(
                            id = UUID.randomUUID().toString(),
                            type = ActivityType.CHEF_FLOWER_MILESTONE,
                            actorUsername = "Savor Kitchen",
                            isActorVerified = true,
                            message = "Bravo! You earned your ${newFlowers}th Chef Flower! 🌸",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            } else if (recipe.isLikedByMe) {
                addActivityNotification(
                    ActivityItem(
                        id = UUID.randomUUID().toString(),
                        type = ActivityType.LIKE,
                        actorUsername = _currentUser.value.username,
                        actorAvatarUrl = _currentUser.value.avatarUrl,
                        recipeId = recipe.id,
                        recipeTitle = recipe.title,
                        message = "liked your recipe ${recipe.title}",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun toggleSave(recipeId: String) {
        _recipes.update { list ->
            list.map { recipe ->
                if (recipe.id == recipeId) {
                    recipe.copy(isSavedByMe = !recipe.isSavedByMe)
                } else recipe
            }
        }
    }

    fun publishRecipe(recipe: Recipe) {
        _recipes.update { listOf(recipe) + it }
    }

    // --- Sharing ---

    fun shareRecipeToStory(recipe: Recipe) {
        val now = System.currentTimeMillis()
        val newStory = Story(
            id = "story_${UUID.randomUUID().toString().take(8)}",
            authorId = _currentUser.value.id,
            authorUsername = _currentUser.value.username,
            authorAvatarUrl = _currentUser.value.avatarUrl,
            isAuthorVerified = _currentUser.value.hasVerificationBadge,
            mediaUrl = recipe.mediaUrl,
            textOverlay = "Check out this recipe! Originally posted by ${recipe.originalPosterUsername ?: recipe.authorUsername}",
            resharedRecipe = recipe,
            createdAtTimestamp = now,
            expiresAtTimestamp = now + 24 * 3600 * 1000L
        )
        _stories.update { listOf(newStory) + it }

        _recipes.update { list ->
            list.map { if (it.id == recipe.id) it.copy(shareCount = it.shareCount + 1) else it }
        }

        addActivityNotification(
            ActivityItem(
                id = UUID.randomUUID().toString(),
                type = ActivityType.SHARE,
                actorUsername = _currentUser.value.username,
                actorAvatarUrl = _currentUser.value.avatarUrl,
                recipeId = recipe.id,
                recipeTitle = recipe.title,
                message = "shared your recipe to their story",
                timestamp = now,
                originalPosterUsername = recipe.originalPosterUsername ?: recipe.authorUsername
            )
        )
    }

    fun sendRecipeAsMessage(conversationId: String, recipe: Recipe) {
        val message = ChatMessage(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            conversationId = conversationId,
            senderId = _currentUser.value.id,
            senderUsername = _currentUser.value.username,
            text = "Shared recipe: ${recipe.title}",
            recipeCard = recipe,
            timestamp = System.currentTimeMillis()
        )
        sendMessage(conversationId, message)

        _recipes.update { list ->
            list.map { if (it.id == recipe.id) it.copy(shareCount = it.shareCount + 1) else it }
        }

        addActivityNotification(
            ActivityItem(
                id = UUID.randomUUID().toString(),
                type = ActivityType.SHARE,
                actorUsername = _currentUser.value.username,
                actorAvatarUrl = _currentUser.value.avatarUrl,
                recipeId = recipe.id,
                recipeTitle = recipe.title,
                message = "shared your recipe in a direct message",
                timestamp = System.currentTimeMillis(),
                originalPosterUsername = recipe.originalPosterUsername ?: recipe.authorUsername
            )
        )
    }

    fun postStory(mediaUrl: String, textOverlay: String) {
        val now = System.currentTimeMillis()
        val newStory = Story(
            id = "story_${UUID.randomUUID().toString().take(8)}",
            authorId = _currentUser.value.id,
            authorUsername = _currentUser.value.username,
            authorAvatarUrl = _currentUser.value.avatarUrl,
            isAuthorVerified = _currentUser.value.hasVerificationBadge,
            mediaUrl = mediaUrl,
            textOverlay = textOverlay,
            createdAtTimestamp = now,
            expiresAtTimestamp = now + 24 * 3600 * 1000L
        )
        _stories.update { listOf(newStory) + it }
    }

    fun deleteStory(storyId: String) {
        _stories.update { it.filter { story -> story.id != storyId } }
    }

    fun sendMessage(conversationId: String, message: ChatMessage) {
        _messages.update { currentMap ->
            val currentList = currentMap[conversationId] ?: emptyList()
            currentMap + (conversationId to (currentList + message))
        }
        _conversations.update { list ->
            list.map { convo ->
                if (convo.id == conversationId) {
                    convo.copy(
                        lastMessage = if (message.recipeCard != null) "Sent a recipe: ${message.recipeCard.title}" else message.text,
                        lastTimestamp = message.timestamp
                    )
                } else convo
            }
        }
    }

    fun markConversationAsRead(conversationId: String) {
        _conversations.update { list ->
            list.map { convo ->
                if (convo.id == conversationId) convo.copy(unreadCount = 0) else convo
            }
        }
        _messages.update { currentMap ->
            val list = currentMap[conversationId] ?: emptyList()
            currentMap + (conversationId to list.map { it.copy(isRead = true) })
        }
    }

    fun addComment(recipeId: String, text: String) {
        val newComment = Comment(
            id = "comment_${UUID.randomUUID().toString().take(8)}",
            recipeId = recipeId,
            authorId = _currentUser.value.id,
            authorUsername = _currentUser.value.username,
            authorAvatarUrl = _currentUser.value.avatarUrl,
            isAuthorVerified = _currentUser.value.hasVerificationBadge,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        _comments.update { currentMap ->
            val list = currentMap[recipeId] ?: emptyList()
            currentMap + (recipeId to (list + newComment))
        }
        _recipes.update { list ->
            list.map { if (it.id == recipeId) it.copy(commentsCount = it.commentsCount + 1) else it }
        }

        val recipe = _recipes.value.find { it.id == recipeId }
        recipe?.let {
            addActivityNotification(
                ActivityItem(
                    id = UUID.randomUUID().toString(),
                    type = ActivityType.COMMENT,
                    actorUsername = _currentUser.value.username,
                    actorAvatarUrl = _currentUser.value.avatarUrl,
                    recipeId = it.id,
                    recipeTitle = it.title,
                    message = "commented: '$text'",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // --- Shopping List ---

    fun addRecipeToShoppingList(recipe: Recipe) {
        _shoppingList.update { currentList ->
            val updated = currentList.toMutableList()
            for (ingredient in recipe.ingredients) {
                val index = updated.indexOfFirst { it.name.equals(ingredient.name, ignoreCase = true) }
                if (index >= 0) {
                    val existing = updated[index]
                    val existingQty = existing.amount.toDoubleOrNull()
                    val newQty = ingredient.amount.toDoubleOrNull()
                    val combinedAmount = if (existingQty != null && newQty != null && existing.unit == ingredient.unit) {
                        "${existingQty + newQty}".removeSuffix(".0")
                    } else {
                        "${existing.amount} + ${ingredient.amount}"
                    }
                    updated[index] = existing.copy(amount = combinedAmount)
                } else {
                    updated.add(
                        ShoppingItem(
                            id = UUID.randomUUID().toString(),
                            name = ingredient.name,
                            amount = ingredient.amount,
                            unit = ingredient.unit,
                            isChecked = false,
                            sourceRecipeTitle = recipe.title,
                            estimatedPriceUsd = (kotlin.random.Random.nextInt(250, 600) / 100.0)
                        )
                    )
                }
            }
            updated
        }
    }

    fun toggleShoppingItem(itemId: String) {
        _shoppingList.update { list ->
            list.map { if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it }
        }
    }

    fun removeShoppingItem(itemId: String) {
        _shoppingList.update { list -> list.filter { it.id != itemId } }
    }

    fun addCustomShoppingItem(name: String, amount: String, unit: String) {
        _shoppingList.update {
            it + ShoppingItem(
                id = UUID.randomUUID().toString(),
                name = name,
                amount = amount,
                unit = unit,
                isChecked = false,
                estimatedPriceUsd = 3.99
            )
        }
    }

    fun clearCompletedShoppingItems() {
        _shoppingList.update { list -> list.filter { !it.isChecked } }
    }

    // --- Meal Planning ---

    fun assignRecipeToMealSlot(dayOfWeek: Int, slot: MealSlot, recipe: Recipe) {
        _mealPlan.update { currentPlan ->
            val filtered = currentPlan.filterNot { it.dayOfWeek == dayOfWeek && it.slot == slot }
            filtered + MealPlanEntry(
                id = UUID.randomUUID().toString(),
                dayOfWeek = dayOfWeek,
                slot = slot,
                recipe = recipe
            )
        }
    }

    fun removeMealPlanEntry(entryId: String) {
        _mealPlan.update { list -> list.filter { it.id != entryId } }
    }

    fun autoGenerateMealPlan() {
        val candidates = _recipes.value
            .filter { canUserViewRecipe(it) }
            .sortedByDescending { it.likesCount }

        if (candidates.isEmpty()) return

        val newEntries = mutableListOf<MealPlanEntry>()
        val slots = listOf(MealSlot.BREAKFAST, MealSlot.LUNCH, MealSlot.DINNER, MealSlot.SNACK)
        var recipeIdx = 0

        for (day in 0..6) {
            for (slot in slots) {
                if (recipeIdx < candidates.size) {
                    val recipe = candidates[recipeIdx % candidates.size]
                    newEntries.add(
                        MealPlanEntry(
                            id = UUID.randomUUID().toString(),
                            dayOfWeek = day,
                            slot = slot,
                            recipe = recipe
                        )
                    )
                    recipeIdx++
                }
            }
        }
        _mealPlan.value = newEntries
    }

    fun saveMealPlanTemplate(name: String, description: String) {
        val newTemplate = MealPlanTemplate(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            entries = _mealPlan.value
        )
        _templates.update { listOf(newTemplate) + it }
    }

    fun loadMealPlanTemplate(template: MealPlanTemplate) {
        _mealPlan.value = template.entries
    }

    fun deleteMealPlanTemplate(templateId: String) {
        _templates.update { list -> list.filter { it.id != templateId } }
    }

    fun bulkAddWeekToShoppingList() {
        for (entry in _mealPlan.value) {
            addRecipeToShoppingList(entry.recipe)
        }
    }

    // --- Activities & Notifications ---

    fun markActivitiesAsRead() {
        _activities.update { list -> list.map { it.copy(isRead = true) } }
    }

    private fun addActivityNotification(item: ActivityItem) {
        _activities.update { listOf(item) + it }
    }

    fun canUserViewRecipe(recipe: Recipe): Boolean {
        val user = _currentUser.value
        return when (recipe.privacyLevel) {
            PrivacyLevel.PUBLIC -> true
            PrivacyLevel.FOLLOWERS_ONLY -> {
                recipe.authorId == user.id || _users.value.find { it.id == recipe.authorId }?.isFollowing == true
            }
            PrivacyLevel.PREMIUM_ONLY -> {
                user.isPremium || recipe.authorId == user.id
            }
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.update { !it }
    }

    fun togglePushSetting(type: ActivityType) {
        _pushSettings.update { current ->
            current + (type to !(current[type] ?: true))
        }
    }

    fun getTotalUnreadCount(): Int {
        val unreadActivities = _activities.value.count { !it.isRead }
        val unreadConvos = _conversations.value.sumOf { it.unreadCount }
        return unreadActivities + unreadConvos
    }
}
