package com.example.sorty

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Ensure these imports are correct based on your project structure
import com.example.sorty.data.models.Task
import com.example.sorty.ui.subjects.Subject
import com.example.sorty.data.models.SubjectFile
import com.example.sorty.data.models.User

// 1. UPDATED DATABASE VERSION TO 7 (Schema Change)
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "sorty.db", null, 7) {

    companion object {
        // Common Column
        const val COL_USER_EMAIL = "user_email" // <--- NEW COLUMN FOR OWNERSHIP

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
                password TEXT, 
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
                $COL_USER_EMAIL TEXT, 
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
                $COL_USER_EMAIL TEXT,
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
                $COL_USER_EMAIL TEXT,
                $COL_FILE_NAME TEXT,
                $COL_FILE_URI TEXT,
                $COL_FILE_TYPE TEXT,
                $COL_FILE_SUBJECT TEXT
            );
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle older versions cleanup
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS users")
            onCreate(db)
            return
        }
        if (oldVersion < 3) createSubjectsTable(db)
        if (oldVersion < 4) createTasksTable(db)
        if (oldVersion < 5) createFilesTable(db)

        // VERSION 6 UPDATE: Add password
        if (oldVersion < 6) {
            try {
                db.execSQL("ALTER TABLE users ADD COLUMN password TEXT DEFAULT ''")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // VERSION 7 UPDATE: Add user_email to all data tables
        if (oldVersion < 7) {
            try {
                db.execSQL("ALTER TABLE subjects ADD COLUMN $COL_USER_EMAIL TEXT DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN $COL_USER_EMAIL TEXT DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE_FILES ADD COLUMN $COL_USER_EMAIL TEXT DEFAULT ''")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // USER FUNCTIONS (Unchanged logic)
    // ==========================================
    fun insertUser(firstName: String, lastName: String, birthday: String, email: String, password: String, school: String, course: String, imageUri: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("first_name", firstName)
            put("last_name", lastName)
            put("birthday", birthday)
            put("email", email)
            put("password", password)
            put("school", school)
            put("course", course)
            put("image_uri", imageUri)
        }
        val result = db.insert("users", null, values)
        return result != -1L
    }

    fun getUser(): User? {
        // NOTE: This gets the FIRST user in DB.
        // Better practice is to use getUserByEmail if you have multiple users.
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users LIMIT 1", null)
        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow("first_name"))
            val lastName = cursor.getString(cursor.getColumnIndexOrThrow("last_name"))
            val birthday = cursor.getString(cursor.getColumnIndexOrThrow("birthday"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val school = cursor.getString(cursor.getColumnIndexOrThrow("school"))
            val course = cursor.getString(cursor.getColumnIndexOrThrow("course"))
            val imageUri = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"))
            user = User(id, firstName, lastName, birthday, email, school, course, imageUri)
        }
        cursor.close()
        return user
    }

    // Inside your DatabaseHelper class
    fun getUserFirstName(email: String): String {
        val db = this.readableDatabase
        // Adjust "first_name" if your column name is different
        val cursor = db.rawQuery("SELECT first_name FROM users WHERE email = ?", arrayOf(email))
        var name = "User"

        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(0)
        }
        cursor?.close()
        return name
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE email = ?", arrayOf(email))
        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow("first_name"))
            val lastName = cursor.getString(cursor.getColumnIndexOrThrow("last_name"))
            val birthday = cursor.getString(cursor.getColumnIndexOrThrow("birthday"))
            val emailVal = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            val school = cursor.getString(cursor.getColumnIndexOrThrow("school"))
            val course = cursor.getString(cursor.getColumnIndexOrThrow("course"))
            val imageUri = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"))
            user = User(id, firstName, lastName, birthday, emailVal, school, course, imageUri)
        }
        cursor.close()
        return user
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query("users", arrayOf("id"), "email = ? AND password = ?", arrayOf(email, password), null, null, null)
        val count = cursor.count
        cursor.close()
        return count > 0
    }

    // ==========================================
    // SUBJECT FUNCTIONS (Updated for Email)
    // ==========================================

    // 1. Insert with Email
    fun insertSubject(userEmail: String, name: String, description: String, color: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_EMAIL, userEmail) // <--- Bind to User
            put("name", name)
            put("description", description)
            put("color", color)
        }
        val result = db.insert("subjects", null, values)
        return result != -1L
    }

    // 2. Get subjects ONLY for this email
    fun getAllSubjects(userEmail: String): List<Subject> {
        val subjectList = ArrayList<Subject>()
        val db = readableDatabase
        // FILTER BY EMAIL
        val cursor = db.rawQuery("SELECT * FROM subjects WHERE $COL_USER_EMAIL = ? ORDER BY id DESC", arrayOf(userEmail))

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

    // Update & Delete rely on ID, which is unique, so strict email checking is optional
    // but good for security. For simplicity, we keep ID based here.
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

    // ==========================================
    // TASK FUNCTIONS (Updated for Email)
    // ==========================================

    // 1. Insert with Email
    fun insertTask(userEmail: String, task: Task): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_EMAIL, userEmail) // <--- Bind to User
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

    // 2. All Getters need to filter by Email
    fun getAllTasks(userEmail: String): List<Task> {
        return filterTasks("SELECT * FROM $TABLE_TASKS WHERE $COL_USER_EMAIL = ? ORDER BY $COL_DUE_DATE ASC", arrayOf(userEmail))
    }

    fun getOngoingTasks(userEmail: String, currentTime: Long): List<Task> {
        return filterTasks(
            "SELECT * FROM $TABLE_TASKS WHERE $COL_USER_EMAIL = ? AND $COL_IS_COMPLETED = 0 AND ($COL_DUE_DATE >= $currentTime OR $COL_DUE_DATE = 0) ORDER BY $COL_DUE_DATE ASC",
            arrayOf(userEmail)
        )
    }

    fun getCompletedTasks(userEmail: String): List<Task> {
        return filterTasks(
            "SELECT * FROM $TABLE_TASKS WHERE $COL_USER_EMAIL = ? AND $COL_IS_COMPLETED = 1 ORDER BY $COL_DUE_DATE DESC",
            arrayOf(userEmail)
        )
    }

    fun getMissedTasks(userEmail: String, currentTime: Long): List<Task> {
        return filterTasks(
            "SELECT * FROM $TABLE_TASKS WHERE $COL_USER_EMAIL = ? AND $COL_IS_COMPLETED = 0 AND $COL_DUE_DATE < $currentTime AND $COL_DUE_DATE != 0 ORDER BY $COL_DUE_DATE DESC",
            arrayOf(userEmail)
        )
    }

    // Updates/Deletes by ID
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

    fun deleteTask(taskId: Long): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_TASKS, "$COL_ID = ?", arrayOf(taskId.toString()))
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

    // ==========================================
    // FILE FUNCTIONS (Updated for Email)
    // ==========================================

    fun insertFile(userEmail: String, fileName: String, fileUri: String, fileType: String, subjectName: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_EMAIL, userEmail) // <--- Bind to User
            put(COL_FILE_NAME, fileName)
            put(COL_FILE_URI, fileUri)
            put(COL_FILE_TYPE, fileType)
            put(COL_FILE_SUBJECT, subjectName)
        }
        val result = db.insert(TABLE_FILES, null, values)
        return result != -1L
    }

    fun getFilesForSubject(userEmail: String, subjectName: String): List<SubjectFile> {
        val fileList = ArrayList<SubjectFile>()
        val db = readableDatabase
        // FILTER BY EMAIL AND SUBJECT
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_FILES WHERE $COL_USER_EMAIL = ? AND $COL_FILE_SUBJECT = ? ORDER BY $COL_FILE_ID DESC",
            arrayOf(userEmail, subjectName)
        )

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

    fun deleteFile(fileId: Long): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_FILES, "$COL_FILE_ID = ?", arrayOf(fileId.toString()))
        return result > 0
    }

    // ==========================================
    // HELPER FUNCTIONS
    // ==========================================

    // Updated helper to accept selectionArgs (for the email filter)
    private fun filterTasks(query: String, args: Array<String>): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.rawQuery(query, args)
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

    // Optional: Reset only THIS user's data
    fun resetUserData(userEmail: String): Boolean {
        val db = writableDatabase
        return try {
            db.delete(TABLE_TASKS, "$COL_USER_EMAIL = ?", arrayOf(userEmail))
            db.delete(TABLE_FILES, "$COL_USER_EMAIL = ?", arrayOf(userEmail))
            db.delete("subjects", "$COL_USER_EMAIL = ?", arrayOf(userEmail))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateUser(id: Int, firstName: String, lastName: String, birthday: String, email: String, school: String, course: String, imageUri: String): Boolean {
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
        // Update the row where ID equals the currentUserId
        val result = db.update("users", values, "id=?", arrayOf(id.toString()))
        return result > 0
    }
}