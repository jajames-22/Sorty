package com.example.sorty

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sorty.databinding.ActivityCreatePasswordBinding
import com.example.sorty.ui.auth.EmailConfirmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreatePassword : AppCompatActivity() {

    private lateinit var bind: ActivityCreatePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityCreatePasswordBinding.inflate(layoutInflater)
        setContentView(bind.root)
        supportActionBar?.hide()

        // Receive data
        val first = intent.getStringExtra("EXTRA_FIRST")
        val last = intent.getStringExtra("EXTRA_LAST")
        val bday = intent.getStringExtra("EXTRA_BDAY")
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: "" // Default to empty if null
        val school = intent.getStringExtra("EXTRA_SCHOOL")
        val course = intent.getStringExtra("EXTRA_COURSE")

        bind.backbtn.setOnClickListener { finish() }

        bind.buttonNext.setOnClickListener {
            val pass = bind.editPassword.text.toString().trim()
            val confirmPass = bind.editConfirmPassword.text.toString().trim()

            // Validate
            if (pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Generate 6-Digit OTP
            val otp = (100000..999999).random().toString()

            // Show loading or disable button so user doesn't spam click
            bind.buttonNext.isEnabled = false
            bind.buttonNext.text = "Sending OTP..."

            // 2. Send Email in Background Thread
            CoroutineScope(Dispatchers.IO).launch {
                val success = GMailSender.sendEmail(
                    email,
                    "Sorty App Verification Code",
                    "Hello $first,\n\nYour verification code is: $otp\n\nThis code expires in 10 minutes."
                )

                // 3. Return to Main Thread to Update UI
                withContext(Dispatchers.Main) {
                    bind.buttonNext.isEnabled = true
                    bind.buttonNext.text = "CONTINUE"

                    if (success) {
                        Toast.makeText(applicationContext, "OTP sent to $email", Toast.LENGTH_SHORT).show()

                        val prefs = getSharedPreferences("SortyPrefs", MODE_PRIVATE)
                        val editor = prefs.edit()
                        editor.putBoolean("has_account", true)
                        editor.apply()

                        // 4. Navigate and Pass the OTP to verify it later
                        val intent = Intent(this@CreatePassword, EmailConfirmActivity::class.java)

                        intent.putExtra("EXTRA_OTP", otp) // <-- PASS OTP TO NEXT SCREEN
                        intent.putExtra("EXTRA_FIRST", first)
                        intent.putExtra("EXTRA_LAST", last)
                        intent.putExtra("EXTRA_BDAY", bday)
                        intent.putExtra("EXTRA_EMAIL", email)
                        intent.putExtra("EXTRA_SCHOOL", school)
                        intent.putExtra("EXTRA_COURSE", course)
                        intent.putExtra("EXTRA_PASSWORD", pass)

                        startActivity(intent)
                    } else {
                        Toast.makeText(applicationContext, "Failed to send email. Check internet or email address.", Toast.LENGTH_LONG).show()
                        // FOR TESTING: You might want to uncomment this line below to bypass email failure while testing
                        // startActivity(intent)
                    }
                }
            }
        }
    }
}