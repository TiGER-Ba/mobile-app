package com.example.mealplanner.ui.recipes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mealplanner.databinding.ItemTempIngredientBinding

class TempIngredientsAdapter(
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<TempIngredientsAdapter.TempIngredientViewHolder>() {

    private var ingredients = listOf<RecipeIngredientWithFood>()

    fun updateIngredients(newIngredients: List<RecipeIngredientWithFood>) {
        this.ingredients = newIngredients
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TempIngredientViewHolder {
        val binding = ItemTempIngredientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TempIngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TempIngredientViewHolder, position: Int) {
        holder.bind(ingredients[position], position)
    }

    override fun getItemCount(): Int = ingredients.size

    inner class TempIngredientViewHolder(
        private val binding: ItemTempIngredientBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ingredientWithFood: RecipeIngredientWithFood, position: Int) {
            binding.textViewIngredientName.text = ingredientWithFood.food.name
            binding.textViewQuantity.text = "${ingredientWithFood.ingredient.quantity} ${ingredientWithFood.ingredient.unit}"

            binding.buttonRemove.setOnClickListener {
                onRemoveClick(position)
            }
        }
    }
}