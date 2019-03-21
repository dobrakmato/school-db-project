package eu.matejkormuth.db2project.ui

interface FormValidation {
    fun isValid(line: String): Boolean
}

object NotEmpty : FormValidation {
    override fun isValid(line: String): Boolean = line.isNotEmpty() && line.isNotBlank()
}

fun OneOf(vararg lowercaseValues: String): FormValidation {
    return object : FormValidation {
        override fun isValid(line: String): Boolean = lowercaseValues.contains(line.trim().toLowerCase())
    }
}