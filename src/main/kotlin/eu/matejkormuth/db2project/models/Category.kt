package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Entity
import eu.matejkormuth.db2project.Id
import eu.matejkormuth.db2project.Maybe
import eu.matejkormuth.db2project.NewId

data class Category(
        val id: Id = NewId,
        val name: String,
        @Maybe val fineAmount: Int?
): Entity()