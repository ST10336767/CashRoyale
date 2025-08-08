package com.example.cashroyale.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cashroyale.Models.User

@Dao
interface UserDAO {
    /** Inserts a new user. */
    @Insert
    fun insertUser(user: User): Long

    /** Gets a user by their email address. */
    @Query("SELECT * FROM User WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
}