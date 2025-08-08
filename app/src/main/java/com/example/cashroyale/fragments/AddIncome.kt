package com.example.cashroyale.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashroyale.MainActivity
import com.example.cashroyale.Models.Income
import com.example.cashroyale.R
import com.example.cashroyale.Services.FireStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Calendar

class AddIncome : AppCompatActivity() {
    private lateinit var Description: EditText
    private lateinit var Amount: EditText
    private lateinit var PaymentMethod: Spinner
    private lateinit var Date: EditText
    private lateinit var PickImage: Button
    private lateinit var iPreview: ImageView
    private lateinit var Save: Button
    private lateinit var btnCalendar: Button

    private lateinit var fireStore: FireStore
    private var selectedImageUri: Uri? = null

    private val paymentMethods = listOf("Cash", "Credit Card")
    private val IMAGE_PICK_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        fireStore = FireStore(FirebaseFirestore.getInstance())

        Description = findViewById(R.id.edtDescription)
        Amount = findViewById(R.id.edtAmount)
        PaymentMethod = findViewById(R.id.spinPayment)
        Date = findViewById(R.id.edtDate)
        PickImage = findViewById(R.id.btnPickImage)
        iPreview = findViewById(R.id.imageView)
        Save = findViewById(R.id.btnSave)
        btnCalendar = findViewById(R.id.btnCalendar)

        setupPaymentMethodSpinner()
        setupDatePicker()
        setupImagePicker()
        setupSaveButton()
        setupCalendarRedirect()
    }

    private fun setupPaymentMethodSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentMethods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        PaymentMethod.adapter = adapter
    }

    private fun setupDatePicker() {
        Date.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedYear}-${(selectedMonth + 1).toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
                Date.setText(selectedDate)
            }, year, month, day)
            datePicker.show()
        }
    }

    private fun setupImagePicker() {
        PickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            iPreview.setImageURI(selectedImageUri)
        }
    }

    private fun setupSaveButton() {
        Save.setOnClickListener {
            val description = Description.text.toString()
            val amountText = Amount.text.toString()
            val date = Date.text.toString()
            val paymentMethod = PaymentMethod.selectedItem?.toString()

            if (description.isBlank() || amountText.isBlank() || date.isBlank() || paymentMethod.isNullOrBlank()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId.isNullOrEmpty()) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val income = Income(
                id = "",
                userId = userId,
                description = description,
                amount = amount,
                date = date,
                paymentMethod = paymentMethod,
                category = "",
                imageUri = selectedImageUri?.toString()
            )

            lifecycleScope.launch {
                try {
                    fireStore.saveIncome(income)
                    runOnUiThread {
                        Toast.makeText(this@AddIncome, "Income saved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@AddIncome, "Failed to save income: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupCalendarRedirect() {
        btnCalendar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("navigateTo", "calendar")
            }
            startActivity(intent)
            finish()
        }
    }
}
