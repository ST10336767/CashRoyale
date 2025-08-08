package com.example.cashroyale.repositories

import com.example.cashroyale.Models.MonthlyGoals
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.tasks.await

class MonthlyGoalsRepository (private val monthlyGoalsCollection: CollectionReference){
    suspend fun getMonthlyGoals(userId: String): MonthlyGoals? {
        return monthlyGoalsCollection.document(userId).get().await()?.toObject(MonthlyGoals::class.java)
    }

    suspend fun saveMonthlyGoal(monthlyGoal: MonthlyGoals): Void? {
        return monthlyGoalsCollection.document(monthlyGoal.userId).set(monthlyGoal).await()
    }
}