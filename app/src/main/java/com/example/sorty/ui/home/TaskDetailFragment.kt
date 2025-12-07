package com.example.sorty.ui.home

import android.app.AlertDialog // 👈 Import for the Modal
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.sorty.databinding.FragmentTaskDetailBinding
import com.example.sorty.data.models.Task
import com.example.sorty.DatabaseHelper

class TaskDetailFragment : BottomSheetDialogFragment() {

    private lateinit var bind: FragmentTaskDetailBinding
    private lateinit var dbHelper: DatabaseHelper

    private var taskId: Long = -1L
    private var currentTask: Task? = null

    companion object {
        private const val ARG_TASK_ID = "task_id"
        fun newInstance(taskId: Long): TaskDetailFragment {
            val fragment = TaskDetailFragment()
            val args = Bundle()
            args.putLong(ARG_TASK_ID, taskId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskId = arguments?.getLong(ARG_TASK_ID) ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())

        loadTaskDetails()
        setupListeners()
    }

    // --- Data Loading Logic ---
    private fun loadTaskDetails() {
        if (taskId != -1L) {
            currentTask = dbHelper.getTaskById(taskId)

            currentTask?.let { task ->
                bind.detailEmojiIcon.text = task.emojiIcon
                bind.detailTaskLabel.text = task.title
                bind.detailTaskDatetime.text = task.getFormattedDateTime()

                // 👇 NEW: Set Subject Name
                val subjectName = if (task.category.isNullOrEmpty()) "None" else task.category
                bind.detailSubjectName.text = "Subject: $subjectName"

                // Description
                val description = task.content
                if (description.isNullOrEmpty()) {
                    bind.detailDescriptionDisplay.text = "No Description"
                } else {
                    bind.detailDescriptionDisplay.text = description
                }
            } ?: run {
                Toast.makeText(context, "Task not found.", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        } else {
            dismiss()
        }
    }

    private fun setupListeners() {
        bind.detailEditBtn.setOnClickListener {
            openEditMode()
        }

        bind.detailDeleteBtn.setOnClickListener {
            // 👇 Show Modal instead of deleting immediately
            showDeleteConfirmation()
        }
    }

    // 👇 NEW: Confirmation Modal Logic
    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteTask() // Call the actual delete function if confirmed
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteTask() {
        currentTask?.let { task ->
            val success = dbHelper.deleteTask(task.id)

            if (success) {
                // Inform the HomeFragment to refresh the list
                (parentFragment as? HomeFragment)?.loadTasksFromDatabase()

                // Inform CourseActivity to refresh if opened from there
                // (Note: This assumes your Activity is named CourseActivity if you are reusing this fragment there)
                // (activity as? com.example.sorty.ui.subjects.CourseActivity)?.loadCourseTasks()

                Toast.makeText(context, "To-Do deleted", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "Failed to delete to-do.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openEditMode() {
        dismiss()

        currentTask?.let { task ->
            val editFragment = addNotes.newInstance(task.id)
            editFragment.show(parentFragmentManager, "EditNotesSheet")
        }
    }
}