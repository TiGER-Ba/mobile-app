package com.example.mealplanner.ui.search

import android.util.Log
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

    companion object {
        private const val TAG = "FoodViewModel"
    }

    // État de recherche
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Résultats de recherche
    val searchResults: Flow<List<Food>> = foodRepository.getAllFoods()

    // État de chargement pour recherches en ligne
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // CORRECTION: État de message avec limitation des répétitions
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message
    private var lastMessage: String? = null
    private var lastMessageTime: Long = 0

    // Liste pour stocker les résultats de recherche en ligne temporaires
    private val _onlineSearchResults = MutableLiveData<List<Food>>()
    val onlineSearchResults: LiveData<List<Food>> = _onlineSearchResults

    fun searchFoods(query: String, searchOnline: Boolean = false) {
        _searchQuery.value = query

        if (searchOnline && query.isNotEmpty()) {
            searchFoodsOnline(query)
        }
    }

    private fun searchFoodsOnline(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Recherche en ligne: $query")

                val results = foodRepository.searchFoodsOnline(query)
                _onlineSearchResults.value = results

                if (results.isEmpty()) {
                    showMessage("Aucun résultat trouvé en ligne")
                } else {
                    Log.d(TAG, "Trouvé ${results.size} résultats en ligne")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de la recherche en ligne", e)
                showMessage("Erreur lors de la recherche: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // CORRECTION: Méthode pour éviter les messages répétés
    private fun showMessage(newMessage: String) {
        val currentTime = System.currentTimeMillis()
        if (newMessage != lastMessage || currentTime - lastMessageTime > 3000) { // 3 secondes minimum
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

    fun toggleFavorite(food: Food) {
        viewModelScope.launch {
            try {
                foodRepository.toggleFavorite(food.id, !food.favorite)
                Log.d(TAG, "Statut favori modifié pour: ${food.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de la modification du favori", e)
                showMessage("Erreur lors de la modification")
            }
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
                    id = "custom_" + name.replace(" ", "_").lowercase() + "_" + System.currentTimeMillis(),
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
                showMessage("Aliment ajouté avec succès")
                Log.d(TAG, "Aliment personnalisé ajouté: ${food.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'ajout de l'aliment", e)
                showMessage("Erreur lors de l'ajout: ${e.message}")
            }
        }
    }

    // CORRECTION: Méthode pour ajouter directement un aliment (pour les données de test)
    suspend fun addCustomFoodDirect(food: Food) {
        try {
            // Vérifier si l'aliment existe déjà
            val existingFood = foodRepository.getFoodById(food.id)
            if (existingFood == null) {
                foodRepository.insertFood(food)
                Log.d(TAG, "Aliment de test ajouté: ${food.name}")
            } else {
                Log.d(TAG, "Aliment déjà présent: ${food.name}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erreur lors de l'ajout de l'aliment ${food.name}: ${e.message}")
            // Ne pas afficher de toast pour les erreurs de données de test
        }
    }

    fun getFoodByIdLiveData(foodId: String): LiveData<Food?> {
        return foodRepository.getFoodByIdLiveData(foodId)
    }

    private val _selectedFood = MutableLiveData<Food?>()
    val selectedFood: LiveData<Food?> = _selectedFood

    fun selectFood(foodId: String) {
        viewModelScope.launch {
            try {
                _selectedFood.value = foodRepository.getFoodById(foodId)
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de la sélection de l'aliment", e)
                showMessage("Erreur lors de la sélection")
            }
        }
    }
}