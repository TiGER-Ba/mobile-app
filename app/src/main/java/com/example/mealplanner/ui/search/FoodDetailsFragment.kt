package com.example.mealplanner.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.mealplanner.R
import com.example.mealplanner.databinding.FragmentFoodDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FoodDetailsFragment : Fragment() {

    private var _binding: FragmentFoodDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FoodViewModel by viewModels()
    private val args: FoodDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFoodDetails()
        setupButtons()
    }

    private fun loadFoodDetails() {
        viewModel.getFoodByIdLiveData(args.foodId).observe(viewLifecycleOwner) { food ->
            food?.let {
                binding.textViewName.text = it.name
                binding.textViewCalories.text = "${it.calories} kcal"
                binding.textViewProtein.text = "${it.protein}g"
                binding.textViewCarbs.text = "${it.carbs}g"
                binding.textViewFat.text = "${it.fat}g"
                binding.textViewFiber.text = "${it.fiber}g"
                binding.textViewSugar.text = "${it.sugar}g"
                binding.textViewServingSize.text = "${it.servingSize} ${it.servingUnit}"

                // Chargement de l'image
                if (!it.imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(it.imageUrl)
                        .placeholder(R.drawable.placeholder_food)
                        .into(binding.imageViewFood)
                } else {
                    binding.imageViewFood.setImageResource(R.drawable.placeholder_food)
                }

                // Mise à jour du bouton des favoris
                updateFavoriteButton(it.favorite)
            } ?: run {
                Toast.makeText(context, "Aliment introuvable", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        val iconRes = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline
        binding.buttonFavorite.setImageResource(iconRes)
    }

    private fun setupButtons() {
        binding.buttonFavorite.setOnClickListener {
            viewModel.getFoodByIdLiveData(args.foodId).value?.let { food ->
                viewModel.toggleFavorite(food)
            }
        }

        // CORRECTION: Navigation vers l'ajout à un repas
        binding.buttonAddToMeal.setOnClickListener {
            try {
                val action = FoodDetailsFragmentDirections.actionFoodDetailsToAddToMeal(args.foodId)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur de navigation", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}