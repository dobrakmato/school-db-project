package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Entity
import eu.matejkormuth.db2project.Id
import eu.matejkormuth.db2project.Lazy
import eu.matejkormuth.db2project.NewId

/**
 * This class represents CrimeScene and is used to generate crime_scenes table.
 */
data class CrimeScene(
        val id: Id = NewId,
        val name: String,
        val cityDistrict: Lazy<CityDistrict>
) : Entity()