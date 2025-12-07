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
    private lateinit var sessionManager: SessionManager

    private var selectedImageUri: Uri? = null

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                selectedImageUri = resultUri
                bind.profileImagePreview.setImageURI(resultUri)
                bind.profileImagePreview.imageTintList = null
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
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

        sessionManager = SessionManager(this)

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false

        // 👇 FIXED: Changed 'bind.main' to 'bind.root'
        ViewCompat.setOnApplyWindowInsetsListener(bind.root) { v, insets ->
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
            val firstName = intent.getStringExtra("EXTRA_FIRST") ?: ""
            val lastName = intent.getStringExtra("EXTRA_LAST") ?: ""
            val bday = intent.getStringExtra("EXTRA_BDAY") ?: ""
            val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""
            val school = intent.getStringExtra("EXTRA_SCHOOL") ?: ""
            val course = intent.getStringExtra("EXTRA_COURSE") ?: ""

            val imageUriString = selectedImageUri?.toString() ?: ""

            // 👇 ENABLED DATABASE SAVING
            val db = DatabaseHelper(this)
            val success = db.insertUser(firstName, lastName, bday, email, school, course, imageUriString)

            if (success) {
                sessionManager.setLogin(true)
                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()

                val homeIntent = Intent(this, Home::class.java)
                homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(homeIntent)
            } else {
                Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationFileName = "cropped_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(cacheDir, destinationFileName))

        val options = UCrop.Options()
        options.setCircleDimmedLayer(true)
        options.setShowCropGrid(false)
        options.setCompressionQuality(90)

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