package com.example.sorty

// --- 1. Standard Android Imports ---
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import java.io.File // Required for creating the crop destination file

// --- 2. Activity & UI Imports ---
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// --- 3. Photo Picker & Result Imports ---
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

// --- 4. Binding Import ---
import com.example.sorty.databinding.ActivityInsertPictureBinding

// --- 5. uCrop Import ---
import com.yalantis.ucrop.UCrop

class InsertPicture : AppCompatActivity() {

    private lateinit var bind: ActivityInsertPictureBinding

    // Variable to hold the FINAL (cropped) image URI
    private var selectedImageUri: Uri? = null

    // ---------------------------------------------------------
    // A. The Cropper Launcher
    // ---------------------------------------------------------
    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                // 1. Save the cropped URI to our variable
                selectedImageUri = resultUri

                // 2. Display the image
                bind.profileImagePreview.setImageURI(resultUri)
                bind.profileImagePreview.imageTintList = null
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------------------------------------------------
    // B. The Photo Picker Launcher
    // ---------------------------------------------------------
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // Instead of displaying immediately, we send it to the cropper
            startCrop(uri)
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bind = ActivityInsertPictureBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(bind.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()
        setupListeners()
    }

    private fun setupListeners() {
        bind.backbtnpic.setOnClickListener {
            finish()
        }

        bind.btnChangePhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        bind.buttonContinue2.setOnClickListener {
            // Retrieve data passed from CreateAccount.kt
            val firstName = intent.getStringExtra("EXTRA_FIRST") ?: ""
            val lastName = intent.getStringExtra("EXTRA_LAST") ?: ""
            val bday = intent.getStringExtra("EXTRA_BDAY") ?: ""
            val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""
            val school = intent.getStringExtra("EXTRA_SCHOOL") ?: ""
            val course = intent.getStringExtra("EXTRA_COURSE") ?: ""

            val imageUriString = selectedImageUri?.toString() ?: ""

            val db = DatabaseHelper(this)
            val success = db.insertUser(firstName, lastName, bday, email, school, course, imageUriString)

            if (success) {
                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                val homeIntent = Intent(this, Home::class.java)
                homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(homeIntent)
            } else {
                Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------------------------------------------------------
    // C. Helper Function to configure and start uCrop
    // ---------------------------------------------------------
    private fun startCrop(uri: Uri) {
        val destinationFileName = "cropped_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(cacheDir, destinationFileName))

        val options = UCrop.Options()
        options.setCircleDimmedLayer(true)
        options.setShowCropGrid(false)
        options.setCompressionQuality(90)

        // 👇👇👇 COLORS CONFIGURATION 👇👇👇

        // 1. Backgrounds: Set Status Bar and Toolbar to Green
        options.setStatusBarColor(getColor(R.color.primary_green))
        options.setToolbarColor(getColor(R.color.primary_green))

        // 2. Text & Icons: Set Toolbar Text and Buttons (Check/Cancel) to White
        options.setToolbarWidgetColor(getColor(R.color.white))

        // 3. Active Tools: Change the "Orange" sliders/controls to Yellow
        options.setActiveControlsWidgetColor(getColor(R.color.secondary_yellow))

        // 4. Background: Set the main background to dark grey
        options.setRootViewBackgroundColor(getColor(R.color.dark_grey))

        // 👆👆👆 COLORS CONFIGURATION END 👆👆👆

        val uCropIntent = UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1000, 1000)
            .withOptions(options)
            .getIntent(this)

        cropImageLauncher.launch(uCropIntent)
    }
}