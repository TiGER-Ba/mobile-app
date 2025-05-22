package com.example.mealplanner.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val instructions: String = "",
    val preparationTime: Int = 0, // minutes
    val cookingTime: Int = 0,     // minutes
    val servings: Int = 1,
    val imageUrl: String? = null,
    val favorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "recipe_ingredients")
data class RecipeIngredient(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recipeId: String,
    val foodId: String,
    val quantity: Float,
    val unit: String
)

// CORRECTION : Utilisation correcte des annotations Room
data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<RecipeIngredient>
)