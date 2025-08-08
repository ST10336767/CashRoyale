package com.example.cashroyale.viewmodels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.Models.Transactions
import com.example.cashroyale.R

// Adapter class to show a list of transactions in a RecyclerView
class TransactionsAdapter(private var transactionsList: List<Transactions>) :
    RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    // Holds the views for one transaction item
    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.tvDescription)
        val amountTextView: TextView = itemView.findViewById(R.id.tvAmount)
        val dateTextView: TextView = itemView.findViewById(R.id.tvDate)
        val typeTextView: TextView = itemView.findViewById(R.id.tvType)
        val categoryTextView: TextView = itemView.findViewById(R.id.tvCategory)
    }

    // Creates new ViewHolder objects when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)  // Load the item layout
        return TransactionViewHolder(view)
    }

    // Connects the data to the views in the ViewHolder
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionsList[position]  // Get the current transaction

        // Set text for each view
        holder.descriptionTextView.text = transaction.description
        holder.amountTextView.text = "R${transaction.amount}"
        holder.dateTextView.text = transaction.date
        holder.typeTextView.text = transaction.type
        holder.categoryTextView.text = transaction.category

        // Change amount text color based on type (green for income, red for expense)
        val colorRes = if (transaction.type.lowercase() == "income") {
            R.color.green
        } else {
            R.color.red
        }
        holder.amountTextView.setTextColor(holder.itemView.context.getColor(colorRes))
    }

    // Returns how many transactions are in the list
    override fun getItemCount(): Int = transactionsList.size

    // Updates the list with new data and refreshes the RecyclerView
    fun updateData(newTransactions: List<Transactions>) {
        transactionsList = newTransactions
        notifyDataSetChanged()
    }
}
