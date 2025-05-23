package com.example.mealplanner.ui.mealplan

import android.util.Log
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val foodRepository: FoodRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MealPlanViewModel"
    }

    // Date sélectionnée - CORRECTION : Initialiser avec la date actuelle
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

    // État d'initialisation
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    init {
        Log.d(TAG, "🔧 Initialisation du MealPlanViewModel")
        // Charger automatiquement le plan pour aujourd'hui
        viewModelScope.launch {
            try {
                val today = System.currentTimeMillis()
                Log.d(TAG, "📅 Initialisation avec la date d'aujourd'hui: $today")
                selectDate(today)
                _isInitialized.value = true
                Log.d(TAG, "✅ ViewModel initialisé avec succès")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'initialisation", e)
                _message.value = "Erreur d'initialisation: ${e.message}"
            }
        }
    }

    fun selectDate(date: Long) {
        Log.d(TAG, "📅 Sélection de la date: $date")
        _selectedDate.value = date
        loadMealPlanForSelectedDate()
    }

    private fun loadMealPlanForSelectedDate() {
        viewModelScope.launch {
            try {
                val selectedDate = _selectedDate.value
                Log.d(TAG, "🔄 Chargement du plan de repas pour la date: $selectedDate")

                val mealPlan = mealPlanRepository.getMealPlanForDate(selectedDate)
                _currentMealPlan.value = mealPlan
                Log.d(TAG, "📋 Plan de repas chargé: ${mealPlan.id}")

                // Charger les repas pour ce plan
                mealPlanRepository.getMealsForMealPlan(mealPlan.id)
                    .catch { e ->
                        Log.e(TAG, "❌ Erreur lors du chargement des repas", e)
                        _message.value = "Erreur lors du chargement des repas"
                        _mealsForCurrentPlan.value = emptyList()
                    }
                    .collect { meals ->
                        Log.d(TAG, "🍽️ Repas chargés: ${meals.size}")
                        _mealsForCurrentPlan.value = meals
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors du chargement du plan", e)
                _message.value = "Erreur lors du chargement du plan: ${e.message}"
                _mealsForCurrentPlan.value = emptyList()
            }
        }
    }

    fun selectMeal(mealId: String?) {
        Log.d(TAG, "🍽️ Sélection du repas: $mealId")
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
                Log.d(TAG, "🔄 Chargement des items pour le repas: $mealId")
                mealPlanRepository.getMealItemsForMeal(mealId)
                    .catch { e ->
                        Log.e(TAG, "❌ Erreur lors du chargement des items", e)
                        _message.value = "Erreur lors du chargement des items"
                        _mealItems.value = emptyList()
                    }
                    .collect { items ->
                        Log.d(TAG, "🥘 Items chargés: ${items.size}")
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
                                } else {
                                    Log.w(TAG, "⚠️ Aliment non trouvé: ${item.foodId}")
                                    null
                                }
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
                                } else {
                                    Log.w(TAG, "⚠️ Recette non trouvée: ${item.recipeId}")
                                    null
                                }
                            } else {
                                Log.w(TAG, "⚠️ Item sans aliment ni recette: ${item.id}")
                                null
                            }
                        }
                        _mealItems.value = detailedItems
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors du chargement des items", e)
                _message.value = "Erreur lors du chargement des items: ${e.message}"
                _mealItems.value = emptyList()
            }
        }
    }

    fun addMeal(type: MealType, time: Long, name: String = "", notes: String = "") {
        viewModelScope.launch {
            try {
                Log.d(TAG, "➕ Ajout d'un repas: $type à $time")
                val mealPlanId = _currentMealPlan.value?.id
                    ?: mealPlanRepository.getMealPlanForDate(_selectedDate.value).id

                val mealId = mealPlanRepository.addMealToMealPlan(
                    mealPlanId = mealPlanId,
                    mealType = type,
                    time = time,
                    name = name,
                    notes = notes
                )

                _message.value = "Repas ajouté avec succès"
                selectMeal(mealId) // Sélectionner le nouveau repas

                Log.d(TAG, "✅ Repas ajouté: $mealId")

                // Recharger la liste des repas
                loadMealPlanForSelectedDate()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'ajout du repas", e)
                _message.value = "Erreur lors de l'ajout du repas: ${e.message}"
            }
        }
    }

    fun updateMeal(mealId: String, type: MealType, time: Long, name: String, notes: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔄 Mise à jour du repas: $mealId")
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
                    Log.d(TAG, "✅ Repas mis à jour")

                    // Recharger la liste des repas
                    loadMealPlanForSelectedDate()
                } else {
                    Log.w(TAG, "⚠️ Repas non trouvé pour mise à jour: $mealId")
                    _message.value = "Repas non trouvé"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de la mise à jour", e)
                _message.value = "Erreur lors de la mise à jour: ${e.message}"
            }
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🗑️ Suppression du repas: $mealId")
                val meal = mealPlanRepository.getMealById(mealId)
                if (meal != null) {
                    mealPlanRepository.deleteMeal(meal)
                    _message.value = "Repas supprimé"
                    selectMeal(null) // Désélectionner
                    Log.d(TAG, "✅ Repas supprimé")

                    // Recharger la liste des repas
                    loadMealPlanForSelectedDate()
                } else {
                    Log.w(TAG, "⚠️ Repas non trouvé pour suppression: $mealId")
                    _message.value = "Repas non trouvé"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de la suppression", e)
                _message.value = "Erreur lors de la suppression: ${e.message}"
            }
        }
    }

    fun addFoodToMeal(mealId: String, foodId: String, quantity: Float, servingSize: Float) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🥘 Ajout d'aliment au repas: $foodId -> $mealId")
                mealPlanRepository.addItemToMeal(
                    mealId = mealId,
                    foodId = foodId,
                    recipeId = null,
                    quantity = quantity,
                    servingSize = servingSize
                )
                _message.value = "Aliment ajouté au repas"
                Log.d(TAG, "✅ Aliment ajouté")

                // Recharger les items du repas
                loadMealItems(mealId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'ajout", e)
                _message.value = "Erreur lors de l'ajout: ${e.message}"
            }
        }
    }

    fun addRecipeToMeal(mealId: String, recipeId: String, servings: Float) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "👨‍🍳 Ajout de recette au repas: $recipeId -> $mealId")
                mealPlanRepository.addItemToMeal(
                    mealId = mealId,
                    foodId = null,
                    recipeId = recipeId,
                    quantity = servings,
                    servingSize = 1f // Pour les recettes, on utilise le nombre de portions
                )
                _message.value = "Recette ajoutée au repas"
                Log.d(TAG, "✅ Recette ajoutée")

                // Recharger les items du repas
                loadMealItems(mealId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'ajout", e)
                _message.value = "Erreur lors de l'ajout: ${e.message}"
            }
        }
    }

    fun updateMealItem(mealItem: MealItem, quantity: Float, servingSize: Float) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔄 Mise à jour item repas: ${mealItem.id}")
                val updated = mealItem.copy(
                    quantity = quantity,
                    servingSize = servingSize,
                    isSynced = false
                )
                mealPlanRepository.updateMealItem(updated)
                _message.value = "Item mis à jour"
                Log.d(TAG, "✅ Item mis à jour")

                // Recharger les items du repas
                _selectedMealId.value?.let { loadMealItems(it) }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de la mise à jour", e)
                _message.value = "Erreur lors de la mise à jour: ${e.message}"
            }
        }
    }

    fun deleteMealItem(mealItemId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🗑️ Suppression item repas: $mealItemId")
                val mealItems = _mealItems.value
                val item = mealItems.find { it.item.id == mealItemId }?.item
                if (item != null) {
                    mealPlanRepository.removeMealItem(item)
                    _message.value = "Item supprimé"
                    Log.d(TAG, "✅ Item supprimé")

                    // Recharger les items du repas
                    _selectedMealId.value?.let { loadMealItems(it) }
                } else {
                    Log.w(TAG, "⚠️ Item non trouvé pour suppression: $mealItemId")
                    _message.value = "Item non trouvé"
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de la suppression", e)
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
    private fun calculateRecipeCalories(recipe: Recipe, servings: Float): Int {
        // Cette fonction est un placeholder. Dans une implémentation réelle,
        // vous calculeriez cela en additionnant les calories de tous les ingrédients
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