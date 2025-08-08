package com.example.cashroyale.Models

data class Expense(
    var id: String = "",
    val userId: String = "",
    /** A brief description of the expense (e.g., "Grocery shopping", "Dinner with friends"). */
    val description: String = "", // Add default value
    /** The monetary amount of the expense. */
    val amount: Double = 0.0, // Add default value
    /** The date when the expense occurred (stored as a String for simplicity). */
    val date: String = "", // Add default value
    /** The method used for payment (e.g., "Cash", "Credit Card"). */
    val paymentMethod: String = "", // Add default value
    /** The category to which the expense belongs (e.g., "Food", "Entertainment"). */
    val category: String = "", // Add default value
    /**
     * The URI of an image associated with the expense (e.g., a receipt).
     * Can be null if no image is attached.
     */
    val imageUri: String? = null
){
    // Required for Firebase
    constructor() : this("", "", "", 0.0, "", "", "", null)
}