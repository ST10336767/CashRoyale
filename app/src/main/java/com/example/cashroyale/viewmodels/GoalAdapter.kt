// In your GoalAdapter.kt file

package com.example.cashroyale.viewmodels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // This was in your commented out code, but not used in the new class, so removing it.
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.Models.Category // Ensure this import is correct
import com.example.cashroyale.R
// Removed kotlinx.coroutines.flow.Flow import as we'll pass a List directly

class GoalAdapter(
    private var categories: List<Category>, // Change from Flow to List
    private var categorySpending: Map<String, Double> // New parameter for spending
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvGoalTitle)
        val tvTarget: TextView = itemView.findViewById(R.id.tvGoalTarget)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarGoal)
        // val rvGoals: RecyclerView = itemView.findViewById(R.id.rvGoals) // This view is for the fragment's RecyclerView, not individual item. Remove this.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val category = categories[position]
        holder.tvTitle.text = category.name // Use category.name for the title
        holder.tvTarget.text = "Limit: R%.2f".format(category.limit) // Use category.limit for target

        val currentSpent = categorySpending[category.name] ?: 0.0 // Get spending for this category

        // Calculate progress percentage, ensuring target is not zero to avoid division by zero
        val progressPercent = if (category.limit > 0) {
            ((currentSpent / category.limit) * 100).toInt().coerceIn(0, 100)
        } else {
            0 // If no limit, progress is 0 or handle as you see fit
        }
        holder.progressBar.progress = progressPercent

        // You can add logic here to change progress bar color or show an icon
        // if the limit is exceeded, similar to what you had in the commented code.
        // For example:
        if (currentSpent > category.limit) {
             holder.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.RED)
         } else {
             holder.progressBar.progressTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.GREEN) // Assuming colorPrimary is your default
         }
    }

    override fun getItemCount(): Int = categories.size // Use 'categories' list size

    // Method to update the adapter's data
    fun updateData(newCategories: List<Category>, newSpending: Map<String, Double>) {
        categories = newCategories
        categorySpending = newSpending
        notifyDataSetChanged() // Notify the RecyclerView that the data has changed
    }
}