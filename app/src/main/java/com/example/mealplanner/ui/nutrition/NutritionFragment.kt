package com.example.mealplanner.ui.nutrition

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mealplanner.databinding.FragmentNutritionBinding
import com.example.mealplanner.ui.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.random.Random

@AndroidEntryPoint
class NutritionFragment : Fragment() {

    private var _binding: FragmentNutritionBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()

    companion object {
        private const val TAG = "NutritionFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNutritionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "NutritionFragment créé")

        observeUserProfile()
        setupNutritionData()
    }

    private fun observeUserProfile() {
        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                Log.d(TAG, "Profil utilisateur reçu pour nutrition")
                setupNutritionProgress(
                    calorieTarget = it.dailyCalorieTarget,
                    proteinTarget = it.proteinTarget,
                    carbTarget = it.carbTarget,
                    fatTarget = it.fatTarget
                )
            }
        }
    }

    private fun setupNutritionData() {
        // En attendant les vraies données, afficher des données d'exemple
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Simuler des données de consommation pour la journée
                val consumedCalories = Random.nextInt(1200, 1800)
                val consumedProtein = Random.nextInt(80, 120)
                val consumedCarbs = Random.nextInt(150, 220)
                val consumedFat = Random.nextInt(40, 80)

                // Utiliser des valeurs par défaut si pas de profil
                setupNutritionProgress(
                    calorieTarget = 2000,
                    proteinTarget = 150,
                    carbTarget = 250,
                    fatTarget = 70,
                    consumedCalories = consumedCalories,
                    consumedProtein = consumedProtein,
                    consumedCarbs = consumedCarbs,
                    consumedFat = consumedFat
                )
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de la configuration des données nutritionnelles", e)
                setupDefaultNutritionData()
            }
        }
    }

    private fun setupNutritionProgress(
        calorieTarget: Int,
        proteinTarget: Int,
        carbTarget: Int,
        fatTarget: Int,
        consumedCalories: Int = Random.nextInt(1200, 1800),
        consumedProtein: Int = Random.nextInt(80, 120),
        consumedCarbs: Int = Random.nextInt(150, 220),
        consumedFat: Int = Random.nextInt(40, 80)
    ) {
        try {
            // Calculer les pourcentages
            val calorieProgress = ((consumedCalories.toFloat() / calorieTarget) * 100).toInt().coerceAtMost(100)
            val proteinProgress = ((consumedProtein.toFloat() / proteinTarget) * 100).toInt().coerceAtMost(100)
            val carbProgress = ((consumedCarbs.toFloat() / carbTarget) * 100).toInt().coerceAtMost(100)
            val fatProgress = ((consumedFat.toFloat() / fatTarget) * 100).toInt().coerceAtMost(100)

            // Mettre à jour l'interface
            binding.calorieProgress.progress = calorieProgress
            binding.textCalorieProgress.text = "$consumedCalories / $calorieTarget kcal"

            binding.textProtein.text = "${consumedProtein}g / ${proteinTarget}g"
            binding.textCarbs.text = "${consumedCarbs}g / ${carbTarget}g"
            binding.textFat.text = "${consumedFat}g / ${fatTarget}g"

            Log.d(TAG, "Données nutritionnelles mises à jour - Calories: $consumedCalories/$calorieTarget")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour de l'interface nutritionnelle", e)
            setupDefaultNutritionData()
        }
    }

    private fun setupDefaultNutritionData() {
        // Données par défaut en cas d'erreur
        binding.calorieProgress.progress = 65
        binding.textCalorieProgress.text = "1300 / 2000 kcal"

        binding.textProtein.text = "85g / 150g"
        binding.textCarbs.text = "180g / 250g"
        binding.textFat.text = "55g / 70g"

        Log.d(TAG, "Données nutritionnelles par défaut appliquées")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}