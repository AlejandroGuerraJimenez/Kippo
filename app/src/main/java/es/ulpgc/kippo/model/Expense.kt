package es.ulpgc.kippo.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Expense(
    @DocumentId val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val paidBy: String = "",
    val splitAmong: List<String> = emptyList(),
    val customSplits: Map<String, Double> = emptyMap(), // uid -> amount; empty = equal split
    val category: String = "otro",
    val householdId: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    val createdBy: String = ""
)
