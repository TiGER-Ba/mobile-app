package com.example.mealplanner.di

import android.content.Context
import androidx.work.WorkManager
import com.example.mealplanner.data.api.ApiClient
import com.example.mealplanner.data.api.NutritionApiService
import com.example.mealplanner.data.database.AppDatabase
import com.example.mealplanner.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return try {
            AppDatabase.getDatabase(context)
        } catch (e: Exception) {
            // En cas d'erreur, recréer la base de données
            context.deleteDatabase("meal_planner_database")
            AppDatabase.getDatabase(context)
        }
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(appDatabase: AppDatabase): UserProfileDao {
        return appDatabase.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideFoodDao(appDatabase: AppDatabase): FoodDao {
        return appDatabase.foodDao()
    }

    @Provides
    @Singleton
    fun provideRecipeDao(appDatabase: AppDatabase): RecipeDao {
        return appDatabase.recipeDao()
    }

    @Provides
    @Singleton
    fun provideRecipeIngredientDao(appDatabase: AppDatabase): RecipeIngredientDao {
        return appDatabase.recipeIngredientDao()
    }

    @Provides
    @Singleton
    fun provideMealPlanDao(appDatabase: AppDatabase): MealPlanDao {
        return appDatabase.mealPlanDao()
    }

    @Provides
    @Singleton
    fun provideMealDao(appDatabase: AppDatabase): MealDao {
        return appDatabase.mealDao()
    }

    @Provides
    @Singleton
    fun provideMealItemDao(appDatabase: AppDatabase): MealItemDao {
        return appDatabase.mealItemDao()
    }

    @Provides
    @Singleton
    fun provideNutritionApiService(): NutritionApiService {
        return try {
            ApiClient.nutritionService
        } catch (e: Exception) {
            // Fallback si l'API ne peut pas être initialisée
            throw RuntimeException("Impossible d'initialiser l'API", e)
        }
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return try {
            WorkManager.getInstance(context)
        } catch (e: Exception) {
            // Si WorkManager ne peut pas être initialisé, on continue sans
            throw RuntimeException("Impossible d'initialiser WorkManager", e)
        }
    }
}