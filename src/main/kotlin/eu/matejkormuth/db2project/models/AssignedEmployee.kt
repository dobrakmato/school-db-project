package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

/* surrogate entity to generate n to n table */
data class AssignedEmployee(
        val employee: Lazy<Employee>,
        val case: Lazy<Case>
) : Entity()