package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.Entity
import eu.matejkormuth.db2project.Id
import eu.matejkormuth.db2project.NewId

/**
 * This class represents CityDistrict and is used to generate city_districts table.
 */
data class CityDistrict(
        val id: Id = NewId,
        val name: String
): Entity()