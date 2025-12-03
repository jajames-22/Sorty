package com.example.sorty.ui.subjects

import com.example.sorty.R
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

class CourseActivity : AppCompatActivity() {

    // Declare UI components
    private lateinit var btnBack: ImageButton
    private lateinit var tvSubjectName: TextView
    private lateinit var tvSubjectDescription: TextView
    private lateinit var btnEditCourse: TextView

    // Tabs
    private lateinit var tabTodo: TextView
    private lateinit var tabFiles: TextView

    // Content Layouts
    private lateinit var layoutContentTodo: ConstraintLayout
    private lateinit var layoutContentFiles: ConstraintLayout

    // Action Buttons
    private lateinit var btnAddTodo: Button
    private lateinit var btnAddFile: Button
    private lateinit var tvFilterOngoing: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_course)

        // 1. Initialize Views
        initViews()

        // 2. Set up Button Listeners
        setupListeners()

        // 3. Set up Tab Switching Logic
        setupTabs()

        // 4. (Optional) Load initial data (e.g., from Intent)
        loadCourseData()
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

    private fun setupListeners() {
        // Back Button
        btnBack.setOnClickListener {
            finish() // Closes this activity and goes back
        }

        // Edit Course Button
        btnEditCourse.setOnClickListener {
            Toast.makeText(this, "Edit Course clicked", Toast.LENGTH_SHORT).show()
            // TODO: Open an edit dialog or activity here
        }

        // Add To-do Button
        btnAddTodo.setOnClickListener {
            Toast.makeText(this, "Add To-do clicked", Toast.LENGTH_SHORT).show()
            // TODO: Open Add Task logic
        }

        // Add File Button
        btnAddFile.setOnClickListener {
            Toast.makeText(this, "Add File clicked", Toast.LENGTH_SHORT).show()
            // TODO: Open File Picker logic
        }

        // Filter Dropdown
        tvFilterOngoing.setOnClickListener {
            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show()
            // TODO: Show a popup menu for filtering (Ongoing, Completed, etc.)
        }
    }

    private fun setupTabs() {
        // Click Listener for "To-do" Tab
        tabTodo.setOnClickListener {
            updateTabSelection(isTodoSelected = true)
        }

        // Click Listener for "Files" Tab
        tabFiles.setOnClickListener {
            updateTabSelection(isTodoSelected = false)
        }
    }

    private fun updateTabSelection(isTodoSelected: Boolean) {
        if (isTodoSelected) {
            // --- SHOW TODO ---

            // 1. Toggle Layout Visibility
            layoutContentTodo.visibility = View.VISIBLE
            layoutContentFiles.visibility = View.GONE

            // 2. Update Tab Backgrounds
            tabTodo.setBackgroundResource(R.drawable.bg_tab_selected_rounded)
            tabFiles.setBackgroundResource(R.drawable.bg_tab_unselected_rounded) // or 0 for transparent

            // 3. Update Text Colors
            tabTodo.setTextColor(ContextCompat.getColor(this, R.color.black_text))
            tabFiles.setTextColor(ContextCompat.getColor(this, R.color.grey_text))

        } else {
            // --- SHOW FILES ---

            // 1. Toggle Layout Visibility
            layoutContentTodo.visibility = View.GONE
            layoutContentFiles.visibility = View.VISIBLE

            // 2. Update Tab Backgrounds
            tabTodo.setBackgroundResource(R.drawable.bg_tab_unselected_rounded)
            tabFiles.setBackgroundResource(R.drawable.bg_tab_selected_rounded)

            // 3. Update Text Colors
            tabTodo.setTextColor(ContextCompat.getColor(this, R.color.grey_text))
            tabFiles.setTextColor(ContextCompat.getColor(this, R.color.black_text))
        }
    }

    private fun loadCourseData() {
        // This is where you get data sent from the previous screen
        val courseName = intent.getStringExtra("COURSE_NAME") ?: "Course Name"
        val courseDesc = intent.getStringExtra("COURSE_DESC") ?: "No description available."

        tvSubjectName.text = courseName
        tvSubjectDescription.text = courseDesc
    }
}