package eu.matejkormuth.db2project

import java.sql.Connection

interface Repository<T : Entity> {
    fun findById(id: Id): T?
    fun findAll(): Iterable<T>
    fun insert(entity: T)
    fun deleteById(id: Id)
    fun update(entity: T)
}

inline fun <reified T : Entity> boundRepository(connection: Connection): Repository<T> {
    return BoundRepository(connection, T::class.java)
}

class BoundRepository<T : Entity>(
        private val connection: Connection,
        private val klass: Class<T>,
        private val table: Table<T> = Database.tableFor(klass)
) : Repository<T> {
    override fun update(entity: T) {
        table.queryBuilder(connection)
                .updateOne(entity)
    }

    override fun deleteById(id: Id) {
        table.queryBuilder(connection)
                .delete()
                .eq("id", id)
                .execute()
    }

    override fun insert(entity: T) {
        table.queryBuilder(connection)
                .insertOne(entity)
    }

    override fun findById(id: Id): T? {
        return table.queryBuilder(connection)
                .select()
                .eq("id", id)
                .fetchOne()
    }

    override fun findAll(): Iterable<T> {
        return table.queryBuilder(connection)
                .select()
                .fetchMultiple()
    }

}