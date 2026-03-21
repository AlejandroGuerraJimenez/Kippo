package es.ulpgc.kippo.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Household(
    @DocumentId val id: String = "",
    val name: String = "",
    val creatorId: String = "",
    val joinCode: String = "",
    val members: List<String> = emptyList(),
    val imageUrl: String? = null,
    @ServerTimestamp val createdAt: Date? = null
)