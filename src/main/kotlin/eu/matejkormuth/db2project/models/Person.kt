package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

/**
 * This class represents Person and is used to generate persons table.
 */
data class Person(
        val id: Id = NewId,
        val name: String,
        val personType: PersonType
) : Entity() {

    companion object {

        /**
         * Punishes person specified by id.
         *
         * @param personId id of person to punish
         * @throws RuntimeException when operation fails
         */
        fun punish(personId: Id): Lazy<Punishment> = transaction {
            val person = findOne<Person>(personId)
                    ?: throw RuntimeException("Specified person does not exists.")

            /* can only punish suspects */
            if (person.personType != PersonType.SUSPECT) throw RuntimeException("You can only punish SUSPECTS.")

            val connection = findReferenced<Connection>(person.id, "person_id", eagerLoad = true)

            /* can only punish confirmed people */
            if (connection?.confirmedAt == null) throw RuntimeException("Connection is not confirmed!")

            val case = connection.case.getOrNull()!!

            val caseCategory = retrieve(case.caseCategory)!!

            try {
                return insertOne(Punishment.create(person, case, caseCategory))
            } finally {
                commit()
            }

        }


    }

}