package es.ulpgc.kippo.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Task(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val points: Long = 0, // Using Long for int64 compatibility
    val assignedTo: String? = null,
    val completed: Boolean = false,
    val householdId: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    val completedAt: Date? = null,
    val completedBy: String? = null,
    val recurrence: String = "none", // "none" | "daily" | "weekly" | "biweekly" | "monthly"
    val dueDate: Date? = null
)
