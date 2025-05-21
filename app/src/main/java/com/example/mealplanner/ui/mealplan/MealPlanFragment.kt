package com.example.mealplanner.ui.mealplan

import android.os.Bundle
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MealPlanFragment : Fragment() {

    private var _binding: FragmentMealPlanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MealPlanViewModel by viewModels()
    private lateinit var mealAdapter: MealAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealPlanBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDateSelector()
        setupMealsList()
        setupAddMealButton()
        observeViewModel()
    }

    private fun setupDateSelector() {
        binding.dateSelector.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Sélectionner une date")
                .setSelection(viewModel.selectedDate.value)
                .build()

            datePicker.addOnPositiveButtonClickListener { date ->
                viewModel.selectDate(date)
            }

            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        // Observer la date sélectionnée
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDate.collect { date ->
                updateDateDisplay(date)
            }
        }
    }

    private fun updateDateDisplay(date: Long) {
        val sdf = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.getDefault())
        binding.textViewSelectedDate.text = sdf.format(Date(date))
    }

    private fun setupMealsList() {
        mealAdapter = MealAdapter { meal ->
            // Navigation vers le détail du repas
            val action = MealPlanFragmentDirections.actionMealPlanToMealDetails(meal.id)
            findNavController().navigate(action)
        }

        binding.recyclerViewMeals.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mealAdapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.mealsForCurrentPlan.collect { meals ->
                mealAdapter.submitList(meals)

                // Afficher un message si aucun repas n'est planifié
                if (meals.isEmpty()) {
                    binding.textViewNoMeals.visibility = View.VISIBLE
                } else {
                    binding.textViewNoMeals.visibility = View.GONE
                }
            }
        }
    }

    private fun setupAddMealButton() {
        binding.fabAddMeal.setOnClickListener {
            val action = MealPlanFragmentDirections.actionMealPlanToAddMeal(viewModel.selectedDate.value)
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }

        viewModel.currentMealPlan.observe(viewLifecycleOwner) { mealPlan ->
            // On peut utiliser l'ID du plan de repas pour d'autres actions si nécessaire
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.meal_plan_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_date_range -> {
                showDateRangePicker()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDateRangePicker() {
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
            Toast.makeText(
                context,
                "Période sélectionnée: ${dateRange.first} à ${dateRange.second}",
                Toast.LENGTH_SHORT
            ).show()
            // Ici vous pourriez implémenter l'affichage d'une vue hebdomadaire
        }

        dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}