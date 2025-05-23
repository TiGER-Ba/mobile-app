package com.example.mealplanner.ui.recipes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mealplanner.R
import com.example.mealplanner.data.model.Recipe
import com.example.mealplanner.databinding.ItemRecipeBinding

class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit,
    private val onFavoriteClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(
        private val binding: ItemRecipeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentRecipe: Recipe? = null

        init {
            binding.root.setOnClickListener {
                currentRecipe?.let { recipe ->
                    onRecipeClick(recipe)
                }
            }

            binding.buttonFavorite.setOnClickListener {
                currentRecipe?.let { recipe ->
                    onFavoriteClick(recipe)
                }
            }
        }

        fun bind(recipe: Recipe) {
            currentRecipe = recipe

            binding.textViewName.text = recipe.name
            binding.textViewDescription.text = recipe.description

            val prepTime = recipe.preparationTime
            val cookTime = recipe.cookingTime
            val totalTime = prepTime + cookTime

            binding.textViewTime.text = "$totalTime min"
            binding.textViewServings.text = "${recipe.servings} portions"

            val favoriteIcon = if (recipe.favorite) {
                R.drawable.ic_favorite_filled
            } else {
                R.drawable.ic_favorite_outline
            }
            binding.buttonFavorite.setImageResource(favoriteIcon)

            if (!recipe.imageUrl.isNullOrEmpty()) {
                Glide.with(binding.imageViewRecipe.context)
                    .load(recipe.imageUrl)
                    .placeholder(R.drawable.placeholder_recipe)
                    .error(R.drawable.placeholder_recipe)
                    .into(binding.imageViewRecipe)
            } else {
                binding.imageViewRecipe.setImageResource(R.drawable.placeholder_recipe)
            }

            if (recipe.tags.isNotEmpty()) {
                val tagsText = recipe.tags.joinToString(", ")
                binding.textViewTags.text = tagsText
                binding.textViewTags.visibility = View.VISIBLE
            } else {
                binding.textViewTags.visibility = View.GONE
            }
        }
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}