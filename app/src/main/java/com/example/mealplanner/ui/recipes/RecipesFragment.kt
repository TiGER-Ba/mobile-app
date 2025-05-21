package com.example.mealplanner.ui.recipes

import android.os.Bundle
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

        setupRecipesList()
        setupSearchView()
        setupTabLayout()
        setupAddRecipeButton()
        observeRecipes()
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
                updateRecipesList(recipes)
            }
        }
    }

    private fun observeFavoriteRecipes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteRecipes.collect { recipes ->
                updateRecipesList(recipes)
            }
        }
    }

    private fun updateRecipesList(recipes: List<Recipe>) {
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