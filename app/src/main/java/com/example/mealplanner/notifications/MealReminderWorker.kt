package com.example.mealplanner.notifications

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.mealplanner.data.repository.MealPlanRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class MealReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val mealPlanRepository: MealPlanRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "MealReminderWorker"
        const val WORK_NAME = "meal_reminder_work"
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Démarrage de la vérification des rappels de repas")

            // Récupérer le meal_id s'il est fourni (pour les rappels reportés)
            val specificMealId = inputData.getString("meal_id")

            if (specificMealId != null) {
                // C'est un rappel reporté pour un repas spécifique
                handleSpecificMealReminder(specificMealId)
            } else {
                // Vérification générale des repas du jour
                checkTodaysMeals()
            }

            // Programmer la prochaine vérification dans 15 minutes
            scheduleNextCheck()

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification des rappels", e)
            Result.retry()
        }
    }

    private suspend fun handleSpecificMealReminder(mealId: String) {
        try {
            // Récupérer les repas d'aujourd'hui pour trouver le repas spécifique
            val today = getStartOfDay(System.currentTimeMillis())
            val mealPlan = mealPlanRepository.getMealPlanForDate(today)
            val meals = mealPlanRepository.getMealsForMealPlan(mealPlan.id).first()

            val targetMeal = meals.find { it.id == mealId }
            if (targetMeal != null) {
                Log.d(TAG, "Envoi du rappel reporté pour le repas: ${targetMeal.name}")
                notificationHelper.showMealReminder(targetMeal)
            } else {
                Log.w(TAG, "Repas spécifique non trouvé: $mealId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du rappel spécifique", e)
        }
    }

    private suspend fun checkTodaysMeals() {
        try {
            val now = System.currentTimeMillis()
            val today = getStartOfDay(now)

            Log.d(TAG, "Vérification des repas pour aujourd'hui")

            // Récupérer le plan de repas d'aujourd'hui
            val mealPlan = mealPlanRepository.getMealPlanForDate(today)
            val meals = mealPlanRepository.getMealsForMealPlan(mealPlan.id).first()

            Log.d(TAG, "Trouvé ${meals.size} repas planifiés pour aujourd'hui")

            // Vérifier chaque repas
            meals.forEach { meal ->
                val mealTime = meal.time
                val currentTime = now % (24 * 60 * 60 * 1000) // Heure actuelle dans la journée
                val mealTimeInDay = mealTime % (24 * 60 * 60 * 1000) // Heure du repas dans la journée

                // Vérifier si c'est l'heure du repas (avec une tolérance de 15 minutes)
                val timeDifference = Math.abs(currentTime - mealTimeInDay)

                if (timeDifference <= 15 * 60 * 1000) { // 15 minutes de tolérance
                    Log.d(TAG, "Il est temps pour le repas: ${meal.name} (${meal.type})")
                    notificationHelper.showMealReminder(meal)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la vérification des repas", e)
        }
    }

    private fun scheduleNextCheck() {
        val nextCheckRequest = OneTimeWorkRequestBuilder<MealReminderWorker>()
            .setInitialDelay(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                nextCheckRequest
            )
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

// Classe utilitaire pour programmer les rappels de repas
object MealReminderScheduler {
    private const val TAG = "MealReminderScheduler"

    fun scheduleDailyMealChecks(context: Context) {
        Log.d(TAG, "Programmation des vérifications quotidiennes des repas")

        val dailyWorkRequest = PeriodicWorkRequestBuilder<MealReminderWorker>(
            15, TimeUnit.MINUTES // Vérifier toutes les 15 minutes
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                MealReminderWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )

        Log.d(TAG, "Rappels de repas programmés avec succès")
    }

    fun cancelMealReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(MealReminderWorker.WORK_NAME)
        Log.d(TAG, "Rappels de repas annulés")
    }
}