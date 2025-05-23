package com.example.mealplanner.data.database

import androidx.room.TypeConverter
import com.example.mealplanner.data.model.MealType
import com.example.mealplanner.data.model.NutritionGoal

class Converters {

    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value.isNullOrEmpty() || value == "[]") return emptyList()
        return try {
            // Méthode simple sans Gson pour éviter les problèmes de type
            value.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().removeSurrounding("\"") }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromListString(list: List<String>?): String {
        return try {
            if (list.isNullOrEmpty()) "[]"
            else "[${list.joinToString(",") { "\"$it\"" }}]"
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun fromFloatListString(value: String?): List<Float> {
        if (value.isNullOrEmpty() || value == "[]") return emptyList()
        return try {
            value.removeSurrounding("[", "]")
                .split(",")
                .map { it.trim().toFloat() }
                .filter { !it.isNaN() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromFloatListToString(list: List<Float>?): String {
        return try {
            if (list.isNullOrEmpty()) "[]"
            else "[${list.joinToString(",")}]"
        } catch (e: Exception) {
            "[]"
        }
    }

    @TypeConverter
    fun fromMealTypeString(value: String?): MealType {
        return try {
            if (value.isNullOrEmpty()) MealType.BREAKFAST
            else enumValueOf<MealType>(value)
        } catch (e: Exception) {
            MealType.BREAKFAST
        }
    }

    @TypeConverter
    fun fromMealTypeToString(value: MealType?): String {
        return (value ?: MealType.BREAKFAST).name
    }

    @TypeConverter
    fun fromNutritionGoalString(value: String?): NutritionGoal {
        return try {
            if (value.isNullOrEmpty()) NutritionGoal.MAINTENANCE
            else enumValueOf<NutritionGoal>(value)
        } catch (e: Exception) {
            NutritionGoal.MAINTENANCE
        }
    }

    @TypeConverter
    fun fromNutritionGoalToString(value: NutritionGoal?): String {
        return (value ?: NutritionGoal.MAINTENANCE).name
    }
}