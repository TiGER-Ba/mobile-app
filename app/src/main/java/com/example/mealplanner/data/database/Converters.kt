package com.example.mealplanner.data.database

import androidx.room.TypeConverter
import com.example.mealplanner.data.model.MealType
import com.example.mealplanner.data.model.NutritionGoal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromFloatList(value: String): List<Float> {
        val listType = object : TypeToken<List<Float>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun toFloatList(list: List<Float>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toMealType(value: String) = enumValueOf<MealType>(value)

    @TypeConverter
    fun fromMealType(value: MealType) = value.name

    @TypeConverter
    fun toNutritionGoal(value: String) = enumValueOf<NutritionGoal>(value)

    @TypeConverter
    fun fromNutritionGoal(value: NutritionGoal) = value.name
}