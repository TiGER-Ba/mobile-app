package com.example.mealplanner.ui.recipes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplanner.data.model.Food
import com.example.mealplanner.data.model.Recipe
import com.example.mealplanner.data.model.RecipeIngredient
import com.example.mealplanner.data.model.RecipeWithIngredients
import com.example.mealplanner.data.repository.FoodRepository
import com.example.mealplanner.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val foodRepository: FoodRepository
) : ViewModel() {

    // Pour liste des recettes
    val allRecipes: Flow<List<Recipe>> = recipeRepository.getAllRecipes()
    val favoriteRecipes: Flow<List<Recipe>> = recipeRepository.getFavoriteRecipes()

    // Pour la recherche
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // État de message (erreurs, succès)
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    // Pour la recette actuellement sélectionnée
    private val _selectedRecipeId = MutableLiveData<String?>()
    val selectedRecipeId: LiveData<String?> = _selectedRecipeId

    // Pour les ingrédients temporaires lors de la création/édition
    private val _tempIngredients = MutableLiveData<List<RecipeIngredientWithFood>>(emptyList())
    val tempIngredients: LiveData<List<RecipeIngredientWithFood>> = _tempIngredients

    // Pour la recherche d'aliments comme ingrédients
    private val _foundFoods = MutableLiveData<List<Food>>(emptyList())
    val foundFoods: LiveData<List<Food>> = _foundFoods

    fun searchRecipes(query: String) {
        _searchQuery.value = query
    }

    fun clearMessage() {
        _message.value = null
    }

    fun selectRecipe(recipeId: String) {
        _selectedRecipeId.value = recipeId
    }

    fun clearSelectedRecipe() {
        _selectedRecipeId.value = null
        _tempIngredients.value = emptyList()
    }



    fun toggleFavorite(recipeId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            recipeRepository.toggleFavorite(recipeId, isFavorite)
        }
    }



    fun searchFoodsForIngredients(query: String) {
        viewModelScope.launch {
            try {
                val foods = foodRepository.searchLocalFoods(query)
                foods.collect {
                    _foundFoods.value = it
                }
            } catch (e: Exception) {
                _message.value = "Erreur de recherche: ${e.message}"
            }
        }
    }

    fun addTempIngredient(foodId: String, quantity: Float, unit: String) {
        viewModelScope.launch {
            val food = foodRepository.getFoodById(foodId)
            if (food != null) {
                val newIngredient = RecipeIngredient(
                    id = UUID.randomUUID().toString(),
                    recipeId = _selectedRecipeId.value ?: "",
                    foodId = foodId,
                    quantity = quantity,
                    unit = unit
                )

                val current = _tempIngredients.value ?: emptyList()
                _tempIngredients.value = current + RecipeIngredientWithFood(newIngredient, food)
            } else {
                _message.value = "Aliment introuvable"
            }
        }
    }

    fun removeTempIngredient(index: Int) {
        val current = _tempIngredients.value ?: emptyList()
        if (index in current.indices) {
            _tempIngredients.value = current.toMutableList().apply { removeAt(index) }
        }
    }

    fun saveRecipe(
        name: String,
        description: String,
        instructions: String,
        prepTime: Int,
        cookTime: Int,
        servings: Int,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                val recipeId = _selectedRecipeId.value ?: UUID.randomUUID().toString()

                val recipe = Recipe(
                    id = recipeId,
                    name = name,
                    description = description,
                    instructions = instructions,
                    preparationTime = prepTime,
                    cookingTime = cookTime,
                    servings = servings,
                    tags = tags,
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false
                )

                val ingredients = _tempIngredients.value?.map { it.ingredient } ?: emptyList()

                recipeRepository.insertRecipe(recipe, ingredients)

                _message.value = "Recette enregistrée avec succès"
                clearSelectedRecipe()
            } catch (e: Exception) {
                _message.value = "Erreur lors de l'enregistrement: ${e.message}"
            }
        }
    }

    fun loadRecipeIngredientsForEdit(recipeId: String) {
        viewModelScope.launch {
            try {
                val recipeWithIngredients = recipeRepository.getRecipeWithIngredients(recipeId)
                recipeWithIngredients.observeForever { recipeWithIngredients ->
                    if (recipeWithIngredients != null) {
                        viewModelScope.launch {
                            val ingredientsWithFood = mutableListOf<RecipeIngredientWithFood>()

                            for (ingredient in recipeWithIngredients.ingredients) {
                                val food = foodRepository.getFoodById(ingredient.foodId)
                                if (food != null) {
                                    ingredientsWithFood.add(
                                        RecipeIngredientWithFood(ingredient, food)
                                    )
                                }
                            }

                            _tempIngredients.value = ingredientsWithFood
                        }
                    }
                }
            } catch (e: Exception) {
                _message.value = "Erreur lors du chargement des ingrédients: ${e.message}"
            }
        }
    }
    // Dans RecipeViewModel.kt, ajoutez ces méthodes :

    fun getRecipeDetails(recipeId: String): LiveData<RecipeWithIngredients?> {
        return recipeRepository.getRecipeWithIngredients(recipeId)
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                val recipe = recipeRepository.getRecipeById(recipeId)
                if (recipe != null) {
                    recipeRepository.deleteRecipe(recipe)
                    _message.value = "Recette supprimée avec succès"
                } else {
                    _message.value = "Recette introuvable"
                }
            } catch (e: Exception) {
                _message.value = "Erreur lors de la suppression: ${e.message}"
            }
        }
    }
}

// Classe d'aide pour afficher les ingrédients avec les informations de l'aliment
data class RecipeIngredientWithFood(
    val ingredient: RecipeIngredient,
    val food: Food
)