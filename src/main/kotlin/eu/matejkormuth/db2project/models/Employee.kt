package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

data class Employee(
        val id: Id = NewId,
        val name: String,
        val type: EmployeeType,
        val department: Lazy<Department>,
        @Maybe val rank: Int? = null
): Entity()