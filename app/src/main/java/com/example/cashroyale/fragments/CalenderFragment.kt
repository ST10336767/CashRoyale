package com.example.cashroyale.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.Models.Transactions
import com.example.cashroyale.R
import com.example.cashroyale.Services.AuthService
import com.example.cashroyale.Services.EmailService
import com.example.cashroyale.Services.FireStore
import com.example.cashroyale.databinding.FragmentCalenderBinding
import com.example.cashroyale.viewmodels.CalenderViewModel
import com.example.cashroyale.viewmodels.CalenderViewModelFactory
import com.example.cashroyale.viewmodels.TransactionsAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class CalenderFragment : Fragment() {

    // View binding for easy access to views in the layout
    private var _binding: FragmentCalenderBinding? = null
    private val binding get() = _binding!!

    // Firebase Authentication instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Firestore database instance
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Service to send emails
    private val emailService = EmailService()

    // Firestore wrapper service
    private lateinit var fireStoreService: FireStore

    // AuthService singleton instance
    private val authService = AuthService.getInstance()

    // ViewModel for calendar data and logic
    private val viewModel: CalenderViewModel by viewModels {
        val application = requireActivity().application
        fireStoreService = FireStore(db)  // Initialize Firestore wrapper
        CalenderViewModelFactory(auth, db, fireStoreService, application)  // Provide dependencies
    }

    // RecyclerView to display transactions
    private lateinit var recyclerView: RecyclerView

    // Adapter for transactions RecyclerView
    private lateinit var adapter: TransactionsAdapter

    // Lists to store income and expense transactions separately
    private val incomeList = mutableListOf<Transactions>()
    private val expenseList = mutableListOf<Transactions>()

    companion object {
        // Factory method to create new instance of this fragment
        fun newInstance() = CalenderFragment()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the fragment layout with view binding
        _binding = FragmentCalenderBinding.inflate(inflater, container, false)
        val view = binding.root

        // Setup RecyclerView with vertical linear layout
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the adapter with an empty list initially
        adapter = TransactionsAdapter(emptyList())
        recyclerView.adapter = adapter

        // Setup button click listeners to navigate to adding income or expense screens
        binding.btnExpenses.setOnClickListener {
            startActivity(Intent(requireContext(), AddExpense::class.java))
        }

        binding.btnIncome.setOnClickListener {
            startActivity(Intent(requireContext(), AddIncome::class.java))
        }

        // Button to send budget report via email
        binding.btnSendReport.setOnClickListener {
            sendReport()
        }

        // Button to show category creation dialog
        binding.createCategoryImageButton.setOnClickListener {
            showWidgetDialogFragment()
        }

        // Start observing LiveData from ViewModel to update UI automatically
        observeViewModel()

        // Get the current user's ID
        val userId = auth.currentUser?.uid

        // If user not logged in, show error and return early
        if (userId.isNullOrEmpty()) {
            Log.e(TAG, "User not authenticated or not logged in. Cannot proceed.")
            Toast.makeText(context, "User not logged in. Please log in.", Toast.LENGTH_LONG).show()
            return view
        }

        // Check if monthly budget goals exist for this user in Firestore
        db.collection("monthlyGoals").whereEqualTo("userId", userId).get().addOnCompleteListener { task ->
            // If no goals found, prompt user to set goals
            if (task.isSuccessful && task.result.isEmpty) {
                showGoalInputDialog(userId)
            }
        }

        // Start real-time listening for income and expense transactions
        setupRealtimeListeners(userId)

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendReport() {
        // Get overall budget info from ViewModel
        val max = viewModel.maxMonthlyBudget.value
        val spent = viewModel.totalExpenses.value
        val remaining = viewModel.remainingMaxBudget.value

        var report = "Monthly Budget Overview:\n\n" +
                "Your overall monthly budget is R ${String.format("%.2f", max ?: 0.0)}\n" +
                "You spent a total of R ${String.format("%.2f", spent ?: 0.0)} this month\n" +
                "Your remaining overall budget is R ${String.format("%.2f", remaining ?: 0.0)}\n\n"

        // Add info about reaching minimum monthly goal
        if (max != null && spent != null && remaining != null) {
            val minGoal = viewModel.minMonthlyBudget.value ?: 0.0
            if (spent >= minGoal) { // Reaching min goal means spending AT LEAST minGoal
                report += "✅ You have reached your minimum monthly goal of R ${String.format("%.2f", minGoal)}\n\n"
            } else {
                report += "❌ You have not yet reached your minimum monthly goal of R ${String.format("%.2f", minGoal)}\n\n"
            }
        }
    }

    // Observe LiveData in ViewModel and update UI when data changes
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeViewModel() {
        viewModel.maxMonthlyBudget.observe(viewLifecycleOwner) { maxBudget ->
            binding.numMaxBudgetTextView.text = maxBudget?.let { "R ${String.format("%.2f", it)}" } ?: "R N/A"
        }

        viewModel.minMonthlyBudget.observe(viewLifecycleOwner) { minBudget ->
            binding.numMinBudgetTextView.text = minBudget?.let { "R ${String.format("%.2f", it)}" } ?: "R N/A"
        }

        viewModel.totalExpenses.observe(viewLifecycleOwner) { totalSpent ->
            binding.numAmountSpentTextView.text = "R ${String.format("%.2f", totalSpent ?: 0.0)}"
        }

        // In observeViewModel()
        viewModel.remainingMaxBudget.observe(viewLifecycleOwner) { remainingBudget ->
            binding.numRemainingBudgetTextView.text = remainingBudget?.let { "R ${String.format("%.2f", it)}" } ?: "R N/A"
        }
    }

    // Setup Firestore real-time listeners for income and expenses for the current user
    private fun setupRealtimeListeners(userId: String) {
        db.collection("income")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { incomeSnapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching income data: ${error.message}", error)
                    return@addSnapshotListener
                }
                incomeList.clear()
                incomeSnapshot?.documents?.forEach { doc ->
                    val income = doc.toObject(Transactions::class.java)
                    income?.type = "income"  // Mark type as income
                    income?.let { incomeList.add(it) }
                }
                mergeAndDisplayTransactions()
            }

        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { expenseSnapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching expense data: ${error.message}", error)
                    return@addSnapshotListener
                }
                expenseList.clear()
                expenseSnapshot?.documents?.forEach { doc ->
                    val expense = doc.toObject(Transactions::class.java)
                    expense?.type = "expense"  // Mark type as expense
                    expense?.let { expenseList.add(it) }
                }
                mergeAndDisplayTransactions()
            }
    }

    // Combine income and expenses, sort by date descending, then update adapter
    private fun mergeAndDisplayTransactions() {
        val mergedList = incomeList + expenseList
        val sortedList = mergedList.sortedByDescending { it.date }
        adapter.updateData(sortedList)
    }

    override fun onResume() {
        super.onResume()
        // When returning to this fragment, refresh listeners if user is logged in
        val userId = auth.currentUser?.uid
        if (!userId.isNullOrEmpty()) {
            setupRealtimeListeners(userId)
        }
    }

    // Show the dialog fragment to create categories (widget)
    private fun showWidgetDialogFragment() {
        val widgetDialogFragment = WidgetCategoriesFragment()
        widgetDialogFragment.show(childFragmentManager, "WidgetDialogFragment")
    }

    // Show dialog to let user input monthly budget goals if none exist
    private fun showGoalInputDialog(userId: String) {
        // Inflate custom layout for setting goals
        val inputView = LayoutInflater.from(requireContext()).inflate(R.layout.set_goals, null)

        // Find the input fields for max and min goals
        val maxGoalEditText = inputView.findViewById<EditText>(R.id.editTextMaxGoal)
        val minGoalEditText = inputView.findViewById<EditText>(R.id.editTextMinGoal)

        // Create dialog with the custom view and title
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Your Monthly Goals")
            .setView(inputView)
            .setCancelable(false)
            .create()

        // When "Set Goals" button is clicked inside the dialog
        inputView.findViewById<Button>(R.id.btnSetGoals).setOnClickListener {
            val maxGoalStr = maxGoalEditText.text.toString().trim()
            val minGoalStr = minGoalEditText.text.toString().trim()

            if (maxGoalStr.isNotEmpty() && minGoalStr.isNotEmpty()) {
                val maxGoal = maxGoalStr.toDoubleOrNull()
                val minGoal = minGoalStr.toDoubleOrNull()

                // Check if goals are valid and min is less or equal max
                if (maxGoal != null && minGoal != null && minGoal <= maxGoal) {
                    lifecycleScope.launch {
                        try {
                            // Save goals through ViewModel
                            viewModel.saveMonthlyGoals(maxGoal, minGoal)
                            dialog.dismiss()
                            Toast.makeText(requireContext(), "Goals saved successfully!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error saving goals from dialog: ${e.message}", e)
                            Toast.makeText(requireContext(), "Error saving goals: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Invalid goal values. Min goal must be less than or equal to Max goal.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill both max and min goals.", Toast.LENGTH_LONG).show()
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks by clearing binding reference
    }
}
