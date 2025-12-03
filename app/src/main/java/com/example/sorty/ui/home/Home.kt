package com.example.sorty.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sorty.R
import com.example.sorty.databinding.ActivityHomeBinding

class Home : AppCompatActivity() {

    private lateinit var bind: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bind = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // Make system bars transparent
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // Set system bar icons to dark
        setSystemBarAppearance(window, true)

        val navView = bind.navView
        val navHostContainer: View = findViewById(R.id.nav_host_fragment_activity_home)

        // Set an insets listener on the root view
        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { _, insets ->

            // --- THIS IS THE CORRECTED LINE ---
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply the top inset as top padding to the NavHostFragment's container.
            navHostContainer.updatePadding(top = systemBars.top)

            // Apply the bottom inset as bottom padding to the BottomNavigationView.
            navView.updatePadding(bottom = systemBars.bottom)

            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_home) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_subjects, R.id.navigation_account
            )
        )
        supportActionBar?.hide()
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun setSystemBarAppearance(window: Window, isLight: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = isLight
        controller.isAppearanceLightNavigationBars = isLight
    }
}