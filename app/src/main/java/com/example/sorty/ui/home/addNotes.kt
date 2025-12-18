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
import com.example.sorty.DatabaseHelper
import com.example.sorty.R
import com.example.sorty.SessionManager // 👈 IMPORT THIS
import com.example.sorty.data.models.Task
import com.example.sorty.databinding.ActivityAddNotesBinding
import com.example.sorty.ui.home.addNotesFiles.EmojiPickerFragment
import com.example.sorty.ui.home.addNotesFiles.EmojiSelectedListener
import com.example.sorty.ui.subjects.CourseActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class addNotes : BottomSheetDialogFragment(), EmojiSelectedListener {

    private lateinit var bind: ActivityAddNotesBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager // 1. Add SessionManager

    private var selectedReminderTimestamp: Long? = null
    private var selectedEmoji: String = "📌"
    private var taskIdToEdit: Long? = null
    private var taskIsCompleted: Boolean = false
    private var currentUserEmail: String = "" // Store email here

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
        dbHelper = DatabaseHelper(requireContext())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
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

        // 2. Initialize Session and Get Email
        sessionManager = SessionManager(requireContext())
        currentUserEmail = sessionManager.getEmail() ?: ""

        setupSubjectDropdown()
        bind.notifydatetime.text = "Notifying schedule is not set"

        if (taskIdToEdit != 0L) {
            loadExistingTask(taskIdToEdit!!)
        } else {
            updateEmojiPreview(selectedEmoji)

            val presetSubject = arguments?.getString("arg_preset_subject")
            if (!presetSubject.isNullOrEmpty()) {
                bind.autoCompleteSubject.setText(presetSubject, false)
            }
        }

        setupListeners()
    }

    private fun setupSubjectDropdown() {
        if (currentUserEmail.isEmpty()) return

        // 3. FIX: Pass email to getAllSubjects
        val subjectList = dbHelper.getAllSubjects(currentUserEmail)

        val subjectNames = subjectList.map { it.name }.toMutableList()
        subjectNames.add(0, "None")

        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, subjectNames)
        bind.autoCompleteSubject.setAdapter(adapter)
        bind.autoCompleteSubject.setDropDownBackgroundResource(R.color.white)
        bind.autoCompleteSubject.setText("None", false)
    }

    private fun loadExistingTask(taskId: Long) {
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
        // Safety check
        if (currentUserEmail.isEmpty()) {
            Toast.makeText(context, "Error: You are not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

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
            // 4. FIX: Pass email to insertTask
            val success = dbHelper.insertTask(currentUserEmail, newTask)

            if (success) Toast.makeText(context, "Note Added", Toast.LENGTH_SHORT).show()
            else Toast.makeText(context, "Error saving task!", Toast.LENGTH_SHORT).show()
        }

        (parentFragment as? HomeFragment)?.loadTasksFromDatabase()
        (activity as? CourseActivity)?.loadCourseTasks()

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
        bind.notifydatetime.visibility = View.GONE
    }
}