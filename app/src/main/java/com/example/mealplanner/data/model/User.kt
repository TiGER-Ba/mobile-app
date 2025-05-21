package com.example.mealplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NutritionGoal {
    WEIGHT_LOSS,
    WEIGHT_GAIN,
    MAINTENANCE,
    MUSCLE_GAIN
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: String = "user_profile",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val weight: Float = 0f,
    val height: Float = 0f,
    val goal: NutritionGoal = NutritionGoal.MAINTENANCE,
    val activityLevel: Int = 2, // 1-5 scale de sédentaire à très actif
    val dailyCalorieTarget: Int = 2000,
    val proteinTarget: Int = 100, // en grammes
    val carbTarget: Int = 250,    // en grammes
    val fatTarget: Int = 70,      // en grammes
    val lastUpdated: Long = System.currentTimeMillis()
)