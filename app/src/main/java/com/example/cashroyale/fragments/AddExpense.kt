package com.example.cashroyale.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
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
import com.example.cashroyale.Models.Expense
import com.example.cashroyale.R
import com.example.cashroyale.Services.FireStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class AddExpense : AppCompatActivity() {

    private lateinit var descriptionField: EditText
    private lateinit var amountField: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var paymentSpinner: Spinner
    private lateinit var dateField: EditText
    private lateinit var pickImageBtn: Button
    private lateinit var imagePreview: ImageView
    private lateinit var saveBtn: Button
    private lateinit var btnCalendar: Button

    private lateinit var firestore: FireStore
    private var selectedImageUri: Uri? = null
    private var capturedImageBitmap: Bitmap? = null

    private val paymentMethods = listOf("Cash", "Credit Card")
    private val IMAGE_PICK_CODE = 1001
    private val IMAGE_CAPTURE_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        firestore = FireStore(FirebaseFirestore.getInstance())

        descriptionField = findViewById(R.id.edtDescription)
        amountField = findViewById(R.id.edtAmount)
        categorySpinner = findViewById(R.id.spinCategory)
        paymentSpinner = findViewById(R.id.spinPayment)
        dateField = findViewById(R.id.edtDate)
        pickImageBtn = findViewById(R.id.btnPickImage)
        imagePreview = findViewById(R.id.imageView)
        saveBtn = findViewById(R.id.btnSave)
        btnCalendar = findViewById(R.id.btnCalendar)

        setupDatePicker()
        setupPaymentSpinner()
        setupImagePicker()
        setupCategorySpinner()
        setupSaveButton()
        setupCalendarRedirect()
    }

    private fun setupPaymentSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentMethods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paymentSpinner.adapter = adapter
    }

    private fun setupCategorySpinner() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        lifecycleScope.launch {
            firestore.getAllCategoriesForUserFlow(userId).first().let { categories ->
                val names = categories.map { it.name }
                val adapter = ArrayAdapter(this@AddExpense, android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }
        }
    }

    private fun setupDatePicker() {
        dateField.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedDate = "$year-${(month + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                    dateField.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    private fun setupImagePicker() {
        pickImageBtn.setOnClickListener {
            showImagePickerDialog()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Choose from Gallery", "Take a Photo")
        AlertDialog.Builder(this)
            .setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> captureImageFromCamera()
                }
            }
            .show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun captureImageFromCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_CODE -> {
                    selectedImageUri = data?.data
                    capturedImageBitmap = null
                    imagePreview.setImageURI(selectedImageUri)
                }
                IMAGE_CAPTURE_CODE -> {
                    val photoBitmap = data?.extras?.get("data") as? Bitmap
                    if (photoBitmap != null) {
                        capturedImageBitmap = photoBitmap
                        selectedImageUri = null
                        imagePreview.setImageBitmap(photoBitmap)
                    }
                }
            }
        }
    }

    private fun setupSaveButton() {
        saveBtn.setOnClickListener {
            val description = descriptionField.text.toString()
            val amountText = amountField.text.toString()
            val date = dateField.text.toString()
            val paymentMethod = paymentSpinner.selectedItem.toString()
            val category = categorySpinner.selectedItem.toString()

            if (description.isBlank() || amountText.isBlank() || date.isBlank() || category.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            val expense = Expense(
                id = "",
                userId = userId,
                description = description,
                amount = amount,
                date = date,
                paymentMethod = paymentMethod,
                category = category,
                imageUri = selectedImageUri?.toString()
            )

            lifecycleScope.launch {
                firestore.saveExpense(expense)
                Toast.makeText(this@AddExpense, "Expense saved", Toast.LENGTH_SHORT).show()
                finish()
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
