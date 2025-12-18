package com.example.sorty.ui.subjects

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sorty.DatabaseHelper
import com.example.sorty.SessionManager // 👈 IMPORT THIS
import com.example.sorty.databinding.FragmentSubjectsBinding

class SubjectsFragment : Fragment(), AddNewSubject.AddNewSubjectListener {

    private lateinit var bind: FragmentSubjectsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: SubjectsAdapter
    private lateinit var sessionManager: SessionManager // 1. Add SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentSubjectsBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. Initialize Helpers
        dbHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        loadSubjects()

        bind.addSubjectBtn.setOnClickListener {
            val bottomSheet = AddNewSubject()
            bottomSheet.setAddNewSubjectListener(this)
            bottomSheet.show(parentFragmentManager, "AddNewSubject")
        }
    }

    override fun onResume() {
        super.onResume()
        loadSubjects()
    }

    private fun setupRecyclerView() {
        adapter = SubjectsAdapter(emptyList()) { selectedSubject ->
            val intent = Intent(requireContext(), CourseActivity::class.java)
            intent.putExtra("COURSE_ID", selectedSubject.id)
            intent.putExtra("COURSE_NAME", selectedSubject.name)
            intent.putExtra("COURSE_DESC", selectedSubject.description)
            intent.putExtra("COURSE_COLOR", selectedSubject.color)
            startActivity(intent)
        }

        bind.recyclerViewSubjects.layoutManager = GridLayoutManager(requireContext(), 2)
        bind.recyclerViewSubjects.adapter = adapter
    }

    private fun loadSubjects() {
        // 3. Get Email and Pass to Database
        val currentUserEmail = sessionManager.getEmail() ?: return

        // FIX: Pass email to getAllSubjects
        val subjects = dbHelper.getAllSubjects(currentUserEmail)
        adapter.updateData(subjects)

        if (subjects.isEmpty()) {
            bind.emptyFolderIcon.visibility = View.VISIBLE
            bind.emptyFolderText.visibility = View.VISIBLE
            bind.recyclerViewSubjects.visibility = View.GONE
        } else {
            bind.emptyFolderIcon.visibility = View.GONE
            bind.emptyFolderText.visibility = View.GONE
            bind.recyclerViewSubjects.visibility = View.VISIBLE
        }
    }

    // --- LISTENER IMPLEMENTATIONS ---

    override fun onSubjectAdded(subjectName: String, subjectDescription: String, colorHex: String) {
        val currentUserEmail = sessionManager.getEmail()

        if (currentUserEmail.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Error: Not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. FIX: Pass email as the first argument
        val success = dbHelper.insertSubject(currentUserEmail, subjectName, subjectDescription, colorHex)

        if (success) {
            Toast.makeText(requireContext(), "Subject Added!", Toast.LENGTH_SHORT).show()
            loadSubjects()
        } else {
            Toast.makeText(requireContext(), "Failed to add subject", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSubjectUpdated(id: Int, subjectName: String, subjectDescription: String, colorHex: String) {
        val success = dbHelper.updateSubject(id, subjectName, subjectDescription, colorHex)
        if (success) {
            Toast.makeText(requireContext(), "Subject Updated!", Toast.LENGTH_SHORT).show()
            loadSubjects()
        }
    }

    override fun onSubjectDeleted(id: Int) {
        val success = dbHelper.deleteSubject(id)
        if (success) {
            Toast.makeText(requireContext(), "Subject Deleted!", Toast.LENGTH_SHORT).show()
            loadSubjects()
        }
    }
}