package com.example.sorty.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sorty.databinding.FragmentHomeBinding
// Assuming the following files are in their respective packages as per structure:
import com.example.sorty.ui.home.TodoAdapter
import com.example.sorty.ui.home.TaskDatabaseHelper
import com.example.sorty.ui.home.TaskDetailFragment
import com.example.sorty.data.models.Task
import com.example.sorty.ui.home.TaskFilter // Assuming TaskFilter is in 'util'
import java.util.Locale
import androidx.core.content.ContextCompat
import android.graphics.drawable.GradientDrawable
import com.example.sorty.R

class HomeFragment : Fragment() {

    private lateinit var bind: FragmentHomeBinding
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var taskDbHelper: TaskDatabaseHelper

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

        // Initialize Database Helper
        taskDbHelper = TaskDatabaseHelper(requireContext())

        setupRecyclerView()
        setupListeners()
        loadTasksFromDatabase() // Load initial tasks based on default filter
    }

    // --- Setup RecyclerView and Adapter ---
    // Inside HomeFragment.kt
private fun showTaskDetails(taskId: Long) {
        // Perform safety checks before starting a Fragment transaction
        // (This prevents the IllegalStateException we fixed earlier)
        if (isAdded && !childFragmentManager.isStateSaved) {

            // 1. Create the instance of the detail fragment, passing the Task ID
            val detailFragment = TaskDetailFragment.newInstance(taskId)

            // 2. Use childFragmentManager to display the Bottom Sheet
            detailFragment.show(childFragmentManager, "TaskDetailSheet")
        }
    }
    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            tasks = emptyList(),

            onItemClicked = { task ->
                // --- UPDATED: Calls the helper function to launch the detail sheet ---
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
        val success = taskDbHelper.updateTaskCompletion(task.id, isCompleted)
        if (success) {
            loadTasksFromDatabase() // Reload list to reflect changes/filtering
            Toast.makeText(context, "${task.title} set to ${if (isCompleted) "Completed" else "Ongoing"}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Error updating task status.", Toast.LENGTH_SHORT).show()
        }
    }



    // --- Core Filtering Logic ---
     fun loadTasksFromDatabase() {
        // 1. Query the database based on the current filter state
        val tasks = when (currentFilter) {
            TaskFilter.ONGOING -> taskDbHelper.getOngoingTasks(System.currentTimeMillis())
            TaskFilter.COMPLETED -> taskDbHelper.getCompletedTasks()
            TaskFilter.MISSED -> taskDbHelper.getMissedTasks(System.currentTimeMillis())
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
            // Using the defined placeholder IDs for the fill color
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
            // Check fragment state for safe transaction
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
            // Map the menu item ID to the TaskFilter enum
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