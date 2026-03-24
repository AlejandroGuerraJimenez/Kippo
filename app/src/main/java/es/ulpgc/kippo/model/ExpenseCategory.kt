package es.ulpgc.kippo.model

enum class ExpenseCategory(val key: String, val label: String) {
    COMIDA("comida", "Food"),
    TRANSPORTE("transporte", "Transport"),
    HOGAR("hogar", "Home"),
    OCIO("ocio", "Entertainment"),
    OTRO("otro", "Other");

    companion object {
        fun fromKey(key: String) = values().find { it.key == key } ?: OTRO
    }
}
