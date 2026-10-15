package com.example.data.model

object NutritionCalculator {
    private val ingredientDatabase = mapOf(
        "chicken" to NutritionInfo(calories = 165, protein = 31, carbs = 0, fat = 4),
        "salmon" to NutritionInfo(calories = 208, protein = 22, carbs = 0, fat = 13),
        "beef" to NutritionInfo(calories = 250, protein = 26, carbs = 0, fat = 15),
        "egg" to NutritionInfo(calories = 78, protein = 6, carbs = 1, fat = 5),
        "tofu" to NutritionInfo(calories = 80, protein = 9, carbs = 2, fat = 5),
        "rice" to NutritionInfo(calories = 130, protein = 3, carbs = 28, fat = 1),
        "pasta" to NutritionInfo(calories = 158, protein = 6, carbs = 31, fat = 1),
        "quinoa" to NutritionInfo(calories = 120, protein = 4, carbs = 21, fat = 2),
        "oats" to NutritionInfo(calories = 150, protein = 5, carbs = 27, fat = 3),
        "avocado" to NutritionInfo(calories = 160, protein = 2, carbs = 9, fat = 15),
        "olive oil" to NutritionInfo(calories = 119, protein = 0, carbs = 0, fat = 14),
        "butter" to NutritionInfo(calories = 102, protein = 0, carbs = 0, fat = 12),
        "cheese" to NutritionInfo(calories = 113, protein = 7, carbs = 1, fat = 9),
        "yogurt" to NutritionInfo(calories = 100, protein = 10, carbs = 6, fat = 2),
        "milk" to NutritionInfo(calories = 120, protein = 8, carbs = 12, fat = 5),
        "spinach" to NutritionInfo(calories = 23, protein = 3, carbs = 4, fat = 0),
        "broccoli" to NutritionInfo(calories = 34, protein = 3, carbs = 7, fat = 0),
        "tomato" to NutritionInfo(calories = 22, protein = 1, carbs = 5, fat = 0),
        "garlic" to NutritionInfo(calories = 15, protein = 1, carbs = 3, fat = 0),
        "onion" to NutritionInfo(calories = 40, protein = 1, carbs = 9, fat = 0),
        "mushroom" to NutritionInfo(calories = 22, protein = 3, carbs = 3, fat = 0),
        "sweet potato" to NutritionInfo(calories = 86, protein = 2, carbs = 20, fat = 0),
        "almond" to NutritionInfo(calories = 160, protein = 6, carbs = 6, fat = 14),
        "peanut butter" to NutritionInfo(calories = 190, protein = 8, carbs = 7, fat = 16),
        "bread" to NutritionInfo(calories = 80, protein = 4, carbs = 15, fat = 1),
        "honey" to NutritionInfo(calories = 64, protein = 0, carbs = 17, fat = 0),
        "berry" to NutritionInfo(calories = 45, protein = 1, carbs = 11, fat = 0),
        "lemon" to NutritionInfo(calories = 17, protein = 1, carbs = 5, fat = 0)
    )

    fun calculate(ingredients: List<Ingredient>, servings: Int): NutritionInfo {
        val safeServings = servings.coerceAtLeast(1)
        var totalCalories = 0
        var totalProtein = 0
        var totalCarbs = 0
        var totalFat = 0

        for (item in ingredients) {
            val lower = item.name.lowercase().trim()
            var matched: NutritionInfo? = null
            for ((key, nutrition) in ingredientDatabase) {
                if (lower.contains(key)) {
                    matched = nutrition
                    break
                }
            }

            // Estimate multiplier based on amount string if numeric
            val quantity = item.amount.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 1.0
            val factor = quantity.coerceIn(0.25, 4.0)

            if (matched != null) {
                totalCalories += (matched.calories * factor).toInt()
                totalProtein += (matched.protein * factor).toInt()
                totalCarbs += (matched.carbs * factor).toInt()
                totalFat += (matched.fat * factor).toInt()
            } else {
                // Default heuristic fallback for unidentified culinary ingredients
                totalCalories += (50 * factor).toInt()
                totalProtein += (2 * factor).toInt()
                totalCarbs += (8 * factor).toInt()
                totalFat += (1 * factor).toInt()
            }
        }

        return NutritionInfo(
            calories = totalCalories / safeServings,
            protein = totalProtein / safeServings,
            carbs = totalCarbs / safeServings,
            fat = totalFat / safeServings
        )
    }
}
