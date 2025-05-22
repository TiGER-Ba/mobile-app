package com.example.mealplanner.ui.mealplan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mealplanner.data.model.Meal
import com.example.mealplanner.data.model.MealType
import com.example.mealplanner.databinding.ItemMealBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MealAdapter(
    private val onMealClick: (Meal) -> Unit
) : ListAdapter<Meal, MealAdapter.MealViewHolder>(MealDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val binding = ItemMealBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MealViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MealViewHolder(
        private val binding: ItemMealBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition // CORRIGÉ ICI
                if (position != RecyclerView.NO_POSITION) {
                    onMealClick(getItem(position))
                }
            }
        }

        fun bind(meal: Meal) {
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
                binding.textViewName.visibility = View.VISIBLE
            } else {
                binding.textViewName.visibility = View.GONE
            }

            if (meal.notes.isNotEmpty()) {
                binding.textViewNotes.text = meal.notes
                binding.textViewNotes.visibility = View.VISIBLE
            } else {
                binding.textViewNotes.visibility = View.GONE
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