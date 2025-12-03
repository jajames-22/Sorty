package com.example.sorty.ui.home


/**
 * Defines the filtering states for the To-Do list displayed on the Home screen.
 */
enum class TaskFilter {
    ONGOING,  // Incomplete tasks with a due date in the future or no due date
    COMPLETED, // Tasks that have been marked as done
    MISSED     // Incomplete tasks that are past their due date (Overdue)
}