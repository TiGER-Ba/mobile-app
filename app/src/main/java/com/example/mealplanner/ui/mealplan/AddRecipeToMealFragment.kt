package com.example.mealplanner.ui.mealplan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealplanner.R
import com.example.mealplanner.databinding.FragmentAddRecipeToMealBinding
import com.example.mealplanner.ui.recipes.RecipeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddRecipeToMealFragment : Fragment() {

    private var _binding: FragmentAddRecipeToMealBinding? = null
    private val binding get() = _binding!!

    private val args: AddRecipeToMealFragmentArgs by navArgs()
    private val mealPlanViewModel: MealPlanViewModel by viewModels()
    private val recipeViewModel: RecipeViewModel by viewModels()

    private lateinit var availableMealsAdapter: AvailableMealsAdapter

    companion object {
        private const val TAG = "AddRecipeToMealFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRecipeToMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMealsList()
        loadRecipeDetails()
        loadTodaysMeals()
        observeViewModel()
    }

    private fun setupMealsList() {
        availableMealsAdapter = AvailableMealsAdapter { meal ->
            showServingsDialog(meal)
        }

        binding.recyclerViewAvailableMeals.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = availableMealsAdapter
        }
    }

    private fun loadRecipeDetails() {
        recipeViewModel.getRecipeDetails(args.recipeId).observe(viewLifecycleOwner) { recipeWithIngredients ->
            recipeWithIngredients?.let { rwi ->
                val recipe = rwi.recipe
                binding.textViewRecipeName.text = recipe.name
                binding.textViewRecipeDescription.text = recipe.description
                binding.textViewRecipeServings.text = "${recipe.servings} portions"

                val totalTime = recipe.preparationTime + recipe.cookingTime
                binding.textViewRecipeTime.text = "$totalTime min"
            } ?: run {
                Toast.makeText(context, "Recette non trouvée", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun loadTodaysMeals() {
        val today = System.currentTimeMillis()
        mealPlanViewModel.selectDate(today)

        viewLifecycleOwner.lifecycleScope.launch {
            mealPlanViewModel.mealsForCurrentPlan.collect { meals ->
                if (isAdded && !isDetached) {
                    Log.d(TAG, "Repas disponibles: ${meals.size}")
                    availableMealsAdapter.submitList(meals)

                    if (meals.isEmpty()) {
                        binding.textViewNoMeals.visibility = View.VISIBLE
                        binding.recyclerViewAvailableMeals.visibility = View.GONE
                    } else {
                        binding.textViewNoMeals.visibility = View.GONE
                        binding.recyclerViewAvailableMeals.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun showServingsDialog(meal: com.example.mealplanner.data.model.Meal) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_servings_selector, null)
        val editTextServings = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextServings)

        // Valeur par défaut
        editTextServings.setText("1")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nombre de portions")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val servings = editTextServings.text.toString().toFloatOrNull() ?: 1f
                addRecipeToMeal(meal, servings)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun addRecipeToMeal(meal: com.example.mealplanner.data.model.Meal, servings: Float) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                mealPlanViewModel.addRecipeToMeal(
                    mealId = meal.id,
                    recipeId = args.recipeId,
                    servings = servings
                )

                Toast.makeText(context, "Recette ajoutée au repas", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'ajout", e)
                Toast.makeText(context, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        mealPlanViewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                if (isAdded && !isDetached) {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    mealPlanViewModel.clearMessage()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}