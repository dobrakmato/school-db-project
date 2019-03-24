package eu.matejkormuth.db2project

import java.sql.ResultSet
import java.util.*

/* use generic param to allow getting class */
inline fun <reified T> T.loadQuery(name: String): String {
    /* stupid scanner tricks */
    T::class.java.getResourceAsStream(name).use {
        val s = Scanner(it).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}

inline fun <T> ResultSet.map(block: ResultSet.() -> T): List<T> {
    val list = mutableListOf<T>()
    while (this.next()) {
        list.add(block(this))
    }
    return list
}