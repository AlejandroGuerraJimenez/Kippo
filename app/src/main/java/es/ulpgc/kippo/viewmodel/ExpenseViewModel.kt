package es.ulpgc.kippo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.ulpgc.kippo.model.Expense
import es.ulpgc.kippo.model.Settlement
import es.ulpgc.kippo.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository = ExpenseRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _settlements = MutableStateFlow<List<Settlement>>(emptyList())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var currentHouseholdId: String = ""

    // uid -> net balance (positive = owed money, negative = owes money)
    val netBalances: StateFlow<Map<String, Double>> = combine(_expenses, _settlements) { expenses, settlements ->
        computeNetBalances(expenses, settlements)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // List of (fromUid, toUid, amount) — minimum transactions to settle all debts
    val simplifiedDebts: StateFlow<List<Triple<String, String, Double>>> = netBalances.map { balances ->
        computeSimplifiedDebts(balances)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeExpenses(householdId: String) {
        if (householdId.isBlank()) return
        currentHouseholdId = householdId
        viewModelScope.launch {
            expenseRepository.observeExpenses(householdId).collect { _expenses.value = it }
        }
        viewModelScope.launch {
            expenseRepository.observeSettlements(householdId).collect { _settlements.value = it }
        }
    }

    fun addExpense(
        title: String,
        amount: Double,
        paidBy: String,
        splitAmong: List<String>,
        category: String,
        notes: String,
        customSplits: Map<String, Double> = emptyMap(),
        receiptImageBase64: String? = null
    ) {
        if (currentHouseholdId.isBlank()) return
        val createdBy = auth.currentUser?.uid ?: return
        val expense = Expense(
            title = title,
            amount = amount,
            paidBy = paidBy,
            splitAmong = splitAmong,
            category = category,
            householdId = currentHouseholdId,
            notes = notes,
            createdBy = createdBy,
            customSplits = customSplits,
            receiptImageBase64 = receiptImageBase64
        )
        viewModelScope.launch {
            expenseRepository.addExpense(expense).onFailure {
                _errorMessage.value = "Error adding expense"
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        if (currentHouseholdId.isBlank()) return
        viewModelScope.launch {
            expenseRepository.deleteExpense(currentHouseholdId, expenseId).onFailure {
                _errorMessage.value = "Error deleting expense"
            }
        }
    }

    fun settleUp(fromUid: String, toUid: String, amount: Double, note: String) {
        if (currentHouseholdId.isBlank()) return
        val settlement = Settlement(
            fromUid = fromUid,
            toUid = toUid,
            amount = amount,
            householdId = currentHouseholdId,
            note = note
        )
        viewModelScope.launch {
            expenseRepository.addSettlement(settlement).onFailure {
                _errorMessage.value = "Error recording payment"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun computeNetBalances(
        expenses: List<Expense>,
        settlements: List<Settlement>
    ): Map<String, Double> {
        val balances = mutableMapOf<String, Double>()

        for (expense in expenses) {
            if (expense.splitAmong.isEmpty()) continue
            balances[expense.paidBy] = (balances[expense.paidBy] ?: 0.0) + expense.amount
            if (expense.customSplits.isNotEmpty()) {
                for ((uid, amount) in expense.customSplits) {
                    balances[uid] = (balances[uid] ?: 0.0) - amount
                }
            } else {
                val share = expense.amount / expense.splitAmong.size
                for (uid in expense.splitAmong) {
                    balances[uid] = (balances[uid] ?: 0.0) - share
                }
            }
        }

        for (settlement in settlements) {
            balances[settlement.fromUid] = (balances[settlement.fromUid] ?: 0.0) + settlement.amount
            balances[settlement.toUid] = (balances[settlement.toUid] ?: 0.0) - settlement.amount
        }

        return balances
    }

    private fun computeSimplifiedDebts(balances: Map<String, Double>): List<Triple<String, String, Double>> {
        val epsilon = 0.01
        val creditors = balances.filter { it.value > epsilon }
            .map { Pair(it.key, it.value) }
            .sortedByDescending { it.second }
            .toMutableList()
        val debtors = balances.filter { it.value < -epsilon }
            .map { Pair(it.key, abs(it.value)) }
            .sortedByDescending { it.second }
            .toMutableList()

        val result = mutableListOf<Triple<String, String, Double>>()
        var i = 0
        var j = 0

        while (i < creditors.size && j < debtors.size) {
            val (creditorUid, credit) = creditors[i]
            val (debtorUid, debt) = debtors[j]
            val settled = minOf(credit, debt)

            result.add(Triple(debtorUid, creditorUid, settled))

            val newCredit = credit - settled
            val newDebt = debt - settled

            if (newCredit < epsilon) i++ else creditors[i] = Pair(creditorUid, newCredit)
            if (newDebt < epsilon) j++ else debtors[j] = Pair(debtorUid, newDebt)
        }

        return result
    }
}
