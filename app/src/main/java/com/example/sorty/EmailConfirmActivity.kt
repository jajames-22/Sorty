package com.example.sorty.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sorty.InsertPicture
import com.example.sorty.databinding.ActivityEmailConfirmBinding

class EmailConfirmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailConfirmBinding
    private lateinit var otpBoxes: Array<EditText>
    private var generatedOtp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Enable Edge-to-Edge BEFORE setting content view
        enableEdgeToEdge()

        binding = ActivityEmailConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // 2. Fix Status Bar Icon Contrast (Set to White)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false // false = white icons

        // 3. Apply System Bars Padding (Prevents overlapping with clock/battery)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        generatedOtp = intent.getStringExtra("EXTRA_OTP")

        // Retrieve and display email
        val userEmail = intent.getStringExtra("EXTRA_EMAIL")
        binding.tvEmailDisplay.text = userEmail ?: "your email"

        setupOtpLogic()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnContinue.setOnClickListener {
            val inputOtp = getOtpString()
            if (inputOtp.length < 6) {
                Toast.makeText(this, "Please enter the full 6-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyCode(inputOtp)
        }
    }

    private fun setupOtpLogic() {
        try {
            // Using ID 'otp_container' from the XML update I gave you earlier
            val otpContainer = binding.otpContainer

            otpBoxes = Array(6) { i -> otpContainer.getChildAt(i) as EditText }

            for (i in 0 until 6) {
                otpBoxes[i].addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (s?.length == 1 && i < 5) {
                            otpBoxes[i + 1].requestFocus()
                        }
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })

                otpBoxes[i].setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                        if (otpBoxes[i].text.isEmpty() && i > 0) {
                            otpBoxes[i - 1].requestFocus()
                            otpBoxes[i - 1].text = null
                            return@setOnKeyListener true
                        }
                    }
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getOtpString(): String {
        if (!::otpBoxes.isInitialized) return ""
        val sb = StringBuilder()
        for (box in otpBoxes) {
            sb.append(box.text.toString())
        }
        return sb.toString()
    }

    private fun verifyCode(inputCode: String) {
        if (inputCode == generatedOtp) {
            Toast.makeText(this, "Verification Successful!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, InsertPicture::class.java).apply {
                // Ensure we are grabbing the "EXTRA_FIRST" from the incoming intent
                // so it can be passed to the next activity
                putExtra("EXTRA_FIRST", this@EmailConfirmActivity.intent.getStringExtra("EXTRA_FIRST"))
                putExtra("EXTRA_LAST", this@EmailConfirmActivity.intent.getStringExtra("EXTRA_LAST"))
                putExtra("EXTRA_BDAY", this@EmailConfirmActivity.intent.getStringExtra("EXTRA_BDAY"))
                putExtra("EXTRA_EMAIL", this@EmailConfirmActivity.intent.getStringExtra("EXTRA_EMAIL"))
                putExtra("EXTRA_SCHOOL", this@EmailConfirmActivity.intent.getStringExtra("EXTRA_SCHOOL"))
                putExtra("EXTRA_COURSE", this@EmailConfirmActivity.intent.getStringExtra("EXTRA_COURSE"))
                putExtra("EXTRA_PASSWORD", this@EmailConfirmActivity.intent.getStringExtra("EXTRA_PASSWORD"))
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show()
        }
    }
}