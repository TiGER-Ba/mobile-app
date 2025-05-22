package com.example.mealplanner.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mealplanner.R
import com.example.mealplanner.data.model.RecipeWithIngredients
import com.example.mealplanner.databinding.FragmentRecipeDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipeDetailsFragment : Fragment() {

    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeViewModel by viewModels()
    private val args: RecipeDetailsFragmentArgs by navArgs()

    private lateinit var ingredientAdapter: RecipeIngredientAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupIngredientsList()
        setupButtons()
        loadRecipeDetails()
        observeViewModel()
    }

    private fun setupIngredientsList() {
        ingredientAdapter = RecipeIngredientAdapter()
        binding.recyclerViewIngredients.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ingredientAdapter
        }
    }

    private fun setupButtons() {
        binding.buttonEditRecipe.setOnClickListener {
            // CORRECTION : Navigation avec findNavController().navigate()
            findNavController().navigate(
                R.id.navigation_add_recipe,
                Bundle().apply {
                    putString("recipeId", args.recipeId)
                }
            )
        }

        binding.buttonDeleteRecipe.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.buttonFavorite.setOnClickListener {
            // Toggle favorite status will be handled by observing recipe changes
        }

        binding.buttonAddToMeal.setOnClickListener {
            Toast.makeText(context, "Fonctionnalité à implémenter", Toast.LENGTH_SHORT).show()
            // Ici vous pourriez naviguer vers un écran de sélection de repas
        }
    }

    private fun loadRecipeDetails() {
        viewModel.getRecipeDetails(args.recipeId).observe(viewLifecycleOwner) { recipeWithIngredients ->
            recipeWithIngredients?.let { updateUI(it) }
        }
    }

    private fun updateUI(recipeWithIngredients: RecipeWithIngredients) {
        val recipe = recipeWithIngredients.recipe

        binding.textViewName.text = recipe.name
        binding.textViewDescription.text = recipe.description
        binding.textViewInstructions.text = recipe.instructions
        binding.textViewPrepTime.text = "${recipe.preparationTime} min"
        binding.textViewCookTime.text = "${recipe.cookingTime} min"
        binding.textViewServings.text = "${recipe.servings} portions"

        // Chargement de l'image
        if (!recipe.imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(recipe.imageUrl)
                .placeholder(R.drawable.placeholder_recipe)
                .into(binding.imageViewRecipe)
        } else {
            binding.imageViewRecipe.setImageResource(R.drawable.placeholder_recipe)
        }

        // Mise à jour du bouton favori
        updateFavoriteButton(recipe.favorite)

        // Affichage des tags
        if (recipe.tags.isNotEmpty()) {
            binding.textViewTags.text = recipe.tags.joinToString(", ")
            binding.textViewTags.visibility = View.VISIBLE
        } else {
            binding.textViewTags.visibility = View.GONE
        }

        // Mise à jour de la liste des ingrédients
        ingredientAdapter.updateIngredients(recipeWithIngredients.ingredients)
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        val iconRes = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline
        binding.buttonFavorite.setImageResource(iconRes)
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer la recette")
            .setMessage("Êtes-vous sûr de vouloir supprimer cette recette ?")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deleteRecipe(args.recipeId)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                if (it.contains("supprimée", ignoreCase = true)) {
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