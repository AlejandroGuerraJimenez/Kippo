package es.ulpgc.kippo.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Household(
    @DocumentId val id: String = "", // Firestore asignará el ID automáticamente
    val name: String = "",          // Nombre del grupo (ej: "Piso de la Calle Mayor")
    val creatorId: String = "",     // UID del usuario que lo creó (el admin)
    val members: List<String> = emptyList(), // Lista de UIDs de los miembros (incluido el creador)
    @ServerTimestamp val createdAt: Date? = null // Fecha de creación
)