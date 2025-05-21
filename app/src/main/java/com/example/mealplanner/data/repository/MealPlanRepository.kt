package com.example.mealplanner.data.repository

import com.example.mealplanner.data.database.dao.MealDao
import com.example.mealplanner.data.database.dao.MealItemDao
import com.example.mealplanner.data.database.dao.MealPlanDao
import com.example.mealplanner.data.model.Meal
import com.example.mealplanner.data.model.MealItem
import com.example.mealplanner.data.model.MealPlan
import com.example.mealplanner.data.model.MealType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealPlanRepository @Inject constructor(
    private val mealPlanDao: MealPlanDao,
    private val mealDao: MealDao,
    private val mealItemDao: MealItemDao
) {
    // Récupère ou crée un plan de repas pour une date donnée
    suspend fun getMealPlanForDate(date: Long): MealPlan {
        val startOfDay = getStartOfDay(date)
        var mealPlan = mealPlanDao.getMealPlanByDate(startOfDay)

        if (mealPlan == null) {
            mealPlan = MealPlan(
                date = startOfDay,
                isSynced = false
            )
            mealPlanDao.insertMealPlan(mealPlan)
        }

        return mealPlan
    }

    fun getMealPlansBetweenDates(startDate: Long, endDate: Long): Flow<List<MealPlan>> {
        return mealPlanDao.getMealPlansBetweenDates(startDate, endDate)
    }

    fun getMealsForMealPlan(mealPlanId: String): Flow<List<Meal>> {
        return mealDao.getMealsForMealPlan(mealPlanId)
    }

    suspend fun getMealById(mealId: String): Meal? {
        return mealDao.getMealById(mealId)
    }

    fun getMealItemsForMeal(mealId: String): Flow<List<MealItem>> {
        return mealItemDao.getMealItemsForMeal(mealId)
    }

    suspend fun addMealToMealPlan(
        mealPlanId: String,
        mealType: MealType,
        time: Long,
        name: String = "",
        notes: String = ""
    ): String {
        val meal = Meal(
            mealPlanId = mealPlanId,
            type = mealType,
            time = time,
            name = name,
            notes = notes
        )
        mealDao.insertMeal(meal)
        return meal.id
    }

    suspend fun updateMeal(meal: Meal) {
        mealDao.updateMeal(meal)
    }

    suspend fun addItemToMeal(
        mealId: String,
        foodId: String?,
        recipeId: String?,
        quantity: Float,
        servingSize: Float
    ) {
        val mealItem = MealItem(
            mealId = mealId,
            foodId = foodId,
            recipeId = recipeId,
            quantity = quantity,
            servingSize = servingSize
        )
        mealItemDao.insertMealItem(mealItem)
    }

    suspend fun updateMealItem(mealItem: MealItem) {
        mealItemDao.updateMealItem(mealItem)
    }

    suspend fun removeMealItem(mealItem: MealItem) {
        mealItemDao.deleteMealItem(mealItem)
    }

    suspend fun deleteMeal(meal: Meal) {
        mealItemDao.deleteMealItemsForMeal(meal.id)
        mealDao.deleteMeal(meal)
    }

    suspend fun deleteMealPlan(mealPlan: MealPlan) {
        val meals = mealDao.getMealsForMealPlan(mealPlan.id)
        meals.collect { mealList ->
            mealList.forEach { meal ->
                mealItemDao.deleteMealItemsForMeal(meal.id)
            }
        }
        mealPlanDao.deleteMealPlan(mealPlan)
    }

    // Pour les fonctions de synchronisation
    suspend fun getUnsyncedMealPlans(): List<MealPlan> {
        return mealPlanDao.getUnsyncedMealPlans()
    }

    suspend fun getUnsyncedMeals(): List<Meal> {
        return mealDao.getUnsyncedMeals()
    }

    suspend fun getUnsyncedMealItems(): List<MealItem> {
        return mealItemDao.getUnsyncedMealItems()
    }

    suspend fun markMealPlansAsSynced(mealPlanIds: List<String>) {
        mealPlanDao.markMealPlansAsSynced(mealPlanIds)
    }

    suspend fun markMealsAsSynced(mealIds: List<String>) {
        mealDao.markMealsAsSynced(mealIds)
    }

    suspend fun markMealItemsAsSynced(mealItemIds: List<String>) {
        mealItemDao.markMealItemsAsSynced(mealItemIds)
    }

    // Utilitaire pour obtenir le début de la journée (minuit)
    private fun getStartOfDay(time: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}