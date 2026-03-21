package es.ulpgc.kippo.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    @get:PropertyName("current_household_id")
    @set:PropertyName("current_household_id")
    var current_household_id: String? = null
)