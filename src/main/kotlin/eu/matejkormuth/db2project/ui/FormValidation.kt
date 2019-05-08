package eu.matejkormuth.db2project.ui

/**
 * Interface for all form validations.
 */
interface FormValidation {
    /**
     * Returns whether the specified line passes this validation.
     */
    fun isValid(line: String): Boolean
}

/**
 * Ensures that form item is not empty.
 */
object NotEmpty : FormValidation {
    override fun isValid(line: String): Boolean = line.isNotEmpty() && line.isNotBlank()
}

/**
 * Ensures that form item is valid integer.
 */
object IsInt : FormValidation {
    override fun isValid(line: String): Boolean = line.toIntOrNull(10) != null
}

/**
 * Create and returns validator that ensures that form item is one of specified items (case-insensitively).
 */
fun OneOf(values: Iterable<String>): FormValidation {
    return object : FormValidation {
        val lowercaseValues = values.map { it.toLowerCase() }
        override fun isValid(line: String): Boolean = lowercaseValues.contains(line.trim().toLowerCase())
    }
}