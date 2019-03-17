package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Id

data class Connection(
        val id: Id,
        val case: Lazy<Case>,
        val crimeScene: Lazy<CrimeScene>,
        val person: Lazy<Person>,
        val confirmed: Boolean
)