package es.ulpgc.kippo.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import es.ulpgc.kippo.model.Expense
import es.ulpgc.kippo.model.Settlement
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ExpenseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun expenseCollection(householdId: String) =
        firestore.collection("household").document(householdId).collection("expenses")

    private fun settlementCollection(householdId: String) =
        firestore.collection("household").document(householdId).collection("settlements")

    suspend fun addExpense(expense: Expense): Result<Unit> {
        return try {
            expenseCollection(expense.householdId).add(expense).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(householdId: String, expenseId: String): Result<Unit> {
        return try {
            expenseCollection(householdId).document(expenseId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeExpenses(householdId: String): Flow<List<Expense>> = callbackFlow {
        val subscription = expenseCollection(householdId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.toObjects(Expense::class.java) ?: emptyList()
                trySend(expenses)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addSettlement(settlement: Settlement): Result<Unit> {
        return try {
            settlementCollection(settlement.householdId).add(settlement).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeSettlements(householdId: String): Flow<List<Settlement>> = callbackFlow {
        val subscription = settlementCollection(householdId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val settlements = snapshot?.toObjects(Settlement::class.java) ?: emptyList()
                trySend(settlements)
            }
        awaitClose { subscription.remove() }
    }
}
