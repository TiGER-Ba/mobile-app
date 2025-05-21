package com.example.mealplanner.ui.mealplan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mealplanner.databinding.ItemMealFoodBinding

class MealItemAdapter(
    private val onItemClick: (MealItemDetails) -> Unit,
    private val onDeleteClick: (MealItemDetails) -> Unit
) : ListAdapter<MealItemDetails, MealItemAdapter.MealItemViewHolder>(MealItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealItemViewHolder {
        val binding = ItemMealFoodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MealItemViewHolder(
        private val binding: ItemMealFoodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition // CHANGÉ ICI
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            binding.buttonDelete.setOnClickListener {
                val position = adapterPosition // CHANGÉ ICI
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(mealItem: MealItemDetails) {
            binding.textViewName.text = mealItem.name
            binding.textViewCalories.text = "${mealItem.calories} kcal"

            val quantityText = if (mealItem.food != null) {
                val unit = mealItem.food.servingUnit
                "${mealItem.item.quantity} × ${mealItem.item.servingSize}$unit"
            } else {
                "${mealItem.item.quantity} portion(s)"
            }

            binding.textViewQuantity.text = quantityText
        }
    }

    class MealItemDiffCallback : DiffUtil.ItemCallback<MealItemDetails>() {
        override fun areItemsTheSame(oldItem: MealItemDetails, newItem: MealItemDetails): Boolean {
            return oldItem.item.id == newItem.item.id
        }

        override fun areContentsTheSame(oldItem: MealItemDetails, newItem: MealItemDetails): Boolean {
            return oldItem == newItem
        }
    }
}