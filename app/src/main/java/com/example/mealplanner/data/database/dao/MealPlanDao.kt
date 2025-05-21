package com.example.mealplanner.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mealplanner.data.model.Meal
import com.example.mealplanner.data.model.MealItem
import com.example.mealplanner.data.model.MealPlan
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface MealPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlan)

    @Update
    suspend fun updateMealPlan(mealPlan: MealPlan)

    @Delete
    suspend fun deleteMealPlan(mealPlan: MealPlan)

    @Query("SELECT * FROM meal_plans WHERE id = :id")
    suspend fun getMealPlanById(id: String): MealPlan?

    @Query("SELECT * FROM meal_plans WHERE date = :date")
    suspend fun getMealPlanByDate(date: Long): MealPlan?

    @Query("SELECT * FROM meal_plans WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getMealPlansBetweenDates(startDate: Long, endDate: Long): Flow<List<MealPlan>>

    @Query("SELECT * FROM meal_plans WHERE isSynced = 0")
    suspend fun getUnsyncedMealPlans(): List<MealPlan>

    @Query("UPDATE meal_plans SET isSynced = 1 WHERE id IN (:mealPlanIds)")
    suspend fun markMealPlansAsSynced(mealPlanIds: List<String>)
}

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal): Long

    @Update
    suspend fun updateMeal(meal: Meal)

    @Delete
    suspend fun deleteMeal(meal: Meal)

    @Query("SELECT * FROM meals WHERE mealPlanId = :mealPlanId ORDER BY time ASC")
    fun getMealsForMealPlan(mealPlanId: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE id = :mealId")
    suspend fun getMealById(mealId: String): Meal?

    @Query("SELECT * FROM meals WHERE isSynced = 0")
    suspend fun getUnsyncedMeals(): List<Meal>

    @Query("UPDATE meals SET isSynced = 1 WHERE id IN (:mealIds)")
    suspend fun markMealsAsSynced(mealIds: List<String>)
}

@Dao
interface MealItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealItem(mealItem: MealItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealItems(mealItems: List<MealItem>)

    @Update
    suspend fun updateMealItem(mealItem: MealItem)

    @Delete
    suspend fun deleteMealItem(mealItem: MealItem)

    @Query("SELECT * FROM meal_items WHERE mealId = :mealId")
    fun getMealItemsForMeal(mealId: String): Flow<List<MealItem>>

    @Query("DELETE FROM meal_items WHERE mealId = :mealId")
    suspend fun deleteMealItemsForMeal(mealId: String)

    @Query("SELECT * FROM meal_items WHERE isSynced = 0")
    suspend fun getUnsyncedMealItems(): List<MealItem>

    @Query("UPDATE meal_items SET isSynced = 1 WHERE id IN (:mealItemIds)")
    suspend fun markMealItemsAsSynced(mealItemIds: List<String>)
}