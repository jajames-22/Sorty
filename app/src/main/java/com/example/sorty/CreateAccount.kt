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

    private lateinit var bind: ActivityCreateAccountBinding
    private val calendar = Calendar.getInstance()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        // Check if already logged in (Legacy check)
        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()

        bind = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(bind.root)
        setStatusBarIconsLight(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()

        bind.backbtn.setOnClickListener {
            finish()
        }

        bind.buttonContinue.setOnClickListener {
            // 1. Get inputs
            val first = bind.editFirstName.text.toString().trim()
            val last = bind.editLastName.text.toString().trim()
            val bday = bind.editBday.text.toString().trim()
            val email = bind.editEmail.text.toString().trim()
            val password = bind.editPassword.text.toString().trim()
            val school = bind.editSchool.text.toString().trim()
            val course = bind.editCourse.text.toString().trim()

            // 2. Validate inputs
            if (first.isEmpty() || last.isEmpty() || bday.isEmpty() ||
                email.isEmpty() || password.isEmpty() || school.isEmpty() || course.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ==================================================================
            // NEW: Save "has_account" flag so MainActivity knows to skip to Login
            // ==================================================================
            val prefs = getSharedPreferences("SortyPrefs", MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putBoolean("has_account", true)
            editor.apply()

            // 3. PASS DATA to InsertPicture
            val intent = Intent(this, InsertPicture::class.java)

            intent.putExtra("EXTRA_FIRST", first)
            intent.putExtra("EXTRA_LAST", last)
            intent.putExtra("EXTRA_BDAY", bday)
            intent.putExtra("EXTRA_EMAIL", email)
            intent.putExtra("EXTRA_PASSWORD", password)
            intent.putExtra("EXTRA_SCHOOL", school)
            intent.putExtra("EXTRA_COURSE", course)

            startActivity(intent)
            // Note: We do NOT finish() here if you want them to be able to come back
            // from InsertPicture. If InsertPicture is a one-way street, you can add finish().
        }

        bind.editBday.setOnClickListener {
            showDatePickerDialog()
        }
    }

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
        // Set max date to today (cannot pick future birthdays)
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