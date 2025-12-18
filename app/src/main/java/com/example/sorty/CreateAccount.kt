package com.example.sorty

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
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

    // 1. Declare Binding
    private lateinit var bind: ActivityCreateAccountBinding

    // Calendar for DatePicker
    private val calendar = Calendar.getInstance()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Check Session (Skip screen if already logged in)
        sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()

        // 3. Initialize ViewBinding
        bind = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // Optional: UI Adjustments (Status bar, hiding Action bar)
        supportActionBar?.hide()
        setStatusBarIconsLight(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 4. Back Button Logic
        bind.backbtn.setOnClickListener {
            finish()
        }

        // 5. Date Picker Logic
        bind.editBday.setOnClickListener {
            showDatePickerDialog()
        }

        // 6. "CONTINUE" BUTTON LOGIC -> Navigates to CreatePassword
        bind.buttonContinue.setOnClickListener {
            // A. Get text from inputs
            val first = bind.editFirstName.text.toString().trim()
            val last = bind.editLastName.text.toString().trim()
            val bday = bind.editBday.text.toString().trim()
            val email = bind.editEmail.text.toString().trim()
            val school = bind.editSchool.text.toString().trim()
            val course = bind.editCourse.text.toString().trim()

            // B. Validate that fields are not empty
            if (first.isEmpty() || last.isEmpty() || bday.isEmpty() ||
                email.isEmpty() || school.isEmpty() || course.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // C. Create Intent to go to CreatePassword
            val intent = Intent(this, CreatePassword::class.java)

            // D. Pass the data to the next screen (so we can save it later)
            intent.putExtra("EXTRA_FIRST", first)
            intent.putExtra("EXTRA_LAST", last)
            intent.putExtra("EXTRA_BDAY", bday)
            intent.putExtra("EXTRA_EMAIL", email)
            intent.putExtra("EXTRA_SCHOOL", school)
            intent.putExtra("EXTRA_COURSE", course)

            // E. Start the Activity
            startActivity(intent)
        }
    }

    // Helper function to show the calendar popup
    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }

        val datePicker = DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Prevent selecting future dates
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    // Helper to format the date text
    private fun updateLabel() {
        val dateFormat = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.US)
        bind.editBday.setText(simpleDateFormat.format(calendar.time))
    }

    // Helper to change status bar icon colors (White text on green background)
    private fun setStatusBarIconsLight(window: Window, isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = window.insetsController ?: return
            if (isLight) {
                insetsController.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                insetsController.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            var flags = decorView.systemUiVisibility
            if (isLight) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            decorView.systemUiVisibility = flags
        }
    }
}