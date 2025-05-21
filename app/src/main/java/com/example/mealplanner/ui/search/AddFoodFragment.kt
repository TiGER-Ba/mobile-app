package com.example.mealplanner.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mealplanner.databinding.FragmentAddFoodBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFoodFragment : Fragment() {

    private var _binding: FragmentAddFoodBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FoodViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSaveButton()
        observeViewModel()
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                saveFood()
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Vérification du nom
        if (binding.editTextName.text.isNullOrBlank()) {
            binding.nameInputLayout.error = "Le nom est requis"
            return false
        } else {
            binding.nameInputLayout.error = null
        }

        // Vérification des calories
        if (binding.editTextCalories.text.isNullOrBlank()) {
            binding.caloriesInputLayout.error = "Les calories sont requises"
            return false
        } else {
            binding.caloriesInputLayout.error = null
        }

        // Les autres champs peuvent avoir des valeurs par défaut

        return true
    }

    private fun saveFood() {
        val name = binding.editTextName.text.toString()
        val calories = binding.editTextCalories.text.toString().toIntOrNull() ?: 0
        val protein = binding.editTextProtein.text.toString().toFloatOrNull() ?: 0f
        val carbs = binding.editTextCarbs.text.toString().toFloatOrNull() ?: 0f
        val fat = binding.editTextFat.text.toString().toFloatOrNull() ?: 0f
        val fiber = binding.editTextFiber.text.toString().toFloatOrNull() ?: 0f
        val sugar = binding.editTextSugar.text.toString().toFloatOrNull() ?: 0f
        val servingSize = binding.editTextServingSize.text.toString().toFloatOrNull() ?: 100f
        val servingUnit = binding.editTextServingUnit.text.toString().ifEmpty { "g" }

        viewModel.addCustomFood(
            name = name,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = fiber,
            sugar = sugar,
            servingSize = servingSize,
            servingUnit = servingUnit
        )
    }

    private fun observeViewModel() {
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                if (it.contains("ajouté", ignoreCase = true)) {
                    findNavController().navigateUp()
                }
                viewModel.clearMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}