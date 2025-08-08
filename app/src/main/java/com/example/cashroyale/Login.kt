package com.example.cashroyale

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cashroyale.DAO.UserDAO
import com.example.cashroyale.Models.AppDatabase
import com.example.cashroyale.Services.AuthService
import com.example.cashroyale.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation

/**
 * Activity for user login. Allows existing users to log in and provides a link to the registration screen.
 */
class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val authService = AuthService.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        db = Firebase.firestore

        // Handle edge-to-edge screen display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val login = findViewById<Button>(R.id.loginButton)
        val register = findViewById<Button>(R.id.registerButton)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        login.setOnClickListener{
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = authService.signInWithEmailAndPassword(email, password)
                    result.onSuccess{
                        Toast.makeText(this@Login, "Login successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Login, MainActivity::class.java))
                        finish()
                    }.onFailure { exception ->
                        Toast.makeText(this@Login, "Login failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                        login.isEnabled = true
                    }
                } catch (e: Exception) {
                        Toast.makeText(this@Login, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        login.isEnabled = true
                    }

            }
        }

        // Set click listener for the register button to navigate to the registration screen
           register.setOnClickListener(){
            intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish() // Prevent the user from going back to the login screen on back press
        }
    }
}