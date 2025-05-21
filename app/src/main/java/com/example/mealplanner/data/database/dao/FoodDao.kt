package com.example.mealplanner.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mealplanner.data.model.Food
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<Food>)

    @Update
    suspend fun updateFood(food: Food)

    @Delete
    suspend fun deleteFood(food: Food)

    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: String): Food?

    @Query("SELECT * FROM foods WHERE id = :id")
    fun getFoodByIdLiveData(id: String): LiveData<Food?>

    @Query("SELECT * FROM foods WHERE favorite = 1 ORDER BY name ASC")
    fun getFavoriteFoods(): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFoods(query: String): Flow<List<Food>>

    @Query("UPDATE foods SET favorite = :isFavorite WHERE id = :foodId")
    suspend fun updateFavoriteStatus(foodId: String, isFavorite: Boolean)

    @Query("SELECT * FROM foods WHERE isSynced = 0")
    suspend fun getUnsyncedFoods(): List<Food>

    @Query("UPDATE foods SET isSynced = 1 WHERE id IN (:foodIds)")
    suspend fun markFoodsAsSynced(foodIds: List<String>)
}