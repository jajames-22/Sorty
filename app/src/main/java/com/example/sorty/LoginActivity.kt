package com.example.sorty

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sorty.databinding.ActivityLoginBinding
import com.example.sorty.ui.home.Home

class LoginActivity : AppCompatActivity() {

    private lateinit var bind: ActivityLoginBinding
    private lateinit var db: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        db = DatabaseHelper(this)

        // Redirect if already logged in
        if (sessionManager.isLoggedIn()) {
            goToHome()
            return
        }

        bind = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // UI Styling: Standard way to set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_green)
        setSystemBarAppearance(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()
        setupButtons()
    }

    private fun setupButtons() {
        bind.btnContinue.setOnClickListener {
            val email = bind.etEmail.text.toString().trim()
            val password = bind.editPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val isValid = db.checkUser(email, password)

                if (isValid) {
                    // Fetch name from DatabaseHelper to store in session
                    val firstName = db.getUserFirstName(email)

                    // Save to SessionManager (String email, String firstName)
                    sessionManager.createLoginSession(email, firstName)

                    // Sync with MainActivity's separate check
                    getSharedPreferences("SortyPrefs", MODE_PRIVATE)
                        .edit().putBoolean("is_logged_in", true).apply()

                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bind.btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, CreateAccount::class.java))
        }

        bind.tvForgotPass.setOnClickListener {
            Toast.makeText(this, "Forgot Password feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToHome() {
        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setSystemBarAppearance(window: Window, isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = window.insetsController ?: return
            insetsController.setSystemBarsAppearance(
                if (isLight) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = if (isLight) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                0
            }
        }
    }
}