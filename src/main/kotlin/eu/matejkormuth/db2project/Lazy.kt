package eu.matejkormuth.db2project

import java.sql.Connection

/**
 * Basically wrapper for integer. Used to encode foreign keys.
 *
 * Lazy's can be empty (representing null in table) or unresolved (containing only
 * id of referenced entity) or resolved (containing the referenced object).
 */
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

    val isEmpty: Boolean = this.id == -1

    companion object {
        fun <T : Entity> empty(): Lazy<T> {
            return Lazy(-1)
        }

        fun <T : Entity> notEmpty(lazy: Lazy<T>?): Boolean {
            return lazy != null && !lazy.isEmpty
        }

        fun <T : Entity> empty(lazy: Lazy<T>?): Boolean {
            return lazy == null || lazy.isEmpty
        }
    }
}

/**
 * Returns resolved lazy or null.
 */
inline fun <reified K : Entity> Lazy<K>.getOrNull(): K? = value

/**
 * Resolved lazy using spcified connection and returns resolved object.
 */
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