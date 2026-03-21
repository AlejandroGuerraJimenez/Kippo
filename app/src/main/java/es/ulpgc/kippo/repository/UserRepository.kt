package es.ulpgc.kippo.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import es.ulpgc.kippo.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    suspend fun getUser(uid: String): Result<User?> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(uids: List<String>): Result<List<User>> {
        if (uids.isEmpty()) return Result.success(emptyList())
        return try {
            // Firestore limit is 10 for 'in' queries, but households are usually small.
            // For larger lists, we'd need to chunk this.
            val snapshots = usersCollection.whereIn(FieldPath.documentId(), uids).get().await()
            val users = snapshots.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val subscription = usersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val user = snapshot?.toObject(User::class.java)
            trySend(user)
        }
        awaitClose { subscription.remove() }
    }
}
