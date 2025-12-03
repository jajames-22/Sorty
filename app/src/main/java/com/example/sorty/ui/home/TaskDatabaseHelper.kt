package com.example.sorty.ui.home // Assuming this is the desired final package

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.sorty.data.models.Task // Ensure this import path is correct

/**
 * Handles database operations for the To-Do list (Tasks) using SQLiteOpenHelper.
 * Database Version is set to 2 to ensure the 'content' column is created.
 */
class TaskDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "sorty_tasks.db", null, 2) { // Version 2

    companion object {
        const val TABLE_NAME = "tasks"
        // Column names
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_CONTENT = "content"
        const val COL_DUE_DATE = "due_date"
        const val COL_CATEGORY = "category"
        const val COL_IS_COMPLETED = "is_completed"
        const val COL_EMOJI_ICON = "emoji_icon"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_NAME (
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

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop and recreate to handle schema changes (like adding the 'content' column)
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // --- INSERT (C: Create) ---
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
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }

    // --- UPDATE 1: Full Edit (Used by addNotes for Save Changes) ---
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
        val rowsAffected = db.update(
            TABLE_NAME,
            values,
            "$COL_ID = ?",
            arrayOf(task.id.toString())
        )
        db.close()
        return rowsAffected > 0
    }

    // --- UPDATE 2: Update Completion Status (Used by HomeFragment checkbox) ---
    fun updateTaskCompletion(taskId: Long, isCompleted: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        val rowsAffected = db.update(
            TABLE_NAME,
            values,
            "$COL_ID = ?",
            arrayOf(taskId.toString())
        )
        db.close()
        return rowsAffected > 0
    }

    // --- READ 1: Retrieve Single Task by ID (Used by addNotes for Load Existing Task) ---
    fun getTaskById(taskId: Long): Task? {
        val db = readableDatabase
        val selection = "$COL_ID = ?"
        val selectionArgs = arrayOf(taskId.toString())

        val cursor = db.query(
            TABLE_NAME, null, selection, selectionArgs, null, null, null
        )

        var task: Task? = null
        cursor.use {
            if (it.moveToFirst()) {
                task = cursorToTask(it)
            }
        }
        db.close()
        return task
    }

    // --- READ 2: Get All Tasks (Fallback) ---
    fun getAllTasks(): List<Task> {
        return filterTasks("SELECT * FROM $TABLE_NAME ORDER BY $COL_DUE_DATE ASC")
    }

    // --- READ 3: Get Ongoing Tasks (Filter) ---
    fun getOngoingTasks(currentTime: Long): List<Task> {
        return filterTasks("SELECT * FROM $TABLE_NAME WHERE $COL_IS_COMPLETED = 0 AND ($COL_DUE_DATE >= $currentTime OR $COL_DUE_DATE = 0) ORDER BY $COL_DUE_DATE ASC")
    }

    // --- READ 4: Get Completed Tasks (Filter) ---
    fun getCompletedTasks(): List<Task> {
        return filterTasks("SELECT * FROM $TABLE_NAME WHERE $COL_IS_COMPLETED = 1 ORDER BY $COL_DUE_DATE DESC")
    }

    // --- READ 5: Get Missed/Overdue Tasks (Filter) ---
    fun getMissedTasks(currentTime: Long): List<Task> {
        return filterTasks("SELECT * FROM $TABLE_NAME WHERE $COL_IS_COMPLETED = 0 AND $COL_DUE_DATE < $currentTime AND $COL_DUE_DATE != 0 ORDER BY $COL_DUE_DATE DESC")
    }

    // --- DELETE (D: Delete) ---
    fun deleteTask(taskId: Long): Boolean {
        val db = writableDatabase
        val rowsAffected = db.delete(
            TABLE_NAME,
            "$COL_ID = ?",
            arrayOf(taskId.toString())
        )
        db.close()
        return rowsAffected > 0
    }

    // --- General Filter/Query Helper ---
    private fun filterTasks(query: String): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.rawQuery(query, null)

        cursor.use {
            while (it.moveToNext()) {
                val task = cursorToTask(it)
                tasks.add(task)
            }
        }
        db.close()
        return tasks
    }

    // --- Helper function to convert Cursor row to Task object ---
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