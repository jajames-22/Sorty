package com.example.sorty

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater // Added
import android.view.View
import android.view.ViewGroup // Added
import android.view.Window
import android.widget.ImageView // Added
import android.widget.TextView // Added
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView // Added for Adapter
import com.example.sorty.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator // Added for Dots
import com.example.sorty.ui.home.Home
class MainActivity : AppCompatActivity() {

    private lateinit var bind: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 0. CHECK SESSION: If logged in, skip to Home immediately
        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish() // Close MainActivity so user can't go back to it
            return // Stop loading the rest of the UI
        }

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
        // START: NEW CAROUSEL CODE (ViewPager2 + TabLayout)
        // =========================================================

        // 1. Define your slides data
        // Ensure you have a drawable named "sorty_logo" (or change to whatever icon you want per slide)
        val slides = listOf(
            OnboardingItem(R.drawable.organize_logo, "Organize your files"),
            OnboardingItem(R.drawable.upload_logo,   "Upload Documents"),
            OnboardingItem(R.drawable.task_logo,     "Manage your tasks")
        )

        // 2. Setup Adapter using ViewBinding
        // Note: bind.viewPager comes from the ID in activity_main.xml
        val adapter = OnboardingAdapter(slides)
        bind.viewPager.adapter = adapter

        // 3. Attach Tabs (The Dots)
        // Note: bind.tabLayout comes from the ID in activity_main.xml
        TabLayoutMediator(bind.tabLayout, bind.viewPager) { _, _ ->
            // No text needed in the tabs, just dots
        }.attach()

        // =========================================================
        // END: NEW CAROUSEL CODE
        // =========================================================

        val createacc = bind.createAcc

        createacc.setOnClickListener {
            val intent = Intent(this, CreateAccount::class.java)
            startActivity(intent)
        }
    }

    /**
     * Helper function for system bars
     */
    private fun setSystemBarAppearance(window: Window, isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = isLight
            controller.isAppearanceLightNavigationBars = isLight
        } else {
            val decorView = window.decorView
            var flags = decorView.systemUiVisibility
            if (isLight) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            } else {
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                }
            }
            decorView.systemUiVisibility = flags
        }
    }
}

// =========================================================
// HELPER CLASSES FOR THE CAROUSEL
// You can keep these at the bottom of the file
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
        // IMPORTANT: Ensure you created 'item_onboarding.xml' in layout folder!
        return OnboardingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding, parent, false)
        )
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}