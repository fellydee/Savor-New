package com.example.data.model

object CulinaryTools {

    private val substitutions = mapOf(
        "heavy cream" to listOf(
            SubstituteOption("Coconut Cream", "1:1", "Rich and creamy with subtle tropical note", "Vegan & Dairy-Free"),
            SubstituteOption("Greek Yogurt + Milk", "3:1 ratio", "Tangy, high protein alternative", "Lower Fat, High Protein"),
            SubstituteOption("Cashew Cream", "1:1", "Silky neutral texture for savory sauces", "Plant-based Dairy-Free")
        ),
        "cream" to listOf(
            SubstituteOption("Coconut Cream", "1:1", "Whippable plant-based fat", "Dairy-Free"),
            SubstituteOption("Evaporated Milk", "1:1", "Lower saturated fat, same rich body", "Lower Calorie")
        ),
        "butter" to listOf(
            SubstituteOption("Extra Virgin Olive Oil", "3/4 cup oil per 1 cup butter", "Heart-healthy polyphenols for roasting & searing", "Mediterranean Diet"),
            SubstituteOption("Coconut Oil", "1:1", "Solid at room temp, great for flaky pastries", "Dairy-Free"),
            SubstituteOption("Unsweetened Applesauce", "1:1", "Moisture in cakes and quick breads", "Low Fat & Low Calorie")
        ),
        "egg" to listOf(
            SubstituteOption("Flax Egg (1 tbsp ground flax + 3 tbsp water)", "1 egg = 1 flax egg", "Whisk and rest 5 minutes until gelatinous", "Vegan & High Fiber"),
            SubstituteOption("Silken Tofu", "1/4 cup blended tofu per egg", "Dense moisture for quiches and batters", "Plant-based Protein"),
            SubstituteOption("Mashed Ripe Banana", "1/2 banana per egg", "Binds beautifully in pancakes and muffins", "Natural Sweetener")
        ),
        "flour" to listOf(
            SubstituteOption("1-to-1 Gluten-Free Flour Blend", "1:1", "Rice and tapioca based with xanthan gum", "Gluten-Free"),
            SubstituteOption("Almond Flour", "1:1 (add 1 extra egg)", "Nutty flavor, low net carbs", "Keto & Grain-Free"),
            SubstituteOption("Oat Flour", "1:1 by weight", "Subtle whole grain sweetness", "Heart Healthy")
        ),
        "cheese" to listOf(
            SubstituteOption("Nutritional Yeast", "2 tbsp per 1/4 cup cheese", "Savory, nutty, umami profile packed with B-vitamins", "Vegan & Dairy-Free"),
            SubstituteOption("Cultured Cashew Cheese", "1:1", "Melts and crumbles similarly to feta or goat cheese", "Lactose-Free")
        ),
        "soy sauce" to listOf(
            SubstituteOption("Coconut Aminos", "1:1", "Naturally brewed from coconut tree sap, slightly sweeter", "Soy-Free & Low Sodium"),
            SubstituteOption("Tamari", "1:1", "Traditionally fermented without wheat", "Gluten-Free Umami")
        ),
        "peanut butter" to listOf(
            SubstituteOption("Sunflower Seed Butter (SunButter)", "1:1", "Rich nutty flavor completely safe for peanut allergies", "Nut-Free School Safe"),
            SubstituteOption("Tahini (Sesame Paste)", "1:1", "Earthy savory depth", "Paleo & Nutrient Dense")
        ),
        "salmon" to listOf(
            SubstituteOption("Firm Tofu (Marinated & Seared)", "1:1", "High protein plant alternative", "Vegetarian"),
            SubstituteOption("Arctic Char or Steelhead Trout", "1:1", "Identical texture and rich omega-3 profile", "Sustainable Seafood")
        ),
        "beef" to listOf(
            SubstituteOption("Portobello Mushrooms", "1:1 in weight", "Meaty chew and earthy glutamate richness", "Low Calorie Plant-based"),
            SubstituteOption("Lentils & Walnuts", "1:1 minced", "Perfect texture for ragùs and taco fillings", "High Fiber")
        )
    )

    fun getSubstitutionsFor(ingredientName: String): List<SubstituteOption> {
        val lower = ingredientName.lowercase()
        for ((key, list) in substitutions) {
            if (lower.contains(key)) {
                return list
            }
        }
        // Default versatile substitution recommendation
        return listOf(
            SubstituteOption("Plant-based alternative", "1:1", "Check label for moisture ratio adjustments", "Allergen-friendly"),
            SubstituteOption("Store pantry staple", "1:1", "Adjust salt and acid to taste", "Convenient")
        )
    }

    // --- Unit Converter ---
    data class ConversionResult(
        val originalValue: Double,
        val originalUnit: String,
        val convertedValue: Double,
        val convertedUnit: String,
        val formula: String
    )

    fun convertUnits(value: Double, fromUnit: String, toUnit: String): ConversionResult {
        val from = fromUnit.lowercase().trim()
        val to = toUnit.lowercase().trim()

        var converted = value
        var formula = ""

        when {
            // Volume
            (from == "cup" || from == "cups") && (to == "ml" || to == "milliliters") -> {
                converted = value * 236.588
                formula = "$value cups × 236.59 = ${String.format("%.1f", converted)} ml"
            }
            (from == "ml" || from == "milliliters") && (to == "cup" || to == "cups") -> {
                converted = value / 236.588
                formula = "$value ml ÷ 236.59 = ${String.format("%.2f", converted)} cups"
            }
            (from == "tbsp" || from == "tablespoons") && (to == "ml") -> {
                converted = value * 14.787
                formula = "$value tbsp × 14.79 = ${String.format("%.1f", converted)} ml"
            }
            (from == "tsp" || from == "teaspoons") && (to == "ml") -> {
                converted = value * 4.929
                formula = "$value tsp × 4.93 = ${String.format("%.1f", converted)} ml"
            }
            // Weight
            (from == "oz" || from == "ounces") && (to == "g" || to == "grams") -> {
                converted = value * 28.3495
                formula = "$value oz × 28.35 = ${String.format("%.1f", converted)} g"
            }
            (from == "g" || from == "grams") && (to == "oz" || to == "ounces") -> {
                converted = value / 28.3495
                formula = "$value g ÷ 28.35 = ${String.format("%.2f", converted)} oz"
            }
            (from == "lb" || from == "lbs" || from == "pounds") && (to == "kg" || to == "kilograms") -> {
                converted = value * 0.453592
                formula = "$value lbs × 0.454 = ${String.format("%.2f", converted)} kg"
            }
            (from == "kg" || from == "kilograms") && (to == "lb" || to == "lbs") -> {
                converted = value / 0.453592
                formula = "$value kg × 2.205 = ${String.format("%.2f", converted)} lbs"
            }
            // Temperature
            (from == "°f" || from == "f") && (to == "°c" || to == "c") -> {
                converted = (value - 32.0) * 5.0 / 9.0
                formula = "($value°F - 32) × 5/9 = ${String.format("%.0f", converted)}°C"
            }
            (from == "°c" || from == "c") && (to == "°f" || to == "f") -> {
                converted = (value * 9.0 / 5.0) + 32.0
                formula = "($value°C × 9/5) + 32 = ${String.format("%.0f", converted)}°F"
            }
            else -> {
                converted = value
                formula = "$value $fromUnit"
            }
        }

        return ConversionResult(value, fromUnit, converted, toUnit, formula)
    }
}
