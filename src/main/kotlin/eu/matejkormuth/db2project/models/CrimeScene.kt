package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Id

data class CrimeScene(
        val id: Id,
        val name: String,
        val cityDistrict: Lazy<CityDistrict>
)