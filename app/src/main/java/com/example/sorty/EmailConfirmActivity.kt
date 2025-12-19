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
import com.example.sorty.InsertPicture
import com.example.sorty.SessionManager
import com.example.sorty.databinding.ActivityEmailConfirmBinding
import com.example.sorty.ui.home.Home

class EmailConfirmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailConfirmBinding
    private lateinit var otpBoxes: Array<EditText>
    private lateinit var sessionManager: SessionManager
    private var generatedOtp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. UI Setup
        enableEdgeToEdge()
        binding = ActivityEmailConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // 2. Initialize Helpers
        sessionManager = SessionManager(this)

        // 3. Status Bar & Window Insets
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false // White icons for green background

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 4. Data Retrieval
        generatedOtp = intent.getStringExtra("EXTRA_OTP")
        val userEmail = intent.getStringExtra("EXTRA_EMAIL")
        binding.tvEmailDisplay.text = userEmail ?: "your email"

        setupOtpLogic()

        // 5. Button Listeners
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

    private fun setupOtpLogic() {
        try {
            // Access the LinearLayout containing the 6 EditTexts
            val otpContainer = binding.otpContainer
            otpBoxes = Array(6) { i -> otpContainer.getChildAt(i) as EditText }

            for (i in 0 until 6) {
                // Move focus forward when typing
                otpBoxes[i].addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (s?.length == 1 && i < 5) {
                            otpBoxes[i + 1].requestFocus()
                        }
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })

                // Move focus backward on backspace
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getOtpString(): String {
        if (!::otpBoxes.isInitialized) return ""
        val sb = StringBuilder()
        for (box in otpBoxes) {
            sb.append(box.text.toString())
        }
        return sb.toString()
    }

    private fun verifyCode(inputCode: String) {
        if (inputCode == generatedOtp) {
            Toast.makeText(this, "Verification Successful!", Toast.LENGTH_SHORT).show()

            val firstName = intent.getStringExtra("EXTRA_FIRST") ?: "User"
            val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""

            // LOGIC BRANCH: Update vs. Registration
            if (sessionManager.isLoggedIn()) {
                // SCENARIO: User is updating profile (Already logged in)

                // 1. Update the local session with the new email/name
                sessionManager.createLoginSession(email, firstName)

                // 2. Clear task stack and go to Home
                val intent = Intent(this, Home::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            } else {
                // SCENARIO: New User Registration

                val intent = Intent(this, InsertPicture::class.java).apply {
                    putExtra("EXTRA_FIRST", firstName)
                    putExtra("EXTRA_LAST", intent.getStringExtra("EXTRA_LAST"))
                    putExtra("EXTRA_BDAY", intent.getStringExtra("EXTRA_BDAY"))
                    putExtra("EXTRA_EMAIL", email)
                    putExtra("EXTRA_SCHOOL", intent.getStringExtra("EXTRA_SCHOOL"))
                    putExtra("EXTRA_COURSE", intent.getStringExtra("EXTRA_COURSE"))
                    putExtra("EXTRA_PASSWORD", intent.getStringExtra("EXTRA_PASSWORD"))
                }
                startActivity(intent)
                finish()
            }
        } else {
            Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show()
        }
    }
}