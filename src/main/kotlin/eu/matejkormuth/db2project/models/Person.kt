package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Id
import eu.matejkormuth.db2project.Lazy
import eu.matejkormuth.db2project.Maybe

data class Person(
        val id: Id,
        val name: String,
        val personType: PersonType,
        @Maybe val punishment: Lazy<Punishment>? = null
)
