package com.example.cashroyale.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels // For activity-scoped ViewModel
import androidx.fragment.app.viewModels
import com.example.cashroyale.Models.Category
import com.example.cashroyale.R
import com.example.cashroyale.Services.FireStore
import com.example.cashroyale.viewmodels.CategoryListViewModel
import com.example.cashroyale.viewmodels.CategoryViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditCategoryFragment : DialogFragment() {

    // These should be initialized once for the fragment lifecycle

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fireStoreService: FireStore // Renamed to avoid confusion with the object instance

    private var category: Category? = null // The category being edited
    private var limitEditText: EditText? = null
    private var editCategoryNameEditText: EditText? = null
    private var editTypeSpinner: Spinner? = null

    // ViewModel should be initialized lazily with the correct factory
    private val viewModel: CategoryListViewModel by viewModels {
        // Initialize dependencies here BEFORE the ViewModel is created
        val application = requireActivity().application
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fireStoreService = FireStore(db) // Initialize FireStore service

        // Pass all required parameters to your CategoryListViewModelFactory
        CategoryViewModelFactory( auth, db, fireStoreService, application)
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        /** Creates a new instance of EditCategoryFragment with the category to edit. */
        fun newInstance(category: Category): EditCategoryFragment {
            val fragment = EditCategoryFragment()
            val args = Bundle()
            args.putParcelable(ARG_CATEGORY, category) // Passes the category as a Parcelable
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the category from the arguments
        arguments?.let {
            category = it.getParcelable(ARG_CATEGORY)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.fragment_edit_category, null) // Ensure this is your layout for editing categories
        builder.setView(view)

        // Initialize UI elements
        editCategoryNameEditText = view.findViewById(R.id.editCategoryNameEditText)
        limitEditText = view.findViewById(R.id.editLimitEditText)
        val cancelButton = view.findViewById<Button>(R.id.editCancelButton)
        val saveButton = view.findViewById<Button>(R.id.editSaveButton)

        // Define available category types
        val types = arrayOf("income", "expense")
        // Set up the adapter for the type spinner
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)
        editTypeSpinner?.adapter = typeAdapter

        // Populate the fields with the existing category data
        category?.let { existingCategory ->
            editCategoryNameEditText?.setText(existingCategory.name)
            limitEditText?.setText(existingCategory.limit.toString()) // Set existing limit
        } ?: run {
            // If for some reason category is null (shouldn't happen with newInstance)
            Toast.makeText(requireContext(), "Error: Category not found for editing.", Toast.LENGTH_LONG).show()
            dismiss()
        }


        // Set the OnClickListener for the save button
        saveButton.setOnClickListener {
            val updatedName = editCategoryNameEditText?.text.toString().trim()
            val selectedType = editTypeSpinner?.selectedItem.toString()
            val limit = limitEditText?.text.toString().toDoubleOrNull() // Use toDoubleOrNull for safety

            // Check if name is not blank, category exists, and limit is valid
            if (updatedName.isNotBlank() && category != null && limit != null) {
                val updatedCategory = category!!.copy(
                    name = updatedName,
                    limit = limit,
                    id = category!!.id,
                    userId = category!!.userId
                )
                // Update the category using the ViewModel (best practice)
                viewModel.updateCategory(updatedCategory)
                dismiss() // Dismiss the dialog after initiating update
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a category name and a valid limit.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Set the OnClickListener for the cancel button
        cancelButton.setOnClickListener {
            dismiss() // Dismiss the dialog
        }

        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear references to views to prevent memory leaks
        limitEditText = null
        editCategoryNameEditText = null
        editTypeSpinner = null
    }
}