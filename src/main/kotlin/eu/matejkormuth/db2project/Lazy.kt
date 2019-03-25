package eu.matejkormuth.db2project

import java.sql.Connection

data class Lazy<T : Entity>(
        val id: Int
) {
    var value: T? = null
    override fun toString(): String {
        if (value == null) {
            return "Lazy(id=$id)"
        }
        return "Lazy($value)"
    }
}

inline fun <reified K : Entity> Lazy<K>.getOrNull(): K? = value

inline fun <reified K : Entity> Lazy<K>.get(connection: Connection): K? {
    if (value == null) {
        value = Database.tableFor(K::class.java)
                .queryBuilder(connection)
                .select()
                .eq("id", id)
                .fetchOne()
    }
    return value
}