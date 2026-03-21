package es.ulpgc.kippo.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val username: String = "",
    val profileicon: String = "placeholder_avatar",
    val total_points: Long = 0,
    @get:PropertyName("current_household_id")
    @set:PropertyName("current_household_id")
    var current_household_id: String? = null
)