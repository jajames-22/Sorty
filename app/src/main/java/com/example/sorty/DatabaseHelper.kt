package com.example.sorty

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.sorty.data.models.Task
import com.example.sorty.data.models.Subject
import com.example.sorty.data.models.SubjectFile
import com.example.sorty.data.models.User
import com.example.sorty.data.models.SharedUser

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "sorty.db", null, 12) {

    companion object {
        const val TABLE_USERS = "users"
        const val TABLE_TASKS = "tasks"
        const val TABLE_FILES = "files"
        const val TABLE_SUBJECTS = "subjects"

        // Common Column
        const val COL_USER_EMAIL = "user_email"
        const val COL_OWNER_EMAIL = "owner_email" // <--- Added for sharing logic

        // Task Constants
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_CONTENT = "content"
        const val COL_DUE_DATE = "due_date"
        const val COL_CATEGORY = "category"
        const val COL_IS_COMPLETED = "is_completed"
        const val COL_EMOJI_ICON = "emoji_icon"

        // File Constants
        const val COL_FILE_ID = "id"
        const val COL_FILE_NAME = "name"
        const val COL_FILE_URI = "uri"
        const val COL_FILE_TYPE = "type"
        const val COL_FILE_SUBJECT = "subject_name"
        const val COL_IS_ARCHIVED = "is_archived"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Users Table
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
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
        """)

        // 2. Subjects Table (Updated with Owner Email)
        db.execSQL("""
            CREATE TABLE $TABLE_SUBJECTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_EMAIL TEXT, 
                $COL_OWNER_EMAIL TEXT,
                name TEXT,
                description TEXT,
                color TEXT,
                $COL_IS_ARCHIVED INTEGER DEFAULT 0
            );
        """)

        // 3. Tasks Table
        db.execSQL("""
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
        """)

        // 4. Files Table
        db.execSQL("""
            CREATE TABLE $TABLE_FILES (
                $COL_FILE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_EMAIL TEXT,
                $COL_FILE_NAME TEXT,
                $COL_FILE_URI TEXT,
                $COL_FILE_TYPE TEXT,
                $COL_FILE_SUBJECT TEXT
            );
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle migration to Version 11
        if (oldVersion < 11) {
            try {
                db.execSQL("ALTER TABLE $TABLE_SUBJECTS ADD COLUMN $COL_OWNER_EMAIL TEXT")
                db.execSQL("UPDATE $TABLE_SUBJECTS SET $COL_OWNER_EMAIL = $COL_USER_EMAIL WHERE $COL_OWNER_EMAIL IS NULL")
            } catch (e: Exception) {
                Log.e("DB_UPGRADE", "Error in version 11 upgrade: ${e.message}")
            }
        }

        // Handle migration to Version 12 (Crucial for the "Add Note" crash)
        if (oldVersion < 12) {
            try {
                // Check if user_email column exists in tasks, add it if missing
                db.execSQL("ALTER TABLE $TABLE_TASKS ADD COLUMN $COL_USER_EMAIL TEXT")
                // Also add it to files just in case
                db.execSQL("ALTER TABLE $TABLE_FILES ADD COLUMN $COL_USER_EMAIL TEXT")
            } catch (e: Exception) {
                Log.e("DB_UPGRADE", "Error in version 12 upgrade: ${e.message}")
            }
        }
    }

    // ==========================================
    // USER FUNCTIONS
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
        return db.insert(TABLE_USERS, null, values) != -1L
    }



    fun checkUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(TABLE_USERS, arrayOf("id"), "email = ? AND password = ?", arrayOf(email, password), null, null, null)
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUsersWithAccess(subjectName: String, ownerEmail: String): List<SharedUser> {
        val userList = mutableListOf<SharedUser>()
        val db = readableDatabase
        val query = """
            SELECT u.email, u.first_name, u.last_name, u.image_uri 
            FROM $TABLE_USERS u
            INNER JOIN $TABLE_SUBJECTS s ON u.email = s.$COL_USER_EMAIL
            WHERE UPPER(TRIM(s.name)) = UPPER(TRIM(?)) 
            AND u.email != ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(subjectName, ownerEmail))
        if (cursor.moveToFirst()) {
            do {
                userList.add(SharedUser(
                    email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    firstName = cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                    lastName = cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return userList
    }

    fun updateUserImage(email: String, imageUri: String): Boolean {
        val db = this.writableDatabase
        return try {
            val contentValues = ContentValues().apply {
                put("image_uri", imageUri) // Ensure this matches your TABLE_USERS column name
            }

            // We use the email as the unique identifier for the WHERE clause
            val result = db.update("users", contentValues, "email = ?", arrayOf(email))

            // result returns the number of rows affected
            result > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getUserFirstName(email: String): String {
        val db = this.readableDatabase
        var firstName = "User" // Default fallback if no match is found

        // Use a projection to only fetch the first_name column for performance
        val query = "SELECT first_name FROM users WHERE email = ?"

        val cursor = db.rawQuery(query, arrayOf(email))

        try {
            if (cursor != null && cursor.moveToFirst()) {
                // Get string from column index 0 (since we only selected one column)
                firstName = cursor.getString(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return firstName
    }

    fun resetUserData(userEmail: String): Boolean {
        val db = writableDatabase
        return try {
            // Start a transaction for data integrity
            db.beginTransaction()

            // 1. Delete all tasks belonging to this user
            db.delete(TABLE_TASKS, "$COL_USER_EMAIL = ?", arrayOf(userEmail))

            // 2. Delete all files belonging to this user
            db.delete(TABLE_FILES, "$COL_USER_EMAIL = ?", arrayOf(userEmail))

            // 3. Delete all subjects/folders belonging to this user
            // Note: For subjects, we use the TABLE_SUBJECTS constant
            db.delete(TABLE_SUBJECTS, "$COL_USER_EMAIL = ?", arrayOf(userEmail))

            // Mark transaction as successful
            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            // End the transaction (commits if successful, rolls back if not)
            db.endTransaction()
        }
    }

    fun getUser(): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users LIMIT 1", null)
        var user: User? = null

        try {
            if (cursor.moveToFirst()) {
                user = User(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    firstName = cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                    lastName = cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                    birthday = cursor.getString(cursor.getColumnIndexOrThrow("birthday")),
                    email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    school = cursor.getString(cursor.getColumnIndexOrThrow("school")),
                    course = cursor.getString(cursor.getColumnIndexOrThrow("course")),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"))
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor.close()
        }

        return user
    }

    fun updateUser(
        id: Int,
        firstName: String,
        lastName: String,
        birthday: String,
        newEmail: String,
        school: String,
        course: String,
        imageUri: String
    ): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            // 1. Get the old email first to update linked tables
            val oldEmailCursor = db.rawQuery("SELECT email FROM users WHERE id = ?", arrayOf(id.toString()))
            var oldEmail = ""
            if (oldEmailCursor.moveToFirst()) {
                oldEmail = oldEmailCursor.getString(0)
            }
            oldEmailCursor.close()

            // 2. Update the User table
            val values = ContentValues().apply {
                put("first_name", firstName)
                put("last_name", lastName)
                put("birthday", birthday)
                put("email", newEmail)
                put("school", school)
                put("course", course)
                put("image_uri", imageUri)
            }
            val userUpdateResult = db.update("users", values, "id = ?", arrayOf(id.toString()))

            // 3. If email changed, update foreign keys in other tables
            if (userUpdateResult > 0 && oldEmail.isNotEmpty() && oldEmail != newEmail) {
                val emailValues = ContentValues().apply { put(COL_USER_EMAIL, newEmail) }

                db.update(TABLE_TASKS, emailValues, "$COL_USER_EMAIL = ?", arrayOf(oldEmail))
                db.update(TABLE_SUBJECTS, emailValues, "$COL_USER_EMAIL = ?", arrayOf(oldEmail))
                db.update(TABLE_FILES, emailValues, "$COL_USER_EMAIL = ?", arrayOf(oldEmail))
            }

            db.setTransactionSuccessful()
            userUpdateResult > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    // ==========================================
    // SUBJECT FUNCTIONS
    // ==========================================
    fun insertSubject(userEmail: String, name: String, description: String, color: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_EMAIL, userEmail)
            put(COL_OWNER_EMAIL, userEmail) // Original creator
            put("name", name)
            put("description", description)
            put("color", color)
            put(COL_IS_ARCHIVED, 0)
        }
        return db.insert(TABLE_SUBJECTS, null, values) != -1L
    }

    fun getAllSubjects(userEmail: String): List<Subject> {
        val list = ArrayList<Subject>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SUBJECTS WHERE $COL_USER_EMAIL = ? AND $COL_IS_ARCHIVED = 0 ORDER BY id DESC", arrayOf(userEmail))
        if (cursor.moveToFirst()) {
            do {
                list.add(Subject(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getString(cursor.getColumnIndexOrThrow("color")),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ARCHIVED)) == 1,
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_OWNER_EMAIL))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getSubjectById(id: Int): Subject? {
        val db = readableDatabase
        var subject: Subject? = null
        try {
            val cursor = db.rawQuery("SELECT * FROM $TABLE_SUBJECTS WHERE id = ?", arrayOf(id.toString()))
            if (cursor.moveToFirst()) {
                subject = Subject(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    color = cursor.getString(cursor.getColumnIndexOrThrow("color")),
                    isArchived = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ARCHIVED)) == 1,
                    userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                    ownerEmail = cursor.getString(cursor.getColumnIndexOrThrow(COL_OWNER_EMAIL))
                )
            }
            cursor.close()
        } catch (e: Exception) { Log.e("DB_ERROR", "getSubjectById failed: ${e.message}") }
        return subject
    }

    fun archiveSubject(subjectId: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put(COL_IS_ARCHIVED, 1) }
        return db.update(TABLE_SUBJECTS, values, "id = ?", arrayOf(subjectId.toString())) > 0
    }

    fun unarchiveSubject(subjectId: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put(COL_IS_ARCHIVED, 0) }
        return db.update(TABLE_SUBJECTS, values, "id = ?", arrayOf(subjectId.toString())) > 0
    }

    fun deleteSubject(id: Int): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_SUBJECTS, "id=?", arrayOf(id.toString())) > 0
    }

    fun updateSubject(id: Int, name: String, description: String, color: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("description", description)
            put("color", color)
        }
        return db.update(TABLE_SUBJECTS, values, "id=?", arrayOf(id.toString())) > 0
    }

    // ==========================================
    // TASK FUNCTIONS
    // ==========================================
    fun insertTask(userEmail: String, task: Task): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            // 1. Prepare the data for the task
            val values = ContentValues().apply {
                put(COL_TITLE, task.title)
                put(COL_CONTENT, task.content)
                put(COL_DUE_DATE, task.dueDate)
                put(COL_CATEGORY, task.category)
                put(COL_IS_COMPLETED, if (task.isCompleted) 1 else 0)
                put(COL_EMOJI_ICON, task.emojiIcon)
            }

            // 2. Identify all users who have a folder with this name
            // This finds the original owner and everyone they shared it with
            val sharedUsers = mutableListOf<String>()
            val query = "SELECT $COL_USER_EMAIL FROM $TABLE_SUBJECTS WHERE UPPER(TRIM(name)) = UPPER(TRIM(?))"
            val cursor = db.rawQuery(query, arrayOf(task.category))

            while (cursor.moveToNext()) {
                sharedUsers.add(cursor.getString(0))
            }
            cursor.close()

            // 3. If the folder isn't shared (or list is empty for some reason),
            // at least insert it for the current user.
            if (sharedUsers.isEmpty()) {
                values.put(COL_USER_EMAIL, userEmail)
                db.insert(TABLE_TASKS, null, values)
            } else {
                // 4. SYNC: Insert the task for EVERY user found in that folder
                for (email in sharedUsers) {
                    values.put(COL_USER_EMAIL, email)
                    db.insert(TABLE_TASKS, null, values)
                }
            }

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    fun deleteTask(taskId: Long): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        return try {
            // 1. First, find the details of the task we are about to delete
            val cursor = db.rawQuery(
                "SELECT $COL_TITLE, $COL_CONTENT, $COL_CATEGORY FROM $TABLE_TASKS WHERE $COL_ID = ?",
                arrayOf(taskId.toString())
            )

            if (cursor.moveToFirst()) {
                val title = cursor.getString(0)
                val content = cursor.getString(1)
                val category = cursor.getString(2)
                cursor.close()

                // 2. Delete ALL tasks that match these details in the same category
                // This ensures if the folder is shared, everyone's copy is removed.
                val result = db.delete(
                    TABLE_TASKS,
                    "$COL_TITLE = ? AND $COL_CONTENT = ? AND $COL_CATEGORY = ?",
                    arrayOf(title, content, category)
                )

                db.setTransactionSuccessful()
                result > 0
            } else {
                cursor.close()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    fun updateFullTask(task: Task): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        return try {
            // 1. Get the "Old" version of this task to find its shared copies
            val cursor = db.rawQuery(
                "SELECT $COL_TITLE, $COL_CONTENT, $COL_CATEGORY FROM $TABLE_TASKS WHERE $COL_ID = ?",
                arrayOf(task.id.toString())
            )

            if (cursor.moveToFirst()) {
                val oldTitle = cursor.getString(0)
                val oldContent = cursor.getString(1)
                val oldCategory = cursor.getString(2)
                cursor.close()

                // 2. Prepare the new values
                val values = ContentValues().apply {
                    put(COL_TITLE, task.title)
                    put(COL_CONTENT, task.content)
                    put(COL_DUE_DATE, task.dueDate)
                    put(COL_CATEGORY, task.category)
                    put(COL_IS_COMPLETED, if (task.isCompleted) 1 else 0)
                    put(COL_EMOJI_ICON, task.emojiIcon)
                }

                // 3. Update ALL matching tasks in that category
                // This ensures edits made by the owner appear for the guest
                val result = db.update(
                    TABLE_TASKS,
                    values,
                    "$COL_TITLE = ? AND $COL_CONTENT = ? AND $COL_CATEGORY = ?",
                    arrayOf(oldTitle, oldContent, oldCategory)
                )

                db.setTransactionSuccessful()
                result > 0
            } else {
                cursor.close()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }



    fun removeGuestAccess(userEmail: String, subjectName: String): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        return try {
            // 1. Delete the subject record for this user
            db.delete(TABLE_SUBJECTS, "$COL_USER_EMAIL = ? AND name = ?", arrayOf(userEmail, subjectName))

            // 2. Delete all tasks (notes) for this user in this category
            db.delete(TABLE_TASKS, "$COL_USER_EMAIL = ? AND $COL_CATEGORY = ?", arrayOf(userEmail, subjectName))

            // 3. Delete all file entries for this user in this subject
            db.delete(TABLE_FILES, "$COL_USER_EMAIL = ? AND $COL_FILE_SUBJECT = ?", arrayOf(userEmail, subjectName))

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    fun getTaskById(taskId: Long): Task? {
        val db = readableDatabase
        var task: Task? = null

        // Using constants for table and column names to prevent typos
        val cursor = db.query(
            TABLE_TASKS,
            null,
            "$COL_ID = ?",
            arrayOf(taskId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            // Mapping the database columns to the Task Data Model
            task = Task(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)).toLong(),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
                dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DUE_DATE)),
                category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_COMPLETED)) == 1,
                emojiIcon = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMOJI_ICON))
            )
        }

        cursor.close()
        return task
    }

    fun getOngoingTasks(userEmail: String, currentTime: Long): List<Task> {
        return filterTasks("SELECT * FROM $TABLE_TASKS WHERE $COL_USER_EMAIL = ? AND $COL_IS_COMPLETED = 0 AND ($COL_DUE_DATE >= $currentTime OR $COL_DUE_DATE = 0) ORDER BY $COL_DUE_DATE ASC", arrayOf(userEmail))
    }

    fun getCompletedTasks(userEmail: String): List<Task> {
        return filterTasks("SELECT * FROM $TABLE_TASKS WHERE $COL_USER_EMAIL = ? AND $COL_IS_COMPLETED = 1 ORDER BY $COL_DUE_DATE DESC", arrayOf(userEmail))
    }

    fun getMissedTasks(userEmail: String, currentTime: Long): List<Task> {
        return filterTasks("SELECT * FROM $TABLE_TASKS WHERE $COL_USER_EMAIL = ? AND $COL_IS_COMPLETED = 0 AND $COL_DUE_DATE < $currentTime AND $COL_DUE_DATE != 0 ORDER BY $COL_DUE_DATE DESC", arrayOf(userEmail))
    }

    private fun filterTasks(query: String, args: Array<String>): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.rawQuery(query, args)
        if (cursor.moveToFirst()) {
            do { tasks.add(cursorToTask(cursor)) } while (cursor.moveToNext())
        }
        cursor.close()
        return tasks
    }

    private fun cursorToTask(cursor: Cursor): Task {
        return Task(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)).toLong(),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
            content = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
            dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DUE_DATE)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
            isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_COMPLETED)) == 1,
            emojiIcon = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMOJI_ICON))
        )
    }

    fun updateTaskCompletion(taskId: Long, isCompleted: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put(COL_IS_COMPLETED, if (isCompleted) 1 else 0) }
        return db.update(TABLE_TASKS, values, "$COL_ID = ?", arrayOf(taskId.toString())) > 0
    }

    // ==========================================
    // FILE FUNCTIONS
    // ==========================================
    fun insertFile(userEmail: String, fileName: String, fileUri: String, fileType: String, subjectName: String): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            // 1. Prepare the file data
            val values = ContentValues().apply {
                put(COL_FILE_NAME, fileName)
                put(COL_FILE_URI, fileUri)
                put(COL_FILE_TYPE, fileType)
                put(COL_FILE_SUBJECT, subjectName)
            }

            // 2. Find all users who own or have been shared this subject
            val sharedUsers = mutableListOf<String>()
            val query = "SELECT $COL_USER_EMAIL FROM $TABLE_SUBJECTS WHERE UPPER(TRIM(name)) = UPPER(TRIM(?))"
            val cursor = db.rawQuery(query, arrayOf(subjectName))

            while (cursor.moveToNext()) {
                sharedUsers.add(cursor.getString(0))
            }
            cursor.close()

            // 3. Sync: Insert the file entry for every user found
            if (sharedUsers.isEmpty()) {
                // Fallback: If no subject found (shouldn't happen), insert for current user
                values.put(COL_USER_EMAIL, userEmail)
                db.insert(TABLE_FILES, null, values)
            } else {
                for (email in sharedUsers) {
                    values.put(COL_USER_EMAIL, email)
                    db.insert(TABLE_FILES, null, values)
                }
            }

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    fun getFilesForSubject(userEmail: String, subjectName: String): List<SubjectFile> {
        val list = ArrayList<SubjectFile>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_FILES WHERE $COL_USER_EMAIL = ? AND UPPER(TRIM($COL_FILE_SUBJECT)) = UPPER(TRIM(?)) ORDER BY $COL_FILE_ID DESC"
        val cursor = db.rawQuery(query, arrayOf(userEmail, subjectName))
        if (cursor.moveToFirst()) {
            do {
                list.add(SubjectFile(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_FILE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_URI)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_SUBJECT))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteFile(fileId: Long): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            // 1. First, find the specific details of the file we are deleting
            val cursor = db.rawQuery(
                "SELECT $COL_FILE_NAME, $COL_FILE_URI, $COL_FILE_SUBJECT FROM $TABLE_FILES WHERE $COL_FILE_ID = ?",
                arrayOf(fileId.toString())
            )

            if (cursor.moveToFirst()) {
                val fileName = cursor.getString(0)
                val fileUri = cursor.getString(1)
                val subjectName = cursor.getString(2)
                cursor.close()

                // 2. Delete the file for ALL users who have this file in this subject
                // This ensures if User A deletes it, it also disappears for User B
                val result = db.delete(
                    TABLE_FILES,
                    "$COL_FILE_NAME = ? AND $COL_FILE_URI = ? AND $COL_FILE_SUBJECT = ?",
                    arrayOf(fileName, fileUri, subjectName)
                )

                db.setTransactionSuccessful()
                result > 0
            } else {
                cursor.close()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    // ==========================================
    // SHARING & UTILS
    // ==========================================

    fun getUserByEmail(email: String): User? {
        val db = this.readableDatabase
        // We use .use to automatically close the cursor after the block finishes
        return db.rawQuery("SELECT * FROM users WHERE email = ?", arrayOf(email)).use { cursor ->
            if (cursor.moveToFirst()) {
                User(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    firstName = cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                    lastName = cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                    birthday = cursor.getString(cursor.getColumnIndexOrThrow("birthday")),
                    email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    school = cursor.getString(cursor.getColumnIndexOrThrow("school")),
                    course = cursor.getString(cursor.getColumnIndexOrThrow("course")),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"))
                )
            } else {
                null
            }
        }
    }
    fun checkUserExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM $TABLE_USERS WHERE email = ?", arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun removeSharedSubject(targetUserEmail: String, subjectName: String): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_SUBJECTS, "$COL_USER_EMAIL = ? AND name = ?", arrayOf(targetUserEmail, subjectName)) > 0
    }

    fun cloneSubjectToUser(subjectId: Int, targetUserEmail: String): Boolean {
        val db = this.writableDatabase
        db.beginTransaction() // Use a transaction so it's "all or nothing"
        return try {
            // 1. Get the original subject details
            val cursor = db.rawQuery("SELECT * FROM $TABLE_SUBJECTS WHERE id = ?", arrayOf(subjectId.toString()))
            if (cursor.moveToFirst()) {
                val folderName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val color = cursor.getString(cursor.getColumnIndexOrThrow("color"))
                val originalOwner = cursor.getString(cursor.getColumnIndexOrThrow(COL_OWNER_EMAIL))
                val originalUserEmail = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL))
                cursor.close()

                // 2. Insert the folder for the receiver
                val folderValues = ContentValues().apply {
                    put("name", folderName)
                    put("description", description)
                    put("color", color)
                    put(COL_USER_EMAIL, targetUserEmail)
                    put(COL_OWNER_EMAIL, originalOwner)
                    put(COL_IS_ARCHIVED, 0)
                }
                db.insert(TABLE_SUBJECTS, null, folderValues)

                // 3. Clone all TASKS (Notes)
                val taskCursor = db.rawQuery(
                    "SELECT * FROM $TABLE_TASKS WHERE $COL_USER_EMAIL = ? AND $COL_CATEGORY = ?",
                    arrayOf(originalUserEmail, folderName)
                )
                if (taskCursor.moveToFirst()) {
                    do {
                        val taskValues = ContentValues().apply {
                            put(COL_USER_EMAIL, targetUserEmail) // Assign to receiver
                            put(COL_TITLE, taskCursor.getString(taskCursor.getColumnIndexOrThrow(COL_TITLE)))
                            put(COL_CONTENT, taskCursor.getString(taskCursor.getColumnIndexOrThrow(COL_CONTENT)))
                            put(COL_DUE_DATE, taskCursor.getLong(taskCursor.getColumnIndexOrThrow(COL_DUE_DATE)))
                            put(COL_CATEGORY, folderName)
                            put(COL_IS_COMPLETED, taskCursor.getInt(taskCursor.getColumnIndexOrThrow(COL_IS_COMPLETED)))
                            put(COL_EMOJI_ICON, taskCursor.getString(taskCursor.getColumnIndexOrThrow(COL_EMOJI_ICON)))
                        }
                        db.insert(TABLE_TASKS, null, taskValues)
                    } while (taskCursor.moveToNext())
                }
                taskCursor.close()

                // 4. Clone all FILES
                val fileCursor = db.rawQuery(
                    "SELECT * FROM $TABLE_FILES WHERE $COL_USER_EMAIL = ? AND $COL_FILE_SUBJECT = ?",
                    arrayOf(originalUserEmail, folderName)
                )
                if (fileCursor.moveToFirst()) {
                    do {
                        val fileValues = ContentValues().apply {
                            put(COL_USER_EMAIL, targetUserEmail) // Assign to receiver
                            put(COL_FILE_NAME, fileCursor.getString(fileCursor.getColumnIndexOrThrow(COL_FILE_NAME)))
                            put(COL_FILE_URI, fileCursor.getString(fileCursor.getColumnIndexOrThrow(COL_FILE_URI)))
                            put(COL_FILE_TYPE, fileCursor.getString(fileCursor.getColumnIndexOrThrow(COL_FILE_TYPE)))
                            put(COL_FILE_SUBJECT, folderName)
                        }
                        db.insert(TABLE_FILES, null, fileValues)
                    } while (fileCursor.moveToNext())
                }
                fileCursor.close()

                db.setTransactionSuccessful()
                true
            } else {
                cursor.close()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }

    fun getArchivedSubjects(userEmail: String): List<Subject> {
        val subjectList = ArrayList<Subject>()
        val db = readableDatabase

        // Query for is_archived = 1 (True)
        val query = "SELECT * FROM $TABLE_SUBJECTS WHERE $COL_USER_EMAIL = ? AND $COL_IS_ARCHIVED = 1 ORDER BY id DESC"

        val cursor = db.rawQuery(query, arrayOf(userEmail))

        try {
            if (cursor.moveToFirst()) {
                do {
                    val subject = Subject(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        color = cursor.getString(cursor.getColumnIndexOrThrow("color")),
                        isArchived = true, // Hardcoded true because of the WHERE clause
                        userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                        ownerEmail = cursor.getString(cursor.getColumnIndexOrThrow(COL_OWNER_EMAIL))
                    )
                    subjectList.add(subject)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor.close()
        }

        return subjectList
    }
}