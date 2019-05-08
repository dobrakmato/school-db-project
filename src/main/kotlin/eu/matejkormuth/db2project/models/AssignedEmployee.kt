package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

/**
 * Surrogate entity to generate n to n table.
 */
data class AssignedEmployee(
        val id: Id = NewId,
        val employee: Lazy<Employee>,
        val case: Lazy<Case>
) : Entity()