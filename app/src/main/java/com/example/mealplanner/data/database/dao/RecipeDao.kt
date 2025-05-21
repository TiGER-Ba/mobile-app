package com.example.mealplanner.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mealplanner.data.model.Recipe
import com.example.mealplanner.data.model.RecipeIngredient
import com.example.mealplanner.data.model.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: String): Recipe?

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeByIdLiveData(recipeId: String): LiveData<Recipe?>

    @Query("SELECT * FROM recipes WHERE favorite = 1 ORDER BY name ASC")
    fun getFavoriteRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchRecipes(query: String): Flow<List<Recipe>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeWithIngredients(recipeId: String): LiveData<RecipeWithIngredients?>

    @Transaction
    @Query("SELECT * FROM recipes")
    fun getAllRecipesWithIngredients(): Flow<List<RecipeWithIngredients>>

    @Query("UPDATE recipes SET favorite = :isFavorite WHERE id = :recipeId")
    suspend fun updateFavoriteStatus(recipeId: String, isFavorite: Boolean)

    @Query("SELECT * FROM recipes WHERE isSynced = 0")
    suspend fun getUnsyncedRecipes(): List<Recipe>

    @Query("UPDATE recipes SET isSynced = 1 WHERE id IN (:recipeIds)")
    suspend fun markRecipesAsSynced(recipeIds: List<String>)
}

@Dao
interface RecipeIngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: RecipeIngredient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<RecipeIngredient>)

    @Update
    suspend fun updateIngredient(ingredient: RecipeIngredient)

    @Delete
    suspend fun deleteIngredient(ingredient: RecipeIngredient)

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    fun getIngredientsForRecipe(recipeId: String): Flow<List<RecipeIngredient>>

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: String)
}