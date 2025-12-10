package com.example.sorty.ui.account

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.sorty.DatabaseHelper
import com.example.sorty.R
import com.example.sorty.databinding.ActivityEditProfileBinding
// Ensure you have this import for the User object type
// import com.example.sorty.data.models.User
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.Calendar

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var db: DatabaseHelper
    private var currentUserId: Int = -1
    private var selectedImageUri: String = ""

    // 👇 1. Store the original user data to compare later
    // Replace 'Any' with your actual User class type (e.g., com.example.sorty.data.models.User)
    private var originalUser: com.example.sorty.data.models.User? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            startCrop(uri)
        }
    }

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                selectedImageUri = resultUri.toString()
                binding.ivProfilePreview.setImageURI(resultUri)
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "Crop failed!", Toast.LENGTH_SHORT).show()
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

    private fun startCrop(uri: Uri) {
        val destinationFileName = "cropped_profile_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(cacheDir, destinationFileName))
        val options = UCrop.Options()
        options.setCircleDimmedLayer(true)
        options.setToolbarColor(ContextCompat.getColor(this, R.color.primary_green))
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_green))
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.secondary_yellow))
        options.setToolbarWidgetColor(Color.WHITE)
        val uCrop = UCrop.of(uri, destinationUri).withOptions(options)
        cropImageLauncher.launch(uCrop.getIntent(this))
    }

    private fun loadUserData() {
        val user = db.getUser()
        if (user != null) {
            // 👇 Store original state
            originalUser = user

            currentUserId = user.id
            selectedImageUri = user.imageUri ?: ""

            binding.etFirstName.setText(user.firstName)
            binding.etLastName.setText(user.lastName)
            binding.etBday.setText(user.birthday)
            binding.etEmail.setText(user.email)
            binding.etSchool.setText(user.school)
            binding.etCourse.setText(user.course)

            updatePreviewCard(user.firstName, user.lastName, user.birthday, user.email, user.school, user.course)

            if (!user.imageUri.isNullOrEmpty()) {
                try {
                    binding.ivProfilePreview.setImageURI(Uri.parse(user.imageUri))
                } catch (e: Exception) { }
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.profilePicContainer.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.etBday.setOnClickListener { showDatePicker() }
        binding.btnSaveChanges.setOnClickListener { validateInputsAndConfirm() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val date = "$day/${month + 1}/$year"
                binding.etBday.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updatePreviewCard(fName: String, lName: String, bday: String, email: String, school: String, course: String) {
        binding.tvPreviewName.text = "$fName $lName"
        binding.tvPreviewBday.text = "Born: $bday"
        binding.tvPreviewEmail.text = email
        binding.tvPreviewSchool.text = school
        binding.tvPreviewCourse.text = course
    }

    // ----------------------------------------------------------------------
    // 👇 NEW LOGIC FLOW
    // ----------------------------------------------------------------------

    // Step 1: Check for Changes
    private fun hasChanges(): Boolean {
        val user = originalUser ?: return true // If no original user, assume changes

        val newFirstName = binding.etFirstName.text.toString().trim()
        val newLastName = binding.etLastName.text.toString().trim()
        val newBday = binding.etBday.text.toString().trim()
        val newEmail = binding.etEmail.text.toString().trim()
        val newSchool = binding.etSchool.text.toString().trim()
        val newCourse = binding.etCourse.text.toString().trim()

        val originalUri = user.imageUri ?: ""

        return newFirstName != user.firstName ||
                newLastName != user.lastName ||
                newBday != user.birthday ||
                newEmail != user.email ||
                newSchool != user.school ||
                newCourse != user.course ||
                selectedImageUri != originalUri
    }

    // Step 2: Validate Inputs
    private fun validateInputsAndConfirm() {
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: No user found", Toast.LENGTH_SHORT).show()
            return
        }

        // 👇 Check if changes exist
        if (!hasChanges()) {
            Toast.makeText(this, "Please make changes to save", Toast.LENGTH_SHORT).show()
            return
        }

        val fName = binding.etFirstName.text.toString().trim()
        val lName = binding.etLastName.text.toString().trim()

        if (fName.isEmpty() || lName.isEmpty()) {
            Toast.makeText(this, "Name Required", Toast.LENGTH_SHORT).show()
            return
        }

        // If valid and changed, show Confirmation Modal
        showConfirmationDialog()
    }

    // Step 3: Show Confirmation Dialog
    private fun showConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_save, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            performDatabaseUpdate()
        }

        dialog.show()
    }

    // Step 4: Actual Database Update
    private fun performDatabaseUpdate() {
        val fName = binding.etFirstName.text.toString().trim()
        val lName = binding.etLastName.text.toString().trim()
        val bday = binding.etBday.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()
        val course = binding.etCourse.text.toString().trim()

        val success = db.updateUser(currentUserId, fName, lName, bday, email, school, course, selectedImageUri)

        if (success) {
            showSuccessModal(fName, lName, bday, email, school, course)
        } else {
            Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show()
        }
    }

    // Step 5: Show Success Modal & Finish
    private fun showSuccessModal(fName: String, lName: String, bday: String, email: String, school: String, course: String) {

        // 👇 FIX 1: Inflate 'dialog_success', NOT 'dialog_confirm_save'
        val dialogView = layoutInflater.inflate(R.layout.dialog_success, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        // 👇 FIX 2: Use the ID from dialog_success.xml (btn_dialog_ok)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_dialog_ok)

        btnOk.setOnClickListener {
            dialog.dismiss()

            // Update UI (Visual confirmation before closing)
            updatePreviewCard(fName, lName, bday, email, school, course)

            // 👇 FIX 3: Go back to the previous activity
            finish()
        }

        dialog.show()
    }
}