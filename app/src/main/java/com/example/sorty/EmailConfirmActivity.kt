package com.example.sorty.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sorty.InsertPicture
import com.example.sorty.databinding.ActivityEmailConfirmBinding

class EmailConfirmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailConfirmBinding
    private lateinit var otpBoxes: Array<EditText>
    private var generatedOtp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // 1. Retrieve the OTP and Email passed from CreatePassword
        generatedOtp = intent.getStringExtra("EXTRA_OTP")
        val userEmail = intent.getStringExtra("EXTRA_EMAIL")

        // 2. Display the actual email address (assuming you added an ID to the TextView in XML)
        // If you haven't added an ID yet, this line is optional but recommended:
        // binding.txtEmailDisplay.text = userEmail

        setupOtpLogic()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnContinue.setOnClickListener {
            val inputOtp = getOtpString()

            if (inputOtp.length < 6) {
                Toast.makeText(this, "Please enter the full 6-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Verify the Code
            verifyCode(inputOtp)
        }
    }

    private fun setupOtpLogic() {
        // Safer approach: Ensure your OTP container LinearLayout has an ID in XML (e.g., android:id="@+id/otpContainer")
        // If you don't have an ID, this 'getChildAt' method is fragile but will work if the layout order hasn't changed.

        // Let's try to find the container safely.
        // Based on your XML structure, the OTP container is inside the second main LinearLayout.
        // It is safer to add `android:id="@+id/otpContainer"` to your XML.
        // Assuming you did that, use: val otpContainer = binding.otpContainer

        // If sticking to your current XML without IDs, we use your logic but wrapped in a try-catch to prevent crashes:
        try {
            val rootLayout = binding.btnContinue.parent as LinearLayout // The layout containing the button
            // The OTP container is the 3rd child (index 3) inside that layout based on your previous XML
            val otpContainer = rootLayout.getChildAt(3) as LinearLayout

            otpBoxes = Array(6) { i -> otpContainer.getChildAt(i) as EditText }

            for (i in 0 until 6) {
                otpBoxes[i].addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // Auto-focus next box
                        if (s?.length == 1 && i < 5) {
                            otpBoxes[i + 1].requestFocus()
                        }
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })

                // Handle Backspace
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
            Toast.makeText(this, "Error initializing OTP boxes. Please check XML IDs.", Toast.LENGTH_LONG).show()
        }
    }

    private fun getOtpString(): String {
        val sb = StringBuilder()
        // Safety check in case otpBoxes wasn't initialized
        if (!::otpBoxes.isInitialized) return ""

        for (box in otpBoxes) {
            sb.append(box.text.toString())
        }
        return sb.toString()
    }

    private fun verifyCode(inputCode: String) {
        // COMPARE INPUT WITH GENERATED OTP
        if (inputCode == generatedOtp) {
            Toast.makeText(this, "Verification Successful!", Toast.LENGTH_SHORT).show()

            // 4. Pass ALL data to the next step: InsertPicture
            val intent = Intent(this, InsertPicture::class.java)

            // Re-bundle the data we received so InsertPicture can use it
            intent.putExtra("EXTRA_FIRST", this.intent.getStringExtra("EXTRA_FIRST"))
            intent.putExtra("EXTRA_LAST", this.intent.getStringExtra("EXTRA_LAST"))
            intent.putExtra("EXTRA_BDAY", this.intent.getStringExtra("EXTRA_BDAY"))
            intent.putExtra("EXTRA_EMAIL", this.intent.getStringExtra("EXTRA_EMAIL"))
            intent.putExtra("EXTRA_SCHOOL", this.intent.getStringExtra("EXTRA_SCHOOL"))
            intent.putExtra("EXTRA_COURSE", this.intent.getStringExtra("EXTRA_COURSE"))
            intent.putExtra("EXTRA_PASSWORD", this.intent.getStringExtra("EXTRA_PASSWORD"))

            // Clear back stack so user can't go back to OTP screen
            intent.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT

            startActivity(intent)
            finish() // Close this activity
        } else {
            Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show()
        }
    }
}