package com.example.sorty.ui.subjects

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.sorty.DatabaseHelper // 👈 Import DatabaseHelper
import com.example.sorty.R
import com.example.sorty.ui.home.addNotes

class CourseActivity : AppCompatActivity() {

    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var tvSubjectName: TextView
    private lateinit var tvSubjectDescription: TextView
    private lateinit var btnEditCourse: TextView

    // Tabs
    private lateinit var tabTodo: TextView
    private lateinit var tabFiles: TextView

    // Layouts
    private lateinit var layoutContentTodo: ConstraintLayout
    private lateinit var layoutContentFiles: ConstraintLayout

    // Buttons
    private lateinit var btnAddTodo: Button
    private lateinit var btnAddFile: Button
    private lateinit var tvFilterOngoing: TextView

    // Data Helpers
    private lateinit var dbHelper: DatabaseHelper // 👈 Declare DatabaseHelper
    private var currentCourseId: Int = -1
    private var currentCourseName: String = ""
    private var currentCourseDesc: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        setContentView(R.layout.activity_course)

        // 1. Initialize Database Helper
        dbHelper = DatabaseHelper(this)

        // --- Fix Navigation Bar ---
        window.navigationBarColor = Color.WHITE
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightNavigationBars = true

        // --- Fix Status Bar Overlap ---
        val rootView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        loadCourseData() // Load data from DB
        setupListeners()
        setupTabs()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        tvSubjectName = findViewById(R.id.tv_subject_name)
        tvSubjectDescription = findViewById(R.id.tv_subject_description)
        btnEditCourse = findViewById(R.id.btn_edit_course)

        tabTodo = findViewById(R.id.tab_todo)
        tabFiles = findViewById(R.id.tab_files)

        layoutContentTodo = findViewById(R.id.layout_content_todo)
        layoutContentFiles = findViewById(R.id.layout_content_files)

        btnAddTodo = findViewById(R.id.btn_add_todo)
        btnAddFile = findViewById(R.id.btn_add_file)
        tvFilterOngoing = findViewById(R.id.tv_filter_ongoing)
    }

    private fun loadCourseData() {
        // 1. Get the ID passed from the previous screen
        currentCourseId = intent.getIntExtra("COURSE_ID", -1)

        if (currentCourseId != -1) {
            // 2. Fetch the latest data from the Database
            val subject = dbHelper.getSubjectById(currentCourseId)

            if (subject != null) {
                currentCourseName = subject.name
                currentCourseDesc = subject.description

                // Update UI
                tvSubjectName.text = currentCourseName
                tvSubjectDescription.text = currentCourseDesc
            } else {
                // Fallback if DB fetch fails (e.g. subject deleted)
                tvSubjectName.text = "Error"
                tvSubjectDescription.text = "Subject not found."
            }
        } else {
            // Fallback if no ID passed
            tvSubjectName.text = intent.getStringExtra("COURSE_NAME") ?: "Course Name"
            tvSubjectDescription.text = intent.getStringExtra("COURSE_DESC") ?: "No description"
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnEditCourse.setOnClickListener {
            Toast.makeText(this, "Edit Course clicked", Toast.LENGTH_SHORT).show()
        }

        // --- Open Add Notes Bottom Sheet ---
        btnAddTodo.setOnClickListener {
            val bottomSheet = addNotes()

            // Pass the subject name so it can pre-select it in the dropdown
            val args = Bundle()
            args.putString("arg_preset_subject", currentCourseName)
            bottomSheet.arguments = args

            bottomSheet.show(supportFragmentManager, "AddNotesSheet")
        }

        btnAddFile.setOnClickListener {
            Toast.makeText(this, "Add File clicked", Toast.LENGTH_SHORT).show()
        }

        tvFilterOngoing.setOnClickListener {
            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTabs() {
        tabTodo.setOnClickListener { updateTabSelection(isTodoSelected = true) }
        tabFiles.setOnClickListener { updateTabSelection(isTodoSelected = false) }
    }

    private fun updateTabSelection(isTodoSelected: Boolean) {
        if (isTodoSelected) {
            layoutContentTodo.visibility = View.VISIBLE
            layoutContentFiles.visibility = View.GONE

            tabTodo.setBackgroundResource(R.drawable.bg_tab_selected_rounded)
            tabFiles.setBackgroundResource(R.drawable.bg_tab_unselected_rounded)

            tabTodo.setTextColor(ContextCompat.getColor(this, R.color.black_text))
            tabFiles.setTextColor(ContextCompat.getColor(this, R.color.grey_text))
        } else {
            layoutContentTodo.visibility = View.GONE
            layoutContentFiles.visibility = View.VISIBLE

            tabTodo.setBackgroundResource(R.drawable.bg_tab_unselected_rounded)
            tabFiles.setBackgroundResource(R.drawable.bg_tab_selected_rounded)

            tabTodo.setTextColor(ContextCompat.getColor(this, R.color.grey_text))
            tabFiles.setTextColor(ContextCompat.getColor(this, R.color.black_text))
        }
    }
}