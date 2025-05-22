package com.example.mealplanner.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mealplanner.R
import com.example.mealplanner.data.model.Food
import com.example.mealplanner.databinding.ItemFoodBinding

class FoodAdapter(
    private val onFoodClick: (Food) -> Unit,
    private val onFavoriteClick: (Food) -> Unit
) : ListAdapter<Food, FoodAdapter.FoodViewHolder>(FoodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FoodViewHolder(
        private val binding: ItemFoodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition // CORRIGÉ ICI
                if (position != RecyclerView.NO_POSITION) {
                    onFoodClick(getItem(position))
                }
            }

            binding.buttonFavorite.setOnClickListener {
                val position = adapterPosition // CORRIGÉ ICI
                if (position != RecyclerView.NO_POSITION) {
                    onFavoriteClick(getItem(position))
                }
            }
        }

        fun bind(food: Food) {
            binding.textViewName.text = food.name
            binding.textViewCalories.text = "${food.calories} kcal"
            binding.textViewMacros.text = "P: ${food.protein}g  C: ${food.carbs}g  L: ${food.fat}g"

            val favoriteIcon = if (food.favorite) {
                R.drawable.ic_favorite_filled
            } else {
                R.drawable.ic_favorite_outline
            }
            binding.buttonFavorite.setImageResource(favoriteIcon)

            if (!food.imageUrl.isNullOrEmpty()) {
                Glide.with(binding.imageViewFood.context)
                    .load(food.imageUrl)
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.placeholder_food)
                    .into(binding.imageViewFood)
            } else {
                binding.imageViewFood.setImageResource(R.drawable.placeholder_food)
            }
        }
    }

    class FoodDiffCallback : DiffUtil.ItemCallback<Food>() {
        override fun areItemsTheSame(oldItem: Food, newItem: Food): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Food, newItem: Food): Boolean {
            return oldItem == newItem
        }
    }
}