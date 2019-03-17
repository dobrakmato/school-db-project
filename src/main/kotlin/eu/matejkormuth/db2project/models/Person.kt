package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Id

data class Person(
        val id: Id,
        val name: String,
        val personType: PersonType,
        val punishment: Lazy<Punishment>?
)
