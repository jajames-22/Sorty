package com.example.sorty

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sorty.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.example.sorty.ui.home.Home

class MainActivity : AppCompatActivity() {

    private lateinit var bind: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 0. SHARED PREFERENCES SETUP
        val prefs = getSharedPreferences("SortyPrefs", MODE_PRIVATE)
        val hasAccount = prefs.getBoolean("has_account", false)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false) // Optional: If you use this for Home check

        // CHECK 1: If already logged in -> Go to Home
        if (isLoggedIn) {
            startActivity(Intent(this, Home::class.java))
            finish()
            return
        }

        // CHECK 2: If they have an account (but not logged in) -> Go to Login
        if (hasAccount) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // --- If we reach here, the user has NO account. Show the Welcome/Carousel screen. ---

        // 1. ENABLE EDGE-TO-EDGE
        enableEdgeToEdge()

        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // --- START: Fullscreen Transparent Styling ---
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        setSystemBarAppearance(window, false)
        // --- END: Fullscreen Transparent Styling ---

        // 4. APPLY INSETS AS PADDING
        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        // =========================================================
        // CAROUSEL CODE
        // =========================================================
        val slides = listOf(
            OnboardingItem(R.drawable.organize_logo, "Organize your files"), // Make sure these drawables exist
            OnboardingItem(R.drawable.upload_logo,   "Upload Documents"),
            OnboardingItem(R.drawable.task_logo,     "Manage your tasks")
        )

        val adapter = OnboardingAdapter(slides)
        bind.viewPager.adapter = adapter

        TabLayoutMediator(bind.tabLayout, bind.viewPager) { _, _ -> }.attach()

        // =========================================================
        // BUTTON LOGIC
        // =========================================================

        bind.createAcc.setOnClickListener {
            val intent = Intent(this, CreateAccount::class.java)
            startActivity(intent)
        }

        // OPTIONAL: You should add a Login button to activity_main.xml for users
        // who re-installed the app but already have an account.
        // bind.btnLogin.setOnClickListener {
        //     startActivity(Intent(this, LoginActivity::class.java))
        // }
    }

    private fun setSystemBarAppearance(window: Window, isLight: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = isLight
        controller.isAppearanceLightNavigationBars = isLight
    }
}

// =========================================================
// HELPER CLASSES
// =========================================================

data class OnboardingItem(val image: Int, val title: String)

class OnboardingAdapter(private val items: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.slideIcon)
        val text: TextView = view.findViewById(R.id.slideTitle)

        fun bind(item: OnboardingItem) {
            image.setImageResource(item.image)
            text.text = item.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        return OnboardingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false)
        )
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}