package com.example.cashroyale

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Activity for user registration. Allows new users to create an account.
 * Implements password complexity checks and email validation.
 */
class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth

        val returnToLoginButton = findViewById<Button>(R.id.returnLoginButton)
        val register = findViewById<Button>(R.id.confirmRegButton)
        val emailEditText = findViewById<EditText>(R.id.regUsernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.regPasswordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.regConfirmPasswordEditText)

        returnToLoginButton.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        register.setOnClickListener {
            Log.d(TAG,"reg button clicked")
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (password.isEmpty() && email.isEmpty() && confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 8) {
                Toast.makeText(
                    this,
                    "Password must be at least 8 characters long",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            register.isEnabled = false
            Log.d(TAG,"before create")
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "before if")
                    if (task.isSuccessful) {
                        Log.d(TAG, "after if")
                        // Registration successful
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Login::class.java))
                        finish()
                    } else {
                        Log.d(TAG, "after else")
                        // Registration failed
                        Toast.makeText(
                            this, "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        register.isEnabled = true
                    }
                }
        }
    }
}