package com.example.mealplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}

@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: Long,
    val isSynced: Boolean = false
)

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val mealPlanId: String,
    val type: MealType,
    val time: Long, // heure du repas
    val name: String = "",
    val notes: String = "",
    val isSynced: Boolean = false
)

@Entity(tableName = "meal_items", primaryKeys = ["id", "mealId"])
data class MealItem(
    val id: String = UUID.randomUUID().toString(),
    val mealId: String,
    val foodId: String? = null, // Si c'est un aliment simple
    val recipeId: String? = null, // Si c'est une recette
    val quantity: Float,
    val servingSize: Float,
    val isSynced: Boolean = false
)