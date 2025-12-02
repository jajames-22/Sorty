package com.example.sorty

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// 1. Change version from 1 to 2 here ---------v
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "sorty.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        // 2. Add "image_uri TEXT" to the table creation
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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This drops the old table and creates the new one with the image column
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    // 3. Add "imageUri: String" to the parameters
    fun insertUser(
        firstName: String,
        lastName: String,
        birthday: String,
        email: String,
        school: String,
        course: String,
        imageUri: String // <--- NEW PARAMETER
    ): Boolean {

        val db = writableDatabase
        val values = ContentValues()

        values.put("first_name", firstName)
        values.put("last_name", lastName)
        values.put("birthday", birthday)
        values.put("email", email)
        values.put("school", school)
        values.put("course", course)
        values.put("image_uri", imageUri) // <--- SAVE THE URI

        val result = db.insert("users", null, values)
        return result != -1L
    }
}