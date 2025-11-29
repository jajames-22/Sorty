package com.example.sorty

import android.content.Intent
import android.graphics.Color // IMPORT THIS
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat // IMPORT THIS
import androidx.core.view.WindowInsetsCompat
import com.example.sorty.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var bind: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. ENABLE EDGE-TO-EDGE
        // This allows your app to draw behind the system bars.
        enableEdgeToEdge()

        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // --- START: Fullscreen Transparent Styling ---

        // 2. MAKE SYSTEM BARS TRANSPARENT
        // This makes the status and navigation bars see-through.
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // 3. SET SYSTEM ICON COLORS
        // We call our helper function. 'isLight' is false, meaning we want LIGHT icons
        // because we assume the background of this activity is dark.
        setSystemBarAppearance(window, false)

        // --- END: Fullscreen Transparent Styling ---


        // 4. APPLY INSETS AS PADDING
        // This listener is crucial. It gets the size of the transparent system bars
        // and adds that space as padding to your root layout, preventing your UI
        // (like login/create account buttons) from being hidden.
        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        val createacc = bind.createAcc
        val loginBtn = bind.loginBtn

        createacc.setOnClickListener {
            val intent = Intent(this, CreateAccount::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
    }

    /**
     * A helper function to control the color of status and navigation bar icons.
     * @param window The activity's window.
     * @param isLight true if the background is light (needs dark icons), false if the background is dark (needs light icons).
     */
    private fun setSystemBarAppearance(window: Window, isLight: Boolean) {
        // Modern approach for API 30+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            // For status bar icons
            controller.isAppearanceLightStatusBars = isLight
            // For navigation bar icons
            controller.isAppearanceLightNavigationBars = isLight
        }
        // Legacy approach for API 23-29
        else {
            val decorView = window.decorView
            var flags = decorView.systemUiVisibility
            if (isLight) {
                // Add flags to make icons dark
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            } else {
                // Remove flags to make icons light
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                }
            }
            decorView.systemUiVisibility = flags
        }
    }
}