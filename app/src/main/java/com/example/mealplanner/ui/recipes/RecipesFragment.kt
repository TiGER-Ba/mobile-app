package com.example.mealplanner.ui.recipes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealplanner.data.model.Recipe
import com.example.mealplanner.data.model.RecipeIngredient
import com.example.mealplanner.databinding.FragmentRecipesBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipesFragment : Fragment() {

    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter

    companion object {
        private const val TAG = "RecipesFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "RecipesFragment créé")

        setupRecipesList()
        setupSearchView()
        setupTabLayout()
        setupAddRecipeButton()
        observeRecipes()

        // Ajouter des recettes de test
        addTestRecipes()
    }

    private fun addTestRecipes() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Recette 1: Pancakes aux bananes
                val recipe1 = Recipe(
                    name = "Pancakes aux bananes",
                    description = "Des délicieux pancakes moelleux et sucrés",
                    instructions = "1. Écraser les bananes dans un bol\n2. Ajouter les œufs et mélanger\n3. Incorporer la farine et le lait\n4. Cuire dans une poêle chaude",
                    preparationTime = 10,
                    cookingTime = 15,
                    servings = 4,
                    tags = listOf("petit-déjeuner", "végétarien", "banane"),
                    isSynced = false
                )

                val ingredients1 = listOf(
                    RecipeIngredient(
                        recipeId = recipe1.id,
                        foodId = "fake_banana_id",
                        quantity = 2f,
                        unit = "pièces"
                    ),
                    RecipeIngredient(
                        recipeId = recipe1.id,
                        foodId = "fake_flour_id",
                        quantity = 200f,
                        unit = "g"
                    ),
                    RecipeIngredient(
                        recipeId = recipe1.id,
                        foodId = "fake_milk_id",
                        quantity = 250f,
                        unit = "ml"
                    )
                )

                viewModel.saveRecipe(
                    name = recipe1.name,
                    description = recipe1.description,
                    instructions = recipe1.instructions,
                    prepTime = recipe1.preparationTime,
                    cookTime = recipe1.cookingTime,
                    servings = recipe1.servings,
                    tags = recipe1.tags
                )

                // Recette 2: Salade de quinoa
                val recipe2 = Recipe(
                    name = "Salade de quinoa aux légumes",
                    description = "Une salade nutritive et colorée",
                    instructions = "1. Cuire le quinoa\n2. Couper les légumes en dés\n3. Mélanger tous les ingrédients\n4. Assaisonner avec l'huile d'olive et le citron",
                    preparationTime = 15,
                    cookingTime = 20,
                    servings = 3,
                    tags = listOf("végétarien", "sans-gluten", "salade"),
                    isSynced = false
                )

                viewModel.saveRecipe(
                    name = recipe2.name,
                    description = recipe2.description,
                    instructions = recipe2.instructions,
                    prepTime = recipe2.preparationTime,
                    cookTime = recipe2.cookingTime,
                    servings = recipe2.servings,
                    tags = recipe2.tags
                )

                // Recette 3: Poulet grillé aux herbes
                val recipe3 = Recipe(
                    name = "Poulet grillé aux herbes",
                    description = "Un plat principal savoureux et sain",
                    instructions = "1. Mariner le poulet avec les herbes\n2. Préchauffer le grill\n3. Griller le poulet 8-10 min de chaque côté\n4. Laisser reposer avant de servir",
                    preparationTime = 20,
                    cookingTime = 25,
                    servings = 4,
                    tags = listOf("protéine", "grill", "herbes"),
                    isSynced = false
                )

                viewModel.saveRecipe(
                    name = recipe3.name,
                    description = recipe3.description,
                    instructions = recipe3.instructions,
                    prepTime = recipe3.preparationTime,
                    cookTime = recipe3.cookingTime,
                    servings = recipe3.servings,
                    tags = recipe3.tags
                )

                Log.d(TAG, "Recettes de test ajoutées")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'ajout des recettes de test", e)
            }
        }
    }

    private fun setupRecipesList() {
        recipeAdapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                val action = RecipesFragmentDirections.actionRecipesToRecipeDetails(recipe.id)
                findNavController().navigate(action)
            },
            onFavoriteClick = { recipe ->
                viewModel.toggleFavorite(recipe.id, !recipe.favorite)
            }
        )

        binding.recyclerViewRecipes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipeAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchRecipes(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.length >= 2 || it.isEmpty()) {
                        viewModel.searchRecipes(it)
                    }
                }
                return true
            }
        })
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> observeAllRecipes()
                    1 -> observeFavoriteRecipes()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupAddRecipeButton() {
        binding.fabAddRecipe.setOnClickListener {
            val action = RecipesFragmentDirections.actionRecipesToAddRecipe(null)
            findNavController().navigate(action)
        }
    }

    private fun observeRecipes() {
        observeAllRecipes() // Par défaut, afficher toutes les recettes
    }

    private fun observeAllRecipes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allRecipes.collect { recipes ->
                if (isAdded && !isDetached) {
                    Log.d(TAG, "Recettes reçues: ${recipes.size}")
                    updateRecipesList(recipes)
                }
            }
        }
    }

    private fun observeFavoriteRecipes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteRecipes.collect { recipes ->
                if (isAdded && !isDetached) {
                    Log.d(TAG, "Recettes favorites reçues: ${recipes.size}")
                    updateRecipesList(recipes)
                }
            }
        }
    }

    private fun updateRecipesList(recipes: List<Recipe>) {
        if (!isAdded || isDetached) return

        recipeAdapter.submitList(recipes)

        if (recipes.isEmpty()) {
            binding.textViewNoRecipes.visibility = View.VISIBLE
            binding.recyclerViewRecipes.visibility = View.GONE
        } else {
            binding.textViewNoRecipes.visibility = View.GONE
            binding.recyclerViewRecipes.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}