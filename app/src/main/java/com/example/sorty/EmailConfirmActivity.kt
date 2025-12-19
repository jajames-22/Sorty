package com.example.sorty.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sorty.DatabaseHelper
import com.example.sorty.InsertPicture
import com.example.sorty.SessionManager
import com.example.sorty.databinding.ActivityEmailConfirmBinding
import com.example.sorty.ui.home.Home

class EmailConfirmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailConfirmBinding
    private lateinit var otpBoxes: Array<EditText>
    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DatabaseHelper
    private var generatedOtp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEmailConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sessionManager = SessionManager(this)
        dbHelper = DatabaseHelper(this)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        generatedOtp = intent.getStringExtra("EXTRA_OTP")
        val userEmail = intent.getStringExtra("EXTRA_EMAIL")
        binding.tvEmailDisplay.text = userEmail ?: "your email"

        setupOtpLogic()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnContinue.setOnClickListener {
            val inputOtp = getOtpString()
            if (inputOtp.length < 6) {
                Toast.makeText(this, "Please enter the full 6-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyCode(inputOtp)
        }
    }

    private fun verifyCode(inputCode: String) {
        if (inputCode == generatedOtp) {
            val firstName = intent.getStringExtra("EXTRA_FIRST") ?: ""
            val lastName = intent.getStringExtra("EXTRA_LAST") ?: ""
            val bday = intent.getStringExtra("EXTRA_BDAY") ?: ""
            val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""
            val school = intent.getStringExtra("EXTRA_SCHOOL") ?: ""
            val course = intent.getStringExtra("EXTRA_COURSE") ?: ""
            val password = intent.getStringExtra("EXTRA_PASSWORD") ?: ""

            if (sessionManager.isLoggedIn()) {
                // SCENARIO: Profile Update (Name/Email changed)
                // Update session so greeting reflects changes immediately
                sessionManager.createLoginSession(email, firstName)
                Toast.makeText(this, "Profile verified and updated!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, Home::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // SCENARIO: New Registration
                val isRegistered = dbHelper.insertUser(firstName, lastName, bday, email, password, school, course, "")

                if (isRegistered) {
                    sessionManager.createLoginSession(email, firstName)
                    Toast.makeText(this, "Account created and verified!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, InsertPicture::class.java).apply {
                        putExtra("EXTRA_EMAIL", email)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed. Try a different email.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupOtpLogic() {
        try {
            val otpContainer = binding.otpContainer
            otpBoxes = Array(6) { i -> otpContainer.getChildAt(i) as EditText }

            for (i in 0 until 6) {
                otpBoxes[i].addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (s?.length == 1 && i < 5) otpBoxes[i + 1].requestFocus()
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })

                otpBoxes[i].setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                        if (otpBoxes[i].text.isEmpty() && i > 0) {
                            otpBoxes[i - 1].requestFocus()
                            otpBoxes[i - 1].text = null
                            return@setOnKeyListener true
                        }
                    }
                    false
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun getOtpString(): String {
        if (!::otpBoxes.isInitialized) return ""
        val sb = StringBuilder()
        for (box in otpBoxes) sb.append(box.text.toString())
        return sb.toString()
    }
}