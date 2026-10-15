package com.example

import com.example.data.model.*
import com.example.data.repository.SavorRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testChefFlowersCalculation() {
        val user0 = User(
            id = "u1",
            username = "chef",
            displayName = "Chef",
            email = "c@savor.app",
            city = "Austin, TX",
            totalLikesReceived = 249
        )
        assertEquals(0, user0.chefFlowers)

        val user1 = user0.copy(totalLikesReceived = 250)
        assertEquals(1, user1.chefFlowers)

        val user10 = user0.copy(totalLikesReceived = 2500)
        assertEquals(10, user10.chefFlowers)
    }

    @Test
    fun testVerificationRules() {
        val regularUser = User(
            id = "u1",
            username = "cook",
            displayName = "Cook",
            email = "c@savor.app",
            city = "Austin, TX",
            followersCount = 9999,
            isPremium = false,
            isVerified = false
        )
        assertFalse(regularUser.hasVerificationBadge)

        val thresholdUser = regularUser.copy(followersCount = 10000)
        assertTrue(thresholdUser.hasVerificationBadge)

        val premiumUser = regularUser.copy(isPremium = true)
        assertTrue(premiumUser.hasVerificationBadge)
    }

    @Test
    fun testNutritionCalculator() {
        val ingredients = listOf(
            Ingredient("Wild Salmon Fillets", "2", "fillets"),
            Ingredient("Olive oil", "1", "tbsp"),
            Ingredient("Spinach", "2", "cups")
        )
        val nutrition = NutritionCalculator.calculate(ingredients, servings = 2)

        assertTrue("Calories should be estimated", nutrition.calories > 100)
        assertTrue("Protein should be estimated from salmon", nutrition.protein > 15)
        assertTrue("Fat should be estimated from olive oil and salmon", nutrition.fat > 5)
    }

    @Test
    fun testShoppingListConsolidation() {
        val repo = SavorRepository()
        val recipe = Recipe(
            id = "r_test",
            authorId = "u_test",
            authorUsername = "test_chef",
            title = "Test Bowl",
            ingredients = listOf(
                Ingredient("Wild Salmon Fillets", "2", "fillets"),
                Ingredient("Avocados", "2", "medium")
            )
        )
        repo.addRecipeToShoppingList(recipe)
        val items = repo.shoppingList.value

        // Wild Salmon Fillets already has 2 in initial seed data; should consolidate
        val salmonItem = items.find { it.name.equals("Wild Salmon Fillets", ignoreCase = true) }
        assertNotNull(salmonItem)
        assertEquals("4", salmonItem?.amount)
    }

    @Test
    fun testParseStepTimerSeconds() {
        // Step with minutes in text
        val sec1 = com.example.ui.screens.parseStepTimerSeconds("Sear salmon skin-side down for 4 minutes until crisp", null)
        assertEquals(240, sec1)

        // Step with explicit metadata
        val sec2 = com.example.ui.screens.parseStepTimerSeconds("Boil pasta", 8)
        assertEquals(480, sec2)

        // Step with seconds in text
        val sec3 = com.example.ui.screens.parseStepTimerSeconds("Blanch basil leaves for 30 seconds", null)
        assertEquals(30, sec3)

        // Step without time (falls back to sensible default 3 minutes = 180s)
        val secDefault = com.example.ui.screens.parseStepTimerSeconds("Garnish with chopped cilantro and lime wedges", null)
        assertEquals(180, secDefault)
    }

    @Test
    fun testFormatTimerDisplay() {
        assertEquals("04:00", com.example.ui.screens.formatTimerDisplay(240))
        assertEquals("01:05", com.example.ui.screens.formatTimerDisplay(65))
        assertEquals("00:09", com.example.ui.screens.formatTimerDisplay(9))
        assertEquals("00:00", com.example.ui.screens.formatTimerDisplay(0))
        assertEquals("15:30", com.example.ui.screens.formatTimerDisplay(930))
    }
}

