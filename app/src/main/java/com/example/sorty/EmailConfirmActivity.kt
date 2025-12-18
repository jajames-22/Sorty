package com.example.sorty.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sorty.MainActivity
import com.example.sorty.R
import com.example.sorty.databinding.ActivityEmailConfirmBinding

class EmailConfirmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailConfirmBinding
    private lateinit var otpBoxes: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // Initialize your OTP boxes array for easier iteration
        // Make sure to add IDs to your XML EditTexts (e.g., otp1, otp2, etc.)
        setupOtpLogic()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnContinue.setOnClickListener {
            val otpValue = getOtpString()
            if (otpValue.length == 6) {
                // Perform verification logic here (e.g., Firebase Auth or Firestore check)
                verifyCode(otpValue)
            } else {
                Toast.makeText(this, "Please enter the full code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupOtpLogic() {
        // Accessing the children of the horizontal LinearLayout containing the OTP boxes
        val layout = binding.btnContinue.parent as android.widget.LinearLayout
        val otpContainer = layout.getChildAt(3) as android.widget.LinearLayout

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

            // Handle Backspace to go to previous box
            otpBoxes[i].setOnKeyListener { v, keyCode, event ->
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
    }

    private fun getOtpString(): String {
        val sb = StringBuilder()
        for (box in otpBoxes) {
            sb.append(box.text.toString())
        }
        return sb.toString()
    }

    private fun verifyCode(code: String) {
        // Placeholder for your verification logic
        Toast.makeText(this, "Verifying: $code", Toast.LENGTH_SHORT).show()

        // On success, go to Home/Main Activity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}