package com.example.sorty.ui.subjects

import android.app.AlertDialog
import android.content.Intent
import com.example.sorty.data.models.SharedUser
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sorty.DatabaseHelper
import com.example.sorty.R
import com.example.sorty.SessionManager
import com.example.sorty.ui.home.TaskDetailFragment
import com.example.sorty.ui.home.TaskFilter
import com.example.sorty.ui.home.TodoAdapter
import com.example.sorty.ui.home.addNotes
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class CourseActivity : AppCompatActivity(), AddNewSubject.AddNewSubjectListener, ShareBottomSheet.ShareBottomSheetListener {

    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var tvSubjectName: TextView
    private lateinit var tvSubjectDescription: TextView
    private lateinit var tvOwner: TextView
    private lateinit var btnEditCourse: ImageView
    private lateinit var bgSubject: View
    private lateinit var tabTodo: TextView
    private lateinit var tabFiles: TextView

    // Layouts
    private lateinit var layoutContentTodo: ConstraintLayout
    private lateinit var layoutContentFiles: ConstraintLayout
    private lateinit var layoutFilesEmptyState: LinearLayout

    // Buttons
    private lateinit var btnAddTodo: Button
    private lateinit var btnAddFile: Button
    private lateinit var tvFilterOngoing: TextView

    // Lists & Adapters
    private lateinit var rvTodoList: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var todoAdapter: TodoAdapter
    private lateinit var rvFilesList: RecyclerView
    private lateinit var fileAdapter: FileAdapter

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private var currentCourseId: Int = -1
    private var currentCourseName: String = ""
    private var currentCourseDesc: String = ""
    private var currentCourseColor: String = "#FFFFFF"
    private var currentFilter: TaskFilter = TaskFilter.ONGOING
    private var currentUserEmail: String = ""
    private var isCurrentlyArchived: Boolean = false
    private var currentSubjectOwnerEmail: String = ""

    // File Picker
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)

            val fileName = getFileNameFromUri(uri)
            val fileType = contentResolver.getType(uri) ?: "application/octet-stream"

            val success = dbHelper.insertFile(currentUserEmail, fileName, uri.toString(), fileType, currentCourseName)
            if (success) loadCourseFiles()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        setContentView(R.layout.activity_course)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)
        currentUserEmail = sessionManager.getEmail() ?: ""

        initViews()
        loadCourseData()
        setupRecyclerView()
        setupFileRecyclerView()
        setupListeners()
        setupTabs()
        setupSwipeToDelete()

        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightNavigationBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bg_subject)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportFragmentManager.setFragmentResultListener("task_updated", this) { _, bundle ->
            if (bundle.getBoolean("refresh")) loadCourseTasks()
        }
    }

    override fun onResume() {
        super.onResume()
        loadCourseTasks()
        loadCourseFiles()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        tvSubjectName = findViewById(R.id.tv_subject_name)
        tvSubjectDescription = findViewById(R.id.tv_subject_description)
        tvOwner = findViewById(R.id.tv_owner)
        btnEditCourse = findViewById(R.id.btn_edit_course)
        bgSubject = findViewById(R.id.bg_subject)
        tabTodo = findViewById(R.id.tab_todo)
        tabFiles = findViewById(R.id.tab_files)
        layoutContentTodo = findViewById(R.id.layout_content_todo)
        layoutContentFiles = findViewById(R.id.layout_content_files)
        btnAddTodo = findViewById(R.id.btn_add_todo)
        btnAddFile = findViewById(R.id.btn_add_file)
        tvFilterOngoing = findViewById(R.id.tv_filter_ongoing)
        rvTodoList = findViewById(R.id.rv_todo_list)
        layoutEmptyState = findViewById(R.id.layout_empty_state)
        rvFilesList = findViewById(R.id.rv_files_list)
        layoutFilesEmptyState = findViewById(R.id.layout_files_empty_state)
    }

    private fun loadCourseData() {
        currentCourseId = intent.getIntExtra("COURSE_ID", -1)
        if (currentCourseId != -1) {
            val subject = dbHelper.getSubjectById(currentCourseId)
            subject?.let {
                currentCourseName = it.name
                currentCourseDesc = it.description
                currentCourseColor = it.color
                isCurrentlyArchived = it.isArchived
                currentSubjectOwnerEmail = it.ownerEmail

                tvSubjectName.text = currentCourseName
                tvSubjectDescription.text = currentCourseDesc

                // Ownership text
                if (currentUserEmail.equals(currentSubjectOwnerEmail, ignoreCase = true)) {
                    tvOwner.text = "Your Folder"
                } else {
                    tvOwner.text = "Folder by: $currentSubjectOwnerEmail"
                }

                try {
                    bgSubject.setBackgroundColor(Color.parseColor(currentCourseColor))
                } catch (e: Exception) {
                    bgSubject.setBackgroundColor(Color.parseColor("#FF8A80"))
                }
            }
        }
    }

    private fun showOptionsMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.subject_options_menu, popup.menu)

        val isOwner = currentUserEmail.equals(currentSubjectOwnerEmail, ignoreCase = true)

        val archiveItem = popup.menu.findItem(R.id.action_archive_subject)
        val shareItem = popup.menu.findItem(R.id.action_share_subject)
        val editItem = popup.menu.findItem(R.id.action_edit_subject)

        // Handle "Remove my Access" visibility based on ownership
        if (isOwner) {
            // Owner sees Edit and Share, but NO "Remove Access"
            shareItem?.isVisible = true
            editItem?.isVisible = true
        } else {
            // Guest sees NO Edit or Share, but gets "Remove Access"
            shareItem?.isVisible = false
            editItem?.isVisible = false
            popup.menu.add("Remove my Access") // Programmatically add for guests only
        }

        archiveItem?.title = if (isCurrentlyArchived) "Remove from Archive" else "Move to Archive"

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit_subject -> {
                    val bottomSheet = AddNewSubject.newInstance(currentCourseId, currentCourseName, currentCourseDesc, currentCourseColor)
                    bottomSheet.setAddNewSubjectListener(this)
                    bottomSheet.show(supportFragmentManager, "EditSubject")
                    true
                }
                R.id.action_archive_subject -> {
                    if (isCurrentlyArchived) {
                        if (dbHelper.unarchiveSubject(currentCourseId)) finish()
                    } else {
                        archiveSubjectConfirmation()
                    }
                    true
                }
                R.id.action_share_subject -> {
                    val bottomSheet = ShareBottomSheet()
                    bottomSheet.setShareListener(this)
                    bottomSheet.show(supportFragmentManager, "ShareSheet")
                    true
                }
                else -> {
                    if (item.title == "Remove my Access") {
                        showRemoveAccessConfirmation()
                        true
                    } else false
                }
            }
        }
        popup.show()
    }

    private fun showRemoveAccessConfirmation() {
        // 1. Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_reset_confirmation, null)

        // 2. Build the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // 3. Set the background to transparent so the CardView corners are visible
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 4. Update the text fields to match the "Remove Access" context
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

        tvTitle.text = "Remove Access?"
        tvMessage.text = "Are you sure you want to remove your access to '$currentCourseName'? This will delete your local copy and its contents."
        btnConfirm.text = "Remove"

        // Optional: Change the button color to match your app's theme if you don't want "Reset Red"
        // btnConfirm.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF5252"))

        // 5. Set up button listeners
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            if (dbHelper.removeGuestAccess(currentUserEmail, currentCourseName)) {
                dialog.dismiss()
                finish()
            } else {
                Toast.makeText(this, "Error removing access", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun archiveSubjectConfirmation() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_save, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogView.findViewById<TextView>(R.id.tv_title).text = "Archive Folder"
        dialogView.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            if (dbHelper.archiveSubject(currentCourseId)) {
                dialog.dismiss()
                finish()
            }
        }
        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // --- ShareBottomSheet Listeners ---
    override fun checkUserExists(email: String) = dbHelper.checkUserExists(email)
    override fun onShareList(emails: List<String>) {
        emails.forEach { dbHelper.cloneSubjectToUser(currentCourseId, it) }
    }
    override fun getSharedUsers() = dbHelper.getUsersWithAccess(currentCourseName, currentUserEmail)
    override fun onRemoveAccess(email: String) { dbHelper.removeGuestAccess(email, currentCourseName) }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(emptyList(), { task ->
            TaskDetailFragment.newInstance(task.id).show(supportFragmentManager, "TaskDetail")
        }, { task, isChecked ->
            if (dbHelper.updateTaskCompletion(task.id, isChecked)) loadCourseTasks()
        })
        rvTodoList.layoutManager = LinearLayoutManager(this)
        rvTodoList.adapter = todoAdapter
    }

    private fun setupFileRecyclerView() {
        fileAdapter = FileAdapter(emptyList()) { file ->
            val intent = Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(file.uri), file.type)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try { startActivity(intent) } catch (e: Exception) { }
        }
        rvFilesList.layoutManager = LinearLayoutManager(this)
        rvFilesList.adapter = fileAdapter
    }

    fun loadCourseTasks() {
        val tasks = when (currentFilter) {
            TaskFilter.ONGOING -> dbHelper.getOngoingTasks(currentUserEmail, System.currentTimeMillis())
            TaskFilter.COMPLETED -> dbHelper.getCompletedTasks(currentUserEmail)
            TaskFilter.MISSED -> dbHelper.getMissedTasks(currentUserEmail, System.currentTimeMillis())
        }.filter { it.category.equals(currentCourseName, ignoreCase = true) }

        todoAdapter.updateTasks(tasks)
        layoutEmptyState.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        rvTodoList.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun loadCourseFiles() {
        val files = dbHelper.getFilesForSubject(currentUserEmail, currentCourseName)
        fileAdapter.updateFiles(files)
        layoutFilesEmptyState.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
        rvFilesList.visibility = if (files.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnEditCourse.setOnClickListener { showOptionsMenu(it) }
        btnAddTodo.setOnClickListener {
            val sheet = addNotes()
            sheet.arguments = Bundle().apply { putString("arg_preset_subject", currentCourseName) }
            sheet.show(supportFragmentManager, "AddNotes")
        }
        btnAddFile.setOnClickListener { pickFileLauncher.launch(arrayOf("*/*")) }
        tvFilterOngoing.setOnClickListener { showFilterMenu(it) }
    }

    private fun showFilterMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add(0, 1, 0, "Ongoing"); popup.menu.add(0, 2, 1, "Completed"); popup.menu.add(0, 3, 2, "Missed")
        popup.setOnMenuItemClickListener {
            currentFilter = when(it.itemId) { 1 -> TaskFilter.ONGOING; 2 -> TaskFilter.COMPLETED; else -> TaskFilter.MISSED }
            loadCourseTasks(); true
        }
        popup.show()
    }

    private fun setupTabs() {
        tabTodo.setOnClickListener { updateTabSelection(true) }
        tabFiles.setOnClickListener { updateTabSelection(false) }
    }

    private fun updateTabSelection(isTodoSelected: Boolean) {
        // 1. Handle Visibility
        layoutContentTodo.visibility = if (isTodoSelected) View.VISIBLE else View.GONE
        layoutContentFiles.visibility = if (isTodoSelected) View.GONE else View.VISIBLE

        // 2. Handle Backgrounds and Text Colors
        if (isTodoSelected) {
            // Todo Tab is Active
            tabTodo.setBackgroundResource(R.drawable.bg_tab_selected_rounded)
            tabTodo.setTextColor(Color.WHITE)

            // Files Tab is Inactive
            tabFiles.setBackgroundResource(R.drawable.bg_tab_unselected_rounded)
            tabFiles.setTextColor(ContextCompat.getColor(this, R.color.grey_text))
        } else {
            // Todo Tab is Inactive
            tabTodo.setBackgroundResource(R.drawable.bg_tab_unselected_rounded)
            tabTodo.setTextColor(ContextCompat.getColor(this, R.color.grey_text))

            // Files Tab is Active
            tabFiles.setBackgroundResource(R.drawable.bg_tab_selected_rounded)
            tabFiles.setTextColor(Color.WHITE)

            loadCourseFiles()
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val fileToDelete = fileAdapter.getFileAt(position)

                // 1. Inflate the custom dialog layout
                val dialogView = layoutInflater.inflate(R.layout.dialog_reset_confirmation, null)
                val dialog = AlertDialog.Builder(this@CourseActivity).setView(dialogView).create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                // 2. Bind the custom views
                val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
                val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
                val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
                val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

                // 3. Customize text for file deletion
                tvTitle.text = "Delete File?"
                tvMessage.text = "Are you sure you want to delete '${fileToDelete.name}'? This will remove the file for all shared users."
                btnConfirm.text = "Delete"

                // 4. Set Listeners
                btnCancel.setOnClickListener {
                    dialog.dismiss()
                    // IMPORTANT: Refresh the item so it swipes back into place if cancelled
                    fileAdapter.notifyItemChanged(position)
                }

                btnConfirm.setOnClickListener {
                    if (dbHelper.deleteFile(fileToDelete.id)) {
                        loadCourseFiles()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this@CourseActivity, "Failed to delete file", Toast.LENGTH_SHORT).show()
                        fileAdapter.notifyItemChanged(position)
                        dialog.dismiss()
                    }
                }

                // Ensure the item swipes back if the dialog is dismissed via back button or outside click
                dialog.setOnCancelListener { fileAdapter.notifyItemChanged(position) }

                dialog.show()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(rvFilesList)
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { if (it.moveToFirst()) result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) }
        }
        return result ?: uri.path?.substringAfterLast('/') ?: "file"
    }

    override fun onSubjectAdded(name: String, desc: String, color: String) {}
    override fun onSubjectUpdated(id: Int, name: String, desc: String, color: String) { if (dbHelper.updateSubject(id, name, desc, color)) loadCourseData() }
    override fun onSubjectDeleted(id: Int) { if (dbHelper.deleteSubject(id)) finish() }
}