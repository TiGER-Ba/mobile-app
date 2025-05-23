package com.example.mealplanner.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_MEAL_REMINDERS_ENABLED = "meal_reminders_enabled"
        private const val KEY_REMINDER_ADVANCE_MINUTES = "reminder_advance_minutes"
        private const val KEY_NUTRITION_SUMMARY_ENABLED = "nutrition_summary_enabled"
        private const val KEY_APP_FIRST_LAUNCH = "app_first_launch"
    }

    // Notifications générales
    var notificationsEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    // Rappels de repas
    var mealRemindersEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_MEAL_REMINDERS_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_MEAL_REMINDERS_ENABLED, value).apply()

    // Minutes d'avance pour les rappels (par défaut 0 = à l'heure exacte)
    var reminderAdvanceMinutes: Int
        get() = sharedPreferences.getInt(KEY_REMINDER_ADVANCE_MINUTES, 0)
        set(value) = sharedPreferences.edit().putInt(KEY_REMINDER_ADVANCE_MINUTES, value).apply()

    // Résumé nutritionnel quotidien
    var nutritionSummaryEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_NUTRITION_SUMMARY_ENABLED, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_NUTRITION_SUMMARY_ENABLED, value).apply()

    // Premier lancement de l'app
    var isFirstLaunch: Boolean
        get() = sharedPreferences.getBoolean(KEY_APP_FIRST_LAUNCH, true)
        set(value) = sharedPreferences.edit().putBoolean(KEY_APP_FIRST_LAUNCH, value).apply()

    fun resetAllSettings() {
        sharedPreferences.edit().clear().apply()
    }

    fun exportSettings(): Map<String, Any> {
        return mapOf(
            KEY_NOTIFICATIONS_ENABLED to notificationsEnabled,
            KEY_MEAL_REMINDERS_ENABLED to mealRemindersEnabled,
            KEY_REMINDER_ADVANCE_MINUTES to reminderAdvanceMinutes,
            KEY_NUTRITION_SUMMARY_ENABLED to nutritionSummaryEnabled
        )
    }

    fun importSettings(settings: Map<String, Any>) {
        val editor = sharedPreferences.edit()
        settings.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is String -> editor.putString(key, value)
                is Float -> editor.putFloat(key, value)
                is Long -> editor.putLong(key, value)
            }
        }
        editor.apply()
    }
}