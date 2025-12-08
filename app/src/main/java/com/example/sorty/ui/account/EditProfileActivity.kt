package com.example.sorty.ui.account

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.sorty.DatabaseHelper
import com.example.sorty.databinding.ActivityEditProfileBinding
import com.yalantis.ucrop.UCrop // Import uCrop
import java.io.File
import java.util.Calendar

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var db: DatabaseHelper
    private var currentUserId: Int = -1
    private var selectedImageUri: String = ""

    // 1. Image Picker: Opens Gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            startCrop(uri) // Send to Cropper immediately
        }
    }

    // 2. Crop Result: Gets the cropped image
    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                // Update UI with the CROPPED image
                selectedImageUri = resultUri.toString()
                binding.ivProfilePreview.setImageURI(resultUri)
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "Crop failed!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        loadUserData()
        setupListeners()
    }

    private fun startCrop(uri: Uri) {
        // Create a unique file name for the cropped image
        val destinationFileName = "cropped_profile_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(cacheDir, destinationFileName))

        // Configure uCrop
        val uCrop = UCrop.of(uri, destinationUri)

        // Optional: Style the crop screen to match your app color
        val options = UCrop.Options()
        options.setCircleDimmedLayer(true) // Makes the crop guide a circle!
        options.setToolbarColor(getColor(com.example.sorty.R.color.primary_green))
        options.setStatusBarColor(getColor(com.example.sorty.R.color.primary_green))
        options.setActiveControlsWidgetColor(getColor(com.example.sorty.R.color.primary_green))

        uCrop.withOptions(options)

        // Launch the cropper
        cropImageLauncher.launch(uCrop.getIntent(this))
    }

    private fun loadUserData() {
        val user = db.getUser()
        if (user != null) {
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

        binding.profilePicContainer.setOnClickListener {
            pickImageLauncher.launch("image/*") // Open Gallery
        }

        binding.etBday.setOnClickListener { showDatePicker() }

        binding.btnSaveChanges.setOnClickListener { saveChanges() }
    }

    private fun saveChanges() {
        if (currentUserId == -1) {
            Toast.makeText(this, "Error: No user found", Toast.LENGTH_SHORT).show()
            return
        }

        val fName = binding.etFirstName.text.toString().trim()
        val lName = binding.etLastName.text.toString().trim()
        val bday = binding.etBday.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val school = binding.etSchool.text.toString().trim()
        val course = binding.etCourse.text.toString().trim()

        if (fName.isEmpty() || lName.isEmpty()) {
            Toast.makeText(this, "Name Required", Toast.LENGTH_SHORT).show()
            return
        }

        val success = db.updateUser(currentUserId, fName, lName, bday, email, school, course, selectedImageUri)

        if (success) {
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
            updatePreviewCard(fName, lName, bday, email, school, course)
        } else {
            Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show()
        }
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
}