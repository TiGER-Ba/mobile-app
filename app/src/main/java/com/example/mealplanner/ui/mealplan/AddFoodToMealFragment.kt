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
import com.example.mealplanner.databinding.FragmentAddFoodToMealBinding
import com.example.mealplanner.ui.search.FoodViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddFoodToMealFragment : Fragment() {

    private var _binding: FragmentAddFoodToMealBinding? = null
    private val binding get() = _binding!!

    private val args: AddFoodToMealFragmentArgs by navArgs()
    private val mealPlanViewModel: MealPlanViewModel by viewModels()
    private val foodViewModel: FoodViewModel by viewModels()

    private lateinit var availableMealsAdapter: AvailableMealsAdapter

    companion object {
        private const val TAG = "AddFoodToMealFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFoodToMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMealsList()
        loadFoodDetails()
        loadTodaysMeals()
        observeViewModel()
    }

    private fun setupMealsList() {
        availableMealsAdapter = AvailableMealsAdapter { meal ->
            showQuantityDialog(meal)
        }

        binding.recyclerViewAvailableMeals.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = availableMealsAdapter
        }
    }

    private fun loadFoodDetails() {
        foodViewModel.getFoodByIdLiveData(args.foodId).observe(viewLifecycleOwner) { food ->
            food?.let {
                binding.textViewFoodName.text = it.name
                binding.textViewFoodCalories.text = "${it.calories} kcal/100g"
                binding.textViewFoodMacros.text = "P: ${it.protein}g • C: ${it.carbs}g • L: ${it.fat}g"
            } ?: run {
                Toast.makeText(context, "Aliment non trouvé", Toast.LENGTH_SHORT).show()
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

    private fun showQuantityDialog(meal: com.example.mealplanner.data.model.Meal) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_quantity_selector, null)
        val editTextQuantity = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextQuantity)
        val editTextServingSize = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextServingSize)

        // Valeurs par défaut
        editTextQuantity.setText("1")
        editTextServingSize.setText("100")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Quantité")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val quantity = editTextQuantity.text.toString().toFloatOrNull() ?: 1f
                val servingSize = editTextServingSize.text.toString().toFloatOrNull() ?: 100f
                addFoodToMeal(meal, quantity, servingSize)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun addFoodToMeal(meal: com.example.mealplanner.data.model.Meal, quantity: Float, servingSize: Float) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                mealPlanViewModel.addFoodToMeal(
                    mealId = meal.id,
                    foodId = args.foodId,
                    quantity = quantity,
                    servingSize = servingSize
                )

                Toast.makeText(context, "Aliment ajouté au repas", Toast.LENGTH_SHORT).show()
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