package com.example.mealplanner.ui.nutrition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mealplanner.databinding.FragmentNutritionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NutritionFragment : Fragment() {

    private var _binding: FragmentNutritionBinding? = null
    private val binding get() = _binding!!

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
        setupCalorieProgress()
    }

    private fun setupCalorieProgress() {
        // Exemple simple sans graphiques
        binding.calorieProgress.progress = 65
        binding.textCalorieProgress.text = "1300 / 2000 kcal"

        // Valeurs nutritionnelles exemple
        binding.textProtein.text = "65g / 140g"
        binding.textCarbs.text = "120g / 250g"
        binding.textFat.text = "40g / 70g"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}