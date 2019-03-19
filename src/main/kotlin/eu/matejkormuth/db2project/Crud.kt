package eu.matejkormuth.db2project

inline fun <reified T : Entity> insert(entity: T): Lazy<T> {
    return Database.tableFor(T::class.java)
            .queryBuilder()
            .insertOne(entity)
}

inline fun <reified T : Entity> T.save() {
    Database.tableFor(T::class.java)
            .queryBuilder()
            .updateOne(this)
}

inline fun <reified T : Entity> delete(id: Id): Boolean {
    return Database.tableFor(T::class.java)
            .queryBuilder()
            .delete()
            .eq("id", id)
            .execute()
}

inline fun <reified T : Entity> findOne(id: Id): T {
    return Database.tableFor(T::class.java)
            .queryBuilder()
            .select()
            .eq("id", id)
            .limit(1)
            .fetchOne()
}

inline fun <reified T : Entity> findAll(): Iterable<T> {
    return Database.tableFor(T::class.java)
            .queryBuilder()
            .select()
            .fetchMultiple()
}