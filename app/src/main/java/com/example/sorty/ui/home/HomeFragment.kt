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
import com.example.sorty.data.models.Task
import com.example.sorty.databinding.FragmentHomeBinding
import com.example.sorty.ui.home.TaskDetailFragment
import com.example.sorty.ui.home.TaskFilter
import com.example.sorty.ui.home.TodoAdapter
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var bind: FragmentHomeBinding
    private lateinit var todoAdapter: TodoAdapter
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

        dbHelper = DatabaseHelper(requireContext())

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
        // 1. Query the database based on the current filter state
        val tasks = when (currentFilter) {
            TaskFilter.ONGOING -> dbHelper.getOngoingTasks(System.currentTimeMillis())
            TaskFilter.COMPLETED -> dbHelper.getCompletedTasks()
            TaskFilter.MISSED -> dbHelper.getMissedTasks(System.currentTimeMillis())
        }

        // 👇 UPDATED: Toggle Empty State Views Visibility
        if (tasks.isEmpty()) {
            // Show Empty State
            bind.emptyNoteIcon.visibility = View.VISIBLE
            bind.emptyNoteText.visibility = View.VISIBLE

            // Hide RecyclerView (Cleaner look)
            bind.recyclerViewTodo.visibility = View.GONE
        } else {
            // Hide Empty State
            bind.emptyNoteIcon.visibility = View.GONE
            bind.emptyNoteText.visibility = View.GONE

            // Show RecyclerView
            bind.recyclerViewTodo.visibility = View.VISIBLE
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

    // --- Button Listeners ---
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