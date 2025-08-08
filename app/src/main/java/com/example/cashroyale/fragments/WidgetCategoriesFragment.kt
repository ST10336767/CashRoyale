package com.example.cashroyale.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
// import com.example.cashroyale.Models.AppDatabase // REMOVE THIS IMPORT - NO LONGER USED WITH FIRESTORE
import com.example.cashroyale.Models.Category
import com.example.cashroyale.R
import com.example.cashroyale.Services.FireStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A DialogFragment for adding new categories.
 */
class WidgetCategoriesFragment : DialogFragment() {
    private var categoryNameEditText: EditText? = null
    private var limitEditText: EditText? = null
    private var transactionSpinner: Spinner? = null
    private var manageCategoriesButton: Button? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fireStore: FireStore


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.fragment_widget_categories, null)
        builder.setView(view)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fireStore = FireStore(db) // Initialize FireStore service with the db instance


        // Initialize UI elements
        categoryNameEditText = view.findViewById(R.id.categoryNameEditText)
        limitEditText = view.findViewById(R.id.limitEditText)
        val okButton = view.findViewById<Button>(R.id.widgetOkButton)
        val cancelButton = view.findViewById<Button>(R.id.widgetCancelButton)
        manageCategoriesButton = view.findViewById(R.id.manageCategoriesButton)


        // Populate the transaction type spinner
        val transactionTypes = arrayOf("income", "expense")
        val transactionAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, transactionTypes)
        transactionSpinner?.adapter = transactionAdapter

        // Set OnClickListener for the OK button to save the new category
        okButton.setOnClickListener {
            val categoryName = categoryNameEditText?.text.toString().trim()
            val limit = limitEditText?.text.toString().toDoubleOrNull() // Use toDoubleOrNull for safety
            val userId = auth.currentUser?.uid // Get current user's UID

            // Input validation checks
            if (userId.isNullOrEmpty()) {
                Log.e(TAG, "User not authenticated or not logged in. Cannot save category.")
                Toast.makeText(context, "User not logged in. Please log in.", Toast.LENGTH_LONG).show()
                return@setOnClickListener // Stop execution if user is not logged in
            }

            if (categoryName.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (limit == null) {
                Toast.makeText(requireContext(), "Please enter a valid limit (number)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Correctly check if a category with the same name exists for the CURRENT USER
                    val categoryExists = fireStore.doesCategoryExist(categoryName, userId)

                    if (!categoryExists) {
                        val category = Category(
                            id = "", // Let Firestore generate ID for new category
                            userId = userId, // Assign the current user's ID
                            name = categoryName,
                            limit = limit,
                        )
                        fireStore.saveCategory(category) // Save the new category to Firestore
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Category added successfully!", Toast.LENGTH_SHORT).show()
                            dismiss() // Close the dialog
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Category name already exists for you.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving category: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error adding category: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Set OnClickListener for the Cancel button to dismiss the dialog
        cancelButton.setOnClickListener {
            dismiss()
        }

        // Set OnClickListener for the Manage Categories button to navigate to the CategoryListFragment
        manageCategoriesButton?.setOnClickListener {
            dismiss() // Close the current dialog
            // Ensure this navigation action is defined in your NavGraph from calenderFragment
            findNavController().navigate(R.id.action_calenderFragment_to_categoryListFragment)
        }

        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear references to views to prevent memory leaks
        categoryNameEditText = null
        limitEditText = null
        transactionSpinner = null
        manageCategoriesButton = null
        // Do NOT nullify auth, db, fireStore here if they are lateinit,
        // as they are singletons or scoped to the app/activity.
    }
}