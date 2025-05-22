package com.example.mealplanner

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MealPlannerApp : Application() {

    companion object {
        private const val TAG = "MealPlannerApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application MealPlanner démarrée")

        try {
            // Initialiser WorkManager avec une configuration personnalisée si nécessaire
            initializeWorkManager()
            Log.d(TAG, "WorkManager initialisé avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'initialisation de WorkManager", e)
        }
    }

    private fun initializeWorkManager() {
        try {
            // WorkManager s'initialise automatiquement
            // Configuration personnalisée uniquement si nécessaire
            Log.d(TAG, "Configuration WorkManager terminée")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur dans la configuration WorkManager", e)
        }
    }
}