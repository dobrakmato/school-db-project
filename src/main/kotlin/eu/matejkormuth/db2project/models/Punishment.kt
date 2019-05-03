package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

data class Punishment(
        val id: Id = NewId,
        @Unique val punished: Lazy<Person>,
        val punishmentType: PunishmentType,
        @Maybe val fineAmount: Int?
) : Entity() {
    companion object {
        fun create(person: Person, case: Case, caseCategory: Category): Punishment {
            val punishmentType = when (case.caseType) {
                CaseType.MISDEMEANOR -> PunishmentType.FINE
                CaseType.CRIME -> PunishmentType.ARREST_WARRANT
                CaseType.PROTECTIVE_ACTION -> throw RuntimeException("Cannot punish people connected to PROTECTIVE_ACTION!")
            }

            return Punishment(
                    punished = Lazy(person.id),
                    punishmentType = punishmentType,
                    fineAmount = if (punishmentType == PunishmentType.FINE) caseCategory.fineAmount else null
            )
        }
    }
}