package com.example.mealplanner.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplanner.data.model.Food
import com.example.mealplanner.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    // État de recherche
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Résultats de recherche
    val searchResults: Flow<List<Food>> = foodRepository.getAllFoods()

    // État de chargement pour recherches en ligne
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // État de message (erreurs, succès)
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    // Liste pour stocker les résultats de recherche en ligne temporaires
    private val _onlineSearchResults = MutableLiveData<List<Food>>()
    val onlineSearchResults: LiveData<List<Food>> = _onlineSearchResults

    fun searchFoods(query: String, searchOnline: Boolean = false) {
        _searchQuery.value = query

        if (searchOnline) {
            searchFoodsOnline(query)
        }
    }

    private fun searchFoodsOnline(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val results = foodRepository.searchFoodsOnline(query)
                _onlineSearchResults.value = results

                if (results.isEmpty()) {
                    _message.value = "Aucun résultat trouvé en ligne"
                }
            } catch (e: Exception) {
                _message.value = "Erreur lors de la recherche: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun toggleFavorite(food: Food) {
        viewModelScope.launch {
            foodRepository.toggleFavorite(food.id, !food.favorite)
        }
    }

    fun addCustomFood(
        name: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float,
        fiber: Float,
        sugar: Float,
        servingSize: Float,
        servingUnit: String
    ) {
        viewModelScope.launch {
            try {
                val food = Food(
                    id = name.replace(" ", "_").lowercase() + "_" + System.currentTimeMillis(),
                    name = name,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    fiber = fiber,
                    sugar = sugar,
                    servingSize = servingSize,
                    servingUnit = servingUnit,
                    servingSizeOptions = listOf(servingSize),
                    isSynced = false
                )

                foodRepository.insertFood(food)
                _message.value = "Aliment ajouté avec succès"
            } catch (e: Exception) {
                _message.value = "Erreur lors de l'ajout: ${e.message}"
            }
        }
    }
    // Ajoutez ces méthodes dans FoodViewModel.kt

    fun getFoodByIdLiveData(foodId: String): LiveData<Food?> {
        return foodRepository.getFoodByIdLiveData(foodId)
    }

    private val _selectedFood = MutableLiveData<Food?>()
    val selectedFood: LiveData<Food?> = _selectedFood

    fun selectFood(foodId: String) {
        viewModelScope.launch {
            _selectedFood.value = foodRepository.getFoodById(foodId)
        }
    }
}