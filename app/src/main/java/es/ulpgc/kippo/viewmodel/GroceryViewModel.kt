package es.ulpgc.kippo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import es.ulpgc.kippo.model.GroceryItem
import es.ulpgc.kippo.model.GroceryList
import es.ulpgc.kippo.ui.components.toast.ToastManager
import es.ulpgc.kippo.util.RealtimeDiffer
import es.ulpgc.kippo.util.UserDirectory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GroceryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listDiffer = RealtimeDiffer<GroceryList> { it.id }
    private val itemDiffers: MutableMap<String, RealtimeDiffer<GroceryItem>> = mutableMapOf()

    private val _groceryLists = MutableStateFlow<List<GroceryList>>(emptyList())
    val groceryLists: StateFlow<List<GroceryList>> = _groceryLists

    private val _items = MutableStateFlow<Map<String, List<GroceryItem>>>(emptyMap())
    val items: StateFlow<Map<String, List<GroceryItem>>> = _items

    fun observeGroceryLists(householdId: String) {
        if (householdId.isEmpty()) return
        Log.d("GroceryViewModel", "Observing lists for household: $householdId")
        
        // Eliminamos el orderBy para evitar el error de índice compuesto de Firestore
        db.collection("groceryLists")
            .whereEqualTo("householdId", householdId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("GroceryViewModel", "Error observing lists", error)
                    return@addSnapshotListener
                }
                val lists = snapshot?.toObjects(GroceryList::class.java) ?: emptyList()
                Log.d("GroceryViewModel", "Found ${lists.size} lists")
                _groceryLists.value = lists
                val me = auth.currentUser?.uid
                listDiffer.diff(
                    newList = lists,
                    onAdded = { l ->
                        if (l.createdBy.isNotBlank() && l.createdBy != me) {
                            val who = UserDirectory.name(l.createdBy)
                            ToastManager.showRealtime("$who created list: ${l.name}")
                        }
                    }
                )
                lists.forEach { list ->
                    observeItems(list.id, list.name)
                }
            }
    }

    private fun observeItems(listId: String, listName: String) {
        if (itemDiffers.containsKey(listId)) {
            // Already observing; listener may be duplicated on re-emits, but avoid duplicate differ state.
        }
        val differ = itemDiffers.getOrPut(listId) { RealtimeDiffer { it.id } }
        db.collection("groceryLists").document(listId).collection("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val itemList = snapshot?.toObjects(GroceryItem::class.java) ?: emptyList()
                val currentMap = _items.value.toMutableMap()
                currentMap[listId] = itemList
                _items.value = currentMap

                val me = auth.currentUser?.uid
                differ.diff(
                    newList = itemList,
                    onAdded = { it2 ->
                        if (it2.addedBy.isNotBlank() && it2.addedBy != me) {
                            val who = UserDirectory.name(it2.addedBy)
                            ToastManager.showRealtime("$who added \"${it2.name}\" to $listName")
                        }
                    },
                    onUpdated = { prev, curr ->
                        if (!prev.checked && curr.checked) {
                            ToastManager.showRealtime("\"${curr.name}\" checked in $listName")
                        }
                    }
                )
            }
    }

    fun createGroceryList(name: String, householdId: String, createdBy: String, initialItems: List<String>) {
        if (householdId.isEmpty()) {
            Log.e("GroceryViewModel", "Cannot create list: householdId is empty")
            return
        }
        
        val newList = GroceryList(
            name = name,
            householdId = householdId,
            createdBy = createdBy
        )
        
        Log.d("GroceryViewModel", "Creating list: $name for household: $householdId")
        db.collection("groceryLists").add(newList)
            .addOnSuccessListener { docRef ->
                Log.d("GroceryViewModel", "List created with ID: ${docRef.id}")
                initialItems.filter { it.isNotBlank() }.forEach { itemName ->
                    addItemToList(docRef.id, itemName, createdBy)
                }
                ToastManager.showSuccess("List \"$name\" created")
            }
            .addOnFailureListener { e ->
                Log.e("GroceryViewModel", "Error adding list", e)
                ToastManager.showError("Error creating list")
            }
    }

    fun addItemToList(listId: String, itemName: String, addedBy: String) {
        val newItem = GroceryItem(
            name = itemName,
            checked = false,
            addedBy = addedBy
        )
        db.collection("groceryLists").document(listId).collection("items").add(newItem)
    }

    fun toggleItemChecked(listId: String, itemId: String, checked: Boolean) {
        db.collection("groceryLists").document(listId).collection("items").document(itemId)
            .update("checked", checked)
    }

    fun deleteList(listId: String) {
        db.collection("groceryLists").document(listId).delete()
            .addOnSuccessListener { ToastManager.showSuccess("List deleted") }
            .addOnFailureListener { ToastManager.showError("Error deleting list") }
    }
}
