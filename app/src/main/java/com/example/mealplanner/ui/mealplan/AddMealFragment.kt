package com.example.mealplanner.ui.mealplan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mealplanner.R
import com.example.mealplanner.data.model.MealType
import com.example.mealplanner.databinding.FragmentAddMealBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddMealFragment : Fragment() {

    private var _binding: FragmentAddMealBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MealPlanViewModel by viewModels()
    private val args: AddMealFragmentArgs by navArgs()

    // Heure sélectionnée en millisecondes
    private var selectedTimeMillis = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser avec la date sélectionnée par l'utilisateur
        viewModel.selectDate(args.date)

        setupMealTypeDropdown()
        setupTimeSelector()
        setupAddButton()
        observeViewModel()
    }

    private fun setupMealTypeDropdown() {
        val mealTypes = listOf(
            Pair(MealType.BREAKFAST, getString(R.string.meal_type_breakfast)),
            Pair(MealType.LUNCH, getString(R.string.meal_type_lunch)),
            Pair(MealType.DINNER, getString(R.string.meal_type_dinner)),
            Pair(MealType.SNACK, getString(R.string.meal_type_snack))
        )

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            mealTypes.map { it.second }
        )

        (binding.dropdownMealType as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            setText(adapter.getItem(0), false) // Sélection par défaut
        }
    }

    private fun setupTimeSelector() {
        // Affichage de l'heure actuelle par défaut
        updateTimeDisplay(System.currentTimeMillis())

        binding.editTextTime.setOnClickListener {
            showTimePicker()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedTimeMillis

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText("Sélectionner l'heure du repas")
            .build()

        picker.addOnPositiveButtonClickListener {
            calendar.set(Calendar.HOUR_OF_DAY, picker.hour)
            calendar.set(Calendar.MINUTE, picker.minute)
            selectedTimeMillis = calendar.timeInMillis
            updateTimeDisplay(selectedTimeMillis)
        }

        picker.show(parentFragmentManager, "TIME_PICKER")
    }

    private fun updateTimeDisplay(timeMillis: Long) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.editTextTime.setText(sdf.format(Date(timeMillis)))
    }

    private fun setupAddButton() {
        binding.buttonAddMeal.setOnClickListener {
            if (validateInputs()) {
                addMeal()
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Vérifier que le type de repas est sélectionné
        if (binding.dropdownMealType.text.isNullOrEmpty()) {
            Toast.makeText(
                context,
                "Veuillez sélectionner un type de repas",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        // Vérifier que l'heure est sélectionnée
        if (binding.editTextTime.text.isNullOrEmpty()) {
            Toast.makeText(
                context,
                "Veuillez sélectionner une heure",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

    private fun addMeal() {
        // Récupérer le type de repas
        val mealTypeText = binding.dropdownMealType.text.toString()
        val mealType = when (mealTypeText) {
            getString(R.string.meal_type_breakfast) -> MealType.BREAKFAST
            getString(R.string.meal_type_lunch) -> MealType.LUNCH
            getString(R.string.meal_type_dinner) -> MealType.DINNER
            getString(R.string.meal_type_snack) -> MealType.SNACK
            else -> MealType.BREAKFAST // Par défaut
        }

        // Récupérer le nom et les notes
        val name = binding.editTextName.text.toString()
        val notes = binding.editTextNotes.text.toString()

        // Ajouter le repas avec tous les paramètres
        viewModel.currentMealPlan.value?.let { mealPlan ->
            viewModel.addMeal(
                type = mealType,
                time = selectedTimeMillis,
                name = name,
                notes = notes
            )
        }
    }

    private fun observeViewModel() {
        viewModel.message.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                if (it.contains("ajouté", ignoreCase = true)) {
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