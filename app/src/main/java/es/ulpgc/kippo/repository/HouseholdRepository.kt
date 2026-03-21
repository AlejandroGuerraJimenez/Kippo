package es.ulpgc.kippo.repository

import com.google.firebase.firestore.FirebaseFirestore
import es.ulpgc.kippo.model.Household
import kotlinx.coroutines.tasks.await

class HouseholdRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val householdCollection = firestore.collection("household")
    private val usersCollection = firestore.collection("users")

    suspend fun createHousehold(name: String, creatorId: String): Result<Household> {
        return try {
            val newHousehold = Household(
                name = name,
                creatorId = creatorId,
                members = listOf(creatorId), // El creador es el primer miembro
                createdAt = null // Firestore usará el ServerTimestamp al guardar
            )

            // 1. Crear el documento en la colección 'household'
            val documentReference = householdCollection.add(newHousehold).await()

            // 2. Actualizar el documento del usuario con el campo correcto: current_household_id
            val householdId = documentReference.id
            usersCollection.document(creatorId)
                .update("current_household_id", householdId)
                .await()

            // Recuperar el Household creado para devolverlo (con su ID)
            val createdHouseholdSnapshot = documentReference.get().await()
            val createdHousehold = createdHouseholdSnapshot.toObject(Household::class.java)

            if (createdHousehold != null) {
                Result.success(createdHousehold)
            } else {
                Result.failure(Exception("Error retrieving created household"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}