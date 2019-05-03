package eu.matejkormuth.db2project

import java.sql.Connection
import java.sql.ResultSet

/* class that brings context (connection) to scope */
class ConnectionAware(val connection: Connection) {
    val log by logger()
}

inline fun <T> transaction(isolation: Int = Connection.TRANSACTION_SERIALIZABLE, block: ConnectionAware.() -> T): T {
    val connection = Database.getConnection()
    val connectionAware = ConnectionAware(connection)
    connection.autoCommit = false
    connection.transactionIsolation = isolation


    try {
        val result = connectionAware.block()
        connectionAware.log.debug("Implicitly COMMIT-ing the current transaction.")
        connection.commit()
        return result
    } catch (e: Exception) {
        connection.rollback()
        throw e
    } finally {
        connection.close()
    }
}

inline fun <reified T : Entity> ConnectionAware.queryBuilder(): QueryBuilder<T> = Database.tableFor(T::class.java).queryBuilder(this.connection)

inline fun <reified T : Entity> ConnectionAware.insertOne(entity: T): Lazy<T> {
    return Database.tableFor(T::class.java)
            .queryBuilder(this.connection)
            .insertOne(entity)
}

inline fun <reified T : Entity> ConnectionAware.insertMultiple(entities: Iterable<T>, maxRowsAtOnce: Int = 10000) {
    entities.chunked(maxRowsAtOnce).forEach { chunk ->
        val qb = Database.tableFor(T::class.java)
                .queryBuilder(this.connection)
        chunk.forEach { qb.insertMultiple(it) }
        qb.execute()
    }
}

inline fun <reified T : Entity> ConnectionAware.updateOne(entity: T) {
    Database.tableFor(T::class.java)
            .queryBuilder(this.connection)
            .updateOne(entity)
}

inline fun <reified T : Entity> ConnectionAware.delete(id: Id): Boolean {
    return Database.tableFor(T::class.java)
            .queryBuilder(this.connection)
            .delete()
            .eq("id", id)
            .execute()
}

inline fun ConnectionAware.commit() = this.connection.commit()

inline fun <reified T : Entity> ConnectionAware.findReferenced(id: Id, columnName: String, eagerLoad: Boolean = false): T? {
    val table = Database.tableFor(T::class.java)
    val qb = table.queryBuilder(this.connection)
    if (eagerLoad) qb.selectEager() else qb.select()
    qb.eq(columnName, id)
    return qb.fetchOne()
}

inline fun <reified T : Entity> ConnectionAware.findAllReferenced(id: Id, columnName: String, eagerLoad: Boolean = false): Iterable<T> {
    val table = Database.tableFor(T::class.java)
    val qb = table.queryBuilder(this.connection)
    if (eagerLoad) qb.selectEager() else qb.select()
    qb.eq(columnName, id)
    return qb.fetchMultiple()
}

inline fun <reified T : Entity> ConnectionAware.find(eagerLoad: Boolean = false): QueryBuilder<T> {
    val table = Database.tableFor(T::class.java)
    val qb = table.queryBuilder(this.connection)
    if (eagerLoad) qb.selectEager() else qb.select()
    return qb
}

inline fun <reified T : Entity> ConnectionAware.findOne(id: Id, eagerLoad: Boolean = false, forUpdate: Boolean = false): T? {

    val table = Database.tableFor(T::class.java)
    val qb = table.queryBuilder(this.connection)

    if (eagerLoad) qb.selectEager() else qb.select()

    qb.eq("${table.name}.id", id)
            .limit(1)

    if (forUpdate) {
        qb.forUpdate()
    }
    return qb.fetchOne()
}


inline fun <reified T : Entity> ConnectionAware.findAll(eagerLoad: Boolean = false): Iterable<T> {
    val table = Database.tableFor(T::class.java)
    val qb = table.queryBuilder(this.connection)
    if (eagerLoad) qb.selectEager() else qb.select()
    qb.orderBy("id")
    return qb.fetchMultiple()
}

inline fun <reified T : Entity> ConnectionAware.findAll(limit: Int): Iterable<T> {
    return Database.tableFor(T::class.java)
            .queryBuilder(this.connection)
            .select()
            .limit(limit)
            .fetchMultiple()
}

inline fun <reified K : Entity> ConnectionAware.retrieve(lazy: Lazy<K>): K? {
    return lazy.get(this.connection)
}

fun ConnectionAware.run(sql: Sql): Boolean = this.connection.createStatement().use {
    log.debug("SQL: $sql")
    return it.execute(sql)
}

fun <T> ConnectionAware.runQuery(sql: Sql, receiver: (it: ResultSet) -> T): T = this.connection.createStatement().use {
    log.debug("SQL: $sql")
    return receiver(it.executeQuery(sql))
}

fun ConnectionAware.rollback() = this.connection.rollback()
