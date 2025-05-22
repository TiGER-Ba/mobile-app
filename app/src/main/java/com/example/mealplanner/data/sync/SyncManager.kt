package com.example.mealplanner.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val SYNC_WORK_NAME = "meal_planner_sync_work"
        private const val TAG = "SyncManager"
    }

    fun setupPeriodicSync() {
        try {
            Log.d(TAG, "Démarrage de la configuration de la synchronisation périodique")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    10000L, // 10 secondes
                    TimeUnit.MILLISECONDS
                )
                .build()

            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE, // CORRIGÉ : UPDATE au lieu de REPLACE
                syncRequest
            )

            Log.d(TAG, "Synchronisation périodique configurée avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la configuration de la synchronisation périodique", e)
            // Ne pas faire planter l'application si la sync échoue
        }
    }

    fun triggerImmediateSync() {
        try {
            Log.d(TAG, "Déclenchement d'une synchronisation immédiate")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    10000L, // 10 secondes
                    TimeUnit.MILLISECONDS
                )
                .build()

            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(syncRequest)

            Log.d(TAG, "Synchronisation immédiate déclenchée avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du déclenchement de la synchronisation immédiate", e)
        }
    }

    fun cancelSync() {
        try {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(SYNC_WORK_NAME)
            Log.d(TAG, "Synchronisation annulée")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'annulation de la synchronisation", e)
        }
    }
}