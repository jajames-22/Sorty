package com.example.sorty

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
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

class CreateAccount : AppCompatActivity() {

    private lateinit var bind: ActivityCreateAccountBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            val school = bind.editSchool.text.toString().trim()
            val course = bind.editCourse.text.toString().trim()

            // 2. Validate inputs
            if (first.isEmpty() || last.isEmpty() || bday.isEmpty() ||
                email.isEmpty() || school.isEmpty() || course.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. PASS DATA to InsertPicture instead of saving to DB
            val intent = Intent(this, InsertPicture::class.java)

            intent.putExtra("EXTRA_FIRST", first)
            intent.putExtra("EXTRA_LAST", last)
            intent.putExtra("EXTRA_BDAY", bday)
            intent.putExtra("EXTRA_EMAIL", email)
            intent.putExtra("EXTRA_SCHOOL", school)
            intent.putExtra("EXTRA_COURSE", course)

            startActivity(intent)
            // Do not finish() here if you want the user to be able to come back and edit details
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