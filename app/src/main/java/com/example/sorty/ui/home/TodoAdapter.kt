package com.example.sorty.ui.home
// Note: Changed package to 'adapters' for better structure

import android.content.Context
import android.graphics.drawable.GradientDrawable // REQUIRED for changing shape color at runtime
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat // REQUIRED for getColor()
import androidx.recyclerview.widget.RecyclerView
import com.example.sorty.R
import com.example.sorty.data.models.Task

class TodoAdapter(
    // The list of tasks to be displayed
    private var tasks: List<Task>,
    // Lambda function to handle full item clicks (for showing details)
    private val onItemClicked: (Task) -> Unit,
    // Lambda function to handle checkbox clicks (for updating completion status)
    private val onCheckboxClicked: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TaskViewHolder>() {

    // --- 1. ViewHolder: Holds references to views from list_item_todo.xml ---
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Ensure these IDs match your list_item_todo.xml
        val innerConstraintLayout: View = itemView.findViewById(R.id.innerConstraintLayout)
        val titleTextView: TextView = itemView.findViewById(R.id.text_task_label)
        val dateTextView: TextView = itemView.findViewById(R.id.text_task_datetime)
        val emojiTextView: TextView = itemView.findViewById(R.id.text_emoji_icon)
        val completionCheckBox: CheckBox = itemView.findViewById(R.id.checkbox_task_complete)

        init {
            // Click listener for viewing task details
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(tasks[position])
                }
            }
        }
    }

    // --- 2. Links the data model to the layout (XML) ---
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            // Inflates the layout template
            .inflate(R.layout.list_item_todo, parent, false)
        return TaskViewHolder(view)
    }

    // --- 3. Binds the data to the views and applies dynamic styling ---
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        val context = holder.itemView.context

        // Set the core task data
        holder.titleTextView.text = task.title
        holder.dateTextView.text = task.getFormattedDateTime()
        holder.emojiTextView.text = task.emojiIcon

        // Apply dynamic color coding to the border/background
        val colorResId = getBorderColorResource(task)
        // Pass the inner ConstraintLayout directly for color update
        applyDynamicBorderColor(context, holder.innerConstraintLayout, colorResId)


        // Handling Checkbox State
        holder.completionCheckBox.setOnCheckedChangeListener(null) // Clears the previous listener
        holder.completionCheckBox.isChecked = task.isCompleted

        // Resets the listener to update the data model upon interaction
        holder.completionCheckBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckboxClicked(task, isChecked)
        }
    }

    /**
     * Determines the correct color resource based on task status (Ongoing, Missed, Completed).
     */
    private fun getBorderColorResource(task: Task): Int {
        val currentTime = System.currentTimeMillis()

        return when {
            // Completed: D3E4D4 (Light Green)
            task.isCompleted -> R.color.completed_border

            // Missed (Overdue): FFA1A1 (Light Red/Pink) - Incomplete AND Due Date is in the past
            task.dueDate != 0L && task.dueDate < currentTime -> R.color.missed_border

            // Ongoing: FFCC72 (Orange/Yellow) - Incomplete AND Due Date is in the future/No Due Date
            else -> R.color.ongoing_border
        }
    }

    /**
     * Helper function to apply the color to the custom drawable border of the inner ConstraintLayout.
     */
    private fun applyDynamicBorderColor(context: Context, borderView: View, colorResId: Int) {
        // Get the drawable from the view's background
        val drawable = borderView.background

        // Check if the drawable is a GradientDrawable (which is what a <shape> is)
        if (drawable is GradientDrawable) {
            // Get the color integer from resources
            val colorInt = ContextCompat.getColor(context, colorResId)

            // Apply the new stroke color (using 2dp width)
            drawable.setStroke(
                4, // Stroke width (using 4 pixels for visibility, approx 2dp)
                colorInt
            )
        }
    }

    // Returns the total number of items
    override fun getItemCount(): Int = tasks.size

    /**
     * Used to update the list of tasks and refresh the RecyclerView.
     */
    fun updateTasks(newTasks: List<Task>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }
}