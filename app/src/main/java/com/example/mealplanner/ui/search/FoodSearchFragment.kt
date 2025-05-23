package com.example.mealplanner.ui.search

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
import com.example.mealplanner.R
import com.example.mealplanner.data.model.Food
import com.example.mealplanner.databinding.FragmentFoodSearchBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FoodSearchFragment : Fragment() {

    private var _binding: FragmentFoodSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FoodViewModel by viewModels()
    private lateinit var foodAdapter: FoodAdapter

    // État de recherche pour savoir si on utilise la recherche locale ou en ligne
    private var isOnlineSearch = false

    companion object {
        private const val TAG = "FoodSearchFragment"
    }

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

        Log.d(TAG, "FoodSearchFragment créé")

        setupFoodsList()
        setupSearchView()
        setupTabLayout()
        setupSearchButtons()
        setupAddFoodButton()
        observeViewModel()

        // Charger les données initiales
        loadInitialData()
    }

    private fun loadInitialData() {
        Log.d(TAG, "Chargement des données initiales")

        // Ajouter des aliments de test si la base est vide
        viewLifecycleOwner.lifecycleScope.launch {
            addTestFoodsIfEmpty()
            // Charger tous les aliments après avoir ajouté les données de test
            viewModel.searchFoods("", false)
        }
    }

    private suspend fun addTestFoodsIfEmpty() {
        try {
            // Ajouter des aliments de test pour avoir quelque chose à afficher
            viewModel.addCustomFood(
                name = "Banane",
                calories = 89,
                protein = 1.1f,
                carbs = 22.8f,
                fat = 0.3f,
                fiber = 2.6f,
                sugar = 12.2f,
                servingSize = 100f,
                servingUnit = "g"
            )

            viewModel.addCustomFood(
                name = "Pomme",
                calories = 52,
                protein = 0.3f,
                carbs = 13.8f,
                fat = 0.2f,
                fiber = 2.4f,
                sugar = 10.4f,
                servingSize = 100f,
                servingUnit = "g"
            )

            viewModel.addCustomFood(
                name = "Poulet (blanc)",
                calories = 165,
                protein = 31f,
                carbs = 0f,
                fat = 3.6f,
                fiber = 0f,
                sugar = 0f,
                servingSize = 100f,
                servingUnit = "g"
            )

            viewModel.addCustomFood(
                name = "Riz complet",
                calories = 123,
                protein = 2.6f,
                carbs = 23f,
                fat = 0.9f,
                fiber = 1.8f,
                sugar = 0.4f,
                servingSize = 100f,
                servingUnit = "g"
            )

            viewModel.addCustomFood(
                name = "Saumon",
                calories = 208,
                protein = 25.4f,
                carbs = 0f,
                fat = 12.4f,
                fiber = 0f,
                sugar = 0f,
                servingSize = 100f,
                servingUnit = "g"
            )

            Log.d(TAG, "Aliments de test ajoutés")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'ajout des aliments de test", e)
        }
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
                        isOnlineSearch = false
                    } else if (it.isEmpty()) {
                        // Réinitialiser à tous les aliments
                        viewModel.searchFoods("", false)
                        isOnlineSearch = false
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
                    0 -> {
                        Log.d(TAG, "Onglet 'Tous' sélectionné")
                        isOnlineSearch = false
                        viewModel.searchFoods("", false) // Tous les aliments
                    }
                    1 -> {
                        Log.d(TAG, "Onglet 'Favoris' sélectionné")
                        isOnlineSearch = false
                        displayFavoriteFoods()
                    }
                    2 -> {
                        Log.d(TAG, "Onglet 'Récents' sélectionné")
                        isOnlineSearch = false
                        displayRecentFoods()
                    }
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
        Log.d(TAG, "Recherche: '$query', En ligne: $isOnlineSearch")
        if (query.isNotEmpty()) {
            viewModel.searchFoods(query, isOnlineSearch)
        } else {
            viewModel.searchFoods("", false) // Afficher tous les aliments
        }
    }

    private fun displayFavoriteFoods() {
        // Pour l'instant, filtrer côté UI - idéalement ajouter une méthode dans le ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { foods ->
                val favoriteFoods = foods.filter { it.favorite }
                updateFoodsList(favoriteFoods)
            }
        }
    }

    private fun displayRecentFoods() {
        // Pour l'instant, trier par lastUsed - idéalement ajouter une méthode dans le ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { foods ->
                val recentFoods = foods.sortedByDescending { it.lastUsed }.take(20)
                updateFoodsList(recentFoods)
            }
        }
    }

    private fun navigateToFoodDetails(food: Food) {
        try {
            val action = FoodSearchFragmentDirections.actionFoodSearchToFoodDetails(food.id)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur navigation vers détails", e)
            Toast.makeText(context, "Erreur de navigation", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToAddFood() {
        try {
            val action = FoodSearchFragmentDirections.actionFoodSearchToAddFood()
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur navigation vers ajout", e)
            Toast.makeText(context, "Erreur de navigation", Toast.LENGTH_SHORT).show()
        }
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
                Log.d(TAG, "Résultats locaux reçus: ${foods.size} aliments")
                if (!isOnlineSearch) {
                    updateFoodsList(foods)
                }
            }
        }

        // Observer les résultats de recherche en ligne
        viewModel.onlineSearchResults.observe(viewLifecycleOwner) { foods ->
            Log.d(TAG, "Résultats en ligne reçus: ${foods.size} aliments")
            if (isOnlineSearch) {
                updateFoodsList(foods)
            }
        }

        // Observer la requête de recherche
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchQuery.collect { query ->
                if (binding.searchView.query.toString() != query) {
                    binding.searchView.setQuery(query, false)
                }
            }
        }
    }

    private fun updateFoodsList(foods: List<Food>) {
        Log.d(TAG, "Mise à jour de la liste avec ${foods.size} aliments")
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