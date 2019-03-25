package eu.matejkormuth.db2project.ui

data class FormItem(
        val text: String,
        val defaultValue: String? = null,
        val validations: Iterable<FormValidation> = emptyList()
) {
    fun validate(line: String): Boolean = validations.all { it.isValid(line) }

    companion object {
        fun required(text: String, defaultValue: String? = null) = FormItem(text, defaultValue, listOf(NotEmpty))

        fun requiredId(text: String, defaultValue: String? = null) = FormItem(text, defaultValue, listOf(NotEmpty, IsInt))

        fun oneOf(text: String, defaultValue: String? = null, possible: Iterable<String>) = FormItem(text, defaultValue, listOf(NotEmpty, OneOf(possible)))
    }
}
