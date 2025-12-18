package com.example.sorty

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sorty.databinding.ActivityCreateAccountBinding
import com.example.sorty.ui.home.Home
import java.text.SimpleDateFormat
import java.util.*

class CreateAccount : AppCompatActivity() {

    private lateinit var bind: ActivityCreateAccountBinding
    private val calendar = Calendar.getInstance()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        bind = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(bind.root)

        supportActionBar?.hide()
        setStatusBarIconsLight(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bind.backbtn.setOnClickListener { finish() }
        bind.editBday.setOnClickListener { showDatePickerDialog() }

        // --- CONTINUE BUTTON LOGIC ---
        bind.buttonContinue.setOnClickListener {
            val first = bind.editFirstName.text.toString().trim()
            val last = bind.editLastName.text.toString().trim() // Optional
            val bday = bind.editBday.text.toString().trim()
            val email = bind.editEmail.text.toString().trim()
            val school = bind.editSchool.text.toString().trim()
            val course = bind.editCourse.text.toString().trim()

            // B. Validate: Removed 'last' from requirement
            if (first.isEmpty() || bday.isEmpty() || email.isEmpty() ||
                school.isEmpty() || course.isEmpty()) {
                Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show Confirmation Modal before proceeding
            showConfirmationDialog(first, last, bday, email, school, course)
        }
    }

    private fun showConfirmationDialog(first: String, last: String, bday: String, email: String, school: String, course: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_save, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()

        // Make background transparent so the CardView corners show
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Update Dialog Text
        dialogView.findViewById<TextView>(R.id.tv_title).text = "Confirm Details"
        dialogView.findViewById<TextView>(R.id.tv_message).text =
            "Please ensure your email ($email) and details are correct before proceeding."
        dialogView.findViewById<TextView>(R.id.btn_confirm).text = "Confirm"

        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            dialog.dismiss()

            // Navigate to CreatePassword
            val intent = Intent(this, CreatePassword::class.java).apply {
                putExtra("EXTRA_FIRST", first)
                putExtra("EXTRA_LAST", last)
                putExtra("EXTRA_BDAY", bday)
                putExtra("EXTRA_EMAIL", email)
                putExtra("EXTRA_SCHOOL", school)
                putExtra("EXTRA_COURSE", course)
            }
            startActivity(intent)
        }

        dialog.show()
    }

    // ... Helper functions for DatePicker and Status Bar remain the same ...
    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }
        val datePicker = DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun updateLabel() {
        val dateFormat = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.US)
        bind.editBday.setText(simpleDateFormat.format(calendar.time))
    }

    private fun setStatusBarIconsLight(window: Window, isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = window.insetsController ?: return
            insetsController.setSystemBarsAppearance(
                if (isLight) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            var flags = decorView.systemUiVisibility
            flags = if (isLight) flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            decorView.systemUiVisibility = flags
        }
    }
}