package com.example.mealplanner.utils

object Constants {
    // API Configuration
    const val EDAMAM_BASE_URL = "https://api.edamam.com/"

    // Pour utiliser l'API Edamam, remplacez ces valeurs par vos vraies clés
    // Vous pouvez obtenir des clés gratuites sur https://developer.edamam.com/
    const val EDAMAM_APP_ID = "8e61618d" // Remplacer par votre vraie clé
    const val EDAMAM_APP_KEY = "b9d1e09c71cb825aa12db710608c75fb" // Remplacer par votre vraie clé

    // Database
    const val DATABASE_NAME = "meal_planner_database"
    const val DATABASE_VERSION = 1

    // Sync
    const val SYNC_INTERVAL_MINUTES = 15L
    const val SYNC_WORK_NAME = "meal_planner_sync_work"

    // Default values
    const val DEFAULT_SERVING_SIZE = 100f
    const val DEFAULT_CALORIES_TARGET = 2000
    const val DEFAULT_PROTEIN_TARGET = 150
    const val DEFAULT_CARB_TARGET = 250
    const val DEFAULT_FAT_TARGET = 70

    // Meal times (in milliseconds from start of day)
    const val DEFAULT_BREAKFAST_TIME = 7 * 60 * 60 * 1000L // 7:00 AM
    const val DEFAULT_LUNCH_TIME = 12 * 60 * 60 * 1000L // 12:00 PM
    const val DEFAULT_DINNER_TIME = 19 * 60 * 60 * 1000L // 7:00 PM
    const val DEFAULT_SNACK_TIME = 15 * 60 * 60 * 1000L // 3:00 PM

    // Validation
    const val MIN_AGE = 10
    const val MAX_AGE = 120
    const val MIN_WEIGHT = 30f
    const val MAX_WEIGHT = 300f
    const val MIN_HEIGHT = 100f
    const val MAX_HEIGHT = 250f

    // Search
    const val MIN_SEARCH_QUERY_LENGTH = 2
    const val MAX_SEARCH_RESULTS = 50

    // Image loading
    const val IMAGE_CACHE_SIZE = 50 * 1024 * 1024 // 50MB
    const val IMAGE_PLACEHOLDER_FOOD = "android.resource://com.example.mealplanner/drawable/placeholder_food"
    const val IMAGE_PLACEHOLDER_RECIPE = "android.resource://com.example.mealplanner/drawable/placeholder_recipe"
}