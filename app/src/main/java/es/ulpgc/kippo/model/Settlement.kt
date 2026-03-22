package es.ulpgc.kippo.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Settlement(
    @DocumentId val id: String = "",
    val fromUid: String = "",
    val toUid: String = "",
    val amount: Double = 0.0,
    val householdId: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
