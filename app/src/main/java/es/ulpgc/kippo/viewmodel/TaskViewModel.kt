package es.ulpgc.kippo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import es.ulpgc.kippo.model.Task
import es.ulpgc.kippo.repository.TaskRepository
import es.ulpgc.kippo.ui.components.toast.ToastManager
import es.ulpgc.kippo.util.RealtimeDiffer
import es.ulpgc.kippo.util.UserDirectory
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository = TaskRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    private var currentHouseholdId: String = ""
    
    val tasks: StateFlow<List<Task>> = _tasks
        .map { list ->
            list.sortedWith(compareBy<Task> { it.completed }.thenByDescending { it.createdAt })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val taskDiffer = RealtimeDiffer<Task> { it.id }

    fun observeTasks(householdId: String) {
        if (householdId.isBlank()) return
        currentHouseholdId = householdId
        viewModelScope.launch {
            taskRepository.observeTasks(householdId).collect { list ->
                _tasks.value = list
                val me = auth.currentUser?.uid
                taskDiffer.diff(
                    newList = list,
                    onAdded = { t ->
                        val assigned = t.assignedTo
                        if (assigned == me && me != null) {
                            ToastManager.showRealtime("New task assigned to you: ${t.title}")
                        }
                    },
                    onUpdated = { prev, curr ->
                        if (!prev.completed && curr.completed) {
                            val by = curr.completedBy
                            if (by != null && by != me) {
                                val who = UserDirectory.name(by)
                                ToastManager.showRealtime("$who completed: ${curr.title}")
                            }
                        }
                    }
                )
            }
        }
    }

    fun createTask(title: String, description: String, points: Long, householdId: String, assignedTo: String?, recurrence: String = "none", dueDate: Date? = null) {
        val newTask = Task(
            title = title,
            description = description,
            points = points,
            householdId = householdId,
            assignedTo = assignedTo,
            completed = false,
            completedAt = null,
            completedBy = null,
            recurrence = recurrence,
            dueDate = dueDate
        )
        viewModelScope.launch {
            taskRepository.createTask(newTask)
                .onSuccess { ToastManager.showSuccess("Task created") }
                .onFailure { ToastManager.showError("Error creating task") }
        }
    }

    fun toggleTask(taskId: String, completed: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        if (currentHouseholdId.isBlank()) return

        val task = _tasks.value.find { it.id == taskId } ?: return

        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(currentHouseholdId, task, completed, userId)
                .onSuccess {
                    if (completed) ToastManager.showSuccess("Task completed +${task.points} pts")
                    else ToastManager.showInfo("Task reopened")
                }
                .onFailure { ToastManager.showError("Error updating task") }
        }
    }

    fun deleteTask(taskId: String) {
        if (currentHouseholdId.isBlank()) return
        viewModelScope.launch {
            taskRepository.deleteTask(currentHouseholdId, taskId)
                .onSuccess { ToastManager.showSuccess("Task deleted") }
                .onFailure { ToastManager.showError("Error deleting task") }
        }
    }

    fun updateTask(taskId: String, title: String, description: String, points: Long, assignedTo: String?, recurrence: String = "none", dueDate: Date? = null) {
        if (currentHouseholdId.isBlank()) return
        val updates = mapOf(
            "title" to title,
            "description" to description,
            "points" to points,
            "assignedTo" to assignedTo,
            "recurrence" to recurrence,
            "dueDate" to dueDate
        )
        viewModelScope.launch {
            taskRepository.updateTask(currentHouseholdId, taskId, updates)
                .onSuccess { ToastManager.showSuccess("Task updated") }
                .onFailure { ToastManager.showError("Error updating task") }
        }
    }
}
