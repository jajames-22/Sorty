package com.example.sorty

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "sorty.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                first_name TEXT,
                last_name TEXT,
                birthday TEXT,
                email TEXT UNIQUE,
                school TEXT,
                course TEXT
            );
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun insertUser(
        firstName: String,
        lastName: String,
        birthday: String,
        email: String,
        school: String,
        course: String
    ): Boolean {

        val db = writableDatabase
        val values = ContentValues()

        values.put("first_name", firstName)
        values.put("last_name", lastName)
        values.put("birthday", birthday)
        values.put("email", email)
        values.put("school", school)
        values.put("course", course)

        val result = db.insert("users", null, values)
        return result != -1L
    }
}
