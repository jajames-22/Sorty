package com.example.sorty.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.sorty.databinding.FragmentTaskDetailBinding
import com.example.sorty.data.models.Task
import com.example.sorty.DatabaseHelper // 👈 CHANGED: Import unified helper

class TaskDetailFragment : BottomSheetDialogFragment() {

    private lateinit var bind: FragmentTaskDetailBinding

    // 👇 CHANGED: Renamed variable and type
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

        // 👇 CHANGED: Initialize Unified Helper
        dbHelper = DatabaseHelper(requireContext())

        loadTaskDetails()
        setupListeners()
    }

    // --- Data Loading Logic ---
    private fun loadTaskDetails() {
        if (taskId != -1L) {
            // 👇 CHANGED: Use dbHelper
            currentTask = dbHelper.getTaskById(taskId)

            currentTask?.let { task ->
                bind.detailEmojiIcon.text = task.emojiIcon
                bind.detailTaskLabel.text = task.title
                bind.detailTaskDatetime.text = task.getFormattedDateTime()

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

    // --- Button Listeners (Delete and Edit) ---
    private fun setupListeners() {
        bind.detailEditBtn.setOnClickListener {
            openEditMode()
        }

        bind.detailDeleteBtn.setOnClickListener {
            deleteTask()
        }
    }

    private fun deleteTask() {
        currentTask?.let { task ->
            // 👇 CHANGED: Use dbHelper
            val success = dbHelper.deleteTask(task.id)

            if (success) {
                // Inform the HomeFragment to refresh the list
                (parentFragment as? HomeFragment)?.loadTasksFromDatabase()
                Toast.makeText(context, "Task '${task.title}' deleted.", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "Failed to delete task.", Toast.LENGTH_SHORT).show()
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