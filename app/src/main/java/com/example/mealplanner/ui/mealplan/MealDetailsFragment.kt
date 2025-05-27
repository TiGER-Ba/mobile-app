package com.example.mealplanner.ui.mealplan

import android.os.Bundle
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
import com.example.mealplanner.data.model.Meal
import com.example.mealplanner.data.model.MealType
import com.example.mealplanner.databinding.FragmentMealDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MealDetailsFragment : Fragment() {

    private var _binding: FragmentMealDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MealPlanViewModel by viewModels()
    private val args: MealDetailsFragmentArgs by navArgs()

    private lateinit var mealItemAdapter: MealItemAdapter
    private var currentMeal: Meal? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFoodsList()
        setupButtons()
        loadMealDetails()
        observeViewModel()
    }

    private fun setupFoodsList() {
        mealItemAdapter = MealItemAdapter(
            onItemClick = { mealItemDetails ->
                showEditQuantityDialog(mealItemDetails)
            },
            onDeleteClick = { mealItemDetails ->
                showDeleteConfirmationDialog(mealItemDetails)
            }
        )

        binding.recyclerViewFoods.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mealItemAdapter
        }
    }

    private fun setupButtons() {
        binding.buttonEditMeal.setOnClickListener {
            showEditMealDialog()
        }

        binding.buttonDeleteMeal.setOnClickListener {
            showDeleteMealConfirmationDialog()
        }

        // CORRECTION: Navigation améliorée vers la recherche d'aliments/recettes
        binding.fabAddFood.setOnClickListener {
            showAddFoodOptionsDialog()
        }
    }

    private fun loadMealDetails() {
        viewModel.selectMeal(args.mealId)

        // Observer le repas sélectionné
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mealsForCurrentPlan.collect { meals ->
                if (isAdded && !isDetached) {
                    val meal = meals.find { it.id == args.mealId }
                    meal?.let {
                        currentMeal = it
                        updateUI(it)
                    }
                }
            }
        }

        // Observer les éléments du repas
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mealItems.collect { items ->
                if (isAdded && !isDetached) {
                    mealItemAdapter.submitList(items)

                    if (items.isEmpty()) {
                        binding.textViewNoFoods.visibility = View.VISIBLE
                    } else {
                        binding.textViewNoFoods.visibility = View.GONE
                    }

                    updateNutritionInfo(items)
                }
            }
        }
    }

    private fun updateUI(meal: Meal) {
        if (!isAdded || isDetached) return

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = timeFormat.format(Date(meal.time))
        binding.textViewTime.text = timeString

        val mealTypeText = when (meal.type) {
            MealType.BREAKFAST -> getString(R.string.meal_type_breakfast)
            MealType.LUNCH -> getString(R.string.meal_type_lunch)
            MealType.DINNER -> getString(R.string.meal_type_dinner)
            MealType.SNACK -> getString(R.string.meal_type_snack)
        }
        binding.textViewType.text = mealTypeText

        if (meal.name.isNotEmpty()) {
            binding.textViewName.text = meal.name
            binding.textViewName.visibility = View.VISIBLE
        } else {
            binding.textViewName.visibility = View.GONE
        }

        if (meal.notes.isNotEmpty()) {
            binding.textViewNotes.text = meal.notes
            binding.textViewNotes.visibility = View.VISIBLE
        } else {
            binding.textViewNotes.visibility = View.GONE
        }
    }

    private fun updateNutritionInfo(items: List<MealItemDetails>) {
        if (!isAdded || isDetached) return

        var totalCalories = 0
        var totalProtein = 0f
        var totalCarbs = 0f
        var totalFat = 0f

        items.forEach { itemDetails ->
            totalCalories += itemDetails.calories

            itemDetails.food?.let { food ->
                val servingRatio = itemDetails.item.quantity * itemDetails.item.servingSize / 100f
                totalProtein += food.protein * servingRatio
                totalCarbs += food.carbs * servingRatio
                totalFat += food.fat * servingRatio
            }
        }

        binding.textViewCalories.text = "$totalCalories kcal"
        binding.textViewProtein.text = "${totalProtein.toInt()}g"
        binding.textViewCarbs.text = "${totalCarbs.toInt()}g"
        binding.textViewFat.text = "${totalFat.toInt()}g"
    }

    private fun showEditMealDialog() {
        currentMeal?.let { meal ->
            Toast.makeText(context, "Fonctionnalité d'édition à implémenter", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteMealConfirmationDialog() {
        if (!isAdded || isDetached) return

        currentMeal?.let { meal ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Supprimer le repas")
                .setMessage("Êtes-vous sûr de vouloir supprimer ce repas ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    viewModel.deleteMeal(meal.id)
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }

    // CORRECTION: Options d'ajout améliorées
    private fun showAddFoodOptionsDialog() {
        if (!isAdded || isDetached) return

        val options = arrayOf("Ajouter un aliment", "Ajouter une recette")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ajouter au repas")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> navigateToFoodSearch()
                    1 -> navigateToRecipeSearch()
                }
            }
            .show()
    }

    // CORRECTION: Navigation vers la recherche d'aliments
    private fun navigateToFoodSearch() {
        try {
            val action = MealDetailsFragmentDirections.actionMealDetailsToFoodSearch()
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur de navigation", Toast.LENGTH_SHORT).show()
        }
    }

    // CORRECTION: Navigation vers la recherche de recettes
    private fun navigateToRecipeSearch() {
        try {
            val action = MealDetailsFragmentDirections.actionMealDetailsToRecipeSearch()
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur de navigation", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditQuantityDialog(mealItemDetails: MealItemDetails) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_quantity_selector, null)
        val editTextQuantity = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextQuantity)
        val editTextServingSize = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextServingSize)

        editTextQuantity.setText(mealItemDetails.item.quantity.toString())
        editTextServingSize.setText(mealItemDetails.item.servingSize.toString())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Modifier la quantité")
            .setView(dialogView)
            .setPositiveButton("Modifier") { _, _ ->
                val quantity = editTextQuantity.text.toString().toFloatOrNull() ?: mealItemDetails.item.quantity
                val servingSize = editTextServingSize.text.toString().toFloatOrNull() ?: mealItemDetails.item.servingSize

                viewModel.updateMealItem(mealItemDetails.item, quantity, servingSize)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(mealItemDetails: MealItemDetails) {
        if (!isAdded || isDetached) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer l'élément")
            .setMessage("Êtes-vous sûr de vouloir supprimer cet élément du repas ?")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deleteMealItem(mealItemDetails.item.id)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                if (isAdded && !isDetached) {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()

                    if (it.contains("supprimé", ignoreCase = true) && it.contains("repas", ignoreCase = true)) {
                        findNavController().navigateUp()
                    }

                    viewModel.clearMessage()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}