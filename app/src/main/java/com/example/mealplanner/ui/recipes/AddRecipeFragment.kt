package com.example.mealplanner.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealplanner.databinding.FragmentAddRecipeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddRecipeFragment : Fragment() {

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeViewModel by viewModels()
    private val args: AddRecipeFragmentArgs by navArgs()

    private lateinit var tempIngredientsAdapter: TempIngredientsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupIngredientsList()
        setupButtons()
        observeViewModel()

        // Si on édite une recette existante
        args.recipeId?.let {
            viewModel.selectRecipe(it)
            viewModel.loadRecipeIngredientsForEdit(it)
        }
    }

    private fun setupIngredientsList() {
        tempIngredientsAdapter = TempIngredientsAdapter { index ->
            viewModel.removeTempIngredient(index)
        }

        binding.recyclerViewIngredients.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tempIngredientsAdapter
        }
    }

    private fun setupButtons() {
        binding.buttonAddIngredient.setOnClickListener {
            // Ici vous pourriez ouvrir un dialogue de sélection d'aliment
            Toast.makeText(context, "Sélection d'ingrédient à implémenter", Toast.LENGTH_SHORT).show()
        }

        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                saveRecipe()
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (binding.editTextName.text.isNullOrBlank()) {
            binding.nameInputLayout.error = "Le nom est requis"
            return false
        } else {
            binding.nameInputLayout.error = null
        }

        if (binding.editTextInstructions.text.isNullOrBlank()) {
            binding.instructionsInputLayout.error = "Les instructions sont requises"
            return false
        } else {
            binding.instructionsInputLayout.error = null
        }

        return true
    }

    private fun saveRecipe() {
        val name = binding.editTextName.text.toString()
        val description = binding.editTextDescription.text.toString()
        val instructions = binding.editTextInstructions.text.toString()
        val prepTime = binding.editTextPrepTime.text.toString().toIntOrNull() ?: 0
        val cookTime = binding.editTextCookTime.text.toString().toIntOrNull() ?: 0
        val servings = binding.editTextServings.text.toString().toIntOrNull() ?: 1
        val tags = binding.editTextTags.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        viewModel.saveRecipe(
            name = name,
            description = description,
            instructions = instructions,
            prepTime = prepTime,
            cookTime = cookTime,
            servings = servings,
            tags = tags
        )
    }

    private fun observeViewModel() {
        viewModel.tempIngredients.observe(viewLifecycleOwner) { ingredients ->
            tempIngredientsAdapter.updateIngredients(ingredients)
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                if (it.contains("enregistrée", ignoreCase = true)) {
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