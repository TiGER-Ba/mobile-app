package com.example.mealplanner.ui.mealplan

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealplanner.R
import com.example.mealplanner.databinding.FragmentMealPlanBinding
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MealPlanFragment : Fragment() {

    private var _binding: FragmentMealPlanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MealPlanViewModel by viewModels()
    private lateinit var mealAdapter: MealAdapter

    // CORRECTION: Variables pour limiter les toasts
    private var lastToastTime = 0L
    private var lastToastMessage = ""

    companion object {
        private const val TAG = "MealPlanFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "🟢 onCreate() - Fragment créé")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "🎨 onCreateView() - Création de la vue")

        try {
            _binding = FragmentMealPlanBinding.inflate(inflater, container, false)
            setHasOptionsMenu(true)
            Log.d(TAG, "✅ Binding créé avec succès")
            return binding.root
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de la création de la vue", e)
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "🔧 onViewCreated() - Configuration de la vue")

        try {
            setupDateSelector()
            Log.d(TAG, "✅ Date selector configuré")

            setupMealsList()
            Log.d(TAG, "✅ Liste des repas configurée")

            setupAddMealButton()
            Log.d(TAG, "✅ Bouton d'ajout configuré")

            observeViewModel()
            Log.d(TAG, "✅ ViewModel observé")

            // CORRECTION: Initialiser avec la date actuelle
            val currentDate = System.currentTimeMillis()
            Log.d(TAG, "📅 Initialisation avec la date: $currentDate")
            viewModel.selectDate(currentDate)
            updateDateDisplay(currentDate)

            Log.d(TAG, "🎯 Configuration complète terminée")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de la configuration de la vue", e)
            showErrorMessage("Erreur lors de l'initialisation: ${e.message}")
        }
    }

    private fun setupDateSelector() {
        try {
            binding.dateSelector.setOnClickListener {
                Log.d(TAG, "📅 Clic sur le sélecteur de date")
                showDatePicker()
            }

            // Observer la date sélectionnée
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.selectedDate.collect { date ->
                    if (isAdded && !isDetached && _binding != null) {
                        Log.d(TAG, "📅 Date mise à jour: $date")
                        updateDateDisplay(date)
                    }
                }
            }

            Log.d(TAG, "✅ Date selector configuré avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur configuration date selector", e)
        }
    }

    private fun showDatePicker() {
        try {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Sélectionner une date")
                .setSelection(viewModel.selectedDate.value)
                .build()

            datePicker.addOnPositiveButtonClickListener { date ->
                Log.d(TAG, "📅 Nouvelle date sélectionnée: $date")
                viewModel.selectDate(date)
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur ouverture date picker", e)
            showLimitedToast("Impossible d'ouvrir le sélecteur de date")
        }
    }

    private fun updateDateDisplay(date: Long) {
        if (!isAdded || isDetached || _binding == null) {
            Log.w(TAG, "⚠️ Fragment non attaché, pas de mise à jour de date")
            return
        }

        try {
            val sdf = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.getDefault())
            val formattedDate = sdf.format(Date(date))
            binding.textViewSelectedDate.text = formattedDate
            Log.d(TAG, "📅 Date affichée: $formattedDate")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur mise à jour affichage date", e)
            binding.textViewSelectedDate.text = "Date non disponible"
        }
    }

    private fun setupMealsList() {
        try {
            mealAdapter = MealAdapter { meal ->
                Log.d(TAG, "🍽️ Clic sur repas: ${meal.id}")
                try {
                    val action = MealPlanFragmentDirections.actionMealPlanToMealDetails(meal.id)
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erreur navigation vers détails repas", e)
                    showLimitedToast("Impossible d'ouvrir les détails du repas")
                }
            }

            binding.recyclerViewMeals.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = mealAdapter
                Log.d(TAG, "✅ RecyclerView configuré")
            }

            // Observer les repas avec meilleure gestion du cycle de vie
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.mealsForCurrentPlan.collect { meals ->
                    if (isAdded && !isDetached && _binding != null) {
                        Log.d(TAG, "🍽️ Mise à jour liste repas: ${meals.size} repas")
                        mealAdapter.submitList(meals)

                        // Afficher/masquer le message "aucun repas"
                        if (meals.isEmpty()) {
                            binding.textViewNoMeals.visibility = View.VISIBLE
                            binding.recyclerViewMeals.visibility = View.VISIBLE
                            Log.d(TAG, "📝 Affichage message 'aucun repas'")
                        } else {
                            binding.textViewNoMeals.visibility = View.GONE
                            binding.recyclerViewMeals.visibility = View.VISIBLE
                            Log.d(TAG, "📋 Affichage de ${meals.size} repas")
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur configuration liste repas", e)
            showLimitedToast("Erreur lors de la configuration de la liste")
        }
    }

    private fun setupAddMealButton() {
        try {
            binding.fabAddMeal.setOnClickListener {
                Log.d(TAG, "➕ Clic sur bouton ajouter repas")
                try {
                    val action = MealPlanFragmentDirections.actionMealPlanToAddMeal(viewModel.selectedDate.value)
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erreur navigation vers ajout repas", e)
                    showLimitedToast("Impossible d'ouvrir l'ajout de repas")
                }
            }
            Log.d(TAG, "✅ Bouton d'ajout configuré")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur configuration bouton ajout", e)
        }
    }

    private fun observeViewModel() {
        try {
            // CORRECTION: Observer les messages avec limitation stricte
            viewModel.message.observe(viewLifecycleOwner) { message ->
                message?.let {
                    if (isAdded && !isDetached) {
                        Log.d(TAG, "💬 Message du ViewModel: $it")
                        showLimitedToast(it)
                        viewModel.clearMessage()
                    }
                }
            }

            // Observer le plan de repas actuel
            viewModel.currentMealPlan.observe(viewLifecycleOwner) { mealPlan ->
                mealPlan?.let {
                    Log.d(TAG, "📋 Plan de repas actuel: ${it.id} pour la date ${it.date}")
                }
            }

            Log.d(TAG, "✅ Observateurs ViewModel configurés")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur configuration observateurs", e)
        }
    }

    // CORRECTION: Méthode pour limiter strictement les toasts
    private fun showLimitedToast(message: String) {
        if (!isAdded || isDetached) return

        try {
            val currentTime = System.currentTimeMillis()
            if (message != lastToastMessage || currentTime - lastToastTime > 3000) { // 3 secondes minimum
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                lastToastMessage = message
                lastToastTime = currentTime
                Log.d(TAG, "📢 Toast affiché: $message")
            } else {
                Log.d(TAG, "🚫 Toast ignoré (répétition): $message")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur affichage toast", e)
        }
    }

    private fun showErrorMessage(message: String) {
        showLimitedToast(message)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        try {
            inflater.inflate(R.menu.meal_plan_menu, menu)
            super.onCreateOptionsMenu(menu, inflater)
            Log.d(TAG, "✅ Menu créé")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur création menu", e)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_date_range -> {
                Log.d(TAG, "📅 Sélection plage de dates")
                showDateRangePicker()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDateRangePicker() {
        if (!isAdded || isDetached) return

        try {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Sélectionner une période")
                .setSelection(
                    Pair(
                        viewModel.selectedDate.value,
                        viewModel.selectedDate.value + (6 * 24 * 60 * 60 * 1000) // +6 jours
                    )
                )
                .build()

            dateRangePicker.addOnPositiveButtonClickListener { dateRange ->
                showLimitedToast("Période sélectionnée: ${dateRange.first} à ${dateRange.second}")
            }

            dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur ouverture sélecteur plage", e)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "▶️ onResume() - Fragment repris")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "⏸️ onPause() - Fragment en pause")
    }

    override fun onDestroyView() {
        Log.d(TAG, "🗑️ onDestroyView() - Destruction de la vue")
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🔴 onDestroy() - Fragment détruit")
    }
}