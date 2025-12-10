package com.example.sorty.ui.account

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import com.example.sorty.R
import com.example.sorty.databinding.ActivitySetupPinBinding

class SetupPinActivity : AppCompatActivity() {

    private lateinit var bind: ActivitySetupPinBinding
    private var enteredPin = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bind = ActivitySetupPinBinding.inflate(layoutInflater)
        setContentView(bind.root)

        ViewCompat.setOnApplyWindowInsetsListener(bind.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupKeypad()
    }

    private fun setupKeypad() {
        // Loop through all children in the GridLayout
        for (i in 0 until bind.keypadGrid.childCount) {
            val view = bind.keypadGrid.getChildAt(i)

            // If it's a Number Button
            if (view is Button) {
                view.setOnClickListener {
                    onKeyClicked(view.text.toString())
                }
            }
        }

        // Backspace Button
        bind.btnBackspace.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin.deleteCharAt(enteredPin.length - 1)
                updateDots()
            }
        }
    }

    private fun onKeyClicked(number: String) {
        if (enteredPin.length < 4) {
            enteredPin.append(number)
            updateDots()

            // Check if PIN is complete
            if (enteredPin.length == 4) {
                savePinAndFinish()
            }
        }
    }

    private fun updateDots() {
        val length = enteredPin.length

        // Update the 4 dots based on length
        bind.dot1.setImageResource(if (length >= 1) R.drawable.bg_pin_dot_on else R.drawable.bg_pin_dot_off)
        bind.dot2.setImageResource(if (length >= 2) R.drawable.bg_pin_dot_on else R.drawable.bg_pin_dot_off)
        bind.dot3.setImageResource(if (length >= 3) R.drawable.bg_pin_dot_on else R.drawable.bg_pin_dot_off)
        bind.dot4.setImageResource(if (length >= 4) R.drawable.bg_pin_dot_on else R.drawable.bg_pin_dot_off)
    }

    private fun savePinAndFinish() {
        val pin = enteredPin.toString()

        // Save PIN to SharedPreferences
        val sharedPref = getSharedPreferences("SortyPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("USER_PIN", pin)
            putBoolean("IS_PIN_SET", true)
            apply()
        }

        Toast.makeText(this, "PIN Set Successfully!", Toast.LENGTH_SHORT).show()

        // Finish activity (return to settings or go to home)
        finish()
    }
}