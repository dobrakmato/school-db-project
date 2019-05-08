package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.loadQuery
import eu.matejkormuth.db2project.map
import eu.matejkormuth.db2project.runQuery
import eu.matejkormuth.db2project.transaction

/**
 * This class represents single row of "dangerous city district" statistic result set.
 */
data class DangerousCityDistricts(
        val year: String,
        val quarter: String,
        val month: String,
        val position: Int,
        val district: String,
        val casesCount: Int,
        val type0Count: Int,
        val type1Count: Int
) {
    companion object {

        /**
         * Retrieves all rows of this statistic from database.
         */
        fun getAllRows(): Iterable<DangerousCityDistricts> {
            return transaction {
                runQuery(loadQuery("/dangerous_city_districts.sql")) { rs ->
                    rs.use {
                        it.map {
                            DangerousCityDistricts(
                                    getString("ye"),
                                    getString("qu"),
                                    getString("mo"),
                                    getInt("pos"),
                                    getString("district"),
                                    getInt("cases"),
                                    getInt("type0"),
                                    getInt("type1")
                            )
                        }
                    }
                }
            }
        }
    }
}