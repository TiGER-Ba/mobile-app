package com.example.mealplanner.ui.search

import android.os.Bundle
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
import com.example.mealplanner.R
import com.example.mealplanner.data.model.Food
import com.example.mealplanner.databinding.FragmentFoodSearchBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FoodSearchFragment : Fragment() {

    private var _binding: FragmentFoodSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FoodViewModel by viewModels()
    private lateinit var foodAdapter: FoodAdapter

    // État de recherche pour savoir si on utilise la recherche locale ou en ligne
    private var isOnlineSearch = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFoodsList()
        setupSearchView()
        setupTabLayout()
        setupSearchButtons()
        setupAddFoodButton()
        observeViewModel()
    }

    private fun setupFoodsList() {
        foodAdapter = FoodAdapter(
            onFoodClick = { food ->
                navigateToFoodDetails(food)
            },
            onFavoriteClick = { food ->
                viewModel.toggleFavorite(food)
            }
        )

        binding.recyclerViewFoods.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = foodAdapter
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    performSearch(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.length >= 2) {
                        // Recherche locale uniquement sur changement de texte
                        viewModel.searchFoods(it, false)
                    } else if (it.isEmpty()) {
                        // Réinitialiser à tous les aliments
                        viewModel.searchFoods("", false)
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
                    0 -> viewModel.searchFoods("", false) // Tous les aliments
                    1 -> displayFavoriteFoods()
                    2 -> displayRecentFoods()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearchButtons() {
        binding.buttonLocalSearch.setOnClickListener {
            isOnlineSearch = false
            binding.buttonLocalSearch.isEnabled = false
            binding.buttonOnlineSearch.isEnabled = true
            performSearch(binding.searchView.query.toString())
        }

        binding.buttonOnlineSearch.setOnClickListener {
            isOnlineSearch = true
            binding.buttonLocalSearch.isEnabled = true
            binding.buttonOnlineSearch.isEnabled = false
            performSearch(binding.searchView.query.toString())
        }
    }

    private fun setupAddFoodButton() {
        binding.fabAddFood.setOnClickListener {
            navigateToAddFood()
        }
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            viewModel.searchFoods(query, isOnlineSearch)
        }
    }

    private fun displayFavoriteFoods() {
        // Observer les aliments favoris
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchFoods("", false)
            // TODO: Filtrer uniquement les favoris - à implémenter dans le ViewModel
        }
    }

    private fun displayRecentFoods() {
        // Observer les aliments récemment utilisés
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchFoods("", false)
            // TODO: Filtrer uniquement les récents - à implémenter dans le ViewModel
        }
    }

    private fun navigateToFoodDetails(food: Food) {
        val action = FoodSearchFragmentDirections.actionFoodSearchToFoodDetails(food.id)
        findNavController().navigate(action)
    }

    private fun navigateToAddFood() {
        val action = FoodSearchFragmentDirections.actionFoodSearchToAddFood()
        findNavController().navigate(action)
    }

    private fun observeViewModel() {
        // Observer l'état de chargement
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observer les messages d'erreur ou de succès
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }

        // Observer les résultats de recherche locaux
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { foods ->
                if (!isOnlineSearch) {
                    updateFoodsList(foods)
                }
            }
        }

        // Observer les résultats de recherche en ligne
        viewModel.onlineSearchResults.observe(viewLifecycleOwner) { foods ->
            if (isOnlineSearch) {
                updateFoodsList(foods)
            }
        }

        // Observer la requête de recherche
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchQuery.collect { query ->
                binding.searchView.setQuery(query, false)
            }
        }
    }

    private fun updateFoodsList(foods: List<Food>) {
        foodAdapter.submitList(foods)

        if (foods.isEmpty()) {
            binding.textViewNoResults.visibility = View.VISIBLE
            binding.recyclerViewFoods.visibility = View.GONE
        } else {
            binding.textViewNoResults.visibility = View.GONE
            binding.recyclerViewFoods.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}