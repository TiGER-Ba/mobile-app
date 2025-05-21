package com.example.mealplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class Food(
    @PrimaryKey
    val id: String,
    val name: String,
    val calories: Int,  // kcal par 100g
    val protein: Float, // grammes par 100g
    val carbs: Float,   // grammes par 100g
    val fat: Float,     // grammes par 100g
    val fiber: Float,   // grammes par 100g
    val sugar: Float,   // grammes par 100g
    val servingSize: Float, // en grammes
    val servingUnit: String, // "g", "ml", etc.
    val imageUrl: String? = null,
    val favorite: Boolean = false,
    val lastUsed: Long = 0,
    val servingSizeOptions: List<Float> = listOf(100f), // options de portions en grammes
    val apiId: String? = null, // ID de l'API source pour les mises à jour
    val isSynced: Boolean = false
)