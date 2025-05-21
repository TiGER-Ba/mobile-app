package com.example.mealplanner.ui.recipes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mealplanner.data.model.RecipeIngredient
import com.example.mealplanner.databinding.ItemRecipeIngredientBinding

class RecipeIngredientAdapter : RecyclerView.Adapter<RecipeIngredientAdapter.IngredientViewHolder>() {

    private var ingredients = listOf<RecipeIngredientDisplay>()

    fun updateIngredients(newIngredients: List<RecipeIngredient>) {
        // Pour cet exemple, nous affichons simplement les noms des ingrédients
        // Dans une implémentation complète, vous devriez récupérer les détails des aliments
        this.ingredients = newIngredients.map { ingredient ->
            RecipeIngredientDisplay(
                ingredient = ingredient,
                foodName = "Aliment ${ingredient.foodId}" // Remplacer par le vrai nom
            )
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemRecipeIngredientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount(): Int = ingredients.size

    class IngredientViewHolder(
        private val binding: ItemRecipeIngredientBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ingredientDisplay: RecipeIngredientDisplay) {
            binding.textViewIngredientName.text = ingredientDisplay.foodName
            binding.textViewQuantity.text = "${ingredientDisplay.ingredient.quantity} ${ingredientDisplay.ingredient.unit}"
        }
    }
}

// Classe spécifique pour l'affichage des ingrédients dans cette liste
data class RecipeIngredientDisplay(
    val ingredient: RecipeIngredient,
    val foodName: String
)