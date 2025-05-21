package com.example.mealplanner.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mealplanner.data.repository.FoodRepository
import com.example.mealplanner.data.repository.MealPlanRepository
import com.example.mealplanner.data.repository.RecipeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val foodRepository: FoodRepository,
    private val recipeRepository: RecipeRepository,
    private val mealPlanRepository: MealPlanRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting data synchronization...")

            // Synchroniser les aliments
            val unsyncedFoods = foodRepository.getUnsyncedFoods()
            if (unsyncedFoods.isNotEmpty()) {
                Log.d(TAG, "Syncing ${unsyncedFoods.size} foods...")
                // Ici, vous implémenteriez la logique d'envoi des données au serveur
                // Pour l'exemple, nous allons simplement marquer les éléments comme synchronisés
                foodRepository.markFoodsAsSynced(unsyncedFoods.map { it.id })
            }

            // Synchroniser les recettes
            val unsyncedRecipes = recipeRepository.getUnsyncedRecipes()
            if (unsyncedRecipes.isNotEmpty()) {
                Log.d(TAG, "Syncing ${unsyncedRecipes.size} recipes...")
                recipeRepository.markRecipesAsSynced(unsyncedRecipes.map { it.id })
            }

            // Synchroniser les planifications de repas
            val unsyncedMealPlans = mealPlanRepository.getUnsyncedMealPlans()
            if (unsyncedMealPlans.isNotEmpty()) {
                Log.d(TAG, "Syncing ${unsyncedMealPlans.size} meal plans...")
                mealPlanRepository.markMealPlansAsSynced(unsyncedMealPlans.map { it.id })
            }

            // Synchroniser les repas
            val unsyncedMeals = mealPlanRepository.getUnsyncedMeals()
            if (unsyncedMeals.isNotEmpty()) {
                Log.d(TAG, "Syncing ${unsyncedMeals.size} meals...")
                mealPlanRepository.markMealsAsSynced(unsyncedMeals.map { it.id })
            }

            // Synchroniser les éléments de repas
            val unsyncedMealItems = mealPlanRepository.getUnsyncedMealItems()
            if (unsyncedMealItems.isNotEmpty()) {
                Log.d(TAG, "Syncing ${unsyncedMealItems.size} meal items...")
                mealPlanRepository.markMealItemsAsSynced(unsyncedMealItems.map { it.id })
            }

            Log.d(TAG, "Data synchronization completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during data synchronization", e)
            Result.retry()
        }
    }
}