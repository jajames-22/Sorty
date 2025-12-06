package com.example.sorty.ui.subjects

import android.content.Intent // 👈 IMPORT THIS
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

    private fun setupRecyclerView() {
        // Initialize adapter
        adapter = SubjectsAdapter(emptyList()) { selectedSubject ->

            // 👇 LOGIC TO OPEN COURSE ACTIVITY 👇
            val intent = Intent(requireContext(), CourseActivity::class.java)

            // Pass data to the next screen
            intent.putExtra("COURSE_ID", selectedSubject.id)
            intent.putExtra("COURSE_NAME", selectedSubject.name)
            intent.putExtra("COURSE_DESC", selectedSubject.description)
            intent.putExtra("COURSE_COLOR", selectedSubject.color) // Optional: Pass color if you want to theme the next page

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

    // Update the override function signature:
    override fun onSubjectAdded(subjectName: String, subjectDescription: String, colorHex: String) {

        // Pass the description to the database
        val success = dbHelper.insertSubject(subjectName, subjectDescription, colorHex)

        if (success) {
            Toast.makeText(requireContext(), "Subject Added!", Toast.LENGTH_SHORT).show()
            loadSubjects()
        } else {
            Toast.makeText(requireContext(), "Failed to add subject", Toast.LENGTH_SHORT).show()
        }
    }
}