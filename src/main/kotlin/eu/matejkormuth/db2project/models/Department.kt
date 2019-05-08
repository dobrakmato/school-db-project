package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Entity
import eu.matejkormuth.db2project.Id
import eu.matejkormuth.db2project.Lazy
import eu.matejkormuth.db2project.NewId

/**
 * This class represents CrimeScene and is used to generate departments table.
 */
data class Department(
        val id: Id = NewId,
        val name: String,
        val headEmployee: Lazy<Employee>
) : Entity()