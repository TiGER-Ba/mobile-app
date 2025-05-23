package com.example.mealplanner.ui.search

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
import com.example.mealplanner.R
import com.example.mealplanner.data.model.Food
import com.example.mealplanner.databinding.FragmentFoodSearchBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FoodSearchFragment : Fragment() {

    private var _binding: FragmentFoodSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FoodViewModel by viewModels()
    private lateinit var foodAdapter: FoodAdapter

    // État de recherche pour savoir si on utilise la recherche locale ou en ligne
    private var isOnlineSearch = false

    // CORRECTION: SharedPreferences pour éviter l'ajout répétitif de données de test
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "FoodSearchFragment"
        private const val PREF_TEST_DATA_ADDED = "test_data_added"
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

        // CORRECTION: Charger les données initiales seulement si nécessaire
        loadInitialData()
    }

    private fun loadInitialData() {
        Log.d(TAG, "Chargement des données initiales")

        // CORRECTION: Vérifier si les données de test ont déjà été ajoutées
        viewLifecycleOwner.lifecycleScope.launch {
            val testDataAdded = sharedPreferences.getBoolean(PREF_TEST_DATA_ADDED, false)
            if (!testDataAdded) {
                addTestFoodsIfEmpty()
                sharedPreferences.edit().putBoolean(PREF_TEST_DATA_ADDED, true).apply()
                Log.d(TAG, "Données de test ajoutées pour la première fois")
            } else {
                Log.d(TAG, "Données de test déjà présentes, pas d'ajout")
            }

            // Charger tous les aliments
            viewModel.searchFoods("", false)
        }
    }

    private suspend fun addTestFoodsIfEmpty() {
        try {
            // CORRECTION: Ajouter des données de test seulement si elles n'existent pas déjà
            Log.d(TAG, "Ajout des aliments de test")

            val testFoods = listOf(
                Food(
                    id = "test_banana",
                    name = "Banane",
                    calories = 89,
                    protein = 1.1f,
                    carbs = 22.8f,
                    fat = 0.3f,
                    fiber = 2.6f,
                    sugar = 12.2f,
                    servingSize = 100f,
                    servingUnit = "g"
                ),
                Food(
                    id = "test_apple",
                    name = "Pomme",
                    calories = 52,
                    protein = 0.3f,
                    carbs = 13.8f,
                    fat = 0.2f,
                    fiber = 2.4f,
                    sugar = 10.4f,
                    servingSize = 100f,
                    servingUnit = "g"
                ),
                Food(
                    id = "test_chicken",
                    name = "Poulet (blanc)",
                    calories = 165,
                    protein = 31f,
                    carbs = 0f,
                    fat = 3.6f,
                    fiber = 0f,
                    sugar = 0f,
                    servingSize = 100f,
                    servingUnit = "g"
                ),
                Food(
                    id = "test_rice",
                    name = "Riz complet",
                    calories = 123,
                    protein = 2.6f,
                    carbs = 23f,
                    fat = 0.9f,
                    fiber = 1.8f,
                    sugar = 0.4f,
                    servingSize = 100f,
                    servingUnit = "g"
                ),
                Food(
                    id = "test_salmon",
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
            )

            // Ajouter chaque aliment individuellement pour éviter les doublons
            testFoods.forEach { food ->
                try {
                    viewModel.addCustomFoodDirect(food)
                } catch (e: Exception) {
                    Log.w(TAG, "Aliment ${food.name} déjà présent ou erreur: ${e.message}")
                }
            }

            Log.d(TAG, "Aliments de test traités")
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
                        viewModel.searchFoods(it, false)
                        isOnlineSearch = false
                    } else if (it.isEmpty()) {
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
                        viewModel.searchFoods("", false)
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
            viewModel.searchFoods("", false)
        }
    }

    private fun displayFavoriteFoods() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { foods ->
                if (isAdded && !isDetached) {
                    val favoriteFoods = foods.filter { it.favorite }
                    updateFoodsList(favoriteFoods)
                }
            }
        }
    }

    private fun displayRecentFoods() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { foods ->
                if (isAdded && !isDetached) {
                    val recentFoods = foods.sortedByDescending { it.lastUsed }.take(20)
                    updateFoodsList(recentFoods)
                }
            }
        }
    }

    private fun navigateToFoodDetails(food: Food) {
        try {
            val action = FoodSearchFragmentDirections.actionFoodSearchToFoodDetails(food.id)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur navigation vers détails", e)
            showToast("Erreur de navigation")
        }
    }

    private fun navigateToAddFood() {
        try {
            val action = FoodSearchFragmentDirections.actionFoodSearchToAddFood()
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur navigation vers ajout", e)
            showToast("Erreur de navigation")
        }
    }

    private fun observeViewModel() {
        // Observer l'état de chargement
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isAdded && !isDetached) {
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observer les messages d'erreur ou de succès avec limitation
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

        // Observer les résultats de recherche locaux
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { foods ->
                if (isAdded && !isDetached) {
                    Log.d(TAG, "Résultats locaux reçus: ${foods.size} aliments")
                    if (!isOnlineSearch) {
                        updateFoodsList(foods)
                    }
                }
            }
        }

        // Observer les résultats de recherche en ligne
        viewModel.onlineSearchResults.observe(viewLifecycleOwner) { foods ->
            if (isAdded && !isDetached) {
                Log.d(TAG, "Résultats en ligne reçus: ${foods.size} aliments")
                if (isOnlineSearch) {
                    updateFoodsList(foods)
                }
            }
        }

        // Observer la requête de recherche
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchQuery.collect { query ->
                if (isAdded && !isDetached && binding.searchView.query.toString() != query) {
                    binding.searchView.setQuery(query, false)
                }
            }
        }
    }

    private fun updateFoodsList(foods: List<Food>) {
        if (!isAdded || isDetached) return

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