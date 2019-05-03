package eu.matejkormuth.db2project

import eu.matejkormuth.db2project.models.Employee
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