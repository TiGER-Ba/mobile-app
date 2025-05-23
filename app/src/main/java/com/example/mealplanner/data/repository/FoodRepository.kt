package com.example.mealplanner.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.mealplanner.data.api.ApiClient
import com.example.mealplanner.data.api.toFood
import com.example.mealplanner.data.database.dao.FoodDao
import com.example.mealplanner.data.model.Food
import com.example.mealplanner.utils.Constants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(
    private val foodDao: FoodDao
) {
    companion object {
        private const val TAG = "FoodRepository"
    }

    fun getAllFoods(): Flow<List<Food>> {
        return foodDao.getAllFoods()
    }

    fun getFavoriteFoods(): Flow<List<Food>> {
        return foodDao.getFavoriteFoods()
    }

    fun searchLocalFoods(query: String): Flow<List<Food>> {
        return if (query.isBlank()) {
            foodDao.getAllFoods()
        } else {
            foodDao.searchFoods(query)
        }
    }

    suspend fun getFoodById(id: String): Food? {
        return foodDao.getFoodById(id)
    }

    fun getFoodByIdLiveData(id: String): LiveData<Food?> {
        return foodDao.getFoodByIdLiveData(id)
    }

    suspend fun insertFood(food: Food) {
        try {
            foodDao.insertFood(food)
            Log.d(TAG, "Aliment inséré: ${food.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'insertion de l'aliment: ${food.name}", e)
            throw e
        }
    }

    suspend fun updateFood(food: Food) {
        try {
            foodDao.updateFood(food)
            Log.d(TAG, "Aliment mis à jour: ${food.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour de l'aliment: ${food.name}", e)
            throw e
        }
    }

    suspend fun deleteFood(food: Food) {
        try {
            foodDao.deleteFood(food)
            Log.d(TAG, "Aliment supprimé: ${food.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la suppression de l'aliment: ${food.name}", e)
            throw e
        }
    }

    suspend fun toggleFavorite(foodId: String, isFavorite: Boolean) {
        try {
            foodDao.updateFavoriteStatus(foodId, isFavorite)
            Log.d(TAG, "Statut favori mis à jour pour l'aliment: $foodId -> $isFavorite")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour du statut favori", e)
            throw e
        }
    }

    // Recherche d'aliments via l'API
    suspend fun searchFoodsOnline(query: String): List<Food> {
        return try {
            Log.d(TAG, "Recherche en ligne: $query")

            // Vérifier si les clés API sont configurées
            if (Constants.EDAMAM_APP_ID == "demo_app_id" || Constants.EDAMAM_APP_KEY == "demo_app_key") {
                Log.w(TAG, "Clés API Edamam non configurées, retour de données de demo")
                return getDemoFoodSearchResults(query)
            }

            val response = ApiClient.nutritionService.searchFood(
                Constants.EDAMAM_APP_ID,
                Constants.EDAMAM_APP_KEY,
                query
            )

            if (response.isSuccessful) {
                val searchResponse = response.body()
                val foodList = mutableListOf<Food>()

                // Convertir les résultats en objets Food
                searchResponse?.foodItems?.forEach { item ->
                    val food = item.food.toFood()
                    foodList.add(food)
                }

                // Sauvegarder en base de données locale
                if (foodList.isNotEmpty()) {
                    foodDao.insertFoods(foodList)
                    Log.d(TAG, "Ajout de ${foodList.size} aliments de l'API en base locale")
                }

                foodList
            } else {
                Log.e(TAG, "Erreur API: ${response.code()} - ${response.errorBody()?.string()}")
                // Retourner des données de demo en cas d'erreur
                getDemoFoodSearchResults(query)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception lors de la recherche en ligne", e)
            // Retourner des données de demo en cas d'erreur
            getDemoFoodSearchResults(query)
        }
    }

    private suspend fun getDemoFoodSearchResults(query: String): List<Food> {
        // Retourner des aliments de demo basés sur la recherche
        val demoFoods = listOf(
            Food(
                id = "demo_${query}_1",
                name = "$query (Demo)",
                calories = 100,
                protein = 5f,
                carbs = 15f,
                fat = 2f,
                fiber = 3f,
                sugar = 8f,
                servingSize = 100f,
                servingUnit = "g"
            ),
            Food(
                id = "demo_${query}_2",
                name = "$query cuit (Demo)",
                calories = 120,
                protein = 6f,
                carbs = 18f,
                fat = 3f,
                fiber = 2f,
                sugar = 5f,
                servingSize = 100f,
                servingUnit = "g"
            )
        )

        // Sauvegarder les données de demo
        try {
            foodDao.insertFoods(demoFoods)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'insertion des données de demo", e)
        }

        return demoFoods
    }

    // Pour les fonctions de synchronisation
    suspend fun getUnsyncedFoods(): List<Food> {
        return try {
            foodDao.getUnsyncedFoods()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des aliments non synchronisés", e)
            emptyList()
        }
    }

    suspend fun markFoodsAsSynced(foodIds: List<String>) {
        try {
            foodDao.markFoodsAsSynced(foodIds)
            Log.d(TAG, "Marqué ${foodIds.size} aliments comme synchronisés")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors du marquage de synchronisation", e)
        }
    }
}