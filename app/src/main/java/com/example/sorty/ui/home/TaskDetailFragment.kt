package com.example.sorty.ui.home

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.sorty.databinding.FragmentTaskDetailBinding
import com.example.sorty.data.models.Task
import com.example.sorty.DatabaseHelper
import com.example.sorty.R
import com.example.sorty.ui.subjects.CourseActivity // Import CourseActivity for casting

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

    private fun loadTaskDetails() {
        if (taskId != -1L) {
            currentTask = dbHelper.getTaskById(taskId)

            currentTask?.let { task ->
                bind.detailEmojiIcon.text = task.emojiIcon
                bind.detailTaskLabel.text = task.title
                bind.detailTaskDatetime.text = task.getFormattedDateTime()

                val subjectName = if (task.category.isNullOrEmpty()) "None" else task.category
                bind.detailSubjectName.text = "Subject: $subjectName"

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
            showDeleteConfirmation()
        }
    }

    // 👇 UPDATED: Uses the Custom XML Layout
    private fun showDeleteConfirmation() {
        // 1. Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_save, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()

        // 2. Transparent background for rounded corners
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 3. Find Views
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

        // 4. Customize for "Delete" context
        tvTitle.text = "Delete Note"
        tvMessage.text = "Are you sure you want to delete this note?"

        // Change Confirm Button to Red/Delete
        btnConfirm.text = "Delete"
        btnConfirm.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_dark)

        // 5. Button Logic
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            deleteTask()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteTask() {
        currentTask?.let { task ->
            val success = dbHelper.deleteTask(task.id)

            if (success) {
                // Refresh HomeFragment if opened from there
                (parentFragment as? HomeFragment)?.loadTasksFromDatabase()

                // Refresh CourseActivity if opened from there
                (activity as? CourseActivity)?.loadCourseTasks()

                Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "Failed to delete note.", Toast.LENGTH_SHORT).show()
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