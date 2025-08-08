package com.example.cashroyale.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cashroyale.R
import com.example.cashroyale.Services.FireStore
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class StatisticsFragment : Fragment() {
    private lateinit var edtStart: EditText
    private lateinit var edtEnd: EditText
    private lateinit var btnGenerate: Button
    private lateinit var chart: BarChart
    private lateinit var legendLayout: LinearLayout

    private val firestore by lazy { FireStore(FirebaseFirestore.getInstance()) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_statistics, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        edtStart = view.findViewById(R.id.edtStartDate)
        edtEnd = view.findViewById(R.id.edtEndDate)
        btnGenerate = view.findViewById(R.id.btnGenerate)
        chart = view.findViewById(R.id.BarChart)
        legendLayout = view.findViewById(R.id.legendLayout)

        edtStart.setOnClickListener { pickDate(edtStart) }
        edtEnd.setOnClickListener { pickDate(edtEnd) }

        btnGenerate.setOnClickListener {
            val startText = edtStart.text.toString()
            val endText = edtEnd.text.toString()

            if (startText.isBlank() || endText.isBlank()) {
                Toast.makeText(context, "Please pick both dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val startDate = formatter.parse(startText)!!
            val endDate = formatter.parse(endText)!!
            val uid = auth.currentUser?.uid ?: return@setOnClickListener

            lifecycleScope.launch {
                val categories = firestore.getAllCategoriesFlow(uid).first()
                val expenses = firestore.getAllExpensesFlow(uid).first()

                val filtered = expenses.filter {
                    val date = formatter.parse(it.date)
                    date != null && !date.before(startDate) && !date.after(endDate)
                }

                displayBarChart(filtered, categories.map { it.name })
            }
        }
    }

    private fun pickDate(target: EditText) {
        val cal = Calendar.getInstance()
        val picker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
                target.setText(formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        picker.show()
    }

    private fun displayBarChart(
        expenses: List<com.example.cashroyale.Models.Expense>,
        categoryNames: List<String>
    ) {
        val totalsByCategory = expenses.groupBy { it.category }
            .mapValues { it.value.sumOf { expense -> expense.amount } }
            .filterValues { it > 0 }

        val categoryLabels = totalsByCategory.keys.toList()

        val entries = categoryLabels.mapIndexed { index, category ->
            BarEntry(index.toFloat(), totalsByCategory[category]!!.toFloat())
        }

        val barDataSet = BarDataSet(entries, "Expenses by Category").apply {
            valueTextSize = 14f
            setDrawValues(true)
            valueFormatter = object : ValueFormatter() {
                override fun getBarLabel(barEntry: BarEntry?): String {
                    return String.format("%.2f", barEntry?.y ?: 0f)
                }
            }

            colors = listOf(
                resources.getColor(R.color.green),
                resources.getColor(R.color.orange),
                resources.getColor(R.color.red),
                resources.getColor(R.color.blue),
                resources.getColor(R.color.rokuPurple)
            )
        }

        val barData = BarData(barDataSet)

        chart.apply {
            data = barData
            xAxis.apply {
                setDrawLabels(false)
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }

            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            description = Description().apply { text = "" }
            legend.isEnabled = false
            animateY(800)
            invalidate()
        }

        //legend to help user understand which bar belongs to which category
        legendLayout.removeAllViews()
        categoryLabels.forEachIndexed { index, category ->
            val legendItem = TextView(requireContext()).apply {
                text = category
                setTextColor(barDataSet.colors[index % barDataSet.colors.size])
                textSize = 16f
                setPadding(8, 4, 8, 4)
            }
            legendLayout.addView(legendItem)
        }
    }
}