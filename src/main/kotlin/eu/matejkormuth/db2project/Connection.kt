package eu.matejkormuth.db2project

import java.sql.Connection
import java.sql.ResultSet

/**
 * Class that brings context (connection) to scope
 */
class ConnectionAware(val connection: Connection) {
    val log by logger()
}

/**
 * Creates database transaction and provides block with connection to perform database operations.
 */
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

/**
 * Returns instance of query builder.
 */
inline fun <reified T : Entity> ConnectionAware.queryBuilder(): QueryBuilder<T> = Database.tableFor(T::class.java).queryBuilder(this.connection)

/**
 * Inserts specified entity to database.
 */
inline fun <reified T : Entity> ConnectionAware.insertOne(entity: T): Lazy<T> {
    return Database.tableFor(T::class.java)
            .queryBuilder(this.connection)
            .insertOne(entity)
}

/**
 * Inserts multiple entities to database with specified amount of max rows at once.
 */
inline fun <reified T : Entity> ConnectionAware.insertMultiple(entities: Iterable<T>, maxRowsAtOnce: Int = 10000) {
    entities.chunked(maxRowsAtOnce).forEach { chunk ->
        val qb = Database.tableFor(T::class.java)
                .queryBuilder(this.connection)
        chunk.forEach { qb.insertMultiple(it) }
        qb.execute()
    }
}

/**
 * Updates specified entity in database.
 */
inline fun <reified T : Entity> ConnectionAware.updateOne(entity: T) {
    Database.tableFor(T::class.java)
            .queryBuilder(this.connection)
            .updateOne(entity)
}

/**
 * Deletes specified entity from database.
 */
inline fun <reified T : Entity> ConnectionAware.delete(id: Id): Boolean {
    return Database.tableFor(T::class.java)
            .queryBuilder(this.connection)
            .delete()
            .eq("id", id)
            .execute()
}

/**
 * Commits the current transaction.
 */
fun ConnectionAware.commit() = this.connection.commit()

/**
 * Finds one referenced row of specified entity.
 *
 * @param id id of referenced entity
 * @param columnName referencing column name
 * @param eagerLoad whether eager load all referenced entities
 */
inline fun <reified T : Entity> ConnectionAware.findReferenced(id: Id, columnName: String, eagerLoad: Boolean = false): T? {
    val table = Database.tableFor(T::class.java)
    val qb = table.queryBuilder(this.connection)
    if (eagerLoad) qb.selectEager() else qb.select()
    qb.eq(columnName, id)
    return qb.fetchOne()
}

/**
 * Finds all referenced rows of specified entity.
 *
 * @param id id of referenced entity
 * @param columnName referencing column name
 * @param eagerLoad whether eager load all referenced entities
 */
inline fun <reified T : Entity> ConnectionAware.findAllReferenced(id: Id, columnName: String, eagerLoad: Boolean = false): Iterable<T> {
    val table = Database.tableFor(T::class.java)
    val qb = table.queryBuilder(this.connection)
    if (eagerLoad) qb.selectEager() else qb.select()
    qb.eq(columnName, id)
    return qb.fetchMultiple()
}

/**
 * Finds one rows of specified entity and returns query builder.
 *
 * @param eagerLoad whether eager load all referenced entities
 */
inline fun <reified T : Entity> ConnectionAware.find(eagerLoad: Boolean = false): QueryBuilder<T> {
    val table = Database.tableFor(T::class.java)
    val qb = table.queryBuilder(this.connection)
    if (eagerLoad) qb.selectEager() else qb.select()
    return qb
}

/**
 * Finds one rows of specified entity by id.
 *
 * @param id id
 * @param eagerLoad whether eager load all referenced entities
 */
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

/**
 * Finds all rows of specified entity by id.
 *
 * @param eagerLoad whether eager load all referenced entities
 */
inline fun <reified T : Entity> ConnectionAware.findAll(eagerLoad: Boolean = false): Iterable<T> {
    val table = Database.tableFor(T::class.java)
    val qb = table.queryBuilder(this.connection)
    if (eagerLoad) qb.selectEager() else qb.select()
    qb.orderBy("id")
    return qb.fetchMultiple()
}

/**
 * Retrieves specified (unresolved) lazy.
 */
inline fun <reified K : Entity> ConnectionAware.retrieve(lazy: Lazy<K>): K? {
    return lazy.get(this.connection)
}

/**
 * Runs specified raw sql.
 */
fun ConnectionAware.run(sql: Sql): Boolean = this.connection.createStatement().use {
    log.debug("SQL: $sql")
    return it.execute(sql)
}

/**
 * Runs specified raw sql query and results result set to provided closure.
 */
fun <T> ConnectionAware.runQuery(sql: Sql, receiver: (it: ResultSet) -> T): T = this.connection.createStatement().use {
    log.debug("SQL: $sql")
    return receiver(it.executeQuery(sql))
}

/**
 * Runs specified raw sql update.
 */
fun ConnectionAware.runUpdate(sql: Sql): Int = this.connection.createStatement().use {
    log.debug("SQL: $sql")
    return it.executeUpdate(sql)
}

/**
 * Rollbacks current transaction.
 */
fun ConnectionAware.rollback() = this.connection.rollback()
