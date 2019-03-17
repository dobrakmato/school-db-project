package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Id

data class Punishment(
        val id: Id,
        val punishmentType: PunishmentType,
        val fineAmount: Int?
)
