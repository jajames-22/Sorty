
package com.example.sorty.ui.home

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.sorty.databinding.ActivityAddNotesBinding
import com.example.sorty.ui.home.addNotesFiles.EmojiPickerFragment
import com.example.sorty.ui.home.addNotesFiles.EmojiSelectedListener
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.sorty.ui.home.TaskDatabaseHelper
import com.example.sorty.data.models.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class addNotes : BottomSheetDialogFragment(), EmojiSelectedListener {

    private lateinit var bind: ActivityAddNotesBinding
    private lateinit var taskDbHelper: TaskDatabaseHelper

    // State Variables
    private var selectedReminderTimestamp: Long? = null
    private var selectedEmoji: String = "📌"
    private var taskIdToEdit: Long? = null
    private var taskIsCompleted: Boolean = false // Stores original completion status

    companion object {
        private const val ARG_TASK_ID = "arg_task_id"
        fun newInstance(taskId: Long): addNotes {
            val fragment = addNotes()
            val args = Bundle().apply {
                putLong(ARG_TASK_ID, taskId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false

        taskIdToEdit = arguments?.getLong(ARG_TASK_ID) ?: 0L // Default to 0L if null
        // CRITICAL FIX: Instantiate Database Helper here (as it requires context)
        taskDbHelper = TaskDatabaseHelper(requireContext())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = ActivityAddNotesBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- EDIT MODE CHECK & INITIAL LOAD ---
        if (taskIdToEdit != 0L) {
            // Load task if in edit mode (Ensure the database helper is initialized in onCreate)
            loadExistingTask(taskIdToEdit!!)
        } else {
            // Default setup for adding new task
            updateEmojiPreview(selectedEmoji)
        }

        setupListeners()
    }

    // --- Data Loading for Editing ---
    private fun loadExistingTask(taskId: Long) {
        val existingTask = taskDbHelper.getTaskById(taskId)
        existingTask?.let { task ->
            // Update UI components for Edit Mode
            bind.textView4.text = "Edit Note"
            bind.buttonAdd2.text = "Save Changes"

            // Load saved data into fields
            bind.inputNoteTitle.setText(task.title)
            bind.inputNoteContent.setText(task.content)

            // Set state variables
            selectedEmoji = task.emojiIcon
            selectedReminderTimestamp = task.dueDate
            taskIsCompleted = task.isCompleted // Save completion status

            // Update UI preview
            updateEmojiPreview(task.emojiIcon)

            // Handle Reminder Switch
            if (task.dueDate > 0L) {
                bind.notifySwitch.isChecked = true
                displayReminderTime(task.dueDate)
            }
        }
    }

    // --- Emoji Picker Logic ---

    private fun showEmojiPicker() {
        val picker = EmojiPickerFragment()
        picker.listener = this
        picker.show(parentFragmentManager, "EmojiPicker")
    }

    private fun updateEmojiPreview(emoji: String) {
        selectedEmoji = emoji
        // FIX: The as? TextView cast is causing problems.
        // Assuming the binding property is named 'emojiPreview' and is a TextView:
        // (If bind.emojiPreview is the TextView)
        // If your bind.emojiPreview is truly the TextView, remove the cast or fix the XML.
        (bind.emojiPreview as? TextView)?.text = emoji // Keeping the cast for compatibility
    }

    override fun onEmojiSelected(emoji: String) {
        updateEmojiPreview(emoji)
    }

    // --- Save/Update Logic ---

    private fun saveNoteAndDismiss() {
        val title = bind.inputNoteTitle.text.toString().trim()
        val content = bind.inputNoteContent.text.toString().trim()
        val emoji = selectedEmoji
        val reminder = selectedReminderTimestamp ?: 0L

        if (title.isEmpty()) {
            Toast.makeText(context, "Note Title cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        // Logic Switch: UPDATE vs. INSERT
        if (taskIdToEdit != 0L) {
            // == UPDATE EXISTING TASK ==
            val updatedTask = Task(
                id = taskIdToEdit!!, // Use the existing ID
                title = title,
                content = content,
                dueDate = reminder,
                category = null,
                isCompleted = taskIsCompleted, // Use the stored completion status
                emojiIcon = emoji
            )
            val success = taskDbHelper.updateFullTask(updatedTask)

            if (success) {
                Toast.makeText(context, "Note Updated: $title", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Error updating task!", Toast.LENGTH_SHORT).show()
            }

        } else {
            // == INSERT NEW TASK ==
            val newTask = Task(
                id = 0L,
                title = title,
                content = content,
                dueDate = reminder,
                category = null,
                isCompleted = false,
                emojiIcon = emoji
            )
            val success = taskDbHelper.insertTask(newTask)

            if (success) {
                Toast.makeText(context, "Note Added: $title", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Error saving task!", Toast.LENGTH_SHORT).show()
            }
        }

        // Notify HomeFragment to refresh list after saving/updating (Assuming HomeFragment has this public function)
        (parentFragment as? HomeFragment)?.loadTasksFromDatabase()
        dismiss()
    }

    // --- Date/Time Picker Logic ---

    private fun setupListeners() {
        bind.btnCancel.setOnClickListener { dismiss() }
        bind.buttonAdd2.setOnClickListener { saveNoteAndDismiss() }
        bind.addEmojiBtn.setOnClickListener { showEmojiPicker() }

        // CRITICAL FIX: Add click listener for the emoji preview area to open the picker
        (bind.emojiPreview as? View)?.setOnClickListener { showEmojiPicker() }

        setupReminderSwitch()
    }

    private fun setupReminderSwitch() {

        // We use setOnCheckedChangeListener to handle the switch Toggling
        bind.notifySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // If reminder is already set (in edit mode), don't show picker again unless tapped
                if (selectedReminderTimestamp == null || selectedReminderTimestamp == 0L) {
                    showDatePicker()
                } else {
                    // Show confirmation of existing time
                    displayReminderTime(selectedReminderTimestamp!!)
                }
            } else {
                resetReminder()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                showTimePicker(selectedYear, selectedMonth, selectedDay)
            },
            year, month, day
        )

        datePickerDialog.setOnCancelListener {
            bind.notifySwitch.isChecked = false
            resetReminder()
        }

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun showTimePicker(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val finalTime = Calendar.getInstance().apply {
                    set(year, month, day, selectedHour, selectedMinute, 0)
                }.timeInMillis

                selectedReminderTimestamp = finalTime
                displayReminderTime(finalTime)
            },
            hour, minute, false
        )

        timePickerDialog.setOnCancelListener {
            bind.notifySwitch.isChecked = false
            resetReminder()
        }

        timePickerDialog.show()
    }

    private fun displayReminderTime(timestamp: Long) {
        val formattedDate = SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault()).format(timestamp)
        // Optionally update the UI fields if you have TextViews for the date/time display
        Toast.makeText(context, "Reminder set for: $formattedDate", Toast.LENGTH_LONG).show()
    }

    private fun resetReminder() {
        selectedReminderTimestamp = null
    }
}