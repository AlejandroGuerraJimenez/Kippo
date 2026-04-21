package es.ulpgc.kippo.util

import es.ulpgc.kippo.model.User

object UserDirectory {
    private val names: MutableMap<String, String> = mutableMapOf()

    fun update(users: List<User>) {
        users.forEach { u ->
            if (u.uid.isNotBlank()) {
                names[u.uid] = u.name.ifBlank { u.username.ifBlank { "Someone" } }
            }
        }
    }

    fun name(uid: String?): String {
        if (uid.isNullOrBlank()) return "Someone"
        return names[uid] ?: "Someone"
    }
}
