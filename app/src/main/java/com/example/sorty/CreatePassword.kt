package com.example.sorty

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sorty.databinding.ActivityCreatePasswordBinding
import com.example.sorty.ui.auth.EmailConfirmActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreatePassword : AppCompatActivity() {

    private lateinit var bind: ActivityCreatePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        bind = ActivityCreatePasswordBinding.inflate(layoutInflater)
        setContentView(bind.root)
        supportActionBar?.hide()

        // Set status bar icons to white
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false

        // Handle Status Bar Padding
        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve Intent Data
        val first = intent.getStringExtra("EXTRA_FIRST") ?: "User"
        val last = intent.getStringExtra("EXTRA_LAST") ?: ""
        val bday = intent.getStringExtra("EXTRA_BDAY") ?: ""
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""
        val school = intent.getStringExtra("EXTRA_SCHOOL") ?: ""
        val course = intent.getStringExtra("EXTRA_COURSE") ?: ""

        bind.backbtn.setOnClickListener { finish() }

        bind.buttonNext.setOnClickListener {
            val pass = bind.editPassword.text.toString().trim()
            val confirmPass = bind.editConfirmPassword.text.toString().trim()

            // Validation
            if (pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val otp = (100000..999999).random().toString()

            // UI State: Loading
            bind.buttonNext.isEnabled = false
            bind.buttonNext.text = "Please Wait..."

            // Use lifecycleScope to handle background work safely
            lifecycleScope.launch(Dispatchers.IO) {
                val emailBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; border: 1px solid #eeeeee; }
                        .header { background-color: #2C4E34; padding: 30px; text-align: center; color: #ffffff; }
                        .content { padding: 40px; text-align: center; color: #333333; }
                        .otp-card { background-color: #FFA800; color: #ffffff; padding: 15px 30px; border-radius: 8px; display: inline-block; }
                        .otp-code { font-size: 32px; font-weight: bold; letter-spacing: 5px; margin: 0; }
                        .footer { background-color: #2C4E34; padding: 15px; text-align: center; color: #ffffff; font-size: 11px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1 style="margin:0;">Sorty Email Verification</h1></div>
                        <div class="content">
                            <p style="font-size: 18px;">Hello <b>$first</b>,</p>
                            <p>Your verification code is:</p>
                            <div class="otp-card"><h2 class="otp-code">$otp</h2></div>
                            <p style="color: #888888; font-size: 14px; margin-top: 20px;">This code expires in 10 minutes.</p>
                        </div>
                        <div class="footer">Keep your files sorted and secure.</div>
                    </div>
                </body>
                </html>
                """.trimIndent()

                val success = GMailSender.sendEmail(email, "Sorty App Verification Code", emailBody)

                withContext(Dispatchers.Main) {
                    bind.buttonNext.isEnabled = true
                    bind.buttonNext.text = "CONTINUE"

                    if (success) {
                        Toast.makeText(this@CreatePassword, "OTP sent to $email", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@CreatePassword, EmailConfirmActivity::class.java).apply {
                            putExtra("EXTRA_OTP", otp)
                            putExtra("EXTRA_FIRST", first)
                            putExtra("EXTRA_LAST", last)
                            putExtra("EXTRA_BDAY", bday)
                            putExtra("EXTRA_EMAIL", email)
                            putExtra("EXTRA_SCHOOL", school)
                            putExtra("EXTRA_COURSE", course)
                            putExtra("EXTRA_PASSWORD", pass)
                        }
                        startActivity(intent)
                        // finish() // Optional: uncomment if you don't want them back here
                    } else {
                        Toast.makeText(this@CreatePassword, "Failed to send email. Check connection.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}