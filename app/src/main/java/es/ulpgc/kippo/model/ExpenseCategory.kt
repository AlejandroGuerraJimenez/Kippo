package es.ulpgc.kippo.model

enum class ExpenseCategory(val key: String, val label: String) {
    COMIDA("comida", "Comida"),
    TRANSPORTE("transporte", "Transporte"),
    HOGAR("hogar", "Hogar"),
    OCIO("ocio", "Ocio"),
    OTRO("otro", "Otro");

    companion object {
        fun fromKey(key: String) = values().find { it.key == key } ?: OTRO
    }
}
