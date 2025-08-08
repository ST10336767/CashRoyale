package com.example.cashroyale.viewmodels

import androidx.appcompat.app.AppCompatActivity

class ViewExpenses : AppCompatActivity() {
// private lateinit var edtSelectDate: EditText
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var appDatabase: AppDatabase
//    private lateinit var expensesAdapter: ExpensesAdapter
//    private lateinit var categorySpinner: Spinner
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_expenses_list)
//
//        // Get references to views
//        edtSelectDate = findViewById(R.id.edtSelectDate)
//        recyclerView = findViewById(R.id.recyclerView)
//        categorySpinner = findViewById(R.id.spinner)
//        val returnButton: Button = findViewById(R.id.button2)
//
//        // Initialize the database
//        appDatabase = AppDatabase.getDatabase(applicationContext)
//
//        // Set up the RecyclerView
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        expensesAdapter = ExpensesAdapter(listOf())
//        recyclerView.adapter = expensesAdapter
//
//        // Load all expenses initially
//        loadExpenses()
//
//        // Set up the date picker
//        edtSelectDate.setOnClickListener {
//            showDatePicker()
//        }
//
//        // Set up return button
//        returnButton.setOnClickListener {
//            finish()
//        }
//
//        // Load categories into the Spinner
//       // loadCategoriesIntoSpinner()
//
//        // Set up category selection listener
//        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
//                val selectedCategory = parent?.getItemAtPosition(position).toString()
//                if (selectedCategory == "All") {
//                    loadExpenses() // Show all expenses if "All" is selected
//                } else {
//                  //  loadExpensesByCategory(selectedCategory) // Load expenses for selected category
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                loadExpenses() // Default to showing all if nothing selected
//            }
//        }
//    }
//
////    private fun loadCategoriesIntoSpinner() {
////        lifecycleScope.launch(Dispatchers.IO) {
////            val categoriesFlow = appDatabase.categoryDAO().getAllCategories()
////            val categories = categoriesFlow.first()
////            val categoryNames = mutableListOf("All") // Add "All" as the first item
////            categoryNames.addAll(categories.map { it.name })
////
////            launch(Dispatchers.Main) {
////                val adapter = ArrayAdapter(
////                    this@ViewExpenses,
////                    android.R.layout.simple_spinner_item,
////                    categoryNames
////                )
////                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
////                categorySpinner.adapter = adapter // Set the adapter to the Spinner
////            }
////        }
////    }
//
//    // Show a date picker dialog when the user clicks on the date field
//    private fun showDatePicker() {
//        val calendar = Calendar.getInstance()
//        val year = calendar.get(Calendar.YEAR)
//        val month = calendar.get(Calendar.MONTH)
//        val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
//            val selectedDate = "${selectedYear}-${(selectedMonth + 1).toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
//            edtSelectDate.setText(selectedDate)
//            loadExpenses(selectedDate) // Load expenses for the selected date
//        }, year, month, day)
//
//        datePicker.show() // Display the date picker dialog
//    }
//
//    // Load expenses from the database (either all or filtered by selected date)
//    private fun loadExpenses(date: String? = null) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val expensesList: List<Expense> = if (date != null) {
//                appDatabase.expenseDAO().getExpensesByDate(date)
//                    .let { flow -> flow.first() }
//            } else {
//                appDatabase.expenseDAO().getAllExpensesOnce()
//            }
//
//            launch(Dispatchers.Main) {
//                expensesAdapter.updateExpenses(expensesList)
//            }
//        }
//    }
//
//    // Load expenses based on selected category
////    private fun loadExpensesByCategory(category: String) {
////        lifecycleScope.launch(Dispatchers.IO) {
////            val filteredExpenses = appDatabase.categoryDAO().getExpensesByCategory(category).first()
////
////            launch(Dispatchers.Main) {
////                expensesAdapter.updateExpenses(filteredExpenses)
////            }
////        }
////    }

}
