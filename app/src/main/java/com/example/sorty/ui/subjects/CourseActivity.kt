package com.example.sorty.ui.subjects

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView // 👈 CHANGED from TextView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sorty.DatabaseHelper
import com.example.sorty.R
import com.example.sorty.SessionManager
import com.example.sorty.data.models.SubjectFile
import com.example.sorty.ui.home.TaskDetailFragment
import com.example.sorty.ui.home.TaskFilter
import com.example.sorty.ui.home.TodoAdapter
import com.example.sorty.ui.home.addNotes
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class CourseActivity : AppCompatActivity(), AddNewSubject.AddNewSubjectListener {

    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var tvSubjectName: TextView
    private lateinit var tvSubjectDescription: TextView

    // 👇 UPDATED: Changed to ImageView to match your new XML
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

    // File Picker
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)

            val fileName = getFileNameFromUri(uri)
            val fileType = contentResolver.getType(uri) ?: "application/octet-stream"

            val success = dbHelper.insertFile(currentUserEmail, fileName, uri.toString(), fileType, currentCourseName)

            if (success) {
                Snackbar.make(bgSubject, "File Added: $fileName", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.primary_green))
                    .setTextColor(Color.WHITE)
                    .show()

                loadCourseFiles()
            } else {
                Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        setContentView(R.layout.activity_course)

        // Initialize Helpers
        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)
        currentUserEmail = sessionManager.getEmail() ?: ""

        window.navigationBarColor = Color.WHITE
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightNavigationBars = true

        val rootView = findViewById<View>(R.id.bg_subject)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        loadCourseData()

        setupRecyclerView()
        setupFileRecyclerView()

        setupSwipeToDelete()

        setupListeners()
        setupTabs()
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

        // 👇 UPDATED cast
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
            if (subject != null) {
                currentCourseName = subject.name
                currentCourseDesc = subject.description
                currentCourseColor = subject.color
                tvSubjectName.text = currentCourseName
                tvSubjectDescription.text = currentCourseDesc
                try {
                    val colorInt = Color.parseColor(currentCourseColor)
                    bgSubject.setBackgroundColor(colorInt)
                } catch (e: Exception) {
                    bgSubject.setBackgroundColor(Color.parseColor("#FF8A80"))
                }
            }
        }
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            tasks = emptyList(),
            onItemClicked = { task ->
                val detailFragment = TaskDetailFragment.newInstance(task.id)
                detailFragment.show(supportFragmentManager, "TaskDetailSheet")
            },
            onCheckboxClicked = { task, isChecked ->
                val success = dbHelper.updateTaskCompletion(task.id, isCompleted = isChecked)
                if (success) loadCourseTasks()
            }
        )
        rvTodoList.layoutManager = LinearLayoutManager(this)
        rvTodoList.adapter = todoAdapter
    }

    private fun setupFileRecyclerView() {
        fileAdapter = FileAdapter(emptyList<SubjectFile>()) { file ->
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(file.uri), file.type)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Cannot open file", Toast.LENGTH_SHORT).show()
            }
        }

        rvFilesList.layoutManager = LinearLayoutManager(this)
        rvFilesList.adapter = fileAdapter
    }

    fun loadCourseTasks() {
        if (currentUserEmail.isEmpty()) return

        val currentTime = System.currentTimeMillis()
        val statusTasks = when (currentFilter) {
            TaskFilter.ONGOING -> dbHelper.getOngoingTasks(currentUserEmail, currentTime)
            TaskFilter.COMPLETED -> dbHelper.getCompletedTasks(currentUserEmail)
            TaskFilter.MISSED -> dbHelper.getMissedTasks(currentUserEmail, currentTime)
        }

        val finalTasks = statusTasks.filter {
            it.category.equals(currentCourseName, ignoreCase = true)
        }

        tvFilterOngoing.text = currentFilter.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }
        if (finalTasks.isEmpty()) {
            rvTodoList.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvTodoList.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            todoAdapter.updateTasks(finalTasks)
        }
    }

    private fun loadCourseFiles() {
        if (currentUserEmail.isEmpty()) return
        val files = dbHelper.getFilesForSubject(currentUserEmail, currentCourseName)
        if (files.isEmpty()) {
            rvFilesList.visibility = View.GONE
            layoutFilesEmptyState.visibility = View.VISIBLE
        } else {
            rvFilesList.visibility = View.VISIBLE
            layoutFilesEmptyState.visibility = View.GONE
            fileAdapter.updateFiles(files)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        // 👇 UPDATED: Replaced direct listener with PopupMenu logic
        btnEditCourse.setOnClickListener { view ->
            showOptionsMenu(view)
        }

        btnAddTodo.setOnClickListener {
            val bottomSheet = addNotes()
            val args = Bundle()
            args.putString("arg_preset_subject", currentCourseName)
            bottomSheet.arguments = args
            bottomSheet.show(supportFragmentManager, "AddNotesSheet")
        }

        btnAddFile.setOnClickListener {
            pickFileLauncher.launch(arrayOf("*/*"))
        }

        tvFilterOngoing.setOnClickListener { showFilterMenu(it) }
    }

    // 👇 NEW FUNCTION: Shows the 3-dot menu
    private fun showOptionsMenu(view: View) {
        val popup = PopupMenu(this, view)

        popup.menuInflater.inflate(R.menu.subject_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit_subject -> {
                    // Open the existing Edit BottomSheet
                    val bottomSheet = AddNewSubject.newInstance(currentCourseId, currentCourseName, currentCourseDesc, currentCourseColor)
                    bottomSheet.setAddNewSubjectListener(this)
                    bottomSheet.show(supportFragmentManager, "EditSubject")
                    true
                }
                R.id.action_archive_subject -> {
                    archiveSubjectConfirmation()
                    true
                }
                R.id.action_share_subject -> {
                    shareSubject()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    // 👇 NEW FUNCTION: Share Logic
    private fun shareSubject() {
        val shareText = "Check out my subject on Sorty!\n\nSubject: $currentCourseName\nDescription: $currentCourseDesc"
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share Subject via"))
    }

    // 👇 NEW FUNCTION: Archive Logic (Requires Database update later)
    private fun archiveSubjectConfirmation() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_save, null)
        val builder = android.app.AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

        tvTitle.text = "Archive Subject"
        tvMessage.text = "Are you sure you want to move '$currentCourseName' to the archive?"
        btnConfirm.text = "Archive"

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            // TODO: Implement dbHelper.archiveSubject(currentCourseId)
            // For now, we simulate success or delete it depending on your preference
            // Example:
            // val success = dbHelper.archiveSubject(currentCourseId)

            Toast.makeText(this, "Subject Archived (Functionality Pending DB)", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            finish() // Close the screen after archiving
        }

        dialog.show()
    }

    private fun showFilterMenu(view: View) {
        val popup = PopupMenu(this, view)
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
                loadCourseTasks()
            }
            true
        }
        popup.show()
    }

    private fun setupTabs() {
        tabTodo.setOnClickListener { updateTabSelection(isTodoSelected = true) }
        tabFiles.setOnClickListener { updateTabSelection(isTodoSelected = false) }
    }

    private fun updateTabSelection(isTodoSelected: Boolean) {
        if (isTodoSelected) {
            layoutContentTodo.visibility = View.VISIBLE
            layoutContentFiles.visibility = View.GONE
            tabTodo.setBackgroundResource(R.drawable.bg_tab_selected_rounded)
            tabFiles.setBackgroundResource(R.drawable.bg_tab_unselected_rounded)
            tabTodo.setTextColor(ContextCompat.getColor(this, R.color.white))
            tabFiles.setTextColor(ContextCompat.getColor(this, R.color.grey_text))
        } else {
            layoutContentTodo.visibility = View.GONE
            layoutContentFiles.visibility = View.VISIBLE
            tabTodo.setBackgroundResource(R.drawable.bg_tab_unselected_rounded)
            tabFiles.setBackgroundResource(R.drawable.bg_tab_selected_rounded)
            tabTodo.setTextColor(ContextCompat.getColor(this, R.color.grey_text))
            tabFiles.setTextColor(ContextCompat.getColor(this, R.color.white))
            loadCourseFiles()
        }
    }

    private fun setupSwipeToDelete() {
        val deleteIcon = ContextCompat.getDrawable(this, R.drawable.baseline_delete_outline_24)!!
        val paint = Paint().apply {
            color = Color.parseColor("#FF5252")
            isAntiAlias = true
        }
        val cornerRadius = 12 * resources.displayMetrics.density

        val swipeHandler = object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
            0,
            androidx.recyclerview.widget.ItemTouchHelper.LEFT or androidx.recyclerview.widget.ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val verticalMargin = (itemHeight * 0.15).toInt()
                val buttonSize = itemHeight - (verticalMargin * 2)
                val iconMargin = (buttonSize - deleteIcon.intrinsicHeight) / 2
                val background = RectF()

                if (dX > 0) {
                    val leftBound = itemView.left.toFloat() + verticalMargin
                    val topBound = itemView.top.toFloat() + verticalMargin
                    val rightBound = leftBound + buttonSize
                    val bottomBound = topBound + buttonSize
                    background.set(leftBound, topBound, rightBound, bottomBound)
                    c.drawRoundRect(background, cornerRadius, cornerRadius, paint)
                    val iconLeft = (leftBound + iconMargin).toInt()
                    val iconTop = (topBound + iconMargin).toInt()
                    val iconRight = (rightBound - iconMargin).toInt()
                    val iconBottom = (bottomBound - iconMargin).toInt()
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.setTint(Color.WHITE)
                    deleteIcon.draw(c)
                } else if (dX < 0) {
                    val rightBound = itemView.right.toFloat() - verticalMargin
                    val topBound = itemView.top.toFloat() + verticalMargin
                    val leftBound = rightBound - buttonSize
                    val bottomBound = topBound + buttonSize
                    background.set(leftBound, topBound, rightBound, bottomBound)
                    c.drawRoundRect(background, cornerRadius, cornerRadius, paint)
                    val iconLeft = (leftBound + iconMargin).toInt()
                    val iconTop = (topBound + iconMargin).toInt()
                    val iconRight = (rightBound - iconMargin).toInt()
                    val iconBottom = (bottomBound - iconMargin).toInt()
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.setTint(Color.WHITE)
                    deleteIcon.draw(c)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val fileToDelete = fileAdapter.getFileAt(position)

                val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_save, null)
                val builder = android.app.AlertDialog.Builder(this@CourseActivity)
                builder.setView(dialogView)
                val dialog = builder.create()

                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val tvTitle = dialogView.findViewById<TextView>(R.id.tv_title)
                val tvMessage = dialogView.findViewById<TextView>(R.id.tv_message)
                val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
                val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

                tvTitle.text = "Delete File"
                tvMessage.text = "Are you sure you want to delete '${fileToDelete.name}'?"
                btnConfirm.text = "Delete"
                btnConfirm.backgroundTintList = ContextCompat.getColorStateList(this@CourseActivity, android.R.color.holo_red_dark)

                btnCancel.setOnClickListener {
                    dialog.dismiss()
                    fileAdapter.notifyItemChanged(position)
                }

                btnConfirm.setOnClickListener {
                    val success = dbHelper.deleteFile(fileToDelete.id)
                    if (success) {
                        loadCourseFiles()
                        Snackbar.make(bgSubject, "File deleted", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.BLACK)
                            .setTextColor(Color.WHITE)
                            .show()
                    } else {
                        fileAdapter.notifyItemChanged(position)
                        Toast.makeText(this@CourseActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }

                dialog.setOnCancelListener {
                    fileAdapter.notifyItemChanged(position)
                }

                dialog.show()
            }
        }

        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(rvFilesList)
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "Unknown File"
    }

    override fun onSubjectAdded(subjectName: String, subjectDescription: String, colorHex: String) {}

    override fun onSubjectUpdated(id: Int, subjectName: String, subjectDescription: String, colorHex: String) {
        val success = dbHelper.updateSubject(id, subjectName, subjectDescription, colorHex)
        if (success) {
            Snackbar.make(bgSubject, "Subject Updated Successfully!", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(this, R.color.primary_green))
                .setTextColor(Color.WHITE)
                .show()
            loadCourseData()
        } else {
            Snackbar.make(bgSubject, "Update Failed", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(Color.BLACK)
                .setTextColor(Color.WHITE)
                .show()
        }
    }

    override fun onSubjectDeleted(id: Int) {
        val success = dbHelper.deleteSubject(id)
        if (success) {
            Toast.makeText(this, "Subject Deleted", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Snackbar.make(bgSubject, "Delete Failed", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(Color.RED)
                .setTextColor(Color.WHITE)
                .show()
        }
    }
}