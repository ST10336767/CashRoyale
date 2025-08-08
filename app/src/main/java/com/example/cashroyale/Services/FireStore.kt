package com.example.cashroyale.Services

import android.util.Log
import com.example.cashroyale.Models.Category
import com.example.cashroyale.Models.Expense
import com.example.cashroyale.Models.Income
import com.example.cashroyale.Models.MonthlyGoals
import com.example.cashroyale.Models.Transactions
import com.example.cashroyale.Models.Transactions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query // Explicit import for Query.Direction
import java.util.Locale

class FireStore(private val db: FirebaseFirestore) {

    // --- Monthly Goals Functions ---
    suspend fun getMonthlyGoals(userId: String): MonthlyGoals? {
        return try {
            db.collection("monthlyGoals")
                .document(userId)
                .get()
                .await()
                .toObject(MonthlyGoals::class.java)
        } catch (e: Exception) {
            Log.e("FireStore", "Error retrieving monthly goals for user $userId: ${e.message}", e)
            null
        }
    }


    suspend fun getMonthlyGoalByUserId(userId: String): MonthlyGoals? {
        return try {
            val querySnapshot = db.collection("monthlyGoals")
                .document(userId)
                .document(userId)
                .get()
                .await()
            if (querySnapshot.exists()) {
                querySnapshot.toObject(MonthlyGoals::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FireStore", "Error getting monthly goal for user $userId: ${e.message}", e)
            null
        }
    }

    fun getMonthlyGoalsFlow(userId: String): Flow<MonthlyGoals?> = callbackFlow {
        val docRef = db.collection("monthlyGoals").document(userId)

        val subscription = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("FireStore", "Listen failed for monthly goals for user $userId: ${e.message}", e)
                close(e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val goals = snapshot.toObject(MonthlyGoals::class.java)
                trySend(goals).isSuccess
            } else {
                trySend(null).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun saveMonthlyGoal(monthlyGoal: MonthlyGoals) {
        try {
            db.collection("monthlyGoals").document(monthlyGoal.userId)
                .set(monthlyGoal, SetOptions.merge())
                .await()
            Log.d("FireStore", "Monthly goal saved successfully for user: ${monthlyGoal.userId}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving monthly goal: ${e.message}", e)
            throw e
        }
    }

    // --- Transactions Functions ---

    suspend fun saveTransaction(transaction: Transactions) {
        try {
            val docRef = if (transaction.id.isEmpty()) {
                db.collection("transactions").document()
                db.collection("transactions").document()
            } else {
                db.collection("transactions").document(transaction.id)
                db.collection("transactions").document(transaction.id)
            }
            // Correctly set the ID in the object before saving
            val transactionToSave = transaction.copy(id = docRef.id) // Create a copy with the generated ID
            docRef.set(transactionToSave).await()
            Log.d("FireStore", "Transaction saved successfully: ${docRef.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving transaction: ${e.message}", e)
            throw e
            throw e
        }
    }

    fun getAllTransactions(userId: String): Flow<List<Transactions>> = callbackFlow {
        val transactionsCollection = db.collection("transactions")

        val listenerRegistration = transactionsCollection
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FireStore", "Listen failed for transactions: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val transactions = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Transactions::class.java)?.apply {
                                this.id = doc.id
                            }
                        } catch (ex: Exception) {
                            Log.e("FireStore", "Error converting document ${doc.id} to Transactions: ${ex.message}", ex)
                            null
                        }
                    }
                    trySend(transactions).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    // --- Category Functions (Corrected and New) ---

    fun getAllCategoriesFlow(userId: String): Flow<List<Category>> = callbackFlow {
        val categoriesCollection = db.collection("categories")

        val listenerRegistration = categoriesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FireStore", "Listen failed for all categories for user $userId: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Category::class.java)?.apply {
                                this.id = doc.id
                            }
                        } catch (ex: Exception) {
                            Log.e("FireStore", "Error converting document ${doc.id} to Category: ${ex.message}", ex)
                            null
                        }
                    }
                    trySend(categories).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getCategoriesByTypeFlow(userId: String, type: String): Flow<List<Category>> = callbackFlow {
        val categoriesCollection = db.collection("categories")

        val listenerRegistration = categoriesCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", type)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FireStore", "Listen failed for categories by type ($type) for user $userId: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Category::class.java)?.apply {
                                this.id = doc.id
                            }
                        } catch (ex: Exception) {
                            Log.e("FireStore", "Error converting document ${doc.id} to Category: ${ex.message}", ex)
                            null
                        }
                    }
                    trySend(categories).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getAllCategoriesForUserFlow(userId: String): Flow<List<Category>> = callbackFlow {
        val categoriesCollection = db.collection("categories")

        val listenerRegistration = categoriesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FireStore", "Listen failed for all categories for user $userId: ${e.message}", e)
                    close(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Category::class.java)?.apply {
                                this.id = doc.id
                            }
                        } catch (ex: Exception) {
                            Log.e("FireStore", "Error converting document ${doc.id} to Category: ${ex.message}", ex)
                            null
                        }
                    }
                    trySend(categories).isSuccess
                } else {
                    trySend(emptyList()).isSuccess
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    // This getCategoryById is problematic because it uses "users" collection directly,
    // while other category functions use "categories" collection.
    // Ensure consistency or verify your Firestore structure.
    suspend fun getCategoryById(categoryId: String, userId: String): Category? =
        withContext(Dispatchers.IO) {
            try {
                // Assuming categories are directly in a top-level "categories" collection
                // and filtered by userId, NOT as a subcollection under "users".
                // If they are under users, then the path should be:
                // db.collection("users").document(userId).collection("categories").document(categoryId)
                val documentSnapshot = db.collection("categories")
                    .document(categoryId)
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    val category = documentSnapshot.toObject(Category::class.java)
                    // Double-check userId if needed
                    if (category != null && category.userId == userId) {
                        category.id = documentSnapshot.id // Ensure ID is set
                        category
                    } else {
                        Log.w("FireStore", "Category $categoryId found but userId mismatch or null object.")
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("FireStore", "Error fetching category by ID: $categoryId for user $userId - ${e.message}", e)
                null
            }
        }


    suspend fun updateCategory(category: Category) {
        try {
            db.collection("categories")
                .document(category.id)
                .set(category, SetOptions.merge())
                .await()
            Log.d("FireStore", "Category updated successfully: ${category.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error updating category ${category.id}: ${e.message}", e)
            throw e
        }
    }

    suspend fun doesCategoryExist(categoryName: String, userId: String): Boolean {
        return try {
            val querySnapshot = db.collection("categories")
                .whereEqualTo("name", categoryName)
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            Log.e("FireStore", "Error checking if category exists for user $userId, name $categoryName: ${e.message}", e)
            false
        }
    }


    suspend fun saveCategory(category: Category) {
        try {
            val docRef = if (category.id.isEmpty()) {
                db.collection("categories").document() // Generate new ID
            } else {
                db.collection("categories").document(category.id) // Use provided ID
            }

            // Create a copy of the category object with the Firestore-generated document ID
            // and then save this modified copy.
            val categoryToSave = category.copy(id = docRef.id) // <-- CRITICAL CHANGE HERE
            docRef.set(categoryToSave).await() // Save the object with the ID populated

            Log.d("FireStore", "Category saved/added with ID: ${docRef.id} for user ${category.userId}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving category: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteCategory(categoryId: String) {
        try {
            db.collection("categories").document(categoryId).delete().await()
            Log.d("FireStore", "Category deleted: $categoryId")
        } catch (e: Exception) {
            Log.e("FireStore", "Error deleting category $categoryId: ${e.message}", e)
            throw e
        }
    }

    // --- Income ---

    suspend fun saveIncome(income: Income) {
        try {
            val docRef = if (income.id.isEmpty()) {
                db.collection("income").document()
            } else {
                db.collection("income").document(income.id)
            }

            docRef.set(income.copy(id = docRef.id)).await()
            Log.d("FireStore", "Income saved with ID: ${docRef.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving income: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateIncome(income: Income) {
        try {
            db.collection("income").document(income.id)
                .set(income, SetOptions.merge()).await()
            Log.d("FireStore", "Income updated: ${income.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error updating income: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteIncome(incomeId: String) {
        try {
            db.collection("income").document(incomeId).delete().await()
            Log.d("FireStore", "Income deleted: $incomeId")
        } catch (e: Exception) {
            Log.e("FireStore", "Error deleting income: ${e.message}", e)
            throw e
        }
    }

    fun getAllIncomeFlow(userId: String): Flow<List<Income>> = callbackFlow {
        val listenerRegistration = db.collection("income")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val incomeList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Income::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(incomeList).isSuccess
                Log.d("FireStore", "All income fetched: ${incomeList.size} documents.")
            }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun getIncomeById(incomeId: String): Income? {
        return try {
            val doc = db.collection("income").document(incomeId).get().await()
            if (doc.exists()) {
                doc.toObject(Income::class.java)?.apply { id = doc.id }
            } else null
        } catch (e: Exception) {
            Log.e("FireStore", "Error getting income: ${e.message}", e)
            null
        }
    }

    // --- Expense specific functions ---

    suspend fun saveExpense(expense: Expense) {
        try {
            val docRef = if (expense.id.isEmpty()) {
                db.collection("expenses").document()
            } else {
                db.collection("expenses").document(expense.id)
            }

            docRef.set(expense.copy(id = docRef.id)).await()
            Log.d("FireStore", "Expense saved with ID: ${docRef.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error saving expense: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateExpense(expense: Expense) {
        try {
            db.collection("expenses").document(expense.id)
                .set(expense, SetOptions.merge()).await()
            Log.d("FireStore", "Expense updated: ${expense.id}")
        } catch (e: Exception) {
            Log.e("FireStore", "Error updating expense: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteExpense(expenseId: String) {
        try {
            db.collection("expenses").document(expenseId).delete().await()
            Log.d("FireStore", "Expense deleted: $expenseId")
        } catch (e: Exception) {
            Log.e("FireStore", "Error deleting expense: ${e.message}", e)
            throw e
        }
    }

    fun getAllExpensesFlow(userId: String): Flow<List<Expense>> = callbackFlow {
        val listenerRegistration = db.collection("expenses")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val expenseList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Expense::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(expenseList).isSuccess
                Log.d("FireStore", "All expenses fetched: ${expenseList.size} documents.")
            }

        awaitClose { listenerRegistration.remove() }
    }

    suspend fun getExpensesBetweenDates(userId: String, startDate: String, endDate: String): List<Expense> {
        return try {
            val snapshot = db.collection("expenses")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Expense::class.java)?.apply { id = doc.id }
            }
        } catch (e: Exception) {
            Log.e("FireStore", "Error getting expenses between dates: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getExpenseById(expenseId: String): Expense? {
        return try {
            val doc = db.collection("expenses").document(expenseId).get().await()
            if (doc.exists()) {
                doc.toObject(Expense::class.java)?.apply { id = doc.id }
            } else null
        } catch (e: Exception) {
            Log.e("FireStore", "Error getting expense: ${e.message}", e)
            null
        }
    }
}