package com.example.sorty.data.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class Task(
    // Unique identifier ng task
    val id: Long,

    // Pamagat o deskripsyon ng task
    val title: String,

    val content: String? = null,

    // Timestamp ng due date/time (in milliseconds). Gumagamit ng 0L kung walang date.
    val dueDate: Long, // Ginamit ang non-nullable Long

    // Kategorya o Subject kung saan naka-link ang task (e.g., "Calculus")
    val category: String?,

    // Status ng pagkakumpleto ng task
    var isCompleted: Boolean = false,


    val emojiIcon: String


) {

    /**
     * Nagbabalik ng due date at time sa isang madaling basahin na format.
     * Ipinapakita ang "No Due Date" kung ang value ay 0L.
     */
    fun getFormattedDateTime(): String {
        // Tiyakin na ang timestamp ay hindi 0L (ang ating code para sa "No Due Date")
        if (dueDate == 0L) {
            return "No Due Date"
        }

        // Format: Month Day, Year | Hour:Minute AM/PM
        val dateFormat = "MMM dd, yyyy | hh:mm a"

        // Gumamit ng default system locale para sa tamang AM/PM at format
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

        return formatter.format(Date(dueDate))
    }
}