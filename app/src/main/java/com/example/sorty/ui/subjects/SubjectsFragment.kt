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
import com.example.sorty.databinding.FragmentSubjectsBinding

class SubjectsFragment : Fragment(), AddNewSubject.AddNewSubjectListener {

    private lateinit var bind: FragmentSubjectsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: SubjectsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentSubjectsBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize DB
        dbHelper = DatabaseHelper(requireContext())

        // 2. Setup RecyclerView
        setupRecyclerView()

        // 3. Load Data
        loadSubjects()

        // 4. FAB Click Listener
        bind.addSubjectBtn.setOnClickListener {
            val bottomSheet = AddNewSubject()
            bottomSheet.setAddNewSubjectListener(this)
            bottomSheet.show(parentFragmentManager, "AddNewSubject")
        }
    }

    // 👇 Refresh list when returning from CourseActivity (in case of edits/deletes there)
    override fun onResume() {
        super.onResume()
        loadSubjects()
    }

    private fun setupRecyclerView() {
        // Initialize adapter
        adapter = SubjectsAdapter(emptyList()) { selectedSubject ->

            // 👇 LOGIC TO OPEN COURSE ACTIVITY
            val intent = Intent(requireContext(), CourseActivity::class.java)

            // Pass data to the next screen
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
        val subjects = dbHelper.getAllSubjects()
        adapter.updateData(subjects)

        // Toggle Empty State
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

    // 1. Handle Add
    override fun onSubjectAdded(subjectName: String, subjectDescription: String, colorHex: String) {
        val success = dbHelper.insertSubject(subjectName, subjectDescription, colorHex)

        if (success) {
            Toast.makeText(requireContext(), "Subject Added!", Toast.LENGTH_SHORT).show()
            loadSubjects()
        } else {
            Toast.makeText(requireContext(), "Failed to add subject", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. 👇 FIXED: Handle Update (Required by Interface)
    override fun onSubjectUpdated(id: Int, subjectName: String, subjectDescription: String, colorHex: String) {
        val success = dbHelper.updateSubject(id, subjectName, subjectDescription, colorHex)
        if (success) {
            Toast.makeText(requireContext(), "Subject Updated!", Toast.LENGTH_SHORT).show()
            loadSubjects()
        }
    }

    // 3. 👇 FIXED: Handle Delete (Required by Interface)
    override fun onSubjectDeleted(id: Int) {
        val success = dbHelper.deleteSubject(id)
        if (success) {
            Toast.makeText(requireContext(), "Subject Deleted!", Toast.LENGTH_SHORT).show()
            loadSubjects()
        }
    }
}