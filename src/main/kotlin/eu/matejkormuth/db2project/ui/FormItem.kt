package eu.matejkormuth.db2project.ui

data class FormItem(
        val text: String,
        val defaultValue: String? = null,
        val validations: Iterable<FormValidation> = emptyList()
) {
    fun validate(line: String): Boolean = validations.all { it.isValid(line) }
}
