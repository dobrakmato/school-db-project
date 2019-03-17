package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Id
import eu.matejkormuth.db2project.Lazy
import eu.matejkormuth.db2project.Maybe

data class Case(
        val id: Id,
        val description: String,
        val headEmployee: Lazy<Employee>,
        val caseType: CaseType,
        val caseCategory: Lazy<Category>,
        @Maybe val crimeScene: Lazy<CrimeScene>? = null /* for protective action */
)