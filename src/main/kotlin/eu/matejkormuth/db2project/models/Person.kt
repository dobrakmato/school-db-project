package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

data class Person(
        val id: Id = NewId,
        val name: String,
        val personType: PersonType,
        @Maybe val punishment: Lazy<Punishment>? = null
) : Entity()