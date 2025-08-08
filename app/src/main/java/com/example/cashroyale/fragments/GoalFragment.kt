package com.example.cashroyale.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.R
import com.example.cashroyale.Services.FireStore
import com.example.cashroyale.Models.Category
import com.example.cashroyale.Models.Expense
import com.example.cashroyale.viewmodels.GoalAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GoalFragment : Fragment() {

    private lateinit var tvGoalStatus: TextView
    private lateinit var progressBarGoalOverview: ProgressBar
    private lateinit var rvGoals: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FireStore
    private lateinit var goalAdapter: GoalAdapter // Declare adapter here

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_goal, container, false)
        tvGoalStatus = view.findViewById(R.id.tvGoalStatus)
        progressBarGoalOverview = view.findViewById(R.id.progressBarGoalOverview)
        rvGoals = view.findViewById(R.id.rvGoals)
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FireStore(db)

        val userId = auth.currentUser?.uid ?: return

        // Initialize RecyclerView and adapter
        rvGoals.layoutManager = LinearLayoutManager(context)
        goalAdapter = GoalAdapter(emptyList(), emptyMap()) // Initialize with empty data
        rvGoals.adapter = goalAdapter

        fetchGoalAndExpenses(userId) // Fetch overall goal and expenses

        // Collect categories and expenses to update the individual goal list
        collectCategorySpending(userId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchGoalAndExpenses(userId: String) {
        db.collection("monthlyGoals").document(userId).get()
            .addOnSuccessListener { goalDoc ->
                if (goalDoc != null && goalDoc.getBoolean("goalSet") == true) {
                    val minGoal = goalDoc.getDouble("minGoalAmount") ?: 0.0
                    val maxGoal = goalDoc.getDouble("maxGoalAmount") ?: 0.0

                    val currentMonth = LocalDate.now().monthValue.toString().padStart(2, '0')
                    val currentYear = LocalDate.now().year.toString()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                    db.collection("expenses")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { expenseSnap ->
                            var totalSpent = 0.0

                            for (doc in expenseSnap) {
                                val dateStr = doc.getString("date") ?: continue
                                val date = LocalDate.parse(dateStr, formatter)
                                if (date.monthValue == currentMonth.toInt() && date.year == currentYear.toInt()) {
                                    totalSpent += doc.getDouble("amount") ?: 0.0
                                }
                            }
                            updateUI(totalSpent, minGoal, maxGoal)
                        }
                } else {
                    tvGoalStatus.text = "No monthly spending goal set."
                    progressBarGoalOverview.progress = 0
                }
            }
            .addOnFailureListener { e ->
                tvGoalStatus.text = "Failed to fetch monthly spending goal: ${e.message}"
                progressBarGoalOverview.progress = 0
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun collectCategorySpending(userId: String) {
        val currentMonth = LocalDate.now().monthValue
        val currentYear = LocalDate.now().year
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // Combine the categories flow with the expenses flow
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    firestore.getAllCategoriesFlow(userId),
                    firestore.getAllExpensesFlow(userId)
                ) { categories, expenses ->
                    // Calculate spending for each category for the current month
                    val categorySpendingMap = expenses
                        .filter { expense ->
                            try {
                                val expenseDate = LocalDate.parse(expense.date, formatter)
                                expenseDate.monthValue == currentMonth && expenseDate.year == currentYear
                            } catch (e: Exception) {
                                false // Handle parsing errors
                            }
                        }
                        .groupBy { it.category }
                        .mapValues { (_, expenseList) -> expenseList.sumOf { it.amount } }

                    // Sort categories by name for consistent display
                    val sortedCategories = categories.sortedBy { it.name }

                    Pair(sortedCategories, categorySpendingMap)
                }.collectLatest { (categories, categorySpendingMap) ->
                    // Update the adapter with the new data
                    goalAdapter.updateData(categories, categorySpendingMap)
                }
            }
        }
    }

    private fun updateUI(spent: Double, min: Double, max: Double) {
        val statusText: String
        val statusColor: Int

        when {
            spent < min -> {
                statusText = "Below Minimum Spending.\nYou can spend more!"
                statusColor = Color.parseColor("#388E3C") // Green 700
            }
            spent in min..max -> {
                statusText = "You're within your goal! Keep it up!"
                statusColor = Color.parseColor("#F57C00") // Orange 700
            }
            else -> {
                statusText = "You've exceeded your spending goal! Time to slow down!"
                statusColor = Color.parseColor("#D32F2F") // Red 700
            }
        }

        val spentText = "This Month's Spending:\nR%.2f\n\n".format(spent)
        val fullText = spentText + statusText

        val spannable = SpannableString(fullText)

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            "This Month's Spending:".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            "This Month's Spending:\n".length,
            spentText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(statusColor),
            spentText.length,
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvGoalStatus.text = spannable
        tvGoalStatus.setPadding(32, 32, 32, 32)
        tvGoalStatus.textSize = 18f

        val progress = if (max > 0) ((spent / max) * 100).toInt().coerceIn(0, 100) else 0
        progressBarGoalOverview.progress = progress
        progressBarGoalOverview.progressTintList = android.content.res.ColorStateList.valueOf(statusColor)
    }
}