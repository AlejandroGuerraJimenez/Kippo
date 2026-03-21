package es.ulpgc.kippo.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.ulpgc.kippo.model.Household
import kotlin.random.Random
import kotlinx.coroutines.tasks.await

class HouseholdRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val householdCollection = firestore.collection("household")
    private val householdCodesCollection = firestore.collection("household_codes")
    private val usersCollection = firestore.collection("users")

    suspend fun createHousehold(name: String, creatorId: String): Result<Household> {
        return try {
            val userRef = usersCollection.document(creatorId)
            var householdRef = householdCollection.document()
            var transactionSucceeded = false
            var attempts = 0

            while (!transactionSucceeded && attempts < 10) {
                attempts++
                val joinCode = generateJoinCode()
                val codeRef = householdCodesCollection.document(joinCode)
                householdRef = householdCollection.document()

                try {
                    firestore.runTransaction { transaction ->
                        val userSnapshot = transaction.get(userRef)
                        val currentHousehold = userSnapshot.getString("current_household_id")
                        if (!currentHousehold.isNullOrBlank()) {
                            throw IllegalStateException("Ya perteneces a un household")
                        }

                        val codeSnapshot = transaction.get(codeRef)
                        if (codeSnapshot.exists()) {
                            throw IllegalStateException("El código de invitación ya existe")
                        }

                        val payload = hashMapOf<String, Any>(
                            "name" to name,
                            "creatorId" to creatorId,
                            "joinCode" to joinCode,
                            "members" to listOf(creatorId),
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        transaction.set(householdRef, payload)
                        transaction.set(
                            codeRef,
                            hashMapOf(
                                "householdId" to householdRef.id,
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                        )
                        transaction.update(userRef, "current_household_id", householdRef.id)
                    }.await()
                    transactionSucceeded = true
                } catch (ex: Exception) {
                    if (ex.message?.contains("código de invitación ya existe") != true) {
                        throw ex
                    }
                }
            }

            if (!transactionSucceeded) {
                return Result.failure(IllegalStateException("No se pudo generar un código único"))
            }

            val createdHousehold = householdRef.get().await().toObject(Household::class.java)
            if (createdHousehold == null) {
                Result.failure(Exception("Error retrieving created household"))
            } else {
                Result.success(createdHousehold)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinHouseholdByCode(userId: String, rawCode: String): Result<Household> {
        return try {
            val joinCode = rawCode.filter { it.isDigit() }
            if (joinCode.length != 6) {
                return Result.failure(IllegalArgumentException("El código debe tener 6 dígitos"))
            }

            val userRef = usersCollection.document(userId)
            val householdId = resolveHouseholdIdByCode(joinCode)
                ?: return Result.failure(IllegalArgumentException("Código no válido"))

            var joinedHouseholdRef = householdCollection.document("invalid")

            firestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val currentHousehold = userSnapshot.getString("current_household_id")
                if (!currentHousehold.isNullOrBlank()) {
                    throw IllegalStateException("Ya perteneces a un household")
                }

                val householdRef = householdCollection.document(householdId)
                joinedHouseholdRef = householdRef
                val householdSnapshot = transaction.get(householdRef)
                if (!householdSnapshot.exists()) {
                    throw IllegalStateException("El household asociado ya no existe")
                }

                transaction.update(householdRef, "members", FieldValue.arrayUnion(userId))
                transaction.update(userRef, "current_household_id", householdId)
            }.await()

            // Normaliza el índice de códigos para households legacy o incompletos.
            householdCodesCollection.document(joinCode)
                .set(
                    mapOf(
                        "householdId" to householdId,
                        "createdAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
                .await()

            householdCollection.document(householdId)
                .set(mapOf("joinCode" to joinCode), SetOptions.merge())
                .await()

            val joinedHousehold = joinedHouseholdRef.get().await().toObject(Household::class.java)
            if (joinedHousehold == null) {
                Result.failure(Exception("Error retrieving joined household"))
            } else {
                Result.success(joinedHousehold)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveHousehold(userId: String, householdId: String): Result<Unit> {
        return try {
            val userRef = usersCollection.document(userId)
            val householdRef = householdCollection.document(householdId)

            firestore.runTransaction { transaction ->
                val householdSnapshot = transaction.get(householdRef)
                if (householdSnapshot.exists()) {
                    val members = householdSnapshot.get("members") as? List<*> ?: emptyList<Any>()
                    val remainingMembers = members.filterIsInstance<String>().filter { it != userId }
                    val joinCode = householdSnapshot.getString("joinCode")

                    if (remainingMembers.isEmpty()) {
                        transaction.delete(householdRef)
                        if (!joinCode.isNullOrBlank()) {
                            transaction.delete(householdCodesCollection.document(joinCode))
                        }
                    } else {
                        transaction.update(householdRef, "members", FieldValue.arrayRemove(userId))
                    }
                }

                transaction.update(userRef, "current_household_id", null)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeMember(householdId: String, userIdToRemove: String): Result<Unit> {
        // La lógica de removeMember es idéntica a leaveHousehold en términos de base de datos
        return leaveHousehold(userIdToRemove, householdId)
    }

    suspend fun updateHouseholdName(householdId: String, newName: String): Result<Unit> {
        return try {
            householdCollection.document(householdId).update("name", newName).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserHousehold(userId: String): Result<Household?> {
        return try {
            val userSnapshot = usersCollection.document(userId).get().await()
            val householdId = userSnapshot.getString("current_household_id")
            if (householdId.isNullOrBlank()) {
                return Result.success(null)
            }

            val household = householdCollection.document(householdId).get().await()
                .toObject(Household::class.java)
            Result.success(household)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateJoinCode(): String {
        return Random.nextInt(0, 1_000_000).toString().padStart(6, '0')
    }

    private suspend fun resolveHouseholdIdByCode(joinCode: String): String? {
        val codeDoc = householdCodesCollection.document(joinCode).get().await()
        codeDoc.getString("householdId")?.let { return it }

        val joinCodeMatch = householdCollection
            .whereEqualTo("joinCode", joinCode)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
        if (joinCodeMatch != null) return joinCodeMatch.id

        val legacyNumericCode = joinCode.toLongOrNull() ?: return null
        val legacyMatch = householdCollection
            .whereEqualTo("code", legacyNumericCode)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        return legacyMatch?.id
    }
}
