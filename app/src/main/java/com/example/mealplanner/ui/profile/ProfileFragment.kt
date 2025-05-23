package com.example.mealplanner.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mealplanner.R
import com.example.mealplanner.data.model.NutritionGoal
import com.example.mealplanner.data.model.UserProfile
import com.example.mealplanner.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    companion object {
        private const val TAG = "ProfileFragment"
    }

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

        Log.d(TAG, "ProfileFragment créé")

        setupDropdowns()
        observeUserProfile()
        setupSaveButton()

        // Charger le profil existant ou créer un profil par défaut
        loadOrCreateProfile()
    }

    private fun loadOrCreateProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Vérifier si un profil existe déjà
                viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
                    if (profile == null) {
                        // Créer un profil par défaut
                        createDefaultProfile()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du chargement du profil", e)
                createDefaultProfile()
            }
        }
    }

    private fun createDefaultProfile() {
        // Créer un profil par défaut pour les tests
        viewModel.saveUserProfile(
            name = "Utilisateur",
            age = 30,
            gender = "Homme",
            weight = 70f,
            height = 175f,
            goal = NutritionGoal.MAINTENANCE,
            activityLevel = 2
        )
        Log.d(TAG, "Profil par défaut créé")
    }

    private fun setupDropdowns() {
        try {
            // Configurer le dropdown pour le genre
            val genders = arrayOf("Homme", "Femme", "Autre")
            val genderAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, genders)
            (binding.dropdownGender as? AutoCompleteTextView)?.apply {
                setAdapter(genderAdapter)
                setText("Homme", false) // Valeur par défaut
            }

            // Configurer le dropdown pour l'objectif
            val goals = listOf(
                Pair(NutritionGoal.WEIGHT_LOSS, "Perte de poids"),
                Pair(NutritionGoal.WEIGHT_GAIN, "Prise de poids"),
                Pair(NutritionGoal.MAINTENANCE, "Maintien du poids"),
                Pair(NutritionGoal.MUSCLE_GAIN, "Prise de muscle")
            )
            val goalAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, goals.map { it.second })
            (binding.dropdownGoal as? AutoCompleteTextView)?.apply {
                setAdapter(goalAdapter)
                setText("Maintien du poids", false) // Valeur par défaut
            }

            // Configurer le dropdown pour le niveau d'activité
            val activityLevels = listOf(
                Pair(1, "Sédentaire"),
                Pair(2, "Légèrement actif"),
                Pair(3, "Modérément actif"),
                Pair(4, "Très actif"),
                Pair(5, "Extrêmement actif")
            )
            val activityAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, activityLevels.map { it.second })
            (binding.dropdownActivityLevel as? AutoCompleteTextView)?.apply {
                setAdapter(activityAdapter)
                setText("Légèrement actif", false) // Valeur par défaut
            }

            Log.d(TAG, "Dropdowns configurés")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la configuration des dropdowns", e)
        }
    }

    private fun observeUserProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                Log.d(TAG, "Profil utilisateur reçu: ${it.name}")
                updateUI(it)
            }
        }
    }

    private fun updateUI(profile: UserProfile) {
        try {
            binding.editTextName.setText(profile.name)
            binding.editTextAge.setText(if (profile.age > 0) profile.age.toString() else "")
            binding.editTextWeight.setText(if (profile.weight > 0) profile.weight.toString() else "")
            binding.editTextHeight.setText(if (profile.height > 0) profile.height.toString() else "")

            // Sélectionner le genre
            when (profile.gender.lowercase()) {
                "homme" -> binding.dropdownGender.setText("Homme", false)
                "femme" -> binding.dropdownGender.setText("Femme", false)
                "autre" -> binding.dropdownGender.setText("Autre", false)
                else -> binding.dropdownGender.setText("Homme", false) // Valeur par défaut
            }

            // Sélectionner l'objectif
            val goalText = when (profile.goal) {
                NutritionGoal.WEIGHT_LOSS -> "Perte de poids"
                NutritionGoal.WEIGHT_GAIN -> "Prise de poids"
                NutritionGoal.MAINTENANCE -> "Maintien du poids"
                NutritionGoal.MUSCLE_GAIN -> "Prise de muscle"
            }
            binding.dropdownGoal.setText(goalText, false)

            // Sélectionner le niveau d'activité
            val activityText = when (profile.activityLevel) {
                1 -> "Sédentaire"
                2 -> "Légèrement actif"
                3 -> "Modérément actif"
                4 -> "Très actif"
                5 -> "Extrêmement actif"
                else -> "Légèrement actif" // Valeur par défaut
            }
            binding.dropdownActivityLevel.setText(activityText, false)

            // Objectifs nutritionnels
            binding.textViewCalories.text = "${profile.dailyCalorieTarget} kcal"
            binding.textViewProtein.text = "${profile.proteinTarget}g"
            binding.textViewCarbs.text = "${profile.carbTarget}g"
            binding.textViewFat.text = "${profile.fatTarget}g"

            Log.d(TAG, "Interface utilisateur mise à jour")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la mise à jour de l'interface", e)
        }
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                saveUserProfile()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Vérifier le nom
        if (binding.editTextName.text.isNullOrEmpty()) {
            binding.nameInputLayout.error = "Le nom est requis"
            isValid = false
        } else {
            binding.nameInputLayout.error = null
        }

        // Vérifier l'âge
        val ageText = binding.editTextAge.text.toString()
        if (ageText.isEmpty() || ageText.toIntOrNull() == null || ageText.toInt() <= 0) {
            binding.ageInputLayout.error = "Âge valide requis"
            isValid = false
        } else {
            binding.ageInputLayout.error = null
        }

        // Vérifier le poids
        val weightText = binding.editTextWeight.text.toString()
        if (weightText.isEmpty() || weightText.toFloatOrNull() == null || weightText.toFloat() <= 0) {
            binding.weightInputLayout.error = "Poids valide requis"
            isValid = false
        } else {
            binding.weightInputLayout.error = null
        }

        // Vérifier la taille
        val heightText = binding.editTextHeight.text.toString()
        if (heightText.isEmpty() || heightText.toFloatOrNull() == null || heightText.toFloat() <= 0) {
            binding.heightInputLayout.error = "Taille valide requise"
            isValid = false
        } else {
            binding.heightInputLayout.error = null
        }

        return isValid
    }

    private fun saveUserProfile() {
        try {
            val name = binding.editTextName.text.toString()
            val age = binding.editTextAge.text.toString().toIntOrNull() ?: 25
            val gender = binding.dropdownGender.text.toString().ifEmpty { "Homme" }
            val weight = binding.editTextWeight.text.toString().toFloatOrNull() ?: 70f
            val height = binding.editTextHeight.text.toString().toFloatOrNull() ?: 175f

            val goalText = binding.dropdownGoal.text.toString()
            val goal = when (goalText) {
                "Perte de poids" -> NutritionGoal.WEIGHT_LOSS
                "Prise de poids" -> NutritionGoal.WEIGHT_GAIN
                "Maintien du poids" -> NutritionGoal.MAINTENANCE
                "Prise de muscle" -> NutritionGoal.MUSCLE_GAIN
                else -> NutritionGoal.MAINTENANCE
            }

            val activityText = binding.dropdownActivityLevel.text.toString()
            val activityLevel = when (activityText) {
                "Sédentaire" -> 1
                "Légèrement actif" -> 2
                "Modérément actif" -> 3
                "Très actif" -> 4
                "Extrêmement actif" -> 5
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

            Toast.makeText(context, "Profil enregistré avec succès", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Profil sauvegardé: $name")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la sauvegarde", e)
            Toast.makeText(context, "Erreur lors de la sauvegarde", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}