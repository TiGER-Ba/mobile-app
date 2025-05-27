package com.example.mealplanner.ui.mealplan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mealplanner.data.model.Meal
import com.example.mealplanner.data.model.MealType
import com.example.mealplanner.databinding.ItemAvailableMealBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AvailableMealsAdapter(
    private val onMealClick: (Meal) -> Unit
) : ListAdapter<Meal, AvailableMealsAdapter.AvailableMealViewHolder>(MealDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailableMealViewHolder {
        val binding = ItemAvailableMealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AvailableMealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvailableMealViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AvailableMealViewHolder(
        private val binding: ItemAvailableMealBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentMeal: Meal? = null

        init {
            binding.root.setOnClickListener {
                currentMeal?.let { meal ->
                    onMealClick(meal)
                }
            }
        }

        fun bind(meal: Meal) {
            currentMeal = meal

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeString = timeFormat.format(Date(meal.time))

            binding.textViewTime.text = timeString

            val mealTypeText = when (meal.type) {
                MealType.BREAKFAST -> "Petit-déjeuner"
                MealType.LUNCH -> "Déjeuner"
                MealType.DINNER -> "Dîner"
                MealType.SNACK -> "Collation"
            }
            binding.textViewType.text = mealTypeText

            if (meal.name.isNotEmpty()) {
                binding.textViewName.text = meal.name
            } else {
                binding.textViewName.text = mealTypeText
            }
        }
    }

    class MealDiffCallback : DiffUtil.ItemCallback<Meal>() {
        override fun areItemsTheSame(oldItem: Meal, newItem: Meal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Meal, newItem: Meal): Boolean {
            return oldItem == newItem
        }
    }
}