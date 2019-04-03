package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.loadQuery
import eu.matejkormuth.db2project.map
import eu.matejkormuth.db2project.runQuery
import eu.matejkormuth.db2project.transaction

data class CopOfMonth(
        val month: Int,
        val closedPosition: Int,
        val closedBy: String,
        val closedCount: Int,
        val confirmedPosition: Int,
        val confirmedBy: String,
        val confirmedCount: Int
) {
    companion object {
        fun getAllRows(): Iterable<CopOfMonth> {
            return transaction {
                runQuery(loadQuery("/cop_of_month.sql")) { rs ->
                    rs.use {
                        it.map {
                            CopOfMonth(
                                    getInt("month"),
                                    getInt("closed_position"),
                                    getString("closed_by"),
                                    getInt("closed_cases"),
                                    getInt("confirmed_position"),
                                    getString("confirmed_by"),
                                    getInt("confirmed_cases")
                            )
                        }
                    }
                }
            }
        }
    }
}