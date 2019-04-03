package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

data class Punishment(
        val id: Id = NewId,
        @Unique val punished: Lazy<Person>,
        val punishmentType: PunishmentType,
        @Maybe val fineAmount: Int?
) : Entity()