package com.example.cashroyale.Models

import android.os.Parcelable
// REMOVE ALL ROOM IMPORTS
// import androidx.room.Entity
// import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents a category for expenses or income, designed for Firebase Firestore.
 * Implements [Parcelable] to allow passing instances between components (e.g., to EditCategoryFragment).
 */
@Parcelize
data class Category(
    // Firestore document ID.
    var id: String = "",
    // The Firebase Authentication user ID that this category belongs to.
    val userId: String = "",
    // The name of the category (e.g., "Food", "Salary")
    val name: String = "",
    // The limit associated with the category (e.g., budget for "Food"). Use 0.0 as default.
    val limit: Double = 0.0,
) : Parcelable {
    // No-argument constructor required for Firebase Firestore automatic deserialization.
    // It should initialize all properties with their default values.
    constructor() : this("", "", "", 0.0)
}