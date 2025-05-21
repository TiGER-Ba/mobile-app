package com.example.mealplanner.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.mealplanner.data.api.ApiClient
import com.example.mealplanner.data.api.toFood
import com.example.mealplanner.data.database.dao.FoodDao
import com.example.mealplanner.data.model.Food
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(
    private val foodDao: FoodDao
) {
    companion object {
        private const val APP_ID = "YOUR_EDAMAM_APP_ID" // Remplacer par votre APP ID Edamam
        private const val APP_KEY = "YOUR_EDAMAM_APP_KEY" // Remplacer par votre APP KEY Edamam
        private const val TAG = "FoodRepository"
    }

    fun getAllFoods(): Flow<List<Food>> {
        return foodDao.getAllFoods()
    }

    fun getFavoriteFoods(): Flow<List<Food>> {
        return foodDao.getFavoriteFoods()
    }

    fun searchLocalFoods(query: String): Flow<List<Food>> {
        return foodDao.searchFoods(query)
    }

    suspend fun getFoodById(id: String): Food? {
        return foodDao.getFoodById(id)
    }

    suspend fun insertFood(food: Food) {
        foodDao.insertFood(food)
    }

    suspend fun updateFood(food: Food) {
        foodDao.updateFood(food)
    }

    suspend fun deleteFood(food: Food) {
        foodDao.deleteFood(food)
    }

    suspend fun toggleFavorite(foodId: String, isFavorite: Boolean) {
        foodDao.updateFavoriteStatus(foodId, isFavorite)
    }

    // Recherche d'aliments via l'API
    suspend fun searchFoodsOnline(query: String): List<Food> {
        return try {
            val response = ApiClient.nutritionService.searchFood(APP_ID, APP_KEY, query)

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
                }

                foodList
            } else {
                Log.e(TAG, "Error searching foods: ${response.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception searching foods", e)
            emptyList()
        }
    }

    // Pour les fonctions de synchronisation
    suspend fun getUnsyncedFoods(): List<Food> {
        return foodDao.getUnsyncedFoods()
    }

    suspend fun markFoodsAsSynced(foodIds: List<String>) {
        foodDao.markFoodsAsSynced(foodIds)
    }
    // Dans FoodRepository.kt, ajoutez cette méthode :

    fun getFoodByIdLiveData(id: String): LiveData<Food?> {
        return foodDao.getFoodByIdLiveData(id)
    }
}