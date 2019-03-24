package eu.matejkormuth.db2project

import eu.matejkormuth.db2project.models.*
import java.sql.Connection
import java.sql.ResultSet

/* class that brings context (connection) to scope */
class ConnectionAware(val connection: Connection) {

    /* all models available in connection aware block */
    val AssignedEmployees by lazy { boundRepository<AssignedEmployee>(connection) }
    val Cases by lazy { boundRepository<Case>(connection) }
    val Categories by lazy { boundRepository<Category>(connection) }
    val CityDistricts by lazy { boundRepository<CityDistrict>(connection) }
    val Connections by lazy { boundRepository<eu.matejkormuth.db2project.models.Connection>(connection) }
    val CrimeScenes by lazy { boundRepository<CrimeScene>(connection) }
    val Departments by lazy { boundRepository<Department>(connection) }
    val Employees by lazy { boundRepository<Employee>(connection) }
    val People by lazy { boundRepository<Person>(connection) }
    val Punishments by lazy { boundRepository<Punishment>(connection) }
}

inline fun <T> transaction(block: ConnectionAware.() -> T): T {
    val connection = Database.getConnection()
    val connectionAware = ConnectionAware(connection)
    connection.autoCommit = false
    try {
        val result = connectionAware.block()
        connection.commit()
        return result
    } catch (e: Exception) {
        connection.rollback()
        throw e
    } finally {
        connection.close()
    }
}

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

inline fun <reified T : Entity> T.save() {

}

inline fun <reified T : Entity> ConnectionAware.delete(id: Id): Boolean {
    return Database.tableFor(T::class.java)
            .queryBuilder(this.connection)
            .delete()
            .eq("id", id)
            .execute()
}

inline fun <reified T : Entity> ConnectionAware.findOne(id: Id, eagerLoad: Boolean = false): T? {
    val table = Database.tableFor(T::class.java)
    val qb = table.queryBuilder(this.connection)

    if (eagerLoad) qb.selectEager() else qb.select()

    return qb.eq("${table.name}.id", id)
            .limit(1)
            .fetchOne()
}

inline fun <reified T : Entity> ConnectionAware.findAll(): Iterable<T> {
    return Database.tableFor(T::class.java)
            .queryBuilder(this.connection)
            .select()
            .fetchMultiple()
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
    println("SQL: $sql")
    return it.execute(sql)
}

fun ConnectionAware.runQuery(sql: Sql): ResultSet = this.connection.createStatement().use {
    return it.executeQuery(sql)
}
