package com.example.sorty

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.viewpager2.widget.ViewPager2
import com.example.sorty.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.example.sorty.ui.home.Home

class MainActivity : AppCompatActivity() {

    private lateinit var bind: ActivityMainBinding

    // --- Auto-scroll variables ---
    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        val totalItems = bind.viewPager.adapter?.itemCount ?: 0
        if (totalItems > 0) {
            val nextItem = (bind.viewPager.currentItem + 1) % totalItems
            bind.viewPager.setCurrentItem(nextItem, true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Session Checks
        val prefs = getSharedPreferences("SortyPrefs", MODE_PRIVATE)
        val hasAccount = prefs.getBoolean("has_account", false)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)

        if (isLoggedIn) {
            startActivity(Intent(this, Home::class.java))
            finish()
            return
        }

        if (hasAccount) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // Styling
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        setSystemBarAppearance(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        // --- CAROUSEL SETUP ---
        val slides = listOf(
            OnboardingItem(R.drawable.organize_logo, "Organize your files"),
            OnboardingItem(R.drawable.upload_logo,   "Upload Documents"),
            OnboardingItem(R.drawable.task_logo,     "Manage your tasks")
        )

        val adapter = OnboardingAdapter(slides)
        bind.viewPager.adapter = adapter

        // Attach Dots Indicator
        TabLayoutMediator(bind.tabLayout, bind.viewPager) { _, _ -> }.attach()

        // Reset timer on manual swipe
        bind.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 4000)
            }
        })

        bind.createAcc.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 4000)
    }

    private fun setSystemBarAppearance(window: Window, isLight: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = isLight
        controller.isAppearanceLightNavigationBars = isLight
    }
}

// =========================================================
// HELPER CLASSES (The missing parts)
// =========================================================

/**
 * Data model for each slide in the carousel
 */
data class OnboardingItem(val image: Int, val title: String)

/**
 * Adapter to manage the carousel views
 */
class OnboardingAdapter(private val items: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // These IDs must match your item_onboarding.xml file
        val image: ImageView = view.findViewById(R.id.slideIcon)
        val text: TextView = view.findViewById(R.id.slideTitle)

        fun bind(item: OnboardingItem) {
            image.setImageResource(item.image)
            text.text = item.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}