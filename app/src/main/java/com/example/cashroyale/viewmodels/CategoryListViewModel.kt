package com.example.cashroyale.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cashroyale.Models.Category
import com.example.cashroyale.Services.FireStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest // We'll still use collectLatest within a launch block
import kotlinx.coroutines.launch

/**
 * ViewModel for the CategoryListFragment, responsible for managing and providing category data.
 * It interacts with the FireStore service for Firebase operations.
 */
class CategoryListViewModel(
    application: Application,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore, // Passed for completeness, but FireStore service uses it
    private val fireStore: FireStore // Your primary data source service
) : AndroidViewModel(application) {

    private val _currentUserId = MutableLiveData<String?>()
    val currentUserId: LiveData<String?> get() = _currentUserId

    // MutableLiveData to hold the actual lists of categories
    private val _allCategories = MutableLiveData<List<Category>>()
    val allCategories: LiveData<List<Category>> get() = _allCategories

    private val _incomeCategories = MutableLiveData<List<Category>>()
    val incomeCategories: LiveData<List<Category>> get() = _incomeCategories

    private val _expenseCategories = MutableLiveData<List<Category>>()
    val expenseCategories: LiveData<List<Category>> get() = _expenseCategories

    // Job references to cancel previous collection when user changes
    private var allCategoriesCollectionJob: Job? = null
    private var incomeCategoriesCollectionJob: Job? = null
    private var expenseCategoriesCollectionJob: Job? = null

    init {
        // Observe the current user's authentication state to get their UID
        auth.addAuthStateListener { firebaseAuth ->
            val newUserId = firebaseAuth.currentUser?.uid
            if (_currentUserId.value != newUserId) { // Only update if user ID actually changed
                _currentUserId.value = newUserId
                Log.d("CategoryListViewModel", "Auth state changed. New User ID: $newUserId")
                // When user ID changes, re-fetch all category types
                fetchCategoriesForCurrentUser(newUserId)
            }
        }
    }

    // This function will fetch and collect categories based on the current user ID
    private fun fetchCategoriesForCurrentUser(userId: String?) {
        // Cancel any ongoing collection jobs to avoid stale data/updates from old user
        allCategoriesCollectionJob?.cancel()
        incomeCategoriesCollectionJob?.cancel()
        expenseCategoriesCollectionJob?.cancel()

        if (userId != null) {
            allCategoriesCollectionJob = viewModelScope.launch {
                fireStore.getAllCategoriesFlow(userId).collectLatest { categories ->
                    _allCategories.postValue(categories)
                    Log.d("CategoryListViewModel", "All categories updated for $userId: ${categories.size}")
                }
            }

            incomeCategoriesCollectionJob = viewModelScope.launch {
                fireStore.getCategoriesByTypeFlow(userId, "income").collectLatest { categories ->
                    _incomeCategories.postValue(categories)
                    Log.d("CategoryListViewModel", "Income categories updated for $userId: ${categories.size}")
                }
            }

            expenseCategoriesCollectionJob = viewModelScope.launch {
                fireStore.getCategoriesByTypeFlow(userId, "expense").collectLatest { categories ->
                    _expenseCategories.postValue(categories)
                    Log.d("CategoryListViewModel", "Expense categories updated for $userId: ${categories.size}")
                }
            }
        } else {
            // If no user, clear all lists
            _allCategories.postValue(emptyList())
            _incomeCategories.postValue(emptyList())
            _expenseCategories.postValue(emptyList())
            Log.d("CategoryListViewModel", "User logged out, cleared category lists.")
        }
    }

    /**
     * Deletes a specific category from Firebase Firestore.
     * Category object must have its 'id' field populated (which is the Firestore document ID).
     */
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                fireStore.deleteCategory(category.id)
                Log.d("CategoryListViewModel", "Category deleted: ${category.id}")
            } catch (e: Exception) {
                Log.e("CategoryListViewModel", "Error deleting category: ${e.message}", e)
                // You might want to expose this error to the UI
            }
        }
    }

    /**
     * Updates an existing category in Firebase Firestore.
     * Category object must have its 'id' field populated (which is the Firestore document ID).
     */
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                fireStore.updateCategory(category)
                Log.d("CategoryListViewModel", "Category updated: ${category.id}")
            } catch (e: Exception) {
                Log.e("CategoryListViewModel", "Error updating category: ${e.message}", e)
                // You might want to expose this error to the UI
            }
        }
    }

    /**
     * Retrieves a specific category from Firebase Firestore based on its ID for the current user.
     * This now returns a single LiveData for the specific ID, not a continuous flow.
     */
    fun getCategoryById(categoryId: String): LiveData<Category?> {
        val result = MutableLiveData<Category?>()
        val userId = auth.currentUser?.uid // Get current user ID immediately
        if (userId != null) {
            viewModelScope.launch {
                try {
                    // Assuming FireStore has a suspend function for single fetch
                    val category = fireStore.getCategoryById(categoryId, userId)
                    result.postValue(category)
                } catch (e: Exception) {
                    Log.e("CategoryListViewModel", "Error fetching category by ID: ${e.message}", e)
                    result.postValue(null)
                }
            }
        } else {
            result.postValue(null)
        }
        return result
    }

    // No need for separate getIncomeCategories() and getExpenseCategories() functions
    // if you always expose the _incomeCategories and _expenseCategories LiveData.
    // If you specifically need functions returning LiveData, they would just return the existing mutable LiveData.
    // e.g., fun getIncomeCategoriesLiveData(): LiveData<List<Category>> = _incomeCategories
}