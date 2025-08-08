package com.example.cashroyale.Models


data class Transactions(

    var id: String = "",
    var userId: String = "", // Crucial for filtering by user
    var description: String = "",
    var amount: Double = 0.0,
    var date: String = "", // "YYYY-MM-DD" format
    var paymentMethod: String = "",
    var category: String = "",
    var imageUri: String? = null,
    var type: String = "" // "income" or "expense"
)