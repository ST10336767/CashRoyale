package com.example.cashroyale.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.Models.Category
import com.example.cashroyale.R
import com.example.cashroyale.Services.FireStore
import com.example.cashroyale.databinding.FragmentCategoryListBinding
import com.example.cashroyale.viewmodels.CategoryAdapter
import com.example.cashroyale.viewmodels.CategoryListViewModel
import com.example.cashroyale.viewmodels.CategoryViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CategoryListFragment : Fragment() {
    // Correct binding for CategoryListFragment
    private var _binding: FragmentCategoryListBinding? = null // Using nullable backing property for ViewBinding
    private val binding get() = _binding!! // Non-null accessor

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var fireStoreService: FireStore

    // ViewModel initialization with the correct factory
    private val viewModel: CategoryListViewModel by viewModels {
        val application = requireActivity().application
        // Initialize fireStoreService BEFORE passing it to the factory
        fireStoreService = FireStore(db)
        CategoryViewModelFactory( auth, db, fireStoreService, application)
    }

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    // private var goToCalendarButton: View? = null // Dont need it

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the correct layout using ViewBinding
        _binding = FragmentCategoryListBinding.inflate(inflater, container, false)
        val view = binding.root

        categoryRecyclerView = binding.categoryRecyclerView // Access via binding
        categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = CategoryAdapter(emptyList(), this::onEditCategory, this::onDeleteCategory)
        categoryRecyclerView.adapter = adapter

        // Observes the LiveData of all categories from the ViewModel
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            adapter.updateList(categories) // Updates the RecyclerView adapter with the new list of categories
        }

        // Sets an OnClickListener for the button to navigate to the CalenderFragment
        binding.goToCalendarButton.setOnClickListener { // Access via binding
            findNavController().navigate(R.id.calenderFragment)
        }

        return view
    }

    /** Handles the edit action for a category. */
    private fun onEditCategory(category: Category) {
        // Creates and shows the EditCategoryFragment dialog for the selected category
        val editDialogFragment = EditCategoryFragment.newInstance(category)
        editDialogFragment.show(childFragmentManager, "editCategoryDialog")
    }

    /** Handles the delete action for a category. */
    private fun onDeleteCategory(category: Category) {
        // Calls the ViewModel function to delete the selected category
        viewModel.deleteCategory(category)
        // The UI will be updated automatically through the LiveData observation
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding when the view is destroyed
    }
}