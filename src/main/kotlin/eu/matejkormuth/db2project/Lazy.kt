package eu.matejkormuth.db2project

data class Lazy<T : Entity>(
        val id: Int
)

inline fun <reified K : Entity> Lazy<K>.get(): K {
    return Database.tableFor(K::class.java)
            .queryBuilder()
            .select()
            .eq("id", id)
            .fetchOne()
}
