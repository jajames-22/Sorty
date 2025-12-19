package com.example.sorty.ui.account

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.sorty.DatabaseHelper
import com.example.sorty.GMailSender
import com.example.sorty.R
import com.example.sorty.databinding.ActivityEditProfileBinding
import com.example.sorty.ui.auth.EmailConfirmActivity
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var db: DatabaseHelper
    private var currentUserId: Int = -1
    private var selectedImageUri: String = ""
    private var originalUser: com.example.sorty.data.models.User? = null

    // --- Image Picker & Cropper ---
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) startCrop(uri)
    }

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                selectedImageUri = resultUri.toString()
                binding.ivProfilePreview.setImageURI(resultUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHelper(this)
        loadUserData()
        setupListeners()
    }

    private fun loadUserData() {
        val user = db.getUser()
        if (user != null) {
            originalUser = user
            currentUserId = user.id
            selectedImageUri = user.imageUri ?: ""

            binding.etFirstName.setText(user.firstName)
            binding.etLastName.setText(user.lastName)
            binding.etBday.setText(user.birthday)
            binding.etEmail.setText(user.email)
            binding.etSchool.setText(user.school)
            binding.etCourse.setText(user.course)

            updatePreviewCard(user.firstName, user.lastName ?: "", user.birthday, user.email, user.school, user.course)

            if (!user.imageUri.isNullOrEmpty()) {
                binding.ivProfilePreview.setImageURI(Uri.parse(user.imageUri))
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.profilePicContainer.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.etBday.setOnClickListener { showDatePicker() }
        binding.btnSaveChanges.setOnClickListener { validateInputsAndConfirm() }
    }

    private fun validateInputsAndConfirm() {
        if (currentUserId == -1) return

        val fName = binding.etFirstName.text.toString().trim()

        // Only First Name is mandatory
        if (fName.isEmpty()) {
            Toast.makeText(this, "First Name is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!hasChanges()) {
            Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show()
            return
        }

        showConfirmationDialog()
    }

    private fun showConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_save, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)

        tvMessage.text = "Do you want to save the changes to your profile?"

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            dialog.dismiss()
            performDatabaseUpdate()
        }
        dialog.show()
    }

    private fun performDatabaseUpdate() {
        val fName = binding.etFirstName.text.toString().trim()
        val lName = binding.etLastName.text.toString().trim()
        val bday = binding.etBday.text.toString().trim()
        val newEmail = binding.etEmail.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()
        val course = binding.etCourse.text.toString().trim()

        val emailChanged = newEmail != originalUser?.email

        val success = db.updateUser(currentUserId, fName, lName, bday, newEmail, school, course, selectedImageUri)

        if (success) {
            if (emailChanged) {
                sendNewVerification(fName, lName, bday, newEmail, school, course)
            } else {
                showSuccessModal(fName, lName, bday, newEmail, school, course)
            }
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendNewVerification(fName: String, lName: String, bday: String, email: String, school: String, course: String) {
        val otp = (100000..999999).random().toString()
        Toast.makeText(this, "Sending verification code...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
            val emailBody = "<h2>Verification Code</h2><p>Your Sorty update code is: <b>$otp</b></p>"
            val sent = GMailSender.sendEmail(email, "Sorty Email Verification", emailBody)

            withContext(Dispatchers.Main) {
                if (sent) {
                    showEmailVerificationNotice(fName, lName, bday, email, school, course, otp)
                } else {
                    Toast.makeText(this@EditProfileActivity, "Failed to send email", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showEmailVerificationNotice(fName: String, lName: String, bday: String, email: String, school: String, course: String, otp: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verify New Email")
        builder.setMessage("A verification code was sent to $email. Please verify to finish.")
        builder.setCancelable(false)
        builder.setPositiveButton("Verify Now") { _, _ ->
            val intent = Intent(this, EmailConfirmActivity::class.java).apply {
                putExtra("EXTRA_OTP", otp)
                putExtra("EXTRA_FIRST", fName)
                putExtra("EXTRA_LAST", lName)
                putExtra("EXTRA_BDAY", bday)
                putExtra("EXTRA_EMAIL", email)
                putExtra("EXTRA_SCHOOL", school)
                putExtra("EXTRA_COURSE", course)
            }
            startActivity(intent)
            finish()
        }
        builder.show()
    }

    private fun hasChanges(): Boolean {
        val user = originalUser ?: return true
        return binding.etFirstName.text.toString().trim() != user.firstName ||
                binding.etLastName.text.toString().trim() != (user.lastName ?: "") ||
                binding.etEmail.text.toString().trim() != user.email ||
                binding.etBday.text.toString().trim() != user.birthday ||
                binding.etSchool.text.toString().trim() != user.school ||
                binding.etCourse.text.toString().trim() != user.course ||
                selectedImageUri != (user.imageUri ?: "")
    }

    private fun showSuccessModal(fName: String, lName: String, bday: String, email: String, school: String, course: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_success, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<Button>(R.id.btn_dialog_ok).setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d -> binding.etBday.setText("$d/${m + 1}/$y") },
            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updatePreviewCard(fName: String, lName: String, bday: String, email: String, school: String, course: String) {
        binding.tvPreviewName.text = "$fName $lName"
        binding.tvPreviewBday.text = "Born: $bday"
        binding.tvPreviewEmail.text = email
        binding.tvPreviewSchool.text = school
        binding.tvPreviewCourse.text = course
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))
        val uCrop = UCrop.of(uri, destinationUri).withOptions(UCrop.Options().apply {
            setCircleDimmedLayer(true)
            setToolbarColor(ContextCompat.getColor(this@EditProfileActivity, R.color.primary_green))
        })
        cropImageLauncher.launch(uCrop.getIntent(this))
    }
}