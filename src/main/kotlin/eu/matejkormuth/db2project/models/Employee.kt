package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Id

data class Employee(
        val id: Id,
        val name: String,
        val type: EmployeeType,
        val departmentId: Lazy<Department>,
        val rank: Int?
)