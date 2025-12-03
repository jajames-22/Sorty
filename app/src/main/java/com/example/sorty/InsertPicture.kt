package com.example.sorty

// --- 1. Standard Android Imports ---
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import java.io.File

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
import com.example.sorty.ui.home.Home

// --- 5. uCrop Import ---
import com.yalantis.ucrop.UCrop

class InsertPicture : AppCompatActivity() {

    private lateinit var bind: ActivityInsertPictureBinding
    private lateinit var sessionManager: SessionManager // Declaration of the Session Manager

    // Variable to hold the final (cropped) image URI
    private var selectedImageUri: Uri? = null

    // ---------------------------------------------------------
    // A. The Cropper Launcher
    // ---------------------------------------------------------
    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                // 1. Store the cropped URI to the state variable
                selectedImageUri = resultUri

                // 2. Display the image in the preview
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
            // Initiate the cropping process immediately
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

        sessionManager = SessionManager(this) // Instantiation of the Session Manager

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
            // Retrieve data passed from the previous Activity
            val firstName = intent.getStringExtra("EXTRA_FIRST") ?: ""
            val lastName = intent.getStringExtra("EXTRA_LAST") ?: ""
            val bday = intent.getStringExtra("EXTRA_BDAY") ?: ""
            val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""
            val school = intent.getStringExtra("EXTRA_SCHOOL") ?: ""
            val course = intent.getStringExtra("EXTRA_COURSE") ?: ""

            val imageUriString = selectedImageUri?.toString() ?: ""

            // NOTE: DatabaseHelper(this) and db.insertUser must be implemented separately.
            // val db = DatabaseHelper(this)
            // val success = db.insertUser(firstName, lastName, bday, email, school, course, imageUriString)

            // Assume the database insertion is successful for demonstrating the session logic
            val success = true

            if (success) {
                // STEP 1: Set the login flag to true to maintain the session
                sessionManager.setLogin(true)

                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()

                // STEP 2: Redirect to the Home Activity and clear the activity back stack
                val homeIntent = Intent(this, Home::class.java) // Ensure 'HomeActivity' is the correct class name
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

        // Configuration for the uCrop UI colors
        options.setStatusBarColor(getColor(R.color.primary_green))
        options.setToolbarColor(getColor(R.color.primary_green))
        options.setToolbarWidgetColor(getColor(R.color.white))
        options.setActiveControlsWidgetColor(getColor(R.color.secondary_yellow))
        options.setRootViewBackgroundColor(getColor(R.color.dark_grey))

        val uCropIntent = UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1000, 1000)
            .withOptions(options)
            .getIntent(this)

        cropImageLauncher.launch(uCropIntent)
    }
}