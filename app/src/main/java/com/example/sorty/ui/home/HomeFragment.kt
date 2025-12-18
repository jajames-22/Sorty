package com.example.sorty.ui.home

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sorty.DatabaseHelper
import com.example.sorty.R
import com.example.sorty.SessionManager // Ensure this import exists
import com.example.sorty.data.models.Task
import com.example.sorty.databinding.FragmentHomeBinding
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var bind: FragmentHomeBinding
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager // 1. Add SessionManager

    // Track the currently active filter (Default to ONGOING)
    private var currentFilter: TaskFilter = TaskFilter.ONGOING

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentHomeBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext()) // 2. Initialize SessionManager

        setupRecyclerView()
        setupListeners()
        loadTasksFromDatabase()
    }

    // --- Setup RecyclerView and Adapter ---
    private fun showTaskDetails(taskId: Long) {
        if (isAdded && !childFragmentManager.isStateSaved) {
            val detailFragment = TaskDetailFragment.newInstance(taskId)
            detailFragment.show(childFragmentManager, "TaskDetailSheet")
        }
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            tasks = emptyList(),
            onItemClicked = { task -> showTaskDetails(task.id) },
            onCheckboxClicked = { task, isChecked -> updateTaskCompletion(task, isChecked) }
        )

        bind.recyclerViewTodo.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todoAdapter
        }
    }

    // --- Database Update Function (Checkbox) ---
    private fun updateTaskCompletion(task: Task, isCompleted: Boolean) {
        val success = dbHelper.updateTaskCompletion(task.id, isCompleted)

        if (success) {
            loadTasksFromDatabase() // Reload list to reflect changes/filtering
            Toast.makeText(context, "${task.title} set to ${if (isCompleted) "Completed" else "Ongoing"}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Error updating task status.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Core Filtering Logic ---
    fun loadTasksFromDatabase() {
        // 3. Get the current user's email
        val currentUserEmail = sessionManager.getEmail() ?: ""

        if (currentUserEmail.isEmpty()) {
            // Safety check: If no email found, just clear the list or handle logout
            todoAdapter.updateTasks(emptyList())
            return
        }

        // 4. Pass the EMAIL to the database helper functions
        val tasks = when (currentFilter) {
            // FIX: Pass (email, time) instead of just (time)
            TaskFilter.ONGOING -> dbHelper.getOngoingTasks(currentUserEmail, System.currentTimeMillis())

            // FIX: Pass (email)
            TaskFilter.COMPLETED -> dbHelper.getCompletedTasks(currentUserEmail)

            // FIX: Pass (email, time)
            TaskFilter.MISSED -> dbHelper.getMissedTasks(currentUserEmail, System.currentTimeMillis())
        }

        // Toggle Empty State Views Visibility
        if (tasks.isEmpty()) {
            bind.emptyNoteIcon.visibility = View.VISIBLE
            bind.emptyNoteText.visibility = View.VISIBLE
            bind.recyclerViewTodo.visibility = View.GONE
        } else {
            bind.emptyNoteIcon.visibility = View.GONE
            bind.emptyNoteText.visibility = View.GONE
            bind.recyclerViewTodo.visibility = View.VISIBLE
        }

        todoAdapter.updateTasks(tasks)

        applyFilterButtonColor()
        bind.filterDropdown.text = currentFilter.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }

    private fun applyFilterButtonColor() {
        val colorResId = when (currentFilter) {
            TaskFilter.ONGOING -> R.color.ongoing_border_fill_placeholder
            TaskFilter.COMPLETED -> R.color.completed_border_fill_placeholder
            TaskFilter.MISSED -> R.color.missed_border_fill_placeholder
        }

        val drawable = bind.filterDropdown.background
        if (drawable is GradientDrawable) {
            val colorInt = ContextCompat.getColor(requireContext(), colorResId)
            drawable.setColor(colorInt)
        }
    }

    private fun setupListeners() {
        bind.addNotesBtn.setOnClickListener {
            if (isAdded && !childFragmentManager.isStateSaved) {
                val bottomSheet = addNotes()
                bottomSheet.show(childFragmentManager, "AddNotesSheet")
            }
        }

        bind.filterDropdown.setOnClickListener { view ->
            showFilterMenu(view)
        }
    }

    private fun showFilterMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)

        popup.menu.apply {
            add(0, 1, 0, "Ongoing")
            add(0, 2, 1, "Completed")
            add(0, 3, 2, "Missed")
        }

        popup.setOnMenuItemClickListener { menuItem ->
            val selectedFilter = when (menuItem.itemId) {
                1 -> TaskFilter.ONGOING
                2 -> TaskFilter.COMPLETED
                3 -> TaskFilter.MISSED
                else -> currentFilter
            }

            if (selectedFilter != currentFilter) {
                currentFilter = selectedFilter
                loadTasksFromDatabase() // Reload tasks with the new filter
            }
            true
        }
        popup.show()
    }
}