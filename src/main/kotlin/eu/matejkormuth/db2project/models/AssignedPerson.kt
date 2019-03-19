package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

/* surrogate entity to generate n to n table */
data class AssignedPerson(
        val person: Lazy<Person>,
        val case: Lazy<Case>
) : Entity()