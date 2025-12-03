package com.example.sorty.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.example.sorty.ui.home.TaskDatabaseHelper
import com.example.sorty.databinding.FragmentTaskDetailBinding
import com.example.sorty.data.models.Task

class TaskDetailFragment : BottomSheetDialogFragment() {

    private lateinit var bind: FragmentTaskDetailBinding
    private lateinit var taskDbHelper: TaskDatabaseHelper
    private var taskId: Long = -1L // Use Long suffix
    private var currentTask: Task? = null

    // Companion object for creating a new instance with arguments
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
        // Retrieve the Task ID passed from HomeFragment
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
        taskDbHelper = TaskDatabaseHelper(requireContext())

        loadTaskDetails()
        setupListeners()
    }

    // --- Data Loading Logic ---
    private fun loadTaskDetails() {
        if (taskId != -1L) {
            // Call the new helper function to get task data
            currentTask = taskDbHelper.getTaskById(taskId)

            currentTask?.let { task ->
                // UI POPULATION
                bind.detailEmojiIcon.text = task.emojiIcon
                bind.detailTaskLabel.text = task.title

                // Set the formatted date/time
                bind.detailTaskDatetime.text = task.getFormattedDateTime()

                // CRITICAL FIX: Handle the Description (content)
                val description = task.content

                if (description.isNullOrEmpty()) {
                    bind.detailDescriptionDisplay.text = "No Description"
                    // Optionally set a light color for 'No Description'
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

    // --- Button Listeners (Delete and Edit) ---
    private fun setupListeners() {
        bind.detailEditBtn.setOnClickListener {
            // Close this detail view and open the Add Notes screen in EDIT mode
            openEditMode()
        }

        bind.detailDeleteBtn.setOnClickListener {
            // Execute the delete operation
            deleteTask()
        }
    }

    private fun deleteTask() {
        // Tiyaking may task bago mag-delete
        currentTask?.let { task ->
            val success = taskDbHelper.deleteTask(task.id)

            if (success) {
                // Inform the HomeFragment to refresh the list
                (parentFragment as? HomeFragment)?.loadTasksFromDatabase()
                Toast.makeText(context, "Task '${task.title}' deleted.", Toast.LENGTH_SHORT).show()
                dismiss() // Close the dialog
            } else {
                Toast.makeText(context, "Failed to delete task.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openEditMode() {
        // HAKBANG 1: Isara ang Task Detail Fragment
        dismiss()

        currentTask?.let { task ->
            // This call is now valid because the static newInstance function exists.
            val editFragment = addNotes.newInstance(task.id)
            editFragment.show(parentFragmentManager, "EditNotesSheet")
        }
    }
}