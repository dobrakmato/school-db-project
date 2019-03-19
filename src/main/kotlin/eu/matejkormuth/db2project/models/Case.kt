package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

data class Case(
        val id: Id = NewId,
        val description: String,
        val headEmployee: Lazy<Employee>,
        val caseType: CaseType,
        val caseCategory: Lazy<Category>,
        @Maybe val crimeScene: Lazy<CrimeScene>? = null /* for protective action */
) : Entity()