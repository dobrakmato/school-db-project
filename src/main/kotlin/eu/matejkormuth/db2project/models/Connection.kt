package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*
import java.lang.RuntimeException
import java.time.Instant

/**
 * This class represents Connection and is used to generate connections table.
 *
 * Surrogate entity for ternary relation.
 */
data class Connection(
        val id: Id = NewId,
        val case: Lazy<Case>,
        val crimeScene: Lazy<CrimeScene>,
        val person: Lazy<Person>,
        @Maybe val confirmedBy: Lazy<Employee>?,
        @Index @Maybe val confirmedAt: Instant?
) : Entity() {
    companion object {

        /**
         * Confirms the specified connection by id with specified employee as confirmer by id.
         *
         * @param connectionId if of connection
         * @param confirmedBy id of confirmer
         * @throws RuntimeException when operation fails
         */
        fun confirm(connectionId: Int, confirmedBy: Int) = transaction {
            val connection = findOne<Connection>(connectionId) ?: throw RuntimeException("Connection not found!")
            val employee = findOne<Employee>(confirmedBy) ?: throw RuntimeException("Employee not found!")

            if (!employee.type.canConfirmConnection()) throw RuntimeException("This employee cannot confirm connections!")
            if (connection.confirmedAt != null) throw RuntimeException("This connection is already confirmed!")

            updateOne(connection.copy(confirmedBy = Lazy(confirmedBy), confirmedAt = Instant.now()))
        }

    }
}