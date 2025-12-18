package com.example.sorty

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sorty.databinding.ActivityCreatePasswordBinding
// 1. Import your EmailConfirmActivity (since it is in a sub-package)
import com.example.sorty.ui.auth.EmailConfirmActivity

class CreatePassword : AppCompatActivity() {

    private lateinit var bind: ActivityCreatePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityCreatePasswordBinding.inflate(layoutInflater)
        setContentView(bind.root)
        supportActionBar?.hide()

        // 2. Receive data from CreateAccount
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

            // 3. Validate Password
            if (pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. Save state (Optional)
            val prefs = getSharedPreferences("SortyPrefs", MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putBoolean("has_account", true)
            editor.apply()

            // 5. Navigate to EmailConfirmActivity
            // We pass ALL the data (including the new password) so it isn't lost
            val intent = Intent(this, EmailConfirmActivity::class.java)

            intent.putExtra("EXTRA_FIRST", first)
            intent.putExtra("EXTRA_LAST", last)
            intent.putExtra("EXTRA_BDAY", bday)
            intent.putExtra("EXTRA_EMAIL", email)
            intent.putExtra("EXTRA_SCHOOL", school)
            intent.putExtra("EXTRA_COURSE", course)
            intent.putExtra("EXTRA_PASSWORD", pass)

            startActivity(intent)
        }
    }
}