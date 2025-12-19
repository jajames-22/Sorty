package com.example.sorty.ui.subjects

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sorty.DatabaseHelper
import com.example.sorty.SessionManager
import com.example.sorty.databinding.ActivityArchivesBinding

class ArchivesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArchivesBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: SubjectsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityArchivesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Initialize Helpers
        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        enableEdgeToEdge()
        supportActionBar?.hide()

        // 2. UI Styling
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 3. Setup RecyclerView and Load Data
        setupRecyclerView()
        loadArchivedSubjects()

        binding.btnBackArchives.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        // We reuse the SubjectsAdapter. Clicking an archive leads to CourseActivity.
        adapter = SubjectsAdapter(emptyList()) { selectedSubject ->
            val intent = Intent(this, CourseActivity::class.java)
            intent.putExtra("COURSE_ID", selectedSubject.id)
            intent.putExtra("COURSE_NAME", selectedSubject.name)
            intent.putExtra("COURSE_DESC", selectedSubject.description)
            intent.putExtra("COURSE_COLOR", selectedSubject.color)
            startActivity(intent)
        }

        binding.recyclerViewSubjects.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewSubjects.adapter = adapter
    }

    private fun loadArchivedSubjects() {
        val currentUserEmail = sessionManager.getEmail() ?: return

        // Call the new function we discussed for DatabaseHelper
        val archivedSubjects = dbHelper.getArchivedSubjects(currentUserEmail)

        adapter.updateData(archivedSubjects)

        // 4. Handle Empty State visibility
        if (archivedSubjects.isEmpty()) {
            binding.emptyFolderIcon.visibility = View.VISIBLE
            binding.emptyFolderText.visibility = View.VISIBLE
            binding.recyclerViewSubjects.visibility = View.GONE
        } else {
            binding.emptyFolderIcon.visibility = View.GONE
            binding.emptyFolderText.visibility = View.GONE
            binding.recyclerViewSubjects.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list if user unarchives something inside CourseActivity
        loadArchivedSubjects()
    }
}