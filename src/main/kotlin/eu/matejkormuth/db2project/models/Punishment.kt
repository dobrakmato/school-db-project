package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Id
import eu.matejkormuth.db2project.Maybe

data class Punishment(
        val id: Id,
        val punishmentType: PunishmentType,
        @Maybe val fineAmount: Int?
)
