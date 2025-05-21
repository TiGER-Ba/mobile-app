package com.example.mealplanner.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealplanner.data.model.NutritionGoal
import com.example.mealplanner.data.model.UserProfile
import com.example.mealplanner.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val userProfile: LiveData<UserProfile?> = userRepository.getUserProfileLiveData()

    fun saveUserProfile(
        name: String,
        age: Int,
        gender: String,
        weight: Float,
        height: Float,
        goal: NutritionGoal,
        activityLevel: Int
    ) {
        viewModelScope.launch {
            userRepository.createOrUpdateUserProfile(
                name = name,
                age = age,
                gender = gender,
                weight = weight,
                height = height,
                goal = goal,
                activityLevel = activityLevel
            )
        }
    }
}