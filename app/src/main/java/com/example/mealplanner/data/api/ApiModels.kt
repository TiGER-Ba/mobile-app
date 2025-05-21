package com.example.mealplanner.data.api

import com.google.gson.annotations.SerializedName

data class FoodSearchResponse(
    @SerializedName("hints")
    val foodItems: List<FoodItem> = emptyList(),
    @SerializedName("text")
    val searchTerm: String = "",
    @SerializedName("parsed")
    val parsed: List<ParsedFood> = emptyList()
)

data class ParsedFood(
    @SerializedName("food")
    val food: FoodData
)

data class FoodItem(
    @SerializedName("food")
    val food: FoodData
)

data class FoodData(
    @SerializedName("foodId")
    val foodId: String = "",
    @SerializedName("label")
    val name: String = "",
    @SerializedName("nutrients")
    val nutrients: Nutrients,
    @SerializedName("image")
    val image: String? = null,
    @SerializedName("servingSizes")
    val servingSizes: List<ServingSize>? = null
)

data class Nutrients(
    @SerializedName("ENERC_KCAL")
    val calories: Float = 0f,
    @SerializedName("PROCNT")
    val protein: Float = 0f,
    @SerializedName("FAT")
    val fat: Float = 0f,
    @SerializedName("CHOCDF")
    val carbs: Float = 0f,
    @SerializedName("FIBTG")
    val fiber: Float = 0f,
    @SerializedName("SUGAR")
    val sugar: Float = 0f
)

data class ServingSize(
    @SerializedName("uri")
    val uri: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("quantity")
    val quantity: Float
)

// Extension pour convertir de l'API vers notre modèle local
fun FoodData.toFood(): com.example.mealplanner.data.model.Food {
    return com.example.mealplanner.data.model.Food(
        id = foodId.ifEmpty { name.replace(" ", "_").lowercase() },
        name = name,
        calories = nutrients.calories.toInt(),
        protein = nutrients.protein,
        carbs = nutrients.carbs,
        fat = nutrients.fat,
        fiber = nutrients.fiber,
        sugar = nutrients.sugar,
        servingSize = 100f,
        servingUnit = "g",
        imageUrl = image,
        servingSizeOptions = servingSizes?.map { it.quantity } ?: listOf(100f),
        apiId = foodId,
        isSynced = false
    )
}