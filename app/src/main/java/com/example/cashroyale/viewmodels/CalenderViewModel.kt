package com.example.cashroyale.viewmodels

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.cashroyale.Models.Category // IMPORT YOUR CATEGORY MODEL
import com.example.cashroyale.Models.MonthlyGoals
import com.example.cashroyale.Models.User // Ensure this User model aligns with your authentication needs
import com.example.cashroyale.Services.FireStore
import com.google.firebase.Timestamp // Keep if used elsewhere, but not directly for dates in this VM anymore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date // Only for internal Date object creation for formatting
import java.util.Locale

class CalenderViewModel(
    application: Application,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore, // Passed for completeness, but FireStore service uses it
    private val fireStore: FireStore // Your primary data source service
) : AndroidViewModel(application) {

    data class CategoryBudgetInfo(
        val categoryName: String,
        val spentAmount: Double,
        val limitAmount: Double
    )

    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> = _loggedInUser

    // Initialized to false, updated by Flow observer
    private val _monthlyGoalsSet = MutableLiveData<Boolean>(false)
    val monthlyGoalsSet: LiveData<Boolean> = _monthlyGoalsSet

    // This LiveData will be updated by the Flow observer
    private val _currentMonthlyGoals = MutableLiveData<MonthlyGoals?>()
    val currentMonthlyGoals: LiveData<MonthlyGoals?> = _currentMonthlyGoals

    // Mapped LiveData for UI directly from _currentMonthlyGoals
    val maxMonthlyBudget: LiveData<Double?> = _currentMonthlyGoals.map { it?.maxGoalAmount }
    val minMonthlyBudget: LiveData<Double?> = _currentMonthlyGoals.map { it?.minGoalAmount }

    private val _selectedDate = MutableLiveData<Long>(System.currentTimeMillis())
    val selectedDate: LiveData<Long> = _selectedDate

    // NEW: LiveData to hold all categories for the current user, including their limits
    private val _userCategories = MutableLiveData<List<Category>>()
    val userCategories: LiveData<List<Category>> = _userCategories // This will provide the limits

    val categorySpendingMap: LiveData<Map<String, Double>> = _selectedDate.switchMap { date ->
        liveData {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                emit(emptyMap())
                return@liveData
            }

            // Dates are now String format, matching FireStore.kt
            val startDateString = getStartOfMonth(date)
            val endDateString = getEndOfMonth(date)

            fireStore.getMonthlyExpensesFlow(currentUserId, startDateString, endDateString) // Pass Strings
                .collectLatest { monthlyExpenses ->
                    val spendingMap = mutableMapOf<String, Double>()
                    monthlyExpenses.forEach { expense ->
                        // Normalize category name (e.g., to lowercase) to match limits
                        val categoryName = expense.category.lowercase(Locale.getDefault())
                        spendingMap[categoryName] = (spendingMap[categoryName] ?: 0.0) + expense.amount
                    }
                    emit(spendingMap)
                    Log.d("CalenderViewModel", "Calculated category spending: $spendingMap")
                }
        }
    }

    // THIS IS THE CORRECT PLACEMENT AND DEFINITION FOR updateCombinedData
    // It's an extension function of MediatorLiveData and is defined directly within
    // the 'apply' block where it's used.
    private fun MediatorLiveData<List<CategoryBudgetInfo>>.updateCombinedData(spendingMap: Map<String, Double>?, categoryList: List<Category>?) {
        if (spendingMap == null && categoryList == null) {
            this.value = emptyList() // Correctly refers to the MediatorLiveData's value
            return
        }

        val resultList = mutableListOf<CategoryBudgetInfo>()
        val lowercasedCategoriesMap = categoryList?.associateBy { it.name.lowercase(Locale.getDefault()) } ?: emptyMap()

        val allCategoryNames = (spendingMap?.keys ?: emptySet()) + lowercasedCategoriesMap.keys

        allCategoryNames.forEach { categoryName ->
            val spent = spendingMap?.get(categoryName) ?: 0.0
            val limit = lowercasedCategoriesMap[categoryName]?.limit ?: 0.0

            val displayName = categoryName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            resultList.add(CategoryBudgetInfo(displayName, spent, limit))
        }
        this.value = resultList.sortedBy { it.categoryName }
        Log.d("CalenderViewModel", "Category budget overview updated: ${this.value}")
    }


    val categoryBudgetOverview: LiveData<List<CategoryBudgetInfo>> = MediatorLiveData<List<CategoryBudgetInfo>>().apply {
        var spending: Map<String, Double>? = null
        var categories: List<Category>? = null

        // Set an initial empty list value immediately when the LiveData is initialized
        this.value = emptyList() // This line is now correct and should resolve the "Unresolved reference"

        addSource(categorySpendingMap) { spentMap ->
            spending = spentMap
            updateCombinedData(spending, categories)
        }

        addSource(userCategories) { categoryList ->
            categories = categoryList
            updateCombinedData(spending, categories)
        }
        // The updateCombinedData helper function is defined just above, outside this apply block
        // but still within the ViewModel class.
    }


    // Total expenses for the current month, observed via LiveData and re-calculated when date or transactions change
    @RequiresApi(Build.VERSION_CODES.O)
    val totalExpenses: LiveData<Double> = _selectedDate.switchMap { date ->
        liveData {
            // These now return formatted Strings, not Longs or Timestamps
            val startDateString = getStartOfMonth(date)
            val endDateString = getEndOfMonth(date)

            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                emit(0.0)
                Log.e("CalenderViewModel", "Current user ID is null. Cannot fetch expenses.")
                return@liveData
            } else {
                Log.d("CalenderViewModel", "Calculating total expenses for user: $currentUserId")
                Log.d("CalenderViewModel", "Querying from Date String: $startDateString to Date String: $endDateString")
            }

            // Pass String dates to the FireStore service
            fireStore.getMonthlyExpensesFlow(currentUserId, startDateString, endDateString) //
                .collectLatest { monthlyExpenses ->
                    Log.d("CalenderViewModel", "Collected ${monthlyExpenses.size} expenses from Firestore.")
                    var sum = 0.0
                    monthlyExpenses.forEach { expense ->
                        sum += expense.amount
                        Log.d("CalenderViewModel", "Adding expense: ${expense.description}, Amount: ${expense.amount}, Date: ${expense.date}")
                    }
                    emit(sum)
                    Log.d("CalenderViewModel", "Emitted total expenses for month: $sum")
                }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    val remainingMaxBudget: LiveData<Double> = MediatorLiveData<Double>().apply {
        var maxBudget: Double? = null
        var totalSpent: Double? = null

        addSource(maxMonthlyBudget) { value ->
            maxBudget = value
            if (maxBudget != null && totalSpent != null) {
                this.value = maxBudget!! - totalSpent!!
            } else {
                this.value = 0.0 // Changed from null to 0.0 for initial state
            }
        }

        addSource(totalExpenses) { value ->
            totalSpent = value
            if (maxBudget != null && totalSpent != null) {
                this.value = maxBudget!! - totalSpent!!
            } else {
                this.value = 0.0 // Changed from null to 0.0
            }
        }
    }


    // Initialize block: Start observing goals and user status when ViewModel is created
    init {
        getLoggedInUserAndLoadGoals()

        // NEW: Start observing user categories (which now include limits)
        viewModelScope.launch {
            auth.currentUser?.uid?.let { userId ->
                fireStore.getUserCategoriesFlow(userId).collectLatest { categories ->
                    _userCategories.value = categories
                    Log.d("CalenderViewModel", "User categories Flow updated: ${categories.size} items: $categories")
                }
            }
        }
    }

    // Fetches logged-in user and sets up real-time observation for monthly goals
    private fun getLoggedInUserAndLoadGoals() {
        Log.d("CalenderViewModel", "getLoggedInUserAndLoadGoals() called")
        viewModelScope.launch {
            val currentUser = auth.currentUser
            Log.d(
                "CalenderViewModel",
                "getLoggedInUserAndLoadGoals() - currentUser UID: ${currentUser?.uid}"
            )

            if (currentUser != null) {
                // Set loggedInUser for UI display if needed.
                _loggedInUser.value = User(
                    email = currentUser.email ?: "",
                    password = ""
                ) // Adjust 'User' model as needed

                val userId = currentUser.uid
                // Start collecting from the real-time flow for monthly goals
                fireStore.getMonthlyGoalsFlow(userId).collectLatest { goals ->
                    _currentMonthlyGoals.value = goals // Update _currentMonthlyGoals LiveData
                    _monthlyGoalsSet.value =
                        goals != null && goals.goalSet // Update _monthlyGoalsSet
                    Log.d(
                        "CalenderViewModel",
                        "Goals Flow updated: $goals, goalsSet: ${_monthlyGoalsSet.value}"
                    )
                }
            } else {
                _loggedInUser.value = null
                _monthlyGoalsSet.value = false
                _currentMonthlyGoals.value = null
                Log.d(
                    "CalenderViewModel",
                    "getLoggedInUserAndLoadGoals() - No logged-in user or user is null"
                )
            }
        }
    }

    // Function to save monthly goals, called from UI (e.g., dialog)
    fun saveMonthlyGoals(maxGoal: Double, minGoal: Double) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val newMonthlyGoal = MonthlyGoals(
                    userId = userId, // Assign the current user's ID
                    maxGoalAmount = maxGoal,
                    minGoalAmount = minGoal,
                    goalSet = true
                )
                try {
                    fireStore.saveMonthlyGoal(newMonthlyGoal)
                    Log.d(
                        "CalenderViewModel",
                        "Monthly goals save initiated via ViewModel. Flow will update UI."
                    )
                    // UI will automatically update because the Flow in getLoggedInUserAndLoadGoals()
                    // detects the change in Firestore and updates _currentMonthlyGoals.
                } catch (e: Exception) {
                    Log.e("CalenderViewModel", "Error saving goals via ViewModel: ${e.message}", e)
                    // You might want to expose this error to the UI (e.g., via a separate LiveData for error messages)
                }
            } else {
                Log.e("CalenderViewModel", "Cannot save goals: User not logged in.")
                // Similarly, expose error to UI
            }
        }
    }

    // MODIFIED FUNCTION TO SAVE/UPDATE CATEGORY LIMITS
    fun saveCategoryLimit(categoryName: String, limitAmount: Double) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                // Find the existing category, or create a new one to update its limit
                val existingCategory = _userCategories.value?.find {
                    // Compare category names case-insensitively
                    it.name.lowercase(Locale.getDefault()) == categoryName.lowercase(Locale.getDefault())
                }

                val categoryToSave = if (existingCategory != null) {
                    // If category exists, copy it and update its 'limit' field
                    existingCategory.copy(limit = limitAmount)
                } else {
                    // If category does not exist, create a new Category object
                    // Assuming you have a default categoryImage or can handle it later
                    Category(
                        userId = userId,
                        name = categoryName,
                        limit = limitAmount
                    )
                }

                try {
                    fireStore.saveOrUpdateCategory(categoryToSave)
                    Log.d("CalenderViewModel", "Category '${categoryName}' limit saved/updated to $limitAmount")
                } catch (e: Exception) {
                    Log.e("CalenderViewModel", "Error saving/updating category limit for '${categoryName}': ${e.message}", e)
                    // Consider exposing this error to the UI
                }
            } else {
                Log.e("CalenderViewModel", "Cannot save category limit: User not logged in.")
            }
        }
    }


    // Date utility functions - MODIFIED TO RETURN STRINGS
    private fun getStartOfMonth(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        // Format to "YYYY-MM-DD" to match your Firestore string date format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun getEndOfMonth(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        // Format to "YYYY-MM-DD" to match your Firestore string date format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}