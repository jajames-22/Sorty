package com.example.sorty

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sorty.databinding.ActivityLoginBinding
import com.example.sorty.ui.home.Home // Ensure this imports your Home Activity

class LoginActivity : AppCompatActivity() {

    private lateinit var bind: ActivityLoginBinding
    private lateinit var db: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize View Binding
        bind = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // 2. Initialize Helpers
        db = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        // 3. Set Status Bar to Green (match background)
        window.statusBarColor = getColor(R.color.primary_green) // Make sure primary_green is defined
        setSystemBarAppearance(window, false) // false = white text/icons

        // 4. Handle Edge-to-Edge padding
        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 5. Hide Action Bar
        supportActionBar?.hide()

        setupButtons()
    }

    private fun setupButtons() {
        // --- LOGIN BUTTON ---
        bind.btnContinue.setOnClickListener {
            val email = bind.etEmail.text.toString().trim()
            val password = bind.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Check Database
                // Note: You need to implement 'checkUser' in DatabaseHelper. See below.
                val isValid = db.checkUser(email, password)

                if (isValid) {
                    // SAVE SESSION
                    sessionManager.createLoginSession(email) // You might need to add this method

                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                    // Navigate to Home
                    val intent = Intent(this, Home::class.java)
                    // Clear history so back button exits app instead of returning to login
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // NAVIGATE TO CREATE ACCOUNT ACTIVITY
        bind.btnCreateAccount.setOnClickListener {
            // "this" refers to LoginActivity, "CreateAccount::class.java" is the destination
            val intent = Intent(this, CreateAccount::class.java)
            startActivity(intent)
        }

        // --- GOOGLE LOGIN (Placeholder) ---
        bind.ivGoogleLogin.setOnClickListener {
            Toast.makeText(this, "Google Login feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // --- FORGOT PASSWORD (Placeholder) ---
        bind.tvForgotPass.setOnClickListener {
            Toast.makeText(this, "Forgot Password feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper for Status Bar Text Color
    private fun setSystemBarAppearance(window: Window, isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = window.insetsController ?: return
            if (isLight) {
                insetsController.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                insetsController.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
            }
        } else {
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