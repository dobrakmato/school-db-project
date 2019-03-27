package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Entity
import eu.matejkormuth.db2project.Id
import eu.matejkormuth.db2project.Lazy
import eu.matejkormuth.db2project.Maybe
import eu.matejkormuth.db2project.NewId

data class Punishment(
        val id: Id = NewId,
        val punished: Lazy<Person>,
        val punishmentType: PunishmentType,
        @Maybe val fineAmount: Int?
) : Entity()