package eu.matejkormuth.db2project.ui

interface FormValidation {
    fun isValid(line: String): Boolean
}

object NotEmpty : FormValidation {
    override fun isValid(line: String): Boolean = line.isNotEmpty() && line.isNotBlank()
}

object IsInt : FormValidation {
    override fun isValid(line: String): Boolean = line.toIntOrNull(10) != null
}

fun OneOf(values: Iterable<String>): FormValidation {
    return object : FormValidation {
        val lowercaseValues = values.map { it.toLowerCase() }
        override fun isValid(line: String): Boolean = lowercaseValues.contains(line.trim().toLowerCase())
    }
}