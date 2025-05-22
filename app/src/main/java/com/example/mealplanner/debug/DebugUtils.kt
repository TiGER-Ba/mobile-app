package com.example.mealplanner.debug

import android.util.Log

object DebugUtils {
    private const val TAG = "MealPlannerDebug"

    fun logAppStart() {
        Log.d(TAG, "=== APPLICATION MEAL PLANNER DÉMARRÉE ===")
        Log.d(TAG, "Version Android: ${android.os.Build.VERSION.SDK_INT}")
        Log.d(TAG, "Modèle: ${android.os.Build.MODEL}")
        Log.d(TAG, "Fabricant: ${android.os.Build.MANUFACTURER}")
    }

    fun logError(component: String, error: Throwable) {
        Log.e(TAG, "Erreur dans $component: ${error.message}", error)
    }

    fun logSuccess(component: String, message: String) {
        Log.d(TAG, "Succès dans $component: $message")
    }
}