package com.example.mealplanner.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mealplanner.R
import com.example.mealplanner.data.model.NutritionGoal
import com.example.mealplanner.data.model.UserProfile
import com.example.mealplanner.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        observeUserProfile()
        setupSaveButton()
    }

    private fun setupDropdowns() {
        // Configurer le dropdown pour le genre
        val genders = arrayOf("Homme", "Femme", "Autre")
        val genderAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, genders)
        (binding.dropdownGender as? AutoCompleteTextView)?.setAdapter(genderAdapter)

        // Configurer le dropdown pour l'objectif
        val goals = listOf(
            Pair(NutritionGoal.WEIGHT_LOSS, getString(R.string.goal_weight_loss)),
            Pair(NutritionGoal.WEIGHT_GAIN, getString(R.string.goal_weight_gain)),
            Pair(NutritionGoal.MAINTENANCE, getString(R.string.goal_maintenance)),
            Pair(NutritionGoal.MUSCLE_GAIN, getString(R.string.goal_muscle_gain))
        )
        val goalAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, goals.map { it.second })
        (binding.dropdownGoal as? AutoCompleteTextView)?.setAdapter(goalAdapter)

        // Configurer le dropdown pour le niveau d'activité
        val activityLevels = listOf(
            Pair(1, getString(R.string.activity_sedentary)),
            Pair(2, getString(R.string.activity_lightly_active)),
            Pair(3, getString(R.string.activity_moderately_active)),
            Pair(4, getString(R.string.activity_very_active)),
            Pair(5, getString(R.string.activity_extremely_active))
        )
        val activityAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, activityLevels.map { it.second })
        (binding.dropdownActivityLevel as? AutoCompleteTextView)?.setAdapter(activityAdapter)
    }

    private fun observeUserProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let { updateUI(it) }
        }
    }

    private fun updateUI(profile: UserProfile) {
        binding.editTextName.setText(profile.name)
        binding.editTextAge.setText(profile.age.toString())
        binding.editTextWeight.setText(profile.weight.toString())
        binding.editTextHeight.setText(profile.height.toString())

        // Sélectionner le genre
        when (profile.gender.lowercase()) {
            "homme" -> binding.dropdownGender.setText("Homme", false)
            "femme" -> binding.dropdownGender.setText("Femme", false)
            "autre" -> binding.dropdownGender.setText("Autre", false)
            else -> binding.dropdownGender.setText("Homme", false) // Valeur par défaut
        }

        // Sélectionner l'objectif
        val goalText = when (profile.goal) {
            NutritionGoal.WEIGHT_LOSS -> getString(R.string.goal_weight_loss)
            NutritionGoal.WEIGHT_GAIN -> getString(R.string.goal_weight_gain)
            NutritionGoal.MAINTENANCE -> getString(R.string.goal_maintenance)
            NutritionGoal.MUSCLE_GAIN -> getString(R.string.goal_muscle_gain)
        }
        binding.dropdownGoal.setText(goalText, false)

        // Sélectionner le niveau d'activité
        val activityText = when (profile.activityLevel) {
            1 -> getString(R.string.activity_sedentary)
            2 -> getString(R.string.activity_lightly_active)
            3 -> getString(R.string.activity_moderately_active)
            4 -> getString(R.string.activity_very_active)
            5 -> getString(R.string.activity_extremely_active)
            else -> getString(R.string.activity_lightly_active) // Valeur par défaut
        }
        binding.dropdownActivityLevel.setText(activityText, false)

        // Objectifs nutritionnels
        binding.textViewCalories.text = "${profile.dailyCalorieTarget} kcal"
        binding.textViewProtein.text = "${profile.proteinTarget}g"
        binding.textViewCarbs.text = "${profile.carbTarget}g"
        binding.textViewFat.text = "${profile.fatTarget}g"
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                saveUserProfile()
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Vérifier que tous les champs sont remplis
        if (binding.editTextName.text.isNullOrEmpty() ||
            binding.editTextAge.text.isNullOrEmpty() ||
            binding.dropdownGender.text.isNullOrEmpty() ||
            binding.editTextWeight.text.isNullOrEmpty() ||
            binding.editTextHeight.text.isNullOrEmpty() ||
            binding.dropdownGoal.text.isNullOrEmpty() ||
            binding.dropdownActivityLevel.text.isNullOrEmpty()
        ) {
            Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveUserProfile() {
        val name = binding.editTextName.text.toString()
        val age = binding.editTextAge.text.toString().toIntOrNull() ?: 0
        val gender = binding.dropdownGender.text.toString()
        val weight = binding.editTextWeight.text.toString().toFloatOrNull() ?: 0f
        val height = binding.editTextHeight.text.toString().toFloatOrNull() ?: 0f

        val goalText = binding.dropdownGoal.text.toString()
        val goal = when (goalText) {
            getString(R.string.goal_weight_loss) -> NutritionGoal.WEIGHT_LOSS
            getString(R.string.goal_weight_gain) -> NutritionGoal.WEIGHT_GAIN
            getString(R.string.goal_maintenance) -> NutritionGoal.MAINTENANCE
            getString(R.string.goal_muscle_gain) -> NutritionGoal.MUSCLE_GAIN
            else -> NutritionGoal.MAINTENANCE
        }

        val activityText = binding.dropdownActivityLevel.text.toString()
        val activityLevel = when (activityText) {
            getString(R.string.activity_sedentary) -> 1
            getString(R.string.activity_lightly_active) -> 2
            getString(R.string.activity_moderately_active) -> 3
            getString(R.string.activity_very_active) -> 4
            getString(R.string.activity_extremely_active) -> 5
            else -> 2
        }

        viewModel.saveUserProfile(
            name = name,
            age = age,
            gender = gender,
            weight = weight,
            height = height,
            goal = goal,
            activityLevel = activityLevel
        )

        Toast.makeText(context, "Profil enregistré", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Ajoutons un fichier dropdown_item.xml dans res/layout pour les dropdowns
// Nous le ferons après ce fragment