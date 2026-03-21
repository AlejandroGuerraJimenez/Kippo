package es.ulpgc.kippo.repository

import com.google.firebase.firestore.FirebaseFirestore
import es.ulpgc.kippo.model.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun getTaskCollection(householdId: String) = 
        firestore.collection("household").document(householdId).collection("tasks")

    suspend fun createTask(task: Task): Result<Unit> {
        return try {
            getTaskCollection(task.householdId).add(task).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeTasks(householdId: String): Flow<List<Task>> = callbackFlow {
        val subscription = getTaskCollection(householdId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tasks = snapshot?.toObjects(Task::class.java) ?: emptyList()
                trySend(tasks)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun toggleTaskCompletion(householdId: String, taskId: String, completed: Boolean, userId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "completed" to completed,
                "completedAt" to if (completed) com.google.firebase.Timestamp.now() else null,
                "completedBy" to if (completed) userId else null
            )
            getTaskCollection(householdId).document(taskId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(householdId: String, taskId: String): Result<Unit> {
        return try {
            getTaskCollection(householdId).document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(householdId: String, taskId: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            getTaskCollection(householdId).document(taskId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
