package com.example.mealplanner.data.repository

import androidx.lifecycle.LiveData
import com.example.mealplanner.data.database.dao.UserProfileDao
import com.example.mealplanner.data.model.NutritionGoal
import com.example.mealplanner.data.model.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userProfileDao: UserProfileDao
) {
    fun getUserProfileLiveData(): LiveData<UserProfile?> {
        return userProfileDao.getUserProfileLiveData()
    }

    suspend fun getUserProfile(): UserProfile? {
        return userProfileDao.getUserProfile()
    }

    suspend fun createOrUpdateUserProfile(
        name: String,
        age: Int,
        gender: String,
        weight: Float,
        height: Float,
        goal: NutritionGoal,
        activityLevel: Int
    ) {
        // Calculer les objectifs nutritionnels en fonction du profil
        val bmr = calculateBMR(gender, weight, height, age)
        val activityMultiplier = when (activityLevel) {
            1 -> 1.2f   // Sédentaire
            2 -> 1.375f // Légèrement actif
            3 -> 1.55f  // Modérément actif
            4 -> 1.725f // Très actif
            5 -> 1.9f   // Extrêmement actif
            else -> 1.375f
        }

        val maintenanceCalories = (bmr * activityMultiplier).toInt()

        // Ajuster les calories en fonction de l'objectif
        val dailyCalorieTarget = when (goal) {
            NutritionGoal.WEIGHT_LOSS -> (maintenanceCalories * 0.8).toInt()
            NutritionGoal.WEIGHT_GAIN -> (maintenanceCalories * 1.15).toInt()
            NutritionGoal.MUSCLE_GAIN -> (maintenanceCalories * 1.1).toInt()
            else -> maintenanceCalories
        }

        // Calculer la répartition des macronutriments
        val proteinTarget = when (goal) {
            NutritionGoal.MUSCLE_GAIN -> (weight * 2).toInt() // 2g par kg de poids
            NutritionGoal.WEIGHT_LOSS -> (weight * 1.8).toInt() // 1.8g par kg de poids
            else -> (weight * 1.2).toInt() // 1.2g par kg de poids
        }

        val fatTarget = (dailyCalorieTarget * 0.3 / 9).toInt() // 30% des calories proviennent des graisses
        val carbTarget = (dailyCalorieTarget - (proteinTarget * 4) - (fatTarget * 9)) / 4 // Reste des calories

        val userProfile = UserProfile(
            name = name,
            age = age,
            gender = gender,
            weight = weight,
            height = height,
            goal = goal,
            activityLevel = activityLevel,
            dailyCalorieTarget = dailyCalorieTarget,
            proteinTarget = proteinTarget,
            carbTarget = carbTarget,
            fatTarget = fatTarget,
            lastUpdated = System.currentTimeMillis()
        )

        userProfileDao.insertUserProfile(userProfile)
    }

    // Équation de Harris-Benedict pour calculer le métabolisme de base (BMR)
    private fun calculateBMR(gender: String, weight: Float, height: Float, age: Int): Float {
        return if (gender.equals("female", ignoreCase = true)) {
            655 + (9.6f * weight) + (1.8f * height) - (4.7f * age)
        } else {
            66 + (13.7f * weight) + (5f * height) - (6.8f * age)
        }
    }
}