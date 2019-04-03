package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

data class Person(
        val id: Id = NewId,
        val name: String,
        val personType: PersonType
) : Entity() {

    fun punish(connectionAware: ConnectionAware): Lazy<Punishment> {
        /* can only punish suspects */
        if (personType != PersonType.SUSPECT) throw RuntimeException("You can only punish SUSPECTS.")

        val connection = connectionAware.find<Connection>(eagerLoad = true)
                .eq("person_id", id)
                .fetchOne()

        /* can only punish confirmed people */
        if (connection?.confirmedAt == null) throw RuntimeException("Connection is not confirmed!")

        val case = connection.case.getOrNull()!!
        val punishmentType = when (case.caseType) {
            CaseType.MISDEMEANOR -> PunishmentType.FINE
            CaseType.CRIME -> PunishmentType.ARREST_WARRANT
            CaseType.PROTECTIVE_ACTION -> throw RuntimeException("Cannot punish people connected to PROTECTIVE_ACTION!")
        }
        val caseCategory = connectionAware.retrieve(case.caseCategory)!!

        return connectionAware.insertOne(Punishment(
                punished = Lazy(id),
                punishmentType = punishmentType,
                fineAmount = caseCategory.fineAmount
        ))
    }

}