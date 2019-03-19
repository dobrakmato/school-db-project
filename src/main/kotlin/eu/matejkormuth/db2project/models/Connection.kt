package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Entity
import eu.matejkormuth.db2project.Id
import eu.matejkormuth.db2project.Lazy
import eu.matejkormuth.db2project.NewId

/* surrogate entity for ternary relation */
data class Connection(
        val id: Id = NewId,
        val case: Lazy<Case>,
        val crimeScene: Lazy<CrimeScene>,
        val person: Lazy<Person>,
        val confirmed: Boolean
): Entity()