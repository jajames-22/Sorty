package com.example.sorty

import android.content.Intent
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
import com.example.sorty.ui.home.Home

class LoginActivity : AppCompatActivity() {

    private lateinit var bind: ActivityLoginBinding
    private lateinit var db: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Helpers
        sessionManager = SessionManager(this)
        db = DatabaseHelper(this)

        // 2. SESSION CHECK: Kung naka-login na, diretso agad sa Home
        if (sessionManager.isLoggedIn()) { // Siguraduhin na may isLoggedIn() method sa SessionManager
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return // Para hindi na ituloy ang pag-inflate ng layout
        }

        // 3. Initialize View Binding
        bind = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // 4. UI Styling (Green Status Bar)
        window.statusBarColor = getColor(R.color.primary_green)
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
            // FIX: added .text bago mag .toString()
            val password = bind.editPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val isValid = db.checkUser(email, password)

                if (isValid) {
                    // SAVE SESSION & Update Prefs for MainActivity logic
                    sessionManager.createLoginSession(email)

                    // Napaka-importante nito para sa logic ng MainActivity mo:
                    val prefs = getSharedPreferences("SortyPrefs", MODE_PRIVATE)
                    prefs.edit().putBoolean("is_logged_in", true).apply()

                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
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