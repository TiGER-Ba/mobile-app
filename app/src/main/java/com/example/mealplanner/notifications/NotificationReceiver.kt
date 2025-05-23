package com.example.mealplanner.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    companion object {
        private const val TAG = "NotificationReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val mealId = intent.getStringExtra("meal_id") ?: return
        Log.d(TAG, "Action reçue: ${intent.action} pour le repas: $mealId")

        when (intent.action) {
            "MARK_AS_EATEN" -> {
                handleMarkAsEaten(context, mealId)
            }
            "SNOOZE" -> {
                val snoozeMinutes = intent.getIntExtra("snooze_minutes", 15)
                handleSnooze(context, mealId, snoozeMinutes)
            }
        }
    }

    private fun handleMarkAsEaten(context: Context, mealId: String) {
        Log.d(TAG, "Marquage du repas $mealId comme pris")

        // Annuler la notification
        notificationHelper.cancelMealNotification(mealId)

        // Ici, vous pourriez ajouter une logique pour marquer le repas comme consommé
        // dans la base de données si vous voulez suivre cette information

        // Optionnel: Afficher une notification de confirmation
        // Toast.makeText(context, "Repas marqué comme pris", Toast.LENGTH_SHORT).show()
    }

    private fun handleSnooze(context: Context, mealId: String, minutes: Int) {
        Log.d(TAG, "Report du repas $mealId de $minutes minutes")

        // Annuler la notification actuelle
        notificationHelper.cancelMealNotification(mealId)

        // Programmer une nouvelle notification dans X minutes
        val snoozeWorkRequest = OneTimeWorkRequestBuilder<MealReminderWorker>()
            .setInitialDelay(minutes.toLong(), TimeUnit.MINUTES)
            .setInputData(workDataOf("meal_id" to mealId))
            .build()

        WorkManager.getInstance(context).enqueue(snoozeWorkRequest)

        Log.d(TAG, "Nouvelle notification programmée dans $minutes minutes")
    }
}