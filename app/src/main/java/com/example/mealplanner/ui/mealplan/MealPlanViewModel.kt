package com.example.mealplanner.ui.mealplan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplanner.data.model.Food
import com.example.mealplanner.data.model.Meal
import com.example.mealplanner.data.model.MealItem
import com.example.mealplanner.data.model.MealPlan
import com.example.mealplanner.data.model.MealType
import com.example.mealplanner.data.model.Recipe
import com.example.mealplanner.data.repository.FoodRepository
import com.example.mealplanner.data.repository.MealPlanRepository
import com.example.mealplanner.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val foodRepository: FoodRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    // Date sélectionnée
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate

    // Plan de repas pour la date sélectionnée
    private val _currentMealPlan = MutableLiveData<MealPlan>()
    val currentMealPlan: LiveData<MealPlan> = _currentMealPlan

    // Repas du plan sélectionné
    private val _mealsForCurrentPlan = MutableStateFlow<List<Meal>>(emptyList())
    val mealsForCurrentPlan: StateFlow<List<Meal>> = _mealsForCurrentPlan

    // État de message
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    // Repas sélectionné pour ajout/édition d'items
    private val _selectedMealId = MutableLiveData<String?>()
    val selectedMealId: LiveData<String?> = _selectedMealId

    // Items pour le repas sélectionné
    private val _mealItems = MutableStateFlow<List<MealItemDetails>>(emptyList())
    val mealItems: StateFlow<List<MealItemDetails>> = _mealItems

    fun selectDate(date: Long) {
        _selectedDate.value = date
        loadMealPlanForSelectedDate()
    }

    private fun loadMealPlanForSelectedDate() {
        viewModelScope.launch {
            try {
                val mealPlan = mealPlanRepository.getMealPlanForDate(_selectedDate.value)
                _currentMealPlan.value = mealPlan

                // Charger les repas pour ce plan
                mealPlanRepository.getMealsForMealPlan(mealPlan.id).collect { meals ->
                    _mealsForCurrentPlan.value = meals
                }
            } catch (e: Exception) {
                _message.value = "Erreur lors du chargement du plan: ${e.message}"
            }
        }
    }

    fun selectMeal(mealId: String?) {
        _selectedMealId.value = mealId
        if (mealId != null) {
            loadMealItems(mealId)
        } else {
            _mealItems.value = emptyList()
        }
    }

    private fun loadMealItems(mealId: String) {
        viewModelScope.launch {
            try {
                mealPlanRepository.getMealItemsForMeal(mealId).collect { items ->
                    val detailedItems = items.mapNotNull { item ->
                        // Pour chaque item, récupérer les détails de l'aliment ou de la recette
                        if (item.foodId != null) {
                            val food = foodRepository.getFoodById(item.foodId)
                            if (food != null) {
                                MealItemDetails(
                                    item = item,
                                    food = food,
                                    recipe = null,
                                    name = food.name,
                                    calories = calculateFoodCalories(food, item.quantity, item.servingSize)
                                )
                            } else null
                        } else if (item.recipeId != null) {
                            val recipe = recipeRepository.getRecipeById(item.recipeId)
                            if (recipe != null) {
                                MealItemDetails(
                                    item = item,
                                    food = null,
                                    recipe = recipe,
                                    name = recipe.name,
                                    calories = calculateRecipeCalories(recipe, item.quantity)
                                )
                            } else null
                        } else null
                    }
                    _mealItems.value = detailedItems
                }
            } catch (e: Exception) {
                _message.value = "Erreur lors du chargement des items: ${e.message}"
            }
        }
    }

    fun addMeal(type: MealType, time: Long, name: String = "", notes: String = "") {
        viewModelScope.launch {
            try {
                val mealPlanId = _currentMealPlan.value?.id
                    ?: mealPlanRepository.getMealPlanForDate(_selectedDate.value).id

                val mealId = mealPlanRepository.addMealToMealPlan(
                    mealPlanId = mealPlanId,
                    mealType = type,
                    time = time,
                    name = name,
                    notes = notes  // Ajout du paramètre notes
                )

                _message.value = "Repas ajouté"
                selectMeal(mealId) // Sélectionner le nouveau repas
            } catch (e: Exception) {
                _message.value = "Erreur lors de l'ajout du repas: ${e.message}"
            }
        }
    }

    fun updateMeal(mealId: String, type: MealType, time: Long, name: String, notes: String) {
        viewModelScope.launch {
            try {
                val meal = mealPlanRepository.getMealById(mealId)
                if (meal != null) {
                    val updated = meal.copy(
                        type = type,
                        time = time,
                        name = name,
                        notes = notes,
                        isSynced = false
                    )
                    mealPlanRepository.updateMeal(updated)
                    _message.value = "Repas mis à jour"
                }
            } catch (e: Exception) {
                _message.value = "Erreur lors de la mise à jour: ${e.message}"
            }
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            try {
                val meal = mealPlanRepository.getMealById(mealId)
                if (meal != null) {
                    mealPlanRepository.deleteMeal(meal)
                    _message.value = "Repas supprimé"
                    selectMeal(null) // Désélectionner
                }
            } catch (e: Exception) {
                _message.value = "Erreur lors de la suppression: ${e.message}"
            }
        }
    }

    fun addFoodToMeal(mealId: String, foodId: String, quantity: Float, servingSize: Float) {
        viewModelScope.launch {
            try {
                mealPlanRepository.addItemToMeal(
                    mealId = mealId,
                    foodId = foodId,
                    recipeId = null,
                    quantity = quantity,
                    servingSize = servingSize
                )
                _message.value = "Aliment ajouté au repas"
            } catch (e: Exception) {
                _message.value = "Erreur lors de l'ajout: ${e.message}"
            }
        }
    }

    fun addRecipeToMeal(mealId: String, recipeId: String, servings: Float) {
        viewModelScope.launch {
            try {
                mealPlanRepository.addItemToMeal(
                    mealId = mealId,
                    foodId = null,
                    recipeId = recipeId,
                    quantity = servings,
                    servingSize = 1f // Pour les recettes, on utilise le nombre de portions
                )
                _message.value = "Recette ajoutée au repas"
            } catch (e: Exception) {
                _message.value = "Erreur lors de l'ajout: ${e.message}"
            }
        }
    }

    fun updateMealItem(mealItem: MealItem, quantity: Float, servingSize: Float) {
        viewModelScope.launch {
            try {
                val updated = mealItem.copy(
                    quantity = quantity,
                    servingSize = servingSize,
                    isSynced = false
                )
                mealPlanRepository.updateMealItem(updated)
                _message.value = "Item mis à jour"
            } catch (e: Exception) {
                _message.value = "Erreur lors de la mise à jour: ${e.message}"
            }
        }
    }

    fun deleteMealItem(mealItemId: String) {
        viewModelScope.launch {
            try {
                val mealItems = _mealItems.value
                val item = mealItems.find { it.item.id == mealItemId }?.item
                if (item != null) {
                    mealPlanRepository.removeMealItem(item)
                    _message.value = "Item supprimé"
                }
            } catch (e: Exception) {
                _message.value = "Erreur lors de la suppression: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    // Fonction pour calculer les calories d'un aliment en fonction de la quantité et de la portion
    private fun calculateFoodCalories(food: Food, quantity: Float, servingSize: Float): Int {
        val servingRatio = quantity * servingSize / 100f // Standardiser à 100g
        return (food.calories * servingRatio).toInt()
    }

    // Calcul approximatif des calories d'une recette
    // Dans une implémentation réelle, vous calculeriez cela en additionnant les calories de tous les ingrédients
    private fun calculateRecipeCalories(recipe: Recipe, servings: Float): Int {
        // Cette fonction est un placeholder. Vous implementeriez ici la logique réelle.
        return (500 * servings).toInt() // Placeholder: 500 kcal par portion
    }
}

// Classe pour afficher les détails d'un item de repas
data class MealItemDetails(
    val item: MealItem,
    val food: Food?,
    val recipe: Recipe?,
    val name: String,
    val calories: Int
)