package es.ulpgc.kippo.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class GroceryList(
    @DocumentId val id: String = "",
    val name: String = "",
    val householdId: String = "",
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

data class GroceryItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val checked: Boolean = false,
    val addedBy: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
