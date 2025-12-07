package com.example.sorty

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Ensure these imports are correct based on your project structure
import com.example.sorty.data.models.Task
import com.example.sorty.ui.subjects.Subject
import com.example.sorty.data.models.SubjectFile // 👈 RENAMED FROM 'File' TO 'SubjectFile' TO FIX CONFLICTS

// 1. Database Version is 5
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "sorty.db", null, 5) {

    companion object {
        // Task Table Constants
        const val TABLE_TASKS = "tasks"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_CONTENT = "content"
        const val COL_DUE_DATE = "due_date"
        const val COL_CATEGORY = "category"
        const val COL_IS_COMPLETED = "is_completed"
        const val COL_EMOJI_ICON = "emoji_icon"

        // File Table Constants
        const val TABLE_FILES = "files"
        const val COL_FILE_ID = "id"
        const val COL_FILE_NAME = "name"
        const val COL_FILE_URI = "uri"
        const val COL_FILE_TYPE = "type"
        const val COL_FILE_SUBJECT = "subject_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Users Table
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name TEXT,
                last_name TEXT,
                birthday TEXT,
                email TEXT UNIQUE,
                school TEXT,
                course TEXT,
                image_uri TEXT 
            );
            """
        )

        // 2. Subjects Table
        createSubjectsTable(db)

        // 3. Tasks Table
        createTasksTable(db)

        // 4. Files Table
        createFilesTable(db)
    }

    private fun createSubjectsTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE subjects (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                description TEXT,
                color TEXT
            );
            """
        )
    }

    private fun createTasksTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_TASKS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT,
                $COL_CONTENT TEXT,
                $COL_DUE_DATE INTEGER, 
                $COL_CATEGORY TEXT,
                $COL_IS_COMPLETED INTEGER, 
                $COL_EMOJI_ICON TEXT
            );
            """
        )
    }

    private fun createFilesTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_FILES (
                $COL_FILE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FILE_NAME TEXT,
                $COL_FILE_URI TEXT,
                $COL_FILE_TYPE TEXT,
                $COL_FILE_SUBJECT TEXT
            );
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS users")
            onCreate(db)
            return
        }
        if (oldVersion < 3) createSubjectsTable(db)
        if (oldVersion < 4) createTasksTable(db)
        if (oldVersion < 5) createFilesTable(db)
    }

    // ==========================================
    // USER FUNCTIONS
    // ==========================================
    fun insertUser(firstName: String, lastName: String, birthday: String, email: String, school: String, course: String, imageUri: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("first_name", firstName)
            put("last_name", lastName)
            put("birthday", birthday)
            put("email", email)
            put("school", school)
            put("course", course)
            put("image_uri", imageUri)
        }
        val result = db.insert("users", null, values)
        return result != -1L
    }

    // ==========================================
    // SUBJECT FUNCTIONS
    // ==========================================
    fun insertSubject(name: String, description: String, color: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("description", description)
            put("color", color)
        }
        val result = db.insert("subjects", null, values)
        return result != -1L
    }

    fun getAllSubjects(): List<Subject> {
        val subjectList = ArrayList<Subject>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM subjects ORDER BY id DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val color = cursor.getString(cursor.getColumnIndexOrThrow("color"))
                subjectList.add(Subject(id, name, desc, color))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return subjectList
    }

    fun deleteFile(fileId: Long): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_FILES, "$COL_FILE_ID = ?", arrayOf(fileId.toString()))
        return result > 0
    }

    fun getSubjectById(id: Int): Subject? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM subjects WHERE id = ?", arrayOf(id.toString()))
        var subject: Subject? = null
        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val desc = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val color = cursor.getString(cursor.getColumnIndexOrThrow("color"))
            subject = Subject(id, name, desc, color)
        }
        cursor.close()
        return subject
    }

    fun updateSubject(id: Int, name: String, description: String, color: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("description", description)
            put("color", color)
        }
        val result = db.update("subjects", values, "id=?", arrayOf(id.toString()))
        return result > 0
    }

    fun deleteSubject(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete("subjects", "id=?", arrayOf(id.toString()))
        return result > 0
    }

    // ==========================================
    // TASK FUNCTIONS
    // ==========================================
    fun insertTask(task: Task): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, task.title)
            put(COL_CONTENT, task.content)
            put(COL_DUE_DATE, task.dueDate)
            put(COL_CATEGORY, task.category)
            put(COL_IS_COMPLETED, if (task.isCompleted) 1 else 0)
            put(COL_EMOJI_ICON, task.emojiIcon)
        }
        val result = db.insert(TABLE_TASKS, null, values)
        return result != -1L
    }

    fun updateFullTask(task: Task): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, task.title)
            put(COL_CONTENT, task.content)
            put(COL_DUE_DATE, task.dueDate)
            put(COL_CATEGORY, task.category)
            put(COL_IS_COMPLETED, if (task.isCompleted) 1 else 0)
            put(COL_EMOJI_ICON, task.emojiIcon)
        }
        val result = db.update(TABLE_TASKS, values, "$COL_ID = ?", arrayOf(task.id.toString()))
        return result > 0
    }

    fun updateTaskCompletion(taskId: Long, isCompleted: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        val result = db.update(TABLE_TASKS, values, "$COL_ID = ?", arrayOf(taskId.toString()))
        return result > 0
    }

    fun getTaskById(taskId: Long): Task? {
        val db = readableDatabase
        val cursor = db.query(TABLE_TASKS, null, "$COL_ID = ?", arrayOf(taskId.toString()), null, null, null)
        var task: Task? = null
        if (cursor.moveToFirst()) {
            task = cursorToTask(cursor)
        }
        cursor.close()
        return task
    }

    fun getAllTasks(): List<Task> {
        return filterTasks("SELECT * FROM $TABLE_TASKS ORDER BY $COL_DUE_DATE ASC")
    }

    fun getOngoingTasks(currentTime: Long): List<Task> {
        return filterTasks("SELECT * FROM $TABLE_TASKS WHERE $COL_IS_COMPLETED = 0 AND ($COL_DUE_DATE >= $currentTime OR $COL_DUE_DATE = 0) ORDER BY $COL_DUE_DATE ASC")
    }

    fun getCompletedTasks(): List<Task> {
        return filterTasks("SELECT * FROM $TABLE_TASKS WHERE $COL_IS_COMPLETED = 1 ORDER BY $COL_DUE_DATE DESC")
    }

    fun getMissedTasks(currentTime: Long): List<Task> {
        return filterTasks("SELECT * FROM $TABLE_TASKS WHERE $COL_IS_COMPLETED = 0 AND $COL_DUE_DATE < $currentTime AND $COL_DUE_DATE != 0 ORDER BY $COL_DUE_DATE DESC")
    }

    fun deleteTask(taskId: Long): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_TASKS, "$COL_ID = ?", arrayOf(taskId.toString()))
        return result > 0
    }

    // ==========================================
    // FILE FUNCTIONS
    // ==========================================
    fun insertFile(fileName: String, fileUri: String, fileType: String, subjectName: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FILE_NAME, fileName)
            put(COL_FILE_URI, fileUri)
            put(COL_FILE_TYPE, fileType)
            put(COL_FILE_SUBJECT, subjectName)
        }
        val result = db.insert(TABLE_FILES, null, values)
        return result != -1L
    }

    fun getFilesForSubject(subjectName: String): List<SubjectFile> { // 👈 Updated to SubjectFile
        val fileList = ArrayList<SubjectFile>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FILES WHERE $COL_FILE_SUBJECT = ? ORDER BY $COL_FILE_ID DESC", arrayOf(subjectName))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_FILE_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_NAME))
                val uri = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_URI))
                val type = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_TYPE))
                val subject = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_SUBJECT))

                fileList.add(SubjectFile(id, name, uri, type, subject))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return fileList
    }

    // --- Helpers ---
    private fun filterTasks(query: String): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                tasks.add(cursorToTask(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tasks
    }

    private fun cursorToTask(cursor: Cursor): Task {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)).toLong()
        val title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE))
        val content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT))
        val dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DUE_DATE))
        val category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY))
        val isCompletedInt = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_COMPLETED))
        val emojiIcon = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMOJI_ICON))

        return Task(
            id = id,
            title = title,
            content = content,
            dueDate = dueDate,
            category = category,
            isCompleted = isCompletedInt == 1,
            emojiIcon = emojiIcon
        )
    }
}