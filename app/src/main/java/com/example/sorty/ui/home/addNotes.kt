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
import androidx.fragment.app.setFragmentResult
import com.example.sorty.DatabaseHelper
import com.example.sorty.R
import com.example.sorty.SessionManager
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

    private var _bind: ActivityAddNotesBinding? = null
    private val bind get() = _bind!!

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private var selectedReminderTimestamp: Long? = null
    private var selectedEmoji: String = "📌"
    private var taskIdToEdit: Long = 0L
    private var taskIsCompleted: Boolean = false
    private var currentUserEmail: String = ""

    companion object {
        private const val ARG_TASK_ID = "arg_task_id"
        private const val ARG_PRESET_SUBJECT = "arg_preset_subject"

        fun newInstance(taskId: Long, presetSubject: String? = null): addNotes {
            val fragment = addNotes()
            val args = Bundle().apply {
                putLong(ARG_TASK_ID, taskId)
                putString(ARG_PRESET_SUBJECT, presetSubject)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true // Changed to true to allow dismissal on back press
        taskIdToEdit = arguments?.getLong(ARG_TASK_ID) ?: 0L
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            // Finding the internal BottomSheet ID using the Material library resource
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
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
        _bind = ActivityAddNotesBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())
        currentUserEmail = sessionManager.getEmail() ?: ""

        setupSubjectDropdown()

        if (taskIdToEdit != 0L) {
            loadExistingTask(taskIdToEdit)
        } else {
            updateEmojiPreview(selectedEmoji)
            val presetSubject = arguments?.getString(ARG_PRESET_SUBJECT)
            if (!presetSubject.isNullOrEmpty()) {
                bind.autoCompleteSubject.setText(presetSubject, false)
            }
        }

        setupListeners()
    }

    private fun setupSubjectDropdown() {
        if (currentUserEmail.isEmpty()) return
        val subjectList = dbHelper.getAllSubjects(currentUserEmail)
        val subjectNames = subjectList.map { it.name }.toMutableList()
        subjectNames.add(0, "None")

        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, subjectNames)
        bind.autoCompleteSubject.setAdapter(adapter)

        // Use standard Android white if R.color.white is causing issues
        bind.autoCompleteSubject.setDropDownBackgroundResource(android.R.color.white)

        if (taskIdToEdit == 0L && bind.autoCompleteSubject.text.isEmpty()) {
            bind.autoCompleteSubject.setText("None", false)
        }
    }

    private fun loadExistingTask(taskId: Long) {
        val task = dbHelper.getTaskById(taskId)
        task?.let {
            bind.textView4.text = "Edit Note"
            bind.buttonAdd2.text = "Save"
            bind.inputNoteTitle.setText(it.title)
            bind.inputNoteContent.setText(it.content)

            val category = if (it.category.isNullOrEmpty()) "None" else it.category
            bind.autoCompleteSubject.setText(category, false)

            selectedEmoji = it.emojiIcon
            selectedReminderTimestamp = it.dueDate
            taskIsCompleted = it.isCompleted
            updateEmojiPreview(it.emojiIcon)

            if (it.dueDate > 0L) {
                bind.notifySwitch.isChecked = true
                displayReminderTime(it.dueDate)
            }
        }
    }

    private fun saveNoteAndDismiss() {
        val title = bind.inputNoteTitle.text.toString().trim()
        val content = bind.inputNoteContent.text.toString().trim()
        var subject = bind.autoCompleteSubject.text.toString()
        if (subject == "None") subject = ""

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Title required", Toast.LENGTH_SHORT).show()
            return
        }

        val task = Task(
            id = taskIdToEdit,
            title = title,
            content = content,
            dueDate = selectedReminderTimestamp ?: 0L,
            category = subject,
            isCompleted = taskIsCompleted,
            emojiIcon = selectedEmoji
        )

        val success = if (taskIdToEdit != 0L) {
            dbHelper.updateFullTask(task)
        } else {
            dbHelper.insertTask(currentUserEmail, task)
        }

        if (success) {
            Toast.makeText(requireContext(), "Note Saved", Toast.LENGTH_SHORT).show()

            // 👇 KEY FIX: Send signal to HomeFragment to refresh immediately
            val result = Bundle().apply { putBoolean("refresh", true) }
            parentFragmentManager.setFragmentResult("task_updated", result)

            dismiss()
        } else {
            Toast.makeText(requireContext(), "Failed to save locally", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setupListeners() {
        bind.btnCancel.setOnClickListener { dismiss() }
        bind.buttonAdd2.setOnClickListener { saveNoteAndDismiss() }
        bind.addEmojiBtn.setOnClickListener { showEmojiPicker() }
        bind.emojiPreview.setOnClickListener { showEmojiPicker() }

        bind.notifySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (selectedReminderTimestamp == null) showDatePicker()
                else displayReminderTime(selectedReminderTimestamp!!)
            } else {
                resetReminder()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d -> showTimePicker(y, m, d) },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { bind.notifySwitch.isChecked = false }
            datePicker.minDate = System.currentTimeMillis() - 1000
            show()
        }
    }

    private fun showTimePicker(y: Int, m: Int, d: Int) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, h, min ->
            val time = Calendar.getInstance().apply { set(y, m, d, h, min, 0) }.timeInMillis
            selectedReminderTimestamp = time
            displayReminderTime(time)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
        ).apply {
            setOnCancelListener { bind.notifySwitch.isChecked = false }
            show()
        }
    }

    private fun displayReminderTime(timestamp: Long) {
        val fmt = SimpleDateFormat("MMM dd, yyyy | hh:mm a", Locale.getDefault())
        bind.notifydatetime.text = fmt.format(timestamp)
        bind.notifydatetime.visibility = View.VISIBLE
    }

    private fun resetReminder() {
        selectedReminderTimestamp = null
        bind.notifydatetime.visibility = View.GONE
    }

    private fun showEmojiPicker() {
        val picker = EmojiPickerFragment()
        picker.listener = this
        picker.show(childFragmentManager, "EmojiPicker")
    }

    private fun updateEmojiPreview(emoji: String) {
        selectedEmoji = emoji
        (bind.emojiPreview as? TextView)?.text = emoji
    }

    override fun onEmojiSelected(emoji: String) = updateEmojiPreview(emoji)

    override fun onDestroyView() {
        super.onDestroyView()
        _bind = null
    }
}