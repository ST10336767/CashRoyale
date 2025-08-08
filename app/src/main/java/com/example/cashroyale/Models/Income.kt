package com.example.cashroyale.Models

/**
 * Represents an income record in the application.
 * This version is adapted for use with Firebase Firestore instead of Room.
 */
data class Income(
    /** Firestore document ID — must be set manually or retrieved after saving. */
    var id: String = "",

    /** The ID of the user to whom this income belongs — used for filtering. */
    val userId: String = "",

    /** A brief description of the income source (e.g., "Salary", "Freelance work"). */
    val description: String = "",

    /** The monetary amount of the income. */
    val amount: Double = 0.0,

    /** The date when the income was received (stored as a String for simplicity). */
    val date: String = "",

    /** The method through which the income was received (e.g., "Bank transfer", "Cash"). */
    val paymentMethod: String = "",

    /** The category to which the income belongs (e.g., "Salary", "Investments"). */
    val category: String = "",

    /**
     * The URI of an image associated with the income (e.g., a deposit slip).
     * Can be null if no image is attached.
     */
    val imageUri: String? = null
)
