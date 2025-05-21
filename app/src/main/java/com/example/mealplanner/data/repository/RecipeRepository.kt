package com.example.mealplanner.data.repository

import androidx.lifecycle.LiveData
import com.example.mealplanner.data.database.dao.RecipeDao
import com.example.mealplanner.data.database.dao.RecipeIngredientDao
import com.example.mealplanner.data.model.Recipe
import com.example.mealplanner.data.model.RecipeIngredient
import com.example.mealplanner.data.model.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao
) {
    fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes()
    }

    fun getFavoriteRecipes(): Flow<List<Recipe>> {
        return recipeDao.getFavoriteRecipes()
    }

    fun searchRecipes(query: String): Flow<List<Recipe>> {
        return recipeDao.searchRecipes(query)
    }

    fun getRecipeWithIngredients(recipeId: String): LiveData<RecipeWithIngredients?> {
        return recipeDao.getRecipeWithIngredients(recipeId)
    }

    fun getAllRecipesWithIngredients(): Flow<List<RecipeWithIngredients>> {
        return recipeDao.getAllRecipesWithIngredients()
    }

    suspend fun getRecipeById(recipeId: String): Recipe? {
        return recipeDao.getRecipeById(recipeId)
    }

    fun getIngredientsForRecipe(recipeId: String): Flow<List<RecipeIngredient>> {
        return recipeIngredientDao.getIngredientsForRecipe(recipeId)
    }

    suspend fun insertRecipe(recipe: Recipe, ingredients: List<RecipeIngredient>) {
        recipeDao.insertRecipe(recipe)

        // Remplacer tous les ingrédients
        recipeIngredientDao.deleteIngredientsForRecipe(recipe.id)
        recipeIngredientDao.insertIngredients(ingredients)
    }

    suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun addIngredientToRecipe(ingredient: RecipeIngredient) {
        recipeIngredientDao.insertIngredient(ingredient)
    }

    suspend fun updateIngredient(ingredient: RecipeIngredient) {
        recipeIngredientDao.updateIngredient(ingredient)
    }

    suspend fun removeIngredientFromRecipe(ingredient: RecipeIngredient) {
        recipeIngredientDao.deleteIngredient(ingredient)
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeIngredientDao.deleteIngredientsForRecipe(recipe.id)
        recipeDao.deleteRecipe(recipe)
    }

    suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean) {
        recipeDao.updateFavoriteStatus(recipeId, isFavorite)
    }

    // Pour les fonctions de synchronisation
    suspend fun getUnsyncedRecipes(): List<Recipe> {
        return recipeDao.getUnsyncedRecipes()
    }

    suspend fun markRecipesAsSynced(recipeIds: List<String>) {
        recipeDao.markRecipesAsSynced(recipeIds)
    }
}