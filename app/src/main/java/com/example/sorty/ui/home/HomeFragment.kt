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
import com.example.sorty.DatabaseHelper // 👈 CHANGED: Import the unified Helper
import com.example.sorty.R
import com.example.sorty.data.models.Task
import com.example.sorty.databinding.FragmentHomeBinding
import com.example.sorty.ui.home.TaskDetailFragment
import com.example.sorty.ui.home.TaskFilter
import com.example.sorty.ui.home.TodoAdapter
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var bind: FragmentHomeBinding
    private lateinit var todoAdapter: TodoAdapter

    // 👇 CHANGED: Renamed variable and type to the unified helper
    private lateinit var dbHelper: DatabaseHelper

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

        // 👇 CHANGED: Initialize the Unified Database Helper
        dbHelper = DatabaseHelper(requireContext())

        setupRecyclerView()
        setupListeners()
        loadTasksFromDatabase() // Load initial tasks based on default filter
    }

    // --- Setup RecyclerView and Adapter ---
    private fun showTaskDetails(taskId: Long) {
        // Perform safety checks before starting a Fragment transaction
        if (isAdded && !childFragmentManager.isStateSaved) {
            val detailFragment = TaskDetailFragment.newInstance(taskId)
            detailFragment.show(childFragmentManager, "TaskDetailSheet")
        }
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            tasks = emptyList(),

            onItemClicked = { task ->
                showTaskDetails(task.id)
            },

            onCheckboxClicked = { task, isChecked ->
                updateTaskCompletion(task, isChecked)
            }
        )

        bind.recyclerViewTodo.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todoAdapter
        }
    }

    // --- Database Update Function (Checkbox) ---
    private fun updateTaskCompletion(task: Task, isCompleted: Boolean) {
        // 👇 CHANGED: Use dbHelper
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
        // 👇 CHANGED: Use dbHelper for all queries
        val tasks = when (currentFilter) {
            TaskFilter.ONGOING -> dbHelper.getOngoingTasks(System.currentTimeMillis())
            TaskFilter.COMPLETED -> dbHelper.getCompletedTasks()
            TaskFilter.MISSED -> dbHelper.getMissedTasks(System.currentTimeMillis())
        }

        if (tasks.isEmpty()) {
            Toast.makeText(context, "No ${currentFilter.name.lowercase()} tasks found.", Toast.LENGTH_SHORT).show()
        }

        todoAdapter.updateTasks(tasks)

        // 2. Apply dynamic fill color to the filter button
        applyFilterButtonColor()

        // 3. Update the UI text of the dropdown button
        bind.filterDropdown.text = currentFilter.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }

    /**
     * Helper function to determine and apply the solid fill color to the filter button.
     */
    private fun applyFilterButtonColor() {
        // Map status to the corresponding placeholder color resource ID
        val colorResId = when (currentFilter) {
            TaskFilter.ONGOING -> R.color.ongoing_border_fill_placeholder
            TaskFilter.COMPLETED -> R.color.completed_border_fill_placeholder
            TaskFilter.MISSED -> R.color.missed_border_fill_placeholder
        }

        // Get the background drawable and apply the color
        val drawable = bind.filterDropdown.background
        if (drawable is GradientDrawable) {
            val colorInt = ContextCompat.getColor(requireContext(), colorResId)
            drawable.setColor(colorInt) // Set the SOLID FILL color
        }
    }

    // --- Button Listeners (Includes FAB and Filter Dropdown) ---
    private fun setupListeners() {
        // FAB Listener
        bind.addNotesBtn.setOnClickListener {
            if (isAdded && !childFragmentManager.isStateSaved) {
                val bottomSheet = addNotes()
                bottomSheet.show(childFragmentManager, "AddNotesSheet")
            }
        }

        // Filter Dropdown Listener
        bind.filterDropdown.setOnClickListener { view ->
            showFilterMenu(view)
        }
    }

    private fun showFilterMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)

        // Add items to the menu
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