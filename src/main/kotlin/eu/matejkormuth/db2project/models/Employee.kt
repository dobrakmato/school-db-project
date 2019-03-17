package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Id
import eu.matejkormuth.db2project.Lazy
import eu.matejkormuth.db2project.Maybe

data class Employee(
        val id: Id,
        val name: String,
        val type: EmployeeType,
        val department: Lazy<Department>,
        @Maybe val rank: Int? = null
)