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
// Tiyaking tama ang class name ng Home Activity mo
import com.example.sorty.MainActivity // <--- PALITAN KUNG IBA ANG PANGALAN
import com.example.sorty.ui.home.Home

class CreateAccount : AppCompatActivity() {

    private lateinit var bind: ActivityCreateAccountBinding
    private val calendar = Calendar.getInstance()
    private lateinit var sessionManager: SessionManager // Deklarasyon ng Session Manager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this) // Instantiation

        // HAKBANG 1: SESSION CHECK - I-redirect agad kung naka-login na
        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish() // Isara ang CreateAccount Activity
            return // Huwag na ituloy ang pag-render ng form
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

        // FIXED: backbtn
        bind.backbtn.setOnClickListener {
            finish()
        }

        // FIXED: button_continue (nagiging buttonContinue sa binding)
        bind.buttonContinue.setOnClickListener {
            // 1. Get inputs
            // FIXED: edit_first_name (nagiging editFirstName sa binding)
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

            // 3. PASS DATA to InsertPicture
            val intent = Intent(this, InsertPicture::class.java)

            intent.putExtra("EXTRA_FIRST", first)
            intent.putExtra("EXTRA_LAST", last)
            intent.putExtra("EXTRA_BDAY", bday)
            intent.putExtra("EXTRA_EMAIL", email)
            intent.putExtra("EXTRA_SCHOOL", school)
            intent.putExtra("EXTRA_COURSE", course)

            startActivity(intent)
        }

        // FIXED: edit_bday
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