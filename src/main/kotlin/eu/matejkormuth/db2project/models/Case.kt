package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*
import java.time.Instant

data class Case(
        val id: Id = NewId,
        val description: String,
        val headEmployee: Lazy<Employee>,
        val caseType: CaseType,
        val caseCategory: Lazy<Category>,
        @Maybe val closedBy: Lazy<Employee>?,
        @Maybe val protectiveActionPlace: Lazy<CrimeScene>? = null /* for protective action */,
        val createdAt: Instant = Instant.now()
) : Entity()