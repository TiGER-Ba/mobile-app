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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
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

    // Date sélectionnée
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate

    // Plan de repas pour la date sélectionnée
    private val _currentMealPlan = MutableLiveData<MealPlan>()
    val currentMealPlan: LiveData<MealPlan> = _currentMealPlan

    // Repas du plan sélectionné
    private val _mealsForCurrentPlan = MutableStateFlow<List<Meal>>(emptyList())
    val mealsForCurrentPlan: StateFlow<List<Meal>> = _mealsForCurrentPlan

    // État de message - CORRECTION: Empêcher les messages répétés
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message
    private var lastMessage: String? = null
    private var lastMessageTime: Long = 0

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
        viewModelScope.launch {
            try {
                val today = System.currentTimeMillis()
                Log.d(TAG, "📅 Initialisation avec la date d'aujourd'hui: $today")
                selectDate(today)
                _isInitialized.value = true
                Log.d(TAG, "✅ ViewModel initialisé avec succès")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'initialisation", e)
                showMessage("Erreur d'initialisation: ${e.message}")
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
                        showMessage("Erreur lors du chargement des repas")
                        _mealsForCurrentPlan.value = emptyList()
                    }
                    .collect { meals ->
                        Log.d(TAG, "🍽️ Repas chargés: ${meals.size}")
                        _mealsForCurrentPlan.value = meals
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors du chargement du plan", e)
                showMessage("Erreur lors du chargement du plan: ${e.message}")
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
                        showMessage("Erreur lors du chargement des items")
                        _mealItems.value = emptyList()
                    }
                    .collect { items ->
                        Log.d(TAG, "🥘 Items chargés: ${items.size}")
                        val detailedItems = items.mapNotNull { item ->
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
                showMessage("Erreur lors du chargement des items: ${e.message}")
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

                showMessage("Repas ajouté avec succès")
                selectMeal(mealId)

                Log.d(TAG, "✅ Repas ajouté: $mealId")
                loadMealPlanForSelectedDate()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'ajout du repas", e)
                showMessage("Erreur lors de l'ajout du repas: ${e.message}")
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
                    showMessage("Repas mis à jour")
                    Log.d(TAG, "✅ Repas mis à jour")
                    loadMealPlanForSelectedDate()
                } else {
                    Log.w(TAG, "⚠️ Repas non trouvé pour mise à jour: $mealId")
                    showMessage("Repas non trouvé")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de la mise à jour", e)
                showMessage("Erreur lors de la mise à jour: ${e.message}")
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
                    showMessage("Repas supprimé")
                    selectMeal(null)
                    Log.d(TAG, "✅ Repas supprimé")
                    loadMealPlanForSelectedDate()
                } else {
                    Log.w(TAG, "⚠️ Repas non trouvé pour suppression: $mealId")
                    showMessage("Repas non trouvé")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de la suppression", e)
                showMessage("Erreur lors de la suppression: ${e.message}")
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
                showMessage("Aliment ajouté au repas")
                Log.d(TAG, "✅ Aliment ajouté")
                loadMealItems(mealId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'ajout", e)
                showMessage("Erreur lors de l'ajout: ${e.message}")
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
                    servingSize = 1f
                )
                showMessage("Recette ajoutée au repas")
                Log.d(TAG, "✅ Recette ajoutée")
                loadMealItems(mealId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'ajout", e)
                showMessage("Erreur lors de l'ajout: ${e.message}")
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
                showMessage("Item mis à jour")
                Log.d(TAG, "✅ Item mis à jour")
                _selectedMealId.value?.let { loadMealItems(it) }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de la mise à jour", e)
                showMessage("Erreur lors de la mise à jour: ${e.message}")
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
                    showMessage("Item supprimé")
                    Log.d(TAG, "✅ Item supprimé")
                    _selectedMealId.value?.let { loadMealItems(it) }
                } else {
                    Log.w(TAG, "⚠️ Item non trouvé pour suppression: $mealItemId")
                    showMessage("Item non trouvé")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur lors de la suppression", e)
                showMessage("Erreur lors de la suppression: ${e.message}")
            }
        }
    }

    // CORRECTION: Méthode pour éviter les messages répétés
    private fun showMessage(newMessage: String) {
        val currentTime = System.currentTimeMillis()
        if (newMessage != lastMessage || currentTime - lastMessageTime > 3000) {
            _message.value = newMessage
            lastMessage = newMessage
            lastMessageTime = currentTime
        }
    }

    fun clearMessage() {
        _message.value = null
        lastMessage = null
        lastMessageTime = 0
    }

    private fun calculateFoodCalories(food: Food, quantity: Float, servingSize: Float): Int {
        val servingRatio = quantity * servingSize / 100f
        return (food.calories * servingRatio).toInt()
    }

    // CORRECTION: Calcul amélioré des calories de recette
    private fun calculateRecipeCalories(recipe: Recipe, servings: Float): Int {
        val baseCaloriesPerServing = when {
            recipe.tags.contains("petit-déjeuner") -> 350
            recipe.tags.contains("salade") -> 250
            recipe.tags.contains("protéine") || recipe.tags.contains("viande") -> 450
            recipe.tags.contains("végétarien") -> 300
            recipe.cookingTime > 30 -> 500
            else -> 400
        }

        return (baseCaloriesPerServing * servings).toInt()
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