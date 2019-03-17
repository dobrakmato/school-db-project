package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Id
import eu.matejkormuth.db2project.Lazy

data class Department(
        val id: Id,
        val name: String,
        val headEmployee: Lazy<Employee>
)