package com.example.cashroyale.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user of the application.
 * The [Entity] annotation specifies that this class maps to a database table named "User".
 */
@Entity(tableName = "User")
data class User(
    /**
     * The primary key for the User entity, which is the user's email address.
     * The [PrimaryKey] annotation marks this field as the primary key of the table.
     */
    @PrimaryKey val email: String,

    /** The user's password (should be stored securely, e.g., hashed). */
    val password: String
)