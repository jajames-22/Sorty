package com.example.sorty

import android.os.Build // IMPORT THIS
import android.os.Bundle
import android.view.View // IMPORT THIS
import android.view.WindowInsetsController // IMPORT THIS
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sorty.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {

    private lateinit var bind: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // 1. Set the background color to white
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)

        // 2. Tell the system to make navigation bar buttons dark for contrast
        setNavigationBarButtonsLight(true)

        // --- END: CODE TO STYLE SYSTEM NAVIGATION BAR ---


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_home) as NavHostFragment
        val navController = navHostFragment.navController
        val navView: BottomNavigationView = bind.navView
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_subjects, R.id.navigation_account
            )
        )
        supportActionBar?.hide()
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    private fun setNavigationBarButtonsLight(isLight: Boolean) {
        // This feature is only available on Android 8.0 (Oreo) and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val decorView = window.decorView
            var flags = decorView.systemUiVisibility
            if (isLight) {
                // Add the flag to make navigation bar icons dark
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                // Remove the flag to make navigation bar icons light (default)
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            decorView.systemUiVisibility = flags
        }
    }
}