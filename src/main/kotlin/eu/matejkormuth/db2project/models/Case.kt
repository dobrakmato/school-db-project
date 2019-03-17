package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Id

data class Case(
        val id: Id,
        val description: String,
        val headEmployee: Lazy<Employee>,
        val caseType: CaseType,
        val caseCategory: Lazy<Category>,
        val crimeScene: Lazy<CrimeScene>? /* for protective action */
)