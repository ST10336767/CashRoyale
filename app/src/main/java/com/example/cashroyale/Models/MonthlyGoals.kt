package com.example.cashroyale.Models

// Remove ALL Room-related imports if you are using Firebase Firestore for this model.
// Example of imports to REMOVE:
// import androidx.room.Entity
// import androidx.room.ForeignKey
// import androidx.room.Index
// import androidx.room.PrimaryKey

import android.os.Parcelable // Keep if you use Parcelable for Intent/Bundle passing
import kotlinx.parcelize.Parcelize // Keep if you use Parcelable for Intent/Bundle passing

/**
 * Represents the monthly financial goals set by a user.
 * This model is designed for use with Firebase Firestore.
 */
@Parcelize // Keep this annotation if you intend to pass MonthlyGoals objects via Bundles or Intents
data class MonthlyGoals(
    val userId: String = "", // Used as the Firestore document ID for a user's goals
    val maxGoalAmount: Double = 0.0,
    val minGoalAmount: Double = 0.0,
    val goalSet: Boolean = false // Flag indicating if goals have been actively set
) : Parcelable {
    // No-argument constructor is required for Firebase Firestore deserialization
    constructor() : this("", 0.0, 0.0, false)
}