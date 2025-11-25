package com.example.sorty

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
// Import the correct binding class
import com.example.sorty.databinding.ActivityCreateAccountBinding

class CreateAccount : AppCompatActivity() {
    // Declare the correct binding class
    private lateinit var bind: ActivityCreateAccountBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate the correct binding class
        bind = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(bind.root)
        setStatusBarIconsLight(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()

        val backbtn = bind.backbtn

        backbtn.setOnClickListener {
            finish()
        }
    }

    private fun setStatusBarIconsLight(window: Window, isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // --- Modern approach for API 30 (R / Android 11) and above ---
            val insetsController = window.insetsController ?: return

            if (isLight) {
                // Request dark icons (used for a light status bar background)
                insetsController.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                // Request light/white icons (used for a dark status bar background)
                insetsController.setSystemBarsAppearance(
                    0, // Clear the light status bar appearance flag
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // --- Legacy approach for API 23 (M / Android 6) to API 29 (Q / Android 10) ---
            val decorView = window.decorView
            var flags = decorView.systemUiVisibility

            // View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR makes icons DARK.
            // To get WHITE icons, we ensure this flag is NOT set.
            if (isLight) {
                // Make icons DARK
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                // Make icons LIGHT/WHITE (by removing the flag)
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            decorView.systemUiVisibility = flags
        }
    }
}