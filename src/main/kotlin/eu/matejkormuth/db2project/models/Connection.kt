package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*
import java.time.Instant

/* surrogate entity for ternary relation */
data class Connection(
        val id: Id = NewId,
        val case: Lazy<Case>,
        val crimeScene: Lazy<CrimeScene>,
        val person: Lazy<Person>,
        @Maybe val confirmedBy: Lazy<Employee>?,
        @Index @Maybe val confirmedAt: Instant?
) : Entity()