package com.example.sorty.ui.home

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.example.sorty.DatabaseHelper // 👈 Import the Unified Helper
import com.example.sorty.R
import com.example.sorty.data.models.Task
import com.example.sorty.databinding.ActivityAddNotesBinding
import com.example.sorty.ui.home.addNotesFiles.EmojiPickerFragment
import com.example.sorty.ui.home.addNotesFiles.EmojiSelectedListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class addNotes : BottomSheetDialogFragment(), EmojiSelectedListener {

    private lateinit var bind: ActivityAddNotesBinding

    // 👇 CHANGED: Use the unified DatabaseHelper
    private lateinit var dbHelper: DatabaseHelper

    private var selectedReminderTimestamp: Long? = null
    private var selectedEmoji: String = "📌"
    private var taskIdToEdit: Long? = null
    private var taskIsCompleted: Boolean = false

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
        taskIdToEdit = arguments?.getLong(ARG_TASK_ID) ?: 0L

        // 👇 INITIALIZE: The unified helper
        dbHelper = DatabaseHelper(requireContext())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            // "Magic Fix" for Bottom Sheet ID
            val bottomSheetId = resources.getIdentifier("design_bottom_sheet", "id", "com.google.android.material")
            if (bottomSheetId != 0) {
                val bottomSheet = dialog.findViewById<View>(bottomSheetId)
                bottomSheet?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.isDraggable = false
                }
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

        setupSubjectDropdown()

        // Set default text
        bind.notifydatetime.text = "Notifying schedule is not set"

        if (taskIdToEdit != 0L) {
            loadExistingTask(taskIdToEdit!!)
        } else {
            updateEmojiPreview(selectedEmoji)
        }

        setupListeners()
    }

    private fun setupSubjectDropdown() {
        // 👇 1. FETCH FROM DATABASE: Get all subjects
        val subjectList = dbHelper.getAllSubjects()

        // 👇 2. EXTRACT NAMES: Convert to a list of strings
        val subjectNames = subjectList.map { it.name }.toMutableList()

        // 👇 3. ADD "None" as the first option
        subjectNames.add(0, "None")

        // 👇 4. SETUP ADAPTER
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, subjectNames)
        bind.autoCompleteSubject.setAdapter(adapter)
        bind.autoCompleteSubject.setDropDownBackgroundResource(R.color.white)
        bind.autoCompleteSubject.setText("None", false)
    }

    private fun loadExistingTask(taskId: Long) {
        // 👇 CHANGED: Use dbHelper
        val existingTask = dbHelper.getTaskById(taskId)

        existingTask?.let { task ->
            bind.textView4.text = "Edit Note"
            bind.buttonAdd2.text = "Save"
            bind.inputNoteTitle.setText(task.title)
            bind.inputNoteContent.setText(task.content)

            val savedCategory = if (task.category.isNullOrEmpty()) "None" else task.category
            bind.autoCompleteSubject.setText(savedCategory, false)

            selectedEmoji = task.emojiIcon
            selectedReminderTimestamp = task.dueDate
            taskIsCompleted = task.isCompleted

            updateEmojiPreview(task.emojiIcon)

            if (task.dueDate > 0L) {
                bind.notifySwitch.isChecked = true
                displayReminderTime(task.dueDate)
            }
        }
    }

    private fun showEmojiPicker() {
        val picker = EmojiPickerFragment()
        picker.listener = this
        picker.show(parentFragmentManager, "EmojiPicker")
    }

    private fun updateEmojiPreview(emoji: String) {
        selectedEmoji = emoji
        (bind.emojiPreview as? TextView)?.text = emoji
    }

    override fun onEmojiSelected(emoji: String) {
        updateEmojiPreview(emoji)
    }

    private fun saveNoteAndDismiss() {
        val title = bind.inputNoteTitle.text.toString().trim()
        val content = bind.inputNoteContent.text.toString().trim()
        val emoji = selectedEmoji
        val reminder = selectedReminderTimestamp ?: 0L

        var subject = bind.autoCompleteSubject.text.toString()
        if (subject == "None") subject = ""

        if (title.isEmpty()) {
            Toast.makeText(context, "Note Title cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        if (taskIdToEdit != 0L) {
            val updatedTask = Task(
                id = taskIdToEdit!!,
                title = title,
                content = content,
                dueDate = reminder,
                category = subject,
                isCompleted = taskIsCompleted,
                emojiIcon = emoji
            )
            // 👇 CHANGED: Use dbHelper
            val success = dbHelper.updateFullTask(updatedTask)

            if (success) Toast.makeText(context, "Note Updated", Toast.LENGTH_SHORT).show()
            else Toast.makeText(context, "Error updating task!", Toast.LENGTH_SHORT).show()

        } else {
            val newTask = Task(
                id = 0L,
                title = title,
                content = content,
                dueDate = reminder,
                category = subject,
                isCompleted = false,
                emojiIcon = emoji
            )
            // 👇 CHANGED: Use dbHelper
            val success = dbHelper.insertTask(newTask)

            if (success) Toast.makeText(context, "Note Added", Toast.LENGTH_SHORT).show()
            else Toast.makeText(context, "Error saving task!", Toast.LENGTH_SHORT).show()
        }

        (parentFragment as? HomeFragment)?.loadTasksFromDatabase()
        dismiss()
    }

    private fun setupListeners() {
        bind.btnCancel.setOnClickListener { dismiss() }
        bind.buttonAdd2.setOnClickListener { saveNoteAndDismiss() }
        bind.addEmojiBtn.setOnClickListener { showEmojiPicker() }
        bind.emojiPreview.setOnClickListener { showEmojiPicker() }

        setupReminderSwitch()
    }

    private fun setupReminderSwitch() {
        bind.notifySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (selectedReminderTimestamp == null || selectedReminderTimestamp == 0L) {
                    showDatePicker()
                } else {
                    displayReminderTime(selectedReminderTimestamp!!)
                }
            } else {
                resetReminder()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day -> showTimePicker(year, month, day) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
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
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                val finalTime = Calendar.getInstance().apply {
                    set(year, month, day, hour, minute, 0)
                }.timeInMillis
                selectedReminderTimestamp = finalTime
                displayReminderTime(finalTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.setOnCancelListener {
            bind.notifySwitch.isChecked = false
            resetReminder()
        }
        timePickerDialog.show()
    }

    private fun displayReminderTime(timestamp: Long) {
        val formattedDate = SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault()).format(timestamp)
        bind.notifydatetime.text = formattedDate
        bind.notifydatetime.visibility = View.VISIBLE
    }

    private fun resetReminder() {
        selectedReminderTimestamp = null
        bind.notifydatetime.text = "Notifying schedule is not set"
    }
}