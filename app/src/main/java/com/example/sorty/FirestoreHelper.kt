package com.example.sorty

import com.example.sorty.data.models.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()
    private val taskCollection = db.collection("tasks")
    private val subjectCollection = db.collection("subjects")

    // --- TASK FUNCTIONS ---

    fun uploadTask(userEmail: String, task: Task, onComplete: (Boolean) -> Unit) {
        // Use userEmail + taskId as the document ID for uniqueness
        val taskId = if (task.id == 0L) System.currentTimeMillis().toString() else task.id.toString()

        val taskData = hashMapOf(
            "user_email" to userEmail,
            "title" to task.title,
            "content" to task.content,
            "due_date" to task.dueDate,
            "category" to task.category,
            "is_completed" to task.isCompleted,
            "emoji_icon" to task.emojiIcon
        )

        taskCollection.document(taskId).set(taskData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getTasks(userEmail: String, callback: (List<Task>) -> Unit) {
        taskCollection
            .whereEqualTo("user_email", userEmail)
            .orderBy("due_date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val taskList = mutableListOf<Task>()
                for (doc in documents) {
                    val task = Task(
                        id = doc.id.toLong(), // Firestore Doc ID
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        dueDate = doc.getLong("due_date") ?: 0L,
                        category = doc.getString("category") ?: "",
                        isCompleted = doc.getBoolean("is_completed") ?: false,
                        emojiIcon = doc.getString("emoji_icon") ?: "📌"
                    )
                    taskList.add(task)
                }
                callback(taskList)
            }
    }

    fun deleteTask(taskId: Long) {
        taskCollection.document(taskId.toString()).delete()
    }
}