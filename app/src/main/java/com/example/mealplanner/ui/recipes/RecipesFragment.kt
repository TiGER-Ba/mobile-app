package com.example.mealplanner.ui.recipes

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealplanner.data.model.Recipe
import com.example.mealplanner.databinding.FragmentRecipesBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RecipesFragment : Fragment() {

    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecipeViewModel by viewModels()
    private lateinit var recipeAdapter: RecipeAdapter

    // CORRECTION: SharedPreferences pour éviter l'ajout répétitif de données de test
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "RecipesFragment"
        private const val PREF_TEST_RECIPES_ADDED = "test_recipes_added"
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

        // CORRECTION: Ajouter des recettes de test seulement si nécessaire
        addTestRecipesIfNeeded()
    }

    private fun addTestRecipesIfNeeded() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val testRecipesAdded = sharedPreferences.getBoolean(PREF_TEST_RECIPES_ADDED, false)
                if (!testRecipesAdded) {
                    Log.d(TAG, "Ajout des recettes de test pour la première fois")
                    addTestRecipes()
                    sharedPreferences.edit().putBoolean(PREF_TEST_RECIPES_ADDED, true).apply()
                } else {
                    Log.d(TAG, "Recettes de test déjà présentes, pas d'ajout")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de l'ajout des recettes de test", e)
            }
        }
    }

    private fun addTestRecipes() {
        try {
            // Recette 1: Pancakes aux bananes
            viewModel.saveRecipe(
                name = "Pancakes aux bananes",
                description = "Des délicieux pancakes moelleux et sucrés",
                instructions = "1. Écraser les bananes dans un bol\n2. Ajouter les œufs et mélanger\n3. Incorporer la farine et le lait\n4. Cuire dans une poêle chaude",
                prepTime = 10,
                cookTime = 15,
                servings = 4,
                tags = listOf("petit-déjeuner", "végétarien", "banane")
            )

            // Recette 2: Salade de quinoa
            viewModel.saveRecipe(
                name = "Salade de quinoa aux légumes",
                description = "Une salade nutritive et colorée",
                instructions = "1. Cuire le quinoa\n2. Couper les légumes en dés\n3. Mélanger tous les ingrédients\n4. Assaisonner avec l'huile d'olive et le citron",
                prepTime = 15,
                cookTime = 20,
                servings = 3,
                tags = listOf("végétarien", "sans-gluten", "salade")
            )

            // Recette 3: Poulet grillé aux herbes
            viewModel.saveRecipe(
                name = "Poulet grillé aux herbes",
                description = "Un plat principal savoureux et sain",
                instructions = "1. Mariner le poulet avec les herbes\n2. Préchauffer le grill\n3. Griller le poulet 8-10 min de chaque côté\n4. Laisser reposer avant de servir",
                prepTime = 20,
                cookTime = 25,
                servings = 4,
                tags = listOf("protéine", "grill", "herbes")
            )

            Log.d(TAG, "Recettes de test ajoutées avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'ajout des recettes de test", e)
        }
    }

    private fun setupRecipesList() {
        recipeAdapter = RecipeAdapter(
            onRecipeClick = { recipe ->
                navigateToRecipeDetails(recipe)
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

    private fun navigateToRecipeDetails(recipe: Recipe) {
        try {
            val action = RecipesFragmentDirections.actionRecipesToRecipeDetails(recipe.id)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur navigation vers détails recette", e)
            showToast("Erreur de navigation")
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
            try {
                val action = RecipesFragmentDirections.actionRecipesToAddRecipe(null)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e(TAG, "Erreur navigation vers ajout recette", e)
                showToast("Erreur de navigation")
            }
        }
    }

    private fun observeRecipes() {
        observeAllRecipes() // Par défaut, afficher toutes les recettes

        // Observer les messages du ViewModel avec limitation
        var lastToastTime = 0L
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                if (isAdded && !isDetached) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastToastTime > 2000) { // Minimum 2 secondes entre les toasts
                        showToast(it)
                        lastToastTime = currentTime
                        viewModel.clearMessage()
                    }
                }
            }
        }
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

    // CORRECTION: Méthode centralisée pour les toasts avec limitation
    private fun showToast(message: String) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'affichage du toast", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}