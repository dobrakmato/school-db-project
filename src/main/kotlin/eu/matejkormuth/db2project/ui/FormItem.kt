package eu.matejkormuth.db2project.ui

/**
 * One item of form items.
 * @see Form
 */
data class FormItem(
        val text: String,
        val defaultValue: String? = null,
        val validations: Iterable<FormValidation> = emptyList()
) {
    /**
     * Validates specified line against all validations.
     */
    fun validate(line: String): Boolean = validations.all { it.isValid(line) }

    companion object {
        /**
         * Creates form item that has to be non-empty.
         */
        fun required(text: String, defaultValue: String? = null) = FormItem(text, defaultValue, listOf(NotEmpty))

        /**
         * Creates form item that has to be non-empty and valid integer.
         */
        fun requiredId(text: String, defaultValue: String? = null) = FormItem(text, defaultValue, listOf(NotEmpty, IsInt))

        /**
         * Creates form item that has to be non-empty and one of specified items.
         */
        fun oneOf(text: String, defaultValue: String? = null, possible: Iterable<String>) = FormItem(text, defaultValue, listOf(NotEmpty, OneOf(possible)))

        /**
         * Creates form item that can be empty or one of specified items.
         */
        fun oneOfOptional(text: String, defaultValue: String? = null, possible: Iterable<String>) = FormItem(text, defaultValue, listOf(OneOf(possible + "")))
    }
}
