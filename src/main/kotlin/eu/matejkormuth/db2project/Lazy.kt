package eu.matejkormuth.db2project

import java.sql.Connection

data class Lazy<T : Entity>(
        val id: Int
)

inline fun <reified K : Entity> Lazy<K>.get(connection: Connection): K? {
    return Database.tableFor(K::class.java)
            .queryBuilder(connection)
            .select()
            .eq("id", id)
            .fetchOne()
}