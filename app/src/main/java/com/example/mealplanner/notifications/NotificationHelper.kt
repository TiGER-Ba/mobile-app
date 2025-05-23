package com.example.mealplanner.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mealplanner.MainActivity
import com.example.mealplanner.R
import com.example.mealplanner.data.model.Meal
import com.example.mealplanner.data.model.MealType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val MEAL_REMINDER_CHANNEL_ID = "meal_reminder_channel"
        const val NUTRITION_CHANNEL_ID = "nutrition_channel"
        const val MEAL_NOTIFICATION_ID_BASE = 1000
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal pour les rappels de repas
            val mealReminderChannel = NotificationChannel(
                MEAL_REMINDER_CHANNEL_ID,
                "Rappels de repas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour rappeler les heures des repas planifiés"
                enableVibration(true)
                enableLights(true)
            }

            // Canal pour les informations nutritionnelles
            val nutritionChannel = NotificationChannel(
                NUTRITION_CHANNEL_ID,
                "Suivi nutritionnel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications pour le suivi des objectifs nutritionnels"
            }

            notificationManager.createNotificationChannel(mealReminderChannel)
            notificationManager.createNotificationChannel(nutritionChannel)
        }
    }

    fun showMealReminder(meal: Meal) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("meal_id", meal.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            meal.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mealTypeText = when (meal.type) {
            MealType.BREAKFAST -> "Petit-déjeuner"
            MealType.LUNCH -> "Déjeuner"
            MealType.DINNER -> "Dîner"
            MealType.SNACK -> "Collation"
        }

        val title = "⏰ Il est temps pour votre $mealTypeText"
        val content = if (meal.name.isNotEmpty()) {
            meal.name
        } else {
            "Votre $mealTypeText est planifié maintenant"
        }

        val notification = NotificationCompat.Builder(context, MEAL_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nutrition)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_favorite_filled,
                "Marquer comme pris",
                createMarkAsEatenIntent(meal.id)
            )
            .addAction(
                R.drawable.ic_time,
                "Reporter de 15 min",
                createSnoozeIntent(meal.id, 15)
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                MEAL_NOTIFICATION_ID_BASE + meal.id.hashCode(),
                notification
            )
        } catch (e: SecurityException) {
            // L'utilisateur n'a pas accordé la permission de notification
            android.util.Log.w("NotificationHelper", "Permission de notification non accordée")
        }
    }

    fun showNutritionSummary(
        caloriesConsumed: Int,
        caloriesTarget: Int,
        proteinConsumed: Int,
        proteinTarget: Int
    ) {
        val caloriesPercentage = if (caloriesTarget > 0) (caloriesConsumed * 100) / caloriesTarget else 0

        val title = when {
            caloriesPercentage >= 100 -> "🎯 Objectif calorique atteint !"
            caloriesPercentage >= 80 -> "👍 Presque à votre objectif"
            caloriesPercentage >= 50 -> "📊 À mi-chemin de votre objectif"
            else -> "💪 Continuez vos efforts !"
        }

        val content = "Calories: $caloriesConsumed/$caloriesTarget • Protéines: ${proteinConsumed}g/${proteinTarget}g"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_nutrition", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NUTRITION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nutrition)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(999, notification)
        } catch (e: SecurityException) {
            android.util.Log.w("NotificationHelper", "Permission de notification non accordée")
        }
    }

    private fun createMarkAsEatenIntent(mealId: String): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "MARK_AS_EATEN"
            putExtra("meal_id", mealId)
        }
        return PendingIntent.getBroadcast(
            context,
            mealId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSnoozeIntent(mealId: String, minutes: Int): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "SNOOZE"
            putExtra("meal_id", mealId)
            putExtra("snooze_minutes", minutes)
        }
        return PendingIntent.getBroadcast(
            context,
            mealId.hashCode() + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancelMealNotification(mealId: String) {
        NotificationManagerCompat.from(context).cancel(MEAL_NOTIFICATION_ID_BASE + mealId.hashCode())
    }

    fun cancelAllMealNotifications() {
        val notificationManager = NotificationManagerCompat.from(context)
        // Annuler toutes les notifications de repas (IDs de 1000 à 1999)
        for (id in 1000..1999) {
            notificationManager.cancel(id)
        }
    }
}