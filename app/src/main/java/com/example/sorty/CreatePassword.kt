package com.example.sorty

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sorty.databinding.ActivityCreatePasswordBinding

class CreatePassword : AppCompatActivity() {

    private lateinit var bind: ActivityCreatePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityCreatePasswordBinding.inflate(layoutInflater)
        setContentView(bind.root)
        supportActionBar?.hide()

        // 1. Receive data from CreateAccount
        val first = intent.getStringExtra("EXTRA_FIRST")
        val last = intent.getStringExtra("EXTRA_LAST")
        val bday = intent.getStringExtra("EXTRA_BDAY")
        val email = intent.getStringExtra("EXTRA_EMAIL")
        val school = intent.getStringExtra("EXTRA_SCHOOL")
        val course = intent.getStringExtra("EXTRA_COURSE")

        bind.backbtn.setOnClickListener { finish() }

        bind.buttonNext.setOnClickListener {
            val pass = bind.editPassword.text.toString().trim()
            val confirmPass = bind.editConfirmPassword.text.toString().trim()

            // 2. Validate Password
            if (pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) { // Optional length check
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Mark account creation as started/active
            val prefs = getSharedPreferences("SortyPrefs", MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putBoolean("has_account", true)
            editor.apply()

            // 4. Pass ALL data to InsertPicture
            val intent = Intent(this, InsertPicture::class.java)
            intent.putExtra("EXTRA_FIRST", first)
            intent.putExtra("EXTRA_LAST", last)
            intent.putExtra("EXTRA_BDAY", bday)
            intent.putExtra("EXTRA_EMAIL", email)
            intent.putExtra("EXTRA_SCHOOL", school)
            intent.putExtra("EXTRA_COURSE", course)
            intent.putExtra("EXTRA_PASSWORD", pass) // Passing the confirmed password

            startActivity(intent)
        }
    }
}