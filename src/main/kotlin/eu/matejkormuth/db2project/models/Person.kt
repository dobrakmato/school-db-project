package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

data class Person(
        val id: Id = NewId,
        val name: String,
        val personType: PersonType
) : Entity() {

    companion object {
        fun punish(personId: Id): Lazy<Punishment> = transaction {
            val person = findOne<Person>(personId)
                    ?: throw RuntimeException("Specified person does not exists.")

            /* can only punish suspects */
            if (person.personType != PersonType.SUSPECT) throw RuntimeException("You can only punish SUSPECTS.")

            val connection = findReferenced<Connection>(person.id, "person_id", eagerLoad = true)

            /* can only punish confirmed people */
            if (connection?.confirmedAt == null) throw RuntimeException("Connection is not confirmed!")

            val case = connection.case.getOrNull()!!
            val punishmentType = when (case.caseType) {
                CaseType.MISDEMEANOR -> PunishmentType.FINE
                CaseType.CRIME -> PunishmentType.ARREST_WARRANT
                CaseType.PROTECTIVE_ACTION -> throw RuntimeException("Cannot punish people connected to PROTECTIVE_ACTION!")
            }
            val caseCategory = retrieve(case.caseCategory)!!

            return insertOne(Punishment(
                    punished = Lazy(person.id),
                    punishmentType = punishmentType,
                    fineAmount = if (punishmentType == PunishmentType.FINE) caseCategory.fineAmount else null
            ))

        }
    }

}