package com.example.cashroyale.viewmodels

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.R
import com.example.cashroyale.Models.Category

/**
 * Adapter for displaying a list of Category items in a RecyclerView.
 * Provides functionality for displaying category name and color, as well as edit and delete actions.
 */
class CategoryAdapter(
    private var categories: List<Category>,
    private val onEditClicked: (Category) -> Unit,
    private val onDeleteClicked: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    /**
     * ViewHolder class for representing each category item in the RecyclerView.
     * Holds references to the views within the item layout.
     */
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        val limitView: TextView = itemView.findViewById(R.id.categoryLimitTextView)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * Inflates the item layout and creates a new CategoryViewHolder.
     * @return A new CategoryViewHolder that holds a View of the layout for each item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(itemView)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Updates the contents of the ViewHolder to reflect the item at the given position.
     */
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentCategory = categories[position]
        holder.nameTextView.text = currentCategory.name
        holder.limitView.text = "Limit: R${String.format("%.2f",currentCategory.limit)}"

        holder.editButton.setOnClickListener {
            onEditClicked(currentCategory)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClicked(currentCategory)
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The size of the categories list.
     */
    override fun getItemCount() = categories.size

    /**
     * Updates the list of categories displayed by the adapter and refreshes the RecyclerView.
     */
    fun updateList(newList: List<Category>) {
        categories = newList
        notifyDataSetChanged() // Informs the RecyclerView that the underlying data has changed
    }
}