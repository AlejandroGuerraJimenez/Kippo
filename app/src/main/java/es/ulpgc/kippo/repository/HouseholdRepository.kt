package es.ulpgc.kippo.repository

import com.google.firebase.firestore.FieldValue
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

    suspend fun leaveHousehold(userId: String, householdId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()

            // 1. Eliminar al usuario de la lista de miembros del hogar
            val householdRef = householdCollection.document(householdId)
            batch.update(householdRef, "members", FieldValue.arrayRemove(userId))

            // 2. Limpiar el current_household_id del perfil del usuario
            val userRef = usersCollection.document(userId)
            batch.update(userRef, "current_household_id", null)

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserHousehold(userId: String): Result<Household?> {
        return try {
            val query = householdCollection.whereArrayContains("members", userId).limit(1).get().await()
            val household = query.documents.firstOrNull()?.toObject(Household::class.java)
            Result.success(household)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
