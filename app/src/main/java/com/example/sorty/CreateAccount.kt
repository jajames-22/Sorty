package com.example.sorty

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
// Import the correct binding class
import com.example.sorty.databinding.ActivityCreateAccountBinding
// --- DATE PICKER IMPORTS ---
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

class CreateAccount : AppCompatActivity() {
    // Declare the correct binding class
    private lateinit var bind: ActivityCreateAccountBinding

    // --- DATE PICKER PROPERTY ---
    // Member variable to keep track of the selected date (initializes to today)
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate the correct binding class
        bind = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(bind.root)
        setStatusBarIconsLight(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()

        val backbtn = bind.backbtn
        val continuebtn =  bind.buttonContinue

        backbtn.setOnClickListener {
            finish()
        }

        continuebtn.setOnClickListener {

            val first = bind.editFirstName.text.toString()
            val last = bind.editLastName.text.toString()
            val bday = bind.editBday.text.toString()
            val email = bind.editEmail.text.toString()
            val school = bind.editSchool.text.toString()
            val course = bind.editCourse.text.toString()

            //  Validate inputs
            if (first.isEmpty() || last.isEmpty() || bday.isEmpty() ||
                email.isEmpty() || school.isEmpty() || course.isEmpty()) {

                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //  Insert into SQLite
            val db = DatabaseHelper(this)
            val success = db.insertUser(first, last, bday, email, school, course)

            if (success) {
                //  Show success message
                Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()

                //  Go to Home and remove CreateAccount from back stack
                val intent = Intent(this, Home::class.java)
                // Option A: just finish CreateAccount
                startActivity(intent)
                finish()

                // --- OR Option B: completely clear stack ---
                // val intent = Intent(this, Home::class.java)
                // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                // startActivity(intent)

            } else {
                Toast.makeText(this, "Failed to save user!", Toast.LENGTH_SHORT).show()
            }
        }


        // --- DATE PICKER INTEGRATION START ---
        // Set the click listener on the Date of Birth EditText (bind.dob)
        bind.editBday.setOnClickListener {
            showDatePickerDialog()
        }
        // --- DATE PICKER INTEGRATION END ---
    }

    /**
     * Shows the DatePickerDialog and handles date selection.
     */
    private fun showDatePickerDialog() {

        // Define the callback function for when a user selects a date
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->

            // Update the local Calendar instance with the chosen date
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // Update the EditText field with the formatted date
            updateLabel()
        }

        // Create and show the DatePickerDialog, initializing it with the current date
        val datePicker = DatePickerDialog(
            this,
            dateSetListener,

            // Initial year, month, and day to display in the dialog
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set the maximum date to today so users cannot pick a future date of birth
        datePicker.datePicker.maxDate = System.currentTimeMillis()

        datePicker.show()
    }

    /**
     * Formats the selected date and sets it as the text in the EditText (bind.dob).
     */
    private fun updateLabel() {
        // Use a consistent and readable date format
        val dateFormat = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.US)

        // Apply the format to the calendar time and set the text
        bind.editBday.setText(simpleDateFormat.format(calendar.time))
    }

    private fun setStatusBarIconsLight(window: Window, isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // --- Modern approach for API 30 (R / Android 11) and above ---
            val insetsController = window.insetsController ?: return

            if (isLight) {
                // Request dark icons (used for a light status bar background)
                insetsController.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                // Request light/white icons (used for a dark status bar background)
                insetsController.setSystemBarsAppearance(
                    0, // Clear the light status bar appearance flag
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // --- Legacy approach for API 23 (M / Android 6) to API 29 (Q / Android 10) ---
            val decorView = window.decorView
            var flags = decorView.systemUiVisibility

            // View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR makes icons DARK.
            // To get WHITE icons, we ensure this flag is NOT set.
            if (isLight) {
                // Make icons DARK
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                // Make icons LIGHT/WHITE (by removing the flag)
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            decorView.systemUiVisibility = flags
        }
    }
}