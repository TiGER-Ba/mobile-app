package com.example.mealplanner

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.mealplanner.notifications.MealReminderScheduler
import com.example.mealplanner.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MealPlannerApp : Application() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    companion object {
        private const val TAG = "MealPlannerApp"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 Application MealPlanner démarrée")

        try {
            // Initialiser WorkManager avec une configuration personnalisée si nécessaire
            initializeWorkManager()
            Log.d(TAG, "✅ WorkManager initialisé avec succès")

            // NOUVEAU: Initialiser le système de notifications
            initializeNotifications()
            Log.d(TAG, "✅ Système de notifications initialisé")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'initialisation de l'application", e)
        }
    }

    private fun initializeWorkManager() {
        try {
            // WorkManager s'initialise automatiquement avec Hilt
            Log.d(TAG, "📝 Configuration WorkManager terminée")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur dans la configuration WorkManager", e)
        }
    }

    // NOUVEAU: Méthode pour initialiser les notifications
    private fun initializeNotifications() {
        try {
            // Vérifier les permissions de notification (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasNotificationPermission = ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (hasNotificationPermission) {
                    startMealReminderService()
                } else {
                    Log.w(TAG, "⚠️ Permission de notification non accordée. Les rappels ne fonctionneront pas.")
                }
            } else {
                // Pour les versions antérieures, démarrer directement
                startMealReminderService()
            }

            Log.d(TAG, "🔔 Système de notifications configuré")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'initialisation des notifications", e)
        }
    }

    private fun startMealReminderService() {
        try {
            // Programmer les vérifications périodiques des rappels de repas
            MealReminderScheduler.scheduleDailyMealChecks(this)
            Log.d(TAG, "⏰ Service de rappels de repas démarré")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors du démarrage du service de rappels", e)
        }
    }
}